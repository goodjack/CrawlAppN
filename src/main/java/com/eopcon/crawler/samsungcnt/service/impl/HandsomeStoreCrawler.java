package com.eopcon.crawler.samsungcnt.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeItemInfo;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeLayerInfo;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeTargetItem;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HandsomeStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.MixxoStoreParser;
import com.eopcon.crawler.samsungcnt.util.CollectDateFormat;
import com.eopcon.crawler.samsungcnt.util.HandsomeToFile;
import com.eopcon.crawler.samsungcnt.util.ItemCrawler;
import com.eopcon.crawler.samsungcnt.util.JsoupConnect;
import com.eopcon.crawler.samsungcnt.util.ObjectToJson;
import com.eopcon.crawler.samsungcnt.util.SeleniumConnect;

/**
 * 한섬 사이트 클롤러 
 */
public class HandsomeStoreCrawler extends OnlineStoreCrawler implements InitializingBean {

	private final String PAGE_ENCODING = "utf-8";
	
	public final static String SITE_NAME = "HANDSOME";
	public final static String BASE_URL = "http://www.thehandsome.com/ko";
	public final static String BASE_HOST = "www.thehandsome.com/ko";
	public final static String CATEGORY_PAGE_URL = "http://www.thehandsome.com/ko";	// http://www.thehandsome.com/ko
	public final static String CATEGORY_DETAIL_PAGE_URL = "http://mixxo.elandmall.com/dispctg/initDispCtg.action?disp_ctg_no=";
	public final static String PRODUCT_STOCK_INFO_URL = "http://mixxo.elandmall.com/goods/searchGoodsItemList.action";
	public final static String PRODUCT_COMMENT_INFO_URL = "http://mixxo.elandmall.com/goods/searchGoodsEvalBody.action";
	public final static String PRODUCT_BEST_ITEM_INFO_URL = "http://mixxo.elandmall.com/shop/initBestBrandMall.action";
	
	private HandsomeStoreParser parser;

	private ObjectToJson objectToJson;
	private List<DataStandard> categoryInfo_AllList;
	private ItemCrawler itemCrawler;
	private HandsomeLayerInfo handsomeLayerInfo;
	private List<DataStandard> productItem_AllList;
	
//	private List<Product> productList = new ArrayList<>();
	public HandsomeStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		
		super(config, constant);
		System.out.println("=================HandsomeStoreCrawler생성=====================");
		System.out.println("ServiceConfig = " + config.toString());
		System.out.println("ServiceConfig = " + constant.toString());
		objectToJson = new ObjectToJson();
		itemCrawler = new ItemCrawler();
		handsomeLayerInfo = new HandsomeLayerInfo();
		categoryInfo_AllList = new ArrayList<DataStandard>();
		productItem_AllList = new ArrayList<DataStandard>();
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.parser = (HandsomeStoreParser) super.parser;
	}

	/**
	 * 카테고리 정보를 가져온다.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Category> getCategories() throws Exception {

		List<Category> categories = new ArrayList<>();
		
		return getAllCategories_test(categories);
	}


	public void categoryCrawl() {
//		categoryInfo_AllList = new ArrayList<DataStandard>();
		categoryInfo_AllList = crawlingCategoryInfo(CATEGORY_PAGE_URL);
		CollectDateFormat collectDateFormat = new CollectDateFormat();
		long startTime = System.currentTimeMillis();
		
		int i = 1;
		for (DataStandard dataStandard : categoryInfo_AllList) {
			System.out.println(i + " category= " + dataStandard);
			i++;
			String categoryCode = handsomeLayerInfo.categoryCode(dataStandard.getCategoryUrl());
			dataStandard.setCategoryCode(categoryCode);
			productItem_AllList = itemCrawler.targetItemCrawl(dataStandard);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("##  시작시간 : " + collectDateFormat.formatTime(startTime));
	    System.out.println("##  종료시간 : " + collectDateFormat.formatTime(endTime));
	    System.out.println("##  전체 아이템 소요시간(초.0f) : " + ( endTime - startTime )/1000.0f +"초");
		
	}
	
	private List<DataStandard> crawlingCategoryInfo(String siteUrl) {
		SeleniumConnect seleniumConnect = new SeleniumConnect();
		String htmlContent = seleniumConnect.getPhantomJSConnect(siteUrl);
		Document document = Jsoup.parse(htmlContent);
		
//		JsoupConnect.setJsoupConnet(siteUrl);
//		Document document = JsoupConnect.getJsoupConnect();
		
		List<DataStandard> categoryInfo_List = new ArrayList<DataStandard>();
		try{
			for (int i=1; i<=2; i++) {
				for (Element level1 : document.select("#cate_m_main > li:nth-child(" + i + ")")) {
					
					
					String gender = level1.select(" > a").text().substring(0, 2);
										
					for(Element level2 : level1.select("div > div > ul > li")) {
						String level2Name = level2.select("> a").text();
						
						if(!("전체보기".equals(level2Name))) {	// 전체보기 제거
							System.out.println(" level2 : " + level2Name);
							
							
							for(Element level3 : level2.select("ul > li > a")) {
								String level3Name = level3.text();
								String categoryUrl = level3.attr("href");
								DataStandard dataStandard = new DataStandard();
								handsomeLayerInfo = new HandsomeLayerInfo();
								
								// layer 4, 5, 6
								dataStandard = handsomeLayerInfo.layer4Set(gender);								
								dataStandard = handsomeLayerInfo.layer5Set(level2Name.replaceAll("\\p{Z}", ""));	// 공백제거				
								dataStandard = handsomeLayerInfo.layer6Set(level2Name.replaceAll("\\p{Z}", ""), level3Name.replaceAll("\\p{Z}", ""));	// 공백제거
								
								dataStandard.setLayer1(dataStandard.getLayer4());
								dataStandard.setLayer2(dataStandard.getLayer5());
								dataStandard.setLayer3(dataStandard.getLayer6());
								dataStandard.setCategoryUrl(categoryUrl);
								
								categoryInfo_List.add(dataStandard);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			objectToJson.errorLog(e, siteUrl);
		}
		
		return categoryInfo_List;
	}
	/**
	 * 카테고리 하위 목록을 가져온다.
	 * 
	 * @param topCategories
	 * @param categories
	 * @return
	 * @throws Exception
	 */
	public List<Category> getAllCategories_test(List<Category> categories) throws Exception {

		try {
			categoryInfo_AllList = crawlingCategoryInfo(CATEGORY_PAGE_URL);
			/* 
			  level1 : 여성
	 		  level2 : 팬츠
			  level3 : 캐주얼 , /ko/c/me021/
			  level3 : 데님 , /ko/c/me022/
			  level3 : 쇼츠 , /ko/c/me023/
			  level3 : 에센셜 , /ko/c/me025/*/
			for (DataStandard dataStandard : categoryInfo_AllList) {
				Category cateInfo = new Category();
				
				if(dataStandard != null) {
					cateInfo.setDataStandard(dataStandard);
				}
				if( !dataStandard.getLayer4().isEmpty() && dataStandard.getLayer4() != null) {
					cateInfo.addCategoryName(dataStandard.getLayer4());
				}
				if( !dataStandard.getLayer5().isEmpty() && dataStandard.getLayer5() != null) {
					cateInfo.addCategoryName(dataStandard.getLayer5());
				}
				if( !dataStandard.getLayer6().isEmpty() && dataStandard.getLayer6() != null) {
					cateInfo.addCategoryName(dataStandard.getLayer6());
					cateInfo.setCategoryCode(dataStandard.getCategoryCode());
				}
				String categoryCode = handsomeLayerInfo.categoryCode(dataStandard.getCategoryUrl());
				dataStandard.setCategoryCode(categoryCode);
				cateInfo.setCategoryUrl(dataStandard.getCategoryUrl());
				cateInfo.setDataStandard(dataStandard);
				categories.add(cateInfo);
			}
		}catch (Exception e) {
			System.out.println(getClass().getName()+"------>" + e.getMessage());
			logger.debug(getClass().getName(),e.getMessage());
		}
		
		
		
		return categories;
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
		categoryInfo_AllList = crawlingCategoryInfo(CATEGORY_PAGE_URL);
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
				tmpCategories = parser.parseCategories(content);
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
		//category 
		//1.cate URL 
		//2.cate name
		HandsomeTargetItem targetItem = new HandsomeTargetItem();
		
		List<DataStandard> list= targetItem.crawlingTargetItem(category.getDataStandard(),page);
		List<Product> productList = new ArrayList<>(); 
		for(DataStandard data : list){
			String productUrl = data.getCrawlUrl();
			String onlineGoodsNum = data.getProductCode();
			Product p = new Product(category, productUrl, onlineGoodsNum);
			p.setBrandName(data.getBrandName());
			p.setDataStandard(data);
			productList.add(p);
		}
		return productList;
	}

	/**
	 * 상품상세 정보를 작성한다.
	 */
	@Override
	public void fillOutProductDetail(String productURL, ProductDetail productDetail) throws Exception {
		HandsomeItemInfo handsomeItemInfo = new HandsomeItemInfo();
		SeleniumConnect seleniumConnect = new SeleniumConnect();
//		Document document = null;
//		synchronized (lock.getLockObject(productDetail.getDataStandard().getProductCode())) {
//			
//			JsoupConnect.setJsoupConnet(productDetail.getDataStandard().getCrawlUrl());
//			document = JsoupConnect.getJsoupConnect();
//		}

		// 셀레니움 크롬브라우저
		String htmlContent = seleniumConnect.getPhantomJSConnect(productDetail.getDataStandard().getCrawlUrl());
		Document document = Jsoup.parse(htmlContent);
		
		JSONArray goodEvalJSonArr = new JSONArray();
		Elements dd = document.select("#contentDiv > div.info > dl > dd:nth-child(2)");
		Elements reviewCnt = document.select("#customerReviewCnt");	// goodEval
		String ddText = dd.toString();
		String ddContent = ddText.replaceAll("</strong>", "").replaceAll("<br>", "</strong>");	// material, origin
		
		Document ddDocument = Jsoup.parse(ddContent);
		
		// goodEval
		goodEvalJSonArr = handsomeItemInfo.itemGoodEvalInfo(productDetail.getDataStandard().getProductCode(), reviewCnt); 
		// material
		String material = handsomeItemInfo.itemMaterialInfo(ddDocument);
		// origin
		String origin = handsomeItemInfo.itemOriginInfo(ddDocument);
		// desc
		String desc = handsomeItemInfo.itemDescInfo(document);
		productDetail.getDataStandard().setMaterial(material);
		productDetail.getDataStandard().setOrigin(origin);
		productDetail.getDataStandard().setDesc(desc);
		productDetail.getDataStandard().setGoodEval(goodEvalJSonArr);
		productDetail.setGoodsNum(productDetail.getDataStandard().getProductCode());
		productDetail.setGoodsName(productDetail.getDataStandard().getProductName());
//		productDetail.setBrandCode();
		productDetail.setCollectURL(productDetail.getDataStandard().getCrawlUrl());
		productDetail.setGoodsImage(productDetail.getDataStandard().getImageUrl().get(0));
		productDetail.setPrice(productDetail.getDataStandard().getNormalPrice());
		productDetail.setDiscountPrice(productDetail.getDataStandard().getProductSalePrice());
		productDetail.setMaftOrigin(productDetail.getDataStandard().getOrigin());
		productDetail.setSite(SITE_NAME);
		productDetail.setGoodsMaterials(productDetail.getDataStandard().getMaterial());		
		String bestItem = productDetail.getDataStandard().getBestYn();
		boolean bestYn = ("Y".equals(bestItem) ? true : false);
		productDetail.setBestItem(bestYn);
		
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
		/*List<String> bestItems = new ArrayList<>();

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
		}*/
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
