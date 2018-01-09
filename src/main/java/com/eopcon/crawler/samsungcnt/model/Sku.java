package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.type.Alias;

import com.eopcon.crawler.samsungcnt.util.StringUtil;

@Alias("sku")
public class Sku implements Serializable {

	private static final long serialVersionUID = 5707156151188962540L;

	/* 기본정보 */
	private Long id; // SKU아이디
	private Long goodsId; // 상품아이디
	private String skuNum; // SKU번호
	private String skuName; // SKU명
	private String color; // 색상
	private String collectURL; // 수집URL
	private String goodsImage; // 상품이미지
	private Integer price; // 상품정가
	/* 할인이력정보 */
	private boolean discounted; // 할인여부
	private Integer discountPrice; // 할인가
	private Float discountRate; // 할인율
	/* 베스트 아이템정보 */
	private boolean bestItem; // 베스트 아이템 여부
	
	private boolean collected; // 수집여부
	private String goodsImageOrg;
	private List<Materials> materials = new ArrayList<>(); // 소재정보
	private List<Stock> stocks = new ArrayList<>(); // 재고 정보
	private String s3ImageUrl;
	
	public Sku() {
		this(true);
	}

	public Sku(boolean collected) {
		this.collected = collected;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGoodsId() {
		return goodsId;
	}

	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}

	public String getSkuNum() {
		return skuNum;
	}

	public void setSkuNum(String skuNum) {
		this.skuNum = skuNum;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
        this.skuName = StringUtil.replaceSymbols(skuName);
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
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

	public boolean isDiscounted() {
		return discounted;
	}

	public void setDiscounted(boolean discounted) {
		this.discounted = discounted;
	}

	public Integer getDiscountPrice() {
		return discountPrice;
	}

	public void setDiscountPrice(Integer discountPrice) {
		this.discountPrice = discountPrice;
	}

	public Float getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(Float discountRate) {
		this.discountRate = discountRate;
	}

	public boolean isBestItem() {
		return bestItem;
	}

	public void setBestItem(boolean bestItem) {
		this.bestItem = bestItem;
	}
	
	public boolean isCollected() {
		return collected;
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

	public String getGoodsImageOrg() {
		return goodsImageOrg;
	}

	public void setGoodsImageOrg(String goodsImageOrg) {
		this.goodsImageOrg = goodsImageOrg;
	}

	public String getS3ImageUrl() {
		return s3ImageUrl;
	}

	public void setS3ImageUrl(String s3ImageUrl) {
		this.s3ImageUrl = s3ImageUrl;
	}
	
	
}
