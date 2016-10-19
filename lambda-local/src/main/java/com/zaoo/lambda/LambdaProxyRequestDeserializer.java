package com.zaoo.lambda;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LambdaProxyRequestDeserializer implements LambdaRequestDeserializer<LambdaProxyRequest> {
    @Override
    public LambdaProxyRequest serialize(HttpServletRequest req) throws IOException {
        String requestPath = req.getRequestURI().substring(req.getContextPath().length());

        LambdaProxyRequest.Identity identity = new LambdaProxyRequest.Identity();
        identity.setSourceIp(req.getRemoteAddr());
        identity.setUserAgent(req.getHeader("User-Agent"));

        LambdaProxyRequest.RequestContext requestContext = new LambdaProxyRequest.RequestContext();
        requestContext.setIdentity(identity);
        requestContext.setResourcePath(requestPath);
        requestContext.setHttpMethod(req.getMethod());

        LambdaProxyRequest input = new LambdaProxyRequest();
        input.setResource(requestPath);
        input.setPath(requestPath);
        input.setHttpMethod(req.getMethod());
        input.setHeaders(toHeaders(req));
        input.setQueryStringParameters(toParameters(req));
        input.setRequestContext(requestContext);
        input.setBody(IOUtils.toString(req.getInputStream(),
                req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8"));
        return input;
    }

    private Map<String, String> toHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        for (Enumeration<String> e = req.getHeaderNames(); e.hasMoreElements(); ) {
            String name = e.nextElement();
            List<String> values = Collections.list(req.getHeaders(name));
            headers.put(name, values.get(values.size() - 1));
        }
        return headers;
    }

    private Map<String, String> toParameters(HttpServletRequest req) {
        return req.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> {
                            String[] values = entry.getValue();
                            return values[values.length - 1];
                        }));
    }
}
