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
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Sku;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.UniqloStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 유니클로 사이트 크롤러 
 */
public class UniqloStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final String PAGE_ENCODING = "utf-8";

	public final static String BASE_HOST = "www.uniqlo.kr";
	public final static String BASE_URL = "http://www.uniqlo.kr";
	public final static String CATEGORY_PAGE_URL = "http://www.uniqlo.kr/display/displayShop.lecs?storeNo=22&siteNo=9&displayNo=UQ1A01A07&displayMallNo=UQ1";
	public final static String COMMENT_PAGE_URL = "http://www.uniqlo.kr/displayDetail/listGoodsAssessment.lecs";

	private UniqloStoreParser parser;
	
	private ObjectMapper mapper = new ObjectMapper();

	public UniqloStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		super(config, constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (UniqloStoreParser) super.parser;
	}

	/**
	 * 카테고리 목록을 가져온다.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Category> getCategories() throws Exception {
		request.openConnection(CATEGORY_PAGE_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			return parser.parseCategories(content);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * 베스트 아니템 목록을 가져온다.
	 * (유니클로는 베스트 아이템 항목이 없음)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getBestItems() throws Exception {
		return Collections.EMPTY_LIST;
	}

	/**
	 * 상품목록을 가져온다.
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
	 * 상품상세 정보를 작성한다.
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

		fillOutComments(productURL, productDetail);
	}

	/**
	 * 고객 상품평 목록을 처리한다.
	 * 
	 * @param productUrl
	 * @param productDetail
	 * @throws Exception
	 */
	private void fillOutComments(String productURL, ProductDetail productDetail) throws Exception {

		Map<String, List<String>> map = getQueryMap(productURL, PAGE_ENCODING);
		String goodsNo = null;

		if (map.containsKey("goodsNo"))
			goodsNo = map.get("goodsNo").get(0);

		String commentUrl = String.format("%s?nolayout=yes&goodsNo=%s&orderType=1", COMMENT_PAGE_URL, goodsNo);

		while (true) {
			request.openConnection(commentUrl);

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("Origin", BASE_URL);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				Map<String, Object> r = parser.parseComments(content, productDetail);

				Boolean lastPage = (Boolean) r.get(OnlineStoreConst.KEY_LAST_PAGE);
				String nextUrl = (String) r.get(OnlineStoreConst.KEY_NEXT_URL);

				if (lastPage)
					break;
				commentUrl = nextUrl;
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
		}
	}
	
	/**
	 * SKU정보를 반환한다.
	 * ※ UNQLO의 경우 색상별 이미지와 가격정보가 다르기 때문에 Override하여 처리함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Sku> getSkuInfomations(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();
		
		String collectURL = productDetail.getCollectURL();
		Integer price = productDetail.getPrice();
		
		Integer salePrice = productDetail.getDiscountPrice();

		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		
		Map<String, String> goodsImageMap = mapper.readValue(productDetail.getExtraString(UniqloStoreParser.KEY_EXTRA_IMAGE_INFO), Map.class);
		Map<String, String> goodsPriceMap = mapper.readValue(productDetail.getExtraString(UniqloStoreParser.KEY_EXTRA_PRICE_INFO), Map.class);

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
				
				discountPrice = salePrice - Integer.parseInt(goodsPriceMap.get(color));
				discountRate = 100f - (discountPrice * 100f / price);
				discounted = price.intValue() > discountPrice.intValue();
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
	 * ※ UNQLO의 경우 색상별 이미지와 가격정보가 다르기 때문에 Override하여 처리함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Sku> getSkuInfomations_new(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();
		
		String collectURL = productDetail.getCollectURL();
		Integer price = productDetail.getPrice();
		
		Integer salePrice = productDetail.getDiscountPrice();

		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		
		Map<String, String> goodsImageMap = mapper.readValue(productDetail.getExtraString(UniqloStoreParser.KEY_EXTRA_IMAGE_INFO), Map.class);
		Map<String, String> goodsPriceMap = mapper.readValue(productDetail.getExtraString(UniqloStoreParser.KEY_EXTRA_PRICE_INFO), Map.class);

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
				
				discountPrice = salePrice - Integer.parseInt(goodsPriceMap.get(color));
				discountRate = 100f - (discountPrice * 100f / price);
				discounted = price.intValue() > discountPrice.intValue();
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
	 * ※ UNIQLO의 경우 숫자 + 색상의 형식으로 처리됨
	 * ex) 00 OFF WHITE
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
