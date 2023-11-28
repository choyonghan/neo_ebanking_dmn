/*
 * 대량계좌조회/계좌이체 파일 수신처리
 */
package com.uc.expenditure.daegu.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.nio.handlers.Acceptor;
import com.uc.framework.nio.handlers.AcceptorListener;
import com.uc.framework.nio.handlers.PacketChannel;
import com.uc.framework.nio.handlers.PacketChannelListener;
import com.uc.framework.nio.io.EventType;
import com.uc.framework.nio.io.SelectorThread;
import com.uc.framework.nio.io.ServerDataEventAsMap;
import com.uc.framework.parsing.DaemonListParser;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.parsing.MessageItem;
import com.uc.framework.parsing.ServiceListParser;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class RecvFromBank implements AcceptorListener, PacketChannelListener
{
    private final SelectorThread       st;

    private static Logger              logger        = Logger.getLogger(RecvFromBank.class);
    private static List<ServerDataEventAsMap>   queue         = new LinkedList<ServerDataEventAsMap>();
    private ServiceListParser          slp           = null;
    private DaemonListParser           dlp           = null;
    private FormatParserAsMap          fp            = null;
    private SqlSession                 session       = null;
    // private ServerProtocol protocol = null;

    private ServerState                ssCurrent     = ServerState.STOPPED;
    private FileWriter                 fw            = null;
    // RandomAccessFile out = null;

    // long lBlockNo = 0;
    // long lLastSeqNo = 0;
    // long lMaxSeqNo = 10;
    // int lDataSize = 1024;

    // 결번
    // LinkedHashMap<Integer, LinkedHashMap<Long, Boolean>> lhmMissingSeq = new
    // LinkedHashMap<Integer, LinkedHashMap<Long, Boolean>>();
    // LinkedHashMap<Integer, ServerState> lhmState = new LinkedHashMap<Integer,
    // ServerState>();

    LinkedHashMap<Integer, ClientInfo> lhmClientList = new LinkedHashMap<Integer, ClientInfo>();
    static MyMap    appRes = new MyMap();

    public RecvFromBank(int listenPort) throws Exception
    {
        this.logger = Logger.getLogger(RecvFromBank.class);
        parse(logger);
        st = new SelectorThread(logger);

        Acceptor acceptor = new Acceptor(listenPort, st, this);
        acceptor.openServerSocket();
        logger.info("클라이언트 접속을 기다립니다. 포트[" + listenPort + "]");
    }


    public RecvFromBank(Logger logger, int listenPort) throws Exception
    {
        this.logger = logger;
        parse(logger);
        st = new SelectorThread(logger);

        Acceptor acceptor = new Acceptor(listenPort, st, this);
        acceptor.openServerSocket();
        logger.debug("클라이언트 접속을 기다립니다. 포트[" + listenPort + "]");
    }

    /**
     * Starts the server.
     *
     * @param listenPort
     *            The port where to listen for incoming connections.
     * @throws Exception
     */

    public static void main(String[] args) throws Exception
    {
        URL url = Loader.getResource("conf/log4j.xml");
        DOMConfigurator.configure(RecvFromBank.class.getResource("/conf/log4j.xml"));

        if (args.length != 1) {
            logger.error("접속을 기다릴 포트가 필요합니다");
            System.exit(-1);
        }
        int listenPort = Integer.parseInt(args[0]);

        Utils.getResources("conf/ApplicationResources", appRes);

        try {
            RecvFromBank svr = new RecvFromBank(listenPort);
            svr.process();
        } catch (Exception ex) {
            logger.error("★★★★★★★★:" + ex);

            System.exit(0);
        }
    }

    public void parse(Logger logger)
    {
        // 전문형식정의 파일을 읽고 분석한다.
        fp = new FormatParserAsMap(logger);
        fp.doParsingAsMap("msgformat1");
    }

    /**
     * 업무처리 메인 루틴
     *
     */
    public void process()
    // public void run()
    {
        ssCurrent = ServerState.OFFLINE;
        long lRetryOpen = 0;

        String msgType = "04";
        String gubun = "E";
        MessageItem mi = fp.getMessageItemAsMap(msgType);

        ServerDataEventAsMap dataEvent;
        while (true) {
            synchronized (queue) {
                while (queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ie) {
                        logger.error("★★★★★★★★:" + ie);
                    }
                }
                dataEvent = queue.remove(0);
            }

            // 접속된 클라이언트 정보 추출
            PacketChannel   pc = (PacketChannel)dataEvent.handle;
            int port = pc.getSocketChannel().socket().getPort();
            ClientInfo ci = lhmClientList.get(port);
            ssCurrent = ci.State;

            switch (ssCurrent) {
                case OFFLINE: // 클라이언트가 접속안됨
                    if (dataEvent.type == EventType.CLIENTACCEPTED) { // 클라이언트 접속
                        logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 클라이언트["
                                + pc.getSocketChannel().socket()
                                        .getInetAddress().getHostAddress()
                                + "]가 접속되었습니다");

                        ci.State = ServerState.STOPPED;
                        lhmClientList.put(port, ci);

                        try {
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트["
                                + pc.getSocketChannel().socket()
                                        .getInetAddress().getHostAddress()
                                + "]가 접속 종료되었습니다");
                        pc.close();
                    }
                    break;
                case STOPPED:
                    if (dataEvent.type == EventType.PACKETARRIVED) { // 데이터 수신
                        String key = dataEvent.msg.getString("MsgDsc");
                        if (key.equals("0600")) {
                            logger.info("=== 파일수신업무 개시 === ");
                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 업무개시요구 수신");

                            MyMap   shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8) + Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm); // 전송일시
                            shead.setString("JobMngInfo", dataEvent.msg.getString("JobMngInfo")); // 업무관리정보
                            shead.setBytes("SenderNm", dataEvent.msg.getBytes("SenderNm")); // 송신자명
                            shead.setString("SenderPw", dataEvent.msg.getString("SenderPw")); // 송신자암호

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0610";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                ci.State = ServerState.STARTING;
                                lhmClientList.put(port, ci);

                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("전문을 송신하지 못했습니다. 접속을 종료합니다.");
                                pc.close();
                            }
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + " ], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 클라이언트["
                                + pc.getSocketChannel().socket()
                                        .getInetAddress().getHostAddress()
                                + "]가 종료되었습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    }
                    break;
                case STARTING:
                    if (dataEvent.type == EventType.PACKETSENT) { // 데이터 송신 완료
                        logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 업무개시통보 송신");
                        ci.State = ServerState.STARTED;
                        lhmClientList.put(port, ci);
                        try {
                            // 수신모드
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 재송신
                        logger.debug("업무개시통보 송신 시간초과");
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else {
                    }
                    break;
                case STARTED:
                    if (dataEvent.type == EventType.PACKETARRIVED) { // 데이터 수신
                        String key = dataEvent.msg.getString("MsgDsc");
                        if (key.equals("0630")) {
                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 파일정보수신 요구 수신");

                            MyMap   shead = new MyMap();
                            shead.setString("FileName", dataEvent.msg.getString("FileName"));
                            shead.setLong("FileSize", 0);   // 최초시 파일크기=0
                            shead.setLong("MsgSize", dataEvent.msg.getLong("MsgSize"));

                            // 수신한 파일이름
                            String fileName = dataEvent.msg.getString("FileName");

                            // 수신한 파일이름
                            long fileSize = dataEvent.msg.getLong("FileSize");

                            // 수신한 파일이름
                            long msgSize = dataEvent.msg.getLong("MsgSize");

                            String  strSvrType = appRes.getString("RecvFromBank.server");

                            logger.info("### strSvrType["+strSvrType+"]");

                            if( !"LOC".equals(strSvrType)) {
                            	ci.lDataSize = (int)msgSize - 34;
                            } else {
                            	ci.lDataSize = (int)msgSize;
                            }

                            logger.info("파일이름[" + fileName + "], 파일크기[" + fileSize + "], 메시지크기[" + msgSize + "]");

                            for (long i = 1; i <= ci.lMaxSeqNo; i++) {
                                ci.lhmMissingSeq.put(i, false);
                            }

/*
                            File ff = new File(fileName);

                            if (ff.isFile()) {
                                // 파일이 존재하면 먼저 삭제한다.
                                ff.delete();
                            }
*/
                            String  realFileName = fileName + "_" + Utils.getCurrent("yyyyMMddHHmmssSSS");
                            String  folder = appRes.getString("RecvFromBank.recvDirectory");
                            String strAccDvCd = appRes.getString("ServiceAccDvCd");
                            String strDporDvCd = appRes.getString("ServiceDporDvCd");
                            String sysDvCd = appRes.getString("sysDvCd");

//                          DGCITY04~DGCITY08 테스트용임!!
                            try {
                                if (fileName.equals(strAccDvCd)) {
                                    ci.sFileName = folder + "/dees_trn/recv" + "/" + realFileName;
                                } else if (fileName.equals(strDporDvCd)) {
                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;

//                                } else if (fileName.equals("DGCITY04")) {
//                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;
//                                } else if (fileName.equals("DGCITY05")) {
//                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;
//                                } else if (fileName.equals("DGCITY06")) {
//                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;
//                                } else if (fileName.equals("DGCITY07")) {
//                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;
//                                } else if (fileName.equals("DGCITY08")) {
//                                    ci.sFileName = folder + "/dees_ver/recv" + "/" + realFileName;

                                }else if (fileName.equals("DG4451")) {
                                    ci.sFileName = folder + "/u/IbmsIF/bank" + "/" + realFileName;
                                } else if (fileName.equals("DG6270")) {
                                    ci.sFileName = folder + "/u/IbmsIF/bank" + "/" + realFileName;
                                } else if (fileName.equals("TGMTAX01")) {
                                    ci.sFileName = folder + "/dees_999/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG01")||fileName.equals("KLIDDG02")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_1/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG03")||fileName.equals("KLIDDG04")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_2/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG05")||fileName.equals("KLIDDG06")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_3/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG07")||fileName.equals("KLIDDG08")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_4/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG09")||fileName.equals("KLIDDG10")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_5/recv" + "/" + realFileName;
                                } else if (fileName.equals("KLIDDG11")||fileName.equals("KLIDDG12")) { // 2020 10 13 추가 6050 데몬
                                    ci.sFileName = folder + "/dees_report_6/recv" + "/" + realFileName;


                                }else if (fileName.equals("CO1001")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("CO1002")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("CO1003")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("CO1004")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("CO1005")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("TR2001")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("TR2002")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("TR2003")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("TR2004")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;
                                }else if (fileName.equals("TR2005")) {
                                    ci.sFileName = folder + "/dees_otc/recv" + "/" + realFileName;

                                } else if ( fileName.equals("KLID"+sysDvCd+"19") // FB_FM_XXX_RD_00001	수입일계표(I) 수신
                                		|| fileName.equals("KLID"+sysDvCd+"20") // FB_FM_XXX_RD_00002	수입일계표(II) 수신
                                		|| fileName.equals("KLID"+sysDvCd+"21") // FB_FM_XXX_RD_00003	세입일계표 수신
                                		|| fileName.equals("KLID"+sysDvCd+"22") // FB_FM_XXX_RD_00005	세출일계표 수신
                                		|| fileName.equals("KLID"+sysDvCd+"23") // FB_FM_XXX_RD_00004	세입세출일계표 수신
                                		|| fileName.equals("KLID"+sysDvCd+"24") // FB_FM_XXX_RD_00006	세입세출외현금일계표 수신
                                		|| fileName.equals("KLID"+sysDvCd+"27") // FB_FM_XXX_RD_00009   자금마감재무건전성평가수신연계( TODO : 경북해서만 처리됨 )
                                		|| fileName.equals("KLID"+sysDvCd+"28") // FB_FE_XXX_RD_08028  금고은행시스템일상경비월별잔액수신
                                		|| fileName.equals("KLID"+sysDvCd+"XX") // FR_FM_XXX_RD_00025   지역개발기금수입지출일계표수신연계 / 미정
                                		|| fileName.equals("KLID"+sysDvCd+"X1") // FR_FM_XXX_RD_00026   지역개발공채발행내역수신연계 / 미정
                                		|| fileName.equals("KLID"+sysDvCd+"X2") // FR_FM_XXX_RD_00027  융자금회수이자내역수신연계 / 미정
                                		) {

                                    ci.sFileName = folder + "/dees_report/recv" + "/" + realFileName;

                                } else if ( fileName.equals("KLID"+sysDvCd+"26") // FB_FM_XXX_RD_00007	계좌거래내역 수신
                                		|| fileName.equals("KLID"+sysDvCd+"25") // FB_FM_XXX_RD_00008	계좌신규 개설 및 약정정보 수신
                                		) {

                                    ci.sFileName = folder + "/dees_report78/recv" + "/" + realFileName;

                                }  else  {
                                    ci.sFileName = folder + "/dees_err/recv" + "/" + realFileName;
                                }
                                ci.out = new RandomAccessFile(ci.sFileName, "rw");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + e);
                            } // 파일지정

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0640";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                ci.State = ServerState.CREATING;
                                lhmClientList.put(port, ci);
                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("send fail");
                            }
                        } else if (key.equals("0600")) {
                            logger.debug("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 업무종료요구 수신");

                            MyMap   shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8) + Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm); // 전송일시
                            shead.setString("JobMngInfo", dataEvent.msg.getString("JobMngInfo")); // 업무관리정보
                            shead.setBytes("SenderNm", dataEvent.msg.getBytes("SenderNm")); // 송신자명
                            shead.setString("SenderPw", dataEvent.msg.getString("SenderPw")); // 송신자암호

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0610";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                ci.State = ServerState.STOPPING;
                                lhmClientList.put(port, ci);

                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("send fail");
                            }

                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        logger.error("파일정보수신 시간초과");
                    }
                    break;
                case CREATING:
                    if (dataEvent.type == EventType.PACKETSENT) { // 데이터 송신 완료
                        logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 파일정보수신 통보 송신");
                        ci.State = ServerState.CREATED;
                        lhmClientList.put(port, ci);
                        try {
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 재송신
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else {
                    }
                    break;
                case CREATED:
                    logger.debug("created...");
                    if (dataEvent.type == EventType.PACKETARRIVED) { // 데이터 수신
                        String key = dataEvent.msg.getString("MsgDsc");
                        logger.debug("created..." + key);
                        if (key.equals("0310")) {
                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 결번데이터 수신");
                            // 계속수신...
                            long BlockNo = dataEvent.msg.getLong("BlockNo");
                            long SeqNo = dataEvent.msg.getLong("SeqNo");
                            long dataSize = dataEvent.msg.getLong("DataSize");
                            byte[] data = dataEvent.msg.getBytes("Data");

                            logger.info("결번데이터 BlockNo[" + BlockNo + "], SeqNo[" + SeqNo + "], Size[" + dataSize + "]");// ,
                                                      // 데이터[" + new String(data) + "]");

                            long seek = (BlockNo - 1) * ci.lDataSize
                                    * ci.lMaxSeqNo + (SeqNo - 1) * ci.lDataSize;
                            try {
                                ci.out.seek(seek);
                                ci.out.write(data, 0, (int) dataSize);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + e);
                            } // 파일지정

                            // lBlockNo = BlockNo; // 최종일련번호
                            // lLastSeqNo = SeqNo; // 최종일련번호
                            ci.lhmMissingSeq.put(SeqNo, true);

                            // 결번확인 송신
                            int count = 0;
                            byte[] bMissing = new byte[(int) ci.lLastSeqNo];
                            for (long l = 1; l <= ci.lLastSeqNo; l++) {
                                if (ci.lhmMissingSeq.get(l).booleanValue() == false) {
                                    logger.debug("결번[" + l + "]");
                                    count++;
                                    bMissing[(int) l - 1] = '0'; // 수신 미완료
                                } else {
                                    bMissing[(int) l - 1] = '1'; // 수신 완료
                                }
                            }
                            logger.info("결번건수[" + count + "], 결번확인[" + new String(bMissing) + "]");

                            MyMap   shead = new MyMap();
                            shead.setLong("BlockNo", ci.lBlockNo); // 블럭번호
                            shead.setLong("LastSeqNo", ci.lLastSeqNo); // 최종일련번호
                            shead.setLong("LostCnt", count); // 결번갯수

                            shead.setBytes("LostSeq", bMissing); // 결번확인

//              if (count > 0) {
//                              shead.setBytes("LostSeq", bMissing); // 결번확인
//              } else {
//                              shead.setBytes("LostSeq", ""); // 결번확인
//              }
                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0300";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                if (count == 0) {
                                    ci.State = ServerState.CHECKING;
                                    lhmClientList.put(port, ci);
                                } else {
                                    ci.State = ServerState.CREATING;
                                    lhmClientList.put(port, ci);
                                }
                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("send fail");
                            }
                        } else if (key.equals("0320")) {
                            long BlockNo = dataEvent.msg.getLong("BlockNo");
                            long SeqNo = dataEvent.msg.getLong("SeqNo");
                            long dataSize = dataEvent.msg.getLong("DataSize");
                            byte[] data = dataEvent.msg.getBytes("Data");

                            String gb = new String(data);
                            if (BlockNo==1 && SeqNo==1){
                            	logger.debug(gb);
                                logger.debug(gb.substring(42,44));
                                logger.debug("gb.substring(30,32) " + gb.substring(30,32));

                                String tmpGb = gb.substring(42,44);

                                if (gb.substring(42,44).equals("VR")){
                                    gubun = "V";
                                }else if (gb.substring(42,44).equals("EA")){
                                    gubun = "A";

                                }else if ( tmpGb.equals("DA") || tmpGb.equals("DB")){

                                	// TODO : TOBE 가상계좌구분
                                	// VR->DA
                                	// EA->DB

                                	gubun = tmpGb;

                                }else if (gb.substring(28,30).equals("EC")){
                                    gubun = "EC";
                                }else if (gb.substring(30,32).equals("SA")){
                                    gubun = "SA";
                                }else{
                                    gubun = "E";
                                }
                            }

                            if (ci.lBlockNo != BlockNo) {
                                // 초기화
                                for (long i = 1; i <= ci.lMaxSeqNo; i++) {
                                    ci.lhmMissingSeq.put(i, false);
                                }
                            }

                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type
                                    + "], 소켓포트[" + dataEvent.socket.socket().getPort()
                                    + "], 데이터 수신 BlockNo[" + BlockNo
                                    + "], SeqNo[" + SeqNo
                                    + "], ci.lDataSize[" + ci.lDataSize
                                    + "], ci.lMaxSeqNo[" + ci.lMaxSeqNo
                                    + "], Size[" + dataSize + "]");

                            long seek = (BlockNo - 1) * ci.lDataSize * ci.lMaxSeqNo + (SeqNo - 1) * ci.lDataSize;
                            logger.info("파일위치[" + seek + "]");

                            try {
                                // 결번확인용
                                ci.lBlockNo = BlockNo; // 최종일련번호
                                ci.lLastSeqNo = SeqNo; // 최종일련번호

                                // if (SeqNo % 3 == 0) {
                                // 결번 테스트...
                                // lhmMissingSeq.put(SeqNo, true);
                                // } else {
                                ci.lhmMissingSeq.put(SeqNo, true);

                                ci.out.seek(seek);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + e);
                                logger.error( Utils.makeStackTrace(e));
                                logger.error("파일위치설정오류[" + e.getLocalizedMessage() + "]");
                            } // 파일지정
                            logger.debug("파일위치설정[" + seek + "]");
                            logger.debug("======================================= dataSize[" + data.length + "], data[" + data + "], dataSize[" + dataSize +"]" );

                            try {
                                ci.out.write(data, 0, data.length);
                            } catch (IOException ioex) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + ioex);
                                logger.error("파일기록오류[" + ioex.getLocalizedMessage() + "]");
                            } catch (Exception ex) {
                                logger.error("★★★★★★★★:" + ex);
                                logger.error("파일기록오류[" + ex.getLocalizedMessage() + "]");
                            } // 파일지정

                            logger.debug("파일기록[" + seek + "]");

                            ci.State = ServerState.CREATED;
                            lhmClientList.put(port, ci);
                            try {
                                pc.resumeReading();
                            } catch (Exception e) {
                                logger.error("★★★★★★★★:" + e);
                            }
                            logger.debug("계속");
                            // 계속수신...
                        } else if (key.equals("0600")) {
                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 일송신요구 수신");

                            int count = 0;
                            byte[] bMissing = new byte[(int) ci.lLastSeqNo];
                            for (long l = 1; l <= ci.lLastSeqNo; l++) {
                                if (ci.lhmMissingSeq.get(l).booleanValue() == false) {
                                    bMissing[(int) l - 1] = '0'; // 수신 미완료
                                } else {
                                    bMissing[(int) l - 1] = '1'; // 수신 완료
                                }
                            }

                            try {
                                ci.out.close();

                                if (gubun.equals("V")){
                                    File    okFile = new File(ci.sFileName + ".VR");
                                    okFile.createNewFile();
                                }else if (gubun.equals("A")){
                                    File    okFile = new File(ci.sFileName + ".EA");
                                    okFile.createNewFile();

                                }else if ( gubun.equals("DA") || gubun.equals("DB")){

                                	// TODO : TOBE 가상계좌파일생성
                                	// VR->DA
                                	// EA->DB

                                    File    okFile = new File(ci.sFileName + "." + gubun);
                                    okFile.createNewFile();

                                /**
                                 * [result]
                                 * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
                                 */
                                }else if (gubun.equals("EC")){
                                    File    okFile = new File(ci.sFileName + ".EC");
                                    okFile.createNewFile();
                                }else if (gubun.equals("SA")){												/* 대용량 */
                                    File    okFile = new File(ci.sFileName + ".SA");
                                    okFile.createNewFile();
                                }else{
                                    File    okFile = new File(ci.sFileName + ".OK");
                                    okFile.createNewFile();
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + e);
                            }
                            // 계속수신...

                            // 결번확인통보
                            MyMap   shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8) + Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm); // 전송일시
                            shead.setString("JobMngInfo", dataEvent.msg.getString("JobMngInfo")); // 업무관리정보
                            shead.setBytes("SenderNm", dataEvent.msg.getBytes("SenderNm")); // 송신자명
                            shead.setString("SenderPw", dataEvent.msg.getString("SenderPw")); // 송신자암호

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0610";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                ci.State = ServerState.COMPLETED;
                                lhmClientList.put(port, ci);
                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("send fail");
                            }
                        } else if (key.equals("0620")) {
                            long BlockNo = dataEvent.msg.getLong("BlockNo");
                            long LastSeqNo = dataEvent.msg.getLong("LastSeqNo");

                            logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 결번확인통보요구 수신, 블럭번호[" + BlockNo
                                    + "], 최종일련번호[" + LastSeqNo + "]");

                            int count = 0;
                            byte[] bMissing = new byte[(int) ci.lLastSeqNo];
                            for (long l = 1; l <= ci.lLastSeqNo; l++) {
                                if (ci.lhmMissingSeq.get(l).booleanValue() == false) {
                                    logger.debug("결번[" + l + "]");
                                    count++;
                                    bMissing[(int) l - 1] = '0'; // 수신 미완료
                                } else {
                                    bMissing[(int) l - 1] = '1'; // 수신 완료
                                }
                            }
/*
                            for (long l = LastSeqNo; l < ci.lMaxSeqNo; l++) {
                                bMissing[(int) l] = '0'; // 수신 미완료
                            }
*/
                            logger.debug("결번건수[" + count + "], 결번확인[" + new String(bMissing) + "]");

                            MyMap   shead = new MyMap();
                            shead.setLong("BlockNo", ci.lBlockNo); // 블럭번호
                            shead.setLong("LastSeqNo", ci.lLastSeqNo); // 최종일련번호
                            shead.setLong("LostCnt", count); // 결번갯수

                            shead.setBytes("LostSeq", bMissing); // 결번확인

//              if (count > 0) {
//                              shead.setBytes("LostSeq", bMissing); // 결번확인
//              } else {
//                              shead.setBytes("LostSeq", ""); // 결번확인
//              }

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0300";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                if (count == 0) {
                                    // 더 이상 결번이 없으면...
                                    ci.State = ServerState.CHECKING;
                                    lhmClientList.put(port, ci);
                                } else {
                                    ci.State = ServerState.CREATING;
                                    lhmClientList.put(port, ci);
                                }
                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("결번확인통보 송신실패");
                            }
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.info("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 오류...
                    }
                    break;
                case CHECKING:
                    if (dataEvent.type == EventType.PACKETSENT) { // 데이터 송신 완료
                        logger.info("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 결번확인통보 송신");
                        ci.State = ServerState.CREATED;
                        lhmClientList.put(port, ci);
                        try {
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 재송신...
                    }
                    break;
                case CHECKED:
                    if (dataEvent.type == EventType.PACKETARRIVED) { // 데이터 수신
                    /*
                     * if
                     * (dataEvent.msg.get("MsgDsc").toString().equals("0310")) {
                     * logger.debug("결번데이터 수신"); // 계속수신... long BlockNo =
                     * Integer
                     * .parseInt(dataEvent.msg.get("BlockNo").toString()); long
                     * SeqNo =
                     * Integer.parseInt(dataEvent.msg.get("SeqNo").toString());
                     * long dataSize =
                     * Integer.parseInt(dataEvent.msg.get("DataSize"
                     * ).toString()); String data =
                     * dataEvent.msg.get("Data").toString();
                     *
                     * logger.debug("결번데이터 BlockNo[" + BlockNo + "], SeqNo[" +
                     * SeqNo + "], Size[" + dataSize + "], data[" + data + "]");
                     *
                     * long seek = BlockNo * (SeqNo-1) * dataSize; try {
                     * out.seek(seek); out.writeBytes(data); } catch
                     * (IOException e) { // TODO Auto-generated catch block
                     * logger.error("★★★★★★★★:" + e); } //파일지정
                     *
                     * lBlockNo = BlockNo; // 최종일련번호 lLastSeqNo = SeqNo; //
                     * 최종일련번호 lhmMissingSeq.put(SeqNo, true);
                     *
                     * // 결번확인 송신 int count = 0; byte[] bMissing = new
                     * byte[(int)lMaxSeqNo]; for (long l = 1; l <= SeqNo; l++) {
                     * if (lhmMissingSeq.get(l).booleanValue() == false) {
                     * logger.debug("결번[" + l + "]"); count++;
                     * bMissing[(int)l-1] = '0'; // 수신 미완료 } else {
                     * bMissing[(int)l-1] = '1'; // 수신 완료 } } for (long l =
                     * SeqNo; l < lMaxSeqNo; l++) { bMissing[(int)l] = '0'; //
                     * 수신 미완료 } logger.debug("결번확인[" + new String(bMissing) +
                     * "]");
                     *
                     * LinkedHashMap<String,Object> shead = new
                     * LinkedHashMap<String,Object>(); shead.put("TrCd",
                     * dataEvent.msg.get("TrCd")); shead.put("JobDsc",
                     * dataEvent.msg.get("JobDsc")); shead.put("BankCd",
                     * dataEvent.msg.get("BankCd")); shead.put("MsgDsc",
                     * "0300"); shead.put("TrxDsc",
                     * dataEvent.msg.get("TrxDsc")); shead.put("SRFlag",
                     * dataEvent.msg.get("SRFlag")); shead.put("FileNm",
                     * dataEvent.msg.get("FileNm")); shead.put("ResCd", "000");
                     *
                     * shead.put("BlockNo", dataEvent.msg.get("BlockNo")); //
                     * 블럭번호 shead.put("LastSeqNo",
                     * dataEvent.msg.get("LastSeqNo")); // 최종일련번호
                     * shead.put("LostCnt", count); // 결번갯수
                     *
                     * shead.put("LostSeq", new String(bMissing)); // 결번확인
                     *
                     * try { ByteBuffer buffer = protocol.encode(shead);
                     *
                     * ssCurrent = ServerState.CHECKING;
                     * pc.sendPacket(buffer); } catch (Exception ex) {
                     * logger.error("send fail"); } ssCurrent =
                     * ServerState.COMPLETING; try {
                     * pc.resumeReading(); } catch (Exception e) {
                     * logger.error("★★★★★★★★:" + e); } }
                     */
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 오류...
                    }
                    break;
                case COMPLETING:
                    if (dataEvent.type == EventType.PACKETARRIVED) { // 데이터 송신 완료
                        String key = dataEvent.msg.getString("MsgDsc");
                        if (key.equals("0600")) {
                            logger.debug("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 파일송신요구 수신");

                            int count = 0;
                            byte[] bMissing = new byte[(int)ci.lLastSeqNo];
                            for (long l = 1; l <= ci.lLastSeqNo; l++) {
                                if (ci.lhmMissingSeq.get(l).booleanValue() == false) {
                                    bMissing[(int) l - 1] = '0'; // 수신 미완료
                                } else {
                                    bMissing[(int) l - 1] = '1'; // 수신 완료
                                }
                            }
                            logger.debug("결번확인[" + new String(bMissing) + "]");

                            try {
                                ci.out.close();
                                File    okFile = new File(ci.sFileName + ".OK");
                                okFile.createNewFile();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                logger.error("★★★★★★★★:" + e);
                            }
                            // 계속수신...

                            // 결번확인통보
                            MyMap   shead = new MyMap();
                            String  dtm = Utils.getCurrentDate().substring(4,8) + Utils.getCurrentTime();
                            shead.setString("TrxDtm", dtm); // 전송일시
                            shead.setString("JobMngInfo", dataEvent.msg.getString("JobMngInfo")); // 업무관리정보
                            shead.setBytes("SenderNm", dataEvent.msg.getBytes("SenderNm")); // 송신자명
                            shead.setString("SenderPw", dataEvent.msg.getString("SenderPw")); // 송신자암호

                            try {
                                Object[]    args = new Object[2];
                                args[0] = "0610";
                                args[1] = "";
                                ByteBuffer buffer = ci.protocol.encode(shead, args, 2);
                                ci.State = ServerState.COMPLETED;
                                lhmClientList.put(port, ci);
                                pc.sendPacket(buffer);
                            } catch (Exception ex) {
                                logger.error("send fail");
                            }
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 재송신...
                    }
                    break;
                case COMPLETED:
                    if (dataEvent.type == EventType.PACKETSENT) {
                        logger.debug("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 파일송신통보 송신");
                        ci.State = ServerState.STARTED;
                        lhmClientList.put(port, ci);
                        try {
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                        // 처음으로 간다.
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 오류...
                    }
                    break;
                case STOPPING:
                    if (dataEvent.type == EventType.PACKETSENT) {
                        logger.debug("현재상태[" + ci.State + "], 이벤트[ " + dataEvent.type + "], 소켓포트[" + dataEvent.socket.socket().getPort() + "], 업무종료통보 송신");
                        logger.debug("=== 파일수신업무 종료 === ");
                        ci.State = ServerState.STOPPED;
                        lhmClientList.put(port, ci);
                        try {
                            pc.resumeReading();
                        } catch (Exception e) {
                            logger.error("★★★★★★★★:" + e);
                        }
                    } else if (dataEvent.type == EventType.CLIENTDISCONNECTED) { // 클라이언트 접속종료
                        logger.debug("클라이언트가 접속을 종료했습니다");
                        ci.State = ServerState.OFFLINE;
                        lhmClientList.put(port, ci);
                        pc.close();
                    } else if (dataEvent.type == EventType.TIMEREXPIRED) { // 클라이언트 접속종료
                        // 재송신...
                    }
                    break;
            }
        }
    }

    // ////////////////////////////////////////
    // Implementation of the callbacks from the
    // Acceptor and PacketChannel classes
    // ////////////////////////////////////////
    /**
     * 새로운 클라이언트가 접속
     */
    @Override
	public void socketConnected(Acceptor acceptor, SocketChannel sc)
    {
        try {
            // We should reduce the size of the TCP buffers or else we will
            // easily run out of memory when accepting several thousands of
            // connctions
            sc.socket().setReceiveBufferSize(5 * 1024);
            sc.socket().setSendBufferSize(5 * 1024);

            ClientInfo ci = new ClientInfo();
            ci.protocol = new ServerProtocol(logger, fp);

            int port = sc.socket().getPort();
            ci.State = ServerState.OFFLINE;
            lhmClientList.put(port, ci);

            // The contructor enables reading automatically.
            PacketChannel pc = new PacketChannel(logger, sc, st, ci.protocol, this);
            synchronized (queue) {
                queue.add(new ServerDataEventAsMap(EventType.CLIENTACCEPTED, Thread.currentThread(), pc, sc, null, 0, null));
                queue.notify();
            }
            // pc.resumeReading();
        } catch (IOException e) {
            logger.error("★★★★★★★★:" + e);
        }
    }

    @Override
	public void socketError(Acceptor acceptor, Exception ex)
    {
//        System.out.println("[" + acceptor + "] Error: " + ex.getMessage());
        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.SOCKETERROR, Thread.currentThread(), (Object)null, null, null, 0, null));
            queue.notify();
        }
    }

    @Override
	public void packetArrived(PacketChannel pc, ByteBuffer pckt)
    {
        // System.out.println("[" + pc.toString() + "] Packet received. Size: "
        // + pckt.remaining() + ", Data["+ new String(pckt.array())+"]");
        // 먼저 헤더부를 분석하여 개별부 정보를 구한다.
        MyMap   msg = new MyMap();
        if (fp.disassembleMessageByteAsMap(pckt.array(), pckt.remaining(), msg, "04") == -1) {
            logger.error("paring error");
        }

        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.PACKETARRIVED, Thread.currentThread(), pc, pc
                    .getSocketChannel(), pckt.array(), pckt.remaining(), msg));
            queue.notify();
        }
    }

    @Override
	public void socketException(PacketChannel pc, Exception ex)
    {
        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.SOCKETEXCEPTION, Thread.currentThread(), pc, pc
                    .getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }

    @Override
	public void socketDisconnected(PacketChannel pc)
    {
        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.CLIENTDISCONNECTED, Thread.currentThread(), pc, pc
                    .getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }

    /**
     * The answer to a request was sent. Prepare to read the next request.
     */
    @Override
	public void packetSent(PacketChannel pc, ByteBuffer pckt)
    {
        synchronized (queue) {
            queue.add(new ServerDataEventAsMap(EventType.PACKETSENT, Thread.currentThread(), pc, pc
                    .getSocketChannel(), null, 0, null));
            queue.notify();
        }
    }
}
