package com.eopcon.crawler.samsungcnt.service.net;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.protocol.HttpContext;

public class AllCookieSpecProvider implements CookieSpecProvider{
	@Override
	public CookieSpec create(HttpContext context) {
		return new AllCookieSpec();
	}
}
