/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 입금지급 처리
 *  기  능  명 : e호조요청건 e세출업무원장 전송처리
 *  클래스  ID : NeoDMWS3020
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  이경두       다산(주)      2022.08.29         %01%             최초작성
 *  하상원       다산(주)      2022.10.07         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import  java.io.Reader;
import  java.sql.SQLException;
import  java.sql.SQLWarning;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import  java.util.ArrayList;
import java.util.Properties;

import  org.apache.ibatis.io.Resources;
import  org.apache.ibatis.session.SqlSession;
import  org.apache.ibatis.session.SqlSessionFactory;
import  org.apache.ibatis.session.SqlSessionFactoryBuilder;
import  org.apache.log4j.Logger;
import  org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.NeoUtils;
import com.uc.framework.utils.Utils;

public class NeoDMWS3020 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger    logger = Logger.getLogger(NeoDMWS3020.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static Thread self = null;
    boolean bAlreadyConfirm = false;
    boolean bAlreadyConfirm2 = false;
    static MyMap appRes = new MyMap();

    private static String FLAG_Y = "Y";
    private static String FLAG_N = "N";
    private static String FLAG_ALL = "A";

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS3020.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS3020.class.getSimpleName() + "] 시작 #####");

        NeoDMWS3020  neoDMWS3020 = new NeoDMWS3020();

        self = new Thread(neoDMWS3020);
        self.setDaemon(true);
        self.start();

        try {
        	self.join();
        } catch (InterruptedException e) {
        	logger.debug("InterruptedException ::: ["+ e.getMessage() +"]");
        }

        logger.debug("##### [" + NeoDMWS3020.class.getSimpleName() + "] 끝 #####");
    }

    /**
     * 데몬 기본 실행
     */
    public void run()
    {
        logger.info("쓰레드[" + Thread.currentThread().getName() + "] 실행");
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

        logger.info("SQLMAPPER생성");
        Utils.getResources("conf/ApplicationResources", appRes);

        while (!Thread.currentThread().isInterrupted()) {
        	mainLoop(); // 호조자료수신

            try {
                Thread.sleep(30000);	// 30초 대기
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
    	// 전송시간체크
        java.util.Date  s1 = null;
        java.util.Date  s2 = null;
        java.util.Date  s3 = null;

        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sf3 = new SimpleDateFormat("yyyyMMddHHmmss");

        try {
            s1 = sf1.parse(Utils.getCurrent("yyyyMMddHHmmss"));
            s2 = sf2.parse(Utils.getCurrent("yyyyMMdd") + "083000");
            s3 = sf3.parse(Utils.getCurrent("yyyyMMdd") + "233000");

            if(s1.before(s2)) {
                logger.info("업무시간대가 아닙니다");
                logger.info("현재시간 : " + s1);
                logger.info("전송시간 : " + s2);

                return -1;
            }

            if(s1.after(s3)) {
                logger.info("업무시간대가 아닙니다");
                logger.info("현재시간 : " + s1);
                logger.info("전송시간 : " + s3);

                return -1;
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }

        // 데이터베이스 세션 생성, 자동확약 끔.
        try {
            session = sqlMapper.openSession(false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        //  휴일여부체크
        MyMap paramMap = new MyMap();
        paramMap.setString("현재일자", Utils.getCurrentDate());

        logger.info("휴일여부 확인");
        MyMap    holiday = null;

        try {
            holiday = (MyMap)session.selectOne("NeoMapperCommon.getHoliday", paramMap);
        } catch (Exception e) {
            logger.error("영업일 조회중 오류[" + e.getLocalizedMessage() + "]");
            session.close();
            return -1;
        }

        if (holiday != null && holiday.getString("휴일여부").equals("Y")) {
            if (!bAlreadyConfirm) {
                logger.debug("[" + paramMap.getString("현재일자") + "]은 휴일입니다");
                bAlreadyConfirm = true;
            }
            bAlreadyConfirm2 = false;
            session.close();
            return -1;
        } else {
            if (!bAlreadyConfirm2) {
                logger.debug("[" + paramMap.getString("현재일자") + "]은 휴일이 아닙니다");
                bAlreadyConfirm2 = true;
            }

            bAlreadyConfirm = false;
        }

        // 링크 리스트 가져오기 -- 구청별로 작업
        @SuppressWarnings("unchecked")
        ArrayList<MyMap> linkList = (ArrayList<MyMap>)session.selectList("NeoMapperCommon.getLinkList");

        /**
         * [check]
         * 임시로 중구청 3410000 1건만 돌림
         * 전체를 돌려도 문제 없을꺼 같아 그냥 기존 로직으로 처리(2022-08-31)
         */
        for (int i = 0; i < linkList.size(); i++) {
            MyMap    linkMap = linkList.get(i);

            logger.debug("[" + linkMap.getString("구청명") + "] 의 DB를 조회합니다. 점번 [" + linkMap.getString("점번") + "] 링크명 [" + linkMap.getString("링크명") + "]");

            // 구청별로 작업
            try {
                DBTrans(linkMap);
            } catch (Exception ex) {
                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
                session.rollback();
                return -1;
            }
        }

        session.close();
        return -1;
    }

    @SuppressWarnings("unchecked")
    private int DBTrans(MyMap param) throws Exception
    {
        ArrayList<MyMap> mainList = new ArrayList<MyMap>();
        MyMap dateMap = new MyMap();	/*오늘과 영업전일을 체크*/

        try{
            // 구청별로 신규 등록된 데이타가 있는지 확인
            dateMap.setString("LAF_CD", param.getString("자치단체코드")); // 신규 추가
            logger.debug("["+param.getString("구청명")+"]["+param.getString("자치단체코드")+"]자치단체 처리 시작.");

            // 호조 지급원장연계송신(TFE2190S)  조회
            mainList = (ArrayList<MyMap>)session.selectList("NeoMapper3020.selectTFE2190S", dateMap);

            if(mainList.size() <= 0){
                logger.debug("신규 등록된 데이터가 없습니다.");
                session.commit(true);
                return 0;
            }

            logger.info("구청명: " + param.getString("구청명") + ", " + "건수: " + mainList.size()  + "건");

            maintService(param, mainList, dateMap);
        } catch (Exception se) {
        	logger.debug(se);
            logger.error("구청명 [" + param.get("구청명") + "] 작업 중 에러:" +se.getMessage());
            session.rollback(true);
            return -1;
        }

        return 0;
    }

    /**
     * 호조 데이터 처리
     * @param param
     * @param mainList
     * @param dateMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private int maintService(MyMap param, ArrayList<MyMap> mainList, MyMap dateMap) throws Exception
    {
        ArrayList<MyMap> detailList  = null;    // new ArrayList<MyMap>();
        MyMap ht = null;    // new MyMap();

        for (int j = 0; j < mainList.size(); j++) {
            ht = mainList.get(j);


            /******************************************************************************
             * 수정일시 : 2012.06.21
             * 수정자   : 신상훈
             * 수정내용 : 거래일자가 해당일자가 아닌경우 반려처리를 한다.
             *****************************************************************************/
            if (!ht.getString("DLNG_YMD").equals(Utils.getCurrentDate())) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)거래일자를 확인하세요.");

                logger.error("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 거래일자 오류입니다.");
                logger.error("현재일자 [" + Utils.getCurrentDate() + "]");
                logger.error("거래일자 [" + ht.getString("DLNG_YMD") + "]");

                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

                // 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

                // 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                // String trnxStr = "EC";   // 거래번호 채번
                // ht.setString("TRNX_NO",      getTrnxNo(trnxStr));
                ht.setString("TRNX_NO",         getTrnxNo2(ht));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            MyMap checkTotalMap = new MyMap();

            checkTotalMap = (MyMap)session.selectOne("NeoMapper3020.checkTotal", ht);

            // 지급원장에 기재되어 있는 정보와 실제 정보를 비교
            if (ht.getLong("DLNG_AMT").longValue() != checkTotalMap.getLong("TOTAL_AMT").longValue()
                    || ht.getLong("DPST_CNT").longValue() != checkTotalMap.getLong("TOTAL_CNT").longValue()) {
                logger.error("구청명 [" + param.getString("구청명") + "] 지급원장 지급명령등록번호 ["
                        + ht.getString("PMOD_RGSTR_NO") + "]기재되어 있는 총 건수와 총 금액이 실제와 다릅니다.");
                logger.error("구청명 [" + param.getString("구청명") + "] 지급원장 지급명령등록번호 ["
                        + ht.getString("PMOD_RGSTR_NO") + "] 총 건수 : "
                        + ht.getLong("DPST_CNT") + " 건 // 총 금액 : "
                        + ht.getLong("DLNG_AMT"));
                logger.error("구청명 [" + param.getString("구청명") + "] 실제명세 지급명령등록번호 ["
                        + ht.getString("PMOD_RGSTR_NO") + "] 총 건수 : "
                        + checkTotalMap.getLong("TOTAL_CNT") + " 건 // 총 금액 : "
                        + checkTotalMap.getLong("TOTAL_AMT"));

               /* ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)구청명 [" + param.getString("구청명") + "] 지급원장 지급명령등록번호 ["+ ht.getString("PMOD_RGSTR_NO") + "]기재되어 있는 총 건수와 총 금액이 실제와 다릅니다.");*/

                /*session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

                // 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);
                // 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                ht.setString("TRNX_NO",         getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();*/

                continue;
            }

            // 출금계좌가 5자리 이하거나 없으면 반려 및 에러 처리
            String public_cd  = "";
            int accno_length = 0;

            try {
                public_cd = ht.getString("DRW_ECRP_ACTNO").replaceAll("-", "").substring(3, 5);    // 출금계좌번호
                accno_length = ht.getString("DRW_ECRP_ACTNO").replaceAll("-", "").length();
            } catch (Exception e){
            	/**
            	 * [check] 파라미터 바꿔야 함
            	 */
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)출금계좌 오류");

                // @@@ 아래 지급명령등록번호 맗고 키값으로!!! @@@
                logger.error("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 출금계좌 오류입니다.");
                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /**
             * 지방자치단체명이 존재하지 않으면 에러로 응답처리
             */
            if("0".equals(ht.getString("LAF_NM_CHK"))) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)지방자치단체명이 존재하지 않습니다.");

                logger.error("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 지방자치단체명 오류입니다.");
                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /******************************************************************************
             * 수정일시 : 2012.03.08
             * 수정자   : 신상훈
             * 수정내용 : 입금명세에 대량이체건이 있으면 반려로 처리
             *****************************************************************************/
            int prcCnt = (Integer) session.selectOne("NeoMapper3020.getPrcCnt", ht );

            if (prcCnt > 0) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)입금대량이체(20)를 계좌이체(10)로 처리하십시요.");

                logger.error("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 대량이체 오류입니다.");
                logger.error("대량이체건 [" + prcCnt + "]");

                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));

                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /******************************************************************************
             * 수정일시 : 2012.12.26
             * 수정자   : 신상훈
             * 수정내용 : 입금금액이 0원이면 반려처리를 한다.
             *****************************************************************************/
            int prcCnt2 = (Integer) session.selectOne("NeoMapper3020.getPrcCnt2", ht );

            if (prcCnt2 > 0) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)입금금액을 확인해주세요.");

                logger.error("링크명 [" + param.getString("링크명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 입금금액 오류입니다.");
                logger.error("현재일자 [" + Utils.getCurrentDate() + "]");
                logger.error("거래일자 [" + ht.getString("DLNG_YMD") + "]");

                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /******************************************************************************
             * 수정일시 : 2023.02.09
             * 수정자   : 장재용
             * 수정내용 : 입금계좌 한도여부가 'Y'인데 세출한도계좌 개설정보테이블에 없으면 반려처리를 한다.
             *****************************************************************************/
            int prcCnt4 = (Integer) session.selectOne("NeoMapper3020.getPrcCnt4", ht );

            if (prcCnt4 > 0) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)입금계좌의 한도계좌여부 상태를 확인해주세요.");

                logger.error("링크명 [" + param.getString("링크명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 입금계좌 한도여부 오류입니다.");
                logger.error("현재일자 [" + Utils.getCurrentDate() + "]");
                logger.error("거래일자 [" + ht.getString("DLNG_YMD") + "]");

                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /******************************************************************************
             * 수정일시 : 2023.02.09
             * 수정자   : 장재용
             * 수정내용 : 입금계좌 잘못된 값이 들어갈 경우 반려처리를 한다.
             *****************************************************************************/
            int prcCnt5 = (Integer) session.selectOne("NeoMapper3020.getPrcCnt5", ht );

            if (prcCnt5 > 0) {
                ht.setString("에러코드", "0006");
                ht.setString("에러내용", "(자동반려)입금계좌에 숫자 이외의 문자가 들어갔습니다.");

                logger.error("링크명 [" + param.getString("링크명") + "] 지급명령등록번호 [" + ht.getString("LINK_SNUM") + "]는 입금계좌 입력 오류입니다.");
                logger.error("현재일자 [" + Utils.getCurrentDate() + "]");
                logger.error("거래일자 [" + ht.getString("DLNG_YMD") + "]");

                session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                session.commit();

				// 세출 지급원장(TFE2190) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

				// 세출 입금원장(TFE2170) 처리불가 저장
                session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                // 시스템반려 지급&입금 INSERT
                String trnxStr = "EC";   // 거래번호 채번
                ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
                session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                session.commit();
                continue;
            }

            /******************************************************************************
             * 수정일시 : 2022.09.13
             * 수정자   : hsw
             * 수정내용 : 입금명세 세출한도계좌여부를 확인한다.
             *****************************************************************************/
            int prcCnt3 = (Integer) session.selectOne("NeoMapper3020.getPrcCnt3", ht );

            if (prcCnt3 > 0) {
                ht.setString("DPST_ANE_LIM_ACC_YN", "Y");
            }

            /**
             * [result]
             * 추후 신구시스템 오픈시 As-is와 To-be를 같이 올려야하기 때문에 거래구분을 EC로 진행하기로 함.
             */
            String trnxGbn = NeoUtils.getGbnCode(ht);	//한도계좌여부확인
            String trnxStr = "";

            /**
             * 한도계좌여부에 따른 거래구분 세트
             * 지급원장에 통합계좌 추가로 거래구분은 하나로 가져 감(2022-09-15)
             */
//            if (FLAG_Y.equals(trnxGbn)) {
//            	trnxStr = "EZ";	//금고전송
//            } else {
//            	trnxStr = "EC";	//지급전송
//            }
            trnxStr = "EC";	//지급전송

            //한도계좌에 따른 데이터 처리를 위해 파라미터 세트
            logger.info("##### trnxGbn["+ trnxGbn +"]");

            ht.setString("TRNX_GBN", 		trnxGbn);
            param.setString("TRNX_GBN", 	trnxGbn);

//            param.setString("거래구분", "EC");
            param.setString("자료구분", ht.getString("TRSFR_DATA_DV_CD"));



            // 호조 입금명세연계송신(TFE2170S)  조회
            detailList = (ArrayList<MyMap>)session.selectList("NeoMapper3020.selectTFE2170S", ht);

            logger.info("구청명 [" + param.getString("구청명")
            			+ "] 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO")
            			+ "]에 입금명세 데이터가 [" + detailList.size() + "]건이 있습니다.");

            if (detailList.size() <= 0) {
                logger.debug("구청명 [" + param.getString("구청명")
                		+ "] 에러! 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO") + "] 입금명세가 없습니다!");
                continue;
            }

            // 거래 일련번호 가져오기
            logger.debug("##### 거래번호 가져오기 #####");

            param.setString("JUMBUN", "031");
            param.setString("거래일자", ht.getString("DLNG_YMD"));

            logger.debug("점번 = " + param.getString("JUMBUN"));
            logger.debug("거래일자 = " + param.getString("거래일자"));
            logger.debug("거래구분 = " + param.getString("거래구분"));

            ht.setString("TRNX_NO", 		getTrnxNo(trnxStr));
			/******************************************************************************
			* 한도계좌가 전체 대상일 경우에는 추가 거래 번호를 하나 더 생성
			* 한도계좌의 통합 계좌 항목을 추가 되면서 별도 데이터 처리 안함으로 변경(2022-09-15)
			******************************************************************************/
            if(FLAG_ALL.equals(trnxGbn)) {
            	MyMap paramObj = new MyMap();
            	paramObj.setString("trnxNo", trnxGbn);
            	paramObj.setString("ECRP_ACTNO", ht.getString("DRW_ECRP_ACTNO"));

            	/**
            	 * [check]
            	 * 한도계좌 맵핑 정보 테이블 확인 후 쿼리 정리
            	 * 테스트를 위해 기존 mapper로 하고 개발에서는 mapper변경해야 함.
            	 */
//            	String strDrwActno = (String) session.selectOne("NeoMapperFile.getDrwActno", paramObj);

            	MyMap actnoMap = (MyMap) session.selectOne("NeoMapperCommon.selectDrwRealActno", paramObj);

            	/**
            	 * [check]
            	 * 한도계좌에 대한 실제계좌가 존재 하지 않으면 일단 진행 못하게 막음.
            	 */
            	if(actnoMap == null) {
                    ht.setString("에러코드", "0006");
                    ht.setString("에러내용", "(자동반려)통합지출계좌를 확인해주세요.");

                    logger.error("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO") + "]는 실제 계좌 정보가 존재 하지 않습니다.");
                    logger.error("현재일자 [" + Utils.getCurrentDate() + "]");
                    logger.error("거래일자 [" + ht.getString("DLNG_YMD") + "]");

                    session.update("NeoMapper3020.updateTFE2170S_ERR", ht);
                    session.update("NeoMapper3020.updateTFE2190S_ERR", ht);
                    session.commit();

    				// 세출 지급원장(TFE2190) 처리불가 저장
                    session.insert("NeoMapper3020.insertTFE2190R_ERR", ht);

    				// 세출 입금원장(TFE2170) 처리불가 저장
                    session.insert("NeoMapper3020.insertTFE2170R_ERR", ht);

                    // 시스템반려 지급&입금 INSERT
                    session.insert("NeoMapper3020.insertTFE2190_ERR", ht);
                    session.insert("NeoMapper3020.insertTFE2170_ERR", ht);

                    session.commit();
	            	continue;
            	}

            	String strDrwActno = actnoMap.getString("MTAC_IDT_NO");

            	ht.setString("DRW_ECRP_ACTNO_NEW", 		strDrwActno);
            	ht.setString("TRNX_NO_NEW", 		"");

            	param.setString("TRNX_NO_NEW", 		"");
            	param.setString("DRW_ECRP_ACTNO_NEW", 		"");

            } else {
            	ht.setString("TRNX_NO_NEW", 		"");
            	ht.setString("DRW_ECRP_ACTNO_NEW", 		"~");

            	param.setString("TRNX_NO_NEW", 		"");
            	param.setString("DRW_ECRP_ACTNO_NEW", 		"~");
            }

            // ht.setString("이체일자", 			ht.getString("DLNG_YMD"));  // 이체일자 = 거래일자
            param.setString("TRNX_NO", 		ht.getString("TRNX_NO"));
            param.setString("LINK_SNUM", 	ht.getString("LINK_SNUM"));	// 지급원장(마스터)의 연계일련번호
            param.setString("PMOD_RGSTR_NO", ht.getString("PMOD_RGSTR_NO"));	// 지급원장의 지급명령등록번호
            param.setString("LAF_CD", ht.getString("LAF_CD"));
            param.setString("DLNG_YMD", ht.getString("DLNG_YMD"));

            /**
             * [result]
             * 기존 as-is처럼 로직 처리를 위해 기존항목 추가
             */
            param.setString("자료구분", ht.getString("TRSFR_DATA_DV_CD"));
            param.setString("TRSFR_DATA_DV_CD", ht.getString("TRSFR_DATA_DV_CD"));
            param.setString("자료수신자명", 		"대구은행");
            ht.setString("자료수신자명", 		param.getString("자료수신자명"));

			/******************************************************************************
			* 자금환수일경우 출금계좌를 입금계좌로 변경한다.
			******************************************************************************/
            if ("46".equals(ht.getString("TRSFR_DATA_DV_CD"))) {
                MyMap ht_accno = null;  // new MyMap();

                param.setString("acc_cd", ht.getString("DRW_BANK_CD"));
                param.setString("acc_no", ht.getString("DRW_ECRP_ACTNO"));

                for (int k = 0; k < detailList.size(); k++) {
                    ht_accno = detailList.get(k);

                    logger.debug("자금환수입금은행코드: " + ht_accno.getString("DPST_BANK_CD"));
                    logger.debug("자금환수입금은행계좌: " + ht_accno.getString("DPST_ECRP_ACTNO"));

                    ht.setString("DRW_BANK_CD", ht_accno.getString("DPST_BANK_CD"));	// 출금은행코드
                    ht.setString("DRW_ECRP_ACTNO", ht_accno.getString("DPST_ECRP_ACTNO"));	// 출금계좌번호
                }
            }

            try {
            	logger.debug("##### ["+ht.toString()+"]");

				// 세출 지급원장(TFE2190) 저장
                session.insert("NeoMapper3020.insertTFE2190", ht);
                logger.debug("지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO") + "] 로컬 DB로 등록완료");

				// 호조 지급원장(TFE2190S) 수신완료처리
                if (session.update("NeoMapper3020.updateTFE2190S_OK", ht) != 1) {
                    session.rollback();
                    continue;
                }

                logger.debug("구청명 [" + param.getString("구청명") + "] 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO") + "] UPDATE 완료");
            } catch (Exception e) {
                session.rollback();
                logger.error("오류[" + e.getLocalizedMessage() + "]");
                continue;
            }

            int i_success = 0;
            long beforeTime = System.currentTimeMillis();

            // e-세출 입금명세 insert
            i_success = subService(detailList, param);

            if (i_success != 0) {
                session.rollback();
                logger.error("구청명 [" + param.getString("구청명")
                		+ "] 로컬 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO")
                		+ "] 등록 및 e-호조 테이블에 업데이트 bit set 도중 오류 발생!");
                continue;
            }

            logger.debug("구청명 [" + param.getString("구청명")
            			+ "] 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO")
            			+ "]에 거래번호 [" + ht.getString("TRNX_NO")
            			+ "] 로컬 DB로 등록완료");

            ht.setString("이체일자", ht.getString("DLNG_YMD"));

            // 정상적으로 종료 됐을 때 Transation commit및 종료
            session.commit();

            long afterTime  = System.currentTimeMillis();
            long secDiffTime = (afterTime - beforeTime)/1000;

            logger.info("구청명 [" + param.getString("구청명")
            			+ "] 지급명령등록번호 [" + ht.getString("PMOD_RGSTR_NO")
            			+ "] 건수 [" + detailList.size()
            			+ "] 작업완료  : [ " + secDiffTime + " 초 ]");

            // 건수가 많을 수 있으므로, 작업이 완료된 입금명세 데이터는 초기화 시킨다.
            detailList.clear();
        }

        logger.info("구청명 [" + param.getString("구청명") + "] 구청 작업완료");

        return 0;
    }

    /**
     * 호조 입금명세 데이터 처리
     * @param detailList
     * @param param
     * @return
     * @throws Exception
     */
    private int subService(ArrayList<MyMap> detailList, MyMap param) throws Exception
    {
        long l_count = 0;
        MyMap ht_detail = null;    // new MyMap();

        /**
         * [result]
         * 자금배정은 묶어서 내려 오기때문에 기존 자금배정 로직은 제외
         */
        // 자료구분: 45(자금배정), 46(자금환수)
        if(param.getString("TRSFR_DATA_DV_CD").equals("46")) {
//            for (int k = 0; k < detailList.size(); k++) {
                ht_detail = detailList.get(0);

                param.setString("입금일련번호", ht_detail.getString("DPST_SNUM"));
                ht_detail.setString("DATA_NM", param.getString("자료수신자명"));	// 자료명
                ht_detail.setString("TRNX_NO", param.getString("TRNX_NO"));
                ht_detail.setString("LINK_SNUM", param.getString("LINK_SNUM"));
    			/******************************************************************************
    			* 자금환수일경우 입금계좌를 출금계좌로 변경한다.
    			******************************************************************************/
                if ("46".equals(ht_detail.getString("TRSFR_DATA_DV_CD"))){	// 이체자료구분코드( 46:자금환수)
                    ht_detail.setString("DPST_BANK_CD", param.getString("acc_cd"));
                    ht_detail.setString("DPST_ECRP_ACTNO", param.getString("acc_no"));
                    ht_detail.setString("DPST_ACNTR_NM", "[반납]" + ht_detail.getString("DPST_ACNTR_NM"));
                }

                logger.debug("요청ID[" + ht_detail.getString("DMND_ID") + "]");
                logger.debug("요청기관구분[" + ht_detail.getString("TRSFR_DMND_INST_DV_CD") + "]");
                logger.debug("자치단체코드[" + ht_detail.getString("LAF_CD") + "]");
                logger.debug("관서코드[" + ht_detail.getString("GOF_CD") + "]");
                logger.debug("부서코드[" + ht_detail.getString("GIVE_DEPT_CD") + "]");
                logger.debug("회계연도[" + ht_detail.getString("FYR") + "]");
                logger.debug("회계코드[" + ht_detail.getString("ACNT_DV_CD") + "]");
                logger.debug("자료구분[" + ht_detail.getString("TRSFR_DATA_DV_CD") + "]");
                logger.debug("지급명령등록번호[" + ht_detail.getString("PMOD_RGSTR_NO") + "]");
                logger.debug("입금일련번호[" + ht_detail.getString("DPST_SNUM") + "]");
                logger.debug("거래일자[" + ht_detail.getString("DLNG_YMD") + "]");
                logger.debug("기관구분[" + ht_detail.getString("LAF_LVL_CD") + "]");
                logger.debug("지급명령번호[" + ht_detail.getString("PMOD_NO") + "]");
                logger.debug("입금유형[" + ht_detail.getString("DPST_TY_CD") + "]");
                logger.debug("입금은행코드[" + ht_detail.getString("DPST_BANK_CD") + "]");
                logger.debug("입금계좌번호[" + ht_detail.getString("DPST_ECRP_ACTNO") + "]");
                logger.debug("입금계좌예금주명[" + ht_detail.getString("DPST_ACNTR_NM") + "]");
                logger.debug("입금명세[" + ht_detail.getString("DPST_DTL_CN") + "]");
                logger.debug("입금금액[" + ht_detail.getString("DLNG_AMT") + "]");
                logger.debug("CMS번호[" + ht_detail.getString("CMS_NO") + "]");
                logger.debug("지급형태[" + ht_detail.getString("PMOD_DV_CD") + "]");
                logger.debug("현금유형코드[" + ht_detail.getString("CASH_TY_CD") + "]");
                logger.debug("현금종류코드[" + ht_detail.getString("CASH_ITM_CD") + "]");
                logger.debug("현금종류명[" + ht_detail.getString("ITM_NM") + "]");
                logger.debug("자료수신자명[" + ht_detail.getString("자료수신자명") + "]");
                logger.debug("재배정여부[" + ht_detail.getString("RAT_YN") + "]");
                logger.debug("압류방지코드[" + ht_detail.getString("SLRY_CHLT_CD") + "]");
                logger.debug("거래번호[" + ht_detail.getString("TRNX_NO") + "]");

                try {
                    session.insert("NeoMapper3020.insertTFE2170one", ht_detail);

                    l_count = l_count + 1;

                    if (l_count%500 == 0) {
//                        session.commit();
//                        logger.debug("링크명 [" + param.getString("링크명") + "] 입금데이타 지급명령등록번호 ["
//                        + param.getString("지급명령등록번호") + "] 입금일련번호 ["
//                        + ht_detail.getString("IN_SERIAL_NO") + "] 로컬 DB로 등록완료");
                    }
                } catch (Exception e) {
                    logger.error("오류[" + e.getLocalizedMessage() + "]");
                    return -1;
                }
//            }
        }else{
            logger.info("자치단체코드[" + param.getString("LAF_CD") + "]");
            logger.info("거래일자[" + param.getString("DLNG_YMD") + "]");
            logger.info("지급명령등록번호[" + param.getString("PMOD_RGSTR_NO") + "]");
            if (session.update("NeoMapper3020.insertTFE2170", param) == 0) {
                logger.error("구청명 [" + param.getString("구청명") + "] 업데이트가 제대로 이루어지지 않았습니다. 지급명령등록번호 ["
                		+ param.getString("지급명령등록번호") + "] ");
                return -1;
            }
        }

		// 호조 입금명세(TFE2170S) 수신완료처리
        if (session.update("NeoMapper3020.updateTFE2170S_OK", param) == 0) {
            logger.error("구청명 [" + param.getString("구청명") + "] 호조 업데이트가 제대로 이루어지지 않았습니다. 지급명령등록번호 ["
                    + param.getString("지급명령등록번호") + "] ");
            try {
                SQLWarning    warn = session.getConnection().getWarnings();
                logger.error(warn.getMessage());
            } catch (SQLException sqlex) {
                logger.error("[" + sqlex.getLocalizedMessage() +"]");
            }
            return -1;
        }

        return 0;
    }

    /**
     * 거래번호 채번 (신규 getSqNo 대신-> 시퀀스로 바꿔야 할듯)
     * @return
     * @throws Exception
     */
    public String getTrnxNo(String trnxGbn) throws Exception {
    	String trnxNo ="";
    	MyMap paramObj = new MyMap();

    	paramObj.setString("trnxNo", trnxGbn);

    	SqlSession ss = sqlMapper.openSession();

    	try {
    		trnxNo = (String) ss.selectOne("NeoMapper3020.getTrnxNo", paramObj);
    	} finally {
	        ss.close();
	    }
	    return trnxNo;
    }

    /**
     * 거래일자 다른경우 거래번호 채번 (반려)
     * @return
     * @throws Exception
     */
    public String getTrnxNo2(MyMap ht) throws Exception {
        String trnxNo ="";
        MyMap paramObj = new MyMap();

        paramObj.setString("trnxNo", "EC");
        paramObj.setString("dlngYmd", ht.getString("DLNG_YMD"));
        logger.info("#####" + ht.getString("DLNG_YMD"));
        logger.info("#####" + paramObj.getString("dlngYmd"));

        SqlSession ss = sqlMapper.openSession();

        try {
            trnxNo = (String) ss.selectOne("NeoMapper3020.getTrnxNo_2", paramObj);
        } finally {
            ss.close();
        }
        return trnxNo;
    }
}
