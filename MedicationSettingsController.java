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
            Color selectedColor
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

            String condition = (String) periodBox.getSelectedItem(); // 식사/수면
            String timing = (String) directionBox.getSelectedItem(); // 전/후
            String offsetText = (String) offsetBox.getSelectedItem(); // "10분" or "1시간"
            int minutes = offsetText.equals("1시간") ? 60 : Integer.parseInt(offsetText.replace("분", ""));
            int dose = Integer.parseInt(countLabel.getText());

            String colorHex = String.format("#%06X", selectedColor.getRGB() & 0xFFFFFF);

            Medicine med = new Medicine(0, userId, name, dose, days, condition, timing, minutes, colorHex);
            int inserted = MedicineDao.insertMedicine(med);
            // 실데 데이터 베이스에 잘 저장 되었는지 콘솔 확인
            System.out.println("💾 저장된 약 정보: " + med);
            return inserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "저장 중 오류 발생: " + e.getMessage());
            return false;
        }
    }
}
