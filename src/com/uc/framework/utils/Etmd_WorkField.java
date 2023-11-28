/**
 *  주시스템명 : 전북은행 e-세출 시스템
 *  업  무  명 : 공통
 *  기  능  명 : 전북은행 대외계연계 업무 공통전문
 *  클래스  ID : Edmd_WorkField
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자      	소속  		    일자            Tag           내용
 * ------------------------------------------------------------------------
 *  김대완       유채널(주)      2010.12.22         %01%         최초작성
 *
 */

package com.uc.framework.utils;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.uc.core.MapForm;
import com.uc.egtob.net.FieldList_e;

public class Etmd_WorkField {


	protected FieldList_e headField;

	protected FieldList_e sendField;

	protected FieldList_e recvField;

	protected FieldList_e repetField;

	private MapForm headMap;

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * 생성자
	 */
	public Etmd_WorkField() {

		headField = new FieldList_e();
		headField.add("전문길이" 		,    4,    "H");    // 전문길이
		headField.add("식별코드" 		,    9,    "C");    // 식별코드-position(5)
		headField.add("업체번호" 		,   12,    "C");    // 업체번호ID-position(14)
		headField.add("은행코드" 		,   2,    "C");    // 은행코드-position(26)
		headField.add("전문구분코드",   4 ,    "C");    // 전문구분코드-position(28)
		headField.add("업무구분코드",   3,    "C");    // 업무구분코드-position(32)
		headField.add("송신횟수"		,   1,    "C");    // 송신횟수-position(35)
		headField.add("전문번호"		,   6,    "H");    // 전문번호-position(36)
		headField.add("전송일자"		,   8,    "C");    // 전송일자-position(42)
		headField.add("전송시각"		,   6,    "C");    // 전송시각-position(50)
		headField.add("응답코드"		,   4,    "C");    // 응답코드-position(56)
		headField.add("예비", 9,    "C");    // 예비-position(60)

		/*header*/
		headField.add("파일구분"		,   2,    "C");    // 파일구분-position(69)
		headField.add("지급명령등록번호", 8,    "C");    // 지급명령번호-position(71)
		headField.add("재배정여부"	,   1,    "C");    // 재배정여부-position(79)
		headField.add("이체일자"		,   8,    "C");    // 이체일자-position(80)
		headField.add("파일코드"		,   14,    "C");    // 파일코드-position(88)
		headField.add("자료구분"		,   2,    "C");    // 자료구분-position(102)
		headField.add("출금계좌번호",   16,    "C");    // 출금계좌번호-position(104)
		headField.add("출금적요"		,   20,    "C");    // 출금적요-position(120)
		headField.add("출금계좌관리점"	,  3,    "C");    // 출금계좌관리점-position(140)
		headField.add("출금계좌관리점명", 20,    "C");    // 출금계좌관리점명-position(143)
		headField.add("출금계좌관리점전화번호",   20,    "C");    // 출금계좌관리점전화번호-position(163)
		headField.add("파일불능코드",   4,    "C");    // 파일불능코드-position(183)
		headField.add("파일불능내용",   50,    "C");    // 파일불능내용-position(187)
//		headField.add("파일식별KEY"	,   49,    "C");    // 파일식별
		/*파일식별KEY*/
		headField.add("요청ID"	,   2,    "C");    // 요청ID-position(237)
		headField.add("요청기관구분"	,   2,    "C");    // 요청기관구분-position(239)
		headField.add("자치단체코드"	,   7,    "C");    // 자치단체코드-position(241)
		headField.add("관서코드"	,   4,    "C");    // 관서코드-position(248)
		headField.add("지급부서코드"	,   7,    "C");    // 지급부서코드-position(252)
		headField.add("회계연도"	,   4,    "C");    // 회계연도-position(259)
		headField.add("회계코드"	,   3,    "C");    // 회계코드-position(263)
		headField.add("자료구분"		,   2,    "C");    // 자료구분-position(266)
		headField.add("지급명령등록번호"	,   8,    "C");    // 지급명령등록번호-position(268)
		headField.add("지급명령번호"		,   10,    "C");    // 지급명령번호-position(276)
		/*파일식별KEY END*/
		headField.add("자치단체명"	,   50,    "C");    // 자치단체명-position(286)
		headField.add("관서명"		,   50,    "C");    // 관서명-position(336)
		headField.add("지급부서명"	,   50,    "C");    // 지급부서명-position(386)
		headField.add("급여구분"		,   1,    "C");    // 급여구분-position(436)
		headField.add("불능분입금계좌번호",   16,    "C");    // 불능분입금계좌번호-position(437)
		/*header END*/
		/*trail 부분. */
		headField.add("전송레코드수",   7,    "H");    // 전송레코드수-position(453)
		headField.add("총의뢰건수"	,   7,    "H");    // 총의뢰건수-position(460)
		headField.add("총의뢰금액"	,   13,    "H");    // 총의뢰금액-position(473)
		headField.add("정상처리건수",   7,    "H");    // 총의뢰금액-position(486)
		headField.add("정상처리금액",   13,    "H");    // 정상처리금액-position(493)
		headField.add("미처리건수"	,   7,    "H");    // 미처리건수-position(500)
		headField.add("미처리금액"	,   13,    "H");    // 미처리금액-position(507)
		headField.add("지급명령총건수",7,    "H");    // 지급명령총건수-position(520)
		headField.add("지급명령총금액",13,    "H");    // 지금명령총금액-position(527)
//		headField.add("한도관리가상계좌", 16,    "C");    // 지금명령총금액-position(527)
//		headField.add("예비1",14,    "C");    // 지금명령총금액-position(527)

		sendField = new FieldList_e(); recvField = new FieldList_e(); repetField = new FieldList_e();
	}


	/**
	 * 송신전문 필드 정의
	 * @param name :: I/O정의서 내의 자료ID
	 * @param size :: 필드 길이
	 * @param type :: Char='C', Number='H', Byte='B'
	 */
	public void addSendField(String name, float size, String type) {

		if(sendField == null) sendField = new FieldList_e();
		sendField.add(name, size, type);
	}


	/**
	 * SEND 필드의 항목을 RECV필드에 복사
	 */
	@SuppressWarnings("unchecked")
	public void sendToRecv() {

		if(recvField == null) recvField = new FieldList_e();

		recvField.addAll(sendField);
	}

	/**
	 * 수신전문 필드정의
	 * @param name :: I/O정의서 내의 자료ID
	 * @param size :: 필드 길이
	 * @param type :: Char='C', Number='H', Byte='B'
	 */
	public void addRecvField(String name, float size, String type) {

		if(recvField == null) recvField = new FieldList_e();
		recvField.add(name, size, type);
	}

	/**
	 * 반복전문 필드정의
	 * @param name
	 * @param size
	 * @param type
	 */
	public void addRepetField(String name, float size, String type) {

		if(repetField == null) repetField = new FieldList_e();
		repetField.add(name, size, type);
	}

	/**
	 *
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	public MapForm parseHeadBuffer(byte[] recvBuff) throws Exception {

		return headField.parseMessage(recvBuff, 0);
	}

	/**
	 * 송신전문 생성
	 * @param dataMap
	 * @return
	 */
	public byte[] makeSendBuffer(MapForm dataMap)  {

		if(dataMap == null) dataMap = new MapForm();

		byte[] retBuff = new byte[headField.getFieldListLen() + (sendField == null?0:sendField.getFieldListLen())];

		if(dataMap.getMap("TX_KIND") == null) dataMap.setMap("TX_KIND", "00");

		byte[] headBuff =  headField.makeMessageByte(dataMap);

		if(sendField == null) return headBuff;	// dataField 가 null 이면 headField만 생성하도록

		byte[] dataBuff =  sendField.makeMessageByte(dataMap);

		System.arraycopy(headBuff, 0, retBuff, 0, headBuff.length);

		System.arraycopy(dataBuff, 0, retBuff, headBuff.length, dataBuff.length);

		return retBuff;
	}

	/**
	 * 수신(응답)전문 생성 :: 전문을 변환해서 전달해야 하는경우 사용해라
	 * @param dataMap
	 * @return
	 */
	public byte[] makeRecvBuffer(MapForm dataMap)  {

		if(dataMap == null) dataMap = new MapForm();

		byte[] retBuff = new byte[headField.getFieldListLen() + (recvField==null?0:recvField.getFieldListLen())];

		if(dataMap.getMap("TX_KIND") == null) dataMap.setMap("TX_KIND", "00");
		byte[] headBuff =  headField.makeMessageByte(dataMap);

		if(recvField == null) return headBuff;	// dataField 가 null 이면 headField만 생성하도록

		byte[] dataBuff =  recvField.makeMessageByte(dataMap);

		System.arraycopy(headBuff, 0, retBuff, 0, headBuff.length);

		System.arraycopy(dataBuff, 0, retBuff, headBuff.length, dataBuff.length);

		return retBuff;
	}

	/**
	 * 수신전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public MapForm parseRecvBuffer(byte[] recvBuff) throws Exception {

		headMap = headField.parseMessage(recvBuff, 0);
		MapForm recvMap = recvField.parseMessage(recvBuff, headField.getFieldListLen());

		recvMap.putAll(headMap);

		return recvMap;

	}

	/**
	 * 송신전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public MapForm parseSendBuffer(byte[] recvBuff) throws Exception {

		headMap = headField.parseMessage(recvBuff, 0);

		log.debug("FIELD SIZE==" + (headField.getFieldListLen() + sendField.getFieldListLen()) + ", RECV SIZE=" + recvBuff.length);
		MapForm sendMap = sendField.parseMessage(recvBuff, headField.getFieldListLen());

		sendMap.putAll(headMap);

		return sendMap;
	}

	/**
	 * 반복 포함 송신전문 생성
	 * @param dataMap
	 * @param reptList
	 * @return
	 * @throws Exception
	 */
	public byte[] makeSendReptBuffer(MapForm dataMap, @SuppressWarnings("rawtypes") ArrayList reptList) throws Exception {

		int reptSize = reptList.size() * repetField.getFieldListLen();

		log.debug(reptList.size() + " * " +repetField.getFieldListLen() );

		byte[] retBuff = new byte[headField.getFieldListLen() + sendField.getFieldListLen() + reptSize];

		byte[] dataBuff = makeSendBuffer(dataMap);

		byte[] reptBuff = repetField.makeRepeatedMessage(reptList);

		System.arraycopy(dataBuff, 0, retBuff, 0, dataBuff.length);

		System.arraycopy(reptBuff, 0, retBuff, dataBuff.length, reptBuff.length);

		return retBuff;
	}

	/**
	 * 반복 포함 수신전문 생성
	 * @param dataMap
	 * @param reptList
	 * @return
	 * @throws Exception
	 */
	public byte[] makeRecvReptBuffer(MapForm dataMap, @SuppressWarnings("rawtypes") ArrayList reptList) throws Exception {

		int reptSize = reptList.size() * repetField.getFieldListLen();

		byte[] retBuff = new byte[headField.getFieldListLen() + recvField.getFieldListLen() + reptSize];

		byte[] dataBuff = makeRecvBuffer(dataMap);

		byte[] reptBuff = repetField.makeRepeatedMessage(reptList);

		System.arraycopy(dataBuff, 0, retBuff, 0, dataBuff.length);

		System.arraycopy(reptBuff, 0, retBuff, dataBuff.length, reptBuff.length);

		return retBuff;
	}

	/**
	 * 반복부 포함하는 송신전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public MapForm parseSendReptBuffer(byte[] recvBuff) throws Exception {

		MapForm retMap = this.parseSendBuffer(recvBuff);

		int reptPos   =  headField.getFieldListLen() + sendField.getFieldListLen();

		log.debug("RECV 총의뢰건수 COUNT==" + headMap.getMap("총의뢰건수"));

		ArrayList<MapForm> retList = repetField.parseRepeatedMessage(recvBuff, reptPos, Integer.parseInt(headMap.getMap("총의뢰건수").toString()));

		retMap.setMap("repetList", retList);

		return retMap;
	}

	/**
	 * 반복부만 있는 전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MapForm> parseReptBuffer(byte[] recvBuff) throws Exception {

		headMap = this.parseHeadBuffer(recvBuff);

		int reptPos   =  headField.getFieldListLen() ;

		log.debug("RECV COUNT==" + headMap.getMap("총의뢰건수"));

		return repetField.parseRepeatedMessage(recvBuff, reptPos, Integer.parseInt(headMap.getMap("총의뢰건수").toString()));

	}

	/**
	 * 반복부만 있는 전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MapForm> parseReptBuffer(byte[] recvBuff, int pos) throws Exception {

		return repetField.parseRepeatedMessage(recvBuff, pos);
	}

	/**
	 * 반복부만 있는 전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MapForm> parseReptBuffer(byte[] recvBuff, int pos, int cnt) throws Exception {

		return repetField.parseRepeatedMessage(recvBuff, pos, cnt);
	}

	/**
	 * 반복부 포함하는 수신전문 Parsing
	 * @param recvBuff
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public MapForm parseRecvReptBuffer(byte[] recvBuff) throws Exception {

		MapForm retMap = this.parseRecvBuffer(recvBuff);

		int reptPos   =  headField.getFieldListLen() + recvField.getFieldListLen();

		ArrayList<MapForm> retList = repetField.parseRepeatedMessage(recvBuff, reptPos, Integer.parseInt(headMap.getMap("총의뢰건수").toString()));

		retMap.setMap("repetList", retList);

		return retMap;
	}

	/**
	 * @param headMap the headMap to set
	 */
	public void setHeadMap(MapForm headMap) {
		this.headMap = headMap;
	}

	/**
	 * @return the headMap
	 */
	public MapForm getHeadMap() {
		return headMap;
	}

	/**
	 * 업무공통부 전문길이 Return
	 * @return
	 */
	public int getHeadLen() {

		return headField.getFieldListLen();
	}

	public int getRepetLen() {

		return repetField.getFieldListLen();
	}
}
