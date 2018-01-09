package com.eopcon.crawler.samsungcnt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeTargetItem;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeTargetItemDetail;


 
public class ItemCrawler {

	private List<DataStandard> targetItemList;
	private List<DataStandard> targetItemDetailList;
	
	private HandsomeTargetItemDetail handsomeTargetItemDetail;
	public ItemCrawler() {
		targetItemList = new ArrayList<DataStandard>();
		targetItemDetailList = new ArrayList<DataStandard>();
		
		handsomeTargetItemDetail = new HandsomeTargetItemDetail();
	}
	
	public List<DataStandard> targetItemCrawl(DataStandard dataStandard) {
		
		CollectDateFormat collectDateFormat = new CollectDateFormat();
		long startTime = System.currentTimeMillis();
		
		/**
		 * 아이템 크롤링
		 */
		HandsomeTargetItem handsomeTargetItem = new HandsomeTargetItem();
		targetItemList = handsomeTargetItem.crawlingTargetItem(dataStandard);
		
		/**
		 * 아이템상세 크롱링
		 */
		targetItemDetailList = handsomeTargetItemDetail.crawlingTargetItemDetail(targetItemList);
		
		
		int t=1;
		for(DataStandard data : targetItemDetailList) {
			System.out.println(t++ + " "  + data.getCategoryCode() + " targetItemDetailList : " + data);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("##  시작시간 : " + collectDateFormat.formatTime(startTime));
	    System.out.println("##  종료시간 : " + collectDateFormat.formatTime(endTime));
	    System.out.println("##  카테고리아이템 소요시간(초.0f) : " + ( endTime - startTime )/1000.0f +"초");
	    
	    return targetItemDetailList;
	}

}
