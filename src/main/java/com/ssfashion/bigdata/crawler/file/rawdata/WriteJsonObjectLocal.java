package com.ssfashion.bigdata.crawler.file.rawdata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.helpers.QuietWriter;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.ssfashion.bigdata.crawler.file.aws.RawDataFileManagement;
import com.ssfashion.bigdata.crawler.util.DateUtil;

public class WriteJsonObjectLocal implements Runnable {
	
	private static int takeCnt = 0;
	protected static Logger validationLogger = LoggerFactory.getLogger(OnlineStoreConst.VALIDATION_LOG);
	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);
	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36";
	private String[] arrKeys = {"origin","material","desc","dependentData","layer3","layer4","layer5" ,"layer6","bestYn","newYn","saleYn"};
	
	// private static ArrayBlockingQueue<Map<String, Object>> queue;
	private ArrayBlockingQueue<Map<String, Object>> queue;

	private int times;
	private String collectSite;
	private String brandName;
	private String localJsonPath;
	private RawDataFileManagement mgr;
	
	private HttpRequestService request;


	public WriteJsonObjectLocal(String brandName, String collectSite, String localJsonPath) {
		this.collectSite = collectSite;
		this.brandName = brandName;
		this.localJsonPath = localJsonPath;	
	}
	
	public WriteJsonObjectLocal(ArrayBlockingQueue<Map<String, Object>> queue
            ,String brandName, String collectSite, String localJsonPath) {
		this.queue = queue;
		
		this.collectSite = collectSite;
		this.brandName = brandName;
		this.localJsonPath = localJsonPath;
		
		this.mgr = new RawDataFileManagement(makeSiteName());
	}
	
	public WriteJsonObjectLocal(ArrayBlockingQueue<Map<String, Object>> queue
			,String brandName, String collectSite, String localJsonPath, HttpRequestService request) {
		System.out.println("WriteJsonObjectLocal1 --->  !!!!!!!!!!!!!" );
		this.queue = queue;
		this.collectSite = collectSite;
		this.brandName = brandName;
		this.localJsonPath = localJsonPath;
		this.mgr = new RawDataFileManagement(makeSiteName());
		this.request = request;
		System.out.println("WriteJsonObjectLocal2 --->  !!!!!!!!!!!!!" );
	}

	/**
	 * 큐에서 꺼낸 맵에 isEnd 라는 키의 값이 존재하면 마지막 작업으로 판단함. BY.HNC
	 * 
	 * @return
	 */
	public Map<String, Object> isJobEnd() {

		Map<String, Object> itemMap = null;
		try {
			System.out.println("takeCnt = " + ++takeCnt + ",\tqueue.size() -->" + queue.size());
			itemMap = ((Map<String, Object>) queue.poll(5, TimeUnit.MINUTES));
			//itemMap = ((Map<String, Object>) queue.take());
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Object isJobEnd = itemMap.get("isEnd");
		Object isJobEnd = itemMap == null ? true : false;

		boolean isEnd = (Boolean) isJobEnd;

		// 마지막 값인지 판단 후에는 isEnd, count 값 삭제
		if(itemMap != null)
		{
			itemMap.remove("isEnd");
			itemMap.remove("count");
		}
		if (isEnd) {
			return null;
		} else {
			return itemMap;
		}
	}

	public String makeSiteName() {
		String fileName = collectSite.replaceAll("\\.", "_");
		if (fileName.equals("www_gmarket_co_kr")) {
			fileName = fileName.concat("_").concat(brandName.toLowerCase());
		}

		return fileName;
	}

	

	// private String imageRelativePath = "";
	private File imageDirectory = null;

	// private String getImageRelativePath(String siteName, String fileName) {
	// return String.format("%s/%s/%s", imageRelativePath, siteName.toUpperCase(), fileName);
	// }

	private File getImageDirectory(String siteName) {
		File dir = new File(imageDirectory, siteName.toUpperCase());
		if (!dir.exists())
			dir.mkdirs();
		return dir;
	}

	/*
	 * public String convertFileName(String imageURL, String imageFileName) {
	 * 
	 * FileOutputStream fos = null; String fileName = imageFileName; Result result = null; try { Pattern pattern = Pattern.compile("\\.(jpg|jpeg|gif|png|bmp)$", Pattern.CASE_INSENSITIVE); Matcher matcher = pattern.matcher(imageURL.replaceAll("\\?.*", "")); boolean found = matcher.find();
	 * 
	 * if (found) fileName += "." + matcher.group(1).toLowerCase();
	 * 
	 * final int CONNECT_TIMEOUT = 30000; final int READ_TIMEOUT = 50000;
	 * 
	 * if (!found) { result = request.create(imageURL).config(HttpRequestService.KEY_CONNECT_TIMEOUT, CONNECT_TIMEOUT) .config(HttpRequestService.KEY_READ_TIMEOUT, READ_TIMEOUT).header("User-Agent", USER_AGENT).get(fos); int responseCode = result.getResponseCode();
	 * 
	 * if (responseCode == HttpStatus.SC_OK) { // 확장자가 없을 시에는 Response 헤더의 Content-Type을 가져온다.
	 * 
	 * Map<String, List<String>> header = result.getHeader(); if (header.containsKey("Content-Type")) { String contentType = header.get("Content-Type").get(0); if (contentType.matches("^image/.+$")) { fileName += "." + contentType.replaceAll("^image/(.+)$", "$1");
	 * 
	 * } }
	 * 
	 * } else if (responseCode == HttpStatus.SC_NOT_FOUND) { // relativePath = getImageRelativePath(siteName, "404"); } else { throw PException.buildException(WSErrorCode.ERROR_SAVE_IMAGE_FAIL, new RuntimeException("responseCode -> " + responseCode)); } } } catch (Exception e) { logger.error(getClass().getSimpleName(), e.getMessage(), e); throw PException.buildException(WSErrorCode.ERROR_SAVE_IMAGE_FAIL, e.getMessage(), e); } finally { IOUtils.closeQuietly(fos); }
	 * 
	 * return fileName; }
	 */

	/**
	 * JsonObject 를 파일로 쓴다.
	 * 
	 * @param str
	 * @throws IOException
	 */
	public void writeJsonString(JSONObject jsonObj, Writer out) {
		try {
			synchronized (out) {
				jsonObj.writeJSONString(out);
				out.append("\n");
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getCollectSite() {
		return collectSite;
	}

	public void setCollectSite(String collectSite) {
		this.collectSite = collectSite;
	}

	@Override
	public void run() {
		Map<String, Object> itemMap = null;
		File jsonFile = null;
		String filePath = null;
		String collectDay = null;
		while ((itemMap = isJobEnd()) != null) {
			//System.out.println("brandName ---> " + brandName);
			if(!"ZARA".equals(brandName)) {
				List<String> imageUrls = (List<String>)itemMap.get("imageUrl");
				List<String> s3ImageUrls = new ArrayList<>();
				System.out.println("imageUrls--->" + imageUrls.toString());
				StringBuffer strIdx = new StringBuffer(new Integer(0).toString());
				String itemNumber = (String) itemMap.get("productCode");
				imageUrls.forEach(url -> {
					try {
						int idx = new Integer(strIdx.toString());

						String imageFileName = itemNumber +  "_" + idx;
						if(url != null && url.length() > 0)
							s3ImageUrls.add(mgr.fileUpload(url, imageFileName, request));
						
						
						int length = strIdx.length();
						idx++;
						strIdx.delete(0, length);
						strIdx.append(new StringBuffer(new Integer(idx).toString()));
						
					} catch (Exception e) {
						logger.error(getClass().getSimpleName(), e.getMessage());
						e.printStackTrace();
					}
					
				});

				itemMap.put("s3ImageUrl", s3ImageUrls);
			}

			System.out.println(getClass().getSimpleName() + "====================================");
			System.out.println(getClass().getSimpleName() +itemMap.toString());
			System.out.println(getClass().getSimpleName() + "====================================");
			logger.debug(getClass().getSimpleName(), "====================================");
			logger.debug(getClass().getSimpleName(), itemMap.toString());
			logger.debug(getClass().getSimpleName(), "====================================");
			
			FileWriter fw = null;
			System.out.println(" collectDay ===>   " + itemMap.get("collectDay"));
			collectDay = itemMap.get("collectDay").toString();
			filePath = this.localJsonPath 
					+ "/" +  collectDay.substring(0, 4)
					+ "/" +  collectDay.substring(4, 6)
					+ "/" +  collectDay.substring(6, 8);
			
			String fileName = makeSiteName();
			fileName = fileName.concat(".json");
			
			
			File fileDir = new File(filePath);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			
			try {
				jsonFile = new File(fileDir, fileName);
				fw = new FileWriter(jsonFile, true);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			itemMap.remove("collectDay");
			validationData(itemMap);
			JSONObject jsonObj = new JSONObject(itemMap);
			
			writeJsonString(jsonObj, fw);

			itemMap = null;
			
		} // end while

		// BY.HNC
		// 파일 쓰기 종료 후 raw-data S3업로드	
		validationLogger.debug("jsonFile : " + jsonFile != null ? jsonFile.getName() : "null");
		validationLogger.debug("filePath : " + filePath);
		validationLogger.debug("collectDay : " + collectDay);
		System.out.println("jsonFile : " + jsonFile != null ? jsonFile.getName() : "null");
		if (jsonFile != null && filePath != null && collectDay != null) {
			validationLogger.info("=========================끝1===========================");
			logger.debug("jsonFile.getName()==>" + jsonFile.getName());
			mgr.saveTotalRawFile(collectDay, jsonFile);
			System.out.println();
			validationLogger.info("jsonFile.getName()==>" + jsonFile.getName());
			validationLogger.info("=========================끝2===========================");

		} else {
			logger.error(getClass().getSimpleName(), "Fail to create Json Raw-data File!");
		}
		
		writerThrEnd(brandName);
	}

	private void writerThrEnd(String brandCode) {
		
		switch (brandCode) {
		case OnlineStoreConst.BRAND_SPAO:
			OnlineStoreCrawler.writerSpao = null;
			validationLogger.info("writerSpao : null");
			break;
		case OnlineStoreConst.BRAND_MIXXO:
			OnlineStoreCrawler.writerMixxo = null;
			validationLogger.info("writerMixxo : null");
			break;
		case OnlineStoreConst.BRAND_HANDSOME:
			OnlineStoreCrawler.writerHandsome = null;
			validationLogger.info("writerHandsome : null");
			break;
		case OnlineStoreConst.BRAND_HNM:
			OnlineStoreCrawler.writerHm = null;
			validationLogger.info("writerHm : null");
			break;
		case OnlineStoreConst.BRAND_UNIQLO:
			OnlineStoreCrawler.writerUniqlo = null;
			validationLogger.info("writerUniqlo : null");
			break;
		case OnlineStoreConst.BRAND_ZARA:
			OnlineStoreCrawler.writerZara = null;
			validationLogger.info("writerZara : null");
			break;
		}
	}

	private void validationData(Map<String,Object> map) {
		try {
			List<String> listKeys = Arrays.asList(arrKeys);
			Set<String> productKeys = map.keySet();
			for (String key : productKeys) {
				
				if(!listKeys.contains(key)) {
					if(map.get(key) == null) {
						validationLogger.info(map.get("productCode") + " : " + key + " null ");
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			validationLogger.error("validationData : " + e.getMessage());
		}
		
	}
}
