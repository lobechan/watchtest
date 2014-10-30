package com.bluefay.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BLDate {

	public static final String MAIL_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
	public static final String MAIL_DATE_FORMAT2 = "d MMM yyyy HH:mm:ss Z";

	public static String encodeMailDate(long seconds) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(seconds * 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat(MAIL_DATE_FORMAT,
				new Locale("en"));
		return dateFormat.format(date.getTime());
	}

	public static long decodeMailDate(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		String format = MAIL_DATE_FORMAT;
		if (str.contains(",") == false) {
			format = MAIL_DATE_FORMAT2;
		}
		SimpleDateFormat simple = new SimpleDateFormat(format, new Locale("en"));
		try {
			Date date = simple.parse(str);
			return date.getTime() / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static long decodeDate(String str, String format) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		if (format == null) {
			format = "yyyy-M-dd HH:mm:ss";
		}

		SimpleDateFormat simple = new SimpleDateFormat(format, new Locale("en"));
		try {
			Date date = simple.parse(str);
			return date.getTime() / 1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String currentTimeString(String format) {
		long seconds = currentTime();
		return makeTimeString(seconds, format);
	}

	public static String makeTimeString(long seconds, String format) {
		if (format == null) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(seconds * 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat(format, new Locale(
				"en"));
		return dateFormat.format(date.getTime());
	}

	/**
	 * Return seconds from 1970
	 * 
	 * @return
	 */
	public static long currentTime() {
		return System.currentTimeMillis() / 1000;
	}
}
