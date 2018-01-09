package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.model.Category;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.parser.impl.ZaraStoreParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class ZaraCategoryParserTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void testZaraCategoryParser() throws Exception {
		String content = readContent("/template/zara/category_2017_08_30.html");
		
		ZaraStoreParser parser = (ZaraStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.ZARA);
		parser.init();
		parser.enableCategoryFilter(true);
		//parser.parseProductDetail(content, new ProductDetail(null), null);
		List<Category> categries = parser.parseCategories(content);
		
		Set<String> categorySet = new LinkedHashSet<>();

		System.out.println("==============모든 카테고리 수집 결과[중복제거 전]===============");
		for(Category c1 : categries) {
			System.out.println(StringUtils.join(c1.getCategoryNames(), ";"));
			categorySet.add(StringUtils.join(c1.getCategoryNames(), ";"));
		}
		
		System.out.println("==============모든 카테고리 수집 결과[중복제거 후]===============");
		for(String s : categorySet) {
			System.out.println(s);
		}
		System.out.println("==============================================");
		
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
