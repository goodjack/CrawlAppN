package com.epopcon.crawler.samsungcnt.test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
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
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class ZaraBatchJobTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private HttpRequestService request;

	private String collectDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
	//private String collectDay = "20170622";

	@Before
	public void setUp() {

	}

	@Test
	public void testZaraJob() throws Exception {

		/*
		UPDATE MSCNT_GOODS_LOG
		SET APPLIED_YN = 0
		WHERE SITE = 'ZARA'
		AND LAST_COLLECT_DAY = '20170214'
		AND APPLIED_YN = 1
		*/

		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.ZARA.toString());
		builder.addString("collectDay", collectDay);

		JobLauncherTestUtils util = new JobLauncherTestUtils();

		util.setJob(job);
		util.setJobLauncher(jobLauncher);
		util.setJobRepository(jobRepository);

		JobExecution jobExecution = util.launchJob(builder.toJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}

	//@Test
	public void test() throws Exception {
		//request.openConnection("http://www.zara.com/kr/ko/ss17-%EC%BB%AC%EB%A0%89%EC%85%98/woman/%EC%9D%B4%EB%B2%88-%EC%A3%BC-%EC%8B%A0%EC%83%81%ED%92%88/%EB%A7%A4%EB%93%AD-%EB%94%94%ED%85%8C%EC%9D%BC-%EB%A0%88%EB%8D%94-%ED%94%8C%EB%9E%AB-%EC%8A%88%EC%A6%88-c805003p4065538.html");
		Result result = null;
		String content = null;
		Document doc = null;
		//Result result = request.executeWithGet(true);

		//if (result.getResponseCode() == HttpStatus.SC_OK) {
			//String content = result.getString();

			//Document doc = Jsoup.parse(content);

			//Elements el = doc.select("div.size-select > label");
			//String sku = el.first().attr("data-sku");
			String body = "{\"products\":[{\"sku\":4374074,\"parentId\":\"4374512\",\"quantity\":23,\"categoryId\":805003}]}";

			request.openConnection("http://www.zara.com/kr/ko/shop/cart/add?ajax=true");

			request.addRequestHeader("Host", "www.zara.com");
			request.addRequestHeader("Origin", "http://www.zara.com");
			request.addRequestHeader("Accept", "application/json");
			request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
			request.addRequestHeader("Content-Type", "application/json");
			request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			result = request.executeWithPost(body, "utf-8", true);

			if (result.getResponseCode() == HttpStatus.SC_OK) {

				request.openConnection("https://www.zara.com/kr/ko/shop/cart");

				result = request.executeWithGet(true);

				if (result.getResponseCode() == HttpStatus.SC_OK) {
					content = result.getString();
					doc = Jsoup.parse(content);

					for (Element e : doc.select("tr[id^='order-item-']")) {
						String id = e.attr("id").replaceAll("^order-item-", "");
						System.out.println(id);

						for (int i = 50; i < 100; i += 10) {
							request.openConnection("https://www.zara.com/kr/ko/shop/cart/update?ajax=true");

							request.addRequestHeader("Host", "www.zara.com");
							request.addRequestHeader("Origin", "http://www.zara.com");
							request.addRequestHeader("Accept", "application/json");
							request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
							request.addRequestHeader("Content-Type", "application/json");
							request.addRequestHeader("X-Requested-With", "XMLHttpRequest");

							body = "{\"products\":\"[{\\\"id\\\":" + id + ",\\\"quantity\\\":" + i + "}]\"}";

							result = request.executeWithPost(body, "utf-8", true);

							if (result.getResponseCode() == HttpStatus.SC_OK) {
								content = result.getString();
								System.out.println(content);
							}
						}
					}
				}
			}
		//}
	}
}
