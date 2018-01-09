package com.eopcon.crawler.samsungcnt.service.handsome;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.util.JsoupConnect;

public class HandsomeTargetItemDetail {
	private Document document;
	private Document ddDocument;
	private HandsomeItemInfo handsomeItemInfo;
	private List<DataStandard> targetItemDetailList;
	

	public HandsomeTargetItemDetail() {
		
		handsomeItemInfo = new HandsomeItemInfo();
	}


	public List<DataStandard> crawlingTargetItemDetail(List<DataStandard> targetItemList) {
		
		targetItemDetailList = new ArrayList<DataStandard>();
		int d=1;
		for (DataStandard dataStandard : targetItemList) {
			JsoupConnect jsoupConnect = new JsoupConnect();
			
			jsoupConnect.setJsoupConnet(dataStandard.getCrawlUrl());
			document = jsoupConnect.getJsoupConnect();
			
			JSONArray goodEvalJSonArr = new JSONArray();
			Elements dd = document.select("#contentDiv > div.info > dl > dd:nth-child(2)");
			Elements reviewCnt = document.select("#customerReviewCnt");	// goodEval
			String ddText = dd.toString();
			String ddContent = ddText.replaceAll("</strong>", "").replaceAll("<br>", "</strong>");	// material, origin
			
			ddDocument = Jsoup.parse(ddContent);
			
			goodEvalJSonArr = handsomeItemInfo.itemGoodEvalInfo(dataStandard.getProductCode(), reviewCnt); 
			String material = handsomeItemInfo.itemMaterialInfo(ddDocument);
			String origin = handsomeItemInfo.itemOriginInfo(ddDocument);
			String desc = handsomeItemInfo.itemDescInfo(document);
			dataStandard.setMaterial(material);
			dataStandard.setOrigin(origin);
			dataStandard.setDesc(desc);
			dataStandard.setGoodEval(goodEvalJSonArr);
			System.out.println(d++ + " targetItemDetail : " + dataStandard);
			targetItemDetailList.add(dataStandard);
		}
		return targetItemDetailList;
	}
	
}


	
	
	