package org.generationinitiative.uploader;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.extern.slf4j.Slf4j;
import org.generationinitiative.uploader.dto.RequestDTO;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class HelloTest {

    public static final String TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3JpY2FyZDBqYXZpZXIuZXUuYXV0aDAuY29tLyIsInN1YiI6ImF1dGgwfDU4NzJiMTVjYmY4ZTYzMzI1MDY1OWEwZSIsImF1ZCI6IlIyZ1RBemJKeGh5NnZnVUQxMW51VEhGY3JteGR0VDJOIiwiZXhwIjoxNDg0ODE2MjIwLCJpYXQiOjE0ODQ3ODAyMjB9.RLuD2PC9yEDyiN5Wv-mXQPManoyFms3nFGwKxo2IvXo";
    public static final String BUCKET = "static.1989generationinitiative.org";
    public static final String KEY = "data/test.json";
    public static final String BODY = "Hello world with JAVA";
    private Hello target;

    @Before
    public void setUp() throws Exception {
        target = new Hello();
    }

    @Test
    public void test_handleRequest() throws Exception {

        System.setProperty("JWT_CLIENT_SECRET", "secret");
        RequestDTO request = new RequestDTO(TOKEN, BUCKET, KEY, BODY);
        Context context = getContext();
        target.handleRequest(request, context);

    }

    private Context getContext() {
        return new Context() {
            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return new LambdaLogger() {
                    @Override
                    public void log(String string) {
                        log.info(string);
                    }
                };
            }
        };
    }
}