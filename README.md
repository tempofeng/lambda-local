# Lambda-Local

Lambda-Local 可以讓 Java 開發者：
* 使用同一套程式碼，可以方便的在自己的電腦開發 AWS Lambda function，不用每次都 Deploy 到 AWS 測試
* 可以透過如 Spring MVC 的方式開發 Rest Service，如：
```Java
@LambdaLocal("/lleHelloRestService")
public class HelloRestService extends AbstractLambdaRestService {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/{firstName}/{lastName}")
    public String hello(@RestPath("firstName") String firstName,
                               @RestPath("lastName") String lastName) {
        return String.format("Hello %s, %s!", firstName, lastName);
    }
}
```

# Download
[![Release](https://jitpack.io/v/com.zaoo.lambda-local/Repo.svg?style=flat-square)](https://jitpack.io/#com.example/Repo)

* Using Gradle:
```Gradle
repositories {
    maven { url "https://jitpack.io" }
}

compile 'com.zaoo.lambda-local:lambda-local:{version}'
```

* Using other build system:
https://jitpack.io/#com.zaoo.lambda-local/lambda-local

# Requirements
Lambda-Local requires at minimum Java 8. 

# Usage

## A Simple Hello Rest Service
1. Install Lambda-Local in your build file. Ex: Using Gradle:
    ```Gradle
    repositories {
        maven { url "https://jitpack.io" }
    }
    
    compile 'com.zaoo.lambda-local:lambda-local:{version}'
    ```
    
1. Add a `WEB-INF/web.xml`.
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
             version="3.1">
        <servlet>
            <servlet-name>dispatcher</servlet-name>
            <servlet-class>com.zaoo.lambda.LambdaLocalServlet</servlet-class>
            <load-on-startup>1</load-on-startup>
            <init-param>
                <param-name>packages</param-name>
                <param-value>com.zaoo.lambda.example</param-value>
            </init-param>
        </servlet>
        <servlet-mapping>
            <servlet-name>dispatcher</servlet-name>
            <url-pattern>/</url-pattern>
        </servlet-mapping>
    </web-app>
    ```    
    Provide your package name as the `init-param`, and Lambda-Local will scan that package and its sub-package for all classes annotated with '@LambdaLocal'.

1. Create your first [AWS Lambda Function Handler](http://docs.aws.amazon.com/lambda/latest/dg/java-programming-model-handler-types.html).
    1. If you're building JSON REST services, you can extend `AbstractLambdaRestService` and use annotations to define your REST services by method (Like Spring MVC). Ex:
        ```Java
        @LambdaLocal("/lleHelloRestService")
        public class HelloRestService extends AbstractLambdaRestService {
            @RestMethod(httpMethod = HttpMethod.GET, path = "/{firstName}/{lastName}")
            public String hello(@RestPath("firstName") String firstName,
                                @RestPath("lastName") String lastName) {
                return String.format("Hello %s, %s!", firstName, lastName);
            }
        }
        ```
        Please note that:
        * `@LambdaLocal.value` must be the same as your Lambda Function name.
        * Annotations:
         * `@RestPath`: The parameter is bound to a URI template variable.
        * Lambda-Local using Jackson to convert Object <-> JSON.
         
    1. If you have setup AWS Lambda using proxy request, you can extend `AbstractLambdaLocalRequestHandler` to process the pre-defined proxy request and response.
        ```Java
        @LambdaLocal("/lleHelloLambdaProxy")
        public class HelloLambdaProxy extends AbstractLambdaLocalRequestHandler {
            @Override
            public LambdaProxyResponse handleRequest(LambdaProxyRequest input, Context context) {
                String firstName = input.getQueryStringParameters().get("firstName");
                String lastName = input.getQueryStringParameters().get("lastName");
                return new LambdaProxyResponse(String.format("Hello! %s,%s", firstName, lastName));
            }
        }
        ```
    1. You can also use Lambda-Local directly without `AbstractLambdaRestService` or `AbstractLambdaLocalRequestHandler` (But with more configurations to do).
        Please take a look at [our sample webapp](https://github.com/tempofeng/lambda-local/tree/master/lambda-local-example).
        
## Testing & Running the web app
1. Testing on your local machine. Ex: Using Gradle and [Gretty plugin](https://github.com/akhikhl/gretty).
    1. In build.gradle, put:
        ```
        apply from: 'https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty.plugin'
        
        gretty {
            servletContainer = 'jetty9'
            contextPath = ''
        }
        ```
    1. Run the web app on your local machine.
        ```
        > ./gradlew appRun
        ```
    1. Open http://localhost:8080/lleHelloRestService or http://localhost:8080/lleHelloLambdaProxy in your browser. (The path depends on your '@LambdaLocal.value')
1. Running on AWS Lambda and API Gateway
    1. Create a new AWS Lambda Function with default AWS API Gateway settings.
    1. Add {proxy+} to all the subpath of the Lambda Function in API Gateway.
 
## Working example
Please take a look at [our sample webapp](https://github.com/tempofeng/lambda-local/tree/master/lambda-local-example).

