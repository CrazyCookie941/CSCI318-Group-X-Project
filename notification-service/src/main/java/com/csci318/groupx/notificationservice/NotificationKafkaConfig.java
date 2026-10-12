package com.csci318.groupx.notificationservice;

import com.csci318.groupx.notificationservice.messaging.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.function.Consumer;

@Configuration
@ConditionalOnProperty(
        prefix = "notification.account-events",
        name = "consumer-enabled",
        havingValue = "true"
)
public class NotificationKafkaConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationKafkaConfig.class);

    @Bean
    public Consumer<AccountEvent> accountEventConsumer() {
        return event -> logger.info(
                "Received account event {} id={} for accountId={}",
                event.eventType(),
                event.eventId(),
                event.accountId()
        );
    }
}
