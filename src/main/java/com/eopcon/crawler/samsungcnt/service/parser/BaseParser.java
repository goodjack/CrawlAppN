package com.eopcon.crawler.samsungcnt.service.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class BaseParser {

	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	protected Element previousElementSibling(Element element, String cssQuery, boolean recursive) {
		Element el = element;
		while ((el = el.previousElementSibling()) != null) {
			if (el.is(cssQuery)) {
				break;
			} else {
				if (recursive)
					if (el.select(cssQuery).size() > 0)
						break;
			}
		}
		return el;
	}

	protected Element nextElementSibling(Element element, String cssQuery, boolean recursive) {
		Element el = element;
		while ((el = el.nextElementSibling()) != null) {
			if (el.is(cssQuery)) {
				break;
			} else {
				if (recursive)
					if (el.select(cssQuery).size() > 0)
						break;
			}
		}
		return el;
	}

	/**
	 * HTML Rowspan이 있는 Table Element 를 List 형태로 반환
	 *
	 * @param rows
	 * @return
	 */
	protected List<Element[]> parseRowspanTableElement(Elements rows) {
		int rowIndex = 0;
		int columnCount = 0;
		Integer[] rowspans = null;

		List<Element[]> list = new ArrayList<>();
		List<Element> pre = new ArrayList<>();

		Integer[] comp = null;
		boolean merge = false;

		for (Element row : rows) {
			if (merge) { // 잘못된 HTML 보정용
				Elements el = row.previousElementSibling().select("th,td");
				row.insertChildren(0, el);

				int size = row.children().size();
				if (size != columnCount)
					throw new IllegalStateException("Column size is not matched!! -> expected : " + columnCount + ", actual : " + size);
				merge = false;
			}
			Elements col = row.select("th,td");

			if (col.size() == 0)
				continue;
			if (rowIndex == 0) {
				columnCount = col.size();
				rowspans = new Integer[columnCount];
				comp = new Integer[columnCount];
				Arrays.fill(comp, 1);
			} else {
				if (Arrays.deepEquals(comp, rowspans) && col.size() < columnCount) {
					merge = true;
					continue;
				}
			}

			Element[] elements = new Element[columnCount];

			int index = 0;
			for (int i = 0; i < columnCount; i++) {

				Element el;
				int rowspan;

				if (rowIndex == 0 || col.size() == columnCount || rowspans[i] == 1) {
					el = col.get(index);
					rowspan = Integer.parseInt(el.attr("rowspan") == null || el.attr("rowspan").length() == 0 ? "1" : el.attr("rowspan"));
					rowspans[i] = rowspan;

					if (rowIndex == 0)
						pre.add(el);
					else
						pre.set(i, el);
					index++;
				} else {
					rowspans[i] -= 1;
					el = pre.get(i);
				}
				elements[i] = el;
			}

			logger.debug("rowspans -> {}", StringUtils.join(rowspans));

			list.add(elements);
			rowIndex++;
		}
		return list;
	}

	/**
	 * HTML body정보만 추출 한다.
	 * 
	 * @param content
	 * @return
	 */
	protected String filterBody(String content) {
		String body = content;
		Pattern pattern = Pattern.compile("<body\\s*[^>]*>(.*)</body\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(body);
		if (matcher.find())
			body = matcher.group(1);
		return body;
	}

	/**
	 * Form Element 를 파싱 후 Map으로 변환
	 *
	 * @param content
	 * @param formSelector
	 * @return
	 */
	public Map<String, List<String>> parseFormParameter(String content, String formSelector) {
		return parseFormParameter(Jsoup.parse(content), formSelector);
	}

	/**
	 * Form Element 를 파싱 후 Map으로 변환
	 *
	 * @param doc
	 * @param formSelector
	 * @return
	 */
	protected Map<String, List<String>> parseFormParameter(Document doc, String formSelector) {
		try {
			Map<String, List<String>> params = new HashMap<>();

			Element form = doc.select(formSelector).first();

			if (form == null)
				throw new IllegalStateException("Parsing Error -> " + formSelector);

			for (Element field : form.select("input, select, textarea")) {
				String name = field.attr("name");
				String type = field.attr("type").toLowerCase();
				String value = null;

				if (StringUtils.isEmpty(name))
					continue;
				if ("submit".equals(type))
					continue;

				if ("checkbox".equals(type) || "radio".equals(type)) {
					if (field.attr("checked") != null) {
						value = field.attr("value");
					}
				} else {
					value = field.val();
				}

				if (StringUtils.isNotEmpty(value)) {
					List<String> param = params.get(name);

					if (param == null) {
						param = new ArrayList<>();
						params.put(name, param);
					}
					param.add(value);
				}
			}
			return params;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IllegalStateException("Parsing Error -> " + e.getMessage(), e);
		}
	}

	private Element cast(Object object) {
		Elements elements;
		Element element = null;

		if (object != null) {
			if (object instanceof Elements) {
				elements = (Elements) object;
				if (elements.size() > 0)
					element = elements.first();
			} else if (object instanceof Element) {
				element = (Element) object;
			} else {
				throw new IllegalArgumentException("Argument -> " + object.getClass().getName());
			}
		}
		return element;
	}

	protected String text(Object object, boolean html) {
		Element element = cast(object);
		String text = StringUtils.EMPTY;

		if (element != null) {
			if (html)
				text = element.html();
			else
				text = element.text();
		}
		text = text.replaceAll("^\u00A0", " "); // removing-non-breaking-spaces
		return text.trim();
	}

	protected String textNode(Object object, int index) {
		Element element = cast(object);
		String text = StringUtils.EMPTY;

		if (element != null) {
			List<TextNode> textNodes = element.textNodes();
			if (textNodes.size() > 0 && textNodes.size() >= index + 1) {
				TextNode textNode = textNodes.get(index);
				text = textNode.text();
			}
		}
		text = text.replaceAll("^\u00A0", " "); // removing-non-breaking-spaces
		return text.trim();
	}

	protected String attr(Object object, String attr) {
		Element element = cast(object);
		String text = StringUtils.EMPTY;

		if (element != null) {
			text = element.attr(attr);
			text = (text == null ? StringUtils.EMPTY : text);
		}
		text = text.replaceAll("^\u00A0", " "); // removing-non-breaking-spaces
		return text.trim();
	}
}
