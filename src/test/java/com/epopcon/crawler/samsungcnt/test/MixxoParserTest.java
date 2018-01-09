package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.dao.ProductDao;
import com.eopcon.crawler.samsungcnt.service.parser.impl.MixxoStoreParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class MixxoParserTest {
	
	@Autowired
	private ApplicationContext context;

	@Test
	public void test1() throws Exception {

		MixxoStoreParser parser = (MixxoStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.MIXXO);

		String content = "[BLUE,DARK GREY] 겉감-폴리에스터 95% 폴리우레탄 5% / 안감-폴리에스터 100% [LIGHT PINK] 겉감-폴리에스터 96% 폴리우레탄 4% / 안감-폴리에스터 100%";
		//String content = "겉감1-폴리에스터100% 겉감2-천연모피(밍크)";
		//String[] colors = new String[] { "(18)M/GREY","(25)PINK","(19)BLACK"};

		String[] colors = new String[] { "(19)BLACK", "(49)KHAKI", "(39)IVORY"};
		
		ProductDetail pd = new ProductDetail(null);
		pd.setOnlineGoodsNum("1704184979");
		
		List<Materials> materials = parser.parseMarterialsString(content, colors, pd);
		System.out.println(materials.size());
		
	
	}
	
	//@Test
	public void test2() throws Exception {

		MixxoStoreParser parser = (MixxoStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.MIXXO);
		
		String content = readContent("/template/mixxo/category.html");
		parser.parseCategories(content);

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