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

package com.uc.framework.common.util.uccomm;

import java.util.HashMap;

import com.uc.framework.common.util.CommonUtils;
import com.uc.framework.common.util.uccomm.net.FieldList;

public class UcCommHead {


	private FieldList headField ;

	private int      len   = 0;
	/**
	 * 생성자
	 */
	public UcCommHead() {

		headField = new FieldList();

		headField.add("DESC"      , 4, "C");
		headField.add("COMMAND"   , 1, "C");  //  1:init, 2:close, 3:data, 5:system, 7:poll
		headField.add("RESOPT"    , 1, "C");  //  1: Request, 2:Response OK, 5:Response NG
		headField.add("SRCID"     , 4, "C");  // 전송ID -- 웹처럼 1회성 데이터 송수신 일 경우 초기 ID를 '0000'으로 SET 한다.
		headField.add("DESTID"    , 4, "C");
		headField.add("TRDATE"    , 8, "C");
		headField.add("TRTIME"    , 6, "C");
		headField.add("RESCD"     , 4, "C");
		headField.add("RESERVE"   , 8, "C");
		headField.add("DTSIZE"    , 8, "H");

		len = headField.getFieldListLen();
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
	public byte[] getHeadBuff(String cmd, String resOpt, String srcId, String destId, String resCd, long buffSize) throws Exception{

		byte[] headBuf ;

		HashMap<String, Object> headMap = new HashMap<String, Object>();

		headMap.put("DESC"     ,  "UCNH");
		headMap.put("COMMAND"  ,  cmd);
		headMap.put("RESOPT"   ,  resOpt);
		headMap.put("SRCID"    ,  srcId);
		headMap.put("DESTID"   ,  destId);
		headMap.put("TRDATE"   ,  CommonUtils.getCurrentDate().substring(0, 6));
		headMap.put("TRTIME"   ,  CommonUtils.getCurrentTime().substring(0, 6));
		headMap.put("RESCD"    ,  resCd);
		headMap.put("RESERVE"  , "w2.0");
		headMap.put("DTSIZE"   , Long.toString(buffSize));

		headBuf =  headField.makeMessageByte(headMap);

		return headBuf;
	}


	/**
	 *
	 * @param buffer
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> parseHeadBuff(byte[] buffer) throws Exception{


			HashMap<String, Object> headMap = null;

			try {
				headMap = headField.parseMessage(buffer, 0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		return headMap;
	}


	public int getLen() {
		// TODO Auto-generated method stub
		return this.len;

	}


	public FieldList getFieldList() {
		// TODO Auto-generated method stub
		return this.headField;
	}


}
