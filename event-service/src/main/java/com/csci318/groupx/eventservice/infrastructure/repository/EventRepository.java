package com.csci318.groupx.eventservice.infrastructure.repository;

import com.csci318.groupx.eventservice.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e
            WHERE (:category IS NULL OR LOWER(e.category) = LOWER(:category))
              AND (:location IS NULL OR LOWER(e.location) = LOWER(:location))
              AND (:maxPrice IS NULL OR e.price <= :maxPrice)
            """)
    List<Event> filterEvents(
            @Param("category") String category,
            @Param("location") String location,
            @Param("maxPrice") Double maxPrice
    );
}