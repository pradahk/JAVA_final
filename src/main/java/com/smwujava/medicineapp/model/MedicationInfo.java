package com.smwujava.medicineapp.model;

import java.awt.Color;

public class MedicationInfo {
    private final String name;
    private final String time;
    private final Color color;

    public MedicationInfo(String name, String time, Color color) {
        this.name = name;
        this.time = time;
        this.color = color;
    }

    public String getName() { return name; }
    public String getTime() { return time; }
    public Color getColor() { return color; }
}
