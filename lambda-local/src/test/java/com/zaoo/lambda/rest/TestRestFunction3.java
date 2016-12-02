package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath3")
public class TestRestFunction3 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public Response hello(@RestParam("key1") String value1,
                          @RestParam("key2") Integer value2,
                          @RestParam("key3") int value3,
                          @RestParam("key4") Long value4,
                          @RestParam("key5") long value5,
                          @RestParam("key6") Float value6,
                          @RestParam("key7") float value7,
                          @RestParam("key8") Double value8,
                          @RestParam("key9") double value9,
                          @RestParam("key10") Boolean value10,
                          @RestParam("key11") boolean value11,
                          @RestParam("gender") Gender gender,
                          @RestParam(value = "customClass", deserializer = CustomRestParamDeserializer.class) CustomClass customClass) {
        return new Response(value1,
                value2,
                value3,
                value4,
                value5,
                value6,
                value7,
                value8,
                value9,
                value10,
                value11,
                gender,
                customClass);
    }

    public static class CustomClass {
        public String firstName;
        public String lastName;

        public CustomClass() {
        }

        public CustomClass(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }

    public static class CustomRestParamDeserializer implements RestParamDeserializer<CustomClass> {
        @Override
        public CustomClass deserialize(String str, JavaType javaType) {
            String[] split = str.split(",");
            return new CustomClass(split[0], split[1]);
        }
    }

    public enum Gender {
        MALE, FEMALE
    }

    public static class Response {
        public String key1;
        public Integer key2;
        public int key3;
        public Long key4;
        public long key5;
        public Float key6;
        public float key7;
        public Double key8;
        public double key9;
        public Boolean key10;
        public boolean key11;
        public Gender gender;
        public CustomClass customClass;

        public Response() {
        }

        public Response(String key1,
                        Integer key2,
                        int key3,
                        Long key4,
                        long key5,
                        Float key6,
                        float key7,
                        Double key8,
                        double key9,
                        Boolean key10,
                        boolean key11,
                        Gender gender,
                        CustomClass customClass) {
            this.key1 = key1;
            this.key2 = key2;
            this.key3 = key3;
            this.key4 = key4;
            this.key5 = key5;
            this.key6 = key6;
            this.key7 = key7;
            this.key8 = key8;
            this.key9 = key9;
            this.key10 = key10;
            this.key11 = key11;
            this.gender = gender;
            this.customClass = customClass;
        }
    }
}
