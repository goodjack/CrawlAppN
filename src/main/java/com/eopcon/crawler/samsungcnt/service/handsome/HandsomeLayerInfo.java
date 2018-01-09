package com.eopcon.crawler.samsungcnt.service.handsome;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.eopcon.crawler.samsungcnt.model.DataStandard;
import com.eopcon.crawler.samsungcnt.util.SeleniumConnect;


public class HandsomeLayerInfo {

	private DataStandard dataStandard = new DataStandard();
	
	public DataStandard layer4Set(String gender) {
		
		if("여성".equals(gender)) {
			dataStandard.setLayer4("WOMEN");
		} else if ("남성".equals(gender)) {
			dataStandard.setLayer4("MAN");
		}
		
		return dataStandard;
	}

	public DataStandard layer5Set(String level2Name) {
		
		if("탑".equals(level2Name)) {
			dataStandard.setLayer5("TOP");
		} else if("팬츠".equals(level2Name)) {
			dataStandard.setLayer5("BOTTOM");
		} else if("스커트".equals(level2Name)) {
			dataStandard.setLayer5("BOTTOM");
		} else if("드레스".equals(level2Name)) {
			dataStandard.setLayer5("TOP");
		} else if("아우터".equals(level2Name)) {
			dataStandard.setLayer5("OUTER");
		} else if("패션잡화".equals(level2Name)) {
			dataStandard.setLayer5("OTHERS");
		} else if("수트".equals(level2Name)) {
			dataStandard.setLayer5("SUIT");
		} else if("키즈".equals(level2Name)) {
			dataStandard.setLayer4("KIDS");
			dataStandard.setLayer5("OTHERS");
		} else if("라이프스타일".equals(level2Name)) {
			dataStandard.setLayer5("OTHERS");
		}
		return dataStandard;
	}

	public DataStandard layer6Set(String level2Name, String level3Name) {
		
		if("탑".equals(level2Name)) {
			dataStandard = topLayer6Set(level3Name);
		} else if("팬츠".equals(level2Name)) {
			dataStandard = pantsLayer6Set(level3Name);
		} else if("스커트".equals(level2Name)) {
			dataStandard = skirtsLayer6Set(level3Name);
		} else if("드레스".equals(level2Name)) {
			dataStandard = dressLayer6Set(level3Name);
		} else if("아우터".equals(level2Name)) {
			dataStandard = outerLayer6Set(level3Name);
		} else if("패션잡화".equals(level2Name)) {
			dataStandard = accLayer6Set(level3Name);
		} else if("수트".equals(level2Name)) {
			dataStandard = suitLayer6Set(level3Name);
		} else if("키즈".equals(level2Name)) {
			dataStandard = kidsLayer6Set(level3Name);
		} else if("라이프스타일".equals(level2Name)) {
			dataStandard = lifeLayer6Set(level3Name);
		}
		return dataStandard;
	}

	private DataStandard lifeLayer6Set(String level3Name) {
		
		dataStandard.setLayer6("LIFESTYLE");
		return dataStandard;
	}

	private DataStandard kidsLayer6Set(String level3Name) {
		
		if("의류".equals(level3Name)) {
			dataStandard.setLayer6("미분류");
		} else if("잡화".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		}
		
		return dataStandard;
	}

	private DataStandard suitLayer6Set(String level3Name) {
		dataStandard.setLayer6("SUIT");
		return dataStandard;
	}

	private DataStandard accLayer6Set(String level3Name) {
		
		if("가방".equals(level3Name)) {
			dataStandard.setLayer6("BAG");
		} else if("지갑/홀더".equals(level3Name)) {
			dataStandard.setLayer6("WALLET");
		} else if("슈즈".equals(level3Name)) {
			dataStandard.setLayer6("SHOES");
		} else if("주얼리".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		} else if("타이".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		} else if("스카프/머플러".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		} else if("기타소품".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		} else if("온라인한정".equals(level3Name)) {
			dataStandard.setLayer6("ACC");
		}
		return dataStandard;
	}

	private DataStandard outerLayer6Set(String level3Name) {
		
		if("코트".equals(level3Name)) {
			dataStandard.setLayer6("COAT");
		} else if("다운/패딩".equals(level3Name)) {
			dataStandard.setLayer6("DOWN/PADDING");
		} else if("점퍼".equals(level3Name)) {
			dataStandard.setLayer6("JUMPER");
		} else if("재킷".equals(level3Name)) {
			dataStandard.setLayer6("JACKET");
		} else if("트렌치".equals(level3Name)) {
			dataStandard.setLayer6("COAT");
		} else if("가디건/베스트".equals(level3Name)) {
			dataStandard.setLayer5("TOP");
			dataStandard.setLayer6("KNIT");
		}
		return dataStandard;
	}

	private DataStandard dressLayer6Set(String level3Name) {
		
//		dataStandard.setLayer5("TOP");
		dataStandard.setLayer6("ONE-PIECE");
		return dataStandard;
	}

	private DataStandard skirtsLayer6Set(String level3Name) {
		
//		dataStandard.setLayer5("BOTTOM");
		dataStandard.setLayer6("SKIRTS");
		return dataStandard;
	}

	private DataStandard pantsLayer6Set(String level3Name) {
		
//		dataStandard.setLayer5("BOTTOM");
		dataStandard.setLayer6("PANTS");
		return dataStandard;
	}

	private DataStandard topLayer6Set(String level3Name) {
		
		if("티셔츠".equals(level3Name)) {
			dataStandard.setLayer6("T-SHIRTS");
		} else if("블라우스".equals(level3Name)) {
			dataStandard.setLayer6("BLOUSE");
		} else if("셔츠".equals(level3Name)) {
			dataStandard.setLayer6("SHIRTS");
		} else if("니트".equals(level3Name)) {
			dataStandard.setLayer6("KNIT");
		} 
		return dataStandard;
	}

	public String categoryCode(String categoryUrl) {
		
		Pattern p = Pattern.compile("(/)(\\w+)(/)(\\w+)(/)(\\w+)(/)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(categoryUrl);
		String code = "";
		if(m.find()){
			code = m.group(6);
		}
		return code;
	}

	public int categoryItemCount(String siteUrl) {
		SeleniumConnect seleniumConnect = new SeleniumConnect();
		String htmlContent = seleniumConnect.getPhantomJSConnect(siteUrl);
		Document document = Jsoup.parse(htmlContent);
		
		// 아이템 전체 갯수
		Elements span = document.select("#categoryListForm > div > div > span.num");
		int totalItemNum = 0 ;
		if(span.text() != null && !"".equals(span.text()))
			totalItemNum = Integer.parseInt(span.text());
		
		// 한 페이지에 보여지는 아이템수
		Elements li = document.select("#listBody > li");
		int pageItemNum = li.size();
		
		// 페이징 수
		int categoryItemPageNum = (int) Math.ceil((double)totalItemNum/pageItemNum);
		return categoryItemPageNum;
	}	

}
