package com.csci318.groupx.registrationservice.service;

import com.csci318.groupx.registrationservice.messaging.AccountEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountEventPublisher {

    private static final Logger logger =
            LoggerFactory.getLogger(AccountEventPublisher.class);

    private final StreamBridge streamBridge;

    public AccountEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishAccountEvent(
            String eventType,
            Long accountId,
            String username,
            String email
    ) {
        AccountEvent event = new AccountEvent(
                eventType,
                accountId,
                username,
                email,
                Instant.now().toString()
        );

        boolean sent = streamBridge.send(
                "account-events-out-0",
                event
        );

        if (sent) {
            logger.info(
                    "Published account event {} for accountId={}",
                    eventType,
                    accountId
            );
            return;
        }

        logger.warn(
                "Kafka send failed for account event {} for accountId={}",
                eventType,
                accountId
        );
    }
}
