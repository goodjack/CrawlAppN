package com.eopcon.crawler.samsungcnt.service.parser;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.net.HttpRequestService;
import com.eopcon.crawler.samsungcnt.service.net.Result;

@Component
public class ScriptManager {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	private ThreadLocal<ScriptManagerInternal> local = new ThreadLocal<>();

	@Autowired
	private HttpRequestService request;

	public ScriptManager enter() {
		object().enter();
		return this;
	}

	public ScriptManager addAllScript(Document doc, String baseUrl, String encoding) throws Exception {
		int index = 1;

		try {
			Elements elements = doc.select("script");

			for (Element element : elements) {
				if (element.hasAttr("src")) {
					String src = element.attr("src");

					if (src.startsWith("http://") || src.startsWith("https://")) {
						addScript(src, src, encoding);
					} else {
						src = src.startsWith("/") ? src : "/" + src;
						addScript(src, baseUrl + src, encoding);
					}
				} else {
					addScript("anonymous#" + index++, element.html());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw e;
		}
		return this;
	}

	public ScriptManager addScript(String sourceName, String jsUrl, String encoding) throws Exception {
		object().addScript(request, sourceName, jsUrl, encoding);
		return this;
	}

	public ScriptManager addScript(String sourceName, String content) {
		object().addScript(sourceName, content);
		return this;
	}

	public ScriptManager addScript(String sourceName, Reader reader) {
		object().addScript(sourceName, reader);
		return this;
	}

	public Object getObject(String name) {
		return object().getObject(name);
	}

	private ScriptManagerInternal object() {
		ScriptManagerInternal smi = local.get();
		if (smi == null) {
			smi = new ScriptManagerInternal();
			local.set(smi);
		}
		return smi;
	}

	public void exit() {
		if (local.get() != null) {
			object().exit();
			local.remove();
		}
	}

	private class ScriptManagerInternal {
		private Context rhino = null;
		private Scriptable scope;

		void enter() {
			if (rhino == null) {
				rhino = Context.enter();
				rhino.setOptimizationLevel(-1);
				scope = rhino.initStandardObjects();
			}
		}

		void addScript(HttpRequestService request, String sourceName, String jsUrl, String encoding) throws Exception {
			Result result = request.executeWithGet(true);
			int responseCode = result.getResponseCode();

			if (responseCode == HttpStatus.SC_OK) {
				String content = result.getString(encoding);
				if (StringUtils.isNotEmpty(content))
					addScript(sourceName, content);
			}
		}

		void addScript(String sourceName, String content) {
			rhino.evaluateString(scope, content, sourceName, 1, null);
		}

		void addScript(String sourceName, Reader reader) {
			try {
				rhino.evaluateReader(scope, reader, sourceName, 1, null);
			} catch (IOException e) {
				throw new IllegalArgumentException(e.getMessage(), e);
			}
		}

		Object getObject(String name) {
			return scope.get(name, scope);
		}

		void exit() {
			if (rhino != null) {
				try {
					Context.exit();
				} catch (Exception e) {
				} finally {
					rhino = null;
				}
			}
		}
	}
}
