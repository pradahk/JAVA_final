package com.smwujava.medicineapp.calendar; // 패키지 경로 수정됨

import com.smwujava.medicineapp.model.Medicine;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;

public class MedicineCard extends JPanel {
    private static final int ARC_WIDTH = 20;
    private static final int ARC_HEIGHT = 20;

    private Medicine medicine;
    private Color trueColor;
    private Color defaultGray = new Color(245, 245, 245);
    private Color currentColor;

    private JLabel nameLabel;
    private JLabel timeLabel;

    private boolean isTaken;

    public interface MedicineCardClickListener {
        void onMedicineCardClicked(Medicine medicine, boolean isTaken);
    }

    private MedicineCardClickListener clickListener;

    public MedicineCard(Medicine medicine, String time, boolean isTaken) {
        this.medicine = medicine;
        this.trueColor = Color.decode(medicine.getColor());
        this.isTaken = isTaken;
        this.currentColor = isTaken ? trueColor : defaultGray;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setPreferredSize(new Dimension(430, 60));
        setMaximumSize(new Dimension(430, 60));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        nameLabel = new JLabel(medicine.getMedName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nameLabel.setForeground(Color.BLACK);

        timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        timeLabel.setForeground(Color.DARK_GRAY);

        add(nameLabel);
        add(timeLabel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleTakenStatus();
                if (clickListener != null) {
                    clickListener.onMedicineCardClicked(medicine, isTaken);
                }
            }
        });
    }

    private void toggleTakenStatus() {
        isTaken = !isTaken;
        currentColor = isTaken ? trueColor : defaultGray;
        repaint();
    }

    public void setTakenStatus(boolean taken) {
        if (this.isTaken != taken) {
            this.isTaken = taken;
            this.currentColor = isTaken ? trueColor : defaultGray;
            repaint();
        }
    }

    public boolean isTaken() {
        return isTaken;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setClickListener(MedicineCardClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(currentColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_WIDTH, ARC_HEIGHT);
        g2.dispose();
    }
}