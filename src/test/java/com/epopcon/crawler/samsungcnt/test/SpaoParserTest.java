package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.parser.impl.SpaoStoreParser;

public class SpaoParserTest {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		test1();
		//test2();
	}

	public static void test1() throws Exception {

		SpaoStoreParser parser = new SpaoStoreParser(OnlineStoreConst.SPAO);

		//String content = readContent("/template/spao/materials2.txt");
		//String content = "GREY 겉감 : 면 58 % , 폴리에스터 42 %, INDIGO 겉감 : 면 87 % , 폴리에스터 13 %, 나머지 겉감 : 면 100%";
		//String content = "L/GREY-폴리에스터100% NAVY-레이온100%";
		String content = "면67.5% 폴리에스터30.6% 폴리우레탄1.9%";
		String[] colors = new String[] { "INDIGO"};
		// String[] colors = new String[] {"GREY", "NAVY", "BURGANDY", "IVORY",
		// "BLACK", "KHAKI", "MUSTARD YELLOW"};

		List<Materials> materials = parser.parseMarterialsString(content, colors, null);
		System.out.println(materials.size());

	}

	public static void test2() throws IOException {
		//String content = readContent("/template/spao/materials2.txt");
		//String colorContent = readContent("/template/spao/color.txt");
		String content = "라이트 그레이-폴리에스터100% 네이비-레이온100%";
		content = content.replaceAll("라이트 베이지", "L/BEIGE");
		content = content.replaceAll("L/블루", "L/BLUE");
		content = content.replaceAll("D/핑크", "D/PINK");
		content = content.replaceAll("M/그레이", "M/GREY");
		content = content.replaceAll("L/그레이", "L/GREY");
		content = content.replaceAll("D/그레이", "D/GREY");
		content = content.replaceAll("다크 레드", "D/RED");
		content = content.replaceAll("라이트 퍼플", "L/PURPLE");
		content = content.replaceAll("라이트 핑크", "L/PINK");
		content = content.replaceAll("라이트핑크", "L/PINK");
		content = content.replaceAll("라이트 블루", "L/BLUE");
		content = content.replaceAll("라이트블루", "L/BLUE");
		content = content.replaceAll("다크 그레이", "D/GREY");
		content = content.replaceAll("다크그레이", "D/GREY");
		content = content.replaceAll("멜란지 그레이", "M/GREY");
		content = content.replaceAll("멜란지그레이", "M/GREY");
		content = content.replaceAll("더스트 화이트", "DST WHITE");
		content = content.replaceAll("더스트화이트", "DST WHITE");
		content = content.replaceAll("아쿠아 스카이", "AQUA SKY");
		content = content.replaceAll("더스트 블랙", "DUST BLACK");
		content = content.replaceAll("더스트블랙", "DUST BLACK");
		content = content.replaceAll("라이트 그레이", "L/GREY");
		content = content.replaceAll("라이트그레이", "L/GREY");
		content = content.replaceAll("다크 블루", "D/BLUE");
		content = content.replaceAll("미디엄 그레이", "M/GREY");
		content = content.replaceAll("레드", "RED");
		content = content.replaceAll("블루", "BLUE");
		content = content.replaceAll("베이지", "BEIGE");
		content = content.replaceAll("블랙", "BLACK");
		content = content.replaceAll("화이트", "WHITE");
		content = content.replaceAll("네이비", "NAVY");
		content = content.replaceAll("헌터", "HUNTER");
		content = content.replaceAll("카키", "KHAKI");
		content = content.replaceAll("로얄", "ROYAL");
		content = content.replaceAll("민트", "MINT");
		content = content.replaceAll("그레이", "GREY");
		content = content.replaceAll("옐로우", "YELLOW");
		content = content.replaceAll("차콜", "CHARCOAL");
		content = content.replaceAll("챠콜", "CHARCOAL");
		content = content.replaceAll("버건디", "BURGUNDY");
		content = content.replaceAll("브라운", "BROWN");
		content = content.replaceAll("인디고", "INDIGO");
		content = content.replaceAll("오트밀", "OATMEAL");
		content = content.replaceAll("믹스", "MIX");
		content = content.replaceAll("핑크", "PINK");
		content = content.replaceAll("아이보리", "IVORY");
		content = content.replaceAll("에메랄드", "EMERALD");
		content = content.replaceAll("퍼플", "PURPLE");
		
		System.out.println(content);

		/*colorContent = colorContent.replaceAll("\\(.*?\\)", "");
		System.out.println(colorContent);*/
		/*
		 * for(String str : content.split("\n")){ System.out.println(str); }
		 */

	/*	SpaoStoreParser parser = new SpaoStoreParser(OnlineStoreConst.SPAO);

		String[] strMaterial = content.split("\n");
		String[] strColor = colorContent.split("\n");
		List<Materials> materialList = new ArrayList<>();
		for (int i = 0; i < strMaterial.length; i++) {
			try {
				String text = "";
				String[] colors;
				text = strMaterial[i].toString();
				colors = strColor[i].split(";");

				System.out.println(text);
				System.out.println(strColor[i]);

				materialList = parser.parseMarterialsString(text, colors);
				System.out.println(materialList.size());
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/
	}

	private static String readContent(String path) throws IOException {
		InputStream stream = null;
		BufferedReader reader = null;
		try {
			stream = Test.class.getResourceAsStream(path);
			reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));

			StringBuilder sb = new StringBuilder();
			String s;

			while ((s = reader.readLine()) != null) {
				sb.append(s + "\n");
			}

			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(stream);
			IOUtils.closeQuietly(reader);
		}
		return null;
	}
}