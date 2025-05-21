package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.dummy.Medication;
import com.smwujava.medicineapp.ui.components.MedicationCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MedicationListPanel extends JPanel {
    public MedicationListPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(new MedicationCard("Omeprazol", "아침 30분 전", new Color(135, 206, 250)));
        add(new MedicationCard("Panpyrin-O", "점심 30분 전", new Color(144, 238, 144)));
        add(new MedicationCard("Ibuprofen", "저녁 30분 후", Color.LIGHT_GRAY));

    }
}