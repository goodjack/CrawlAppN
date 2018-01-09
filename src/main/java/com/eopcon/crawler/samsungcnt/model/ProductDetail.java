package com.eopcon.crawler.samsungcnt.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ProductDetail implements Serializable {

	private static final long serialVersionUID = -767716641664608746L;

	private String onlineGoodsNum; // 온라인 상품번호
	private String goodsNum; // 상품번호
	private String goodsName; // 상품명
	private String brandCode; // 브랜드코드
	private String collectURL; // URL
	private String goodsImage; // 상품이미지
	private Integer price; // 정가
	private Integer discountPrice; // 할인가
	private String maftOrigin; // 제조원산지
	private String site; // 사이트명
	private String goodsMaterials; // 소재정보 원본
	private boolean bestItem; // 베스트 아이템 여부
	private File backupFile = null; // 백업파일
	private DataStandard dataStandard = null;
	
	private List<Category> categories;
	private List<Materials> materials = new ArrayList<>(); // 소재정보
	private List<Stock> stocks = new ArrayList<>();; // 재고 정보
	private List<Comment> comments = new ArrayList<>();; // 상품평가정보

	private Map<String, String> extra = new HashMap<>(); // 기타 정보

	public DataStandard getDataStandard() {
		return dataStandard;
	}

	public void setDataStandard(DataStandard dataStandard) {
		this.dataStandard = dataStandard;
	}

	public ProductDetail(List<Category> categories) {
		this.categories = categories;
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

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getBrandCode() {
		return brandCode;
	}

	public void setBrandCode(String brandCode) {
		this.brandCode = brandCode;
	}

	public String getCollectURL() {
		return collectURL;
	}

	public void setCollectURL(String collectURL) {
		this.collectURL = collectURL;
	}

	public String getGoodsImage() {
		return goodsImage;
	}

	public void setGoodsImage(String goodsImage) {
		this.goodsImage = goodsImage;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public Integer getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Integer discountPrice) {
		this.discountPrice = discountPrice;
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

	public boolean isBestItem() {
		return bestItem;
	}

	public void setBestItem(boolean bestItem) {
		this.bestItem = bestItem;
	}

	public File getBackupFile() {
		return backupFile;
	}

	public void setBackupFile(File backupFile) {
		this.backupFile = backupFile;
	}

	public String getGoodsMaterials() {
		return goodsMaterials;
	}

	public void setGoodsMaterials(String goodsMaterials) {
		this.goodsMaterials = goodsMaterials;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public List<Materials> getMaterials() {
		return materials;
	}

	public void addMaterials(Materials materials) {
		this.materials.add(materials);
	}

	public List<Stock> getStocks() {
		return stocks;
	}

	public void addStock(Stock stock) {
		this.stocks.add(stock);
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void addComment(Comment comment) {
		this.comments.add(comment);
	}

	public String getExtraString(String key) {
		return extra.get(key);
	}

	public void putExtraString(String key, String value) {
		extra.put(key, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
