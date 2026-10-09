package com.csci318.groupx.accountservice.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AccountEventPublisherTests {

    @Test
    void disabledPublisherDoesNotSend() {
        StreamBridge bridge = mock(StreamBridge.class);
        AccountEventPublisher publisher = new AccountEventPublisher(bridge, false);
        publisher.accountCreated(42L);
        verifyNoInteractions(bridge);
    }

    @Test
    void publishesMinimalAccountEventWhenEnabled() {
        StreamBridge bridge = mock(StreamBridge.class);
        when(bridge.send(eq("accountEvents-out-0"), any())).thenReturn(true);
        AccountEventPublisher publisher = new AccountEventPublisher(bridge, true);

        publisher.accountCreated(42L);
        ArgumentCaptor<Object> captured = ArgumentCaptor.forClass(Object.class);
        verify(bridge).send(eq("accountEvents-out-0"), captured.capture());

        AccountEvent event = assertInstanceOf(AccountEvent.class, captured.getValue());
        assertEquals(1, event.schemaVersion());
        assertEquals("ACCOUNT_CREATED", event.eventType());
        assertEquals(Long.valueOf(42L), event.accountId());
        assertNotNull(event.eventId());
        assertNotNull(event.occurredAt());
        assertEquals(5, event.getClass().getRecordComponents().length);
    }

    @Test
    void sendingFailureDoesNotThrowIntoAccountWorkflow() {
        StreamBridge bridge = mock(StreamBridge.class);
        when(bridge.send(eq("accountEvents-out-0"), any()))
                .thenThrow(new IllegalStateException("broker unavailable"));
        AccountEventPublisher publisher = new AccountEventPublisher(bridge, true);
        assertDoesNotThrow(() -> publisher.accountDeleted(42L));
    }
}
