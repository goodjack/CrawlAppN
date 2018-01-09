package com.eopcon.crawler.samsungcnt.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Product implements Serializable {

	private static final long serialVersionUID = -2838377764649092254L;

	private List<Category> categories = new ArrayList<>();
	private String onlineGoodsNum = null; // 온라인 상품번호
	private String brandName = null; // 브랜드 명
	private File queueFile = null;
	private File backupFile = null;
	private DataStandard dataStandard = null; // 한섬

	
	public DataStandard getDataStandard() {
		return dataStandard;
	}

	public void setDataStandard(DataStandard dataStandard) {
		this.dataStandard = dataStandard;
	}

	public Product(Category category, String productUrl, String onlineGoodsNum) {
		Category c = category.copy();
		c.setProductUrl(productUrl);

		this.categories.add(c);
		this.onlineGoodsNum = onlineGoodsNum;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public String getOnlineGoodsNum() {
		return onlineGoodsNum;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public File getBackupFile() {
		return backupFile;
	}

	public void setBackupFile(File backupFile) {
		this.backupFile = backupFile;
	}

	public File getQueueFile() {
		return queueFile;
	}

	public void setQueueFile(File queueFile) {
		this.queueFile = queueFile;
	}

	public void merge(Product product) {
		for (Category c : product.getCategories()) {
			if (!categories.contains(c))
				categories.add(c);
		}
	}

	@Override
	public String toString() {
		return "Product [dataStandard=" + dataStandard + "]";
	}
	
}
