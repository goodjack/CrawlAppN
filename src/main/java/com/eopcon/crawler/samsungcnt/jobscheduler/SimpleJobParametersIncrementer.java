package com.eopcon.crawler.samsungcnt.jobscheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

public class SimpleJobParametersIncrementer implements JobParametersIncrementer{
	
	private final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Override
	public JobParameters getNext(JobParameters parameters) {
		String id = format.format(new Date());
		return new JobParametersBuilder(parameters).addString("run.id", id).toJobParameters();
	}
}
