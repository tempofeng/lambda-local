package com.zaoo.lambda;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LambdaLocal {
    /**
     * The server path of this request handler when you start lambda-local.
     * The path is like {@link HttpServletRequest#getPathInfo()}.
     * Ex: '/helloWorld'.
     */
    String[] value();

    String[] handler() default {};

    Class<? extends LambdaRequestDeserializer<?>>[] deserializer() default {};

    Class<? extends LambdaResponseSerializer<?>>[] serializer() default {};
}
