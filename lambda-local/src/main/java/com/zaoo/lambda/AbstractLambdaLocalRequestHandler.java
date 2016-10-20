package com.zaoo.lambda;

public abstract class AbstractLambdaLocalRequestHandler extends AbstractJacksonRequestHandler<LambdaProxyRequest, LambdaProxyResponse> {
    public AbstractLambdaLocalRequestHandler() {
        super(LambdaProxyRequest.class);
    }
}
