package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;
import java.awt.*;


public class JMovingIdler extends JComponent {

    private static final long serialVersionUID = -2980521421870322717L;

    int position = 0;
    boolean destroyed = false, running = false;
    Thread moverThread = new Thread() {
        public void run() {
            while (!destroyed) {
                boolean back = false;
                while (running) {
                    if (back) {
                        position = --position % 100;
                        if (position == 0) {
                            back = false;
                        }
                    } else {
                        position = ++position % 100;
                        if (position == 99) {
                            back = true;
                        }
                    }
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                    }
                    JMovingIdler.this.revalidate();
                    JMovingIdler.this.repaint();
                }
            }
        }
    };

    public void start() {
        if (!moverThread.isAlive()) {
            moverThread.start();
        }
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void destroy() {
        destroyed = true;
        running = false;
        try {
            moverThread.join();
        } catch (InterruptedException e) {
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = this.getWidth();
        int h = this.getHeight();

        g2.setColor(getBackground());
        g2.fillRect(0, 0, w, h);

        int x = w * position / 100;

        Paint p = new GradientPaint(x - (w / 8), 0, getBackground(), x, 0, getForeground());
        g2.setPaint(p);
        g2.fillRect(Math.max(0, x - (w / 8)), 0, Math.min(x, w), h);

        p = new GradientPaint(x, 0, getForeground(), x + (w / 8), 0, getBackground());
        g2.setPaint(p);
        g2.fillRect(Math.max(0, x), 0, Math.min(x + (w / 8), w), h);

        g2.setColor(Color.BLACK);
        g2.drawLine(0, 0, 0, h);
        g2.drawLine(0, 0, w, 0);
        g2.drawLine(w, 0, w, h);
        g2.drawLine(0, h, w, h);
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (!aFlag) destroy();
    }

}

