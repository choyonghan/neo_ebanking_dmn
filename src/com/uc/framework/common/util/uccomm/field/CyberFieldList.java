/**
 *  가상계좌상태변경 전문 송수신
 */
package com.uc.framework.common.util.uccomm.field;

import java.util.HashMap;

import com.uc.framework.common.util.StringUtil;
import com.uc.framework.common.util.uccomm.net.FieldList;

public class CyberFieldList {

	private HashMap<String, Object> dataMap = null;

	private FieldList field ;

	private int     len =  0;
	/**
	 * 생성자
	 */
	public CyberFieldList() {

		field = new FieldList();

		field.add( "MessageLength"      ,   4, "H" );

		field.add( "DCRM_CD", 9, "C" );//식별코드 - 'TAGCD001 '
		field.add( "SYS_DVCD", 3, "C" );//시스템구분코드 - 'VAS'
		field.add( "SV_DVCD", 3, "C" );//서비스구분코드 - '000'
		field.add( "ORGTCD", 2, "C" );//기관코드 - 사이버 : '04'
		field.add( "GRAM_MGNO", 10, "C" );//전문관리번호
		field.add( "GRAM_SPECD", 4, "C" );//전문종별코드 - 요청:'0800', 응답:'0810'
		field.add( "BIZ_DVCD", 3, "C" );//업무구분코드 - 개시:101, 종료:102, 장애:103, 장애회복:104
		field.add( "SNRC_FLAG", 1, "C" );//송수신FLAG - B
		field.add( "RPCD", 3, "C" );//응답코드 - 000
		field.add( "GRAM_TRS_DT", 8, "C" );//전문일자
		field.add( "GRAM_TRS_TM", 6, "C" );//전문시간
		field.add( "BANK_CN", 10, "C" );//은행사용 - 공백
		field.add( "ENPC", 8, "C" );//업체코드 - 사이버 : 'DGCYB001'
		field.add( "COMPRT_PPR_01", 30, "C" );//공통부예비1

		//len = field.getFieldListLen();

		this.dataMap = new HashMap<String, Object>();
	}

	public byte[] getBuff(HashMap<String, Object> mapForm) throws Exception{

		dataMap = mapForm;

		return getBuff();
	}

	public byte[] getBuff() throws Exception{

		byte[] headBuf ;

		/** 기본값 세팅 */
		//dataMap.put("MessageLength", Integer.toString(len));
		dataMap.put("MessageLength", "100");

		//dataMap.put("DCRM_CD", "TAGCD001 ");//식별코드
		dataMap.put("DCRM_CD", "時플쿨猝@");//식별코드
		dataMap.put("SYS_DVCD", "VAS");//시스템구분코드
		dataMap.put("SV_DVCD", "000");//서비스구분코드
		//dataMap.put("ORGTCD", "04");//기관코드
		dataMap.put("GRAM_MGNO", "");//전문관리번호
		dataMap.put("GRAM_SPECD", "0800");//전문종별코드
		dataMap.put("SNRC_FLAG", "B");//송수신FLAG
		dataMap.put("RPCD", "000");//응답코드
		dataMap.put("GRAM_TRS_DT", StringUtil.getCurrent("yyyyMMdd"));//전문전송일자
		dataMap.put("GRAM_TRS_TM", StringUtil.getCurrent("HHmmss"));//전문전송시간
		dataMap.put("BANK_CN", "");//은행사용 - 공백
		//dataMap.put("ENPC", "DGCYB001");//업체코드
		dataMap.put("COMPRT_PPR_01", "");//공통부예비1
		/** 기본값 세팅 */

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
