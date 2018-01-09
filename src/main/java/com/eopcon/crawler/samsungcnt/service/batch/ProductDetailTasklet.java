package com.eopcon.crawler.samsungcnt.service.batch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.batch.ProductWriter.Callback;
import com.ssfashion.bigdata.crawler.file.aws.RawDataFileManagement;
import com.ssfashion.bigdata.crawler.util.DateUtil;

@Component
@Scope("step")
public class ProductDetailTasklet implements StoppableTasklet, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	private static final int MAX_RETRY_COUNT = 3;

	private static String WORKER_NAME = "ProductWriter";

	@Value("#{jobParameters['type']}")
	private OnlineStoreConst constant;
	@Value("#{jobParameters['collectDay']}")
	private String collectDay;
	@Value("#{stepExecution.jobExecution.jobId}")
	private Long jobExecutionId;
	@Value("${crawler.default.product.writerSize:3}")
	private int defaultWorkerSize = 5;
	@Autowired
	protected Properties properties;

	@Autowired
	private ApplicationContext context;

	private ProductReader reader;
	private ProductWriter[] writers;
	
	private OnlineStoreCrawler crawler;
	private int retryCount = 0;
	private static int writerCount = 0;
	//private int writerEndCount = 0;


	@Override
	public void afterPropertiesSet() throws Exception {
		
		crawler = (OnlineStoreCrawler) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, constant, collectDay);
		
		System.out.println("-===========================ProductDetailTasklet1================================");
	}

	private void initialize() {
		// Worker 사이즈 
		String strWorkerSize = properties.getProperty("crawler." + constant.toString().toLowerCase() + ".product.writerSize");
		int workerSize = strWorkerSize == null ? defaultWorkerSize : Integer.parseInt(strWorkerSize);
		reader = new ProductReader(jobExecutionId, crawler);
		writers = new ProductWriter[workerSize];
		
		for (int i = 0; i < writers.length; i++) {
			writers[i] = new ProductWriter(jobExecutionId, constant, crawler, reader,retryCount);
			writers[i].setName(String.format("%s-%s", WORKER_NAME, (i + 1)));
		}

	}

	@Override
	public void stop() {
		logger.debug("# Stop event has been received!!");

		for (ProductWriter writer : writers)
			writer.interrupt();
		reader.interrupt();
	}

	/**
	 * 온라인 아이템 상세 정보를 수집한다.
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		initialize();
		
		final StepContribution RESULT = contribution;

		Callback callback = new Callback() {
			@Override
			public void onSuccess() {
				if(retryCount == 0)
					RESULT.incrementReadCount();
				RESULT.incrementWriteCount(1);
			}

			@Override
			public boolean onFailure(Exception e) {
				if(retryCount == 0) {
					RESULT.incrementReadCount();
				}
					
				return true;
			}

			@Override
			public void onComplete() {
				writerCount--;
				/*if(writerCount == 0 && writerCount > -1 && retryCount == 2) {
					crawler.endQueue();
				}*/
			}
		};

		long elapsedTime = System.currentTimeMillis();

		reader.start();

		for (ProductWriter writer : writers) {
			writer.setCallback(callback);
			writer.start();
		}

		for (ProductWriter writer : writers)
			writer.join();

		// 판매종료 날짜 갱신
		crawler.updateGoodsCloseDay();

		elapsedTime = (System.currentTimeMillis() - elapsedTime) / 1000;
		logger.debug("# Type -> {}, ElapsedTime -> {} sec, Retry Count -> {},  Total Count -> {}, Success Count -> {}, Fail Count -> {}", 
				constant, elapsedTime, retryCount, contribution.getReadCount(), contribution.getWriteCount(), Math.max(contribution.getReadCount() - contribution.getWriteCount(), 0));

		// 실패 건수 조회
		Long failCount = crawler.getGoodsLogFailCount();
		if (failCount > 0) {
			retryCount++;
			if (retryCount < MAX_RETRY_COUNT)
				return RepeatStatus.CONTINUABLE; // 다시 실행
			throw new UnexpectedJobExecutionException("Remain Fail Count -> " + failCount);
		}
		
		
	

		return RepeatStatus.FINISHED;
	}
}
