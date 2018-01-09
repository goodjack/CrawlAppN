package com.eopcon.crawler.samsungcnt.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CollectDateFormat {
	private final Date startDate = new Date();
	private String collectDate;
	private String collectTime;
	private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private DateFormat dailyFormat = new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	/**
	 * 오늘 날짜 출력 
	 * @return "yyyyMMddHHmmSS";
	 */
	public String getTodayCollectDate (){
		collectDate = dateFormat.format(startDate);
		return collectDate;
	}
	
	/**
	 * 오늘 날짜 출력 
	 * @return "yyyy-MM-dd";
	 */
	public String getDailyCollectDate (){
		collectDate = dailyFormat.format(startDate);
		return collectDate;
	}
	
	/**
	 * 오늘 시간 출력 
	 * @return "HH:mm:ss";
	 */
	public String getTodayTimeCollectDate(){
		collectDate = timeFormat.format(startDate);
		return collectDate;
	}
	/**
	 * date 전 날짜 출력
	 * ex) date = -1 : 하루전 
	 * ex) date = -7 : 1주일전
	 * @param date 
	 * @return "yyyy-MM-dd";
	 */
	public String getCollectDate(int date){
		collectDate = dateFormat.format(startDate.getTime()-1000*60*60*24*(date));
		return collectDate;
	}
	public String formatTime(long lTime) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(lTime);
		return (c.get(Calendar.HOUR_OF_DAY) + "시 " + c.get(Calendar.MINUTE) + "분 " + c.get(Calendar.SECOND) + "."
				+ c.get(Calendar.MILLISECOND) + "초");
	}
	
	
	
	
	
	
	/*

	public static void setStringToDate(String collectDate) {
		try {
			toDate = dateFormat.parse(collectDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	*//**
	 * 오 날짜 출력
	 * 
	 * @param collectDate
	 * @return "yyyy-MM-dd";
	 *//*
	public static String getTodayCollectDate() {
		collectDate = dateFormat.format(toDate);
		return collectDate;
	}

	*//**
	 * 어제 날짜 출력
	 * 
	 * @param collectDate
	 * @return "yyyy-MM-dd";
	 *//*
	public static String getYesterdayCollectDate() {
		collectDate = dateFormat.format(toDate.getTime() - 1000 * 60 * 60 * 24);
		return collectDate;
	}

	*//**
	 * date 전 날짜 출력 ex) date = -1 : 하루전 ex) date = -7 : 1주일전
	 * 
	 * @param collectDate
	 * @param date
	 * @return "yyyy-MM-dd";
	 *//*
	public static String getCollectDate(int date) {
		collectDate = dateFormat.format(toDate.getTime() - 1000 * 60 * 60 * 24 * (-date));
		return collectDate;
	}

	*/
}

