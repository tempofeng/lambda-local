package com.zaoo.lambda;

import com.zaoo.lambda.test.TestFunction1;
import com.zaoo.lambda.test.TestFunction2;
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
        LambdaFunction lambdaFunction2 = new LambdaFunction("/testPath2",
                handlerClass,
                ReflectionUtils.getMethods(handlerClass, method -> method.getName().equals("method1")).stream()
                        .findFirst()
                        .get(),
                deserializer,
                serializer);
        handlerClass = Class.forName("com.zaoo.lambda.test.TestFunction2");
        LambdaFunction lambdaFunction3 = new LambdaFunction("/testPath3",
                handlerClass,
                ReflectionUtils.getMethods(handlerClass, method -> method.getName().equals("method2")).stream()
                        .findFirst()
                        .get(),
                deserializer,
                serializer);

        List<LambdaFunction> lambdaFunctions = scanner.listLambdaFunctions(
                "com.zaoo.lambda.test");
        assertThat(lambdaFunctions.size()).isEqualTo(3);
        assertThat(lambdaFunctions).contains(lambdaFunction1,
                lambdaFunction2,
                lambdaFunction3);
    }


    @Test
    public void createLambdaFunction() throws Exception {
        LambdaFunction lambdaFunction = scanner.createLambdaFunction("/testPath1",
                "com.zaoo.lambda.test.TestFunction1", null, null);
        assertThat(lambdaFunction).isEqualTo(new LambdaFunction("/testPath1",
                TestFunction1.class,
                null,
                new LambdaProxyRequestDeserializer(),
                new LambdaProxyResponseSerializer()));
    }

    @Test
    public void createLambdaFunction_methodHandler() throws Exception {
        LambdaFunction lambdaFunction = scanner.createLambdaFunction("/testPath2",
                "com.zaoo.lambda.test.TestFunction2::method1", null, null);
        Class<?> handlerClass = TestFunction2.class;
        assertThat(lambdaFunction).isEqualTo(new LambdaFunction("/testPath2",
                handlerClass,
                ReflectionUtils.getMethods(handlerClass, method -> method.getName().equals("method1")).stream()
                        .findFirst()
                        .get(),
                new LambdaProxyRequestDeserializer(),
                new LambdaProxyResponseSerializer()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createLambdaFunction_notImplementsRequestHandler() throws Exception {
        scanner.createLambdaFunction("/testPath2",
                "com.zaoo.lambda.test.TestFunction2", null, null);
    }

}