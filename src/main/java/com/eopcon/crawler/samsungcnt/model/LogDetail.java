package com.eopcon.crawler.samsungcnt.model;

import org.apache.ibatis.type.Alias;

@Alias("logs")
public class LogDetail {

	private Long id = null;
	private String site = null; // 사이트
	private String onlineGoodsNum = null; // 온라인 상품번호
	private String goodsNum; // 상품번호
	private String collectURL; // 수집URL
	private String val1; // 최초출시일 혹은 등록일(yyyyMMdd) -> ex) 20160131
	private String val2; // 카테고리(원본값) -> ex) 카테고리1; 카테고리2; 카테고리3;
	private String val3; // 소재(원본값) -> ex) [몸판] 레이온100%, [리브 부분] 폴리에스터100%
	private String val4; // 정가이력(날짜,가격) -> ex) 20170131=33000;20160121=22000;
	private String val5; // 판매이력(날짜) -> ex) 20170131~;20160501~20160530;
	private String val6; // 수집이력 -> ex) 20170131;20170130;20170129;
	private String val7; // 제품상세URL -> ex) http://
	private String val8;
	private String val9;
	private String val10; // 백업파일위치
	private String lastCollectDay; // 마지막 수집일
	private short errorStep;
	private short errorNum = 0;
	private String errorMessage; // 실패메시지
	private boolean appliedYn = false; // 반영여부
	private Long jobExecutionId = null; // JOB ID

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getOnlineGoodsNum() {
		return onlineGoodsNum;
	}

	public void setOnlineGoodsNum(String onlineGoodsNum) {
		this.onlineGoodsNum = onlineGoodsNum;
	}

	public String getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		this.goodsNum = goodsNum;
	}
	
	public String getCollectURL() {
		return collectURL;
	}

	public void setCollectURL(String collectURL) {
		this.collectURL = collectURL;
	}

	public String getVal1() {
		return val1;
	}

	public void setVal1(String val1) {
		this.val1 = val1;
	}

	public String getVal2() {
		return val2;
	}

	public void setVal2(String val2) {
		this.val2 = val2;
	}

	public String getVal3() {
		return val3;
	}

	public void setVal3(String val3) {
		this.val3 = val3;
	}

	public String getVal4() {
		return val4;
	}

	public void setVal4(String val4) {
		this.val4 = val4;
	}

	public String getVal5() {
		return val5;
	}

	public void setVal5(String val5) {
		this.val5 = val5;
	}

	public String getVal6() {
		return val6;
	}

	public void setVal6(String val6) {
		this.val6 = val6;
	}

	public String getVal7() {
		return val7;
	}

	public void setVal7(String val7) {
		this.val7 = val7;
	}

	public String getVal8() {
		return val8;
	}

	public void setVal8(String val8) {
		this.val8 = val8;
	}

	public String getVal9() {
		return val9;
	}

	public void setVal9(String val9) {
		this.val9 = val9;
	}

	public String getVal10() {
		return val10;
	}

	public void setVal10(String val10) {
		this.val10 = val10;
	}

	public String getLastCollectDay() {
		return lastCollectDay;
	}

	public void setLastCollectDay(String lastCollectDay) {
		this.lastCollectDay = lastCollectDay;
	}

	public short getErrorStep() {
		return errorStep;
	}

	public void setErrorStep(short errorStep) {
		this.errorStep = errorStep;
	}

	public short getErrorNum() {
		return errorNum;
	}

	public void setErrorNum(short errorNum) {
		this.errorNum = errorNum;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isAppliedYn() {
		return appliedYn;
	}

	public void setAppliedYn(boolean appliedYn) {
		this.appliedYn = appliedYn;
	}

	public Long getJobExecutionId() {
		return jobExecutionId;
	}

	public void setJobExecutionId(Long jobExecutionId) {
		this.jobExecutionId = jobExecutionId;
	}
}
