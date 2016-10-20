package com.zaoo.lambda;

import com.zaoo.lambda.test.*;
import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LambdaFunctionAnnotationScannerTest {
    private LambdaFunctionAnnotationScanner scanner;

    @Before
    public void setUp() throws Exception {
        scanner = new LambdaFunctionAnnotationScanner();
    }


    @Test
    public void listLambdaFunctions() throws Exception {
        LambdaProxyRequestDeserializer deserializer = new LambdaProxyRequestDeserializer();
        LambdaProxyResponseSerializer serializer = new LambdaProxyResponseSerializer();
        LambdaFunction lambdaFunction1 = new LambdaFunction("/testPath1",
                Class.forName("com.zaoo.lambda.test.TestFunction1"),
                null,
                deserializer,
                serializer);
        Class<?> handlerClass = Class.forName("com.zaoo.lambda.test.TestFunction2");
        LambdaFunction lambdaFunction21 = new LambdaFunction("/testPath2.1",
                handlerClass,
                ReflectionUtils.getMethods(handlerClass, method -> "method1".equals(method.getName())).stream()
                        .findFirst()
                        .get(),
                deserializer,
                serializer);
        handlerClass = Class.forName("com.zaoo.lambda.test.TestFunction2");
        LambdaFunction lambdaFunction22 = new LambdaFunction("/testPath2.2",
                handlerClass,
                ReflectionUtils.getMethods(handlerClass, method -> "method2".equals(method.getName())).stream()
                        .findFirst()
                        .get(),
                deserializer,
                serializer);
        LambdaFunction lambdaFunction3 = new LambdaFunction("/testPath3",
                Class.forName("com.zaoo.lambda.test.TestFunction3"),
                null,
                new StringDesearializer(),
                new StringSerializer());
        LambdaFunction lambdaFunction4 = new LambdaFunction("/testPath4",
                Class.forName("com.zaoo.lambda.test.TestFunction4"),
                null,
                new LambdaProxyRequestStreamDeserializer(),
                new LambdaProxyResponseStreamSerializer());
        LambdaFunction lambdaFunction5 = new LambdaFunction("/testPath3",
                Class.forName("com.zaoo.lambda.test.TestFunction3"),
                null,
                new StringStreamDesearializer(),
                new StringStreamSerializer());

        List<LambdaFunction> lambdaFunctions = scanner.listLambdaFunctions(
                "com.zaoo.lambda.test");
        assertThat(lambdaFunctions.size()).isEqualTo(6);
        assertThat(lambdaFunctions).contains(lambdaFunction1,
                lambdaFunction21,
                lambdaFunction22,
                lambdaFunction3,
                lambdaFunction4,
                lambdaFunction5);
    }

    @Test
    public void createLambdaFunction() throws Exception {
        LambdaFunction lambdaFunction = scanner.createLambdaFunction("/testPath1",
                "com.zaoo.lambda.test.TestFunction1", null, null);
        assertThat(lambdaFunction.getPath()).isEqualTo("/testPath1");
        assertThat(lambdaFunction.getHandlerClass()).isEqualTo(TestFunction1.class);
        assertThat(lambdaFunction.getHandlerMethod()).isNull();
        assertThat(lambdaFunction.getDeserializer().getClass()).isEqualTo(LambdaProxyRequestDeserializer.class);
        assertThat(lambdaFunction.getSerializer().getClass()).isEqualTo(LambdaProxyResponseSerializer.class);
    }

    @Test
    public void createLambdaFunction_requestStreamHandler() throws Exception {
        LambdaFunction lambdaFunction = scanner.createLambdaFunction("/testPath4",
                "com.zaoo.lambda.test.TestFunction4", null, null);
        assertThat(lambdaFunction.getPath()).isEqualTo("/testPath4");
        assertThat(lambdaFunction.getHandlerClass()).isEqualTo(TestFunction4.class);
        assertThat(lambdaFunction.getHandlerMethod()).isNull();
        assertThat(lambdaFunction.getDeserializer().getClass()).isEqualTo(LambdaProxyRequestStreamDeserializer.class);
        assertThat(lambdaFunction.getSerializer().getClass()).isEqualTo(LambdaProxyResponseStreamSerializer.class);
    }

    @Test
    public void createLambdaFunction_methodHandler() throws Exception {
        LambdaFunction lambdaFunction = scanner.createLambdaFunction("/testPath2",
                "com.zaoo.lambda.test.TestFunction2::method1", null, null);
        Class<?> handlerClass = TestFunction2.class;
        assertThat(lambdaFunction.getPath()).isEqualTo("/testPath2");
        assertThat(lambdaFunction.getHandlerClass()).isEqualTo(handlerClass);
        assertThat(lambdaFunction.getHandlerMethod()).isEqualTo(ReflectionUtils.getMethods(handlerClass,
                method -> "method1".equals(method.getName())).stream()
                .findFirst()
                .get());
        assertThat(lambdaFunction.getDeserializer().getClass()).isEqualTo(LambdaProxyRequestDeserializer.class);
        assertThat(lambdaFunction.getSerializer().getClass()).isEqualTo(LambdaProxyResponseSerializer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLambdaFunction_notImplementsRequestHandler() throws Exception {
        scanner.createLambdaFunction("/testPath2",
                "com.zaoo.lambda.test.TestFunction2", null, null);
    }

}