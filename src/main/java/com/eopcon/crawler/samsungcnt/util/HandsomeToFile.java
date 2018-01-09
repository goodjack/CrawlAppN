package com.eopcon.crawler.samsungcnt.util;


import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;



public class HandsomeToFile {

	CollectDateFormat collectDateFormat = new CollectDateFormat();
	public void jsonToFile(JSONArray jsonArr) {
		String today = collectDateFormat.getDailyCollectDate();
		String fileName = "c://handsomeJsonFile/" + today + ".json";
		
		try {
			FileWriter fw = new FileWriter(fileName,true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(jsonArr.toJSONString());
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void jsonToFile(JSONObject jsonObject) {
		String today = collectDateFormat.getDailyCollectDate();
		String fileName = "c://handsomeJsonFile/" + today + ".json";
		
		try {
			FileWriter fw = new FileWriter(fileName,true);
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(jsonObject.toJSONString());
			bw.flush();
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// 이미지 저장
	public void imageToFile(List<String> imageUrlList) {
		
		String imageFormat = "jpg";
		for (String imageUrl : imageUrlList ) {
			String imageFilePath = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
			
			try {
				// image 가져오기
				BufferedImage image = ImageIO.read(new URL(imageUrl));
				
				// image 저장할 파일
				File imageFile = new File("C://handsomeImage/" + imageFilePath);
				
				// 이미지 저장
				ImageIO.write(image, imageFormat, imageFile);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
