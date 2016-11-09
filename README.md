# Lambda-Local

Lambda-Local 可以讓 Java 開發者：

1. 方便的在自己的電腦開發 AWS Lambda function，不用每次都 Deploy 到 AWS 測試
1. 可以透過如 Spring MVC 的方式開發 Rest Service，如：
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

```
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

Please take a look at [our sample app](../tree/master/lambda-local-example).
