package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@LambdaLocal("/testRestPath8")
@CrossOrigin
public class TestRestFunction8 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/test1")
    public List<String> test1(@RestParam("test") List<String> test) {
        return test;
    }

    @RestMethod(httpMethod = HttpMethod.GET, path = "/test2")
    public Map<String, String> test2(@RestParam("test") Map<String, String> test) {
        return test;
    }

    @RestMethod(httpMethod = HttpMethod.GET, path = "/test3")
    public TestClass test3(@RestParam("test") TestClass test) {
        return test;
    }

    @RestMethod(httpMethod = HttpMethod.GET, path = "/test4")
    public List<TestClass> test4(@RestParam("test") List<TestClass> test) {
        return test;
    }

    @RestMethod(httpMethod = HttpMethod.GET, path = "/test5")
    public Map<String, TestClass> test5(@RestParam("test") Map<String, TestClass> test) {
        return test;
    }

    public static class TestClass {
        public String str;

        public TestClass() {
        }

        public TestClass(String str) {
            this.str = str;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return Objects.equals(str, testClass.str);
        }

        @Override
        public int hashCode() {
            return Objects.hash(str);
        }
    }
}
