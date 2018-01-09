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
import com.eopcon.crawler.samsungcnt.service.parser.impl.SpaoStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.UniqloStoreParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class ParserTest {

	@Autowired
	private ApplicationContext context;

	@Test
	public void testUniqloParser() throws Exception {
		UniqloStoreParser parser = (UniqloStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.UNIQLO);
		
		//String content = readContent("/template/uniqlo/product_detail.html");
		//parser.parseProductDetail(content, new ProductDetail(null));
		
		/*String content = readContent("/template/uniqlo/comments.html");
		parser.parseComments(content, new ProductDetail(null));*/
		
		String test1 = "먄100％";
		String test2 = "폴리에스터81%·레이온17%·폴리우레탄2%";
		String test3 = "[08 DARK GRAY/66 BLUE] [몸판] 폴리에스터65%·면35%, [리브 부분] 면53%·폴리에스터47%, [그 외 컬러] [몸판] 폴리에스터75%·면25%, [리브 부분] 폴리에스터69%·면31%";
		String test4 = "[몸판] 면100%, [리브 부분] 면95%·폴리우레탄5%";
		String test5 = "[겉감] 나일론100%, [충전재] [몸판] 다운90%·깃털10%, [목 부분:바깥쪽/어깨 부분:안쪽] 다운90%·깃털10%, [목 부분:안쪽/어깨 부분:바깥쪽] 폴리에스터100%, [안감] 나일론100%";
		String test6 = "[겉감 (원단)] 면100%, [안감] 면75%·폴리에스터25%, [바닥 소재] 합성바닥";
		String test7 = "[03 GRAY,65 BLUE] 면59%·폴리에스터27%·폴리우레탄14%, [그 외컬러] 면58%·폴리에스터27%·폴리우레탄15%";
		String test8 = "[66 BLUE/69 Navy/09 black] 면98%·폴리우레탄2%, [00 White/63 BLUE/68 BLUE] 면99%·폴리우레탄1%";
		String test9 = "[00 White, 67 Blue] 면99％·폴리우레탄1％, [09 Black, 69 Navy] 면98％·폴리우레탄2％";
		String test10 = "레이온(모달)61%·폴리에스터30%·폴리우레탄9%";
		String test11 = "[02 LIGHT GRAY] [몸판] 면93%·폴리우레탄7%, [웨스트 부분] 폴리에스터62%·나일론16%·면14%·폴리우레탄8% [ 그 외 컬러] [몸판] 면93%·폴리우레탄7%, [웨스트 부분] 나일론47%·폴리에스터43%·폴리우레탄10%";
		String test12 = "[상의] : [03 GRAY/08 DARK GRAY] [몸판] 면53%·폴리에스터47%, [리브 부분] 폴리에스터71%·면29%, [69 NAVY/09 BLACK] [몸판] 면78%·폴리에스터22%, [리브 부분] 면82%·폴리에스터18% [하의] : [03 GRAY/08 DARK GRAY] 면53%·폴리에스터47%, [69 NAVY/09 BLACK] 면78%·폴리에스터22%";
		String test13 = "[00 WHITE] [재질] [테/렌즈] 폴리카보네이트, [광선특성] [필터 범주] 1, [시감투과율 범위] 43% <τV ≤ 80%, [자외선 차단율] 99%, [08 DARK GRAY，16 RED] [재질] 테 : 폴리카보네이트, 렌즈 : 셀룰로오스 아세테이트, [광선특성] [필터 범주] 3, [시감투과율 범위] 8% < τV ≤ 18%, [자외선 차단율] 99%";
		String test14 = "[상의] : [03 GRAY] [몸판] 면53%·폴리에스터47%, [리브 부분] 폴리에스터71%·면29%, [그 외 컬러] [몸판] 면78%·폴리에스터22%, [리브 부분] 면82%·폴리에스터18% [하의] : [69 NAVY/03 GRAY] 면53%·폴리에스터47%, [그 외 컬러] 면78%·폴리에스터22%";
		String test15 = "[끈] TPU, [바닥 소재] EVA 수지";
		
		ProductDetail pd = new ProductDetail(null);
		pd.setOnlineGoodsNum("UQ31080164");
		
		String[] colors = new String[] { "08 DARK GRAY", "66 BLUE", "09 BLACK" };
		List<Materials> list = parser.parseMarterialsString(test15, colors, pd);

		for (Materials m : list)
			System.out.println(m);
	}
	
	//@Test
	public void testSpaoParser() throws Exception {
		SpaoStoreParser parser = (SpaoStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.SPAO);
		
		//String content = readContent("/template/uniqlo/product_detail.html");
		//parser.parseProductDetail(content, new ProductDetail(null));
		
		/*String content = readContent("/template/uniqlo/comments.html");
		parser.parseComments(content, new ProductDetail(null));*/
		
		//String test1 = "GREY 겉감 : 면 58 % , 폴리에스터 42 %, INDIGO 겉감 : 면 87 % , 폴리에스터 13 %, 나머지 겉감 : 면 100%";
		//String test2= "L/GREY-폴리에스터100% NAVY-레이온100%";
		String test4 = "면67.5% 폴리에스터30.6% 폴리우레탄1.9%";
		String[] colors = new String[] { "INDIGO" };

		List<Materials> list = parser.parseMarterialsString(test4, colors, new ProductDetail(null));

		for (Materials m : list)
			System.out.println(m);
	}
	

	protected String readContent(String path) throws IOException {
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
