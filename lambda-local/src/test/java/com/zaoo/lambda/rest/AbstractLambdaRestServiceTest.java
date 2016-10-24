package com.zaoo.lambda.rest;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLambdaRestServiceTest {
    private AbstractLambdaRestService abstractLambdaRestService;

    @Before
    public void setUp() throws Exception {
        abstractLambdaRestService = new AbstractLambdaRestService() {
        };
    }

    @Test
    public void createMethodInvokers() throws Exception {
        Map<HttpMethod, MethodInvoker> methodInvokers = abstractLambdaRestService.createMethodInvokers(TestRestFunction1.class);
        assertThat(methodInvokers.size()).isEqualTo(1);
        MethodInvoker methodInvoker = methodInvokers.get(HttpMethod.POST);
        assertThat(methodInvoker).isNotNull();
    }

}