package org.generationinitiative.uploader;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
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

import java.io.UnsupportedEncodingException;

public class Hello {

    private static final int STATUS_UNAUTHORISED = 401;
    private String SECRET = "default";

    private final AmazonS3 s3 = new AmazonS3Client();

    public Hello() {
        String systemSecret = System.getenv("JWT_CLIENT_SECRET");
        if (systemSecret != null) {
            SECRET = systemSecret;
        }
    }

    @SneakyThrows
    public String handleRequest(String requestString, Context context) {

        ResultDTO resultDTO = new ResultDTO();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        LambdaLogger logger = context.getLogger();
        logger.log("context:\n" + objectMapper.writeValueAsString(context));
        logger.log("request:\n" + requestString);

        JsonNode body = objectMapper.readTree(requestString).get("body");
        String bodyString = body.asText("{}");
        RequestDTO request = objectMapper.readValue(bodyString, RequestDTO.class);

        if (!isTokenValid(request.getToken())) {
            resultDTO.setStatus(STATUS_UNAUTHORISED);
            return objectMapper.writeValueAsString(resultDTO);
        }

        StringInputStream stringInputStream = new StringInputStream(request.getBody());
        s3.putObject(request.getBucket(), request.getKey(), stringInputStream, null);

        return objectMapper.writeValueAsString(resultDTO);
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
