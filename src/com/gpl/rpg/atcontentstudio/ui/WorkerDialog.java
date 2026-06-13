package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;


public class WorkerDialog extends JDialog {
    private static final long serialVersionUID = 8239669104275145995L;

    private WorkerDialog(String message, Frame parent) {
        super(parent, "Loading...");
        this.setIconImage(DefaultIcons.getMainIconImage());
        this.getContentPane().setLayout(new BorderLayout());

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new JideBoxLayout(messagePanel, JideBoxLayout.PAGE_AXIS, 6));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        messagePanel.add(new JLabel("<html><font size=" + (int) (5 * ATContentStudio.SCALING) + ">Please wait.<br/>" + message + "</font></html>"), JideBoxLayout.VARY);
        this.getContentPane().add(messagePanel, BorderLayout.CENTER);

        JMovingIdler idler = new JMovingIdler();
        idler.setBackground(Color.WHITE);
        idler.setForeground(Color.GREEN);
        idler.setPreferredSize(new Dimension(0, 10));
        idler.start();
        this.getContentPane().add(idler, BorderLayout.SOUTH);
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    public static void showTaskMessage(String message, Frame parent, Runnable workload) {
        showTaskMessage(message, parent, false, workload);
    }

    public static void showTaskMessage(final String message, final Frame parent, final boolean showConfirm, final Runnable workload) {
        new Thread() {
            public void run() {
                WorkerDialog info = new WorkerDialog(message, parent);
                info.setVisible(true);
                workload.run();
                info.dispose();
                if (showConfirm)
                    JOptionPane.showMessageDialog(parent, "<html><font size=" + (int) (5 * ATContentStudio.SCALING) + ">Done !</font></html>");
            }

        }.start();
    }
}
