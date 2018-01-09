package com.epopcon.crawler.samsungcnt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class TempTest {

	public static void main(String[] args) {
		
		Set<String> set = new HashSet<>();
		
		set.add("카테고리>기획전>스타착용");
		set.add("HEART CLUB");
		set.add("HEART CLUB");
		set.add("상의>반팔");
		
		System.out.println(StringUtils.join(set, ";"));
		
		/*
		final String[] HEADER = new String[] { "구글애드아이디", "자주와 아이디", "상점명", "상점아이디", "카테고리(대)", "금액", "카드명(전체)", "카드명", "카드발급회사", "구매일자", "라이프존1 위도", "라이프존1 경도", "라이프존2 위도", "라이프존2 경도", "라이프존3 위도", "라이프존3 경도" };

		File sourceDir = new File("C:/TEMP/1");
		File targetDir = new File("C:/TEMP/2");

		for (File file : sourceDir.listFiles()) {
			CSVReader reader = null;
			CSVWriter writer = null;

			InputStream in = null;
			OutputStream out = null;

			try {
				in = new FileInputStream(file);
				out = new FileOutputStream(new File(targetDir, file.getName()));

				reader = new CSVReader(new InputStreamReader(in, "euckr"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1);
				writer = new CSVWriter(new OutputStreamWriter(out, "euckr"), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER);
				String[] s;

				writer.writeNext(HEADER);

				while ((s = reader.readNext()) != null) {
					String googleAdId = s[0];
					String epopconId = s[1];
					String storeName = s[2];
					String storeId = s[3];
					String cate = s[5];
					String price = s[15];
					String cardFullName = s[16];
					String cardName = s[17];
					String cardCompany = s[18];
					String dealDate = s[19];
					String lifeZoneLat1 = s[20];
					String lifeZoneLong1 = s[21];
					String lifeZoneLat2 = s[22];
					String lifeZoneLong2 = s[23];
					String lifeZoneLat3 = s[24];
					String lifeZoneLong3 = s[25];

					writer.writeNext(new String[] { googleAdId, epopconId, storeName, storeId, cate, price, cardFullName, cardName, cardCompany, dealDate, lifeZoneLat1, lifeZoneLong1, lifeZoneLat2, lifeZoneLong2, lifeZoneLat3, lifeZoneLong3 });
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(reader);
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(writer);
			}
		}
		 */
	}
}
