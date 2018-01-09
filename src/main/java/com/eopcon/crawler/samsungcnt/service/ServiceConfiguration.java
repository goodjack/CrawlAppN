package com.eopcon.crawler.samsungcnt.service;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.eopcon.crawler.samsungcnt.service.dao.Lock;
import com.eopcon.crawler.samsungcnt.service.impl.HandsomeStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.HnmStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.LfmallStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.MixxoStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.SpaoStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.UniqloStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.impl.ZaraStoreCrawler;
import com.eopcon.crawler.samsungcnt.service.net.TrustAllStrategy;
import com.eopcon.crawler.samsungcnt.service.net.proxy.PropertyProxyServerPool;
import com.eopcon.crawler.samsungcnt.service.net.proxy.ProxyServerPool;
import com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HandsomeStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.HnmStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.LfmallStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.MixxoStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.SpaoStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.UniqloStoreParser;
import com.eopcon.crawler.samsungcnt.service.parser.impl.ZaraStoreParser;

@Configuration
public class ServiceConfiguration {

	@Autowired
	private ApplicationContext context;

	@Bean(name = { OnlineStoreConst.BEAN_NAME_ONLINE_STORE_CRAWLER })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public OnlineStoreCrawler onlineStoreCrawler(OnlineStoreConst type, String collectDay) {
		OnlineStoreCrawler crawler = null;
		ServiceConfig config = (ServiceConfig) context.getBean(OnlineStoreConst.BEAN_NAME_SERVICE_CONFIG, type, collectDay);

		switch (type) {
		case SPAO:
			crawler = new SpaoStoreCrawler(config, type);
			break;
		case UNIQLO:
			crawler = new UniqloStoreCrawler(config, type);
			break;
		case HM:
			crawler = new HnmStoreCrawler(config, type);
			break;
		case ZARA:
			crawler = new ZaraStoreCrawler(config, type);
			break;
		case LFMALL:
			crawler = new LfmallStoreCrawler(config, type);
			break;
		case MIXXO:
			crawler = new MixxoStoreCrawler(config, type);
			break;
		case HANDSOME:
			crawler = new HandsomeStoreCrawler(config, type);
			break;
		case _8S:
		case GMARKET:
		case DARKVICTORY:
		case MIXXMIX:
		case MUSINSA:
			crawler = new UniqloStoreCrawler(config, type);
			break;
		}
		return crawler;
	}

	@Bean(name = { OnlineStoreConst.BEAN_NAME_ONLINE_STORE_PARSER })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public OnlineStoreParser onlineStoreParser(OnlineStoreConst type) {
		OnlineStoreParser parser = null;

		switch (type) {
		case SPAO:
			parser = new SpaoStoreParser(type);
			break;
		case UNIQLO:
			parser = new UniqloStoreParser(type);
			break;
		case HM:
			parser = new HnmStoreParser(type);
			break;
		case ZARA:
			parser = new ZaraStoreParser(type);
			break;
		case LFMALL:
			parser = new LfmallStoreParser(type);
			break;
		case MIXXO:
			parser = new MixxoStoreParser(type);
			break;
		case HANDSOME:
			parser = new HandsomeStoreParser(type);
			break;
		case _8S:
		case GMARKET:
		case DARKVICTORY:
		case MIXXMIX:
		case MUSINSA:
			break;
		}
		return parser;
	}

	@Bean(name = { OnlineStoreConst.BEAN_NAME_CATEGORY_MAPPER })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public CategoryMapper categoryMapper(OnlineStoreConst type) {
		return new CategoryMapper(type);
	}

	@Bean(name = { OnlineStoreConst.BEAN_NAME_SERVICE_CONFIG })
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public ServiceConfig serviceConfig(OnlineStoreConst type, String collectDay) {
		return new ServiceConfig(type, collectDay);
	}

	@Bean
	public HttpClientConnectionManager poolingConnManager() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustAllStrategy()).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslsf).build();

		PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

		poolingConnManager.setMaxTotal(20);
		poolingConnManager.setDefaultMaxPerRoute(5);

		return poolingConnManager;
	}

	@Bean
	public ProxyServerPool proxyServerPool() throws Exception {
		Properties properties = context.getBean("properties", Properties.class);
		PropertyProxyServerPool pool = new PropertyProxyServerPool();
		pool.setProperties(properties);
		return pool;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public Lock lock() throws Exception {
		return new Lock();
	}
}
