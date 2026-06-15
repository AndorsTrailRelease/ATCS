package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class WorkerDialog extends JDialog {
    private static final long serialVersionUID = 8239669104275145995L;
    private static final Color DARK_GRAY = new Color(0x12, 0x24, 0x38);
    private static final Color LIGHT_TEXT = new Color(0xfc, 0xfc, 0xfc);

    private static final String LOADING_ANIMATION_XML = "/com/gpl/rpg/atcontentstudio/img/loading_anim.xml";

    private AnimatedIconLabel loadingAnimation;

    private WorkerDialog(String message, Frame parent) {
        super(parent, "Loading...");
        this.setIconImage(DefaultIcons.getMainIconImage());
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().setBackground(DARK_GRAY);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new JideBoxLayout(messagePanel, JideBoxLayout.PAGE_AXIS, 6));
        messagePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        messagePanel.setBackground(DARK_GRAY);
        JLabel messageLabel = new JLabel("<html><font size=%d>%s</font></html>".formatted((int) (5 * ATContentStudio.SCALING), message));
        messageLabel.setForeground(LIGHT_TEXT);
        messagePanel.add(messageLabel, JideBoxLayout.VARY);
        this.getContentPane().add(messagePanel, BorderLayout.CENTER);

        AnimationSequence animation = loadAnimationSequence();
        if (animation.frames.length > 0) {
            loadingAnimation = new AnimatedIconLabel(animation.frames, animation.durations);
            loadingAnimation.setBackground(DARK_GRAY);
            loadingAnimation.setOpaque(true);
            loadingAnimation.setHorizontalAlignment(SwingConstants.CENTER);
            this.getContentPane().add(loadingAnimation, BorderLayout.SOUTH);
        } else {
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setBackground(DARK_GRAY);
            progressBar.setForeground(Color.GREEN);
            progressBar.setBorderPainted(false);
            progressBar.setPreferredSize(new Dimension(0, 10));
            this.getContentPane().add(progressBar, BorderLayout.SOUTH);
        }
        this.pack();
        this.setLocationRelativeTo(parent);
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    private static AnimationSequence loadAnimationSequence() {
        List<Icon> frames = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();

        try (InputStream in = WorkerDialog.class.getResourceAsStream(LOADING_ANIMATION_XML)) {
            if (in != null) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                NodeList items = doc.getElementsByTagName("item");
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    String drawable = item.getAttribute("android:drawable");
                    String durationText = item.getAttribute("android:duration");
                    Icon frame = loadDrawableIcon(drawable);
                    if (frame != null) {
                        frames.add(frame);
                        durations.add(parseDuration(durationText));
                    }
                }
            }
        } catch (IOException | SAXException | RuntimeException | javax.xml.parsers.ParserConfigurationException e) {
            Notification.addWarn("Failed to load loading animation: " + e.getMessage());
        }

        Icon[] frameArray = frames.toArray(new Icon[0]);
        int[] durationArray = new int[durations.size()];
        for (int i = 0; i < durations.size(); i++) {
            durationArray[i] = durations.get(i);
        }
        return new AnimationSequence(frameArray, durationArray);
    }

    private static int parseDuration(String durationText) {
        try {
            return Integer.parseInt(durationText);
        } catch (NumberFormatException ex) {
            return 120;
        }
    }

    private static Icon loadDrawableIcon(String drawableRef) {
        if (drawableRef == null || drawableRef.isEmpty()) {
            return null;
        }
        String fileName = drawableRef;
        if (fileName.startsWith("@drawable/")) {
            fileName = fileName.substring("@drawable/".length());
        }
        URL url = WorkerDialog.class.getResource("/com/gpl/rpg/atcontentstudio/img/" + fileName + ".png");
        return url == null ? null : new ImageIcon(url);
    }

    @Override
    public void dispose() {
        if (loadingAnimation != null) {
            loadingAnimation.stopAnimation();
        }
        super.dispose();
    }

    public static void showTaskMessage(String message, Frame parent, Runnable workload) {
        showTaskMessage(message, parent, false, workload);
    }

    public static void showTaskMessage(final String message, final Frame parent, final boolean showConfirm, final Runnable workload) {
        new Thread(() -> {
            WorkerDialog info = new WorkerDialog(message, parent);
            info.setVisible(true);
            workload.run();
            info.dispose();
            if (showConfirm)
                JOptionPane.showMessageDialog(parent, "<html><font size=%d>Done !</font></html>".formatted((int) (5 * ATContentStudio.SCALING)));
        }).start();
    }

    private static class AnimatedIconLabel extends JLabel {
        private final Icon[] frames;
        private final int[] durations;
        private final Timer timer;
        private int frameIndex = 0;

        private AnimatedIconLabel(Icon[] frames, int[] durations) {
            this.frames = frames;
            this.durations = durations;
            setIcon(frames[0]);
            setPreferredSize(new Dimension(frames[0].getIconWidth(), frames[0].getIconHeight()));
            timer = new Timer(Math.max(1, durations[0]), this::advanceFrame);
            timer.start();
        }

        private void advanceFrame(ActionEvent event) {
            frameIndex = (frameIndex + 1) % this.frames.length;
            setIcon(this.frames[frameIndex]);
            ((Timer) event.getSource()).setDelay(Math.max(1, durations[frameIndex]));
        }

        private void stopAnimation() {
            timer.stop();
        }
    }

    private static class AnimationSequence {
        private final Icon[] frames;
        private final int[] durations;

        private AnimationSequence(Icon[] frames, int[] durations) {
            this.frames = frames;
            this.durations = durations;
        }
    }
}
