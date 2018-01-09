package com.eopcon.crawler.samsungcnt.jobscheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;

public class RunScheduler implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	private JobLauncher jobLauncher;
	private List<Job> jobList;

	private Map<String, String> parameterMap = new HashMap<>();

	public void setJobLauncher(JobLauncher jobLauncher) {
		this.jobLauncher = jobLauncher;
	}

	public void setJobList(List<Job> jobList) {
		this.jobList = jobList;
	}

	public void setParameterMap(Map<String, String> parameterMap) {
		this.parameterMap = parameterMap;
	}

	@Override
	public void run() {
		JobParametersBuilder builder = new JobParametersBuilder();

		for (Entry<String, String> entry : parameterMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			builder.addString(key, value);
		}

		JobParameters jobParameters = builder.toJobParameters();

		for (Job job : jobList) {
			try {
				jobLauncher.run(job, jobParameters);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				break;
			}
		}
	}
}
