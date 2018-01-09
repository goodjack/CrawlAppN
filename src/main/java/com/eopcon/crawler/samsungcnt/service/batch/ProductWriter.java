package com.eopcon.crawler.samsungcnt.service.batch;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.model.LogDetail;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.ServiceConfig;

public class ProductWriter extends Thread {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);
	private static Logger commonLogger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	private Long jobExecutionId;

	protected ServiceConfig config;
	protected OnlineStoreConst constant;

	private ProductReader reader;
	private OnlineStoreCrawler crawler;
	private int retryCount;
	private Callback callback;

	private final int DELAY = 1000;

	public ProductWriter(Long jobExecutionId, OnlineStoreConst constant, OnlineStoreCrawler crawler, ProductReader reader) {
		
		this.jobExecutionId = jobExecutionId;
		this.config = crawler.getServiceConfig();
		this.constant = constant;
		this.crawler = crawler;
		this.reader = reader;
	}
	
	public ProductWriter(Long jobExecutionId, OnlineStoreConst constant, OnlineStoreCrawler crawler, ProductReader reader, int retryCount) {
		
		this.jobExecutionId = jobExecutionId;
		this.config = crawler.getServiceConfig();
		this.constant = constant;
		this.crawler = crawler;
		this.reader = reader;
		this.retryCount = retryCount;
	}

	@Override
	public void run() {
		List<String> list = crawler.resotre(OnlineStoreConst.CONFIG_BACKUP_BEST_ITEMS_FILE_NAME);
		Set<String> bestItems = null;
		if (list == null)
			bestItems = new HashSet<>();
		else
			bestItems = new HashSet<>(list);

		while (!Thread.interrupted()) {
			String onlineGoodsNum = null;
			System.out.println("!Thread.interrupted()================================");
			try {
				LogDetail logDetail = reader.read();

				if (logDetail == null)
					break;

				short errorStep = logDetail.getErrorStep();
				Product product = null;
				ProductDetail productDetail = null;
				String filePath = null;
				File backupFile = null;
				DataStandard dataStandard = null;
				switch (errorStep) {
				case OnlineStoreConst.LOG_ERROR_STEP_GET_DETAIL:
					filePath = logDetail.getVal9();

					if (StringUtils.isEmpty(filePath))
						continue;
					backupFile = new File(filePath);
					if (backupFile.exists()) {
						product = crawler.resotre(backupFile);
						dataStandard = product.getDataStandard();
						onlineGoodsNum = product.getOnlineGoodsNum();
						
						MDC.put("location", String.format("%s/%s", constant, onlineGoodsNum));
						
						List<Category> categories = product.getCategories();
						productDetail = new ProductDetail(categories);
						productDetail.setOnlineGoodsNum(onlineGoodsNum);
						productDetail.setBestItem(bestItems.contains(onlineGoodsNum));
						productDetail.setDataStandard(product.getDataStandard());
						//crawler.writeProductDetail(jobExecutionId, errorStep, product, productDetail);
						crawler.writeProductDetail_new(jobExecutionId, errorStep, product, productDetail , retryCount);
					}
					break;
				case OnlineStoreConst.LOG_ERROR_STEP_WRITE_DB:
				case OnlineStoreConst.LOG_ERROR_STEP_NONE:
					filePath = logDetail.getVal10();
					logger.error("시작3");
					logger.debug("시작3");
					if (StringUtils.isEmpty(filePath))
						continue;
					backupFile = new File(filePath);

					if (backupFile.exists()) {
						productDetail = crawler.resotre(backupFile);
						productDetail.setBackupFile(backupFile);
						onlineGoodsNum = productDetail.getOnlineGoodsNum();
						MDC.put("location", String.format("%s/%s", constant, onlineGoodsNum));
						crawler.writeProductDetail_new(jobExecutionId, errorStep, product, productDetail ,retryCount);
					}
					break;
				}

				if (logger.isDebugEnabled())
					logger.debug("# Success -> Type : {}, OnlineGoodsNum : {}", constant, onlineGoodsNum);
				if (commonLogger.isDebugEnabled()) {
					MDC.remove("location");
					MDC.put("location", constant.toString());
					commonLogger.debug("# Success -> Type : {}, OnlineGoodsNum : {}", constant, onlineGoodsNum);
				}

				if (callback != null)
					callback.onSuccess();

				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				String errorMessage = String.format("# Failure -> Type : %s, OnlineGoodsNum : %s, ErrorMessage : %s", constant, onlineGoodsNum, e.getMessage());
				MDC.remove("location");
				MDC.put("location", constant.toString());
				logger.error(errorMessage, e);
				commonLogger.error(errorMessage, e);
				
				if (callback != null) {
					if (!callback.onFailure(e))
						break;
				}
			} finally {
				MDC.remove("location");
			}
		}

		if (callback != null)
			callback.onComplete();
	}

	@Override
	public void interrupt() {
		super.interrupt();
		crawler.interrupt();
	}

	public void setCrawler(OnlineStoreCrawler crawler) {
		this.crawler = crawler;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public interface Callback {
		void onSuccess();

		boolean onFailure(Exception e);

		void onComplete();
	}
}
