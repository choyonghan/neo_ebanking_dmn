/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : 보고서 정보 세출원장 저장
 *  기  능  명 : 보고서 정보 세출원장 저장
 *  클래스  ID : DMWS6050
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * ------------------------------------------------------------------------
 *  김민섭       다산(주)      2020.09.22    %01%         최초작성
 *  하상우       다산(주)      2022.08.29    %01%         수정작성(보고서통합)
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import  org.apache.log4j.Logger;
import  org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;


/**
 *
 * 흐름 : [은행->세출->재정]
 * 은행에서 오는 보고서파일을 받아서(RecvFromBank.java)
 * 재정(호조)의 R 테이블에 반영한다.
 *
 */
public class NeoDMWS6050 implements Runnable {

    static Logger logger = Logger.getLogger(NeoDMWS6050.class);
    private static Thread self = null;

    private static SqlSession session = null;
    private static SqlSessionFactory sqlMapper = null;

    private static FormatParserAsMap fp = null;
    static MyMap appRes = new MyMap();

	private final String FB_FM_XXX_RD_00001 = "419"; // FB_FM_XXX_RD_00001	수입일계표(I) 수신 / KLIDDG19
	private final String FB_FM_XXX_RD_00002 = "420"; // FB_FM_XXX_RD_00002	수입일계표(II) 수신 / KLIDDG20
	private final String FB_FM_XXX_RD_00003 = "421"; // FB_FM_XXX_RD_00003	세입일계표 수신 / KLIDDG21
	private final String FB_FM_XXX_RD_00005 = "422"; // FB_FM_XXX_RD_00005	세출일계표 수신 / KLIDDG22
	private final String FB_FM_XXX_RD_00004 = "423"; // FB_FM_XXX_RD_00004	세입세출일계표 수신 / KLIDDG23
	private final String FB_FM_XXX_RD_00006 = "424"; // FB_FM_XXX_RD_00006	세입세출외현금일계표 수신 / KLIDDG24
	private final String FB_FM_XXX_RD_00009 = "427"; // FB_FM_XXX_RD_00009   자금마감재무건전성평가수신연계( TODO : 경북해서만 처리됨 ) / KLIDDG27
	private final String FB_FE_XXX_RD_08028 = "428"; // FB_FE_XXX_RD_08028  금고은행시스템일상경비월별잔액수신 / KLIDDG28

	private final String FR_FM_XXX_RD_00025 = "429"; // FR_FM_XXX_RD_00025   지역개발기금수입지출일계표수신연계 / 미정
	private final String FR_FM_XXX_RD_00026 = "430"; // FR_FM_XXX_RD_00026   지역개발공채발행내역수신연계 / 미정
	private final String FR_FM_XXX_RD_00027 = "431"; // FR_FM_XXX_RD_00027  융자금회수이자내역수신연계 / 미정


	public static void main(String...strings) {

		DOMConfigurator.configure(NeoDMWS6050.class.getResource("/conf/log4j.xml"));

		logger.debug("===== [" + NeoDMWS6050.class.getSimpleName() + "] 시작 =====");

		NeoDMWS6050  hello = new NeoDMWS6050();
		self = new Thread(hello);
		self.setDaemon(true);
		self.start();

		try {
			self.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.error("===== [" + NeoDMWS6050.class.getSimpleName() + "] 작업 종료 =====");
	}

	@Override
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
		logger.debug("SQLMAPPER생성 : " + sqlMapper);
		if( logger.isDebugEnabled()) {
			for( String str : sqlMapper.getConfiguration().getMappedStatementNames()) {
				logger.debug("mapperName : " + str);
			}
		}

		fp = new FormatParserAsMap(logger);
		if (fp.doParsingAsMap("NeoDMWS6050_msgformat") < 0) {
			logger.error("전문포맷 분석오류");
			return;
		}

		Utils.getResources("conf/ApplicationResources", appRes);

		while (!Thread.currentThread().isInterrupted()) {
			//mainLoop();
			mainLoop_re();
			try {
				Thread.sleep(30000);

			} catch (InterruptedException e) {
				logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
				break;
			}
		}
	}

	private int mainLoop_re() {

		final String FILE_EXT = ".OK"; // 전송완료파일확장자

		// /dees_svr/FTP_FILE
		final String FOLDER = appRes.getString("RecvFromBank.recvDirectory");
		final String RECV_DIR = FOLDER.concat("/dees_report/recv");
		final String WORK_DIR = FOLDER.concat("/dees_report/work");
		final String BACK_DIR = FOLDER.concat("/dees_report/back");
		final String ERROR_DIR = FOLDER.concat("/dees_report/error");

		// ------------------------------------------------------------------ 수신 파일 확인
	    File recv_Dir = new File( RECV_DIR );
	    String[] recv_file_list = recv_Dir.list(new FilenameFilter() {

	    	@Override
	    	public boolean accept(File dir, String name) {
	            return name.endsWith( FILE_EXT );
	        }
	    });

	    if( recv_file_list == null || recv_file_list.length == 0 ) {
			logger.info("수신 된 파일이 없습니다.[" + RECV_DIR + "]");
	    	return 0;
	    }

	    int recv_file_cnt = recv_file_list.length;
	    logger.info( ">>>>>>>>>수신데이타파일 건수 : " + recv_file_cnt + "건" );


		// ------------------------------------------------------------------ 데이터베이스 세션 생성, 자동확약 끔.
		try {
			session = sqlMapper.openSession(false);

		} catch (Exception ex) {
			logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
			return -1;
		}

	    for( String recvFile : recv_file_list ) {

	    	 long beforeTime = System.currentTimeMillis();
	    	logger.info( "파일명 [" + recvFile + "]" );

	    	String[] file_nm = recvFile.split( "\\." );

	    	File ok_file    = new File( RECV_DIR.concat(File.separator).concat(recvFile));  // 수신디렉토리 - OK 확인파일
	    	File recv_file  = new File( RECV_DIR.concat(File.separator).concat(file_nm[0]));  // 수신디렉토리 - 수신파일
	    	File work_file  = new File( WORK_DIR.concat(File.separator).concat(file_nm[0]));  // 작업디렉토리 - 작업파일
	    	File back_file  = new File( BACK_DIR.concat(File.separator).concat(file_nm[0]));  // 완료디렉토리 - 처리완료파일
	    	File error_file  = new File( ERROR_DIR.concat(File.separator).concat(file_nm[0]).concat(".").concat(Utils.getCurrentTime()));  // 완료디렉토리 - 처리에러파일

	    	Path okPath = ok_file.toPath();
	    	Path recvPath = recv_file.toPath();
	    	Path workPath = work_file.toPath();
	    	Path backPath = back_file.toPath();
	    	Path errorPath = error_file.toPath();

    		try {
				Files.copy( recvPath, workPath, StandardCopyOption.REPLACE_EXISTING);
		    	Files.deleteIfExists( recvPath );
			} catch (IOException e) {
				logger.error("WORK파일 복사 중 에러가 발생하였습니다.[" + e.getLocalizedMessage() + "]");
				continue;
			}

	    	// --------------------------------------------------------------- work file read
	    	logger.info( "work_file ==  " + work_file + "]" );

        	int iRequestNum = 1; // 파일별 번호
        	int iTrscSeq = 1;    // 파일 라인별 번호

			try {

				String keyValue = ""; // 잔액증명 APPEND를 위한 변수

				for( String readLine : Files.readAllLines(workPath, Charset.forName("EUC-KR"))) {

					// 라인별 마이맵 만들기
					MyMap dataMap = getData( readLine );
//					if( dataMap == null || dataMap.isEmpty()) {
//						logger.error("데이터 파싱 오류");
//						break; // readline while break;
//					}
					if( logger.isDebugEnabled()) {
						logger.debug( "dataMap: " + dataMap );
					}

					String gbCd = dataMap.getString("GB_CD"); 	 		 // H or D(1)

					if( "H".equalsIgnoreCase( gbCd)) {

						String fileWorkGb = dataMap.getString("fileWorkGb"); // KLIDDG03(8)
						keyValue = getKeyValue( fileWorkGb );

						iRequestNum = selectRequestNum( dataMap );
						if( logger.isDebugEnabled()) {
							logger.debug( "iRequestNum: " + iRequestNum );
						}

					}else if( "D".equalsIgnoreCase( gbCd)) {

				        // 업무 insert
				        int iCnt = insertBiz( iRequestNum , dataMap );
				        if( iCnt == -1 ) {

				    		Files.copy( workPath, errorPath, StandardCopyOption.REPLACE_EXISTING);
					    	Files.deleteIfExists( workPath );

				            session.rollback();
				            return -1;
				        }

					} // end if gbCd

					// -------------------------------------------------------------- 트랜로그 처리
					insertTranLog( iRequestNum, iTrscSeq, dataMap, readLine );
					iTrscSeq = iTrscSeq + 1;

				} // end for readLine


				// 파일 처리 종료 후
				// 일상경비월별잔액수신 연계일 경우, 세출한도계좌 정보를 R 에 등록한다.
	            if( FB_FE_XXX_RD_08028.equals( keyValue)) {

	            	// FB_FE_XXX_RD_08028  금고은행시스템일상경비월별잔액수신
	                // TFE2551R(일상경비월별잔액수신연계)

	            	// 1. 지출, 가상계좌 이체결과처리 데몬 에서 세출한도계좌정보를 TFMA050A에 데이터를 넣어준다. 전송상태 : N
	            	// 2. 금고은행에서 계좌거래내역 수신이 오면, 세출한도계좌정보와 함께 재정으로 보낸다. 전송상태 : Y
	            	// 3. 금고은행에서 보고서(월별잔액수신)이 오면, 전송상태 Y 건의 세출한도계좌 잔액정보를 재정으로 보낸다.

                	@SuppressWarnings("unchecked")
					List<MyMap> aneList = session.selectList("NeoMapper6050.selectTfma050aByRamt");
                	int aneSize = aneList.size();
			    	logger.info( ">>>>>>>>>TFMA050A 처리 건수 : " + aneSize + "건" );

                	logger.info("#### 일상경비 월별잔액 insertTfe2551R&updateTfma050aTrnmtYn_start");

                	for( MyMap aneMap : aneList ) {

                		//aneMap.setMap("LINK_TRSC_ID_SEQ", iRequestNum);

                		session.insert("NeoMapper6050.insertTfe2551R", aneMap);
			    		session.update("NeoMapper6050.updateTfma050aTrnmtYn", aneMap);

                	}

                	logger.info("#### 일상경비 월별잔액 insertTfe2551R&updateTfma050aTrnmtYn_end");

	            }

		    	Files.copy( workPath, backPath, StandardCopyOption.REPLACE_EXISTING);
		    	Files.deleteIfExists( workPath );
		    	session.commit();

		    	long afterTime  = System.currentTimeMillis();
	            long secDiffTime = (afterTime - beforeTime)/1000;

	            logger.info( "[" + keyValue + "] 작업 완료  : [ " + secDiffTime + " 초 ]" );

			} catch (Exception e) {

		    	try {

					logger.error("파일처리 중 에러가 발생하였습니다.[" + e.getLocalizedMessage() + "]");
					Files.copy( workPath, errorPath, StandardCopyOption.REPLACE_EXISTING);
			    	Files.deleteIfExists( workPath );
		            continue;   // 다음 파일

				} catch (IOException e1) {
					logger.error("ERROR파일 복사 및 WORK파일 삭제 중 에러가 발생하였습니다. 내용[" + e.getLocalizedMessage() + "]");
				}finally {
					session.rollback();
				}

				continue;

			}finally {

		    	try {
					Files.deleteIfExists( okPath );
				} catch (IOException e) {
					logger.error("OK파일삭제 중 에러가 발생하였습니다. 내용[" + e.getLocalizedMessage() + "]");
				}
			}


	    } // end for recv_file_list

        session.close();
		return 0;


	}

	@Deprecated
	private int mainLoop() {

		//System.out.println("----------------------------------- sqlMapClient_cyber : " + sqlMapClient_cyber);

		final String FILE_EXT = ".OK"; // 전송완료파일확장자

		// /dees_svr/FTP_FILE
		final String FOLDER = appRes.getString("RecvFromBank.recvDirectory");
		final String RECV_DIR = FOLDER.concat("/dees_report/recv");
		final String WORK_DIR = FOLDER.concat("/dees_report/work");
		final String BACK_DIR = FOLDER.concat("/dees_report/back");
		final String ERROR_DIR = FOLDER.concat("/dees_report/error");


		// ------------------------------------------------------------------ 수신 파일 확인
	    File recv_Dir = new File( RECV_DIR );
	    String[] recv_file_list = recv_Dir.list(new FilenameFilter() {

	    	@Override
	    	public boolean accept(File dir, String name) {
	            return name.endsWith( FILE_EXT );
	        }
	    });

	    if( recv_file_list == null || recv_file_list.length == 0 ) {
			logger.debug("수신 된 파일이 없습니다.[" + RECV_DIR + "]");
	    	return 0;
	    }

	    int recv_file_cnt = recv_file_list.length;
	    logger.info( ">>>>>>>>>수신데이타파일 건수 : " + recv_file_cnt + "건" );


		// ------------------------------------------------------------------ 데이터베이스 세션 생성, 자동확약 끔.
		try {
			session = sqlMapper.openSession(false);

		} catch (Exception ex) {
			logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
			return -1;
		}

	    for( String recvFile : recv_file_list ) {

	    	logger.info( "파일명 [" + recvFile + "]" );

	    	String[] file_nm = recvFile.split( "\\." );

	    	File ok_file    = new File( RECV_DIR.concat(File.separator).concat(recvFile));  // 수신디렉토리 - OK 확인파일
	    	File recv_file  = new File( RECV_DIR.concat(File.separator).concat(file_nm[0]));  // 수신디렉토리 - 수신파일
	    	File work_file  = new File( WORK_DIR.concat(File.separator).concat(file_nm[0]));  // 작업디렉토리 - 작업파일
	    	File back_file  = new File( BACK_DIR.concat(File.separator).concat(file_nm[0]));  // 완료디렉토리 - 처리완료파일
	    	File error_file  = new File( ERROR_DIR.concat(File.separator).concat(file_nm[0]).concat(".").concat(Utils.getCurrentTime()));  // 완료디렉토리 - 처리에러파일

	    	Utils.fileMove( recv_file, work_file, WORK_DIR );
	    	//recv_file.renameTo( work_file );
	    	//ok_file.delete();

	    	logger.info( "work_file ==  " + work_file + "]" );

	    	// --------------------------------------------------------------- file read

	        FileInputStream fis = null;
	        InputStreamReader isr = null;
	        BufferedReader br = null;

	        try {

	        	fis = new FileInputStream(work_file);
	        	isr = new InputStreamReader(fis, "euckr");
	        	br = new BufferedReader(isr);

		        String readLine = null;

	        	int iRequestNum = 1; // 파일별 번호
	        	int iTrscSeq = 1;    // 파일 라인별 번호

	        	while( (readLine = br.readLine()) != null ) {

	        		// 라인별 마이맵 만들기
	        		MyMap dataMap = getData( readLine );
	        		if( dataMap == null || dataMap.isEmpty()) {
	        			logger.error("데이터 파싱 오류");
	        			break; // readline while break;
	        		}

	        		logger.debug( "dataMap: " + dataMap );

	        		String gbCd = dataMap.getString("GB_CD"); 	 		 // H or D(1)

		        	if( "H".equalsIgnoreCase( gbCd)) {

		        		iRequestNum = selectRequestNum( dataMap );

		        	}else if( "D".equalsIgnoreCase( gbCd)) {

                        // 업무 insert
                        int iCnt = insertBiz( iRequestNum , dataMap );
                        if( iCnt == -1 ) {

            	        	br.close();
            	        	isr.close();
            	        	fis.close();


            	        	Utils.fileMove( work_file, error_file, ERROR_DIR );
                            //work_file.renameTo( error_file );
                            ok_file.delete();

                            session.rollback();
                            session.close();
                            return -1;
                        }

		        	}

                	// -------------------------------------------------------------- 트랜로그 처리
		        	try {

		        		insertTranLog( iRequestNum, iTrscSeq, dataMap, readLine );
			        	iTrscSeq = iTrscSeq + 1;

		        	}catch( Exception e) {
		        		logger.error( "로그 프린트 중 에러:" + e.getLocalizedMessage());
		        	}

	        	} // end while readline

	        	br.close();
	        	isr.close();
	        	fis.close();

	        	Utils.fileMove( work_file, back_file, BACK_DIR );
	            //work_file.renameTo( back_file );
	            ok_file.delete();
	            session.commit();

	        } catch (FileNotFoundException e) {

                logger.error("파일을 열수 없습니다. 내용[" + e.getLocalizedMessage() + "]");
                Utils.fileMove( work_file, error_file, ERROR_DIR );
		        //work_file.renameTo( error_file );
                ok_file.delete();
	            continue;   // 다음 파일

	        } catch (UnsupportedEncodingException e) {
                logger.error("파일을 열수 없습니다. 내용[" + e.getLocalizedMessage() + "]");
                Utils.fileMove( work_file, error_file, ERROR_DIR );
		        //work_file.renameTo( error_file );
                ok_file.delete();
	            continue;   // 다음 파일

	        } catch (IOException e) {
                logger.error("파일을 열수 없습니다. 내용[" + e.getLocalizedMessage() + "]");
                Utils.fileMove( work_file, error_file, ERROR_DIR );
		        //work_file.renameTo( error_file );
                ok_file.delete();
	            continue;   // 다음 파일
	        } catch (Exception e) {
                logger.error("파일처리 중 에러가 발생하였습니다. 내용[" + e.getLocalizedMessage() + "]");
                Utils.fileMove( work_file, error_file, ERROR_DIR );
		        //work_file.renameTo( error_file );
                ok_file.delete();
	            continue;   // 다음 파일
	        } finally {
	        	try {
		        	if( br != null ) {
		        		br.close();
		        	}
		        	if( isr != null ) {
		        		isr.close();
		        	}
		        	if( fis != null ) {
		        		fis.close();
		        	}
	        	}catch(IOException e) {
	        		logger.error("=====>     파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
	        	}

	        }
	    } // end for recv_file_list
        session.close();

		return 0;
	} // end mainLoop

	/**
	 * 라인 문자열을 MyMap 으로 만든다.
	 *
	 * @param readLine - 파일 라인 문자열
	 * @return MyMap
	 * @throws UnsupportedEncodingException
	 */
	private MyMap getData( String readLine ) throws UnsupportedEncodingException {
		MyMap dataMap = new MyMap();

    	byte[] readBytes = readLine.getBytes("euckr");
    	int bytesLength = readBytes.length;

    	// fp msgformat 찾기위해서 GB->DG 로 변경함.
    	String fileWorkGb = readLine.substring(0,8); // KLIDDG03(8)
    	fileWorkGb = fileWorkGb.replace("GB", "DG");
    	if( logger.isDebugEnabled()) {
    		logger.debug("report fileWorkGb[" + fileWorkGb + "]");
    	}
    	String dataGb = readLine.substring(8,9);	 // H or D(1)

    	String keyValue = getKeyValue( fileWorkGb );

    	if( "H".equals( dataGb)) {
            if (fp.disassembleMessageByteHeadAsMap( readBytes, bytesLength, dataMap, keyValue) < 0) {
                dataMap = null;
            }
    	}else if( "D".equals( dataGb)) {
            if (fp.disassembleFileByteBodyAsMap6050_5( readBytes, bytesLength, dataMap, keyValue, fileWorkGb) < 0) {
                dataMap = null;
            }
    	}
    	if( dataMap == null ) {
    		logger.error("데이터 파싱 오류");
    	}

		return dataMap;
	}

	/**
	 * NeoDMWS6050_msgformat.xml 에 등록된 전문번호 구하기
	 *
	 * @param fileWorkGb - 파일업무구분
	 * @return
	 */
	private String getKeyValue( String fileWorkGb ) {
		return "4" + fileWorkGb.substring( fileWorkGb.length()-2, fileWorkGb.length()); // 파일업무구분 뒤에 2자리
	}

	/**
	 * 파일 헤더 라인 기준으로 REQUEST_NUM 번호를 조회한다. <br/>
	 * SPRT_ORG, TRSC_DATE, 종(SPRT_KIND_CODE)별 REQUEST_NUM 번호 <br/>
	 * 보고서 파일별 번호라고 보면 된다. <br/><br/>
	 *
	 * <참조> <br/>
	 * -- SPRT_ORG, TRSC_DATE, 종별 REQUEST_NUM 번호 <br/>
	 * -- REQUEST_NUM 번호별 TRSC_SEQ 번호 <br/>
	 *
	 * @param dataMap
	 * @return
	 */
	private int selectRequestNum( MyMap dataMap ) {

		String fileWorkGb = dataMap.getString("fileWorkGb"); // KLIDDG03(8)
		String keyValue = getKeyValue( fileWorkGb );

		MyMap param = new MyMap();

		param.setMap("SPRT_ORG", "R");    // 구분값
		param.setMap("TRSC_DATE", Utils.getCurrentDate());    // 전송일자
		param.setMap("SPRT_KIND_CODE", "0"+keyValue);    // 전문종별

		return (int) session.selectOne("NeoMapperCommon.selectRequestNum", param);
	}

	/**
	 * 인터페이스별 R 테이블 등록
	 *
	 * @param iRequestNum - 파일별 번호
	 * @param dataMap - 파일라인 MyMap
	 * @return
	 * @throws Exception
	 */
	private int insertBiz( int iRequestNum, MyMap dataMap ) throws Exception {
		int iCnt = 0;
		try {

			String fileWorkGb = dataMap.getString("fileWorkGb"); // KLIDDG03(8)
			String keyValue = getKeyValue( fileWorkGb );

			String queryId = "";

			dataMap.setMap("LINK_TRSC_ID_SEQ", iRequestNum);

			// 연계기본 컬럼 셋팅
			// dataMap 에 키값이 없으면 쿼리 실행시, 오류 발생함.
			dataMap.setMap("BTCH_PRCS_STAT_CD", "");
			dataMap.setMap("BTCH_PRCS_DT", "");
			dataMap.setMap("LINK_PRCS_DV_CD", "C");
			dataMap.setMap("LINK_TRSM_STAT_CD", "");
			dataMap.setMap("LINK_ERR_MSG_CN", "");
			dataMap.setMap("PRCT_PRCS_DT", "");
			dataMap.setMap("LINK_STAT_PRCS_DT", "");

            if( FB_FM_XXX_RD_00001.equals( keyValue)) {
                // 401 , 402
                // TEF_INCOMESUM1
                // FB_FM_XXX_RD_00001	수입일계표(I) 수신
                // TFMA060R(자금마감수입1일집계수신연계)
            	queryId = "NeoMapper6050.insertTfma060R";

            } else if( FB_FM_XXX_RD_00002.equals( keyValue)) {
                // 403 , 404
                // TEF_INCOMESUM2
                // FB_FM_XXX_RD_00002	수입일계표(II) 수신
                // TFMA070R(자금마감수입2일집계수신연계)
            	queryId = "NeoMapper6050.insertTfma070R";

            } else if( FB_FM_XXX_RD_00003.equals( keyValue)) {
                // 405 , 406
                // TEF_REVDYMNSUM
                // FB_FM_XXX_RD_00003	세입일계표 수신
                // TFMA080R(자금마감세입집계수신연계)
            	queryId = "NeoMapper6050.insertTfma080R";

            } else if( FB_FM_XXX_RD_00005.equals( keyValue)) {
                // 407 , 408
                // TEF_REVTEDYMNSUM
                // FB_FM_XXX_RD_00005	세출일계표 수신
                // TFMA100R(자금마감세출집계수신연계)
            	queryId = "NeoMapper6050.insertTfma100R";

            } else if( FB_FM_XXX_RD_00004.equals( keyValue)) {
                // 409 , 410
                // TEF_TEDYMNSUM
                // FB_FM_XXX_RD_00004	세입세출일계표 수신
                // TFMA090R(자금마감세입세출집계수신연계)
            	queryId = "NeoMapper6050.insertTfma090R";

            } else if( FB_FM_XXX_RD_00006.equals( keyValue)) {
                // 411 , 412
                // TEF_REVTEETCCASHSUM
                // FB_FM_XXX_RD_00006	세입세출외현금일계표 수신
                // TFMA110R(자금마감세입세출외현금집계수신연계)
            	queryId = "NeoMapper6050.insertTfma110R";

            } else if( FR_FM_XXX_RD_00025.equals( keyValue)) { // 신규
                // FR_FM_XXX_RD_00025
                // TFM3130R(지역개발기금수입지출일계표수신연계)
            	queryId = "NeoMapper6050.insertTfm3130R";

            } else if( FR_FM_XXX_RD_00026.equals( keyValue)) {
                // FR_FM_XXX_RD_00026
                // TFM3160R(지역개발공채발행내역수신연계)
            	queryId = "NeoMapper6050.insertTfm3160R";

            } else if( FR_FM_XXX_RD_00027.equals( keyValue)) {
                // FR_FM_XXX_RD_00027
                // TFM3170R(융자금회수이자내역수신연계)
            	queryId = "NeoMapper6050.insertTfm3170R";

            } else if( FB_FE_XXX_RD_08028.equals( keyValue)) {
            	// FB_FE_XXX_RD_08028  금고은행시스템일상경비월별잔액수신
                // TFE2551R(일상경비월별잔액수신연계)
            	queryId = "NeoMapper6050.insertTfe2551R";

            	// TODO : 한도계좌 잔액


            } else if( FB_FM_XXX_RD_00009.equals( keyValue)) {
            	// 경북에서만 처리됨.
                // 신규
            	// 분기 (3,6,9,12월말일 익일 1시)
                // FB_FM_XXX_RD_00009 자금마감재무건전성평가수신연계
                // TFMA130R(자금마감재무건전성평가수신연계)
            	queryId = "NeoMapper6050.insertTfma130R";

            }

			if( logger.isDebugEnabled()) {
				logger.debug( "QUERYID[" + queryId + "]" );
				logger.debug( "REPORT NUMBER[" + iRequestNum + "]" );
				logger.debug( "PARAM[" + dataMap + "]");
			}

            if( !"".equals( queryId )) {

                logger.debug( "6050 insert queryId[" + queryId + "] 실행" );
                session.insert( queryId, dataMap);
                logger.debug( "6050 insert queryId[" + queryId + "] 실행완료" );

            }else {
            	logger.debug( "6050 실행할 쿼리ID가 없습니다." );
            }

		}catch(Exception e) {
            logger.error("원인[" + e.getLocalizedMessage() + "]");

			iCnt = -1;
		}
		return iCnt;
	}

	/**
	 * 트랜로그(TRAN_RECP_TAB) 등록 <br/>
	 *
	 * @param iRequestNum - 파일별 번호
	 * @param iTrscSeq - 파일 라인별 번호
	 * @param dataMap - 라인 문자열 MyMap
	 * @param readLine - 파일 라인 문자열
	 * @throws Exception
	 */
	private void insertTranLog( int iRequestNum, int iTrscSeq, MyMap dataMap, String readLine ) throws Exception  {

 		String currentDate = Utils.getCurrentDate(); // YYYYMMDD
 		String currentTime = Utils.getCurrentTime(); // HHmmss

    	String requestNum = Utils.getCurrentDate() + Utils.extendData(4, '9', iRequestNum+"");
    	String trscSeq = Utils.extendData(10, '9', iTrscSeq+"");

		String fileWorkGb = dataMap.getString("fileWorkGb"); // KLIDDG03(8)
		String trscFlog = dataMap.getString("GB_CD"); 	 		 // H or D(1)
		String lafCd = dataMap.getString("LAF_CD") == null ? "" : dataMap.getString("LAF_CD"); 	 	 // 지자치단계코드(3)

		String keyValue = getKeyValue( fileWorkGb );

    	MyMap param = new MyMap();

    	param.setMap("SPRT_ORG", "R");    			  // 구분값
    	param.setMap("TRSC_DATE", currentDate); 	  // 전송일자
    	param.setMap("TRSC_SEQ",  trscSeq);     	  // 전송일련번호
    	param.setMap("TRSC_TIME", currentTime); 	  // 전송시간
    	param.setMap("TRSC_FLAG",  trscFlog);   	  // 구분코드
    	param.setMap("SPRT_KIND_CODE", "0"+keyValue); // 전문종별
    	param.setMap("SPRT_TRSC_CODE", "KLID" + keyValue.substring( keyValue.length()-2, keyValue.length())); // 파일명
    	param.setMap("INPUT_TLGM",  readLine);    	  // 입력전문
    	param.setMap("REQUEST_NUM", requestNum);  	  // 입력전문 REQUEST_NUM
    	param.setMap("ELEC_BILL_NUM", lafCd);	  	  // 지방자치단체코드 20221012신규추가

        logger.info("INPUT_TLGM =["+readLine+"]");
        logger.info("LogMf =["+param+"]");

        session.insert( "NeoMapperCommon.insertTranLog", param );

        logger.info("========> insertTRAN_RECP OK !! ");

	}

    /**
     * 입력된 Source 데이터를 intLength 만큼 strMode 형태로 확장한 String 을 return
     *
     * @param intLength 최대크기 int
     * @param charMode 확장모드 char
     * @param Source 원본데이터 String
     * @return String 변경된데이터
    private String extendData(int intLength, char charMode, String Source) throws ArrayIndexOutOfBoundsException {

        if ( Source.length() > intLength )
            throw new ArrayIndexOutOfBoundsException("입력데이터 [" + Source + "]가 길이[" + intLength + "]를 초과하였습니다.");

     	String byteTarget = Source;

     	switch ( charMode )
     	{
     	case '9':
     	    for (int i=0; i<intLength-Source.length(); i++){
     	    	byteTarget = "0"+byteTarget;
     	    }
     	    break;
     	default :
     		for (int i=0; i<intLength-Source.length(); i++){
     			byteTarget = " "+byteTarget;
     		}

     		break;
     	}

     	return byteTarget;
    }
     */

	/**
	 * 원본파일을 대상파일로 이동한다.
	 * 원본파일은 삭제한다.
	 *
	 * @param sourceFile - 원본파일
	 * @param trgetFile - 대상파일
	 * @param trgetPath - 대상파일경로
    private void fileMove( File sourceFile, File trgetFile, String trgetPath ) {
    	File trgetPathDir = new File( trgetPath );
    	if( !trgetPathDir.isDirectory()) {
    		trgetPathDir.mkdirs();
    	}
    	sourceFile.renameTo( trgetFile );
    	sourceFile.delete();
    }
	 */


} // end class
