package com.eopcon.crawler.samsungcnt.service.impl;

import java.util.ArrayList;
import java.util.Collections;
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
import com.eopcon.crawler.samsungcnt.service.parser.impl.LfmallStoreParser;

public class LfmallStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final String PAGE_ENCODING = "utf-8";

	public final static String BASE_HOST = "www.lfmall.co.kr";
	public final static String BASE_URL = "http://www.lfmall.co.kr";

	public final static String API_BASE_HOST = "mapi.lfmall.co.kr";
	public final static String API_BASE_URL = "http://mapi.lfmall.co.kr";
	
	public final static String MOBILE_BASE_URL = "http://m.lfmall.co.kr";

	public final static String CATEGORY_PAGE_URL = "http://www.lfmall.co.kr/display.do?cmd=main";
	public final static String PRODUCT_LIST_INIT_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/category";
	public final static String PRODUCT_LIST_MORE_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/more";
	public final static String PRODUCT_PAGE_URL = "http://www.lfmall.co.kr/product.do?cmd=getProductDetail";
	public final static String PRODUCT_DETAIL_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/detail";
	public final static String PRODUCT_DETAIL_MORE_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/detail/informationNotice";
	public final static String COMMENT_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/detail/review";
	public final static String COMMENT_MORE_PAGE_URL = "http://mapi.lfmall.co.kr/api/products/detail/moreReview";
	
	private final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Mobile Safari/537.36";

	private LfmallStoreParser parser;

	public LfmallStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		super(config, constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (LfmallStoreParser) super.parser;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getBestItems() throws Exception {
		return Collections.EMPTY_LIST;
	}

	@Override
	public List<Category> getCategories() throws Exception {
		List<Category> categories = new ArrayList<>();
		List<Category> mainCategories = getMainCategories();

		for (Category category : mainCategories)
			categories.addAll(getSubCategories(category));
		return categories;
	}

	@SuppressWarnings("unchecked")
	private List<Category> getMainCategories() throws Exception {
		request.openConnection(CATEGORY_PAGE_URL);

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			return parser.parseMainCategories(content);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		return Collections.EMPTY_LIST;
	}

	@SuppressWarnings("unchecked")
	private List<Category> getSubCategories(Category category) throws Exception {
		request.openConnection(category.getCategoryUrl());

		request.addRequestHeader("Host", BASE_HOST);
		request.addRequestHeader("Origin", BASE_URL);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			return parser.parseSubCategories(content, category);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Product> getProductList(Category category, int page) throws Exception {

		if (page > 1) {
			Boolean lastPage = parser.getObject(OnlineStoreConst.KEY_LAST_PAGE, true);
			if (lastPage) {
				parser.removeObject(OnlineStoreConst.KEY_PAY_LOAD, OnlineStoreConst.KEY_TOTAL_COUNT, OnlineStoreConst.KEY_LOAD_COUNT, OnlineStoreConst.KEY_LAST_PAGE);
				return Collections.EMPTY_LIST;
			}
		}

		String categoryUrl = category.getCategoryUrl();
		Map<String, List<String>> queryMap = getQueryMap(categoryUrl, PAGE_ENCODING);

		String id = queryMap.get("id").get(0);
		String url = "";

		switch (page) {
		case 1:
			url = addJsonpCallback(PRODUCT_LIST_INIT_PAGE_URL + "?id=" + id);
			break;
		default:
			url = PRODUCT_LIST_MORE_PAGE_URL;
			break;
		}

		Result result = null;
		int responseCode;

		switch (page) {
		case 1:
			request.openConnection(url);

			request.addRequestHeader("Host", API_BASE_HOST);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");
			request.addRequestHeader("Referer", categoryUrl);

			result = request.executeWithGet(true);
			responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				return parser.parseProductList(content, page, category);
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
			break;
		default:
			request.openConnection(url);

			request.addRequestHeader("Host", API_BASE_HOST);
			request.addRequestHeader("Origin", API_BASE_URL);
			request.addRequestHeader("User-Agent", USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");
			request.addRequestHeader("Referer", categoryUrl);
			request.addRequestHeader("Content-Type", "application/json");
			request.addRequestHeader("pcweb", String.valueOf(true));
			String body = parser.getObject(OnlineStoreConst.KEY_PAY_LOAD, "{}");

			result = request.executeWithPost(body, PAGE_ENCODING, true);
			responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				return parser.parseProductList(content, page, category);
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
			break;
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public void fillOutProductDetail(String productURL, ProductDetail productDetail) throws Exception {

		Map<String, List<String>> queryMap = getQueryMap(productURL, PAGE_ENCODING);
		String prodCd = null;
		String url = null;

		Result result = null;
		int responseCode;

		if (queryMap.containsKey("PROD_CD"))
			prodCd = queryMap.get("PROD_CD").get(0);

		url = addJsonpCallback(PRODUCT_DETAIL_PAGE_URL + "?productId=" + prodCd);
		request.openConnection(url);

		request.addRequestHeader("Host", API_BASE_HOST);
		request.addRequestHeader("User-Agent", USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");

		result = request.executeWithGet(true);
		responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			parser.parseProductDetail(content, productDetail);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}

		url = addJsonpCallback(PRODUCT_DETAIL_MORE_PAGE_URL + "?productId=" + prodCd);
		request.openConnection(url);

		request.addRequestHeader("Host", API_BASE_HOST);
		request.addRequestHeader("User-Agent", MOBILE_USER_AGENT);
		request.addRequestHeader("Accept-Language", "ko-KR");
		request.addRequestHeader("Referer", productURL.replace(BASE_URL, MOBILE_BASE_URL));

		result = request.executeWithGet(true);
		responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			parser.parseMoreInfomation(content, productDetail);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}

		fillOutComments(productURL, prodCd, productDetail);
	}

	/**
	 * 고객 상품평 목록을 처리한다.
	 * 
	 * @param productUrl
	 * @param productDetail
	 * @throws Exception
	 */
	private void fillOutComments(String productURL, String prodCd, ProductDetail productDetail) throws Exception {

		int page = 1;
		while (true) {
			String commentUrl;

			switch (page) {
			case 1:
				commentUrl = addJsonpCallback(COMMENT_PAGE_URL + "?productId=" + prodCd + "&page=1&orderType=N");
				break;
			default:
				int totalCount = parser.getObject(OnlineStoreConst.KEY_TOTAL_COUNT, 0);
				commentUrl = addJsonpCallback(COMMENT_MORE_PAGE_URL + "?productId=" + prodCd + "&page=" + page + "&totalCount=" + totalCount + "&orderType=N");
				break;
			}

			request.openConnection(commentUrl);

			request.addRequestHeader("Host", API_BASE_HOST);
			request.addRequestHeader("User-Agent", MOBILE_USER_AGENT);
			request.addRequestHeader("Accept-Language", "ko-KR");
			request.addRequestHeader("Referer", productURL.replace(BASE_URL, MOBILE_BASE_URL));

			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString();
				parser.parseComments(content, page, productDetail);

				int lastPageNumber = parser.getObject(OnlineStoreConst.KEY_LAST_PAGE, 1);
				if (lastPageNumber <= page) {
					parser.removeObject(OnlineStoreConst.KEY_LAST_PAGE, OnlineStoreConst.KEY_TOTAL_COUNT);
					break;
				}
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
			}
			page++;
		}
	}

	/**
	 * JSONP 사용시 사용되는 Callback함수 파라미터 생성
	 * 
	 * @param url
	 * @return
	 */
	private String addJsonpCallback(String url) {
		long timestamp = System.currentTimeMillis();
		String queryString = String.format("jsonpCallback=jQuery191%s_%s&_=%s", String.valueOf(Math.random()).replaceAll("\\D", ""), timestamp, timestamp + 1);
		return url + (url.indexOf('?') == -1 ? "?" : "&") + queryString;
	}
}
