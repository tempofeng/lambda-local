package com.zaoo.lambda;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LambdaProxyRequest {
    private String resource;
    private String path;
    private String httpMethod;
    /**
     * If there is more than one header with the same name, we will pick the last one. (like API Gateway)
     */
    private Map<String, String> headers = Collections.emptyMap();
    /**
     * If there is more than one queryString parameter with the same name, we will pick the last one. (like API Gateway)
     */
    private Map<String, String> queryStringParameters = Collections.emptyMap();
    private Map<String, String> pathParameters = Collections.emptyMap();
    private Map<String, String> stageVariables = Collections.emptyMap();
    private RequestContext requestContext;
    private String body = "";

    public LambdaProxyRequest() {
    }


    LambdaProxyRequest(String resource,
                       String path,
                       String httpMethod,
                       Map<String, String> headers,
                       Map<String, String> queryStringParameters,
                       RequestContext requestContext,
                       String body) {
        this.resource = resource;
        this.path = path;
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.queryStringParameters = queryStringParameters;
        this.requestContext = requestContext;
        this.body = body;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getQueryStringParameters() {
        return queryStringParameters;
    }

    public void setQueryStringParameters(Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public void setPathParameters(Map<String, String> pathParameters) {
        this.pathParameters = pathParameters;
    }

    public Map<String, String> getStageVariables() {
        return stageVariables;
    }

    public void setStageVariables(Map<String, String> stageVariables) {
        this.stageVariables = stageVariables;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "LambdaProxyRequest{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryStringParameters=" + queryStringParameters +
                ", body='" + body + '\'' +
                '}';
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RequestContext {
        private String accountId;
        private String resourceId;
        private String stage;
        private String requestId;
        private Identity identity;
        private String resourcePath;
        private String httpMethod;
        private String apiId;

        public RequestContext() {
        }

        public RequestContext(Identity identity, String resourcePath, String httpMethod) {
            this.accountId = "EXAMPLE_ACCOUNT_ID";
            this.resourceId = "EXAMPLE_RESOURCE_ID";
            this.stage = "EXAMPLE_STAGE";
            this.requestId = "EXAMPLE_REQUEST_ID";
            this.identity = identity;
            this.resourcePath = resourcePath;
            this.httpMethod = httpMethod;
            this.apiId = "EXAMPLE_API_ID";
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getResourceId() {
            return resourceId;
        }

        public void setResourceId(String resourceId) {
            this.resourceId = resourceId;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Identity getIdentity() {
            return identity;
        }

        public void setIdentity(Identity identity) {
            this.identity = identity;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public void setResourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getApiId() {
            return apiId;
        }

        public void setApiId(String apiId) {
            this.apiId = apiId;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Identity {
        private String cognitoIdentityPoolId;
        private String accountId;
        private String cognitoIdentityId;
        private String caller;
        private String apiKey;
        private String sourceIp;
        private String cognitoAuthenticationType;
        private String cognitoAuthenticationProvider;
        private String userArn;
        private String userAgent;
        private String user;

        public Identity() {
        }

        public Identity(String sourceIp, String userAgent) {
            this.cognitoIdentityPoolId = "EXAMPLE_COGNITO_IDENTITY_POOL_ID";
            this.accountId = "EXAMPLE_ACCOUNT_ID";
            this.cognitoIdentityId = "EXAMPLE_COGNITO_IDENTITY_ID";
            this.caller = "EXAMPLE_CALLER";
            this.apiKey = "EXAMPLE_API_KEY";
            this.sourceIp = sourceIp;
            this.cognitoAuthenticationType = "EXAMPLE_COGNITO_AUTHENTICATION_TYPE";
            this.cognitoAuthenticationProvider = "EXAMPLE_COGNITO_AUTHENTICATION_PROVIDER";
            this.userArn = "EXAMPLE_USER_ARN";
            this.userAgent = userAgent;
            this.user = "EXAMPLE_USER";
        }

        public String getCognitoIdentityPoolId() {
            return cognitoIdentityPoolId;
        }

        public void setCognitoIdentityPoolId(String cognitoIdentityPoolId) {
            this.cognitoIdentityPoolId = cognitoIdentityPoolId;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getCognitoIdentityId() {
            return cognitoIdentityId;
        }

        public void setCognitoIdentityId(String cognitoIdentityId) {
            this.cognitoIdentityId = cognitoIdentityId;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getSourceIp() {
            return sourceIp;
        }

        public void setSourceIp(String sourceIp) {
            this.sourceIp = sourceIp;
        }

        public String getCognitoAuthenticationType() {
            return cognitoAuthenticationType;
        }

        public void setCognitoAuthenticationType(String cognitoAuthenticationType) {
            this.cognitoAuthenticationType = cognitoAuthenticationType;
        }

        public String getCognitoAuthenticationProvider() {
            return cognitoAuthenticationProvider;
        }

        public void setCognitoAuthenticationProvider(String cognitoAuthenticationProvider) {
            this.cognitoAuthenticationProvider = cognitoAuthenticationProvider;
        }

        public String getUserArn() {
            return userArn;
        }

        public void setUserArn(String userArn) {
            this.userArn = userArn;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
