package com.eopcon.crawler.samsungcnt.service.net;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;

public class Result implements Closeable, AutoCloseable {

	private StatusLine statusLine;
	private HttpEntity entity;

	private Map<String, List<String>> header;

	private byte[] content = null;
	private CloseableHttpResponse response;
	private boolean closeIOAfterRespone = true;

	public Result(CloseableHttpResponse response, boolean closeIOAfterRespone) throws IOException {
		this.closeIOAfterRespone = closeIOAfterRespone;
		
		
		this.response = response;
		this.entity = response.getEntity();
		this.statusLine = response.getStatusLine();

		toHeader(response);
		
		if(this.closeIOAfterRespone) {
			content = getBytes();
		}
	}
	
	public Result(CloseableHttpResponse response, boolean closeIOAfterRespone, boolean isHeader ) throws IOException {
		this.closeIOAfterRespone = closeIOAfterRespone;
		this.response = response;
		if(response.getEntity() != null)
			this.entity = response.getEntity();
		if(response.getStatusLine() != null)
			this.statusLine = response.getStatusLine();
		System.out.println("response-->" + response.toString());
		toHeader(response);
		System.out.println("header --> " + header.toString());
		if(this.closeIOAfterRespone) {
			content = getBytes();
		}
	}
	
	private void toHeader(HttpResponse response) {

		Header[] headers = response.getAllHeaders();
		header = new HashMap<String, List<String>>(headers.length);

		for (Header h : headers) {
			String name = h.getName();
			String value = h.getValue();

			if (header.containsKey(name)) {
				header.get(name).add(value);
			} else {
				List<String> list = new ArrayList<String>();
				list.add(value);
				header.put(name, list);
			}
		}
	}

	public Map<String, List<String>> getHeader() {
		return header;
	}

	public int getResponseCode() {
		return statusLine.getStatusCode();
	}

	public void writeTo(OutputStream outputStream) throws IOException {
		if (entity != null && closeIOAfterRespone != true) {
			InputStream in = entity.getContent();
			
			try {
				entity.writeTo(outputStream);
				
				in.close();
				response.close();
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(response);
			}
		}
	}

	public byte[] getBytes() throws IOException {
		if (content == null) {
			InputStream in = null;
			try {
				in = getInputStream();
				content = IOUtils.toByteArray(in);
				
				in.close();
				response.close();
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(response);
			}
		}
		return content;
	}

	public String getString(String encoding) throws IOException {
		return new String(getBytes(), encoding);
	}

	public String getString() throws IOException {
		return new String(getBytes(), getCharset());
	}

	public long getContentLength() throws IOException {
		return (entity == null) ? 0 : entity.getContentLength();
	}

	public String getCharset() throws IOException {
		String encoding = "";
		List<String> contentTypes = header.get("Content-Type");
		String contentType;

		if (contentTypes.size() > 0) {
			contentType = contentTypes.get(0);
			String[] temp = contentType.split(";");
			for (String str : temp) {
				if (str.trim().indexOf("charset=") > -1) {
					String charset = str.trim().replaceAll("charset=(.+)", "$1");
					encoding = charset;
					break;
				}
			}
		} else {
			throw new IllegalStateException("No header value -> Content-Type");
		}

		if (StringUtils.isEmpty(encoding))
			throw new IllegalStateException("Parsing header value -> " + contentType);

		return encoding;
	}

	private InputStream getInputStream() throws IOException {
		return (entity == null) ? null : entity.getContent();
	}
	
	@Override
	public void close(){
		InputStream in = null;
		try {
			in = getInputStream();
			if (in != null)
				in.close();
			response.close();
		} catch (Exception e) {
			
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(response);
		}
	}
}
