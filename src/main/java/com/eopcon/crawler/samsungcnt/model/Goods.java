package com.eopcon.crawler.samsungcnt.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.type.Alias;

@Alias("goods")
public class Goods {

	@Override
	public String toString() {
		return "Goods [id=" + id + ", goodsNum=" + goodsNum + ", goodsCate1=" + goodsCate1 + ", goodsCate2=" + goodsCate2 + ", goodsCate3=" + goodsCate3 + ", goodsCate4=" + goodsCate4 + ", brandCode=" + brandCode + ", price=" + price + ", releaseDt=" + releaseDt + ", sellCloseDt=" + sellCloseDt + ", sellPrd=" + sellPrd + ", maftOrigin=" + maftOrigin + ", site=" + site + ", status=" + status + ", collectDay=" + collectDay + ", lastCollectDay=" + lastCollectDay + ", sku=" + sku + ", comments=" + comments + ", orgCateName=" + orgCateName + ", coltItemMap=" + coltItemMap + "]";
	}

	/* 기본정보 */
	private Long id; // 상품아이디
	private String goodsNum; // 상품번호
	private String goodsCate1; // 상품카테고리1
	private String goodsCate2; // 상품카테고리2
	private String goodsCate3; // 상품카테고리3
	private String goodsCate4; // 상품카테고리4
	private String brandCode; // 브랜드코드
	private Integer price; // 상품정가
	private Date releaseDt; // 출시일
	private Date sellCloseDt; // 판매종료일
	private Integer sellPrd; // 판매기간
	private String maftOrigin; // 제조원산지
	private String site; // 사이트명
	private int status = 1; // 상태
	private String collectDay; // 수집일(yyyyMMdd)
	private String lastCollectDay; // 지난 수집일(yyyyMMdd)

	private List<Sku> sku; // 상품평가정보
	private List<Comment> comments; // 상품평가정보

	private String orgCateName = "";	//원본카테고리(depth는 ;로 구분)
	
	private Map<String,Object> coltItemMap;
	
	public Map<String, Object> getColtItemMap() {
		return coltItemMap;
	}

	public void setColtItemMap(Map<String, Object> coltItemMap) {
		this.coltItemMap = coltItemMap;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGoodsNum() {
		return goodsNum;
	}

	public void setGoodsNum(String goodsNum) {
		this.goodsNum = goodsNum;
	}

	public String getGoodsCate1() {
		return goodsCate1;
	}

	public void setGoodsCate1(String goodsCate1) {
		this.goodsCate1 = goodsCate1;
	}

	public String getGoodsCate2() {
		return goodsCate2;
	}

	public void setGoodsCate2(String goodsCate2) {
		this.goodsCate2 = goodsCate2;
	}

	public String getGoodsCate3() {
		return goodsCate3;
	}

	public void setGoodsCate3(String goodsCate3) {
		this.goodsCate3 = goodsCate3;
	}

	public String getGoodsCate4() {
		return goodsCate4;
	}

	public void setGoodsCate4(String goodsCate4) {
		this.goodsCate4 = goodsCate4;
	}

	public String getBrandCode() {
		return brandCode;
	}

	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Date getReleaseDt() {
		return releaseDt;
	}

	public void setReleaseDt(Date releaseDt) {
		this.releaseDt = releaseDt;
	}

	public Date getSellCloseDt() {
		return sellCloseDt;
	}

	public void setSellCloseDt(Date sellCloseDt) {
		this.sellCloseDt = sellCloseDt;
	}

	public Integer getSellPrd() {
		return sellPrd;
	}

	public void setSellPrd(Integer sellPrd) {
		this.sellPrd = sellPrd;
	}

	public String getMaftOrigin() {
		return maftOrigin;
	}

	public void setMaftOrigin(String maftOrigin) {
		this.maftOrigin = maftOrigin;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCollectDay() {
		return collectDay;
	}

	public void setCollectDay(String collectDay) {
		this.collectDay = collectDay;
	}

	public String getLastCollectDay() {
		return lastCollectDay;
	}

	public void setLastCollectDay(String lastCollectDay) {
		this.lastCollectDay = lastCollectDay;
	}

	public List<Sku> getSku() {
		return sku;
	}

	public void setSku(List<Sku> sku) {
		this.sku = sku;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getOrgCateName() {
		if(orgCateName.length() > 0)
			return orgCateName.substring(0, orgCateName.length()-1);
		else 
			return orgCateName;
	}

	public void setOrgCateName(String orgCateName) {
		this.orgCateName = orgCateName;
	}
	
	public Goods addOrgCateName(String orgCateName) {
		if(StringUtils.isNotEmpty(orgCateName)) {
			this.orgCateName += orgCateName + ";";
		}
		return this;
	}
	
	

}
