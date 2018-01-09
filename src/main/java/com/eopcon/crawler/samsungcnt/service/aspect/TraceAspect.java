package com.eopcon.crawler.samsungcnt.service.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.eopcon.crawler.samsungcnt.exception.BizException;
import com.eopcon.crawler.samsungcnt.exception.ErrorType;
import com.eopcon.crawler.samsungcnt.exception.ExceptionBuilder;
import com.eopcon.crawler.samsungcnt.service.OnlineStoreConst;
import com.eopcon.crawler.samsungcnt.service.aspect.annotation.Logging;

@Aspect
@Component
public class TraceAspect implements InitializingBean {

	private static Logger logger = LoggerFactory.getLogger(OnlineStoreConst.LOGGER_NAME_COMMON);

	@Autowired
	private ExceptionBuilder exceptionBuilder;

	@Override
	public void afterPropertiesSet() throws Exception {

	}

	@Pointcut("target(com.eopcon.crawler.samsungcnt.service.parser.OnlineStoreParser) && execution(@com.eopcon.crawler.samsungcnt.service.aspect.annotation.Logging * *(..))")
	public void pointcutOnlineStoreParser() {

	}

	@Around("pointcutOnlineStoreParser() && args(content,..) && @annotation(logging)")
	public Object aroundOnlineStoreParser(ProceedingJoinPoint joinPoint, Logging logging, String content) throws Throwable {
		if (joinPoint.getSignature() instanceof MethodSignature) {
			MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
			String className = methodSignature.getDeclaringType().getSimpleName();
			String methodName = methodSignature.getName();

			Object result = null;
			try {
				long elapsedTime = System.currentTimeMillis();
				result = joinPoint.proceed();
				logger.debug(String.format("%s.%s -> elapsedTime : %s ms.", className, methodName, System.currentTimeMillis() - elapsedTime));
			} catch (BizException e) {
				short errorNumber = e.getErrorNumber();
				if (errorNumber == ErrorType.ERROR_PARSING_FAIL.getErrorNumber() || errorNumber == ErrorType.ERROR_PARSING_MATERIALS_FAIL.getErrorNumber())
					logger.error("[PARSINT_ERROR_CONTENT] -> " + content);
				throw e;
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				
				ErrorType type = logging.value() == ErrorType.NULL ? ErrorType.ERROR_PARSING_FAIL : logging.value();
				if (type == ErrorType.ERROR_PARSING_FAIL || type == ErrorType.ERROR_PARSING_MATERIALS_FAIL)
					logger.error("[PARSINT_ERROR_CONTENT] -> " + content);
				exceptionBuilder.raiseException(type, e);
			}
			return result;
		}
		return joinPoint.proceed();
	}
}
