package com.zaoo.lambda.rest;

public class DefaultRestParamDeserializer implements RestParamDeserializer<Object> {
    @Override
    public Object deserialize(String str, Class<?> cls) {
        if (String.class.isAssignableFrom(cls)) {
            return str;
        }
        if (Integer.class.isAssignableFrom(cls) || int.class.isAssignableFrom(cls)) {
            return Integer.parseInt(str);
        }
        if (Long.class.isAssignableFrom(cls) || long.class.isAssignableFrom(cls)) {
            return Long.parseLong(str);
        }
        if (Float.class.isAssignableFrom(cls) || float.class.isAssignableFrom(cls)) {
            return Float.parseFloat(str);
        }
        if (Double.class.isAssignableFrom(cls) || double.class.isAssignableFrom(cls)) {
            return Double.parseDouble(str);
        }
        if (Boolean.class.isAssignableFrom(cls) || boolean.class.isAssignableFrom(cls)) {
            return Boolean.parseBoolean(str);
        }
        if (Enum.class.isAssignableFrom(cls)) {
            return Enum.valueOf((Class) cls, str);
        }

        throw new IllegalArgumentException("Unable to get RestParamDeserializer of this type:" + cls);
    }
}
