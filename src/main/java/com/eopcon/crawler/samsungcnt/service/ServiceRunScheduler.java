package com.eopcon.crawler.samsungcnt.service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import com.eopcon.crawler.samsungcnt.jobscheduler.RunScheduler;

public class ServiceRunScheduler implements InitializingBean, Runnable {

	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	@Qualifier(OnlineStoreConst.JOB_NAME_CRAWLING)
	private Job job;
	@Autowired
	private TaskScheduler taskScheduler;

	private OnlineStoreConst type;

	private Integer delay = null;
	private String cronExpression = null;

	private ScheduledFuture<?> future = null;

	public void setType(OnlineStoreConst type) {
		this.type = type;
	}

	public void setScheduleWithFixedDelay(Integer delay) {
		this.delay = delay;
		this.cronExpression = null;
	}

	public void setCronExpression(String cronExpression) {
		this.delay = null;
		this.cronExpression = cronExpression;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

	public void start() {
		if (delay != null)
			future = taskScheduler.scheduleWithFixedDelay(this, delay);
		if (cronExpression != null)
			future = taskScheduler.schedule(this, new CronTrigger(cronExpression));
	}

	public void stop() {
		if (future != null) {
			if (future.cancel(true)) {
				future = null;
			}
		}
	}

	@Override
	public void run() {
		if (future != null) {
			RunScheduler runScheduler = getObject();
			runScheduler.run();
		}
	}

	public RunScheduler getObject() {

		Map<String, String> parameterMap = new HashMap<>();

		parameterMap.put("type", type.toString());
		parameterMap.put("collectDay", new SimpleDateFormat("yyyyMMdd").format(new Date()));
		parameterMap.put("timestamp", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

		RunScheduler runScheduler = new RunScheduler();
		runScheduler.setJobLauncher(jobLauncher);
		runScheduler.setJobList(Arrays.asList(job));
		runScheduler.setParameterMap(parameterMap);

		return runScheduler;
	}
}
