/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 대량예금주 조회 처리
 *  기  능  명 : e호조요청건 e세출업무원장 전송처리
 *  클래스  ID : NeoDMWS3010
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 *  하상원       다산(주)      2022.10.07         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.expenditure.daegu.bank.DgMessageService;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS3010 implements Runnable {
	@SuppressWarnings("unchecked")
	private static Logger logger = Logger.getLogger(NeoDMWS3010.class);

	/* 데이터베이스 접속을 위한 세션 선언 */
	private static SqlSession session = null;
	private static SqlSessionFactory sqlMapper = null;
	private static int loopcount = 0;
	private static Thread self = null;
	boolean bAlreadyConfirm = false;
	boolean bAlreadyConfirm2 = false;
	static MyMap appRes = new MyMap();
	private static DgMessageService service = null;

	/**
	 * 메인함수
	 *
	 * @param args
	 */
	public static void main(String args[]) {
		/* 로그설정 파일을 읽는다. 컨텍스트를 기준으로 한다 */
		DOMConfigurator.configure(NeoDMWS3010.class.getResource("/conf/log4j.xml"));

		logger.debug("##### [" + NeoDMWS3010.class.getSimpleName() + "] 시작 #####");

		/* 클래스 인스턴스 생성 */
		NeoDMWS3010 hello = new NeoDMWS3010();

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

		logger.debug("##### [" + NeoDMWS3010.class.getSimpleName() + "] 끝 #####");
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
		service = new DgMessageService(appRes.getString("ServiceIP"), appRes.getInteger("ServicePort"));

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

		logger.info("dmws3010 start");
		// 휴일여부 체크
		MyMap paramMap = new MyMap();

		paramMap.setString("현재일자", Utils.getCurrentDate());
		paramMap.setString("거래구분", "SA");

		// 20220804 Neo
		paramMap.setString("CURR_DATE", Utils.getCurrentDate()); // 현재일자
		paramMap.setString("TRNX_TYPE", "SA"); // 거래구분, 대용량계좌검증(Neo)

		MyMap holiday = null;
		try {
			holiday = (MyMap) session.selectOne("NeoMapperCommon.getHoliday", paramMap);
		} catch (Exception e) {
			logger.error("영업일 조회중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
			session.close();
			return -1;
		}

		// 날짜가 변경될때만 로그 출력한다.
		if (holiday != null && holiday.getString("휴일여부").equals("Y")) {
			if (!bAlreadyConfirm) {
				logger.debug("[" + paramMap.getString("CURR_DATE") + "]은 휴일입니다");
				bAlreadyConfirm = true;
			}
			bAlreadyConfirm2 = false;
			session.close();
			return -1;
		} else {
			if (!bAlreadyConfirm2) {
				logger.debug("[" + paramMap.getString("CURR_DATE") + "]은 휴일이 아닙니다");
				bAlreadyConfirm2 = true;
			}
			bAlreadyConfirm = false;
		}
		loopcount = 0;

		logger.info("TFE2311S(이뱅킹계좌검증 수신 테이블을 조회합니다.");

		try {
			// 구청별로 작업
			DBTrans(paramMap);
		} catch (Exception ex) {
			logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
			session.rollback();
			session.close();
			return -1;
		}

		session.close();
		return 0;
	}

	@SuppressWarnings("unchecked")
	private int DBTrans(MyMap param) throws Exception {
		ArrayList<MyMap> veriList = null;

		try {
			/******************************************************************************
			 * Neo Del 구청별로 신규 등록된 데이타가 있는지 확인 TFE2311S에 새로 등록된 DATA가 있는지 확인 Neo 20220803 먼저
			 * 구청별로 count - 로직 삭제(쿼리는 일단 주석처리해 둠.[session.selectList("NeoMapper3010.getCountByLaf", param)])
			 ******************************************************************************/

			veriList = (ArrayList<MyMap>) session.selectList("NeoMapper3010.selectTFE2311S", param); // 현재일자(CurrentDate),거래구분(SE), 점번

			logger.debug("신규등록된  TFE2311S 체크 veriList.size " + veriList.size());

			if (veriList.size() <= 0) {
				logger.debug("(Neo)신규 등록된 데이터가 없습니다.");
				session.commit(true);
				return 0;
			}

			maintService(param, veriList);

		} catch (Exception se) {
			logger.error("신규등록된 대용량계좌검증 정보 (TFE2311S) 작업중 오류발생, 내용[" + se.getLocalizedMessage() + "]");
			session.rollback(true);
			return -1;
		}
		return 0;
	}

	/**
	 * 신규등록된 계좌검증건을 조회하여 TFE2311에 insert
	 * @param param
	 * @param veriList
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private int maintService(MyMap param, ArrayList<MyMap> veriList) throws Exception {
		ArrayList<MyMap> dataList = null;

		/******************************************************************************
		 * 현재일자(CurrentDate), 거래구분(SE), 점번
		 * veriList (지자체, 지출단계, 지출번호, 지출일련번호로 group by 된 TFE2311S)
		 ******************************************************************************/
		for (int j = 0; j < veriList.size(); j++) {

			MyMap st = veriList.get(j); // VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO

			long beforeTime = System.currentTimeMillis();
//			if (st.getLong("VERI_CNT") <= appRes.getInteger("verCnt") && st.getString("EP_BYLV_DV_CD").equals("B")) { // 건수&&지출단계
			if (st.getLong("VERI_CNT") <= appRes.getInteger("verCnt") ) { // 건수

				st.setString("CURR_DATE", param.get("CURR_DATE"));

				// 실시간 거래번호 생성 SS
				MyMap silsi = new MyMap(); // 거래번호 채번용 param
				silsi.setString("DLNG_CD", "SS"); // 파일순번용 거래구분 (실시간 SS)
				silsi.setString("DLNG_YMD", Utils.getCurrentDate()); // 파일순번용 거래일자
				silsi.setString("JUM_NO", "999"); // 파일순번용 임시점번

				long fileSeqNo = getSqNo(silsi); // 파일순번생성
				String sqno = String.format("%06d", fileSeqNo);
				st.setString("TRNX_NO", "SS" + Utils.getCurrentDate().toString().substring(2) + sqno); // 거래번호(거래구분 + 거래일자+ seq)
				logger.info("실시간 TRNX_NO : " + st.getString("TRNX_NO") );

				dataList = (ArrayList<MyMap>) session.selectList("NeoMapper3010.getTFE2311S", st);

				logger.info("실시간 :  자치단체코드[" + st.getString("LAF_CD") + "]" +
							" 일련번호[" + st.getString("LINK_SNUM") + "]" +
							" 트랜잭션ID[" + st.getString("LINK_TRSC_ID") + "]" +
							" 지출단계별구분코드[" + st.getString("EP_BYLV_DV_CD") + "]" +
							" 지출번호구분코드[" + st.getString("EP_NO_DV_CD") + "]" +
							" 지출일련번호[" + st.getString("EP_SNUM") + "]" +
							"에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

				for (int k = 0; k < dataList.size(); k++) {
					MyMap ht = dataList.get(k);

					String inDepositor = ht.getString("DPOR_NM"); // 예금주

					service.setData("BANKCODE", ht.getString("FNIS_CD")); // 금융기관코드(은행)
					service.setData("ACCOUNT", ht.getString("ECRP_ACTNO")); // 계좌번호(암호화계좌번호)
					service.setData("NAME", ht.getString("DPOR_NM")); // 예금주
					service.setData("CURRENCY", ht.getString("GIVE_AMT"));
					service.service("6015", 150);

					logger.debug(" 실시간 예금주조회 전송 [" + ht.getString("LINK_SNUM") + "]" +
								" 자치단체코드[" + ht.getString("LAF_CD") + "]" +
								" 트랜잭션ID[" + ht.getString("LINK_TRSC_ID") + "]" +
								" 지출단계[" + ht.getString("EP_BYLV_DV_CD") + "]" +
								" 지출번호구분[" + ht.getString("EP_NO_DV_CD") + "]" +
								" 지출순번[" + ht.getString("EP_SNUM") + "]" +
								" 계좌검증일련번호[" + ht.getString("ACC_VRFC_SNUM") + "]" + "");

					logger.debug(" 실시간 예금주조회 Real 전송 " +
								" BANKCODE[" + ht.getString("FNIS_CD") + "]" +
								" ACCOUNT[" + ht.getString("ECRP_ACTNO") + "]" +
								" NAME[" + ht.getString("DPOR_NM") + "]" + "");

					String outDepositor = service.getData("SNAME");

					logger.debug("RECVMAP==" + service.getDataMap());
					logger.debug("오류코드[" + service.getData("Errcode") + "]");
					logger.debug("오류내용[" + service.getData("ERRMSG") + "]");
					logger.debug("은행코드[" + service.getData("BANKCODE") + "]");
					logger.debug("계좌번호[" + service.getData("ACCOUNT") + "]");
					logger.debug("예금주명[" + service.getData("NAME") + "]");
					logger.debug("실예금주[" + outDepositor + "]");

					ht.setString("NOM_DPOR_NM", outDepositor); // 정상예금주

					if (service.getData("Errcode").equals("0000")) {
						if (Utils.isSameYegeumju(inDepositor, outDepositor) == 1) {
							logger.debug("예금주명 일치함");

//                            ht.setString( "검증결과" , "Y" );

							ht.setString("ACC_VRFC_RSLT_CD", "20"); // 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
							ht.setString("ERR_RSON_CN", ""); // 오류사유
						} else {
							logger.debug("예금주명 일치하지않음");

//                            ht.setString( "검증결과" , "N" );

							ht.setString("ACC_VRFC_RSLT_CD", "30"); // 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
							ht.setString("ERR_RSON_CN",
									"9005-[" + inDepositor + "]이 실제예금주 명[" + outDepositor + "]과 다릅니다."); // 오류사유
						}
					} else {
						logger.debug("에러발생");

//                        ht.setString( "검증결과" , "N" );

						ht.setString("ACC_VRFC_RSLT_CD", "30"); // 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
						ht.setString("ERR_RSON_CN", service.getData("Errcode") + "-" + service.getData("ERRMSG")); // 오류사유
					}

					logger.debug(" 실시간 등록 일련번호[" + ht.getString("LINK_SNUM") + "]" +
								" 자치단체코드[" + ht.getString("LAF_CD") + "]" +
								" 트랜잭션ID[" + ht.getString("LINK_TRSC_ID") + "]" +
								" 지출단계[" + ht.getString("EP_BYLV_DV_CD") + "]" +
								" 지출번호구분[" + ht.getString("EP_NO_DV_CD") + "]" +
								" 지출순번[" + ht.getString("EP_SNUM") + "]" +
								" 계좌검증일련번호[" + ht.getString("ACC_VRFC_SNUM") + "]" +
								" 계좌검증결과코드[" + ht.getString("ACC_VRFC_RSLT_CD") + "]" +
								" 오류사유내용[" + ht.getString("ERR_RSON_CN") + "]" +
								" 계좌검증일련번호[" + ht.getString("ACC_VRFC_SNUM") + "]" + " .");

					ht.setString("ACC_VRFC_INPT_NO", Utils.getCurrentDate().toString().substring(2)); // 일련번호
					ht.setString("ACC_VRFC_GIVE_NO", "SS" + silsi.getString("JUM_NO") + "000000"); // 지급번호 : 거래구분, 전번, filesqno
					ht.setString("CURR_DATE", param.get("CURR_DATE"));

					// TFE2311S update Neo 20220802
					session.update("NeoMapper3010.updateTFE2311S", ht);

					// TFE2311R insert Neo 20220802
					// session.update("NeoMapper4010.insertDirectTFE2311R", ht);

					// 10분주기로 한번에 R테이블에 넣기 위해 연계R테이블에 바로 넣지 않고 원장에 적재 2023-06-27
					ht.setString("TRNX_NO", st.getString("TRNX_NO"));
	                ht.setString("DMND_YMD", param.getString("CURR_DATE")); // 요청일자 yyyymmdd(요청일자)
	                logger.debug("insert param ht " + ht);
	                session.update("NeoMapper4010.insertSilsiTFE2311", ht);
				}
				session.commit();
			} else {
				// 거래 일련번호 가져오기
				param.setString("거래일자", param.getString("CURR_DATE"));

				st.setString("LINK_PRCS_DV_CD", "C"); // Neo 20220804 연계처리구분코드 C : insert, U : Update, D : Delete. Default ‘C’
				/**
				 * [check] 확인필요
				 */
				st.setString("LINK_TRSM_STAT_CD", "N"); // Neo 20220804 연계전송상태코드 N : 전송대기, P : 전송중, W : 상대기관 수신확인대기, S :전송 성공, F : 전송 실패, Default ‘N’

				st.setString("JOB_SYS_CD", "01"); 						// 작업시스템코드
				st.setString("JOB_STAT_CD", "01"); 						// 작업상태코드
				st.setString("DMND_YMD", param.getString("CURR_DATE")); // 요청일자 yyyymmdd(요청일자)
				st.setString("CURR_DATE", param.getString("CURR_DATE"));

				/******************************************************************************
				 * 완료된 계좌검증 파일이 있는지 확인
				 * st : VERI_CNT, LAF_CD, EP_BYLV_DV_CD, EP_NO_DV_CD, EP_SNUM, JUM_NO
				 ******************************************************************************/
				/**
				 * [check] 로직이 존재하지 않음.-일단 협의하에 주석처리해 둠.(20221021)
				 */
//				logger.debug("Neo 완료된 계좌검증 파일이 있는지 확인");
//				long verify_cnt = (Long) session.selectOne("NeoMapper3010.checkTFE2311Data", st);
//				if (verify_cnt > 0) {
//					logger.info(" 자치단체코드[" + st.getString("LAF_CD") + "]" + " 트랜잭션ID[" + st.getString("LINK_TRSC_ID")
//							+ "]" + " 지출단계[" + st.getString("EP_BYLV_DV_CD") + "]" + " 지출번호구분["
//							+ st.getString("EP_NO_DV_CD") + "]" + " 지출순번[" + st.getString("EP_SNUM") + "]"
//							+ " 기존에 완료된 작업이 있어서 삭제 후 등록합니다.");
//
//					session.delete("NeoMapperFile.delTFE2311", st);
//				}

				// 중복된 계좌검증 파일이 있는지 확인
				if(st.getLong("VERI_CNT") > 3000) {
					long verify_cnt = (Long) session.selectOne("NeoMapper3010.checkTFE2311Before", st);

					if (verify_cnt > 0) {
						logger.info(" 자치단체코드[" + st.getString("LAF_CD") + "]" +
									" 트랜잭션ID[" + st.getString("LINK_TRSC_ID") + "]" +
									" 지출단계[" + st.getString("EP_BYLV_DV_CD") + "]" +
									" 지출번호구분[" + st.getString("EP_NO_DV_CD") + "]" +
									" 지출순번[" + st.getString("EP_SNUM") + "]" +
									" 기존 작업이 진행 중이어서 Skip 합니다.");
						continue;
					}
				}

				// e-호조 테이블에서 대상 데이타 조회
				dataList = (ArrayList<MyMap>) session.selectList("NeoMapper3010.getTFE2311S", st);

				logger.info(" 연계트랜잭션ID[" + st.getString("LINK_TRSC_ID") + "]" +
							" 지방자치단체코드[" + st.getString("LAF_CD") + "]" +
							" 지출단계별구분코드[" + st.getString("EP_BYLV_DV_CD") + "]" +
							" 지출번호구분코드[" + st.getString("EP_NO_DV_CD") + "]" +
							" 지출일련번호[" + st.getString("EP_SNUM") + "]" + " 계좌검증일련번호[" + st.getString("ACC_VRFC_SNUM") + "]" +
							"에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

				/******************************************************************************
				* 로컬 이뱅킹계좌검증에 등록 후, e-호조 테이블에 업데이트 bit set
				******************************************************************************/
				beforeTime = System.currentTimeMillis();

				int i_success = InsertService(dataList, st);

				if (i_success < 0) {
					session.rollback();
					logger.debug("거래번호[" + st.getString("TRNX_NO") + "] 로컬에 등록 및 e-호조 테이블에 업데이트 bit set 도중 오류 발생!");

					continue;
				}
				session.commit();
			}

			// 작업 건수가 많을 수 있으므로, 계좌검증 데이터는 초기화 시킨다.
			param.setString("VERI_CNT", dataList.size());

			dataList.clear();
			long afterTime = System.currentTimeMillis();
			long secDiffTime = (afterTime - beforeTime) / 1000;

			logger.info("거래번호[" + st.getString("TRNX_NO") + "] " +
						"지출번호구분 [" + st.getString("EP_NO_DV_CD") + "] " +
						"지출순번 [" + st.getString("EP_SNUM") + "] " +
						"건수 [" + param.getString("VERI_CNT") + "] 정상적으로 Update 완료  : [ " + secDiffTime + " 초 ]");
		}

		logger.info("TFE2311S UPDATE, 원장 INSERT 작업완료");

		return 0;
	}

	/**
	 * 호조원장 데이터를 업무원장으로 이관처리
	 * @param dataList
	 * @param st
	 * @return
	 * @throws Exception
	 */
	private int InsertService(ArrayList<MyMap> dataList, MyMap st) throws Exception {

		long l_count = 0;
		int nErrCount = 0;

		for (int k = 0; k < dataList.size(); k++) {
			MyMap ht = dataList.get(k);

			/**
			 * 은행코드와 계좌번호 유효성 검증
			 * [check]계좌유효성은 왜 제거 했지? - 일단 넣어 둠.
			 */
			if (Utils.isNumber(ht.getString("FNIS_CD")) == false || Utils.isNumber(ht.getString("ECRP_ACTNO")) == false) {

				ht.setString("JOB_SYS_CD", "01"); 				/* 작업시스템코드 */
				ht.setString("JOB_STAT_CD", "31"); 				/* 작업상태코드 */
				ht.setString("ACC_VRFC_RSLT_CD", "10"); 		/* 계좌검증결과코드(검증결과) 기존: N Neo : 10(이체불가) */
				ht.setString("ERR_RSON_CN", "계좌번호 부적합"); 	/* 오류사유내용(오류사유) */
				ht.setString("ACC_VRFC_STAT_CD", "E"); 			/* 계좌검증상태코드(요청상태) */
				nErrCount++;

				logger.error("DATA ERROR : " + st.getString("TRNX_NO") +
							" 일련번호 [" + ht.getString("ACC_VRFC_SNUM") + "] "); // 거래번호, 계좌검증일련번호(검증순번)
				// return -1;

			} else {
				ht.setString("JOB_SYS_CD", st.getString("JOB_SYS_CD")); 	/* 작업시스템코드 */
				ht.setString("JOB_STAT_CD", st.getString("JOB_STAT_CD")); 	/* 작업상태코드 */
				ht.setString("ACC_VRFC_STAT_CD", "R"); 						/* 계좌검증상태코드(요청상태) */
			}

			ht.setString("TRNX_NO", st.getString("TRNX_NO")); 	/* 거래번호 */
			ht.setString("DMND_YMD", st.getString("DMND_YMD")); /* 요청일자 */

			/**
			 * [check]확인 후에 제거
			 */
//            ht.setString("ACC_VRFC_INPT_NO", st.getString("ACC_VRFC_INPT_NO"));
//            ht.setString("ACC_VRFC_GIVE_NO", st.getString("ACC_VRFC_GIVE_NO"));

			ht.setString("LINK_PRCS_DV_CD", st.getString("LINK_PRCS_DV_CD")); 		/* 연계처리구분코드 Neo 20880830 */
			ht.setString("LINK_TRSM_STAT_CD", st.getString("LINK_TRSM_STAT_CD")); 	/* 연계전송상태코드 Neo 20880830 */

			logger.trace("[" + k + "] 자치단체코드[" + ht.getString("LAF_CD") + " 연계트랜잭션ID[" + st.getString("LINK_TRSC_ID") + "]"
					+ "],거래번호[" + ht.getString("TRNX_NO")
					+ "],지출단계구분코드[" + ht.getString("EP_BYLV_DV_CD") 	/* 지출단계 */
					+ "],지출번호구분코드[" + ht.getString("EP_NO_DV_CD") 		/* 지출번호구분 */
					+ "],지출일련번호[" + ht.getString("EP_SNUM") 				/* 지출순번 */
					+ "],계좌검증일련번호[" + ht.getString("ACC_VRFC_SNUM") 	/* 검증순번 */
					+ "],거래처명[" + ht.getString("CLT_NM") 					/**/
					+ "],예금주[" + ht.getString("DPOR_NM") 					/**/
					+ "],은행[" + ht.getString("FNIS_CD") 					/* 은행 */
//            		+ "],계좌번호[" + ht.getString("계좌번호")					/*계좌번호*/
					+ "],지급금액[" + ht.getLong("GIVE_AMT") 					/* 지급금액 */
					+ "],요청상태[" + ht.getString("ACC_VRFC_STAT_CD") 		/* 계좌검증상태코드(요청상태) */
//            		+ "],요청일시[" + ht.getString("ACC_VRFC_DMND_DT")		/**/
//            		+ "],요청자ID[" + ht.getString("요청자ID")					/**/
//            		+ "],거래번호[" + ht.getString("거래번호")					/**/
					+ "],작업시스템코드[" + ht.getString("JOB_SYS_CD") + "],작업상태코드[" + ht.getString("JOB_STAT_CD")
					+ "],압류방지코드[" + ht.getString("ATH_BAN_SLRY_CHLT_CD") /* 압류금지급여성격코드 */
//            		+ "],요청일자[" + ht.getString("요청일자")					/* ACC_VRFC_DT(계좌검증일시? ) */
					+ "]");

			ht.setString("TRNX_TYPE", "SA"); // 기존 (SE) -> SA로 변경
		}

		try {
			st.setString("ACC_VRFC_STAT_CD", "R");

			// 거래번호 채번용 param
			MyMap param = new MyMap();

			param.setString("DLNG_CD", "SA"); // 파일순번용 거래구분 (기존 SE -> SA)
			param.setString("DLNG_YMD", Utils.getCurrentDate()); // 파일순번용 거래일자

			/**
			 * [result] TFC0004 테이블의 점번 정보를 기준으로 할 필요 없어서
			 * 수성구청 점번(149) 세팅하여 시퀀스를 구하는 걸로 변경(20221021)
			 */
			param.setString("JUM_NO", "149"); // 파일순번용 점번
//			param.setString("JUM_NO", st.getString("JUM_NO")); // 파일순번용 점번

			/******************************************************************************
			 * 거래번호 생성 규칙 : TRNX_TYPE : SA + yymmdd + seq (6자리) , 점번은 뺌
			 ******************************************************************************/
			long seqNo = getSqNo(param); // 파일순번생성

			String filesqno = String.format("%06d", seqNo);

			logger.debug("Neo Del 거래일련번호 가져오기(Neo) seqNo " + seqNo +
						" TRNX_TYPE" + "SA" + "거래일자  " + Utils.getCurrentDate().toString().substring(2) +
						" filesqno " + filesqno);

			st.setString("TRNX_NO", "SA" + Utils.getCurrentDate().toString().substring(2) + filesqno); // 거래번호(거래구분 + 거래일자+ seq)

			logger.debug("Neo Del TRNX_NO " + st.getString("TRNX_NO") + " JUM_NO :  " + param.getString("JUM_NO"));

//            st.setString("일련번호", param.getString("현재일자"));
//            st.setString("지급번호", param.getString("거래구분") + param.getString("JUMBUN") + filesqno);

			st.setString("ACC_VRFC_INPT_NO", Utils.getCurrentDate().toString().substring(2)); // 일련번호
			st.setString("ACC_VRFC_GIVE_NO", "SA" + param.getString("JUM_NO") + filesqno); // 지급번호 : 거래구분, 전번, filesqno

			int inChkCnt = session.insert("NeoMapper3010.insertTFE2311", st); // 로컬 DB(TFE2311) 에 등록(select 후 insert)

			int updChkCnt = session.update("NeoMapper3010.updateTFE2311S", st);

			/**
			 * [check] mybatis update 시 결과값 반환이 되지 않아 주석 처리 함.
			 */
//			if (updChkCnt == 0) {
//				logger.error("호조(TFE2311S) 업데이트가 제대로 이루어지지 않았습니다. " + st);
//				session.rollback();
//				return -1;
//			} else {
//				logger.error("호조(TFE2311S) 업데이트가 제대로 이루어짐. " + st);
//
//			}
		} catch (Exception e) {
			logger.error("세출(TFE2311) 업데이트가 제대로 이루어지지 않았습니다. " + e);
			session.rollback();
			return -1;
		}

		return nErrCount;
	}

	/**
	 * 거래 일련번호 가져오기 Neo 20220803
	 * @param mapForm
	 * @return
	 * @throws Exception
	 */
	public long getSqNo(MyMap mapForm) throws Exception {
		long sqNo = 0;

		SqlSession ss = sqlMapper.openSession(false);

		try {
			try {
				long nCnt = (Long) ss.selectOne("NeoMapperCommon.nCntSelectNeo", mapForm);
				logger.debug("COUNT[" + nCnt + "]");

				if (nCnt > 0) {
					sqNo = (Long) ss.selectOne("NeoMapperCommon.sqNoSelectNeo", mapForm);
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
