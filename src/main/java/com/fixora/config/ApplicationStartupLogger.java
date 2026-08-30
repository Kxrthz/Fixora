package com.fixora.config;

import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${spring.application.name:fixora-backend}")
    private String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("==================================================================");
        log.info(" APPLICATION RUNNING: {}", appName.toUpperCase());
        log.info(" ACTIVE PROFILE:     {}", activeProfile);
        log.info(" SERVER PORT:        {}", port);
        log.info(" HEALTH ENDPOINT:    http://0.0.0.0:{}/actuator/health", port);
        log.info(" RENDER STATUS:      CONTAINER BINDING READY AND LIVE");
        log.info("==================================================================");
    }
}
