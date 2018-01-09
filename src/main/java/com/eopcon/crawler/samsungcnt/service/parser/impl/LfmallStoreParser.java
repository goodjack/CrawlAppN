package com.eopcon.crawler.samsungcnt.service.parser.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Comment;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.aspect.annotation.Logging;
import com.eopcon.crawler.samsungcnt.service.impl.LfmallStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LfmallStoreParser extends OnlineStoreParser implements InitializingBean {

	private final static String[] TOP_CATEGORIES = new String[] { "패션" };
	private final static String ALL_CATEGORIES = "^(?:전체)$";

	private final static String FILTER_CATEGORIES = "^(?:여성의류|남성의류)$";

	private ObjectMapper mapper = new ObjectMapper();

	public LfmallStoreParser(OnlineStoreConst constant) {
		super(constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}

	@Logging
	public List<Category> parseMainCategories(String content) throws Exception {
		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("ul.category_lst > li");

		String categoryName1 = null;
		String categoryName2 = null;
		String categoryUrl = null;

		for (int i = 0; i < TOP_CATEGORIES.length; i++) {
			Element element = elements.get(i);

			categoryName1 = TOP_CATEGORIES[i];
			categoryName2 = null;

			for (Element el : element.select(" div.category_overview > div.inner > div.sub_category")) {
				categoryName2 = text(el.select("> dl > dt"), false);

				if (!categoryName2.matches(FILTER_CATEGORIES))
					continue;

				for (Element e : el.select("> dl > dd")) {
					String title = text(e.select("> a"), false);

					if (title.matches(ALL_CATEGORIES)) {
						categoryUrl = LfmallStoreCrawler.BASE_URL + attr(e.select("> a"), "data-url");

						Category category = new Category();
						category.setCategoryUrl(categoryUrl);

						category.addCategoryName(categoryName1);
						category.addCategoryName(categoryName2);

						categories.add(category);
						break;
					}
				}
			}
		}
		return categories;
	}

	@Logging
	public List<Category> parseSubCategories(String content, Category category) throws Exception {
		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("ul.cate_lst > li[id^='menuCategory-depth2']");

		String categoryName = null;
		String categoryUrl = null;

		for (Element element : elements) {
			categoryName = text(element.select("> a.depth2_tit"), false);

			if (categoryName.matches(ALL_CATEGORIES))
				continue;

			for (Element el : element.select("> div.depth3 > ul > li[id^='menuCategory-depth3']")) {
				Category c = category.copy();
				c.addCategoryName(categoryName);

				categoryName = text(el.select("> a"), false);
				categoryUrl = attr(el.select("> a.depth3_tit"), "href");

				c.addCategoryName(categoryName);
				c.setCategoryUrl(categoryUrl);

				categories.add(c);
			}
		}
		return categories;
	}

	@Logging
	@SuppressWarnings("unchecked")
	public List<Product> parseProductList(String content, int page, Category category) throws Exception {
		List<Product> productList = new ArrayList<>();
		String jsonString = content;

		switch (page) {
		case 1:
			jsonString = removeJsonpCallback(jsonString);
			break;
		}

		Map<String, Object> json = mapper.readValue(jsonString, Map.class);
		String message = StringUtils.defaultString((String) json.get("message"));

		if (message.equals("SUCCESS")) {
			Map<String, Object> results = (Map<String, Object>) json.get("results");
			List<Map<String, Object>> products = (List<Map<String, Object>>) results.get("products");

			for (Map<String, Object> product : products) {
				String onlineGoodsNum = StringUtils.defaultString((String) product.get("id"));
				String brandName = StringUtils.defaultString((String) product.get("brandEngName"));
				String productUrl = String.format("%s&PROD_CD=%s", LfmallStoreCrawler.PRODUCT_PAGE_URL, onlineGoodsNum);

				Product p = new Product(category, productUrl, onlineGoodsNum);
				p.setBrandName(brandName);

				productList.add(p);
			}

			Map<String, Object> filterCondition = (Map<String, Object>) results.get("filterCondition");

			String conditionKey = StringUtils.defaultString((String) filterCondition.get("conditionKey"));

			filterCondition.put("sortType", String.valueOf(1));
			filterCondition.put("selectBrandGroupIds", Collections.EMPTY_LIST);
			filterCondition.put("selectCategoryIds", Arrays.asList(conditionKey));
			filterCondition.put("selectSizes", Collections.EMPTY_LIST);
			filterCondition.put("selectColors", Collections.EMPTY_LIST);
			filterCondition.put("lowPrice", String.valueOf(0));
			filterCondition.put("highPrice", String.valueOf(0));

			Map<String, Object> productFilter = (Map<String, Object>) results.get("productFilter");
			Integer totalProductCount;
			Integer loadProductCount = getObject(OnlineStoreConst.KEY_LOAD_COUNT, 0) + products.size();

			if (page == 1)
				totalProductCount = (Integer) productFilter.get("totalProductCount");
			else
				totalProductCount = getObject(OnlineStoreConst.KEY_TOTAL_COUNT, 0);

			boolean lastPage = loadProductCount >= totalProductCount;

			putObject(OnlineStoreConst.KEY_PAY_LOAD, mapper.writeValueAsString(filterCondition));
			putObject(OnlineStoreConst.KEY_TOTAL_COUNT, totalProductCount);
			putObject(OnlineStoreConst.KEY_LOAD_COUNT, loadProductCount);
			putObject(OnlineStoreConst.KEY_LAST_PAGE, lastPage);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL);
		}
		return productList;
	}

	@Logging
	@SuppressWarnings("unchecked")
	public void parseProductDetail(String content, ProductDetail productDetail) throws Exception {

		String jsonString = removeJsonpCallback(content);

		Map<String, Object> json = mapper.readValue(jsonString, Map.class);
		String message = StringUtils.defaultString((String) json.get("message"));

		if (message.equals("SUCCESS")) {
			Map<String, Object> results = (Map<String, Object>) json.get("results");
			Map<String, Object> pd = (Map<String, Object>) results.get("productDetail");
			Map<String, Object> representImage = (Map<String, Object>) pd.get("representImage");
			List<String> colors = (List<String>) pd.get("colors");
			List<Map<String, Object>> itemStocks = (List<Map<String, Object>>) pd.get("itemStocks");
			List<Map<String, Object>> sizes = (List<Map<String, Object>>) itemStocks.get(0).get("sizes");

			String goodsNum = StringUtils.defaultString((String) pd.get("id"));
			String goodsName = StringUtils.defaultString((String) pd.get("name"));
			String goodsImage = StringUtils.defaultString((String) representImage.get("url"));
			Integer price = (Integer) pd.get("originalPrice");
			Integer discountPrice = (Integer) pd.get("salePrice");
			String goodsMaterials = StringUtils.defaultString((String) pd.get("materialDescription"));
			String color = colors.get(0);

			productDetail.setGoodsNum(goodsNum);
			productDetail.setGoodsName(goodsName);
			productDetail.setGoodsImage(goodsImage);
			productDetail.setPrice(price);
			productDetail.setDiscountPrice(discountPrice);
			productDetail.setGoodsMaterials(goodsMaterials);
			productDetail.setBrandCode("00"); // 테스트

			for (Map<String, Object> m : sizes) {
				String size = StringUtils.defaultString((String) m.get("value"));
				int stockAmount = (Integer) m.get("stock");

				Stock stock = new Stock();

				stock.setColor(color);
				stock.setSize(size);
				stock.setStockAmount(stockAmount);
				stock.setOpenMarketStockAmount(0);

				productDetail.addStock(stock);
			}
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL);
		}
	}

	@Logging
	@SuppressWarnings("unchecked")
	public void parseMoreInfomation(String content, ProductDetail productDetail) throws Exception {

		String jsonString = removeJsonpCallback(content);

		Map<String, Object> json = mapper.readValue(jsonString, Map.class);
		String message = StringUtils.defaultString((String) json.get("message"));

		if (message.equals("SUCCESS")) {
			Map<String, Object> results = (Map<String, Object>) json.get("results");
			List<Map<String, Object>> informationNotifications = (List<Map<String, Object>>) results.get("informationNotifications");

			if (informationNotifications.size() > 0) {
				Map<String, Object> informationNotification = informationNotifications.get(0);
				String html = (String) informationNotification.get("value");
				Document doc = Jsoup.parse(html);

				String maftOrigin = "";
				String goodsMaterials = productDetail.getGoodsMaterials();

				for (Element el : doc.select("table.tbl-details table.tbl-y > tbody > tr > th")) {
					String title = text(el, false);

					if (title.equals("제품 소재(제품 주소재)")) {
						if(StringUtils.isEmpty(goodsMaterials))
							goodsMaterials = text(el.nextElementSibling(), false);
					}else if (title.equals("제조국")) {
						String temp = text(el.nextElementSibling(), false);
						maftOrigin = temp.replaceAll("^([가-힣a-zA-Z\\s]+)\\*?.*$", "$1").trim();
					}
				}
				
				productDetail.setGoodsMaterials(goodsMaterials);
				productDetail.setMaftOrigin(maftOrigin);
			}
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL);
		}
	}

	@Logging
	@Override
	public List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception {
		return null;
	}

	@Logging
	@SuppressWarnings("unchecked")
	public void parseComments(String content, int page, ProductDetail productDetail) throws Exception {

		String jsonString = removeJsonpCallback(content);

		Map<String, Object> json = mapper.readValue(jsonString, Map.class);
		String message = StringUtils.defaultString((String) json.get("message"));

		if (message.equals("SUCCESS")) {
			Map<String, Object> results = (Map<String, Object>) json.get("results");
			Map<String, Object> reviews = (Map<String, Object>) results.get("reviews");

			Map<String, Object> paginator = (Map<String, Object>) reviews.get("paginator");
			List<Map<String, Object>> list = (List<Map<String, Object>>) reviews.get("reviews");

			if (page == 1) {
				int totalCount = (Integer) paginator.get("totalCount");
				int lastPageNumber = (Integer) paginator.get("endPage");

				putObject(OnlineStoreConst.KEY_TOTAL_COUNT, totalCount);
				putObject(OnlineStoreConst.KEY_LAST_PAGE, lastPageNumber);
			}

			for (Map<String, Object> review : list) {

				Integer score = (Integer) review.get("score");

				Float goodsRating = score.floatValue(); // 평점
				String goodsComment = StringUtils.defaultString((String) review.get("content")); // 내용

				Comment comment = new Comment();

				comment.setGoodsRating(goodsRating);
				comment.setGoodsComment(goodsComment);

				productDetail.addComment(comment);
			}
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL);
		}
	}

	private String removeJsonpCallback(String content) {
		String jsonString = content.replaceAll("/\\*.*\\*/", "");
		return jsonString.replaceAll("^jQuery\\d+_\\d+\\((.*)\\);$", "$1");
	}
}
