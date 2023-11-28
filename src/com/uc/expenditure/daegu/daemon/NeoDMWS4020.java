/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 입금지급 처리
 *  기  능  명 : e세출업무원장 이체결과 e호조에 결과 처리
 *  클래스  ID : NeoDMWS4020
 *  변경  이력 :
 * -----------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * -----------------------------------------------------------------------------
 *  이경두       다산(주)      2022.08.29         %01%             최초작성
 *  하상원       다산(주)      2022.10.07         %01%             수정작성
 */
package com.uc.expenditure.daegu.daemon;

import  java.io.Reader;
import  java.util.ArrayList;
import java.util.Properties;

import  org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import  org.apache.ibatis.session.SqlSession;
import  org.apache.ibatis.session.SqlSessionFactory;
import  org.apache.ibatis.session.SqlSessionFactoryBuilder;
import  org.apache.log4j.Logger;
import  org.apache.log4j.xml.DOMConfigurator;

import com.uc.framework.utils.MyMap;
import com.uc.framework.utils.NeoUtils;
import com.uc.framework.utils.Utils;

public class NeoDMWS4020 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger    logger = Logger.getLogger(NeoDMWS4020.class);
    private static SqlSessionFactory sqlMapper = null;
    private int govId  = 0;

    /**
     * [result]
     * 전체를 돌려도 문제 없을꺼 같아 그냥 기존 로직으로 처리(2022-09-01)
     */
//    private String[] govIdArray  = {"3410000"};
//    private static Thread[] self = new Thread[1];
    private static Thread[] self = new Thread[9];
    private String[] govIdArray  = {"6270000","3410000","3420000","3430000","3440000","3450000","3460000","3470000","3480000"};

    public NeoDMWS4020(int i) {
        this.govId = i;
    }

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS4020.class.getResource("/conf/log4j.xml"));

        logger.info("##### [" + NeoDMWS4020.class.getSimpleName() + "] 시작 #####");

        for(int i=0;i< self.length;i++) {
            NeoDMWS4020  neoDMWS4020 = new NeoDMWS4020(i);
            self[i] = new Thread(neoDMWS4020);
            self[i].setDaemon(true);
            self[i].start();
        }

        for(int i=0;i< self.length;i++) {
            try {
                self[i].join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        logger.info("##### [" + NeoDMWS4020.class.getSimpleName() + "] 끝 #####");
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

        while (!Thread.currentThread().isInterrupted()) {
            logger.info("##### mainLoop start #####");
            mainLoop();
            logger.info("##### mainLoop end #####");
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
    public int mainLoop()
    {
        // 데이터베이스 세션 생성, 자동확약 끔.
        SqlSession  session = sqlMapper.openSession(ExecutorType.BATCH,false);

        MyMap paramMap = new MyMap();

        // 링크 리스트 가져오기
        paramMap.setString("govcode",govIdArray[govId]);

        ArrayList<MyMap> linkList = (ArrayList<MyMap>)session.selectList("NeoMapperCommon.getLinkList2", paramMap);

        for (int i = 0; i < linkList.size(); i++) {
            MyMap ht = linkList.get(i);

            paramMap.setString("자치단체코드", ht.getString("자치단체코드"));
            paramMap.setString("구청명", ht.getString("구청명"));

            logger.info("[" + ht.getString("구청명") + "] 의 DB를 조회합니다.");

            // 구청별로 작업
            try {
                logger.debug("DBTrans start");
                DBTrans(session,paramMap);
                logger.debug("DBTrans end");
            } catch (Exception ex) {
                logger.error("작업중 오류발생, 내용[" + ex.getLocalizedMessage() + "]");
                session.rollback();
            }
        }

        session.close();
        return 0;
    }

    @SuppressWarnings("unchecked")
    private int DBTrans(SqlSession  session,MyMap paramMap) throws Exception
    {
        ArrayList<MyMap> viewList = null;
        ArrayList<MyMap> dataList = null;

        try {
        	// 세출 지급원장 조회 (이체처리완료 후 호조 전송안한건  - 작업상태코드 31)
            viewList = (ArrayList<MyMap>)session.selectList("NeoMapper4020.selectTFE2190" , paramMap );

            if (viewList == null ||  viewList.size() <= 0 ) {
                logger.debug("수신 데이터가 없습니다.");
                return -1;
            }

            logger.info( "[" + paramMap.get("구청명") + "] 결과업데이트 된 지급 원장이 " + viewList.size() + "개 있습니다." );

            MyMap ht = null;

            for( int i = 0 ; i < viewList.size() ; i++ ) {
            	/**
            	 * 중복 cast처리 됨.
            	 */
//                ht = ( MyMap ) viewList.get( i );
                ht = viewList.get( i );

			    /******************************************************************************
 		        * 한도계좌조회
 		        * 한도계좌 거래내역 처리 로직을 21번 데몬에서 처리 하기 때문에 한도계좌여부 처리 로직 주석 처리 함.(20221109)
 				******************************************************************************/
//             	MyMap paramObj = new MyMap();
//             	paramObj.setString("TRNX_NO", ht.getString("TRNX_NO"));
//
//             	MyMap mapDrwActno = (MyMap) session.selectOne("NeoMapperFile.getLimitAccount", paramObj);
//             	String trnxGbn = NeoUtils.getGbnCode(mapDrwActno);	//한도계좌여부확인
//             	logger.info("##### trnxGbn["+ trnxGbn +"]");
//
//             	ht.setString( "ANE_LIM_ACC_YN"			, mapDrwActno.getString( "ANE_LIM_ACC_YN"));
//             	ht.setString( "DPST_ANE_LIM_ACC_YN"		, mapDrwActno.getString( "DPST_ANE_LIM_ACC_YN"));

                paramMap.setString( "거래번호"			, ht.getString( "TRNX_NO"        		));  // key
                paramMap.setString( "연계일련번호"		, ht.getString( "LINK_SNUM"        		));  // key
                paramMap.setString( "거래일자"			, ht.getString( "DLNG_YMD"        		));
                paramMap.setString( "요청ID"			, ht.getString( "DMND_ID"           	));
                paramMap.setString( "요청기관구분"		, ht.getString( "TRSFR_DMND_INST_DV_CD"	));
                paramMap.setString( "자치단체코드"		, ht.getString( "LAF_CD"     			));
                paramMap.setString( "관서코드"			, ht.getString( "GOF_CD"         		));
                paramMap.setString( "지급부서코드"		, ht.getString( "GIVE_DEPT_CD"     		));
                paramMap.setString( "회계연도"			, ht.getString( "FYR"         			));
                paramMap.setString( "회계코드"			, ht.getString( "ACNT_DV_CD"         	));
                paramMap.setString( "자료구분"			, ht.getString( "TRSFR_DATA_DV_CD"		));
                paramMap.setString( "지급명령등록번호"	, ht.getString( "PMOD_RGSTR_NO" 		));
                paramMap.setString( "재배정여부"		, ht.getString( "RAT_YN"				));

                // 세출 입금명세 조회
                dataList = (ArrayList<MyMap>)session.selectList("NeoMapper4020.selectTFE2170", paramMap);

                if (dataList == null || dataList.size() <= 0 ) {
                    logger.debug( "알맞는 입금명세가 없습니다." );
                    logger.debug("거래일자[" 		 + paramMap.getString("거래일자") + "]");
                    logger.debug("요청ID[" 		 + paramMap.getString("요청ID") + "]");
                    logger.debug("요청기관구분[" 	 + paramMap.getString("요청기관구분") + "]");
                    logger.debug("자치단체코드[" 	 + paramMap.getString("자치단체코드") + "]");
                    logger.debug("관서코드[" 		 + paramMap.getString("관서코드") + "]");
                    logger.debug("부서코드[" 		 + paramMap.getString("지급부서코드") + "]");
                    logger.debug("회계연도[" 		 + paramMap.getString("회계연도") + "]");
                    logger.debug("회계코드[" 		 + paramMap.getString("회계코드") + "]");
                    logger.debug("자료구분[" 		 + paramMap.getString("자료구분") 		+ "]");
                    logger.debug("지급명령등록번호[" + paramMap.getString("지급명령등록번호")	+ "]");
                    logger.debug("재배정여부[" 	 + paramMap.getString("재배정여부") 		+ "]");
                    continue;
                }

                long beforeTime = System.currentTimeMillis();

                logger.info( paramMap.getString("자치단체코드")  + " - 지급명령등록번호[" + ht.getString( "PMOD_RGSTR_NO" ) + "]의 결과처리 된 입금 명세가 " + dataList.size() + "개 있습니다." );

                try {
					logger.debug("##### 4020_ht : ["+ht.toString()+"]");

                    session.insert( "NeoMapper4020.insertTFE2170R" , ht );

                    logger.info( "지급명령등록번호[" + ht.getString( "PMOD_RGSTR_NO" ) + "]의 e-호조 입금명세 업데이트 완료" );

                    logger.debug("자료수신여부[" 	+ ht.getString("DATA_RCTN_CD") 			+ "]");
                    logger.debug("결과코드[" 		+ ht.getString("TRSFR_PRCS_RSLT_CD") 	+ "]");
                    logger.debug("결과설명[" 		+ ht.getString("TRSFR_PRCS_RSLT_CN") 	+ "]");
                    logger.debug("결과처리자명[" 	+ ht.getString("TRSFR_RSLT_DLPS_NM") 	+ "]");
                    logger.debug("결과처리여부[" 	+ ht.getString("TRSFR_RSLT_PRCS_YN") 	+ "]");

                    // 지급원장연계수신테이블 insert select TFE2190 insert TFE2190R
                    session.insert( "NeoMapper4020.insertTFE2190R" , ht );

                    logger.debug( "지급명령등록번호[" + ht.getString( "PMOD_RGSTR_NO" ) + "]의 e-호조 지급원장 업데이트 완료" );

                    // 세출 지급원장 작업상태 32로 최종 업데이트
                    session.update( "NeoMapper4020.updateTFE2190End" , ht );

                    long afterTime  = System.currentTimeMillis();
                    long secDiffTime = (afterTime - beforeTime)/1000;

                    logger.info( paramMap.getString("자치단체코드")  + " - 지급명령등록번호[" + ht.getString( "PMOD_RGSTR_NO" ) + "]의 로컬 지급원장 업데이트 완료  : [ " + secDiffTime + " 초 ]" );

                    // 정상적으로 종료 됐을 때 Transation commit및 종료
                    session.commit();

                } catch (Exception e) {
                    session.rollback();
                    logger.error( "지급명령등록번호 [" + ht.getString( "PMOD_RGSTR_NO" ) + "] 작업 중 에러:" + e.getLocalizedMessage());
                    return -1;
                }

                /**
                 * [result]
                 * 거래내역은 일단 별도 처리로 변경 함.
                 * 한도계좌 거래내역은 실시간 처리가 필요 없어 21번 데몬에서 18시 이후에 처리되도록 하기 위해서 위치를 옯김(20221109)
                 * 일단 혹시 실시간 처리가 필요할 수 있기 때문에 주석처리 해 놓음.(20221109)
                 */
//                try {
                    /**
                     * [check]
                     * 한도계좌일 경우 거래이력(잔액) 처리해야 함.-지급명세가 한도계좌일 경우
                     */
//                    if(FLAG_Y.equals(mapDrwActno.getString("ANE_LIM_ACC_YN"))) {
//                    	session.insert( "NeoMapper4020.insertTFE2190ToTFMA050A" , ht );
//                    }
                    /**
                     * [check]
                     * 한도계좌일 경우 거래이력(잔액) 처리해야 함.-입금명세가 한도계좌일 경우
                     */
//                    if(FLAG_Y.equals(mapDrwActno.getString("DPST_ANE_LIM_ACC_YN"))) {
//                    	session.insert( "NeoMapper4020.insertTFE2170ToTFMA050A" , ht );
//                    }
                    // 정상적으로 종료 됐을 때 Transation commit및 종료
//                    session.commit();
//                } catch (Exception e) {
//                    session.rollback();
//                    logger.error( "지급명령등록번호 [" + ht.getString( "PMOD_RGSTR_NO" ) + "] 작업 중 에러:" + e.getLocalizedMessage());
////                    return -1;
//                }

                dataList.clear();
                dataList = null;
            }

//            session.commit();
            logger.info("[" + paramMap.get("구청명") + "] 작업 완료");
        } catch (Exception se) {
            logger.error("[" + paramMap.get("구청명") + "] 작업 중 에러:" +se.getMessage());
            session.rollback(true);
            return -1;
        }
        return 0;
    }
}
