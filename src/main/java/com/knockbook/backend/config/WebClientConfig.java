package com.knockbook.backend.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties(ImgbbApiProperties.class)
public class WebClientConfig {

    @Bean(name = "imgbbWebClient")
    public WebClient imgbbWebClient(ImgbbApiProperties props) {
        final var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMillis())
                .responseTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));

        final var builder = WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(h -> h.setAccept(List.of(MediaType.APPLICATION_JSON)));

        if (props.getMaxInMemorySizeBytes() != null) {
            builder.codecs(c ->
                    c.defaultCodecs().maxInMemorySize(props.getMaxInMemorySizeBytes()));
        }

        return builder.build();
    }
}
