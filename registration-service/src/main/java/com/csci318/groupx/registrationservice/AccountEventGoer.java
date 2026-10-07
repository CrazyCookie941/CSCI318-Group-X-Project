package com.csci318.groupx.registrationservice;

import java.util.ArrayList;
import java.util.List;

public class AccountEventGoer extends Account {


    private List<String> bookingHistoryLog;

    // Constructor passing base attributes up using super()
    public AccountEventGoer(String email, String username, String location, String password) {
        super(email, username, location, password);
        this.bookingHistoryLog = new ArrayList<>();
    }

    public List<String> getBookingHistoryLog() {
        return bookingHistoryLog;
    }

    public void setBookingHistoryLog(List<String> bookingHistoryLog) {
        this.bookingHistoryLog = bookingHistoryLog;
    }
}