package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.model.HnmCategory;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HnmStoreParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class HnmCategoryParserTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void testZaraCategoryParser() throws Exception {
		String content = readContent("/template/hnm/top_category_2017_07_21.html");
		
		HnmStoreParser parser = (HnmStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.HM);
		
		List<HnmCategory> topCategoryList = parser.parseTopCategories(content);
		
		System.out.println("==============top 카테고리 수집 결과===============");
		for(HnmCategory c : topCategoryList) {
			System.out.println(StringUtils.join(c.getCategoryNames(), ";") + "[" + c.getCategoryUrl() + "]");
		}
		
		

		
		content = readContent("/template/hnm/sub_depth2_category_2017_07_21.html");
		List<HnmCategory> categoryList2 = parser.parseSubCategories(content, topCategoryList.get(13), 2);

		System.out.println("==============sub1 카테고리 수집 결과===============");
		for(HnmCategory c : categoryList2) {
			System.out.println(StringUtils.join(c.getCategoryNames(), ";") + "[" + c.getCategoryUrl() + "]");
		}
		
		

		content = readContent("/template/hnm/sub_depth3_category_2017_07_21.html");
		List<HnmCategory> categoryList3 = parser.parseSubCategories(content, categoryList2.get(0), 3);

		System.out.println("==============sub2 카테고리 수집 결과===============");
		for(HnmCategory c : categoryList3) {
			System.out.println(StringUtils.join(c.getCategoryNames(), ";") + "[" + c.getCategoryUrl() + "]");
		}
		
		
				
		
	}

	private String readContent(String path) throws IOException {
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
			stream.close();
			reader.close();
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
