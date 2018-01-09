package com.eopcon.crawler.samsungcnt.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.HnmCategory;
import com.eopcon.crawler.samsungcnt.model.HnmStock;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Sku;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HnmStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * H&M Site Crawler
 */
public class HnmStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final static String BASE_HOST = "www2.hm.com";
	private final static String BASE_URL = "http://www2.hm.com";
	private final static String CATEGORY_PAGE_URL = "http://www2.hm.com/ko_kr/index.html";
	private final static String BEST_ITEM_PAGE_URL = "http://www2.hm.com/ko_kr/ladies/offers-highlights/KRL30_bestsellers.html?product-type=KRL30_BESTSELLERS&sort=stock&offset=0&page-size=10000";	//  베스트아이템 URL이 사라졌음
	private final static long SLEEP_MILLIS = 2L;

	private HnmStoreParser parser;
	
	private ObjectMapper mapper = new ObjectMapper();

	public HnmStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		super(config, constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (HnmStoreParser) super.parser;
	}

	/**
	 * 크롤링할 Top 카테코리를 구함
	 */
	private List<HnmCategory> getTopCategories() throws Exception {
		request.openConnection(CATEGORY_PAGE_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		List<HnmCategory> topCategories = null;
		
		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			topCategories = parser.parseTopCategories(content);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		
		return topCategories;
	}

	/**
	 * 크롤링할 카테고리를 구함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Category> getCategories() throws Exception {
				
		List<Category> categories = new ArrayList<>(); 
		
		List<HnmCategory> topCategories = getTopCategories();
			
		List<HnmCategory> sub2Categories = getSubCategories(topCategories, categories, 2);
		List<HnmCategory> sub3Categories = getSubCategories(sub2Categories, categories, 3);
		getSubCategories(sub3Categories, categories, 4);
		
		if(categories.size() > 0)
			return categories;
		
		return Collections.EMPTY_LIST;
	}
	
	
	/**
	 * 크롤링할 left 카테고리를 구함
	 */
	private List<HnmCategory> getSubCategories(List<HnmCategory> tmpCategories, List<Category> categories, int depth) throws Exception {
		List<HnmCategory> subCategories = new ArrayList<>();
		
		for(HnmCategory tmpCategory : tmpCategories) {
			if(tmpCategory.isEndOfNode() == true) continue;
				
			request.openConnection(tmpCategory.getCategoryUrl());

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("Origin", BASE_URL);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			
			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				List<HnmCategory> tmpSubCategories = parser.parseSubCategories(content, tmpCategory, depth);
				subCategories.addAll(tmpSubCategories);
				
				if(tmpCategory.isEndOfNode()) {
					categories.add(tmpCategory.copy());
				}
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
		}
		
		return subCategories;
	}
	
	

	/**
	 * 베스트아이템을 리턴
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getBestItems() throws Exception {
		/* 베스트아이템 링크가 사라져 주석 처림함(2017.03.11)
		request.openConnection(BEST_ITEM_PAGE_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();
		
		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			return parser.parseBestItems(content);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		*/
		
		return Collections.EMPTY_LIST;
	}

	/**
	 * 카테고리에 해당하는 URL을 호출하여 상품목록을 구함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Product> getProductList(Category category, int page) throws Exception {
		switch (page) {
		case 1:
			String categoryUrl = category.getCategoryUrl();

			request.openConnection(categoryUrl);

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("Origin", BASE_URL);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				return parser.parseProductList(content, category);
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
			break;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 *  상품 상세 URL을 호출하여 상품 정보를 구함
	 */
	@Override
	public void fillOutProductDetail(String productURL, ProductDetail productDetail) throws Exception {

		request.openConnection(productURL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			parser.parseProductDetail(content, productDetail);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}

	}
	
	
	
	/**
	 * SKU정보를 반환한다.
	 * ※ H&M의 경우 색상별 이미지와 가격정보가 다르기 때문에 Override하여 처리함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Sku> getSkuInfomations(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();
		String collectURL = productDetail.getCollectURL();
		Integer price = productDetail.getPrice();
		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		
		Map<String, String> goodsImageMap = mapper.readValue(productDetail.getExtraString(HnmStoreParser.KEY_EXTRA_IMAGE_INFO), Map.class);
		Map<String, String> goodsPriceMap = mapper.readValue(productDetail.getExtraString(HnmStoreParser.KEY_EXTRA_PRICE_INFO), Map.class);

		for (Stock stock : stocks)
			set.add(stock.getColor());
		
		for (String color : colors) {
			
			boolean collected = set.contains(color);

			String skuNum;
			String skuName;
			String skuColor;
			
			String goodsImage = null;
			String goodsImageOrg = null;
			Integer discountPrice = null;
			Float discountRate = null;
			Boolean discounted = false;

			String[] numAndNameAndColor = getSkuNumAndNameAndColor(productDetail, color);

			skuNum = numAndNameAndColor[0];
			skuName = numAndNameAndColor[1];
			skuColor = numAndNameAndColor[2];
			
			// 수집된 Color일 경우
			if (collected) {
				goodsImage = goodsImageMap.get(color);
				goodsImageOrg = goodsImageMap.get(color);
				if (StringUtils.isEmpty(goodsImage))
					exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_FAIL, new IllegalArgumentException("[Assertion failed] - [GoodsImage] must have text; it must not be null, empty, or blank"));
				
				goodsImage = "";//saveImage_new(skuNum, goodsImage,productDetail.getSite());
				if(goodsPriceMap.get(color) != null) {
					discountPrice = Float.valueOf(goodsPriceMap.get(color)).intValue();
					discountRate = 100f - (discountPrice * 100f / price);
					discounted = price.intValue() > discountPrice.intValue();
				} 
			}
			
			Sku s = new Sku(collected);
			
			s.setSkuNum(skuNum);
			s.setSkuName(skuName);
			s.setColor(skuColor);
			s.setCollectURL(collectURL);
			s.setGoodsImage(goodsImage);
			s.setPrice(price);
			s.setGoodsImageOrg(goodsImageOrg);
			s.setDiscounted(discounted);
			s.setDiscountPrice(discountPrice);
			s.setDiscountRate(discountRate);
			s.setBestItem(bestItem);
			
			// 수집된 Color일 경우
			if (collected) {
				for (int i = 0; i < stocks.size(); i++) {
					Stock stock = stocks.get(i);
					if (color.equals(stock.getColor())){
						stock.setColor(skuColor);
						s.addStock(stock);
						
						stocks.remove(i--);
					}
				}
			} 
			
			for (int i = 0; i < materials.size(); i++) {
				Materials m = materials.get(i);
				if (color.equals(m.getColor())) {
					m.setColor(skuColor);
					s.addMaterials(m);
					
					materials.remove(i--);
				}
			}
			
			sku.add(s);
		}
		return sku;
	}
	
	/**
	 * SKU정보를 반환한다.
	 * ※ H&M의 경우 색상별 이미지와 가격정보가 다르기 때문에 Override하여 처리함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Sku> getSkuInfomations_new(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();
		String collectURL = productDetail.getCollectURL();
		Integer price = productDetail.getPrice();
		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		
		Map<String, String> goodsImageMap = mapper.readValue(productDetail.getExtraString(HnmStoreParser.KEY_EXTRA_IMAGE_INFO), Map.class);
		Map<String, String> goodsPriceMap = mapper.readValue(productDetail.getExtraString(HnmStoreParser.KEY_EXTRA_PRICE_INFO), Map.class);

		for (Stock stock : stocks)
			set.add(stock.getColor());
		
		for (String color : colors) {
			
			boolean collected = set.contains(color);

			String skuNum;
			String skuName;
			String skuColor;
			
			String goodsImage = null;
			String goodsImageOrg = null;
			Integer discountPrice = null;
			Float discountRate = null;
			Boolean discounted = false;

			String[] numAndNameAndColor = getSkuNumAndNameAndColor(productDetail, color);

			skuNum = numAndNameAndColor[0];
			skuName = numAndNameAndColor[1];
			skuColor = numAndNameAndColor[2];
			
			// 수집된 Color일 경우
			if (collected) {
				goodsImage = goodsImageMap.get(color);
				goodsImageOrg = goodsImageMap.get(color);
				if (StringUtils.isEmpty(goodsImage))
					exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_FAIL, new IllegalArgumentException("[Assertion failed] - [GoodsImage] must have text; it must not be null, empty, or blank"));
				
				goodsImage = "";//saveImage_new(skuNum, goodsImage,productDetail.getSite());
				if(goodsPriceMap.get(color) != null) {
					discountPrice = Float.valueOf(goodsPriceMap.get(color)).intValue();
					discountRate = 100f - (discountPrice * 100f / price);
					discounted = price.intValue() > discountPrice.intValue();
				} 
			}
			
			Sku s = new Sku(collected);
			
			s.setSkuNum(skuNum);
			s.setSkuName(skuName);
			s.setColor(skuColor);
			s.setCollectURL(collectURL);
			s.setGoodsImage(goodsImage);
			s.setPrice(price);
			s.setGoodsImageOrg(goodsImageOrg);
			s.setDiscounted(discounted);
			s.setDiscountPrice(discountPrice);
			s.setDiscountRate(discountRate);
			s.setBestItem(bestItem);
			
			// 수집된 Color일 경우
			if (collected) {
				for (int i = 0; i < stocks.size(); i++) {
					Stock stock = stocks.get(i);
					if (color.equals(stock.getColor())){
						stock.setColor(skuColor);
						s.addStock(stock);
						
						stocks.remove(i--);
					}
				}
			} 
			
			for (int i = 0; i < materials.size(); i++) {
				Materials m = materials.get(i);
				if (color.equals(m.getColor())) {
					m.setColor(skuColor);
					s.addMaterials(m);
					
					materials.remove(i--);
				}
			}
			
			sku.add(s);
		}
		return sku;
	}
	

	/**
	 * SKU 번호/명/색상 정보를 가져온다.
	 */
	@Override
	public String[] getSkuNumAndNameAndColor(ProductDetail productDetail, String color) {
		String skuNum, skuName, skuColor;
		
		String goodsNum = productDetail.getGoodsNum();
		String goodsName = productDetail.getGoodsName();
		
		if(color.matches("^\\d{2}\\s+.+$")){
			String colorCode = color.replaceAll("^(\\d{2})\\s+.+$", "$1");
			skuNum = String.format("%s/%s", goodsNum, colorCode);
		} else {
			skuNum = String.format("%s/%s", goodsNum, color);
		}
		
		skuName = String.format("%s / %s", goodsName, color);
		skuColor = color;

		return new String[] { skuNum, skuName, skuColor };
	}
}
