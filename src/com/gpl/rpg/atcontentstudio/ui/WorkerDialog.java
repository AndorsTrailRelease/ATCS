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
        this.getContentPane().setLayout(new JideBoxLayout(this.getContentPane(), JideBoxLayout.PAGE_AXIS, 6));
        this.getContentPane().add(new JLabel("<html><font size=" + (int) (5 * ATContentStudio.SCALING) + ">Please wait.<br/>" + message + "</font></html>"), JideBoxLayout.VARY);
        JMovingIdler idler = new JMovingIdler();
        idler.setBackground(Color.WHITE);
        idler.setForeground(Color.GREEN);
        idler.start();
        this.getContentPane().add(idler, JideBoxLayout.FIX);
        this.pack();
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = this.getSize();
        idler.setPreferredSize(new Dimension(wdim.width, 10));
        this.pack();
        wdim = this.getSize();
        this.setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
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
