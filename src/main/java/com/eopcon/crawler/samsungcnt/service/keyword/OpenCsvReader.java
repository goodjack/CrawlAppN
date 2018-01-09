package com.eopcon.crawler.samsungcnt.service.keyword;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 네이버에 검색할 카테고리를 CSV 파일로 부터 읽는 클래스
 *
 */
public class OpenCsvReader {

	/**
	 * csv 파일로부터 카테고리 정보를 읽음
	 */
	public List<Map<String, String>> readCategoryInfo(File file) throws IOException {
		List<Map<String, String>> cateInfoList = new ArrayList<Map<String, String>>();
		CSVReader reader = new CSVReader(new FileReader(file));
		String[] nextLine;

		while ((nextLine = reader.readNext()) != null) {

			Map<String, String> cateMap = new HashMap<>();
			cateMap.put("cate1", nextLine[0]);
			cateMap.put("cate2", nextLine[1]);
			cateMap.put("cate3", nextLine[2]);
			cateMap.put("cateId", nextLine[3]);
			cateInfoList.add(cateMap);
		}

		return cateInfoList;
	}
}