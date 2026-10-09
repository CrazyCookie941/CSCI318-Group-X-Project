package com.csci318.groupx.accountservice.messaging;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Sends account domain events via Spring Cloud Stream.
 * When the feature flag is false, no Kafka connection is attempted by this
 * publisher. Delivery is best-effort; production would need an outbox.
 */
@Component
public class AccountEventPublisher {

    private static final Logger logger =
            LoggerFactory.getLogger(AccountEventPublisher.class);

    private static final String BINDING_NAME = "accountEvents-out-0";

    private final StreamBridge streamBridge;
    private final boolean enabled;

    public AccountEventPublisher(
            StreamBridge streamBridge,
            @Value("${account.events.enabled:false}") boolean enabled
    ) {
        this.streamBridge = streamBridge;
        this.enabled = enabled;
    }

    public void accountCreated(Long accountId) {
        publish("ACCOUNT_CREATED", accountId);
    }

    public void accountUpdated(Long accountId) {
        publish("ACCOUNT_UPDATED", accountId);
    }

    public void accountDeleted(Long accountId) {
        publish("ACCOUNT_DELETED", accountId);
    }

    private void publish(String eventType, Long accountId) {
        if (!enabled) {
            logger.debug("Account event publishing disabled: {} id={}", eventType, accountId);
            return;
        }

        AccountEvent event = new AccountEvent(
                1,
                UUID.randomUUID().toString(),
                eventType,
                accountId,
                Instant.now().toString()
        );

        try {
            boolean accepted = streamBridge.send(BINDING_NAME, event);
            if (accepted) {
                logger.info("Account event accepted by StreamBridge: type={} accountId={}",
                        eventType, accountId);
            } else {
                logger.error("Account event was NOT accepted: type={} accountId={}",
                        eventType, accountId);
            }
        } catch (RuntimeException exception) {
            // Fail open for this local prototype so REST account operations
            // are not rejected only because the event broker is offline.
            logger.error("Account event publishing failed: type={} accountId={}",
                    eventType, accountId, exception);
        }
    }
}
