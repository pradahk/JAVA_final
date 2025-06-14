package com.smwujava.medicineapp.controller;

import com.smwujava.medicineapp.dao.MedicineDao;
import com.smwujava.medicineapp.model.Medicine;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MedicationSettingsController {

    public static boolean saveMedicine(
            int userId,
            JTextField nameField,
            JCheckBox[] dayCheckboxes,
            JComboBox<String> periodBox,
            JComboBox<String> offsetBox,
            JComboBox<String> directionBox,
            JLabel countLabel,
            Color selectedColor,
            Runnable onSaveSuccess
    ) {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(null, "약 이름을 입력하세요.");
                return false;
            }

            if (selectedColor == null) {
                JOptionPane.showMessageDialog(null, "색상을 선택해주세요.");
                return false;
            }

            String days = Arrays.stream(dayCheckboxes)
                    .filter(JCheckBox::isSelected)
                    .map(AbstractButton::getText)
                    .collect(Collectors.joining(","));

            if (days.isEmpty()) {
                JOptionPane.showMessageDialog(null, "복용 주기를 하나 이상 선택하세요.");
                return false;
            }

            String condition = (String) periodBox.getSelectedItem();
            String timing = (String) directionBox.getSelectedItem();
            String offsetText = (String) offsetBox.getSelectedItem();
            int minutes = offsetText.equals("1시간") ? 60 : Integer.parseInt(offsetText.replace("분", ""));
            int dose = Integer.parseInt(countLabel.getText());
            String colorHex = String.format("#%06X", selectedColor.getRGB() & 0xFFFFFF);

            MedicineDao medicineDao = new MedicineDao();
            Medicine med = new Medicine(0, userId, name, dose, days, condition, timing, minutes, colorHex);
            int insertedId = medicineDao.insertMedicine(med);

            if (insertedId > 0) {
                if (onSaveSuccess != null) {
                    onSaveSuccess.run();
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "저장 중 오류 발생: " + e.getMessage());
            return false;
        }
    }
}