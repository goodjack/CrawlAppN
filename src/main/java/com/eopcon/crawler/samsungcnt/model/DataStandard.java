package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;
import java.util.List;

import org.json.simple.JSONArray;


// @Data
public class DataStandard implements Serializable {
	
	public DataStandard() {
		
	}
	
	public String getSaleYn() {
		return saleYn;
	}

	public void setSaleYn(String saleYn) {
		this.saleYn = saleYn;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public int getNormalPrice() {
		return normalPrice;
	}

	public void setNormalPrice(int normalPrice) {
		this.normalPrice = normalPrice;
	}

	public int getProductSalePrice() {
		return productSalePrice;
	}

	public void setProductSalePrice(int productSalePrice) {
		this.productSalePrice = productSalePrice;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public List<String> getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(List<String> imageUrl) {
		this.imageUrl = imageUrl;
	}

	public List<String> getS3ImageUrl() {
		return s3ImageUrl;
	}

	public void setS3ImageUrl(List<String> s3ImageUrl) {
		this.s3ImageUrl = s3ImageUrl;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getLayer1() {
		return layer1;
	}

	public void setLayer1(String layer1) {
		this.layer1 = layer1;
	}

	public String getLayer2() {
		return layer2;
	}

	public void setLayer2(String layer2) {
		this.layer2 = layer2;
	}

	public String getLayer3() {
		return layer3;
	}

	public void setLayer3(String layer3) {
		this.layer3 = layer3;
	}

	public String getLayer4() {
		return layer4;
	}

	public void setLayer4(String layer4) {
		this.layer4 = layer4;
	}

	public String getLayer5() {
		return layer5;
	}

	public void setLayer5(String layer5) {
		this.layer5 = layer5;
	}

	public String getLayer6() {
		return layer6;
	}

	public void setLayer6(String layer6) {
		this.layer6 = layer6;
	}

	public List<String> getProductColor() {
		return productColor;
	}

	public void setProductColor(List<String> productColor) {
		this.productColor = productColor;
	}

	public List<String> getProductSize() {
		return productSize;
	}

	public void setProductSize(List<String> productSize) {
		this.productSize = productSize;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getStockInfo() {
		return stockInfo;
	}

	public void setStockInfo(String stockInfo) {
		this.stockInfo = stockInfo;
	}

	public String getCrawlDate() {
		return crawlDate;
	}

	public void setCrawlDate(String crawlDate) {
		this.crawlDate = crawlDate;
	}

	public String getCrawlUrl() {
		return crawlUrl;
	}

	public void setCrawlUrl(String crawlUrl) {
		this.crawlUrl = crawlUrl;
	}

	public String getBestYn() {
		return bestYn;
	}

	public void setBestYn(String bestYn) {
		this.bestYn = bestYn;
	}

	public String getNewYn() {
		return newYn;
	}

	public void setNewYn(String newYn) {
		this.newYn = newYn;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDependentData() {
		return dependentData;
	}

	public void setDependentData(String dependentData) {
		this.dependentData = dependentData;
	}

	public JSONArray getGoodEval() {
		return goodEval;
	}

	public void setGoodEval(JSONArray goodEval) {
		this.goodEval = goodEval;
	}

	public String getCategoryUrl() {
		return categoryUrl;
	}

	public void setCategoryUrl(String categoryUrl) {
		this.categoryUrl = categoryUrl;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public DataStandard(DataStandard dataStandard) {
		saleYn = dataStandard.saleYn;
		productCode = dataStandard.productCode;
		normalPrice = dataStandard.normalPrice;
		productSalePrice = dataStandard.productSalePrice;
		brandName = dataStandard.brandName;
		imageUrl = dataStandard.imageUrl;
		s3ImageUrl = dataStandard.s3ImageUrl;
		productName = dataStandard.productName;
		layer1 = dataStandard.layer1;
		layer2 = dataStandard.layer2;
		layer3 = dataStandard.layer3;
		layer4 = dataStandard.layer4;
		layer5 = dataStandard.layer5;
		layer6 = dataStandard.layer6;
		productColor = dataStandard.productColor;
		productSize = dataStandard.productSize;
		material = dataStandard.material;
		origin = dataStandard.origin;
		stockInfo = dataStandard.stockInfo;
		crawlDate = dataStandard.crawlDate;
		crawlUrl = dataStandard.categoryUrl;
		bestYn = dataStandard.bestYn;
		newYn = dataStandard.newYn;
		grade = dataStandard.grade;
		desc = dataStandard.desc;
		dependentData = dataStandard.dependentData;
		goodEval = dataStandard.goodEval;
		categoryUrl = dataStandard.categoryUrl;
		categoryCode = dataStandard.categoryCode;
		pageSize = dataStandard.pageSize;
	}
	private String saleYn;
	private String productCode;
	private int normalPrice;
	private int productSalePrice;
	private String brandName;
	private List<String> imageUrl;
	private List<String> s3ImageUrl;
	private String productName;
	private String layer1;
	private String layer2;
	private String layer3;
	private String layer4;
	private String layer5;
	private String layer6;
	private List<String> productColor;
	private List<String> productSize;
	private String material;
	private String origin;
	private String stockInfo;
	private String crawlDate;
	private String crawlUrl;
	private String bestYn;
	private String newYn;
	private String grade;
	private String desc;
	private String dependentData;
	private JSONArray goodEval;
	private String categoryUrl;
	private String categoryCode;
	private int pageSize;
	
	//	private String itemUrl;
	@Override
	public String toString() {
		return "DataStandard [saleYn=" + saleYn + ", productCode=" + productCode + ", normalPrice=" + normalPrice + ", productSalePrice=" + productSalePrice + ", brandName=" + brandName + ", imageUrl=" + imageUrl + ", s3ImageUrl=" + s3ImageUrl + ", productName=" + productName + ", layer1=" + layer1 + ", layer2=" + layer2 + ", layer3=" + layer3 + ", layer4=" + layer4 + ", layer5=" + layer5 + ", layer6=" + layer6 + ", productColor=" + productColor + ", productSize=" + productSize + ", material=" + material + ", origin=" + origin + ", stockInfo=" + stockInfo + ", crawlDate=" + crawlDate + ", crawlUrl=" + crawlUrl + ", bestYn=" + bestYn + ", newYn=" + newYn + ", grade=" + grade + ", desc=" + desc + ", dependentData=" + dependentData + ", goodEval=" + goodEval + ", categoryUrl=" + categoryUrl
				+ ", categoryCode=" + categoryCode + ", pageSize=" + pageSize + "]";
	}
	
	
}
