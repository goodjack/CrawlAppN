package com.eopcon.crawler.samsungcnt.service.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.ssl.TrustStrategy;

public class TrustAllStrategy implements TrustStrategy {
	@Override
	public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		return true;
	}
}
