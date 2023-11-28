/**
 *
 */
package com.uc.framework.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.uc.framework.parsing.FormatParserAsMap;

/**
 * @author hotaep
 *
 */
public class Utils
{
	public static boolean isNumber(String str)
	{
	    char c;

	    if (str.equals(""))
	    	return false;

	    for (int i = 0 ; i < str.length() ; i++) {
	        c = str.charAt(i);
	        if (c < 48 || c > 59) {
	            return false;
	        }
	    }
	    return true;
	}

	public static String subStringBytes(String str, int byteLength)
	{
		int	length = str.length();
		int	retLength = 0;
		int	tempSize = 0;
		int	asc;

		for (int i = 1; i <= length; i++) {
			asc = str.charAt(i-1);
			if (asc > 127) {
				if (byteLength >= tempSize + 2) {
					tempSize += 2;
					retLength++;
				} else {
					return str.substring(0, retLength);
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
	 * 현재 시스템의 시간 또는 날자를 포멧에 맞춰 가져오기
	 * @param format
	 * @return
	 */
	public static String getCurrent(String format)
	{
		String currentDate = null;

		Calendar cal = Calendar.getInstance( );
		SimpleDateFormat df = new SimpleDateFormat(format);

		currentDate = df.format( cal.getTime( ) );

		return currentDate;

	}


	/**
	 *  현재 시스템 날자 가져오기 YYYYMMDD
	 */
	public static String getCurrentDate( )
	{

		String currentDate = null;

		Calendar cal = Calendar.getInstance( );
		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd" );

		currentDate = df.format( cal.getTime( ) );

		return currentDate;
	}


	/**
	 *  날짜 증감 가져오기  YYYYMMDD
	 *  예) getCalDate(-1)  -> 어제날짜
	 */
	public static String getCalDate(int amount)
	{

		String currentDate = null;

		Calendar cal = Calendar.getInstance( );
		SimpleDateFormat df = new SimpleDateFormat( "yyyyMMdd" );
		cal.add(Calendar.DATE, amount);
		currentDate = df.format( cal.getTime( ) );

		return currentDate;
	}



	/**
	 *  현재 시스템 시간 가져오기 HHMMSSsss
	 */
	public static String getCurrentTime( )
	{

		String currentTime = null;

		Date date = new Date( );
		SimpleDateFormat df = new SimpleDateFormat( "HHmmss" );

		currentTime = df.format( date );

		return currentTime;
	}

	/**
	  *
	 */
	public static SQLException findSQLExceptionWithVendorErrorCode (Throwable t)
	{
		  Throwable rootCause = t;
		  // this can become an endless loop if someone circularly nests an exception, which would be dumb, but throw a control in there anyway
		  while (rootCause != null) {
		    if (rootCause instanceof SQLException) {
		        SQLException sqlException = (SQLException) rootCause;
           return sqlException;
		        }
		  	}
		  rootCause = t.getCause();
		  return null;  // is there a better way to indicate that no SQLException was found?
		}
    /**
     * Method a2k. 8859_1 에서 MS949 로 문자세트변환
     * @param str 바꾸려는 문자열
     * @return String
     */
    public static String a2k(String str)
    {
        try
        {
            return new String(str.getBytes("8859_1"), "MS949");
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * Method k2a. MS949 에서 8859_1 로 문자세트변환
     * @param str 바꾸려는 문자열
     * @return String
     */
    public static String k2a(String str)
    {
        try
        {
            return new String(str.getBytes("MS949"), "8859_1");
        }
        catch (Exception e)
        {
            return "";
        }
    }
    /**
     * Method u2k. EUC-KR 에서 8859_1 로 문자세트변환
     * @param str 바꾸려는 문자열
     * @return String
     */
    public static String u2k(String str)
    {
        try
        {
            return new String(str.getBytes("EUC-KR"), "8859_1");
        }
        catch (Exception e)
        {
            return "";
        }
    }

    /**
     * Method k2u. 8859_1 에서 EUC-KR 로 문자세트변환
     * @param str 바꾸려는 문자열
     * @return String
     */
    public static String k2u(String str)
    {
        try
        {
            return new String(str.getBytes("8859_1"), "EUC-KR");
        }
        catch (Exception e)
        {
            return "";
        }
    }

    public static ByteBuffer assembleMessage(FormatParserAsMap fp, MyMap body, String msggrp, byte[] msg)
    {
        body.setLong("MsgLen", 103);
        body.setString("TrDTM", Utils.getCurrent("yyyyMMddhhmmss"));
        body.setString("FILL", "0000");
//        body.put("MsgType", msgtype);
        body.setString("MsgDsc", "09");
        body.setString("MsgReqCd", "0200");
//        body.put("TxSeq", "");
//        body.setString("ErrCd", "0000");

        byte[]    bMsg = new byte[1024];
        int length = fp.assembleMessageByteAsMap(bMsg, 1024, body, msggrp);
        if (length == -1) {
//            logger.error("error while assembling message");
            return null;
        }

        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.put(bMsg, 0, length);
        buffer.limit(buffer.position());
        buffer.flip();

        return buffer;
    }

    public static int disassebleMessage(FormatParserAsMap fp, byte[] bMsg, long len, String msggrp, MyMap body)
    {
        int length = fp.disassembleMessageByteAsMap(bMsg, (int)len, body, msggrp);
        if (length == -1) {
//            logger.error("error while assembling message");
            return -1;
        }

        return 0;
    }

    public static void dumpAsHex(Logger log, byte[] byteBuffer, String title, int length)
    {
        int p = 0;
        int rows = length / 16;

        StringBuffer wBuf = new StringBuffer("\n   ");

        String t = "====== " +title + "(" + length+ ") =";

        for(int i = t.getBytes().length; i < 64; i++){
            t += "=";
        }
        wBuf.append(t + "\n   ");

        try {
            for (int i = 0; i < rows; i++) {
                int ptemp = p;

                for (int j = 0; j < 16; j++) {
                    String  hexVal = Integer.toHexString(byteBuffer[ptemp] & 0xff);

                    if (hexVal.length() == 1) {
                        hexVal = "0" + hexVal;
                    }

                    wBuf.append(hexVal + " ");

                    // System.out.print(hexVal + " ");
                    ptemp++;
                }

                System.out.print(" ");

                for (int j = 0; j < 16; j++) {
                    if ((byteBuffer[p] > 32) && (byteBuffer[p] < 127)) {
                        wBuf.append((char)byteBuffer[p]);
                        // System.out.print((char) byteBuffer[p]);
                    } else {
                        // System.out.print(".");
                        wBuf.append(".");
                    }

                    p++;
                }

                // System.out.println();
                wBuf.append("\n   ");
            }

            int n = 0;

            if(p < length) {
                for (int i = p; i < length; i++) {
                    String  hexVal = Integer.toHexString(byteBuffer[i] & 0xff);

                    if (hexVal.length() == 1) {
                        hexVal = "0" + hexVal;
                    }
                    wBuf.append(hexVal + " ");
                    // System.out.print(hexVal + " ");
                    n++;
                }

                for (int i = n; i < 16; i++) {
                    // System.out.print("   ");
                    wBuf.append("   ");
                }

                // System.out.print(" ");
                // wBuf.append(" ");

                for (int i = p; i < length; i++) {
                    if ((byteBuffer[i] > 32) && (byteBuffer[i] < 127)) {
                        // System.out.print((char) byteBuffer[i]);
                        wBuf.append((char)byteBuffer[i]);
                    } else {
                        // System.out.print(".");
                        wBuf.append(".");
                    }
                }
                wBuf.append("\n   ");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            wBuf.append(e.toString());
        }

        wBuf.append("================================================================");
        if(log == null) System.out.println(wBuf.toString());
        else {
                log.info(wBuf.toString());
        }
    }

	/**
	 * @전각을 반각으로 Method 예) "）" => ")"
	 * @param sFull    => 전각
	 * @return String  => 반각
	 */
	public static String convertFullHalf(String sFull)
	{

		 String UNICODE_HALF_FULL_ASCII[][] ={
				 {" ","　"},{"!","！"},{"#","＃"},{"$","＄"},{"%","％"},{"&","＆"},{"'","’"},{"(","（"},{")","）"},{"*","＊"},{"+","＋"},{",","，"},{"-","－"},{".","．"},{"/","／"},
				 {"0","０"},{"1","１"}, {"2","２"},{"3","３"},{"4","４"},{"5","５"},{"6","６"},{"7","７"},{"8","８"},{"9","９"},{":","："},{";","；"},{"<","＜"},{"=","＝"},{">","＞"},{"?","？"},
				 {"@","＠"},{"A","Ａ"}, {"B","Ｂ"},{"C","Ｃ"},{"D","Ｄ"},{"E","Ｅ"},{"F","Ｆ"},{"G","Ｇ"},{"H","Ｈ"},{"I","Ｉ"},{"J","Ｊ"},{"K","Ｋ"},{"L","Ｌ"},{"M","Ｍ"},{"N","Ｎ"},{"O","Ｏ"},
				 {"P","Ｐ"},{"Q","Ｑ"}, {"R","Ｒ"},{"S","Ｓ"},{"T","Ｔ"},{"U","Ｕ"},{"V","Ｖ"},{"W","Ｗ"},{"X","Ｘ"},{"Y","Ｙ"},{"Z","Ｚ"},{"[","［"},{"]","］"},{"^","＾"},{"_","＿"},
				 {"`","‘"},{"a","ａ"}, {"b","ｂ"},{"c","ｃ"},{"d","ｄ"},{"e","ｅ"},{"f","ｆ"},{"g","ｇ"},{"h","ｈ"},{"i","ｉ"},{"j","ｊ"},{"k","ｋ"},{"l","ｌ"},{"m","ｍ"},{"n","ｎ"},{"o","ｏ"},
				 {"p","ｐ"},{"q","ｑ"}, {"r","ｒ"},{"s","ｓ"},{"t","ｔ"},{"u","ｕ"},{"v","ｖ"},{"w","ｗ"},{"x","ｘ"},{"y","ｙ"},{"z","ｚ"},{"{","｛"},{"|","｜"},{"}","｝"},{"~","～"}
//				 ,{"\","￥"},{""","”"}
			 };

		 int len = sFull.length();
		 int index=0;
		 int cnt=1;
		 String returnValue =  "";
		 int jungakyn = 0;

		 while (index < len) {
			 jungakyn = 0;  // 전각 여부
		     if (sFull.charAt(index) >= 256) {
		         for(int i=0;i<UNICODE_HALF_FULL_ASCII.length;i++){
		             if( sFull.substring(cnt-1,cnt).equals(UNICODE_HALF_FULL_ASCII[i][1]) ) {
		                 returnValue += UNICODE_HALF_FULL_ASCII[i][0];
		                 jungakyn = 1;
		                 break;
		             }
		         }
		         if(jungakyn == 0) returnValue += sFull.substring(cnt-1,cnt); // 한글일 경우 그냥 복사
		     }else { // 1바이트 문자라면...
		         returnValue += sFull.substring(cnt-1,cnt);
		     }
		     index++;
		     cnt++;
		 }

         return returnValue;
	}

	/**
	 * @예금주 체크
	 * @param org_name : 비교이름, real_name : 예금주명
	 * @return int 1:같음, 0:다름
	 */
	public static int isSameYegeumju(String org_name, String real_name)
	{

		// 계정계의 예금주 명이 최대 9자리(대구은행 10자리, 타행 9자리)이므로 trim()으로 공백 제거 후 9자 까지만 비교
		// 단, 전각 글자는 반각 글자로 변경 후 비교

		try {
			String real_tran_name = Utils.convertFullHalf(real_name).toString().replaceAll("\\p{Space}", "");
			String org_tran_name = Utils.convertFullHalf(org_name).toString().replaceAll("\\p{Space}", "");

			String Real = real_tran_name;
			if( real_tran_name.length() > org_tran_name.length())
				Real = real_tran_name.substring(0, org_tran_name.length());

			String Org = org_tran_name;
			if(org_tran_name.length() > real_tran_name.length())
				Org = org_tran_name.substring(0, real_tran_name.length());

			// 실예금주와 DB의 예금주가 맞는지 검사
			if(Org.equals(Real)){
				return 1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

	}

    public static void getResources(String resFile, MyMap myRes)
    {
            ResourceBundle  bundle = null;
            try {
                    bundle = ResourceBundle.getBundle(resFile);
                    Enumeration myenum = bundle.getKeys();
                    for (; myenum.hasMoreElements(); ) {
                            String  name = (String)myenum.nextElement();
                            String  value = bundle.getString(name);

                            myRes.setString(name, value);
                    }
            } catch (Exception ex) {
			    ex.printStackTrace();
            }
    }

    public static void getProperties(String resFile, MyMap myRes)
    {
        File file = new File(resFile);

        try {
            BufferedInputStream bis =  new BufferedInputStream(new FileInputStream(file));
            Properties prop = new Properties();

            prop.load(bis);

            for(Entry entry : prop.entrySet()){
                String  name = entry.getKey().toString();
                String  value = entry.getValue().toString();
                myRes.setString(name, value);
            }

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

   /**
    * Exception PrintStackTrace를 String으로 변환
    * ex) makeStackTrace(ex); (Exception ex)
    * @param t
    * @return
    */
    public static String makeStackTrace(Throwable t) {

    if (t == null)
    return "";

    try {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        t.printStackTrace();
        bout.flush();
        String error = new String(bout.toByteArray());

        return error;
    } catch (Exception ex) {
        return "";
    }

    }


	/**
	 * 원본파일을 대상파일로 이동한다.
	 * 원본파일은 삭제한다.
	 *
	 * @param sourceFile - 원본파일
	 * @param trgetFile - 대상파일
	 * @param trgetPath - 대상파일경로
	 */
    public static void fileMove( File sourceFile, File trgetFile, String trgetPath ) {
    	File trgetPathDir = new File( trgetPath );
    	if( !trgetPathDir.isDirectory()) {
    		trgetPathDir.mkdirs();
    	}
    	sourceFile.renameTo( trgetFile );
    	sourceFile.delete();
    }

    /**
     * 입력된 Source 데이터를 intLength 만큼 strMode 형태로 확장한 String 을 return
     * ASIS : dmws6050
     *
     * @param intLength 최대크기 int
     * @param charMode 확장모드 char
     * @param Source 원본데이터 String
     * @return String 변경된데이터
     */
    public static String extendData(int intLength, char charMode, String Source) throws ArrayIndexOutOfBoundsException {

        if ( Source.length() > intLength ) {
        	throw new ArrayIndexOutOfBoundsException("입력데이터 [" + Source + "]가 길이[" + intLength + "]를 초과하였습니다.");
        }

     	String byteTarget = Source;

     	switch ( charMode ) {
     	case '9':
     	    for (int i=0; i<intLength-Source.length(); i++){
     	    	byteTarget = "0"+byteTarget;
     	    }
     	    break;
     	default :
     		for (int i=0; i<intLength-Source.length(); i++){
     			byteTarget = " "+byteTarget;
     		}
     		break;
     	}

     	return byteTarget;
    }

    /**
     * 암호화
     * @param str - 암호화할 문자열
     * @return
     */
	public static String getEncrypt( String str ) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
		encryptor.setPassword("BRACE_PASS");
		//encryptor.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));
		return encryptor.encrypt(str);
	}

	/**
	 * 복호화
	 * @param encStr - 복화화할 문자열
	 * @return
	 */
	public static String getDecrypt( String encStr ) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
		encryptor.setPassword("BRACE_PASS");
		//encryptor.setSaltGenerator(new StringFixedSaltGenerator("someFixedSalt"));
		return encryptor.decrypt(encStr);
	}
}
