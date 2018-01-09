package com.eopcon.crawler.samsungcnt.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.eopcon.crawler.samsungcnt.exception.BizException;
import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.Comment;
import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.model.Goods;
import com.eopcon.crawler.samsungcnt.model.LogDetail;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Sku;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.Marker.Mark;
import com.eopcon.crawler.samsungcnt.service.dao.Lock;
import com.eopcon.crawler.samsungcnt.service.dao.ProductDao;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.Assertion;
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.eopcon.crawler.samsungcnt.util.DateUtils;
import com.eopcon.crawler.samsungcnt.util.SerializationUtils;
import com.ssfashion.bigdata.crawler.file.aws.RawDataFileManagement;
import com.ssfashion.bigdata.crawler.file.rawdata.WriteJsonObjectLocal;
import com.ssfashion.bigdata.crawler.util.DateUtil;

public abstract class OnlineStoreCrawler implements InitializingBean, DisposableBean {

	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);
	
	protected static int goodsInputCount = 0;
	protected static int goodsOutputCount = 0;
	protected final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";

	@Autowired
	private ApplicationContext context;
	@Autowired
	protected Properties properties;
	@Autowired
	protected HttpRequestService request;
	@Autowired
	protected ExceptionBuilder exceptionBuilder;
	@Autowired
	protected Assertion assertion;
	@Autowired
	protected ProductDao productDao;
	@Autowired
	protected Lock lock;
	@Autowired(required = false)
	private ImageUploader uploader;
	// 카테고리 매핑 클래스 객체
	private String site;
	protected ServiceConfig config;
	protected OnlineStoreConst constant;
	protected RawDataFileManagement rwaDataFileMgr;
	protected OnlineStoreParser parser;
	private CategoryMapper categoryMapper;
	private ArrayBlockingQueue<Map<String, Object>> queue;
	private Marker marker;
	// BY.HNC =========================================== START
	public static WriteJsonObjectLocal writerSpao;
	public static WriteJsonObjectLocal writerMixxo;
	public static WriteJsonObjectLocal writerHandsome;
	public static WriteJsonObjectLocal writerZara;
	public static WriteJsonObjectLocal writerUniqlo;
	public static WriteJsonObjectLocal writerHm;
	private boolean interrupted = false;
	

	public OnlineStoreCrawler(ServiceConfig config, OnlineStoreConst constant) {
		this.config = config;
		this.constant = constant;
		marker = new Marker(config.getMarkerDirectory());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		logger.debug("config.getMarkerDirectory() = " + config.getMarkerDirectory());
		
		parser = (OnlineStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, constant);
		categoryMapper = (CategoryMapper) context.getBean(OnlineStoreConst.BEAN_NAME_CATEGORY_MAPPER, constant);

		rwaDataFileMgr = new RawDataFileManagement(constant.getSite());

		// BY.HNC =========================================== START
		site = constant.getSite();
		
		switch (site) {
		case OnlineStoreConst.SITE_SPAO:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.SPAO_QUEUE_NAME);//, constant);
			break;
		case OnlineStoreConst.SITE_MIXXO:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.MIXXO_QUEUE_NAME);//, constant);
			break;
		case OnlineStoreConst.SITE_HANDSOME:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.HANSOME_QUEUE_NAME);//, constant);
			break;
		case OnlineStoreConst.SITE_HM:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.HM_QUEUE_NAME);//, constant);
			break;
		case OnlineStoreConst.SITE_UNIQLO:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.UNIQLO_QUEUE_NAME);//, constant);
			break;
		case OnlineStoreConst.SITE_ZARA:
			queue = (ArrayBlockingQueue<Map<String, Object>>) context.getBean(OnlineStoreConst.ZARA_QUEUE_NAME);//, constant);
			break;
		}

	}

	@Override
	public void destroy() throws Exception {
		marker.destroy();
	}

	/**
	 * 작업을 중단한다.
	 */
	public void interrupt() {
		interrupted = true;
		request.interrupt();
	}

	public void addRequestConfig(String key, Object value) {
		request.addRequestConfig(key, value);
	}

	/**
	 * 상품목록을 수집한다. ※ 수집정보 -> 카테고리 목록, 베스트아이템 목록, 상품목록
	 * 
	 * @param jobExecutionId
	 * @return 상품목록 건수
	 * @throws Exception
	 */
	public int queryProductList(Long jobExecutionId) throws Exception {

		final int DELAY = 500;
		int count = 0;

		try {
			
			System.out.println("execute2");
			Mark mark = marker.getLastMark_new();
			System.out.println("mark 생성");
			if (!marker.isFinished()) {
				System.out.println("mark 생성1");
				int i = 0; // 0 7
				int page = 1;
				int size = 0;

				List<Category> categories = null;
				List<String> bestItems = null;
				if (mark == null) {

					mark = new Mark();
					//System.out.println("1");
					categories = getCategories();
				//	System.out.println("2");
					bestItems = getBestItems();

					backup(categories, OnlineStoreConst.CONFIG_BACKUP_CATEGORIES_FILE_NAME);
					backup(bestItems, OnlineStoreConst.CONFIG_BACKUP_BEST_ITEMS_FILE_NAME);
				} else {

					categories = resotre(OnlineStoreConst.CONFIG_BACKUP_CATEGORIES_FILE_NAME);

					i = mark.getCategoryNumber();
					page = mark.getPage();

					if (size == 0) {
						i++;
						page = 1;
					}
				}

				// 상품 페이지
				//System.out.println("categories.size() --> " +  categories.size());
				for (; i < categories.size(); i++) { // categories.size() 8
					System.out.println("mark 생성2");
					Category category = categories.get(i);

					while (true) {
						throwIfInterrupted();
						List<Product> list = getProductList(category, page);
						// list.get(page).getDataStandard().getPageSize();

						size = list.size();

						mark.setCategoryNumber(i);
						mark.setPage(page);
						mark.setSize(size);

						if (size == 0) {
							marker.mark(mark, category);
							break;
						}

						for (Product p : list) {
							if (!serialize(p)) {
								writeLogBackup(jobExecutionId, p);
								count += 1;

								if (logger.isDebugEnabled())
									logger.debug("# Product -> {}", p);
							}
						}

						marker.mark(mark, category);
						page++;

						Thread.sleep(DELAY);
					}
					page = 1;
				}
				marker.finish();
				finish();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return count;
	}

	/**
	 * 마스터 테이블에 반영이 안된 온라인 아이템 목록을 가져온다.
	 * 
	 * @param jobExecutionId
	 * @param id
	 * @param pageSize
	 * @return
	 */
	public List<LogDetail> getNotAppliedGoodsLogs(Long jobExecutionId, long id, int pageSize) {
		return productDao.getNotAppliedGoodsLogs(jobExecutionId, constant.getSite(), config.getCollectDayString(), id, pageSize);
	}

	/**
	 * 실패한 온라인 아이템 건수를 반환한다.
	 * 
	 * @return
	 */
	public Long getGoodsLogFailCount() {
		return productDao.getGoodsLogFailCount(constant.getSite(), config.getCollectDayString());
	}

	/**
	 * 크롤링에 필요한 정보를 반영한다.
	 * 
	 * @param jobExecutionId
	 * @param p
	 */
	private void writeLogBackup(Long jobExecutionId, Product p) {
		productDao.updateGoodsLogBackup(jobExecutionId, constant.getSite(), p.getOnlineGoodsNum(), config.getCollectDayString(), p.getBackupFile());
	}

	/**
	 * 크롤링을 수행한다.
	 * 
	 * @param jobExecutionId
	 * @param product
	 * @param productDetail
	 * @throws Exception
	 */
/*	public void writeProductDetail(Long jobExecutionId, Product product, ProductDetail productDetail ) throws Exception {
		writeProductDetail(jobExecutionId, OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL, product, productDetail);
	}*/

	/**
	 * 크롤링을 수행한다.
	 * 
	 * @param jobExecutionId
	 * @param product
	 * @param productDetail
	 * @throws Exception
	 */
	public void writeProductDetail_new(Long jobExecutionId, Product product, ProductDetail productDetail ,int retryCount) throws Exception {
		writeProductDetail_new(jobExecutionId, OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL, product, productDetail,retryCount);
	}
	/**
	 * 크롤링을 수행한다.
	 * 
	 * @param jobExecutionId
	 * @param errorStep
	 * @param product
	 * @param productDetail
	 * @throws Exception
	 *//*
	public void writeProductDetail(Long jobExecutionId, short errorStep, Product product, ProductDetail productDetail) throws Exception {
		throwIfInterrupted();
		String site = constant.getSite();
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();
		LogDetail logDetail = productDao.getGoodsLog(site, onlineGoodsNum);
		String collectDay = config.getCollectDayString();

		productDetail.setSite(site);

		if (logDetail == null) {
			logDetail = new LogDetail();
			logDetail.setSite(site);
			logDetail.setOnlineGoodsNum(onlineGoodsNum);
		}

		logDetail.setLastCollectDay(collectDay);
		logDetail.setJobExecutionId(jobExecutionId);

		Map<String, String> meta = new HashMap<>();

		try {
			if (!"HANDSOME".equals(site)) {
				// 카테고리 맵핑
				categoryMapper.mappingCategory(meta, productDetail.getCategories());
			}
			// 제품 상세 정보 요청
			if (errorStep == OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL) {
				File inputFile = product.getBackupFile();
				logDetail.setVal10(inputFile.getAbsolutePath());

				String collectURL = meta.get(OnlineStoreConst.KEY_COLLECT_URL);
				productDetail.setCollectURL(collectURL);
				// 물품상세 정보 요청
				fillOutProductDetail(collectURL, productDetail);
				// 유효성체크 수행
				if (productDetail.getDataStandard() == null) {
					assertion.assertNotEmpty(constant, productDetail);
				}
				// 백업파일 생성
				File outputFile = serialize(onlineGoodsNum, productDetail);
				productDetail.setBackupFile(outputFile);

				if (logger.isDebugEnabled())
					logger.debug("# ProductDetail -> {}", productDetail);

				errorStep = OnlineStoreConst.LOG_ERROR_STEP_WRITE_DB;
			}
			// DB작업 수행
			if (errorStep == OnlineStoreConst.LOG_ERROR_STEP_WRITE_DB || errorStep == OnlineStoreConst.LOG_ERROR_STEP_NONE) {
				fillOutLogDetail(meta, logDetail, productDetail);
				if("ZARA".equals(site)) {
					writeGoods(meta, logDetail, convertObject_new(meta, productDetail));
				}
				else if (!"HANDSOME".equals(site)) {
					writeGoods_new(meta, logDetail, convertObject_new(meta, productDetail));
				} else {
					writeGoods_handsome(meta, logDetail, productDetail);
				}
				errorStep = OnlineStoreConst.LOG_ERROR_STEP_NONE;
			}
			writeLog(errorStep, logDetail, null);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			BizException exception = exceptionBuilder.buildException(e);
			writeLog(errorStep, logDetail, exception);
			throw e;
		}
	}*/
	
	/**
	 * 크롤링을 수행한다.
	 * 
	 * @param jobExecutionId
	 * @param errorStep
	 * @param product
	 * @param productDetail
	 * @throws Exception
	 */
	public void writeProductDetail_new(Long jobExecutionId, short errorStep, Product product, ProductDetail productDetail , int retryCount) throws Exception {
		throwIfInterrupted();
		String site = constant.getSite();
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();
		LogDetail logDetail = productDao.getGoodsLog(site, onlineGoodsNum);
		String collectDay = config.getCollectDayString();

		productDetail.setSite(site);

		if (logDetail == null) {
			logDetail = new LogDetail();
			logDetail.setSite(site);
			logDetail.setOnlineGoodsNum(onlineGoodsNum);
		}

		logDetail.setLastCollectDay(collectDay);
		logDetail.setJobExecutionId(jobExecutionId);

		Map<String, String> meta = new HashMap<>();

		try {
			if (!"HANDSOME".equals(site)) {
				// 카테고리 맵핑
				categoryMapper.mappingCategory(meta, productDetail.getCategories());
			}
			// 제품 상세 정보 요청
			if (errorStep == OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL) {
				File inputFile = product.getBackupFile();
				logDetail.setVal10(inputFile.getAbsolutePath());

				String collectURL = meta.get(OnlineStoreConst.KEY_COLLECT_URL);
				productDetail.setCollectURL(collectURL);
				// 물품상세 정보 요청
				fillOutProductDetail(collectURL, productDetail);
				// 유효성체크 수행
				if (productDetail.getDataStandard() == null) {
					assertion.assertNotEmpty(constant, productDetail);
				}
				// 백업파일 생성
				File outputFile = serialize(onlineGoodsNum, productDetail);
				productDetail.setBackupFile(outputFile);

				if (logger.isDebugEnabled())
					logger.debug("# ProductDetail -> {}", productDetail);

				errorStep = OnlineStoreConst.LOG_ERROR_STEP_WRITE_DB;
			}
			// DB작업 수행
			if (errorStep == OnlineStoreConst.LOG_ERROR_STEP_WRITE_DB || errorStep == OnlineStoreConst.LOG_ERROR_STEP_NONE) {
				fillOutLogDetail(meta, logDetail, productDetail);
				if("ZARA".equals(site)) {
					//writeGoods_zara(meta, logDetail, convertObject_new(meta, productDetail),retryCount);
					writeGoods(meta, logDetail, convertObject(meta, productDetail));
				}
				else if (!"HANDSOME".equals(site)) {
					writeGoods_new(meta, logDetail, convertObject_new(meta, productDetail),retryCount);
				} else {
					writeGoods_handsome(meta, logDetail, productDetail,retryCount);
				}
				errorStep = OnlineStoreConst.LOG_ERROR_STEP_NONE;
			}
			writeLog(errorStep, logDetail, null);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			BizException exception = exceptionBuilder.buildException(e);
			writeLog(errorStep, logDetail, exception);
			throw e;
		}
	}

	/**
	 * 로그 정보를 작성한다.
	 * 
	 * @param meta
	 * @param logDetail
	 * @param productDetail
	 */
	private void fillOutLogDetail(Map<String, String> meta, LogDetail logDetail, ProductDetail productDetail) {

		Long id = logDetail.getId();

		String site = productDetail.getSite();
		String onlineGoodsNum = productDetail.getOnlineGoodsNum();
		String collectDay = config.getCollectDayString();
		String collectURL = productDetail.getCollectURL();

		String goodsNum = productDetail.getGoodsNum();
		String goodsMaterials = productDetail.getGoodsMaterials();
		Integer price = productDetail.getPrice();
		File backupFile = productDetail.getBackupFile();
		String category = StringUtils.EMPTY;

		List<Stock> stocks = productDetail.getStocks();
		Set<String> colors = new HashSet<>();

		for (Stock stock : stocks)
			colors.add(stock.getColor());

		final int LIMIT = 500;

		if (id == null) {
			String val4 = String.format("%s=%s;", collectDay, price); // 정가이력(날짜,가격)
			String val5 = String.format("%s;", collectDay); // 수집이력
			String val6 = String.format("%s;", StringUtils.join(colors, ";")); // 컬러

			logDetail.setSite(site);
			logDetail.setOnlineGoodsNum(onlineGoodsNum);
			logDetail.setVal1(collectDay);
			logDetail.setVal4(val4);
			logDetail.setVal5(val5);
			logDetail.setVal6(val6);

			meta.put(OnlineStoreConst.KEY_COLLECT_COLORS, val6);
			meta.put(OnlineStoreConst.KEY_LAST_PRICE_COLLECT_DAY, "0");
			meta.put(OnlineStoreConst.KEY_LAST_PRICE, String.valueOf(price));
			meta.put(OnlineStoreConst.KEY_LAST_COLLECT_DAY, collectDay);
		} else {
			String val1 = logDetail.getVal1(); // 최초 등록일
			String val4 = logDetail.getVal4(); // 정가이력(날짜,가격)
			String val5 = logDetail.getVal5(); // 수집이력
			String val6 = logDetail.getVal6(); // 컬러

			if (StringUtils.isEmpty(val1)) {
				logDetail.setVal1(collectDay);
			}
			if (StringUtils.isEmpty(val4)) {
				val4 = String.format("%s=%s;", collectDay, price); // 정가이력(날짜,가격)
				logDetail.setVal4(val4);
			}
			if (StringUtils.isEmpty(val5)) {
				val5 = String.format("%s;", collectDay); // 수집이력
				logDetail.setVal5(val5);
			}
			if (StringUtils.isEmpty(val6)) {
				val6 = String.format("%s;", StringUtils.join(colors, ";")); // 색상이력
				logDetail.setVal6(val6);
			}

			val4 = val4.replaceAll(collectDay + "=[^;]+;", "").replaceAll("^([^=]+)=([^;]+).*$", "$1,$2");
			val5 = val5.replaceAll("^([^;]*).*$", "$1");

			String lastPriceCollectDay = "0";
			int lastPrice = price;
			String lastCollectDay = val5;

			if (StringUtils.isNotEmpty(val4)) {
				String[] temp = val4.split(",");
				lastPriceCollectDay = temp[0];
				lastPrice = Integer.parseInt(temp[1]);

				if (collectDay.compareTo(lastPriceCollectDay) > 0 && price != lastPrice) {
					val4 = String.format("%s=%s;%s", collectDay, price, logDetail.getVal4().replaceAll(collectDay + "=[^;]+;", ""));
					if (val4.length() > LIMIT)
						val4 = val4.replaceAll("^(.*);[^=]+=[^;]+;$", "$1;");
					logDetail.setVal4(val4);
				}
			}

			if (collectDay.compareTo(lastCollectDay) > 0) {
				val5 = String.format("%s;%s", collectDay, logDetail.getVal5());
				if (val5.length() > LIMIT)
					val5 = val5.replaceAll("^(.*);([^;]*);$", "$1;");
				logDetail.setVal5(val5);
			}

			List<String> beforeColors = Arrays.asList(val6.split(";"));
			colors.addAll(beforeColors);

			val6 = String.format("%s;", StringUtils.join(colors, ";"));
			logDetail.setVal6(val6);

			meta.put(OnlineStoreConst.KEY_COLLECT_COLORS, val6);
			meta.put(OnlineStoreConst.KEY_LAST_PRICE_COLLECT_DAY, lastPriceCollectDay);
			meta.put(OnlineStoreConst.KEY_LAST_PRICE, String.valueOf(lastPrice));
			meta.put(OnlineStoreConst.KEY_LAST_COLLECT_DAY, lastCollectDay);
		}

		String releaseDay = logDetail.getVal1();
		meta.put(OnlineStoreConst.KEY_RELEASE_DAY, releaseDay);

		List<Category> categories = productDetail.getCategories();

		for (Category c : categories)
			category += "|" + StringUtils.join(c.getCategoryNames(), "; ");

		logDetail.setGoodsNum(goodsNum);
		logDetail.setCollectURL(collectURL);
		logDetail.setVal2(category.substring(1)); // 카테고리(원본값)
		logDetail.setVal3(goodsMaterials); // 소재(원본값)
		logDetail.setVal7(collectURL); // 제품상세URL
		logDetail.setVal10(backupFile.getAbsolutePath());

		logDetail.setLastCollectDay(collectDay);
		logDetail.setErrorStep(OnlineStoreConst.LOG_ERROR_STEP_NONE);
		logDetail.setErrorNum((short) 0);
		logDetail.setErrorMessage(null);
		logDetail.setAppliedYn(true);
		logDetail.setLastCollectDay(collectDay);
	}

	/**
	 * DB에 반영하기위한 Object로 변환한다.
	 * 
	 * @param meta
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	public Goods convertObject(Map<String, String> meta, ProductDetail productDetail) throws Exception {
		Goods goods = new Goods();
		logger.error("convertObject 시작");
		logger.debug("convertObject 시작");
		String goodsNum = productDetail.getGoodsNum();
		String goodsCate1 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_1);
		String goodsCate2 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_2);
		String goodsCate3 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_3);
		String goodsCate4 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_4);
		String brandCode = productDetail.getBrandCode();
		Integer price = productDetail.getPrice();
		String releaseDt = meta.get(OnlineStoreConst.KEY_RELEASE_DAY);
		String maftOrigin = productDetail.getMaftOrigin();
		String site = productDetail.getSite();
		String collectDay = config.getCollectDayString();
		String lastCollectDay = meta.get(OnlineStoreConst.KEY_LAST_COLLECT_DAY);

		String goodsMaterials = productDetail.getGoodsMaterials();

		List<Materials> materials = productDetail.getMaterials();
		List<Stock> stocks = productDetail.getStocks();
		List<Comment> comments = productDetail.getComments();

		String[] colors = getColors(meta);

		// 색상 값 보정
		for (Stock s : stocks)
			s.setColor(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, s.getColor()));

		// 소재정보가 없으면 소재 문자열을 파싱 처리
		if (materials == null || materials.size() == 0) {
			materials = getGoodsMaterials(goodsMaterials, colors, productDetail);

			// 유효성체크 수행
			if (materials == null || materials.size() == 0)
				exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_MATERIALS_FAIL, new IllegalArgumentException("[Assertion failed] - [Materials] must not be empty: it must contain at least 1 element"));
			for (Materials m : materials) {
				if (logger.isDebugEnabled())
					logger.debug("# Materials -> {}", m);

				assertion.assertNotEmpty(constant, m);
				productDetail.addMaterials(m);
			}

		}

		// 색상 값 및 소재 값 보정
		for (Materials m : materials) {
			m.setColor(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, m.getColor()));
			m.setMaterials(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, m.getMaterials()));
		}
		// SKU정보 가져오기
		List<Sku> sku = getSkuInfomations(colors, productDetail);

		goods.setGoodsNum(goodsNum);
		goods.setGoodsCate1(goodsCate1);
		goods.setGoodsCate2(goodsCate2);
		goods.setGoodsCate3(goodsCate3);
		goods.setGoodsCate4(goodsCate4);
		goods.setBrandCode(brandCode);
		goods.setPrice(price);
		goods.setReleaseDt(DateUtils.parseDate(releaseDt));
		goods.setSellCloseDt(null);
		goods.setSellPrd(null);
		goods.setMaftOrigin(maftOrigin);
		goods.setSite(site);
		goods.setCollectDay(collectDay);
		goods.setLastCollectDay(lastCollectDay);

		goods.setSku(sku);
		goods.setComments(comments);

		return goods;
	}

	public Goods convertObject_new(Map<String, String> meta, ProductDetail productDetail) throws Exception {
		Goods goods = new Goods();
		
		goodsInputCount++;
		
		String goodsNum = productDetail.getGoodsNum();
		String goodsCate1 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_1);
		String goodsCate2 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_2);
		String goodsCate3 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_3);
		String goodsCate4 = meta.get(OnlineStoreConst.KEY_MAPPING_CATE_4);
		String brandCode = productDetail.getBrandCode();
		Integer price = productDetail.getPrice();
		String releaseDt = meta.get(OnlineStoreConst.KEY_RELEASE_DAY);
		String maftOrigin = productDetail.getMaftOrigin();
		String site = productDetail.getSite();
		String collectDay = config.getCollectDayString();
		String lastCollectDay = meta.get(OnlineStoreConst.KEY_LAST_COLLECT_DAY);

		String goodsMaterials = productDetail.getGoodsMaterials();

		Map<String, Object> coltItemMap = new HashMap<>();

		List<Materials> materials = productDetail.getMaterials();
		List<Stock> stocks = productDetail.getStocks();
		List<Comment> comments = productDetail.getComments();
		Map<String, String> productSize = new HashMap<>();
		List<String> arrProductSize = new ArrayList<>();
		String[] colors = getColors(meta);
		Map<String, Object> materialsInfo = new HashMap<>();
		String strMaterialsInfo = "";
		int totalStockCnt = 0;
		String stockStr = "";
		String discountPrice = "";
		List<Map<String, Object>> goodEvals = new ArrayList<>();

		// Comment
		if (comments != null) {
			comments.forEach(evalut -> {
				Map<String, Object> evalMap = new HashMap<>();
				evalMap.put("rating", evalut.getGoodsRating());
				evalMap.put("comment", evalut.getGoodsComment());
				goodEvals.add(evalMap);
			});
			coltItemMap.put("goodEval", goodEvals);
		} else {
			logger.debug("coltItemEvalut = ", "null");

			Map<String, Object> evalMap = null;
			goodEvals.add(evalMap);
			coltItemMap.put("goodEval", null);
		}

		// 색상 값 보정
		for (Stock s : stocks) {
			s.setColor(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, s.getColor()));

			String color = s.getColor();
			String size = s.getSize();
			int stockAmt = s.getStockAmount();

			totalStockCnt += stockAmt;

			stockStr += goodsNum + "_" + color + "_" + size + ":" + stockAmt + ",";
			if (productSize.containsKey(s.getColor())) {
				String strSize = productSize.get(s.getColor()) + "," + s.getSize();
				productSize.remove(s.getColor());
				productSize.put(s.getColor(), strSize);
			} else {
				productSize.put(s.getColor(), s.getColor() + ":" + s.getSize());
			}
		}

		stockStr = "totalStock:" + totalStockCnt + "||" + stockStr;
		stockStr = stockStr.length() > 1 ? stockStr.substring(0, stockStr.length() - 1) : "";

		// 소재정보가 없으면 소재 문자열을 파싱 처리
		if (materials == null || materials.size() == 0) {
			materials = getGoodsMaterials(goodsMaterials, colors, productDetail);

			// 유효성체크 수행
			if (materials == null || materials.size() == 0)
				exceptionBuilder.raiseException(ErrorType.ERROR_ASSERTION_MATERIALS_FAIL, new IllegalArgumentException("[Assertion failed] - [Materials] must not be empty: it must contain at least 1 element"));
			for (Materials m : materials) {
				if (logger.isDebugEnabled())
					logger.debug("# Materials -> {}", m);

				assertion.assertNotEmpty(constant, m);
				if (materialsInfo.containsKey(m.getGoodsComposed())) {
					String strMaterials = materialsInfo.get(m.getGoodsComposed()) + " " + m.getMaterials() + " " + m.getRatio() + "%";
					materialsInfo.remove(m.getGoodsComposed());
					materialsInfo.put(m.getGoodsComposed(), strMaterials);
				} else {
					materialsInfo.put(m.getGoodsComposed(), m.getGoodsComposed() + " " + m.getMaterials() + " " + m.getRatio() + "%");
				}

				productDetail.addMaterials(m);
			}

		}
		Set<String> materialsInfoKeys = materialsInfo.keySet();

		for (String key : materialsInfoKeys) {
			if (strMaterialsInfo.indexOf(materialsInfo.get(key).toString()) < 0) {
				strMaterialsInfo += materialsInfo.get(key) + " ";
			}
		}

		Set<String> productSizeKeys = productSize.keySet();

		for (String key : productSizeKeys) {
			arrProductSize.add(productSize.get(key));
		}

		// 색상 값 및 소재 값 보정
		for (Materials m : materials) {
			m.setColor(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, m.getColor()));
			m.setMaterials(parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, OnlineStoreConst.WORD_CORRECTION_CATE_MATERIALS, m.getMaterials()));
		}
		
		
		List<String> s3ImageUrls = new ArrayList<>();
		List<String> orgImageUrls = new ArrayList<>();
		List<Sku> sku = null;
		// SKU정보 가져오기
		if("ZARA".equals(site)) 
			sku = getSkuInfomations(colors, productDetail);
		else
			sku = getSkuInfomations_new(colors, productDetail);
	
		for (Sku skuInfo : sku) {
			s3ImageUrls.add(skuInfo.getS3ImageUrl());
			orgImageUrls.add(skuInfo.getGoodsImageOrg());
			if (skuInfo.getDiscountPrice() != null && skuInfo.getDiscountPrice() == 0) {
				discountPrice = String.valueOf(skuInfo.getDiscountPrice());
			} else {
				discountPrice = String.valueOf(skuInfo.getPrice());
			}
		}
		
		goods.setGoodsNum(goodsNum);
		goods.setGoodsCate1(goodsCate1);
		goods.setGoodsCate2(goodsCate2);
		goods.setGoodsCate3(goodsCate3);
		goods.setGoodsCate4(goodsCate4);
		goods.setBrandCode(brandCode);
		goods.setPrice(price);
		goods.setReleaseDt(DateUtils.parseDate(releaseDt));
		goods.setSellCloseDt(null);
		goods.setSellPrd(null);
		goods.setMaftOrigin(maftOrigin);
		goods.setSite(site);
		goods.setCollectDay(collectDay);
		goods.setLastCollectDay(lastCollectDay);
		goods.setSku(sku);
		goods.setComments(comments);
		
		// 크롤링 날짜 시분초 : 현재 시스템 시간으로 설정
		DateTime dateTime = DateUtil.getCurrentDateTime("yyyyMMddHHmmss");
		String crawlDate = DateUtil.getTodayStr(dateTime, "yyyyMMddHHmmss");

		// 카테고리 매핑
		//String[] cates = goods.getOrgCateName().split(";");
		String layer1 = null;
		String layer2 = null;
		String layer3 = null;
		String layer4 = null;
		String layer5 = null;
		String layer6 = null;

		coltItemMap.put("productCode", goods.getGoodsNum() != null ? goods.getGoodsNum() : null);
		coltItemMap.put("normalPrice", goods.getPrice() != null ? goods.getPrice() : null);
		coltItemMap.put("brandName", goods.getBrandCode() != null ? goods.getBrandCode() : null);
		coltItemMap.put("imageUrl", orgImageUrls != null ? orgImageUrls : null);
		coltItemMap.put("s3ImageUrl", s3ImageUrls != null ? s3ImageUrls : null);
		coltItemMap.put("productName", productDetail.getGoodsName() != null ? productDetail.getGoodsName() : null);
		coltItemMap.put("layer1", layer1);
		coltItemMap.put("layer2", layer2);
		coltItemMap.put("layer3", layer3);
		coltItemMap.put("layer4", layer4);
		coltItemMap.put("layer5", layer5);
		coltItemMap.put("layer6", layer6);
		coltItemMap.put("productColor", StringUtils.join(colors, ","));
		coltItemMap.put("productSize", arrProductSize.size() == 0 ? null : arrProductSize);
		coltItemMap.put("material", strMaterialsInfo != "" ? strMaterialsInfo : "");
		coltItemMap.put("origin", goods.getMaftOrigin() != null ? goods.getMaftOrigin() : null);
		coltItemMap.put("stockInfo", stockStr);
		coltItemMap.put("crawlDate", crawlDate);
		coltItemMap.put("crawlUrl", productDetail.getCollectURL() != null ? productDetail.getCollectURL() : null);
		coltItemMap.put("saleYn", discountPrice == null ? "N" : "Y");
		coltItemMap.put("salePrice", discountPrice);
		coltItemMap.put("newYn", null);
		coltItemMap.put("desc", null);
		coltItemMap.put("dependentData", null);

		goods.setColtItemMap(coltItemMap);
		strMaterialsInfo = "";
		goodsOutputCount ++;

		return goods;
		

	}
	/**
	 * SKU정보를 변환하여 가져온다.
	 * 
	 * @param colors
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	public List<Sku> getSkuInfomations_new(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();

		String goodsNum = productDetail.getGoodsNum();
		String goodsName = productDetail.getGoodsName();
		String collectURL = productDetail.getCollectURL();
		String goodsImage = productDetail.getGoodsImage();
		String goodsImageOrg = productDetail.getGoodsImage();
		Integer price = productDetail.getPrice();

		Integer discountPrice = productDetail.getDiscountPrice();
		Float discountRate = discountPrice == null ? null : 100f - (discountPrice * 100f / price);
		Boolean discounted = discountPrice != null;

		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		logger.error("getSkuInfomations 시작");
		logger.debug("getSkuInfomations 시작");

		for (Stock stock : stocks)
			set.add(stock.getColor());

		for (String color : colors) {

			boolean collected = set.contains(color);

			String skuNum;
			String skuName;
			String skuColor;

			String[] numAndNameAndColor = getSkuNumAndNameAndColor(productDetail, color);
			// 구현이 안되어 있는 경우
			if (numAndNameAndColor == null) {
				skuNum = String.format("%s/%s", goodsNum, color);
				skuName = String.format("%s / %s", goodsName, color);
				skuColor = color;
			} else {
				skuNum = numAndNameAndColor[0];
				skuName = numAndNameAndColor[1];
				skuColor = numAndNameAndColor[2];
			}

			Sku s = new Sku(collected);
			s.setSkuNum(skuNum);
			s.setSkuName(skuName);
			s.setColor(skuColor);
			s.setCollectURL(collectURL);
			//s.setGoodsImage(saveImage_new(skuNum, goodsImage, productDetail.getSite()));
			s.setS3ImageUrl("");
			s.setGoodsImage("");
			s.setPrice(price);
			s.setGoodsImageOrg(goodsImageOrg);
			s.setDiscounted(discounted);
			s.setDiscountPrice(discountPrice);
			s.setDiscountRate(discountRate);

			s.setBestItem(bestItem);

			// 수집된 Color일 경우
			if (collected) {
				for (int i = 0; i < stocks.size(); i++) {
					Stock stock = stocks.get(i);
					if (matchColor(color, stock.getColor())) {
						stock.setColor(skuColor);
						s.addStock(stock);

						stocks.remove(i--);
					}
				}
			}

			for (int i = 0; i < materials.size(); i++) {
				Materials m = materials.get(i);
				if (matchColor(color, m.getColor())) {
					m.setColor(skuColor);
					s.addMaterials(m);

					materials.remove(i--);
				}
			}
			sku.add(s);
		}
		return sku;
	}

	/**
	 * SKU정보를 변환하여 가져온다.
	 * 
	 * @param colors
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	public List<Sku> getSkuInfomations(String[] colors, ProductDetail productDetail) throws Exception {
		List<Sku> sku = new ArrayList<>();

		String goodsNum = productDetail.getGoodsNum();
		String goodsName = productDetail.getGoodsName();
		String collectURL = productDetail.getCollectURL();
		String goodsImage = productDetail.getGoodsImage();
		String goodsImageOrg = productDetail.getGoodsImage();
		Integer price = productDetail.getPrice();

		Integer discountPrice = productDetail.getDiscountPrice();
		Float discountRate = discountPrice == null ? null : 100f - (discountPrice * 100f / price);
		Boolean discounted = discountPrice != null;

		boolean bestItem = productDetail.isBestItem();

		List<Materials> materials = new ArrayList<>(productDetail.getMaterials());
		List<Stock> stocks = new ArrayList<>(productDetail.getStocks());
		Set<String> set = new HashSet<>();
		logger.error("getSkuInfomations 시작");
		logger.debug("getSkuInfomations 시작");

		for (Stock stock : stocks)
			set.add(stock.getColor());

		for (String color : colors) {

			boolean collected = set.contains(color);

			String skuNum;
			String skuName;
			String skuColor;

			String[] numAndNameAndColor = getSkuNumAndNameAndColor(productDetail, color);
			// 구현이 안되어 있는 경우
			if (numAndNameAndColor == null) {
				skuNum = String.format("%s/%s", goodsNum, color);
				skuName = String.format("%s / %s", goodsName, color);
				skuColor = color;
			} else {
				skuNum = numAndNameAndColor[0];
				skuName = numAndNameAndColor[1];
				skuColor = numAndNameAndColor[2];
			}

			Sku s = new Sku(collected);
			Map<String,String> resultMap = saveImage_zara(skuNum, goodsImage);//s3ImageUrl
			s.setSkuNum(skuNum);
			s.setSkuName(skuName);
			s.setColor(skuColor);
			s.setCollectURL(collectURL);
			s.setGoodsImage(resultMap.get("relativePath"));
			//s.setS3ImageUrl(resultMap.get("s3ImageUrl"));
			s.setPrice(price);
			s.setGoodsImageOrg(goodsImageOrg);
			s.setDiscounted(discounted);
			s.setDiscountPrice(discountPrice);
			s.setDiscountRate(discountRate);

			s.setBestItem(bestItem);

			// 수집된 Color일 경우
			if (collected) {
				for (int i = 0; i < stocks.size(); i++) {
					Stock stock = stocks.get(i);
					if (matchColor(color, stock.getColor())) {
						stock.setColor(skuColor);
						s.addStock(stock);

						stocks.remove(i--);
					}
				}
			}

			for (int i = 0; i < materials.size(); i++) {
				Materials m = materials.get(i);
				if (matchColor(color, m.getColor())) {
					m.setColor(skuColor);
					s.addMaterials(m);

					materials.remove(i--);
				}
			}
			sku.add(s);
		}
		return sku;
	}

	/**
	 * 색상 비교를 위해 사용
	 * 
	 * @param color1
	 * @param color2
	 * @return
	 */
	protected boolean matchColor(String color1, String color2) {
		return color1.equals(color2);
	}

	/**
	 * SKU 번호/명/색상 정보를 가져온다.
	 * 
	 * @param productDetail
	 * @param color
	 * @return
	 */
	public String[] getSkuNumAndNameAndColor(ProductDetail productDetail, String color) {
		return null;
	}

	private String[] getColors(Map<String, String> meta) {
		String string = meta.get(OnlineStoreConst.KEY_COLLECT_COLORS);
		String[] colors = string.split(";");

		// 색상 값 보정
		for (int i = 0; i < colors.length; i++)
			colors[i] = parser.replaceByRules(OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, OnlineStoreConst.WORD_CORRECTION_CATE_COLOR, colors[i]);

		Set<String> set = new HashSet<>(Arrays.asList(colors));
		return set.toArray(new String[set.size()]);
	}

	public Map<String,String> saveImage_zara(String skuNum, String imageURL) throws Exception {
		// ZARA 경우 주석 result2 관련 모두 주석
		FileOutputStream fos = null;
		Map<String,String> map = new HashMap<>();
		// FileOutputStream fos2 = null;
		String fileName = skuNum.replaceAll("[\\\\/:\\*?\"<>|\\s]+", "_");
		String relativePath = null;
//TO-DO S3 ZARA
//String s3ImageUrl = null;
		Result result = null;
		// Result result2 = null;

		try {
			Pattern pattern = Pattern.compile("\\.(jpg|jpeg|gif|png|bmp)$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(imageURL.replaceAll("\\?.*", ""));
			boolean found = matcher.find();

			if (found)
				fileName += "." + matcher.group(1).toLowerCase();

			relativePath = config.getImageRelativePath(fileName);

			request.openConnection(imageURL);
			request.addRequestHeader("User-Agent", USER_AGENT);

			result = request.executeWithGet(false);
			// result2 = request.executeWithGet(false);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				// 확장자가 없을 시에는 Response 헤더의 Content-Type을 가져온다.
				if (!found) {
					Map<String, List<String>> header = result.getHeader();
					if (header.containsKey("Content-Type")) {
						String contentType = header.get("Content-Type").get(0);
						if (contentType.matches("^image/.+$")) {
							fileName += "." + contentType.replaceAll("^image/(.+)$", "$1");
							relativePath = config.getImageRelativePath(fileName);
						}
					}
				}

				File file = new File(config.getImageDirectory(), fileName);
				fos = new FileOutputStream(file);
				result.writeTo(fos);
//TO-DO S3 ZARA				
//S3 file upload 
//s3ImageUrl = rwaDataFileMgr.fileUpload_zara(fileName, file);
				//File file2 = new File("/app/ec/imageserver/public/crawler/" + constant.name(), fileName);
				// fos2 = new FileOutputStream(file2);
				// result2.writeTo(fos2);

				if (uploader != null) {
					uploader.uploadImage(config.getImageRelativePath(), file);
					// logger.error("# 확인");
					// logger.debug("# 확인");
				} else {
					// logger.error("# uploader2 -> {}", " uploader -> NULL");
					// logger.debug("# uploader2 -> {}", " uploader -> NULL");
				}

			} else if (responseCode == HttpStatus.SC_NOT_FOUND) {
				relativePath = config.getImageRelativePath("404");
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_SAVE_IMAGE_FAIL, new RuntimeException("responseCode -> " + responseCode));
			}

			if (result != null)
				result.close();
			// if (result2 != null)
			// result2.close();
			if (fos != null)
				fos.close();
			// if (fos2 != null)
			// fos2.close();

			Assert.hasText(relativePath, "[Assertion failed] - [GoodsImage] must have text; it must not be null, empty, or blank");
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_SAVE_IMAGE_FAIL, e);
		} finally {
			IOUtils.closeQuietly(result);
			// IOUtils.closeQuietly(result2);
			IOUtils.closeQuietly(fos);
			// IOUtils.closeQuietly(fos2);
		}
		map.put("relativePath", relativePath);
//TO-DO S3 ZARA
//map.put("s3ImageUrl", s3ImageUrl);
		return map;
	}

	public String saveImage(String skuNum, String imageURL) throws Exception {
		// ZARA 경우 주석 result2 관련 모두 주석
		FileOutputStream fos = null;
		// FileOutputStream fos2 = null;
		String fileName = skuNum.replaceAll("[\\\\/:\\*?\"<>|\\s]+", "_");
		String relativePath = null;
		Result result = null;
		// Result result2 = null;

		try {
			Pattern pattern = Pattern.compile("\\.(jpg|jpeg|gif|png|bmp)$", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(imageURL.replaceAll("\\?.*", ""));
			boolean found = matcher.find();

			if (found)
				fileName += "." + matcher.group(1).toLowerCase();

			relativePath = config.getImageRelativePath(fileName);

			request.openConnection(imageURL);
			request.addRequestHeader("User-Agent", USER_AGENT);

			result = request.executeWithGet(false);
			// result2 = request.executeWithGet(false);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				// 확장자가 없을 시에는 Response 헤더의 Content-Type을 가져온다.
				if (!found) {
					Map<String, List<String>> header = result.getHeader();
					if (header.containsKey("Content-Type")) {
						String contentType = header.get("Content-Type").get(0);
						if (contentType.matches("^image/.+$")) {
							fileName += "." + contentType.replaceAll("^image/(.+)$", "$1");
							relativePath = config.getImageRelativePath(fileName);
						}
					}
				}

				File file = new File(config.getImageDirectory(), fileName);
				fos = new FileOutputStream(file);
				result.writeTo(fos);

				File file2 = new File("/app/ec/imageserver/public/crawler/" + constant.name(), fileName);
				// fos2 = new FileOutputStream(file2);
				// result2.writeTo(fos2);

				if (uploader != null) {
					uploader.uploadImage(config.getImageRelativePath(), file);
					// logger.error("# 확인");
					// logger.debug("# 확인");
				} else {
					// logger.error("# uploader2 -> {}", " uploader -> NULL");
					// logger.debug("# uploader2 -> {}", " uploader -> NULL");
				}

			} else if (responseCode == HttpStatus.SC_NOT_FOUND) {
				relativePath = config.getImageRelativePath("404");
			} else {
				exceptionBuilder.raiseException(ErrorType.ERROR_SAVE_IMAGE_FAIL, new RuntimeException("responseCode -> " + responseCode));
			}

			if (result != null)
				result.close();
			// if (result2 != null)
			// result2.close();
			if (fos != null)
				fos.close();
			// if (fos2 != null)
			// fos2.close();

			Assert.hasText(relativePath, "[Assertion failed] - [GoodsImage] must have text; it must not be null, empty, or blank");
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_SAVE_IMAGE_FAIL, e);
		} finally {
			IOUtils.closeQuietly(result);
			// IOUtils.closeQuietly(result2);
			IOUtils.closeQuietly(fos);
			// IOUtils.closeQuietly(fos2);
		}
		return relativePath;
	}
	
	public String saveImage_new(String fileName, String imageURL, String site) throws Exception {

		String s3ImageUrl = ""; //rwaDataFileMgr.fileUpload(imageURL, fileName, request);
		return s3ImageUrl;
	}

	/**
	 * 온라인 아이템별 작업결과 성공 여부를 Log테이블에 반영한다.
	 * 
	 * @param errorStep
	 * @param logDetail
	 * @param exception
	 * @throws Exception
	 */
	private void writeLog(short errorStep, LogDetail logDetail, BizException exception) throws Exception {
		try {
			Long id = logDetail.getId();

			logDetail.setErrorStep(errorStep);

			final int LIMIT = 3500;

			short errorNum = 0;
			String errorMessage = null;
			boolean appliedYn = true;

			if (exception != null) {
				errorNum = exception.getErrorNumber();
				errorMessage = exception.getStackTraceString();
				appliedYn = false;

				if (errorMessage.length() > LIMIT)
					errorMessage = errorMessage.substring(0, LIMIT);
			}

			logDetail.setErrorNum(errorNum);
			logDetail.setErrorMessage(errorMessage);
			logDetail.setAppliedYn(appliedYn);

			if (id == null)
				productDao.insertGoodsLog(logDetail);
			else
				productDao.updateGoodsLog(logDetail);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 크롤링한 온라인 아이템 정보를 마스터 테이블에 반영한다.
	 * 
	 * @param meta
	 * @param logDetail
	 * @param goods
	 */
	private void writeGoods(Map<String, String> meta, LogDetail logDetail, Goods goods) {
		try {
			logger.error("시작4");
			logger.debug("시작4");
			boolean success = Boolean.parseBoolean(meta.get(OnlineStoreConst.KEY_CATEGORY_MAPPING_YN));
			int status = Integer.parseInt(meta.get(OnlineStoreConst.KEY_STATUS));
			Long goodsLogId = logDetail.getId();
			String goodsNum = goods.getGoodsNum();

			String cate1 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_1);
			String cate2 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_2);
			String cate3 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_3);
			String cate4 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_4);

			goods.addOrgCateName(cate1).addOrgCateName(cate2).addOrgCateName(cate3).addOrgCateName(cate4);

			goods.setStatus(status);

			synchronized (lock.getLockObject(goodsNum)) {
				productDao.mergeGoods(config.getCollectDayString(), goods);
			}
			
			Long goodsId = goods.getId();

			productDao.mergeGoodsLogMapping(goodsLogId, goodsId, config.getCollectDayString());

			if (!success) {
				productDao.mergeCateMappingFail(goodsId, cate1, cate2, cate3, cate4);
			} else {
				productDao.deleteCateMappingFail(goodsId);
			}
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_DB_WRITE_FAIL, e);
		}
	}

	/**
	 * 크롤링한 온라인 아이템 정보를 마스터 테이블에 반영한다.
	 * 
	 * @param meta
	 * @param logDetail
	 * @param goods
	 */
	private void writeGoods_zara(Map<String, String> meta, LogDetail logDetail, Goods goods, int retryCount) {
		try {
			logger.error("시작4");
			logger.debug("시작4");
			boolean success = Boolean.parseBoolean(meta.get(OnlineStoreConst.KEY_CATEGORY_MAPPING_YN));
			int status = Integer.parseInt(meta.get(OnlineStoreConst.KEY_STATUS));
			String localJsonPath = config.properties.getProperty("crawler.base.directory");
			Long goodsLogId = logDetail.getId();
			String goodsNum = goods.getGoodsNum();

			String cate1 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_1);
			String cate2 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_2);
			String cate3 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_3);
			String cate4 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_4);

			goods.addOrgCateName(cate1).addOrgCateName(cate2).addOrgCateName(cate3).addOrgCateName(cate4);

			goods.setStatus(status);

			synchronized (lock.getLockObject(goodsNum)) {
				productDao.mergeGoods(config.getCollectDayString(), goods);
			}
			
			goods.getColtItemMap().put("layer1", cate1);
			goods.getColtItemMap().put("layer2", cate2);
			goods.getColtItemMap().put("layer3", cate3);
			goods.getColtItemMap().put("layer4", cate4);
			goods.getColtItemMap().put("collectDay", config.getCollectDayString());
			
		/*	Map<String, Object> map = goods.getColtItemMap();
			
			if(retryCount == 0 ) {
				putItemToQueue(map);
				writerThr(site, site, localJsonPath);
			}*/
				
			Long goodsId = goods.getId();

			productDao.mergeGoodsLogMapping(goodsLogId, goodsId, config.getCollectDayString());

			if (!success) {
				productDao.mergeCateMappingFail(goodsId, cate1, cate2, cate3, cate4);
			} else {
				productDao.deleteCateMappingFail(goodsId);
			}
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_DB_WRITE_FAIL, e);
		}
	}


	private void writeGoods_new(Map<String, String> meta, LogDetail logDetail, Goods goods, int retryCount) {
		try {
		
			int status = Integer.parseInt(meta.get(OnlineStoreConst.KEY_STATUS));
			String localJsonPath = config.properties.getProperty("crawler.base.directory");
			String cate1 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_1);
			String cate2 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_2);
			String cate3 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_3);
			String cate4 = meta.get(OnlineStoreConst.KEY_ORIGINAL_CATE_4);

			goods.addOrgCateName(cate1).addOrgCateName(cate2).addOrgCateName(cate3).addOrgCateName(cate4);

			goods.setStatus(status);
			
			goods.getColtItemMap().put("layer1", cate1);
			goods.getColtItemMap().put("layer2", cate2);
			goods.getColtItemMap().put("layer3", cate3);
			goods.getColtItemMap().put("layer4", cate4);
			goods.getColtItemMap().put("collectDay", config.getCollectDayString());
			Map<String, Object> map = goods.getColtItemMap();
			
			if(retryCount == 0) {
				putItemToQueue(map);
				writerThr(site, site, localJsonPath);
			}
			

		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_DB_WRITE_FAIL, e);
			e.printStackTrace();
		}
	}

	private void writerThr(String brandCode, String site, String localJsonPath) {
		Thread writerThr;
		String collectSite = constant.getSiteUrl(site);
		
		switch (site) {
		case OnlineStoreConst.SITE_SPAO:
			if(writerSpao == null) {
				writerSpao = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);

				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerSpao);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}
			
			break;
		case OnlineStoreConst.SITE_MIXXO:
			if(writerMixxo == null) {
				writerMixxo = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);

				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerMixxo);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}
			break;
		case OnlineStoreConst.SITE_HANDSOME:
			if(writerHandsome == null) {
				writerHandsome = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);

				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerHandsome);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}

			break;
		case OnlineStoreConst.SITE_HM:
			if(writerHm == null) {
				writerHm = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);

				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerHm);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}
			break;
		case OnlineStoreConst.SITE_UNIQLO:
			if(writerUniqlo == null) {
				writerUniqlo = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);

				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerUniqlo);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}

			break;
		case OnlineStoreConst.SITE_ZARA:
			if(writerZara == null) {
				writerZara = new WriteJsonObjectLocal(this.queue, brandCode, collectSite, localJsonPath, request);
				// runnable 객체로 thread 객체를 생성하고, 그 객체가 살아있지 않으면 시작시키다.
				writerThr = new Thread(writerZara);
				if (!writerThr.isAlive()) {
					System.out.println("writerThr --> start");
					writerThr.start();
				}
			}
			break;
		}
	}

	private void writeGoods_handsome(Map<String, String> meta, LogDetail logDetail, ProductDetail productDetail ,int retryCount) {
		try {
			DataStandard dst = productDetail.getDataStandard();
			String localJsonPath = config.properties.getProperty("crawler.base.directory");
			WriteJsonObjectLocal wjol = new WriteJsonObjectLocal(productDetail.getBrandCode(), productDetail.getSite(), localJsonPath);
			//synchronized (lock.getLockObject(dst.getProductCode())) {

/*			List<String> imgUrls = productDetail.getDataStandard().getImageUrl();
			List<String> seImgUrls = new ArrayList<>();*/

	/*		for (String imgUrl : imgUrls) {
				String imageFilePath = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
				String seImgUrl = rwaDataFileMgr.fileUpload(imgUrl, imageFilePath, request);
				seImgUrls.add(seImgUrl);
			}
*/
			//productDetail.getDataStandard().setS3ImageUrl(seImgUrls);

			DataStandard productItem = productDetail.getDataStandard();

			Map<String, Object> obj = new HashMap<>();
			obj.put("saleYn", productItem.getSaleYn());
			obj.put("productCode", productItem.getProductCode());
			obj.put("normalPrice", productItem.getNormalPrice());
			obj.put("salePrice", productItem.getProductSalePrice());
			obj.put("brandName", productItem.getBrandName());
			obj.put("imageUrl", productItem.getImageUrl());
			obj.put("s3ImageUrl", productItem.getS3ImageUrl());
			obj.put("productName", productItem.getProductName());
			obj.put("layer1", productItem.getLayer1());
			obj.put("layer2", productItem.getLayer2());
			obj.put("layer3", productItem.getLayer3());
//			obj.put("layer4", productItem.getLayer4());
//			obj.put("layer5", productItem.getLayer5());
//			obj.put("layer6", productItem.getLayer6());
			obj.put("productColor", productItem.getProductColor());
			obj.put("productSize", productItem.getProductSize());
			obj.put("material", productItem.getMaterial());
			obj.put("origin", productItem.getOrigin());
			obj.put("stockInfo", productItem.getStockInfo());
			obj.put("crawlDate", productItem.getCrawlDate());
			obj.put("crawlUrl", productItem.getCrawlUrl());
			obj.put("bestYn", productItem.getBestYn());
			obj.put("newYn", productItem.getNewYn());
			obj.put("goodEval", productItem.getGoodEval());
			obj.put("desc", productItem.getDesc());
			obj.put("dependentData", productItem.getDependentData());
			obj.put("collectDay", config.getCollectDayString());
			// wjol.writeJsonObject(obj);
			if(retryCount == 0) {
				putItemToQueue(obj);
				writerThr(site, site, localJsonPath);	
			}
			
			//}

			// productDao.mergeGoodsLogMapping(logDetail.getId(), goodsId, config.getCollectDayString());
		} catch (Exception e) {
			exceptionBuilder.raiseException(ErrorType.ERROR_DB_WRITE_FAIL, e);
		}
	}

	/**
	 * 소재 정보를 반환한다.
	 * 
	 * @param goodsMaterials
	 * @param colors
	 * @param productDetail
	 * @return
	 * @throws Exception
	 */
	private List<Materials> getGoodsMaterials(String goodsMaterials, String[] colors, ProductDetail productDetail) throws Exception {
		return parser.parseMarterialsString(goodsMaterials, colors, productDetail);
	}

	/**
	 * 판매 종료 날짜를 반영한다.
	 */
	public void updateGoodsCloseDay() {
		List<Map<String, Object>> list = productDao.getClosedGoods(constant.getSite(), config.getCollectDayString());

		for (Map<String, Object> map : list) {
			Long id = (Long) map.get("id");
			String lastCollectDay = (String) map.get("lastCollectDay");

			productDao.updateGoodsCloseDay(id, lastCollectDay);
		}
	}

	/**
	 * 베스트 아이템 목록을 반환한다.
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract List<String> getBestItems() throws Exception;

	/**
	 * 카테고리 목록 정보를 반환한다.
	 * 
	 * @return
	 * @throws Exception
	 */
	public abstract List<Category> getCategories() throws Exception;

	/**
	 * 온라인 아이템 목록을 반환한다.
	 * 
	 * @param category
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public abstract List<Product> getProductList(Category category, int page) throws Exception;

	/**
	 * 온라인 아이템 상세정보를 작성한다.
	 * 
	 * @param productURL
	 * @param productDetail
	 * @throws Exception
	 */
	public abstract void fillOutProductDetail(String productURL, ProductDetail productDetail) throws Exception;

	/**
	 * Product 객체를 파일로 변환
	 * 
	 * @param p
	 * @return 기존 Product 파일 존재여부
	 */
	private boolean serialize(Product p) {
		String onlineGoodsNum = p.getOnlineGoodsNum();
		File file = new File(config.getInputDirectory(), OnlineStoreConst.CONFIG_PRODUCT_LIST_FILE_PREFIX + onlineGoodsNum);
		Product product = null;

		boolean exists = file.exists();

		if (file.exists()) {
			product = resotre(file);
			product.merge(p);
		} else {
			product = p;
		}
		product.setBackupFile(file);
		SerializationUtils.serialize(file, product);

		return exists;
	}

	private File serialize(String onlineGoodsNum, ProductDetail productDetail) {
		File file = new File(config.getOutputDirectory(), OnlineStoreConst.CONFIG_PRODUCT_DETAIL_FILE_PREFIX + onlineGoodsNum);
		SerializationUtils.serialize(file, productDetail);
		return file;
	}

	public void backup(Object object, String fileName) {
		SerializationUtils.serialize(new File(config.getBackupDirectory(), fileName), object);
	}

	public <T> T resotre(String fileName) {
		return resotre(new File(config.getBackupDirectory(), fileName));
	}

	@SuppressWarnings("unchecked")
	public <T> T resotre(File file) {
		if (file.exists()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				fis.close();
				return (T) SerializationUtils.deserialize(new FileInputStream(file));
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			} finally {
				IOUtils.closeQuietly(fis);
			}
		}
		return null;
	}

	private void finish() {
		try {
			File file = new File(config.getInputDirectory(), OnlineStoreConst.CONFIG_JOB_FINISED_FILE_NAME);
			if (!file.exists())
				file.createNewFile();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private void throwIfInterrupted() {
		if (interrupted)
			exceptionBuilder.raiseException(ErrorType.ERROR_TASK_INTERRUPTED);
	}

	public ServiceConfig getServiceConfig() {
		return config;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, List<String>> getQueryMap(String url, String encoding) {
		String queryString = url.replaceAll("^.*\\?([^\\?]+)$", "$1");

		if (StringUtils.isNotEmpty(queryString)) {
			try {

				Map<String, List<String>> map = new HashMap<>();

				String[] params = queryString.split("&");
				for (String param : params) {
					if (param.indexOf('=') > -1) {

						String name = param.substring(0, param.indexOf('='));
						String value = param.substring(param.indexOf('=') + 1);

						List<String> values;
						if (map.containsKey(name)) {
							values = map.get(name);
						} else {
							values = new ArrayList<>();
							map.put(name, values);
						}

						values.add(URLDecoder.decode(value, encoding));
					}
				}
				return map;
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		return Collections.EMPTY_MAP;
	}

	public void endQueue() {

		// BY.HNC ========================================== START
		// serviceId 를 보고 어떤 큐에 종료 값을 넣을지 결정할 수 있음.
		// 이 클래스는 @Component 클래스이기는 하지만 여기서 큐를 @Autowired 할 순 없음.
		// 콜렉트 사이트가 어느 곳이냐에 따라 잡의 종료를 알리는 값을 달리 넣어야 할 것이기 때문

		// BlockingQueue<Map<String, Object>> queue = null;
		Map<String, Object> jobEnd = new HashMap<>();
		jobEnd.put("isEnd", true);

		logger.debug("ServiceJobHistory", "========================");
		logger.debug("ServiceJobHistory", jobEnd.toString());
		logger.debug("ServiceJobHistory", "========================");


		try {
			if (queue != null)
				queue.put(jobEnd);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// BY.HNC ========================================== END

	}

	public void putItemToQueue(Map<String, Object> itemMap) {

		try {
			synchronized (itemMap) {
				itemMap.put("isEnd", false);
			}
			System.out.println(itemMap);
			queue.put(itemMap);


		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("putItemToQueue == > " + e.toString());
		}
	}
}
