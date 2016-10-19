package com.zaoo.lambda;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LambdaLocal {
    String[] path();

    String[] handler() default {};

    Class<? extends LambdaRequestDeserializer<?>>[] deserializer() default {};

    Class<? extends LambdaResponseSerializer<?>>[] serializer() default {};
}
