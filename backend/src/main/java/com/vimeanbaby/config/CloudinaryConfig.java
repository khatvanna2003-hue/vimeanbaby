package com.vimeanbaby.config;

import com.cloudinary.Cloudinary;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class CloudinaryConfig {

    @Value("${app.cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${app.cloudinary.api-key:}")
    private String apiKey;

    @Value("${app.cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, Object> config = new HashMap<>();

        if (StringUtils.hasText(cloudinaryUrl) && cloudinaryUrl.startsWith("cloudinary://")) {
            URI uri = URI.create(cloudinaryUrl);
            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String[] parts = userInfo.split(":", 2);
                config.put("api_key", parts[0]);
                config.put("api_secret", parts[1]);
            }
            String host = uri.getHost();
            if (StringUtils.hasText(host)) {
                config.put("cloud_name", host);
            }
        } else {
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
        }

        config.put("secure", true);
        return new Cloudinary(config);
    }
}
