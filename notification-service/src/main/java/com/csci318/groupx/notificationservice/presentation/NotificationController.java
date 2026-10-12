package com.csci318.groupx.notificationservice.presentation;

import com.csci318.groupx.notificationservice.domain.NotificationPreference;
import com.csci318.groupx.notificationservice.domain.NotificationRecord;
import com.csci318.groupx.notificationservice.repository.NotificationPreferenceRepository;
import com.csci318.groupx.notificationservice.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @GetMapping
    public List<NotificationRecord> list(@RequestParam Long accountId) {
        requireValidId(accountId);
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @GetMapping("/unread-count")
    public UnreadCount unreadCount(@RequestParam Long accountId) {
        requireValidId(accountId);
        return new UnreadCount(notificationRepository.countByAccountIdAndReadFalse(accountId));
    }

    @PutMapping("/{notificationId}/read")
    @Transactional
    public NotificationRecord markRead(
            @PathVariable Long notificationId,
            @RequestParam Long accountId
    ) {
        requireValidId(accountId);
        NotificationRecord notification = notificationRepository
                .findByIdAndAccountId(notificationId, accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found."));
        notification.markRead();
        return notification;
    }

    @DeleteMapping("/{notificationId}")
    @Transactional
    public void clear(
            @PathVariable Long notificationId,
            @RequestParam Long accountId
    ) {
        requireValidId(accountId);
        NotificationRecord notification = notificationRepository
                .findByIdAndAccountId(notificationId, accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found."));
        if (!notification.isRead()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mark the notification as read before clearing it.");
        }
        if (notificationRepository.deleteByIdAndAccountId(notificationId, accountId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found.");
        }
    }

    @GetMapping("/preferences/{accountId}")
    public PreferenceResponse preference(@PathVariable Long accountId) {
        requireValidId(accountId);
        NotificationPreference preference = preferenceRepository.findById(accountId)
                .orElseGet(() -> new NotificationPreference(accountId));
        return new PreferenceResponse(preference.isEnabled(), preference.getArea());
    }

    @PutMapping("/preferences/{accountId}")
    public PreferenceResponse savePreference(
            @PathVariable Long accountId,
            @RequestBody PreferenceRequest request
    ) {
        requireValidId(accountId);
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preference is required.");
        }
        NotificationPreference preference = preferenceRepository.findById(accountId)
                .orElseGet(() -> new NotificationPreference(accountId));
        preference.setEnabled(request.enabled());
        String area = request.area() == null ? "" : request.area().trim();
        preference.setArea(area.isEmpty() ? null : area);
        NotificationPreference saved = preferenceRepository.save(preference);
        return new PreferenceResponse(saved.isEnabled(), saved.getArea());
    }

    @PostMapping("/events/created")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public int eventCreated(@RequestBody NewEventRequest event) {
        if (event == null || event.eventId() == null || event.title() == null
                || event.title().isBlank() || event.location() == null || event.location().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event details are required.");
        }
        String eventLocation = event.location().toLowerCase(Locale.ROOT);
        List<NotificationRecord> notifications = preferenceRepository
                .findByEnabledTrueAndAreaIsNotNull()
                .stream()
                .filter(preference -> eventLocation.contains(
                        preference.getArea().toLowerCase(Locale.ROOT)))
                .map(preference -> new NotificationRecord(
                        preference.getAccountId(),
                        event.eventId(),
                        "New event in your area",
                        event.title() + " is happening at " + event.location() + "."))
                .toList();
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    @PostMapping("/events/changed")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public int eventChanged(@RequestBody EventChangeRequest eventChange) {
        if (eventChange == null || eventChange.eventId() == null
                || eventChange.title() == null || eventChange.title().isBlank()
                || eventChange.message() == null || eventChange.message().isBlank()
                || eventChange.accountIds() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event change details are required.");
        }
        List<Long> accountIds = eventChange.accountIds().stream()
                .filter(accountId -> accountId != null && accountId > 0)
                .distinct()
                .toList();
        var preferences = preferenceRepository.findAllById(accountIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        NotificationPreference::getAccountId,
                        NotificationPreference::isEnabled));
        List<NotificationRecord> notifications = accountIds.stream()
                .filter(accountId -> preferences.getOrDefault(accountId, true))
                .map(accountId -> new NotificationRecord(
                        accountId, eventChange.eventId(), eventChange.title(), eventChange.message()))
                .toList();
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    private static void requireValidId(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid accountId is required.");
        }
    }

    public record UnreadCount(long count) {
    }

    public record PreferenceRequest(boolean enabled, String area) {
    }

    public record PreferenceResponse(boolean enabled, String area) {
    }

    public record NewEventRequest(Long eventId, String title, String location) {
    }

    public record EventChangeRequest(
            Long eventId,
            String title,
            String message,
            List<Long> accountIds
    ) {
    }
}
