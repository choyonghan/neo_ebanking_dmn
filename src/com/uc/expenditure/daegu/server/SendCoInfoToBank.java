/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출  공통코드 파일 송신처리(회계구분정보, 부서정보,  분야부문, 세출통계목, 세입목코드, 세입목코드매핑, 금고은행시스템출금계좌검증정보송신))
 *  기  능  명 : 공통코드 SAM파일전송
 *
 *  클래스  ID : SendCoInfoToBank
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 */
package com.uc.expenditure.daegu.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.nio.handlers.Connector;
import com.uc.framework.nio.handlers.ConnectorListener;
import com.uc.framework.nio.handlers.PacketChannel;
import com.uc.framework.nio.handlers.PacketChannelListener;
import com.uc.framework.nio.handlers.TimerListener;
import com.uc.framework.nio.io.EventType;
import com.uc.framework.nio.io.SelectorThread;
import com.uc.framework.nio.io.ServerDataEventAsMap;
import com.uc.framework.nio.io.TimerThread3;
import com.uc.framework.parsing.DaemonListParser;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.parsing.ServiceListParser;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class SendCoInfoToBank implements ConnectorListener, PacketChannelListener, TimerListener
{
  /** A single selector for all clients */
  private static SelectorThread st;

  /** How many connections to created */
  private static int connectionCount;
  /** How many connections were opened so far */
  private static int connectionsEstablished = 0;
  private static int connectionsFailed = 0;
  /** How many connections were disconnected so far */
  private static int connectionsClosed = 0;
  /**
   * Keeps a list of connections that have been established but
   * not yet started.
   */
  private static List<PacketChannel> establishedConnections = new ArrayList<PacketChannel>(512);

  /** How many packets each instance sent so far. */
  private static int packetsSent = 0;

  private static Logger	logger = Logger.getLogger(SendCoInfoToBank.class);
  private static List<ServerDataEventAsMap>	queue	 = new LinkedList<ServerDataEventAsMap>();
  private static InetSocketAddress	remotePoint = null;
	private	ServiceListParser	slp = null;
	private	DaemonListParser	dlp = null;
	private static	FormatParserAsMap	fp = null;
	SqlSessionFactory sqlMapper  = null;
	private	SqlSession	session = null;
	private	SqlSession	session2 = null;
	private ClientState	ssCurrent = ClientState.STOPPED;
	  Connector connector = null;
      private	ServerProtocol	protocol = null;

      private FileWriter fw = null;
      RandomAccessFile    inFile = null;

      int    lBlockNo = 0;
      int    lLastSeqNo = 0;
      int    lMaxSeqNo = 100;
      int    lDataSize = 4096;
      int   lFileSize = 0;
      boolean   bDone = false;

      // 결번
      LinkedHashMap<Integer, Boolean>    lhmSentSeq = new  LinkedHashMap<Integer, Boolean>();
      LinkedHashMap<Integer, Boolean>    lhmMissingSeq = new  LinkedHashMap<Integer, Boolean>();
      static MyMap  appRes = new MyMap();

  /**
   * Initiates a non-blocking connection attempt to the given address.
   *
   * @param remotePoint Where to try to connect.
   * @throws Exception
   */
  public SendCoInfoToBank(InetSocketAddress remotePoint) throws Exception
  {
	  parse(logger);
	  this.remotePoint = remotePoint;
	  protocol = new ServerProtocol(logger, fp);
//	  connector = new Connector(st, remotePoint, this);
//    connector.connect();
//   logger.debug("["+ connector + "] 접속을 합니다...");
  }

  public SendCoInfoToBank(Logger logger, String address, int port) throws Exception
  {
	  this.logger = logger;
	  parse(logger);
	  this.remotePoint = new InetSocketAddress(address, port);
	  protocol = new ServerProtocol(logger, fp);
//	  connector = new Connector(st, remotePoint, this);
//    connector.connect();
//   logger.debug("["+ connector + "] 접속을 합니다...");
  }
	public void parse(Logger logger)
	{
		// 전문형식정의 파일을 읽고 분석한다.
		fp = new FormatParserAsMap(logger);
		fp.doParsingAsMap("msgformat4");

		// myBatis 환경설정 파일을 읽고 필요한 환경을 만든다..
		Reader	reader = null;
		try {

			reader = Resources.getResourceAsReader( "res/Configuration.xml" );
			Properties properties = Resources.getResourceAsProperties( "res/db.properties" );

			String pw = Utils.getDecrypt( properties.getProperty("password"));
			properties.setProperty("password", pw);
			sqlMapper = new SqlSessionFactoryBuilder().build( reader , properties);

		} catch (Exception ex) {
			logger.error("오류가 발생했습니다" + ex.getLocalizedMessage());
			return;
		}

	}

  //////////////////////////////////////////
  // Implementation of the callbacks from the
  // Acceptor and PacketChannel classes
  //////////////////////////////////////////
  /**
   * A new client connected. Creates a PacketChannel to handle it.
   */
	@Override
  public void connectionEstablished(Connector connector, SocketChannel sc)
  {
	  logger.debug("connectionEstablished called from [" + Thread.currentThread().getName() + "]");
    try {
      // We should reduce the size of the TCP buffers or else we will
      // easily run out of memory when accepting several thousands of
      // connctions
      sc.socket().setReceiveBufferSize(2*1024);
      sc.socket().setSendBufferSize(2*1024);
      // The contructor enables reading automatically.
      PacketChannel pc = new PacketChannel(
    		  logger,
          sc,
          st,
          protocol,
          this);

      // Do not start sending packets right away. Waits for all sockets
      // to connect. Otherwise, the load created by sending and receiving
      // packets will increase dramatically the time taken for all
      // connections to be established. It is better to establish all
      // connections and only then to start sending packets.
      establishedConnections.add(pc);
      connectionsEstablished++;
      // If if all connections are established.
      synchronized(queue) {
//      	queue.add(new ServerDataEventAsMap(EventType.CONNECTED, Thread.currentThread(), (Object)pc, sc, null, 0, null));
      	queue.add(new ServerDataEventAsMap(EventType.CONNECTED, Thread.currentThread(), pc, sc, null, 0, null));
      	queue.notify();
      }
//      checkAllConnected();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
	@Override
  public void connectionFailed(Connector connector, Exception cause)
  {
    logger.debug("["+ connector + "] Error: " + cause.getMessage());
      synchronized(queue) {
        queue.add(new ServerDataEventAsMap(EventType.CONNECTIONFAILED, null, null, null, null, 0, null));
        queue.notify();
      }
//    connectionsFailed++;
//    checkAllConnected();
  }
	@Override
  public void packetArrived(PacketChannel pc, ByteBuffer pckt)
  {
	  MyMap	msg = new MyMap();
	  if (fp.disassembleMessageByteAsMap(pckt.array(), pckt.remaining(), msg, "04") == -1) {
		  logger.error("paring error");
	  }

	  synchronized(queue) {
//        	queue.add(new ServerDataEventAsMap(EventType.PACKETARRIVED, Thread.currentThread(), (Object)pc, null, pckt.array(), pckt.remaining(), msg));
        	queue.add(new ServerDataEventAsMap(EventType.PACKETARRIVED, Thread.currentThread(), pc, null, pckt.array(), pckt.remaining(), msg));
        	queue.notify();
        }
  }
	@Override
  public void socketException(PacketChannel pc, Exception ex)
  {
    logger.debug("[" + pc.toString() + "] Error: " + ex.getMessage());
    connectionClosed();
  }
	@Override
  public void socketDisconnected(PacketChannel pc)
  {
    synchronized(queue) {
//      	queue.add(new ServerDataEventAsMap(EventType.DISCONNECTED, null, (Object)pc, null, null, 0, null));
      	queue.add(new ServerDataEventAsMap(EventType.DISCONNECTED, null, pc, null, null, 0, null));
      	queue.notify();
      }
//    connectionClosed();
  }

  /**
   * The request was sent. Prepare to read the answer.
   */
	@Override
  public void packetSent(PacketChannel pc, ByteBuffer pckt)
  {
//    logger.debug("[" + pc.toString() + "] Packet sent.");
    synchronized(queue) {
//      	queue.add(new ServerDataEventAsMap(EventType.PACKETSENT, null, (Object)pc, null, null, 0, null));
      	queue.add(new ServerDataEventAsMap(EventType.PACKETSENT, null, pc, null, null, 0, null));
      	queue.notify();
      }
/*
     try {
       pc.resumeReading();
    } catch (Exception e) {
      e.printStackTrace();
    }
 */
}

  ////////////////////////////
  // Helper methods
  ////////////////////////////
  /**
   * Called when a connection is closed. Checks if all connections
   * have been closed and if so exits the virtual machine.
   */
  private void connectionClosed()
  {
      st.requestClose();
      synchronized(queue) {
        	queue.add(new ServerDataEventAsMap(EventType.DISCONNECTED, null, null, null, null, 0, null));
        	queue.notify();
      }
  }
  @Override
  public void timerExpired(Object pc)
  {
    synchronized(queue) {
    	queue.add(new ServerDataEventAsMap(EventType.TIMEREXPIRED, null, pc, null, null, 0, null));
    	queue.notify();
    }
  }

  /* process */
  public void process()
  {
	    Connector connector = new Connector(logger, st, remotePoint, this);
	    ssCurrent = ClientState.OFFLINE;

	    TimerThread3	tt = TimerThread3.getInstance();
	    Object	t = null;
        t = tt.setTimer(1000, this, null);

	    int	dataCount = 1;
	    boolean    isConnected = false;

	    ArrayList<MyMap> alLinkList = new ArrayList<MyMap>();

	    String  folder = appRes.getString("SendColInfoToBank.sendDirectory");
	    String sysDvCd = appRes.getString("sysDvCd");
	    logger.info("##### send to bank sysDvCd ["+ sysDvCd +"]");

	    ServerDataEventAsMap	dataEvent;
	    while (true) {
			 synchronized(queue) {
			     logger.info(ssCurrent + " : " + queue);
				 while (queue.isEmpty()) {
					 try {
//						 long beforeTime = System.currentTimeMillis();

                     	 logger.info("#wait_before#");
//                         queue.wait(1800000);
                         queue.wait();
                         logger.info("#wait_after#");

//                         long afterTime = System.currentTimeMillis();
//              			 long secDiffTime = (afterTime - beforeTime) / 1000;
//
//              		     if(secDiffTime >= 1700) {
//              				logger.info("#wait_after_limit#");
//              				 isConnected = false;
//                              ssCurrent = ClientState.OFFLINE;
//                              t = tt.setTimer(30000, this, null);
//              			 }
					 } catch (InterruptedException ie) {
					     logger.debug("=== queue 발생 === ");
                         logger.error("[오류발생]"+ie.getLocalizedMessage());
					 }
				 }
			 	 dataEvent = queue.remove(0);
			 }

             PacketChannel  pc = (PacketChannel)dataEvent.handle;

             logger.info(ssCurrent + " : "  + dataEvent.type + " : " + pc);

			 switch (ssCurrent) {
			 case OFFLINE:
			 	if (dataEvent.type == EventType.TIMEREXPIRED) {
	                try {
	                    inFile = null;
	                    alLinkList.clear();
	                    session = sqlMapper.openSession(false);// AUTOCOMMIT is false

	                    MyMap param = new MyMap();
	                    param.setString("SYS_DV_CD", sysDvCd);
	                    logger.info("공통코드정보 조회를 시작합니다.");
	                    alLinkList = (ArrayList<MyMap>)session.selectList("NeoMapperFile.getVerifyCoList", param);
	                    session.close();

	                    // 조회결과 표시
	                    logger.info("공통정보  [" + alLinkList.size() + "]건을 조회했습니다");

	                } catch (Exception ex) {
	                    // 반환예외가 SQLException인지 검사
                        logger.error("[오류] "+ ex);
                        try{
                            session.rollback();
                        }catch(Exception e){
                            logger.error("[오류] "+e);
                        }
                        System.exit(0);
	                }
                    if (alLinkList.size() > 0) {
                    	//
                    	 try {
                    		 try{
                    			 if(session2 != null ) {
                    				 session2.rollback();
                    				 session2.close();
                    			 }
                    		 }catch (Exception e) {
                    			 logger.info("TIMEREXPIRED session2 error  : " + session2);
							 }

                    		 session2 = sqlMapper.openSession(false);// AUTOCOMMIT is false
                    		 session2.selectList("NeoMapperFile.getJobCheck");
                             MyMap tmpMap = new MyMap();
                             tmpMap.setMap("CHK_ID", "0000");
                             tmpMap.setMap("JOB_ID", "204");
                             session2.insert("NeoMapperFile.insertJobCheck", tmpMap);
                         } catch (Exception e) {
                             try {
                                 session2.rollback();
                                 session2.close();
                                 logger.info(e);
                                 logger.info("대기중!!!!!!");
                                 isConnected = false;
                                 ssCurrent = ClientState.OFFLINE;
                                 t = tt.setTimer(30000, this, null);
                                 break;
                             } catch (Exception e1) {
                                 isConnected = false;
                                 ssCurrent = ClientState.OFFLINE;
                                 t = tt.setTimer(30000, this, null);
                                 break;
                             }
                         }


			 	        if (!isConnected) {
    				        try {
    						    logger.debug("서버에 접속을 시작합니다");
    						    connector.connect();
    						    ssCurrent = ClientState.STOPPED;
    					    } catch (IOException e1) {
    					    	//
    					    	session2.rollback();
                                session2.close();
    					        logger.error("=========[connector : "+connector+"]==========");
    					        logger.error("=========[pc : "+pc+"]==========");
    					        System.exit(0);
    					    }
                        } else {
                            logger.error( "================EDN!!!!!==================" );

                            isConnected = false;
                            ssCurrent = ClientState.OFFLINE;
                            t = tt.setTimer(30000, this, null);
                        }
	                } else {

	                    ssCurrent = ClientState.OFFLINE;
	                    t = tt.setTimer(30000, this, null);
			 	    }
				}
			 	break;
			 case STOPPED:
			 	if (dataEvent.type == EventType.CONNECTED) {

			 		logger.info("@=@= Neo server 접속완료  "  );
			 	    // 서버에 접속되었다.
					 logger.info("서버[" + pc.getSocketChannel().socket().getInetAddress().getHostAddress() +
							 ":" + pc.getSocketChannel().socket().getPort() + "] 접속완료");

					 // 다음단계로 진행
                    isConnected = true;
					ssCurrent = ClientState.STARTING;

	                t = tt.setTimer(1000, this, pc);
                } else if (dataEvent.type == EventType.CONNECTIONFAILED) {
                    // 서버에 접속하지 못했다. 30초후에 접속을 시도한다.
                    logger.debug("서버접속실패");
                    isConnected = false;
                    ssCurrent = ClientState.OFFLINE;
                    t = tt.setTimer(30000, this, null);
				} else if (dataEvent.type == EventType.DISCONNECTED) {	// 서버 종료
					logger.debug("서버가 접속을 종료했습니다");
					pc.close();
                    isConnected = false;
                    ssCurrent = ClientState.OFFLINE;
                    t = tt.setTimer(30000, this, null);
				}
				 break;
			 case STARTING:
				 logger.info("@=@= 2. STARTING   "  );
	                if (dataEvent.type == EventType.TIMEREXPIRED) {
	                    if (isConnected) {
	                        // 송신대상 조회한다.

	                        if (alLinkList.size() > 0) {
				 			    logger.debug("=== 파일송신업무개시 === ");

	                            // 업무개시요구 송신
				 			    MyMap    shead = new MyMap();
                                String  dtm = Utils.getCurrentDate().substring(4,8)+Utils.getCurrentTime();
                                shead.setString("TrxDtm", dtm);        // 전송일시
                                shead.setString("JobMngInfo", "001");
                                shead.setBytes("SenderNm", "");
                                shead.setString("SenderPw", "");
                                try {
                                	Object[]	args = new Object[2];
                                	args[0] = "0600";
                                	args[1] = alLinkList.get(0).getString("TRANS_NM");			//DGCITY**
                                    ByteBuffer buffer = protocol.encode(shead, args, 2);
                                    ssCurrent = ClientState.STARTED;
                                    pc.sendPacket(buffer);
                                } catch (Exception ex) {
                                    if( pc != null ){
                                        pc.close();
                                    }else{
//                                      logger.error("=========isConnected "+isConnected+"========");
//                                      this.connectionClosed();
//                                      logger.error("=========강제 소켓 종료=========");
//                                        isConnected = false;
                                    }
                                    logger.error( "================[PacketChannel close  ]==================" );
                                    ssCurrent = ClientState.OFFLINE;
                                    logger.error( "================[ClientState OFFLINE  ]==================" );
                                    t = tt.setTimer(30000, this, null);
                                    logger.error( "================[AFTER 30 SEC ==> OFFLINE  ]==================" );

//                                  pc.close();
//	                                ssCurrent = ClientState.OFFLINE;
//	                                t = tt.setTimer(30000, this, null);
                                }
	                        } else {
	                            ssCurrent = ClientState.STARTING;

	                            t = tt.setTimer(30000, this, pc);
	                        }
	                    } else {
	                        logger.error( "================EDN!!!!!==================" );
	                        // 아직 접속되지 않음.
	                        if( pc != null ){
                                pc.close();
                            }
	                        isConnected = false;
	                        ssCurrent = ClientState.OFFLINE;
	                        t = tt.setTimer(30000, this, null);
	                    }
	                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
	                   //
	                   session2.commit();
	                   session2.close();
	                   if( pc != null ){
                           pc.close();
                       }else {
                           logger.error("PC = null");
                       }

	                   isConnected = false;
	                   ssCurrent = ClientState.OFFLINE;
	                   t = tt.setTimer(30000, this, null);

	                   logger.info("서버가 접속을 종료했습니다");
	                } else {
	                    logger.error( "================EDN!!!!!==================" );
                        // 아직 접속되지 않음.
                        if( pc != null ){
                            pc.close();
                        }
                        isConnected = false;
                        ssCurrent = ClientState.OFFLINE;
                        t = tt.setTimer(30000, this, null);
	                }
	                break;
/*

			 	if (dataEvent.type == EventType.PACKETARRIVED) {	// 서버 접속
	                 String  key = new String((byte[])dataEvent.msg.get("MsgDsc"));
	                 if (key.equals("0600")) {
	                     logger.debug("업무개시요구 수신");

	                     LinkedHashMap<String,Object>    shead = new LinkedHashMap<String,Object>();
	                     shead.setMap("TrxDtm", Utils.getCurrentDate());        // 전송일시
	                     shead.setMap("JobMngInfo", dataEvent.msg.get("JobMngInfo"));   // 업무관리정보
	                     shead.setMap("SenderNm", dataEvent.msg.get("SenderNm"));       // 송신자명
	                     shead.setMap("SenderPw", dataEvent.msg.get("SenderPw"));       // 송신자암호

	                     try {
	                         ByteBuffer buffer = protocol.encode(shead, "DGCITY05", "0610");

	                         ssCurrent = ClientState.STARTED;
	                         pc.sendPacket(buffer);
	                     } catch (Exception ex) {
	                         logger.error("전문을 송신하지 못했습니다. 접속을 종료합니다.");
	                         pc.close();
	                     }
	                 }
				 break;
*/
	         case STARTED:
			 	if (dataEvent.type == EventType.PACKETSENT) {
					logger.debug("업무개시요구 송신");
					try {
                        ssCurrent = ClientState.CREATING;
                        pc.resumeReading();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (dataEvent.type == EventType.TIMEREXPIRED) {
				} else if (dataEvent.type == EventType.DISCONNECTED) {	// 클라이언트 종료
				    logger.debug("서버가 접속을 종료했습니다");
				    pc.close();
				    isConnected = false;
					ssCurrent = ClientState.OFFLINE;
				}
				 break;
			 case CREATING:
                if (dataEvent.type == EventType.PACKETARRIVED) {    // 데이터 수신
                    String  key = dataEvent.msg.getString("MsgDsc");
                    if (key.equals("0610")) {

                        logger.debug("업무개시통보 수신");

                        LinkedHashMap   oneRow = alLinkList.get(0);

                        String  fileName = null;
                        String verifyType = null;
                        if (oneRow != null) {
                            fileName = oneRow.get("TRNX_NO").toString();
                            verifyType = oneRow.get("VERIFY_TYPE").toString();

                            logger.debug("거래번호[" + fileName + "]");
                        }

                        try {

                            inFile = new RandomAccessFile(folder + "/dees_com/send" + "/" + fileName, "r");
                        } catch (IOException e) {
                            logger.error("=========[pc : "+pc+"]==========");
                            if( pc != null ){
                                pc.close();
                            }

                            logger.error( "================[PacketChannel close  ]==================" );
                            ssCurrent = ClientState.OFFLINE;
                            logger.error( "================[ClientState OFFLINE  ]==================" );
                            t = tt.setTimer(30000, this, null);
                            logger.error( "================[AFTER 30 SEC ==> OFFLINE  ]==================" );
                        }   //파일지정

                        try {
                            lFileSize = (int)inFile.length();
                            logger.info("파일이름[" + fileName + "], 파일길이[" + lFileSize + "]");
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        String sentfilename = oneRow.get("TRANS_NM").toString();

                        // 파일정보 수신 요구
                        MyMap    shead = new MyMap();
                        shead.setString("FileName", "sentfilename");								//DGCITY**

                        shead.setLong("FileSize", lFileSize);
                        shead.setLong("MsgSize", lDataSize);

                        try {
                        	Object[]	args = new Object[2];
                        	args[0] = "0630";
                        	args[1] = sentfilename;							//DGCITY**
                            ByteBuffer buffer = protocol.encode(shead, args, 2);
                            ssCurrent = ClientState.CREATED;
                            pc.sendPacket(buffer);
                        } catch (Exception ex) {
                            logger.error("send fail");
                        }
                    }
/*
                }
 			 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 송신 완료

					logger.debug("파일정보수신요구 송신");
					ssCurrent = ClientState.CREATED;
				    try {
				        pc.resumeReading();
				      } catch (Exception e) {
				        e.printStackTrace();
				      }
*/			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 재송신
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
               }
				 break;
			 case CREATED:
                if (dataEvent.type == EventType.PACKETSENT) {    // 데이터 수신
                    logger.debug("파일정보송신");
                    ssCurrent = ClientState.CREATED2;
                    try {
                        pc.resumeReading();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
			 case CREATED2:
			 	if (dataEvent.type == EventType.PACKETARRIVED) {	// 데이터 수신
                    String  key = dataEvent.msg.getString("MsgDsc");
                    if (key.equals("0640")) {
			 			logger.debug("파일정보수신 보고");
//						파일생성 및 초기화

                        // 초기화
                        for (int i = 1; i <= lMaxSeqNo; i++) {
                            lhmSentSeq.put(i, false);
                            lhmMissingSeq.put(i, false);
                        }

                        bDone = false;

                        lBlockNo = 1;
                        lLastSeqNo = 1;
                        long    pos = (lBlockNo-1) * lMaxSeqNo * lDataSize + (lLastSeqNo-1) * lDataSize;

                        byte[]  readByte = new byte[lDataSize];
                        long    readSize = 0;
                        try {
                            inFile.seek(pos);
                            readSize = inFile.read(readByte,0, lDataSize);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if (readSize < lDataSize) {
                            // 완료
                            logger.debug("마지막 블록");
                            bDone = true;
                        }

                        // 데이터 송신
                        MyMap	shead = new MyMap();
					    shead.setLong("BlockNo", lBlockNo);
					    shead.setLong("SeqNo", lLastSeqNo);
					    shead.setLong("DataSize", readSize);
                        byte[]  readByte2 = new byte[(int)readSize];
                        System.arraycopy(readByte, 0, readByte2, 0, (int)readSize);
					    shead.setBytes("Data", readByte2);

					    LinkedHashMap   oneRow = alLinkList.get(0);
					    String sentfilename = oneRow.get("TRANS_NM").toString();

					    try {
                        	Object[]	args = new Object[2];
                        	args[0] = "0320";
                        	args[1] = sentfilename;				//DGCITY**
                            ByteBuffer buffer = protocol.encode(shead, args, 2);
					    	ssCurrent = ClientState.CHECKING;
					    	pc.sendPacket(buffer);
					    } catch (Exception ex) {
					    	logger.error("send fail");
					    }
			 		}
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 오류...
			 	}
				 break;
			 case CHECKING:
				 logger.debug("CHECKING...");
			 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 송신 완료
					lhmSentSeq.put(lLastSeqNo, true);

					logger.debug("데이터 송신 lLastSeqNo[" + lLastSeqNo + "], lMaxSeqNo[" + lMaxSeqNo + "], bDone[" + bDone + "]");

					if (lLastSeqNo >= lMaxSeqNo || bDone == true) {
				 		// 송신이 완료되었으면, 결번확인 요청을 한다.
						logger.debug("송신이 완료되어, 결번확인 요청을 한다");
						MyMap	shead = new MyMap();
					    shead.setLong("BlockNo", lBlockNo);
					    shead.setLong("LastSeqNo", lLastSeqNo);

					    LinkedHashMap   oneRow = alLinkList.get(0);
					    String sentfilename = oneRow.get("TRANS_NM").toString();


					    try {
                        	Object[]	args = new Object[2];
                        	args[0] = "0620";
                        	args[1] = sentfilename;									//DGCITY**
                            ByteBuffer buffer = protocol.encode(shead, args, 2);
					    	ssCurrent = ClientState.CHECKED;
					    	pc.sendPacket(buffer);
					    } catch (Exception ex) {
					    	logger.error("send fail");
					    }
					} else {
			 			// 데이터 송신
						logger.debug("[" + lLastSeqNo + "]번째 데이터 송신");

						lLastSeqNo++;   // 송신횟수 증가시킴. 테스트.

                        long    pos = (lBlockNo-1) * lMaxSeqNo * lDataSize + (lLastSeqNo-1) * lDataSize;

                        byte[]  readByte = new byte[lDataSize];
                        long    readSize = 0;
                        try {
                            inFile.seek(pos);
                            readSize = inFile.read(readByte,0, lDataSize);
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
//                        Utils.dumpAsHex(logger,  readByte, "파일에서 읽는 데이터", (int)readSize);

                        if (readSize < lDataSize) {
                            // 완료
                            logger.debug("마지막 블록");
                            bDone = true;
                        }

                        MyMap	shead = new MyMap();
					    shead.setLong("BlockNo", lBlockNo);
					    shead.setLong("SeqNo", lLastSeqNo);
					    shead.setLong("DataSize", readSize);
                        byte[]  readByte2 = new byte[(int)readSize];
                        System.arraycopy(readByte, 0, readByte2, 0, (int)readSize);
					    shead.setBytes("Data", readByte2);

					    LinkedHashMap   oneRow = alLinkList.get(0);
					    String sentfilename = oneRow.get("TRANS_NM").toString();

					    try {
                        	Object[]	args = new Object[2];
                        	args[0] = "0320";
                        	args[1] = sentfilename;				//DGCITY**
                            ByteBuffer buffer = protocol.encode(shead, args, 2);
						    ssCurrent = ClientState.CHECKING;
						    pc.sendPacket(buffer);
                            logger.debug("전문송신, 일련번호[" + lLastSeqNo + "]");
					    } catch (Exception ex) {
					    	logger.error("send fail");
					    }
					}
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 재송신...
			 	}
			 	break;
			 case CHECKED:
			 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 수신
					logger.debug("결번확인 요구 송신");
				    ssCurrent = ClientState.COMPLETING;
				    try {
				        pc.resumeReading();
				      } catch (Exception e) {
				        e.printStackTrace();
				      }
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 오류...
			 	}
			 	break;
			 case COMPLETING:
			 	if (dataEvent.type == EventType.PACKETARRIVED) {	// 데이터 송신 완료
                    String  key = dataEvent.msg.getString("MsgDsc");
                    if (key.equals("0300")) {
			 			logger.debug("결번확인통보 수신");
			 			long	lostCnt = dataEvent.msg.getLong("LostCnt");
			 			if (lostCnt == 0) {
			 				logger.debug("결번이 없습니다");

			 				if (bDone) {
		                    // 파일송신 완료요구를 송신한다.
			 					MyMap    shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8)+Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm);        // 전송일시
		                    shead.setString("JobMngInfo", "003");
		                    shead.setBytes("SenderNm", "");
		                    shead.setString("SenderPw", "");

						    LinkedHashMap   oneRow = alLinkList.get(0);
						    String sentfilename = oneRow.get("TRANS_NM").toString();

		                    try {
	                        	Object[]	args = new Object[2];
	                        	args[0] = "0600";
	                        	args[1] = sentfilename;								//DGCITY**
	                            ByteBuffer buffer = protocol.encode(shead, args, 2);
		                        ssCurrent = ClientState.STOPPING;
		                        pc.sendPacket(buffer);
		                    } catch (Exception ex) {
		                        logger.error("send fail");
		                    }
			 				} else {
			 				    if (lLastSeqNo == lMaxSeqNo) {
			 				        lBlockNo++;
			 				        lLastSeqNo = 1;
			 				    }

		                        long    pos = (lBlockNo-1) * lMaxSeqNo * lDataSize + (lLastSeqNo-1) * lDataSize;

		                        byte[]  readByte = new byte[lDataSize];
		                        long  readSize = 0;
		                        try {
		                            inFile.seek(pos);
		                            readSize = inFile.read(readByte,0, lDataSize);
		                        } catch (IOException e) {
		                            // TODO Auto-generated catch block
		                            e.printStackTrace();
		                        }

		                        if (readSize < lDataSize) {
		                            // 완료
		                            logger.debug("마지막 블록");
		                            bDone = true;
		                        }

		                        // 데이터 송신
		                        MyMap    shead = new MyMap();
		                        shead.setLong("BlockNo", lBlockNo);
		                        shead.setLong("SeqNo", lLastSeqNo);
		                        shead.setLong("DataSize", readSize);
                                byte[]  readByte2 = new byte[(int)readSize];
                                System.arraycopy(readByte, 0, readByte2, 0, (int)readSize);
					            shead.setBytes("Data", readByte2);

							    LinkedHashMap   oneRow = alLinkList.get(0);
							    String sentfilename = oneRow.get("TRANS_NM").toString();

		                        try {
		                        	Object[]	args = new Object[2];
		                        	args[0] = "0320";
		                        	args[1] = sentfilename;								//DGCITY**
		                            ByteBuffer buffer = protocol.encode(shead, args, 2);
		                            ssCurrent = ClientState.CHECKING;
		                            pc.sendPacket(buffer);
		                        } catch (Exception ex) {
		                            logger.error("send fail");
		                        }
			 				}
			 			} else {
                            long BlockNo = dataEvent.msg.getLong("BlockNo");
                            long SeqNo = dataEvent.msg.getLong("LastSeqNo");
                            byte[]  bMissing = new byte[lMaxSeqNo];
                            bMissing = dataEvent.msg.getBytes("LostSeq");

                            logger.debug("결번이 [" + lostCnt + "]건 있습니다. 결번확인[" + new String(bMissing) + "]");
                            int seq = 0;
                            for (seq = 1; seq < SeqNo; seq++) {
                                if (bMissing[seq-1] == '0')
                                    break;
                            }

                            long    pos = (BlockNo-1) * lMaxSeqNo * lDataSize + (seq-1) * lDataSize;

                            byte[]  readByte = new byte[lDataSize];
                            long  readSize = 0;
                            try {
                                inFile.seek(pos);
                                readSize = inFile.read(readByte,0, lDataSize);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
    //						파일생성 및 초기화

    			 			// 결번데이터 송신
                            MyMap	shead = new MyMap();
    					    shead.setLong("BlockNo", BlockNo);
    					    shead.setLong("SeqNo", seq);
    					    shead.setLong("DataSize", readSize);

                            byte[]  readByte2 = new byte[(int)readSize];
                            System.arraycopy(readByte, 0, readByte2, 0, (int)readSize);
    					    shead.setBytes("Data", readByte2);

						    LinkedHashMap   oneRow = alLinkList.get(0);
						    String sentfilename = oneRow.get("TRANS_NM").toString();

    					    try {
                            	Object[]	args = new Object[2];
                            	args[0] = "0310";
                            	args[1] = sentfilename;				//DGCITY**
                                ByteBuffer buffer = protocol.encode(shead, args, 2);
    						    ssCurrent = ClientState.CHECKED;
    						    pc.sendPacket(buffer);
    					    } catch (Exception ex) {
    					    	logger.error("send fail");
    					    }
			 			}
			 		}
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 재송신...
			 	}
				break;
			 case COMPLETED:
			 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 수신
					// 파일송신 완료요구를 송신한다.
			 		MyMap	shead = new MyMap();
                    String  dtm = Utils.getCurrentDate().substring(4,8)+Utils.getCurrentTime();
                    shead.setString("TrxDtm", dtm);        // 전송일시
				    shead.setString("JobMngInfo", "004");
				    shead.setBytes("SenderNm", "hotaep");
				    shead.setBytes("SenderPw", "hotaep1");

				    LinkedHashMap   oneRow = alLinkList.get(0);
				    String sentfilename = oneRow.get("TRANS_NM").toString();

				    try {
                    	Object[]	args = new Object[2];
                    	args[0] = "0600";
                    	args[1] = sentfilename;				//DGCITY**
                        ByteBuffer buffer = protocol.encode(shead, args, 2);
					    ssCurrent = ClientState.STOPPING;
					    pc.sendPacket(buffer);
				    } catch (Exception ex) {
				    	logger.error("send fail");
				    }
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 오류...
			 	}
				break;
			 case STOPPING:
			 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 송신 완료
					logger.debug("파일송신 완료요구 송신");
					ssCurrent = ClientState.PRESTOPPED;
				    try {
				        pc.resumeReading();
				      } catch (Exception e) {
				        e.printStackTrace();
				      }
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
			 		// 재송신...
			 	}
				break;
			 case PRESTOPPED:
			 	if (dataEvent.type == EventType.PACKETARRIVED) {	// 데이터 송신 완료
                    String  key = dataEvent.msg.getString("MsgDsc");
                    if (key.equals("0610")) {
			 			logger.debug("파일송신통보 수신");

	                    logger.debug("송신완료처리");
	                    String tmpFilename = "";

	                    try {
	                        LinkedHashMap   oneRow = alLinkList.get(0);
	                        Set    keySet = oneRow.keySet();
	                        Iterator itr = keySet.iterator();
	                        //iterate through LinkedHashMap values iterator
	                        while (itr.hasNext()) {
	                            String key1 = (String)itr.next();
	                            String value = (String)oneRow.get(key1);
	                            logger.debug("[" + key1 + "] = [" + value + "]");
	                        }
	                        session = sqlMapper.openSession(false);

	                        String verifyType = oneRow.get("VERIFY_TYPE").toString();

	                        int cnt = 0;

	                        if("CA".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTCM1482", oneRow);
	                        }else if("CD".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTCM2061", oneRow);
	                        }else if("TB".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTCM1222", oneRow);
	                        }else if("TS".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTCM1172", oneRow);
	                        }else if("TC".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTFM1051", oneRow);
	                        }else if("TM".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTFM4020", oneRow);
	                        }else if("WA".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTFM6210", oneRow);
	                        }else if("EI".equals(verifyType)){
	                        	cnt = session.update("NeoMapperFile.updateTFC0011", oneRow);
	                        }else{

	                        }
	                        session.commit();
	                        session.close();
	                        inFile.close();
	                        // 조회결과 표시
	                        /**
	                         * [add]
	                         */
	                        tmpFilename = oneRow.get("TRANS_NM").toString();

	                        logger.debug("[" + cnt + "]건을 변경했습니다");
	                    } catch (Exception ex) {
	                        // 반환예외가 SQLException인지 검사
	                        logger.error("[오류발생]"+ex.getLocalizedMessage());
	                        try{
	                            session.rollback();
	                        }catch(Exception e){
	                            logger.error("[오류] "+e);
	                        }
	                        System.exit(0);
	                    }

	                    alLinkList.remove(0);    // delete

			 			if (alLinkList.size() > 0) {
		                    LinkedHashMap   oneRow = alLinkList.get(0);

		                    String  fileName = null;
		                    if (oneRow != null) {
//		                        fileName = oneRow.get("거래번호").toString();
		                        fileName = oneRow.get("TRNX_NO").toString();
		                        logger.debug("거래번호[" + fileName + "]");
		                    }

		                    try {
		                        inFile = new RandomAccessFile(folder + "/dees_com/send" + "/" + fileName, "r");
		                    } catch (IOException e) {
		                        logger.error("=========[pc : "+pc+"]==========");
                                if( pc != null ){
                                    pc.close();
                                }

                                logger.error( "================[PacketChannel close  ]==================" );
                                ssCurrent = ClientState.OFFLINE;
                                logger.error( "================[ClientState OFFLINE  ]==================" );
                                t = tt.setTimer(30000, this, null);
                                logger.error( "================[AFTER 30 SEC ==> OFFLINE  ]==================" );
		                    }   //파일지정

		                    try {
		                        lFileSize = (int)inFile.length();
                                logger.debug("파일이름[" + fileName + "], 파일길이[" + lFileSize + "]");
		                    } catch (IOException e) {
		                        // TODO Auto-generated catch block
		                        e.printStackTrace();
		                    }

		                    String sentfilename = oneRow.get("TRANS_NM").toString();

		                    // 파일정보 수신 요구
		                    MyMap    shead = new MyMap();
		                    shead.setString("FileName", sentfilename);				//DGCITY**
		                    shead.setLong("FileSize", lFileSize);
		                    shead.setLong("MsgSize", lDataSize);

		                    try {
	                        	Object[]	args = new Object[2];
	                        	args[0] = "0630";
	                        	args[1] = sentfilename;				//DGCITY**
	                            ByteBuffer buffer = protocol.encode(shead, args, 2);
		                        ssCurrent = ClientState.CREATED;
		                        pc.sendPacket(buffer);
		                    } catch (Exception ex) {
		                        logger.error("send fail");
		                    }
			 			} else {
    			 			// 업무종료요구 송신
			 				MyMap	shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8)+Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm);        // 전송일시
    					    shead.setString("JobMngInfo", "004");
    					    shead.setBytes("SenderNm", "");
    					    shead.setString("SenderPw", "");

    					    try {
                            	Object[]	args = new Object[2];
                            	args[0] = "0600";
                            	args[1] = tmpFilename;				//DGCITY**
                                ByteBuffer buffer = protocol.encode(shead, args, 2);
    					    	ssCurrent = ClientState.POSTSTOPPED;
    					    	pc.sendPacket(buffer);

    					    	logger.debug("업무종료요구 송신");
    					    } catch (Exception ex) {
    					    	logger.error("send fail");
    					    }
			 			}
			 		}
                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
                   logger.debug("서버가 접속을 종료했습니다");
                   pc.close();
                   isConnected = false;
                   ssCurrent = ClientState.OFFLINE;
                   t = tt.setTimer(30000, this, null);
			 	}
			 	break;
			 case POSTSTOPPED:
				 	if (dataEvent.type == EventType.PACKETSENT) {	// 데이터 송신 완료
						logger.debug("업무종료요구 송신");
						ssCurrent = ClientState.POSTSTOPPED2;
					    try {
					        pc.resumeReading();
					      } catch (Exception e) {
					        e.printStackTrace();
					      }
	                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
	                    logger.debug("서버가 접속을 종료했습니다");
	                    pc.close();
	                    isConnected = false;
	                    ssCurrent = ClientState.OFFLINE;
	                    t = tt.setTimer(30000, this, null);
				 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
				 		// 재송신...
				 	}
			 		break;
			 case POSTSTOPPED2:
				 	if (dataEvent.type == EventType.PACKETARRIVED) {	// 데이터 송신 완료
	                    String  key = dataEvent.msg.getString("MsgDsc");
	                    if (key.equals("0610")) {
				 			logger.debug("업무종료통보 수신");
				 			logger.debug("=== 파일송신업무종료 === ");
//							 ssCurrent = ClientState.STOPPED;
//							 connectionClosed();
		                   try {
		                       pc.resumeReading();
		                   } catch (Exception e) {
		                       e.printStackTrace();
		                   }
	                        ssCurrent = ClientState.STARTING;
				 		}
	                } else if (dataEvent.type == EventType.DISCONNECTED) {  // 서버 종료
	                    logger.debug("서버가 접속을 종료했습니다");
	                    pc.close();
	                    isConnected = false;
	                    ssCurrent = ClientState.OFFLINE;
	                    t = tt.setTimer(30000, this, null);

	                    session2.commit();
	                    session2.close();
				 	} else if (dataEvent.type == EventType.TIMEREXPIRED) {
				 		// 재송신...
				 	}
				 	break;
			 }
	    }
  }

  public static void main(String[] args) throws Exception
  {
    URL url = Loader.getResource("conf/log4j.xml");
    DOMConfigurator.configure(SendCoInfoToBank.class.getResource("/conf/log4j.xml"));

    if (args.length != 2) {
        logger.error("접속할 서버주소와 포트가 필요합니다");
        System.exit(0);
    } else {
        logger.debug("주소[" + args[0] + "], 포트[" + args[1] + "]");
    }

    Utils.getResources("conf/ApplicationResources", appRes);

    InetSocketAddress remotePoint = new InetSocketAddress(args[0], Integer.valueOf(args[1]));
//  InetSocketAddress remotePoint = new InetSocketAddress("172.18.4.51",61020);
    st = new SelectorThread(logger);
    SendCoInfoToBank	mc = new SendCoInfoToBank(remotePoint);

    try {
        mc.process();
    } catch (Exception e) {
        logger.error("비정상 종료 : " + e);
        System.exit(0);
    }
  }

}
