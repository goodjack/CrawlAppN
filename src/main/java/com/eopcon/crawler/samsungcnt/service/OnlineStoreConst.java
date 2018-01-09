package com.eopcon.crawler.samsungcnt.service;

public enum OnlineStoreConst {

	SPAO, UNIQLO, HM, ZARA, LFMALL, MIXXO, _8S, GMARKET, DARKVICTORY, MIXXMIX, MUSINSA,HANDSOME ;

	public final static String SITE_SPAO = "SPAO";
	public final static String SITE_UNIQLO = "UNIQLO";
	public final static String SITE_HM = "HM";
	public final static String SITE_ZARA = "ZARA";
	public final static String SITE_LFMALL = "LFMALL";
	public final static String SITE_MIXXO = "MIXXO";
	public final static String SITE_HANDSOME = "HANDSOME";
	public final static String SITE_8S = "8S";
	
	public final static String SITE_URL_SPAO = "spao_elandmall_com";
	public final static String SITE_URL_UNIQLO = "www_uniqlo_kr";
	public final static String SITE_URL_HM = "www2_hm_com";
	public final static String SITE_URL_ZARA = "www_zara_com";
	public final static String SITE_URL_LFMALL = "LFMALL";
	public final static String SITE_URL_MIXXO = "mixxo_elandmall_com";
	public final static String SITE_URL_HANDSOME = "www_thehandsome_com";
	public final static String SITE_URL_8S = "8S";
	
	public final static String BRAND_SPAO = "SPAO";
	public final static String BRAND_UNIQLO = "UNIQLO";
	public final static String BRAND_HNM = "HM";
	public final static String BRAND_ZARA = "ZARA";
	public final static String BRAND_MIXXO = "MIXXO";
	public final static String BRAND_HANDSOME = "HANDSOME";
	public final static String BRAND_8S = "8S";

	public final static String CONFIG_MARKER_DIR = "/marker";
	public final static String CONFIG_CSV_DIR = "/csv";
	public final static String CONFIG_BACKUP_DIR = "/backup";
	public final static String CONFIG_INPUT_DIR = "/input";
	public final static String CONFIG_INPUT_QUEUE_DIR = "/input/0";
	public final static String CONFIG_INPUT_BAK_DIR = "/input/1";
	public final static String CONFIG_OUTPUT_DIR = "/output";

	public final static String CONFIG_BACKUP_CATEGORIES_FILE_NAME = "categories";
	public final static String CONFIG_BACKUP_BEST_ITEMS_FILE_NAME = "bestItems";

	public final static String CONFIG_PRODUCT_LIST_FILE_PREFIX = "plist_";
	public final static String CONFIG_PRODUCT_DETAIL_FILE_PREFIX = "product_";
	public final static String CONFIG_JOB_FINISED_FILE_NAME = "-1";
	public final static String CONFIG_PARSING_ERROR_FILE_PREFIX = "pasing_error_";

	public final static String CONFIG_CSV_ENCODING = "utf8";

	public final static String BEAN_NAME_ONLINE_STORE_CRAWLER = "onlineStoreCrawler";
	public final static String BEAN_NAME_ONLINE_STORE_PARSER = "onlineStoreParser";
	public final static String BEAN_NAME_CATEGORY_MAPPER = "categoryMapper";
	public final static String BEAN_NAME_SERVICE_CONFIG = "serviceConfig";

	public final static String LOGGER_NAME_COMMON = "commonLogger";
	public final static String LOGGER_NAME_BATCH = "batchLogger";
	public final static String VALIDATION_LOG = "validationLogger";

	public final static short LOG_ERROR_STEP_NONE = 0;
	public final static short LOG_ERROR_STEP_GET_DETAIL = 1;
	public final static short LOG_ERROR_STEP_WRITE_DB = 2;

	public final static String KEY_PRODUCT_LIST = "KEY_PRODUCT_LIST";
	public final static String KEY_NEXT_URL = "KEY_NEXT_URL";
	public final static String KEY_LAST_PAGE = "KEY_LAST_PAGE";
	public final static String KEY_TOTAL_COUNT = "KEY_TOTAL_COUNT";
	public final static String KEY_LOAD_COUNT = "KEY_LOAD_COUNT";
	public final static String KEY_PAY_LOAD = "KEY_PAY_LOAD";
	
	public final static String KEY_ORIGINAL_CATE_1 = "KEY_ORIGINAL_CATE_1";
	public final static String KEY_ORIGINAL_CATE_2 = "KEY_ORIGINAL_CATE_2";
	public final static String KEY_ORIGINAL_CATE_3 = "KEY_ORIGINAL_CATE_3";
	public final static String KEY_ORIGINAL_CATE_4 = "KEY_ORIGINAL_CATE_4";

	public final static String KEY_CATEGORY_MAPPING_YN = "KEY_CATEGORY_MAPPING_YN";

	public final static String KEY_MAPPING_CATE_1 = "KEY_MAPPING_CATE_1";
	public final static String KEY_MAPPING_CATE_2 = "KEY_MAPPING_CATE_2";
	public final static String KEY_MAPPING_CATE_3 = "KEY_MAPPING_CATE_3";
	public final static String KEY_MAPPING_CATE_4 = "KEY_MAPPING_CATE_4";

	public final static String KEY_COLLECT_URL = "KEY_COLLECT_URL";
	public final static String KEY_LAST_COLLECT_DAY = "KEY_LAST_COLLECT_DAY";
	public final static String KEY_LAST_PRICE_COLLECT_DAY = "KEY_LAST_PRICE_COLLECT_DAY";
	public final static String KEY_LAST_PRICE = "KEY_LAST_PRICE";
	public final static String KEY_RELEASE_DAY = "KEY_RELEASE_DAY";
	public final static String KEY_COLLECT_COLORS = "KEY_COLLECT_COLORS";
	public final static String KEY_COLLECT_SIZES = "KEY_COLLECT_SIZES";
	public final static String KEY_STATUS = "KEY_STATUS";

	public final static String KEY_SITE = "SITE";
	public final static String KEY_CATE = "CATE";
	public final static String KEY_ORIGINAL_WORD = "ORIGINAL";
	public final static String KEY_MODIFIED_WORD = "MODIFIED";
	
	public final static String KEY_CONNECT_TIMEOUT = "KEY_CONNECT_TIMEOUT";
	public final static String KEY_READ_TIMEOUT = "KEY_READ_TIMEOUT";
	public final static String KEY_USE_PROXY = "KEY_USE_PROXY";

	public final static String WORD_CORRECTION_CATE_MATERIALS = "MATERIALS";
	public final static String WORD_CORRECTION_CATE_COLOR = "COLOR";

	public final static String JOB_NAME_CRAWLING = "productJob";
	
	public final static int ERROR_STOCK_SIZE_LIMIT = 10000;
	//spaoQueue
	public final static String SPAO_QUEUE_NAME = "spaoQueue";
	public final static String MIXXO_QUEUE_NAME = "mixxoQueue";
	public final static String HANSOME_QUEUE_NAME = "handsomeQueue";
	public final static String HM_QUEUE_NAME = "hmQueue";
	public final static String ZARA_QUEUE_NAME = "zaraQueue";
	public final static String UNIQLO_QUEUE_NAME = "uniqloQueue";

	public String getSite() {
		switch (this) {
		case SPAO:
			return OnlineStoreConst.SITE_SPAO;
		case UNIQLO:
			return OnlineStoreConst.SITE_UNIQLO;
		case HM:
			return OnlineStoreConst.SITE_HM;
		case ZARA:
			return OnlineStoreConst.SITE_ZARA;
		case LFMALL:
			return OnlineStoreConst.SITE_LFMALL;
		case MIXXO:
			return OnlineStoreConst.SITE_MIXXO;
		case HANDSOME:
			return OnlineStoreConst.SITE_HANDSOME;
		case _8S:
			return OnlineStoreConst.SITE_8S;
		case GMARKET:
			return "GMARKET";
		case DARKVICTORY :
			return "DARKVICTORY";
		case MIXXMIX :
			return "MIXXMIX";
		case MUSINSA :
			return "MUSINSA";	
		}
		return null;
	}
	
	public String getSiteUrl(String site) {
		switch (site) {
		case SITE_SPAO:
			return OnlineStoreConst.SITE_URL_SPAO;
		case SITE_UNIQLO:
			return OnlineStoreConst.SITE_URL_UNIQLO;
		case SITE_HM:
			return OnlineStoreConst.SITE_URL_HM;
		case SITE_ZARA:
			return OnlineStoreConst.SITE_URL_ZARA;
		case SITE_LFMALL:
			return OnlineStoreConst.SITE_URL_LFMALL;
		case SITE_MIXXO:
			return OnlineStoreConst.SITE_URL_MIXXO;
		case SITE_HANDSOME:
			return OnlineStoreConst.SITE_URL_HANDSOME;
		case SITE_8S:
			return OnlineStoreConst.SITE_URL_8S;
	
		}
		return null;
	}
}
