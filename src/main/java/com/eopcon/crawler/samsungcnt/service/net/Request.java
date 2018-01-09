package com.eopcon.crawler.samsungcnt.service.net;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

public class Request {

	private URI uri;
	private boolean interrupted = false;

	private List<String[]> headers = new ArrayList<String[]>();
	private List<String[]> parameters = new ArrayList<String[]>();
	private List<Object[]> attatchments = new ArrayList<Object[]>();

	private HttpContext context = new BasicHttpContext();

	public Request(RequestConfig config) {
		context.setAttribute(HttpClientContext.REQUEST_CONFIG, config);
		context.setAttribute(HttpClientContext.COOKIE_STORE, new BasicCookieStore());
	}

	public void setUri(String uri) {
		try {
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public URI getURI() {
		return uri;
	}

	public void addHeader(String name, String value) {
		headers.add(new String[] { name, value });
	}

	public void addCookie(String uri, String path, String name, String value, Date expiryDate) {
		try {
			URI u = new URI(uri);

			BasicCookieStore cookieStore = (BasicCookieStore) context.getAttribute(HttpClientContext.COOKIE_STORE);
			BasicClientCookie cookie = new BasicClientCookie(name, value);

			if (StringUtils.isNotBlank(path))
				cookie.setPath(path);
			cookie.setDomain(u.getAuthority());

			if (expiryDate != null)
				cookie.setExpiryDate(expiryDate);

			cookieStore.addCookie(cookie);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void addCookie(String uri, String rawCookie) {

		try {
			URI u = new URI(uri);

			BasicCookieStore cookieStore = (BasicCookieStore) context.getAttribute(HttpClientContext.COOKIE_STORE);

			String[] rawCookieParams = rawCookie.split(";");
			String[] rawCookieNameAndValue = rawCookieParams[0].split("=");

			if (rawCookieNameAndValue.length != 2) {
				throw new IllegalArgumentException("Invalid cookie: missing name and value.");
			}

			String cookieName = rawCookieNameAndValue[0].trim();
			String cookieValue = rawCookieNameAndValue[1].trim();
			BasicClientCookie cookie = new BasicClientCookie(cookieName, cookieValue);

			for (int i = 1; i < rawCookieParams.length; i++) {
				String rawCookieParamNameAndValue[] = rawCookieParams[i].trim().split("=");

				String paramName = rawCookieParamNameAndValue[0].trim();

				if (paramName.equalsIgnoreCase("secure")) {
					cookie.setSecure(true);
				} else {
					if (rawCookieParamNameAndValue.length != 2) {
						throw new IllegalArgumentException("Invalid cookie: attribute not a flag or missing value.");
					}

					String paramValue = rawCookieParamNameAndValue[1].trim();

					if (paramName.equalsIgnoreCase("expires")) {
						Date expiryDate = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).parse(paramValue);
						cookie.setExpiryDate(expiryDate);
					} else if (paramName.equalsIgnoreCase("max-age")) {
						long maxAge = Long.parseLong(paramValue);
						Date expiryDate = new Date(System.currentTimeMillis() + maxAge);
						cookie.setExpiryDate(expiryDate);
					} else if (paramName.equalsIgnoreCase("domain")) {
						cookie.setDomain(paramValue);
					} else if (paramName.equalsIgnoreCase("path")) {
						cookie.setPath(paramValue);
					} else if (paramName.equalsIgnoreCase("comment")) {
						cookie.setPath(paramValue);
					} else {
						throw new IllegalArgumentException("Invalid cookie: invalid attribute name.");
					}
				}
			}

			String doamin = cookie.getDomain();
			if (StringUtils.isEmpty(doamin))
				cookie.setDomain(u.getAuthority());

			cookieStore.addCookie(cookie);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	public void addParameter(String name, String value) {
		parameters.add(new String[] { name, value });
	}

	public void addAttatchment(String name, File file) {

		if (!file.exists() || !file.isFile())
			throw new IllegalArgumentException("The File is not exists or file. -> " + file.getAbsolutePath());

		attatchments.add(new Object[] { name, file.getName(), file });
	}

	public void clear() {
		headers.clear();
		parameters.clear();
		attatchments.clear();
	}

	public boolean isMultiPartRequest() {
		return attatchments.size() > 0;
	}

	public void setHeaderAndBody(RequestBuilder requestBuilder) {
		setHeader(requestBuilder);
		setBody(requestBuilder);
	}

	public void setHeader(RequestBuilder requestBuilder) {
		for (String[] header : headers) {
			String name = header[0];
			String value = header[1];

			requestBuilder.addHeader(name, value);
		}
	}

	public void setBody(RequestBuilder requestBuilder) {
		if (isMultiPartRequest()) {

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();

			for (String[] parameter : parameters) {
				String name = parameter[0];
				String value = parameter[1];

				builder.addTextBody(name, value);
			}

			for (Object[] attatchment : attatchments) {
				String name = (String) attatchment[0];
				String filename = (String) attatchment[1];
				File file = (File) attatchment[2];

				builder.addBinaryBody(name, file, ContentType.APPLICATION_OCTET_STREAM, filename);
			}

			HttpEntity entity = builder.build();
			requestBuilder.setEntity(entity);
		} else {
			for (String[] parameter : parameters) {
				String name = parameter[0];
				String value = parameter[1];

				requestBuilder.addParameter(name, value);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<String>> getHeader() {
		if (headers.size() > 0) {
			Map<String, List<String>> map = new HashMap<>();

			for (String[] header : headers) {
				String name = header[0];
				String value = header[1];

				if (map.containsKey(name)) {
					List<String> values = map.get(name);
					values.add(value);
				} else {
					List<String> values = new ArrayList<>();
					values.add(value);
					map.put(name, values);
				}
			}
			return map;
		}
		return Collections.EMPTY_MAP;
	}

	public String getQueryString(String encoding) {
		try {
			if (parameters.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (String[] param : parameters)
					sb.append("&").append(param[0] + "=" + URLEncoder.encode(param[1], encoding));
				return sb.toString().substring(1);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return StringUtils.EMPTY;
	}

	public HttpContext getContext() {
		return context;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}
}
