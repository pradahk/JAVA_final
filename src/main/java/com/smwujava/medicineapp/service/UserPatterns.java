package com.smwujava.medicineapp.model;

import java.time.LocalTime;

public class UserPatterns {
    private int userId;
    private LocalTime breakfast;
    private LocalTime lunch;
    private LocalTime dinner;
    private LocalTime sleep;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalTime getBreakfast() { return breakfast; }
    public void setBreakfast(LocalTime breakfast) { this.breakfast = breakfast; }

    public LocalTime getLunch() { return lunch; }
    public void setLunch(LocalTime lunch) { this.lunch = lunch; }

    public LocalTime getDinner() { return dinner; }
    public void setDinner(LocalTime dinner) { this.dinner = dinner; }

    public LocalTime getSleep() { return sleep; }
    public void setSleep(LocalTime sleep) { this.sleep = sleep; }
}
