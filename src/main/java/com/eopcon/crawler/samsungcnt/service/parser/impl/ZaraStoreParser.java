package com.eopcon.crawler.samsungcnt.service.parser.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.aspect.annotation.Logging;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

/**
 * ZARA Site Paser
 */
public class ZaraStoreParser extends OnlineStoreParser {

	private final String PAGE_ENCODING = "utf-8";
	private final static String CONTENT_TYPE_JSON = "json";
	private final static String CONTENT_TYPE_HTTP = "http";
	
	private boolean isCategoryFilter = true;

	@Value("${zara.category.isBlackFilter:ture}")
	private boolean isBlackFilter = true;
	

	protected static Logger logger = LoggerFactory.getLogger(ZaraStoreParser.class);
	
	private List<String> categoryFilterList = null;
	private List<String> bestItemCategoryList = null;
	
	
	@Value("${zara.http.host:'www.zara.com'}")
	private String host = "www.zara.com";
	
	@Value("${zara.http.origin:'https://www.zara.com'}")
	private String origin = "https://www.zara.com";

	@Value("${zara.cart.url:'https://www.zara.com/kr/ko/shop/cart'}")
	private String cartUrl = "https://www.zara.com/kr/ko/shop/cart";
	

	
	@Autowired
	private HttpRequestService request;

	@Autowired
	protected Properties properties;

	@Autowired
	protected ExceptionBuilder exceptionBuilder;
	
	private ObjectMapper mapper = new ObjectMapper();
	
	
	public void setBlackFilter(boolean black) {
		isBlackFilter = black;
	}
	
	public void enableCategoryFilter(boolean enable) {
		this.isCategoryFilter = enable;
	}

	private List<String> getCategoryFilterList() {
		CSVReader reader = null;
		InputStream in = null;

		List<String> list = new ArrayList<String>();

		try {
			String filterResource = "";
			if(isBlackFilter) {
				filterResource = "/assets/black_category_list.csv";
			} else {
				filterResource = "/assets/white_category_list.csv";
			}
			in = getClass().getResourceAsStream(filterResource);
			reader = new CSVReader(new InputStreamReader(in, OnlineStoreConst.CONFIG_CSV_ENCODING), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.NULL_CHARACTER, 1);

			String[] s;
			while ((s = reader.readNext()) != null) {
				String category = s[0];
				list.add(category);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionBuilder.raiseException(e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
		}
		return list;
	}
	
	private List<String> getBestItemCategoryList() {
		List<String> list = new ArrayList<String>();
		String[] bestItemCategories = properties.getProperty("zara.category.bestitems").split(",");
		
		for(String bestItemCategory : bestItemCategories)
			list.add(bestItemCategory);
		
		return list;
	}
	
	
	/**
	 * 수집할 카테고리의 화이트 리스트 설정을 로딩
	 */
	@PostConstruct
	public void init() {
		categoryFilterList = getCategoryFilterList();
		bestItemCategoryList = getBestItemCategoryList();
	}
	
	
	public ZaraStoreParser(OnlineStoreConst constant) {
		super(constant);
	}

	/**
	 * 베스트아이템을 구한다
	 */
	public void getBestItmes(List<Category> categories, List<String> bestItems) throws Exception{
		bestItems.clear();


		for(Category category: categories) {
			if(isBestItemCategory(org.apache.commons.lang.StringUtils.join(category.getCategoryNames(), ";"))) {
				System.out.println(org.apache.commons.lang.StringUtils.join(category.getCategoryNames(), ";"));
				getBestItmes(category.getCategoryUrl(), bestItems);
			}
		}
		
	}
	
	/**
	 * 베스트아이템 URL을 호출하여 베스트 아이템을 파싱 후 베스트아이템 리스트에 추가한다  
	 */
	private void getBestItmes(String url, List<String> bestItems) throws Exception{

		request.openConnection(url);
		setHttpHeaders(CONTENT_TYPE_HTTP, "https://www.zara.com/kr/");
		
		Result result = request.executeWithGet(true);

		if (result.getResponseCode() == HttpStatus.SC_OK) {
			String content = result.getString();
			parseBestItems(content, bestItems);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
	}
	
	/**
	 * 베스트 아이템을 파싱
	 */
	private void parseBestItems(String content, List<String> bestItems) {

		Document doc = Jsoup.parse(filterBody(content));
		
		for (Element element : doc.select("section#products > ul > li")) {
			Element el = element.select("> div > div").get(1);
			String goodsName = text(el, false);
			String onlineGoodsNum = attr(element, "data-productid");

			if(!goodsName.trim().equals("")) {
				bestItems.add(onlineGoodsNum);
			}
		}
	}
	
	
	/**
	 * 카테고리 목록을 파싱.
	 */
	@Logging
	public List<Category> parseCategories(String content) throws Exception {
		
		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		//Elements elements = doc.select("nav#menu > ul > li");	// WOMAN, MAN, KIDS
		Elements elements = doc.select("nav#menu > ul > li > ul > li");	// SALE(WOMAN, MAN) , FW17 컬렉션(WOMAN, MAN)
		
		
		String categoryName1 = "";
		String categoryName2 = "";
		String categoryName3 = "";
		String categoryName4 = "";
		
		for(int i=0; i < elements.size(); i++) {			
			/*
			 * depth 1
			 */
			Element el1 = elements.get(i);	// nav#menu > ul > li (KIDS 위의 li)

			categoryName1 = text(el1.select("> a").first(), false);	// <a href="//www.zara.com/kr/ko/kids-c693284.html">KIDS</a>
			

			if(isCategoryFilter) {
				if(isBlackFilter) {
					if(isWhiteOrBlackCategory(categoryName1)) {	// 블랙트리스트 여부 체크
						continue;
					}	
				} else {
					if(!isWhiteOrBlackCategory(categoryName1)) {	// 화이트리스트 여부 체크
						continue;
					}				
				}
			}
			
			for(Element el2: el1.select("> ul > li")) {	// nav#menu > ul > li > ul > li (GIRL | 만4세 - 14세 위의 li)
				/*
				 * depth 2
				 */

				categoryName2 = text(el2.select("> a").first(), false);	// <a href="//www.zara.com/kr/ko/kids/girl-%7C-%EB%A7%8C4%EC%84%B8---14%EC%84%B8/new-in-c806503.html">GIRL | 만4세 - 14세</a>

				if(isCategoryFilter) {
					if(isBlackFilter) {
						if(isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2)) {	// 블랙트리스트 여부 체크
							continue;
						}	
					} else {
						if(!isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2)) {	// 화이트리스트 여부 체크
							continue;
						}				
					}
				}
			
				if(el2.select("> ul > li").isEmpty()) {
					Category category = new Category();
					category.addCategoryName(categoryName1);
					category.addCategoryName(categoryName2);
					//String categoryUrl = "http:" + attr(el2.select("> a").first(), "href");
					String categoryUrl = attr(el2.select("> a").first(), "href");
					category.setCategoryUrl(categoryUrl);
					categories.add(category);
				} else {
					for(Element el3: el2.select("> ul > li")) {	// nav#menu > ul > li > ul > li > ul > li (악세서리 위의 li)
						/*
						 * depth 3
						 */

						categoryName3 = text(el3.select("> a").first(), false);	// <a href="//www.zara.com/kr/ko/kids/girl-%7C-%EB%A7%8C4%EC%84%B8---14%EC%84%B8/%EC%95%85%EC%84%B8%EC%84%9C%EB%A6%AC/%EB%AA%A8%EB%91%90-%EB%B3%B4%EA%B8%B0-c719512.html">악세서리</a>

						if(isCategoryFilter) {
							if(isBlackFilter) {
								if(isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2 + ";" + categoryName3)) {	// 블랙트리스트 여부 체크
									continue;
								}	
							} else {
								if(!isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2 + ";" + categoryName3)) {	// 화이트리스트 여부 체크
									continue;
								}				
							}
						}
						
						if(el3.select("> ul > li").isEmpty()) {
							Category category = new Category();
							category.addCategoryName(categoryName1);
							category.addCategoryName(categoryName2);
							category.addCategoryName(categoryName3);

							//String categoryUrl = "http:" + attr(el3.select("> a").first(), "href");
							String categoryUrl = attr(el3.select("> a").first(), "href");
							category.setCategoryUrl(categoryUrl);
							categories.add(category);
							
						} else {							
							for(Element el4: el3.select("> ul > li")) {	// nav#menu > ul > li > ul > li > ul > li > ul > li (모자｜스카프 위의 li)
								/*
								 * depth 4
								 */

								categoryName4 = text(el4.select("> a").first(), false);	// <a href="//www.zara.com/kr/ko/kids/girl-%7C-%EB%A7%8C4%EC%84%B8---14%EC%84%B8/%EC%95%85%EC%84%B8%EC%84%9C%EB%A6%AC/%EB%AA%A8%EC%9E%90%EF%BD%9C%EC%8A%A4%EC%B9%B4%ED%94%84-c714513.html">모자｜스카프</a>

								if(isCategoryFilter) {
									if(isBlackFilter) {
										if(isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2 + ";" + categoryName3 + ";" + categoryName4)) {	// 블랙트리스트 여부 체크
											continue;
										}	
									} else {
										if(!isWhiteOrBlackCategory(categoryName1 + ";" + categoryName2 + ";" + categoryName3 + ";" + categoryName4)) {	// 화이트리스트 여부 체크
											continue;
										}				
									}
								}
								
								
								Category category = new Category();
								category.addCategoryName(categoryName1);
								category.addCategoryName(categoryName2);
								category.addCategoryName(categoryName3);
								category.addCategoryName(categoryName4);
								//String categoryUrl = "http:" + attr(el4.select("> a").first(), "href");
								String categoryUrl = attr(el4.select("> a").first(), "href");
								category.setCategoryUrl(categoryUrl);
								categories.add(category);
							} // end of for
						} // end of if
					} // end of for
				} // end of if
			} // end of for
		} // end of for
		
		return categories;
	}

	
	/**
	 * 카테코리가 화이트리스트에 속해있는지 확인 
	 */
	private boolean isWhiteOrBlackCategory(String category) {
		boolean rtn = false;
		for(String categoryFilter: categoryFilterList) {
			if(isBlackFilter) {
				if(category.replaceAll(" ", "").indexOf(categoryFilter.replaceAll(" ", "")) > -1) {
					rtn = true;
					break;
				}
			} else {
				if(categoryFilter.replaceAll(" ", "").indexOf(category.replaceAll(" ", "")) > -1) {
					rtn = true;
					break;
				}
			}
		}
		return rtn;
	}

	/**
	 * 베스트아이템인지 여부를 확인
	 */
	private boolean isBestItemCategory(String categoryName) {
		boolean rtn = false;
		
		for(String bestItemCategory: bestItemCategoryList) {
			if(bestItemCategory.equals(categoryName)) {
				rtn = true;
				break;
			}
		}
		
		return rtn;
	}

	
	/**
	 * 카테코리에 속한 상품목록을 파싱
	 */
	@SuppressWarnings("unchecked")
	@Logging
	public List<Product> parseProductList(String content, Category category) throws Exception {
		
		if(isBestItemCategory(org.apache.commons.lang.StringUtils.join(category.getCategoryNames(), ";"))) {	// 베스트아이템이 아닌 경우만 처리함
			return Collections.EMPTY_LIST;		
		}	
			
		List<Product> productList = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));

		for (Element element : doc.select("section#products > div > ul > li")) {
			String path = attr(element.select("> a").first(), "href");

			Element el = element.select("> div > div").get(1);
			String goodsName = text(el, false);
			//String productUrl = "http:" + path;
			String productUrl = path;
			String onlineGoodsNum = attr(element, "data-productid");

			if(!goodsName.trim().equals("")) {
				Product product = new Product(category, productUrl, onlineGoodsNum);
				productList.add(product);
			}
		}
		
		return productList;
	}
	

	/**
	 * 상품 상세페이지를 파싱
	 */
	@SuppressWarnings("unchecked")
	@Logging
	public void parseProductDetail(String content, ProductDetail productDetail, String productURL) throws Exception {
		Document doc = Jsoup.parse(content);
		Elements scripts = doc.select("script");

		Document docBody = Jsoup.parse(filterBody(content));
		Elements imageElements = docBody.select("div#main-images > div > a");	// SS17 컬렉션, SALE

		try {
			scriptManager.enter();


			String goodsNum = "";		// 상품번호
			String goodsName = "";		// 상품명
			//String brandCode = constant.ZARA.toString();
			String goodsImage = "http:" + attr(imageElements.first(), "href");		//상품이미지
			Integer price = null;			// 정가
			Integer discountPrice = null;	// 할인가
			String goodsMaterials; // 소재정보 원본
			
			
			
			if (scripts.size() > 0) {
				int i = 0;
				StringBuilder sb = new StringBuilder();

				scriptManager.addScript("anonymous#" + i++, "var window = {};");
				scriptManager.addScript("anonymous#" + i++, "window.zara = {};");
				
				for (Element el : scripts) {
					String script = text(el, true);

					if (script.indexOf("window.zara.dataLayer = ") > -1) {
						Pattern pattern = Pattern.compile("(window.zara.dataLayer\\s*=\\s*\\{.*\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						Matcher matcher = pattern.matcher(script);
						while (matcher.find()) {
							String temp = matcher.group(1);

							sb.append(temp);
							sb.append('\n');
						}
					}

				}

				
				scriptManager.addScript("anonymous#" + i++, sb.toString());
				
				scriptManager.addScript("anonymous#" + i++, "var jsonGoodsDetail = JSON.stringify(window.zara.dataLayer);");
				
				Map<String, Object> dataLayer = mapper.readValue((String)scriptManager.getObject("jsonGoodsDetail"), Map.class) ;
				
				Map<String, Object> product = (Map<String, Object>)dataLayer.get("product"); 
				Map<String, Object> detail = (Map<String, Object>)product.get("detail");
				List<Map<String, Object>> colors = (List<Map<String, Object>>)detail.get("colors");
				
				Map<String, Object> detailedComposition = (Map<String, Object>)detail.get("detailedComposition");
				
				List<Map<String, Object>> detailedCompositionParts = null; 
				if(detailedComposition != null)		
					detailedCompositionParts =		(List<Map<String, Object>>)detailedComposition.get("parts");

				String parentId = (String)dataLayer.get("parentId");
				
				String categoryId = "";
				if(!((Map<String, Object>)dataLayer.get("category")).isEmpty()) {
					categoryId = ((Integer)((Map<String, Object>)dataLayer.get("category")).get("id")).toString();
				}
				
				if(logger.isDebugEnabled()) {
					logger.debug("================= [{},{},{}]생삭, 사이즈, 제고여부]=====================", (String)product.get("name"), (String)detail.get("displayReference") , (Integer) product.get("price"));
				}
			
				if(colors.isEmpty())	// 색상이 없는경우 판매 종료로 판단함(상품목록 가져올때는 판매중이나 상품상세 가져올 때 상품이 판매 완료되는 경우가 있음)
					exceptionBuilder.raiseException(ErrorType.ERROR_END_OF_SALES);
				
				Map<String, Object> color = colors.get(0);

				if(logger.isDebugEnabled()) {
					logger.debug("--------------------------------------------------------------------");
					logger.debug("coler: " + color.get("name"));	// 색상
				}
			
				
				
				
				for(Map<String, Object> size: (List<Map<String, Object>>)color.get("sizes")) {
					int stockAmount = 0;

					if(size.get("price") != null) {  
						if(size.get("oldPrice") == null) {
							price = (Integer) size.get("price");
						} else {
							price = (Integer) size.get("oldPrice");
							discountPrice = (Integer) size.get("price");
						}
					}
					
					Stock stock = new Stock();
					stock.setColor((String)color.get("name"));
					stock.setSize((String)size.get("name"));
					
					if(logger.isDebugEnabled()) {
						logger.debug("	sku: " + size.get("sku"));
						logger.debug("	name: " + size.get("name"));	// XS (KR 44)
						logger.debug("	availability: " + size.get("availability"));	// 재고여부("in_stock"인 경우 재고있음이며 "in_stock"가 아닌 경우는 재고없음)
					}
					
					if(((String)size.get("availability")).equalsIgnoreCase("in_stock")) {
						stockAmount = getStockAmount(((Integer)size.get("sku")).toString(), parentId, categoryId, productURL);	// 재고수량
					}

					if(logger.isDebugEnabled()) {
						logger.debug("	quantity: " + stockAmount);
						logger.debug("");
					}

					
					stock.setStockAmount(stockAmount);
					
					productDetail.addStock(stock);
				}
				
				
				

				if(logger.isDebugEnabled()) {
					logger.debug("================= 소재 =====================");
				}
				
				goodsMaterials = detailedCompositionParts == null ? "" :detailedCompositionParts.toString();		// 소재정보 원본

				if(detailedCompositionParts != null) {
					for(Map<String, Object> part: detailedCompositionParts) {
						String goodsComposed = (String)part.get("description");
						
						if(logger.isDebugEnabled()) {
							logger.debug("--------------------------------------------------------------------");
						}
						
						List<Map<String, Object>> areas = (List<Map<String, Object>>)part.get("areas");
						
						if(areas.size() > 0) {
							for(Map<String, Object> area: areas) {
								for(Map<String, Object> component: (List<Map<String, Object>>)area.get("components")) {
									
									if(logger.isDebugEnabled()) {
										logger.debug("	goodsComposed: " + goodsComposed +"/"+(String)area.get("description"));	// 구성정보(겉감/주요소재)
										logger.debug("	material: " + component.get("material"));	// 폴리에스터
										logger.debug("	percentage: " + component.get("percentage"));	// 64%
									}
									
									Materials materials = new Materials();
									materials.setGoodsComposed(goodsComposed +"/"+(String)area.get("description"));	// 구성정보(겉감/주요소재)
									materials.setColor((String)color.get("name"));	// 색상
									materials.setMaterials((String)component.get("material"));	// 소재
									String ratio = (String)component.get("percentage");
									
									if(ratio != null) ratio = ratio.replaceAll("%", "");
									
									materials.setRatio(new Float(ratio));
									productDetail.addMaterials(materials);
								}
							}
						} else {
							for(Map<String, Object> component: (List<Map<String, Object>>)part.get("components")) {

								if(logger.isDebugEnabled()) {
									logger.debug("	goodsComposed: " + goodsComposed);	// 구성정보(안감)
									logger.debug("	material: " + component.get("material"));	// 폴리에스터
									logger.debug("	percentage: " + component.get("percentage"));	// 64%
								}
								
								Materials materials = new Materials();
								materials.setGoodsComposed(goodsComposed);	// 구성정보(안감)
								materials.setColor((String)color.get("name"));	// 색상
								materials.setMaterials((String)component.get("material"));	// 소재
								String ratio = (String)component.get("percentage");
								
								if(ratio != null) ratio = ratio.replaceAll("%", "");
								
								materials.setRatio(new Float(ratio));
								productDetail.addMaterials(materials);
								
							}
							
						}
					}
				}
				
				if(productDetail.getMaterials().size() == 0) {
					Materials materials = new Materials();
					materials.setGoodsComposed("전체");	// 구성정보(안감)
					materials.setColor((String)color.get("name"));	// 색상
					materials.setMaterials("기타");	// 소재
					materials.setRatio(new Float("100"));
					productDetail.addMaterials(materials);
				}
				
				
				goodsName = (String)product.get("name");
				goodsNum = (String)detail.get("displayReference");

				if(product.get("price") != null) {  
					if(product.get("oldPrice") == null) {
						price = (Integer) product.get("price");
					} else {
						price = (Integer) product.get("oldPrice");
						discountPrice = (Integer) product.get("price");
					}
				}
				
				
				productDetail.setGoodsMaterials(goodsMaterials);
				productDetail.setGoodsNum(goodsNum);
				productDetail.setGoodsName(goodsName);
				productDetail.setPrice(price);
				productDetail.setDiscountPrice(discountPrice);
				productDetail.setGoodsImage(goodsImage);
				productDetail.setBrandCode(OnlineStoreConst.BRAND_ZARA);
				
			}
		
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			scriptManager.exit();
		}
	}

	/**
	 * 재고 수량을 확인
	 */
	private int getStockAmount(String sku, String parentId, String categoryId, String productURL) throws Exception{
		int quantity = 0;

		try {
			String content = "";
	
			Result result = addCart(sku, parentId, categoryId, productURL);	// 장바구니 추가
	
			if (result.getResponseCode() == HttpStatus.SC_OK) {
	
				request.openConnection("https://www.zara.com/kr/ko/shop/cart");
				setHttpHeaders(CONTENT_TYPE_HTTP, productURL);
				
				
				result = request.executeWithGet(true);
	
				if (result.getResponseCode() == HttpStatus.SC_OK) {
					content = result.getString();

					if(logger.isDebugEnabled()) {
						//logger.debug("cart content: " + content);
					}
					
					Document doc = Jsoup.parse(content);
	
					Element e = doc.select("tr[id^='order-item-']").first();
					String productId = e.attr("id").replaceAll("^order-item-", "");
					
					try{
						quantity = getAvailableQuantity(productId, 1000);
					} finally {
						clearCart(productId);	// 장바구니 초기화
					}
				} else {
					exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
				}
	
			} else if(result.getResponseCode() == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
				quantity = 0;
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
			}
		} catch(Exception e) {
			e.printStackTrace();
			exceptionBuilder.raiseException(e);
		}
		return quantity;
	}
	
	/**
	 * 재고수량을 확인하기 위하여 장바구니에 상품을 추가
	 */
	private Result addCart(String sku, String parentId, String categoryId, String productURL) throws Exception {
		String body = "";
		if(!StringUtils.isEmpty(categoryId)) {
			body = "{\"products\":[{\"sku\":" + sku + ",\"parentId\":\"" + parentId + "\",\"quantity\":1,\"categoryId\":" + categoryId + "}]}";
		} else {
			body = "{\"products\":[{\"sku\":" + sku + ",\"parentId\":\"" + parentId + "\",\"quantity\":1}]}";
		}
		

		request.openConnection(cartUrl + "/add?ajax=true");
		setHttpHeaders(CONTENT_TYPE_JSON, productURL);
		
		Result result = request.executeWithPost(body,PAGE_ENCODING, true);
		return result;
		
	}
	
	/**
	 * 재고수량 확인 후 장바구니를 비움
	 */
	private void clearCart(String productId) throws Exception{
		String  body = "{\"products\":\"[{\\\"id\\\":" + productId + ",\\\"quantity\\\":0}]\"}";

		request.openConnection(cartUrl + "/update?ajax=true");
		setHttpHeaders(CONTENT_TYPE_JSON, "https://www.zara.com/kr/ko/shop/cart");
		
		Result result = request.executeWithPost(body,PAGE_ENCODING, true);

		if(logger.isDebugEnabled()) {
			logger.debug("clearCart result: " + result.getResponseCode());
		}
	}
	
	/**
	 * 재고수량을 파싱 
	 */
	@SuppressWarnings("unchecked")
	private int getAvailableQuantity(String productId, int quantity) throws Exception {
		Result result;
		String body = "";
		String content = "";
		
		int availableQuantity = 0;
		
		request.openConnection(cartUrl + "/update?ajax=true");
		setHttpHeaders(CONTENT_TYPE_JSON, "https://www.zara.com/kr/ko/shop/cart");

		body = "{\"products\":\"[{\\\"id\\\":" + productId + ",\\\"quantity\\\":" + quantity + "}]\"}";

		result = request.executeWithPost(body,PAGE_ENCODING, true);
		if (result.getResponseCode() == HttpStatus.SC_OK) {
			content = result.getString();

			if(logger.isDebugEnabled()) {
				logger.debug("isProductsOutOfStock content: " + content);
			}
			
			
			Map<String, Object> data = mapper.readValue(content, Map.class) ;
			
			Map<String, Object> shopCart = (Map<String, Object>) data.get("shopCart");
			
			List<Map<String, Object>> items = (List<Map<String, Object>>)shopCart.get("items");
			Map<String, Object> item = items.get(0);
			if(item.get("availableQuantity") != null)
				availableQuantity = (Integer)item.get("availableQuantity");
			
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
		
		return availableQuantity;
	}
	
	
	/**
	 * HTTP Header 를 설정
	 */
	private void setHttpHeaders(String contentType, String productURL) {
		request.addRequestHeader("Host", host);
		request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		switch(contentType) {
			case CONTENT_TYPE_HTTP:
				request.addRequestHeader("Upgrade-Insecure-Requests", "1");
				request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				if(org.apache.commons.lang.StringUtils.isNotEmpty(productURL))
					request.addRequestHeader("Referer", productURL);
				request.addRequestHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
				break;
			case CONTENT_TYPE_JSON:
				request.addRequestHeader("Origin", origin);
				request.addRequestHeader("Accept", "application/json");
				request.addRequestHeader("Content-Type", "application/json");
				request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
				
				// 6.22 추가
				request.addRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
				request.addRequestHeader("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.6,en;q=0.4");
				if(org.apache.commons.lang.StringUtils.isNotEmpty(productURL))
					request.addRequestHeader("Referer", productURL);
				break;
			}
		
	}


	/**
	 * 상품구성별 소재정보  파싱
	 * - ZARA의 경우 상품 상세페이지 파싱 시 소재정보를 많들기 때문에 null을 리턴
	 */
	@Override
	public List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception {
		return null;
	}
	
}
