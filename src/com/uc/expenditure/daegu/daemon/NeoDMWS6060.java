/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : 청백리 정보 세출원장 저장
 *  기  능  명 : 청백리 정보 세출원장 저장
 *  클래스  ID : DMWS6050
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * ------------------------------------------------------------------------
 *  하상우       다산(주)      2022.08.29    %01%         최초작성
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
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;


/**
 *
 * 흐름 : [은행->세출->재정]
 * 은행에서 오는 보고서파일을 받아서(RecvFromBank.java)
 * 재정(호조)의 R 테이블에 반영한다.
 *
 * 청백리 : asis NeoBATCHJOB
 */
public class NeoDMWS6060 implements Runnable {

    static Logger logger = Logger.getLogger(NeoDMWS6060.class);
    private static Thread self = null;

    private static SqlSession session = null;
    private static SqlSessionFactory sqlMapper = null;

    private static FormatParserAsMap fp = null;
    static MyMap appRes = new MyMap();

    private final String FB_FM_XXX_RD_00007 = "426"; // FB_FM_XXX_RD_00007	계좌거래내역 수신 / KLIDDG26
    private final String FB_FM_XXX_RD_00008 = "425"; // FB_FM_XXX_RD_00008	계좌신규 개설 및 약정정보 수신 / KLIDDG25

    public static void main(String args[]) {

    	DOMConfigurator.configure(NeoDMWS6060.class.getResource("/conf/log4j.xml"));

        logger.debug("===== [" + NeoDMWS6060.class.getSimpleName() + "] 시작 =====");

        NeoDMWS6060  hello = new NeoDMWS6060();
        self = new Thread(hello);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
			e.printStackTrace();
        }

        logger.debug("===== [" + NeoDMWS6060.class.getSimpleName() + "] 끝 =====");
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
        logger.debug("SQLMAPPER생성");

        fp = new FormatParserAsMap(logger);
        if (fp.doParsingAsMap("NeoDMWS6060_msgformat") < 0) {
            logger.error("전문포맷 분석오류");
            return;
        }

        Utils.getResources("conf/ApplicationResources", appRes);

        while (!Thread.currentThread().isInterrupted()) {
            //mainLoop();
            mainLoop_re();
            try {
            	Thread.sleep( 30000 );

            } catch (InterruptedException e) {
                logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	private int mainLoop_re() {

		final String FILE_EXT = ".OK"; // 전송완료파일확장자

		// /dees_svr/FTP_FILE
		final String FOLDER = appRes.getString("RecvFromBank.recvDirectory");
		final String RECV_DIR = FOLDER.concat("/dees_report78/recv");
		final String WORK_DIR = FOLDER.concat("/dees_report78/work");
		final String BACK_DIR = FOLDER.concat("/dees_report78/back");
		final String ERROR_DIR = FOLDER.concat("/dees_report78/error");

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

				String keyValue = ""; // 세출한도계좌거래내역 APPEND를 위한 변수

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
				// 계좌거래내역 수신 연계일 경우, 세출한도계좌 정보를 R 에 등록한다.
			    if( FB_FM_XXX_RD_00007.equals( keyValue)) {
			    	// 07 : FB_FM_XXX_RD_00007	계좌거래내역 수신

			        // 세출한도계좌 거래내역 append
			    	// 1. 지출, 가상계좌 이체결과처리 데몬 에서 세출한도계좌정보를 TFMA050A에 데이터를 넣어준다. 전송상태 : N
			    	// 2. 금고은행에서 계좌거래내역 수신이 오면, 세출한도계좌정보와 함께 재정으로 보낸다. 전송상태 : Y
			    	// 3. 금고은행에서 보고서(월별잔액수신)이 오면, 전송상태 Y 건의 세출한도계좌 잔액정보를 재정으로 보낸다.
			    	/**
			    	 * TFE2547 -- 반납계좌 - 입금;
			    	 * TFE2170 -- 입금
			    	 * TFE2190 -- 지급(출금);
			    	 */
			    	logger.info( "#### selectTfma050aList_start" );

			    	@SuppressWarnings("unchecked")
					List<MyMap> aneList = session.selectList("NeoMapper6060.selectTfma050aList");
			    	int aneSize = aneList.size();

			    	logger.info( "#### selectTfma050aList_end" );

			    	logger.info( ">>>>>>>>>세출한도계좌 거래내역 건수 : " + aneSize + "건" );

			    	logger.info( "#### 계좌거래내역 insertTfma050R&updateTfma050aTrnmtYn_start" );

			    	for( MyMap aneMap : aneList ) {

			    		//aneMap.setMap("LINK_TRSC_ID_SEQ", iRequestNum);

			    		 session.insert("NeoMapper6060.insertTfma050R", aneMap);
			    		 session.update("NeoMapper6060.updateTfma050aTrnmtYn", aneMap);

                	}

			    	logger.info( "#### 계좌거래내역 insertTfma050R&updateTfma050aTrnmtYn_end" );

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
    private int mainLoop() throws Exception {

		final String FILE_EXT = ".OK"; // 전송완료파일확장자

		// /dees_svr/FTP_FILE
		final String FOLDER = appRes.getString("RecvFromBank.recvDirectory");
		final String RECV_DIR = FOLDER.concat("/dees_report78/recv");
		final String WORK_DIR = FOLDER.concat("/dees_report78/work");
		final String BACK_DIR = FOLDER.concat("/dees_report78/back");
		final String ERROR_DIR = FOLDER.concat("/dees_report78/error");


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

	        	int iRequestNum = 1;
	        	int iTrscSeq = 1;

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
	 * NeoDMWS6060_msgformat.xml 에 등록된 전문번호 구하기
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

			System.out.println("----------------------------------------------------------- AAAA");
			String fileWorkGb = dataMap.getString("fileWorkGb"); // KLIDDG03(8)
			String keyValue = getKeyValue( fileWorkGb );

			dataMap.setMap("LINK_TRSC_ID_SEQ", iRequestNum);

			// 연계기본 컬럼 셋팅
			// dataMap 에 키값이 없으면 쿼리 실행시, 오류 발생함.
			dataMap.setMap("BTCH_PRCS_STAT_CD", "");
			dataMap.setMap("BTCH_PRCS_DT", "");
			dataMap.setMap("TRNMT_YN", "");
			dataMap.setMap("LINK_PRCS_DV_CD", "C");
			dataMap.setMap("LINK_TRSM_STAT_CD", "");
			dataMap.setMap("LINK_ERR_MSG_CN", "");
			dataMap.setMap("PRCT_PRCS_DT", "");
			dataMap.setMap("LINK_STAT_PRCS_DT", "");

			if( logger.isDebugEnabled()) {
				logger.debug( "REPORT NUMBER[" + iRequestNum + "]" );
				logger.debug( "PARAM[" + dataMap + "]");
			}

            if( FB_FM_XXX_RD_00007.equals( keyValue)) {
            	// 07 : FB_FM_XXX_RD_00007	계좌거래내역 수신

                // TFMA050R(자금마감계좌거래내역수신연계)
            	iCnt = session.insert("NeoMapper6060.insertTfma050R", dataMap);

            } else if( FB_FM_XXX_RD_00008.equals( keyValue)) {
            	// 08 : FB_FM_XXX_RD_00008	계좌신규 개설 및 약정정보 수신

            	// 업무원장등록
            	String stbxEcrpActno = dataMap.getString("STBX_ECRP_ACTNO");
            	logger.debug( "[계좌신규 개설 및 약정정보 수신] 세출원장(매핑테이블) 등록[" + stbxEcrpActno + "]" );

            	MyMap param = new MyMap();
            	param.setString("STBX_ECRP_ACTNO", stbxEcrpActno);

            	// 세출원장 건수조회
            	int rCnt = (int) session.selectOne("NeoMapper6060.selectTfma120Cnt", param);
            	if( rCnt == 0 ) {

            		// insert 세출원장
            		iCnt = session.insert("NeoMapper6060.insertTfma120", dataMap);

            	}else {

            		// 이력등록
            		session.insert("NeoMapper6060.insertTfma120h", dataMap);

            		// update 세출원장
            		iCnt = session.update("NeoMapper6060.updateTfma120", dataMap);

            	}

            	if( iCnt > 0 ) {
            		// 세출원장 등록/수정되면 R테이블 등록

            		try {

                        // TFMA120R(자금마감계좌신규개설약정수신연계)
                    	iCnt = session.insert("NeoMapper6060.insertTfma120R", dataMap);

            		}catch(Exception e) {
            			e.printStackTrace();
            		}

            	}
            }

    		logger.debug( "6060 insert 실행 ["+iCnt+"]" );

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

        logger.debug("INPUT_TLGM =["+readLine+"]");
        logger.debug("LogMf =["+param+"]");

        session.insert( "NeoMapperCommon.insertTranLog", param );

        logger.debug("========> insertTRAN_RECP OK !! ");

	}

} // end class
