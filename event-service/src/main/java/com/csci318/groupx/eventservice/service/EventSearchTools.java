
package com.csci318.groupx.eventservice.service;

import com.csci318.groupx.eventservice.domain.model.Event;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class EventSearchTools {

    private static final int MAX_RESULTS = 30;

    private final EventService eventService;
    private final ChatTelemetry telemetry;

    public EventSearchTools(
            EventService eventService,
            ChatTelemetry telemetry
    ) {
        this.eventService = eventService;
        this.telemetry = telemetry;
    }

    @Tool("""
            List events currently stored in the platform database.
            Call this tool when a user asks what events are available.
            It returns event IDs, names, locations, dates and prices.
            This does not confirm real-time ticket availability.
            """)
    public String listEvents() {

        return execute("listEvents", () -> {

            List<Event> events = eventService.getAllEvents();

            return formatResults(events);
        });
    }

    @Tool("""
            Search actual event records in the platform database.
            Use this tool for event recommendations, location searches,
            category searches and price constraints.
            All four filters are optional.
            Do not invent events not returned by this tool.
            """)
    public String searchEvents(

            @P(
                    name = "location",
                    value = "City or location, such as Sydney. Optional.",
                    required = false
            )
            String location,

            @P(
                    name = "maxPriceAud",
                    value = "Maximum ticket price in Australian dollars. Optional.",
                    required = false
            )
            Double maxPriceAud,

            @P(
                    name = "category",
                    value = "Event category such as technology or music. Optional.",
                    required = false
            )
            String category,

            @P(
                    name = "keyword",
                    value = "Optional event name or description keyword.",
                    required = false
            )
            String keyword
    ) {

        return execute("searchEvents", () -> {

            if (maxPriceAud != null &&
                    (!Double.isFinite(maxPriceAud) ||
                            maxPriceAud < 0)) {

                return "Invalid maximum price. Use a non-negative AUD amount.";
            }

            List<Event> matchingEvents =
                    eventService.getAllEvents()
                            .stream()
                            .filter(event ->
                                    matches(
                                            event.getLocation(),
                                            location
                                    )
                            )
                            .filter(event ->
                                    matches(
                                            event.getCategory(),
                                            category
                                    )
                            )
                            .filter(event ->
                                    maxPriceAud == null ||
                                            (event.getPrice() != null &&
                                                    event.getPrice() <= maxPriceAud)
                            )
                            .filter(event ->
                                    matchesKeyword(event, keyword)
                            )
                            .toList();

            return formatResults(matchingEvents);
        });
    }

    private String execute(
            String toolName,
            Supplier<String> operation
    ) {

        long started = System.nanoTime();
        boolean success = false;

        try {

            String result = operation.get();
            success = true;
            return result;

        } finally {

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - started
            );

            telemetry.recordToolCall(
                    toolName,
                    elapsedMs,
                    success
            );
        }
    }

    private String formatResults(List<Event> events) {

        if (events == null || events.isEmpty()) {
            return "No matching events were found in the database.";
        }

        List<Event> selected = events
                .stream()
                .sorted(
                        Comparator.comparing(
                                Event::getStartDateTime,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .limit(MAX_RESULTS)
                .toList();

        String result = selected.stream()
                .map(this::formatEvent)
                .collect(Collectors.joining("\n\n"));

        String header =
                "Matching database records: " + events.size() +
                        ". Returned records: " + selected.size() + ".";

        if (events.size() > MAX_RESULTS) {
            header += " Results are truncated.";
        }

        return header + "\n\n" + result;
    }

    private String formatEvent(Event event) {

        return """
                Event ID: %s
                Name: %s
                Category: %s
                Location: %s
                Description: %s
                Start: %s
                End: %s
                Price AUD: %s
                Capacity: %s
                Registered count: %s
                """.formatted(
                event.getId(),
                safe(event.getName()),
                safe(event.getCategory()),
                safe(event.getLocation()),
                shorten(event.getDescription(), 200),
                formatDate(event.getStartDateTime()),
                formatDate(event.getEndDateTime()),
                event.getPrice() == null
                        ? "Unknown"
                        : event.getPrice(),
                event.getCapacity() == null
                        ? "Unknown"
                        : event.getCapacity(),
                event.getRegisteredCount() == null
                        ? "Unknown"
                        : event.getRegisteredCount()
        );
    }

    private boolean matches(String actual, String expected) {

        if (expected == null || expected.isBlank()) {
            return true;
        }

        if (actual == null) {
            return false;
        }

        return actual.toLowerCase(Locale.ROOT)
                .contains(
                        expected.trim().toLowerCase(Locale.ROOT)
                );
    }

    private boolean matchesKeyword(Event event, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String searchable = String.join(
                " ",
                safe(event.getName()),
                safe(event.getDescription()),
                safe(event.getCategory()),
                safe(event.getLocation())
        ).toLowerCase(Locale.ROOT);

        return searchable.contains(
                keyword.trim().toLowerCase(Locale.ROOT)
        );
    }

    private String safe(String value) {

        if (value == null || value.isBlank()) {
            return "Not provided";
        }

        return value
                .replace('\n', ' ')
                .replace('\r', ' ')
                .trim();
    }

    private String shorten(String value, int maxLength) {

        String text = safe(value);

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }

    private String formatDate(LocalDateTime date) {

        return date == null
                ? "Not specified"
                : date.toString();
    }
}
