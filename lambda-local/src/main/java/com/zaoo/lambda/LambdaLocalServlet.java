package com.zaoo.lambda;

import com.amazonaws.services.lambda.runtime.RequestHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LambdaLocalServlet extends HttpServlet {
    private final LambdaFunctionAnnotationScanner lambdaFunctionAnnotationScanner = new LambdaFunctionAnnotationScanner();
    private Map<String, LambdaFunction> lambdaFunctions;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String packages = config.getInitParameter("packages");
        if (packages == null) {
            throw new IllegalArgumentException("Unable to find 'init-param:packages' in servlet config");
        }

        initLambdaFunctions(Arrays.asList(packages.split(",")));
    }

    private void initLambdaFunctions(List<String> packageNames) {
        lambdaFunctions = packageNames.stream()
                .flatMap(packageName -> lambdaFunctionAnnotationScanner.listLambdaFunctions(packageName).stream())
                .collect(Collectors.toMap(LambdaFunction::getPath, lambdaFunction -> lambdaFunction));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = getRequestPath(req);
        LambdaFunction lambdaFunction = lambdaFunctions.get(path);
        if (lambdaFunction == null) {
            super.service(req, resp);
            return;
        }

        try {
            invokeLambdaFunction(req, resp, lambdaFunction);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to find handler class:" + lambdaFunction.getHandlerClass(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to find handler method:" + lambdaFunction.getHandlerMethod(), e);
        }
    }

    void invokeLambdaFunction(HttpServletRequest req,
                              HttpServletResponse resp,
                              LambdaFunction lambdaFunction) throws InstantiationException, IllegalAccessException, IOException, InvocationTargetException {
        Object handler = getLambdaFunctionObj(lambdaFunction);
        LambdaLocalContext context = new LambdaLocalContext();

        Object input = lambdaFunction.getDeserializer().serialize(req);
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
