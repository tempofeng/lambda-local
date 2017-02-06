package com.zaoo.lambda.rest;

import com.google.common.base.Strings;
import com.zaoo.lambda.LambdaProxyRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.DefaultCookieSpec;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

class MethodInvocationContext {
    private static final Logger log = LoggerFactory.getLogger(MethodInvocationContext.class);
    private static final CookieSpec cookieSpec = new DefaultCookieSpec();
    private final String lambdaLocalPath;
    private final String methodPath;
    private final LambdaProxyRequest request;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private Map<String, String> postParams;
    private Map<String, String> pathVariables;
    private Map<String, Cookie> cookies;

    public MethodInvocationContext(String lambdaLocalPath, String methodPath, LambdaProxyRequest request) {
        this.lambdaLocalPath = lambdaLocalPath;
        this.methodPath = methodPath;
        this.request = request;
    }

    public synchronized Map<String, String> getPostParams() {
        if (postParams == null) {
            postParams = parsePostParameters(request);
        }
        return postParams;
    }

    public synchronized Map<String, String> getPathVariables() {
        if (pathVariables == null) {
            pathVariables = pathMatcher.extractUriTemplateVariables(methodPath,
                    getRestMethodPath(request));
        }
        return pathVariables;
    }

    public synchronized Map<String, Cookie> getCookies() {
        if (cookies == null) {
            cookies = parseCookies(request);
        }
        return cookies;
    }

    private String getRestMethodPath(LambdaProxyRequest request) {
        return request.getPath().equals(lambdaLocalPath) ?
                "/" :
                request.getPath().substring(lambdaLocalPath.length());
    }

    Map<String, String> parsePostParameters(LambdaProxyRequest request) {
        String contentType = request.getHeaders().get("Content-Type");
        if(contentType == null || !contentType.contains("application/x-www-form-urlencoded")) {
            return Collections.emptyMap();
        }

        String body = request.getBody();
        if (Strings.isNullOrEmpty(body)) {
            return Collections.emptyMap();
        }

        return URLEncodedUtils.parse(body, Charset.forName("UTF-8")).stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    }

    private Map<String, Cookie> parseCookies(LambdaProxyRequest request) {
        try {
            BasicHeader header = new BasicHeader("Cookie", request.getHeaders().get("Cookie"));
            // we need the value only.
            CookieOrigin origin = new CookieOrigin("dummy", 80, request.getPath(), false);
            return cookieSpec.parse(header, origin).stream()
                    .collect(Collectors.toMap(Cookie::getName, cookie -> cookie));
        } catch (MalformedCookieException e) {
            log.warn(e.getLocalizedMessage(), e);
            return Collections.emptyMap();
        }
    }

    public Map<String, String> getQueryStringParameters() {
        return request.getQueryStringParameters();
    }

    public String getBody() {
        return request.getBody();
    }

    public Map<String, String> getHeaders() {
        return request.getHeaders();
    }
}
