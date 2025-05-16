package com.smwujava.medicineapp.ui.panels;

import com.smwujava.medicineapp.ui.components.MedicationCard;

import javax.swing.*;
import java.awt.*;

public class MedicationListPanel extends JPanel {
    public MedicationListPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(340, 480));

        // 내용 패널: 약 카드들을 세로로 쌓기
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        addCentered(contentPanel, new MedicationCard("오메프라졸 (Omeprazole)", "오전 9시", new Color(153, 153, 255)));
        addSpacer(contentPanel);
        addCentered(contentPanel, new MedicationCard("타이레놀 (Paracetamol)", "오후 2시", new Color(204, 255, 153)));
        addSpacer(contentPanel);
        addCentered(contentPanel, new MedicationCard("이부프로펜 (Ibuprofen)", "저녁 7시", new Color(224, 224, 224)));

        add(contentPanel, BorderLayout.NORTH); // 💡 위쪽 정렬 고정
    }

    private void addCentered(JPanel parent, JComponent comp) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(comp);
        parent.add(wrapper);
    }

    private void addSpacer(JPanel parent) {
        parent.add(Box.createVerticalStrut(10));
    }
}
