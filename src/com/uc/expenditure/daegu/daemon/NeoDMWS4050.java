/**
 *  주시스템명 : 과오납
 *  업  무  명 : 차세대지방세입 -> 금고은행
 *  기  능  명 : 전문 파싱
 *  클래스  ID : NeoDMWS4050
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박상현       다산(주)     2023.08.31         %01%             최초작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
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

public class NeoDMWS4050 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger   logger = Logger.getLogger(NeoDMWS4050.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static FormatParserAsMap          fp = null;
    private static Thread   self = null;
    static MyMap    appRes = new MyMap();

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS4050.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS4050.class.getSimpleName() + "] 시작 #####");

        NeoDMWS4050  NeoDMWS4050 = new NeoDMWS4050();

        self = new Thread(NeoDMWS4050);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
            logger.error(e);
        }

        logger.info("##### [" + NeoDMWS4050.class.getSimpleName() + "] 작업 종료 #####");
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
            logger.debug("##### SqlSessionFactoryBuilder #####");
        } catch (Exception ex) {
            logger.debug("##### Exception #####");
            logger.error("데이터베이스 접속중 오류[" + ex.getLocalizedMessage() + "]");
            return;
        }

        logger.debug("SQLMAPPER생성");

        fp = new FormatParserAsMap(logger);

        if (fp.doParsingAsMap("msgformat5") < 0) {
            logger.error("전문포맷 분석오류");
            return;
        }

        Utils.getResources("conf/ApplicationResources", appRes);

        while (!Thread.currentThread().isInterrupted()) {
            mainLoop(); // 공통
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
                break;
            }
        }
    }

    /**
     * process 실행   공통
     * @return
     */
    public int mainLoop()
    {
        /**
         * [result]
         * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
         */
        logger.debug("mainLoop 시작");
        String  folder = appRes.getString("RecvFromBank.recvDirectory");
        String  recvDir = folder + "/dees_otc/recv";
        String  workDir = folder + "/dees_otc/work";
        String  backDir = folder + "/dees_otc/back";
        String  errorDir = folder + "/dees_otc/error";

        // 대구,경북 구분 대구:D,  경북:G
        String  slforgGb = "D";

        logger.debug("##### "+ recvDir +" #####");

        BufferedInputStream bis=null;    //20230911

        File    recv_Dir   = new File( recvDir ); //c:/dees_svr/FTP_FILE/dees_otc/recv
        MyMap paramMap     = new MyMap();
        MyMap supplyledger = new MyMap();
        MyMap seqMap     = new MyMap();

        String[] recv_file_list = recv_Dir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                /**
                 * [result]
                 * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
                 */
                logger.debug("파일명 ["+ name +"]");
                return name.endsWith(".OK");
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

            if (recv_file_list[i].substring(0,6).equals("CO1001")) {  // 1001 전문 처리 시작
                logger.info( "파일명 [" + recv_file_list[i] + "]");
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


                /******************************************************************************
                 * 일시 : 2023.09.14 (PM 박영진과장 요청 추가)
                 * 내용 : 같은 파일명으로 데이터가 들어올 경우는 없다고 본다 하지만, 들어올 경우 이전 파일 통해 등록된 DB 데이터 삭제 하고 새로 등록 하게 하자
                 *****************************************************************************/
                if(slforgGb.equals("G")) {
                    supplyledger.setString( "파일명" , file_nm[0]);  // 파일명으로만 체크해서 확인 함.
                    logger.error("**************** 중복파일명 [" + supplyledger.getString( "파일명") + "] 중복 파일 발생.****************");

                    int prcCnt = (Integer) session.selectOne("NeoMapper4050.getDataCntCO1001", supplyledger );

                    if (prcCnt > 0) {
                        try {
                            logger.error("## 중복파일명 [" + file_nm[0] + "] 중복 파일 발생. 이전 데이터 삭제 진행 시작");

                            // 중복된 파일이 들어 왔다는 것은 이전 파일에 대한 변경된 데이터가 왔다고 판단해서 삭제하고, 나중에 들어온 파일로 다시 데이터 적재
                            int b = session.update("NeoMapper4050.deleteCO1001", supplyledger);
                            session.commit();

                            logger.info( "delete[" + b + " 중복 파일 발생. 이전 데이터 삭제 완료 ]" );
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }
                    }
                }
                /******************************************************************************/

                int iTrscSeq = 1;
                byte[] buffer=new byte[170];   // CO1001
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11") && slforgGb.equals("G")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1001") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           logger.debug( "#### Heaer 처리 시작" );
    //                       logger.info(paramMap);

    //                       supplyledger.setString( "업무구분"            , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"            , paramMap.getString( "일련번호"));
                           supplyledger.setString( "송신기관코드"        , paramMap.getString( "송신기관코드"));
                           supplyledger.setString( "수신기관코드"        , paramMap.getString( "수신기관코드"));
                           supplyledger.setString( "총DataRecord수"    , paramMap.getString( "총DataRecord수"));
                           supplyledger.setString( "전달일"            , paramMap.getString( "전달일"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));

                           paramMap.clear();
                           logger.debug( "#### Heaer 처리 종료" );
                       } else if (DataGubun.equals("22") && slforgGb.equals("G")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1001", "MDATA");
                           logger.info(paramMap);

    //                     supplyledger.setString( "업무구분"        , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"        , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));
                           supplyledger.setString( "구분코드"        , paramMap.getString( "구분코드"));
                           supplyledger.setString( "시스템구분코드"    , paramMap.getString( "시스템구분코드"));
                           supplyledger.setString( "전송일시"        , paramMap.getString( "전송일시"));
                           supplyledger.setString( "이용기관지로번호"    , paramMap.getString( "이용기관지로번호"));
                           supplyledger.setString( "지방자치단체코드"    , paramMap.getString( "지방자치단체코드"));
                           supplyledger.setString( "지방자치단체명"    , paramMap.getString( "지방자치단체명"));
                           supplyledger.setString( "세입과목전달건수"    , paramMap.getString( "세입과목전달건수"));
                           supplyledger.setString( "FILLER2"    , paramMap.getString( "FILLER2"));

                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       }
                       else if (DataGubun.equals("33") && slforgGb.equals("G")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1001", "DDATA");
                           logger.info(paramMap);
                           supplyledger.setString( "업무구분"      , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"      , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"      , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"      , paramMap.getString( "FILLER"));
                           supplyledger.setString( "순번"          , paramMap.getString( "순번"));
                           supplyledger.setString( "회계년도"      , paramMap.getString( "회계년도"));
                           supplyledger.setString( "회계구분"      , paramMap.getString( "회계구분"));
                           supplyledger.setString( "세입과목코드" , paramMap.getString( "세입과목코드"));
                           supplyledger.setString( "세입과목명"      , paramMap.getString( "세입과목명"));
                           supplyledger.setString( "운영과목코드" , paramMap.getString( "운영과목코드"));
                           supplyledger.setString( "운영과목명"  , paramMap.getString( "운영과목명"));
                           supplyledger.setString( "FILLER2"  , paramMap.getString( "FILLER2"));

                           try {
                               logger.info("---------------------------- DETAIL(DB) --------------------------");

                               int a = session.insert("NeoMapper4050.insertCO1001", supplyledger );
                               logger.info("insert[" + a + "]");
                           } catch (Exception ex) {
                               logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
                           }
                           paramMap.clear();

                           logger.debug( "#### DETAIL 처리 종료" );
                       }

                       if(slforgGb.equals("D")) {
                           insertTranRecpLog( iTrscSeq, str );
                       }
                       if(iTrscSeq % 500 == 0) {
                           session.commit();
                       }
                       iTrscSeq++;
                    }
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        supplyledger.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            // 1001 전문 끝
            } else if ( recv_file_list[i].substring(0,6).equals("CO1002")) {
                logger.info( "파일명 [" + recv_file_list[i] + "]");
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


                /******************************************************************************
                 * 일시 : 2023.09.14 (PM 박영진과장 요청 추가)
                 * 내용 : 같은 파일명으로 데이터가 들어올 경우는 없다고 본다 하지만, 들어올 경우 이전 파일 통해 등록된 DB 데이터 삭제 하고 새로 등록 하게 하자
                 *****************************************************************************/
                if(slforgGb.equals("G")) {
                    supplyledger.setString( "파일명" , file_nm[0]);  // 파일명으로만 체크해서 확인 함.
                    logger.info("**************** 파일명 [" + supplyledger.getString( "파일명") + "] ****************");

                    int prcCnt = (Integer) session.selectOne("NeoMapper4050.getDataCntCO1002", supplyledger );

                    if (prcCnt > 0) {
                        try {
                            logger.error("## 중복파일명 [" + file_nm[0] + "] 중복 파일 발생. 이전 데이터 삭제 진행 시작");

                            // 중복된 파일이 들어 왔다는 것은 이전 파일에 대한 변경된 데이터가 왔다고 판단해서 삭제하고, 나중에 들어온 파일로 다시 데이터 적재
                            int b = session.update("NeoMapper4050.deleteCO1002", supplyledger);
                            session.commit();
                            logger.info( "delete[" + b + " 중복 파일 발생. 이전 데이터 삭제 완료 ]" );
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }
                    }
                }
                /******************************************************************************/
                int iTrscSeq = 1;
                byte[] buffer=new byte[190];   // CO1002
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11") && slforgGb.equals("G")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1002") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           logger.debug( "#### Heaer 처리 시작" );
                           logger.info(paramMap);

//                           supplyledger.setString( "업무구분"            , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"            , paramMap.getString( "일련번호"));
                           supplyledger.setString( "송신기관코드"        , paramMap.getString( "송신기관코드"));
                           supplyledger.setString( "수신기관코드"        , paramMap.getString( "수신기관코드"));
                           supplyledger.setString( "총DataRecord수"    , paramMap.getString( "총DataRecord수"));
                           supplyledger.setString( "전달일"            , paramMap.getString( "전달일"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));

                           paramMap.clear();
                           logger.debug( "#### Heaer 처리 종료" );
                       } else if (DataGubun.equals("22") && slforgGb.equals("G")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1002", "MDATA");
                           logger.info(paramMap);

//                         supplyledger.setString( "업무구분"        , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"        , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));
                           supplyledger.setString( "구분코드"        , paramMap.getString( "구분코드"));
                           supplyledger.setString( "시스템구분코드"    , paramMap.getString( "시스템구분코드"));
                           supplyledger.setString( "전송일시"        , paramMap.getString( "전송일시"));
                           supplyledger.setString( "이용기관지로번호"    , paramMap.getString( "이용기관지로번호"));
                           supplyledger.setString( "지방자치단체코드"    , paramMap.getString( "지방자치단체코드"));
                           supplyledger.setString( "지방자치단체명"    , paramMap.getString( "지방자치단체명"));
                           supplyledger.setString( "회계과목전달건수"    , paramMap.getString( "회계과목전달건수"));
                           supplyledger.setString( "FILLER2"    , paramMap.getString( "FILLER2"));

                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       }
                       else if (DataGubun.equals("33") && slforgGb.equals("G")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1002", "DDATA");
                           logger.info(paramMap);
                           supplyledger.setString( "업무구분"      , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"      , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"      , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"      , paramMap.getString( "FILLER"));
                           supplyledger.setString( "순번"          , paramMap.getString( "순번"));
                           supplyledger.setString( "특별회계사업코드" , paramMap.getString( "특별회계사업코드"));
                           supplyledger.setString( "특별회계사업명"  , paramMap.getString( "특별회계사업명"));
                           supplyledger.setString( "FILLER2"  , paramMap.getString( "FILLER2"));

                           try {
                               logger.info("---------------------------- DETAIL(DB) START --------------------------");

                               int a = session.insert("NeoMapper4050.insertCO1002", supplyledger );
                               logger.info("insert[" + a + "]");
                           } catch (Exception ex) {
                               logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
                           }
                           paramMap.clear();

                           logger.debug( "#### DETAIL 처리 종료" );
                       }

                       if(slforgGb.equals("D")) {
                           insertTranRecpLog( iTrscSeq, str );
                       }
                       if(iTrscSeq % 500 == 0) {
                           session.commit();
                       }
                       iTrscSeq++;
                    }
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        supplyledger.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            } else if ( recv_file_list[i].substring(0,6).equals("CO1003")) {
                // 사용자 정보
                logger.info( "파일명 [" + recv_file_list[i] + "]");
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


                /******************************************************************************
                 * 일시 : 2023.09.14 (PM 박영진과장 요청 추가)
                 * 내용 : 같은 파일명으로 데이터가 들어올 경우는 없다고 본다 하지만, 들어올 경우 이전 파일 통해 등록된 DB 데이터 삭제 하고 새로 등록 하게 하자
                 *****************************************************************************/
                if(slforgGb.equals("G")) {
                    supplyledger.setString( "파일명" , file_nm[0]);  // 파일명으로만 체크해서 확인 함.
                    logger.info("**************** 파일명 [" + supplyledger.getString( "파일명") + "] ****************");

                    int prcCnt = (Integer) session.selectOne("NeoMapper4050.getDataCntCO1003", supplyledger );

                    if (prcCnt > 0) {
                        try {
                            logger.error("## 중복파일명 [" + file_nm[0] + "] 중복 파일 발생. 이전 데이터 삭제 진행 시작");

                            // 중복된 파일이 들어 왔다는 것은 이전 파일에 대한 변경된 데이터가 왔다고 판단해서 삭제하고, 나중에 들어온 파일로 다시 데이터 적재
                            int b = session.update("NeoMapper4050.deleteCO1003", supplyledger);
                            session.commit();
                            logger.info( "delete[" + b + " 중복 파일 발생. 이전 데이터 삭제 완료 ]" );
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }
                    }
                }
                /******************************************************************************/

                int iTrscSeq = 1;
                byte[] buffer=new byte[470];   // CO1003
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11") && slforgGb.equals("G")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1003") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           logger.debug( "#### Heaer 처리 시작" );
                           logger.info(paramMap);

//                           supplyledger.setString( "업무구분"            , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"            , paramMap.getString( "일련번호"));
                           supplyledger.setString( "송신기관코드"        , paramMap.getString( "송신기관코드"));
                           supplyledger.setString( "수신기관코드"        , paramMap.getString( "수신기관코드"));
                           supplyledger.setString( "총DataRecord수"    , paramMap.getString( "총DataRecord수"));
                           supplyledger.setString( "전달일"            , paramMap.getString( "전달일"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));

                           paramMap.clear();
                           logger.debug( "#### Heaer 처리 종료" );
                       } else if (DataGubun.equals("22") && slforgGb.equals("G")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1003", "MDATA");
                           logger.info(paramMap);

//                         supplyledger.setString( "업무구분"        , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"        , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));
                           supplyledger.setString( "구분코드"        , paramMap.getString( "구분코드"));
                           supplyledger.setString( "시스템구분코드"    , paramMap.getString( "시스템구분코드"));
                           supplyledger.setString( "전송일시"        , paramMap.getString( "전송일시"));
                           supplyledger.setString( "이용기관지로번호"    , paramMap.getString( "이용기관지로번호"));
                           supplyledger.setString( "지방자치단체코드"    , paramMap.getString( "지방자치단체코드"));
                           supplyledger.setString( "지방자치단체명"    , paramMap.getString( "지방자치단체명"));
                           supplyledger.setString( "사용자건수"        , paramMap.getString( "사용자건수"));
                           supplyledger.setString( "FILLER2"    , paramMap.getString( "FILLER2"));

                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1003", "DDATA");
                           logger.info(paramMap);
                           supplyledger.setString( "업무구분"      , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"      , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"      , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"      , paramMap.getString( "FILLER"));
                           supplyledger.setString( "순번"          , paramMap.getString( "순번"));
                           supplyledger.setString( "금융기관코드" , paramMap.getString( "금융기관코드"));
                           supplyledger.setString( "사용자ID"  , paramMap.getString( "사용자ID"));
                           supplyledger.setString( "사용자명" , paramMap.getString( "사용자명"));
                           supplyledger.setString( "소속과코드"  , paramMap.getString( "소속과코드"));
                           supplyledger.setString( "소속팀계코드" , paramMap.getString( "소속팀계코드"));
                           supplyledger.setString( "시스템권한구분"  , paramMap.getString( "시스템권한구분"));
                           supplyledger.setString( "사용자HASH값"  , paramMap.getString( "사용자HASH값"));
                           supplyledger.setString( "FILLER2"  , paramMap.getString( "FILLER2"));
                           if  ( slforgGb.equals("G")) {
                        	   try {
                        		   int a = session.insert("NeoMapper4050.insertCO1003", supplyledger );
                        		   logger.info("insert[" + a + "]");

                        	   } catch (Exception ex) {
                        		   logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
                        	   }
                           }else if ( slforgGb.equals("D")) {
	                           try {

	                        	   String depcode = supplyledger.getString("소속과코드").substring(0,3);
	                        	   logger.info("depdep : " + depcode);
	                        	   int a = session.delete("NeoMapper4050.deleteComuserm", supplyledger );
	                        	   logger.info("delete[" + a + "]");
	                        	   int b = session.delete("NeoMapper4050.deleteIntusrpm", supplyledger );
	                        	   logger.info("delete[" + b + "]");
	                        	   if(depcode.equals("627") || depcode.equals("341")|| depcode.equals("342") || depcode.equals("343")|| depcode.equals("344")|| depcode.equals("345")|| depcode.equals("346")|| depcode.equals("347")) {

	                               logger.info("---------------------------- DETAIL(DB) START --------------------------");

	                               int c = session.insert("NeoMapper4050.insertComuserm", supplyledger );
	                               logger.info("insert[" + c + "]");
	                               int d = session.insert("NeoMapper4050.insertIntusrpm", supplyledger );
	                               logger.info("insert[" + d + "]");
	                               }
	                           } catch (Exception ex) {
	                               logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
	                           }
                           }
                           paramMap.clear();

                           logger.debug( "#### DETAIL 처리 종료" );
                       }

                       if(slforgGb.equals("D")) {
                           insertTranRecpLog( iTrscSeq, str );
                       }
                       if(iTrscSeq % 500 == 0) {
                    	   session.commit();
                       }
                       iTrscSeq++;
                    }
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        supplyledger.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            } else if ( recv_file_list[i].substring(0,6).equals("CO1004")) {
                // 사용자 정보
                logger.info( "파일명 [" + recv_file_list[i] + "]");
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


                /******************************************************************************
                 * 일시 : 2023.09.14 (PM 박영진과장 요청 추가)
                 * 내용 : 같은 파일명으로 데이터가 들어올 경우는 없다고 본다 하지만, 들어올 경우 이전 파일 통해 등록된 DB 데이터 삭제 하고 새로 등록 하게 하자
                 *****************************************************************************/
                if(slforgGb.equals("G")) {
                    supplyledger.setString( "파일명" , file_nm[0]);  // 파일명으로만 체크해서 확인 함.
                    logger.info("**************** 파일명 [" + supplyledger.getString( "파일명") + "] ****************");

                    int prcCnt = (Integer) session.selectOne("NeoMapper4050.getDataCntCO1004", supplyledger );

                    if (prcCnt > 0) {
                        try {
                            logger.error("## 중복파일명 [" + file_nm[0] + "] 중복 파일 발생. 이전 데이터 삭제 진행 시작");

                            // 중복된 파일이 들어 왔다는 것은 이전 파일에 대한 변경된 데이터가 왔다고 판단해서 삭제하고, 나중에 들어온 파일로 다시 데이터 적재
                            int b = session.update("NeoMapper4050.deleteCO1004", supplyledger);
                            session.commit();
                            logger.info( "delete[" + b + " 중복 파일 발생. 이전 데이터 삭제 완료 ]" );
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }
                    }
                }
                /******************************************************************************/
                int iTrscSeq = 1;
                byte[] buffer=new byte[180];   // CO1004
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11") && slforgGb.equals("G")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1004") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           logger.debug( "#### Heaer 처리 시작" );
                           logger.info(paramMap);

//                           supplyledger.setString( "업무구분"            , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"            , paramMap.getString( "일련번호"));
                           supplyledger.setString( "송신기관코드"        , paramMap.getString( "송신기관코드"));
                           supplyledger.setString( "수신기관코드"        , paramMap.getString( "수신기관코드"));
                           supplyledger.setString( "총DataRecord수"    , paramMap.getString( "총DataRecord수"));
                           supplyledger.setString( "전달일"            , paramMap.getString( "전달일"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));

                           paramMap.clear();
                           logger.debug( "#### Heaer 처리 종료" );
                       } else if (DataGubun.equals("22") && slforgGb.equals("G")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1004", "MDATA");
                           logger.info(paramMap);

//                         supplyledger.setString( "업무구분"        , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"        , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));
                           supplyledger.setString( "구분코드"        , paramMap.getString( "구분코드"));
                           supplyledger.setString( "시스템구분코드"    , paramMap.getString( "시스템구분코드"));
                           supplyledger.setString( "전송일시"        , paramMap.getString( "전송일시"));
                           supplyledger.setString( "이용기관지로번호"    , paramMap.getString( "이용기관지로번호"));
                           supplyledger.setString( "지방자치단체코드"    , paramMap.getString( "지방자치단체코드"));
                           supplyledger.setString( "지방자치단체명"    , paramMap.getString( "지방자치단체명"));
                           supplyledger.setString( "부서과건수"        , paramMap.getString( "부서과건수"));
                           supplyledger.setString( "FILLER2"    , paramMap.getString( "FILLER2"));

                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       }
                       else if (DataGubun.equals("33") && slforgGb.equals("G")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1004", "DDATA");
                           logger.info(paramMap);
                           supplyledger.setString( "업무구분"      , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"      , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"      , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"      , paramMap.getString( "FILLER"));
                           supplyledger.setString( "순번"          , paramMap.getString( "순번"));
                           supplyledger.setString( "부서코드과"  , paramMap.getString( "부서코드과"));
                           supplyledger.setString( "부서명"     , paramMap.getString( "부서명"));
                           supplyledger.setString( "FILLER2"  , paramMap.getString( "FILLER2"));

                           try {
                               logger.info("---------------------------- DETAIL(DB) START --------------------------");
                               int a = session.insert("NeoMapper4050.insertCO1004", supplyledger );
                               logger.info("insert[" + a + "]");
                           } catch (Exception ex) {
                               logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
                           }
                           paramMap.clear();

                           logger.debug( "#### DETAIL 처리 종료" );
                       }

                       if(slforgGb.equals("D")) {
                           insertTranRecpLog( iTrscSeq, str );
                       }
                       if(iTrscSeq % 500 == 0) {
                           session.commit();
                       }
                       iTrscSeq++;
                    }
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        supplyledger.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            } else if ( recv_file_list[i].substring(0,6).equals("CO1005")) {
                // 사용자 정보
                logger.info( "파일명 [" + recv_file_list[i] + "]");
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


                /******************************************************************************
                 * 일시 : 2023.09.14 (PM 박영진과장 요청 추가)
                 * 내용 : 같은 파일명으로 데이터가 들어올 경우는 없다고 본다 하지만, 들어올 경우 이전 파일 통해 등록된 DB 데이터 삭제 하고 새로 등록 하게 하자
                 *****************************************************************************/
                if(slforgGb.equals("G")) {
                    supplyledger.setString( "파일명" , file_nm[0]);  // 파일명으로만 체크해서 확인 함.
                    logger.info("**************** 파일명 [" + supplyledger.getString( "파일명") + "] ****************");

                    int prcCnt = (Integer) session.selectOne("NeoMapper4050.getDataCntCO1005", supplyledger );

                    if (prcCnt > 0) {
                        try {
                            logger.error("## 중복파일명 [" + file_nm[0] + "] 중복 파일 발생. 이전 데이터 삭제 진행 시작");

                            // 중복된 파일이 들어 왔다는 것은 이전 파일에 대한 변경된 데이터가 왔다고 판단해서 삭제하고, 나중에 들어온 파일로 다시 데이터 적재
                            int b = session.update("NeoMapper4050.deleteCO1005", supplyledger);
                            session.commit();
                            logger.info( "delete[" + b + " 중복 파일 발생. 이전 데이터 삭제 완료 ]" );
                        } catch (Exception ex) {
                            logger.error("원인[" + ex.getLocalizedMessage() + "]");
                        }
                    }
                }
                /******************************************************************************/
                int iTrscSeq = 1;
                byte[] buffer=new byte[150];   // CO1005
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11") && slforgGb.equals("G")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1005") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           logger.debug( "#### Heaer 처리 시작" );
                           logger.info(paramMap);

//                           supplyledger.setString( "업무구분"            , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"            , paramMap.getString( "일련번호"));
                           supplyledger.setString( "송신기관코드"        , paramMap.getString( "송신기관코드"));
                           supplyledger.setString( "수신기관코드"        , paramMap.getString( "수신기관코드"));
                           supplyledger.setString( "총DataRecord수"    , paramMap.getString( "총DataRecord수"));
                           supplyledger.setString( "전달일"            , paramMap.getString( "전달일"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));

                           paramMap.clear();
                           logger.debug( "#### Heaer 처리 종료" );
                       } else if (DataGubun.equals("22") && slforgGb.equals("G")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1005", "MDATA");
                           logger.info(paramMap);

//                         supplyledger.setString( "업무구분"        , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"        , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"        , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"        , paramMap.getString( "FILLER"));
                           supplyledger.setString( "구분코드"        , paramMap.getString( "구분코드"));
                           supplyledger.setString( "시스템구분코드"    , paramMap.getString( "시스템구분코드"));
                           supplyledger.setString( "전송일시"        , paramMap.getString( "전송일시"));
                           supplyledger.setString( "이용기관지로번호"    , paramMap.getString( "이용기관지로번호"));
                           supplyledger.setString( "지방자치단체코드"    , paramMap.getString( "지방자치단체코드"));
                           supplyledger.setString( "지방자치단체명"    , paramMap.getString( "지방자치단체명"));
                           supplyledger.setString( "부서계팀건수"    , paramMap.getString( "부서계팀건수"));
                           supplyledger.setString( "FILLER2"    , paramMap.getString( "FILLER2"));

                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       } else if (DataGubun.equals("33") && slforgGb.equals("G")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "1005", "DDATA");
                           logger.info(paramMap);
                           supplyledger.setString( "업무구분"       , paramMap.getString( "업무구분"));
                           supplyledger.setString( "데이터구분"       , paramMap.getString( "데이터구분"));
                           supplyledger.setString( "일련번호"       , paramMap.getString( "일련번호"));
                           supplyledger.setString( "FILLER"       , paramMap.getString( "FILLER"));
                           supplyledger.setString( "순번"           , paramMap.getString( "순번"));
                           supplyledger.setString( "부서코드계팀"  , paramMap.getString( "부서코드계팀"));
                           supplyledger.setString( "부서계팀명"    , paramMap.getString( "부서계팀명"));
                           supplyledger.setString( "상위부서과코드" , paramMap.getString( "상위부서과코드"));
                           supplyledger.setString( "소속팀계코드"  , paramMap.getString( "소속팀계코드"));
                           supplyledger.setString( "FILLER2"   , paramMap.getString( "FILLER2"));

                           try {
                               logger.info("---------------------------- DETAIL(DB) START --------------------------");
                               int a = session.insert("NeoMapper4050.insertCO1005", supplyledger );
                               logger.info("insert[" + a + "]");
                           } catch (Exception ex) {
                               logger.error("INSERT 실패, 원인[" + ex.getLocalizedMessage() + "]", ex);
                           }
                           paramMap.clear();

                           logger.debug( "#### DETAIL 처리 종료" );
                       }

                       if(slforgGb.equals("D")) {
                           insertTranRecpLog( iTrscSeq, str );
                       }
                       if(iTrscSeq % 500 == 0) {
                           session.commit();
                       }
                       iTrscSeq++;
                    }
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        supplyledger.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            }

            // ############### TR2001 전문 처리 START #####################
            if (recv_file_list[i].substring(0,6).equals("TR2001")) {
                logger.info( "파일명 [" + recv_file_list[i] + "]");

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

                byte[] buffer=new byte[850];   // TR2001
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       logger.info(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(0,6);
                       DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2001") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_H", paramMap );
                           paramMap.clear();

                       } else if (DataGubun.equals("22")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2001", "MDATA");

                           /* 22 단위로 순번 생성  */
                           seqMap.clear();
                           String seq = (String) session.selectOne("NeoMapper4050.getTRseq", null);
                           seqMap.setString("seq", seq);  // 징수번호
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2001_M", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2001", "DDATA");

                           /* 22 단위의 순번 생성  */
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2001_D", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("44")) {  // 44
                           fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2001");

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_T", paramMap );
                           paramMap.clear();
                       }
                    }
                    // logger.debug("while 종료");
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        seqMap.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + e.getLocalizedMessage() + "]", e);
                        return 0;
                    }
                 }
            } else if (recv_file_list[i].substring(0,6).equals("TR2002")) {  //2002 551충당
                logger.info( "파일명 [" + recv_file_list[i] + "]");

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

                byte[] buffer=new byte[1070];   // TR2002
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(0,6);
                       DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2002") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_H", paramMap );
                           paramMap.clear();

                       } else if (DataGubun.equals("22")) {  // 22
                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2002", "MDATA");

                           /* 22 단위로 순번 생성  */
                           seqMap.clear();
                           String seq = (String) session.selectOne("NeoMapper4050.getTRseq", null);
                           seqMap.setString("seq", seq);  // 징수번호
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2002_M", paramMap );
                           paramMap.clear();

                           logger.debug( "#### MAIN 처리 종료" );
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2002", "DDATA");

                           /* 22 단위의 순번 생성  */
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2002_D", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("44")) {  // 44
                           logger.debug( "#### TAIL 처리 시작" );
                           fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2002");

                           paramMap.setString("파일명", file_nm[0]);  // 파일명 저장
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_T", paramMap );
                           paramMap.clear();
                       }
                    }
                    // logger.debug("while 종료");
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        seqMap.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            } else if (recv_file_list[i].substring(0,6).equals("TR2003")) { // 2003 543 지급
                logger.info( "파일명 [" + recv_file_list[i] + "]");

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

                byte[] buffer=new byte[950];   // TR2003
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );


                       if (DataGubun.equals("11")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2003") < 0) {
                                   logger.error("헤더부분 파싱 오류");

                                   logger.info(paramMap);

                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           paramMap.setString("파일명", file_nm[0]);
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_H", paramMap );
                           paramMap.clear();

                       } else if (DataGubun.equals("22")) {  // 22

                           seqMap.clear();
                           String seq = (String) session.selectOne("NeoMapper4050.getTRseq", null);
                           seqMap.setString("seq", seq);  // 징수번호

                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2003", "MDATA");
                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2003_M", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2003", "DDATA");

                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2003_D", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("44")) {  // 44
                           logger.debug( "#### TAIL 처리 시작" );
                           fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2003");
                           paramMap.setString("파일명", file_nm[0]);

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_T", paramMap );
                           paramMap.clear();
                       }
                    }
                    // logger.debug("while 종료");
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        seqMap.clear();
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
             // ############### TR2003 전문 처리 END   #####################
            } else if (recv_file_list[i].substring(0,6).equals("TR2004")) {
             // ############### TR2004 전문 처리 START #####################
                logger.info( "파일명 [" + recv_file_list[i] + "]");

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

                byte[] buffer=new byte[540];   // TR2004
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(0,6);
                       DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2004") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           paramMap.setString("파일명", file_nm[0]);
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_H", paramMap );
                           paramMap.clear();
                       } else if (DataGubun.equals("22")) {

                           seqMap.clear();
                           String seq = (String) session.selectOne("NeoMapper4050.getTRseq", null);
                           seqMap.setString("seq", seq);  // 징수번호

                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2004", "MDATA");
                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2004_M", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2004", "DDATA");
                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2004_D", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("44")) {  // 44
                           logger.debug( "#### TAIL 처리 시작" );
                           fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2004");
                           paramMap.setString("파일명", file_nm[0]);

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_T", paramMap );
                           paramMap.clear();
                       }
                    }
                    // logger.debug("while 종료");
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
             // ############### TR2004 전문 처리 END   #####################
            } else if (recv_file_list[i].substring(0,6).equals("TR2005")) {
             // ############### TR2005 전문 처리 START #####################
                logger.info( "파일명 [" + recv_file_list[i] + "]");

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

                byte[] buffer=new byte[500];   // TR2005
                try {
                    bis = new BufferedInputStream(fis);
                    while(true) {
                        // 한줄로 들어오는 전문을 데이터구분별로 자르면서 저장한다.
                       int readedByte=bis.read(buffer);
                       if(readedByte == -1) break;
                       System.out.println(new String(buffer,0,readedByte,"euc-kr"));
                       str = new String(buffer,0,readedByte,"euc-kr");
                       String  DataGubun = str.substring(0,6);
                       DataGubun = str.substring(6,8);  // 11,22,33,44
                       //logger.debug( "DataGubun [" + DataGubun + "]" );

                       if (DataGubun.equals("11")) {  // 11
                           try {
                               if (fp.disassembleMessageByteHeadAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2005") < 0) {
                                   logger.error("헤더부분 파싱 오류");
                                   continue;
                               }
                           } catch (UnsupportedEncodingException e1) {
                               // TODO Auto-generated catch block
                               logger.error("파일을 처리할수  없습니다. 내용[" + e1.getLocalizedMessage() + "]");
                               continue;
                           }

                           paramMap.setString("파일명", file_nm[0]);
                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_H", paramMap );
                           paramMap.clear();

                       } else if (DataGubun.equals("22")) {  // 22

                           seqMap.clear();
                           String seq = (String) session.selectOne("NeoMapper4050.getTRseq", null);
                           seqMap.setString("seq", seq);  // 징수번호

                           logger.debug( "#### MAIN 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2005", "MDATA");
                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2005_M", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("33")) {  // 33
                           logger.debug( "#### DETAIL 처리 시작" );
                           fp.disassembleFileByteBodyAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2005", "DDATA");
                           paramMap.setString("파일명", file_nm[0]);
                           paramMap.setString("seq", seqMap.getString( "seq" ));

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR2005_D", paramMap );
                           paramMap.clear();
                       }
                       else if (DataGubun.equals("44")) {  // 44
                           logger.debug( "#### TAIL 처리 시작" );
                           fp.disassembleMessageByteTailAsMap(str.getBytes("euckr"), str.getBytes("euckr").length, paramMap, "2005");
                           paramMap.setString("파일명", file_nm[0]);

                           logger.info(paramMap);
                           int a = session.insert("NeoMapper4050.insertTR_T", paramMap );
                           paramMap.clear();
                       }
                    }
                    // logger.debug("while 종료");
                 } catch(Exception e) {
                    logger.error(e);
                 } finally {
                    try {
                        session.commit();
                        logger.debug( "[ back_file_start  ]");
                        work_file.renameTo(back_file );
                        ok_file.delete();
                        if(fis!=null)fis.close();
                        if(bis!=null)bis.close();
                        if(isr!=null)isr.close();
                        logger.debug( "[ back_file_end  ]");
                    } catch(Exception e) {
                        // TODO: handle exception
                        session.rollback();
                        logger.error("작업에러!!!!! : [" + supplyledger.getString( "지급명령등록번호" ) + "]", e);
                        return 0;
                    }
                 }
            }
        }// end of for statement

        logger.debug("mainLoop 작업 완료!!!");
        session.close();
        return 0;
    }

    /**
     * 트랜로그(TRAN_RECP_TAB) 등록 <br/>
     *
     * @param iTrscSeq - 파일 라인별 번호
     * @param readLine - 파일 라인 문자열
     * @throws Exception
     */
    private void insertTranRecpLog( int iTrscSeq, String readLine ) throws Exception  {

        String trscSeq = Utils.extendData(10, '9', iTrscSeq+"");
        String DataGubun = readLine.substring(6,8);
        String trscFlog = null;

        if(DataGubun.equals("11")) {
            trscFlog = "H";
        } else if(DataGubun.equals("22")) {
            trscFlog = "M";
        } else if(DataGubun.equals("33")) {
            trscFlog = "D";
        } else if(DataGubun.equals("44")) {
            trscFlog = "T";
        } else {
            trscFlog = "";
        }

        MyMap param = new MyMap();

        param.setMap("TRSC_SEQ",  trscSeq);           // 전송일련번호
        param.setMap("TRSC_FLAG",  trscFlog);         // 구분코드
        param.setMap("INPUT_TLGM",  readLine);        // 입력전문

        logger.info("param Log = ["+param+"]");

        session.insert( "NeoMapper4050.insertTranRecpLog", param );

        logger.info("#### insert TRAN_RECP_TAB OK ####");

    }
}
