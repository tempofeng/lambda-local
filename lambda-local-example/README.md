    1. You can also using @LambdaLocal directly without AbstractLambdaRestService. Ex:
        ```Java
        @LambdaLocal(value = "/lleHelloStream", serializer = HelloStream.Serializer.class, deserializer = HelloStream.Deserializer.class)
        public class HelloStream implements RequestStreamHandler {
            @Override
            public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
                String content = IOUtils.toString(input, "UTF-8");
                IOUtils.write(String.format("Hello %s!", content), output, "UTF-8");
            }
        
            public static class Deserializer implements LambdaStreamRequestDeserializer {
                @Override
                public byte[] serialize(HttpServletRequest req) throws IOException {
                    return IOUtils.toByteArray(req.getInputStream());
                }
            }
        
            public static class Serializer implements LambdaStreamResponseSerializer {
                @Override
                public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
                    IOUtils.write(output, resp.getOutputStream());
                }
            }
        }
        ```
    '@LambdaLocal.serializer' and '@LambdaLocal.deserializer' define how Lambda-Local serialize/deserialize the HttpServletRequest/HttpServletResponse in development mode.
    Based on the serialization/deserialization you defined, you also have to define how you map the request/response in AWS API Gateway.
