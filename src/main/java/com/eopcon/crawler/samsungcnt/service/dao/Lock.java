package com.eopcon.crawler.samsungcnt.service.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;

public class Lock implements DisposableBean {

	private Map<String, Object> locks = new ConcurrentHashMap<>();
	
	/**
	 * Lock Object를 반환한다.
	 * 
	 * @param key
	 * @return
	 */
	public synchronized Object getLockObject(String key) {
		Object lock = locks.get(key);
		if(lock == null) {
			lock = new Object();
			locks.put(key, lock);
		}
		return lock;
	}

	@Override
	public void destroy() throws Exception {
		locks.clear();
	}
}
