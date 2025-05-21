package com.smwujava.medicineapp.dummy;

import java.awt.*;
import java.util.List;

public class Medication {
    public String name;
    public List<String> days;
    public String timeInfo;
    public int dose;
    public Color color;

    public Medication(String name, List<String> days, String timeInfo, int dose, Color color) {
        this.name = name;
        this.days = days;
        this.timeInfo = timeInfo;
        this.dose = dose;
        this.color = color;
    }
}