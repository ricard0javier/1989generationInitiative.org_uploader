package org.generationinitiative.uploader;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringInputStream;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.generationinitiative.uploader.dto.RequestDTO;
import org.generationinitiative.uploader.dto.ResultDTO;

import java.io.*;

public class Hello implements RequestStreamHandler {

    private LambdaLogger logger;
    private ObjectMapper objectMapper;
    private static final int STATUS_UNAUTHORISED = 401;
    private static final String STREAMS_ENCODING = "UTF-8";
    private String SECRET = "default";

    private final AmazonS3 s3 = new AmazonS3Client();

    public Hello() {
        String systemSecret = System.getenv("JWT_CLIENT_SECRET");
        if (systemSecret != null) {
            SECRET = systemSecret;
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaLogger logger = getLogger(context);

        String inputString = convertToString(input);
        logger.log("request:\n" + inputString);

        String body = extract(inputString, "body");

        process(body, logger);
    }

    private String convertToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(STREAMS_ENCODING);
    }

    private String extract(String inputString, String nodeKey) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(inputString);
        if (jsonNode == null || !jsonNode.has(nodeKey)) {
            return null;
        }
        return jsonNode.findValue(nodeKey).asText("{}");
    }

    private ObjectMapper getObjectMapper() {
        if (objectMapper != null) {
            return objectMapper;
        }
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return objectMapper;
    }

    private LambdaLogger getLogger(Context context) {
        if (logger != null) {
            return logger;
        }
        logger = context.getLogger();
        return logger;
    }

    @SneakyThrows
    public ResultDTO process(String requestBody, LambdaLogger logger) {
        logger.log("requestBody:\n" + requestBody);

        ResultDTO resultDTO = new ResultDTO();

        ObjectMapper objectMapper = getObjectMapper();

        RequestDTO request = objectMapper.readValue(requestBody, RequestDTO.class);

        if (!isTokenValid(request.getToken())) {
            resultDTO.setStatus(STATUS_UNAUTHORISED);
            return resultDTO;
        }

        StringInputStream stringInputStream = new StringInputStream(request.getBody());
        s3.putObject(request.getBucket(), request.getKey(), stringInputStream, null);

        return resultDTO;
    }

    private boolean isTokenValid(String token) throws UnsupportedEncodingException {
        try {
            JWT.require(Algorithm.HMAC256(SECRET))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return false;
        }
        return true;
    }
}
