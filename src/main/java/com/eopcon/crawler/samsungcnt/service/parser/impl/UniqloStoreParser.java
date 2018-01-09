package com.eopcon.crawler.samsungcnt.service.parser.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.NativeArray;
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

/**
 * 유니클로 사이이트 파서
 */
public class UniqloStoreParser extends OnlineStoreParser implements InitializingBean {

	public final static String KEY_EXTRA_PRICE_INFO = "KEY_EXTRA_PRICE_INFO";
	public final static String KEY_EXTRA_IMAGE_INFO = "KEY_EXTRA_IMAGE_INFO";

	private final static String[] TOP_CATEGORIES = new String[] { "WOMEN", "MEN" };
	private final static String FILTER_CATEGORIES = "^(?:SPECIAL|FEATURE|COLLABORATION)$";

	public UniqloStoreParser(OnlineStoreConst constant) {
		super(constant);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}

	/**
	 * 카테고리 수집을 위한 HTML을 파싱처리한다.
	 * 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Category> parseCategories(String content) throws Exception {

		List<Category> categories = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		Elements elements = doc.select("ul#navHeader > li");

		for (int i = 0; i < TOP_CATEGORIES.length; i++) {
			Element element = elements.get(i);

			for (Element el : element.select("> div > div > div.col_block")) {
				String temp = StringUtils.EMPTY;

				List<String> list = new ArrayList<>();
				list.add(TOP_CATEGORIES[i]);

				Element sibling = el.firstElementSibling();
				if (sibling != null && sibling.nodeName().equals("h4")) {
					temp = text(sibling, false);
					if (StringUtils.isEmpty(temp)) {
						Element previousParent = sibling.parent().previousElementSibling();
						sibling = previousParent.child(0);
						if (sibling != null && sibling.nodeName().equals("h4"))
							temp = text(sibling, false);
					}
				}

				if (StringUtils.isNotEmpty(temp)) {
					temp = temp + " " + text(el.select("> h5").first(), false);
				} else {
					temp = text(el.select("> h5").first(), false);
				}

				if (temp.matches(FILTER_CATEGORIES))
					continue;

				list.add(temp);

				for (Element e : el.select("> ul > li")) {
					String categoryName = text(e.select("> a").first(), false);
					String categoryUrl = attr(e.select("> a").first(), "href");

					logger.debug("# categoryName : {}, categoryUrl : {}", categoryName, categoryUrl);

					if (categoryUrl.indexOf("/display") == -1 || categoryUrl.indexOf("/index.html") > -1 || categoryUrl.indexOf("/timeline") > -1)
						continue;

					if (!categoryUrl.matches("^http[s]?://.+$"))
						categoryUrl = "http://www.uniqlo.kr" + categoryUrl;

					Category category = new Category();

					for (String name : list)
						category.addCategoryName(name);

					category.addCategoryName(categoryName);
					category.setCategoryUrl(categoryUrl);

					categories.add(category);
				}
			}
		}
		return categories;
	}

	/**
	 * 상품목록 수집을 위한 HTML을 파싱처리한다.
	 * 
	 * @param content
	 * @param category
	 * @return
	 * @throws Exception
	 */
	@Logging
	public List<Product> parseProductList(String content, Category category) throws Exception {
		List<Product> productList = new ArrayList<>();

		Document doc = Jsoup.parse(filterBody(content));
		List<String> titles = new ArrayList<>();

		Elements elements = doc.select(" div.blkMultibuyContent > h3.tittype02");

		if (!elements.isEmpty()) {
			for (Element el : elements) {
				String title = text(el, false);
				titles.add(title);

				if (logger.isDebugEnabled())
					logger.debug("# Product Title -> {}", title);
			}

			int i = 0;
			for (Element el : doc.select(" div.blkItemList")) {
				Category c = category.copy();
				c.addCategoryName(titles.get(i++));

				for (Element element : el.select(" ul.uniqlo_info > li")) {
					String path = attr(element.select("> div.thumb a").first(), "href");
					String productUrl = "http://www.uniqlo.kr" + path;
					String onlineGoodsNum = productUrl.replaceAll(".+[\\?&]{1}goodsNo=([^&]+)(:?&.+)?$", "$1");

					Product product = new Product(c, productUrl, onlineGoodsNum);
					product.setBrandName(OnlineStoreConst.BRAND_UNIQLO);

					productList.add(product);
				}
			}
		}
		return productList;
	}

	/**
	 * 상품상세 정보 수집을 위한 HTML을 파싱처리한다.
	 * 
	 * @param content
	 * @param productDetail
	 * @throws Exception
	 */
	@Logging
	public void parseProductDetail(String content, ProductDetail productDetail) throws Exception {
		Document doc = Jsoup.parse(content);

		try {
			scriptManager.enter();

			Element cookieForm = doc.select("form#latestGoodsCookieForm").first();
			Elements scripts = doc.select("script");

			String goodsNum = attr(cookieForm.select("> input[name='corporationGoodsNo']").first(), "value");
			String goodsName = attr(cookieForm.select("> input[name='goodsName']").first(), "value");
			String brandCode = OnlineStoreConst.BRAND_UNIQLO;
			String goodsImage = null;
			Integer price = Integer.parseInt(attr(cookieForm.select("> input[name='originSalePrice']").first(), "value"));
			Integer discountPrice = Integer.parseInt(attr(cookieForm.select("> input[name='salePrice']").first(), "value"));
			String maftOrigin = "";
			String goodsMaterials = "";

			for (Element el : doc.select("div#prodDetail dl.spec_new > dt")) {
				String title = text(el, false);

				if (title.equals("제품소재")) {
					goodsMaterials = text(el.nextElementSibling(), false);
				} else if (title.equals("제조국")) {
					maftOrigin = text(el.nextElementSibling(), false);
				}
			}

			int i = 0;
			StringBuilder sb = new StringBuilder();

			scriptManager.addScript("anonymous#" + i++, "var opt_list = new Array();");
			scriptManager.addScript("anonymous#" + i++, "var opt_tval = new Array();");
			scriptManager.addScript("anonymous#" + i++, "var itemInvQtyInfo = new Array();");
			scriptManager.addScript("anonymous#" + i++, "var itemImageInfo = new Array();");

			for (Element el : scripts) {
				String script = text(el, true);

				if (script.indexOf("itemImageInfo[optval] = ") > -1) {
					Pattern pattern = Pattern.compile("(itemImageInfo\\[optval\\]\\s*=\\s*\\{[^}]+\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(script);
					int number = 0;
					while (matcher.find()) {
						String temp = matcher.group(1);
						temp = temp.replaceAll("\\[optval\\]", "[" + (number++) + "]");

						sb.append(temp);
						sb.append('\n');
					}
				}

				if (script.indexOf("itemInvQtyInfo['0'] = ") > -1) {
					Pattern pattern = Pattern.compile("(itemInvQtyInfo\\['\\d+'\\]\\s*=\\s*\\{[^}]+\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(script);

					while (matcher.find()) {
						String temp = matcher.group(1);

						sb.append(temp);
						sb.append('\n');
					}
				}

				if (script.indexOf("opt_list['0'] = ") > -1) {
					Pattern pattern = Pattern.compile("(opt_list\\['\\d+'\\]\\s*=\\s*\\{[^}]+\\};)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(script);

					while (matcher.find()) {
						String temp = matcher.group(1);

						sb.append(temp);
						sb.append('\n');
					}
				}

				if (script.indexOf("fn_selectPrice = function(type){") > -1) {
					Pattern pattern = Pattern.compile("fn_selectPrice\\s*=\\s*function\\(type\\)\\{([^}]+)\\}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher matcher = pattern.matcher(script);

					if (matcher.find()) {
						String str = matcher.group(1);

						Pattern p = Pattern.compile("(opt_tval\\['\\d+'\\]\\s*=\\s*\\[[^\\]]+\\];)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
						Matcher m = p.matcher(str);

						while (m.find()) {
							String temp = m.group(1);

							sb.append(temp);
							sb.append('\n');
						}
					}
				}
			}

			scriptManager.addScript("anonymous#" + i++, sb.toString());
			scriptManager.addScript("anonymous#" + i++, String.format("%s var images = getImages('http://image.uniqlo.kr');", properties.getProperty("script.uniqlo.imageInfo")));
			scriptManager.addScript("anonymous#" + i++, String.format("%s var items = getItemInvQty();", properties.getProperty("script.uniqlo.itemInvQtyInfo")));
			scriptManager.addScript("anonymous#" + i++, String.format("%s var prices = getPrices();", properties.getProperty("script.uniqlo.optPriceInfo")));

			productDetail.putExtraString(KEY_EXTRA_IMAGE_INFO, (String) scriptManager.getObject("images"));
			productDetail.putExtraString(KEY_EXTRA_PRICE_INFO, (String) scriptManager.getObject("prices"));

			NativeArray nativeArray = (NativeArray) scriptManager.getObject("items");

			for (Object object : nativeArray) {
				if (object instanceof NativeObject) {
					NativeObject no = (NativeObject) object;

					String color = StringUtils.defaultString((String) no.get("color")); // ex) 03 GREY
					String size = StringUtils.defaultIfEmpty((String) no.get("size"), "0"); // ex) XL
					Double quantity = (Double) no.get("qty");

					Stock stock = new Stock();

					stock.setColor(color);
					stock.setSize(size);
					stock.setStockAmount(quantity.intValue());

					productDetail.addStock(stock);
				}
			}

			productDetail.setGoodsNum(goodsNum);
			productDetail.setGoodsName(goodsName);
			productDetail.setBrandCode(brandCode);
			productDetail.setGoodsImage(goodsImage);
			productDetail.setPrice(price);
			productDetail.setDiscountPrice(discountPrice);
			productDetail.setMaftOrigin(maftOrigin);
			productDetail.setGoodsMaterials(goodsMaterials);
		} finally {
			scriptManager.exit();
		}
	}

	@Override
	@Logging(ErrorType.ERROR_PARSING_MATERIALS_FAIL)
	public List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception {

		List<Materials> materials = new ArrayList<>();
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();

		// 파싱을 위해 몇몇 문자를 replace
		content = replaceString(onlineGoodsNum, content);

		if (StringUtils.isEmpty(content)) {
			for (String color : colors) {
				Materials m = new Materials();
				m.setColor(color);
				m.setGoodsComposed("전체");
				m.setMaterials("기타");
				m.setRatio(100F);

				materials.add(m);
			}
		} else {
			Map<String, List<Materials>> repo = parseMarterialsString(content);

			for (String color : colors) {
				List<Materials> list = repo.containsKey(color) ? repo.get(color) : repo.get("ALL");
				for (Materials ma : list) {
					Materials m = ma.copy();
					m.setColor(color);

					materials.add(m);
				}
			}
		}
		return materials;
	}

	private void shiftRightIfNull(String[] args) {
		String[] temp = new String[args.length];

		int index = args.length - 1;
		for (int i = index; i >= 0; i--) {
			if (StringUtils.isEmpty(args[i])) {
				index++;
			} else {
				temp[index] = args[i];
			}
			index--;
		}

		for (int i = 0; i < args.length; i++)
			args[i] = temp[i];
	}

	/**
	 * 소재정보 파싱을 위한 replace처리를 수행한다.
	 * 
	 * @param onlineGoodsNum
	 * @param content
	 * @return
	 */
	private String replaceString(String onlineGoodsNum, String content) {
		Pattern pattern = null;
		Matcher matcher = null;

		// 패턴으로 한계에 있는 문자 스트링인 경우 기 정의된 문자로 치환한다.
		content = replaceMarterialsString(onlineGoodsNum, content);
		// 오타 수정
		content = replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, "REPLACE", content);

		// 소재정보만 있을 경우 ex) 천연가죽(소가죽)
		if (content.matches("(?i)^[가-힣a-z\\(\\)]+$"))
			content += "100%";

		// 특정패턴 치환-1 ex) [00 WHITE, 65 BLUE] -> [00 WHITE/ 65 BLUE]
		pattern = Pattern.compile("(\\[[^\\]]+\\])");
		matcher = pattern.matcher(content);

		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String group = matcher.group(1);
			if (group.matches("^(?i)\\[\\d{2}\\s+.+\\]$"))
				group = group.replaceAll(",", "/");
			matcher.appendReplacement(sb, group);
		}
		matcher.appendTail(sb);
		content = sb.toString();

		/**
		 * 특정패턴 치환-2 ex)[상의] : [03 GRAY] [몸판] 면53%·폴리에스터47%, [리브 부분] 폴리에스터71%·면29%, [그 외 컬러] [몸판] 면78%·폴리에스터22%, [리브 부분] 면82%·폴리에스터18% [하의] : [69 NAVY/03 GRAY] 면53%·폴리에스터47%, [그 외 컬러] 면78%·폴리에스터22% -> [03 GRAY] [상의] [몸판] 면53%·폴리에스터47% [상의] [리브 부분] 폴리에스터71%·면29% [그 외 컬러] [상의] [몸판] 면78%·폴리에스터22% [상의] [리브 부분] 면82%·폴리에스터18% [69 NAVY/03 GRAY] [하의] 면53%·폴리에스터47% [그 외 컬러] [하의] 면78%·폴리에스터22%
		 */
		if (content.matches("(?i)^(:?\\[(?:상의|하의)\\]\\s*:).+$")) {
			sb.setLength(0);
			String[] array = content.split("(?=(:?\\[(?:상의|하의)\\]\\s*:))");

			for (String string : array) {
				if (StringUtils.isEmpty(string))
					continue;

				pattern = Pattern.compile("(\\[(?:상의|하의)\\])\\s*:\\s*(.*)", Pattern.CASE_INSENSITIVE);
				matcher = pattern.matcher(string.trim());

				while (matcher.find()) {
					String a = matcher.group(1);
					String b = matcher.group(2);

					Pattern p = Pattern.compile("(\\[[^\\]]+\\])?\\s*(\\[[^\\]]+\\])?\\s*((?:[가-힣\\(\\)a-z]+(?:\\d{1,3}%)?·?)+),?", Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(b);

					while (m.find()) {
						String[] group = new String[] { m.group(1), StringUtils.EMPTY, m.group(2), m.group(3) };

						for (int i = 0; i < group.length; i++) {
							String temp = StringUtils.defaultString(group[i]);
							if (StringUtils.isNotEmpty(temp) && (temp.matches("^(?i)\\[\\d{2}\\s+.+\\]$") || temp.matches("^(?i)\\[\\s*그\\s*외\\s*컬러\\s*\\]$"))) {
								group[i] = null;
								group[0] = temp;
							}
						}
						group[1] = a;

						String color = StringUtils.defaultString(group[0]);
						String parentComposed = StringUtils.defaultString(group[1]);
						String childComposed = StringUtils.defaultString(group[2]);
						String materials = StringUtils.defaultString(group[3]);

						// 하위 구성요소가 '상의|하의'일 경우
						if (childComposed.matches("^\\[(?:상의|하의)\\]$"))
							parentComposed = StringUtils.EMPTY;

						sb.append(String.format("%s %s %s %s ", color, parentComposed, childComposed, materials));
					}
				}
			}
			content = sb.toString();
		}

		/**
		 * 특정패턴 치환-3 ex) [겉감] 나일론100%, [충전재] [몸판] 옷깃 부분 : 다운90%·깃털10%, [목 부분] 바깥쪽 : 다운90%·깃털10%, 안쪽 : 폴리에스터100%, [안감] 나일론100% -> [겉감] 나일론100%, [충전재] [몸판:옷깃 부분] : 다운90%·깃털10%, [목 부분:바깥쪽] : 다운90%·깃털10%, [목 부분:안쪽] : 폴리에스터100%, [안감] 나일론100%
		 */
		if (content.matches("(?i).*(?:\\[([^\\]]+)\\])\\s*(?:([가-힣/]+쪽|[가-힣]+\\s*부분|[가-힣]+\\s*소재)\\s*:\\s*((?:[가-힣\\(\\)a-z]+(?:\\d{1,3}%)?·?)+),\\s*?)+.*")) {
			sb.setLength(0);
			pattern = Pattern.compile("(?:\\[([^\\]]+)\\])?\\s*(?:([가-힣/]+쪽|[가-힣]+\\s*부분|[가-힣]+\\s*소재)\\s*:\\s*((?:[가-힣\\(\\)a-z]+(?:\\d{1,3}%)?·?)+),\\s*?)+", Pattern.CASE_INSENSITIVE);
			matcher = pattern.matcher(content);

			String parentComposed = null;
			String childComposed = null;
			String materials = null;

			while (matcher.find()) {
				parentComposed = StringUtils.defaultString(matcher.group(1), parentComposed);
				childComposed = matcher.group(2);
				materials = matcher.group(3);

				matcher.appendReplacement(sb, String.format("[%s:%s] %s ", parentComposed, childComposed, materials));
			}
			matcher.appendTail(sb);
			content = sb.toString();
		}
		return content.trim();
	}

	public Map<String, List<Materials>> parseMarterialsString(String content) {

		Map<String, List<Materials>> repo = new HashMap<>();
		String[] colors = new String[] { "ALL" };

		String color = null;
		String goodsComposed = "전체";
		String parentComposed = null;
		String childComposed = null;
		String materials = null;

		if (content.matches("(?i)^[가-힣\\(\\)a-z]+\\d{1,3}%[·,]?.*$")) {
			parseMarterialsString(repo, colors, goodsComposed, content);
		} else if (Pattern.compile("\\[\\s*(?:테|렌즈|테/렌즈)\\s*\\]").matcher(content).find()) {
			Pattern pattern = Pattern.compile("(?:\\[\\s*(\\d{2}\\s+[^\\]]+|그\\s*외\\s*컬러)\\s*\\])?\\s*\\[\\s*재질\\s*\\]\\s*((?:\\[\\s*(?:테|렌즈|테/렌즈)\\s*\\]\\s*(?:[가-힣a-z\\s]+)\\s*,?\\s*)+)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(content);

			while (matcher.find()) {
				color = StringUtils.defaultString(matcher.group(1), "ALL");

				Pattern p = Pattern.compile("(?:\\[\\s*(테|렌즈|테/렌즈)\\s*\\]\\s*([가-힣a-z\\s]+))", Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(StringUtils.defaultString(matcher.group(2).trim()));

				while (m.find()) {
					goodsComposed = m.group(1);
					materials = String.format("%s100%%", StringUtils.defaultString(m.group(2)).trim());

					if (StringUtils.isNotEmpty(color)) {
						if (color.matches("^(?i)\\d{2}\\s+.+$"))
							colors = color.split("/");
						else if (color.matches("^(?i)그\\s*외\\s*컬러$"))
							colors = new String[] { "ALL" };
					}
					parseMarterialsString(repo, colors, goodsComposed, materials);
				}
			}
		} else if (content.matches("(?i)^(\\[[^\\]]+\\])\\s*(\\[[^\\]]+\\])?\\s*(\\[[^\\]]+\\])?\\s*((?:[가-힣\\(\\)a-z]+(?:\\d{1,3}%)?·?)+),?.*$")) {
			Pattern pattern = Pattern.compile("(\\[[^\\]]+\\])\\s*(\\[[^\\]]+\\])?\\s*(\\[[^\\]]+\\])?\\s*((?:[가-힣\\(\\)a-z]+(?:\\d{1,3}%)?·?)+),?", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(content);

			while (matcher.find()) {

				String[] group = new String[] { matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4) };
				shiftRightIfNull(group);

				for (int i = 0; i < group.length; i++) {
					String temp = StringUtils.defaultString(group[i]);
					if (StringUtils.isNotEmpty(temp) && (temp.matches("^(?i)\\[\\d{2}\\s+.+\\]$") || temp.matches("^(?i)\\[\\s*그\\s*외\\s*컬러\\s*\\]$"))) {
						group[i] = null;
						group[0] = temp;
					}
				}

				color = StringUtils.defaultString(group[0], color);
				parentComposed = StringUtils.defaultString(group[1], parentComposed);
				childComposed = group[2];
				materials = group[3];

				if (StringUtils.isNotEmpty(color))
					color = color.replaceAll("\\[([^\\]]+)\\]", "$1").trim();
				if (StringUtils.isNotEmpty(parentComposed))
					parentComposed = parentComposed.replaceAll("\\[([^\\]]+)\\]", "$1").trim();
				if (StringUtils.isNotEmpty(childComposed))
					childComposed = childComposed.replaceAll("\\[([^\\]]+)\\]", "$1").trim();

				// 해당 조합은 상하위 구성요소가 될 수 없다.
				if (StringUtils.isNotEmpty(parentComposed) && ((parentComposed.matches("^(?:충전재)$") && childComposed.matches("^(?:겉감|안감)$")) || (parentComposed.matches("^(?:상의|하의)$") && childComposed.matches("^(?:상의|하의)$")) || (parentComposed.matches("^(?:겉감|안감)$") && childComposed.matches("^(?:겉감|안감)$")))) {
					goodsComposed = childComposed;
				} else {
					goodsComposed = (StringUtils.isNotEmpty(parentComposed) ? String.format("[%s]%s", parentComposed, childComposed) : childComposed);
				}

				if (StringUtils.isNotEmpty(color)) {
					if (color.matches("^(?i)\\d{2}\\s+.+$"))
						colors = color.split("/");
					else if (color.matches("^(?i)그\\s*외\\s*컬러$"))
						colors = new String[] { "ALL" };
				}
				parseMarterialsString(repo, colors, goodsComposed, materials);
			}
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_PARSING_MATERIALS_FAIL);
		}
		return repo;
	}

	private void parseMarterialsString(Map<String, List<Materials>> repo, String[] colors, String goodsComposed, String materials) {
		if (StringUtils.isNotEmpty(materials)) {
			int count = 0;
			Pattern p = Pattern.compile("([가-힣\\(\\)\\s+]+)(\\d{1,3})%", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(materials);

			while (m.find()) {

				materials = m.group(1);
				Float ratio = Float.parseFloat(m.group(2));

				for (String c : colors) {
					c = c.toUpperCase().trim();

					List<Materials> list;
					Materials ma = new Materials();

					ma.setColor(c);
					ma.setGoodsComposed(StringUtils.defaultIfBlank(goodsComposed, "전체"));
					ma.setMaterials(materials);
					ma.setRatio(ratio);

					if (!repo.containsKey(c)) {
						list = new ArrayList<>();
						repo.put(c, list);
					} else {
						list = repo.get(c);
					}
					list.add(ma);
				}
				count++;
			}

			if (count == 0) {
				for (String c : colors) {
					c = c.toUpperCase().trim();

					List<Materials> list;
					Materials ma = new Materials();

					ma.setColor(c);
					ma.setGoodsComposed(StringUtils.defaultIfBlank(goodsComposed, "전체"));
					ma.setMaterials(materials);
					ma.setRatio(100F);

					if (!repo.containsKey(c)) {
						list = new ArrayList<>();
						repo.put(c, list);
					} else {
						list = repo.get(c);
					}
					list.add(ma);
				}
			}
		}
	}

	/**
	 * 상품평정보 수집을 위한 HTML을 파싱처리한다.
	 * 
	 * @param content
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	@Logging
	public Map<String, Object> parseComments(String content, ProductDetail productDetail) throws Exception {

		Map<String, Object> map = new HashMap<>();
		Document doc = Jsoup.parse(content);

		for (Element e : doc.select("table#goodsAssessmentTable > tbody > tr[id^='reviewtit']")) {
			Float goodsRating = Float.parseFloat(text(e.select("> td.txt02"), false).replaceAll("\\D", "")); // 평점
			String goodsComment = text(e.nextElementSibling().select("div.reTxt"), false); // 내용
			// String b = text(e.select("> td.txt04"), false).replaceAll("\\(구매자\\)$", ""); // 평점등록자
			// String c = text(e.select("> td.txt05"), false).replaceAll("\\D", ""); // 평점등록일자

			Comment comment = new Comment();

			comment.setGoodsRating(goodsRating);
			comment.setGoodsComment(goodsComment);

			productDetail.addComment(comment);
		}

		map.put(OnlineStoreConst.KEY_LAST_PAGE, true);
		Element navi = doc.select("div.pageNavi_bottom > div.pageShift > strong").first();

		if (navi != null) {
			Element e = navi.nextElementSibling();

			if (e != null) {
				String url = StringEscapeUtils.unescapeHtml(attr(e, "href"));
				url = url.replaceAll("^javascript:_moveAjaxGoodsAssessment\\('([^']+)'\\)$", "$1");

				if (!url.matches("^http[s]?:.+$"))
					url = "http://www.uniqlo.kr" + url;

				map.put(OnlineStoreConst.KEY_NEXT_URL, url);
				map.put(OnlineStoreConst.KEY_LAST_PAGE, false);
			}
		}
		return map;
	}
}
