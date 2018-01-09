package com.eopcon.crawler.samsungcnt.service.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.StepContribution;
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

@Component
@Scope("step")
public class ProductListTasklet implements StoppableTasklet, InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	private static final int MAX_RETRY_COUNT = 3;

	@Value("#{jobParameters['type']}")
	private OnlineStoreConst constant;
	@Value("#{jobParameters['collectDay']}")
	private String collectDay;
	@Value("#{stepExecution.jobExecution.jobId}")
	private Long jobExecutionId;
	@Autowired
	private ApplicationContext context;

	private OnlineStoreCrawler crawler;

	private int retryCount = 0;

	@Override
	public void afterPropertiesSet() throws Exception {
		crawler = (OnlineStoreCrawler) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER, constant, collectDay);
	}

	@Override
	public void stop() {
		logger.debug("# Stop event has been received!!");
		crawler.interrupt();
	}

	/**
	 * 온라인 아이템 목록을 수집한다.
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		try {
			if (logger.isDebugEnabled() && retryCount > 0)
				logger.debug("# Type -> {}, Retry Count -> {}", constant, retryCount);
			
			logger.debug(getClass().getName(), "   ->  execute");
			System.out.println(getClass().getName() + "   ->  execute");
			crawler.addRequestConfig(OnlineStoreConst.KEY_USE_PROXY, false);
			
			MDC.put("location", constant.toString());
			long elapsedTime = System.currentTimeMillis();
			System.out.println("execute1");
			int count = crawler.queryProductList(jobExecutionId);

			contribution.incrementWriteCount(count);
			System.out.println("count===============>>> " + count);
			elapsedTime = (System.currentTimeMillis() - elapsedTime) / 1000;

			if (logger.isDebugEnabled())
				logger.debug("# Type -> {}, Count -> {}, ElapsedTime -> {} sec", constant, count, elapsedTime);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			retryCount++;

			if (retryCount < MAX_RETRY_COUNT)
				return RepeatStatus.CONTINUABLE; // 다시 실행
			throw e;
		} finally {
			MDC.remove("location");
		}
		return RepeatStatus.FINISHED;
	}
}
