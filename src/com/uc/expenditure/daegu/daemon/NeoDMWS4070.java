/**
 *  주시스템명 : 과오납
 *  업  무  명 : 과오납원장생성 (Master / Detail)
 *  기  능  명 : 과오납원장생성  (Master / Detail)
 *  클래스  ID : NeoDMWS4070
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

public class NeoDMWS4070 implements Runnable
{
    @SuppressWarnings("unchecked")
    static Logger   logger = Logger.getLogger(NeoDMWS4070.class);
    private static SqlSession  session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static Thread   self = null;
    static MyMap    appRes = new MyMap();

    public static void main(String args[])
    {
        DOMConfigurator.configure(NeoDMWS4070.class.getResource("/conf/log4j.xml"));

        logger.debug("##### [" + NeoDMWS4070.class.getSimpleName() + "] 시작 #####");

        NeoDMWS4070  NeoDMWS4070 = new NeoDMWS4070();

        self = new Thread(NeoDMWS4070);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("##### [" + NeoDMWS4070.class.getSimpleName() + "] 작업 종료 #####");
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
        logger.debug("######## TR2001_548_MAKE() 마스트/디테일 만들기 시작");
        TR2002_551_MAKE();
        TR2002_553_MAKE();
        logger.debug("######## TR2001_548_MAKE() 마스트/디테일 만들기 끝");
        TR2003_543_MAKE();

        session.close();
        return 0;
    }
    @SuppressWarnings("unchecked")
    private int TR2002_551_MAKE() {
        // TODO Auto-generated method stub
        try {
            /* 로직 START*/
            ArrayList<MyMap> arrTR551MasterMapList = null;
            ArrayList<MyMap> arrTR551DetailMapList = null;
            session.update("NeoMapper4070.updateTR2002_551_Status_P");

            arrTR551MasterMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2002_551M_DateSelect", arrTR551MasterMapList );

            if (arrTR551MasterMapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }

            for(int j = 0; j < arrTR551MasterMapList.size(); j++) {
                MyMap TR551MasterMap = arrTR551MasterMapList.get(j);

                logger.debug("TR551MasterMap[" + TR551MasterMap + "] ###################");
                session.insert("NeoMapper4070.insertTR2002_551M", TR551MasterMap );
                int seq = 0;
                /*trn_seq_str = null;*/
                arrTR551DetailMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2002_551D_DateSelect", TR551MasterMap );

                for(int k = 0; k < arrTR551DetailMapList.size(); k++) {
                    MyMap TR551DetailMap = arrTR551DetailMapList.get(k);
                    TR551DetailMap.put("SEQ",k+1);
                    logger.debug("TR551DetailMap[" + TR551DetailMap + "] ###################");

                    session.insert("NeoMapper4070.insertTR2002_551D", TR551DetailMap );
                }
            }
            session.update("NeoMapper4070.updateTR2002_551_Status_06");
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
    private int TR2002_553_MAKE() {
        // TODO Auto-generated method stub
        try {
            /* 로직 START*/
            ArrayList<MyMap> arrTR553MasterMapList = null;
            ArrayList<MyMap> arrTR553DetailMapList = null;
            session.update("NeoMapper4070.updateTR2002_553_Status_P");

            arrTR553MasterMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2002_553M_DateSelect", arrTR553MasterMapList );

            if (arrTR553MasterMapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }

            for(int j = 0; j < arrTR553MasterMapList.size(); j++) {
                MyMap TR553MasterMap = arrTR553MasterMapList.get(j);

                logger.debug("TR553MasterMap[" + TR553MasterMap + "] ###################");
                session.insert("NeoMapper4070.insertTR2002_553M", TR553MasterMap );
                int seq = 0;
                /*trn_seq_str = null;*/
                arrTR553DetailMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2002_553D_DateSelect", TR553MasterMap );

                for(int k = 0; k < arrTR553DetailMapList.size(); k++) {
                    MyMap TR553DetailMap = arrTR553DetailMapList.get(k);
                    TR553DetailMap.put("SEQ",k+1);
                    logger.debug("TR553DetailMap[" + TR553DetailMap + "] ###################");

                    session.insert("NeoMapper4070.insertTR2002_553D", TR553DetailMap );
                }
            }
            session.update("NeoMapper4070.updateTR2002_553_Status_06");
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
            ArrayList<MyMap> arrTR543MasterMapList = null;
            ArrayList<MyMap> arrTR543DetailMapList = null;
            int a = session.update("NeoMapper4070.updateTR2003_543_Status_P");

            arrTR543MasterMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2003_543M_DateSelect", arrTR543MasterMapList );

            if (arrTR543MasterMapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }

            for(int j = 0; j < arrTR543MasterMapList.size(); j++) {
                MyMap TR543MasterMap = arrTR543MasterMapList.get(j);

                logger.debug("TR543MasterMap[" + TR543MasterMap + "] ###################");
                session.insert("NeoMapper4070.insertTR2003M", TR543MasterMap );
                int seq = 0;
                /*trn_seq_str = null;*/

                arrTR543DetailMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2003_543D_DateSelect", TR543MasterMap );

                for(int k = 0; k < arrTR543DetailMapList.size(); k++) {
                    MyMap TR543DetailMap = arrTR543DetailMapList.get(k);
                    logger.debug("TR543DetailMap[" + TR543DetailMap + "] ###################");

                    session.insert("NeoMapper4070.insertTR2003D", TR543DetailMap );

                }
            }
            session.update("NeoMapper4070.updateTR2003_543_Status_06");
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
    private int TR2001_548_MAKE() {
        // TODO Auto-generated method stub
        try {
            /* 로직 START*/
            ArrayList<MyMap> arrTR548MasterMapList = null;
            ArrayList<MyMap> arrTR548DetailMapList = null;
            session.update("NeoMapper4070.updateTR2001_548_Status_P");

            arrTR548MasterMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2001_548M_DateSelect", arrTR548MasterMapList );

            if (arrTR548MasterMapList.size() <= 0) {   // 조회된 값이 없으면 탈출
                return 0;
            }

            for(int j = 0; j < arrTR548MasterMapList.size(); j++) {
                MyMap TR548MasterMap = arrTR548MasterMapList.get(j);

                logger.debug("TR548MasterMap[" + TR548MasterMap + "] ###################");
                session.insert("NeoMapper4070.insertTR2001M", TR548MasterMap );
                int seq = 0;

                arrTR548DetailMapList = (ArrayList<MyMap>) session.selectList("NeoMapper4070.getTR2001_548D_DateSelect", TR548MasterMap );

                for(int k = 0; k < arrTR548DetailMapList.size(); k++) {
                    MyMap TR548DetailMap = arrTR548DetailMapList.get(k);
                    TR548DetailMap.put("SEQ", k+1);
                    logger.debug("TR548DetailMap[" + TR548DetailMap + "] ###################");

                    session.insert("NeoMapper4070.insertTR2001D", TR548DetailMap );
                }
            }
            session.update("NeoMapper4070.updateTR2001_548_Status_04");
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
