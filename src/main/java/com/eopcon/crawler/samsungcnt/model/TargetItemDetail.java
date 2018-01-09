package com.eopcon.crawler.samsungcnt.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class TargetItemDetail {
	private String saleYn;
	private String productCode;
	private int normalPrice;
	private int salePrice;
	private String brandName;
	private String imageUrl;
	private Map<String, ArrayList<String>> imageUrlMap;
	private List<String> imgageUrlList;
	private String s3ImageUrl;
	private Map<String, ArrayList<String>> s3ImageUrlMap;
	private List<String> s3ImageUrlList;
	private String productName;
	private String layer1;
	private String layer2;
	private String layer3;
	private String layer4;
	private String layer5;
	private String productColor;
	private Map<String, ArrayList<String>> productColorMap;
	private List<String> productColorList;
	private String productSize;
	private Map<String, ArrayList<String>> productSizeMap;
	private List<String> productSizeList;
	private String material;
	private String origin;
	private String stockInfo;
	private int totalStock;
	private String detailStock;
	private Map<String, Integer> detailStockMap;
	private String crawlDate;
	private String crawlUrl;
	private String bestYn;
	private String newYn;
	private String goodEval;
	private int rating;
	private String comment;
	private String desc;
	
	
}
