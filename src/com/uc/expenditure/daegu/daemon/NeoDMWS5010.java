/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 대량예금주 조회 처리
 *  기  능  명 : e세출업무원장 대량예금주 조회건 sam파일 생성처리
 *  클래스  ID : NeoDMWS5010
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 *  하상원       다산(주)      2022.10.07         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS5010 implements Runnable {

	@SuppressWarnings("unchecked")
	static Logger logger = Logger.getLogger(NeoDMWS5010.class);
	private static SqlSession session = null;
	private static SqlSessionFactory sqlMapper = null;
	private static FormatParserAsMap fp = null;
	private static Thread self = null;
	static MyMap appRes = new MyMap();

	public static void main(String args[]) {
		DOMConfigurator.configure(NeoDMWS5010.class.getResource("/conf/log4j.xml"));

		logger.debug("##### [" + NeoDMWS5010.class.getSimpleName() + "] 시작 #####");

		NeoDMWS5010 hello = new NeoDMWS5010();
		self = new Thread(hello);
		self.setDaemon(true);
		self.start();

		try {
			self.join();
		} catch (InterruptedException e) {
		}

		logger.debug("##### [" + NeoDMWS5010.class.getSimpleName() + "] 끝 #####");
	}

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

		fp = new FormatParserAsMap(logger);
		if (fp.doParsingAsMap("msgformat2") < 0) {
			logger.error("전문포맷 분석오류");
			return;
		}

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
	@SuppressWarnings("unchecked")
	public int mainLoop() {
		try {
			session = sqlMapper.openSession(false);
		} catch (Exception ex) {
			logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
			return -1;
		}

		String folder = appRes.getString("NeoDMWS5010.directory");

		MyMap headerMap = new MyMap();
		ArrayList<MyMap> viewList = new ArrayList<MyMap>();
		ArrayList<MyMap> dataList = new ArrayList<MyMap>();
		MyMap paramMap = new MyMap();

		paramMap.setString("DMND_YMD", Utils.getCurrentDate());

		try {
			/******************************************************************************
			 * SAM파일 생성 데이터 조회
			 ******************************************************************************/
			viewList = (ArrayList<MyMap>) session.selectList("NeoMapper5010.getVerifyTFE2311", paramMap);
			if (viewList.size() <= 0) {
				logger.info("신규 등록된 데이터가 없습니다.");
				session.close();
				return -1;
			}

			if (viewList.size() > 0) {
				logger.info("총 요청건수 : [" + viewList.size() + "]건");

				MyMap ht = new MyMap();

				String makeDir = folder + "/dees_ver/send";
				File dir = new File(makeDir);

				if (!dir.exists()) {
					logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
					session.close();
					return -1;
				}

				for (int i = 0; i < viewList.size(); i++) {
					/**
					 * 중복cast 처리
					 */
					ht = viewList.get(i);

					headerMap.setString("파일코드", ht.getString("TRNX_NO")); /* 거래번호 */

					// B:사전품의, S:품의, C:원인행위, R:결의, P:지급명령';
					if (ht.getString("EP_BYLV_DV_CD").equals("S")) { /* 지출단계(지출단계별구분코드) 기존1 -> S */
						headerMap.setString("지급명령번호", ht.getString("EP_SNUM")); /* 지출일련번호(지출순번) */
					}

					try {
						dataList = (ArrayList<MyMap>) session.selectList("NeoMapper5010.selectTFE2311", ht);

						logger.info("자치단체코드 [" + ht.getString("LAF_CD") +
									"]자치단체코드 [" + ht.getString("LAF_CD") +
									"] 지출단계 [" + ht.getString("EP_BYLV_DV_CD") +
									"] 지출번호구분 [" + ht.getString("EP_NO_DV_CD") +
									"] 지출순번 [" + ht.getString("EP_SNUM") +
									"]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

						if (dataList.size() > 0) {

							File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO")); /* 거래번호 */

							// file 생성
							makeFile(infofile, headerMap, dataList);

							/* TFE2311 요청상태, 작업상태코드 변경 */
							session.update("NeoMapper5010.updateVerifyData", ht);
						}
					} catch (Exception e) {
						logger.error("작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
						session.close();
						return -1;
					}
				}
				logger.info("모든 데이터 파일 생성 완료");
			} else {
				logger.info("신규 등록된 데이터가 없습니다.");
			}

			session.commit();
			logger.info("작업 완료!!!");
			session.close();
			return -1;
		} catch (Exception e) {
			logger.error("오류[" + e + "]");
			e.printStackTrace();
			session.close();
			return -1;
		}
	}

	/**
	 * sam 파일 생성
	 * @param paramFile
	 * @param headerForm
	 * @param dataList
	 * @throws Exception
	 */
	private void makeFile(File paramFile, MyMap headerForm, ArrayList<MyMap> dataList) throws Exception {
		if (dataList.size() > 0) {

	        String strDporDvCd = appRes.getString("ServiceDporDvCd");

	        logger.info("##### strDporDvCd ["+ strDporDvCd +"]");

			FileOutputStream fos = new FileOutputStream(paramFile);

			/******************************************************************************
			* HEADER 부 조립
			******************************************************************************/
			headerForm.setString("파일업무구분", strDporDvCd);
			headerForm.setString("구분코드", "H");
			headerForm.setString("화일구분", "B2");
			headerForm.setString("검증일자", Utils.getCurrentDate());

			byte[] bMsg = new byte[1024];
			logger.info("headerForm " + headerForm);
			int length = fp.assembleMessageByteHeadAsMap(bMsg, 1024, headerForm, "200");
			fos.write(bMsg, 0, length);
			fos.write("\n".getBytes(), 0, 1);

			/******************************************************************************
			* DATA 부 조립
			******************************************************************************/
			MyMap DataForm = new MyMap();
			for (int i = 0; i < dataList.size(); i++) {
				/**
				 * 중복cast 처리
				 */
				MyMap ht = dataList.get(i);

				logger.debug("일련번호[" + ht.getLong("SAVE_SEQ") +
						"], 은행코드[" + ht.getString("BANK_CD") +
						"], 계좌번호[" + ht.getString("ACC_NO") +
						"], 금액[" + ht.getLong("SUPPLY_AMT") +
						"], 거래구분[" + ht.getLong("거래구분") +
						"], 기관예금주[" + ht.getString("ORG_NAME") + "]");

				/**
				 * [delete]추후 정상 테스트 가능할 때 주석처리해야 함.(2022-10-24)
				 */
//		        DataForm.setString("불능코드", "000000");
//		        DataForm.setString("불능내용", "정상처리되었습니다.");
//		        DataForm.setString("실예금주", ht.getString("ORG_NAME"));

				DataForm.setString("파일업무구분", strDporDvCd); // 파일업무구분 대량예금주( DGCITY02 )
				DataForm.setString("구분코드", "D"); // 구분코드 D
				DataForm.setLong("일련번호", ht.getLong("SAVE_SEQ")); // 일련번호
				DataForm.setString("은행코드", ht.getString("BANK_CD")); // 은행코드 ex) 031( 대구은행 )
				DataForm.setString("계좌번호", ht.getString("ACC_NO")); // 계좌번호 조회할 입금계좌번호
				DataForm.setLong("금액", ht.getLong("SUPPLY_AMT")); // 금액
				DataForm.setString("기관예금주", ht.getString("ORG_NAME")); // 기관예금주 e호조에서 입력한 계좌명

				// 2012.03.13 압류방지코드 추가
				DataForm.setString("압류방지코드", ht.getString("SAL_CHR_CD")); // 압류방지코드

				/******************************************************************************
				* 파일저장
				******************************************************************************/
				length = fp.assembleMessageByteBodyAsMap(bMsg, 1024, DataForm, "200", "DGCITY02");
				fos.write(bMsg, 0, length);
				fos.write("\n".getBytes(), 0, 1);
				DataForm.clear();
			}

			/******************************************************************************
			* TAILER 부 조립
			******************************************************************************/
			MyMap TrailerForm = new MyMap();
			TrailerForm.setString("파일업무구분", strDporDvCd);
			TrailerForm.setString("구분코드", "E");
			TrailerForm.setLong("전송레코드수", dataList.size() + 2);
			TrailerForm.setLong("총의뢰건수", dataList.size());
			length = fp.assembleMessageByteTailAsMap(bMsg, 1024, TrailerForm, "200");
			fos.write(bMsg, 0, length);
			fos.write("\n".getBytes(), 0, 1);
			dataList.clear();

			fos.close();

			logger.debug("파일 생성 완료");
			dataList.clear();
		} else {
			logger.debug("파일을 생성하기 위한 데이터가 존재하지 않습니다.");
		}
	}
}
