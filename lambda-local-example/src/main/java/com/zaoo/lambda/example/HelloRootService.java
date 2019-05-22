package com.zaoo.lambda.example;

import com.zaoo.lambda.rest.AbstractRootRestService;

import java.util.Collections;
import java.util.List;

public class HelloRootService extends AbstractRootRestService {
    @Override
    protected List<String> getPackages() {
        return Collections.singletonList("com.zaoo.lambda.example");
    }
}
