/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출  공통 코드 조회처리
 *  기  능  명 : e호조요청건 공통코드 조회건 SAM파일생성
 *
 *  클래스  ID : NeoDMWS3050
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 *  신상훈       다산(주)      2022.11.23         %01%             최초작성
 */
package com.uc.expenditure.daegu.daemon;

import  java.io.Reader;
import  java.util.ArrayList;
import java.util.Properties;

import  org.apache.ibatis.io.Resources;
import  org.apache.ibatis.session.SqlSession;
import  org.apache.ibatis.session.SqlSessionFactory;
import  org.apache.ibatis.session.SqlSessionFactoryBuilder;
import  org.apache.log4j.Logger;
import  org.apache.log4j.xml.DOMConfigurator;
import  com.uc.expenditure.daegu.bank.DgMessageService;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS3050 implements Runnable
{
    @SuppressWarnings("unchecked")
    /* 로그 변수 선언 */
    private static Logger   logger = Logger.getLogger(NeoDMWS3050.class);

    private static SqlSessionFactory sqlMapper = null;
    private static Thread[] self = new Thread[10];

    //연계전송상태코드 N:전송대기, P:전송중, S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패
    private static String STAT_A = "A";
    private static String STAT_N = "N";
    private static String STAT_S = "S";

    boolean bAlreadyConfirm = false;
    boolean bAlreadyConfirm2 = false;
    static MyMap appRes = new MyMap();
    private static DgMessageService service = null;

    private int comCdId  = 0;

    // 10개, 수신
    /*
     *  CA : 회계구분정보
     *  CD : 부서정보
     *  CU : 사용자정보
     *  TB : 분야부문
     *  TS : 세출통계목
     *  TC : 세입목코드
     *  TM : 세입목코드매핑
     *  CK : 현금종류송신
     *  WA : 금고은행시스템출금계좌검증정보송신
     *  EI : 세출한도계좌개설정보통합금고송신
     */
    private String[] comCdArray  = {"CA", "CD", "CU", "TB", "TS", "TC", "TM", "CK", "WA", "EI"};

    public NeoDMWS3050(int i) {
		this.comCdId = i;
	}

    /**
     * 메인함수
     * @param args
     */
    public static void main(String args[])
    {
        /* 로그설정 파일을 읽는다. 컨텍스트를 기준으로 한다 */
        DOMConfigurator.configure(NeoDMWS3050.class.getResource("/conf/log4j.xml"));

        logger.debug("===== [" + NeoDMWS3050.class.getSimpleName() + "] 시작 =====");

        for(int i=0;i< self.length;i++){
        	/* 클래스 인스턴스 생성 */
        	NeoDMWS3050  neoDMWS3050 = new NeoDMWS3050(i);
        	/* 인스턴스를 쓰레드(데몬)로 실행 */
            self[i] = new Thread(neoDMWS3050);
            self[i].setDaemon(true);
            self[i].start();
        }

        for(int i=0;i< self.length;i++){
            try {
				self[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        logger.debug("===== [" + NeoDMWS3050.class.getSimpleName() + "] 끝 =====");
    }

    /**
     * 쓰레드
     */
    public void run()
    {
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

        while (!Thread.currentThread().isInterrupted()) {
            mainLoop();
            try {
                Thread.sleep(30000);						//30 sec(테스트용)
//              Thread.sleep(60*60*1000);					//1시간 sec (360000)
            } catch (InterruptedException e) {
                logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
                break;
            }
        }
    }

    /**
     * 메인루프
     * @return
     */
    @SuppressWarnings("unchecked")
	public int mainLoop()
    {
        // 데이터베이스 세션 생성, 자동확약 끔.
    	SqlSession  session = null;
        try {
            session = sqlMapper.openSession(false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        // 휴일여부 체크
        MyMap paramMap = new MyMap();

        paramMap.setString("govcode",comCdArray[comCdId]);

        String comCodeType =   paramMap.get("govcode").toString();

        paramMap.setString("현재일자", Utils.getCurrentDate());
        paramMap.setString("CURR_DATE", Utils.getCurrentDate());

        logger.info("공통정보 - TCM1482(회계정보), TCM2061S(부서정보), TCM4091S(사용자정보) 등  공통코드 수신 테이블을 조회합니다.");

        if("CA".equals(comCodeType)) {					// TCM1482 (회계정보) ACCO
        	ArrayList<MyMap> veriAccoList = null;

        	// 회계정보 parameter
            MyMap paramAcco = new MyMap();
            paramAcco.setString("LINK_TRSM_STAT_CD", STAT_N);								// 연계전송상태코드

        	logger.info("공통코드 - TCM1482S(회계정보)  송신 테이블을 조회합니다");
        	veriAccoList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTCM1482S", paramAcco);

        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriAccoList.isEmpty()) {
        		logger.info("공통정보 - TCM1482(회계정보)  송신 테이블  조회완료 " + veriAccoList.size() );

        		try {

                    //paramMap govcode, CURR_DATE, TRNX_NO
        			int i_success = InsertService(session, paramMap, veriAccoList);
        			if (i_success < 0){
                         session.rollback();
                         session.close();

                         logger.debug("회계구분정보(TCM1482  등록 및 e-호조 테이블에 업데이트 bit set 도중 오류 발생!");
                         //continue;
                     }
                     session.commit();
              } catch (Exception ex) {
                  	logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
                  	session.rollback();
                  	session.close();
                  	return -1;
              }
        	}

        }else if("CD".equals(comCodeType)) {						// 부서 TCM2061 DEPT
        	ArrayList<MyMap> veriDeptList = null;

        	logger.info("공통코드 - TCM2061S(부서정보) 송신 테이블을 조회합니다.");
        	// 부서정보 parameter
            MyMap paramDept = new MyMap();
            paramDept.setString("LINK_TRSM_STAT_CD", STAT_N);								// 연계전송상태코드

        	veriDeptList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTCM2061S", paramDept);

        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriDeptList.isEmpty()) {
        		logger.info("공통정보 - TCM2061S(부서정보)  송신 테이블  조회완료 " + veriDeptList.size() );
        		try {

        		    session.update("NeoMapper3050.updateOldTCM2061");  // 이전날짜 LAF_CD + 1001000

                    int i_success = InsertService(session, paramMap, veriDeptList);

                    if (i_success != 0){
	                      session.rollback();
	                      session.close();
	                      logger.debug("부서(TCM2061등록 및 TCM2061S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();

              } catch (Exception ex) {
                  logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
                  session.rollback();
                  session.close();
                  return -1;
              }
            }
        }else if("CU".equals(comCodeType)) {		// 사용자정보

        	// 사용자정보 parameter
        	MyMap paramUser = new MyMap();
            paramUser.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> veriUserList = null;
        	logger.info("공통정보 - TCM4091S(사용자정보)  송신 테이블을 조회합니다.");

        	veriUserList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTCM4091S", paramUser);

        	paramMap.setString("TRNX_TYPE", "CU");							// 거래구분

        	if(!veriUserList.isEmpty()) {
        		logger.info("공통정보 - TCM4091S(사용자정보 )  송신 테이블  조회완료 " + veriUserList.size() );
        		try {

                    int i_success = InsertService(session, paramMap, veriUserList);

	     			 if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("사용자(TCM4091등록 및 TCM4091S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("TB".equals(comCodeType)) {		//분야부문 TCM1222 SECT

        	// 분야부문 parameter
            MyMap paramSect = new MyMap();
            paramSect.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> veriSectList = null;
        	logger.info("공통정보 - TCM1222S(분야부문)  송신 테이블을 조회합니다.");

        	veriSectList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTCM1222S", paramSect);

        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriSectList.isEmpty()) {
        		logger.info("공통정보 - TCM1222S(분야부문)  송신 테이블을 조회 완료 " + veriSectList.size() );
        		try {

                    int i_success = InsertService(session, paramMap, veriSectList);

                    if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("분야부문(TCM1222등록 및 TCM1222S에 업데이트 bit set 도중 오류 발생!");
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("TS".equals(comCodeType)) {		// 세출통계목 TCM1172S AEST

        	MyMap paramAest = new MyMap();
            paramAest.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> veriAestList = null;
        	logger.info("공통정보 - TCM1172S(세출통계목)  송신 테이블을 조회합니다.");

        	veriAestList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTCM1172S", paramAest);

        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriAestList.isEmpty()) {
        		logger.info("공통정보 - TCM1172S(세출통계목)  송신 테이블을 조회 완료 " + veriAestList.size() );
        		try {

                    int i_success = InsertService(session, paramMap, veriAestList);

	     			if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("세출통계목부문(TCM1172등록 및 TCM1172S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("TC".equals(comCodeType)) {			// 세입목코드 TFM1051S TXCD

        	MyMap paramTxcd = new MyMap();
        	paramTxcd.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> veriTxcdList = null;
        	logger.info("Neo User 공통정보 - TFM1051S(세입목코드)  송신 테이블을 조회합니다.");

        	veriTxcdList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTFM1051S", paramTxcd);
        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriTxcdList.isEmpty()) {
        		logger.info("공통정보 - TFM1051S(세입목코드)  송신 테이블을 조회 완료 " + veriTxcdList.size() );
        		try {
                    int i_success = InsertService(session, paramMap, veriTxcdList);
	     			if (i_success != 0){
	                      session.rollback();
	                      session.close();
	                      logger.debug("세입목코드(TFM1051등록 및 TFM1051S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("TM".equals(comCodeType)) {			// 세입목맵핑 TFM4020S TXMP TM

        	MyMap paramTxmp = new MyMap();
        	paramTxmp.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> veriTxmpList = null;
        	logger.info("Neo User 공통정보 - TFM4020S(세입목코드매핑)  송신 테이블을 조회합니다.");

        	veriTxmpList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTFM4020S", paramTxmp);
        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!veriTxmpList.isEmpty()) {
        		logger.info("공통정보 - TFM4020S(세입목코드맵핑)  송신 테이블을 조회 완료 " + veriTxmpList.size() );
        		try {
                    int i_success = InsertService(session, paramMap, veriTxmpList);

	     			if (i_success != 0){
	                      session.rollback();
	                      session.close();
	                      logger.debug("세입목코드(TFM4020등록 및 TFM4020S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("CK".equals(comCodeType)) {			// 현금종류송신 TFD3602S CASH

        	MyMap paramTxmp = new MyMap();
        	paramTxmp.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> cashTxmpList = null;
        	logger.info("Neo User 공통정보 - TFD3602S(현금종류송신)  송신 테이블을 조회합니다.");

        	cashTxmpList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTFD3602S", paramTxmp);
        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!cashTxmpList.isEmpty()) {
        		logger.info("공통정보 - TFD3602S(현금종류송신)  송신 테이블을 조회 완료 " + cashTxmpList.size() );
        		try {
                    int i_success = InsertService(session, paramMap, cashTxmpList);

                    if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("현금종류송신(TFD3602등록 및 TFD3602S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("WA".equals(comCodeType)) {		// 금고은행시스템출금계좌검증정보송신 TFM6210S WDAC

        	MyMap paramTxmp = new MyMap();
        	paramTxmp.setString("LINK_TRSM_STAT_CD", STAT_N);

        	ArrayList<MyMap> wdacTxmpList = null;
        	logger.info("Neo User 공통정보 - TFM6210S(금고은행시스템출금계좌검증정보송신)  송신 테이블을 조회합니다.");

        	wdacTxmpList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTFM6210S", paramTxmp);
        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!wdacTxmpList.isEmpty()) {
        		logger.info("공통정보 - TFD3602S(금고은행시스템출금계좌검증정보송신)  송신 테이블을 조회 완료 " + wdacTxmpList.size() );
        		try {
                    int i_success = InsertService(session, paramMap, wdacTxmpList);

                    if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("금고은행시스템출금계좌검증정보송신(TFM6210등록 및 TFM6210S에 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else if("EI".equals(comCodeType)) {		//세출한도계좌개설정보통합금고송신 TFC0011

        	MyMap paramTxmp = new MyMap();

        	ArrayList<MyMap> eiTxmpList = null;
        	logger.info("공통정보 - TFC0011(세출한도계좌개설정보통합금고송신)  송신 테이블을 조회합니다.");

        	eiTxmpList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.getTFC0011", paramTxmp);
        	paramMap.setString("TRNX_TYPE",comCodeType);							// 거래구분

        	if(!eiTxmpList.isEmpty()) {
        		logger.info("공통정보 - TFC0011(세출한도계좌개설정보통합금고송신)  송신 테이블을 조회 완료 " + eiTxmpList.size() );
        		try {
                    int i_success = InsertService(session, paramMap, eiTxmpList);

                    if (i_success != 0){
	                      session.rollback();
	                      session.close();

	                      logger.debug("세출한도계좌개설정보통합금고송신(TFC0011 업데이트 bit set 도중 오류 발생!");
	                      //continue;
	                  }
	                  session.commit();
	            } catch (Exception ex) {
	                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
	                session.rollback();
	                session.close();
	                return -1;
	            }
            }
        }else{
        	//ERROR
        }
        session.close();
        return 0;
    }

    //e-세출 테이블 insert 및 송신테이블 update
    private int InsertService(SqlSession session, MyMap param, ArrayList<MyMap> veriList) throws Exception
    {
    	logger.debug("공통코드 e-세출 테이블 insert 및 송신테이블 update TRNX_TYPE : " + param.getString("TRNX_TYPE") + " , " + veriList.size() );

    	//param govcode, CURR_DATE, TRNX_NO
    	param.setString("JOB_SYS_CD", "01");						//작업시스템코드
        param.setString("JOB_STAT_CD", "01");						//작업상태코드


        if("CA".equals(param.getString("TRNX_TYPE"))){				// 회계정보

        	int nErrCount = 0;
        	int caListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {				// group by 된것

        		long beforeTimeCa = System.currentTimeMillis();
        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		ArrayList<MyMap> accoList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTCM1482S", veriParam);
        		caListCnt = accoList.size();

        		MyMap trnxParam = new MyMap();

    			trnxParam.setString("DLNG_CD", "CA");										// 파일순번용 거래구분(회계정보)
    			trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());					// 파일순번용 거래일자
    			trnxParam.setString("JUM_NO", "036");										// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_NO", "CA" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			// 거래구분 + 거래일자+ seq

        		for (int j=0; j < accoList.size(); j++) {

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		            MyMap st = accoList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);				// 연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("회계정보 입력 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		        		+ "],자치단체코드[" + st.getString("LAF_CD")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],지방자치단체명[" + st.getString("LAF_NM")
		                + "],회계연도[" + st.getString("FYR")
		                + "],회계구분마스터코드[" + st.getString("ACNT_DV_MSTR_CD")
		                + "],회계구분마스터명[" + st.getString("ACNT_DV_MSTR_NM")
		                + "],회계구분코드[" + st.getString("ACNT_DV_CD")
		                + "],회계구분명[" + st.getString("ACNT_DV_NM")
		                + "],사용유무[" + st.getString("USE_YN")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "]");

		        		session.update("NeoMapper3050.updateOldTCM1482", st);   // insert 전에 이전 동일한 자료 사용여부 N으로 update

                        logger.info("#### 이전자료 사용여부 N으로 UPDATE 실행완료 - 신규데이터 INSERT 시작");

			        	inChkCnt = session.insert("NeoMapper3050.insertTCM1482", st);			 // 로컬 DB(TCM1482) 에 등록 */

			        	 if (inChkCnt== 0){							 // 로컬 DB(TCM1482) 에  insert */
			                 logger.error("회계정보송신(TCM1482) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 return -1;
			             }

			        	//     	 logger.debug("회계정보(TCM1482) 등록 완료");
			        	 upChkCnt = session.insert("NeoMapper3050.updateTCM1482S", st);

			        	 if (upChkCnt == 0){							 // 로컬 DB(TCM1482S) 에  update */
			                 logger.error("회계정보송신(TCM1482S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 return -1;

			             }
	//	        	 logger.debug("회계정보(TCM1482S UPDATE 완료");
		        	}catch(Exception e){
		        		session.rollback();
		        		logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	}
	//                session.commit();
	        	}
        		long afterTimeCa = System.currentTimeMillis();
    			long secDiffTimeCa = (afterTimeCa - beforeTimeCa) / 1000;

    			logger.info("회계구분정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + caListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeCa + " 초 ]");
        	}//FOR


        }else if("CD".equals(param.getString("TRNX_TYPE"))){			// 부서정보

        	int nErrCount = 0;
        	int cdListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeCd = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		ArrayList<MyMap> deptList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTCM2061S", veriParam);
        		cdListCnt = deptList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "CD");										// 파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());					// 파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");										// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "CD");							// 거래구분
                param.setString("TRNX_NO", "CD" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < deptList.size(); j++) {
	        		MyMap st = deptList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

	        		logger.trace("부서정보입력 : ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
	                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
	                + "],자치단체코드[" + st.getString("LAF_CD")
	                + "],지방자치단체명[" + st.getString("LAF_NM")
	                + "],GCC부서코드[" + st.getString("GCC_DEPT_CD")
	                + "],부서코드[" + st.getString("DEPT_CD")
	                + "],부서명[" + st.getString("DEPT_NM")
	                + "],관서코드[" + st.getString("GOF_CD")
	                + "],관서명[" + st.getString("GOF_NM")
	                + "],실국코드[" + st.getString("SLNGK_CD")
	                + "],실국명[" + st.getString("SLNGK_NM")
	                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
	                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
	                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
	                + "],거래번호[" + st.getString("TRNX_NO")
	                + "]");

	        		st.setString("TRNX_NO", param.getString("TRNX_NO"));					// 거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										// 연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

	        		try {
//	        			logger.debug("(Neo) param " + st );
	        			inChkCnt = session.insert("NeoMapper3050.insertTCM2061", st);			 // 로컬 DB(TCM2061) 에 등록 */

	        			if(inChkCnt == 0) {
	        				logger.error("부서정보(TCM2061)  입력이 제대로 이루어지지 않았습니다. " + st);
	    	        		session.rollback();
	    	        		session.close();
	    	        		return -1;
	        			}

	        			upChkCnt = session.insert("NeoMapper3050.updateTCM2061S", st);			 // 로컬 DB(TCM2061S) 에  update */

	    	        	if (upChkCnt == 0){
	    	        		logger.error("부서정보(TCM2061S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
	    	        		session.rollback();
	    	        		session.close();
	    	        		return -1;
	             		}

//	    	        	logger.debug("부서정보(TCM2061) UPDATE 완료");
	        		}catch(Exception e){
		        		session.rollback();
		                logger.error("부서정보 업데이트 오류[" + e.getLocalizedMessage() + "]");
		                return -99;
		                //continue;
		        	 }
	//                session.commit();
	        	}				//for
        		long afterTimeCd= System.currentTimeMillis();
    			long secDiffTimeCd = (afterTimeCd - beforeTimeCd) / 1000;


    			logger.info("부서정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + cdListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeCd + " 초 ]");
        	}
        	return nErrCount;

        }else if("CU".equals(param.getString("TRNX_TYPE"))) {							// 사용자정보 USER TCM4091

        	int nErrCount = 0;
        	int cuListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeCu = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

//        		logger.debug("user veriParam " + veriParam);

        		ArrayList<MyMap> userList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTCM4091S", veriParam);
        		cuListCnt = userList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "CU");												// 파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							// 파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "CU");							//거래구분
                param.setString("TRNX_NO", "CU" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < userList.size(); j++) {
	        		MyMap st = userList.get(j);				//VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		st.setString("USER_PW", "");					//패스워드
		        	st.setString("USER_PW", "");					//패스워드
		        	st.setString("SYS_AUTH", "00");					//시스템권한, 공통코드로 들어오는것 00
		        	st.setString("BF_LOGIN_DT", "");				//이전로그인일시
		        	st.setString("LT_LOGIN_DT", "");				//최종로그인일시
		        	st.setString("USE_N_DT", "");					//사용중지일시
		        	st.setString("PW_CHG_DT", "");					//비밀번호변경일시
		        	st.setString("PW_ERR_CNT", 0);				//비밀번호오류횟수
	//	        	st.setString("RGSTR_DT", "");					//등록일시

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										//연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	logger.trace("사용자정보 : ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
	                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
	                + "],자치단체코드[" + st.getString("LAF_CD")
	                + "],회계구분코드[" + st.getString("ACNT_DV_CD")
	                + "],회계별업무담당코드[" + st.getString("GCC_DEPT_CD")
	                + "],회계별업무담당명[" + st.getString("ADC_NM")
	                + "],관서코드[" + st.getString("GOF_CD")
	                + "],부서코드[" + st.getString("DEPT_CD")
	                + "],GCC부서코드[" + st.getString("GCC_DEPT_CD")
	                + "],사용자ID[" + st.getString("USR_ID")
	                + "],사용자명[" + st.getString("USR_NM")
	                + "],승인권한여부[" + st.getString("APRV_RGH_YN")

	                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
	                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
	                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
	                + "],응용처리일시[" + st.getString("PRCT_PRCS_DT")
	                + "],연계상태처리일시[" + st.getString("LINK_STAT_PRCS_DT")
	                + "],회계연도[" + st.getString("FYR")
	                + "],패스워드[" + st.getString("USER_PW")
	                + "],시스템권한[" + st.getString("SYS_AUTH")
	                + "],이전로그인일시[" + st.getString("BF_LOGIN_DT")
	                + "],최종로그인일시[" + st.getString("LT_LOGIN_DT")
	                + "],사용중지일시[" + st.getString("USE_N_DT")

	                + "],비밀번호변경일시[" + st.getString("PW_CHG_DT")
	                + "],비밀번호오류횟수[" + st.getString("PW_ERR_CNT")
	                + "],등록일시[" + st.getString("RGSTR_DT")
	                + "]");

		        	try {

//		        		session.insert("NeoMapper3050.insertTCM4091", st);			 // 로컬 DB(TCM4091) 에 등록 */

	    	        	if (session.update("NeoMapper3050.updateTCM4091S", st) == 0){
	    	        		logger.error("사용자정보(TCM2061S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
	    	        		session.rollback();
	    	        		return -1;
	             		}

	    	        	logger.debug("사용자정보(TCM4091) UPDATE 완료");
	        		}catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                continue;
		        	 }

	        	}
        		long afterTimeCu= System.currentTimeMillis();
    			long secDiffTimeCu = (afterTimeCu - beforeTimeCu) / 1000;

    			logger.info("사용자정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
						"건수 [" + cuListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeCu + " 초 ]");
        	}
        }else if("TB".equals(param.getString("TRNX_TYPE"))) {							// 분야 TCM1222S SECT

        	int nErrCount = 0;
        	int tbListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeTb = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("sect veriParam " + veriParam);

        		ArrayList<MyMap> sectList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTCM1222S", veriParam);
        		tbListCnt = sectList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "TB");												//파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							//파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												//파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "TB");							// 거래구분
                param.setString("TRNX_NO", "TB" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < sectList.size(); j++) {
	        		MyMap st = sectList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					//거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										//연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("분야부문 입력 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		        		+ "],자치단체코드[" + st.getString("LAF_CD")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],회계연도[" + st.getString("FYR")
		                + "],분야코드[" + st.getString("FLD_CD")
		                + "],분야명[" + st.getString("FLD_NM")
		                + "],부문코드[" + st.getString("SECT_CD")
		                + "],부문명[" + st.getString("SECT_NM")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "],DMND_YMD[" + st.getString("DMND_YMD")
		                + "]");

		        		inChkCnt = session.insert("NeoMapper3050.insertTCM1222", st);

			        	 if (inChkCnt== 0){
			                 logger.error("분야부문(TCM1222) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 return -1;
			             }

			        	 upChkCnt = session.insert("NeoMapper3050.updateTCM1222S", st);

			        	 if (upChkCnt == 0){
			                 logger.error("분야부문(TCM1222S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 return -1;
			        	 }

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();
	        	}
        		long afterTimeTb = System.currentTimeMillis();
    			long secDiffTimeTb = (afterTimeTb - beforeTimeTb) / 1000;

    			logger.info("분야정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + tbListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeTb + " 초 ]");
        	}

        }else if("TS".equals(param.getString("TRNX_TYPE"))) {							// 세출통계목 TCM1172 AEST
        	int nErrCount = 0;
        	int tsListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeTs = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("aest veriParam " + veriParam);

        		ArrayList<MyMap> aestList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTCM1172S", veriParam);
        		tsListCnt = aestList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "TS");												// 파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							// 파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "TS");							// 거래구분
                param.setString("TRNX_NO", "TS" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < aestList.size(); j++) {
	        		MyMap st = aestList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					// 거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										// 연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("세출통계목 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		        		+ "],자치단체코드[" + st.getString("LAF_CD")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],회계연도[" + st.getString("FYR")
		                + "],세출편성목코드[" + st.getString("ANE_CPLBD_CD")
		                + "],세출편성목명[" + st.getString("ANE_CPLBD_NM")
		                + "],세출통계목코드[" + st.getString("ANE_STMK_CD")
		                + "],세출통계목명[" + st.getString("SECT_NM")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "],DMND_YMD[" + st.getString("DMND_YMD")
		                + "]");

		        		inChkCnt = session.insert("NeoMapper3050.insertTCM1172", st);

		        		if (inChkCnt== 0){
			                 logger.error("세출통계목(TCM1172) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
		        		}

		        		upChkCnt = session.insert("NeoMapper3050.updateTCM1172S", st);

		        		if (upChkCnt == 0){
			                 logger.error("세출통계목(TCM1172) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
		        		}

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();

	        	}
        		long afterTimeTs = System.currentTimeMillis();
    			long secDiffTimeTs = (afterTimeTs - beforeTimeTs) / 1000;

    			logger.info("세출통계목 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + tsListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeTs + " 초 ]");
        	}
        }else if("TC".equals(param.getString("TRNX_TYPE"))) {			// 세입목 TFM1051S TXCD
        	int nErrCount = 0;
        	int tcListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeTc = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("txcd veriParam " + veriParam);

        		ArrayList<MyMap> txcdList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTFM1051S", veriParam);
        		tcListCnt = txcdList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "TC");												// 파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							// 파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "TC");							// 거래구분
                param.setString("TRNX_NO", "TC" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < txcdList.size(); j++) {
	        		MyMap st = txcdList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					// 거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										// 연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("세입목코드 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],회계연도[" + st.getString("FYR")
		                + "],세입장코드[" + st.getString("RVJG_CD")
		                + "],세입장명[" + st.getString("RVJG_NM")
		                + "],세입관코드[" + st.getString("TXRV_GYAN_CD")
		                + "],세입관명[" + st.getString("TXRV_GYAN_NM")
		                + "],세입항코드[" + st.getString("TXRV_HANG_CD")
		                + "],세입항명[" + st.getString("TXRV_HANG_NM")
		                + "],세입목코드[" + st.getString("ARMK_CD")
		                + "],세입목명[" + st.getString("ARMK_NM")
		                + "],사용여부[" + st.getString("USE_YN")
		                + "],변경적용일자[" + st.getString("CHG_APLCN_YMD")
		                + "],폐지일자[" + st.getString("ABL_YMD")
		                + "],변경일자[" + st.getString("CHG_YMD")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "]");

		        		inChkCnt = session.insert("NeoMapper3050.insertTFM1051", st);

		        		if (inChkCnt== 0){
			                 logger.error("세입목코드(TFM1051) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
		        		}

		        		upChkCnt = session.insert("NeoMapper3050.updateTFM1051S", st);

		        		if (upChkCnt == 0){
			                 logger.error("세입목코드(TFM1051S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
		        		}

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();
	        	}
        		long afterTimeTc = System.currentTimeMillis();
    			long secDiffTimeTc = (afterTimeTc - beforeTimeTc) / 1000;
    			logger.info("세입목코드정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + tcListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeTc + " 초 ]");
        	}
        }else if("TM".equals(param.getString("TRNX_TYPE"))) {			// 세입목코드매핑 TFM4020S TXMP

        	int nErrCount = 0;
        	int tmListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeTm = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("txmp veriParam " + veriParam);

        		ArrayList<MyMap> txmpList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selecTFM4020S", veriParam);
        		tmListCnt = txmpList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "TM");												// 파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							// 파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												// 파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "TM");							// 거래구분
                param.setString("TRNX_NO", "TM" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < txmpList.size(); j++) {
	        		MyMap st = txmpList.get(j);				// VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					// 거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										// 연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("세입목코드매핑 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],회계연도[" + st.getString("FYR")
		                + "],세입장코드[" + st.getString("RVJG_CD")
		                + "],세입장명[" + st.getString("RVJG_NM")
		                + "],세입관코드[" + st.getString("TXRV_GYAN_CD")
		                + "],세입관명[" + st.getString("TXRV_GYAN_NM")
		                + "],세입항코드[" + st.getString("TXRV_HANG_CD")
		                + "],세입항명[" + st.getString("TXRV_HANG_NM")
		                + "],세입목코드[" + st.getString("ARMK_CD")
		                + "],세입목명[" + st.getString("ARMK_NM")
		                + "],대표세입목코드[" + st.getString("RPRS_TXRV_SBJ_CD")
		                + "],대표세입목명[" + st.getString("RPRS_TXRV_SBJ_NM")
		                + "],사용여부[" + st.getString("USE_YN")
		                + "],변경적용일자[" + st.getString("CHG_APLCN_YMD")
		                + "],폐지일자[" + st.getString("ABL_YMD")
		                + "],변경일자[" + st.getString("CHG_YMD")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "]");

		        		inChkCnt = session.insert("NeoMapper3050.insertTFM4020", st);

		        		if (inChkCnt== 0){
			                 logger.error("세입목코드매핑(TFM4020) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
			            }

		        		upChkCnt = session.insert("NeoMapper3050.updateTFM4020S", st);

			        	if (upChkCnt == 0){
			        		logger.error("세입목코드매핑(TFM4020S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                nErrCount++;
			                session.rollback();
			                session.close();
			                return -1;
			        	}

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();
	        	}
        		long afterTimeTm = System.currentTimeMillis();
    			long secDiffTimeTm = (afterTimeTm - beforeTimeTm) / 1000;

    			logger.info("세입목코드매핑정보 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + tmListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeTm + " 초 ]");
        	}

        }else if("CK".equals(param.getString("TRNX_TYPE"))) {			// 현금종류송신 TFD3602S CK CASH
        	int nErrCount = 0;
        	int ckListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeCk = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("cash veriParam " + veriParam);

        		ArrayList<MyMap> cashList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTFD3602S", veriParam);
        		ckListCnt = cashList.size();

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "CK");												//파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							//파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												//파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "CK");							//거래구분
                param.setString("TRNX_NO", "CK" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < cashList.size(); j++) {
	        		MyMap st = cashList.get(j);				//VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					//거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										//연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("현금종류매핑 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		        		+ "],지방자치단체코드[" + st.getString("LAF_CD")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],출금계좌일련번호[" + st.getString("DRW_ACC_SNUM")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],현금유형코드[" + st.getString("CASH_TY_CD")
		                + "],현금종목코드[" + st.getString("CASH_ITM_CD")
		                + "],현금유형명[" + st.getString("CASH_TY_NM")
		                + "],현금종목명[" + st.getString("CASH_ITM_NM")
		                + "],사용여부[" + st.getString("USE_YN")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "]");

		        		inChkCnt = session.insert("NeoMapper3050.insertTFD3602", st);			 // 로컬 DB(TFD3602) 에 등록 */

			        	 if (inChkCnt== 0){							 // 로컬 DB(TFD3602) 에  insert */
			                 logger.error("현금종류매핑(TFD3602) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
			             }

			        	 upChkCnt = session.insert("NeoMapper3050.updateTFD3602S", st);

			        	 if (upChkCnt == 0){							 // 로컬 DB(TCM1482S) 에  update */
			                 logger.error("현금종류송신(TFD3602S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;

			        	 }

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();
        		}
        		long afterTimeCk = System.currentTimeMillis();
    			long secDiffTimeCk = (afterTimeCk - beforeTimeCk) / 1000;

    			logger.info("현금종류송신 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + ckListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeCk + " 초 ]");
        	}

	    }else if("WA".equals(param.getString("TRNX_TYPE"))) {		// 금고은행시스템출금계좌검증정보송신 TFM6210S WDAC
        	int nErrCount = 0;
        	int waListCnt = 0;

        	for (int i=0; i < veriList.size(); i++) {					//group by 된것

        		long beforeTimeWa = System.currentTimeMillis();

        		MyMap veriParam = new MyMap();
        		veriParam.setString("LINK_TRSC_ID", veriList.get(i).getString("LINK_TRSC_ID"));
        		veriParam.setString("LAF_CD", veriList.get(i).getString("LAF_CD"));

        		logger.debug("wdac veriParam " + veriParam);

        		ArrayList<MyMap> wdacList = (ArrayList<MyMap>)session.selectList("NeoMapper3050.selectTFM6210S", veriParam);

        		MyMap trnxParam = new MyMap();
        		trnxParam.setString("DLNG_CD", "WA");												//파일순번용 거래구분(부서)
        		trnxParam.setString("DLNG_YMD", Utils.getCurrentDate());							//파일순번용 거래일자
        		trnxParam.setString("JUM_NO", "036");												//파일순번용 점번(본점으로 셋팅)

                long seqNo = getSqNo(trnxParam);

                String  filesqno = String.format("%06d", seqNo);

                param.setString("TRNX_TYPE", "WA");							//거래구분
                param.setString("TRNX_NO", "WA" + param.getString("CURR_DATE").toString().substring(2) + filesqno);			//거래구분 + 거래일자+ seq

        		for (int j=0; j < wdacList.size(); j++) {
	        		MyMap st = wdacList.get(j);				//VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

	        		int inChkCnt = 0;
	        		int upChkCnt = 0;

		        	st.setString("TRNX_NO", param.getString("TRNX_NO"));					//거래번호
		        	st.setString("JOB_SYS_CD",  param.getString("JOB_SYS_CD"));
		        	st.setString("JOB_STAT_CD",  param.getString("JOB_STAT_CD"));
		        	st.setString("DMND_YMD", param.getString("CURR_DATE"));

		        	st.setString("LINK_TRSM_STAT_CD", STAT_S);										//연계전송상태코드 N:전송대기 P:전송중 S:연계전송성공, F:실패, A:응용호출성공, B:응용호출실패

		        	try {
		        		logger.trace("출금계좌검증정보 ["+ j + "] 연계일련번호 [" + st.getString("LINK_SNUM")
		        		+ "],지방자치단체코드[" + st.getString("LAF_CD")
		                + "],연계트랜잭션ID[" + st.getString("LINK_TRSC_ID")
		                + "],출금계좌일련번호[" + st.getString("DRW_ACC_SNUM")
		                + "],회계연도[" + st.getString("FYR")
		                + "],지출자금구분코드[" + st.getString("EP_FD_DV_CD")
		                + "],회계구분코드[" + st.getString("ACNT_DV_CD")
		                + "],경비구분코드[" + st.getString("EXPS_DV_CD")
		                + "],회계별업무담당구분코드[" + st.getString("DEPT_CD")
		                + "],관서코드[" + st.getString("GOF_CD")
		                + "],은행코드[" + st.getString("BANK_CD")
		                + "],암호화계좌번호[" + st.getString("ECRP_ACTNO")
		                + "],출금계좌관리번호[" + st.getString("DRW_ACC_MNG_NO")
		                + "],사용자명[" + st.getString("USR_NM")
		                + "],사용여부[" + st.getString("USE_YN")
		                + "],등록사용자ID[" + st.getString("RGSTR_USR_ID")
		                + "],변경사용자ID[" + st.getString("CHG_USR_ID")
		                + "],변경일자[" + st.getString("CHG_YMD")
		                + "],비고내용[" + st.getString("RMKS_CN")
		                + "],재배정여부[" + st.getString("RAT_YN")
		                + "],승인상태코드[" + st.getString("APRV_STAT_CD")
		                + "],승인사용자ID[" + st.getString("APRV_USR_ID")
		                + "],승인일시[" + st.getString("APRV_DT")
		                + "],승인대상사용자ID[" + st.getString("APRV_TRGT_USR_ID")
		                + "],반려사유내용[" + st.getString("RJCT_RSON_CN")
		                + "],최초등록사용자ID[" + st.getString("FRST_RGSTR_USR_ID")
		                + "],최초등록일시[" + st.getString("FRST_RGSTR_DT")
		                + "],최종수정사용자ID[" + st.getString("LAST_MDFCN_USR_ID")
		                + "],최종수정일시[" + st.getString("LAST_MDFCN_DT")
		                + "],금고은행코드[" + st.getString("STBX_BANK_CD")
		                + "],연계처리구분코드[" + st.getString("LINK_PRCS_DV_CD")
		                + "],연계전송상태코드[" + st.getString("LINK_TRSM_STAT_CD")
		                + "],거래번호[" + st.getString("TRNX_NO")
		                + "],작업시스템코드[" + st.getString("JOB_SYS_CD")
		                + "],작업상태코드[" + st.getString("JOB_STAT_CD")
		                + "]");

		        		session.update("NeoMapper3050.updateOldTFM6210", st);   // insert 전에 이전자료 사용여부 N으로 update

		        		logger.info("#### 이전자료 사용여부 N으로 UPDATE 실행완료 - 신규데이터 INSERT 시작");

		        		inChkCnt = session.insert("NeoMapper3050.insertTFM6210", st); // insert

			        	 if (inChkCnt== 0){							 // 로컬 DB(TFM6210) 에  insert */
			                 logger.error("출금계좌검증정보(TFM6210) 입력이 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;
			             }

			        	 upChkCnt = session.insert("NeoMapper3050.updateTFM6210S", st);

			        	 if (upChkCnt == 0){
			                 logger.error("출금계좌검증정보(TFM6210S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
			                 nErrCount++;
			                 session.rollback();
			                 session.close();
			                 return -1;

			        	 }

		        	} catch(Exception e){
		        		session.rollback();
		                logger.error("오류[" + e.getLocalizedMessage() + "]");
		                return -1;
		        	 }
	//                session.commit();
	        	}
        		long afterTimeWa = System.currentTimeMillis();
    			long secDiffTimeWa = (afterTimeWa - beforeTimeWa) / 1000;

    			logger.info("현금종류송신 연계트랜잭션ID ["  + veriList.get(i).getString("LINK_TRSC_ID") + "] " +
    					"자치단체코드 [" + veriList.get(i).getString("LAF_CD") + "] " +
    					"건수 [" + waListCnt + "] 정상적으로 Update 완료  : [ " + secDiffTimeWa + " 초 ]");
        	}
        	}else if("EI".equals(param.getString("TRNX_TYPE"))) {							// 세출한도계좌개설정보통합금고송신 TFC0011

        		long beforeTimeCu = System.currentTimeMillis();
            	int cuListCnt = 0;

            	// 거래번호 채번
            	param.setString("DLNG_CD", "EI");												// 파일순번용 거래구분(부서)
            	param.setString("DLNG_YMD", Utils.getCurrentDate());							// 파일순번용 거래일자
            	param.setString("JUM_NO", "036");												// 파일순번용 점번(본점으로 셋팅)
                long seqNo = getSqNo(param);
                String  filesqno = String.format("%06d", seqNo);
                param.setString("TRNX_NO", "EI" + param.getString("CURR_DATE").toString().substring(2) + filesqno);

                for (int i=0; i < veriList.size(); i++) {					//group by 된것
            		MyMap veriParam = new MyMap();
            		veriParam.setString("GRAM_TRSM_YMD", veriList.get(i).getString("GRAM_TRSM_YMD"));
            		veriParam.setString("GRAM_ID", veriList.get(i).getString("GRAM_ID"));
            		veriParam.setString("TRNX_NO", param.getString("TRNX_NO").toString());
            		logger.debug("user veriParam " + veriParam);

                    if (session.update("NeoMapper3050.updateTFC0011", veriParam) == 0){
    	        		logger.error("세출한도계좌개설정보통합금고송신(TFC0011) 업데이트가 제대로 이루어지지 않았습니다. " + veriParam);
    	        		session.rollback();
    	        		return -1;
             		}
            	}

                long afterTimeCu= System.currentTimeMillis();
    			long secDiffTimeCu = (afterTimeCu - beforeTimeCu) / 1000;

    			logger.info("세출한도계좌개설정보통합금고송신 " +
						"건수 [" + veriList.size() + "] 정상적으로 Update 완료  : [ " + secDiffTimeCu + " 초 ]");

	    }

        return 0;
    }

    // 거래 일련번호 가져오기
    public long getSqNo(MyMap mapForm) throws Exception
    {
        long sqNo = 0;

        SqlSession  ss = sqlMapper.openSession(false);

        try {
            try {
                long nCnt = (Long)ss.selectOne("NeoMapperCommon.nCntSelectNeo", mapForm);

                if (nCnt > 0) {
                    sqNo = (Long)ss.selectOne("NeoMapperCommon.sqNoSelectNeo", mapForm);
                }
            } catch (NullPointerException e) {
                // null 포인터는 무시
                logger.error("SqNoSelect Null Point Error");
            }

            sqNo++;

            mapForm.setLong("FILESQNO", sqNo);
            if (sqNo == 1) {
                ss.insert("NeoMapperCommon.sqNoInsertNeo", mapForm);
            } else {
                ss.update("NeoMapperCommon.sqNoUpdateNeo", mapForm);
            }

            ss.commit();
        } finally {
            ss.close();
        }
        return sqNo;
    }

}
