package com.eopcon.crawler.samsungcnt.model;



public class Collectlog {
	/** 로그 수집 **/
	private String collectDate;		// 수집일
	private String collectTime;		// 수집시간

	private String message;			// log 정보
	private String target;			// 대상
	private String className;		// 클래스명
	private String logType;			// 로그 타입
	
	public String getCollectDate() {
		return collectDate;
	}
	public void setCollectDate(String collectDate) {
		this.collectDate = collectDate;
	}
	public String getCollectTime() {
		return collectTime;
	}
	public void setCollectTime(String collectTime) {
		this.collectTime = collectTime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
}
