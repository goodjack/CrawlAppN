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
 * 스파오 사이트 파서 
 */
public class SpaoStoreParser extends OnlineStoreParser implements InitializingBean {

	private final static String[] TOP_CATEGORIES = new String[] { "MEN", "WOMEN", "FOR MEN" };
	private final static String[] TOP_CATEGORIES_NEW = new String[] { "OUTER", "TOP", "SHIRT","BOTTOM","FOR MEN","INNERWEAR", "DRESS"};
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

	public SpaoStoreParser(OnlineStoreConst constant) {
		super(constant);
	}

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
	 * 카테고리 수집을 위한 HTML을 파싱처리한다.
	 * ※ 탑 카테고리
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
			// 스파오는MAN : for man 2단계 
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
	 * 카테고리 수집을 위한 HTML을 파싱처리한다.
	 * ※ 서브 카테고리
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

			// NEW 카테고리 제외
			if (!secondDepthName.equals("NEW")) {
				Elements el = elements.get(i).select("> div.depth2 > ul > li");

				// 카테고리 스텝이 2단계인 경우
				if (el.size() == 0) {
					String url = "http://spao.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + secondDepthCode;

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
					String url = "http://spao.elandmall.com/dispctg/initDispCtg.action?listOnly=Y&disp_ctg_no=" + code;

					Category category = new Category();
					category.addCategoryName(topDepthName);
					category.addCategoryName(secondDepthName);
					category.addCategoryName(name);
					category.setCategoryUrl(url);

					categories.add(category);
				}
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

			String productUrl = "http://spao.elandmall.com/goods/initGoodsDetail.action?goods_no=" + goodsNum;

			Product product = new Product(category, productUrl, goodsNum);
			product.setBrandName(OnlineStoreConst.BRAND_SPAO);

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
				if (maftOrigin.length() == 0 && detailTitle.get(i).text().contains("제조국"))
					maftOrigin = detailTitle.get(i).nextElementSibling().text();
				else if (goodsMaterials.length() == 0 && detailTitle.get(i).text().contains("소재"))
					goodsMaterials = detailTitle.get(i).nextElementSibling().text();

				if (maftOrigin.length() > 0 && goodsMaterials.length() > 0)
					break;
			}
			
			if(StringUtils.isBlank(maftOrigin) && StringUtils.isBlank(goodsMaterials))
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
						if(sibling != null && sibling.is("span.tx_sale_price"))
							minimun = Integer.parseInt(text(sibling, false).replaceAll("\\D", ""));
						else	
							minimun = Integer.parseInt(textNode(el, 2).replaceAll("\\D", ""));
					} else {
						if(e != null && e.is("span.tx_sale_price"))
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
			productDetail.setBrandCode(OnlineStoreConst.BRAND_SPAO);
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
			if(StringUtils.isEmpty(writeComment))
				writeComment = " ";

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
		
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();
		
		// 패턴으로 한계에 있는 문자 스트링인 경우 기 정의된 문자로 치환한다.
		String tmpContent = replaceMarterialsString(onlineGoodsNum, content);
		tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "REPLACE", tmpContent).trim();

		// 색상 및 소재 정보 변환
		Map<String, String> colorMap = changeColors(colors);
		// 소재 정보의 색상형태 변환 (영문변환)
		tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "COLOR", tmpContent);

		List<Materials> materialList = new ArrayList<>();

		// colors 에서 처리하고 남은 색상리스트
		List<String> remainColor = new ArrayList<String>(colorMap.keySet());

		// content에서 색상 존재 유무
		boolean colorExist = false;
		// content에 colors 색상 존재 유무 (colors: 재고정보의 색상)
		boolean contentContainColor = false;

		// 소재정보 자체가 없는 경우
		if (StringUtils.isBlank(tmpContent) || tmpContent.contains("매장 확인") || tmpContent.equals(".") || tmpContent.equals("1")) {
			for (Entry<String, String> entry : colorMap.entrySet()) {
				Materials materials = new Materials();
				materials.setColor(entry.getValue());
				materials.setGoodsComposed("전체");
				materials.setMaterials("기타");
				materials.setRatio(Float.valueOf(100));

				materialList.add(materials);
			}
			return materialList;
		}else if (tmpContent.contains("00차")) { // ex) 00차 그레이, 블랙 -폴리에스터 84% 레이온 12% 폴리우레탄 4% 00차 네이비 - 폴리에스터 98% 폴리우레탄 2% 01차 네이비 - 폴리에스터 84% 폴리우레탄 4% 레이온 12% 02차 그레이, 블랙 - 폴리에스터 85% 폴리우레탄 5% 레이온 10%
			tmpContent = tmpContent.replaceAll("\"", "");
			tmpContent = tmpContent.replaceAll(" ", "");
			Pattern pattern = Pattern.compile("\\d\\d차[a-zA-Z]+(?:\\,[a-zA-Z]+)?\\-([가-힣]+\\d+%)*");
			Matcher matcher = pattern.matcher(tmpContent);
			int last = 0;
			while (matcher.find()) {
				tmpContent = tmpContent.substring(last);
				if (matcher.group().contains("00차")) {
					last = matcher.end() - last;
				} else {
					last = 0;
				}
			}
			tmpContent = tmpContent.replaceAll("\\d\\d차", "");
		} else if (tmpContent.contains("재입고")) { // ex) 블랙, 차콜 : 나일론 20%, 울 80% 차콜(재입고) : 아크릴 2%, 면 2%, 폴리에스터 30%, 울 62%, 레이온 2%
			Pattern pattern = Pattern.compile("[a-zA-Z]+\\(재입고\\)");
			Matcher matcher = pattern.matcher(tmpContent);
			String copyContent = tmpContent;
			String reFind = "";
			while (matcher.find()) {
				String tmp = matcher.group();
				copyContent = copyContent.replaceAll("[a-zA-Z]+\\(재입고\\)", "");
				reFind = tmp.replaceAll("\\(.*?\\)", "");
			}
			if (copyContent.contains(reFind))
				tmpContent = tmpContent.replaceFirst(reFind, "");
		}

		// 공백, %, 괄호포함 내용 제거
		tmpContent = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "REPLACE_POST", tmpContent);

		String etcContent = tmpContent;
		colorExist = colorInfoExist(tmpContent, colorMap.keySet());

		Pattern tmpP = Pattern.compile("[a-zA-Z]+");
		Matcher tmpM = tmpP.matcher(tmpContent);
		if (tmpM.find())
			contentContainColor = true;
		else
			contentContainColor = false;

		// 소재+숫자 형식
		Pattern pattern = Pattern.compile("([가-힣]+(?:\\([가-힣]+\\))?\\d+(?:\\.\\d+)?)");
		Matcher matcher = pattern.matcher(tmpContent);

		List<Map<String, String>> list = new ArrayList<>();

		while (matcher.find()) {
			Map<String, String> map = new HashMap<>();
			map.put("materialRatio", matcher.group(0));
			map.put("start", String.valueOf(matcher.start()));
			map.put("end", String.valueOf(matcher.end()));
			etcContent = etcContent.replace(matcher.group(0), "");

			list.add(map);
		}

		// 소재+숫자 형식(list)있는 경우,
		// 겉감/안감 및 색상 정보 파악하기 위해 해당 list의 앞부분 정보 담기
		if (list.size() > 0) {
			int startIndex = 0, endIndex = Integer.parseInt(list.get(0).get("start"));

			for (int i = 0; i < list.size(); i++) {
				String preContent = tmpContent.substring(startIndex, endIndex);

				list.get(i).put("preContent", preContent);

				if (i <= list.size() - 2) {
					if (Integer.parseInt(list.get(i + 1).get("start")) - Integer.parseInt(list.get(i).get("end")) > 2) {
						etcContent = etcContent.replaceFirst(preContent, "");
						startIndex = Integer.parseInt(list.get(i).get("end"));
						endIndex = Integer.parseInt(list.get(i + 1).get("start"));
					}
				}
				if (i == list.size() - 1)
					etcContent = etcContent.replaceFirst(list.get(i).get("preContent"), "");
			}
		}

		// list별 앞부분에서 겉감/안감 및 색상 정보 추출
		for (int i = 0; i < list.size(); i++) {
			Map<String, String> object = list.get(i);
			StringTokenizer st;
			List<String> array = new ArrayList<>();

			String tmpPreContent = object.get("preContent");

			// 담아놓은 materialRatio에서 소재랑 비율 정보 추출
			String tmpMaterial = object.get("materialRatio").replaceAll("\\d+(?:\\.\\d+)?", "");
			String tmpRatio = object.get("materialRatio").replaceAll("[가-힣]+", "");

			// 담아놓은 preContent에서 색상및 겉감/안감 정보 추출
			if (!tmpPreContent.contains("D") && !tmpPreContent.contains("L") && !tmpPreContent.contains("M"))
				st = new StringTokenizer(tmpPreContent, "-,:/");
			else
				st = new StringTokenizer(tmpPreContent, "-,:");

			while (st.hasMoreTokens())
				array.add(st.nextToken());

			// 색상 매칭 결과정보
			Map<String, Object> colorMatchResult = new HashMap<>();
			List<String> colorArray = new ArrayList<>();
			List<Integer> removeIndex = new ArrayList<>();

			colorMatchResult = matchingColor(array, remainColor);
			removeIndex = (List<Integer>) colorMatchResult.get("removeIndex");
			colorArray = (List<String>) colorMatchResult.get("colorMatchSuccess");

			// 겉감/안감 매칭정보
			List<String> composedArray = new ArrayList<>();
			composedArray.addAll(matchingComposed(array));

			// 소재정보 색상 = 재고 색상인 경우
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

			// 소재정보 색상 != 재고색상인 경우
			if (colorArray.size() == 0) {
				// 소재정보 색상 = 재고색상외의 다른 색상인 경우 스킵
				if (tmpPreContent.matches(".*[a-zA-Z].*")) {
					continue;
				}
				// 소재정보에 처음부터 색상 정보 알 수 없는 경우 추후 확인 위해 list 맨 뒤로 추가
				// ex) 면 68 / 나일론 32 블랙 : 면70/ 나일론30 더스트블랙 : 폴리63/ 면20/ 나일론17
				if (colorExist && i == 0) {
					list.add(object);
					list.remove(0);
				}
				// 재고 색상에 맞는 소재정보 없음
				else if (!colorExist && contentContainColor) {
					return Collections.EMPTY_LIST;
				}
				// ex) 마52% 면48% || 소재정보 색상엔 없고, 재고색상(colors)에만 있는 경우
				else if (!contentContainColor || colorExist) {
					// remainColor = 재고색상(colors)에서 처리하지 않은 색상 의미
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
					if (!list.get(i + 1).get("preContent").equals(tmpPreContent)) {
						int a = removeIndex.get(j);
						remainColor.remove(a);
					}
				}

				// ex) 면 100/바닥 PVC 인경우 대비 -> ACC 카테고리에 해당하여 불필요해짐
				if (etcContent.length() > 1) {
					etcContent = etcContent.replaceAll("", "");
					etcContent = etcContent.replaceAll("\\/", "");
					etcContent = etcContent.replaceAll("\\,", "");
				}
			}
		}

		// 기타 (소재+비율 형식이 아닐 경우)
		if (list.size() == 0) {
			Pattern p;
			Matcher m;

			String tmpMaterial = "", tmpRatio = "", tmpComposed = "전체";
			Map<String, String> map = new HashMap<>();

			if (tmpContent.contains(":")) { // ex) 겉감:인조가죽, 창:러버
				p = Pattern.compile("[가-힣]+:[가-힣]+");
				m = p.matcher(tmpContent);

				while (m.find()) {
					tmpMaterial = m.group(0);
					tmpRatio = "100";

					int index = tmpMaterial.indexOf(":");
					tmpComposed = tmpMaterial.substring(0, index);
					tmpMaterial = tmpMaterial.substring(index + 1);

					map.put("tmpMaterial", tmpMaterial);
					map.put("tmpComposed", tmpComposed);
					map.put("tmpRatio", tmpRatio);
					list.add(map);
				}
			} else { // ex) 고무
				p = Pattern.compile("[가-힣]+");
				m = p.matcher(tmpContent);

				while (m.find()) {
					tmpMaterial = m.group(0);
					tmpRatio = "100";

					map.put("tmpMaterial", tmpMaterial);
					map.put("tmpComposed", tmpComposed);
					map.put("tmpRatio", tmpRatio);
					list.add(map);
				}
			}

			// ex) 폴리에스터, 플라스틱, FRP
			tmpContent = tmpContent.replaceAll("[가-힣]", "");
			p = Pattern.compile("[a-zA-Z]+");
			m = p.matcher(tmpContent);

			while (m.find()) {
				tmpMaterial = m.group(0).replaceAll("\\d", "");
				tmpRatio = tmpContent.replaceAll("\\D", "");

				if (tmpRatio.length() == 0)
					tmpRatio = "100";

				map.put("tmpMaterial", tmpMaterial);
				map.put("tmpComposed", tmpComposed);
				map.put("tmpRatio", tmpRatio);
				list.add(map);
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
		Pattern p = Pattern.compile("겉감하단|겉감외피|겉감상단|겉감내피|소매안감|바디안감|몸판안감|충전재|시보리|비소리|다운백|표면|이면|안감|소매|배색|밑단|몸판|겉면|겉감|창");

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
			boolean currentColorCheck = false;
			for (int k = 0; k < array.size(); k++) {
				if (array.get(k).contains(remainColors.get(j))) {
					colorMatchSuccess.add(remainColors.get(j));
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
}