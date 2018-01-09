package com.eopcon.crawler.samsungcnt.service.handsome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.eopcon.crawler.samsungcnt.model.GoodEval;
import com.eopcon.crawler.samsungcnt.util.HandsomeToFile;
import com.eopcon.crawler.samsungcnt.util.JsoupConnect;
import com.eopcon.crawler.samsungcnt.util.ObjectToJson;
import com.eopcon.crawler.samsungcnt.util.SeleniumConnect;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class HandsomeItemInfo {

	private String siteItemUrlForm = "http://www.thehandsome.com/ko/p/";
	private String siteReviewUrlForm = "http://www.thehandsome.com/ko/p/review?productCode=";
	private String siteStockUrlForm = "http://www.thehandsome.com/ko/p/productDetailAjax.json?code=";
	private ObjectToJson objectToJson = new ObjectToJson();
	private HandsomeToFile handsomeToFile = new HandsomeToFile();
	private Document document;
	
	public String itemFlagInfo(JSONObject resultJsonObj, String flagType) {
		
		String flag = String.valueOf(resultJsonObj.get("productFlag")).toUpperCase();
		
		String flagYn = "N";
		if (flag.contains(flagType)) {
			flagYn = "Y";
		}
		return flagYn;
	}

	public List<String> itemImageInfo(JSONObject resultJsonObj) {
		
		List<String> imageUrlList = new ArrayList<String>();
		List<String> imageUrlTempList = new ArrayList<String>();
		String productImageUrl1 = String.valueOf(resultJsonObj.get("productImageUrl1"));
		String productImageUrl2 = String.valueOf(resultJsonObj.get("productImageUrl2"));
		String productStyleCode = String.valueOf(resultJsonObj.get("productStyleCode"));	// YN1H3WTO558W_IV
		
		JSONObject colorTempObj = (JSONObject) resultJsonObj.get("productColorTemp");
		List<String> productColorList = new ArrayList<>();
		try {
			// json을  map 으로 변환
			Map<String, List<String>> colorMap = new ObjectMapper().readValue(colorTempObj.toJSONString(), Map.class);
			
			for (Map.Entry<String, List<String>> entry : colorMap.entrySet()) {
				String colorKey = entry.getKey();
//				List<String> colorValueList = entry.getValue();
				productColorList.add(colorKey);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		imageUrlTempList.add(productImageUrl1);
		imageUrlTempList.add(productImageUrl2);
		imageUrlList.add(productImageUrl1);
		imageUrlList.add(productImageUrl2);
		
		// 다른 색상 이미지 추가
		for (String imageUrlTemp : imageUrlTempList) {
			for(String productColor : productColorList) {
				if(!productStyleCode.equals(productColor)) {
					String imageUrl = imageUrlTemp.replace(productStyleCode, productColor);
					imageUrlList.add(imageUrl);
				}
			}
		}
		// 이미지 파일 저장
//		handsomeToFile.imageToFile(imageUrlList);
		
		return imageUrlList;
	}

	public List<String> itemColorCodeInfo(JSONObject resultJsonObj) {
		
		JSONObject colorTempObj = (JSONObject) resultJsonObj.get("productColorTemp");
		List<String> productColorList = new ArrayList<>();
		try {
			Map<String, List<String>> colorMap = new ObjectMapper().readValue(colorTempObj.toJSONString(), Map.class);
			
			for (Map.Entry<String, List<String>> entry : colorMap.entrySet()) {
				String colorKey = entry.getKey();
				List<String> colorValueList = entry.getValue();
				productColorList.add(colorValueList.get(0));
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return productColorList;
	}
	
	public List<String> itemColorKeyInfo(JSONObject resultJsonObj) {
		
		JSONObject colorTempObj = (JSONObject) resultJsonObj.get("productColorTemp");
		List<String> productColorKeyList = new ArrayList<>();
		try {
			Map<String, List<String>> colorMap = new ObjectMapper().readValue(colorTempObj.toJSONString(), Map.class);
			
			for (Map.Entry<String, List<String>> entry : colorMap.entrySet()) {
				String colorKey = entry.getKey();
//				List<String> colorValueList = entry.getValue();
				productColorKeyList.add(colorKey);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return productColorKeyList;
	}
	
	public List<String> itemSizeInfo_Pre(JSONObject resultJsonObj) {
		
		JSONObject sizeTempObj = (JSONObject) resultJsonObj.get("productStyleSizeTemp");
//		JSONObject sizeJsonObj = new JSONObject();
		List<String> sizeCodeList = new ArrayList<>();
		
		try {
			Map<String, List<String>> sizeMap = new ObjectMapper().readValue(sizeTempObj.toJSONString(), Map.class);	// Json to Map
			
			for (Map.Entry<String, List<String>> entry : sizeMap.entrySet()) {
				String sizeKey = entry.getKey();
				List<String> sizeValueList = entry.getValue();
				String codeAndSize = sizeKey + ":" + sizeValueList.get(0);
				sizeCodeList.add(codeAndSize);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sizeCodeList;
	}
	
	// 상품 url 
	public String itemUrlInfo(JSONObject resultJsonObj) {
		
		String productBaseCode = String.valueOf(resultJsonObj.get("productStyleCode"));
		String itemUrlInfo = siteItemUrlForm + productBaseCode;
		
		return itemUrlInfo;
	}

	// material 정보
	public String itemMaterialInfo(Document document) {
		Elements strong = document.select("strong:nth-child(3)");
		String strongText = strong.text().toString();
		String materialText = strongText.substring(strongText.indexOf(":") + 1);
		
		return materialText.trim();
	}

	// origin 정보
	public String itemOriginInfo(Document document) {
		Elements strong = document.select("strong:nth-child(6)");
		String strongText = strong.text().toString();
		String originText = strongText.substring(strongText.indexOf(":") + 1);
		return originText.trim();
	}

	// desc 정보
	public String itemDescInfo(Document document) {
		Elements p = document.select("#contentDiv > div.info > div:nth-child(1) > p.item_txt");
		String descText = p.text();
		return descText;
	}
	
	// 상품평 수집
	public JSONArray itemGoodEvalInfo(String productCode, Elements reviewCnt) {
		int reviewTotalNum = Integer.parseInt(String.valueOf(reviewCnt.text()));
		int reviewPageNum = (int) Math.ceil((double)reviewTotalNum/10);	// review 페이징 수
		
		JSONArray goodEvalJSonArr = new JSONArray();
		List<GoodEval> goodEvalPageList = new ArrayList<GoodEval>();
		List<GoodEval> goodEvalList = new ArrayList<GoodEval>();
		for (int pageNum=1; pageNum<=reviewPageNum; pageNum++) {
			
			// 상품평 url
			String reviewUrl = siteReviewUrlForm + productCode + "&pageNum=" + pageNum + "&pageSize=10";
			SeleniumConnect seleniumConnect = new SeleniumConnect();
			String htmlContent = seleniumConnect.getPhantomJSConnect(reviewUrl);
			document = Jsoup.parse(htmlContent);
//			JsoupConnect.setJsoupConnet(reviewUrl);
//			document = JsoupConnect.getJsoupConnect();
			
			try {
				JSONParser jsonParser = new JSONParser();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(document.text());
				// 상품평 수집
				goodEvalPageList = objectToJson.targetItemReviewInfo(jsonObject);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			goodEvalList.addAll(goodEvalPageList);			
		}
		// 상품평 json 타입으로 변환
		goodEvalJSonArr = objectToJson.targetItemReviewToJson(goodEvalList);
		return goodEvalJSonArr;
	}

	public List<String> itemStockKeyInfo_Pre(JSONObject resultJsonObj) {
		
		
		JSONObject stockTempObj = (JSONObject) resultJsonObj.get("productStyleSizeTemp");
		
		List<String> stockKeyList = new ArrayList<>();
		
		try {
			Map<String, List<String>> sizeMap = new ObjectMapper().readValue(stockTempObj.toJSONString(), Map.class);	// Json to Map
			
			for (Map.Entry<String, List<String>> entry : sizeMap.entrySet()) {
				String stockKey = entry.getKey();
				List<String> stockValueList = entry.getValue();
				List<String> stockList = itemStockList(stockValueList.get(0));
				
				for (String stock : stockList) {
					String codeAndSize = stockKey + "_" + stock;
					stockKeyList.add(codeAndSize);
				}
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stockKeyList;
	}
	
	private List<String> itemStockList(String sizeValue) {
		List<String> sizeList = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(sizeValue, ",");
		while(tokenizer.hasMoreTokens()) {
			sizeList.add(tokenizer.nextToken());
		}
		return sizeList;
	}

	public List<String> itemStockValueInfo(List<String> productColorKeyList, List<String> productStockKeyList) {
		
		List<Integer> stockValueList = new ArrayList<Integer>();	// 재고 리스트 합
		List<String> stockKeyValueList = new ArrayList<String>();
//		List<String> stockList = new ArrayList<String>();
		for (String productCode : productColorKeyList) {
			String itemStockUrl = siteStockUrlForm + productCode;	// http://www.thehandsome.com/ko/p/productDetailAjax.json?code= + SY1H7WTO170W_PP
			// 색상별 재고 정보 함수 호출 
			stockValueList.addAll(itemStockCrawl(itemStockUrl, productStockKeyList));
		}
		stockKeyValueList = itemStockInfo(productStockKeyList, stockValueList);
//		stockList.addAll(stockValueList);
		return stockKeyValueList;
	}
	
	public int itemStockTotalInfo(List<String> productColorKeyList, List<String> productStockKeyList) {
		List<Integer> stockValueList = new ArrayList<Integer>();	// 재고 리스트 합
		List<String> stockKeyValueList = new ArrayList<String>();
//		List<String> stockList = new ArrayList<String>();
		for (String productCode : productColorKeyList) {
			String itemStockUrl = siteStockUrlForm + productCode;	// http://www.thehandsome.com/ko/p/productDetailAjax.json?code= + SY1H7WTO170W_PP
			// 색상별 재고 정보 함수 호출 
			stockValueList.addAll(itemStockCrawl(itemStockUrl, productStockKeyList));
		}
		
		int itemStockTotal = itemStockTotalSum(stockValueList);
		
		return itemStockTotal;
	}

	// 재고 정보
	private List<Integer> itemStockCrawl(String itemStockUrl, List<String> productStockKeyList) {
		
		List<Integer> stockPartSumList = new ArrayList<Integer>();
	    List<Integer> stockSumList = new ArrayList<Integer>();
	    JsoupConnect jsoupConnect = new JsoupConnect();
	    jsoupConnect.setJsoupConnet(itemStockUrl);
	    Document document = jsoupConnect.getJsoupConnect();
	    
		Elements els = document.select("script");
	    String scriptTag = els.first().toString();
	    int start = scriptTag.indexOf("stockOnlineStockpile[\"");
	    int end = scriptTag.indexOf("$(\".btn_close\"");
	    String stockStr = scriptTag.substring(start, end);
	    
	    String stockReplace = stockStr.replace("stockOnlineStockpile", "").replace("warehouseStockpile", "");
	    
	    for(String productStockKey : productStockKeyList) {
	    	stockReplace = stockReplace.replace("[\"" + productStockKey + "\"] = \"", "");
	    	
	    }
	    
	    StringTokenizer stn = new StringTokenizer(stockReplace.trim(), "\";");
	    List<String> stockNumList = new ArrayList<String>();
	    while(stn.hasMoreTokens()) {
	    	String token = stn.nextToken();
	    	token = token.replaceAll("\\s", "");
	    	
	    	stockNumList.add(token);
	    }
	    stockPartSumList = itemStockSum(stockNumList);
	    stockSumList.addAll(stockPartSumList);
	    
	    return stockSumList;
		
	}
	// 상품별 재고합
	private List<Integer> itemStockSum(List<String> stockNumList) {
		
		int stockNum = 0;
		List<Integer> stockList = new ArrayList<Integer>();
		try {
			for (int i=0; i<stockNumList.size(); i++) {
				if(i%2 == 0) {
					stockNum = Integer.parseInt(stockNumList.get(i));
				} else if (i%2 != 0) {
					stockNum = stockNum + Integer.parseInt(stockNumList.get(i));
					stockList.add(stockNum);
				}
			}	
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		
		return stockList;
	}
	// 상품 재고 총합
	private int itemStockTotalSum(List<Integer> stockValueList) {
		
		int stockTotalNum = 0;
		for (int i=0; i<stockValueList.size(); i++) {
			stockTotalNum = stockTotalNum + stockValueList.get(i);
		}
		return stockTotalNum;
	}
	

	private List<String> itemStockInfo(List<String> productStockKeyList, List<Integer> stockValueList) {
		
		List<String> productStockList = new ArrayList<String>();
		try {
			
			for(int i=0; i<productStockKeyList.size(); i++) {
				String stockKeyValue = productStockKeyList.get(i) + ":" + stockValueList.get(i);
				productStockList.add(stockKeyValue);
			}
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
		return productStockList;
	}

	public List<String> itemSizeInfo(List<String> productColorKeyList) {
		
		List<String> sizeKeyValueList = new ArrayList<String>();
		for (String productCode : productColorKeyList) {
			String itemStockUrl = siteStockUrlForm + productCode;	// http://www.thehandsome.com/ko/p/productDetailAjax.json?code= + SY1H7WTO170W_PP
			// 색상별 재고 정보 함수 호출 
			sizeKeyValueList.add(itemSizeCrawl(itemStockUrl, productCode));
		}
		return sizeKeyValueList;
	}

	private String itemSizeCrawl(String itemStockUrl, String productCode) {
		
		List<String> sizeValueList = new ArrayList<String>();
		List<String> sizeKeyValueList = new ArrayList<String>();
		
		JsoupConnect jsoupConnect = new JsoupConnect();
		
		jsoupConnect.setJsoupConnet(itemStockUrl);
	    Document document = jsoupConnect.getJsoupConnect();
	    
	    for(Element elSize : document.select("#contentDiv > div.info > div:nth-child(3) > ul > li:nth-child(2) > span.txt > ul > li")) {
	    	String sizeId = elSize.attr("id");
	    	String sizeValue = elSize.select(">a").text();
	    	sizeValueList.add(sizeValue);
	    }
	    
	    // 배열을 String으로 변환  [XS, S] -> "XS, S"
	    String sizeValue = "";
	    for (int i=0; i<sizeValueList.size(); i++) {
	    	if (i != sizeValueList.size()-1) {
	    		sizeValue += sizeValueList.get(i) + ",";
	    	} else if(i == sizeValueList.size()-1) {
	    		sizeValue += sizeValueList.get(i);
	    	}
	    	
	    }
	    String sizeKeyValue = productCode + ":" + sizeValue;
	    
		return sizeKeyValue;
	}

	public List<String> itemStockKeyInfo(List<String> productColorKeyList) {
		
		List<String> stockKeyValueList = new ArrayList<String>();
		for (String productCode : productColorKeyList) {
			String itemStockUrl = siteStockUrlForm + productCode;	// http://www.thehandsome.com/ko/p/productDetailAjax.json?code= + SY1H7WTO170W_PP
			// 색상과 사이즈가 더해진 상품명 수집
			stockKeyValueList.addAll(itemStockCrawl(itemStockUrl));
		}
		return stockKeyValueList;
	}

	private List<String> itemStockCrawl(String itemStockUrl) {
		List<String> stockIdList = new ArrayList<String>();
		
		JsoupConnect jsoupConnect = new JsoupConnect();
		
		jsoupConnect.setJsoupConnet(itemStockUrl);
	    Document document = jsoupConnect.getJsoupConnect();
	    
	    for(Element elStockId : document.select("#contentDiv > div.info > div:nth-child(3) > ul > li:nth-child(2) > span.txt > ul > li")) {
	    	String stockId = elStockId.attr("id");
	    	stockIdList.add(stockId);
	    }
	    
		return stockIdList;
	}
	
	
}
