package com.csci318.groupx.notificationservice;

import com.csci318.groupx.notificationservice.messaging.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class NotificationKafkaConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationKafkaConfig.class);

    @Bean
    public Consumer<AccountEvent> accountEventConsumer() {
        return event -> logger.info(
                "Received account event {} for accountId={} username={} email={}",
                event.eventType(),
                event.accountId(),
                event.username(),
                event.email()
        );
    }
}
