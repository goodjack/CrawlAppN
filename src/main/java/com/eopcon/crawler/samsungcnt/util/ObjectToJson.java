package com.eopcon.crawler.samsungcnt.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.eopcon.crawler.samsungcnt.model.Collectlog;
import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.model.GoodEval;
import com.eopcon.crawler.samsungcnt.service.handsome.HandsomeItemInfo;





public class ObjectToJson {
	
	CollectDateFormat collectDateFormat = new CollectDateFormat();
	private List<DataStandard> targetItemPagingList;
	private List<GoodEval> goodEvalPageList;
	private String flagSale = "SALE";
	private String flagBest = "BEST";
	private String flagNew = "NEW";
	private int j = 1;
	
	public void categoryToJson(List<DataStandard> categoryInfoList) {
		
	}
	
	
	public void errorLog(Exception e, String url) {
		e.printStackTrace();
		Collectlog collectLog = new Collectlog();
		collectLog.setCollectTime(collectDateFormat.getTodayTimeCollectDate());
		collectLog.setCollectDate(collectDateFormat.getTodayCollectDate());
		collectLog.setMessage(e.toString());
		collectLog.setTarget(url);
		collectLog.setClassName(getClass().getName());
		collectLog.setLogType("ERROR");
	}

	public void categorySucessLog(String siteName, int categoryTotal) {
		
		Collectlog collectLog = new Collectlog();
		collectLog.setCollectTime(collectDateFormat.getTodayTimeCollectDate());
		collectLog.setCollectDate(collectDateFormat.getTodayCollectDate());
		collectLog.setMessage(siteName +" Category Reult : " + categoryTotal);
		collectLog.setClassName(getClass().getName());
		collectLog.setLogType("INFO");
//		ElasticDao.insertCollectLog(collectLog);
		
	}

	public List<DataStandard> targetItemDetialInfo(DataStandard dataStandard, JSONObject jsonObject) {
		
		HandsomeItemInfo handsomeItemInfo = new HandsomeItemInfo();
		JSONArray resultJsonArr = (JSONArray) jsonObject.get("results");
		
		targetItemPagingList = new ArrayList<DataStandard>();
		for (int i=0; i<resultJsonArr.size(); i++) {
			JSONObject resultJsonObj = (JSONObject) resultJsonArr.get(i);
			DataStandard data = new DataStandard(dataStandard);
			
			
			// 정상가
			int normalPrice = (int)Float.parseFloat(String.valueOf(resultJsonObj.get("productNormalityPrice")));
			// 세일가
			int salePrice = (int)Float.parseFloat(String.valueOf(resultJsonObj.get("productPrice")));
			// saleYn
			String saleYn = handsomeItemInfo.itemFlagInfo(resultJsonObj, flagSale);
			// bestYn
			String bestYn = handsomeItemInfo.itemFlagInfo(resultJsonObj, flagBest);
			// newYn
			String newYn = handsomeItemInfo.itemFlagInfo(resultJsonObj, flagNew);
			// imageUrl
			List<String> imageUrlList = handsomeItemInfo.itemImageInfo(resultJsonObj);
			
			// productColorKeyList
			List<String> productColorKeyList = handsomeItemInfo.itemColorKeyInfo(resultJsonObj);	// [SJ1HAWTO032W_MB, SJ1HAWTO032W_PK]
			// productColor
			List<String> productColorList = handsomeItemInfo.itemColorCodeInfo(resultJsonObj);	// [#061836, #ea589b]
			// productSize_pre X
//			List<String> productSizeList_pre = handsomeItemInfo.itemSizeInfo_Pre(resultJsonObj);	// [SJ1HAWTO032W_MB:76,82,88, SJ1HAWTO032W_PK:76,88]
			
			// productSize
			List<String> productSizeList = handsomeItemInfo.itemSizeInfo(productColorKeyList);			
//			System.out.println("productSizeList : " +productSizeList);	// [SJ1HAWTO032W_MB:76,82,88, SJ1HAWTO032W_PK:76,88]
			// productStockKeyList_Pre X
//			List<String> productStockKeyList = handsomeItemInfo.itemStockKeyInfo_Pre(resultJsonObj);	// [SJ1HAWTO032W_MB_76, SJ1HAWTO032W_MB_82, SJ1HAWTO032W_MB_88, SJ1HAWTO032W_PK_76, SJ1HAWTO032W_PK_88]
			
			// productStockKeyList
			List<String> productStockKeyList = handsomeItemInfo.itemStockKeyInfo(productColorKeyList);
			// stockKey
//			List<String> stockKeyList = handsomeItemInfo.itemStockKeyInfo(resultJsonObj);	// [SJ1HAWTO032W_MB_76, SJ1HAWTO032W_MB_82, SJ1HAWTO032W_MB_88, SJ1HAWTO032W_PK_76, SJ1HAWTO032W_PK_88]
			
			// productStockList
			List<String> productStockList = handsomeItemInfo.itemStockValueInfo(productColorKeyList, productStockKeyList);	// [JN1HATTO114W_WN_82 : 2, JN1HATTO114W_WN_88 : 1, JN1HATTO114W_BK_82 : 2, JN1HATTO114W_BK_88 : 1]
			// productStockTotal
			int productStockTotalCnt = handsomeItemInfo.itemStockTotalInfo(productColorKeyList, productStockKeyList);
			
			// stockInfo
			String stockInfo = "totalStock:" + productStockTotalCnt + "||" + productStockList; 
			
			// crawlUrl
			String productItemUrl = handsomeItemInfo.itemUrlInfo(resultJsonObj);
			
			// crawlDate
			Date today = new Date();
			DateFormat todayFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			String todayStr = todayFormat.format(today);
			
			data.setSaleYn(saleYn);
//			System.out.print(dataStandard.getBestYn() + " ");
			data.setProductCode(String.valueOf(resultJsonObj.get("productBaseCode")));
			data.setNormalPrice(normalPrice);
			data.setProductSalePrice(salePrice);
			data.setBrandName(String.valueOf(resultJsonObj.get("productBrandName")));
			data.setImageUrl(imageUrlList);
			
			data.setProductName(String.valueOf(resultJsonObj.get("productName")));
//			data.setLayer3(String.valueOf(resultJsonObj.get("productBrandName")));
			data.setProductColor(productColorList);
			data.setProductSize(productSizeList);
			
			data.setCrawlDate(todayStr);
			data.setBestYn(bestYn);
			data.setNewYn(newYn);
			data.setCrawlUrl(productItemUrl);
			data.setStockInfo(stockInfo);
			data.setPageSize(i+1);
			System.out.println(j++ +  " " + data.getCategoryCode() + " : " + data);
			targetItemPagingList.add(data);
			

		}
		return targetItemPagingList;
	}

	public List<GoodEval> targetItemReviewInfo(JSONObject jsonObject) {
		
		JSONObject jsonResultObj = (JSONObject) jsonObject.get("reviewList");
		JSONArray resultJsonArr = (JSONArray) jsonResultObj.get("results");
		
		goodEvalPageList = new ArrayList<GoodEval>();
		for (int i=0; i<resultJsonArr.size(); i++) {
			JSONObject reviewJsonObj = (JSONObject) resultJsonArr.get(i);
			GoodEval goodEval = new GoodEval();
			goodEval.setComment(String.valueOf(reviewJsonObj.get("headline")));
			goodEvalPageList.add(goodEval);
		}
		return goodEvalPageList;
	}

	public JSONArray targetItemReviewToJson(List<GoodEval> goodEvalList) {
		
		JSONObject goodEvalJsonObj = new JSONObject();
		JSONArray goodEvalArrObj = new JSONArray();
		
		for (int i=0; i<goodEvalList.size(); i++) {
			JSONObject obj = new JSONObject();
			obj.put("comment", goodEvalList.get(i).getComment());
			goodEvalArrObj.add(obj);
		}
//		goodEvalJsonObj.put("goodEval", goodEvalArrObj);
		return goodEvalArrObj;
	}

	
	public void handsomeToJSon(DataStandard productItem) {
		
		HandsomeToFile handsomeToFile = new HandsomeToFile();
		JSONArray jsonArr = new JSONArray();
		
		
		
		JSONObject obj = new JSONObject();
		obj.put("saleYn", productItem.getSaleYn());
		obj.put("productCode", productItem.getProductCode());
		obj.put("normalPrice", productItem.getNormalPrice());
		obj.put("productSalePrice", productItem.getProductSalePrice());
		obj.put("brandName", productItem.getBrandName());
		obj.put("imageUrl", productItem.getImageUrl());
		obj.put("s3ImageUrl", productItem.getS3ImageUrl());
		obj.put("productName", productItem.getProductName());
		obj.put("layer1", productItem.getLayer1());
		obj.put("layer2", productItem.getLayer2());
		obj.put("layer3", productItem.getLayer3());
		obj.put("layer4", productItem.getLayer4());
		obj.put("layer5", productItem.getLayer5());
		obj.put("layer6", productItem.getLayer6());
		obj.put("productColor", productItem.getProductColor());
		obj.put("productSize", productItem.getProductSize());
		obj.put("material", productItem.getMaterial());
		obj.put("origin", productItem.getOrigin());
		obj.put("stockInfo", productItem.getStockInfo());
		obj.put("crawlDate", productItem.getCrawlDate());
		obj.put("crawlUrl", productItem.getCrawlUrl());
		obj.put("bestYn", productItem.getBestYn());
		obj.put("newYn", productItem.getNewYn());
		obj.put("goodEval", productItem.getGoodEval());
		obj.put("desc", productItem.getDesc());
		obj.put("dependenData", productItem.getDependentData());
		
		handsomeToFile.jsonToFile(obj);
		
//		System.out.println(jsonArr);
		
	}
}
