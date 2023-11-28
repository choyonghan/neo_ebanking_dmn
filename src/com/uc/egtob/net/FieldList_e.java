/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) ansi radix(10) lradix(10)
// Source File Name:   FieldList.java

package com.uc.egtob.net;

import com.uc.core.MapForm;
import com.uc.core.misc.Utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// Referenced classes of package com.uc.egtob.net:
//            Field

public class FieldList_e extends ArrayList
{

    public FieldList_e()
    {
        log = LogFactory.getLog(getClass());
    }

    public void add(String strFieldName, float fFieldSize, String strFieldType)
    {
        add(new Field(strFieldName, fFieldSize, strFieldType));
    }

    public void add(String strFieldName, float fFieldSize, String strFieldType, String code)
    {
        add(new Field(strFieldName, fFieldSize, strFieldType, code));
    }

    public void add(String strFieldName, float fFieldSize, String strFieldType, String code, String nameField)
    {
        add(new Field(strFieldName, fFieldSize, strFieldType, code, nameField));
    }

    public byte[] makeMessageByte(Object object)
    {
        byte byteMessage[] = new byte[getFieldListLen()];
        Field tmaxField = null;
        int offset = 0;
        Object value = null;
        for(Iterator iterator = iterator(); iterator.hasNext();)
        {
            tmaxField = (Field)iterator.next();
            String fieldName = tmaxField.getFieldName();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = getPropertyValue(object, fieldName);
            byte b[] = (byte[])null;
            if(fieldType.equals("B"))
            {
                if(value instanceof String)
                {
                    b = ((String)value).getBytes();
                } else
                {
                    b = new byte[(int)fieldSize];
                    if(value instanceof Integer)
                        b[0] = ((Integer)value).byteValue();
                    else
                    if(value instanceof Character)
                        b[0] = Character.getDirectionality(((Character)value).charValue());
                    else
                    if(value instanceof Byte)
                        b[0] = ((Byte)value).byteValue();
                    else
                    if(value instanceof byte[])
                        b = (byte[])value;
                }
            } else
            	/*5020 : 보낼때  euc-kr로 변환 후 전송*/
            	/* fixlength, subStringBytes 함수로 한글짤리는 현상 해결 5.30**/
        	try {
            if(fieldType.equals("C"))
				b = fixlength(0, (int)fieldSize, (String)value).getBytes("euc-kr");
			else
            if(fieldType.equals("D"))
                b = fixlength(0, (int)fieldSize, Utils.convertDate((String)value)).getBytes("euc-kr");
            else
            if(fieldType.equals("T"))
                b = fixlength(0, (int)fieldSize, Utils.convertTime((String)value)).getBytes("euc-kr");
            else
            if(fieldType.equals("DT"))
                b = fixlength(0, (int)fieldSize, Utils.convertDateTime((String)value)).getBytes("euc-kr");
            else
            if(fieldType.equals("N"))
                b = fixlength(2, (int)fieldSize, Utils.reConvertCash((String)value)).getBytes("euc-kr");
            else
            if(fieldType.equals("H"))
            {
                if((value instanceof String) || value == null)
                {
                    b = fixlength(2, (int)fieldSize, (String)value).getBytes("euc-kr");
                } else
                {
                    log.debug((new StringBuilder("-------------------")).append(value).toString());
                    b = fixlength(2, (int)fieldSize, String.valueOf(value)).getBytes("euc-kr");
                }
            } else
            if(fieldType.equals("G"))
                b = fixlength(1, (int)fieldSize, (String)value).getBytes("euc-kr");

            log.debug((new StringBuilder("전문필드 SET ")).append(fieldName).append("=").append(new String(b)).toString());
            System.arraycopy(b, 0, byteMessage, offset, b.length);
            offset = (int)((float)offset + fieldSize);
        	} catch (UnsupportedEncodingException e) {
				// TODO 자동 생성된 catch 블록
				e.printStackTrace();
			}
        }

        return byteMessage;
    }

    public String makeMessageText(Object bean)
        throws Exception
    {
        StringBuffer sbHeaderMessage = new StringBuffer();
        Field tmaxField = null;
        String value = null;
        for(Iterator iterator = iterator(); iterator.hasNext();)
        {
            tmaxField = (Field)iterator.next();
            String fieldName = tmaxField.getFieldName();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = (String)getPropertyValue(bean, fieldName);
            if(fieldType.equals("B"))
                throw new Exception((new StringBuilder("makeMessageText() 에서는 Byte 데이터를 처리할 수 없습니다(")).append(fieldName).append(")").toString());
            if(fieldType.equals("C"))
                sbHeaderMessage.append(Utils.fixlength(0, (int)fieldSize, value));
            else
            if(fieldType.equals("D"))
                sbHeaderMessage.append(Utils.fixlength(0, (int)fieldSize, Utils.convertDate(value)));
            else if(fieldType.equals("T"))
                sbHeaderMessage.append(Utils.fixlength(0, (int)fieldSize, Utils.convertTime(value)));
            else
            if(fieldType.equals("DT"))
                sbHeaderMessage.append(Utils.fixlength(0, (int)fieldSize, Utils.convertDateTime(value)));
            else
            if(fieldType.equals("N"))
                sbHeaderMessage.append(Utils.fixlength(2, (int)fieldSize, Utils.reConvertCash(value)));
            else
            if(fieldType.equals("H"))
                sbHeaderMessage.append(Utils.fixlength(2, (int)fieldSize, value));
            else
            if(fieldType.equals("G"))
                sbHeaderMessage.append(Utils.fixlength(1, (int)fieldSize, value));
        }

        return sbHeaderMessage.toString();
    }

    private Object getPropertyValue(Object bean, String fieldName)
    {
        if(fieldName.indexOf('.') > 0)
        {
            String subBeanName = fieldName.substring(0, fieldName.indexOf('.'));
            String subFldName = fieldName.substring(fieldName.indexOf('.') + 1);
            Object subBean = null;
            try
            {
                subBean = PropertyUtils.getSimpleProperty(bean, subBeanName);
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch(InvocationTargetException e)
            {
                e.printStackTrace();
            }
            catch(NoSuchMethodException e)
            {
                subBean = ((MapForm)bean).getMap(subBeanName);
            }
            if(subBean == null)
                return "";
            else
                return getPropertyValue(subBean, subFldName);
        } else {

			if (bean instanceof Map) {
				return ((Map) bean).get(fieldName);
			}
			if (bean instanceof HashMap) {
				return ((HashMap) bean).get(fieldName);
			} else if (bean instanceof MapForm) {
				return ((MapForm) bean).getMap(fieldName);
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

    private Object convertValue(byte value[], String fieldType, int fieldSize)
    {
    	// Date type : 20051025 -> 2005-10-25
        if(fieldType != null && fieldType.equals("D"))	return Utils.reConvertDate(new String(value));
        // Time type : 120000 -> 12:00:00
        else if(fieldType != null && fieldType.equals("T")) return Utils.reConvertTime(new String(value));
        // DateTime Data+Time
        else if (fieldType.equals("DT")) return Utils.reConvertDateTime(new String(value));
        // NumberType : 0000123456789 -> 123456789
        else if(fieldType.equals("N")) return  Utils.removeZero(new String(value));
        // NumberType : 0000123456789 -> 123,456,789
        else if(fieldType.equals("M")) return  Utils.convertCash(Utils.removeZero(new String(value)));
        // NumberType : 0000123456789 -> 123456789
        else if(fieldType.equals("H")) return  Utils.removeZero(new String(value));
        // String Type : 일반 String Type
        else if(fieldType.equals("C")) {

                try {
                    String  aa = new String(value, "euckr");
                    byte[]  v = aa.getBytes("euckr");
                    int i = v.length;
                    int a1_chk = 0;
                    if (i == 0) return null;

                    int l = i;

                    while (i-- > 0)
                    {

                        if( v[i] == 0xa1 )
                        {
                            if( a1_chk == 0 ) a1_chk = 1;
                            else
                            {
                                v[i+1] = 0x20;
                                v[i]   = 0x20;
                                a1_chk = 0;
                            }
                        }else{
                            if( a1_chk == 1 ) a1_chk = 0;
                        }
                    }
                    aa = new String( v , "euckr" );
                    v = aa.replaceAll( "  " , " " ).trim().getBytes("euckr");
                    byte[]  s = new byte[v.length];
                    System.arraycopy(v, 0, s, 0, v.length );

		    log.debug("[" + new String(value, "euckr") + "], 변환된값[" + new String(s, "euckr") + "]");
                    return new String(s, "euckr");
                } catch (UnsupportedEncodingException e) {
                        return null;
                }
        } else if(fieldType.equals("B")) return (Byte)value[0];
        //
        else return null;
    }

    public void parseMessageByte(byte byteMessageText[], Object bean, int iPos)
    {
        Field tmaxField = null;
        Object value = null;
        Iterator iterator = iterator();
        int nPos = iPos;
        while(iterator.hasNext())
        {
            tmaxField = (Field)iterator.next();
            String fieldName = tmaxField.getFieldName();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = new byte[(int)fieldSize];
            System.arraycopy(byteMessageText, nPos, value, 0, (int)fieldSize);
            nPos += (int)fieldSize;
            value = convertValue((byte[])value, fieldType, (int)fieldSize);
            if(bean instanceof MapForm)
                ((MapForm)bean).setMap(fieldName, value);
            else
                try
                {
                    PropertyUtils.setSimpleProperty(bean, fieldName, value);
                }
                catch(IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch(InvocationTargetException e)
                {
                    e.printStackTrace();
                }
                catch(NoSuchMethodException e)
                {
                    e.printStackTrace();
                }
        }
    }

    public void parseMessageText(String strMessageText, Object bean, int iPos)
    {
        Field tmaxField = null;
        Object value = null;
        Iterator iterator = iterator();
        int nPos = iPos;
        byte byteMessageText[] = strMessageText.getBytes();
        while(iterator.hasNext())
        {
            tmaxField = (Field)iterator.next();
            String fieldName = tmaxField.getFieldName();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = new byte[(int)fieldSize];
            System.arraycopy(byteMessageText, nPos, value, 0, (int)fieldSize);
            nPos += (int)fieldSize;
            value = convertValue((byte[])value, fieldType, (int)fieldSize);
            if(value instanceof byte[])
                value = new String((byte[])value);
            try
            {
                PropertyUtils.setSimpleProperty(bean, fieldName, value);
            }
            catch(IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch(InvocationTargetException e)
            {
                e.printStackTrace();
            }
            catch(NoSuchMethodException e)
            {
                e.printStackTrace();
            }
        }
    }

    public MapForm parseMessage(byte byteMessageText[], int iPos)
        throws Exception
    {
        if(byteMessageText.length < getFieldListLen())
            throw new Exception((new StringBuilder("수신전문 오류(Expect:")).append(getFieldListLen()).append(", Recv:").append(byteMessageText.length).append(")�Դϴ�").toString());
        MapForm map = new MapForm();
        Field tmaxField = null;
        Object value = null;
        Iterator iterator = iterator();
        int nPos = iPos;
        for(; iterator.hasNext(); map.setMap(tmaxField.getFieldName(), value))
        {
            tmaxField = (Field)iterator.next();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = new byte[(int)fieldSize];
            try
            {
                System.arraycopy(byteMessageText, nPos, value, 0, (int)fieldSize);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Exception((new StringBuilder("수신전문에 오류가 있습니다(")).append(tmaxField.getFieldName()).append(")").toString());
            }
            nPos += (int)fieldSize;
            value = convertValue((byte[])value, fieldType, (int)fieldSize);
        }

        return map;
    }

    public MapForm parseMessage(byte byteMessageText[], int iPos, MapForm map)
        throws Exception
    {
        Field tmaxField = null;
        Object value = null;
        Iterator iterator = iterator();
        int nPos = iPos;
        for(; iterator.hasNext(); map.setMap(tmaxField.getFieldName(), value))
        {
            tmaxField = (Field)iterator.next();
            float fieldSize = tmaxField.getFieldSize();
            String fieldType = tmaxField.getFieldType();
            value = new byte[(int)fieldSize];
            try
            {
                System.arraycopy(byteMessageText, nPos, value, 0, (int)fieldSize);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                throw new Exception((new StringBuilder("수신전문에 오류가 있습니다(")).append(tmaxField.getFieldName()).append(")").toString());
            }
            nPos += (int)fieldSize;
            value = convertValue((byte[])value, fieldType, (int)fieldSize);
        }

        return map;
    }

    public ArrayList parseRepeatedMessage(byte message[])
        throws Exception
    {
        return parseRepeatedMessage(message, 0);
    }

    public ArrayList parseRepeatedMessage(byte message[], int nStartPos)
        throws Exception
    {
        int cnt = (message.length - nStartPos) / getFieldListLen();
        if((message.length - nStartPos) % getFieldListLen() > 0)
            throw new Exception((new StringBuilder("반복전문의 길이(")).append(message.length - nStartPos).append(")와 필드길이(").append(getFieldListLen()).append(" )�� �Ǽ��� ��Ȯ���� �ʽ��ϴ�").toString());
        else
            return parseRepeatedMessage(message, nStartPos, cnt);
    }

    public ArrayList parseRepeatedMessage(byte message[], int nStartPos, int nCnt)
        throws Exception
    {
        int iPos = nStartPos;
        int iRowSize = 0;
        iRowSize = getFieldListLen();
        ArrayList retList = new ArrayList();
        log.debug((new StringBuilder("CNT=")).append(nCnt).append(", rowsize=").append(iRowSize).append(", msglen=").append(message.length).toString());
        for(int i = 0; i < nCnt; i++)
        {
            retList.add(parseMessage(message, iPos));
            iPos += iRowSize;
        }

        return retList;
    }

    public byte[] makeRepeatedMessage(MapForm mapList[])
    {
        int totSize = 0;
        byte tempByte[][] = new byte[mapList.length][];
        for(int i = 0; i < mapList.length; i++)
        {
            tempByte[i] = makeMessageByte(mapList[i]);
            totSize += tempByte[i].length;
        }

        byte retBuff[] = new byte[totSize];
        int offset = 0;
        for(int i = 0; i < mapList.length; i++)
        {
            System.arraycopy(tempByte[i], 0, retBuff, offset, tempByte[i].length);
            offset += tempByte[i].length;
        }

        return retBuff;
    }

    public byte[] makeRepeatedMessage(ArrayList msgList)
        throws Exception
    {
        int totSize = 0;
        byte tempByte[][] = new byte[msgList.size()][];
        for(int i = 0; i < msgList.size(); i++)
        {
            tempByte[i] = makeMessageByte(msgList.get(i));
            totSize += tempByte[i].length;
        }

        byte retBuff[] = new byte[totSize];
        int offset = 0;
        for(int i = 0; i < msgList.size(); i++)
        {
            System.arraycopy(tempByte[i], 0, retBuff, offset, tempByte[i].length);
            offset += tempByte[i].length;
        }

        return retBuff;
    }

    public int getFieldListLen()
    {
        Field tmaxField = null;
        int iSize = 0;
        for(Iterator iterator = iterator(); iterator.hasNext();)
        {
            tmaxField = (Field)iterator.next();
            float fieldSize = tmaxField.getFieldSize();
            iSize += (int)fieldSize;
        }

        return iSize;
    }

    public Field getField(String fieldName)
    {
        Field field = null;
        for(Iterator iterator = iterator(); iterator.hasNext();)
        {
            field = (Field)iterator.next();
            if(field.getFieldName().equals(fieldName))
                return field;
        }

        return null;
    }
   /*
    * out_len : 컬럼 크기
    * str : 바꿀문자
    * */
    public static String fixlength(int kind, int out_len, String str) {

		if ( str == null) str ="";
		str = subStringBytes(str, out_len, 2);
		byte[] input;
		try {
			input = str.getBytes("euckr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		byte [] temp = new byte [out_len];

		int i,j;
		int in_len = input.length;

		if ( kind == 2 )  {
			for (i=0; i<out_len; i++) {
				temp[i] =(byte) '0';
			}
		} else {
			for (i=0; i<out_len; i++) {
				temp[i] =(byte) ' ';
			}
		}

		// 입력된 길이보다 해당 String이 긴 경우
		if (in_len > out_len) 	in_len = out_len;
		// String enc =cropByte( input[out_len-1] ); // byte에 대한 한글 체크

		if (kind == 0)
			for (i=0; i<in_len; i++) {
				temp[i] = input[i];
			}
		else
			for (i=(out_len-in_len),j=0; i<out_len; i++,j++) {
				temp[i] = input[j];
			}

		// out_len에 해당하는 Byte가 한글일 경 우 byte가 짤리므로 공백으로 초기화 해준다.
		String output = new String(temp, 0, out_len );
		if( output.length() == 0 && out_len > 0)  temp[out_len-1] = (byte) ' ';

		try {
			output = new String(temp, 0, out_len, "euckr" );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		return output;
	}

    /*
     * str : 바꿀 문자
     * byteLength : 컬럼 크기
     * sizePerLetter : euc-kr 이면 2 , utf-8 이면 3
     * */
    public static String subStringBytes(String str, int byteLength, int sizePerLetter) {

    	  int retLength = 0;
    	  int tempSize = 0;
    	  int asc;

    	  if (str == null || "".equals(str) || "null".equals(str)) {
    	    str = "";
    	  }

    	  int length = str.length();

    	  for (int i = 1; i <= length; i++) {
    	    asc = (int) str.charAt(i - 1);

    	    if (asc > 127) {

    	      if (byteLength >= tempSize + sizePerLetter) {
    	        tempSize += sizePerLetter;
    	        retLength++;
    	      }

    	    } else {

    	      if (byteLength > tempSize) {
    	        tempSize++;
    	        retLength++;
    	      }
    	    }
    	  }

    	  return str.substring(0, retLength);
    	}
    private static final long serialVersionUID = 1L;
    protected Log log;
}


/*
	DECOMPILATION REPORT

	Decompiled from: E:\eGovFrameDev\workspace\dev-ebanking-ap\lib\egtob_1.0.jar
	Total time: 66 ms
	Jad reported messages/errors:
The class file version is 50.0 (only 45.3, 46.0 and 47.0 are supported)
Couldn't fully decompile method getPropertyValue
Couldn't resolve all exception handlers in method getPropertyValue
	Exit status: 0
	Caught exceptions:
*/