package com.knockbook.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "imgbb.api")
public class ImgbbApiProperties {
    private String baseUrl;
    private String uploadPath;
    private String key;
    private long timeoutSeconds = 30;
    private int connectTimeoutMillis = 5000;
    private Integer maxInMemorySizeBytes;
}
