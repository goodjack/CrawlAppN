package com.eopcon.crawler.samsungcnt.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class JsoupConnect {
	private Document document;
	
	public void setJsoupConnet(String url) {
		try {
			
			document = Jsoup.connect(url).ignoreContentType(true)
					.userAgent(
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36")
					.referrer("http://www.thehandsome.com/ko/").timeout(2000).followRedirects(true).get();
//			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("catch : " + e.getMessage());
			setJsoupConnet(url);
		}
		
		
	}
	public void setJsoupConnet(String url, int timeout) {
		
		try {
			document = Jsoup.connect(url).ignoreContentType(true)
					.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
					.referrer("http://www.thehandsome.com/ko").timeout(timeout).followRedirects(true).get();
		} catch (Exception e) {
			setJsoupConnet(url);
		}
	}
	public Document getJsoupConnect() {
		return document;
	}
}
