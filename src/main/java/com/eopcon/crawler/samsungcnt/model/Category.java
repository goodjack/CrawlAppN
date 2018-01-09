package com.eopcon.crawler.samsungcnt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Category implements Serializable {

	private static final long serialVersionUID = -3086125555075393213L;

	private List<String> categoryNames = new ArrayList<>();
	private String categoryUrl = null;
	private String productUrl = null;
	private String categoryCode = null;
	private DataStandard dataStandard = null;
	
	public DataStandard getDataStandard() {
		return dataStandard;
	}

	public void setDataStandard(DataStandard dataStandard) {
		this.dataStandard = dataStandard;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public List<String> getCategoryNames() {
		return categoryNames;
	}

	public void setCategoryNames(List<String> categoryNames) {
		this.categoryNames = categoryNames;
	}

	public void addCategoryName(String categoryName) {
		categoryNames.add(categoryName);
	}

	public String getCategoryUrl() {
		return categoryUrl;
	}

	public void setCategoryUrl(String categoryUrl) {
		this.categoryUrl = categoryUrl;
	}

	public String getProductUrl() {
		return productUrl;
	}

	void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

	public Category copy() {
		Category category = new Category();

		category.setCategoryNames(new ArrayList<>(categoryNames));
		category.setCategoryUrl(categoryUrl);

		return category;
	}
	
	@Override
	public boolean equals(Object object){
		if(object instanceof Category){
			String url = StringUtils.defaultString(((Category) object).getCategoryUrl());
			return url.equals(categoryUrl);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Category [categoryNames=" + categoryNames + ", categoryUrl=" + categoryUrl + "]";
	}
	
	
}
