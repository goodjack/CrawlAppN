package com.eopcon.crawler.samsungcnt.exception;

public class TooManyRedirectsException extends RuntimeException {

	private static final long serialVersionUID = -933210017798486471L;

	public TooManyRedirectsException(String message) {
		super(message);
	}
}
