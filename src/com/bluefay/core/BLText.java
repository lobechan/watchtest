package com.bluefay.core;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BLText {

	/** carriage return - line feed sequence */
	public static final String CRLF = "\r\n";

	/** US-ASCII CR, carriage return (13) */
	public static final int CR = '\r';

	/** US-ASCII LF, line feed (10) */
	public static final int LF = '\n';

	/** US-ASCII SP, space (32) */
	public static final int SP = ' ';

	/** US-ASCII HT, horizontal-tab (9) */
	public static final int HT = '\t';

	public static boolean isWhitespace(char ch) {
		return ch == SP || ch == HT || ch == CR || ch == LF;
	}

	public static boolean isWhitespace(final String s) {
		if (s == null) {
			throw new IllegalArgumentException("String may not be null");
		}
		final int len = s.length();
		for (int i = 0; i < len; i++) {
			if (!isWhitespace(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isStrEqual(String str1, String str2) {

		if (str1 == null || str2 == null) {
			return false;
		}

		if (str1.equals("") || str2.equals("")) {
			return false;
		}
		return str1.equals(str2);
	}

	public static boolean isStrEqualIgnoreCase(String str1, String str2) {

		if (str1 == null || str2 == null) {
			return false;
		}

		if (str1.equals("") || str2.equals("")) {
			return false;
		}
		return str1.equalsIgnoreCase(str2);
	}

	public static String convertHumanReadableSize(long bytes) {

		if (bytes < 1024) {
			return String.format("%dB", bytes);
		} else {
			bytes >>= 10;
			if (bytes > 1024) {
				bytes >>= 10;
				return String.format("%dMB", bytes);
			} else {
				return String.format("%dKB", bytes);
			}
		}
	}

	/**
	 * 
	 * @param time
	 *            Seconds from 1970 UTC
	 * @return
	 */
	public static String makeTimeText(long seconds) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(seconds * 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date.getTime());
	}

	public static boolean isContainsNonAscii(String text) {
		try {
			return text.getBytes("utf-8").length != text.length();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static String getHost(String url) {
		if (url.startsWith("http://")) {
			int index = url.indexOf("/", 7);
			return url.substring(7, index);
		} else {
			int index = url.indexOf("/", 0);
			return url.substring(7, index);
		}
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static String skipHtmlTag(String htmlStr) {
		String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>";
		String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>";
		String regEx_html = "<[^>]+>";
		htmlStr = htmlStr.replaceAll(regEx_script, "");
		htmlStr = htmlStr.replaceAll(regEx_style, "");
		htmlStr = htmlStr.replaceAll(regEx_html, "");
		htmlStr = htmlStr.trim();
		return htmlStr;
	}

	public static boolean isNotEmpty(String str) {
		return str != null && str.length() > 0;
	}
}
