package com.thecodestein.configuration;

import io.deepstream.DeepstreamClient;
import io.deepstream.Event;
import io.deepstream.ListenListener;
import io.deepstream.RecordEventsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Slf4j
@Configuration
public class DeepstreamConfiguration {

//    @Bean
    public DeepstreamClient deepstreamClient() throws URISyntaxException {

        DeepstreamClient ds = new DeepstreamClient("localhost:6021");
        ds.login();

        ds.record.getRecord("form").addRecordEventsListener(new RecordEventsListener() {

            @Override
            public void onError(String s, Event event, String s1) {

            }

            @Override
            public void onRecordHasProviderChanged(String s, boolean b) {
                log.info("s === {}", s);
                log.info("b === {}", b);

            }

            @Override
            public void onRecordDeleted(String s) {
                log.info("s === {}", s);

            }

            @Override
            public void onRecordDiscarded(String s) {
                log.info("s === {}", s);

            }
        });
        return ds;
    }
}
