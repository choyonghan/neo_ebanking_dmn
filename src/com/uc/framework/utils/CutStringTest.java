package com.uc.framework.utils;

import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.uc.expenditure.daegu.daemon.NeoDMWS3020;
import com.uc.expenditure.daegu.daemon.NeoDMWS4040;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class CutStringTest {
    @SuppressWarnings("unchecked")
    static Logger   logger = Logger.getLogger(CutStringTest.class);

	/**
	 * 만든 이: 자바클루(javaclue) 만든 날: 2003/05/15
	 *
	 * 지정한 정수의 개수 만큼 빈칸(" ")을 스트링을 구한다.
	 *
	 * @param int 문자 개수
	 * @return String 지정된 개수 만큼의 빈칸들로 연결된 String
	 */
	public static String spaces(int count) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < count; i++) {
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * 만든 이: 자바클루(javaclue) 만든 날: 2003/06/26
	 *
	 * 지정한 정수의 개수 만큼 빈칸(" ")을 스트링을 구한다. 절단된 String의 바이트 수가 자를 바이트 개수보다 모자라지 않도록 한다.
	 *
	 * @param str 원본 String
	 * @param     int 자를 바이트 개수
	 * @return String 절단된 String
	 */
	public static String cutStringByBytes(String str, int length) {
		byte[] bytes = str.getBytes();
		int len = bytes.length;
		int counter = 0;
		if (length >= len) {
			return str + spaces(length - len);
		}
		for (int i = length - 1; i >= 0; i--) {
			if (((int) bytes[i] & 0x80) != 0)
				counter++;
		}
		return new String(bytes, 0, length + (counter % 2));
	}

	/**
	 * 만든 이: 자바클루(javaclue) 만든 날: 2003/06/26
	 *
	 * 지정한 정수의 개수 만큼 빈칸(" ")을 스트링을 구한다. 절단된 String의 바이트 수가 자를 바이트 개수를 넘지 않도록 한다.
	 *
	 * @param str 원본 String
	 * @param     int 자를 바이트 개수
	 * @return String 절단된 String
	 */
	public static String cutInStringByBytes(String str, int length) {
		byte[] bytes = str.getBytes();
		int len = bytes.length;
		int counter = 0;
		if (length >= len) {
			return str + spaces(length - len);
		}
		for (int i = length - 1; i >= 0; i--) {
			if (((int) bytes[i] & 0x80) != 0)
				counter++;
		}
		return new String(bytes, 0, length - (counter % 2));
	}

	public static String lengthLimit(String inputStr, int limit, String fixStr) {

		if (inputStr == null)
			return "";

		if (limit <= 0)
			return inputStr;

		byte[] strbyte = null;

		strbyte = inputStr.getBytes();

		if (strbyte.length <= limit) {
			return inputStr;
		}

		char[] charArray = inputStr.toCharArray();

		int checkLimit = limit;
		for (int i = 0; i < charArray.length; i++) {
			if (charArray[i] < 256) {
				checkLimit -= 1;
			} else {
				checkLimit -= 2;
			}

			if (checkLimit <= 0) {
				break;
			}
		}

		// 대상 문자열 마지막 자리가 2바이트의 중간일 경우 제거함
		byte[] newByte = new byte[limit + checkLimit];

		for (int i = 0; i < newByte.length; i++) {
			newByte[i] = strbyte[i];
		}

		if (fixStr == null) {
			return new String(newByte);
		} else {
			return new String(newByte) + fixStr;
		}
	}

	public String strCut(String szText, String szKey, int nLength, int nPrev, boolean isNotag, boolean isAdddot) { // 문자열
																													// 자르기

		String r_val = szText;
		int oF = 0, oL = 0, rF = 0, rL = 0;
		int nLengthPrev = 0;
		Pattern p = Pattern.compile("<(/?)([^<>]*)?>", Pattern.CASE_INSENSITIVE); // 태그제거 패턴

		if (isNotag) {
			r_val = p.matcher(r_val).replaceAll("");
		} // 태그 제거
		r_val = r_val.replaceAll("&amp;", "&");
		r_val = r_val.replaceAll("(!/|\r|\n|&nbsp;)", ""); // 공백제거

		try {
			byte[] bytes = r_val.getBytes("utf-8"); // 바이트로 보관

			if (szKey != null && !szKey.equals("")) {
				nLengthPrev = (r_val.indexOf(szKey) == -1) ? 0 : r_val.indexOf(szKey); // 일단 위치찾고
				nLengthPrev = r_val.substring(0, nLengthPrev).getBytes("MS949").length; // 위치까지길이를 byte로 다시 구한다
				nLengthPrev = (nLengthPrev - nPrev >= 0) ? nLengthPrev - nPrev : 0; // 좀 앞부분부터 가져오도록한다.
			}

			// x부터 y길이만큼 잘라낸다. 한글안깨지게.
			int j = 0;

			if (nLengthPrev > 0)
				while (j < bytes.length) {
					if ((bytes[j] & 0x80) != 0) {
						oF += 2;
						rF += 3;
						if (oF + 2 > nLengthPrev) {
							break;
						}
						j += 3;
					} else {
						if (oF + 1 > nLengthPrev) {
							break;
						}
						++oF;
						++rF;
						++j;
					}
				}

			j = rF;

			while (j < bytes.length) {
				if ((bytes[j] & 0x80) != 0) {
					if (oL + 2 > nLength) {
						break;
					}
					oL += 2;
					rL += 3;
					j += 3;
				} else {
					if (oL + 1 > nLength) {
						break;
					}
					++oL;
					++rL;
					++j;
				}
			}

			r_val = new String(bytes, rF, rL, "utf-8"); // charset 옵션

			if (isAdddot && rF + rL + 3 <= bytes.length) {
				r_val += "...";
			} // ...을 붙일지말지 옵션
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return r_val;

	}

	public static String[] parseStringByBytes(String raw, int len, String encoding) {
		if (raw == null)
			return null;

		String[] ary = null;

		try {
			byte[] rawBytes = raw.getBytes(encoding);
			int rawLength = rawBytes.length;

			System.out.println("[" + raw + "]의 길이는 [" + rawLength + "]입니다");

			int index = 0;
			int minus_byte_num = 0;
			int offset = 0;

			int hangul_byte_num = encoding.equals("utf-8") ? 3 : 2;

			if (rawLength > len) {
				int aryLength = (rawLength / len) + (rawLength % len != 0 ? 1 : 0);
				ary = new String[aryLength];
				for (int i = 0; i < aryLength; i++) {
					minus_byte_num = 0;
					offset = len;
					if (index + offset > rawBytes.length) {
						offset = rawBytes.length - index;
					}
					for (int j = 0; j < offset; j++) {
						if (((int) rawBytes[index + j] & 0x80) != 0) {
							minus_byte_num++;
						}
					}

					if (minus_byte_num % hangul_byte_num != 0) {
						offset -= minus_byte_num % hangul_byte_num;
					}
					ary[i] = new String(rawBytes, index, offset, encoding);
					index += offset;
				}
			} else {
				ary = new String[] { raw };
			}
		} catch (Exception e) {
		}
		return ary;
	}

	public static String LocalString(String val) {
		if (val == null)
			return null;
		else {
			byte[] b;

			try {
				b = val.getBytes("8859_1");
				CharsetDecoder decoder = Charset.forName("utf-8").newDecoder();
				try {
					CharBuffer r = decoder.decode(ByteBuffer.wrap(b));
					return r.toString();
				} catch (CharacterCodingException e) {
					return new String(b, "EUC-KR");
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	/**
	 *
	 * @author Georgios Migdos <cyberpython@gmail.com>
	 */

	public static Charset detectCharset(File f, String[] charsets) {

		Charset charset = null;

		for (String charsetName : charsets) {
			charset = detectCharset(f, Charset.forName(charsetName));
			if (charset != null) {
				break;
			}
		}

		return charset;
	}

	private static Charset detectCharset(File f, Charset charset) {
		try {
//              BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));

			CharsetDecoder decoder = charset.newDecoder();
			decoder.reset();

			String utf8str = new String("뷁우리는 대한민국인이다");
			String euckrstr = new String("뷁우리는 대한민국인이다".getBytes("euckr"), "euckr");

			byte[] buffer = new byte[512];
			buffer = utf8str.getBytes("euckr");
			System.out.println("euckr[" + new String(buffer, "euckr"));
			buffer = utf8str.getBytes("utf8");
			System.out.println("utf8[" + new String(buffer, "utf8"));
			buffer = utf8str.getBytes("iso2022kr");
			System.out.println("ISO2022KR[" + new String(buffer, "iso2022kr"));

			buffer = euckrstr.getBytes("euckr");
			System.out.println("euckr[" + new String(buffer, "euckr"));
			buffer = euckrstr.getBytes("utf8");
			System.out.println("utf8[" + new String(buffer, "utf8"));
			buffer = euckrstr.getBytes("iso2022kr");
			System.out.println("ISO2022KR" + new String(buffer, "iso2022kr"));

			byte[] buffer2 = new byte[512];
			buffer2 = euckrstr.getBytes("utf8");

			boolean identified = false;
			while (!identified) {
				identified = identify(buffer, decoder);
			}

//             input.close();

			if (identified) {
				return charset;
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

	private static boolean identify(byte[] bytes, CharsetDecoder decoder) {
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
//		String str = "자바클루(javaclue)가 만든 글자 자르기";
//		File f = new File("/example.txt");
//
//		String[] charsetsToBeTested = { "EUC-KR", "utf-8" };
//
//		Charset charset = detectCharset(f, charsetsToBeTested);
//
//		if (charset != null) {
//			try {
//				InputStreamReader reader = new InputStreamReader(new FileInputStream(f), charset);
//				int c = 0;
//				while ((c = reader.read()) != -1) {
//					System.out.print((char) c);
//				}
//				reader.close();
//			} catch (FileNotFoundException fnfe) {
//				fnfe.printStackTrace();
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			}
//
//		} else {
//			System.out.println("Unrecognized charset.");
//		}
//
//		try {
//			byte[] euchan = str.getBytes("EUC-KR");
//			String euckrstr = new String(euchan, "EUC-KR");
//			System.out.println("[" + euchan.length + "], euckr[" + euckrstr + "]");
//
//			byte[] utf8han = str.getBytes("utf-8");
//			String utf8str = new String(utf8han, "utf-8");
//			System.out.println("[" + utf8han.length + "], euckr[" + utf8str + "]");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(lengthLimit(str, 9, ""));
//		for (int i = 4; i < 40; i++)
//			System.out.println(i + ": [" + cutInStringByBytes(str, i) + "]");
//		String aa = LocalString(str);
//		;
//		System.out.println("[" + aa + "]");

		DOMConfigurator.configure(CutStringTest.class.getResource("/conf/log4j.xml"));

		String strHeadTmp = "DGCITY01HB200008420N20220926EC22092600000110111223334447                        018대명동지점                              0000                                                  LF0134100004610461013220221001000008420000001                                                                                                                                                          011122233334444                                                           ";
		String strBodyTmp = "DGCITY01D1      03110234789123456    백승일                                  급여                0000399691010                    00YBN10501정상처리 되었습니다.                                                                                                                                                                                                                                                                                                  ";
		String strTailTmp = "DGCITY01E000001200000100000033327970000001000000333279700000000000000000000000000100000033327970                                                                                                                                                                                                                                                                                                                                                                  ";
		NeoUtils obj = new NeoUtils();
		MyMap tmpMap     = new MyMap();
		try {
//			obj.fileToPrint("msgformat1", strHeadTmp.getBytes("euckr"), strHeadTmp.getBytes("euckr").length, tmpMap, "100", null, logger, "H");
			obj.fileToPrint("msgformat1", strBodyTmp.getBytes("euckr"), strBodyTmp.getBytes("euckr").length, tmpMap, "100", "DGCITY01", logger, "B");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}