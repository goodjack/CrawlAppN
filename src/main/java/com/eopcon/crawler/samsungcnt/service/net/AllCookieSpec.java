package com.eopcon.crawler.samsungcnt.service.net;

import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.DefaultCookieSpec;

public class AllCookieSpec extends DefaultCookieSpec {
	
	private static final String[] DEFAULT_DATE_PATTERNS = new String[] {
        DateUtils.PATTERN_RFC1123,
        DateUtils.PATTERN_RFC1036,
        DateUtils.PATTERN_ASCTIME,
        "EEE, dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MMM-yyyy HH-mm-ss z",
        "EEE, dd MMM yy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH:mm:ss z",
        "EEE dd MMM yyyy HH:mm:ss z",
        "EEE dd-MMM-yyyy HH-mm-ss z",
        "EEE dd-MMM-yy HH:mm:ss z",
        "EEE dd MMM yy HH:mm:ss z",
        "EEE,dd-MMM-yy HH:mm:ss z",
        "EEE,dd-MMM-yyyy HH:mm:ss z",
        "EEE, dd-MM-yyyy HH:mm:ss z",
        "EEE, dd-MMM-yy HH:mm:ss z" // NetscapeDraftSpec
    };
	
	public AllCookieSpec() {
        super(DEFAULT_DATE_PATTERNS, false);
    }

	@Override
	public void validate(Cookie cookie, CookieOrigin origin) throws MalformedCookieException {
	}
}
