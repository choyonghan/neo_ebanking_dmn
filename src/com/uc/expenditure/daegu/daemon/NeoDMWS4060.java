/**
 *  주시스템명 : 과오납
 *  업  무  명 : 과오납원장생성
 *  기  능  명 : 과오납원장생성
 *  클래스  ID : NeoDMWS4060
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  박상현       다산(주)     2023.10.18         %01%             최초작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.Utils;

public class NeoDMWS4060 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger   logger = Logger.getLogger(NeoDMWS4060.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static Thread   self = null;
    static MyMap    appRes = new MyMap();

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS4060.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS4060.class.getSimpleName() + "] 시작 #####");

        NeoDMWS4060  NeoDMWS4060 = new NeoDMWS4060();

        self = new Thread(NeoDMWS4060);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("##### [" + NeoDMWS4060.class.getSimpleName() + "] 작업 종료 #####");
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
        Utils.getResources("conf/ApplicationResources", appRes);

        while (!Thread.currentThread().isInterrupted()) {

            mainLoop(); // 공통
            try {
                Thread.sleep(30000); //30000
            } catch (InterruptedException e) {
                logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 종료");
                break;
            }
        }
    }

    public int mainLoop() {
        // TODO Auto-generated method stub
        try {
            session = sqlMapper.openSession(ExecutorType.BATCH,false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        // TR2001 - 548 조회
        TR2001_548_MAKE();
        TR2003_543_MAKE();
        TR2004_543_MAKE();
        TR2005_543_MAKE();
        logger.debug("###################### 마스트/디테일 만들기 시작 ######################");
        TR2002_551_553_MAKE();
        logger.debug("###################### 마스트/디테일 만들기 끝 ######################");

        session.close();
        return 0;
    }

    @SuppressWarnings("unchecked")
    private int TR2002_551_553_MAKE() {
        // TODO Auto-generated method stub
        try {
            /* 로직 START*/
            ArrayList<MyMap> TR2002SelectList = null;
            MyMap TR2002Map = new MyMap();
            TR2002Map.setString("STATUS_CODE", "00");
            TR2002Map.setString("업무구분", "TR2002");
            // 공통 - 업무구분으로 파일명을 조회 하는 쿼리
            TR2002SelectList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNameTR", TR2002Map );

            logger.debug("TR2002SelectList : "+ TR2002SelectList);
            if (TR2002SelectList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }
            for(int m=0; m < TR2002SelectList.size(); m++) {
                MyMap TR2002Select = TR2002SelectList.get(m);

                /* SEQ 조회 */
                ArrayList<MyMap> TR2002FileNumberList = null;
                TR2002FileNumberList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNumberTR2002M", TR2002Select );

                logger.debug("TR2002FileNumberList : "+ TR2002FileNumberList);
                if (TR2002FileNumberList.size() <= 0) {   // 조회된 값이 없으면 탈출
                    return 0;
                }

                for(int j = 0; j < TR2002FileNumberList.size(); j++) {
                    MyMap TR2002FileNumber = TR2002FileNumberList.get(j);
                    ArrayList<MyMap> arrTR551MapList = null;

                    arrTR551MapList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getSelectTR2002", TR2002FileNumber );

                    if (arrTR551MapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                        return 0;
                    }

                    for(int k = 0; k < arrTR551MapList.size(); k++) {
                        MyMap TR551Map = arrTR551MapList.get(k);

                        // 광역,시군구,국 계산
                        long TOTAL_AMT = 0;
                        long DO_AMT = 0;
                        long SI_AMT = 0;
                        long KU_AMT = 0;

                        DO_AMT = TR551Map.getLong("광역시도세세입금액_D") + TR551Map.getLong("광역시도세세가산금_D");
                        SI_AMT = TR551Map.getLong("시군구세세입금액_D") + TR551Map.getLong("시군구세가산금_D");
                        KU_AMT = TR551Map.getLong("국세세입금액_D") + TR551Map.getLong("국세가산금_D");
                        TOTAL_AMT = DO_AMT + SI_AMT + KU_AMT;

                        /* 현과년도구분 */
                        long h_amt = TR551Map.getLong("광역시도세현년도출금금액_M")+TR551Map.getLong("시군구세현년도출금금액_M")+TR551Map.getLong("국세현년도출금금액_M");
                        long g_amt = TR551Map.getLong("광역시도세과년도출금금액_M")+TR551Map.getLong("시군구세과년도출금금액_M")+TR551Map.getLong("국세과년도출금금액_M");
                        if (h_amt > 0) { // 현년도
                            TR551Map.setString("현과년도구분_M", "1");
                        } else { // 과년도
                            TR551Map.setString("현과년도구분_M", "2");
                        }

                        if (TR551Map.getString("입출금지시거래구분코드_M").equals("2001")) {
                            if (!TR551Map.getString("세입금충당OCR밴드입금부_D").equals("")) {
                                // OCR값이 있으면 실행 START
                                // OCR벤드 분리
                                String ocr_line1 = TR551Map.getString("세입금충당OCR밴드입금부_D").substring(0,54);
                                String ocr_line2 = TR551Map.getString("세입금충당OCR밴드입금부_D").substring(54,108);
                                TR551Map.setString( "OCR_Line1", ocr_line1);
                                TR551Map.setString( "OCR_Line2", ocr_line2);
                                // OCR 처리
                                MyMap hm    = new MyMap();
                                hm.setString("시도"         ,ocr_line1.substring(0,2));
                                hm.setString("시군구"       ,ocr_line1.substring(2,5));
                                hm.setString("검1"          ,ocr_line1.substring(5,6));
                                hm.setString("회계"     ,ocr_line1.substring(6,8));
                                hm.setString("과목"         ,ocr_line1.substring(8,11));
                                hm.setString("세목"         ,ocr_line1.substring(11,14));
                                hm.setString("부과연도"     ,ocr_line1.substring(14,18));
                                hm.setString("월"           ,ocr_line1.substring(18,20));
                                hm.setString("기분"         ,ocr_line1.substring(20,21));
                                hm.setString("행정동"       ,ocr_line1.substring(21,24));
                                hm.setString("과세번호"     ,ocr_line1.substring(24,30));
                                hm.setString("검2"          ,ocr_line1.substring(30,31));
                                hm.setString("납기내총액"   ,ocr_line1.substring(31,42));
                                hm.setString("납기후총댁"   ,ocr_line1.substring(42,53));
                                hm.setString("검3"          ,ocr_line1.substring(53,54));

                                hm.setString("본세"         ,ocr_line2.substring(0,11));
                                hm.setString("도시계획세"   ,ocr_line2.substring(11,21));
                                hm.setString("소방_농특세"  ,ocr_line2.substring(21,31));
                                hm.setString("지방교육세"   ,ocr_line2.substring(31,42));
                                hm.setString("납기일"       ,ocr_line2.substring(42,50));
                                hm.setString("검4"          ,ocr_line2.substring(50,51));
                                hm.setString("fill"         ,ocr_line2.substring(51,52));
                                hm.setString("수납구분"     ,ocr_line2.substring(52,53));
                                hm.setString("검5"          ,ocr_line2.substring(53,54));

                                /* 회계연도 */
                                String etaxAcctYy = "";

                                //기본
                                String gibunCode = hm.getString("기분").toString();

                                if( gibunCode.equals("1") || gibunCode.equals("2") ){
                                    etaxAcctYy = hm.getString("부과연도").toString();
                                }else if( gibunCode.equals("3") ){
                                    etaxAcctYy = TR551Map.getString("회계일자_M").substring(0,4);   // 회계일자
                                }
                                TR551Map.setString( "ETAX_ACCT_YY_D", etaxAcctYy);  //회계연도

                                //회계코드
                                String acctCode = hm.getString("회계");

                                //과목,세목,과목+세목
                                String gCode  = hm.getString("과목");
                                String sCode  = hm.getString("세목");
                                String gsCode = gCode + sCode;

                                //부과연도, 부가월, 부가연월
                                String bgYear       = hm.getString("부과연도");
                                String bgMonth      = hm.getString("월");
                                String bgDate       = bgYear + bgMonth;
                                long bgDateInt       = Long.parseLong(bgDate);

                                long bonse_amt       = hm.getLong("본세");
                                long dosi_amt        = hm.getLong("도시계획세");
                                long sobang_nong_amt = hm.getLong("소방_농특세");
                                long edu_amt         = hm.getLong("지방교육세");

                                long si_amt = 0;        //시세
                                long gu_amt = 0;        //구세
                                long kuk_amt = 0;       //국세

                                long total_amt       = hm.getLong("납기내총액");

                                if( acctCode.equals("30") ){    //시세일 경우

                                    si_amt = bonse_amt + dosi_amt;      //시세 = 본세 + 도시계획세

                                    //과목이 105:재산세일 경우
                                    if( gCode.equals("105") ){
                                        si_amt = si_amt + sobang_nong_amt;  //시세 = 시세 + 공동시설세
                                    }else{
                                        kuk_amt = sobang_nong_amt;          //국세 = 농특세
                                    }

                                    //지방교육세 교육세 체크 - 지방교육세 기준일 2001.01.01
                                    if( bgDateInt < 200101 ){
                                        logger.info("\t[국고 교육세적용]");
                                        kuk_amt = kuk_amt + edu_amt;        //국세 = 국세 + 국고 교육세
                                    }else{
                                        si_amt = si_amt + edu_amt;          //시세 = 시세 + 지방 교육세
                                    }

                                }else if( acctCode.equals("40") ){      //구세일 경우
                                    gu_amt = bonse_amt ;
                                    si_amt = dosi_amt;

                                    //과목이 105:재산세일 경우
                                    if( gCode.equals("105") ){
                                        si_amt = si_amt + sobang_nong_amt;  //시세 = 시세 + 공동시설세
                                    }else{
                                        kuk_amt = sobang_nong_amt;          //국세 = 농특세
                                    }

                                    //지방교육세 교육세 체크 - 지방교육세 기준일 2001.01.01
                                    if( bgDateInt < 200101 ){
                                        logger.info("\t[국고 교육세적용]");
                                        kuk_amt = kuk_amt + edu_amt;
                                    }else{
                                        si_amt = si_amt + edu_amt;
                                    }

                                }else{
                                    logger.info("[ 회계코드가 정상적이지 않습니다. ]");
                                    si_amt = 0;        //시세
                                    gu_amt = 0;        //구세
                                    kuk_amt = 0;       //국세
        //                            error_result_code = "019";
        //                            error_result_desc = "회계코드가 정상적이지 않습니다.";
        //                            error_time = TimeUtil.getFulltime();
        //                            error_status_code = Constants.ETAX_BIZ_RECV_FILE_BANK_ETAX_UPDATE;
                                }

                                TR551Map.setString( "ETAX_SI_AMT", si_amt);  //시세
                                TR551Map.setString( "ETAX_GU_AMT", gu_amt);   //구세
                                TR551Map.setString( "ETAX_KUK_AMT", kuk_amt);  //국세
                                // OCR값이 있으면 실행 END
                            } else {
                                // OCR 값이 없으면 실행
                                TR551Map.setString( "ETAX_SI_AMT", DO_AMT);  //시세
                                TR551Map.setString( "ETAX_GU_AMT", SI_AMT);   //구세
                                TR551Map.setString( "ETAX_KUK_AMT", KU_AMT);  //국세
                            }

                            /* 개발 기간에만 사용  - OCR 데이터 err로 인해 */
                            TR551Map.setString( "ETAX_SI_AMT", DO_AMT);  //시세
                            TR551Map.setString( "ETAX_GU_AMT", SI_AMT);   //구세
                            TR551Map.setString( "ETAX_KUK_AMT", KU_AMT);  //국세

                            TR551Map.setString("OUT_AMT", TOTAL_AMT);
                            TR551Map.setString("OUT_DO_AMT", DO_AMT);
                            TR551Map.setString("OUT_SI_AMT", SI_AMT);
                            TR551Map.setString("OUT_KUK_AMT", KU_AMT);

                            // 551 테이블에 저장 ( 2001:환급금충당 )
                            logger.info("NeoMapper4060.insertTR2002_551 : " + TR551Map);
                            session.insert("NeoMapper4060.insertTR2002_551", TR551Map );
                        } else {

                            TR551Map.setString("SUNAP_AMT", TOTAL_AMT);
                            TR551Map.setString("SUNAP_DO_AMT", DO_AMT);
                            TR551Map.setString("SUNAP_SI_AMT", SI_AMT);
                            TR551Map.setString("SUNAP_KUK_AMT", KU_AMT);

                            TR551Map.setString( "ETAX_SI_AMT", DO_AMT);  //시세
                            TR551Map.setString( "ETAX_GU_AMT", SI_AMT);   //구세
                            TR551Map.setString( "ETAX_KUK_AMT", KU_AMT);  //국세

                            // 553 테이블에 저장 ( 2002~6 )
                            logger.info("NeoMapper4060.insertTR2002_553 : " + TR551Map);
                            session.insert("NeoMapper4060.insertTR2002_553", TR551Map );
                        }
                    }
                }
                // 헤더 상태값 업데이트 로직 추가..
                session.update("NeoMapper4060.updateTRHeader", TR2002Select);
            }
            /* 로직 END*/
            session.commit();
            return 0;
        } catch(Exception e) {
            // TODO: handle exception
            logger.error("에러메세지 : " + e);
            session.rollback();
            return -1;
        }

    }

    @SuppressWarnings("unchecked")
    private int TR2003_543_MAKE() {

        try {
            /* 로직 START*/
            ArrayList<MyMap> TR2003SelectList = null;
            MyMap TR2003Map = new MyMap();
            TR2003Map.setString("STATUS_CODE", "00");
            TR2003Map.setString("업무구분", "TR2003");
            TR2003SelectList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNameTR", TR2003Map );

            logger.debug("TR2003SelectList : "+ TR2003SelectList);
            if (TR2003SelectList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }
            for(int m = 0; m < TR2003SelectList.size(); m++) {
                MyMap TR2003Select = TR2003SelectList.get(m);

                ArrayList<MyMap> TR2003FileNumberList = null;
                TR2003FileNumberList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNumberTR2003M", TR2003Select );

                logger.debug("TR2003FileNumberList : "+ TR2003FileNumberList);
                if (TR2003FileNumberList.size() <= 0) {   // 조회된 값이 없으면 탈출
                    return 0;
                }

                for(int j = 0; j < TR2003FileNumberList.size(); j++) {
                    MyMap TR2003FileNumber = TR2003FileNumberList.get(j);
                    ArrayList<MyMap> arrTR543MapList = null;
                    arrTR543MapList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.get543InsertData", TR2003FileNumber );

                    if (arrTR543MapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                        return 0;
                    }

                    for(int k = 0; k < arrTR543MapList.size(); k++) {
                        MyMap TR543Map = arrTR543MapList.get(k);

                        int a = session.insert("NeoMapper4060.insertTR2003", TR543Map );
                        logger.info("NeoMapper4060.insertTR2003 : " + TR543Map);

                    }

                }
                // 헤더 상태값 업데이트 로직 추가..
                session.update("NeoMapper4060.updateTRHeader", TR2003Select);
            }
            /* 로직 END*/
            session.commit();
            return 0;
        } catch(Exception e) {
            // TODO: handle exception
            logger.error("에러메세지 : " + e);
            session.rollback();
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    private int TR2004_543_MAKE() {

        try {
            /* 로직 START*/
            /* TR2004 START*/
            ArrayList<MyMap> TR2004SelectList = null;
            MyMap TR2004Map = new MyMap();
            TR2004Map.setString("STATUS_CODE", "00");
            TR2004Map.setString("업무구분", "TR2004");
            TR2004SelectList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNameTR", TR2004Map );

            logger.debug("TR2004SelectList : "+ TR2004SelectList);
            if (TR2004SelectList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }
            for(int m = 0; m < TR2004SelectList.size(); m++) {
                MyMap TR2004Select = TR2004SelectList.get(m);

                ArrayList<MyMap> TR2004FileNumberList = null;
                TR2004FileNumberList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNumberTR2004M", TR2004Select );

                logger.debug("TR2004FileNumberList : "+ TR2004FileNumberList);
                if (TR2004FileNumberList.size() <= 0) {   // 조회된 값이 없으면 탈출
                    return 0;
                }

                for(int j = 0; j < TR2004FileNumberList.size(); j++) {
                    MyMap TR2004FileNumber = TR2004FileNumberList.get(j);
                    ArrayList<MyMap> arrTR543MapList2004 = null;
                    arrTR543MapList2004 = (ArrayList<MyMap>) session.selectList("NeoMapper4060.get543InsertData2004", TR2004FileNumber );

                    if (arrTR543MapList2004.size() <= 0) {   // 조회된 값이 없으면 탈출
                        return 0;
                    }

                    for(int k = 0; k < arrTR543MapList2004.size(); k++) {
                        MyMap TR543Map2004 = arrTR543MapList2004.get(k);

                        int a = session.insert("NeoMapper4060.insertTR2004", TR543Map2004 );
                        logger.info("NeoMapper4060.insertTR2004 : "  + TR543Map2004);
                    }
                }
                // 헤더 상태값 업데이트 로직 추가..
                session.update("NeoMapper4060.updateTRHeader", TR2004Select);
            }
            /* 로직 END*/
            session.commit();
            return 0;
        } catch(Exception e) {
            // TODO: handle exception
            session.rollback();
            return -1;
        }

    }

    @SuppressWarnings("unchecked")
    private int TR2005_543_MAKE() {

        try {
            /* 로직 START*/
            /* TR2005 START*/
            ArrayList<MyMap> TR2005SelectList = null;
            MyMap TR2005Map = new MyMap();
            TR2005Map.setString("STATUS_CODE", "00");
            TR2005Map.setString("업무구분", "TR2005");
            TR2005SelectList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNameTR", TR2005Map );

            logger.debug("TR2005SelectList : "+ TR2005SelectList);
            if (TR2005SelectList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }
            for(int m = 0; m < TR2005SelectList.size(); m++) {
                MyMap TR2005Select = TR2005SelectList.get(m);

                ArrayList<MyMap> TR2005FileNumberList = null;
                TR2005FileNumberList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNumberTR2005M", TR2005Select );

                logger.debug("TR2005FileNumberList : "+ TR2005FileNumberList);
                if (TR2005FileNumberList.size() <= 0) {   // 조회된 값이 없으면 탈출
                    return 0;
                }

                for(int j = 0; j < TR2005FileNumberList.size(); j++) {
                    MyMap TR2005FileNumber = TR2005FileNumberList.get(j);
                    ArrayList<MyMap> arrTR543MapList2005 = null;
                    arrTR543MapList2005 = (ArrayList<MyMap>) session.selectList("NeoMapper4060.get543InsertData2005", TR2005FileNumber );

                    if (arrTR543MapList2005.size() <= 0) {   // 조회된 값이 없으면 탈출
                        return 0;
                    }

                    for(int k = 0; k < arrTR543MapList2005.size(); k++) {
                        MyMap TR543Map2005 = arrTR543MapList2005.get(k);

                        int a = session.insert("NeoMapper4060.insertTR2005", TR543Map2005 );
                        logger.info("NeoMapper4060.insertTR2005 : " + TR543Map2005);
                    }
                }
                // 헤더 상태값 업데이트 로직 추가..
                session.update("NeoMapper4060.updateTRHeader", TR2005Select);
            }
            /* 로직 END*/
            session.commit();
            return 0;
        } catch(Exception e) {
            // TODO: handle exception
            session.rollback();
            return -1;
        }

    }

    @SuppressWarnings("unchecked")
    private int TR2001_548_MAKE() {
        // TODO Auto-generated method stub
        /* 혹시, 현년도, 과년도 계좌가 없는데, 전문 변경으로 생기면 현년도/과년도 데이터가 때로 생성될 수 있도록 쿼리에 그룹바이 추가 필요. */
        try {
            /* 로직 START*/
            ArrayList<MyMap> TR2001SelectList = null;
            MyMap TR2001Map = new MyMap();
            TR2001Map.setString("STATUS_CODE", "00");
            TR2001Map.setString("업무구분", "TR2001");
            TR2001SelectList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNameTR", TR2001Map );

            logger.debug("TR2001SelectList : "+ TR2001SelectList);
            if (TR2001SelectList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }
            for(int m=0; m < TR2001SelectList.size(); m++) {
                MyMap TR2001Select = TR2001SelectList.get(m);

                /* SEQ 조회 */
                ArrayList<MyMap> TR2001FileNumberList = null;
                TR2001FileNumberList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getFileNumberTR2001M", TR2001Select );

                logger.debug("TR2001FileNumberList : "+ TR2001FileNumberList);
                if (TR2001FileNumberList.size() <= 0) {   // 조회된 값이 없으면 탈출
                    return 0;
                }

                for(int j = 0; j < TR2001FileNumberList.size(); j++) {
                    MyMap TR2001FileNumber = TR2001FileNumberList.get(j);
                    ArrayList<MyMap> arrTR548MapList = null;
                    arrTR548MapList = (ArrayList<MyMap>) session.selectList("NeoMapper4060.getSelectTR2001", TR2001FileNumber );

                    if (arrTR548MapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                        return 0;
                    }

                    for(int k = 0; k < arrTR548MapList.size(); k++) {
                        MyMap TR548Map = arrTR548MapList.get(k);

                        logger.info("TR548Map : " + TR548Map);

                        long TOTAL_AMT = 0;
                        long DO_AMT = 0;
                        long SI_AMT = 0;
                        long KU_AMT = 0;

                        DO_AMT = TR548Map.getLong("광역시도세세입금액_D") + TR548Map.getLong("광역시도세세가산금_D");
                        SI_AMT  = TR548Map.getLong("시군구세세입금액_D") + TR548Map.getLong("시군구세가산금_D");
                        KU_AMT = TR548Map.getLong("국세세입금액_D") + TR548Map.getLong("국세세입가산금_D");
                        TOTAL_AMT = DO_AMT + SI_AMT + KU_AMT;

                        TR548Map.put("TOTAL_AMT", TOTAL_AMT);
                        TR548Map.put("DO_AMT", DO_AMT);
                        TR548Map.put("SI_AMT", SI_AMT);
                        TR548Map.put("KU_AMT", KU_AMT);

                        session.insert("NeoMapper4060.insertTR2001", TR548Map );
                    }
                }
                // 헤더 상태값 업데이트 로직 추가..
                session.update("NeoMapper4060.updateTRHeader", TR2001Select);
            }
            /* 로직 END*/
            session.commit();
            return 0;
        } catch(Exception e) {
            // TODO: handle exception
            logger.error("에러메세지 : " + e);
            session.rollback();
            return -1;
        }
    }

}
