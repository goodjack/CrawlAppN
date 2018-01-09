package com.eopcon.crawler.samsungcnt.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.MixxoStoreParser;

/**
 * 미쏘 사이트 클롤러 
 */
public class MixxoStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final String PAGE_ENCODING = "utf-8";

	public final static String BASE_URL = "http://mixxo.elandmall.com";
	public final static String BASE_HOST = "mixxo.elandmall.com";
	public final static String CATEGORY_PAGE_URL = "http://mixxo.elandmall.com/dispctg/searchGnbAllCategoryList.action";
	public final static String CATEGORY_DETAIL_PAGE_URL = "http://mixxo.elandmall.com/dispctg/initDispCtg.action?disp_ctg_no=";
	public final static String PRODUCT_STOCK_INFO_URL = "http://mixxo.elandmall.com/goods/searchGoodsItemList.action";
	public final static String PRODUCT_COMMENT_INFO_URL = "http://mixxo.elandmall.com/goods/searchGoodsEvalBody.action";
	public final static String PRODUCT_BEST_ITEM_INFO_URL = "http://mixxo.elandmall.com/shop/initBestBrandMall.action";
	
	private MixxoStoreParser parser;

	public MixxoStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		super(config, constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (MixxoStoreParser) super.parser;
	}

	/**
	 * 카테고리 정보를 가져온다.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Category> getCategories() throws Exception {

		List<Category> categories = new ArrayList<>();

		request.openConnection(CATEGORY_PAGE_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			return getAllCategories(parser.parseTopCategories_new(content), categories);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * 카테고리 하위 목록을 가져온다.
	 * 
	 * @param topCategories
	 * @param categories
	 * @return
	 * @throws Exception
	 */
	public List<Category> getAllCategories(List<Integer> topCategories, List<Category> categories) throws Exception {

		List<Category> tmpCategories = null;

		for (int topCategory : topCategories) {

			tmpCategories = new ArrayList<>();

			request.openConnection(CATEGORY_DETAIL_PAGE_URL + topCategory);

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("Upgrade-Insecure-Requests", "1");
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				tmpCategories = parser.parseCategories_new(content);
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}

			for (Category tmp : tmpCategories) {
				categories.add(tmp);
			}
		}
		return categories;
	}

	/**
	 * 상품목록을 가져온다.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Product> getProductList(Category category, int page) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		List<Product> productList;
		boolean lastPage = false;

		while (true) {
			if (lastPage)
				break;

			String categoryUrl = category.getCategoryUrl() + "&page_idx=" + page;

			request.openConnection(categoryUrl);

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Upgrade-Insecure-Requests", "1");
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();

				resultMap = parser.parseProductList(content, category, page);

				lastPage = (boolean) resultMap.get("lastPage");
				productList = (List<Product>) resultMap.get("productList");

				return productList;
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
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
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();

			Map<String, String> param = parser.parseProductDetail(content, productDetail, productURL);

			fillOutStockInfo(MixxoStoreParser.FLAG_PARSING_COLOR, productDetail, param);
			
			fillOutComments(productDetail);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
	}

	/**
	 * 제품의 재고를 수집 및 작성한다.
	 * 
	 * @param flag
	 * @param productDetail
	 * @param param
	 * @throws Exception
	 */
	public void fillOutStockInfo(int flag, ProductDetail productDetail, Map<String, String> param) throws Exception {
		request.openConnection(PRODUCT_STOCK_INFO_URL);
		
		switch(flag){
		case MixxoStoreParser.FLAG_PARSING_SIZE:
			request.addFormParameter("opt_val_nm1", param.get("opt_val_nm1"));
		default :
			request.addFormParameter("goods_no", param.get("goods_no"));
			request.addFormParameter("vir_vend_no", param.get("vir_vend_no"));
			request.addFormParameter("low_vend_type_cd", param.get("low_vend_type_cd"));
			request.addFormParameter("reserv_yn", param.get("reserv_yn"));
			request.addFormParameter("color_yn", param.get("color_yn"));
			break;
		}
		
		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("AJAX_YN", "Y");
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithFormData(PAGE_ENCODING, true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			parser.parseStockDetail(content, flag, productDetail, param);
			
			switch(flag){
			case MixxoStoreParser.FLAG_PARSING_COLOR:
				String[] colors = param.get(OnlineStoreConst.KEY_COLLECT_COLORS).split("; ");
				for(String color : colors){
					param.put("opt_val_nm1", color);
					fillOutStockInfo(MixxoStoreParser.FLAG_PARSING_SIZE, productDetail, param);
				}
				break;
			}
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
	}

	/**
	 * 상품평을 수집 및 작성한다.
	 * 
	 * @param productDetail
	 * @throws Exception
	 */
	public void fillOutComments(ProductDetail productDetail) throws Exception {
		int currentPage = 1;
		boolean lastPage = false;

		while (true) {
			if (lastPage)
				break;

			String goodsNum = productDetail.getGoodsNum();

			request.openConnection(PRODUCT_COMMENT_INFO_URL);

			request.addFormParameter("goods_no", goodsNum);
			request.addFormParameter("page_idx", String.valueOf(currentPage));

			request.addRequestHeader("Host", BASE_HOST);
			request.addRequestHeader("Origin", BASE_URL);
			request.addRequestHeader("AJAX_YN", "Y");
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			request.addRequestHeader("Accept-Language", "ko-KR");

			Result result = request.executeWithFormData(PAGE_ENCODING, true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				lastPage = parser.parseComments(content, productDetail, currentPage);

				currentPage++;
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
			
		}
	}

	/**
	 * 베스트 아이템 목록을 가져온다.
	 */
	@Override
	public List<String> getBestItems() throws Exception {
		List<String> bestItems = new ArrayList<>();

		request.openConnection(PRODUCT_BEST_ITEM_INFO_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Upgrade-Insecure-Requests", "1");
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			bestItems = parser.parseBestItems(content);

			return bestItems;
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		return Collections.emptyList();
	}
	
	/**
	 * 컬러 정보 매칭처리 한다.
	 */
	@Override
	protected boolean matchColor(String color1, String color2){
		final String COLOR_REGEX = "^\\([^\\(\\)]+\\)\\s*.+$";
		
		if(!(color1.matches(COLOR_REGEX) && color2.matches(COLOR_REGEX))) {
			if(color1.matches(COLOR_REGEX))
				color1 = color1.replaceAll("^\\([^\\(\\)]+\\)\\s*(.+)$", "$1");
			if(color2.matches(COLOR_REGEX))
				color2 = color2.replaceAll("^\\([^\\(\\)]+\\)\\s*(.+)$", "$1");
		}
		return color1.equals(color2);
	}
	
	/**
	 * SKU 번호/명/색상 정보를 가져온다.
	 * ※ MIXXO의 경우 괄호안에 색상코드를 포함한다.
	 * ex) (10)WHITE
	 */
	@Override
	public String[] getSkuNumAndNameAndColor(ProductDetail productDetail, String color) {
		String skuNum, skuName, skuColor;
		String goodsNum = productDetail.getGoodsNum();
		String goodsName = productDetail.getGoodsName();
		
		if(color.matches("^\\([^\\(\\)]+\\)\\s*.+$")){
			String colorCode = color.replaceAll("^\\(([^\\(\\)]+)\\)\\s*.+$", "$1");
			
			skuNum = String.format("%s/%s", goodsNum, colorCode);
			skuColor = color.replaceAll("^\\([^\\(\\)]+\\)\\s*(.+)$", "$1");
		} else {
			skuNum = String.format("%s/%s", goodsNum, color);
			skuColor = color;
		}
		
		skuName = String.format("%s / %s", goodsName, color);

		return new String[] { skuNum, skuName, skuColor };
	}
}
