package com.eopcon.crawler.samsungcnt.service.parser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.NativeObject;
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
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 미쏘 사이트 파서
 */
public class HandsomeStoreParser extends OnlineStoreParser implements InitializingBean {

	public HandsomeStoreParser(OnlineStoreConst constant) {
		super(constant);
		// TODO Auto-generated constructor stub
	}

	private final static String[] TOP_CATEGORIES = new String[] { "CLOTHING", "ACCESSORY", "MIXXO SECRET" };
	private final static String[] TOP_CATEGORIES_NEW = new String[] { "아우터", "원피스", "니트/가디건","블라우스/셔츠","팬츠","스커트","티셔츠","★해나's Pick!","★패딩 점퍼","★체크 아이템","★무스탕 자켓","백","슈즈","스카프/머플러","기타 상품","★겨울 아이템","브라","팬티","라운지 웨어","비치웨어","짐웨어","기타 상품"};
	
	private final static String PATTERN_FIND_COLOR = "(LIGHT YELLOW|LIGHT VIOLET|LIGHT PURPLE|LIGHT PINK|LIGHT KHAKI|LIGHT INDIGO|LIGHT GREY" 
			+ "|LIGHT GREEN|LIGHT BLUE|LIGHT BEIGE|DARK RED|DARK PINK|DARK NAVY|DARK INDIGO|DARK GREY|DARK BROWN|DARK BLUE|DARK BEIGE" 
			+ "|OATMEAL MELANGE|NEON PINK|NEON ORANGE|MUSTARD YELLOW|MINT GREEN|MELANGE GREY|MELANGE BLUE|TWO TONE|DST BLACK|AQUA BLUE" 
			+ "|YELLOW|WINE|WHITE|VIOLET|SILVER|RED|R/BLUE|PURPLE|PINK|PEWTER|ORANGE|OCHER|NAVY|MIX|MINT|M/INDIGO|M/GREY|M/BLUE|LAVENDER" 
			+ "|L/YELLOW|L/PINK|L/INDIGO|L/GREY|L/BLUE|L/BEIGE|KHAKI|IVORY|INDIGO|HUNTER|GREY|GREEN|GOLD|D/RED|D/PINK|D/KHAKI|D/GREY" 
			+ "|D/BROWN|D/BLUE|CREAM|CORAL|CHARCOAL|CAMEL|BURGUNDY|BURGANDY|BROWN|BRONZE|BLUE|BLACK|BEIGE|통합칼라)";

	public final static int FLAG_PARSING_COLOR = 0;
	public final static int FLAG_PARSING_SIZE = 1;

	private ObjectMapper mapper = new ObjectMapper();

	private Comparator<Materials> materialsComparator = new Comparator<Materials>() {
		@Override
		public int compare(Materials o1, Materials o2) {
			int compare = StringUtils.defaultString(o1.getColor()).compareTo(o2.getColor());
			if (compare == 0)
				compare = StringUtils.defaultString(o1.getGoodsComposed()).compareTo(o2.getGoodsComposed());
			if (compare == 0)
				compare = StringUtils.defaultString(o1.getMaterials()).compareTo(o2.getMaterials());
			return compare;
		}
	};


	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}
	/**
	 * 카테고리 수집을 위한 HTML을 파싱처리한다.
	 * ※ 탑 카테고리
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Integer> parseTopCategories_new(String content) throws Exception {

		List<Integer> topCategories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("ul > li > dl > dd");
		logger.debug("elements_ ul > li > dl > dd" + elements);
		for (int i = 0; i < elements.size(); i++) {
			Element topDepth = elements.get(i);

			for (String top : TOP_CATEGORIES_NEW) {
				if (top.equals(text(topDepth, false))) {
					String topDepthCode = attr(topDepth.select("> a"), "onclick").replaceAll("\\D", "");
					topCategories.add(Integer.parseInt(topDepthCode));
				}
			}
		}
		return topCategories;
	}
	/**
	 * 카테고리 수집을 위한 HTML을 파싱처리한다. ※ 탑 카테고리
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Integer> parseTopCategories(String content) throws Exception {

		List<Integer> topCategories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("ul > li > dl > dt");

		for (int i = 0; i < elements.size(); i++) {
			Element topDepth = elements.get(i);

			for (String top : TOP_CATEGORIES) {
				if (top.equals(text(topDepth, false))) {
					String topDepthCode = attr(topDepth.select("> a"), "onclick").replaceAll("\\D", "");
					topCategories.add(Integer.parseInt(topDepthCode));
				}
			}
		}
		return topCategories;
	}

	/**
	 * 카테고리 수집을 위한 HTML을 파싱처리한다.
	 * ※ 서브 카테고리
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Category> parseCategories_new(String content) throws Exception {
		logger.debug("========================parseCategories_star====================");
		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		doc.select("em").remove();
		Elements elements = doc.select(".cate_area02 > .lnb_cate02 > ul > li > div.depth2 > ul > li");
		
		logger.debug("elements : " + elements);
		
		String topDepthName = doc.select("div.location > ul > li > select#nav1 > option[selected]").text();
		String secondDepthName = doc.select("div.location > ul > li > select#nav2 > option[selected]").text();
		
		if(elements.size() == 0 ){
			//카테고리 스텝이 2단계인 경우
			Elements elementsDepth2 = doc.select(".cate_area02 > .lnb_cate02 > ul > li > div.depth2 > ul > li");
			String code = attr(elementsDepth2.select("> a"), "href").replaceAll("\\D", "");
			String url = "http://spao.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + code;

			Category category = new Category();
			category.addCategoryName(topDepthName);
			category.addCategoryName(secondDepthName);
			category.setCategoryUrl(url);

			categories.add(category);
		}
		else {
			for (int i = 0; i < elements.size(); i++) {
				
				//   카테고리 스텝이 3단계인 경우
				String name = text(elements.get(i).select("> a > span"), false);
				String code = attr(elements.get(i).select("> a"), "href").replaceAll("\\D", "");
				String url = "http://spao.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + code;
				logger.debug("topDepthName : " + topDepthName);
				logger.debug("secondDepthName : " + secondDepthName);
				logger.debug("name : " + name);
				logger.debug("code : " + code);
				logger.debug("url : " + url);
				Category category = new Category();
				category.addCategoryName(topDepthName);
				category.addCategoryName(secondDepthName);
				category.addCategoryName(name.trim());
				category.setCategoryUrl(url);

				categories.add(category);
				
			}

		}
				logger.debug("========================parseCategories_stop====================");
		return categories;
	}
	
	
	/**
	 * 카테고리 수집을 위한 HTML을 파싱처리한다. ※ 서브 카테고리
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Category> parseCategories(String content) throws Exception {

		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("div.lnb_cate01 > ul > li");

		String topDepthName = text(doc.select("h2.tit_h2"), false);

		for (int i = 0; i < elements.size(); i++) {
			String secondDepthName = text(elements.get(i).select("> a > span"), true);
			String secondDepthCode = attr(elements.get(i).select("> a"), "onclick").replaceAll("\\D", "");

			Elements el = elements.get(i).select("> div.depth2 > ul > li");

			// 카테고리 스텝이 2단계인 경우
			if (el.size() == 0) {
				String url = "http://mixxo.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + secondDepthCode;

				Category category = new Category();
				category.addCategoryName(topDepthName);
				category.addCategoryName(secondDepthName);
				category.setCategoryUrl(url);

				categories.add(category);
			}

			// 카테고리 스텝이 3단계인 경우
			for (int j = 0; j < el.size(); j++) {
				String name = text(el.get(j).select("> a > span"), false);
				String code = attr(el.get(j).select("> a"), "onclick").replaceAll("\\D", "");
				String url = "http://mixxo.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + code;

				Category category = new Category();
				category.addCategoryName(topDepthName);
				category.addCategoryName(secondDepthName);
				category.addCategoryName(name);
				category.setCategoryUrl(url);

				categories.add(category);
			}
		}

		return categories;
	}

	/**
	 * 상품목록 수집을 위한 HTML을 파싱 처리한다.
	 * 
	 * @param content
	 * @param category
	 * @param currentPage
	 * @return
	 * @throws Exception
	 */
	@Logging
	public Map<String, Object> parseProductList(String content, Category category, int currentPage) throws Exception {
		int lastPage = 1;
		Map<String, Object> productListmap = new HashMap<>();
		List<Product> productList = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements goodsList = doc.select("#goodsList > ul.list > li");
		Elements lastPageInfo = doc.select("#page_idx > span > a");

		// 페이지 정보
		if (lastPageInfo.size() > 1)
			lastPage = Integer.parseInt(attr(lastPageInfo.last(), "value"));

		// 상품리스트 추가
		for (Element element : goodsList) {
			String goodsNum = attr(element.select("a"), "onclick");
			goodsNum = goodsNum.replaceAll("elandmall.goods.goDetail\\(\\{goods_no:'(\\d+)'.+$", "$1");

			String productUrl = "http://mixxo.elandmall.com/goods/initGoodsDetail.action?goods_no=" + goodsNum;

			Product product = new Product(category, productUrl, goodsNum);
			product.setBrandName(OnlineStoreConst.BRAND_MIXXO);

			productList.add(product);
		}

		productListmap.put("productList", productList);

		// 현재 페이지와 마지막 페이지 비교
		if (lastPage == currentPage)
			productListmap.put("lastPage", true);
		else
			productListmap.put("lastPage", false);

		return productListmap;
	}

	/**
	 * 상품상세 수집을 위한 HTML을 파싱 처리한다.
	 * 
	 * @param content
	 * @param productDetail
	 * @param productUrl
	 * @return
	 * @throws Exception
	 */
	@Logging
	public Map<String, String> parseProductDetail(String content, ProductDetail productDetail, String productUrl) throws Exception {

		Map<String, String> param = new HashMap<>();

		try {
			scriptManager.enter();

			Document doc = Jsoup.parse(content);

			Elements scripts = doc.select("script");

			Elements basicInfo = doc.select("div.goods_title");
			Elements detailTitle = doc.select("#data_table01 > table > tbody > tr > th");

			String goodsNum = text(basicInfo.select("> div.goods_code"), false).replaceAll("\\D", "");

			String goodsName = text(basicInfo.select("> strong.goods_name"), false);
			String goodsImage = "http://" + attr(doc.select("#d_elevate_img"), "src").replaceAll("//", " ").trim();
			String maftOrigin = "";
			Integer price = 0, discountPrice = null;
			String goodsMaterials = "";

			// 제조원산지 & 소재
			for (int i = 0; i < detailTitle.size(); i++) {
				if (maftOrigin.length() == 0 && detailTitle.get(i).text().contains("제조국")) {
					maftOrigin = detailTitle.get(i).nextElementSibling().text();

					// 제조국에 세탁정보가 들어가 있는 경우 따로 처리
					if (maftOrigin.length() > 1 && maftOrigin.contains("-"))
						maftOrigin = maftOrigin.substring(0, 2);
				} else if (goodsMaterials.length() == 0 && detailTitle.get(i).text().contains("소재"))
					goodsMaterials = detailTitle.get(i).nextElementSibling().text();

				if (maftOrigin.length() > 0 && goodsMaterials.length() > 0)
					break;
			}

			if (StringUtils.isBlank(maftOrigin) && StringUtils.isBlank(goodsMaterials))
				exceptionBuilder.raiseException(ErrorType.ERROR_NOT_APPLICABLE_GOODS, "1+1 패키지");

			// 가격 정보 - 판매가,세일가,쿠폰가,최대혜택가 로 표시됨
			for (Element el : doc.select("div.goods_txt_info > div.goods_detail_txt > ul.sale_price > li")) {
				Element element = el.select("div.same_th").first();
				String title = null;

				if (element.select("> a.ico_tooltip").size() > 0) {
					if (element.select("> span.txt_c05").size() > 0)
						title = text(element.select("> span.txt_c05"), false);
					else
						title = textNode(element, 0);
				} else {
					title = text(element, false);
				}

				Element e = element.nextElementSibling();
				int minimun = 0;

				if (!title.matches("^(?:판매가|세일가|쿠폰가|최대혜택가)$"))
					exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL, new IllegalArgumentException("title -> " + title));

				if (title.matches("^(?:판매가|세일가)$")) {
					if (e != null && e.is("span.pr_m_line")) {
						Element sibling = e.nextElementSibling();
						price = Integer.parseInt(text(e, false).replaceAll("\\D", ""));
						if (sibling != null && sibling.is("span.tx_sale_price"))
							minimun = Integer.parseInt(text(sibling, false).replaceAll("\\D", ""));
						else
							minimun = Integer.parseInt(textNode(el, 2).replaceAll("\\D", ""));
					} else {
						if (e != null && e.is("span.tx_sale_price"))
							price = Integer.parseInt(text(e, false).replaceAll("\\D", ""));
						else
							price = Integer.parseInt(textNode(el, 1).replaceAll("\\D", ""));
					}
					logger.debug("# {} -> {}", title, price);
				} else if (title.matches("^(?:쿠폰가)$")) {
					minimun = Integer.parseInt(text(e, false).replaceAll("\\D", ""));
				} else if (title.matches("^(?:최대혜택가)$")) {
					minimun = Integer.parseInt(text(e, false).replaceAll("\\D", ""));
				}

				if (minimun > 0) {
					logger.debug("# {} -> {}", title, minimun);

					if (discountPrice == null || discountPrice > minimun)
						discountPrice = minimun;
				}
			}

			String p = null;

			for (Element el : scripts) {
				String script = text(el, true);

				if (script.indexOf("elandmall.goodsDetail.initOpt({") > -1) {
					Pattern pattern = Pattern.compile("elandmall\\.goodsDetail\\.initOpt\\((\\{[^\\}]+\\})\\);", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(script);
					if (matcher.find()) {
						p = matcher.group(1);
						break;
					}
				}
			}

			if (StringUtils.isEmpty(p))
				exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_FAIL);

			scriptManager.addScript("anonymous#1", String.format("var param = %s;", p));

			NativeObject nativeObject = (NativeObject) scriptManager.getObject("param");

			String goodsNo = StringUtils.defaultString((String) nativeObject.get("goods_no"));
			String virVendNo = StringUtils.defaultString((String) nativeObject.get("vir_vend_no"));
			String lowVendTypeCd = StringUtils.defaultString((String) nativeObject.get("low_vend_type_cd"));
			String reservYn = StringUtils.defaultString(doc.select("#reserv_limit_divi_cd").attr("value")).equals("10") ? "Y" : "N";
			String colorYn = StringUtils.defaultString(doc.select("#color_mapp_option").attr("value")).equals("1") ? "Y" : StringUtils.EMPTY;

			param.put("goods_no", goodsNo);
			param.put("vir_vend_no", virVendNo);
			param.put("low_vend_type_cd", lowVendTypeCd);
			param.put("reserv_yn", reservYn);
			param.put("color_yn", colorYn);

			// 상품 데이터
			productDetail.setDiscountPrice(discountPrice);
			productDetail.setGoodsImage(goodsImage);
			productDetail.setGoodsName(goodsName);
			productDetail.setGoodsNum(goodsNum);
			productDetail.setOnlineGoodsNum(goodsNum);
			productDetail.setMaftOrigin(maftOrigin);
			productDetail.setPrice(price);
			productDetail.setBrandCode(OnlineStoreConst.BRAND_MIXXO);
			productDetail.setGoodsMaterials(goodsMaterials);
			productDetail.setCollectURL(productUrl);
		} finally {
			scriptManager.exit();
		}
		return param;
	}

	/**
	 * 재고정보 파싱을 위한 JSON을 파싱 처리한다.
	 * 
	 * @param content
	 * @param flag
	 * @param productDetail
	 * @param param
	 * @throws JSONException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@Logging
	@SuppressWarnings("unchecked")
	public void parseStockDetail(String content, int flag, ProductDetail productDetail, Map<String, String> param) throws JSONException, JsonParseException, JsonMappingException, IOException {

		// 재고 정보 JSON 형태
		List<Map<String, Object>> list = (List<Map<String, Object>>) mapper.readValue(content, List.class);

		String color = null, size = null;
		int stockAmount = 0;

		switch (flag) {
		case FLAG_PARSING_COLOR:
			List<String> colors = new ArrayList<>();
			for (Map<String, Object> item : list) {
				color = StringUtils.defaultString(((String) item.get("OPT_VAL_NM1")));
				colors.add(color);
			}

			param.put(OnlineStoreConst.KEY_COLLECT_COLORS, StringUtils.join(colors, "; "));
			break;
		case FLAG_PARSING_SIZE:
			for (Map<String, Object> item : list) {
				color = StringUtils.defaultString(param.get("opt_val_nm1")).toUpperCase();
				color = color.replaceAll("GRAY", "GREY"); // 오타 수정
				color = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "COLOR", color);

				size = StringUtils.defaultString(((String) item.get("OPT_VAL_NM2"))).toUpperCase();
				stockAmount = (Integer) item.get("SALE_POSS_QTY");

				// 재고데이터 추가
				Stock stock = new Stock();

				stock.setColor(color);
				stock.setSize(size);
				stock.setStockAmount(stockAmount);
				stock.setOpenMarketStockAmount(0);

				productDetail.addStock(stock);
			}
			break;
		}
	}

	/**
	 * 상품평 파싱을 위한 HTML을 파싱 처리한다.
	 * 
	 * @param content
	 * @param productDetail
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@Logging
	public boolean parseComments(String content, ProductDetail productDetail, int page) throws Exception {
		int commentLastPage = 1;
		Float rating;
		String writeComment = "";

		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("div.review_table > table > tbody > tr");

		// 페이지 정보
		Elements ele = doc.select("#page_nav a");
		if (ele.size() > 0)
			commentLastPage = Integer.parseInt(ele.last().attr("value"));

		// 상품평 데이터
		for (Element element : elements) {
			Comment comment = new Comment();

			String rates = element.select(" div.set_grade02 > span").attr("style").replaceAll("width:(.*)%;", "$1");
			rating = Float.parseFloat(rates) / 20;
			writeComment = element.select("> td").eq(1).select("div.rv_info03").text();

			comment.setGoodsRating(rating);
			comment.setGoodsComment(writeComment);

			productDetail.addComment(comment);
		}

		// 현재 페이지와 마지막 페이지 비교
		if (page == commentLastPage)
			return true;
		else
			return false;
	}

	/**
	 * 베스트 아이템 수집을 위한 HTML 파싱처리를 수행한다.
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<String> parseBestItems(String content) throws Exception {
		List<String> bestItemsNum = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("div.list_product01 > ul.list > li");

		// 베스트아이템 추가
		for (int i = 0; i < elements.size(); i++) {
			Element ele = elements.get(i);

			String goodsNum = attr(ele.select("> a"), "onclick");
			goodsNum = goodsNum.replaceAll("elandmall.goods.goDetail\\(\\{goods_no:'(\\d+)'.+$", "$1");
			bestItemsNum.add(goodsNum);
		}
		return bestItemsNum;
	}

	/**
	 * 제품 소재정보 파싱을 위한 Text 파싱처리를 수행한다.
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Logging(ErrorType.ERROR_PARSING_MATERIALS_FAIL)
	public List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception {

		// content에서 색상 존재 유무
		boolean colorExist = false;
		// content에 colors 색상 존재 유무 (colors: 재고정보의 색상)
		boolean contentContainColor = false;

		List<Materials> materialList = new ArrayList<>();

		// 색상 및 소재 정보 변환
		Map<String, String> colorMap = changeColors(colors);

		// 재고색상(colors)에서 처리하지 않은 색상 의미
		List<String> remainColor = new ArrayList<String>(colorMap.keySet());

		// 패턴으로 한계에 있는 문자 스트링인 경우 기 정의된 문자로 치환
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();
		content = replaceMarterialsString(onlineGoodsNum, content);

		// 소재 정보의 색상 및 소재 오타 수정
		String tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "REPLACE", content).trim();
		tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "COLOR", tmpContent);

		// 소재정보 자체가 없는 경우
		if (StringUtils.isBlank(tmpContent) || tmpContent.contains("Tel") || tmpContent.equals("-") || tmpContent.equals("--")) {
			for (Entry<String, String> entry : colorMap.entrySet()) {
				Materials materials = new Materials();
				materials.setColor(entry.getValue());
				materials.setGoodsComposed("전체");
				materials.setMaterials("기타");
				materials.setRatio(Float.valueOf(100));

				materialList.add(materials);
			}
			return materialList;
		}

		// %, &, (제외) 제거
		tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "REPLACE_POST", tmpContent);
		// 비율 숫자 앞,뒤 공백 제거
		tmpContent = removeUnlessString(tmpContent);

		String etcContent = tmpContent;
		colorExist = colorInfoExist(tmpContent, colorMap.keySet());

		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map;

		Pattern patternMaterial = Pattern.compile("([가-힣]+(?:\\([가-힣]+\\))?(?:\\+[가-힣]+)?\\d+(?:\\.\\d+)?)");
		Pattern patternColor = Pattern.compile("(" + PATTERN_FIND_COLOR + "(?:\\,)?)");
		Matcher matcherMaterial, matcherColor;

		boolean etcMatcherCheck = false;

		// 색상 정보 포함 유무 판별
		if (tmpContent.contains("[")) {
			contentContainColor = true;

			List<String> stList = new ArrayList<>();

			StringTokenizer st = new StringTokenizer(tmpContent, "[]");
			while (st.hasMoreTokens()) {
				stList.add(st.nextToken());
			}

			tmpContent = tmpContent.replaceAll("\\[|\\]", "");

			for (int i = 1; i < stList.size(); i += 2) {
				int plusIndex = 0;

				st = new StringTokenizer(stList.get(i - 1), ",");
				while (st.hasMoreTokens()) {
					tmpContent = tmpContent.replaceAll(st.nextToken().trim() + "(?:\\,)?", "");
				}

				stList.set(i, stList.get(i).replaceAll(" |\\/", ""));

				matcherMaterial = patternMaterial.matcher(stList.get(i));

				if (list.size() > 0) {
					plusIndex = Integer.parseInt(list.get(list.size() - 1).get("end"));
				}

				while (matcherMaterial.find()) {
					map = new HashMap<>();
					map.put("materialRatio", matcherMaterial.group(0));
					map.put("start", String.valueOf(matcherMaterial.start() + plusIndex));
					map.put("end", String.valueOf(matcherMaterial.end() + plusIndex));
					map.put("preColorContent", stList.get(i - 1));

					etcContent = etcContent.replace(matcherMaterial.group(0), "");

					matcherColor = patternColor.matcher(stList.get(i - 1));

					while (matcherColor.find()) {
						etcContent = etcContent.replaceFirst(matcherColor.group(1), "");
					}

					etcContent = etcContent.replace(stList.get(i - 1), "");
					etcContent = etcContent.replaceAll(" |\\[|\\]", "");

					list.add(map);
				}
			}
			tmpContent = tmpContent.replaceAll(" |\\/", "");
		} else {
			contentContainColor = false;

			Pattern p;
			Matcher m;

			// ex) 겉감-폴리에스터94%폴리우레탄6%배색1-폴리에스터94%폴리우레탄6%
			// ex) 겉감1 - 폴리에스터 88% 폴리우레탄 12% 겉감2 - 나일론 73% 폴리우레탄 27% 안감 - 면 100% 경우
			if (tmpContent.contains("배색1") || tmpContent.contains("겉감1")) {
				tmpContent = content.replaceAll(" ", "");

				p = Pattern.compile("([가-힣]+(?:\\d)?)-(([가-힣]+\\d+%)*)");
				m = p.matcher(tmpContent);

				while (m.find()) {
					etcMatcherCheck = true;

					matcherMaterial = patternMaterial.matcher(m.group(2));
					while (matcherMaterial.find()) {
						map = new HashMap<>();
						map.put("materialRatio", matcherMaterial.group(0));
						map.put("preContent", m.group(1));
						map.put("preColorContent", null);
						etcContent = etcContent.replace(matcherMaterial.group(0), "");
						etcContent = etcContent.replace(m.group(1), "");

						list.add(map);
					}
				}
			}

			if (!etcMatcherCheck) {
				matcherMaterial = patternMaterial.matcher(tmpContent);
				while (matcherMaterial.find()) {
					map = new HashMap<>();
					map.put("materialRatio", matcherMaterial.group(0));
					map.put("start", String.valueOf(matcherMaterial.start()));
					map.put("end", String.valueOf(matcherMaterial.end()));
					map.put("preColorContent", null);
					etcContent = etcContent.replace(matcherMaterial.group(0), "");

					list.add(map);
				}
			}
		}

		// 소재+숫자 형식(list)있는 경우,
		// 구성 및 색상 정보 파악하기 위해 해당 list의 앞부분 정보 담기
		if (list.size() > 0 && !etcMatcherCheck) {
			int startIndex = 0, endIndex = Integer.parseInt(list.get(0).get("start"));
			boolean checkMatch = false;
			Pattern p = Pattern.compile("([가-힣]+)\\((.*?)\\)");
			Matcher m;

			for (int i = 0; i < list.size(); i++) {

				String preContent = tmpContent.substring(startIndex, endIndex);
				list.get(i).put("preContent", preContent);

				if (i <= list.size() - 2) {
					if (Integer.parseInt(list.get(i + 1).get("start")) - Integer.parseInt(list.get(i).get("end")) > 1) {
						startIndex = Integer.parseInt(list.get(i).get("end"));
						endIndex = Integer.parseInt(list.get(i + 1).get("start"));
					}
				}
				m = p.matcher(preContent);
				checkMatch = false;
				while (m.find()) {
					checkMatch = true;
					etcContent = etcContent.replaceFirst(m.group(1) + "\\(" + m.group(2) + "\\)", "");
				}
				if (!checkMatch) {
					etcContent = etcContent.replaceFirst(preContent, "");
				}
			}
		}

		// list 앞부분에서 구성 및 색상 정보 추출
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> object = list.get(i);

			StringTokenizer st;
			List<String> stComposedArray = new ArrayList<>();
			List<String> stColorArray = new ArrayList<>();

			// 색상 매칭 결과정보
			Map<String, Object> colorMatchResult = new HashMap<>();
			List<String> colorArray = new ArrayList<>();
			List<Integer> removeIndex = new ArrayList<>();

			// 구성요소 매칭 결과 정보
			List<String> composedArray = new ArrayList<>();

			String tmpPreContent = object.get("preContent");
			String tmpPreColorContent = object.get("preColorContent");

			// 담아놓은 materialRatio에서 소재랑 비율 정보 추출
			String tmpMaterial = object.get("materialRatio").replaceAll("\\d+(?:\\.\\d+)?", "");
			String tmpRatio = object.get("materialRatio").replaceAll("([가-힣]+(?:\\([가-힣]+\\))?)", "");

			// 색상 정보 추출
			if (tmpPreColorContent != null) {
				st = new StringTokenizer(tmpPreColorContent, "-,:");

				while (st.hasMoreTokens())
					stColorArray.add(st.nextToken());

				colorMatchResult = matchingColor(stColorArray, remainColor);
				removeIndex = (List<Integer>) colorMatchResult.get("removeIndex");
				colorArray = (List<String>) colorMatchResult.get("colorMatchSuccess");
			}

			// 구성 정보 추출
			if (tmpPreContent != null) {
				st = new StringTokenizer(tmpPreContent, "-,:/");

				while (st.hasMoreTokens())
					stComposedArray.add(st.nextToken());

				composedArray.addAll(matchingComposed(stComposedArray));
			}

			// 소재정보 색상 = 재고 색상인 경우
			if (colorArray.size() > 0) {
				for (int j = 0; j < colorArray.size(); j++) {
					for (int k = 0; k < composedArray.size(); k++) {
						Materials materials = new Materials();
						materials.setColor(colorMap.get(colorArray.get(j)));
						materials.setGoodsComposed(composedArray.get(k));
						materials.setMaterials(tmpMaterial);
						materials.setRatio(Float.valueOf(tmpRatio));

						materialList.add(materials);
					}
				}
			}
			// 소재정보 색상 != 재고색상인 경우
			else if (colorArray.size() == 0) {
				// 소재정보 색상이 재고 색상에 없는 색인 경우 스킵
				if (tmpPreColorContent != null && tmpPreColorContent.matches(".*[a-zA-Z].*")) {
					continue;
				}
				// 재고 색상에 맞는 소재정보 없음
				else if (!colorExist && contentContainColor) {
					return Collections.EMPTY_LIST;
				}
				// ex) 마52% 면48% || 소재정보 색상엔 없고, 재고색상(colors)에만 있는 경우
				else if (!contentContainColor || colorExist) {
					for (int j = 0; j < remainColor.size(); j++) {
						for (int k = 0; k < composedArray.size(); k++) {
							Materials materials = new Materials();
							materials.setColor(colorMap.get(remainColor.get(j)));
							materials.setGoodsComposed(composedArray.get(k));
							materials.setMaterials(tmpMaterial);
							materials.setRatio(Float.valueOf(tmpRatio));

							materialList.add(materials);
						}
					}
				}
			}

			// 이미 처리한 색상 제거
			if (i < list.size() - 1) {
				for (int j = removeIndex.size() - 1; j >= 0; j--) {
					if (!list.get(i + 1).get("preColorContent").equals(tmpPreColorContent)) {
						int a = removeIndex.get(j);
						remainColor.remove(a);
					}
				}
			}

			// 기타 패턴 추출 위해
			if (etcContent.length() > 1) {
				etcContent = etcContent.replaceAll(" |\\/|\\,", "");
			}
		}

		// 기타 (소재+비율 형식이 아닐 경우)
		if (list.size() == 0 || etcContent.length() > 1) {

			if (list.size() > 0 && etcContent.length() > 1) {
				list.clear();
				tmpContent = etcContent;
			}

			int index = 0;

			Pattern p;
			Matcher m;

			String tmpMaterial = "", tmpRatio = "", tmpComposed = "전체";

			if (tmpContent.contains("겉감1") || tmpContent.contains("겉감2")) {
				tmpContent = tmpContent.replaceAll(" ", "");

				// ex) 겉감1-양가죽/겉감2-폴리에스터/안감-폴리에스터
				p = Pattern.compile("([가-힣]+\\d-[가-힣]+)(?: )?\\/(?: )?([가-힣]+\\d-[가-힣]+)(?: )?\\/(?: )?([가-힣]+-[가-힣]+)");
				m = p.matcher(tmpContent);

				while (m.find()) {
					for (int i = 1; i <= m.groupCount(); i++) {
						String group = m.group(i);

						tmpComposed = group.substring(0, group.indexOf("-"));
						tmpMaterial = group.substring(group.indexOf("-") + 1);
						tmpRatio = "100";

						map = new HashMap<>();
						map.put("tmpMaterial", tmpMaterial);
						map.put("tmpComposed", tmpComposed);
						map.put("tmpRatio", tmpRatio);
						list.add(map);
					}
				}

				// ex) 겉감1-폴리에스터100% 겉감2-천연모피(밍크)
				p = Pattern.compile("-([가-힣]+\\d)-([가-힣]+\\([가-힣]+\\))");
				m = p.matcher(tmpContent);

				while (m.find()) {
					tmpComposed = m.group(1);
					tmpMaterial = m.group(2);
					tmpRatio = "100";

					map = new HashMap<>();
					map.put("tmpMaterial", tmpMaterial);
					map.put("tmpComposed", tmpComposed);
					map.put("tmpRatio", tmpRatio);
					list.add(map);
				}
			}

			if (list.size() == 0) {

				// ex) 갑피:인조가죽 창:TPU, 갑피-FABRIC / 창-TPR
				if (tmpContent.contains(":") || tmpContent.contains("-")) {
					p = Pattern.compile("(\\w+|[가-힣]+)(?::)?(?:(?:\\s)?-(?:\\s)?)?(\\w+|[가-힣]+)(?:\\+\\w+|[가-힣]+)?");
					m = p.matcher(tmpContent);

					while (m.find()) {
						map = new HashMap<>();
						tmpMaterial = m.group(0);
						tmpMaterial = tmpMaterial.replaceAll(" ", "");
						tmpRatio = "100";

						if (tmpMaterial.contains(":") || tmpMaterial.contains("-")) {
							if (tmpMaterial.contains(":"))
								index = tmpMaterial.indexOf(":");
							else if (tmpMaterial.contains("-"))
								index = tmpMaterial.indexOf("-");

							tmpComposed = tmpMaterial.substring(0, index);
							tmpMaterial = tmpMaterial.substring(index + 1);
						}

						map.put("tmpMaterial", tmpMaterial);
						map.put("tmpComposed", tmpComposed);
						map.put("tmpRatio", tmpRatio);
						list.add(map);
					}
				} else { // ex) 고무
					tmpContent = tmpContent.replaceAll(" ", "");

					p = Pattern.compile("[가-힣]+");
					m = p.matcher(tmpContent);

					while (m.find()) {
						map = new HashMap<>();
						tmpMaterial = m.group(0);
						tmpRatio = "100";

						map.put("tmpMaterial", tmpMaterial);
						map.put("tmpComposed", tmpComposed);
						map.put("tmpRatio", tmpRatio);
						list.add(map);
					}
				}
			}
			// 기타형식에서 추출한 소재데이터 추가
			for (int i = 0; i < list.size(); i++) {
				for (String color : colorMap.keySet()) {
					String tmpCol = color;
					String tmpCom = list.get(i).get("tmpComposed");
					String tmpMat = list.get(i).get("tmpMaterial");
					String tmpRat = list.get(i).get("tmpRatio");

					Materials materials = new Materials();
					materials.setColor(colorMap.get(tmpCol));
					materials.setGoodsComposed(tmpCom);
					materials.setMaterials(tmpMat);
					materials.setRatio(Float.valueOf(tmpRat));

					materialList.add(materials);
				}
			}
		}

		// 보정 로직
		Collections.sort(materialList, materialsComparator);
		CollectionUtils.filter(materialList, new Predicate() {

			private Materials materials = null;

			@Override
			public boolean evaluate(Object object) {
				boolean result = true;
				Materials materials = (Materials) object;
				if (this.materials != null)
					result = (materialsComparator.compare(materials, this.materials) != 0);
				this.materials = materials;
				return result;
			}
		});

		return materialList;
	}

	// 재고정보의 색상형태 변환 (숫자제거)
	private Map<String, String> changeColors(String[] colors) {

		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < colors.length; i++) {
			String value = colors[i].replaceAll("\\(.*?\\)", "");
			String key = value.replaceAll(" ", "");

			map.put(key, value);
		}
		return map;
	}

	// 소재정보에 재고 색상정보 들어있는지 판단
	private boolean colorInfoExist(String content, Set<String> colors) {
		for (String color : colors) {
			if (content.contains(color))
				return true;
		}
		return false;
	}

	// 소재정보의 구성부분(겉감/안감) 매칭
	private List<String> matchingComposed(List<String> array) {
		List<String> composedResult = new ArrayList<>();

		Pattern p = Pattern.compile("상의|하의|컵\\(배색겉감\\)|컵\\(겉감\\)|컵\\(레이스\\)|컵\\(안감\\)|컵\\(메쉬안감\\)|컵\\(메인안감\\)|날개\\(겉감\\)|날개\\(안감\\)|날개\\(레이스\\)|마지\\(겉감\\)|마지\\(안감\\)|옆판\\(겉감\\)|옆판\\(안감\\)|겉감1|겉감2|겉감\\(플로킹\\)|겉감\\(표면\\)" + "|겉감\\(이면\\)|소매\\(충전재\\)|마찌\\(안감\\)|몸판안감|소매안감|바디안감|배색감|주머니감|배색1|배색2|앞판\\(겉감\\)|앞판\\(안감\\)|앞판\\(상단\\)|앞판\\(하단\\)|겉감|안감|배색|앞판|뒷판|옆판|갑피|창|외층|내층|충전재|시보리|다운백|충전백|밑창|퍼옷깃표면|퍼옷깃이면|퍼옷깃|방울|RUF|FIBER|LINING");

		for (int j = 0; j < array.size(); j++) {
			Matcher m = p.matcher(array.get(j));
			if (m.find())
				composedResult.add(m.group(0));
		}

		if (composedResult.size() == 0)
			composedResult.add("전체");

		return composedResult;
	}

	// 소재정보에서 재고색상과의 매칭
	private Map<String, Object> matchingColor(List<String> array, List<String> remainColors) {
		Map<String, Object> resultMap = new HashMap<>();
		List<String> colorMatchSuccess = new ArrayList<>();
		List<Integer> removeIndex = new ArrayList<>();

		resultMap.put("colrContains", false);

		for (int j = 0; j < remainColors.size(); j++) {
			String remainColor = remainColors.get(j).replaceAll("\\s+", "");
			boolean currentColorCheck = false;
			for (int k = 0; k < array.size(); k++) {
				String currentColor = array.get(k).replaceAll("\\s+", "");
				if (currentColor.contains(remainColor)) {
					colorMatchSuccess.add(remainColor);
					resultMap.put("colrContains", true);
					currentColorCheck = true;
				}
			}
			if (currentColorCheck)
				removeIndex.add(j);
		}

		resultMap.put("colorMatchSuccess", colorMatchSuccess);
		resultMap.put("removeIndex", removeIndex);

		return resultMap;
	}

	// 숫자 앞,뒤,필요없는 기호 제거
	private String removeUnlessString(String str) {
		List<Integer> remove = new ArrayList<>();

		Pattern p = Pattern.compile("(\\s)(\\d+)");
		Matcher m = p.matcher(str);
		while (m.find()) {
			remove.add(m.start(0));
		}
		p = Pattern.compile("-(\\d+)");
		m = p.matcher(str);
		while (m.find()) {
			remove.add(m.start(0));
		}
		str = stringBuffer(str, remove, 0);

		p = Pattern.compile("(\\d+)(\\s)");
		m = p.matcher(str);
		while (m.find()) {
			remove.add(m.end(0));
		}
		p = Pattern.compile("(\\d+)(,)");
		m = p.matcher(str);
		while (m.find()) {
			remove.add(m.end(0));
		}

		str = stringBuffer(str, remove, 1);

		return str;
	}

	private String stringBuffer(String str, List<Integer> remove, int flag) {
		StringBuffer sb;

		Collections.sort(remove, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.intValue() - o1.intValue();
			}
		});

		sb = new StringBuffer(str);

		switch (flag) {
		case 0: // 앞부분 제거
			for (int i = 0; i < remove.size(); i++) {
				sb.delete(remove.get(i), remove.get(i) + 1);
			}
			break;
		case 1: // 뒷부분 제거
			for (int i = 0; i < remove.size(); i++) {
				sb.delete(remove.get(i) - 1, remove.get(i));
			}
			break;
		}
		remove.clear();

		return sb.toString();
	}
}