package com.eopcon.crawler.samsungcnt.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class JobLogListener extends JobExecutionListenerSupport {
	
	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		long jobId = jobExecution.getJobInstance().getId();
		String jobName = jobExecution.getJobInstance().getJobName();
		
		logger.info("# Job Started - job id : {} | job name : {}", jobId, jobName);
	}
	
	@Override
	public void afterJob(JobExecution jobExecution) {
		long jobId = jobExecution.getJobInstance().getId();
		String jobName = jobExecution.getJobInstance().getJobName();
		BatchStatus status = jobExecution.getStatus();
	
		logger.info("# Job Finished - job id : {} | job name : {} | job status : {} | job summary -> \n{}", jobId, jobName, status.toString(), jobExecution.toString());
	}
}
