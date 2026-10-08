package com.csci318.groupx.registrationservice;

import java.util.ArrayList;
import java.util.List;

public class AccountEventManager extends Account {

    // Specialized attributes for a manager
    private String organizationName;
    private List<String> managedEvents;

    // Constructor passing base attributes up using super()
    public AccountEventManager(String email, String username, String location, String password, String organizationName) {
        super(email, username, location, password);
        this.organizationName = organizationName;
        this.managedEvents = new ArrayList<>();
    }

    // Getters and Setters
    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public List<String> getManagedEventIds() {
        return managedEvents;
    }

    public void setManagedEvents(List<String> managedEvents) {
        this.managedEvents = managedEvents;
    }
}