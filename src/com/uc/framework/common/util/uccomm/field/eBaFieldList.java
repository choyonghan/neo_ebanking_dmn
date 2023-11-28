/**
 *  주 시스템명 : 유채널 프레임웍//
 *  업  무  명 : 내부 전문 송수신 연계
 *  기  능  명 : 전문 송수신 헤더
 *  클래스  ID : KCNLHeader
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자     소속  		 일자      Tag   내용
 * ------------------------------------------------------------------------
 *  김대완  유채널(주)  2007.11.23     %01%  신규작성
 *
 */

package com.uc.framework.common.util.uccomm.field;

import java.util.HashMap;

import com.uc.framework.common.util.StringUtil;
import com.uc.framework.common.util.uccomm.net.FieldList;

public class eBaFieldList {

	private HashMap<String, Object> dataMap = null;

	private FieldList field ;

	private int     len =  0;
	/**
	 * 생성자
	 */
	public eBaFieldList() {

		field = new FieldList();

		field.add( "MessageLength"      ,   4, "H" );
		field.add( "TrDateTime"         ,  14, "C" );
		field.add( "FILL"               ,   4, "C" );
		field.add( "MessageType"        ,   4, "C" );
		field.add( "MessageDesc"        ,   2, "C" );
		field.add( "MessageRequestCode" ,   4, "C" );
		field.add( "TXSEQ"              ,  14, "C" );
		field.add( "UpcheReq"           ,  10, "C" );
		field.add( "Errcode"            ,   4, "C" );

		field.add( "TRANS_SIZE"         ,   4, "H" ); // 전문길이
		field.add( "HOST_TCODE"         ,   9, "C" ); // HOST TRANSACTION CODE
		field.add( "TRAN_KIND"          ,   4, "C" ); // 전문종류별코드
		field.add( "TRDV_CODE"          ,   4, "C" ); // 거래구분코드
		field.add( "SPARE"              ,   1, "C" ); // 예비
		field.add( "SYS_CODE"           ,   1, "C" ); // 시스템코드
		field.add( "RES_CODE"           ,   4, "C" ); // 응답코드
		field.add( "TRAN_DATE"          ,   8, "C" ); // 전문전송일자
		field.add( "TRAN_SEQ"           ,   8, "C" ); // 전문일련번호
		field.add( "TRAN_TIME"          ,   6, "C" ); // 전문전송시간
		field.add( "TRAN_ETC"           ,  50, "C" ); // 비고
		field.add( "ERR_CODE"           ,   4, "C" ); // 오류모듈코드
		field.add( "ERR_TNAME"          ,   8, "C" ); // 오류TASK명
		field.add( "ERR_MNAME"          ,   8, "C" ); // 오류모듈명
		field.add( "ERRMSG"             ,  70, "C" ); // 오류메세지1
		field.add( "UpcheReq"           ,  10, "C" ); // 업체식별번호
		field.add( "BANKCODE"           ,   3, "C" ); // 은행코드
		field.add( "WTHI_ACCOUNT"       ,  19, "C" ); // 계좌번호
		field.add( "BALANCE"            ,  16, "C" ); // 계좌잔액
		field.add( "FILLER"             , 275, "C" ); // FILLER

		/** TOBE 추가 */
		field.add("accInqDvCd", 1, "C");//계좌조회구분코드
		field.add("inqAmt", 14, "N");//조회금액
		field.add("drwPsbYn", 1, "C");//출금가능여부

		len = field.getFieldListLen();

		this.dataMap = new HashMap<String, Object>();
	}


	/**
	 * 전문 엔진에 송신할 헤더 생성
	 * @param cmd      : 2:등록, 3:데이터
	 * @param sbCmd    : 1:First, 2:Middle, 3:Last
	 * @param srcId    : 응답받을 서버 ID
	 * @param destId   : 전송할 대상 ID
	 * @param buffSize : 데이터부 길이
	 * @return
	 */
	public byte[] getBuff(String msgType, String srcId, HashMap<String, Object> mapForm) throws Exception{

		dataMap = mapForm;

		return getBuff(msgType, srcId);
	}


	/**
	 *
	 * @param msgType
	 * @param srcId
	 * @return
	 * @throws Exception
	 */
	public byte[] getBuff(String msgType, String srcId) throws Exception{

		byte[] headBuf ;

 	   String vTrNo   = "" ; // 전문일련번호
	   String vEtc    = "" ; // 비고
	   String vErrMCd = "" ; // 오듀모듈코드
	   String vErrTNm = "" ; // 오류TASK명
	   String vErrMNm = "" ; // 오류모듈명
	   String vErrMsg = "" ; // 오류메세지1
	   String vRsCd   = "" ;


		dataMap.put( "MessageLength"       , Integer.toString( len ));
		dataMap.put( "TrDateTime"          , StringUtil.getCurrent( "yyyyMMddHHmmss" ));
		dataMap.put( "FILL"                , "0000"  );
		dataMap.put( "MessageType"         , msgType );
		dataMap.put( "MessageDesc"         , "09"    );
		dataMap.put( "MessageRequestCode"  , "0200"  );
		dataMap.put( "UpcheReq"            , srcId   );
    	dataMap.put( "Errcode"             , "0000"  );


	   dataMap.put( "TRANS_SIZE" ,  Integer.toString(len));           	 // 전문길이
	   dataMap.put( "HOST_TCODE" ,  "GGVIDRTR"           );               // HOST TRANSACTION CODE
	   dataMap.put( "TRAN_KIND"  ,  "8000"               );               // 전문종류별코드
	   dataMap.put( "TRDV_CODE"  ,  "0200"               );               // 거래구분코드
	   dataMap.put( "SPARE"      ,  " "                  );               // 예비
	   dataMap.put( "SYS_CODE"   ,  "H"                  );               // 시스템코드
	   dataMap.put( "RES_CODE"   ,  StringUtil.fixlength( 0 , 4 , vRsCd    )); // 응답코드
	   dataMap.put( "TRAN_DATE"  ,  StringUtil.getCurrent( "yyyyMMdd"      )); // 전문전송일자
	   dataMap.put( "TRAN_SEQ"   ,  StringUtil.fixlength( 0 ,  8 , vTrNo   )); // 응답코드
	   dataMap.put( "TRAN_TIME"  ,  StringUtil.getCurrent( "HHmmss"        )); // 전문전송시간
	   dataMap.put( "TRAN_ETC"   ,  StringUtil.fixlength( 0 , 50 , vEtc    )); // 비고
	   dataMap.put( "ERR_CODE"   ,  StringUtil.fixlength( 0 ,  4 , vErrMCd )); // 오류모듈코드
	   dataMap.put( "ERR_TNAME"  ,  StringUtil.fixlength( 0 ,  8 , vErrTNm )); // 오류TASK명
	   dataMap.put( "ERR_MNAME"  ,  StringUtil.fixlength( 0 ,  8 , vErrMNm )); // 오류모듈명
	   dataMap.put( "ERRMSG"     ,  StringUtil.fixlength( 0 , 70 , vErrMsg )); // 오류메세지1
	   dataMap.put( "UpcheReq"   ,  srcId                );               // 업체식별번호

		headBuf =  field.makeMessageByte(dataMap);

		return headBuf;

	}

	public byte[] getBuff(HashMap<String, Object> mapForm) throws Exception{

		dataMap = mapForm;

		return getBuff();
	}

	public byte[] getBuff() throws Exception{

		byte[] headBuf ;

		dataMap.put("MessageLength", Integer.toString(len));

		headBuf =  field.makeMessageByte(dataMap);

		return headBuf;

	}

	/**
	 *
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> parseBuff(byte[] buffer) throws Exception{

			try {
				dataMap = field.parseMessage(buffer, 0);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		return dataMap;
	}

	/**
	 *
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> parseBuff(byte[] buffer, int pos) throws Exception{

			try {
				dataMap = field.parseMessage(buffer, pos);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		return dataMap;
	}

	public int getLen() {
		return len;
	}


	public FieldList getFieldList() {
		return field;
	}

	/**
	 *
	 * @param fldName
	 * @return
	 */
	public String getField(String fldName) {

		return (String) dataMap.get(fldName);
	}


	/**
	 *
	 * @param key
	 * @param val
	 */
	public void setField(String key, String val) {
		this.dataMap.put(key, val);
	}


	public HashMap<String, Object> getDataMap() {
		return this.dataMap;
	}


	/**
	 *
	 * @param mapForm
	 */
	public void setDataMap(HashMap<String, Object> mapForm) {
		this.dataMap = mapForm;
	}

}
