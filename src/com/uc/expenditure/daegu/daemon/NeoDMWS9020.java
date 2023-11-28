/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 일일배치
 *  기  능  명 : e세출 일일배치
 *  클래스  ID : NeoDMWS9020
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  신상훈       다산(주)      2023.03.02         %01%             최초작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import com.uc.core.MapForm;
import com.uc.framework.common.util.StringUtil;
import com.uc.framework.common.util.uccomm.field.CyberFieldList;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS9020 implements Runnable {
	private static Logger logger = Logger.getLogger(NeoDMWS9020.class);

	/* 데이터베이스 접속을 위한 세션 선언 */
	private static SqlSession session = null;
	private static SqlSessionFactory sqlMapper = null;
	private static Thread self = null;
	boolean bAlreadyConfirm = false;
	boolean bAlreadyConfirm2 = false;
	static MyMap appRes = new MyMap();

	/**
	 * 메인함수
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		/* 로그설정 파일을 읽는다. 컨텍스트를 기준으로 한다 */
		DOMConfigurator.configure(NeoDMWS9020.class.getResource("/conf/log4j.xml"));

		logger.debug("##### [" + NeoDMWS9020.class.getSimpleName() + "] 시작 #####");

		/* 클래스 인스턴스 생성 */
		NeoDMWS9020 hello = new NeoDMWS9020();

		/* 인스턴스를 쓰레드(데몬)로 실행 */
		self = new Thread(hello);
		self.setDaemon(true);
		self.start();

		try {
			self.join();
			logger.debug("쓰레드 종료");
		} catch (InterruptedException e) {
			logger.debug("InterruptedException ::: ["+ e.getMessage() +"]");
		}

		logger.debug("##### [" + NeoDMWS9020.class.getSimpleName() + "] 끝 #####");
	}

	/**
	 * 쓰레드
	 */
	public void run() {
		logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 실행");

		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader( "res/Configuration.xml" );
			Properties properties = Resources.getResourceAsProperties( "res/db.properties" );
			String pw = Utils.getDecrypt( properties.getProperty("password"));
			properties.setProperty("password", pw);
			sqlMapper = new SqlSessionFactoryBuilder().build( reader , properties);

		} catch (Exception ex) {
			logger.error("데이터베이스 접속중 오류[" + ex.getLocalizedMessage() + "]");
			return;
		}
		logger.debug("SQLMAPPER생성");
		Utils.getResources("conf/ApplicationResources", appRes);

		while (!Thread.currentThread().isInterrupted()) {
			mainLoop();

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
				break;
			}
		}
	}

	/**
	 * process 실행
	 * @return
	 */
	public int mainLoop() {
		// 데이터베이스 세션 생성, 자동확약 끔.
		try {
			session = sqlMapper.openSession(false);
		} catch (Exception ex) {
			logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
			return -1;
		}
    	logger.info("dmws9020 start");

    	Socket socket = null;

		try {

			// 전날 미처리건 반려처리
	    	int checkTime = Integer.parseInt(getTime("HHmmss"));
	    	if (10000 <= checkTime && 30000 >=  checkTime){
	    	    ArrayList<MyMap> NoResultMap = (ArrayList<MyMap>)session.selectList("NeoMapper9020.selectNoResultData");
	    	    logger.info("오전1시 ~ 오전3시 어제날짜 미처리건 확인");
	    	    if( NoResultMap.size() > 0 ) {
	                logger.info("어제날짜 미처리건 : " + NoResultMap.size() + "건 확인");

	                for (int j= 0 ; j < NoResultMap.size() ; j++) {
					    MyMap	ht = NoResultMap.get(j);
					    logger.info("거래번호 : " + ht.getString("TRNX_NO"));
					    // 반려 update
		                session.update("NeoMapper9020.updateTFE2190Cancle", ht);
		                session.update("NeoMapper9020.updateTFE2170Cancle", ht);
					}
					session.commit();
					logger.info("미처리건 반려 작업 완료!!!");
	            }
	    	}


	    	Date now = new Date();
	    	SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
	    	String formatedNow = sf1.format(now);
	    	//logger.info("현재날짜 : " + formatedNow);

	    	MyMap resultMap = new MyMap();

	    	if(formatedNow.equals("20231022")) {
	    	    /** 특정날짜 상태여부 세팅 */
	    	    logger.info("현재날짜(" + formatedNow + ")가 특정작업일입니다.");
                resultMap = (MyMap)session.selectOne("NeoMapper9020.selectTargetStatCd");
	    	} else {
	            /** 가상계좌 대기 상태 조회 : 웹요청, 종료(23:50), 개시(03:00) */
	            resultMap = (MyMap)session.selectOne("NeoMapper9020.selectStandbyStatCd");
	    	}

			logger.info("select결과 : " + resultMap);

			/** 결과없으면 return */
			if( StringUtil.isEmpty(resultMap) ) {
				logger.info("없음");
				return 0;
			}

			/** 다빈치 connect */
	        try {
	            socket = new Socket();
	            socket.connect(new InetSocketAddress(appRes.getString("EtaxDavinchiIp"), appRes.getInteger("EtaxDavinchiPort")));
	            logger.info("다빈치 접속성공");

	        }catch(IOException e) {
	        	throw new Exception("다빈치 접속오류");
	        }

	        /** 다빈치 send */
	        MapForm paramMap = new MapForm();
	        paramMap.setMap("BIZ_DVCD", resultMap.getString("STANDBY_STAT_CD") );//업무구분코드
	        paramMap.setMap("ENPC", "DGCYB001" );//업체코드
            paramMap.setMap("ORGTCD", "04" );//기관코드

	        CyberFieldList cyberFieldList = new CyberFieldList();
	        @SuppressWarnings("unchecked")
			byte[] sendBuff = cyberFieldList.getBuff(paramMap);

	        OutputStream os = socket.getOutputStream();
            os.write(sendBuff);
            os.flush();
            logger.info("송신 데이터1 : " + new String(sendBuff));

            /** 다빈치 receive */
            socket.setSoTimeout(10000);//10초 timeout
            InputStream is = socket.getInputStream();
            byte[] buffer = new byte[128];
            is.read(buffer);
            logger.info("수신 데이터1 : " + new String(buffer));

            paramMap.setMap("ENPC", "DGCYB002" );//업체코드
            paramMap.setMap("ORGTCD", "UD" );//기관코드
            @SuppressWarnings("unchecked")
            byte[] sendBuff2 = cyberFieldList.getBuff(paramMap);

            os.write(sendBuff2);
            os.flush();
            logger.info("송신 데이터2 : " + new String(sendBuff2));
            /** 다빈치 receive */
            byte[] buffer2 = new byte[128];
            is.read(buffer2);
            logger.info("수신 데이터2 : " + new String(buffer2));

            HashMap<String, Object> receiveMap = cyberFieldList.parseBuff(buffer2);
            //logger.info("receiveMap : " + receiveMap);

            String strRpcd = String.valueOf(receiveMap.get("RPCD"));//응답코드
            if( "000".equals(strRpcd) ) {
            	logger.info("정상응답");

                /** 가상계좌상태 update */
                session.update("NeoMapper9020.updateWorkStat", resultMap);

                /** 가상계좌상태이력 insert */
                session.update("NeoMapper9020.insertHistory", resultMap);
            } else {
            	logger.info("비정상응답 : " + strRpcd);
            }

            session.commit();

		} catch (Exception ex) {
			logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
			session.rollback();
			return -1;

		} finally {
		    if( null != socket ) {
				try {
					socket.close();
					logger.info("socket close!");
				} catch (IOException e) {
					logger.error("socket close 에러");
				}
			}

			session.close();
			logger.info("session close!");
		}
		return 0;
	}

    public String getTime(String format)
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(calendar.getTime());
    }
}