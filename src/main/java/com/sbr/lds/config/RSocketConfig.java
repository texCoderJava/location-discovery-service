package com.sbr.lds.config;

import io.netty.handler.codec.string.StringDecoder;
import io.rsocket.core.Resume;
import io.rsocket.resume.InMemoryResumableFramesStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@Slf4j
public class RSocketConfig {
    @Bean
    RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
        return strategies -> strategies.metadataExtractorRegistry(r -> r.metadataToExtract(
                MimeType.valueOf(MediaType.TEXT_PLAIN_VALUE),
                String.class,
                "thresholdRadius"
        )).decoder(new Jackson2JsonDecoder()).encoder(new Jackson2JsonEncoder());
    }
    
    @Bean
    public RSocketServerCustomizer customizer() {
        return c -> c.resume(resumeStrategy());
    }
    
    private Resume resumeStrategy() {
        return new Resume().sessionDuration(Duration.ofHours(1)).storeFactory(token -> {
            ByteBuffer buf = token.nioBuffer();
            byte[] arr = new byte[buf.remaining()];
            buf.get(arr);
            log.info("token ::{}", new String(arr, StandardCharsets.UTF_8));
            return new InMemoryResumableFramesStore("client", token, 100000);
        });
    }
}
