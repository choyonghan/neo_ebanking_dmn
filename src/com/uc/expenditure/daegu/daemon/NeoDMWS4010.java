/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 대량예금주 조회 처리
 *  기  능  명 : e세출업무원장 예금주 조회 결과 e호조에 결과 처리
 *  클래스  ID : NeoDMWS4010
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

import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS4010 implements Runnable {

	@SuppressWarnings("unchecked")
	static Logger logger = Logger.getLogger(NeoDMWS4010.class);

	private static SqlSessionFactory sqlMapper = null;
	private static Thread[] self = new Thread[9];
//    private static Thread[] self = new Thread[14];							//경북

	private int govId = 0;

//    대구,중구,동구,서구,남구,북구,수성구,달서구,달성군
	private String[] govIdArray = { "6270000", "3410000", "3420000", "3430000", "3440000", "3450000", "3460000", "3470000", "3480000" };

	// 경북, 경산, 포항, 구미, 영주, 문경, 상주, 김천, 성주, 고령, 영천, 경주, 청도, 칠곡
//    private String[] govIdArray  = {"6470000", "5130000","5020000","5080000","5090000","5120000","5110000","5060000","5210000","5200000", "5100000", "5050000", "5190000" , "5220000" };				//경북

	public NeoDMWS4010(int i) {
		this.govId = i;
	}

	public static void main(String args[]) {
		DOMConfigurator.configure(NeoDMWS4010.class.getResource("/conf/log4j.xml"));

		logger.debug("##### [" + NeoDMWS4010.class.getSimpleName() + "] 시작 #####");

		for (int i = 0; i < self.length; i++) {
			NeoDMWS4010 hello = new NeoDMWS4010(i);
			self[i] = new Thread(hello);
			self[i].setDaemon(true);
			self[i].start();
		}

		for (int i = 0; i < self.length; i++) {
			try {
				self[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		logger.debug("##### [" + NeoDMWS4010.class.getSimpleName() + "] 끝 #####");
	}

	/**
	 * 스레드 실행
	 */
	public void run() {
		logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 실행");

		Reader reader = null;
		try {
			reader = Resources.getResourceAsReader("res/Configuration.xml");
			Properties properties = Resources.getResourceAsProperties( "res/db.properties" );

			String pw = Utils.getDecrypt( properties.getProperty("password"));
			properties.setProperty("password", pw);
            sqlMapper = new SqlSessionFactoryBuilder().build(reader, properties);
		} catch (Exception ex) {
			logger.error("데이터베이스 접속중 오류[" + ex.getLocalizedMessage() + "]");
			return;
		}

		logger.debug("SQLMAPPER생성");

		while (!Thread.currentThread().isInterrupted()) {
			logger.debug("mainLoop start");
			mainLoop();
			logger.debug("mainLoop end");
			try {
				Thread.sleep(600000);
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
		/**
		 * [check]BATCH타입을 왜 기본으로 바꿨지!?
		 */
		// 데이터베이스 세션 생성, 자동확약 끔.
//    	SqlSession  session = sqlMapper.openSession(ExecutorType.BATCH,false);
		SqlSession session = sqlMapper.openSession(false);

		MyMap paramMap = new MyMap();
		paramMap.setString("govcode", govIdArray[govId]);

		// 링크 리스트 가져오기
		// 구청별 링크리스트로 유지할지 삭제할지 고려해볼것, Neo 2208509
//        ArrayList<MyMap> linkList = (ArrayList<MyMap>)session.selectList("NeoMapperCommon.getLinkList2", paramMap);
		@SuppressWarnings("unchecked")
		ArrayList<MyMap> linkList = (ArrayList<MyMap>) session.selectList("NeoMapperCommon.getLinkListNeo", paramMap);

		for (int i = 0; i < linkList.size(); i++) {
			MyMap ht = linkList.get(i);

			paramMap.setString("LAF_CD", ht.getString("LAF_CD")); /* 자치단체코드 */
			paramMap.setString("OFC_NM", ht.getString("OFC_NM")); /* 구청명 */

			logger.info("[" + ht.getString("OFC_NM") + "] 의 DB를 조회합니다.");

			// 구청별로 작업
			try {
				DBTrans(session, paramMap);
			} catch (Exception ex) {
				logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
				session.rollback();
			}
		}

		session.close();
		return 0;
	}

	/**
	 * 검증결과처리
	 * @param session
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	private int DBTrans(SqlSession session, MyMap paramMap) throws Exception {

		try {
			@SuppressWarnings("unchecked")
			ArrayList<MyMap> viewList = (ArrayList<MyMap>) session.selectList("NeoMapper4010.getVerifyTFE2311", paramMap);

			if (viewList == null || viewList.size() <= 0) {
				return -1;
			}

			logger.info("[" + paramMap.get("OFC_NM") + "] 결과업데이트 된 검증 그룹이 " + viewList.size() + "개 있습니다.");

			MyMap ht = null;

			for (int i = 0; i < viewList.size(); i++) {
				/**
				 * 중복cast 처리
				 */
				ht = viewList.get(i);

				/******************************************************************************
				* 지출단계가 "1"일 경우는 대량이체 시 계좌검증이므로 e-hojo가 아닌 Local DB의 지급원장을 업데이트 한다.
				* "2"일 경우는 엑셀계좌검증이므로 엑셀계좌검증 테이블에 업데이트 한다.
				* "S","C","R","P" 일 경우는 대량계좌검증
				******************************************************************************/
				int i_ret = 0;

				String Gbn = ht.getString("EP_BYLV_DV_CD"); // 지츨단계

				// char cGbn[] = ht.get( "지출단계" ).toString().toCharArray();

				String sMsg = "자치단체코드:" + ht.get("LAF_CD") + "," +
							  "지출단계:" + ht.get("EP_BYLV_DV_CD") + "," +
							  "지출번호구분:" + ht.get("EP_NO_DV_CD") + "," +
							  "지출순번:" + ht.get("EP_SNUM") + "," +
							  "거래번호:" + ht.get("TRNX_NO");

				long beforeTime = System.currentTimeMillis();

				/**
				 * [delete]대량이체 시 계좌검증 사용하지 않음(20221024)
				 */
				if (Gbn.equals("1")) { // 대량이체 시 계좌검증
					logger.info("대량이체 시 계좌검증 사용하지 않음");
				} else if (Gbn.equals("2")) { // 엑셀 계좌검증
					logger.info("엑셀 계좌검증-" + sMsg);
					i_ret = setExcel(session, ht);
				} else /*if (Gbn.equals("S")
						|| Gbn.equals("C")
						|| Gbn.equals("R")
						|| Gbn.equals("P")
						|| Gbn.equals("B")
						|| Gbn.equals("E"))*/ { // 대량검증 시 계좌검증
					logger.info("대량검증 시 계좌검증-" + sMsg);

					i_ret = setEhojo(session, ht);
				}
//				else {
//					logger.debug("지출단계를 알 수 없음-" + sMsg);
//				}

				long afterTime = System.currentTimeMillis();
				long secDiffTime = (afterTime - beforeTime) / 1000;

				logger.info(sMsg + " 걸린시간 : [ " + secDiffTime + "초 ]");

				if (i_ret != 0) {
					session.rollback(true);
					logger.error("로컬에 등록 및 e-호조 테이블에 업데이트 bit set 도중 오류 발생!-" + sMsg);
					return -1;
				}

				// 정상적으로 종료 됐을 때 Transation commit및 종료
				session.commit();
				logger.info("업데이트 완료!-" + sMsg);
				ht.clear();
			}

			session.commit();
			logger.info("모든 데이터 업데이트 완료");

		} catch (Exception se) {
			logger.error("[" + paramMap.get("구청명") + "] 작업 중 에러:" + se.getMessage());
			session.rollback();
			return -1;
		}

		return 0;
	}

	/**
	 * 대량검증시 계좌검증
	 * @param session
	 * @param param
	 * @return
	 * @throws Exception
	 */
	private int setEhojo(SqlSession session, MyMap param) throws Exception {
		MyMap jumbunMap = new MyMap();
		ArrayList<MyMap> dataList = null;

		try {
			int nUpdated = 0;
			int nInserted = 0;

			/**
			 * [insert]거래번호 기준으로 한번에 R테이블 insert 처리 함.(20221024)
			 */
			nInserted = session.update("NeoMapper4010.insertTFE2311R", param);
			logger.debug("[NeoDMWS4010.insertTFE2311R]의 변경건수[" + nInserted + "]");

			nUpdated = session.update("NeoMapper4010.updateTFE2311", param);

		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 * 엑셀계좌검증
	 * @param session
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private int setExcel(SqlSession session, MyMap param) throws Exception {
		MyMap jumbunMap = new MyMap();
		ArrayList<MyMap> dataList = null;

		try {
			dataList = (ArrayList<MyMap>) session.selectList("NeoMapper4010.selectTFE2311", param);

			if (dataList == null || dataList.size() <= 0) {
				return 0;
			}

			MyMap st = null;
			int nUpdated = 0;

			/**
			 * [result]TFA0006 엑셀검증결과 update시 필요한 결과 건수에 대한 변수
			 */
			int errorCnt = 0;
			int sucessCnt = 0;

			try {
				for (int j = 0; j < dataList.size(); j++) {
					/**
					 * 중복cast 처리
					 */
					st = dataList.get(j);

					//에러건수 확인
					if(!"20".equals(st.getString("ACC_VRFC_RSLT_CD"))) {
						errorCnt++;
					} else {
						sucessCnt++;
					}

					/**
					 * [delete]작업상태를 단순하게 `32`로 update 하는데 건by건으로 할 필요 없이 거래번호 단위로 처리하면 되어서 루프에 제외하여 한번 update로 변경 함.
					 */
//					nUpdated = session.update("NeoMapper4010.updateVerifyData", st);
//					logger.debug("[NeoDMWS4010.updateEhojoResult]의 변경건수[" + nUpdated + "]");
				}

				/**
				 * [insert]작업상태를 단순하게 `32`로 update 하는데 건by건으로 할 필요 없이 거래번호 단위로 처리하면 되어서 루프에 제외하여 한번 update로 변경 함.
				 */
				nUpdated = session.update("NeoMapper4010.updateVerifyData", param);
				logger.debug("[NeoMapper4010.updateVerifyData]의 변경건수[" + nUpdated + "]");

				/**
				 * [delete]조회한 데이터를 기준으로 결과건 카운트해서 세팅하기 때문에 아래 As-is 쿼리는 필요 없음.
				 */
//				int ok_cnt = (Integer) session.selectOne("NeoMapper4010.checkOkCnt", param);
//				param.put("정상건수", Integer.toString(ok_cnt));

				param.put("NOM_CNT", sucessCnt);
				param.put("ERR_CNT", errorCnt);

				session.update("NeoMapper4010.updateExcel", param);

			} catch (Exception e) {
				logger.debug("검증데이타 [" + st.getString("자치단체코드") + "|" + st.getString("지출순번") + "|"
						+ st.getString("검증순번") + "] 엑셀계좌검증 업데이트 에러!!!");
				e.printStackTrace();
				return -1;
			}

			// 건수가 많을 수 있으므로, 작업이 완료된 계좌검증 데이터는 초기화 시킨다.
			dataList.clear();
			dataList = null;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}

		return 0;
	}
}
