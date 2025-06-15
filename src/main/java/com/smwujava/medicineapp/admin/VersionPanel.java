package com.smwujava.medicineapp.admin;

import javax.swing.*;
import java.awt.*;

public class VersionPanel extends JPanel {

    private final String currentVersion = "v1.3.2";
    private final String latestVersion = "v1.3.2";
    private final String lastModifiedDate = "2025-05-28";

    public VersionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 제목 라벨
        JLabel titleLabel = new JLabel("버전 관리", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // 상단: 현재 버전 정보
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);

        JLabel versionLabel = new JLabel("현재 버전: " + currentVersion);
        versionLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        versionLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel dateLabel = new JLabel("최종 수정일: " + lastModifiedDate);
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        dateLabel.setHorizontalAlignment(SwingConstants.LEFT);

        infoPanel.add(versionLabel);
        infoPanel.add(dateLabel);

        // 중앙: 변경 로그 (스크롤 포함)
        JTextArea logArea = new JTextArea(
                "- v1.3.2: 사용자 그래프 기능 추가\n" +
                        "- v1.3.1: 복약 기록 버그 수정\n" +
                        "- v1.3.0: 관리자 대시보드 추가"
        );
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(200, 80));
        scrollPane.setBorder(BorderFactory.createTitledBorder("업데이트 로그"));

        // 하단: 업데이트 버튼
        JButton updateButton = new JButton("업데이트 확인");
        updateButton.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        updateButton.addActionListener(e -> checkForUpdate());

        // 조립
        add(infoPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(updateButton, BorderLayout.SOUTH);
    }

    private void checkForUpdate() {
        if (currentVersion.equals(latestVersion)) {
            JOptionPane.showMessageDialog(this,
                    "현재 버전(" + currentVersion + ")이 최신입니다.",
                    "업데이트 확인",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                    "최신 버전(" + latestVersion + ")이 있습니다.\n업데이트를 진행하시겠습니까?",
                    "업데이트 가능",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this,
                        "업데이트가 진행되었습니다.",
                        "업데이트 완료",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}