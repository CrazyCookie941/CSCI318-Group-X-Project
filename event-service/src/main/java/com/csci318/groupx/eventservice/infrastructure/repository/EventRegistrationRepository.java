package com.csci318.groupx.eventservice.infrastructure.repository;

import com.csci318.groupx.eventservice.domain.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    List<EventRegistration> findByAccountId(Long accountId);

    List<EventRegistration> findByEventId(Long eventId);

    Optional<EventRegistration> findByAccountIdAndEventId(Long accountId, Long eventId);

    long deleteByIdAndAccountId(Long id, Long accountId);

    long deleteByEventId(Long eventId);
}
