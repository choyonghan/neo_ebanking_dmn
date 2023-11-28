package com.uc.framework.common.util.uccomm;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import com.uc.expenditure.daegu.daemon.Etdm5020Msgfield;

public class WsMessageService extends ClientMessageService
{

	private String addr;
    private int    port;

	private String destId = "2010";

	public String getDestId()
	{
		return destId;
	}

	public void setDestId(String destId)
	{
		this.destId = destId;
	}

	Etdm5020Msgfield etdm5020 = new Etdm5020Msgfield();

	public WsMessageService()
	{
		super();
	}

	public WsMessageService(String addr, int port)
	{
		super();
		this.addr = addr;
		this.port = port;
	}

	public synchronized HashMap<String, Object> service (String msgType , byte[] array , int timeOut ) throws UnknownHostException , IOException , Exception
	{
		HashMap<String, Object> recMap      = null;
		int     cmd         =    0;
		log.info("====>>>> msgType  ==>>>  " + msgType );
		try
		{
			log.debug("0000");
			this.Connect( addr , port );
			byte[] sendBuf   = null;
			byte[] recvBuff  = null;

			log.debug("DATA : " + new String(array,"euc-kr"));
			try
			{
				cmd   = Integer.parseInt( msgType );
				log.debug( "service 입력 msgType = [" + msgType + "]" );
				log.debug("cmd : " + cmd);
				switch( cmd )
				{
				    //-----------------------------------------------------------------------
  				    // 예금주조회 송신
				    //-----------------------------------------------------------------------
					case 7777:
						       log.debug( "[6015] 업무 시작" );
						       this.Send( destId   , array );  // 송신  to   DMWSCOMM -> DMTI1030 ------------>  대구은행
						       recvBuff = this.Recv( timeOut );  // 수신  from DMWSCOMM <- DMTI2030 (WsServer) <-  대구은행
						       recMap   = etdm5020.parseSendReptBuffer(recvBuff);
						       log.debug( "[6015] 업무 종료" );
						       break;
                    default:

						       break;
				}
			}catch( Exception e ){
				System.out.println( "WsMessageService Data 송수신 오류!!!" );
				e.printStackTrace();

				return  recMap ;
			}finally{
				this.Disconnect();
			}
		}catch( Exception e ){
			System.out.println( "WsMessageService Connect 오류!!!" );
			e.printStackTrace();
			return recMap ;
		}

		return recMap;

	}

	//---------------------------------------------------------------------------
	// 에러메세지 처리
	//---------------------------------------------------------------------------
	public HashMap<String, Object> setErrMsg( HashMap<String, Object> dataMap , String ErrCode , String ErrMsg )
	{
		dataMap.put( "Errcode", ErrCode             );
		dataMap.put( "ERRMSG" , ErrCode+": "+ErrMsg );

		return dataMap;
	}
}
