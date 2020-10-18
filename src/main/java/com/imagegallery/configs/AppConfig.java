package com.imagegallery.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${api.key}")
    public String apiKey;

    @Value("${api.url.auth}")
    public String authUrl;

    @Value("${api.url.image-url}")
    public String imageUrl;

    @Value("${gallery.schedule.time}")
    public Integer scheduleSecTime;

}
