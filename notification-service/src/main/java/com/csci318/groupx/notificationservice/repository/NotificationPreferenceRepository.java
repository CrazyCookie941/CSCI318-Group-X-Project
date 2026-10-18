package com.csci318.groupx.notificationservice.repository;

import com.csci318.groupx.notificationservice.domain.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, Long> {

    List<NotificationPreference> findByEnabledTrueAndAreaIsNotNull();
}
