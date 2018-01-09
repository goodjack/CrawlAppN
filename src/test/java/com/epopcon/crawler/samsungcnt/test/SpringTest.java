package com.epopcon.crawler.samsungcnt.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.eopcon.crawler.samsungcnt.common.file.LocalFile;
import com.eopcon.crawler.samsungcnt.common.file.LocalFileSet;
import com.eopcon.crawler.samsungcnt.common.file.service.LocalFileService;
import com.eopcon.crawler.samsungcnt.model.Materials;
import com.eopcon.crawler.samsungcnt.model.Product;
import com.eopcon.crawler.samsungcnt.model.ProductDetail;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.parser.impl.UniqloStoreParser;
import com.eopcon.crawler.samsungcnt.util.SerializationUtils;

public class SpringTest {
	
	private static String slice(String content) {
		byte[] b = content.getBytes();
		int length = b.length;
		if (length > 8000) {
			byte[] nb = Arrays.copyOfRange(b, 0, 8000);
			try {
				return new String(nb, "utf8");
			} catch (UnsupportedEncodingException e) {

			}
		}
		return content;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		//test0();
		//test1();
		//test2();
		//test3();
		//test4();
		//test5();
		//test6();
		test7();
	}

	public static void test0() {
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression("#name.replaceAll('^(.+)\\.(\\w+)$', '$1_' + #today + '.$2')");

		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariable("name", "aa.a.txt");
		context.setVariable("today", "20150101");

		System.out.println(exp.getValue(context, String.class));

		Matcher m = Pattern.compile("^.+-(\\d{8})\\.\\w+$").matcher("aaaaaa-20150101.txt");

		if (m.find()) {
			String temp = m.group(1).replaceAll("\\D", "");
			if ("20140102".compareTo(temp) < 0) {
				System.out.print("11111111111111111");
			}
		}
	}

	public static void test1() throws Exception {

		UniqloStoreParser parser = new UniqloStoreParser(OnlineStoreConst.UNIQLO);

		String test1 = "면100％";
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
		String test13 = "[재료의 종류] [본체] 마38%·마32%·면30%, [핸들 부분] 천연가죽(소가죽)";
		String test14 = "[하의] : [상의] [몸판] 폴리에스터80%·면20%, [리브 부분] 폴리에스터54%·면46%, [하의] 폴리에스터80%·면20%";
		String test15 = "[끈] TPU, [바닥 소재] EVA 수지";

		String[] colors = new String[] { "08 DARK GRAY", "66 BLUE", "09 BLACK" };
		// String[] colors = new String[] { "08 DARK GRAY"};
		
		ProductDetail pd = new ProductDetail(null);
		pd.setOnlineGoodsNum("0");
		List<Materials> list = parser.parseMarterialsString(test14, colors, pd);

		for (Materials m : list)
			System.out.println(m);

	}

	public static void test2() throws Exception {
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(final Path entry) {
				Path fileName = entry.getFileName();
				return fileName.toString().startsWith(OnlineStoreConst.CONFIG_PRODUCT_DETAIL_FILE_PREFIX);
			}
		};

		DirectoryStream<Path> stream = null;
		try {
			File dir = new File("C:/TEMP/20170309/SPAO/output");

			if (dir.exists()) {
				stream = Files.newDirectoryStream(dir.toPath(), filter);

				Iterator<Path> iterator = stream.iterator();
				while (iterator.hasNext()) {
					Path path = iterator.next();

					ProductDetail detail = (ProductDetail) SerializationUtils.deserialize(path.toFile());
					System.out.println(detail);
					//test4(detail.getGoodsName());
				}
			}
			stream.close();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static void test3() throws Exception {
		// String test1 = "[00 WHITE] [재질] [테/렌즈] 폴리카보네이트, [광선특성] [필터 범주] 1, [시감투과율 범위] 43%";
		// String test1 = "[재질/테/렌즈] 폴리카보네이트, [광선특성/가시광선 투과율] 38 DARK BROWN : 91%, 08 DARK GRAY : 90%, [자외선 차단율] 99%";
		// String test1 = "[재질] 테 : 나일론, [렌즈] 셀룰로오스 아세테이트, [광선특성] 필터 범주 : 3, [시감투과율 범위] 8% < τV ≤ 18%, [자외선 차단율] 99%";
		String test1 = "[08 DARK GRAY] [테/렌즈/재질] 폴리카보네이트, [광선특성] [필터 범주] 3, [시감투과율 범위] 8% < τV ≤ 18% [37 BROWN] [테/렌즈/재질] 폴리카보네이트, [광선특성] [필터 범주] 2, [시감투과율 범위] 18% < τV ≤ 43%";

		test1 = test1.replaceAll("\\[\\s*재질\\s*/\\s*테\\s*/\\s*렌즈\\]", "[재질][테/렌즈]");
		test1 = test1.replaceAll("\\[\\s*테\\s*/\\s*렌즈/\\s*재질]", "[재질][테/렌즈]");
		test1 = test1.replaceAll("\\s+테 :\\s+", "[테]");
		test1 = test1.replaceAll("\\s+렌즈 :\\s+", "[렌즈]");

		System.out.println(Pattern.compile("\\[\\s*(?:테|렌즈|테/렌즈)\\s*\\]").matcher(test1).find());

		Pattern pattern = Pattern.compile("(?:\\[\\s*(\\d{2}\\s+[^\\]]+)\\s*\\])?\\s*\\[\\s*재질\\s*\\]\\s*((?:\\[\\s*(?:테|렌즈|테/렌즈)\\s*\\]\\s*(?:[가-힣a-z\\s]+)\\s*,?\\s*)+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(test1);

		while (matcher.find()) {
			String color = StringUtils.defaultString(matcher.group(1), "ALL");
			String materials = StringUtils.defaultString(matcher.group(2));

			Pattern p = Pattern.compile("(?:\\[\\s*(테|렌즈|테/렌즈)\\s*\\]\\s*([가-힣a-z\\s]+))", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(materials);

			if (m.find()) {
				System.out.println(color);
				System.out.println(m.group(1));
				System.out.println(String.format("%s100%%", m.group(2)));
			}
		}
	}
	
	public static void test4(String title) throws Exception {
		Pattern pattern = Pattern.compile("(민트|바이올렛|코랄|크림|오프화이트|화이트|핑크|페일핑크|페일블루|퍼플|카키베이지|카키그레이|카키|카멜|챠콜|차콜그레이|차콜|와인|오렌지|옐로우그린|옐로우|아이보리|실버|블루|블랙|브론즈|브라운|베이지|멜란지그레이|멜란지|머스타드|레드|라일락|라인|라이트핑트|라이트핑크|라이트터쿠아즈블루|라이트코랄|라이트카키|라이트카멜|라이트오렌지|라이트옐로우그린|라이트옐로우|라이트아쿠아블루|라이트아이보리|라이트세피아|라이트블루|라이트브라운|라이트베이지|라이트바이올렛|라이트레드|라이트네이비|라이트그린|라이트그레이|데이지|다크핑크|다크터쿠아즈블루|다크카키|다크오렌지|다크옐로우그린|다크옐로우|다크엘로우|다크아이보리|다크블루|다크브라운|다크베이지|다크레드|다크네이비|다크그린|다크그레이|네이비|그린|그레이쉬카키|그레이쉬블루|그레이쉬네이비|그레이|골드)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(title);

		String color = "";
		
		if (matcher.find()) {
			color = StringUtils.defaultString(matcher.group(1)).trim();
		}
		System.out.println(String.format("%s | %s", title, color));
	}
	
	public static void test5() throws Exception {
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
			@Override
			public boolean accept(final Path entry) {
				Path fileName = entry.getFileName();
				return fileName.toString().startsWith(OnlineStoreConst.CONFIG_PRODUCT_LIST_FILE_PREFIX);
			}
		};

		DirectoryStream<Path> stream = null;
		try {
			File dir = new File("C:/TEMP/20170308/HM/input");

			if (dir.exists()) {
				stream = Files.newDirectoryStream(dir.toPath(), filter);

				Iterator<Path> iterator = stream.iterator();
				while (iterator.hasNext()) {
					Path path = iterator.next();

					Product product = (Product) SerializationUtils.deserialize(path.toFile());
					System.out.println(product);
					//test4(detail.getGoodsName());
				}
			}
			stream.close();
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public static void test6() throws Exception {
		String content = readContent("/template/lfmall/materials.txt");
		
		//content = "겉감:폴리에스터88%나일론12%안감:폴리에스터100% 라이너-겉감/안감/충전재:폴리에스터100%";
		content = content.replaceAll("(^|\\s+)(원피스|라이너|베스트|내피|외피|코트|점퍼|상의|하의|겉옷|속옷|겉면|카라)([^가-힣])", "$1[$2]$3");
		content = content.replaceAll("\\(지정외섬유\\)", "");
		content = content.replaceAll("프라다 원단 ", "");
		content = content.replaceAll("\\((?:[/]?[^\\)]+\\s*\\d+%\\s*[^\\)]*)+\\)", "");
		content = content.replaceAll("(\\d+%\\s*)\\([^\\)]+\\)", "$1");
		content = content.replaceAll("\\{", "[");
		content = content.replaceAll("\\}", "]");
		
		StringBuffer sb = new StringBuffer();
		Pattern p = null;
		Matcher m = null;
		
		p = Pattern.compile("(\\[[^\\]]+\\]|[가-힣][가-힣0-9,/]*\\s*[;:\\-])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		m = p.matcher(content);
		
		while(m.find()) {
			String group = m.group(1);
			
			if(!group.matches("^\\[[^\\]]+\\]$")) {
				group = "[" + group.replaceAll("[;:\\-]", "").trim() + "]";
			}
			m.appendReplacement(sb, group);
		}
		m.appendTail(sb);
		
		content = sb.toString();
		sb.setLength(0);
		
		p = Pattern.compile("(\\s+(?:겉감|안감|충전재)\\d?\\s+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		m = p.matcher(content);
		
		while(m.find()) {
			String group = m.group(1).trim();
			group = "[" + group.replaceAll("[;:-]", "").trim() + "]";
			m.appendReplacement(sb, group);
		}
		m.appendTail(sb);
		
		content = sb.toString();
		content = content.replaceAll("[;:\\-]", "");
		
		int i=0;
		for (String line : content.split("\n")) {
			
			Pattern pattern = Pattern.compile("(\\[[^\\]]+\\])?\\s*(\\[[^\\]]+\\])?\\s*((?:[가-힣\\(\\)a-z]+\\s*(?:\\d{1,3}%?)?)+)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(line);

			while (matcher.find()) {
				String[] group = new String[] { matcher.group(1), matcher.group(2), matcher.group(3) };
				System.out.println(String.format("%s.%s -> %s|%s|%s", i, line, group[0], group[1], group[2]));
			}
			i++;
		}
	}
	
	public static void test7() throws Exception {
		LocalFileService lfs = new LocalFileService();
		LocalFileSet fileSet = lfs.ls("C:/TEMP/images/product/이전전달용", true);
		
		for(LocalFile lf : fileSet.getFiles()){
			System.out.println(lf.getFile().getName());
		}
	}

	private static String readContent(String path) throws IOException {
		InputStream stream = null;
		BufferedReader reader = null;
		try {
			stream = SpringTest.class.getResourceAsStream(path);
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
