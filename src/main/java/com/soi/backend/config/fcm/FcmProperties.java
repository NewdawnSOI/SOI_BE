package com.soi.backend.config.fcm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.fcm")
public class FcmProperties {

    private boolean enabled;
    private String credentialsPath;
    private String projectId;
}
