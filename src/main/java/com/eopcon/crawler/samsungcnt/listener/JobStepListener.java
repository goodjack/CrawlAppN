package com.eopcon.crawler.samsungcnt.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class JobStepListener implements StepExecutionListener {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	@Override
	public void beforeStep(StepExecution stepExecution) {
		JobExecution jobExecution = stepExecution.getJobExecution();

		long jobId = jobExecution.getJobInstance().getId();
		String jobName = jobExecution.getJobInstance().getJobName();
		String stepName = stepExecution.getStepName();

		logger.info("# Step Started - job id : {} | job name : {} | step name : {}", jobId, jobName, stepName);
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		JobExecution jobExecution = stepExecution.getJobExecution();

		long jobId = jobExecution.getJobInstance().getId();
		String jobName = jobExecution.getJobInstance().getJobName();
		String stepName = stepExecution.getStepName();
		ExitStatus status = stepExecution.getExitStatus();

		logger.info("# Step Ended - job id : {} | job name : {} | step name : {} | step status : {} | step summary -> \n{}", jobId, jobName, stepName, status.toString(), stepExecution.getSummary());

		return status;
	}
}
