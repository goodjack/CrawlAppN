package com.eopcon.crawler.samsungcnt.exception;

import org.apache.commons.lang.exception.ExceptionUtils;

public class BizException extends RuntimeException {

	private static final long serialVersionUID = 1948266154243105441L;

	private short errorNumber;

	public BizException(short errorNumber) {
		super();
		this.errorNumber = errorNumber;
	}

	public BizException(short errorNumber, String message) {
		super(message);
		this.errorNumber = errorNumber;
	}

	public BizException(short errorNumber, String message, Throwable throwable) {
		super(message, throwable);
		this.errorNumber = errorNumber;
	}

	public short getErrorNumber() {
		return errorNumber;
	}

	public String getStackTraceString() {
		return ExceptionUtils.getFullStackTrace(this);
	}
}
