package com.eopcon.crawler.samsungcnt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.service.dao.ProductDao;

public class CategoryMapper implements InitializingBean {

	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	@Autowired
	protected ExceptionBuilder exceptionBuilder;
	@Autowired
	protected ProductDao productDao;

	protected OnlineStoreConst constant;

	private Map<String, Object> mappingCategories = null;
	private Map<String, Object> etcCategories = null;
	private List<String> ignoreMappingCategories = null;

	public CategoryMapper(OnlineStoreConst constant) {
		this.constant = constant;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("==============================CategoryMapper======================================");
		mappingCategories = productDao.getMappingCategories(constant.getSite());
		logger.debug("# categoryMapper -> {}", mappingCategories.keySet().toString());
		etcCategories = productDao.getEtcMappingCategories();
		ignoreMappingCategories = productDao.getIgnoreMappingCategories(constant.getSite());
		
	}

	/**
	 * 카테고리 맵핑을 수행한다.
	 * 
	 * @param meta
	 * @param categories
	 */
	@SuppressWarnings("unchecked")
	public void mappingCategory(Map<String, String> meta, List<Category> categories) {
		try {
			String[] mapping = null;
			List<Object[]> candidates = new ArrayList<>();
			logger.debug("============================mappingCategory==================================");
			logger.debug("categories : " + categories.size());
			logger.debug("meta : " + meta.size());
			for (int n = 0; n < categories.size(); n++) {
				Category category = categories.get(n);
				
//				if (logger.isDebugEnabled())
					logger.debug("# Categories -> {} | Product URL -> {}", StringUtils.join(category.getCategoryNames(), "; "), category.getProductUrl());
			
				int i = 0;
				boolean success = false;
				List<String> categoryCodes = new ArrayList<>();
				List<String> categoryNames = category.getCategoryNames();
				logger.debug("categoryNames : " + categoryNames.size());
				logger.debug("categoryCodes : " + categoryCodes.size());
				Map<String, Object> parent = mappingCategories;

				for (String categoryName : categoryNames) {
					Object object = parent.get("_child=" + categoryName);
					if (object == null)
						object = parent.get("_value=" + categoryName);

					if (object instanceof Map) {
						String categoryCode = (String) parent.get("_name=" + categoryName);
						categoryCodes.add(categoryCode);

						parent = (Map<String, Object>) object;
						i++;
					} else if (object instanceof String[]) {
						mapping = (String[]) object;
						break;
					} else {
						Set<String> code = new HashSet<>();
						for (Entry<String, Object> entry : parent.entrySet()) {
							String key = entry.getKey();
							if (key.startsWith("_value=")) {
								String[] temp = (String[]) entry.getValue();
								String categoryCode = temp[i];
								code.add(categoryCode);
							}
						}

						if (code.size() == 1) {
							String categoryCode = code.iterator().next();
							categoryCodes.add(categoryCode);
							i++;
						}
						break;
					}
				}
				
				success = mapping != null;
				
				// 맵핑되는 카테고리가 존재 하지 않을 경우
				if (mapping == null) {
					String categoryCode = null;
					List<String> ignores = ignoreMappingCategories;
					if (ignores == null)
						ignores = Collections.EMPTY_LIST;

					switch (i) {
					case 1:
					case 2:
					case 3:
						int from = i;
						if (i == 3 && i + 1 <= categoryNames.size()) {
							List<String> temp = new ArrayList<>(categoryNames);
							for (int j = i; j < temp.size(); j++)
								temp.remove(j--);

							if (ignores.contains(StringUtils.join(temp, "; ")))
								from = 1;
						}

						for (int k = from; k > 0; k--) {
							parent = (Map<String, Object>) etcCategories.get("_child=" + String.valueOf(k));

							for (int j = 0; j < categoryCodes.size(); j++) {
								categoryCode = categoryCodes.get(j);
								Object object = parent.get("_child=" + categoryCode);
								if (object == null)
									object = parent.get("_value=" + categoryCode);

								if (object instanceof Map) {
									parent = (Map<String, Object>) object;
								} else if (object instanceof String[]) {
									mapping = (String[]) object;
									break;
								} else {
									break;
								}
							}

							if (mapping != null)
								break;
						}
						break;
					}
				}
				
				if(mapping != null) {
					if(success) {
						candidates.clear();
						candidates.add(new Object[]{n, success, mapping});
						break;
					} else {
						candidates.add(new Object[]{n, success, mapping});
					}
				}
				
				mapping = null;
			}
			
			int n = 0;
			boolean success = false;
					
			if(candidates.size() > 0) {
				Object[] object = candidates.get(0);
				n = (Integer) object[0];
				success = (Boolean) object[1];
				mapping = (String[]) object[2];
			} else {
				mapping = null;
			}
			
			Category category = categories.get(n);
			String productUrl = category.getProductUrl();
			
			int i = 0;
			for (String categoryName : category.getCategoryNames()) {
				switch (i) {
				case 0:
					meta.put(OnlineStoreConst.KEY_ORIGINAL_CATE_1, categoryName);
					break;
				case 1:
					meta.put(OnlineStoreConst.KEY_ORIGINAL_CATE_2, categoryName);
					break;
				case 2:
					meta.put(OnlineStoreConst.KEY_ORIGINAL_CATE_3, categoryName);
					break;
				case 3:
					meta.put(OnlineStoreConst.KEY_ORIGINAL_CATE_4, categoryName);
					break;
				}
				i++;
			}
			meta.put(OnlineStoreConst.KEY_CATEGORY_MAPPING_YN, String.valueOf(success));
			
			
			if (mapping != null) {
				String mappingCate1 = StringUtils.defaultString(mapping[0]);
				String mappingCate2 = StringUtils.defaultString(mapping[1]);
				String mappingCate3 = StringUtils.defaultString(mapping[2]);
				String mappingCate4 = StringUtils.defaultString(mapping[3]);

				if (logger.isDebugEnabled())
					logger.debug(getClass().getSimpleName(),
							String.format("# Mapping Categories -> %s, %s, %s, %s", mappingCate1, mappingCate2, mappingCate3, mappingCate4));

				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_1, mappingCate1);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_2, mappingCate2);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_3, mappingCate3);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_4, mappingCate4);
				meta.put(OnlineStoreConst.KEY_COLLECT_URL, productUrl);
				meta.put(OnlineStoreConst.KEY_STATUS, String.valueOf(1));
			} else {
				logger.warn(getClass().getSimpleName(),
						"Category Mapping Fail!!! => Original Category -> " + categories.toString());
				
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_1, StringUtils.EMPTY);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_2, StringUtils.EMPTY);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_3, StringUtils.EMPTY);
				meta.put(OnlineStoreConst.KEY_MAPPING_CATE_4, StringUtils.EMPTY);
				meta.put(OnlineStoreConst.KEY_COLLECT_URL, productUrl);
				meta.put(OnlineStoreConst.KEY_STATUS, String.valueOf(0));
			}
		} catch (Exception e) {
			logger.error("Mapping Fail!! -> " + categories);
			exceptionBuilder.raiseException(ErrorType.ERROR_CATEGORY_MAPPING_FAIL, e);
		}
	}
}
