package com.eopcon.crawler.samsungcnt.service.handsome;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.util.CollectDateFormat;
import com.eopcon.crawler.samsungcnt.util.ItemCrawler;
import com.eopcon.crawler.samsungcnt.util.ObjectToJson;
import com.eopcon.crawler.samsungcnt.util.SeleniumConnect;


public class HandsomeCategoryInfo {
	
	private ObjectToJson objectToJson;
	private List<DataStandard> categoryInfo_AllList;
	private ItemCrawler itemCrawler;
	private HandsomeLayerInfo handsomeLayerInfo;
	private List<DataStandard> targetItemPagingList1;
	private List<DataStandard> targetItemPagingList2;
	private List<DataStandard> productItem_AllList;
	
	String siteUrl;
	
	public HandsomeCategoryInfo() {
		objectToJson = new ObjectToJson();
		itemCrawler = new ItemCrawler();
		handsomeLayerInfo = new HandsomeLayerInfo();
		targetItemPagingList1 = new ArrayList<DataStandard>();
		targetItemPagingList2 = new ArrayList<DataStandard>();
		categoryInfo_AllList = new ArrayList<DataStandard>();
		productItem_AllList = new ArrayList<DataStandard>();
		siteUrl = "http://www.thehandsome.com/ko";
	}
	public void categoryCrawl() {
//		categoryInfo_AllList = new ArrayList<DataStandard>();
		categoryInfo_AllList = crawlingCategoryInfo(siteUrl);
		CollectDateFormat collectDateFormat = new CollectDateFormat();
		long startTime = System.currentTimeMillis();
		
		int i = 1;
		for (DataStandard dataStandard : categoryInfo_AllList) {
			System.out.println(i + " category= " + dataStandard);
			i++;
//			System.out.println(dataStandard);
			String categoryCode = handsomeLayerInfo.categoryCode(dataStandard.getCategoryUrl());
			dataStandard.setCategoryCode(categoryCode);
			productItem_AllList = itemCrawler.targetItemCrawl(dataStandard);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("##  시작시간 : " + collectDateFormat.formatTime(startTime));
	    System.out.println("##  종료시간 : " + collectDateFormat.formatTime(endTime));
	    System.out.println("##  전체 아이템 소요시간(초.0f) : " + ( endTime - startTime )/1000.0f +"초");
		
	}
	private List<DataStandard> crawlingCategoryInfo(String siteUrl) {
		SeleniumConnect seleniumConnect = new SeleniumConnect();
		String htmlContent = seleniumConnect.getPhantomJSConnect(siteUrl);
		Document document = Jsoup.parse(htmlContent);
		
		List<DataStandard> categoryInfo_List = new ArrayList<DataStandard>();
		try{
			for (int i=1; i<=2; i++) {
				for (Element level1 : document.select("#cate_m_main > li:nth-child(" + i + ")")) {
					
					
					String gender = level1.select(" > a").text().substring(0, 2);

					for(Element level2 : level1.select("div > div > ul > li")) {
						String level2Name = level2.select("> a").text();
						
						if(!("전체보기".equals(level2Name))) {	// 전체보기 제거
							
							for(Element level3 : level2.select("ul > li > a")) {
								String level3Name = level3.text();
								String categoryUrl = level3.attr("href");
								DataStandard dataStandard = new DataStandard();
								handsomeLayerInfo = new HandsomeLayerInfo();
								
								
								// layer 4,5,6
//								dataStandard = handsomeLayerInfo.layer4Set(gender);								
//								dataStandard = handsomeLayerInfo.layer5Set(level2Name.replaceAll("\\p{Z}", ""));	// 공백제거				
//								dataStandard = handsomeLayerInfo.layer6Set(level2Name.replaceAll("\\p{Z}", ""), level3Name.replaceAll("\\p{Z}", ""));	// 공백제거
								
								dataStandard.setLayer1("자사몰");
								dataStandard.setLayer2("한섬");
								dataStandard.setCategoryUrl(categoryUrl);
								
								categoryInfo_List.add(dataStandard);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			objectToJson.errorLog(e, siteUrl);
		}
		
		return categoryInfo_List;
	}
	
}
