package com.zaoo.lambda.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Only supported when Content-Type = 'application/x-www-form-urlencoded'
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RestForm {
    String value();

    boolean required() default true;

    Class<? extends RestParamDeserializer<?>> deserializer() default DefaultRestParamDeserializer.class;
}
