/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 입금지급 처리(한도계좌)
 *  기  능  명 : e세출업무원장 지급요청건 sam파일 생성처리
 *  클래스  ID : NeoDMWS5021
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  하상원       다산(주)      2022.10.25         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import com.uc.core.MapForm;
import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.NeoUtils;
import com.uc.framework.utils.Utils;

public class NeoDMWS5021 implements Runnable
{
    @SuppressWarnings("unchecked")
	static Logger	logger = Logger.getLogger(NeoDMWS5021.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;

    private static FormatParserAsMap fp = null;
    private static Thread	self = null;
    static MyMap    appRes = new MyMap();

    //데몬 실행시 실시간 전송 대상 건수를 파라미터로 받아 로직을 생성(2022-09-13)
    private static int termCount = 0;
    private static String FLAG_Y = "Y";
    private static String FLAG_ALL = "A";

    public static void main(String args[])
    {
	  	DOMConfigurator.configure(NeoDMWS5021.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS5021.class.getSimpleName() + "] 시작 #####");

        if (args.length == 1) {
            termCount = Integer.parseInt(args[0]);
        } else {
            logger.info("실시간 전송 실행 하지 않음.!!!");
        }

        logger.info("##### termCount["+termCount+"]");

        NeoDMWS5021  neoDMWS5020 = new NeoDMWS5021();

        self = new Thread(neoDMWS5020);
        self.setDaemon(true);
        self.start();

        try {
        	self.join();
        } catch (InterruptedException e) {
        	logger.debug("InterruptedException ::: ["+ e.getMessage() +"]");
        }

        logger.info("##### [" + NeoDMWS5021.class.getSimpleName() + "] 끝 #####");
    }

    /**
     * 데몬 기본 실행
     */
 	public void run()
	{
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

	    if (fp.doParsingAsMap("msgformat1") < 0) {
	    	logger.error("전문포맷 분석오류");
	    	return;
	    }

        Utils.getResources("conf/ApplicationResources", appRes);

        if(termCount == 0) {
        	termCount = appRes.getInteger("ServiceRealSendStbxCnt");
        }

		while (!Thread.currentThread().isInterrupted()) {
			mainLoop();

			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				break;
			}

			logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 실행");
		}
	}

 	/**
 	 * process실행
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
	public int mainLoop()
	{
		// 데이터베이스 세션 생성, 자동확약 끔.
		try {
		    session = sqlMapper.openSession(false);
		} catch (Exception ex) {
	    	logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
	    	return -1;
	    }

        String  folder = appRes.getString("NeoDMWS5020.directory");

		MyMap   paramMap      = new   MyMap();
		MyMap   headerMap     = new   MyMap();
		MyMap   headerFileKey = new   MyMap();

		ArrayList<MyMap>	viewList = null;
		ArrayList<MyMap>	dataList = null;
		ArrayList<MyMap>	updateList = null;

        logger.debug("##### mainLoop() Started !!!!!");

		paramMap.setString("현재일자", Utils.getCurrentDate());

		try {
			/**
			 * 실시간 처리 결과 받지 못한 건에 대해서 30분이  지나면 재전송하기 위해 상태코드를 변경해 줌.(20221121)
			 */
			MyMap paramRestoreObj = new MyMap();
//			session.update("NeoMapper5020.updateSendBankRealTimeStbxRestore", paramRestoreObj);
//			session.commit();

			updateList = (ArrayList<MyMap>)session.selectList("NeoMapper5020.selectStbxRestoreTFE2190", paramRestoreObj);
			if (updateList.size() <= 0) {
				logger.info("실시간 처리결과 받지 못한 건이 없습니다.");
			}else {
				logger.info("실시간 처리결과 받지 못한 건이 " + updateList.size() + "개 있습니다.");
				for (int j= 0 ; j < updateList.size() ; j++) {

				    MyMap	ht = updateList.get(j);
				    logger.info("거래번호[" + ht.getString("TRNX_NO") + "]");
				    session.update("NeoMapper5020.updateSendBankRealTimeStbxRestore", ht);
				}
				session.commit();
				logger.info("작업 완료!!!");
			}

			viewList = (ArrayList<MyMap>)session.selectList("NeoMapper5020.selectTFE2190Stbx", paramMap);

			if (viewList.size() <= 0) {
				logger.info("신규 등록된 데이터가 없습니다.");
				session.close();
				return -1;
			}

			String	makeDir = folder + "/dees_trn/send";
			File dir   = new File(makeDir);

			if (!dir.exists()) {
			    logger.debug("신규 등록된 지급 원장이 " + viewList.size() + "개 있습니다.");
				logger.error("파일을 생성할 디렉토리가 존재하지 않습니다.");
				session.close();
				return -1;
			}

			logger.debug("신규 등록된 지급 원장이 " + viewList.size() + "개 있습니다.");

	        String strLimAccDvCd = appRes.getString("ServiceLimAccDvCd");

	        logger.debug("##### header LimAccDvCd ["+ strLimAccDvCd +"]");

			/******************************************************************************
			* 파일생성 루프
			******************************************************************************/
			for (int i = 0 ; i < viewList.size() ; i++) {
				/**
				 * 중복 cast 됨.
				 */
//			    MyMap	ht = (MyMap)viewList.get(i);
			    MyMap	ht = viewList.get(i);

			    /******************************************************************************
		        * 한도계좌조회
				******************************************************************************/
            	MyMap paramObj = new MyMap();
            	paramObj.setString("TRNX_NO", ht.getString("TRNX_NO"));

            	MyMap mapDrwActno = (MyMap) session.selectOne("NeoMapperFile.getLimitAccount", paramObj);
            	String trnxGbn = NeoUtils.getGbnCode(mapDrwActno);	//한도계좌여부확인
            	logger.debug("##### trnxGbn["+ trnxGbn +"]");

            	ht.setString("trnxGbn", trnxGbn);
             	ht.setString( "ANE_LIM_ACC_YN"			, mapDrwActno.getString( "ANE_LIM_ACC_YN"));
             	ht.setString( "DPST_ANE_LIM_ACC_YN"		, mapDrwActno.getString( "DPST_ANE_LIM_ACC_YN"));

			    /******************************************************************************
	            * HEADER 부 조립
				******************************************************************************/
			    headerMap.setString("파일업무구분", 	strLimAccDvCd); // 예약이체파일(DGCITY01)
				headerMap.setString("구분코드", 		"H"); 			// H : Header
				headerMap.setString("파일구분", 		"B2"); 			// B2 : 업체송신시, 2B : 은행결과송신시
				headerMap.setString("재배정여부", 		ht.getString("재배정여부")); // Y: 재배정자료, N:일반배정자료
				headerMap.setString("이체일자", 		ht.getString("이체일자")); // yyyymmdd
				headerMap.setString("파일코드", 		ht.getString("거래번호")); // 거래번호

				/**
				 * 한도계좌 금고/계정계 모두 대상일 경우 한도계좌에 맵핑 되어 있는 통합 실제 계좌를 세팅(20221007)
				 * 5021데몬에서는 한도계좌건에 대해서만 처리하기 때문에 한도계좌 기준으로 맞춤(20221025)
				 */
				if(FLAG_ALL.equals(trnxGbn)) {
//					headerMap.setString("출금계좌번호", 	ht.getString("출금실제계좌번호").replaceAll("-", "")); // 출금계좌번호
					headerMap.setString("출금계좌번호", 	ht.getString("출금계좌번호").replaceAll("-", "")); // 출금계좌번호

					headerMap.setString("파일불능코드", ht.getString("TRSFR_PRCS_RSLT_CD"));
					headerMap.setString("파일불능내용", ht.getString("TRSFR_PRCS_RSLT_CN"));
					headerMap.setString("불능분입금계좌번호", ht.getString("별단계좌번호").replaceAll("-", ""));
//			        headerForm.setString("출금계좌관리점", "018");
//			        headerForm.setString("출금계좌관리점명", "대명동지점");
				} else {
					headerMap.setString("출금계좌번호", 	ht.getString("출금계좌번호").replaceAll("-", "")); // 출금계좌번호
				}

				headerMap.setString("입금명세구분", 	ht.getString("입금명세구분"));
				headerMap.setString("요청ID", 		ht.getString("요청ID"));
				headerMap.setString("요청기관구분", 	ht.getString("요청기관구분"));
				headerMap.setString("자치단체코드", 	ht.getString("자치단체코드"));
				headerMap.setString("관서코드", 		ht.getString("관서코드"));
				headerMap.setString("지급부서코드", 	ht.getString("지급부서코드"));
				headerMap.setString("회계연도", 		ht.getString("회계연도"));
				headerMap.setString("회계코드", 		ht.getString("회계코드"));
				headerMap.setString("자료구분", 		ht.getString("자료구분"));
				headerMap.setString("지급명령등록번호", ht.getString("지급명령등록번호"));
				headerMap.setString("지급명령번호", 	ht.getString("거래번호").substring(8));
				headerMap.setString("자치단체명", 		ht.getString("자치단체명"));
				headerMap.setString("관서명", 		ht.getString("관서명"));
				headerMap.setString("지급부서명", 		ht.getString("지급부서명"));
				headerMap.setString("급여구분", 		ht.getString("급여구분"));

				/**
				 * 금고요청으로 전문 헤더 공란에 요구부서코드 세트(20221102)
				 */
				headerMap.setString("예비", 		ht.getString("DND_DEPT_CD"));
				headerMap.setString("공란", 		ht.getString("DND_DEPT_CD"));

				//로그정보
				logger.debug("##### headerMap["+headerMap.toString()+"]");

				dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5020.selectTFE2170Stbx", ht);

				logger.debug("지급명령등록번호[" + headerMap.getString("지급명령등록번호") + "]의 신규 등록된 입금 명세가 " + dataList.size() + "개 있습니다.");

				if (dataList.size() <= 0) {
					logger.debug("계정계로 보낼 데이터가 없기 때문에 원장을 완료 상태로 바꿉니다.");
					session.update("NeoMapper5020.updateTFE2190_ERR", ht);
					ht.clear();
					continue;
				}

				//금고용 파일 오브젝트 생성
				File infoStbxfile = new File(makeDir + "/" + ht.getString("거래번호") + "_STBX");

				logger.debug("파일이름[" + makeDir + "/" + ht.getString("거래번호") + "_STBX" + "]");

				logger.debug("termCount " + termCount);

                if (dataList.size() <= termCount) {
    				/**
    				 * 실시전송일 때는 응답결과를 update하기 전에 구분이 필요하기 때문에 한도구분을 예비 항목에 세트 함.(20221025)
    				 */
    				headerMap.setString("trnxGbn", 		ht.getString("trnxGbn")); // 거래번호

    				logger.info("거래번호(start) :  " + ht.getString("거래번호"));
                	/**
                	 * 한도계좌 금고처리 실시간 전송 대상 건 요청(20221007)
                	 */
	                SendBankRealTime(headerMap, dataList, ht); // 실시간 은행전문전송 호출
	                logger.info("거래번호(end)");
                	/**
                	 * 거래번호 단위로 한도계좌 거래내역을 등록 함.(4020데몬에서 5021데몬으로 위치 변경)-20221109
                	 */
                	try {
                		logger.info("ANE_LIM_ACC_YN : " + mapDrwActno.getString("ANE_LIM_ACC_YN"));
	                    if(FLAG_Y.equals(mapDrwActno.getString("ANE_LIM_ACC_YN"))) {
	                        int prcCnt = (Integer) session.selectOne("NeoMapper4020.getTFE2190ToTFMA050APrcCnt", ht );

	                        if (prcCnt <= 0) {
	                        	session.insert( "NeoMapper4020.insertTFE2190ToTFMA050A" , ht );
	                        }
	                    }
	                    logger.info("DPST_ANE_LIM_ACC_YN : " + mapDrwActno.getString("DPST_ANE_LIM_ACC_YN"));
	                    if(FLAG_Y.equals(mapDrwActno.getString("DPST_ANE_LIM_ACC_YN"))) {
	                        int prcCnt = (Integer) session.selectOne("NeoMapper4020.getTFE2170ToTFMA050APrcCnt", ht );

	                        if (prcCnt <= 0) {
	                        	session.insert( "NeoMapper4020.insertTFE2170ToTFMA050A" , ht );
	                        }
	                    }

	                    session.commit();
	                } catch (Exception e) {
	                    session.rollback();
	                    logger.error( "거래번호 [" + ht.getString( "TRNX_NO" ) + "] 한도계좌 거래내역 작업 중 에러:" + e.getLocalizedMessage());
	                }

                	ht.clear();

                } else {
                	/**
                	 * [check]
                	 * sam file은 18시 이후에 처리 되도록 처리.(20221108)
                	 * 자금배정의 Y건은 실시간 처리가 되어야 해서 A건에 대해서 18시 이후에 처리하는 것으로 변경(20221111)
                	 */
                    String  strSvrType = appRes.getString("RecvFromBank.server");

                    logger.debug("### strSvrType["+strSvrType+"]");

                    if( !"REAL".equals(strSvrType)) {
                    	logger.debug("### strSvrType[DEVL]");
                    } else {
                    	logger.debug("### strSvrType[REAL]");

                    	if(FLAG_ALL.equals(trnxGbn)) {
    	                	if(checkSendTime() < 0) {
    	                		continue;
    	                	}
                    	}
                    }

                	/**
                	 * 한도계좌 금고 대상일 경우 별도 금고파일 생성(20221007)
                	 * 한도계좌만 처리하기 때문에 복사한 map을 쓰지 않고 원래 로직의 map을 활용.(20221025)
                	 */
                	makeStbxFile(infoStbxfile, headerMap, headerFileKey, dataList);         // 파일생성 처리

                	try {
	                	//한도계좌가 금고 전송건이면 금고전송여부만 상태를 변경하여 금고로만 전송
                		logger.info("### trnxGbn :: " + trnxGbn + ":: ht :: " + ht);
	                	if(FLAG_Y.equals(trnxGbn)) {
	                        session.update("NeoMapper5020.updateStbxFileMakeOK", ht); // 금고파일 생성 후 데이타 처리 (금고전송여부 = 'A' or 'Y' 로 업데이트)
	                	} else {
	                        session.update("NeoMapper5020.updateStbxFileMakeOK", ht); // 파일 생성 후 데이타 처리 (금고전송여부 = 'A' or 'Y'로 업데이트)
	                	}

	                	session.commit();
                	} catch(Exception e1) {
                		session.rollback();
                		logger.error( "거래번호 [" + ht.getString( "TRNX_NO" ) + "] sam파일 작업 중 에러:" + e1.getLocalizedMessage());
                	}

                	/**
                	 * 거래번호 단위로 한도계좌 거래내역을 등록 함.(4020데몬에서 5021데몬으로 위치 변경)-20221109
                	 */
                	try {

	                    if(FLAG_Y.equals(mapDrwActno.getString("ANE_LIM_ACC_YN"))) {
	                        int prcCnt = (Integer) session.selectOne("NeoMapper4020.getTFE2190ToTFMA050APrcCnt", ht );

	                        if (prcCnt <= 0) {
	                        	session.insert( "NeoMapper4020.insertTFE2190ToTFMA050A" , ht );
	                        }
	                    }

	                    if(FLAG_Y.equals(mapDrwActno.getString("DPST_ANE_LIM_ACC_YN"))) {
	                        int prcCnt = (Integer) session.selectOne("NeoMapper4020.getTFE2170ToTFMA050APrcCnt", ht );

	                        if (prcCnt <= 0) {
	                        	session.insert( "NeoMapper4020.insertTFE2170ToTFMA050A" , ht );
	                        }
	                    }

	                    session.commit();
	                } catch (Exception e) {
	                    session.rollback();
	                    logger.error( "거래번호 [" + ht.getString( "TRNX_NO" ) + "] 한도계좌 거래내역 작업 중 에러:" + e.getLocalizedMessage());
	                }

                	ht.clear();

                    logger.info("작업 완료!!!");
                }
			}

			session.close();
			return -1;
		} catch(Exception e) {
		    logger.error("오류[" + e + "]");
			e.printStackTrace();
			session.close();
			return -1;
		}
	}

	/**
	 * 금고용 sam 파일 생성
	 * @param paramFile
	 * @param headerForm
	 * @param headerFileKeyForm
	 * @param dataList
	 * @return
	 * @throws Exception
	 */
	private int makeStbxFile(File paramFile, MyMap headerForm, MyMap headerFileKeyForm, ArrayList<MyMap> dataList) throws Exception
	{
		FileOutputStream	fos = null;
		try {
			fos = new FileOutputStream(paramFile);
		} catch (FileNotFoundException fnfex) {
			logger.error("오류[" + fnfex.getLocalizedMessage() + "]");
		}

        logger.debug("##### makeStbxFile() Started !!!!!");

        String strLimAccDvCd = appRes.getString("ServiceLimAccDvCd");

        logger.info("##### LimAccDvCd ["+ strLimAccDvCd +"]");

        /******************************************************************************
		* HEADER 섹션 파일 저장
		******************************************************************************/
        byte[] bMsg = new byte[1024];

		int length = fp.assembleMessageByteHeadAsMap(bMsg, 1024, headerForm, "100");
		fos.write(bMsg, 0, length);
		fos.write("\n".getBytes(), 0, 1);

		/******************************************************************************
        * DATA 부 조립 및 저장
		******************************************************************************/
		long tot_am = 0;
		long tmp_am = 0;
        MyMap DataForm    = new MyMap();

		for (int i = 0; i < dataList.size(); i++) {
			/**
			 * 중복 cast 됨.
			 */
//			MyMap	ht = (MyMap) dataList.get(i);
			MyMap	ht = dataList.get(i);

			/**
			 * 이체결과가 정상건이 아닌건은 금고전송 대상에 포함 시키지 않음.
			 * 정상건 상관 없이 다 보내기로 함.(20221221)
			 */
//			if(!"0000".equals(ht.getString("TRSFR_PRCS_RSLT_CD"))) {
//				if(ht.getString("TRSFR_PRCS_RSLT_CD") != null && !"".equals(ht.getString("TRSFR_PRCS_RSLT_CD"))) {
//					ht.clear();
//					continue;
//				}
//			}

			tmp_am = ht.getLong("입금금액"); // 입금금액
			tot_am = tot_am + tmp_am;  // 총의뢰금액

			DataForm.setString("파일업무구분", 	strLimAccDvCd);
			DataForm.setString("구분코드", 	"D");
			DataForm.setString("입금일련번호", 	ht.getString("입금일련번호"));
			DataForm.setString("입금은행코드", 	ht.getString("입금은행코드"));
			DataForm.setString("입금유형", 	ht.getString("입금유형"));

			// 2012.03.23 신상훈 : 입급유형이 고지서(40), 현금(99), 수표(60)인경우 계좌번호값을 '111111' 로 변경
			if(ht.getString("입금유형").toString().equals("40")||
			   ht.getString("입금유형").toString().equals("60")||
			   ht.getString("입금유형").toString().equals("99") ) {
			    //DataForm.setString("입금은행코드", "031");
			    DataForm.setString("입금계좌번호", "111111");

			    logger.info("##### 입급유형 고지서_현금_수표 : ["+DataForm.toString()+"]");
			} else {
			    DataForm.setString("입금계좌번호", ht.getString("입금계좌번호").replaceAll("-", ""));
			}

			DataForm.setString("입금계좌예금주명", 	ht.getString("입금계좌예금주명"));
			DataForm.setString("입금적요", 		ht.getString("입금명세"));

			logger.debug("자치단체코드:[" + headerForm.getString("자치단체코드") + "]");
			logger.debug("입금명세구분:[" + headerForm.getString("입금명세구분") + "]");
			logger.debug("압류방지코드["  + ht.getString("압류방지코드") + "]");

			if (headerForm.getString("입금명세구분").equals("1")) {
				//PAY_GB : "1" 급여
				//ht.put("입금적요", "급여");
				DataForm.setString("입금적요", "급여");
			} else {
				DataForm.setString("입금적요", 	ht.getString("입금명세"));
			}

			DataForm.setLong("이체금액", tmp_am);

			/**
			 * [delete] @@@@ 자료구분, 거래구분 관련 주석막은 소스 지움 @@@@@
			 */
			DataForm.setString("거래구분", headerForm.getString("자료구분"));

			/******************************************************************************
			* "입금유형"이 50 : CMS 인 경우 CMS계좌번호를 세트 한다.
			******************************************************************************/
			if (DataForm.getString("입금유형").equals("50")) {
				DataForm.setString("CMS번호", ht.getString("CMS번호"));
			}

			/******************************************************************************
			* 압류방지코드
			******************************************************************************/
			if (ht.getString("압류방지코드") == "") {
				DataForm.setString("압류방지코드", "00");
			} else {
				DataForm.setString("압류방지코드", ht.getString("압류방지코드"));
			}

			DataForm.setString("처리여부", ht.getString("TRSFR_RSLT_PRCS_YN"));
			DataForm.setString("이체처리불능코드", ht.getString("TRSFR_PRCS_RSLT_CD"));	// 정상샘플
			DataForm.setString("이체처리불능내용", ht.getString("TRSFR_PRCS_RSLT_CN"));

			length = fp.assembleMessageByteBodyAsMap(bMsg, 1024, DataForm, "100", "DGCITY01");
			fos.write(bMsg, 0, length);
			fos.write("\n".getBytes(), 0, 1);
			DataForm.clear();
		}

		/******************************************************************************
        * TRAILER 부 조립
		******************************************************************************/
		MyMap TrailerForm = new MyMap();

        logger.debug("TRAILER 부 조립 시작");

		TrailerForm.setString("파일업무구분", 	strLimAccDvCd); 		// 예약이체파일(DGCITY01)
		TrailerForm.setString("구분코드", 		"E"); 					// 구분코드 E : Tailer

		TrailerForm.setLong("전송레코드수", 	dataList.size() + 2); 	// 파일의 레코드 건수

		TrailerForm.setLong("총의뢰건수", 		dataList.size()); 		// 헤더, 트레일러 제외한 파일레코드 건수
		TrailerForm.setLong("총의뢰금액", 		tot_am); 				// 데이타부 합계금액

		// 테스트
		TrailerForm.setLong("정상처리건수", 	dataList.size()); 		// 헤더, 트레일러 제외한 파일레코드 건수
		TrailerForm.setLong("정상처리금액", 	tot_am); 				// 데이타부 합계금액

		TrailerForm.setLong("미처리건수", 		0); 					// 헤더, 트레일러 제외한 파일레코드 건수
		TrailerForm.setLong("미처리금액", 		0); 					// 데이타부 합계금액

		TrailerForm.setLong("지급명령총건수", 	dataList.size()); 		// DATA부의 지급총건수
		TrailerForm.setLong("지급명령총금액", 	tot_am); 				// DATA부의 지급총금액

		length = fp.assembleMessageByteTailAsMap(bMsg, 1024, TrailerForm, "100");
		fos.write(bMsg, 0, length);
		fos.write("\n".getBytes(), 0, 1);
		dataList.clear();
		fos.close();

		logger.info("파일 생성 완료");
		dataList.clear();

		return 0;
	}

	/**
	 * 거래번호 조회
	 * @param mapForm
	 * @return
	 * @throws Exception
	 */
	public long getSqNo(MyMap mapForm) throws Exception
	{
		long sqNo = 0;
		SqlSession	ss = sqlMapper.openSession(false);

		try {
			try {
				long	nCnt = (Long)ss.selectOne("NeoMapper5020.nCntSelect_5020", mapForm);
				logger.debug("COUNT[" + nCnt + "]");

				if (nCnt > 0) {
					sqNo = (Long)ss.selectOne("NeoMapper5020.SqNoSelect_5020", mapForm);
				}
			} catch (NullPointerException e) {
				// null 포인터는 무시
				logger.error("SqNoSelect Null Point Error");
			}

			sqNo++;

			mapForm.setLong("FILESQNO", sqNo);
			if (sqNo == 1) {
				ss.insert("NeoMapper5020.SqNoInsert_5020", mapForm);
			} else {
			    ss.update("NeoMapper5020.SqNoUpdate_5020", mapForm);
			}

			ss.commit();
		} finally {
		    ss.close();
		}
		return sqNo;
	}

	/**
	 * 10건 이하일 경우 실시간 지급 전송 처리
	 * (10건이하 파일 처리 시 시간 더걸림. 10건이하가 50% 넘음)
	 * @param headerMap
	 * @param dataList
	 * @param ht
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    private void SendBankRealTime(MyMap headerMap, ArrayList<MyMap> dataList,MyMap ht) throws Exception {
        Etdm5020Msgfield    sendData = new    Etdm5020Msgfield();
        MapForm inqMap = new MapForm();

        // 공통부 설정 공통부길이 : 59 업무 : 39 반복 : 데이터사이즈 * 23
        // 현재는 아무값 들어가있지만 ?
        logger.info(headerMap);
//        String TRNX_NO  = headerMap.getMap("파일코드").toString().substring(8); //지급원장 거래번호 뒷자리 6자리.
        int gLength = (sendData.getHeadLen() + (dataList.size() * sendData.getRepetLen())) - 4;

        MyMap mMap = new MyMap();

        String IP_ADDR = appRes.getString("ServiceRealTimeStbxIP");
        int IP_PORT = appRes.getInteger("ServiceRealTimeStbxPort");

        String strLimAccDvCd = appRes.getString("ServiceLimAccDvCd");

        logger.info("##### real send LimAccDvCd ["+ strLimAccDvCd +"]");

        /*전문번호 세팅.*/
        mMap.setString("JUMBUN", "031");
        mMap.setString("거래일자", Utils.getCurrentDate());
        mMap.setString("거래구분", "JB");

        logger.debug("점번 = " + mMap.getString("JUMBUN"));
        logger.debug("거래일자 = " + mMap.getString("거래일자"));
        logger.debug("거래구분 = " + mMap.getString("거래구분"));

        long seqNo = getSqNo(mMap);
        logger.debug("seqNo ::: ["+ seqNo +"]");

        inqMap.setMap("전문길이",		gLength);
        inqMap.setMap("식별코드",		("TRFRB001"));
        inqMap.setMap("업체번호",		strLimAccDvCd);
        inqMap.setMap("은행코드",		("31"));
        inqMap.setMap("전문구분코드",	("4100"));
        inqMap.setMap("업무구분코드",	("400"));
        inqMap.setMap("송신횟수",		("1"));
//      inqMap.setMap("전문번호",		TRNX_NO );
        inqMap.setMap("전문번호",		seqNo);
        inqMap.setMap("전송일자",		Utils.getCurrent("yyyyMMdd"));
        inqMap.setMap("전송시각",		Utils.getCurrent("HHmmss"));

        // 업무설정
        inqMap.putAll(headerMap); //header 조립 한 부분 전체 putAll
//        inqMap.setMap("파일불능코드",   "0006"); // 은행 테스트 용 , 0006->반려
//        inqMap.setMap("파일불능코드",   "0000"); // 은행 테스트 용 , 0000->정상

        long tot_am = 0;
        long tmp_am = 0;

        /*입금명세 조립.*/
        for (int i = 0; i < dataList.size(); i++) {
        	/**
        	 * 중복 cast 됨.
        	 */
//            MyMap   htm = (MyMap) dataList.get(i);
            MyMap   htm = dataList.get(i);

            /**
             * 이체결과가 정상건이 아닌건은 금고전송 대상에 포함 시키지 않음.
             * 정상건 상관 없이 다 보내기로 함.(20221221)
             */
//			if(!"0000".equals(htm.getString("TRSFR_PRCS_RSLT_CD"))) {
//				if(htm.getString("TRSFR_PRCS_RSLT_CD") != null && !"".equals(htm.getString("TRSFR_PRCS_RSLT_CD"))) {
//					htm.clear();
//					continue;
//				}
//			}

            tmp_am = htm.getLong("입금금액"); // 입금금액
            tot_am = tot_am + tmp_am;  // 총의뢰금액

            if(htm.getMap("입금유형").toString().equals("10"))
            {
            	dataList.get(i).setMap("입금계좌번호", htm.getMap("입금계좌번호").toString().replaceAll("-", ""));
            }
            	dataList.get(i).setMap("입금적요", htm.getMap("입금명세"));

            dataList.get(i).setMap("이체금액", tmp_am);
            dataList.get(i).setMap("거래구분", headerMap.getMap("자료구분"));

            dataList.get(i).setMap("입금일련번호", htm.getMap("입금일련번호"));

            /**
             * 결과분 추가
             */
            dataList.get(i).setMap("처리여부", htm.getString("TRSFR_RSLT_PRCS_YN"));
            dataList.get(i).setMap("이체처리불능코드", htm.getString("TRSFR_PRCS_RSLT_CD"));
            dataList.get(i).setMap("이체처리불능내용", htm.getString("TRSFR_PRCS_RSLT_CN"));
        }

        /* trail 부분 조립. */
        inqMap.setMap("전송레코드수",	dataList.size());
        inqMap.setMap("총의뢰건수",		dataList.size());
        inqMap.setMap("총의뢰금액",		tot_am);
        inqMap.setMap("정상처리건수",	dataList.size());
        inqMap.setMap("정상처리금액",	tot_am);
        inqMap.setMap("지급명령총건수",	dataList.size());
        inqMap.setMap("지급명령총금액",	tot_am);

        OutputStream os = null;
        Socket sendSocket = null;

        try {
            byte[] makeMsgBuff  = null;                                     // 전송할 전문
            makeMsgBuff = sendData.makeSendReptBuffer(inqMap, dataList);

            logger.debug("##### sendData : " + new String(makeMsgBuff,"euc-kr") + "," +  (new String(makeMsgBuff)).length()) ;
//            logger.debug("##### sendData : " + dataList) ;

            /**
             * 금고에만 보내는 건일 경우에만 상태를 update 함.(20221025)
             */
            if("Y".equals(headerMap.getString("trnxGbn"))) {
	            int sendbankCount = session.update("NeoMapper5020.updateSendBankRealTimeStbx", ht); // 01, 06로 수정 . 보낸것은 06 재전송을 막기 위해.

	            if ( sendbankCount > 0 ) {
	            	logger.info("01, 06 수정 [ " + sendbankCount + " ] 건 업데이트 되었습니다.");
	            } else {
	            	logger.info("01, 06 수정 [ " + sendbankCount + " ] 건 업데이트 되었습니다. 업데이트 되지 않았음. ");
	            }

	            session.commit();
            } else if("A".equals(headerMap.getString("trnxGbn"))) {
	            int sendbankCount = session.update("NeoMapper5020.updateSendBankRealTimeCaseAStbx", ht); // 수신일자로 수정 . 보낸것은 재전송을 막기 위해.

	            if ( sendbankCount > 0 ) {
	            	logger.info("수신일자 현재로 수정 [ " + sendbankCount + " ] 건 업데이트 되었습니다.");
	            } else {
	            	logger.info("수신일자 현재로 수정 [ " + sendbankCount + " ] 건 업데이트 되었습니다. 업데이트 되지 않았음. ");
	            }

	            session.commit();
            }

            sendSocket = new Socket(IP_ADDR , IP_PORT);
            os = sendSocket.getOutputStream();
            os.write(makeMsgBuff);
            os.flush();

        } catch (Exception e1) {
            logger.error("### 에러발생 : " + e1);
            logger.error("### 에러발생 : [" + dataList + "]");
            session.rollback();
            try {
            	os.close();
            } catch (Exception ec) {
            	logger.error("### client close 에러발생 : " + ec);
            	logger.error("### client close 에러발생 : [" + dataList + "]");
            }
        } finally {
        	os.close();

            /**
             * [result]시간간격을 줌(2022-10-13)
             */
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("에러발생 : " + e);
			}
        }
        logger.info("작업 완료!!!");
    }

	/**
	 * CMS는 18시 이후에 일괄처리하기 위해 발송시간을 체크
	 * @return
	 * @throws Exception
	 */
    private int checkSendTime() throws Exception {
    	// 전송시간체크
        java.util.Date  s1 = null;
        java.util.Date  s2 = null;
        int rsultIdx = 0;

        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            s1 = sf1.parse(Utils.getCurrent("yyyyMMddHHmmss"));
            s2 = sf2.parse(Utils.getCurrent("yyyyMMdd") + appRes.getString("sendStbxTime"));

            if(s1.before(s2)) {
                logger.debug("전송시간대가 아닙니다");
                logger.debug("현재시간 : " + s1);
                logger.debug("전송시간 : " + s2);

                rsultIdx = -1;
            }
        } catch (ParseException e1) {
            logger.error("#### time check Error : ["+ e1.getMessage() +"]");
            rsultIdx = -1;
        }

        return rsultIdx;
	}
}