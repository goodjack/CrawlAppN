package com.eopcon.crawler.samsungcnt.service.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.eopcon.crawler.samsungcnt.exception.ErrorType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Logging {
	ErrorType value() default ErrorType.NULL;
}
