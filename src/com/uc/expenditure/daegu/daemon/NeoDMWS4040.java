/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 입금지급 처리
 *  기  능  명 : 이체처리 결과에 대한 e세출업무원장 이체결과 생성처리
 *  클래스  ID : NeoDMWS4040
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  이경두       다산(주)      2022.08.29         %01%             최초작성
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

import com.uc.framework.parsing.FormatParserAsMap;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS4040 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger   logger = Logger.getLogger(NeoDMWS4040.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static FormatParserAsMap          fp = null;
    private static Thread   self = null;
    static MyMap    appRes = new MyMap();

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS4040.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS4040.class.getSimpleName() + "] 시작 #####");

        NeoDMWS4040  neoDMWS4040 = new NeoDMWS4040();

        self = new Thread(neoDMWS4040);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("##### [" + NeoDMWS4040.class.getSimpleName() + "] 작업 종료 #####");
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
    public int mainLoop()
    {
    	/**
    	 * [result]
    	 * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
    	 */
        String  folder = appRes.getString("RecvFromBank.recvDirectory");
        String  recvDir = folder + "/dees_trn/recv";
        String  workDir = folder + "/dees_trn/work";
        String  backDir = folder + "/dees_trn/back";
        String  errorDir = folder + "/dees_trn/error";

        File    recv_Dir     = new File( recvDir );
        MyMap paramMap     = new MyMap();
        MyMap supplyledger = new MyMap();

        String[] recv_file_list = recv_Dir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                /**
                 * [result]
                 * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
                 */
                return name.endsWith(".EC");
            }
        });

        // Receive File Parser
        MyMap SearchMap = new MyMap();
        int recv_file_cnt = recv_file_list.length;

        if ( recv_file_cnt <= 0 ) {
            logger.info("수신 된 파일이 없습니다.");
            return -1;
        }

        if (recv_file_cnt > 0) {
            logger.info( "###############수신데이타파일 건수 : [" + recv_file_cnt + "]건" );
        }

        try {
            session = sqlMapper.openSession(ExecutorType.BATCH,false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        /******************************************************************************
        * 파일목록 처리 시작
        ******************************************************************************/
        for( int i = 0 ; i < recv_file_cnt ; i++ ) {
            logger.info( "파일명 [" + recv_file_list[i] + "]" );

            SearchMap.clear();

            String[] file_nm = recv_file_list[i].split( "\\." );
            File recv_file  = new File( recvDir  + "/" + file_nm[0]);  // 수신디렉토리
            File work_file  = new File( workDir  + "/" + file_nm[0]);  // 작업디렉토리
            File back_file  = new File( backDir  + "/" + file_nm[0]);  // 작업디렉토리
            File ok_file    = new File( recvDir  + "/" + recv_file_list[i] );  // 수신디렉토리
            File error_file = new File( errorDir + "/" + file_nm[0] + "." + Utils.getCurrentTime() ); // 에러디렉토리

            // 작업폴더로 이동
            logger.info( "[ work_file_start  ] ");

            recv_file.renameTo( work_file );  // XXXX.ok -> XXXX

            logger.info("###recv_file : ["+recv_file+"]");
            logger.info("###work_file : ["+work_file+"]");
            logger.info( "[ work_file_end  ] ");

            String str = null;
            FileInputStream fis = null;

            try {
                fis = new FileInputStream(work_file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                logger.error("파일을 열수 없습니다. 내용[" + e.getLocalizedMessage() + "]");
                continue;   // 다음 파일
            }

            InputStreamReader isr = null;

            try {
                isr = new InputStreamReader(fis, "euckr");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                logger.error("파일을 열수 없습니다. 내용[" + e.getLocalizedMessage() + "]");
                continue;   // 다음 파일
            }

            BufferedReader br = new BufferedReader(isr);

            try {
                str = br.readLine();
            } catch (IOException ioex) {
                logger.error("파일을 읽을 수 없습니다. 내용[" + ioex.getLocalizedMessage() + "]");
                continue;   // 다음 파일
            }

            if (str == null) {
                logger.error("파일을 읽을 수 없습니다");
                continue;   // 다음 파일
            }

            try {
                logger.debug( "Heaer 처리 시작[" + str.getBytes("euckr") + "]" );
                if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "100") < 0) {
                    logger.error("헤더부분 파싱 오류");
                    continue;
                }
            } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                continue;
            }

            /******************************************************************************
             * 일시 : 2022.11.01
             * 내용 : recv daemon에 as-is건도 처리 되기 때문에 to-be건인지 체크하여 원장 테이블에 존재하는 건만 처리.
             *****************************************************************************/
//            int prcCnt = (Integer) session.selectOne("NeoMapper4040.getPrcCnt", paramMap );
//            if (prcCnt <= 0) {
//                logger.error("거래번호 [" + paramMap.getString("파일코드") + "] 존재하지 않아 SKIP 합니다.");
//                logger.error("조회건수 [" + prcCnt + "]");
//                continue;
//            }

			// 정상이 아닐 경우 처리
            if (!paramMap.getString("파일불능코드").equals("0000")) {
                logger.error("이체파일[" + paramMap.getString("파일코드") + "] 처리중 오류가 발생했습니다. 오류코드[" + paramMap.getString("파일불능코드") + "]");

                // 지급원장
                MyMap   errorMap = new MyMap();
                errorMap.setString("분류코드", "WS0001");  // 대구은행 이체 오류
                errorMap.setString("코드", paramMap.getString("파일불능코드"));
                errorMap = (MyMap)session.selectOne( "NeoMapper4040.getErrorCode", errorMap);

                if (errorMap.getString("ERRMSG") != null) {
                    paramMap.setString("RESULT_CD", paramMap.getString("파일불능코드"));
                    paramMap.setString("RESULT_EX", errorMap.getString("ERRMSG"));
                } else {
                    paramMap.setString("RESULT_CD", "9999");
                    paramMap.setString("RESULT_EX", "알수없는 오류");
                }

                paramMap.setString("거래일자", paramMap.getString("파일코드").substring(2, 8));

                logger.debug("요청ID[" 			+ paramMap.getString("요청ID") 		 + "]");
                logger.debug("요청기관구분[" 		+ paramMap.getString("요청기관구분") 	 + "]");
                logger.debug("자치단체코드[" 		+ paramMap.getString("자치단체코드") 	 + "]");
                logger.debug("관서코드[" 			+ paramMap.getString("관서코드") 		 + "]");
                logger.debug("지급부서코드[" 		+ paramMap.getString("지급부서코드") 	 + "]");
                logger.debug("회계연도[" 			+ paramMap.getString("회계연도") 		 + "]");
                logger.debug("회계코드[" 			+ paramMap.getString("회계코드") 		 + "]");
                logger.debug("자료구분[" 			+ paramMap.getString("자료구분") 		 + "]");
                logger.debug("지급명령등록번호[" 	+ paramMap.getString("지급명령등록번호") + "]");
                logger.debug("지급명령번호[" 		+ paramMap.getString("지급명령번호") 	 + "]");
                logger.debug("재배정여부[" 		+ paramMap.getString("재배정여부") 	 + "]");
                logger.debug("이체일자[" 			+ paramMap.getString("이체일자") 		 + "]");
                logger.debug("파일코드[" 			+ paramMap.getString("파일코드") 		 + "]");
                logger.debug("결과코드[" 			+ paramMap.getString("결과코드") 		 + "]");
                logger.debug("결과설명[" 			+ paramMap.getString("결과설명") 		 + "]");
                logger.debug("결과처리자명[" 		+ paramMap.getString("결과처리자명") 	 + "]");

                // 묶음건이면 호조업데이트 없음
                if ("EF".equals(paramMap.getString("요청ID"))) {
                    paramMap.setString( "작업상태코드" , "32");
                } else {
                    paramMap.setString( "작업상태코드" , "31");
                }

                logger.debug("작업상태코드[" 		+ paramMap.getString("작업상태코드") 	 + "]");

                try {
                    // 지급원장 및 입금명세 업데이트 (자료수신여부를 'D로 업데이트)
                    session.update("NeoMapper4040.update_supplyledger_result_restore", paramMap);
                    session.update("NeoMapper4040.update_receipt_result_restore", paramMap);
                    logger.debug("이체파일 오류건을 처리했습니다. 파일코드 ["+paramMap.getString("파일코드") +"]");
                    session.commit();
                } catch (Exception ex) {
                    logger.error("원인[" + ex.getLocalizedMessage() + "]");
                    session.rollback();
                    session.close();
                    return -1;
                }

                try {
                    br.close();
                    isr.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
                }

                work_file.renameTo( error_file );
                ok_file.delete();
                logger.error("확약");
                continue;
            } else {
                supplyledger.setString( "요청ID"			, paramMap.getString( "요청ID"));
                supplyledger.setString( "요청기관구분"		, paramMap.getString( "요청기관구분"));
                supplyledger.setString( "자치단체코드"		, paramMap.getString( "자치단체코드"));
                supplyledger.setString( "관서코드"		, paramMap.getString( "관서코드"));
                supplyledger.setString( "지급부서코드"		, paramMap.getString( "지급부서코드"));
                supplyledger.setString( "회계연도"		, paramMap.getString( "회계연도"));
                supplyledger.setString( "회계코드"		, paramMap.getString( "회계코드"));
                supplyledger.setString( "자료구분"		, paramMap.getString( "자료구분"));
                supplyledger.setString( "지급명령등록번호"	, paramMap.getString( "지급명령등록번호"));
                supplyledger.setString( "재배정여부"		, paramMap.getString( "재배정여부"));
                supplyledger.setString( "파일코드"		, paramMap.getString( "파일코드"));
                supplyledger.setString( "거래일자"		, paramMap.getString( "파일코드").substring(2, 8));
                supplyledger.setString( "이체일자"		, paramMap.getString( "이체일자"));
                supplyledger.setString( "불능분입금계좌번호"	, paramMap.getString( "불능분입금계좌번호"));

                logger.info( "거래일자 ["+supplyledger.getString( "거래일자") +"]" );

                if (paramMap.getString("출금계좌관리점") != "") {
                    logger.debug("출금계좌관리점[" + paramMap.getString("출금계좌관리점") + "]");
                    logger.debug("출금계좌관리점명[" + paramMap.getString("출금계좌관리점명") + "]");
                    logger.debug("출금계좌번호[" + paramMap.getString("출금계좌번호") + "]");
                    paramMap.setString("은행코드", "031");

                    try {
                    	session.update("NeoMapper4040.deleteAccountBranch", paramMap);
                        session.insert("NeoMapper4040.insertAccountBranch", paramMap);
                    } catch (Exception ex) {
                        logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]");
                    }
                }
                paramMap.clear();

                logger.info( "Heaer 처리 종료" );
            }

            long beforeTime = System.currentTimeMillis();
            long l_count = 0;

            try {
            	logger.info( "##### NeoMapper4040.updateTFE2170Fail_start" );
                while( ( str = br.readLine() ) != null ) {
                    MyMap errorMap    = new MyMap();
                    String  indentityCd = str.substring( 8 , 9 );
                    /******************************************************************************
                    * 파일데이타 정보인 경우
                    ******************************************************************************/
                    if( indentityCd.equals( "D" ) ) {
                        fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "100", "DGCITY01");
                        logger.debug("입금예금주명[" + paramMap.getString("입금계좌예금주명","euckr") + "]");

						//@@@ 아래 예전 키값 필요없을듯 @@@
                        paramMap.setString( "요청ID" , 		supplyledger.getString( "요청ID"));
                        paramMap.setString( "요청기관구분" , 	supplyledger.getString( "요청기관구분"));
                        paramMap.setString( "자치단체코드" , 	supplyledger.getString( "자치단체코드"));
                        paramMap.setString( "관서코드" , 		supplyledger.getString( "관서코드"));
                        paramMap.setString( "지급부서코드" , 	supplyledger.getString( "지급부서코드"));
                        paramMap.setString( "부서코드" , 		supplyledger.getString( "지급부서코드"));
                        paramMap.setString( "회계연도" , 		supplyledger.getString( "회계연도"));
                        paramMap.setString( "회계코드" , 		supplyledger.getString( "회계코드"));
                        paramMap.setString( "자료구분" , 		supplyledger.getString( "자료구분"));
                        paramMap.setString( "지급명령등록번호",	supplyledger.getString( "지급명령등록번호"));
                        paramMap.setString( "거래일자", 		supplyledger.getString( "거래일자"));
                        paramMap.setString( "재배정여부", 		supplyledger.getString( "재배정여부"));
                        paramMap.setString( "RESULT_NM", 	"대구은행" );
                        paramMap.setString( "RESULT_YN", 	"Y" );
                        paramMap.setString( "거래번호", 	    supplyledger.getString( "파일코드"));  // 키값 추가@@@@@@@@@@

                        if (paramMap.getString("이체처리불능코드") == null || "".equals(paramMap.getString("이체처리불능코드"))) {
                            logger.debug("[" + paramMap.getString("지급명령등록번호") + "] 결과코드값이 없습니다.");
                            paramMap.setString("RESULT_EX" , "결과코드가 없습니다. 입급내역을 확인하세요");
                            paramMap.setString("RCPT_YN"   , "Y" ); // 자료수신여부
                            paramMap.setString("이체처리불능코드", "0000");
                        }
                        else if( paramMap.getString("이체처리불능코드").equals("BN10501")) {
                            paramMap.setString("RESULT_EX" , paramMap.getString("이체처리불능내용","euckr"));
                            paramMap.setString("RCPT_YN"   , "Y" ); // 자료수신여부
                            paramMap.setString("이체처리불능코드", "0000");
                        } else {
                        	//입급유형이 고지서(40), 현금(99), 수표(60)인 경우
                            if (paramMap.getString("입금유형").equals("40") || paramMap.getString("입금유형").equals("60") || paramMap.getString("입금유형").equals("99")) {
                                // 대구은행에서 오류로 줌->정상으로 처리한다.
                                paramMap.setString("RESULT_EX" , "정상처리 되었습니다.");
                                paramMap.setString("RCPT_YN"   , "Y" ); // 자료수신여부
                                paramMap.setString("이체처리불능코드", "0000");
                            } else {
                            	logger.info("###이체처리불능코드 : ["+paramMap.getString( "이체처리불능코드" )+"]");

                            	if (paramMap.getString("이체처리불능내용") != null && !"".equals(paramMap.getString("이체처리불능내용"))) {
	                            	logger.info("###이체처리불능내용 : ["+paramMap.getString( "이체처리불능내용" )+"]");
	                            	paramMap.setString("RESULT_EX" , paramMap.getString("이체처리불능내용"));
                            	} else {
                            		paramMap.setString( "RESULT_EX" , "그 외 오류" ); // 결과설명
                            	}

                            	/**
                            	 * 은행에서 넘어 온 이체결과내용을 그대로 세팅해주는것으로 협의하여 변경(20221219)
                            	 */
//                                errorMap.setString( "코드" , paramMap.getString( "이체처리불능코드" ).substring(4));
//
//                                logger.info("###코드 : ["+errorMap.getString( "코드" )+"]");
//
//                                if("031".equals(paramMap.getString( "입금은행코드" ))) {
//                                    errorMap.setString( "분류코드" , "WS0001" );  // 대구은행 이체 오류
//                                } else {
//                                    errorMap.setString( "분류코드" , "WS0002" );  // 타행 오류
//                                }
//                                errorMap = (MyMap)session.selectOne( "NeoMapper4040.getErrorCode" , errorMap );
//
//                                if(!errorMap.isEmpty()) {
//                                    paramMap.setString( "RESULT_EX" , errorMap.getString( "ERRMSG" ) ); // 결과설명
//                                } else {
//                                    paramMap.setString( "RESULT_EX" , "그 외 오류" ); // 결과설명
//                                }

                                // 입금오류 건은 자료수신여부를 'D'로 봐꾼다.
                                paramMap.setString( "RCPT_YN" , "Y" ); // 자료수신여부
                            }
                        }

                        logger.debug("paramMap : " + paramMap);

                        try {
                        	/******************************************************************************
							* (세출)입금명세 이체처리결과 최종업데이트(정상 아닌것만!!!)
							******************************************************************************/
							if(!"0000".equals(paramMap.getString("이체처리불능코드"))) {
								session.update( "NeoMapper4040.updateTFE2170Fail" , paramMap );
								//session.commit();
							}
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }

                        paramMap.clear();
                    }
                    /******************************************************************************
                    * 파일테일러 정보인 경우
                    ******************************************************************************/
                    else if( indentityCd.equals( "E" ) ) {
                        fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "100");

                        supplyledger.setLong( "정상처리건수" , paramMap.getLong( "정상처리건수" ) );
                        supplyledger.setLong( "정상처리금액" , paramMap.getLong( "정상처리금액" ) );

                        // 파일에 포함되지 않은 데이터 역시 오류이므로 포함시킴
                        supplyledger.setLong("미처리건수" , paramMap.getLong("미처리건수"));
                        supplyledger.setLong("미처리금액" , paramMap.getLong("미처리금액"));

                        paramMap.clear();
                    }

                    l_count = l_count + 1;

//                    if( l_count%1000 == 0 ) {
//                        session.commit();
//                    }
                }
                logger.info( "##### NeoMapper4040.updateTFE2170Fail_end" );
                logger.debug("while 종료");

                /**
                 * [result]
                 * 정상건  전체 한번에 업데이트
                 */
                paramMap.setString( "거래번호", 	    supplyledger.getString( "파일코드"));  // 키값 추가@@@@@@@@@@
                logger.info("##### NeoMapper4040.updateTFE2170Succ_start");
                session.update( "NeoMapper4040.updateTFE2170Succ"  , paramMap );
                logger.info("##### NeoMapper4040.updateTFE2170Succ_end");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }// end of while

            try {
                br.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                logger.error("파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
            }

            try {
                isr.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                logger.error("파일닫는중 오류발생, 내용[" + e.getLocalizedMessage() + "]");
            }

            if (supplyledger.getLong( "미처리건수" ) > 0 ) {
                supplyledger.setString( "RESULT_CD" , "9000"                                                     ); // 결과코드
                supplyledger.setString( "RESULT_EX" , "입금 오류 건이 있습니다. 입금명세를 확인하시기 바랍니다." ); // 결과설명
            } else {
                supplyledger.setString( "RESULT_CD" , "0000"     ); // 결과코드
                supplyledger.setString( "RESULT_EX" , "지급완료" ); // 결과설명
            }

            supplyledger.setString( "RESULT_NM" , "대구은행"     ); // 처리자명
            supplyledger.setString( "RESULT_YN" , "Y"            ); // <---- 사용하지 아니한다.

            /******************************************************************************
            * (세출)지급원장 이체처리결과 최종업데이트
            ******************************************************************************/
			logger.info("##### (세출)지급원장 이체처리결과 최종업데이트 ::: ["+supplyledger.toString()+"]");

            session.update( "NeoMapper4040.updateTFE2190Result" , supplyledger );

            long afterTime  = System.currentTimeMillis();
            long secDiffTime = (afterTime - beforeTime)/1000;

            logger.info( " [ " + supplyledger.getString("자치단체코드") + " - " + supplyledger.getString("지급명령등록번호") + " - " +
                                        supplyledger.getString("파일코드") + " - " + supplyledger.getString("정상처리건수") +
                             " 건 ] 작업 완료"  + " [ " + secDiffTime + " 초 ]");

            try {
                session.commit();
                logger.debug( "[ back_file_start  ]");
                work_file.renameTo(back_file );
                ok_file.delete();
                logger.debug( "[ back_file_end  ]");
                supplyledger.clear();
            } catch (Exception e) {
                // TODO: handle exception
                session.rollback();
                logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]");
                return 0;
            }
        }// end of for statement

        logger.debug("작업 완료!!!");
        session.close();
        return 0;
    }
}
