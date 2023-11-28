/**
 *  주 시스템명 : 유채널 프레임웍
 *  업  무  명 : 내부 전문 송수신 연계
 *  기  능  명 : 전문 송수신 I/F(화면에서 전문 송/수신 인터페이스)
 *  클래스  ID : DataEvent
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자     소속  		 일자      Tag   내용
 * ------------------------------------------------------------------------
 *  김대완  유채널(주)  2007.11.23     %01%  신규작성
 *
 */

package com.uc.framework.common.util.uccomm;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 *
 * @author 프리비
 *
 */
public abstract class ClientMessageService  {

	private InetAddress servAddr;
	private int         servPort;


	private SocketChannel socket;

	protected UcCommHead header = new UcCommHead();

	protected HashMap<String, Object> headMap = null;

	protected Logger log = Logger.getLogger(ClientMessageService.class) ;

	protected String procId = "0000";

	protected String commId = "1000";

	private int Trans_Mode = Constants.ASYNC;

	protected Selector select = null;					// SELECT..

	/**
	 * @return the selector
	 */
	public Selector getSelector() {
		return select;
	}

	/**
	 * @param selector the selector to set
	 */
	public void setSelector(Selector selector) {
		this.select = selector;
	}

	/**
	 * @return the trans_Mode
	 */
	public int getTrans_Mode() {
		return Trans_Mode;
	}

	/**
	 * @param transMode the trans_Mode to set
	 */
	public void setTrans_Mode(int transMode) {
		Trans_Mode = transMode;
	}

	/**
	 *
	 * @throws IOException
	 */
	public void Disconnect() throws IOException {

		log.info("======================================================");
		log.info("연결이 종료되었습니다 IP=" + socket.socket().getRemoteSocketAddress() +", PORT=" + socket.socket().getPort());
		log.info("======================================================");

		getSelector().close();
		socket.close();
	}


	/**
	 *
	 * @param sendBuf
	 * @return
	 * @throws IOException
	 */
	public int Send(String destId, byte[] sendBuf) throws IOException , Exception{

		// Utils.dumpAsHex(log, sendBuf, "송신 전문", sendBuf.length);

		this.sendData("3", "1", destId, sendBuf);
		return 0;

	}

	/**
	 *
	 * @param timeOut
	 * @return : recvData
	 * @throws Exception
	 */
	public byte[] Recv(int timeOut) throws Exception {

		byte[] recvData = null;

		try {
			recvData = recv(timeOut);
		} catch (Exception e) {
			throw e;
		}

		log.debug("HEAD==" + headMap);

		int dataLen = 0;
		byte[] readBuf = null;

		if(getTrans_Mode() == Constants.ASYNC)
		{
			log.debug( "recvData=["+new String( recvData, "euckr" )+ "]" );
			log.debug( "recvData.length=["+Integer.toString(recvData.length) +"]");
			log.debug( "header.getLen  =["+Integer.toString(header.getLen()) +"]");

		    dataLen = recvData.length - header.getLen();

		    readBuf = new byte[dataLen];

		    System.arraycopy(recvData, header.getLen(), readBuf, 0, dataLen);
		} else {
		    dataLen = recvData.length;

		    readBuf = new byte[dataLen];

		    System.arraycopy(recvData, 0, readBuf, 0, dataLen);
		}

		// Utils.dumpAsHex(log, readBuf, "수신 전문", dataLen);

		return readBuf;

	}


	/**
	 *
	 * @param  : timeOut : Secone
	 * @return : recvBuff
	 * @throws : IOException, Exception
	 */
	@SuppressWarnings("rawtypes")
	private byte[] recv(int timeOut) throws Exception
	{
		ByteBuffer readB = null;
		byte[] readBuff = null;
		int	rc = 0;

		try {
			Selector	select1 = getSelector();
			// Register interest in when connection
			socket.register(select1, SelectionKey.OP_READ);

			// Wait for an event one of the registered channels
			while (select1.select(timeOut*1000) > 0) {
				Set readyKeys = select1.selectedKeys();
				Iterator	readyItorator = readyKeys.iterator();

				while (readyItorator.hasNext()) {
					SelectionKey	key = (SelectionKey)readyItorator.next();
					readyItorator.remove();
					SocketChannel	keyChannel = (SocketChannel)key.channel();
  					if (key.isReadable()) {
 						readB = ByteBuffer.allocate(10240);
						rc = keyChannel.read(readB);
						readB.flip();

						if (readBuff == null) {
							readBuff = new byte[rc];
							System.arraycopy(readB.array(), 0, readBuff, 0, rc);
						} else {
							byte[] tempBuff = readBuff;

							readBuff = new byte[tempBuff.length + rc];

							System.arraycopy(tempBuff, 0, readBuff, 0, tempBuff.length);
							System.arraycopy(readB.array(), 0, readBuff, tempBuff.length, rc);
						}
						log.debug("서버에서 수신했습니다.");
						log.debug("RECV==[" + new String(readBuff, "euckr")+"]");

						socket.register(select1, SelectionKey.OP_CONNECT);
					}

  					if (getTrans_Mode() == Constants.ASYNC) {
  						if(readBuff.length < header.getLen())
  							continue;	// 헤더길이보다도 짧다면 다시 돌아라

  						try {
  							headMap = header.parseHeadBuff(readBuff);
  							log.debug("MSG SIZE=" + headMap.get("DTSIZE") + ", READ SIZE=" + readBuff.length);
  						} catch (Exception e) {
  							// TODO Auto-generated catch block
  							e.printStackTrace();
  							throw e;
  						}

  						if (readBuff.length > Integer.parseInt(headMap.get("DTSIZE").toString())) {
  							rc = readBuff.length;
  							break;
  						}
  					} else {
  						if (readBuff.length >= Integer.parseInt(new String(readBuff, 0, 4))) {
  							rc = readBuff.length;
  							break;
  						}
  					}
				}
				
				break;//2022.10.04
			}
		} catch (SocketException se) {
//			se.printStackTrace();
			log.error("소켓오류 발생");
			throw se;
		} catch (SocketTimeoutException te) {
			log.error("소켓대기시간 초과오류 발생");
			throw te;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error("오류 발생");
//			e1.printStackTrace();
			throw e1;
		}

/*
  		socket.register(select, SelectionKey.OP_READ);

		int rc = 0;
		try {
			int readCnt = 0;

			while(true) {

				int selectCnt = select.select(timeOut*1000);
				log.debug("select keys==" + select.keys().size() + ", selected keys==" + selectCnt + ", RND=="+ ++readCnt);

				if(selectCnt == 0) {

					throw new Exception("응답 전문을 수신하지 못했습니다(TIMEOUT)");
				} else if (selectCnt == -1) {

					throw new Exception("응답 전문을 수신하지 못했습니다(서버연결끊김)");
				}

				Iterator<SelectionKey> selectedKeys = select.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}
					readB = ByteBuffer.allocate(10240);

					rc = socket.read(readB);
					log.debug("RC==" + rc);

					readB.flip();

					// log.debug("RECV==" + new String(readB.array()));

					if(rc < 0) {
						throw new Exception("응답 전문을 수신하지 못했습니다(서버연결끊김)");
					}
					if(readBuff == null) {

						readBuff = new byte[rc];
						System.arraycopy(readB.array(), 0, readBuff, 0, rc);

					} else {// 읽은넘을 뒤에 붙인다.

						byte[] tempBuff = readBuff;

						readBuff = new byte[tempBuff.length + rc];

						System.arraycopy(tempBuff, 0, readBuff, 0, tempBuff.length);
						System.arraycopy(readB.array(), 0, readBuff, tempBuff.length, rc);

					}

					log.debug("RECV==" + new String(readBuff));

					key.interestOps(SelectionKey.OP_READ);

				}

				if(getTrans_Mode() == Constants.ASYNC) {
					if(readBuff.length < header.getLen()) continue;	// 헤더길이보다도 짧다면 다시 돌아라

					try {
						headMap = header.parseHeadBuff(readBuff);

						log.debug("MSG SIZE=" + headMap.get("DTSIZE") + ", READ SIZE=" + readBuff.length);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

						throw e;
					}

					if(readBuff.length > Integer.parseInt(headMap.get("DTSIZE").toString())) {
						rc = readBuff.length;

						break;
					}
				} else {



					if(readBuff.length >= Integer.parseInt(new String(readBuff, 0,4))) {
						rc = readBuff.length;

						break;
					}
				}

			}

		} catch (SocketException se) {
			se.printStackTrace();
			throw se;
		} catch (SocketTimeoutException te) {
			throw te;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

			throw e1;

		}
*/

		if(getTrans_Mode() == Constants.ASYNC) {
			if(headMap.get("RESOPT").equals("5")) { // 응답 오류

				log.error("오류전문 수신 CODE==" + headMap.get("RESCD"));

				throw new Exception("오류전문 수신 CODE==" + headMap.get("RESCD"));
			}
		}

		return readBuff;

	}


	/**
	 *
	 * @param addr
	 * @param port
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public void Connect(String addr, int port) throws UnknownHostException, IOException, Exception
	{
		log.debug("CONNECT IP=" + addr + ", PORT=" + port);

		this.servAddr = InetAddress.getByName(addr);
		this.servPort = port;

		try {
			// Connect
			socket = SocketChannel.open();		// 소켓채널을 연다.
			socket.configureBlocking(false);	// 소켓을 비블럭킹모드로 설정한다.
			socket.connect(new InetSocketAddress(this.servAddr, this.servPort));	// 접속...

			// Open Selector
			select = Selector.open();

			// Register interest in when connection
			socket.register(select, SelectionKey.OP_CONNECT);

			//if( !socket.isConnected() ) {
				// Wait for an event one of the registered channels
				while (select.select(10000) > 0) {	// 1분
					Set readyKeys = select.selectedKeys();
					Iterator	readyItorator = readyKeys.iterator();

					while (readyItorator.hasNext()) {
						SelectionKey	key = (SelectionKey)readyItorator.next();
						readyItorator.remove();
						SocketChannel	keyChannel = (SocketChannel)key.channel();
						if (key.isConnectable()) {
							if (keyChannel.isConnectionPending()) {
								keyChannel.finishConnect();
							}
						}
					}
					
					break;
				}
			//}
			
		} catch (IOException ioex) {
			socket.close();
			getSelector().close();
			this.select = null;
			throw ioex;
//			System.err.println(ioex);
		} catch (Exception ex) {
			socket.close();
			getSelector().close();
			this.select = null;
			throw ex;
//			System.err.println(ex);
		}

		if (!socket.isConnected()) {
			log.error("서버에 연결할 수 없습니다.");
			getSelector().close();
			throw new Exception("서버에 연결할 수 없습니다");
		} else {
			log.info("======================================================");
			log.info("연결이 완료되었습니다 IP=" + socket.socket().getRemoteSocketAddress() +", PORT=" + socket.socket().getPort());
			log.info("======================================================");
		}

		try {
			if (getTrans_Mode() == Constants.ASYNC) {
    			this.sendData("1", "1", commId, null);

	    		recv(30);

			    if (!headMap.get("RESOPT").equals("3"))
			    	throw new Exception("서버 응답 오류 :: " + headMap.get("RESCD"));

				procId = (String) headMap.get("DESTID");

				log.info("RECV PROCID===" + procId);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
			getSelector().close();
			this.select = null;
			throw e;
		}
	}

/*
	public void Connect(String addr, int port) throws UnknownHostException , IOException , Exception{

		this.servAddr = InetAddress.getByName(addr);
		this.servPort = port;

		select = Selector.open();

		socket = SocketChannel.open();

		socket.configureBlocking(false);


		log.debug("CONNECT IP=" + addr + ", PORT=" + port);

		try {
    		socket.connect(new InetSocketAddress(this.servAddr, this.servPort));
		} catch (Exception e1) {
//			e1.printStackTrace();
			socket.close();
			getSelector().close();
			this.select = null;
			throw e1;
		}

		socket.register(select, SelectionKey.OP_CONNECT);

		select.select(10000);

		// 소켓 연결을 완료한다.
		try {
    		socket.finishConnect();
		} catch (IOException e) {
//			e.printStackTrace();
			socket.close();
			getSelector().close();
			this.select = null;
			throw e;
		}

		if(!socket.isConnected()) {
			log.error("서버에 연결할 수 없습니다.");
			getSelector().close();
			throw new Exception("서버에 연결할 수 없습니다");
		}

		try {
			if(getTrans_Mode() == Constants.ASYNC) {
    			this.sendData("1", "1", commId, null);

	    		recv(30);

			    if(!headMap.get("RESOPT").equals("3")) throw new Exception("서버 응답 오류 :: " + headMap.get("RESCD"));

				procId = (String) headMap.get("DESTID");

				log.info("RECV PROCID===" + procId);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			socket.close();
			getSelector().close();
			this.select = null;
			throw e;
		}


	}
*/

	/**
	 *
	 * @param port
	 * @param desId
	 * @param resCd
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public  void sendData(String cmd, String resOpt, String destId, byte[] sendData) throws Exception
	{
		// TODO Auto-generated method stub

		try {

			if(sendData == null) sendData = new byte[0];

			byte[] sendBuff = null;
			byte[] headBuff = null;

			if(getTrans_Mode() == Constants.ASYNC) {
				sendBuff = new byte[sendData.length + header.getLen()];

				headBuff = header.getHeadBuff(cmd, resOpt, procId, destId, "0000", sendData.length);

				System.arraycopy(headBuff, 0, sendBuff, 0, header.getLen());

				System.arraycopy(sendData, 0, sendBuff, header.getLen(), sendData.length);
			} else {
				sendBuff = new byte[sendData.length];

				System.arraycopy(sendData, 0, sendBuff, 0, sendData.length);
			}

//			socket.write(ByteBuffer.wrap(sendBuff));
			ByteBuffer bb = ByteBuffer.allocate(sendBuff.length);
			bb.clear();
			bb.put(sendBuff);
			bb.flip();

			Selector	select1 = getSelector();
			socket.register(select1, SelectionKey.OP_WRITE);	// 셀렉트를 등록

			// Wait for an event one of the registered channels
			while (select1.select(10000) > 0) {
				Set readyKeys = select1.selectedKeys();
				Iterator	readyItorator = readyKeys.iterator();

				while (readyItorator.hasNext()) {
					SelectionKey	key = (SelectionKey)readyItorator.next();
					readyItorator.remove();
					SocketChannel	keyChannel = (SocketChannel)key.channel();
					if (key.isWritable()) {
						while (bb.hasRemaining()) {
							Thread.sleep(10);
							keyChannel.write(bb);
						}
						socket.register(select1, SelectionKey.OP_CONNECT);	// 셀렉트를 등록
						log.debug("서버에 송신하였습니다.");
						log.debug("SEND==[" + new String(sendBuff)+"]");
					}
				}
				
				break;//2022.10.04
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
}

