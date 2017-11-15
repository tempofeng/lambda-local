package com.zaoo.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaLocalServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(LambdaLocalServlet.class);
    private final LambdaFunctionAnnotationScanner lambdaFunctionAnnotationScanner = new LambdaFunctionAnnotationScanner();
    private List<LambdaFunction> lambdaFunctions;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try {
            String objectMapperFactory = config.getInitParameter("objectMapperFactory");
            if (Strings.isNullOrEmpty(objectMapperFactory)) {
                Class<? extends ObjectMapperFactory> cls = Class.forName(objectMapperFactory).asSubclass(
                        ObjectMapperFactory.class);
                ObjectMappers.setObjectMapperFactory(cls.newInstance());
            }

            String packages = config.getInitParameter("packages");
            if (Strings.isNullOrEmpty(packages)) {
                throw new IllegalArgumentException("Unable to find 'init-param:packages' in servlet config");
            }
            initLambdaFunctions(Arrays.asList(packages.split(",")));
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ServletException(e.getLocalizedMessage(), e);
        }
    }

    private void initLambdaFunctions(List<String> packageNames) {
        lambdaFunctions = packageNames.stream()
                .flatMap(packageName -> lambdaFunctionAnnotationScanner.listLambdaFunctions(packageName).stream())
                .collect(Collectors.toList());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LambdaFunction lambdaFunction = getLambdaFunction(req);
        if (lambdaFunction == null) {
            super.service(req, resp);
            return;
        }

        try {
            log.debug("invokeLambdaFunction:lambdaLocalPath={}", lambdaFunction.getPath());
            invokeLambdaFunction(req, resp, lambdaFunction);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to find handler class:" + lambdaFunction.getHandlerClass(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to find handler method:" + lambdaFunction.getHandlerMethod(), e);
        }
    }

    private LambdaFunction getLambdaFunction(HttpServletRequest req) {
        String path = getRequestPath(req);
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

    @SuppressWarnings("unchecked")
    void invokeLambdaFunction(HttpServletRequest req,
                              HttpServletResponse resp,
                              LambdaFunction lambdaFunction) throws InstantiationException, IllegalAccessException, IOException, InvocationTargetException {
        Object handler = getLambdaFunctionObj(lambdaFunction);
        LambdaLocalContext context = new LambdaLocalContext();
        Object input = lambdaFunction.getDeserializer().serialize(req);

        if (handler instanceof RequestStreamHandler) {
            RequestStreamHandler streamHandler = (RequestStreamHandler) handler;
            byte[] buffer = (byte[]) input;
            try (ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                streamHandler.handleRequest(in, out, context);
                out.flush();
                lambdaFunction.getSerializer().deserialize(out.toByteArray(), resp);
            }
            return;
        }

        if (handler instanceof RequestHandler) {
            RequestHandler requestHandler = (RequestHandler) handler;
            Object output = requestHandler.handleRequest(input, context);
            lambdaFunction.getSerializer().deserialize(output, resp);
            return;
        }

        if (lambdaFunction.getHandlerMethod().getParameterCount() == 1) {
            Object output = lambdaFunction.getHandlerMethod().invoke(handler, input);
            lambdaFunction.getSerializer().deserialize(
                    lambdaFunction.getHandlerMethod().getReturnType().cast(output),
                    resp);
            return;
        }

        if (lambdaFunction.getHandlerMethod().getParameterCount() == 2) {
            Object output = lambdaFunction.getHandlerMethod().invoke(handler, input, context);
            lambdaFunction.getSerializer().deserialize(output, resp);
            return;
        }

        throw new IllegalStateException("Unable to find Lambda handler:" + lambdaFunction);
    }

    private Object getLambdaFunctionObj(LambdaFunction lambdaFunction) throws InstantiationException, IllegalAccessException {
        return lambdaFunction.getHandlerClass().newInstance();
    }

    private String getRequestPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }
}
