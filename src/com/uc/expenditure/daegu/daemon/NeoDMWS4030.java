/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 대량예금주 조회 처리
 *  기  능  명 : 예금주조회 결과에 대한 e세출업무원장 예금주조회 결과 생성처리
 *  클래스  ID : NeoDMWS4030
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 *  하상원       다산(주)      2022.10.07         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.expenditure.daegu.bank.DgMessageService;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS4030 implements Runnable {
	@SuppressWarnings("unchecked")
	static Logger logger = Logger.getLogger(NeoDMWS4030.class);
	private static SqlSession session = null;
	private static SqlSessionFactory sqlMapper = null;
	private static int loopcount = 0;
	private static FormatParserAsMap fp = null;
	private static Thread self = null;
	static MyMap appRes = new MyMap();
	private static DgMessageService service = null;
	private static String IP_ADDR_4030 = "100.31.15.64";

	/**
	 * main
	 * @param args
	 */
	public static void main(String args[]) {
		DOMConfigurator.configure(NeoDMWS4030.class.getResource("/conf/log4j.xml"));

		logger.debug("##### [" + NeoDMWS4030.class.getSimpleName() + "] 시작 #####");

		NeoDMWS4030 hello = new NeoDMWS4030();
		self = new Thread(hello);
		self.setDaemon(true);
		self.start();

		try {
			self.join();
		} catch (InterruptedException e) {
		}

		logger.debug("##### [" + NeoDMWS4030.class.getSimpleName() + "] 끝 #####");
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

		String encoding = null; // 데이터베이스의 문자집합에 따라서 인코딩을 하기위함
		String folder = appRes.getString("RecvFromBank.recvDirectory");
        String strDporDvCd = appRes.getString("ServiceDporDvCd");

        logger.info("##### strDporDvCd ["+ strDporDvCd +"]");

		String recvDir = folder + "/dees_ver/recv";
		String workDir = folder + "/dees_ver/work";
		String backDir = folder + "/dees_ver/back";

		File recv_Dir = new File(recvDir);
		MyMap paramMap = new MyMap();
		MyMap verifyMap = new MyMap();

		String[] recv_file_list = recv_Dir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
//                return name.endsWith(".OK");
				return name.endsWith(".SA");
			}
		});

		if (recv_file_list == null) {
			logger.debug("파일이 없습니다");
			return -1;
		}

		int recv_file_cnt = recv_file_list.length;

		if (recv_file_cnt <= 0) {
			logger.debug("수신 된 대량이체결과파일이 없거나 수신 완료된 대량이체결과파일이 없습니다.");
			return -1;
		}

		// 데이터베이스 세션 생성, 자동확약 끔.
		try {
			session = sqlMapper.openSession(ExecutorType.BATCH, false);
		} catch (Exception ex) {
			logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
			return -1;
		}

		if (recv_file_cnt > 0) {
			logger.info("##### Neo Del 수신데이타파일 건수 : " + recv_file_cnt + "건");
		}

		for (int i = 0; i < recv_file_cnt; i++) {
			logger.info("파일명 [" + recv_file_list[i] + "]");

			if (!recv_file_list[i].endsWith(".SA")) {
				continue;
			}

			String[] file_nm = recv_file_list[i].split("\\.");
			File recv_file = new File(recvDir + "/" + file_nm[0]);
			File work_file = new File(workDir + "/" + file_nm[0]);
			File back_file = new File(backDir + "/" + file_nm[0]);
			File ok_file = new File(recvDir + "/" + recv_file_list[i]);
			boolean move_file = false;

			move_file = recv_file.renameTo(work_file);
			ok_file.delete();

			logger.debug("결과 파일을 작업 디렉토리로 이동 [" + move_file + "]");

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(work_file);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			InputStreamReader isr = null;
			try {
				isr = new InputStreamReader(fis, "euckr");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			BufferedReader br = new BufferedReader(isr);

			String str = null;
			byte[] strReceiveMessage = null;
			long beforeTime = 0;
			long l_count = 0;

			verifyMap.clear();

			try {
				while ((str = br.readLine()) != null) {
					MyMap errorMap = new MyMap();
					String indentityCd = str.substring(8, 9);

					if (indentityCd.equals("H")) {
						beforeTime = System.currentTimeMillis();
						fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "200");

						logger.info("[" + paramMap + "]");
						logger.info("##### NeoMapper4030.updateVerfifyTFE2311_start");

						verifyMap.setString("TRNX_NO", paramMap.getString("파일코드"));

						paramMap.clear();
					} else if (indentityCd.equals("D")) {
						try {
							// 파일을 읽어 map에 setting
							// 파일은 한글로 (이전과 동일)
							fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "200", "DGCITY02");

							// Neo 220830 모두 debug 로 변경할것
							logger.debug("일련번호[" + paramMap.getString("일련번호") + "]");
							logger.debug("은행코드[" + paramMap.getString("은행코드") + "]");
							logger.debug("계좌번호[" + paramMap.getString("계좌번호") + "]");
							logger.debug("금액[" + paramMap.getString("금액") + "]");
							logger.debug("거래구분[" + paramMap.getString("거래구분") + "]");
							logger.debug("기관예금주[" + paramMap.getString("기관예금주") + "]");
							logger.debug("불능코드[" + paramMap.getString("불능코드") + "]");
							logger.debug("불능내용[" + paramMap.getString("불능내용") + "]");
							logger.debug("실예금주[" + paramMap.getString("실예금주") + "]");

							String inDepositor = paramMap.getString("기관예금주");
							String outDepositor = paramMap.getString("실예금주");

							verifyMap.setString("DPOR_NM", inDepositor);							//예금주
							verifyMap.setString("NOM_DPOR_NM", outDepositor);				//정상예금주

//                            logger.debug(" paramMap.getString (불능코드 ) "  + paramMap.getString( "불능코드" ));
//                            logger.debug(" inDepositor "  + inDepositor + " outDepositor :  " + outDepositor );

							if (paramMap.getString("불능코드").equals("000000")) {

								// 계정계는 전각을 체크 못하기 때문에 18byte까지 같을 경우 정상으로 처리!
								if (Utils.isSameYegeumju(inDepositor, outDepositor) == 1) {
									logger.debug("예금주명 일치함");
									logger.debug("NEO  예금주명 일치함"); // 삭제하장

									verifyMap.setString("ACC_VRFC_RSLT_CD", "20"); 		// 검증결과 (이전 Y,N) -> 10:이체불가 20:이체가능 30:불능사유
									verifyMap.setString("ERR_RSON_CN", ""); 				// 오류사유
								} else {
//									verifyMap.setString("ACC_VRFC_RSLT_CD", "10");					//검증결과 예금주명 상이는 바꿔야함 (30) 20230310
									verifyMap.setString("ACC_VRFC_RSLT_CD", "30");					//검증결과 예금주명 상이 (30) 20230314 호조요청
									verifyMap.setString("ERR_RSON_CN", "9005-[" + inDepositor + "]이 실제예금주 명[" + outDepositor + "]과 다릅니다.");		//오류사유
								}
							}
							/**
							 * [check] 실시간 조회는 테스트 끝날때까지 임시로 막음(20221207)
							 */
//							else if (!paramMap.getString("은행코드").equals("031")
//									&& paramMap.getString("불능코드").equals("999999")) {
//
//								logger.info("은행코드 " + paramMap.getString("은행코드") + " 불능코드 : "
//										+ paramMap.getString("불능코드") + " 실시간조회를 합니다");
//								try {
//
//									if (paramMap.getString("은행코드").equals("999")) {
//
//										logger.info(paramMap);
//
//										/**
//										 * [check]To-be 기준으로 변경(20221024)
//										 */
//										verifyMap.setString("ACC_VRFC_RSLT_CD", "10"); 				// 계좌검증결과코드
//										verifyMap.setString("ERR_RSON_CN", "계좌없음"); 				// 오류사유내용(오류사유)
//									} else {
//										// 오류건에 대해 다시 체크하는 부분
//										logger.info(" Neo Del 실시간조회를 합니다");
//										service.setData("BANKCODE", paramMap.getString("은행코드"));
//										service.setData("ACCOUNT", paramMap.getString("계좌번호"));
//										service.setData("NAME", inDepositor);
//										service.setData("CURRENCY", "000000000000");
//										service.service("6015", 150);
//
//										outDepositor = service.getData("SNAME");
//
////                                        logger.info(" Neo Del 실시간조회를 합니다 outDepositor " + outDepositor);
//
//										logger.info(verifyMap.getString("거래번호") + " - RECVMAP==" + service.getDataMap());
//										logger.debug("오류코드[" + service.getData("Errcode") + "]");
//										logger.debug("오류내용[" + service.getData("ERRMSG") + "]");
//										logger.debug("은행코드[" + service.getData("BANKCODE") + "]");
//										logger.debug("계좌번호[" + service.getData("ACCOUNT") + "]");
//										logger.debug("예금주명[" + service.getData("NAME") + "]");
//										logger.debug("실예금주[" + outDepositor + "]");
//
//										verifyMap.setString("NOM_DPOR_NM", outDepositor); // 정상예금주
//
//										if (service.getData("Errcode").equals("0000")) {
//											if (Utils.isSameYegeumju(inDepositor, outDepositor) == 1) {
//												logger.debug("예금주명 일치함");
//
//												verifyMap.setString("ACC_VRFC_RSLT_CD", "20"); 		// 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
//												verifyMap.setString("ERR_RSON_CN", ""); 				// 오류사유
//											} else {
//												logger.debug("예금주명 일치하지않음");
//
//												verifyMap.setString("ACC_VRFC_RSLT_CD", "10"); 						// 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
//												verifyMap.setString("ERR_RSON_CN", "9005-[" + inDepositor + "]이 실제예금주 명[" + outDepositor + "]과 다릅니다."); // 오류사유
//											}
//										} else {
//											logger.debug("에러발생");
//
//											verifyMap.setString("ACC_VRFC_RSLT_CD", "30"); 					// 계좌검증결과코드 10:이체불가 20:이체가능 30:불능
//											verifyMap.setString("ERR_RSON_CN", service.getData("Errcode") + "-" + service.getData("ERRMSG")); // 오류사유
//										}
//									}
//
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									logger.error("error");
//								}
//
//							}
							else {
								errorMap.setString("CD", paramMap.getString("불능코드"));

								if (paramMap.getString("은행코드").equals("031")) {
									errorMap.setString("CLS_CD", "WS0001");
								} else {
									errorMap.setString("CLS_CD", "WS0002");
								}

								/**
								 * 은행전문 그대로 에러 메세지로 사용(20230117)
								 */
//								errorMap = (MyMap) session.selectOne("NeoMapper4030.getErrorCode", errorMap);

								verifyMap.setString("ACC_VRFC_RSLT_CD", "30"); 				// 계좌검증결과코드(검증결과 ) 10:이체불가, 20:이체가능, 30:불능사유

								/**
								 * 은행전문 그대로 에러 메세지로 사용(20230117)
								 */
								verifyMap.setString("ERR_RSON_CN", paramMap.getString("불능코드") + "-" + paramMap.getString("불능내용")); // 오류사유내용(오류사유)

//								if (errorMap != null) {
//									verifyMap.setString("ERR_RSON_CN", paramMap.getString("불능코드") + "-" + errorMap.getString("ERRMSG")); // 오류사유내용(오류사유)
//									logger.debug("ERRMSG[" + errorMap.getString("ERRMSG") + "]");
//								} else {
//									verifyMap.setString("ERR_RSON_CN", paramMap.getString("불능코드") + "-" + "그 외 오류"); // 오류사유내용(오류사유)
//								}
							}


							if (verifyMap.getString("ACC_VRFC_RSLT_CD").equals("30")) {							//검증결과(N)
								logger.debug("오류사유==" + verifyMap.getString("오류사유"));
							}

							verifyMap.setString("FNIS_CD", paramMap.getString("은행코드")); // 금융기관코드(은행)
							verifyMap.setString("ECRP_ACTNO", paramMap.getString("계좌번호")); // 암호화계좌번호
							verifyMap.setString("ACC_VRFC_SNUM", paramMap.getString("일련번호")); // 계좌검증일련번호(검증순번)

						} catch (Exception e) {
							verifyMap.setString("FNIS_CD", paramMap.getString("은행코드")); // 금융기관코드(은행)
							verifyMap.setString("ECRP_ACTNO", paramMap.getString("계좌번호")); // 암호화계좌번호
							verifyMap.setString("ACC_VRFC_SNUM", paramMap.getString("일련번호")); // 계좌검증일련번호(검증순번)
							verifyMap.setString("코드", paramMap.getString("불능코드"));
							verifyMap.setString("DPOR_NM", paramMap.getString("기관예금주")); // 예금주
							verifyMap.setString("NOM_DPOR_NM", paramMap.getString("실예금주")); // 정상예금주명
						} finally {
							logger.debug("검증결과[" + verifyMap.getString("ACC_VRFC_RSLT_CD") + "]"); // 계좌검증결과코드(검증결과)
							logger.debug("오류사유[" + verifyMap.getString("ERR_RSON_CN") + "]"); // 오류사유내용(오류사유)
							logger.debug("정상예금주[" + verifyMap.getString("NOM_DPOR_NM") + "]"); // 정상예금주
							logger.debug("거래번호[" + verifyMap.getString("TRNX_NO") + "]"); // 거래번호
							logger.debug("계좌번호[" + verifyMap.getString("ECRP_ACTNO") + "]"); // 계좌번호
							logger.debug("검증순번[" + verifyMap.getString("ACC_VRFC_SNUM") + "]"); // 계좌검증일련번호(검증순번)
							session.update("NeoMapper4030.updateVerfifyTFE2311", verifyMap);
							paramMap.clear();
						}
					} else if (indentityCd.equals("E")) {
						fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "200");

						long afterTime = System.currentTimeMillis();
						long secDiffTime = (afterTime - beforeTime) / 1000;

						logger.info("##### NeoMapper4030.updateVerfifyTFE2311_end");
						logger.info("작업끝 : " + verifyMap.getString("TRNX_NO") + " [" + paramMap + "]  [ " + secDiffTime + " 초 ]"); // 거래번호
						paramMap.clear();
					}

					l_count = l_count + 1;

					if (l_count % 1000 == 0) {
						logger.info("l_count : " + l_count);
						session.flushStatements();
//						session.commit();
					}
				}

				session.flushStatements();
				session.commit();

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				br.close();
			} catch (IOException e) {
				logger.error("파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				isr.close();
			} catch (IOException e) {
				logger.error("파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			logger.debug("결과 업데이트 정상적으로 되었기 때문에 e-hojo 업데이트 비트를 등록합니다.");
			logger.info("Neo Del 결과 업데이트 정상적으로 되었기 때문에 e-hojo 업데이트 비트를 등록합니다.");

			logger.debug("거래번호[" + verifyMap.getString("TRNX_NO") + "]"); // 거래번호
			logger.debug("계좌번호[" + verifyMap.getString("ECRP_ACTNO") + "]"); // 계좌번호
			logger.debug("검증순번[" + verifyMap.getString("ACC_VRFC_SNUM") + "]"); // 검증순번

			/**
			 * [result]
			 * 해당 아래 로직은 어차피 계좌번호단위로 결과를 업데이트를 하는데 별도로 검증단계를 업데이트를 따로 할 필요 없을거 같음.-신상훈수석님과 협의하여 검증결과를 업데이트할때 같이 처리하는걸로 변경(20221129)
			 */
//			MyMap veriKeyMap = (MyMap) session.selectOne("NeoMapper4030.selectTFE2311", verifyMap);
//
//			if (veriKeyMap == null) {
//				logger.debug("해당 내역이 조회안됨");
//				logger.info("Neo Del @=@=@=@= veriKeyMap 해당 내역이 조회안됨.");
//			} else {
//				logger.debug("거래번호[" + veriKeyMap.getString("TRNX_NO") + "]");
//				logger.debug("자치단체코드[" + veriKeyMap.getString("LAF_CD") + "]");
//				logger.debug("지출단계[" + veriKeyMap.getString("EP_BYLV_DV_CD") + "]");
//				logger.debug("지출번호구분[" + veriKeyMap.getString("EP_NO_DV_CD") + "]");
//				logger.debug("지출순번[" + veriKeyMap.getString("EP_SNUM") + "]");
//				logger.debug("계좌번호[" + veriKeyMap.getString("ECRP_ACTNO") + "]");
//
//				int upcnt = session.update("NeoMapper4030.updateTFE2311", veriKeyMap);
//
//				if (upcnt > 0) {
//					logger.debug("결과처리함");
//				} else {
//					logger.debug("결과처리못함");
//				}
//				session.commit();
//			}

			logger.info("backup folder trans...!!! ");
			move_file = work_file.renameTo(back_file);

//          work_file.delete();
		}

		logger.info("작업 완료!!!");
		session.close();
		return 0;
	}
}
