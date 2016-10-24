package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLambdaRestServiceTest {
    private AbstractLambdaRestService abstractLambdaRestService;

    @Before
    public void setUp() throws Exception {
        abstractLambdaRestService = new TestLambdaRestService();
    }

    @Test
    public void createMethodInvokers() throws Exception {
        List<MethodInvoker> methodInvokers = abstractLambdaRestService.createMethodInvokers(TestRestFunction1.class);
        assertThat(methodInvokers.size()).isEqualTo(1);
    }

    @LambdaLocal("/test")
    private static class TestLambdaRestService extends AbstractLambdaRestService {
    }
}