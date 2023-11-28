package com.uc.framework.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.ibm.icu.util.ChineseCalendar;


/**
 * @author puresuit
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StringProcess {



    public static final String parseString(Object value, String defaultValue) {

        if ( value==null || value.toString().trim().equals(""))
            return defaultValue;


        return value.toString();

    }

    public static final String parseString(Object value) {

        return parseString(value, "");
    }

    public static final String parseString(String value, String defaultValue){

    	if ( value==null || value.toString().trim().equals(""))
            return defaultValue;

        return value;
    }

    public static final long parseLong(Object value) {

        long result = 0;

        if ( value==null || value.equals(""))
            return result;

        try {
            result = Long.parseLong( value.toString() );
        }
        catch ( Exception e ) {

            return result;
        }


        return result;
    }

    public static final long parseLong(Object value, long defaultNumber) {

        long result = defaultNumber;

        if ( value==null || value.equals(""))
            return defaultNumber;

        try {
            result = Long.parseLong( value.toString() );
        }
        catch ( Exception e ) {

            return defaultNumber;
        }


        return result;
    }


    /*
     * Object를 int로 변환한다. null이거나 parsing에러시 파라미터로 넘겨진 디폴트 값을 리턴한다.
     */
    public static final int parseInt(Object value, int defaultNumber) {

        int result = defaultNumber;

        if ( value==null || value.equals(""))
            return defaultNumber;

        try {
            result = Integer.parseInt( value.toString() );
        }
        catch ( Exception e ) {

            return defaultNumber;
        }


        return result;
    }


    public static final int parseInt(String value, int defaultNumber) {

        int result = defaultNumber;

        if ( value==null || value.equals(""))
            return defaultNumber;

        try {
            result = Integer.parseInt( value.toString() );
        }
        catch ( Exception e ) {

            return defaultNumber;
        }


        return result;
    }

    public static final String parseKrString(Object value, String defaultValue)
    {
        String str = null;

        try
        {
            str = new String(parseString(value, defaultValue).getBytes("ISO-8859-1"),"EUC-KR");
        }
        catch ( Exception e)
        {
            str = defaultValue;
        }

        return str ;
    }


    public static final String parseKrString(Object value)
    {
        return parseKrString(value, "");
    }



    public static final int parseInt(Object value) {

        return parseInt(value, 0);
    }




    /*
     * Object를 int로 변환한다. null이거나 parsing에러시 파라미터로 넘겨진 디폴트 값을 리턴한다.
     */
    public static final float parseFloat(Object value, float defaultNumber) {

        float result = defaultNumber;

        if ( value==null || value.equals(""))
            return defaultNumber;

        try {
            result = Float.parseFloat( value.toString() );
        }
        catch ( Exception e ) {

            return defaultNumber;
        }


        return result;
    }


    public static final float parseFloat(Object value) {

        return parseFloat(value, 0);
    }

    public static final String convertStrFloat(String value1, String value2) {

    	if(value1 == null || value1.equals("")) value1 = "0";
    	if(value2 == null || value2.equals("")) value2 = "0";

    	float var1 = Float.parseFloat(value1);
    	float var2 = Float.parseFloat(value2);
    	float ret = var1 + var2;
    	return String.valueOf(ret);
    }

    /*
     * Object를 int로 변환한다. null이거나 parsing에러시 파라미터로 넘겨진 디폴트 값을 리턴한다.
     */
    public static final boolean parseBoolean(Object value, boolean defaultBool) {

        boolean result = defaultBool;

        if ( value==null || value.equals(""))
            return defaultBool;

        try {
            result = Boolean.getBoolean( value.toString() );
        }
        catch ( Exception e ) {

            return defaultBool;
        }


        return result;
    }





    public static final boolean parseBoolean(Object value) {

        return parseBoolean(value, false);
    }


    /**
     * 20051 -> 2005.1
     * @param src
     * @return
     */
    public static final String termStringFormat(Object src) {

        if ( src == null )
            return "";

        String result = src.toString().trim();

        if( result.length() != 5 ) { // null이거나 5글자가 아니면 아무것도 하지 않는다.

            return result;
        }

        return result.substring(0,4) + "." + result.substring(4,5);
    }



    /**
     * OC4J에서 빈 문자열을 출력할시 테이블이 깨지는 문제를 해결하기 위해서..
     * @param str
     * @return
     */
    public static String getFixedString(Object str) {

        if( str==null || str.equals("") ) {

            return "&nbsp;";
        }
        else
            return str.toString();
    }


    /**
     * 인증요건의 글자를 이미지 경로를 포함한 path로 바꾸어서 리턴
     * @param table
     * @param approval
     */
    public static String gradeIcon( Object approval ) {


        if ( approval==null || approval.equals("") )
            return "";


        String imgName = "";

        String appPrefix  = approval.toString().substring(0,1).toLowerCase();


        if ( approval.toString().length() == 2 ) {  // A+, A-, A0, ... D+, D0, D-


            String appPostfix = approval.toString().substring(1,2);


            if ( appPostfix.equals("+") )
                imgName = appPrefix + "01";

            else if ( appPostfix.equals("0") )
                imgName = appPrefix + "02";

            else if ( appPostfix.equals("-") )

                imgName = appPrefix + "03";

            imgName = "<img src='../image/student/" + imgName + ".gif'>";

        } else if ( appPrefix.equals("f") || appPrefix.equals("s") || appPrefix.equals("u") )  {

            imgName = appPrefix;
            imgName = "<img src='../image/student/" + imgName + ".gif'>";
        } else {

            imgName = approval.toString();
        }




        return imgName;

    }




    /**
     * 문자를 숫자로 변환
     * 설문조사 숫자포맷에만 한정적으로 이용되므로 20까지만 정의하였다.
     * @param str
     * @return
     */
    public static int parseCharInt(String ch) {

        if ( ch.length() != 1)
            return 0;

        final String[] charArr    = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i","j","k","l","m","n" };

        for( int i=0; i<=20; i++) {

            if ( ch.equalsIgnoreCase( charArr[i]) )
                return i;
        }

        return 0;
    }


    /**
     * 날짜와 날짜 사이에 있는지?
     * @param day
     * @param from
     * @param to
     * @return
     */
    public static boolean betweenDays(String from , String to) {

        java.text.DateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        Calendar cal_today  = Calendar.getInstance();
        Calendar cal_from   = Calendar.getInstance();
        Calendar cal_to     = Calendar.getInstance();

        try {

            cal_from.setTime( formatter.parse(from) );
            cal_to.setTime( formatter.parse(to) );
        }
        catch (Exception e) { // 스트링 파싱에 실패할시 사이에 없는걸로 간주

            return false;
        }

        boolean isAfter  = cal_today.equals(cal_from) || cal_today.after(cal_from); // from보다 같거나 이후인지?
        boolean isBefore = cal_today.equals(cal_to)   || cal_today.before(cal_to);  // to보다 같거나 이전인지?

        return (isAfter && isBefore);
    }

    public static String parseStr(int val){
       	return String.valueOf(val);
    }
	public static String convertDatetime(String date_format)
	{
		if(date_format.length() > 9)
		{
			String d1 = date_format.substring(0, 4);
			String d2 = date_format.substring(5, 7);
			String d3 = date_format.substring(8, 10);

			return d1 + "." + d2 +"." + d3;
		}
		else
		{
			return date_format;
		}
	}


	// Date형식을 YYYY.MM.DD형식으로 변환
	public static String convertDate(String date_format)
	{
		if(date_format.length() > 7)
		{
			String d1 = date_format.substring(0, 4);
			String d2 = date_format.substring(4, 6);
			String d3 = date_format.substring(6, 8);

			return d1 + "." + d2 + "." + d3;
		}
		else
		{
			return date_format;
		}
	}

	// Date형식을 YYYY.MM.DD형식으로 변환
	public static String convertDateHan(String date_format)
	{
		if(date_format.length() > 7)
		{
			String d1 = date_format.substring(0, 4);
			String d2 = date_format.substring(4, 6);
			String d3 = date_format.substring(6, 8);

			return d1 + "년 " + d2 + "월 " + d3 + "일";
		}
		else
		{
			return date_format;
		}
	}

	// 입력시 문자처리
	public static String setString(String str, String default_str)
	{
		try {
			if(str != null) {
				String value = new String(str.getBytes("8859_1"), "KSC5601");
				str = value.trim(); //.(addslashes($str));
			}
			else {
				str = default_str;
			}
			return str;
		} catch (Exception e) {
			return default_str;
		}
	}


	// 출력시 문자처리
	public static String getString(String str)
	{
	   return str.trim(); // stripslashes(str)
	}

	public static String getString(Object str_before)
	{
		String str = (String)str_before;
	   return str.trim(); // stripslashes(str)
	}

	public static String getString(String str,String default_str)
	{
		try {
			if(str != null) {

				str = str.trim(); //.(addslashes($str));
			}
			else {
				str = default_str;
			}
			return str;
		} catch (Exception e) {
			return default_str;
		}
	}

	public static String getString(Object str_before,String default_str)
	{
		String str = (String) str_before;
		try {
			if(str != null) {

				str = str.trim(); //.(addslashes($str));
			}
			else {
				str = default_str;
			}
			return str;
		} catch (Exception e) {
			return default_str;
		}
	}

	public static String getString2(String str,String default_str)
	{
		try {
			if(!str.equals("")) {

				str = str.trim(); //.(addslashes($str));
			}
			else {
				str = default_str;
			}
			return str;
		} catch (Exception e) {
			return default_str;
		}
	}

	public static String getString(String[] str,String default_str)
	{
		String ret = "";
		try {
			if(str != null) {

				ret = String.valueOf(str).trim(); //new String(str).trim();
			}
			else {
				ret = default_str;
			}
			return ret;
		} catch (Exception e) {
			return default_str;
		}
	}

	public static String getInt(String temp)
	{
		NumberFormat nf = NumberFormat.getInstance();

		if(temp == null || temp.equals("")) {
			return temp="0";
		} else {
			return nf.format(Double.parseDouble(temp));
		}
	}

	public static String getInt(int temp)
	{
		NumberFormat nf = NumberFormat.getInstance();

		return nf.format((double)temp);
	}

	public static int getInt(int temp,int defaultInt)
	{
		if(temp < 1)
		{
			return defaultInt;
		} else {
			return temp;
		}
	}

	// 출력시 수정폼에서문자처리
	// TODO : replaceAll replace 함수 비교
	//삭제요망
	public static String getStringAscii(String str)
	{
	   str = str.trim(); //stripslashes($str));
	   str = str.replaceAll("\"","&#34;");
	   return str;
	}
	public static String getStringreplaceAll(String Expression, String Pattern, String Rep)
	{
	    if (Expression==null || Expression.equals("")) return "";
	    int s = 0;
	    int e = 0;
	    StringBuffer result = new StringBuffer();

	    while ((e = Expression.indexOf(Pattern, s)) >= 0) {
	        result.append(Expression.substring(s, e));
	        result.append(Rep);
	        s = e + Pattern.length();
	    }
	    result.append(Expression.substring(s));
	    return result.toString();
	}

	// 출력시 수정폼에서문자처리
	// TODO : replaceAll replace 함수 비교
	public static String getStringBlank1(String str)
	{
	   str = str.trim(); //stripslashes($str));
	   str = str.replaceAll("&nbsp;","");
	   return str;
	}

	@SuppressWarnings("unused")
	public static String cutString(String str, int byteLength)
	{
		int length = str.length();
		int retLength = 0;
        int tempSize = 0;
        int asc;

        for ( int i = 1; i<=length; i++)
        {
            asc = (int)str.charAt(i-1);

            if ( asc > 127)
            {
                if ( byteLength > tempSize )
                {
                    tempSize += 2;
                    retLength++;
                }
            }
            else
            {
                if ( byteLength > tempSize )
                {
                    tempSize++;
                    retLength++;
                }
            }
        }

        if (str==null) {
			return "";
		}

        String tmp = str;

		if(tmp.length() > byteLength) {
			tmp=tmp.substring(0, tempSize)+ "..";
		}

		return tmp;

	}

	// 문자열 자르기(변수,길이)
	public static String cutStringEx(String str, int end)
	{
		if (str==null) {
			return "";
		}

		String tmp = str;

		if(tmp.length() > end) {
			tmp=tmp.substring(0, end)+"..";
		}
		return tmp;
	}

	public static String cutStringNo(String str, int end)
	{
		if (str==null) {
			return "";
		}

		String tmp = str;

		if(tmp.length() > end) {
			tmp=tmp.substring(0, end);
		}
		return tmp;
	}
	// HTML 태그 제거 함수
	// TODO : replaceAll replace 함수 비교
	public static String RemoveHTML(String str) {
		str = str.replaceAll("/</", "&lt;");
		str = str.replaceAll("/>/", "&gt;");
		return str;
	}


	// 파일의 확장자 가져오는 함수
	public static String getExt(String file){
		String needle[] = file.split( ".");
		return needle[needle.length-1];
	}

	public static String soMun(String str){

		String str2="";
		char[] imsi = str.toCharArray();
		for(int i=0;i<=str.length()-1;i++){


		 if(Character.isUpperCase(imsi[i]))
		 {
			 imsi[i]=Character.toLowerCase(imsi[i]);
		 }
		/* else
		if(Character.isLowerCase(imsi[i]))
		{
		    imsi[i]=Character.toUpperCase(imsi[i]);
		}*/
		str2 += imsi[i]+"";
		}
		 return str2.trim();
}

	// 파일 존재 유무 체크 함수
	public static String FileCheck(String path, String savefile, String no, String lastname) {

		File file = new File(path);

		String [] str =	file.list();

		savefile = no + "." + lastname;

		for(int i = 0; i < str.length ; ++i )	{
			StringTokenizer st = new StringTokenizer(str[i], ".");
			if(st.nextToken().equals(savefile))
			{
				savefile = no + "_" + i + "." + lastname;
			}
		}
		return savefile;
	}


	// 에러 메시지 함수

	public static String error_msg( String msg )
	{
		return "<script language='javascript'>alert('"+ msg +"');history.go(-1);</script>";
	}


	// 페이지 이동 함수
	public static String PageMove( String url) {
		return "<meta http-equiv='refresh' content='0; url="+ url + "'>";
	}


	/**
	 * 날짜가 YYYYMMDD 형식으로 넘어 올때.
	 */
	public static boolean check_newIcon_str(String c_date)
	{
		return check_newIcon(StringProcess.convertDate(c_date));
	}

	//////////////////////////////////////////////////////////////////////////
	////오늘로 부터 5일 이내일 경우 true 리턴함
	////ex) check_newIcon("2005-02-22 12:03:31.0") 오늘이 20050223일경우 호출시 true리턴
	//////////////////////////////////////////////////////////////////////////
	public static boolean check_newIcon(String compare_date){

		if (compare_date.length() < 10){
			return false;
		}
		String d1 = compare_date.substring(0, 4);
		String d2 = compare_date.substring(5, 7);
		String d3 = compare_date.substring(8, 10);

		String compare_date1 = d1 + d2 + d3;

		Calendar cal= Calendar.getInstance();
		String yyyy=Integer.toString(cal.get(Calendar.YEAR));
		String mm=Integer.toString((cal.get(Calendar.MONTH) - Calendar.JANUARY+1));
		String dd=Integer.toString(cal.get(Calendar.DATE));
		if ( mm.length()==1){
			mm="0"+mm;
		}
		if ( dd.length()==1){
			dd="0"+dd;
		}
		String today_date = yyyy+mm+dd;

		//날짜 차이를 구함
		long tmp;
		long gap = 0;

		try{
			SimpleDateFormat sf_date = new SimpleDateFormat("yyyyMMdd");
			Date s_wdate, n_wdate;
			s_wdate = sf_date.parse(compare_date1);				//실제 날짜형 타입으로 변환
			n_wdate = sf_date.parse(today_date);				//실제 날짜형 타입으로 변환
			tmp = n_wdate.getTime() - s_wdate.getTime();		//오늘 날짜를 long타입으로 가지고 옴
			gap = tmp / (1000*60*60*24);
		}catch(Exception e) {}

		//5일 이내 이면 true
		if (gap >= 0 && gap <= 5){
			return true;
		}
		else {
			return false;
		}
	}

	///////////////////////////////////////////////////////////////////
	//// 날짜 빼는 모듈
	//// ex) dayminus("20020402" , 1) 호출 시 "20020401" 이 리턴 됨
	///////////////////////////////////////////////////////////////////

	public static String dayMinus( String start_date, long num ) {

		try {
			SimpleDateFormat sf_date = new SimpleDateFormat("yyyyMMdd");
			Date wdate;
			long a, b, c;

			wdate = sf_date.parse(start_date);		//실제 날짜형 타입으로 변환
			a = wdate.getTime();					// 오늘 날짜를 long타입으로 가지고 옴
			b = num * 24 * 60 * 60 * 1000;			// 몇일이 지난 타임
			c = a - b;
			wdate.setTime(c);
			start_date = sf_date.format(wdate);
		}
		catch(Exception e) {}

		return start_date;
	}

	/**
	* String이 null 일때 공백을 반환한다.    <BR>
	* int cnt = StrUtil.nullToStr("10","");	<BR>
	* @param    str     검사할 String문자열.
	* @return   변환된 str 값.
	*/
	public static String nullToStr(String str, String defaultStr) {

		return (str==null || str.equals(""))?defaultStr:str.trim();

	}

	public static String LPAD(String str, int size)
	{
		StringBuffer buf = new StringBuffer();

		for(int i=0; i < size - str.length(); i++)
		{
			buf.append("0");
		}

		buf.append(str);

		return buf.toString();
	}

	public static String LPAD(String str, int size, String pi)
	{
		StringBuffer buf = new StringBuffer();

		for(int i=0; i < size - str.length(); i++)
		{
			buf.append(pi);
		}

		buf.append(str);

		return buf.toString();
	}
	public static String RPAD(String str, int size)
	{
		StringBuffer buf = new StringBuffer();

		buf.append(str);

		for(int i=0; i < size - str.length(); i++)
		{
			buf.append("0");
		}

		return buf.toString();
	}

	public static String RPAD(String str, int size, String pi)
	{
		StringBuffer buf = new StringBuffer();

		buf.append(str);

		for(int i=0; i < size - str.length(); i++)
		{
			buf.append(pi);
		}

		return buf.toString();
	}
	/////////////////////////////////////////////////////////////////////
	//	 입력받은 숫자를 #0.00 (Dot) 형태로 리턴
	/////////////////////////////////////////////////////////////////////
	public static String number_format(double in) {

		StringBuffer out = new StringBuffer(Double.toString(in));
		int dotpos = out.reverse().toString().indexOf(".");

		while (dotpos > 2){
			out.reverse().deleteCharAt(out.length()-1);
			dotpos = out.reverse().toString().indexOf(".");
		}


		if (dotpos == 1) out.reverse().append("0");
		else if (dotpos == 2 ) out.reverse();



		return out.toString() ;
	}

	/* -------------------------------------------------------------------------------------------*/
	//	 int, long, String 을 [#,##0] 포멧으로 나타낸다.
	//	 boolean 이 false 이면 공백을 리턴한다.
	/* -------------------------------------------------------------------------------------------*/
	//	 int
		public static String toComma(int in, boolean isValid)
		{
			if ( isValid == false ) return "&nbsp;" ;

			NumberFormat nf = NumberFormat.getInstance() ;
			String out = null ;
			try	 { out = nf.format(in); }
			catch (NumberFormatException e) { out = "&nbsp;" ; }

			return out;
		}
		// long
		public static String toComma(long in, boolean isValid)
		{
			if ( isValid == false ) return "&nbsp;" ;

			NumberFormat nf = NumberFormat.getInstance() ;
			String out = null ;
			try	 { out = nf.format(in); }
			catch (NumberFormatException e) { out = "&nbsp;" ; }

			return out;
		}
		// String
		public static String toComma(String in, boolean isValid)
		{
			if ( isValid == false ) return "&nbsp;" ;

			NumberFormat nf = NumberFormat.getInstance() ;
			String out = null ;
			try	 { out = nf.format(Long.parseLong(in)); }
			catch (NumberFormatException e) { out = "&nbsp;" ; }

			return out;
		}


	/** 현재시간을 포맷의 형식으로 변환후 리턴
     * getDate("YYYY/MM/DD HH:mm:SS")
     * @param format
     * @return
     * 예: getDate("yyyyMMddHHmmss")
     */
    public static String getDate(String format)
    {
        return getDate( Calendar.getInstance(), format);
    }


    /** 시간 포맷
     * getDate("YYYY/MM/DD HH:mm:SS")
     * @param format
     * @return
     * 예: getDate("yyyyMMddHHmmss")
     */
    public static String getDate(Calendar cal, String format)
    {
        SimpleDateFormat dateformat = new SimpleDateFormat(format);
        return dateformat.format( cal.getTime() );
    }

    public static Calendar getCalendar(String src, String format) throws Exception
    {
        SimpleDateFormat dateformat = new SimpleDateFormat(format);
        Date date = dateformat.parse(src);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static String changeFormat(String date, String srcFormat, String dstFormat, String defaultDate )
    {
        try
        {
          Calendar cal = StringProcess.getCalendar(date, srcFormat);
          return StringProcess.getDate(cal, dstFormat);
        }
        catch ( Exception e)
        {
            return defaultDate;
        }
    }

    public static String changeFormat(String date, String srcFormat, String dstFormat)
    {
        return changeFormat(date, srcFormat, dstFormat, date);
    }

    public static String[] splitString(String pStr, String pDelim)
    {

    		if (pStr == null || pStr.length() == 0 || pDelim == null ||
                       pDelim.length() == 0) {

                         return null;
              }


              int cnt = 0;
              String temp = pStr;
              String[] rtn = null;

              while ( true ) {

                       int sepIdx = temp.indexOf(pDelim);

                       if( sepIdx == -1 ){
                                cnt++;
                                break;
                       }

                       temp = temp.substring(sepIdx+pDelim.length(), temp.length());

                       cnt++;
              }

              rtn = new String[cnt];
              temp = pStr;

              for( int i = cnt; i > 0; i-- ) {
                       int sepIdx = temp.indexOf(pDelim);
                       if( sepIdx == -1 ){
                                rtn[cnt-1] = temp;
                                break;
                       }


                       rtn[cnt-i] = temp.substring(0,sepIdx);

                       temp = temp.substring(sepIdx+pDelim.length(), temp.length());
              }
              return rtn;
     }

	public static String getFormat(double f)
	{
		return new DecimalFormat("#.#").format(f);
	}

	public static double getsTod(String str)
	{
		try
		{
			if (str != null)
				return Double.parseDouble(str);
			else
				return 0;
		}catch(Exception e)
		{
       			e.printStackTrace();
       			return 0;
    	}
	}//end of getsTod


	public static String dfNumberFormat(String str , int size){
		String fmt = "";
		for(int i =0; i < size; i++){
			fmt = fmt +"0";
		}
		DecimalFormat df  = new DecimalFormat(fmt);
		return df.format(Long.parseLong(str));
	}

	public static String dfNumberFormat(double dou){
		DecimalFormat df  = new DecimalFormat("000000000000");
		return df.format(dou);
	}

	public static String dcNumberFormat(double dou){
		DecimalFormat df  = new DecimalFormat("0000000000");
		return df.format(dou);
	}

	public static String dfNumberFormat(int dou)
	{
		DecimalFormat df  = new DecimalFormat("00");
		return df.format(dou);
	}

	public static String dfNumberFormat(String dou)
	{
		DecimalFormat df  = new DecimalFormat("00");
		return df.format(Integer.parseInt(dou));
	}

	public static String addNumber(String Str, String addStr)
	{
		int num1 = StringProcess.parseInt(Str);
		int num2 = StringProcess.parseInt(addStr);
		int sum = num1 + num2;
		return StringProcess.parseStr(sum);
	}


	 public static String fileReName(String fname, String alias)
	 {
		 String body = null;
	     String ext = null;
		 int dot = fname.lastIndexOf(".");

		 if(dot != -1)
	     {
	            body = fname.substring(0, dot);
	            ext = fname.substring(dot);
	     } else {
	    	 body = fname;
	         ext = "";
	     }

		 String newName;
	     newName = body + alias + ext;

	     return newName;
	 }

	 /**
	  * String을 BASE64로 인코딩한다
	  *
	  * @param str
	  * @return BASE64로 인코딩된값
	  */
	 public static String base64Encode(String str){

		 String result = "";
		 sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
		 byte[] b1 = str.getBytes();
		 result = encoder.encode(b1);
		 return result;

	 }

	 /**
	  * BASE64방식으로 디코딩을 한다
	  *
	  * @param str
	  * @return  BASE64 디코딩된 String
	  */
	 public static String base64Decode(String str){
		 String result = "";
		 try{

			 sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
			 byte[] b1 = decoder.decodeBuffer(str);
			 result = new String(b1);
		 }catch(Exception ex){

		 }
		 return result;
	 }

	 /**
		 * 한글과 영문이 포함된 문자를 바이트 길이 단위로  손상없이 잘라 반환한다.
		 * 유의사항: 한글문자가 바이트 경계에 걸릴 경우 한바이트 덜 잘라서 반환한다.
		 * ex) truncateNicely("대한민국", 5, "") => 대한까지만 반환
		 * @param s 반환하려는 전체 문자
		 * @param len 잘라낼 반환 길이
		 * @param tail 잘라지고 남은 길이가 있으면 뒤에 붙여줄 테일문자. null일 경우 아무것도 안붙임 = 빈공란 문자와 동일
		 * @return 잘라낸 문자.
		 */
		public static String truncateNicely(String s, int len, String tail)
		{
			if (s == null)
				return null;

			int srcLen = s.getBytes().length;
			if (srcLen < len)
				return s;

			String tmpTail = tail;
			if (tail == null)
				tmpTail = "";

			int tailLen = tmpTail.getBytes().length;
			if (tailLen > len)
				return "";

			char a;
			int i = 0;
			int realLen = 0;
			for (i = 0; i < len - tailLen && realLen < len - tailLen; i++)
			{
				a = s.charAt(i);
				if ((a & 0xFF00) == 0)
					realLen += 1;
				else
					realLen += 2;
			}

			while (s.substring(0, i).getBytes().length > len - tailLen)
			{
				i--;
			}
			return s.substring(0, i) + tmpTail;
		}

		/**
		 * 입력받은 문자열을 자르거나 공백을 채워서 입력받은 길이만큼 만든다
		 * @param 문자열, 길이
		 * @return 처리된 문자열
		 */
		public static String makeFormatStr(String str, int len)
		{
			str = str.trim();

			int strlen = str.getBytes().length;

			if (strlen > len)	//문자열이 입력받은 길이보다 길다면
			{
				str = truncateNicely(str,len," ");
			}
			else if(strlen < len)
			{
				for(int i = 0; i < len-strlen ; i++)
				{

					str = str+" ";

				}
			}
			return str;
		}

		/**
		 * 개인실명인증 전문문자열을 생성한다
		 * @param 주민등록번호, 성명
		 * @return 실명인증 전문문자열
		 */
		public static String makeTelegramTo(String reg_no, String name, String gubun)
		{
			/*modify ksh.*/
			String fClass = "";				//자료종류
			String fGCode = "";				//구분코드
			String check="";

			if(gubun.equals("1")){	//개인일때
				fClass = "1";
				fGCode = "1";
			}else if(gubun.equals("4")){//외국인일때
				fClass = "7";
				fGCode = "4";
				check = reg_no.substring(11,12);
				if("7".equals(check)||"8".equals(check)||"9".equals(check)){
			}
				else{
				}
			}

			String telegram = "";

			String fGubun = "I";			//처리구분
			//String fClass = "1";			//자료종류
			String fCode = "B249";		//사업자식별코드
			String fRegno = makeFormatStr(reg_no,16);				//주민/외국인등록번호
			//String fGCode = "1";			//구분코드
			String fName = makeFormatStr(name,40);				//성명
			String fRCode = "1";			//요청구분
			String fCid = "B249SYSTEM";					//접속자 ID
			String fFILLER = " ";				//FILLER
			String fEndstr = "\n";				//EndStr


			//전문 생성
			telegram = fGubun+fClass+fCode+fRegno+fGCode+fName+fRCode+fCid+fFILLER+fEndstr;
			return telegram;

		}

		/**
		 * 법인실명인증 전문문자열을 생성한다
		 * @param 주민등록번호, 성명
		 * @return 실명인증 전문문자열
		 */
		public static String makeTelegramTo2(String reg_no, String name)
		{
			String telegram = "";

			String fGubun = "I";			//처리구분
			String fClass = "D";			//자료종류
			String fCode = "B249";		//사업자식별코드
			String fRegno = makeFormatStr(reg_no,10);				//주민/외국인등록번호
			String fName = makeFormatStr(name,100);				//성명
			String fCid = "B249SYSTEM";					//접속자 ID
			String fEndstr = "\n";				//EndStr

			//전문 생성
			telegram = fGubun+fClass+fCode+fRegno+fName+fCid+fEndstr;
			return telegram;

		}

		/**
		 * 일반 텍스트 내용을 Html형식으로 변환한다
		 * @param 변환할 문자열
		 * @return 변환된 문자열
		 */
		public static String textToHtml(String strTmp)
		{
			strTmp = strTmp.replaceAll("&", "&amp;");
	    	strTmp = strTmp.replaceAll( "<", "&lt");
	    	strTmp = strTmp.replaceAll( ">", "&gt");
	    	strTmp = strTmp.replaceAll( " ", "&nbsp");
	    	strTmp = strTmp.replaceAll( "\"", "&quot");
	    	strTmp = strTmp.replaceAll( "\n", "<br>");

			return strTmp;
		}

		/**
		 * 현재날짜시간을 리턴한다 (형식 : yyyyMMddHHmmss)
		 * @return String
		 */
		public static String getCurrTime(){
			SimpleDateFormat formatter = new SimpleDateFormat( "yyyyMMddHHmmss");
			Date currentTime = new Date();
			String dateString = formatter.format(currentTime);
			return dateString;
		}

		/**
		 * String 배열값을 String 변환한다.. (출력용)
		 * @return String
		 */
		public static String stringArrToString(String[] arr){
			StringBuffer result = new StringBuffer();
			result.append("\n▒▒▒▒▒ 바인딩된변수 ▒▒▒▒▒\n");
			for(int i = 0; i < arr.length; i++){
				result.append("StrArr["+i+"] "+arr[i]+"\n");
			}
			return result.toString();
		}

		/**
		 * String 배열값을 String 변환한다.. (출력용)
		 * @return String
		 */
		public static String envTax_yygi(String str){
			String val = "";
			if(str.equals("01")){
				val = "00";
			}else if(str.equals("01")){
				val = "00";
			}else if(str.equals("02")){
				val = "00";
			}else if(str.equals("03")){
				val = "00";
			}else if(str.equals("04")){
				val = "00";
			}else if(str.equals("05")){
				val = "00";
			}else if(str.equals("06")){
				val = "00";
			}else if(str.equals("07")){
				val = "00";
			}else if(str.equals("08")){
				val = "00";
			}else if(str.equals("09")){
				val = "02";
			}else if(str.equals("10")){
				val = "02";
			}else if(str.equals("11")){
				val = "02";
			}else if(str.equals("12")){
				val = "02";
			}
			return val;
		}

		/**
		 * String문자열에서 숫자값만 추출
		 * @return String
		 */
		public static String extractMum(String str){

			String numeral = "";
			if( str == null ){
				numeral = null;
			} else {
				String patternStr = "\\D"; //숫자를 패턴으로 지정
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(str);

				while(matcher.find()) {
					numeral += matcher.group(0); //지정된 패턴과 매칭되면 numeral 변수에 넣는다. 여기서는 숫자!!
				}
			}
			return numeral;
		}

		/**
		 * String문자열에서 문자값만 추출
		 * @return String
		 */
		public static String extractStr(String str){

			String strValue = "";
			if( str == null ){
				strValue = null;
			} else {
				String patternStr = "\\d"; //문자를 패턴으로 지정
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(str);

				while(matcher.find()) {
					strValue += matcher.group(0); //지정된 패턴과 매칭되면 numeral 변수에 넣는다. 여기서는 숫자!!
				}
			}
			return strValue;
		}

		public static String etaxLogMsg(Throwable e){
			StringBuffer msg = new StringBuffer();
			try{

				if(e.toString().length() > 2000){
					StackTraceElement se[] = e.getStackTrace();

		            for(int i = 0; i < se.length; i++){
		            	if(se.length < 4000){
		            		msg.append("\tat ");
			            	msg.append(se[i].getMethodName()+"("+se[i].getFileName()+":"+se[i].getLineNumber()+")");
			            	msg.append("\n");
		            	}else{
		            		break;
		            	}
		            }
				}else{
					msg.append("error : "+e.toString()+"\n");
					StackTraceElement se[] = e.getStackTrace();

		            for(int i = 0; i < se.length; i++){
		            	if(se.length < 4000){
		            		msg.append("\tat ");
			            	msg.append(se[i].getMethodName()+"("+se[i].getFileName()+":"+se[i].getLineNumber()+")");
			            	msg.append("\n");
		            	}else{
		            		break;
		            	}
		            }
				}
	        }catch(Exception ex){
	        	return e.toString();
	        }
	        return msg.toString();
	    }

		/**
		 * 입력값중에 특수문자를 제외한다.
		 * @return String
		 * */
		public static String keyStr(String keyWord){

			keyWord = keyWord.replaceAll("-", "");
			keyWord = keyWord.replaceAll("%", "");
			keyWord = keyWord.replaceAll(";", "");
			keyWord = keyWord.replaceAll("\'", "");
			keyWord = keyWord.replaceAll("\"", "");
			keyWord = keyWord.replaceAll("--", "");
			keyWord = keyWord.replaceAll("#", "");
			keyWord = keyWord.replaceAll("\\(", "");
			keyWord = keyWord.replaceAll("\\)", "");
			keyWord = keyWord.replaceAll(">", "");
			keyWord = keyWord.replaceAll("<", "");
			keyWord = keyWord.replaceAll("=", "");
			keyWord = keyWord.replaceAll("/", "");
			keyWord = keyWord.replaceAll("\\+", "");
			return keyWord;
		}


	    /** 원하는길이에 필요한 문자를 추가하여 맞춰서 만들어줌
	     * str : 문자
	     * set : 붙일 문자
	     * number : 필요한 길이
	     * */
	    public static String StrFormat(String str, String set, int number) {

	    	int alen = number-str.length();

	    	for(int i=0; i<alen;i++) {
	    		str = set + str;
	    	}

			return str;
	    }


		@SuppressWarnings("unused")
		public static String SendTelegram(String Ip, String sendStr)	{

	    	Socket clientSocket = null;
	        OutputStream outputStream = null;
	        InputStream inputStream = null;

	    	String byteStr = sendStr;
	    	// 전문길이구하기
	        String len = dfNumberFormat(byteStr.length());

	        //byteStr = len+byteStr;

	        byte[] byteData = byteStr.getBytes();

	        //응답결과
	        String str2 = null;
	        try
	        {
	        	clientSocket = new Socket(Ip, 63003);
	            outputStream = clientSocket.getOutputStream();
	            inputStream = clientSocket.getInputStream();
	        	outputStream.write(byteData, 0, byteData.length);
	        	outputStream.flush();

	            try{
	            	Thread.sleep(5 * 1000);
	            }catch(Exception ex){
	            	ex.printStackTrace();
	            }

	            byte[] rcvByteData = new byte[404];
	        	int availableVal = 0;

	          	while((availableVal = inputStream.available()) != 0)
	          	{
		          	int readCnt = inputStream.read(rcvByteData, 0, availableVal);
		          	str2 = new String(rcvByteData);
	          	}

	            inputStream.close();
	            clientSocket.close();
	        }catch(SocketException ex)
	        {
	        	ex.printStackTrace();
	        }
	        catch(IOException ex)
	        {
	        	ex.printStackTrace();
	        }
	        return str2;
		}

		// 양력날짜를 음력으로 변환
		/*
		public static String convertLunar(String date) {
			ChineseCalendar cc = new ChineseCalendar();
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
			cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(6)));

			cc.setTimeInMillis(cal.getTimeInMillis());

			int y = cc.get(ChineseCalendar.EXTENDED_YEAR) - 2637;
			int m = cc.get(ChineseCalendar.MONTH) + 1;
			int d = cc.get(ChineseCalendar.DAY_OF_MONTH);

			StringBuffer ret = new StringBuffer();
			ret.append(String.format("%04d", y));
			ret.append(String.format("%02d", m));
			ret.append(String.format("%02d", d));

			return ret.toString();
		}*/

}