package com.eopcon.crawler.samsungcnt.service.parser.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.HnmCategory;
import com.eopcon.crawler.samsungcnt.model.HnmStock;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.aspect.annotation.Logging;
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * H&M Site Parser
 */
public class HnmStoreParser extends OnlineStoreParser implements InitializingBean {

	public final static String KEY_EXTRA_PRICE_INFO = "KEY_EXTRA_PRICE_INFO";
	public final static String KEY_EXTRA_IMAGE_INFO = "KEY_EXTRA_IMAGE_INFO";
	public final static String KEY_EXTRA_COMPOSITION_INFO = "KEY_EXTRA_COMPOSITION_INFO";

	private final static String[] TOP_CATEGORIES = new String[] { "여성", "남성" };
	private final static String FILTER_CATEGORIES = "^(?:신상품|기획상품|셀렉션|캠페인|컨셉별 구매|모두 보기)$";	// 필터링할 메뉴
	private final static String END_NODE_CATEGORIES= ".*(/ko_kr/ladies/shop-by-product/shorts.html|/ko_kr/men/shop-by-product/shorts.html|/ko_kr/men/shop-by-product/underwear.html)$"; 
	

	private ObjectMapper mapper = new ObjectMapper();

	public HnmStoreParser(OnlineStoreConst constant) {
		super(constant);
	}

	/**
	 * 웹사이트 상단의 카테고리 목록 파싱
	 */
	@Logging
	public List<HnmCategory> parseTopCategories(String content) throws Exception {
		List<HnmCategory> topCategories = new ArrayList<>();
		String categoryName1 = "";
		String categoryName2 = "";
		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("nav.primary-menu > ul > li");
		
		for(Element el : elements) {
			categoryName1 = text(el.select("> a").first(), false);
			
			boolean included = false; 
			for (String top : TOP_CATEGORIES) {
				if(top.equals(categoryName1)) {
					included = true;
				}
			}
			
			if(!included) 
				continue;

			for(Element e1: el.select("> div > div > div > div")) {
				String categoryHeader = text(e1.select("> h4").first(), false);
				if(categoryHeader.matches(FILTER_CATEGORIES))
					continue;
				
				for(Element e2: e1.select("> ul > li > a")) {
					categoryName2 = text(e2, false);
					if(categoryName2.matches(FILTER_CATEGORIES))
						continue;
					
					String categoryUrl = attr(e2, "href");
					
					HnmCategory category = new HnmCategory();
					category.addCategoryName(categoryName1);
					category.addCategoryName(categoryName2);
					category.setCategoryUrl("http://www2.hm.com" + categoryUrl);
					category.setEndOfNode(false);
					
					topCategories.add(category);
				}
			}
		}
		return topCategories;
	}

	
	
	/**
	 * 웹사이트 왼쪽의 카테고리 목록 파싱 
	 */
	@Logging
	public List<HnmCategory>  parseSubCategories(String content, HnmCategory category, int depth) throws Exception {
		List<HnmCategory> subCategories = new ArrayList<>();
		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = null;
		
		switch(depth) {
			case 2:
				elements = doc.select("aside > div > nav > ul > li > div > div > div > div > ul > li.section-menu-subdepartment.current > ul > li");
				break;
			case 3:
				elements = doc.select("aside > div > nav > ul > li > div > div > div > div > ul > li.section-menu-subdepartment.current > ul > li.section-menu-subcategory.current > ul > li");
				break;
			case 4:
				elements = doc.select("aside > div > nav > ul > li > div > div > div > div > ul > li.section-menu-subdepartment.current > ul > li.section-menu-subcategory.current > ul > li.section-menu-subdepartment.current > ul > li");
				break;
		}
		
		if(elements.size() == 0 || category.getCategoryUrl().matches(END_NODE_CATEGORIES)) {	// 마지막 카테고리인 경우
			
			category.setEndOfNode(true);
			int dataTotalCount = Integer.parseInt(StringUtils.defaultIfEmpty(attr(doc.select("a.hidden.listing-total-count").first(), "data-total-count").trim(), "1"));
			
			if(dataTotalCount > 30) {	// 총 상품의 수가 30개 이상인 경우만 전체 상품 목록을 조회하는 url을 만든다.
				String dataLoadMoreUrl = attr(doc.select("a.load-more-link.button.infinite-scroll").first(), "data-load-more-url");
				if(!dataLoadMoreUrl.equals("")) {
					String dataCategoryFilter = attr(doc.select("ul.category-inputlist.category-inputlist-checkboxes.menu").first(), "data-category-filter");
					dataLoadMoreUrl = "http://www2.hm.com" + dataLoadMoreUrl + "?product-type=" +URLEncoder.encode(dataCategoryFilter, "UTF-8") + "&sort=stock&offset=0&page-size=5000";
					category.setCategoryUrl(dataLoadMoreUrl);
				}
			}
		} else {	// 하위 카테고리가 존재하는 경우
			for(Element el : elements) {									
				HnmCategory tmpCategory = new HnmCategory();
				tmpCategory.setCategoryNames(new ArrayList<>(category.getCategoryNames()));
				tmpCategory.addCategoryName(text(el.select("> a").first(), false));
				tmpCategory.setCategoryUrl("http://www2.hm.com" + attr(el.select("> a").first(), "href"));
				tmpCategory.setEndOfNode(false);
				subCategories.add(tmpCategory);
			}
		}
		
		return subCategories;
	}
	
	/**
	 * 카테고리에 속한 상품 목록을 파싱 
	 */
	
	@Logging
	public List<Product> parseProductList(String content, Category category) throws Exception {
		List<Product> productList = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));

		for (Element el : doc.select(" div.product-items-wrapper > section > div > div > div > article")) {
			String path = attr(el.select("> a").first(), "href");
			String onlineGoodsNum = attr(el, "data-articlecode");
			String goodsName = text(el.select("> div > h3 > a").first(), false);
			String productUrl = "http://www2.hm.com" + path;
			if(!goodsName.trim().equals("")) {
				Product product = new Product(category, productUrl, onlineGoodsNum);
				productList.add(product);
			}
		}

		return productList;
	}

	
	/**
	 * 베스트아이템 상품 목록을 파싱
	 */
	@Logging
	public List<String> parseBestItems(String content) throws Exception {
		List<String> bestItems = new ArrayList<>();


		Document doc = Jsoup.parse(filterBody(content));

		for (Element el : doc.select(" div.product-items-wrapper > section > div > div > div > article")) {
			String onlineGoodsNum = attr(el, "data-articlecode");
			String goodsName = text(el.select("> div > h3 > a").first(), false);
			if(!goodsName.trim().equals("")) {
				bestItems.add(onlineGoodsNum);
			}
		}
		
		return bestItems;
	}
	
	
	/**
	 * 상품 상세 페이지를 파싱
	 */
	@SuppressWarnings("unchecked")
	@Logging
	public void parseProductDetail(String content, ProductDetail productDetail) throws Exception {
		Document doc = Jsoup.parse(content);
		Elements scripts = doc.select("script");

		try {
			scriptManager.enter();
			
			boolean productArticleDetailsExist = false;
			
			if (scripts.size() > 0) {


				int i = 0;
				StringBuilder sb = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				String goodsNum = "";
				String utagData = "";
				
				for (Element el : scripts) {
					String script = text(el, true);

					if (script.indexOf("var productArticleDetails = ") > -1) {	// 제품의 색상별 image url, price, discount price를 구하기 위한 문자열 추출
						Pattern pattern = Pattern.compile("(var productArticleDetails\\s*=\\s*\\{.*\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						Matcher matcher = pattern.matcher(script);
						while (matcher.find()) {
							String temp = matcher.group(1);
							sb.append("var isDesktop = true;");
							sb.append('\n');
							sb.append(temp);
							sb.append('\n');
							productArticleDetailsExist = true;
						}
					}

					if (script.indexOf("utag_data = {") > -1) {	// 제품의 품번을 구하기 위한 문자열 추출 
						Pattern pattern = Pattern.compile("(utag_data\\s*=\\s*\\{.*\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						Matcher matcher = pattern.matcher(script);
						while (matcher.find()) {
							sb2.append(matcher.group(1));
							sb2.append('\n');
						}
					}
				}
/*
				if(!productArticleDetailsExist) {
					exceptionBuilder.raiseException(ErrorType.ERROR_NOT_APPLICABLE_GOODS, "productArticleDetails(json) is not defined");
				}
*/				
				utagData = sb2.toString();
				
				scriptManager.addScript("anonymous#" + i++, sb.toString());
				
				scriptManager.addScript("anonymous#" + i++, "var jsonGoodsDetail = JSON.stringify(productArticleDetails);");
				
				Map<String, Object> productArticleDetails = mapper.readValue((String)scriptManager.getObject("jsonGoodsDetail"), Map.class) ;
				
				Element productElement = doc.select("div.product.parbase > div.row.product-detail").first();
				
				Element imageElement = doc.select("div.product-detail-main-image-container > img").first();
				
				Pattern p = Pattern.compile("product_id : \\[\"(.*?)\"\\]");
				Matcher m = p.matcher(utagData);
				
				while(m.find()) {
					goodsNum = m.group(1);
				}
				
				String goodsName = text(productElement.select("> section > div > h1"), false);
				String goodsImage = "http:" + attr(imageElement, "src");
				
				//Integer price = Integer.parseInt(text(productElement.select("> section > div > div").first().select("> div > div > span"), false).replaceAll("\\D", ""));
				Integer price = Integer.parseInt(text(productElement.select("> section > div > div").first().select("> div > span"), false).replaceAll("\\D", ""));
				
				String maftOrigin = text(doc.select("li.product-detail-article-production-country"), false);
				if(!maftOrigin.equals("")) {
					maftOrigin = maftOrigin.replaceAll(",$", "");
				}
				
				Map<String, String> images = new HashMap<>();
				Map<String, String> discountPrices = new HashMap<>();
				Map<String, String> compositions = new HashMap<>();
				
				
				Elements sizeElements = doc.select("div.product-sizes > ul");
				
				Elements colorElements = doc.select("div.product-colors.clearfix > ul > li > a");
				for(Element colorElement : colorElements) {
					String dataArticlecode = attr(colorElement, "data-articlecode");
					
					Map<String, Object> skuMap = (Map<String, Object>)productArticleDetails.get(dataArticlecode);
					List<Map<String, Object>> imageList = (List<Map<String, Object>>)skuMap.get("images");
					Map<String, Object> imageMap = imageList.get(0);
					
					String colorName = (String)skuMap.get("colorCode") + " " + (String)skuMap.get("name");
					String imageUrl = "http:" + (String)imageMap.get("image");
					String tmpImageUrl1 = imageUrl.substring(0, imageUrl.indexOf("?"));
					String tmpImageUrl2 = URLEncoder.encode(imageUrl.substring(imageUrl.indexOf("?"), imageUrl.length()), "UTF-8");
					imageUrl = tmpImageUrl1 + tmpImageUrl2;
					
					String priceSaleValue = (String)skuMap.get("priceSaleValue");
					List<String> compositionList = (List<String>)skuMap.get("composition");
					
					images.put(colorName, imageUrl);
					discountPrices.put(colorName, priceSaleValue);
					compositions.put(colorName, StringUtils.join(compositionList, "|"));
					
					
					for(Element sizeElement : sizeElements) {
						if(dataArticlecode.equals(attr(sizeElement, "data-sizelist"))) {
							for(Element el1 : sizeElement.select("> li > label > input")) {
								
								HnmStock stock = new HnmStock();
								stock.setColor(colorName);
								stock.setSize(attr(el1, "value"));
								stock.setStockAmount(0);
								stock.setDataCode(attr(el1, "data-code"));
								
								productDetail.addStock(stock);
							}
							break;
						}
						
					}
				}

				productDetail.putExtraString(KEY_EXTRA_IMAGE_INFO, mapper.writeValueAsString(images));
				productDetail.putExtraString(KEY_EXTRA_PRICE_INFO, mapper.writeValueAsString(discountPrices));
				productDetail.putExtraString(KEY_EXTRA_COMPOSITION_INFO, mapper.writeValueAsString(compositions));
				productDetail.setGoodsNum(goodsNum);
				productDetail.setGoodsName(goodsName);
				productDetail.setBrandCode(OnlineStoreConst.BRAND_HNM);
				productDetail.setGoodsImage(goodsImage);
				productDetail.setPrice(price);
				productDetail.setMaftOrigin(maftOrigin);
				productDetail.setGoodsMaterials(compositions.toString());
			}
		} finally {
			scriptManager.exit();
		}
	}


	/**
	 * 생상별 소재정보 파싱
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Logging(ErrorType.ERROR_PARSING_MATERIALS_FAIL)
	public List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception {

		List<Materials> materials = new ArrayList<>();
		Map<String, String> compositionMap = mapper.readValue(productDetail.getExtraString(HnmStoreParser.KEY_EXTRA_COMPOSITION_INFO), Map.class);
		
		for(String color : colors) {
			if(!compositionMap.containsKey(color) || StringUtils.isEmpty(compositionMap.get(color))) {
				Materials m = new Materials();
				m.setColor(color);
				m.setGoodsComposed("전체");
				m.setMaterials("기타");
				m.setRatio(100f);
				materials.add(m);
			} else {
				List<Materials> list = parseMarterialsString(compositionMap.get(color));
				for (Materials ma : list) {
					Materials m = ma.copy();
					m.setColor(color);

					materials.add(m);
				}
			}
		}
		
		return materials;
	}



	/**
	 * 상품구성별 소재정보  파싱
	 */
	public List<Materials> parseMarterialsString(String content) {
		List<Materials> list = new ArrayList<>();

		String[] rows = content.split("[|]");

		for(String row:  rows) {
			if(row.replaceAll("\\D", "").equals("")) 
				continue;
			
			if(row.lastIndexOf(":") > -1) {
				String[] colsArr = row.split(":");
				String goodsComposed = colsArr[0];
				String strMaterials = colsArr[1];
				String[] tmpMaterialsArr = strMaterials.split(";");
				for(String tmpMaterial : tmpMaterialsArr) {
					String material = tmpMaterial.replaceAll("(\\d+)%$", "").trim();
					String ratio = tmpMaterial.replaceAll(".*\\s+(\\d+)%$", "$1").trim();
					Materials materials = new Materials();
					materials.setGoodsComposed(goodsComposed);
					materials.setMaterials(material);
					materials.setRatio(new Float(ratio));
					list.add(materials);
				}
			} else {
				String goodsComposed = "전체";
				String strMaterials = row;
				String[] tmpMaterialsArr = strMaterials.split(";");
				for(String tmpMaterial : tmpMaterialsArr) {
					String material = tmpMaterial.replaceAll("(\\d+)%$", "").trim();
					String ratio = tmpMaterial.replaceAll(".*\\s+(\\d+)%$", "$1").trim();
					Materials materials = new Materials();
					materials.setGoodsComposed(goodsComposed);
					materials.setMaterials(material);
					materials.setRatio(new Float(ratio));
					list.add(materials);
				}
				
			}
		}
		return list;
	}

	
}
