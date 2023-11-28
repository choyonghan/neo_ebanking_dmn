/**
 *  주 시스템명 : 유채널 프레임웍
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

public class EhSCFieldList {

	private HashMap<String, Object> dataMap = null;

	private FieldList field ;

	private int     len =  0;
	/**
	 * 생성자
	 */
	public EhSCFieldList() {

		field = new FieldList();

		field.add("MessageLength"        ,  4 , "H");
		field.add("TrDateTime"           , 14 , "C");
		field.add("FILL"                 ,  4 , "C");
		field.add("MessageType"          ,  4 , "C");
		field.add("MessageDesc"          ,  2 , "C");
		field.add("MessageRequestCode"   ,  4 , "C");
		field.add("TXSEQ"                , 14 , "C");
		field.add("UpcheReq"             , 10 , "C");
		field.add("Errcode"              ,  4 , "C");
		field.add("BANKCODE"             ,  3 , "C");
		field.add("ACCOUNT"              , 16 , "C");
		field.add("NAME"                 , 24 , "C"); // 24->40
		field.add("SNAME"                , 24 , "C"); // 24->40
		field.add("CURRENCY"             , 12 , "N");
		field.add("ERRMSG"               ,100 , "C");

		/** 2022.09.01 추가 */
		field.add("locGovCd", 7, "C");//자치단체코드
		field.add("trsfrAmt", 15, "N");//이체금액
		field.add("trsfrPsbYn", 1, "C");//이체가능여부
		field.add("athBanSlryChltCd", 2, "C");//압류금지급여성격코드
		field.add("inqAccRnmNo", 10, "C");//조회계좌실명번호
		field.add("aidBzrAccYn", 1, "C");//보조사업자계좌여부

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

		dataMap.put("MessageLength", Integer.toString(len));
		dataMap.put("TrDateTime"   , StringUtil.getCurrent("yyyyMMddHHmmss"));
		dataMap.put("FILL"         , "0000");
		dataMap.put("MessageType"  , msgType);
		dataMap.put("MessageDesc"  , "09");
		dataMap.put("MessageRequestCode"  , "0200");
		dataMap.put("UpcheReq"     , srcId);
		dataMap.put("Errcode"      , "0000");

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
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		return dataMap;
	}


	public int getLen() {
		// TODO Auto-generated method stub
		return len;
	}


	public FieldList getFieldList() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		this.dataMap.put(key, val);
	}


	public HashMap<String, Object> getDataMap() {
		// TODO Auto-generated method stub
		return this.dataMap;
	}


	/**
	 *
	 * @param mapForm
	 */
	public void setDataMap(HashMap<String, Object> mapForm) {
		// TODO Auto-generated method stub
		this.dataMap = mapForm;
	}

}
