package com.zaoo.lambda;

import com.amazonaws.services.lambda.runtime.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class LambdaLocalContext implements Context {
    private static final Logger log = LoggerFactory.getLogger(LambdaLocalContext.class);
    private final String awsRequestId = "EXAMPLE_REQUEST_ID";
    private final ClientContext clientContext = new LambdaLocalClientContext();
    private final String functionName = "EXAMPLE_FUNCTION_NAME";
    private final CognitoIdentity identity = new LambdaLocalCognitoIdentity();
    private final String logGroupName = "EXAMPLE_LOG_GROUP_NAME";
    private final String logStreamName = "EXAMPLE_STREAM_NAME";
    private final LambdaLogger logger = new LambdaLogger() {
        @Override
        public void log(String message) {
            log.info(message);
        }

        @Override
        public void log(byte[] message) {
            log.info(new String(message, StandardCharsets.UTF_8));
        }
    };
    private final int memoryLimitInMB = 128;
    private final int remainingTimeInMillis = 15000;

    @Override
    public String getAwsRequestId() {
        return awsRequestId;
    }

    @Override
    public ClientContext getClientContext() {
        return clientContext;
    }

    @Override
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public String getFunctionVersion() {
        return null;
    }

    @Override
    public String getInvokedFunctionArn() {
        return null;
    }

    @Override
    public CognitoIdentity getIdentity() {
        return identity;
    }

    @Override
    public String getLogGroupName() {
        return logGroupName;
    }

    @Override
    public String getLogStreamName() {
        return logStreamName;
    }

    @Override
    public LambdaLogger getLogger() {
        return logger;
    }

    @Override
    public int getMemoryLimitInMB() {
        return memoryLimitInMB;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return remainingTimeInMillis;
    }

    public static class LambdaLocalClientContext implements ClientContext {
        private final Client client = new LambdaLocalClient();
        private final Map<String, String> custom = Collections.emptyMap();
        private final Map<String, String> environment = Collections.emptyMap();

        @Override
        public Client getClient() {
            return client;
        }

        @Override
        public Map<String, String> getCustom() {
            return custom;
        }

        @Override
        public Map<String, String> getEnvironment() {
            return environment;
        }
    }

    public static class LambdaLocalClient implements Client {
        private final String installationId = "EXAMPLE_INSTALLATION_ID";
        private final String appTitle = "EXAMPLE_APP_TITLE";
        private final String appVersionName = "EXAMPLE_APP_VERSION_NAME";
        private final String appVersionCode = "EXAMPLE_APP_VERSION_CODE";
        private final String appPackageName = "EXAMPLE_APP_PACKAGE_NAME";

        @Override
        public String getInstallationId() {
            return installationId;
        }

        @Override
        public String getAppTitle() {
            return appTitle;
        }

        @Override
        public String getAppVersionName() {
            return appVersionName;
        }

        @Override
        public String getAppVersionCode() {
            return appVersionCode;
        }

        @Override
        public String getAppPackageName() {
            return appPackageName;
        }
    }

    public static class LambdaLocalCognitoIdentity implements CognitoIdentity {
        private final String identityId = "EXAMPLE_IDENTITY_ID";
        private final String identityPoolId = "EXAMPLE_IDENTITY_POOL_ID";

        @Override
        public String getIdentityId() {
            return identityId;
        }

        @Override
        public String getIdentityPoolId() {
            return identityPoolId;
        }
    }
}