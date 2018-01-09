package com.eopcon.crawler.samsungcnt.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	/**
	 * String객체를 TimeInMillisecond 형식으로 반환한다.
	 * 
	 * @param arg
	 *            : 날짜(문자열중 숫자만 8,12,14자리가 되어야함) 8자리 : 20110901 (yyyyMMdd) 12자리
	 *            : 201109011300 (yyyyMMddhhmi) 14자리 : 20110901130030
	 *            (yyyyMMddhhmiss)
	 * @return
	 * @throws Exception
	 */
	public static Date parseDate(String arg) throws Exception {
		Calendar cal = Calendar.getInstance();
		int year, month, day;
		int hour = 0;
		int minute = 0;
		int second = 0;

		String date = arg.replaceAll("\\D", "");

		if (date.length() == 8) {
			year = Integer.parseInt(date.substring(0, 4));
			month = Integer.parseInt(date.substring(4, 6));
			day = Integer.parseInt(date.substring(6, 8));
		} else if (date.length() == 12) {
			year = Integer.parseInt(date.substring(0, 4));
			month = Integer.parseInt(date.substring(4, 6));
			day = Integer.parseInt(date.substring(6, 8));
			hour = Integer.parseInt(date.substring(8, 10));
			minute = Integer.parseInt(date.substring(10, 12));
		} else if (date.length() == 14) {
			year = Integer.parseInt(date.substring(0, 4));
			month = Integer.parseInt(date.substring(4, 6));
			day = Integer.parseInt(date.substring(6, 8));
			hour = Integer.parseInt(date.substring(8, 10));
			minute = Integer.parseInt(date.substring(10, 12));
			second = Integer.parseInt(date.substring(12, 14));
		} else {
			throw new IllegalArgumentException("Unable to parse the date: " + arg);
		}
		cal.set(year, month - 1, day, hour, minute, second);
		return cal.getTime();
	}
}
