package com.eopcon.crawler.samsungcnt.service.net.proxy;

import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.net.Result;
import com.eopcon.crawler.samsungcnt.service.net.TrustAllStrategy;

public class ProxyServerPool implements InitializingBean {
	
	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_BATCH);

	protected LinkedBlockingQueue<HttpHost> proxyServers = new LinkedBlockingQueue<>();
	private String testUrl;

	private CloseableHttpClient client = null;

	public void setTestUrl(String testUrl) {
		this.testUrl = testUrl;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		HttpClientBuilder builder = HttpClientBuilder.create()
				.setSSLSocketFactory(sslsf)
				.disableRedirectHandling();

		client = builder.build();
	}

	public HttpHost getHost() {
		HttpHost host = null;

		if (proxyServers.size() > 0) {
			try {
				host = proxyServers.take();
			} catch (InterruptedException e) {
			} finally {
				if (host != null)
					proxyServers.add(host);
			}
		}
		return host;
	}

	protected boolean test(HttpHost host) {
		HttpUriRequest request = null;
		CloseableHttpResponse response = null;

		try {
			RequestBuilder requestBuilder = RequestBuilder.get().setUri(testUrl);

			RequestConfig.Builder config = RequestConfig.custom()
					.setConnectTimeout(2000)
					.setConnectionRequestTimeout(3000)
					.setProxy(host)
					.setSocketTimeout(3000);

			HttpClientContext context = HttpClientContext.create();
			context.setAttribute(HttpClientContext.REQUEST_CONFIG, config.build());
			request = requestBuilder.build();
			response = client.execute(request, context);
			
			return testResponse(new Result(response, true));
		} catch (Exception e) {
			// logger.error(e.getMessage(), e);
		} finally {
			if (request != null)
				request.abort();
			IOUtils.closeQuietly(response);
		}
		return false;
	}

	protected boolean testResponse(Result result) throws Exception {
		if (result.getResponseCode() == HttpStatus.SC_OK) 
			return true;
		return false;
	}
}
