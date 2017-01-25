package org.generationinitiative.uploader;

import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringInputStream;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.generationinitiative.uploader.dto.RequestDTO;
import org.generationinitiative.uploader.dto.ResultDTO;

import java.io.*;
import java.nio.charset.Charset;

import static com.amazonaws.regions.Regions.EU_CENTRAL_1;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class Hello implements RequestStreamHandler {

    // constants
    private static final String DEFAULT_BODY = "{}";
    private static final Charset STREAMS_ENCODING = Charset.forName("UTF-8");

    private LambdaLogger logger;
    private ObjectMapper objectMapper;
    private String SECRET = "default";

    private final AmazonS3 s3;

    public Hello() {

        // use the JWT CLIENT SECRET from the System environment properties
        String systemSecret = System.getenv("JWT_CLIENT_SECRET");
        if (systemSecret != null) {
            SECRET = systemSecret;
        }

        // configure the s3 client
        s3 = new AmazonS3Client();
        s3.setRegion(Region.getRegion(EU_CENTRAL_1));
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaLogger logger = getLogger(context);

        String inputString = convertToString(input);
        logger.log("input:\n" + inputString);
        logger.log("output:\n" + output.toString());

        String body = extract(inputString, "body");

        String result = process(body, logger);

        result = result == null ? DEFAULT_BODY : result;

        output.write(result.getBytes(STREAMS_ENCODING));

        logger.log("output:\n" + output.toString());
    }

    private String convertToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(STREAMS_ENCODING.displayName());
    }

    private String extract(String inputString, String nodeKey) throws IOException {
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(inputString);
        if (jsonNode == null || !jsonNode.has(nodeKey)) {
            return DEFAULT_BODY;
        }
        return jsonNode.findValue(nodeKey).asText(DEFAULT_BODY);
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
    public String process(String requestBody, LambdaLogger logger) {
        logger.log("requestBody:\n" + requestBody);

        ResultDTO resultDTO = new ResultDTO();

        ObjectMapper objectMapper = getObjectMapper();

        RequestDTO request = objectMapper.readValue(requestBody, RequestDTO.class);

        if (!isTokenValid(request.getToken())) {
            resultDTO.setStatus(HTTP_UNAUTHORIZED);
            return objectMapper.writeValueAsString(resultDTO);
        }

        StringInputStream stringInputStream = new StringInputStream(request.getBody());

        ObjectMetadata metadata = new ObjectMetadata();
        byte[] resultByte = DigestUtils.md5(stringInputStream);
        String streamMD5 = new String(Base64.encodeBase64(resultByte));
        metadata.setContentMD5(streamMD5);

        s3.putObject(request.getBucket(), request.getKey(), stringInputStream, metadata);

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
