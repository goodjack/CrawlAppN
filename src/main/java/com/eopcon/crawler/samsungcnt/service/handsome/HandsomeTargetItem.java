package com.eopcon.crawler.samsungcnt.service.handsome;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.util.JsoupConnect;
import com.eopcon.crawler.samsungcnt.util.ObjectToJson;



public class HandsomeTargetItem{
	private DataStandard dataStandard;
	private Document document;
	private List<DataStandard> targetItemPagingList;
	private List<DataStandard> targetItemList;
	private HandsomeLayerInfo handsomeLayerInfo;
	private ObjectToJson objectToJson;
	
	private String siteUrl;
	private String urlForm;
	
	public HandsomeTargetItem() {
		dataStandard = new DataStandard();
		targetItemPagingList = new ArrayList<DataStandard>();
		targetItemList = new ArrayList<DataStandard>();
		handsomeLayerInfo = new HandsomeLayerInfo();
		objectToJson = new ObjectToJson();
		
		siteUrl = "http://www.thehandsome.com";
		urlForm = "http://www.thehandsome.com/ko/c/categoryList?categoryCode=";
	}
//		System.out.println(siteUrl + dataStandard.getCategoryUrl());
	public List<DataStandard> crawlingTargetItem(DataStandard dataStandard ,int page) {

		//int categoryItemPageNum = handsomeLayerInfo.categoryItemCount(siteUrl + dataStandard.getCategoryUrl());	// http://www.thehandsome.com + /ko/c/we011
		
		//for (int pageNum=1; pageNum<=categoryItemPageNum; pageNum++) {
		String categoryItemUrl = urlForm + dataStandard.getCategoryCode() + "&pageNum=" + page;	// http://www.thehandsome.com/ko/c/categoryList?categoryCode= + we011 + &pageNum= + 1
		JsoupConnect jsoupConnect = new JsoupConnect();
		
		jsoupConnect.setJsoupConnet(categoryItemUrl);
		document = jsoupConnect.getJsoupConnect();
		try {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(document.text());
			targetItemPagingList = objectToJson.targetItemDetialInfo(dataStandard, jsonObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		targetItemList.addAll(targetItemPagingList);
		//}
		

		return targetItemList;
	}
	
	public List<DataStandard> crawlingTargetItem(DataStandard dataStandard) {

		System.out.println(" dataStandard.getCategoryUrl()" +  dataStandard.getCategoryUrl());
		int categoryItemPageNum = handsomeLayerInfo.categoryItemCount(siteUrl + dataStandard.getCategoryUrl());	// http://www.thehandsome.com + /ko/c/we011
		System.out.println("categoryItemPageNum" + categoryItemPageNum);
		
		for (int pageNum=1; pageNum<=categoryItemPageNum; pageNum++) {
			String categoryItemUrl = urlForm + dataStandard.getCategoryCode() + "&pageNum=" + pageNum;	// http://www.thehandsome.com/ko/c/categoryList?categoryCode= + we011 + &pageNum= + 1
			System.out.println("categoryItemUrl" + categoryItemUrl);
			JsoupConnect jsoupConnect = new JsoupConnect();
			
			jsoupConnect.setJsoupConnet(categoryItemUrl);
			document = jsoupConnect.getJsoupConnect();
	//			String text = document.text();
	//			System.out.println(text);
			try {
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(document.text());
				targetItemPagingList = objectToJson.targetItemDetialInfo(dataStandard, jsonObject);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetItemList.addAll(targetItemPagingList);
		}
		

		return targetItemList;
	}
}
