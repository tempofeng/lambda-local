package com.zaoo.lambda.rest;

import java.lang.annotation.Annotation;

public class RestParamDeserializerFactory implements RestParamDeserializer<Object> {


    public RestParamDeserializer getDeserializer(Class<?> cls, Annotation annotation) {
        if (annotation instanceof RestQuery) {
            RestQuery restQuery = (RestQuery) annotation;
            if (restQuery.deserializer() != null) {
                return createDeserializer(restQuery.deserializer());
            }
        }

        if (annotation instanceof RestForm) {
            RestForm restForm = (RestForm) annotation;
            if (restForm.deserializer() != null) {
                return createDeserializer(restForm.deserializer());
            }
        }

        if (annotation instanceof RestHeader) {
            RestHeader restHeader = (RestHeader) annotation;
            if (restHeader.deserializer() != null) {
                return createDeserializer(restHeader.deserializer());
            }
        }

        if (String.class.isAssignableFrom(cls)) {
            return new StringRestParamDeserializer();
        }
        if (Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)) {
            return new IntegerRestParamDeserializer();
        }
        if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)) {
            return new LongRestParamDeserializer();
        }
        if (Float.class.isAssignableFrom(cls) || float.class.isAssignableFrom(cls)) {
            return new FloatRestParamDeserializer();
        }
        if (Double.class.isAssignableFrom(cls) || double.class.isAssignableFrom(cls)) {
            return new DoubleRestParamDeserializer();
        }
        if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
            return new BooleanRestParamDeserializer();
        }
        if (Enum.class.isAssignableFrom(cls)) {
            return new EnumRestParamDeserializer();
        }

        throw new IllegalArgumentException("Unable to get RestParamDeserializer of this type:" + cls);
    }

    private RestParamDeserializer createDeserializer(Class<? extends RestParamDeserializer<?>> deserializer) {
        try {
            return deserializer.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deserialize(String str, Class<?> cls) {
        return null;
    }

    public static class IntegerRestParamDeserializer implements RestParamDeserializer<Integer> {
        @Override
        public Integer deserialize(String str, Class<?> cls) {
            return Integer.parseInt(str);
        }
    }

    public static class LongRestParamDeserializer implements RestParamDeserializer<Long> {
        @Override
        public Long deserialize(String str, Class<?> cls) {
            return Long.parseLong(str);
        }
    }

    public static class FloatRestParamDeserializer implements RestParamDeserializer<Float> {
        @Override
        public Float deserialize(String str, Class<?> cls) {
            return Float.parseFloat(str);
        }
    }

    public static class DoubleRestParamDeserializer implements RestParamDeserializer<Double> {
        @Override
        public Double deserialize(String str, Class<?> cls) {
            return Double.parseDouble(str);
        }
    }

    public static class BooleanRestParamDeserializer implements RestParamDeserializer<Boolean> {
        @Override
        public Boolean deserialize(String str, Class<?> cls) {
            return Boolean.parseBoolean(str);
        }
    }

    public static class StringRestParamDeserializer implements RestParamDeserializer<String> {
        @Override
        public String deserialize(String str, Class<?> cls) {
            return str;
        }
    }

    public static class EnumRestParamDeserializer implements RestParamDeserializer<Enum<?>> {
        @Override
        public Enum<?> deserialize(String str, Class<?> cls) {
            return Enum.valueOf((Class) cls, str);
        }
    }
}

