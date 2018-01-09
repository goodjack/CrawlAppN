package com.eopcon.crawler.samsungcnt.service.net;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.net.proxy.ProxyServerPool;

@Component
public class HttpRequestService implements InitializingBean {

	protected static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	private int connectTimeout = 10000;
	private int readTimeout = 10000;
	private int retryCount = 3;
	
	private boolean followRedirects = true;

	private ThreadLocal<Map<String, Object>> configs = new ThreadLocal<>();
	private ThreadLocal<Request> local = new ThreadLocal<>();
	private CloseableHttpClient client = null;
	
	private Registry<CookieSpecProvider> cookieSpecRegistry;
	
	@Autowired
	protected ProxyServerPool proxyServerPool;
	@Autowired
	private ExceptionBuilder exceptionBuilder;
	@Autowired
	private HttpClientConnectionManager connectionManager;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// 쿠키 모두 받아 들임
		cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
	            .register("allCookie", new AllCookieSpecProvider())
	            .build();
		
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		HttpClientBuilder builder = HttpClientBuilder.create()
				.setSSLSocketFactory(sslsf)
				.setDefaultCookieSpecRegistry(cookieSpecRegistry)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, true));

		if (!followRedirects)
			builder.disableRedirectHandling();
		if (connectionManager != null)
			builder.setConnectionManager(connectionManager);

		client = builder.build();
	}
	
	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public void setConnectionManager(HttpClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public void interrupt() {
		Request request = getRequest();
		request.setInterrupted(true);
	}
	
	public void addRequestConfig(String key, Object value){
		Map<String, Object> config = configs.get();
		
		if(config == null) {
			config = new HashMap<>();
			configs.set(config);
		}
		config.put(key, value);
	}

	public void openConnection(String uri) {

		Assert.notNull(client, "HttpClient must be initializied!!");

		Request request = getRequest();
		throwIfInterrupted();

		request.clear();
		request.setUri(uri);
	}

	public void addRequestHeader(String name, String value) {
		Request request = getRequest();
		request.addHeader(name, value);
	}

	public void addFormParameter(String name, String value) {
		Request request = getRequest();
		request.addParameter(name, value);
	}

	public void addFormParameter(String name, File file) {
		Request request = getRequest();
		request.addAttatchment(name, file);
	}

	public void addCookie(String uri, String path, String name, String value, Date expiryDate) {
		Request request = getRequest();
		request.addCookie(uri, path, name, value, expiryDate);
	}

	public void addCookie(String uri, String path, String name, String value, int expiry) {
		Request request = getRequest();
		request.addCookie(uri, path, name, value, expiry == -1 ? null : new Date(System.currentTimeMillis() + expiry * 1000));
	}
	
	public void addCookie(String uri, String rawCookie) {
		Request request = getRequest();
		request.addCookie(uri, rawCookie);
	}
	
	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param body
	 * @param encoding
	 * @param closeIOAfterRespone (true: HTTP 요청 후 Pool에 Connection을 반납, false: Result 객체의 getString, getBytes Method 호출 시 Pool에 Connection을 반납)
	 * @return Result  
	 */
	public Result executeWithPost(String body, String encoding, boolean closeIOAfterRespone) throws IOException {
		throwIfInterrupted();
		Request request = getRequest();
		
		if (logger.isDebugEnabled())
			logger.debug("C->S | url -> {}, method -> POST, header -> {}, body -> {}", request.getURI(), request.getHeader(), body);
		
		return executeWithPost(request, new StringEntity(body, Charset.forName(encoding)), closeIOAfterRespone);
	}

	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param instream
	 * @param closeIOAfterRespone (true: HTTP 요청 후 Pool에 Connection을 반납, false: Result 객체의 getString, getBytes Method 호출 시 Pool에 Connection을 반납)
	 * @return Result  
	 */
	public Result executeWithPost(InputStream instream, boolean closeIOAfterRespone) throws IOException {
		throwIfInterrupted();
		Request request = getRequest();
		
		if (logger.isDebugEnabled())
			logger.debug("C->S | url -> {}, method -> POST, header -> {}, stream available -> {}", request.getURI(), request.getHeader(), instream.available());
		
		return executeWithPost(request, new InputStreamEntity(instream), closeIOAfterRespone);
	}

	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param request
	 * @param entity
	 * @param closeIOAfterRespone
	 * @return
	 * @throws IOException
	 */
	private Result executeWithPost(Request request, HttpEntity entity, boolean closeIOAfterRespone) throws IOException {

		RequestBuilder requestBuilder = RequestBuilder.post()
				.setUri(request.getURI());

		request.setHeader(requestBuilder);
		requestBuilder.setEntity(entity);

		long elapsedTime = System.currentTimeMillis();
		CloseableHttpResponse response = client.execute(requestBuilder.build(), request.getContext());
		
		Result result = new Result(response, closeIOAfterRespone);
		

		if (logger.isDebugEnabled())
			logger.debug("S->C | elapsedTime : {} ms. | url -> {}, responseCode -> {}, header -> {}", (System.currentTimeMillis() - elapsedTime), request.getURI(), result.getResponseCode(), result.getHeader());

		return result;
	}
	
	

	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param encoding
	 * @param closeIOAfterRespone (true: HTTP 요청 후 Pool에 Connection을 반납, false: Result 객체의 getString, getBytes Method 호출 시 Pool에 Connection을 반납)
	 * @return Result  
	 */
	public Result executeWithFormData(String encoding, boolean closeIOAfterRespone) throws IOException {

		throwIfInterrupted();
		Request request = getRequest();
		
		if (logger.isDebugEnabled())
			logger.debug("C->S | url -> {}, method -> POST, header -> {}, body -> {}", request.getURI(), request.getHeader(), request.getQueryString(encoding));

		RequestBuilder requestBuilder = RequestBuilder.post()
				.setCharset(Charset.forName(encoding))
				.setUri(request.getURI());

		request.setHeaderAndBody(requestBuilder);

		long elapsedTime = System.currentTimeMillis();
		CloseableHttpResponse response = client.execute(requestBuilder.build(), request.getContext());
		Result result = new Result(response, closeIOAfterRespone);

		if (logger.isDebugEnabled())
			logger.debug("S->C | elapsedTime : {} ms. | url -> {}, responseCode -> {}, header -> {}", (System.currentTimeMillis() - elapsedTime), request.getURI(), result.getResponseCode(), result.getHeader());

		return result;
	}

	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param closeIOAfterRespone (true: HTTP 요청 후 Pool에 Connection을 반납, false: Result 객체의 getString, getBytes Method 호출 시 Pool에 Connection을 반납)
	 * @return Result  
	 */
	public Result executeWithGet(boolean closeIOAfterRespone) throws IOException {

		throwIfInterrupted();
		Request request = getRequest();
		
		if (!logger.isDebugEnabled())
			logger.debug("C->S | url -> {}, method -> GET, header -> {}", request.getURI(), request.getHeader());
			logger.debug("request -> {}", request);

		RequestBuilder requestBuilder = RequestBuilder.get()
				.setUri(request.getURI());

		request.setHeader(requestBuilder);

		long elapsedTime = System.currentTimeMillis();
		CloseableHttpResponse response = client.execute(requestBuilder.build(), request.getContext());
				
		
		Result result = new Result(response, closeIOAfterRespone);

		if (!logger.isDebugEnabled())
			logger.debug("S->C | elapsedTime : {} ms. | url -> {}, responseCode -> {}, header -> {}", (System.currentTimeMillis() - elapsedTime), request.getURI(), result.getResponseCode(), result.getHeader());

		return result;
	}
	
	/**
	 * HTTP 전송요청을 수행한다.
	 * 
	 * @param closeIOAfterRespone (true: HTTP 요청 후 Pool에 Connection을 반납, false: Result 객체의 getString, getBytes Method 호출 시 Pool에 Connection을 반납)
	 * @return Result  
	 */
	public Result executeWithHead(boolean closeIOAfterRespone) throws IOException {

		throwIfInterrupted();
	
		Request request = getRequest();
		HttpHead headMethod = null;
		headMethod = new HttpHead(request.getURI());
		if (!logger.isDebugEnabled())
			logger.debug("C->S | url -> {}, method -> GET, header -> {}", request.getURI(), request.getHeader());
			logger.debug("request -> {}", request);

		//RequestBuilder requestBuilder = RequestBuilder.get()
		//		.setUri(request.getURI());

		//request.setHeader(requestBuilder);
		//request.setHeader(requestBuilder);
		long elapsedTime = System.currentTimeMillis();
		//CloseableHttpResponse response = client.execute(requestBuilder.build(), request.getContext());
		CloseableHttpResponse response = client.execute(headMethod);
		System.out.println("response -- > " + response.toString());
		Result result = new Result(response,closeIOAfterRespone,true);

		if (!logger.isDebugEnabled())
			logger.debug("S->C | elapsedTime : {} ms. | url -> {}, responseCode -> {}, header -> {}", (System.currentTimeMillis() - elapsedTime), request.getURI(), result.getResponseCode(), result.getHeader());

		return result;
	}
	
	/**
	 * Request 객체 반환(Thread별)
	 * 
	 * @return
	 */
	private Request getRequest() {
		Request request = local.get();

		if (request == null) {
			Map<String, Object> config = configs.get();
			
			int connectTimeout = this.connectTimeout;
			int readTimeout = this.readTimeout;
			boolean useProxy = true;
			
			if(config != null) {
				if(config.containsKey(OnlineStoreConst.KEY_CONNECT_TIMEOUT))
					connectTimeout = (Integer) config.get(OnlineStoreConst.KEY_CONNECT_TIMEOUT);
				if(config.containsKey(OnlineStoreConst.KEY_READ_TIMEOUT))
					readTimeout = (Integer) config.get(OnlineStoreConst.KEY_READ_TIMEOUT);
				if(config.containsKey(OnlineStoreConst.KEY_USE_PROXY))
					useProxy = (Boolean) config.get(OnlineStoreConst.KEY_USE_PROXY);
			}
			
			RequestConfig.Builder builder = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setConnectionRequestTimeout(connectTimeout)
					.setCookieSpec("allCookie")
					.setSocketTimeout(readTimeout)
					.setRedirectsEnabled(true)
					.setCircularRedirectsAllowed(true);
			
			if(useProxy){
				HttpHost host = proxyServerPool.getHost();
				if(host != null)
					builder.setProxy(host);
			}

			request = new Request(builder.build());
			local.set(request);
		}
		return request;
	}
	
	public void clearRequest(){
		local.remove();
	}

	private void throwIfInterrupted() {
		Request request = local.get();
		if (request.isInterrupted())
			exceptionBuilder.raiseException(ErrorType.ERROR_TASK_INTERRUPTED);
	}
}
