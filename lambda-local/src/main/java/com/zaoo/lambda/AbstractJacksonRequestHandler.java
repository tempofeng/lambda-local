package com.zaoo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractJacksonRequestHandler<I, O> implements RequestStreamHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<I> inputClass;

    public AbstractJacksonRequestHandler(Class<I> inputClass) {
        this.inputClass = inputClass;
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        I inputObj = objectMapper.readValue(input, inputClass);
        O outputObj = handleRequest(inputObj, context);
        objectMapper.writeValue(output, outputObj);
    }

    /**
     * Handles a Lambda Function request
     *
     * @param input   The Lambda Function input
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     */
    public abstract O handleRequest(I input, Context context);
}
