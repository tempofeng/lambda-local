package com.zaoo.lambda.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestMethod {
    /**
     * Use {@link HttpMethod#ANY} if this method accepts all HTTP methods.
     */
    HttpMethod httpMethod() default HttpMethod.ANY;

    String path();
}
