package com.eopcon.crawler.samsungcnt.service.net.proxy;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.InitializingBean;

public class PropertyProxyServerPool extends ProxyServerPool implements InitializingBean {
	
	private Properties properties;
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String proxies = properties.getProperty("crawler.proxy.servers");

		if (StringUtils.isNotBlank(proxies)) {
			for (String temp : proxies.split(";")) {
				HttpHost host = HttpHost.create(temp);
				proxyServers.add(host);
			}	
		}
	}
}
