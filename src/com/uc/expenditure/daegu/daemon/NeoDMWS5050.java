/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출  공통 코드 원장생성
 *  기  능  명 : e호조요청건 e세출업무원장 전송처리, (회계정보, 부서정보,사용자정보 ( 공통신규))
 *
 *  클래스  ID : NeoDMWS3050
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박미정       다산(주)      2022.08.29         %01%             최초작성
 *  신상훈       다산(주)      2022.11.23         %01%             추가
 */
package com.uc.expenditure.daegu.daemon;

import  org.apache.log4j.Logger;
import  org.apache.log4j.xml.DOMConfigurator;
import  java.io.File;
import  java.io.FileOutputStream;
import  java.io.Reader;
import  java.util.ArrayList;
import java.util.Properties;

import  org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import  org.apache.ibatis.session.SqlSession;
import  org.apache.ibatis.session.SqlSessionFactory;
import  org.apache.ibatis.session.SqlSessionFactoryBuilder;

import  com.uc.framework.parsing.FormatParserAsMap;
import  com.uc.framework.utils.*;

public class NeoDMWS5050 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger    logger = Logger.getLogger(NeoDMWS5050.class);
    private static SqlSessionFactory sqlMapper = null;
    private static FormatParserAsMap          fp = null;
    private static Thread[] self = new Thread[7];
//    private static Thread[] self = new Thread[3];

    static MyMap    appRes = new MyMap();
    private String sysDvCd = "DG"; // DG : 대구, GB : 경북 , applicationResources 에 정의됨.

    private int comCdId  = 0;

    // 공통코드 9개, 수신(일상경비) 중
    // 회계구분정보, 부서정보, 사용자정보, 분야부문, 세출통계목, 세입목코드, 세입목코드매핑,  현금종류송신, 금고은행시스템출금계좌검증정보송신
    // 사용자정보(CU), 현금종류송신(CK)을 제외한 7가지 코드만 sam 파일 생성
    /*
     * CA : 회계구분정보
     * CD : 부서정보
     * TB : 분야부문
     * TS : 세출통계목
     * TC : 세입목코드
     * TM : 세입목코드매핑
     * WA : 금고은행시스템출금계좌검증정보송신
     * EI : 세출한도계좌개설정보통합금고송신
     */
    private String[] comCdArray  = {"CA", "CD",  "TB", "TS", "TC", "TM", "EI"};

    public NeoDMWS5050(int i) {
		this.comCdId = i;
	}

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS5050.class.getResource("/conf/log4j.xml"));

        logger.debug("===== [" + NeoDMWS5050.class.getSimpleName() + "] 시작 =====");

        for(int i=0;i< self.length;i++){
        	/* 클래스 인스턴스 생성 */
        	NeoDMWS5050  neoDMWS5050 = new NeoDMWS5050(i);
        	/* 인스턴스를 쓰레드(데몬)로 실행 */
            self[i] = new Thread(neoDMWS5050);
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

        logger.debug("===== [" + NeoDMWS5050.class.getSimpleName() + "] 끝 =====");
    }

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

        fp = new FormatParserAsMap(logger);
        if (fp.doParsingAsMap("msgformat4") < 0) {
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

    @SuppressWarnings("unchecked")
	public int mainLoop()
    {
    	SqlSession session = null;
    	try {
            session = sqlMapper.openSession(false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        sysDvCd = appRes.getString("sysDvCd");
        if( logger.isDebugEnabled()) {
        	logger.debug("시스템구분[" + sysDvCd + "] 기동중입니다.");
        }
        String  folder = appRes.getString("NeoDMWS5050.directory");

        MyMap headerMap  = new MyMap()  ;
        ArrayList<MyMap> dataList = new ArrayList<MyMap>();

        MyMap paramMap   = new MyMap()  ;

        paramMap.setString("govcode",comCdArray[comCdId]);
        String comCodeType =   paramMap.get("govcode").toString();

//        paramMap.setString("요청일자", Utils.getCurrentDate());
        paramMap.setString("DMND_YMD", Utils.getCurrentDate());


        if("CA".equals(comCodeType)) {								//TCM1482 (회계정보) ACCO

        	ArrayList<MyMap> accoList = new ArrayList<MyMap>();

        	try {
        		accoList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTCM1482");

//               if (accoList.size() <= 0) {
               if( accoList.isEmpty()) {
                      logger.info("신규 등록된 회계정보 데이터가 없습니다.");
                      session.close();
                      return -1;
              }

              if (accoList.size() > 0) {
//                  logger.info("총 요청건수(회계정보) : [" + accoList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

                  try {
	                  for (int i = 0; i < accoList.size(); i++) {
	                      ht = (MyMap)accoList.get(i);

                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTCM1482", ht);

	                          logger.info("회계정보 거래코드 [" + ht.getString("TRNX_NO")
	                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

	                          if (dataList.size() > 0) {
	                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
	                              //file 생성
	                              makeFile(infofile, headerMap, dataList, comCodeType);

	                              /* TFE2311 요청상태, 작업상태코드 변경 */
	                              session.update("NeoMapper5050.updateTCM1482", ht);
	                          }

	                  }
                  } catch (Exception e) {
                      logger.error("회계정보 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                      session.close();
                      return -1;
                  }
                  logger.info("모든 데이터(회계정보) 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 데이터(회계정보)가 없습니다.");
              }

              session.commit();
              logger.info("작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("CD".equals(comCodeType)) {							//TCM2061 (부서정보) DEPT


        	ArrayList<MyMap> deptList = new ArrayList<MyMap>();

        	try {
        		deptList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTCM2061");

//               if (deptList.size() <= 0) {
        	   if (deptList.isEmpty()) {
                      logger.info("신규 등록된 부서정보 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

               if (deptList.size() > 0) {
                  logger.info("총 부서정보 요청건수 : [" + deptList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

                  for (int i = 0; i < deptList.size(); i++) {
                      ht = (MyMap)deptList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTCM2061", ht);


                          logger.info("부서정보 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);
//                              ArrayList<MyMap> veriList = null;

                              /* TCM2061 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTCM2061", ht);
                          }
                      } catch (Exception e) {
                          logger.error("부서정보 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 부서정보 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 부서정보 데이터가 없습니다.");
              }

              session.commit();
              logger.info("부서정보 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("부서정보 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("TB".equals(comCodeType)) {						// 분야부문 TCM1222 SECT

        	ArrayList<MyMap> sectList = new ArrayList<MyMap>();

        	try {
        		sectList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTCM1222");

//               if (sectList.size() <= 0) {
        	   if (sectList.isEmpty()) {
                      logger.info("신규 등록된 분야부문 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (sectList.size() > 0) {
//                  logger.info("총 요청건수(분야부문) : [" + sectList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

//                  logger.debug("분야부문 TCM1222 송신 파일을 생성합니다. ");

                  for (int i = 0; i < sectList.size(); i++) {
                      ht = (MyMap)sectList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTCM1222", ht);

//                          logger.info("분야부문 거래코드 [" + ht.getString("TRNX_NO")
//                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);
//                              ArrayList<MyMap> veriList = null;

                              /* TCM1222 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTCM1222", ht);
                          }
                      } catch (Exception e) {
                          logger.error("분야부문 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 분야부문 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 분야부문 데이터가 없습니다.");
              }

              session.commit();
              logger.info("분야부문 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("분야부문 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("TS".equals(comCodeType)) {						//세출통계목 TCM1172 AEST 503 DGCITY07

        	ArrayList<MyMap> aestList = new ArrayList<MyMap>();

        	try {
        		aestList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTCM1172");

//               if (aestList.size() <= 0) {
        	   if (aestList.isEmpty()) {
                      logger.info("신규 등록된 세출통계목 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (aestList.size() > 0) {
                  logger.info("총 요청건수(세출통계목) : [" + aestList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

//                  logger.debug("세출통계목 송신 파일을 생성합니다. ");
                  try {
	                  for (int i = 0; i < aestList.size(); i++) {
	                      ht = (MyMap)aestList.get(i);

	                      dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTCM1172", ht);

                          logger.info("세출통계목 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);
//                              ArrayList<MyMap> veriList = null;

                              /* TCM1172 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTCM1172", ht);
                          }

	                  }
                  } catch (Exception e) {
                      logger.error("세출통계목 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                      session.close();
                      return -1;
                  }
                  logger.info("세출통계목 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 세출통계목 데이터가 없습니다.");
              }

              session.commit();
              logger.info("세출통계목 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("세출통계목 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("TC".equals(comCodeType)) {						//세입목코드 TFM1051 TXCD

        	ArrayList<MyMap> txcdList = new ArrayList<MyMap>();

        	try {
        		txcdList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTFM1051");

//               if (txcdList.size() <= 0) {
               if( txcdList.isEmpty()) {
                      logger.info("세입목코드 신규 등록된 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (txcdList.size() > 0) {
                  logger.info("총 요청건수(세입목코드) : [" + txcdList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

                  for (int i = 0; i < txcdList.size(); i++) {
                      ht = (MyMap)txcdList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTFM1051", ht);

                          logger.info("세입목코드 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);
//                              ArrayList<MyMap> veriList = null;

                              /* TFM1051 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTFM1051", ht);
                          }
                      } catch (Exception e) {
                          logger.error("세입목코드 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 세입목코드 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 세입목코드 데이터가 없습니다.");
              }

              session.commit();
              logger.info("세입목코드 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("세입목코드오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("TM".equals(comCodeType)) {						//세입목맵핑 TFM4020 TXMP TM

        	ArrayList<MyMap>  txmpList = new ArrayList<MyMap>();

        	try {
        		txmpList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTFM4020");

//               if (txmpList.size() <= 0) {
        	   if (txmpList.isEmpty()) {
                      logger.info("신규 등록된 세입목맵핑 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (txmpList.size() > 0) {
                  logger.info("총 요청건수(세입목맵핑) : [" + txmpList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

//                  logger.debug("세입목매핑 송신 파일을 생성합니다. ");

                  for (int i = 0; i < txmpList.size(); i++) {
                      ht = (MyMap)txmpList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTFM4020", ht);

                          logger.info("세입목맵핑 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);
//                              ArrayList<MyMap> veriList = null;

                              /* TCM2061 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTFM4020", ht);
                          }
                      } catch (Exception e) {
                          logger.error("세입목맵핑 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 세입목맵핑 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 세입목맵핑 데이터가 없습니다.");
              }

              session.commit();
              logger.info("세입목맵핑 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("세입목맵핑 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("WA".equals(comCodeType)) {					// 금고은행시스템출금계좌검증정보송신 TFM6210 WDAC

        	ArrayList<MyMap> wdacList = new ArrayList<MyMap>();

        	try {
        		wdacList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTFM6210");

//               if (wdacList.size() <= 0) {
        	   if( wdacList.isEmpty()) {
                      logger.info("신규 등록된 금고은행시스템출금계좌검증정보 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (wdacList.size() > 0) {
                  logger.info("총 요청건수(금고은행시스템출금계좌검증정보): [" + wdacList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

//                  logger.debug("금고은행시스템출금계좌검증정보 송신 파일을 생성합니다. ");

                  for (int i = 0; i < wdacList.size(); i++) {
                      ht = (MyMap)wdacList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTFM6210", ht);

                          logger.info("금고은행시스템출금계좌검증정보 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);

                              /* TCM2061 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTFM6210", ht);
                          }
                      } catch (Exception e) {
                          logger.error("금고은행시스템출금계좌검증정보 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 금고은행시스템출금계좌검증정보 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 금고은행시스템출금계좌검증정보 데이터가 없습니다.");
              }

              session.commit();
              logger.info("금고은행시스템출금계좌검증정보 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("금고은행시스템출금계좌검증정보 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }else if("EI".equals(comCodeType)) { // 세출한도계좌개설정보통합금고송신 TFC0011 LIMIT

        	ArrayList<MyMap> eiList = new ArrayList<MyMap>();

        	try {
        		eiList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.getVerifyTFC0011");

//               if (eiList.size() <= 0) {
            	   if (eiList.isEmpty()) {

                      logger.info("신규 등록된 세출한도계좌개설정보통합금고송신정보가 데이터가 없습니다.");
                      session.close();
                      return -1;
                  }

              if (eiList.size() > 0) {
                  logger.info("총 요청건수(세출한도계좌개설정보통합금고송신정보): [" + eiList.size() + "]건");

                  MyMap ht = new MyMap();

                  String    makeDir = folder + "/dees_com/send";
                  File dir   = new File(makeDir);
                  if (!dir.exists()) {
                      logger.error("파일을 생성할 디렉토리[" + makeDir + "]가 존재하지 않습니다");
                      session.close();
                      return -1;
                  }

                  for (int i = 0; i < eiList.size(); i++) {
                      ht = (MyMap)eiList.get(i);

                      try {
                          dataList = (ArrayList<MyMap>)session.selectList("NeoMapper5050.selectTFC0011", ht);

                          logger.info("세출한도계좌개설정보통합금고송신 거래코드 [" + ht.getString("TRNX_NO")
                        + "]에 검증데이터가 [" + dataList.size() + "]건이 있습니다.");

                          if (dataList.size() > 0) {
                              File infofile = new File(makeDir + "/" + ht.getString("TRNX_NO"));							/* 거래번호 */
                              //file 생성
                              makeFile(infofile, headerMap, dataList, comCodeType);

                              /* TCM2061 요청상태, 작업상태코드 변경 */
                              session.update("NeoMapper5050.updateTFC0011", ht);
                          }
                      } catch (Exception e) {
                          logger.error("금고은행시스템출금계좌검증정보 작업 중 에러, 내용[" + e.getLocalizedMessage() + "]");
                          session.close();
                          return -1;
                      }
                  }
                  logger.info("모든 금고은행시스템출금계좌검증정보 데이터 파일 생성 완료");
              } else{
                  logger.info("신규 등록된 금고은행시스템출금계좌검증정보 데이터가 없습니다.");
              }

              session.commit();
              logger.info("금고은행시스템출금계좌검증정보 작업 완료!!!");
              session.close();
              return -1;
	      } catch(Exception e) {
	          logger.error("금고은행시스템출금계좌검증정보 오류[" + e + "]");
	          e.printStackTrace();
	          session.close();
	          return -1;
	      }
        }
//        session.close();
        return 0;
    }

    private void makeFile(File paramFile, MyMap headerForm, ArrayList<MyMap> dataList, String codeType) throws Exception
    {
        if (dataList.size() > 0 ) {

        	if("CA".equals(codeType)) {					//TCM1482 (회계정보) ACCO 500 	DGCITY04
        		FileOutputStream    fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"14" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */


                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "500");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);

                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"14");             			// 파일업무구분    회계정보( DGCITY04 )
                    DataForm.setString("GB_CD", "D");                        			// 구분코드            ex) D
                    DataForm.setString("LAF_CD", ht.getString("LAF_CD"));         		// 자치단체코드  ex) 6440000'
                    DataForm.setString("LAF_NM", ht.getString("LAF_NM"));     			// 지방자치단체명 ex) '충남
                    DataForm.setString("FYR", ht.getString("FYR"));     				// 회계연도
                    DataForm.setString("ACNT_DV_MSTR_CD", ht.getString("ACNT_DV_MSTR_CD" ));        	// 회계구분마스터코드
                    DataForm.setString("ACNT_DV_MSTR_NM", ht.getString("ACNT_DV_MSTR_NM"));    			// 회계구분마스터명
                    DataForm.setString("ACNT_DV_CD", ht.getString("ACNT_DV_CD"));    			// 회계구분코드
                    DataForm.setString("ACNT_DV_NM", ht.getString("ACNT_DV_NM"));    			// 회계구분명
                    DataForm.setString("USE_YN", ht.getString("USE_YN"));    		// 사용유무


                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    // length = fp.assembleMessageByteBodyAsMap(bMsg, 1024, DataForm, "500", "DGCITY04");
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "500", "KLIDDG14");
                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();
                logger.debug("회계정보 파일 생성 완료");
                dataList.clear();
        	}else if("CD".equals(codeType)) {							// 부서 TCM2061 DEPT 501 DGCITY05

        		logger.debug("부서정보 파일생성 ");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"13" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "501");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);

                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"13");             				// 파일업무구분    부서정보( DGCITY05 )
                    DataForm.setString("GB_CD", "D");                        				// 구분코드            ex) D
                    DataForm.setString("LAF_CD", ht.getString("LAF_CD"));        			// 자치단체코드  ex) 6440000'
                    DataForm.setString("LAF_NM", ht.getString("LAF_NM"));     				// 지방자치단체명 ex) '충남
                    DataForm.setString("GCC_DEPT_CD", ht.getString("GCC_DEPT_CD"));     	// GCC부서코드      ex) ''6440949'
                    DataForm.setString("DEPT_CD", ht.getString("DEPT_CD" ));         		// 부서코드  ex)1004002
                    DataForm.setString("DEPT_NM", ht.getString("DEPT_NM"));    				// 부서명      ex)의회사무국
                    DataForm.setString("GOF_CD", ht.getString("GOF_CD"));    				// 관서코드      ex)0020
                    DataForm.setString("GOF_NM", ht.getString("GOF_NM"));    				// 관서명      ex)의회사무과
                    DataForm.setString("SLNGK_CD", ht.getString("SLNGK_CD"));    			// 실국코드     ex)2005
                    DataForm.setString("SLNGK_NM", ht.getString("SLNGK_NM"));    			// 실국명      ex)의회사무과

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "501", "KLIDDG13");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("부서정보 파일 생성 완료");
                dataList.clear();

            }else if("TB".equals(codeType)) {						// 분야부문 TCM1222 SECT DGCITY06 502
            	logger.debug("분야부문 파일생성 시작");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"29" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "502");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);

                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"29");             			// 파일업무구분    분야부문( DGCITY06 )
                    DataForm.setString("GB_CD", "D");                        			// 구분코드            ex) D
                    DataForm.setString("LAF_CD", ht.getString("LAF_CD"));         		// 자치단체코드  ex) 6440000'
                    DataForm.setString("FYR", ht.getString("FYR"));     				// 회계연도 ex) 2019
                    DataForm.setString("FLD_CD", ht.getString("FLD_CD"));     			// 분야코드      ex) 010
                    DataForm.setString("FLD_NM", ht.getString("FLD_NM" ));         		// 분야명  ex)일반공공행정
                    DataForm.setString("SECT_CD", ht.getString("SECT_CD"));   			// 부문코드 ex)'013'
                    DataForm.setString("SECT_NM", ht.getString("SECT_NM"));   			// 부문명      ex)'지방행정.재정지원'

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "502", "KLIDDG29");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("분야부문 파일 생성 완료");
                dataList.clear();
            }else if("TS".equals(codeType)) {						// 세출통계목 TCM1172S AEST 503 DGCITY07
            	logger.debug("세출통계목 파일생성 시작");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"30" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "503");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);

                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"30");             						// 파일업무구분    분야부문( DGCITY07 )
                    DataForm.setString("GB_CD", "D");                        						// 구분코드            ex) D
                    DataForm.setString("LAF_CD", ht.getString("LAF_CD"));         					// 자치단체코드  ex) 6440000'
                    DataForm.setString("FYR", ht.getString("FYR"));     							// 회계연도 ex) 2019
                    DataForm.setString("ANE_CPLBD_CD", ht.getString("ANE_CPLBD_CD"));     			// 세출편성목코드      ex) 101
                    DataForm.setString("ANE_CPLBD_NM", ht.getString("ANE_CPLBD_NM" ));         		// 세출편성목명  ex)보수
                    DataForm.setString("ANE_STMK_CD", ht.getString("ANE_STMK_CD"));   				// 세출통계목코드 ex)10103
                    DataForm.setString("ANE_STMK_NM", ht.getString("ANE_STMK_NM"));   				// 세출통계목명      ex)정액급식비

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "503", "KLIDDG30");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("세출통계목 파일 생성 완료");
                dataList.clear();
            }else if("TC".equals(codeType)) {						// 세입목코드 TFM1051S TXCD 504 DGCITY08
            	logger.debug("세입목코드 파일 생성 ");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"15" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */


                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "504");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);


                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"15");             					// 파일업무구분    세입목코드( DGCITY08 )
                    DataForm.setString("GB_CD", "D");                        					// 구분코드            ex) D
                    DataForm.setString("FYR", ht.getString("FYR"));     						// 회계연도 ex) 2019
                    DataForm.setString("RVJG_CD", ht.getString("RVJG_CD"));     				// 세입장코드
                    DataForm.setString("RVJG_NM", ht.getString("RVJG_NM" ));         			// 세입장명
                    DataForm.setString("TXRV_GYAN_CD", ht.getString("TXRV_GYAN_CD"));   		// 세입관코드
                    DataForm.setString("TXRV_GYAN_NM", ht.getString("TXRV_GYAN_NM"));   		// 세입관명
                    DataForm.setString("TXRV_HANG_CD", ht.getString("TXRV_HANG_CD"));   		// 세입항코드
                    DataForm.setString("TXRV_HANG_NM", ht.getString("TXRV_HANG_NM"));   		// 세입항명
                    DataForm.setString("ARMK_CD", ht.getString("ARMK_CD"));   					// 세입목코드
                    DataForm.setString("ARMK_NM", ht.getString("ARMK_NM"));   					// 세입목명
                    DataForm.setString("USE_YN", ht.getString("USE_YN"));   					// 사용여부
                    DataForm.setString("CHG_APLCN_YMD", ht.getString("CHG_APLCN_YMD"));   		// 변경적용일자
                    DataForm.setString("ABL_YMD", ht.getString("ABL_YMD"));   					// 폐지일자
                    DataForm.setString("CHG_YMD", ht.getString("CHG_YMD"));   					// 변경일자

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "504", "KLIDDG15");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("세입목코드 파일 생성 완료");
                dataList.clear();
            }else if("TM".equals(codeType)) {						//세입목맵핑 TFM4020S TXMP TM DGCITY09
            	logger.debug("세입목맵핑 파일생성 ");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"16" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "505");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);

                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"16");             					// 파일업무구분    세입목매핑( DGCITY09 )
                    DataForm.setString("GB_CD", "D");                        					// 구분코드            ex) D
                    DataForm.setString("FYR", ht.getString("FYR"));     						// 회계연도 ex) 2019
                    DataForm.setString("RVJG_CD", ht.getString("RVJG_CD"));     				// 세입장코드
                    DataForm.setString("RVJG_NM", ht.getString("RVJG_NM" ));         			// 세입장명
                    DataForm.setString("TXRV_GYAN_CD", ht.getString("TXRV_GYAN_CD"));   		// 세입관코드
                    DataForm.setString("TXRV_GYAN_NM", ht.getString("TXRV_GYAN_NM"));   		// 세입관명
                    DataForm.setString("TXRV_HANG_CD", ht.getString("TXRV_HANG_CD"));   		// 세입항코드
                    DataForm.setString("TXRV_HANG_NM", ht.getString("TXRV_HANG_NM"));   		// 세입항명
                    DataForm.setString("ARMK_CD", ht.getString("ARMK_CD"));   					// 세입목코드
                    DataForm.setString("ARMK_NM", ht.getString("ARMK_NM"));   					// 세입목명
                    DataForm.setString("RPRS_TXRV_SBJ_CD", ht.getString("RPRS_TXRV_SBJ_CD"));   // 대표세입목코드
                    DataForm.setString("RPRS_TXRV_SBJ_NM", ht.getString("RPRS_TXRV_SBJ_NM"));   // 대표세입과목목명
                    DataForm.setString("USE_YN", ht.getString("USE_YN"));   					// 사용여부
                    DataForm.setString("CHG_APLCN_YMD", ht.getString("CHG_APLCN_YMD"));   		// 변경적용일자
                    DataForm.setString("ABL_YMD", ht.getString("ABL_YMD"));   					// 폐지일자
                    DataForm.setString("CHG_YMD", ht.getString("CHG_YMD"));   					// 변경일자

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "505", "KLIDDG16");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("세입목맵핑 파일 생성 완료");
                dataList.clear();
            }else if("WA".equals(codeType)) {						// 금고은행시스템출금계좌검증정보송신 TFM6210S WDAC 506 DGCITY10
            	logger.debug("출금계좌정보 파일생성 ");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", "KLID"+sysDvCd+"17" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "506");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);


                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", "KLID"+sysDvCd+"17");             					// 파일업무구분    분야부문( DGCITY10 )
                    DataForm.setString("GB_CD", "D");                        					// 구분코드            ex) D
                    DataForm.setString("LAF_CD", ht.getString("LAF_CD"));         				// 자치단체코드  ex) 6440000'
                    DataForm.setString("DRW_ACC_SNUM", ht.getString("DRW_ACC_SNUM"));			// 출금계좌일련번호
                    DataForm.setString("FYR", ht.getString("FYR"));     						// 회계연도 ex) 2019
                    DataForm.setString("EM_EF_FG", ht.getString("EM_EF_FG"));     				// 지출자금구분
                    DataForm.setString("ACNT_DV_CD", ht.getString("ACNT_DV_CD" ));         		// 회계구분코드
                    DataForm.setString("EXPS_DV_CD", ht.getString("EXPS_DV_CD"));   			// 경비구분코드
                    DataForm.setString("ADC_DV_CD", ht.getString("ADC_DV_CD"));   				// 회계별업무담당구분코드
                    DataForm.setString("DEPT_CD", ht.getString("DEPT_CD"));   					// 부서코드
                    DataForm.setString("GOF_CD", ht.getString("GOF_CD"));   					// 관서코드
                    DataForm.setString("BANK_CD", ht.getString("BANK_CD"));   					// 은행코드
                    DataForm.setString("ECRP_ACTNO", ht.getString("ECRP_ACTNO"));   			// 암호화계좌번호
                    DataForm.setString("DRW_ACC_MNG_NO", ht.getString("DRW_ACC_MNG_NO"));   	// 출금계좌관리번호
                    DataForm.setString("USE_YN", ht.getString("USE_YN"));   					// 사용여부
                    DataForm.setString("CHG_YMD", ht.getString("CHG_YMD"));   					// 변경일자
                    DataForm.setString("RAT_YN", ht.getString("RAT_YN"));   					// 재배정여부
                    DataForm.setString("APRV_STAT_CD", ht.getString("APRV_STAT_CD"));   		// 승인상태코드
                    DataForm.setString("APRV_DT", ht.getString("APRV_DT"));   					// 승인일시
                    DataForm.setString("RJCT_RSON_CN", ht.getString("RJCT_RSON_CN"));   		// 반려사유내용
                    DataForm.setString("FRST_RGSTR_DT", ht.getString("FRST_RGSTR_DT"));   		// 최초등록일시
                    DataForm.setString("LAST_MDFCN_DT", ht.getString("LAST_MDFCN_DT"));   		// 최종수정일시

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "506", "KLIDDG17");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("금고은행시스템출금계좌검증정보 파일 생성 완료");
                dataList.clear();
            }else if("EI".equals(codeType)) {						// 세출한도계좌개설정보통합금고송신정보
            	logger.debug("세출한도계좌개설정보통합금고송신정보 파일생성 ");
        		FileOutputStream fos = new FileOutputStream(paramFile);

                //------------------------------------------------------------------------------------
                // HEADER 부 조립
                //------------------------------------------------------------------------------------
                headerForm.setString("fileWorkGb", sysDvCd+"TGMB02" );							/* 파일업무구분 */
                headerForm.setString("GB_CD", "H");											/* 구분코드 */
                headerForm.setString("WORK_DATE", Utils.getCurrentDate());				/* 작업일자 */
                headerForm.setString("LAF_CD", "0000000");									/* 지방자치단체코드 */
                headerForm.setString("FYR", "0000");											/* 회계연도 */
                headerForm.setString("START_DATE", Utils.getCurrentDate());				/* 시작일자 */
                headerForm.setString("END_DATE", Utils.getCurrentDate());				/* 종료일자 */
                headerForm.setLong("REC_CNT", dataList.size()+1);								/* 전송레코드수 */
                headerForm.setLong("TOT_CNT", dataList.size());							/* 총의뢰건수 */

                byte[] bMsg = new byte[2048];
                int length = fp.assembleMessageByteHeadAsMap(bMsg, 2048, headerForm, "507");
                fos.write(bMsg, 0, length);
                fos.write("\n".getBytes(), 0, 1);


                //------------------------------------------------------------------------------------
                // DATA 부 조립
                //------------------------------------------------------------------------------------
                MyMap DataForm    = new MyMap();
                for (int i = 0; i < dataList.size(); i++ ) {
                    MyMap    ht = (MyMap)dataList.get(i);

                    DataForm.setString("fileWorkGb", sysDvCd+"TGMB02");             		    // 파일업무구분    분야부문( DGTGMB02 )
                    DataForm.setString("GB_CD", "D");                        		    // 구분코드            ex) D
                    DataForm.setString("GRAM_TRSM_YMD", ht.getString("GRAM_TRSM_YMD")); // 전송일자
                    DataForm.setString("GRAM_ID", ht.getString("GRAM_ID"));			    // 전문일련번호
                    DataForm.setString("LOC_GOV_CD", ht.getString("LOC_GOV_CD"));     	// 자치단체코드
                    DataForm.setString("FYR", ht.getString("FYR"));     				// 회계연도
                    DataForm.setString("FB_CD", ht.getString("FB_CD" ));         		// 회계구분코드
                    DataForm.setString("EXPS_DV_CD", ht.getString("EXPS_DV_CD"));   	// 경비구분코드
                    DataForm.setString("GOF_CD", ht.getString("GOF_CD"));   			// 관서코드
                    DataForm.setString("DEPT_CD", ht.getString("DEPT_CD"));   			// 부서코드
                    DataForm.setString("BANK_CD", ht.getString("BANK_CD"));   			// 은행코드
                    DataForm.setString("ANE_LIM_ACC_NO", ht.getString("ANE_LIM_ACC_NO"));  // 세출한도계좌번호
                    DataForm.setString("MTAC_IDT_NO", ht.getString("MTAC_IDT_NO"));   	   // 통합지출계좌번호
                    DataForm.setString("DPOR_NM", ht.getString("DPOR_NM"));   	        // 세출한도계좌예금주명
                    DataForm.setString("BIZ_IN_NO", ht.getString("BIZ_IN_NO"));   		// 사업자등록번호
                    DataForm.setString("ACC_NO_USE_YN", ht.getString("ACC_NO_USE_YN")); // 계좌사용유무
                    DataForm.setString("BIGO", ht.getString("BIGO"));   				// 비고
                    DataForm.setString("REQ_ID", ht.getString("REQ_ID"));   		    // 개설자ID
                    DataForm.setString("REQ_ID_NM", ht.getString("REQ_ID_NM"));   		// 개설자명
                    DataForm.setString("OPEN_DT", ht.getString("OPEN_DT"));   		    // 개설일자
                    DataForm.setString("RGSTR_DT", ht.getString("RGSTR_DT"));   		// 등록일시

                    //----------------------------------------------------------
                    // 파일저장
                    //----------------------------------------------------------
                    length = fp.assembleMessageByteBodyAsMap(bMsg, 2048, DataForm, "507", "KLIDDG18");

                    fos.write(bMsg, 0, length);
                    fos.write("\n".getBytes(), 0, 1);
                    DataForm.clear();
                }

                dataList.clear();
                fos.close();

                logger.debug("금고은행시스템출금계좌검증정보 파일 생성 완료");
                dataList.clear();
            }

        } else {
            logger.debug("파일을 생성하기 위한 데이터가 존재하지 않습니다.");
        }
    }

    public long getSqNo(MyMap mapForm) throws Exception
    {
        long sqNo = 0;

        SqlSession    ss = sqlMapper.openSession(false);

        try {
            try {
                long    nCnt = (Long)ss.selectOne("NeoMapperFile.nCntSelect", mapForm);
                logger.debug("COUNT[" + nCnt + "]");

                if (nCnt > 0) {
                    sqNo = (Long)ss.selectOne("NeoMapperFile.SqNoSelect", mapForm);
                }
            } catch (NullPointerException e) {
                // null 포인터는 무시
                logger.error("SqNoSelect Null Point Error");
            }

            sqNo++;

            mapForm.setLong("FILESQNO", sqNo);
            if (sqNo == 1) {
                ss.insert("NeoMapperFile.SqNoInsert", mapForm);
            } else {
                ss.update("NeoMapperFile.SqNoUpdate", mapForm);
            }

            ss.commit();
        } finally {
            ss.close();
        }
        return sqNo;
    }
}
