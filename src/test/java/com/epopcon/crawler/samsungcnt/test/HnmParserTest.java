package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.model.Stock;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HnmStoreParser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:config/spring/spring-test.xml" })
public class HnmParserTest {

	@Autowired
	private ApplicationContext context;

	//@Test
	public void test() throws UnsupportedEncodingException {
		
		String tmp = "안감:1";
		
		
		System.out.println(tmp.replaceAll("\\D", "").equals(""));
		
		String utagData = "<script type=\"text/javascript\">var osaArea = getOsaArea(); var osaType = getOsaType(); var virtualCategory = getVirtualCategory(); utag_data = {product_id : [\"0325027\"], product_name : [\"IRINA SOFTBRA 2PK\"], product_view_type : \"PDP\",product_view_price_type: [\"\"], product_category : [\"LADIES_MATERNITY_LINGERIETIGHTS\"], product_color_code : [\"\"], product_size_code : [\"\"], page_osa_area : osaArea, page_osa_type : osaType, product_virtual_category : virtualCategory, conversion_id  : \"\", conversion_category  : \"\", conversion_step  : \"\", event_type  : \"\", customer_id : \"\", customer_email : \"\", customer_zip : \"\", customer_city : \"\", customer_state : \"\", customer_state : \"\", customer_country : \"\", customer_loyalty_level : \"\", customer_has_children : \"\", customer_fashion_news : \"\" , touchpoint : getTouchpoint() , page_id : \"PRODUCT DETAIL : 0325027 : IRINA SOFTBRA 2PK\", category_id : \"LADIES_MATERNITY_LINGERIETIGHTS\", category_path : \"PRODUCT_DETAIL_PAGE\", selected_market : \"KR\", display_language : \"ko_KR\", conversion_id  : \"\", conversion_category  : \"\", conversion_step  : \"\", event_type  : \"\", customer_id : \"\", customer_email : \"\", customer_zip : \"\", customer_city : \"\", customer_state : \"\", customer_state : \"\", customer_country : \"\", customer_loyalty_level : \"\", customer_has_children : \"\", customer_fashion_news : \"\" }; utagTealiumTrack(function(a,b,c,d){a=getTealiumURL(\"KR\");b=document;c='script';d=b.createElement(c);d.src=a;d.type='text/java'+c;d.async=true;a=b.getElementsByTagName(c)[0];a.parentNode.insertBefore(d,a);}); </script>";
		Pattern p = Pattern.compile("product_id : \\[\"(.*?)\"\\]");
		Matcher m = p.matcher(utagData);
		
		while(m.find()) {
			System.out.println(m.group(1));
		}
		
			
			
			
			
		String url = "http://lp2.hm.com/hmprod?set=source[/model/2016/E00 0190252 013 00 2079.jpg],type[STILLLIFE_FRONT]&hmver=0&call=url[file:/product/main]";
		  String string = url.replaceAll("^(.+)\\?set=source\\[([^\\]]+)\\],type\\[([^\\]]+)\\]&hmver=([^&]+)&call=url\\[([^\\]]+)\\]$", "$1,$2,$3,$4,$5");
		  
		  String[] args = string.split(",");
      
      for (int i = 1; i < args.length; i++) {
         args[i] = URLEncoder.encode(StringUtils.defaultString(args[i]), "UTF-8");
      }
      
      System.out.println(String.format("%s?set=source[%s],type[%s]&hmver=%s&call=url[%s]", args));
		
		
		
		//String content = "";
		//String content = "폴리에스테르 90%|안감: 폴리에스테르 100%|코팅: 폴리에스테르 90%; 코튼 10%";
		String content = "코팅: 폴리에스2테르 90%; 코튼 10%";
		String[] rows = content.split("[|]");

		for(String row:  rows) {
			if(row.lastIndexOf(":") > -1) {
				String[] colsArr = row.split(":");
				String composed = colsArr[0];
				String materials = colsArr[1];
				String[] tmpMaterialsArr = materials.split(";");
				for(String tmpMaterial : tmpMaterialsArr) {
					String material = tmpMaterial.replaceAll("(\\d+)%$", "").trim();
					String ratio = tmpMaterial.replaceAll(".*\\s+(\\d+)%$", "$1").trim();
					System.out.println("1");
				}
				
				
			} else {
				String composed = "전체";
				String materials = row;
				String[] tmpMaterialsArr = materials.split(";");
				for(String tmpMaterial : tmpMaterialsArr) {
					String material = tmpMaterial.replaceAll("(\\d+)%$", "").trim();
					String ratio = tmpMaterial.replaceAll(".*\\s+(\\d+)%$", "$1").trim();
					System.out.println("1");
				}
				
			}
			
			
		}
		
		System.out.println(rows);
		
	}
	
	
	@Test
	public void testHnmParser() throws Exception {
		String content = readContent("/template/hnm/product_detail_1017.html");
		
		HnmStoreParser parser = (HnmStoreParser) context.getBean(OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER, OnlineStoreConst.HM);
		ProductDetail p = new ProductDetail(null);
		parser.parseProductDetail(content, p);
		
		List<Stock> stockList = p.getStocks();
		
		String tmp = "";
		for(Stock stock : stockList) {
			tmp += stock.getColor() + ";";
		}
		tmp = tmp.substring(0, tmp.length()-1);
		
		String[] colors = {"10 블랙/화이트 스트라이프", "12 블랙/베이지 패턴", "10 화이트 플로럴" , "09 블랙"};
		
		parser.parseMarterialsString("", colors, p);
		
		
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
