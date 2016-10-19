package com.zaoo.lambda;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface LambdaResponseSerializer<O> {
    void deserialize(O output, HttpServletResponse resp) throws IOException;
}
