package com.eopcon.crawler.samsungcnt.service.keyword;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eopcon.crawler.samsungcnt.service.ServiceConfig;

@Component
public class CollectDateTest {

	protected ServiceConfig config;
	
	@Scheduled(cron = "0 34 19 * * *")
	public void execute() {
		System.out.println("execute()");
		String collectDay = config.getCollectDayString();
		System.out.println(collectDay);
	}
}
