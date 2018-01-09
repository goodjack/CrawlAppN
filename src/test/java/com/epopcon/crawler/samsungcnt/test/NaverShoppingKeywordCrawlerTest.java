package com.epopcon.crawler.samsungcnt.test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.keyword.NaverShoppingKeywordCrawler;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class NaverShoppingKeywordCrawlerTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext applicationContext;
	
	@Before
	public void setUp() {

	}

	//@Test
	public void test() {
		int week = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		
 		Calendar calendar = Calendar.getInstance();

 		int y = 2017;
 		int m = 4-1;
 		int d = 3;
 		calendar.set(y, m, d);
 		
 		week = calendar.get(Calendar.WEEK_OF_YEAR);
 		System.out.println(String.valueOf(week) + "(" + sdf.format(calendar.getTime()) + ")");
 		
 		/* 		
 		week = calendar.get(Calendar.WEEK_OF_YEAR);
 		System.out.println(week);
 		*/
 		
 		calendar.set(Calendar.WEEK_OF_YEAR, week-1);
 		
 		
 		calendar.add(Calendar.DAY_OF_MONTH, 2 - calendar.get(Calendar.DAY_OF_WEEK));
 		System.out.println("전주의 월요일: " +  sdf.format(calendar.getTime()));

 		calendar.set(Calendar.WEEK_OF_YEAR, week);

 		calendar.add(Calendar.DAY_OF_MONTH, (1 - calendar.get(Calendar.DAY_OF_WEEK)));
 		
 		System.out.println("현재 주의 일요일: " +  sdf.format(calendar.getTime()));
 		
	}
	
	
	@Test
	public void testNaverKeyword() throws Exception {
				
		NaverShoppingKeywordCrawler naverShoppingKeywordCrawler = applicationContext.getBean("naverShoppingKeywordCrawler", NaverShoppingKeywordCrawler.class);
		
		//최근 수집일
		String start_dt = "2017.03.07";  // Start date
		String end_dt = "2017.03.13";  // Start date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
		
		Calendar sc = Calendar.getInstance();
		sc.setTime(sdf.parse(start_dt));
		Calendar ec = Calendar.getInstance();
		ec.setTime(sdf.parse(end_dt));
		
		//한번씩 크롤링하기
		//naverShoppingKeywordCrawler.getKeywords(start_dt,end_dt);
		
		
		naverShoppingKeywordCrawler.execute();
		
		/* 기간내 반복 크롤링
		for(int i=0;i<2;i++)
		{
			System.out.println("setDate ->" + start_dt);
			System.out.println("setDate ->" + end_dt);
			//크라울러 저장 함수
			naverShoppingKeywordCrawler.getKeywords(start_dt,end_dt);
			
			ec.add(Calendar.DATE, -7);  // number of days to add
			end_dt = sdf.format(ec.getTime());  // dt is now the new date
			
			sc.add(Calendar.DATE, -7);  // number of days to add
			start_dt = sdf.format(sc.getTime());  // dt is now the new date		
		}
		*/
	
	}

}
