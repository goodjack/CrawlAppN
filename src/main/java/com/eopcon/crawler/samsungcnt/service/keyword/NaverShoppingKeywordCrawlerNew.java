package com.eopcon.crawler.samsungcnt.service.keyword;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.eopcon.crawler.samsungcnt.model.NaverKeyword;
import com.eopcon.crawler.samsungcnt.service.dao.ProductDao;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.parser.BaseParser;
import com.eopcon.crawler.samsungcnt.util.JsoupConnect;

/**
 * 네이버 키워드 크롤러
 */
@Component
public class NaverShoppingKeywordCrawlerNew extends BaseParser {

	@Autowired
	private HttpRequestService request;
	@Autowired
	protected ProductDao productDao;

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36";
	OpenCsvReader csvReader = new OpenCsvReader();

	private ClassPathResource classPathResource = new ClassPathResource("/assets/navershoppingbest100.csv");

	/**
	 * 매주 월요일 오후 1시 정각 실행
	 */
	@Scheduled(cron = "0 0 13 * * MON")
	public void execute() {
		String setDate = "";
		String endDate = "";
		int week = 0;
		int year = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");

		Calendar calendar = Calendar.getInstance();

		year = calendar.get(Calendar.YEAR);
		week = calendar.get(Calendar.WEEK_OF_YEAR);

		calendar.set(Calendar.WEEK_OF_YEAR, week - 1);
		calendar.add(Calendar.DAY_OF_MONTH, 2 - calendar.get(Calendar.DAY_OF_WEEK));
		setDate = sdf.format(calendar.getTime()); // 전주의 월요일

		calendar.set(Calendar.WEEK_OF_YEAR, week);
		calendar.add(Calendar.DAY_OF_MONTH, (1 - calendar.get(Calendar.DAY_OF_WEEK)));
		calendar.set(Calendar.YEAR, year);
		endDate = sdf.format(calendar.getTime()); // 현재 주의 일요일

		getKeywords(setDate, endDate);

	}

	/**
	 * 주단위로 네이버쇼핑 인기키워드를 검색
	 */
	public void getKeywords(String setDate, String endDate) {

		// CSV파일에서 네이버쇼핑 인기키워드 카테고리 명, 아이디 가져오기
		List<Map<String, String>> categoryInfoList = null;
		try {
			// categoryInfoList = csvReader.readCategoryInfo("C:\\Development\\git_workspace\\samsungcnt\\crawler-server\\src\\main\\resources\\assets\\navershoppingbest100.csv");
			categoryInfoList = csvReader.readCategoryInfo(classPathResource.getFile());
			System.out.println("######### categoryInfoList #########");
			System.out.println(categoryInfoList);
			System.out.println("##############################");
			// categoryInfoList = csvReader.readCategoryInfo(new File("D:/navershoppingbest100.csv"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 카테고리만큼 반복 크라울링
		for (Map<String, String> categoryInfo : categoryInfoList) {
			// System.out.println("카테고리:"+categoryInfo.get("cate1")+">>"+categoryInfo.get("cate2")+">>"+categoryInfo.get("cate3"));

			String setCategoryID = categoryInfo.get("cateId");
			// String getContentURL = "http://shopping.naver.com/best100v2/detail/kwd.nhn?catId="+setCategoryID+"&kwdType=KWD&dateType=today&startDate="+setDate+"&endDate="+setDate;
			String getContentURL = "http://shopping.naver.com/best100v2/detail/kwd.nhn?catId=" + setCategoryID + "&kwdType=KWD&dateType=week&startDate=" + setDate + "&endDate=" + endDate;

			JsoupConnect jsoupConnect = new JsoupConnect();
			jsoupConnect.setJsoupConnet(getContentURL);
			
			Document doc = jsoupConnect.getJsoupConnect();

			Elements elements = doc.select("div.section > ul > li");

			for (int i = 0; i < elements.size(); i++) {

				String rank = text(elements.get(i).select("> em"), true);
				rank = rank.replace("위", "");
				String keyword = text(elements.get(i).select(">  span >a"), true);
				String updown = text(elements.get(i).select(">  span.vary > span.up"), true);
				String temp_shiftrank = text(elements.get(i).select(">  span.vary"), true);

				int temp_pos = 0;
				temp_pos = temp_shiftrank.indexOf("</span>");
				String temp = temp_shiftrank.substring(temp_pos + 7);

				int shiftrank = 0;
				if (temp == null || temp.equals("")) {
					shiftrank = 0;
				} else {
					try {
						shiftrank = Integer.parseInt(temp);
					} catch (NumberFormatException e) {
						shiftrank = 0;
					}
				}

				if (updown == "") {
					updown = text(elements.get(i).select(">  span.vary > span.keep"), true);
				}
				if (updown == "") {
					updown = text(elements.get(i).select("> span.vary >  span.down"), true);
				}
				if (updown == "") {
					updown = text(elements.get(i).select(">  span.vary > span.new"), true);
				}

				if (updown == "하락") {
					shiftrank = shiftrank * -1;
				}

				System.out.println("rank ->" + rank); // 순위
				System.out.println("keyword ->" + keyword); // 검색어
				System.out.println("updown ->" + updown); // 순위변동 방향
				System.out.println("shiftrank ->" + shiftrank); // 변동된 순위수
				System.out.println("================================");

				NaverKeyword naverkeyword = new NaverKeyword();
				naverkeyword.setKeyword(keyword);
				naverkeyword.setCate1(categoryInfo.get("cate1"));
				naverkeyword.setCate2(categoryInfo.get("cate2"));
				naverkeyword.setCate3(categoryInfo.get("cate3"));
				naverkeyword.setCate4(categoryInfo.get("cateId"));
				naverkeyword.setCnt(0);
				naverkeyword.setRank(Integer.parseInt(rank));
				naverkeyword.setRankChange(shiftrank);
				naverkeyword.setStartDay(setDate);
				naverkeyword.setEndDay(endDate);
				naverkeyword.setCollectDay(setDate);
				naverkeyword.setOriginCode("NAVER");

				productDao.insertSearchKeyword(naverkeyword);

			}
		}

	}
}
