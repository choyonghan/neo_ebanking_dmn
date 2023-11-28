/**
 *  주시스템명 : 대구시 통합자금관리
 *  업  무  명 : e세출 가상계좌(일상경비, 세입세출외) 이체파일 생성처리
 *  기  능  명 : e세출 가상계좌(일상경비, 세입세출외) 이체파일 생성처리
 *  클래스  ID : NeoDMWS5030
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자         소속                 일자            Tag                 내용
 * ------------------------------------------------------------------------
 *  하상우       다산(주)      2022.08.29    %01%         최초작성
 */
package com.uc.expenditure.daegu.daemon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Reader;
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
 * 흐름 : [세출->은행]
 * <br />
 * 1. 다빈치 가상계좌 납부요구(202)에서 [일상경비반납 및 세입세출현금반납]의 [금고요청구분코드(STBX_DMND_DV_CD) / 수납요청구분코드(RCVMT_DMND_DV_CD)] 값이 "17" 인 건에 대해서
 * <br />
 * 은행 계정계에 보낼 이체파일을 생성하는 데몬이다.
 * <br />
 * <br />
 * 이체파일 생성 후 [금고요청구분코드(STBX_DMND_DV_CD) / 수납요청구분코드(RCVMT_DMND_DV_CD)] 값을 "18" 로 수정처리한다.
 * <br />
 * 2. 이체파일 생성 후 SendTransferToBank.java 를 통해서 은행 계정계로 전송된다.
 * SendTransferToBank.java 에서 [금고요청구분코드(STBX_DMND_DV_CD) / 수납요청구분코드(RCVMT_DMND_DV_CD)] 값을 "19" 로 수정처리한다.
 * <br />
 *
 */
public class NeoDMWS5030 implements Runnable {
    @SuppressWarnings("unchecked")
    static Logger logger = Logger.getLogger(NeoDMWS5030.class);
    private static SqlSession session = null;
    private static SqlSessionFactory sqlMapper = null;
    private static FormatParserAsMap fp = null;
    private static Thread self = null;
    static MyMap appRes = new MyMap();

    private String CURRENT_DATE = Utils.getCurrentDate();
    private String TRGET_DIRECTORY = "";
    private final String DGCITY01 = "DGCITY01"; // 예약이체파일(DGCITY01)
    private String fileDvcd = "";

    public static void main(String args[]) {

        DOMConfigurator.configure(NeoDMWS5030.class.getResource("/conf/log4j.xml"));
        if( logger.isDebugEnabled()) {
        	logger.debug("===== [" + NeoDMWS5030.class.getSimpleName() + "] 시작 =====");
        }
        NeoDMWS5030 hello = new NeoDMWS5030();
        self = new Thread(hello);
        self.setDaemon(true);
        self.start();

        try {
            self.join();
        } catch (InterruptedException e) {
        }

        if( logger.isDebugEnabled()) {
        	logger.debug("===== [" + NeoDMWS5030.class.getSimpleName() + "] 끝 =====");
        }
    }

    @Override
	public void run() {
        
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
        logger.info("SQLMAPPER생성");

        fp = new FormatParserAsMap(logger);
        if (fp.doParsingAsMap("msgformat1") < 0) {
            logger.error("전문포맷 분석오류");
            return;
        }

        Utils.getResources("conf/ApplicationResources", appRes);

        while (!Thread.currentThread().isInterrupted()) {
        	
            if( logger.isDebugEnabled()) {
            	logger.debug("쓰레드[" + Thread.currentThread().getName() + "] 실행");
            }
            
            mainLoop();
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public int mainLoop() {
    	if( logger.isDebugEnabled()) {
            logger.debug(">>>>>>>>mainLoop() Started !!!!!");
    	}

        // 데이터베이스 세션 생성, 자동확약 끔.
        try {
            session = sqlMapper.openSession(false);
        } catch (Exception ex) {
            logger.error("데이터베이스 오류, 내용[" + ex.getLocalizedMessage() + "]");
            return -1;
        }

        try {

        	MyMap param = new MyMap();
        	param.setString("현재일자", CURRENT_DATE);
        	List<MyMap> trgetList = session.selectList("NeoMapper5030.trgetList", param);
        	int tCnt = trgetList.size();
            logger.info("신규 등록된 지급 원장이 " + tCnt + "개 있습니다.");

            // ------------------------------------------------------------------------ 디렉토리 체크
            CURRENT_DATE = Utils.getCurrentDate();
            TRGET_DIRECTORY = appRes.getString("SendTransferToBank.sendDirectory").concat("/dees_trn/send");
            fileDvcd = appRes.getString("ServiceAccDvCd"); // 계좌 CMS전송 파일 업무구분

            File dir = new File( TRGET_DIRECTORY );
            if (!dir.exists()) {
            	logger.info("신규 등록된 지급 원장이 " + tCnt + "개 있습니다.");
                logger.error("파일을 생성할 디렉토리가 존재하지 않습니다.(" + TRGET_DIRECTORY + ")");
                return -1;
            }

            // ------------------------------------------------------------------------
            // 파일생성 루프
            // ------------------------------------------------------------------------
        	for( MyMap trgetData : trgetList ) {

        		String trnxNo = trgetData.getString("거래번호");

        		MyMap headerMap = getHeaderMap( trgetData );
        		MyMap dataMap = getDataMap( trgetData );
        		MyMap trailerMap = getTrailer( trgetData );

        		// 1. 계정계 전송 파일 생성 H:모계좌 / D:실계좌
        		int proc = fileWriter( trnxNo, headerMap, dataMap, trailerMap );
        		if( proc == 0 ) {

                    updateData( trgetData ); // DRW_REAL_ACTNO : 입금계좌

        		}

        	} // end for trgetData

            session.commit();
            if( logger.isDebugEnabled()) {
            	logger.debug("작업 완료!!!");
            }
        } catch (Exception e) {
            logger.error("mainloop exception", e);
            session.rollback();
        }finally {
        	session.close();
        }
        return -1;

    }

    private int fileWriter( String trnxNo , MyMap headerMap, MyMap dataMap, MyMap trailerMap ) throws Exception {
    	int proc = 0;

        FileOutputStream fos = null;
        try {

    		String trgetFilePath = TRGET_DIRECTORY.concat(File.separator).concat( trnxNo);
    		if( logger.isDebugEnabled()) {
    			logger.debug("파일이름[" + trgetFilePath + "]");
    		}

            fos = new FileOutputStream( trgetFilePath );

            byte[] bMsg = new byte[1024];

            // ------------------------------------------------------------------------------------
            // HEADER 섹션 파일 저장
            // ------------------------------------------------------------------------------------
            int length = fp.assembleMessageByteHeadAsMap(bMsg, 1024, headerMap, "100");
            fos.write(bMsg, 0, length);
            fos.write("\n".getBytes(), 0, 1);

            // ------------------------------------------------------------------------------------
            // DATA 부 조립 및 저장
            // ------------------------------------------------------------------------------------
            length = fp.assembleMessageByteBodyAsMap(bMsg, 1024, dataMap, "100", DGCITY01);
            fos.write(bMsg, 0, length);
            fos.write("\n".getBytes(), 0, 1);

            // ------------------------------------------------------------------------------------
            // TRAILER 부 조립
            // ------------------------------------------------------------------------------------
            length = fp.assembleMessageByteTailAsMap(bMsg, 1024, trailerMap, "100");
            fos.write(bMsg, 0, length);
            fos.write("\n".getBytes(), 0, 1);
            fos.close();

    		if( logger.isDebugEnabled()) {
    			logger.debug("파일 생성 완료");
    		}

        } catch (FileNotFoundException fnfex) {
            logger.error("오류[" + fnfex.getLocalizedMessage() + "]");
            proc = -1;

        } finally {
        	if( fos != null ) {
        		fos.close();
        	}
        }

    	return proc;
    }

    private MyMap getHeaderMap( MyMap trgetData ) throws Exception {
    	MyMap headerMap = new MyMap();

        // 모계좌번호(출금계좌번호)
        String mainAcctNo = (String) session.selectOne("NeoMapper5030.getQryMainAcctNo", trgetData);

        // ------------------------------------------------------------------------------------
        // HEADER 부 조립
        // ------------------------------------------------------------------------------------
        headerMap.setString("파일업무구분", DGCITY01); // 예약이체파일(DGCITY01)
        headerMap.setString("구분코드", "H"); // H : Header
        headerMap.setString("파일구분", "B2"); // B2 : 업체송신시, 2B : 은행결과송신시

        headerMap.setString("JUMBUN", "031");
        headerMap.setString("거래일자", CURRENT_DATE);
        //headerMap.setString("거래구분", "VR");
        headerMap.setString("거래구분", trgetData.getString("자료구분"));

    	// 거래구분
        long seqNo = getSqNo(headerMap);

        String filesqno = String.format("%06d", seqNo);

        headerMap.setString("지급명령번호", filesqno); // e-호조 지급명령번호
        headerMap.setString("지급명령등록번호", "00" + filesqno); // e-호조 지급명령번호
        headerMap.setString("재배정여부", trgetData.getString("재배정여부")); // Y: 재배정자료, N:일반배정자료
        headerMap.setString("이체일자", trgetData.getString("이체일자")); // yyyymmdd
        headerMap.setString("파일코드", trgetData.getString("거래번호")); // 거래번호
        headerMap.setString("입금명세구분", trgetData.getString("입금명세구분"));
        headerMap.setString("요청ID", trgetData.getString("요청ID"));
        headerMap.setString("요청기관구분", trgetData.getString("요청기관구분"));
        headerMap.setString("자치단체코드", trgetData.getString("자치단체코드"));
        headerMap.setString("관서코드", trgetData.getString("관서코드"));
        headerMap.setString("지급부서코드", trgetData.getString("지급부서코드"));
        headerMap.setString("회계연도", trgetData.getString("회계연도"));
        headerMap.setString("회계코드", trgetData.getString("회계코드"));
        headerMap.setString("자료구분", trgetData.getString("자료구분"));
        headerMap.setString("자치단체명", trgetData.getString("자치단체명"));
        headerMap.setString("관서명", trgetData.getString("관서명"));
        headerMap.setString("지급부서명", trgetData.getString("지급부서명"));
        headerMap.setString("급여구분", trgetData.getString("급여구분"));
        headerMap.setString("복지급여여부", trgetData.getString("복지급여여부"));
        headerMap.setString("출금계좌번호", mainAcctNo); // 출금계좌번호

        // 20230110. 공란에 세출한도계좌추가
        headerMap.setString("공란", trgetData.getString("공란"));
        

        // TODO : test data
        /*
        headerMap.setString("파일불능코드", "0000");
        headerMap.setString("불능분입금계좌번호", "11122233334444");
        */

        return headerMap;
    }
    private MyMap getDataMap( MyMap trgetData ) throws Exception {

        MyMap dataMap = new MyMap();
        long tot_am = 0;
        long tmp_am = 0;

        tmp_am = trgetData.getLong("입금금액"); // 입금금액
        tot_am = tot_am + tmp_am; // 총의뢰금액

        dataMap.setString("파일업무구분", DGCITY01);
        dataMap.setString("구분코드", "D");
        dataMap.setString("입금일련번호", trgetData.getString("입금일련번호"));
        dataMap.setString("입금은행코드", trgetData.getString("입금은행코드"));
        dataMap.setString("입금유형", trgetData.getString("입금유형"));
        dataMap.setString("입금계좌번호", trgetData.getString("입금계좌번호").replaceAll("-", ""));
        dataMap.setString("입금계좌예금주명", trgetData.getString("입금계좌예금주명"));
        dataMap.setString("입금적요", trgetData.getString("입금명세"));
        dataMap.setLong("이체금액", tmp_am);
        //DataForm.setString("거래구분", headerForm.getString("자료구분"));
        dataMap.setString("거래구분", trgetData.getString("자료구분"));
        dataMap.setString("CMS번호", trgetData.getString("CMS번호"));
        dataMap.setString("압류방지코드", trgetData.getString("압류방지코드"));

        // TODO : test data
        /*
        dataMap.setString("처리여부", "Y");
//		DataForm.setString("이체처리불능코드", "BE01513");	// 오류샘플
        dataMap.setString("이체처리불능코드", "BN10501");	// 정상샘플
        dataMap.setString("이체처리불능내용", "정상처리 되었습니다.");
        */


        return dataMap;
    }
    private MyMap getTrailer( MyMap trgetData ) throws Exception {

        MyMap TrailerMap = new MyMap();
        long tot_am = 0;
        long tmp_am = 0;

        tmp_am = trgetData.getLong("입금금액"); // 입금금액
        tot_am = tot_am + tmp_am; // 총의뢰금액

		if( logger.isDebugEnabled()) {
			logger.debug("TRAILER 부 조립 시작");
		}

        TrailerMap.setString("파일업무구분", DGCITY01); // 예약이체파일(DGCITY01)
        TrailerMap.setString("구분코드", "E"); // 구분코드 E : Tailer
        TrailerMap.setLong("전송레코드수", 1 + 2); // 파일의 레코드 건수
        TrailerMap.setLong("총의뢰건수", 1); // 헤더, 트레일러 제외한 파일레코드 건수
        TrailerMap.setLong("총의뢰금액", tot_am); // 데이타부 합계금액
        TrailerMap.setLong("정상처리건수", 1); // 헤더, 트레일러 제외한 파일레코드 건수
        TrailerMap.setLong("정상처리금액", tot_am); // 데이타부 합계금액
        TrailerMap.setLong("미처리건수", 0); // 헤더, 트레일러 제외한 파일레코드 건수
        TrailerMap.setLong("미처리금액", 0); // 데이타부 합계금액
        TrailerMap.setLong("지급명령총건수", 1); // DATA부의 지급총건수
        TrailerMap.setLong("지급명령총금액", tot_am); // DATA부의 지급총금액

        return TrailerMap;
    }

    private int updateData( MyMap trgetData ) {
    	int nCnt = 0;
    	String dvcd = trgetData.getString("자료구분");

        // 업데이트 용도
    	trgetData.setString("STBX_DMND_DV_CD", "18"); // REQ_FG
    	trgetData.setString("RCVMT_DMND_DV_CD", "18"); // RECPT_REQ_FG

    	//if( "VR".equalsIgnoreCase(dvcd)) {
    	if( "DA".equalsIgnoreCase(dvcd)) {

    		// 이력등록
    		session.update("NeoMapper5030.insert2547h", trgetData);

    		nCnt = session.update("NeoMapper5030.update2547", trgetData); // 파일 생성 후 데이타 처리 (작업상태 = '18' 로 업데이트)

    	//}else if( "EA".equalsIgnoreCase(dvcd)) {
    	}else if( "DB".equalsIgnoreCase(dvcd)) {

    		// 이력등록
    		session.update("NeoMapper5030.insert3601h", trgetData);

    		nCnt = session.update("NeoMapper5030.update3601", trgetData); // 파일 생성 후 데이타 처리 (작업상태 = '18' 로 업데이트)
    	}

    	return nCnt;
    }

    public long getSqNo(MyMap mapForm) throws Exception
    {
        long sqNo = 0;

        SqlSession    ss = sqlMapper.openSession();
        ss.commit(false);

        try {
            try {
                long  nCnt = (Long)ss.selectOne("NeoMapperCommon.nCntSelect", mapForm);
        		if( logger.isDebugEnabled()) {
        			logger.debug("COUNT[" + nCnt + "]");
        		}

                if (nCnt > 0) {
                    sqNo = (Long)ss.selectOne("NeoMapperCommon.SqNoSelect", mapForm);
                }
            } catch (NullPointerException e) {
                // null 포인터는 무시
                logger.error("SqNoSelect Null Point Error");
            }

            sqNo++;

            mapForm.setLong("FILESQNO", sqNo);
            if (sqNo == 1) {

                String dvcd = mapForm.getString("거래구분");
                if( "DB".equalsIgnoreCase( dvcd)) {
                //if( "EA".equalsIgnoreCase( dvcd)) {

                	// EA->DB
                    //sqNo = 800001; //5030과 다름
                    sqNo = 600001;
                }else {

                    // VR->DA
                    //sqNo = 900001;
                    sqNo = 500001;
                }
                
                mapForm.setLong("FILESQNO", sqNo);
                ss.insert("NeoMapperCommon.SqNoInsert", mapForm);

            } else {
                ss.update("NeoMapperCommon.SqNoUpdate", mapForm);
            } // end if sqNo == 1

            ss.commit();

        } finally {
            ss.close();
        }

        return sqNo;
    }
}
