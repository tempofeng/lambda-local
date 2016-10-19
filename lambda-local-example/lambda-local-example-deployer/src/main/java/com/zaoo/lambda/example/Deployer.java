package com.zaoo.lambda.example;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.zaoo.puu.BuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

public class Deployer {
    private static final Logger log = LoggerFactory.getLogger(Deployer.class);

    private final TransferManager transferManager;
    private final AWSLambdaClient awsLambdaClient;
    private final File zipPath;
    private final String s3Bucket;
    private final String s3Key;
    private final String functionPrefix;

    public static void main(final String[] args) throws AmazonClientException, InterruptedException {
        Deployer deployer = new Deployer(BuildConfig.deployerAwsAccessKeyId,
                BuildConfig.deployerAwsSecretAccessKey,
                BuildConfig.functionPrefix,
                BuildConfig.projectName,
                BuildConfig.s3Region,
                BuildConfig.s3Bucket,
                BuildConfig.s3Dir);
        deployer.deploy();
        System.exit(0);
    }

    public Deployer(String deployerAwsAccessKeyId,
                    String deployerAwsSecretAccessKey,
                    String functionPrefix,
                    String projectName,
                    String s3Region,
                    String s3Bucket,
                    String s3Dir) {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(deployerAwsAccessKeyId,
                deployerAwsSecretAccessKey);
        transferManager = createTransferManager(credentials);
        awsLambdaClient = createAwsLambdaClient(credentials, s3Region);
        this.functionPrefix = functionPrefix;
        this.s3Bucket = s3Bucket;
        s3Key = generateS3Key(s3Dir, projectName);
        zipPath = getZipPath(projectName);
    }

    private TransferManager createTransferManager(BasicAWSCredentials credentials) {
        return new TransferManager(credentials);
    }

    private AWSLambdaClient createAwsLambdaClient(BasicAWSCredentials credentials, String s3Region) {
        final AWSLambdaClient awsLambdaClient = new AWSLambdaClient(credentials);
        awsLambdaClient.setRegion(Region.getRegion(Regions.fromName(s3Region)));
        return awsLambdaClient;
    }

    private String generateS3Key(String s3Dir, String projectName) {
        return String.format("%s%s-%s.zip", s3Dir.endsWith("/") ? s3Dir : s3Dir + "/", projectName, UUID.randomUUID());
    }

    private File getZipPath(String projectName) {
        return new File(String.format("../build/distributions/%s.zip", projectName));
    }

    public void deploy() throws InterruptedException {
        uploadZipToS3();
        updateFunctions();
    }

    private void uploadZipToS3() throws AmazonClientException, InterruptedException {
        log.info("uploadZipToS3:{}->{}", zipPath, s3Key);

        Upload upload = transferManager.upload(s3Bucket,
                s3Key,
                zipPath);
        upload.waitForCompletion();
    }

    private void updateFunctions() {
        awsLambdaClient.listFunctions().getFunctions()
                .stream()
                .filter(f -> f.getFunctionName().startsWith(functionPrefix))
                .forEach(this::updateFunctionCode);
    }

    private void updateFunctionCode(FunctionConfiguration functionConfiguration) {
        log.info("updateFunctionCode:name={}", functionConfiguration.getFunctionName());
        UpdateFunctionCodeRequest request = new UpdateFunctionCodeRequest()
                .withFunctionName(functionConfiguration.getFunctionName())
                .withS3Bucket(s3Bucket)
                .withS3Key(s3Key)
                .withPublish(true);
        awsLambdaClient.updateFunctionCode(request);
    }
}
