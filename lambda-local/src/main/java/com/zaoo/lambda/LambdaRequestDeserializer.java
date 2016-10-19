package com.zaoo.lambda;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface LambdaRequestDeserializer<I> {
    I serialize(HttpServletRequest req) throws IOException;
}
