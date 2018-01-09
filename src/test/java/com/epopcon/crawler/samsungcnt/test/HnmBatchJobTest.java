package com.epopcon.crawler.samsungcnt.test;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.model.HnmStock;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HnmStoreParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class HnmBatchJobTest {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private JobRepository jobRepository;
	
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private HttpRequestService request;
	
	@Autowired
	protected ExceptionBuilder exceptionBuilder;

	private String collectDay = new SimpleDateFormat("yyyyMMdd").format(new Date());
	//private String collectDay = "20170119";

	@Before
	public void setUp() {

	}

	@Test
	public void testHnmJob() throws Exception {

		/*
		UPDATE MSCNT_GOODS_LOG
		SET APPLIED_YN = 0
		WHERE SITE = 'ZARA'
		AND LAST_COLLECT_DAY = '20170214'
		AND APPLIED_YN = 1
		*/

		Job job = applicationContext.getBean("productJob", Job.class);

		JobParametersBuilder builder = new JobParametersBuilder();

		builder.addString("type", OnlineStoreConst.HM.toString());
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
		request.openConnection("http://www2.hm.com/ko_kr/productpage.0434773001.html");	// 여성>재킷&코드>재킷>데님 재킷(49,000원)
		Result result = null;
		String content = null;
		Document doc = null;
		result = request.executeWithGet(true);

		if (result.getResponseCode() == HttpStatus.SC_OK) {
			content = result.getString();

			doc = Jsoup.parse(content);
			

			request.openConnection("http://www2.hm.com/ko_kr/cart/add");		// 카트에 상품 담기

			request.addRequestHeader("Host", "www2.hm.com");
			request.addRequestHeader("Origin", "http://www2.hm.com");
			request.addRequestHeader("Accept", "application/json");
			request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
			request.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			
			
			//String body = "qty=1&productCodePost=0434773001001&product_osa_area=&product_osa_type="; // 해당 사이즈 재고가 있는 상품
			String body = "qty=1&productCodePost=0434773001010&product_osa_area=&product_osa_type=";	// 해당 사이즈 재고가 없는 상품
			result = request.executeWithPost(body, "utf-8", true);

			if (result.getResponseCode() == HttpStatus.SC_OK) {

				content = result.getString();
				//"errorCode" : "noStockAvailable" <-- 재고가 없는경우의 응답
				System.out.println("카트에 담은 후 응답: " + content);
				
				
				request.openConnection("http://www2.hm.com/ko_kr/cart/context");	// 카트 내용 조회
				request.addRequestHeader("Host", "www2.hm.com");
				request.addRequestHeader("Origin", "http://www2.hm.com");
				request.addRequestHeader("Accept", "application/json");
				request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

				result = request.executeWithGet(true);

				if (result.getResponseCode() == HttpStatus.SC_OK) {
					content = result.getString();
					
					System.out.println("카트 내용 요청 후 응답: " + content);
					
					
					request.openConnection("http://www2.hm.com/ko_kr/cart/removeItem");	// 카트에 담은 상품 삭제
					request.addRequestHeader("Host", "www2.hm.com");
					request.addRequestHeader("Origin", "http://www2.hm.com");
					request.addRequestHeader("Accept", "application/json");
					request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
					request.addRequestHeader("Content-Type", "application/json;charset=UTF-8");

					body = "{\"variantCode\":\"0434773001001\"}";
					
					result = request.executeWithPost(body, "utf-8", true);

					if (result.getResponseCode() == HttpStatus.SC_OK) {
						content = result.getString();
						
						System.out.println("카드에 담은 상품 삭제 후 응답: " + content);

						
						request.openConnection("http://www2.hm.com/ko_kr/cart/context");	// 카트 내용 조회
						request.addRequestHeader("Host", "www2.hm.com");
						request.addRequestHeader("Origin", "http://www2.hm.com");
						request.addRequestHeader("Accept", "application/json");
						request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

						result = request.executeWithGet(true);
						if (result.getResponseCode() == HttpStatus.SC_OK) {
							content = result.getString();
							
							System.out.println("카트 내용 요청 후 응답: " + content);
						}
					}
					
					
				}
			}
		}
	}
	

	//@Test
	public void test2() throws Exception {
		request.openConnection("http://www2.hm.com/ko_kr/productpage.0434773001.html");	// 여성>재킷&코드>재킷>데님 재킷(49,000원)
		Result result = null;
		String content = null;
		Document doc = null;
		result = request.executeWithGet(true);

		if (result.getResponseCode() == HttpStatus.SC_OK) {
			content = result.getString();


			HnmStoreParser parser = (HnmStoreParser) applicationContext.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.HM);
			ProductDetail p = new ProductDetail(null);
			parser.parseProductDetail(content, p);

			List<Stock> stockList = p.getStocks();
			List<String> itemList = new ArrayList<>();
			
			for(Stock stock : stockList) {
				itemList.add(((HnmStock)stock).getDataCode());
			}

			List<String> availableItemList = getAvailableItem(itemList);
			System.out.println("availableItemList: " + availableItemList);
			
			for(String itemId : availableItemList) {
				addCart(itemId);
				getAvailableQuantity(itemId);
				clearCart(itemId);
			}
			
			System.out.println("asdfasfdasdfasdfasdf");
			
		}
	}
	
	
	
	// 삼품의 재고 유무 확인
	private List<String> getAvailableItem(List<String> itemList) throws Exception {
		request.openConnection("http://www2.hm.com/ko_kr/getAvailability?variants=" + StringUtils.join(itemList, ","));

		request.addRequestHeader("Host", "www2.hm.com");
		request.addRequestHeader("Origin", "http://www2.hm.com");
		request.addRequestHeader("Accept", "application/json");
		request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
		
		Result result = request.executeWithGet(true);
		String content = "";
		
		
		
		if(result.getResponseCode() == HttpStatus.SC_OK) {
			content = result.getString();
			return parseAvailabeItem(content);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
		
		return Collections.EMPTY_LIST;
	}
	
	private List<String> parseAvailabeItem(String content) throws Exception {
		List<String> availableItemList = new ArrayList<>();

		//availableItemList = mapper.readValue(content, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
		availableItemList = mapper.readValue(content, List.class);
				
		return availableItemList;
	}
	
	
	private void addCart(String itemId) throws Exception {

		request.openConnection("http://www2.hm.com/ko_kr/cart/add");

		request.addRequestHeader("Host", "www2.hm.com");
		request.addRequestHeader("Origin", "http://www2.hm.com");
		request.addRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
		request.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		request.addRequestHeader("X-Requested-With", "XMLHttpRequest");
		
		String body = "qty=1&productCodePost=" + itemId + "&product_osa_area=&product_osa_type=";
		
		Result result = request.executeWithPost(body, "utf-8", true);

		
		if(result.getResponseCode() != HttpStatus.SC_OK) {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
	}
	
	private void clearCart(String itemId) throws Exception {
		request.openConnection("http://www2.hm.com/ko_kr/cart/removeItem");

		request.addRequestHeader("Host", "www2.hm.com");
		request.addRequestHeader("Accept", "application/json, text/plain, */*");
		request.addRequestHeader("Origin", "http://www2.hm.com");
		request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		request.addRequestHeader("Content-Type", "application/json;charset=UTF-8");
		
		String body = "{\"variantCode\":\"" + itemId + "\"}";
		
		Result result = request.executeWithPost(body, "utf-8", true);

		
		if(result.getResponseCode() != HttpStatus.SC_OK) {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
	}
	
	
	
	
	private void getAvailableQuantity(String itemId) throws Exception {

		request.openConnection("http://www2.hm.com/ko_kr/cart/context");

		request.addRequestHeader("Host", "www2.hm.com");
		request.addRequestHeader("Origin", "http://www2.hm.com");
		request.addRequestHeader("Accept", "application/json, text/plain, */*");
		request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
		
		Result result = request.executeWithGet(true);
		String content = "";
		
		
		
		if(result.getResponseCode() == HttpStatus.SC_OK) {
			content = result.getString();
			parseAvailableQuantity(content, itemId);
		} else {
			exceptionBuilder.raiseException(ErrorType.ERROR_REQUEST_FAIL, result.getResponseCode());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseAvailableQuantity(String content, String itemId) throws Exception {
		Map<String, Object> cartData = mapper.readValue(content, Map.class);
		List<Map<String, Object>> cartEntries = (List<Map<String, Object>>)cartData.get("cartEntries");
		
		for(Map<String, Object> cartEntry : cartEntries) {
			if(itemId.equals((String)cartEntry.get("variantCode"))) {
				System.out.println("stock data: " + "color(" + (String)cartEntry.get("color") + "), maxQuantity(" + ((Integer)cartEntry.get("maxQuantity")).toString() +  "), productName(" + (String)cartEntry.get("productName") + "), variantCode(" + (String)cartEntry.get("variantCode") + ")");	
			}
		}
	}
	
	
	
}
