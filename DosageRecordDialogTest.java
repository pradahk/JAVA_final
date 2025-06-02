package com.smwujava.medicineapp.test;

import com.smwujava.medicineapp.admin.DosageRecordDialog;
import com.smwujava.medicineapp.dao.DosageRecordDao;

import javax.swing.*;

public class DosageRecordDialogTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dummyFrame = new JFrame("Test Frame");
            dummyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dummyFrame.setSize(300, 200);
            dummyFrame.setVisible(false);

            DosageRecordDao dao = new DosageRecordDao();
            DosageRecordDialog dialog = new DosageRecordDialog(dummyFrame, dao);
            dialog.setVisible(true);
        });
    }
}

