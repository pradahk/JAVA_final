package com.smwujava.medicineapp.admin;

import com.smwujava.medicineapp.dao.DosageRecordDao;
import com.smwujava.medicineapp.model.DosageRecord;

import javax.swing.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter; // LocalDateTime을 String으로 변환하기 위해 추가
import java.util.List;

public class DosageRecordDialog extends JDialog {

    // 날짜/시간 포맷터 (DosageRecordDao와 동일하게 설정)
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DosageRecordDialog(JFrame parent, DosageRecordDao dao) {
        super(parent, "복용 기록 목록", true);

        List<DosageRecord> records;
        try {
            records = dao.findAll(); // findAll()은 SQLException을 던질 수 있으므로 try-catch 필요
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "복용 기록을 불러오는 데 오류가 발생했습니다: " + e.getMessage(), "데이터 로드 오류", JOptionPane.ERROR_MESSAGE);
            records = new java.util.ArrayList<>(); // 오류 시 빈 리스트로 초기화
        }


        String[] columns = {"ID", "UserID", "MedicineID", "Scheduled Time", "Actual Taken Time", "Rescheduled Time", "Skipped"}; // 컬럼명 조정
        String[][] data = new String[records.size()][columns.length];

        for (int i = 0; i < records.size(); i++) {
            DosageRecord r = records.get(i);
            data[i][0] = String.valueOf(r.getRecordId()); // recordId 사용
            data[i][1] = String.valueOf(r.getUserId());
            data[i][2] = String.valueOf(r.getMedId()); // medId 사용
            data[i][3] = (r.getScheduledTime() != null) ? r.getScheduledTime().format(DATETIME_FORMATTER) : ""; // LocalDateTime을 String으로 변환
            data[i][4] = (r.getActualTakenTime() != null) ? r.getActualTakenTime().format(DATETIME_FORMATTER) : ""; // actualTakenTime 사용, String으로 변환
            data[i][5] = (r.getRescheduledTime() != null) ? r.getRescheduledTime().format(DATETIME_FORMATTER) : ""; // String으로 변환
            data[i][6] = String.valueOf(r.isSkipped()); // isSkipped 필드 추가
        }

        JTable table = new JTable(data, columns);
        // 테이블 내용 수정 불가능하게 설정 (선택 사항)
        table.setDefaultEditor(Object.class, null);
        // 정렬 기능 추가 (사용자 경험 개선)
        table.setAutoCreateRowSorter(true);


        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);

        setSize(800, 500); // 크기 조정 (컬럼 추가로 넓게)
        setLocationRelativeTo(parent);
    }
}