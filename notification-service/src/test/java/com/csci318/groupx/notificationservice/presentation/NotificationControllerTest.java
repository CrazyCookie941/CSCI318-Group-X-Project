package com.csci318.groupx.notificationservice.presentation;

import com.csci318.groupx.notificationservice.domain.NotificationPreference;
import com.csci318.groupx.notificationservice.domain.NotificationRecord;
import com.csci318.groupx.notificationservice.repository.NotificationPreferenceRepository;
import com.csci318.groupx.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Test
    void newEventNotifiesOnlyEnabledUsersWhoseAreaMatches() {
        NotificationPreference sydney = preference(101L, true, "Sydney");
        NotificationPreference wollongong = preference(102L, true, "Wollongong");
        when(preferenceRepository.findByEnabledTrueAndAreaIsNotNull())
                .thenReturn(List.of(sydney, wollongong));

        NotificationController controller = new NotificationController(
                notificationRepository, preferenceRepository);

        int created = controller.eventCreated(
                new NotificationController.NewEventRequest(9L, "Local music", "Sydney CBD"));

        assertEquals(1, created);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<NotificationRecord>> captor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<NotificationRecord> notifications = toList(captor.getValue());
        assertEquals(1, notifications.size());
        assertEquals(101L, notifications.getFirst().getAccountId());
        assertFalse(notifications.getFirst().isRead());
    }

    @Test
    void eventChangesRespectOptOutAndDefaultUnknownPreferencesToEnabled() {
        NotificationPreference disabled = preference(201L, false, null);
        when(preferenceRepository.findAllById(List.of(201L, 202L)))
                .thenReturn(List.of(disabled));

        NotificationController controller = new NotificationController(
                notificationRepository, preferenceRepository);

        int created = controller.eventChanged(new NotificationController.EventChangeRequest(
                12L, "Event postponed", "The event was postponed.", List.of(201L, 202L, 202L)));

        assertEquals(1, created);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<NotificationRecord>> captor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<NotificationRecord> notifications = toList(captor.getValue());
        assertEquals(1, notifications.size());
        assertEquals(202L, notifications.getFirst().getAccountId());
    }

    @Test
    void readingAndClearingAreSeparateOperations() {
        NotificationRecord notification =
                new NotificationRecord(301L, 21L, "Event postponed", "Check the event listing.");
        when(notificationRepository.findByIdAndAccountId(1L, 301L))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.deleteByIdAndAccountId(1L, 301L)).thenReturn(1L);

        NotificationController controller = new NotificationController(
                notificationRepository, preferenceRepository);

        assertFalse(notification.isRead());
        assertThrows(ResponseStatusException.class, () -> controller.clear(1L, 301L));
        verify(notificationRepository, never()).deleteByIdAndAccountId(1L, 301L);
        assertTrue(controller.markRead(1L, 301L).isRead());
        controller.clear(1L, 301L);
        assertTrue(notification.isRead());
        verify(notificationRepository).deleteByIdAndAccountId(1L, 301L);
    }

    private static NotificationPreference preference(
            Long accountId, boolean enabled, String area
    ) {
        NotificationPreference preference = new NotificationPreference(accountId);
        preference.setEnabled(enabled);
        preference.setArea(area);
        return preference;
    }

    private static List<NotificationRecord> toList(Iterable<NotificationRecord> records) {
        java.util.ArrayList<NotificationRecord> result = new java.util.ArrayList<>();
        records.forEach(result::add);
        return result;
    }
}
