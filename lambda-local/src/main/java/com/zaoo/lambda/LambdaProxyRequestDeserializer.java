package com.zaoo.lambda;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LambdaProxyRequestDeserializer implements LambdaRequestDeserializer<LambdaProxyRequest> {
    private static final String UTF_8 = "UTF-8";
    private static final String USER_AGENT = "User-Agent";

    @Override
    public LambdaProxyRequest serialize(HttpServletRequest req) throws IOException {
        String requestPath = req.getRequestURI().substring(req.getContextPath().length());

        LambdaProxyRequest.Identity identity = new LambdaProxyRequest.Identity(req.getRemoteAddr(),
                req.getHeader(USER_AGENT));

        LambdaProxyRequest.RequestContext requestContext = new LambdaProxyRequest.RequestContext(identity,
                requestPath,
                req.getMethod());

        return new LambdaProxyRequest(requestPath,
                requestPath,
                req.getMethod(),
                toHeaders(req),
                toParameters(req),
                requestContext,
                IOUtils.toString(req.getReader()));
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
