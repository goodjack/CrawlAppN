package com.eopcon.crawler.samsungcnt.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.ZaraStoreParser;

/**
 * ZARA Site Crawler
 */
public class ZaraStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final String PAGE_ENCODING = "utf-8";

	@Value("${zara.http.host:'www.zara.com'}")
	private String host = "www.zara.com";
	
	@Value("${zara.http.origin:'https://www.zara.com'}")
	private String origin = "https://www.zara.com";

	@Value("${zara.category.page.url:'https://www.zara.com/kr'}")
	private String categoryPageUrl = "https://www.zara.com/kr";
	
	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

	private ZaraStoreParser parser;
	
	private List<String> bestItems = new ArrayList<String>();
	
	
	public ZaraStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		super(config, constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (ZaraStoreParser) super.parser;
	}

	/**
	 * 크롤링한 카테고리를 구함 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Category> getCategories() throws Exception {
		request.openConnection(categoryPageUrl);

		request.addRequestHeader("Host", host);
		request.addRequestHeader("User-Agent", USER_AGENT);
		
		// 6.22 추가
		request.addRequestHeader("Upgrade-Insecure-Requests", "1");
		request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.addRequestHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");

		Result result = request.executeWithGet(true);
		
		if (result.getResponseCode() == HttpStatus.SC_OK) {
			String content = result.getString();
			List<Category> categories = parser.parseCategories(content);
			parser.getBestItmes(categories, bestItems);
			return categories;
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
		return Collections.EMPTY_LIST;
	}

	
	/**
	 * 카테고리 URL을 호출하여 상품 목록을 구함
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Product> getProductList(Category category, int page) throws Exception {
		switch (page) {
		case 1:
			String categoryUrl = category.getCategoryUrl();

			request.openConnection(categoryUrl);

			request.addRequestHeader("Host", host);
			request.addRequestHeader("Origin", origin);
			request.addRequestHeader("User-Agent", USER_AGENT);
			
			// 6.22 추가
			request.addRequestHeader("Upgrade-Insecure-Requests", "1");
			request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			request.addRequestHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");

			Result result = request.executeWithGet(true);

			if (result.getResponseCode() == HttpStatus.SC_OK) {
				String content = result.getString();
				return parser.parseProductList(content, category);
			}
			break;
		}
		return Collections.EMPTY_LIST;
	}

	/**
	 * 상품상세 페이지를 호출하여 상품 정보를 구함
	 */
	@Override
	public void fillOutProductDetail(String productURL, ProductDetail productDetail) throws Exception {


		request.openConnection(productURL);

		request.addRequestHeader("Host", host);
		request.addRequestHeader("Origin", origin);
		request.addRequestHeader("User-Agent", USER_AGENT);
		//request.addRequestHeader("Accept-Language", "ko-KR");
		
		// 6.22 추가
		request.addRequestHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
		request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		

		Result result = request.executeWithGet(true);
		int responseCode = result.getResponseCode();

		if (responseCode == HttpStatus.SC_OK) {
			String content = result.getString();
			parser.parseProductDetail(content, productDetail, productURL);
		} else if(responseCode == HttpStatus.SC_GONE) {
			exceptionBuilder.raiseException(ErrorType.GONE, responseCode);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, responseCode);
		}

	}

	/**
	 * 베스트 아이템을 리턴
	 */
	@Override
	public List<String> getBestItems() throws Exception {
		return bestItems;
	}
}
