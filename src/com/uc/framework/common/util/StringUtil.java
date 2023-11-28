package com.uc.framework.common.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * @fileName   : StringUtil.java
 * @package     : kr.go.sntest.common.util
 * @author : 추대호
 * @date       : 2014. 10. 22. 오전 11:38:07
 */
public class StringUtil {

	/**
		 * @Method Name            : arrayJoin
		 * @Method description : String 배열을 구분자로 연결된 String으로 변환
		 * @author                            : 추대호
		 * @date                                  : 2014. 10. 22. 오전 11:38:10
		 * ------------------------------------ Change History -------------------------------------
		 * Modification Log :
		 * Date                                                            Programmer                    Description
		 * 2014. 10. 22. 오전 11:38:10                  추대호                          Create
		 * @param glue
		 * @param array
		 * @return
		 */
	public  static String arrayJoin(String glue, String array[]){
		String result = "";
		if(null != array){
			for(int i=0;i<array.length;i++){
				result += array[i];
				if(i < array.length-1) result += glue;
			}
		}
		return result;
	}

	/**
		 * @Method Name            : isEmpty
		 * @Method description : 넘어온 parameter가 null이거나 빈 값이면 true 반환 아니면 false 반환
		 * @author                            : 추대호
		 * @date                                  : 2014. 10. 22. 오전 11:38:33
		 * ------------------------------------ Change History -------------------------------------
		 * Modification Log :
		 * Date                                                            Programmer                    Description
		 * 2014. 10. 22. 오전 11:38:33                  추대호                          Create
		 * @param obj
		 * @return
		 */
	@SuppressWarnings("rawtypes")
	public static boolean isEmpty(Object obj){
        if( obj instanceof String ) return obj==null || "".equals(obj.toString().trim());
        else if( obj instanceof List ) return obj==null || ((List)obj).isEmpty();
        else if( obj instanceof Map ) return obj==null || ((Map)obj).isEmpty();
        else if( obj instanceof Object[] ) return obj==null || Array.getLength(obj)==0;
        else return obj==null;
    }


    /**
    	 * @Method Name            : isNotEmpty
    	 * @Method description : 넘어온 parameter가 null이거나 빈 값이면 false 반환 아니면 true 반환
    	 * @author                            : 추대호
    	 * @date                                  : 2014. 10. 22. 오전 11:38:44
    	 * ------------------------------------ Change History -------------------------------------
    	 * Modification Log :
    	 * Date                                                            Programmer                    Description
    	 * 2014. 10. 22. 오전 11:38:44                  추대호                          Create
    	 * @param s
    	 * @return
    	 */
    public static boolean isNotEmpty(Object obj){
        return !isEmpty(obj);
    }

    /**
	*
	* 입력 데이타를 해당 길이만큼 채워준다. default => X 모드
	* @see
	* @param	int len
	* @param	String data
	* @return	String
	* @throws
	*/
  public static String expand(int len, String data)
  {
    return expand(len, "9", data);
  }

	/**
	*
	* 입력 데이타를 해당 길이만큼 채워준다.
	* @see
	* @param	int len
	* @param	String mode
	* @param	String data
	* @return	String
	* @throws
	*/
  public static String expand(int len, String mode, String data)
  {
    if( data == null ) return data;
    return expand(len, mode, data.getBytes());
  }

	/**
	*
	* 입력 데이타를 해당 길이만큼 채워준다. default => X 모드
	* @see
	* @param	int len
	* @param	String mode
	* @param	byte[] data
	* @return	String
	* @throws	ArrayIndexOutOfBoundsException
	*/
  public static String expand(int len, String mode, byte[] data)
    throws ArrayIndexOutOfBoundsException
  {
    byte[] resultBytes = new byte[len];

    try
    {

    	if(null != data){
	      // 9 mode
	      if( "9".equals(mode) )
	      {
	        int i = 0;
	        for ( ; i < len - data.length ; i++ )
	          resultBytes[i] = '0';

	        int j = 0;
	        for ( ; i < len ; i++)
	          resultBytes[i] = data[j++];
	      }
	      // X mode or C(암호화) mode
	      else if ( "X".equalsIgnoreCase(mode) || "C".equalsIgnoreCase(mode) )
	      {
	        int i = 0;
	        for ( ; i < data.length ; i++ )
	          resultBytes[i] = data[i];

	        for ( ; i < len ; i++ )
	          resultBytes[i] = ' ';
	      }
    	}
    }catch(NumberFormatException e){
        throw new ArrayIndexOutOfBoundsException("Data[" + new String(data) +
                "] ===> 길이 ["+ len +"] 범위를 초과하였습니다.");
    }catch(Exception e)
    {
      throw new ArrayIndexOutOfBoundsException("Data[" + new String(data) +
          "] ===> 길이 ["+ len +"] 범위를 초과하였습니다.");
    }

    return new String(resultBytes);
  }

	public static String textToTextarea(String str)
	{
		if (str==null)
		{
			return null;
		}

		String temp = str;
		temp=strReplace(temp, "\"", "&quot;");
		temp=strReplace(temp, "\'", "&#39;");
		temp=strReplace(temp, "<", "&lt;");
		temp=strReplace(temp, ">", "&gt;");
		return temp;
	}

  /**
   * html을 text로 바꾼다.
   * @param String str 변환될 스트링
   * @return String temp 변환된 스트링
   */
/*  public static String textToHtml(String str)
  {
    if (str==null)
    {
      return null;
    }

    String temp = str;
    temp=strReplace(temp, "\n", "<br>");
    temp=strReplace(temp, " ", "&nbsp;");
    temp=strReplace(temp, "\"", "&quot;");
    temp=strReplace(temp, "\'", "&#39;");
//    temp=strReplace(temp, "&lt;", "<");
//    temp=strReplace(temp, "&gt;", ">");
    return temp;
  }
*/

	/**
	*
	* html code를 text로 바꾼다.
	* @see
	* @param	String str
	* @return	String
	* @throws
	*/
  public static String textToHtml(String str)
  {
    if (str==null)
    {
      return null;
    }

    String temp = str;
    //temp=strReplace(temp, " ", "&nbsp;");
    temp=strReplace(temp, "&nbsp;", " ");
    temp=strReplace(temp, "\"", "&quot;");
    temp=strReplace(temp, "\'", "&#39;");
    temp=strReplace(temp, "<", "&lt;");
    temp=strReplace(temp, ">", "&gt;");
		temp=strReplace(temp, "\r\n", "<br/>");
		temp=strReplace(temp, "\n", "<br/>");
        temp=strReplace(temp, "?", "&#63;");
    return temp;
  }

    /**
    *
    * html code를 text로 바꾼다.
    * @see
    * @param    String str
    * @return   String
    * @throws
    */
    public static String textToHtmlNoSpace(String str)
    {
      if (str==null)
      {
        return null;
      }

      String temp = str;
//      temp=strReplace(temp, " ", "&nbsp;");
      temp=strReplace(temp, "&nbsp;", " ");
      temp=strReplace(temp, "\"", "&quot;");
      temp=strReplace(temp, "\'", "&#39;");
      temp=strReplace(temp, "<", "&lt;");
      temp=strReplace(temp, ">", "&gt;");
        temp=strReplace(temp, "\r\n", "<br>");
        temp=strReplace(temp, "\n", "<br>");
      temp=strReplace(temp, "?", "&#63;");
      return temp;
    }



	/**
	*
	* html code를 <br> 태그없이 text로 바꾼다.
	* @see
	* @param	String str
	* @return	String
	* @throws
	*/
  public static String textToHtmlNoBR(String str)
  {
    if (str==null)
    {
      return null;
    }

    String temp = str;
//    temp=strReplace(temp, " ", "&nbsp;");
    temp=strReplace(temp, "&nbsp;", " ");
    temp=strReplace(temp, "\"", "&quot;");
    temp=strReplace(temp, "\'", "&#39;");
    temp=strReplace(temp, "<", "&lt;");
    temp=strReplace(temp, ">", "&gt;");
    return temp;
  }

	/**
	*
	* 스트링에서 특정 문자열을 원하는 문자열로 바꾼다.
	*  특정 스트링에서 어떤 문자열을 원하는 문자열로 바꿀때 사용된다.
	*  "that is red pig" 라는 문자열에서 "is"를 "was"로 바꾸려 할때는
	*  TAUtil.strReplace("this is red pig", "is", "was")
	*  와 같이 호출하면 "this was red pig"를 리턴한다.
	* @see
	* @param	String dest
	* @param	String src
	* @param	String rep
	* @return	String
	* @throws
	*/
  public  static String strReplace(String dest,String src,String rep)
  {
    String retstr="";
    String left="";
    int pos=0;
    if(null == dest || null == src || null == rep)
    {
      return retstr;
    }

    while ( true )
    {
      if( (pos=dest.indexOf(src))!= -1 )
      {
        left = dest.substring(0, pos);
        dest = dest.substring(pos+src.length(), dest.length());
        retstr=retstr+left+rep;
        pos=pos+src.length();
      }
      else
      {
        retstr=retstr+dest;
        break;
      }
    }
    return retstr;
  }


 /**
	*
	* 시간의 /형식을 :형식으로 변환하여 리턴한다.
	* @see
	* @param	String dest
	* @param	String src
	* @param	String rep
	* @return	String
	* @throws
	*/
	public static String formatDateTimeString(String dateTime)
	{
	  return formatDateTimeString(dateTime, "/", ":");
	}

 /**
	*
	* 시간의 표현 형식을 변환하여 리턴한다.
	* @see
	* @param	String dateTime
	* @param	String dateDelim
	* @param	String timeDelim
	* @return	String
	* @throws
	*/
	public static String formatDateTimeString(String dateTime, String dateDelim, String timeDelim) {
	  if ( dateTime != null && dateTime.length() >= 14 )
	    return dateTime.substring(0, 4) + dateDelim + dateTime.substring(4, 6) +
	        dateDelim + dateTime.substring(6, 8) + " " +
	        dateTime.substring(8, 10) + timeDelim + dateTime.substring(10, 12) + timeDelim +
	        dateTime.substring(12);
	  else
	    return dateTime;
	}

 /**
	*
	* 시간의 표현 형식을 변환하여 리턴한다.
	* @see
	* @param	String date
	* @param	String delim
	* @return	String
	* @throws
	*/
	public static String formatDateString(String date, String delim) {
	  if ( date != null && date.length() >= 8 )
	    return date.substring(0, 4) + delim + date.substring(4, 6) + delim +
	        date.substring(6, 8);
	  else if ( date != null && date.length() >= 6 )
      return date.substring(0, 4) + delim + date.substring(4, 6);
	    return date;
	}

	/**
	*
	* 시간의 표현 형식을 변환하여 리턴한다.
	* @see
	* @param	String date
	* @return	String
	* @throws
	*/
	public static String formatDateString(String date)
	{
	  return formatDateString(date, "-");
	}

 /**
	*
	* 시간의 표현 형식을 변환하여 리턴한다.
	* @see
	* @param	String time
	* @param	String delim
	* @return	String
	* @throws
	*/
	public static String formatTimeString(String time, String delim) {
	  if ( time != null && time.length() >= 6 )
	    return time.substring(0, 2) + delim + time.substring(2, 4) + delim +
	        time.substring(4, 6);
	  else
	    return time;
	}

	/**
	*
	* 시간의 표현 형식을 변환하여 리턴한다.
	* @see
	* @param	String time
	* @return	String
	* @throws
	*/
	public static String formatTimeString(String time)
	{
	  return formatTimeString(time, ":");
	}

	/**
	*
	* 두문자를 비교하여 셀렉트 문자를 리턴한다.
	* @see
	* @param	String value1
	* @param	String value2
	* @return	String
	* @throws
	*/
	public static String isSelected(String value1, String value2)
	{
	  if ( value1 != null && value1.equals(value2) )
	    return "selected";
	  else
	    return "";
	}

	/**
	*
	* 두문자를 비교하여 체크 문자를 리턴한다.
	* @see
	* @param	String value1
	* @param	String value2
	* @return	String
	* @throws
	*/
	public static String isChecked(String value1, String value2)
	{
	  if ( value1 != null && value1.equals(value2) )
	    return "checked";
	  else
	    return "";
	}

	/**
	*
	* 두문자를 비교하여 사용/미사용을 리턴한다.
	* @see
	* @param	String value1
	* @param	String value2
	* @return	String
	* @throws
	*/
	public static String isUse(String value1, String value2)
	{
	  if ( value1 != null && value1.equals(value2) )
	    return "사용";
	  else
		return "미사용";
	}

	/**
	*
	* 숫자 3자리마다 컴마를 찍는다.
	* @see
	* @param	int str
	* @return	String
	* @throws
	*/
  public static String money(int str){

    java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
    String r_str = nf.format(str);

    return r_str;

  }

	/**
	*
	* 숫자 3자리마다 컴마를 찍는다.
	* @see
	* @param	double str
	* @return	String
	* @throws
	*/
  public static String money(double str){

    java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
    String r_str = nf.format(str);

    return r_str;

  }

  /**
   *
   * 숫자 3자리마다 컴마를 찍는다.
   * @see
   * @param    Long str
   * @return   String
   * @throws
   */
 public static String money(Long str){

   java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
   String r_str = nf.format(str);

   return r_str;

 }

	/**
	*
	* 지정된 구분자로 구분된 문자열을 분리하여 문자열 배열로 변환한다.
	* @see
	* @param	String src
	* @param	char sep
	* @return	String[]
	* @throws
	*/
	public static String[] splitString(String src,char sep) {
		String[] dest=null;
		if (src!=null && src.length()>0) {
			int count=countChar(src,sep)+1;
			int startIndex=0;
			int endIndex=0;
			dest=new String[count];
			for (int i=0;i<count;i++) {
				if ((endIndex=src.indexOf(sep,startIndex))<0) {
					endIndex=src.length();
				}
				dest[i]=src.substring(startIndex,endIndex);
				startIndex=endIndex+1;
			}
		}
		return dest;
	}

	/**
	*
	* 문자열 내에 지정된 char의 수를 count한다.
	* @see
	* @param	String src
	* @param	char c
	* @return	int
	* @throws
	*/
	public static int countChar(String src,char c) {
		int count=0;
		if (src!=null) {
			for (int i=0;i<src.length();i++) {
				if (src.charAt(i)==c) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	*
  * 문자열이 지정된 길이 보다 긴 경우 지정된 길이에서 잘라내고 '...'을 붙인다.
	* @see
	* @param	String str
	* @param	int byteLength
	* @return	String
	* @throws
	*/
  public static String cutOff(String str, int byteLength)
  {
	  // String 을 byte 길이 만큼 자르기.

	    int retLength = 0;
	    int tempSize = 0;
	    int asc;
	    if(str == null || "".equals(str) || "null".equals(str)){
	        str = "";
	    }

	    int length = str.length();

	    for (int i = 1; i <= length; i++) {
	        asc = (int) str.charAt(i - 1);
	        if (asc > 127) {
	            if (byteLength >= tempSize + 2) {
	                tempSize += 2;
	                retLength++;
	            } else {
	                return str.substring(0, retLength) + "...";
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

	/**
	*
  * 지정한 소수점 자릿수만큼 반올림하여 리턴한다.
	* @see
	* @param	double value
	* @param	int scale
	* @return	double
	* @throws
	*/
  public static double round(double value, int scale)
  {
	BigDecimal temp1 = new BigDecimal(value);
	BigDecimal temp2 = new BigDecimal(Math.round(temp1.movePointRight(
		scale).doubleValue()));

	return temp2.movePointLeft(scale).doubleValue();
  }

	/**
	*
	* IP패턴 검사.
	* @see
	* @param	double str
	* @return	String
	* @throws
	*/
  public static boolean isValidIP(String strIP){

		if ( strIP == null || "".equals(strIP) )
			return false;
		else
		{
			StringTokenizer st = new StringTokenizer(strIP , ".");

			if ( st.countTokens() != 4 )
				return false;
			else
			{
				String strTmp = "";
				while ( st.hasMoreTokens() )
				{
					strTmp = st.nextToken();

						if ( Integer.parseInt(strTmp)>=1000 || Integer.parseInt(strTmp)<0 )
							return false;
				}
			}
		}

		return true;
  }




    public static String[] invalidSQLParam = {"<",">","{","}","#","&","SCRIPT",";","--"," OR "};
    public static String checkValidSQLParam(String param) throws java.sql.SQLException{
        if( param==null || param.equals("") )
                return param;
        else{
            String tmp = param.toUpperCase();
            tmp = tmp.replaceAll("&#\\d{5};","T"); // #,&인 경우, &#인 경우 체크 제외

            int size = invalidSQLParam.length;
                for(int i=0; i<size; i++){ //체크할 문자 목록만큼 LOOP
                    if( tmp.indexOf(invalidSQLParam[i])>-1 ){
                        throw new java.sql.SQLException("입력 내용 중 보안정책에 위배되어 저장할 수 없는 문자가 있습니다. ["+ invalidSQLParam[i] +"]", "", 9999 );
                    }
                }//for i

            return param;
            }//else
    }

    /**
     *
     * csv파일변환시 데이타 자체에 컴마나 줄바꿈 표시가 들어가 있는 경우 처리
     * @see
     * @param    String str
     * @return   String
     * @throws
     */
     public static String csvReplaceVal(String str)
     {
       if (str==null)
       {
         return null;
       }

       String returnVal = str;

       returnVal = returnVal.replaceAll(","," ");
       returnVal = returnVal.replaceAll("\n"," ");
       returnVal = returnVal.replaceAll("\r"," ");
       returnVal = returnVal.replaceAll("\r\n"," ");

       return returnVal;
     }


    public static String getString(String val, String def){
		if(val == null)
			return def;
		return val;
	}

	public static int getInt(String val, int def){
		if(val == null){
			return def;
		}else{
			return Integer.parseInt(val);
		}
	}

	public static float getFloat(String val, float def){
		if(val == null)
			return def;
		return Float.parseFloat(val);
	}

	public static String removeSpChar(String org){
		String result = "";
		result = org.replaceAll("[?][$]\\(\\)\\{\\}[*][+]\\^[|]\\[\\]", "");
		result = result.replaceAll("!", "");
		result = result.replaceAll("#", "");
		result = result.replaceAll("%", "");
		result = result.replaceAll("&", "");
		result = result.replaceAll("@", "");
		result = result.replaceAll("'", "");
		result = result.replaceAll(":", "");
		result = result.replaceAll(";", "");
		result = result.replaceAll("-", "");
		result = result.replaceAll("<", "");
		result = result.replaceAll(">", "");
		result = result.replaceAll("~", "");
		result = result.replaceAll(" ", "");
		return result;
	}

	/**
	 * Method a2k. 8859_1 에서 MS949 로 문자세트변환
	 * @param str 바꾸려는 문자열
	 * @return String
	 */
	public static String a2k(String str) {
		try {
			return new String(str.getBytes("8859_1"), "MS949");
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * @날짜형식변환 Method 예)2003-10-25 --> 20031025
	 * @param str 바꾸려는 날짜
	 * @return String
	 */
	public static String convertDate(String date) {
		System.out.println(date);
		try {
			if(date.trim().length() == 10) {
				return new String(date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10));
			} else {
				return date;
			}
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * @시형식변환 Method 예)12:31:00 --> 123100
	 * @param str 바꾸려는 날짜
	 * @return String
	 */
	public static String convertTime(String time) {
		try {
			return new String(time.substring(0, 2) + time.substring(3, 5) + time.substring(6, 8));
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * @시형식변환 Method 예)2008-12-31 12:31:00 --> 20081231123100
	 * @param str 바꾸려는 날짜
	 * @return String
	 */
	public static String convertDateTime(String datetime) {
		try {
			String date = datetime.substring(0, 10);
			String time = datetime.substring(11);

			return convertDate(date) + convertTime(time);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	/**
	 * @ 금액변환용 Method 예) 120,000 --> 120000
	 * @param str 바꾸려는 금액
	 * @return String
	 * @desc setter Method에 사용
	 */
	public static String reConvertCash(String string) {
		String result = "";

		if ((string != null) && (!string.equals(""))) {
			string = string.trim();

			int index_length = string.length();

			for (int i = 0; i < index_length; i++) {
				if (string.charAt(i) == ',') {
					continue;
				} else if (string.charAt(i) != ',') {
					result = result + string.charAt(i);
				}
			}
		}

		return result;
	}


	/**
	 * 입력 String을 고정된 length의 String으로 변환 한다.
	 * 고정 길이를 맞추기 위해 입력 String의 앞 또는 뒤에 "0" 또는 " "을 붙힌다.
	 * 문자열이 긴경우 substring한다.
	 *
	 * kind의 값은 0,1,2의 값을 가진다.
	 *
	 *ex1.
	 *int out_len=5;
	 *String str="1234567890";
	 *int kind=0;
	 *return "12345";  //kind값과 관계없이 앞에서부터 substring한다.
	 *
	 *
	 *ex2.
	 *int out_len=10;
	 *String str="12345";
	 *
	 *int kind=0;
	 *return "12345     ";  //공백문자를 사용하여 왼쪽정렬
	 *
	 *int kind=1;
	 *return "     12345";  //공백문자를 사용하여 오른쪽정렬
	 *
	 *int kind=2;
	 *return "0000012345";  //좌측에 "0"을 채움
	 *
	 *@return String
	 *@param int kind 정렬 방법
	 *@param int out_len 고정 길이
	 *@param String str
	 */
	public static String fixlength(int kind, int out_len, String str) {

		if ( str == null) str ="";
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


	/**
	 * @날짜형식변환 Method 예)20031025 --> 2003-10-25
	 * @param str 바꾸려는 날짜
	 * @return String
	 */
	public static String reConvertDate(String date) {
		date = removeZero(date);
		try {
			if (date.trim().length() != 8) {
				return "";
			}
			return new String(date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8));
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 숫자형 String 좌측의 '0'제거   예)0000700 ->700
	 * @param str
	 * @return '0'이 제거된 문자열
	 * 2008-04-22 -FREEB- 수정 -
	 *  ---- 0000700 -> -700이 되도록
	 */
	public static String removeZero(String str) {
		String ofset = "";

		if (str == null || str.length() == 0) {
			return "";
		}

		if (str.charAt(0) == '-') {
			str = str.substring(1);
			ofset = "-";
		}

		StringBuffer temp = new StringBuffer(str.trim());
		int len = 0;
		len = temp.length();

		for (int i = 0; i < len - 1; i++) {
			if (temp.charAt(i) == '0') {
				if (i == (len - 2)) {
					return ofset + temp.substring(i + 1);
				}
				continue;
			}
			temp = temp.delete(0, i);
			break;
		}
		return ofset + temp.toString();
	}


	/**
	 * @시간형식변환 Method 예)123012 --> 12:30:12
	 * @param str 바꾸려는 시간
	 * @return String
	 */
	public static String reConvertTime(String time) {
		try {
			if (time.trim().length() == 4) {
				return new String(time.substring(0, 2) + ":" + time.substring(2, 4));
			} else if (time.trim().length() != 6) {
				return "";
			}
			return new String(time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6));
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @시간형식변환 Method 예)20081231123100 --> 2008-12-31 12:31:00
	 * @param str 바꾸려는 시간
	 * @return String
	 */
	public static String reConvertDateTime(String datetime) {
		try {
			String date = datetime.substring(0, 8);
			String time = datetime.substring(8);
			return reConvertDate(date) + " " + reConvertTime(time);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


	/**
	 * @ 금액출력용 Method 예) 120000 --> 120,000
	 * @param str 바꾸려는 금액
	 * @return String
	 * @desc getter Method에 사용
	 */
	public static String convertCash(String strNumber) {
		try {
			String type = "plus";

			if (strNumber.charAt(0) == '-') {
				strNumber = strNumber.substring(1);
				type = "minus";
			}

			int intSize = strNumber.length(); // 전체길이
			int intCnt = (int) (intSize / 3);
			int intMod = intSize % 3;
			String result = "";

			if (intSize > 3) {
				for (int i = 0; i < intCnt; i++) {
					if (intCnt == (i + 1)) {
						if (intMod == 0) {
							result = strNumber.substring(0, 3) + result;
						} else if (intMod == 1) {
							result = strNumber.substring(0, 1) + "," + strNumber.substring(intSize - (3 * (i + 1)), intSize - (3 * i)) + result;
						} else if (intMod == 2) {
							result = strNumber.substring(0, 2) + "," + strNumber.substring(intSize - (3 * (i + 1)), intSize - (3 * i)) + result;
						}
					} else {
						result = "," + strNumber.substring(intSize - (3 * (i + 1)), intSize - (3 * i)) + result;
					}
				}
			} else {
				result = strNumber;
			}

			// 입력받은 값이 '-'일경우
			if (type.equals("minus")) {
				result = "-" + result;
			}

			return result;
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * 입력받은 문자열의 앞쪽에 0으로 채워서 사이즈를 맞춤
	 * @param inStr
	 * @param nFillSize
	 * @return
	 */
	public static String fillZero(String inStr, int nFillSize) {
		String tempStr = "";
		int tempLen;
		int addZeroCount = 0;
		tempStr = inStr.trim();

		tempLen = tempStr.length();
		addZeroCount = nFillSize - tempLen;

		for (int i = 0; i < addZeroCount; i++) {
			inStr = "0" + inStr;
		}

		return inStr;
	}

	/**
	 * 현재 시스템의 시간 또는 날자를 포멧에 맞춰 가져오기
	 * @param format
	 * @return
	 */
	public static String getCurrent(String format) {
		String currentDate = null;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat(format);

		currentDate = df.format(cal.getTime());

		return currentDate;
	}
	//Xss필터링
    @SuppressWarnings("unused")
	public static String cleanXSS(String value) {
        //You'll need to remove the spaces from the html entities below
		value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		value = value.replaceAll("'", "&#39;");
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		value = value.replaceAll("script", "");
		return value;
    }

    //Xss필터링
    @SuppressWarnings("unused")
    public static String deXSS(String value) {
    	//You'll need to remove the spaces from the html entities below
    	value = value.replaceAll( "&lt;","<").replaceAll( "&gt;",">");
    	value = value.replaceAll("&#40;","\\(").replaceAll( "&#41;","\\)");
    	value = value.replaceAll("&#39;", "'");
    	return value;
    }


    /**
     * 16진 문자열을 byte 배열로 변환한다.
     */
    public static byte[] hexToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            return new byte[]{};
        }

        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            byte value = (byte)Integer.parseInt(hex.substring(i, i + 2), 16);
            bytes[(int) Math.floor(i / 2)] = value;
        }
        return bytes;
    }

}