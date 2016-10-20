package com.zaoo.lambda.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.zaoo.lambda.AbstractLambdaLocalRequestHandler;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LambdaLocal("/helloLambdaProxy")
@SuppressWarnings("unused")
public class HelloLambdaProxy extends AbstractLambdaLocalRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HelloLambdaProxy.class);

    @Override
    public LambdaProxyResponse handleRequest(LambdaProxyRequest input, Context context) {
        String firstName = input.getQueryStringParameters().get("firstName");
        String lastName = input.getQueryStringParameters().get("lastName");
        log.info("firstName={},lastName={}", firstName, lastName);
        return new LambdaProxyResponse(String.format("Hello! %s,%s", firstName, lastName));
    }
}
