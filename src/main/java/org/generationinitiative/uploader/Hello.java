package org.generationinitiative.uploader;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.StringInputStream;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.generationinitiative.uploader.dto.RequestDTO;
import org.generationinitiative.uploader.dto.ResultDTO;

import java.io.UnsupportedEncodingException;

@Slf4j
public class Hello implements RequestHandler<RequestDTO, ResultDTO> {

    private static final int STATUS_UNAUTHORISED = 401;
    private static final String SECRET = System.getenv("JWT_CLIENT_SECRET");

    private final AmazonS3 s3 = new AmazonS3Client();

    @SneakyThrows
    public ResultDTO handleRequest(RequestDTO request, Context context) {

        ResultDTO resultDTO = new ResultDTO();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        log.info("context:\n {}", objectMapper.writeValueAsString(context));
        log.info("request:\n {}", objectMapper.writeValueAsString(request));


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
