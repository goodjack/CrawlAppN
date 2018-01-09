package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;

import org.apache.ibatis.type.Alias;

@Alias("naverkeyword")
public class NaverKeyword implements Serializable {

	private static final long serialVersionUID = 1L;

	private String cate1; // 카테고리1
	private String cate2; // 카테고리2
	private String cate3; // 카테고리3
	private String cate4; // 카테고리4

	private String startDay; // 수집시작일(yyyyMMdd)
	private String endDay; // 수집종료일(yyyyMMdd)
	
	private int cnt; // 조회수
	private int rank; // 랭크
	private int rankChange; // 검색어
	private String collectDay; // 수집일(yyyyMMdd)
	private String originCode; // 수집사이트코드
	private String keyword;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getOriginCode() {
		return originCode;
	}

	public void setOriginCode(String originCode) {
		this.originCode = originCode;
	}

	public String getCate1() {
		return cate1;
	}

	public void setCate1(String cate1) {
		this.cate1 = cate1;
	}

	public String getCate2() {
		return cate2;
	}

	public void setCate2(String cate2) {
		this.cate2 = cate2;
	}

	public String getCate3() {
		return cate3;
	}

	public void setCate3(String cate3) {
		this.cate3 = cate3;
	}

	public String getCate4() {
		return cate4;
	}

	public void setCate4(String cate4) {
		this.cate4 = cate4;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRankChange() {
		return rankChange;
	}

	public void setRankChange(int rankChange) {
		this.rankChange = rankChange;
	}
	
	public String getStartDay() {
		return startDay;
	}

	public void setStartDay(String startDay) {
		this.startDay = startDay;
	}
	
	public String getEndDay() {
		return endDay;
	}

	public void setEndDay(String endDay) {
		this.endDay = endDay;
	}	

	public String getCollectDay() {
		return collectDay;
	}

	public void setCollectDay(String collectDay) {
		this.collectDay = collectDay;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return "NaverKeyword [cate1=" + cate1 + ", cate2=" + cate2 + ", cate3=" + cate3 + ", cate4=" + cate4 + ", cnt=" + cnt + ", rank=" + rank + ", rankChange=" + rankChange + ", collectDay=" + collectDay + "]";
	}

}
