package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.zaoo.lambda.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractRootRestService extends AbstractLambdaLocalRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractRootRestService.class);
    private final LambdaFunctionAnnotationScanner lambdaFunctionAnnotationScanner = new LambdaFunctionAnnotationScanner();
    private final List<LambdaFunction> lambdaFunctions;
    private final Map<String, Object> lambdaFunctionHandlers = new HashMap<>();

    abstract protected List<String> getPackages();

    public AbstractRootRestService() {
        lambdaFunctions = getPackages().stream()
                .flatMap(packageName -> lambdaFunctionAnnotationScanner.listLambdaFunctions(packageName).stream())
                .collect(Collectors.toList());
    }

    @Override
    public LambdaProxyResponse handleRequest(LambdaProxyRequest input, Context context) {
        LambdaFunction lambdaFunction = getLambdaFunction(input);
        if (lambdaFunction == null) {
            throw new IllegalArgumentException(String.format("Unhandled lambda request:path=%s", input.getPath()));
        }

        try {
            log.debug("invokeLambdaFunction:lambdaLocalPath={}", lambdaFunction.getPath());
            return invokeLambdaFunction(input, context, lambdaFunction);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to find handler class:" + lambdaFunction.getHandlerClass(), e);
        }
    }

    private LambdaFunction getLambdaFunction(LambdaProxyRequest req) {
        String path = req.getPath();
        LambdaFunction ret = null;
        for (LambdaFunction lambdaFunction : lambdaFunctions) {
            if (path.startsWith(lambdaFunction.getPath())) {
                if (ret == null) {
                    ret = lambdaFunction;
                } else {
                    // There might be more than one lambdaFunction.path() matched. Pick the longest matched.
                    ret = lambdaFunction.getPath().length() > ret.getPath().length() ? lambdaFunction : ret;
                }
            }
        }
        return ret;
    }

    private LambdaProxyResponse invokeLambdaFunction(LambdaProxyRequest input,
                                                     Context context,
                                                     LambdaFunction lambdaFunction) throws InstantiationException, IllegalAccessException {
        Object handler = getLambdaFunctionObj(lambdaFunction);
        if (!(handler instanceof AbstractLambdaLocalRequestHandler)) {
            throw new IllegalArgumentException("Handler must be a subclass of AbstractLambdaLocalRequestHandler:class=" + handler.getClass().getName());
        }
        AbstractLambdaLocalRequestHandler requestHandler = (AbstractLambdaLocalRequestHandler) handler;
        return requestHandler.handleRequest(input, context);
    }

    private Object getLambdaFunctionObj(LambdaFunction lambdaFunction) throws InstantiationException, IllegalAccessException {
        Object handler = lambdaFunctionHandlers.get(lambdaFunction.getHandlerClass().getName());
        if (handler != null) {
            return handler;
        }
        handler = lambdaFunction.getHandlerClass().newInstance();
        lambdaFunctionHandlers.put(lambdaFunction.getHandlerClass().getName(), handler);
        return handler;
    }
}
