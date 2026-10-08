package com.csci318.groupx.registrationservice;

import java.util.List;
import java.util.Map;

public class Event {

    // Existing fields
    private String name;
    private String date;
    private String location;
    private String eventType;
    private String description;
    private Map<String, Double> admissionType;
    private List<String> eventStatus;

    // Constructor
    public Event(String name, String date, String location, String eventType, String description, Map<String, Double> admissionType, List<String> eventStatus) {
        this.name = name;
        this.date = date;
        this.location = location;
        this.eventType = eventType;
        this.description = description;
        this.admissionType = admissionType;
        this.eventStatus= eventStatus;
    }

    // Getters and Setters for new fields
    public Map<String, Double> getAdmissionType() {
        return admissionType;
    }

    public void setAdmissionType(Map<String, Double> admissionType) {
        this.admissionType = admissionType;
    }

    public List<String> getEventStatusHistory() {
        return eventStatus;
    }

    public void setEventStatusHistory(List<String> eventStatusHistory) {
        this.eventStatus = eventStatusHistory;
    }

    // Getters and Setters for existing fields
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}