package com.eopcon.crawler.samsungcnt.common.file.factory;

public interface SessionFacotry<T> {

	public T getSession() throws Exception;

	public void releaseSession(T session);

	public boolean isAlive(T session);
}
