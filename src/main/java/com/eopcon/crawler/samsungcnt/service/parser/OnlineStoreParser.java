package com.eopcon.crawler.samsungcnt.service.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.dao.ProductDao;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

public abstract class OnlineStoreParser extends BaseParser implements InitializingBean {

	@Autowired
	protected Properties properties;
	@Autowired
	protected ScriptManager scriptManager;
	@Autowired
	protected ExceptionBuilder exceptionBuilder;
	@Autowired
	protected ProductDao productDao;

	protected OnlineStoreConst constant;

	private ThreadLocal<Map<String, Object>> local = new ThreadLocal<>();

	private Map<String, List<Map<String, String>>> wordCorrectionRules = null;
	private Map<String, Map<String, String>> mappingMaterials = null;

	public OnlineStoreParser(OnlineStoreConst constant) {
		this.constant = constant;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		wordCorrectionRules = getWordCorrectionRules();
		mappingMaterials = getMappingMaterials();
	}

	/**
	 * 소재 정보를 파싱한다.
	 * 
	 * @param content
	 * @param colors
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	public abstract List<Materials> parseMarterialsString(String content, String[] colors, ProductDetail productDetail) throws Exception;

	public OnlineStoreConst getType() {
		return constant;
	}

	/**
	 * 파싱에 필요한 메타정보를 반환한다.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <K> K getObject(String key, K defaultValue) {
		Map<String, Object> data = local.get();

		if (data == null) {
			data = new HashMap<>();
			local.set(data);
		}
		Object value = data.get(key);

		if (value == null)
			return defaultValue;
		return (K) value;
	}

	/**
	 * 파싱에 필요한 메타정보를 저장한다.
	 * 
	 * @param key
	 * @param value
	 */
	public void putObject(String key, Object value) {
		Map<String, Object> data = local.get();

		if (data == null) {
			data = new HashMap<>();
			local.set(data);
		}
		data.put(key, value);
	}

	/**
	 * 메타정보를 삭제한다.
	 * 
	 * @param keys
	 */
	public void removeObject(String... keys) {
		Map<String, Object> data = local.get();

		if (data != null) {
			for (String key : keys)
				data.remove(key);
		}
	}

	/**
	 * 기 정의된 소재 매핑정보를 반환한다.
	 * 
	 * @return
	 */
	protected Map<String, Map<String, String>> getMappingMaterials() {
		CSVReader reader = null;
		InputStream in = null;
		Map<String, Map<String, String>> data = new HashMap<>();

		try {
			in = getClass().getResourceAsStream("/assets/materials_mapping.csv");
			reader = new CSVReader(new InputStreamReader(in, OnlineStoreConst.CONFIG_CSV_ENCODING), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.NULL_CHARACTER, 1);

			String[] s;
			while ((s = reader.readNext()) != null) {
				String site = s[0];
				String onlineGoodsNum = s[1];
				String original = s[2];
				String modified = s[3];

				if (site.equals(constant.getSite())) {
					Map<String, String> map = data.get(onlineGoodsNum);

					if (map == null) {
						map = new HashMap<>();
						data.put(onlineGoodsNum, map);
					}

					map.put(OnlineStoreConst.KEY_ORIGINAL_WORD, original);
					map.put(OnlineStoreConst.KEY_MODIFIED_WORD, modified);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionBuilder.raiseException(e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
		}
		return data;
	}

	/**
	 * 기 정의된 오타 매핑정보를 반환한다.
	 * 
	 * @return
	 */
	private Map<String, List<Map<String, String>>> getWordCorrectionRules() {
		CSVReader reader = null;
		InputStream in = null;

		Map<String, List<Map<String, String>>> data = new HashMap<>();

		try {
			in = getClass().getResourceAsStream("/assets/word_correction.csv");
			reader = new CSVReader(new InputStreamReader(in, OnlineStoreConst.CONFIG_CSV_ENCODING), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.NULL_CHARACTER, 1);

			String[] s;
			while ((s = reader.readNext()) != null) {
				String site = s[0];
				String cate = StringUtils.join(new String[] { s[1], s[2] }, "; ");
				String original = s[3];
				String modified = s[4];

				if (site.equals(constant.getSite()) || site.equals("ALL")) {
					List<Map<String, String>> list = data.get(cate);

					if (list == null) {
						list = new ArrayList<>();
						data.put(cate, list);
					}

					Map<String, String> map = new HashMap<>();

					map.put(OnlineStoreConst.KEY_CATE, cate);
					map.put(OnlineStoreConst.KEY_ORIGINAL_WORD, original);
					map.put(OnlineStoreConst.KEY_MODIFIED_WORD, modified);

					list.add(map);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionBuilder.raiseException(e);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(reader);
		}
		return data;
	}

	/**
	 * 기존 소재 정보를 기 정의된 소재 맵핑정보로 변경한다.
	 * 
	 * @param onlineGoodsNum
	 * @param content
	 * @return
	 */
	public String replaceMarterialsString(String onlineGoodsNum, String content) {
		Map<String, String> mapping = mappingMaterials.get(onlineGoodsNum);

		if (mapping != null) {
			String original = mapping.get(OnlineStoreConst.KEY_ORIGINAL_WORD);
			String modified = mapping.get(OnlineStoreConst.KEY_MODIFIED_WORD);

			if (content.equals(original)) {
				content = modified;

				if (logger.isDebugEnabled())
					logger.debug("# Marterials string is replaced -> original : {}, modified : {}", original, modified);
			}
		}
		return content;
	}

	/**
	 * 특정 단어를 기 정의된 오타 맵핑 단어로 변경한다.(정규식 패턴)
	 * 
	 * @param cate1
	 * @param cate2
	 * @param content
	 * @return
	 */
	public String replaceByRules(String cate1, String cate2, String content) {
		String cate = StringUtils.join(new String[] { cate1, cate2 }, "; ");
		String tempContent = content;

		List<Map<String, String>> list = wordCorrectionRules.get(cate);

		if (list != null) {
			for (Map<String, String> map : list) {
				String original = map.get(OnlineStoreConst.KEY_ORIGINAL_WORD);
				String modified = map.get(OnlineStoreConst.KEY_MODIFIED_WORD);

				String temp = tempContent.replaceAll(StringEscapeUtils.unescapeJava(original), modified);

				if (logger.isDebugEnabled() && !temp.equals(tempContent))
					logger.debug("# Replace String -> Category : {}, Before : {}, Regexp : {}, Replace : {}, After : {}", cate, content, original, modified, temp);

				tempContent = temp;
			}
		}
		return tempContent;
	}
}
