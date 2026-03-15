package com.soi.backend.config.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(FcmProperties.class)
@ConditionalOnProperty(name = "app.fcm.enabled", havingValue = "true")
public class FcmConfig {

    @Bean
    public FirebaseApp firebaseApp(FcmProperties fcmProperties) throws IOException {
        if (fcmProperties.getCredentialsPath() == null || fcmProperties.getCredentialsPath().isBlank()) {
            throw new IllegalStateException("app.fcm.credentials-path 설정이 필요합니다.");
        }

        Path credentialsPath = Path.of(fcmProperties.getCredentialsPath());
        try (InputStream inputStream = Files.newInputStream(credentialsPath)) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream));

            if (fcmProperties.getProjectId() != null && !fcmProperties.getProjectId().isBlank()) {
                optionsBuilder.setProjectId(fcmProperties.getProjectId());
            }

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(optionsBuilder.build());
            }

            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
