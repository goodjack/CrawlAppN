package com.ssfashion.bigdata.crawler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtil {
	
//	private static Logger logger = WSLogger.getLogger(WSTYPE..HTTP); 
    
	public static  String getCurrentTime(String pattern) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
		DateTimeZone seoul = DateTimeZone.forID("Asia/Seoul");
		DateTime current=new DateTime(seoul);
		
		return dtf.print(current);
	}
    
	public static DateTime getCurrentDateTime(String pattern) {
		DateTimeZone seoul = DateTimeZone.forID("Asia/Seoul");
		
		return new DateTime(seoul);
	}
	
	public static String getTodayStr(DateTime dateTime, String pattern) {
		DateTimeFormatter dtf = DateTimeFormat.forPattern(pattern);
		
		return dtf.print(dateTime);
	}
	
	public static String getCollectDayStr(String collectDay, String pattern) {
		DateFormat df = new SimpleDateFormat(pattern);
		
		return df.format(collectDay);
	}
	
	
    public static Date getDate(String pattern) {
    	Instant instant = Instant.parse(getCurrentTime(pattern));
    	Date date = Date.from(instant);   	
		return date;
	}
    
    public static String getTodayStr() {
    	return DateUtil.getCurrentTime("yyyyMMdd")+"000000";
    }
    
    public static Date getTodayDate() throws Exception{
    	SimpleDateFormat transFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    	return transFormat.parse(DateUtil.getTodayStr());
    }
    
}
