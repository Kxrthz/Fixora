package com.fixora.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Fixora Backend Application started successfully!");
    }
}
