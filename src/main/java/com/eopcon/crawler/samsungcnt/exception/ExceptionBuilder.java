package com.eopcon.crawler.samsungcnt.exception;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import javax.net.ssl.SSLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
public class ExceptionBuilder {

	@Autowired
	protected MessageSource messageSource;

	public BizException buildException(Throwable cause) {
		if (cause instanceof BizException)
			return (BizException) cause;

		ErrorType type = ErrorType.ERROR_ETC;

		if (cause instanceof UnknownHostException)
			type = ErrorType.NETWORK_CONNECTION_FAIL;
		else if (cause instanceof SSLException)
			type = ErrorType.SSL_CONNECTION_FAIL;
		else if (cause instanceof SocketException)
			type = ErrorType.SOCKET_CLOSED;
		else if (cause instanceof SocketTimeoutException)
			type = ErrorType.REQUEST_TIMEOUT;
		else if (cause instanceof TooManyRedirectsException)
			type = ErrorType.TOO_MANY_REDIRECTS;

		return new BizException(type.getErrorNumber(), messageSource.getMessage(type.getMessageCode(), null, Locale.getDefault()), cause);
	}

	public void raiseException(Throwable cause) {
		throw buildException(cause);
	}

	public void raiseException(ErrorType type, Throwable cause, Object... args) {
		throw new BizException(type.getErrorNumber(), messageSource.getMessage(type.getMessageCode(), args, Locale.getDefault()), cause);
	}

	public void raiseException(ErrorType type, Object... args) {
		throw new BizException(type.getErrorNumber(), messageSource.getMessage(type.getMessageCode(), args, Locale.getDefault()));
	}
}
