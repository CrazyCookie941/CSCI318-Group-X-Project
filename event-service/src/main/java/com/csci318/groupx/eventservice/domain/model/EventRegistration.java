package com.csci318.groupx.eventservice.domain.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "event_registrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_registration_account_event",
                columnNames = {"account_id", "event_id"}
        )
)
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    protected EventRegistration() {
    }

    public EventRegistration(Long accountId, Long eventId) {
        this.accountId = accountId;
        this.eventId = eventId;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getEventId() {
        return eventId;
    }
}
