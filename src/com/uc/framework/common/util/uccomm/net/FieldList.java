/**
 *  주 시스템명 : 유채널 프레임웍
 *  업  무  명 : TMAX 연계
 *  기  능  명 : Tmax Field List
 *  클래스  ID : TmaxFieldList
 *  변경  이력 :
 * ------------------------------------------------------------------------
 *  작성자     소속  		 일자      Tag   내용
 * ------------------------------------------------------------------------
 *  김대완  유채널(주)  2007.11.23     %01%  신규작성
 *
 */

package com.uc.framework.common.util.uccomm.net;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.uc.framework.common.util.StringUtil;
import com.uc.framework.common.util.uccomm.CommonModel;

@SuppressWarnings("rawtypes")
public class FieldList extends ArrayList<Field> {

	private static final long serialVersionUID = 1L;

	protected Log log = LogFactory.getLog(this.getClass());

	private CommonModel commonModel;

	public FieldList(CommonModel commonModel) {
		super();
		this.commonModel = commonModel;
	}

	public FieldList() {
		super();
		this.commonModel = new CommonModel();
	}

	/**
	 * Field List 추가
	 * @param strFieldName  Field명
	 * @param nFieldSize    Field 크기
	 * @param strFieldType  Field 종류 ("C"-문자, "N"-"숫자")
	 */
	public void add(String strFieldName, float fFieldSize, String strFieldType) {
		add(new Field(strFieldName, fFieldSize, strFieldType));
	}

	/**
	 *
	 * @param strFieldName  Field명
	 * @param nFieldSize    Field 크기
	 * @param strFieldType  Field 종류 ("C"-문자, "N"-"숫자")
	 * @param code:         CommonModel 코드
	 */
	public void add(String strFieldName, float fFieldSize, String strFieldType, String code) {
		add(new Field(strFieldName, fFieldSize, strFieldType, code));
	}

	/**
	 *
	 * @param strFieldName   Field명
	 * @param nFieldSize     Field 크기
	 * @param strFieldType   Field 종류 ("C"-문자, "N"-"숫자")
	 * @param code :         CommonModel 코드
	 * @param nameField :    코드명을 담을 필드명
	 */
	public void add(String strFieldName, float fFieldSize, String strFieldType, String code, String nameField) {
		add(new Field(strFieldName, fFieldSize, strFieldType, code, nameField));
	}

	/**
	 * 전문 버퍼 생성
	 *
	 * @param object
	 * @return
	 */
	public byte[] makeMessageByte(Object object) {

		byte[] byteMessage = new byte[this.getFieldListLen()];

		Field tmaxField = null;
		String fieldName;

		int offset = 0;
		float fieldSize;
		String fieldType;
		Object value = null;

		Iterator<Field> iterator = iterator();

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();

			fieldName = tmaxField.getFieldName();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();

			value = getPropertyValue(object, fieldName);

			// log.debug( "fieldName : " + fieldName + "\nvalue : (" + (value ==
			// null ? "null" : value) +")" );

			byte[] b = null;

			// Char type
			if (fieldType.equals("B")) {
				if (value instanceof String) {
					b = ((String) value).getBytes();
				} else {
					b = new byte[(int) fieldSize];
					if (value instanceof Integer) {
						b[0] = Byte.parseByte(Integer.toString((Integer) value));
					} else if (value instanceof Character) {
						b[0] = (byte) Character.getNumericValue((Character) value);
					} else if (value instanceof Byte) {
						b[0] = (Byte) value;
					}
				}
			} else if (fieldType.equals("C")) { // String Type
				try {
					b = StringUtil.fixlength(0, (int) fieldSize, (String) value).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals("D")) { // Date type
				try {
					b = StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertDate((String) value)).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals("T")) { // Time type
				try {
					b = StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertTime((String) value)).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals("DT")) { // DateTime Type
				try {
					b = StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertDateTime((String) value)).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals("N")) { // Numeric type
				try {
					b = StringUtil.fixlength(2, (int) fieldSize, StringUtil.reConvertCash((String) value)).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else if (fieldType.equals("H")) {
				try {
					b = StringUtil.fixlength(2, (int) fieldSize, (String) value).getBytes("euckr");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			try {
				log.debug("전문필드 SET " + fieldName + "=" + new String(b, "euckr") + "], len[" + b.length + "]");
			} catch (UnsupportedEncodingException ex) {
				log.debug("전문필드 SET " + fieldName + "=" + new String(b) + "], len[" + b.length + "]");
			}
			// Copy
			System.arraycopy(b, 0, byteMessage, offset, b.length);

			offset += fieldSize;

		}
		return byteMessage;
	}

	/**
	 * 전문 문자열로 변환
	 *
	 * @throws Exception
	 */
	public String makeMessageText(Object bean) throws Exception {
		StringBuffer sbHeaderMessage = new StringBuffer();
		Field tmaxField = null;
		String fieldName;

		float fieldSize;
		String fieldType;
		String value = null;

		Iterator<Field> iterator = iterator();

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();

			fieldName = tmaxField.getFieldName();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();

			/************************************
			 * 필드값을 여러 객체안에 들어있어도 가져올 수 있게... 2008.04.14 -Freeb- 바쁜와중에 뻘짓...
			 *************************************/
			value = (String) getPropertyValue(bean, fieldName);
			// Byte type
			if (fieldType.equals("B")) { // Message Text에서는 Byte 데이터를 처리할 수 업다.
				throw new Exception("makeMessageText() 에서는 Byte 데이터를 처리할 수 없습니다(" + fieldName + ")");
			}
			// Char type
			else if (fieldType.equals("C")) {
				sbHeaderMessage.append(StringUtil.fixlength(0, (int) fieldSize, value));
			}
			// Date type
			else if (fieldType.equals("D")) {
				sbHeaderMessage.append(StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertDate(value)));
			}
			// Time type
			else if (fieldType.equals("T")) {
				sbHeaderMessage.append(StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertTime(value)));
			}
			// DateTime Type
			else if (fieldType.equals("DT")) {
				sbHeaderMessage.append(StringUtil.fixlength(0, (int) fieldSize, StringUtil.convertDateTime(value)));
			}
			// Numeric type
			else if (fieldType.equals("N")) { // 금액Type
				sbHeaderMessage.append(StringUtil.fixlength(2, (int) fieldSize, StringUtil.reConvertCash(value)));
			} else if (fieldType.equals("H")) { // 숫자Type
				sbHeaderMessage.append(StringUtil.fixlength(2, (int) fieldSize, value));
			}
		}
		return sbHeaderMessage.toString();
	}

	@SuppressWarnings("unchecked")
	private Object getPropertyValue(Object bean, String fieldName) {

		if (fieldName.indexOf('.') > 0) {
			String subBeanName = fieldName.substring(0, fieldName.indexOf('.'));
			String subFldName = fieldName.substring(fieldName.indexOf('.') + 1);
			Object subBean = null;
			try {
				subBean = PropertyUtils.getSimpleProperty(bean, subBeanName);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				subBean = ((HashMap<String, Object>) bean).get(subBeanName);
			}

			if (subBean == null) {
				return "";
			}
			return getPropertyValue(subBean, subFldName);
		} else {
			if (bean instanceof Map) {
				return ((Map) bean).get(fieldName);
			}
			if (bean instanceof HashMap) {
				return ((HashMap) bean).get(fieldName);
			} else if (bean instanceof Map) {
				return ((HashMap<String, Object>) bean).get(fieldName);
			} else if (bean instanceof String) { /* String 인 경우 그냥 Return 해버린다. */
				return bean;
			} else {
				try {
					return PropertyUtils.getSimpleProperty(bean, fieldName);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return "";
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					return "";
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
					return "";
				}
			}
		}
	}

	/**
	 * 수신받은 데이터를 TYPE에 맞게 변환한다.
	 *
	 * @param value
	 * @param fieldType
	 * @return
	 */
	private Object convertValue(byte[] value, String fieldType) {

		// Date type : 20051025 -> 2005-10-25
		if (fieldType != null && fieldType.equals("D"))
			return StringUtil.reConvertDate(new String(value));
		// Time type : 120000 -> 12:00:00
		else if (fieldType != null && fieldType.equals("T"))
			return StringUtil.reConvertTime(new String(value));
		// DateTime Data+Time
		else if (fieldType.equals("DT"))
			return StringUtil.reConvertDateTime(new String(value));
		// NumberType : 0000123456789 -> 123456789
		else if (fieldType.equals("N"))
			return StringUtil.removeZero(new String(value));
		// NumberType : 0000123456789 -> 123,456,789
		else if (fieldType.equals("M"))
			return StringUtil.convertCash(StringUtil.removeZero(new String(value)));
		// NumberType : 0000123456789 -> 123456789
		else if (fieldType.equals("H"))
			return StringUtil.removeZero(new String(value));
		// String Type : 일반 String Type
		else if (fieldType.equals("C")) {

			try {
				String aa = new String(value, "euckr");
				byte[] v = aa.getBytes("euckr");
				int i = v.length;
				int a1_chk = 0;

				if (i == 0) {
					return null;
				}

				while (i-- > 0) {

					if (v[i] == 0xa1) {
						if (a1_chk == 0) {
							a1_chk = 1;
						} else {
							v[i + 1] = 0x20;
							v[i] = 0x20;
							a1_chk = 0;
						}
					} else {
						if (a1_chk == 1) {
							a1_chk = 0;
						}
					}
				}
				aa = new String(v, "euckr");
				v = aa.replaceAll("  ", " ").trim().getBytes("euckr");
				byte[] s = new byte[v.length];
				System.arraycopy(v, 0, s, 0, v.length);

				log.debug("[" + new String(value, "euckr") + "], 변환된값[" + new String(s, "euckr") + "]");
				return new String(s, "euckr");
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		} else if (fieldType.equals("B"))
			return (Byte) value[0];
		//
		else
			return null;
	}

	/**
	 * 전문 문자열을 Field 객체로 변환함
	 *
	 * @param strMessageText
	 *            전문 문자열
	 * @param bean
	 *            FORM객체
	 * @param iPos
	 *            문자열 시작위치
	 */
	@SuppressWarnings("unchecked")
	public void parseMessageByte(byte[] byteMessageText, Object bean, int iPos) {
		Field tmaxField = null;
		String fieldName;
		float fieldSize;
		String fieldType;
		String codeId;
		String nameField;

		Object value = null;

		Iterator iterator = iterator();

		// int nPos = 0;
		int nPos = iPos;

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();
			fieldName = tmaxField.getFieldName();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();
			codeId = tmaxField.getCodeID();
			nameField = tmaxField.getNameField();

			value = new byte[(int) fieldSize];
			System.arraycopy(byteMessageText, nPos, value, 0, (int) fieldSize);

			// new String(byteMessageText, nPos, (int)fieldSize).trim();
			nPos = nPos + (int) fieldSize;

			/******************************************
			 * FIELD TYPE에 따라 데이터 변환
			 ******************************************/
			value = convertValue((byte[]) value, fieldType);

			if (bean instanceof Map) {
				((HashMap<String, Object>) bean).put(fieldName, value);
				if (codeId != null) { // 코드명 담아주기..
					if (nameField == null) {
						nameField = fieldName + "_name";
					}
					String codeVal = commonModel.getNameByCode(codeId, (String) value);
					((HashMap<String, Object>) bean).put(nameField, codeVal);
				}
			} else {
				try {
					PropertyUtils.setSimpleProperty(bean, fieldName, value);

					if (codeId != null && nameField != null) { // 필드에 코드명 담아주기..
						String codeVal = commonModel.getNameByCode(codeId, (String) value);
						PropertyUtils.setSimpleProperty(bean, nameField, codeVal);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 전문 문자열을 Field 객체로 변환함
	 *
	 * @param strMessageText
	 *            전문 문자열
	 * @param bean
	 *            FORM객체
	 * @param iPos
	 *            문자열 시작위치
	 */
	public void parseMessageText(String strMessageText, Object bean, int iPos) {
		Field tmaxField = null;
		String fieldName;
		float fieldSize;
		String fieldType;
		Object value = null;
		String codeId;
		String nameField;

		Iterator iterator = iterator();

		// int nPos = 0;
		int nPos = iPos;

		byte[] byteMessageText = strMessageText.getBytes();

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();
			fieldName = tmaxField.getFieldName();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();
			codeId = tmaxField.getCodeID();
			nameField = tmaxField.getNameField();

			value = new byte[(int) fieldSize];
			System.arraycopy(byteMessageText, nPos, value, 0, (int) fieldSize);
			// value = new String(byteMessageText, nPos, (int)fieldSize).trim();
			nPos = nPos + (int) fieldSize;

			/******************************************
			 * FIELD TYPE에 따라 데이터 변환
			 ******************************************/
			value = (String) convertValue((byte[]) value, fieldType);

			try {
				PropertyUtils.setSimpleProperty(bean, fieldName, value);

				if (codeId != null && nameField != null) { // 필드에 코드명 담아주기..
					String codeVal = commonModel.getNameByCode(codeId, (String) value);

					PropertyUtils.setSimpleProperty(bean, nameField, codeVal);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 전문 문자열을 HashMap<String, Object>() 객체로 변환함
	 *
	 * @param byteMessageText
	 *            전문문자열
	 * @param iPos
	 *            시작 위치
	 * @return map
	 * @throws Exception
	 */
	public HashMap<String, Object> parseMessage(byte[] byteMessageText, int iPos) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Field tmaxField = null;
		String fieldType;
		float fieldSize;
		Object value = null;

		String codeId = null;
		String nameField = null;

		Iterator iterator = iterator();

		int nPos = iPos;

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();

			codeId = tmaxField.getCodeID();
			nameField = tmaxField.getNameField();

			value = new byte[(int) fieldSize];

			try {
				System.arraycopy(byteMessageText, nPos, value, 0, (int) fieldSize);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("수신전문에 오류가 있습니다(" + tmaxField.getFieldName() + ")");
			}

			// value = new String(byteMessageText, nPos, (int)fieldSize).trim();
			nPos += (int) fieldSize;

			/******************************************
			 * FIELD TYPE에 따라 데이터 변환
			 ******************************************/
			value = convertValue((byte[]) value, fieldType);
			// log.debug("PARSE " + tmaxField.getFieldName() + "=" + value);

			map.put(tmaxField.getFieldName(), value);

			if (codeId != null) {
				if (nameField == null) {
					nameField = tmaxField.getFieldName() + "_name";
				}
				map.put(nameField, commonModel.getNameByCode(codeId, (String) value));
			}
		}

		return map;
	}

	/**
	 * 전문 문자열을 HashMap<String, Object>() 객체로 변환함
	 *
	 * @param byteMessageText
	 * @param iPos
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> parseMessage(byte[] byteMessageText, int iPos, HashMap<String, Object> map) throws Exception {

		Field tmaxField = null;
		String fieldType;
		float fieldSize;
		Object value = null;

		String codeId = null;
		String nameField = null;

		Iterator<?> iterator = iterator();

		int nPos = iPos;

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();
			fieldSize = tmaxField.getFieldSize();
			fieldType = tmaxField.getFieldType();

			codeId = tmaxField.getCodeID();
			nameField = tmaxField.getNameField();

			value = new byte[(int) fieldSize];

			try {
				System.arraycopy(byteMessageText, nPos, value, 0, (int) fieldSize);
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("수신전문에 오류가 있습니다(" + tmaxField.getFieldName() + ")");
			}

			nPos += (int) fieldSize;

			/******************************************
			 * FIELD TYPE에 따라 데이터 변환
			 ******************************************/
			value = convertValue((byte[]) value, fieldType);
			// log.debug("PARSE " + tmaxField.getFieldName() + "=" + value);

			map.put(tmaxField.getFieldName(), value);

			if (codeId != null) {
				if (nameField == null) {
					nameField = tmaxField.getFieldName() + "_name";
				}
				map.put(nameField, commonModel.getNameByCode(codeId, (String) value));
			}
		}
		return map;
	}

	/**
	 *
	 * @param message
	 * @param nStartPos
	 * @param nCnt
	 * @return
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, Object>> parseRepeatedMessage(byte[] message, int nStartPos, int nCnt) throws Exception {
		int iPos = nStartPos;
		int iRowSize = 0;
		iRowSize = this.getFieldListLen();

		ArrayList<HashMap<String, Object>> retList = new ArrayList<HashMap<String, Object>>();

		// byte[] byteMessageText = strMessageText.getBytes();
		// log.debug("CNT="+nCnt + ", rowsize=" + iRowSize + ", msglen="+
		// message.length);
		for (int i = 0; i < nCnt; i++) {
			retList.add(this.parseMessage(message, iPos));
			iPos += iRowSize;
		}
		return retList;
	}

	public byte[] makeRepeatedMessage(ArrayList<HashMap<String, Object>> msgList) throws Exception {

		int totSize = 0;
		byte[][] tempByte = new byte[msgList.size()][];

		for (int i = 0; i < msgList.size(); i++) {
			tempByte[i] = this.makeMessageByte(msgList.get(i));
			totSize += tempByte[i].length;
		}

		byte[] retBuff = new byte[totSize];

		int offset = 0;
		for (int i = 0; i < msgList.size(); i++) {
			System.arraycopy(tempByte[i], 0, retBuff, offset, tempByte[i].length);
			offset += tempByte[i].length;
		}

		return retBuff;
	}

	/**
	 * 전문 Field List 길이 추출
	 *
	 * @return int 필드전체 길이
	 */
	public int getFieldListLen() {
		Field tmaxField = null;
		float fieldSize;
		int iSize = 0;

		Iterator iterator = iterator();

		while (iterator.hasNext()) {
			tmaxField = (Field) iterator.next();
			fieldSize = tmaxField.getFieldSize();
			iSize = iSize + (int) fieldSize;
		}
		return iSize;
	}

	public Field getField(String fieldName) {
		Field field = null;
		Iterator<Field> iterator = iterator();

		while (iterator.hasNext()) {
			field = (Field) iterator.next();
			if (field.getFieldName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}
}
