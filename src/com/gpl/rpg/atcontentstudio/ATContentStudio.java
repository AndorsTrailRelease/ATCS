package com.gpl.rpg.atcontentstudio;

import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.ui.StudioFrame;
import com.gpl.rpg.atcontentstudio.ui.WorkerDialog;
import com.gpl.rpg.atcontentstudio.ui.WorkspaceSelector;
import prefuse.data.expression.parser.ExpressionParser;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ATContentStudio {

    public static final String APP_NAME = "Andor's Trail Content Studio";
    public static final String APP_VERSION = readVersionFromFile();

    public static final String CHECK_UPDATE_URL = "https://andorstrail.com/static/ATCS_latest";
    public static final String DOWNLOAD_URL = "https://andorstrail.com/viewtopic.php?f=6&t=4806";

    public static final String FONT_SCALE_ENV_VAR_NAME = "FONT_SCALE";

    public static boolean STARTED = false;
    public static float SCALING = 1.0f;
    public static StudioFrame frame = null;

    // Need to keep a strong reference to it, to avoid garbage collection that'll
    // reset these loggers.
    public static final List<Logger> configuredLoggers = new LinkedList<Logger>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fontScaling = System.getProperty(FONT_SCALE_ENV_VAR_NAME);
        Float fontScale;
        if (fontScaling != null) {
            try {
                fontScale = Float.parseFloat(fontScaling);
                SCALING = fontScale;
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse font scaling parameter. Using default.");
                e.printStackTrace();
            }
        }

        ConfigCache.init();

        try {
            String laf = ConfigCache.getFavoriteLaFClassName();
            if (laf == null)
                laf = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(laf);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        scaleUIFont();

        // Need to keep a strong reference to it, to avoid garbage collection that'll
        // reset this setting.
        Logger l = Logger.getLogger(ExpressionParser.class.getName());
        l.setLevel(Level.OFF);
        configuredLoggers.add(l);

        final WorkspaceSelector wsSelect = new WorkspaceSelector();
        wsSelect.pack();
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = wsSelect.getSize();
        wsSelect.setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
        wsSelect.setVisible(true);

        wsSelect.addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosed(WindowEvent e) {
                if (wsSelect.selected != null && !STARTED) {
                    ATContentStudio.STARTED = true;
                    final File workspaceRoot = new File(wsSelect.selected);
                    WorkerDialog.showTaskMessage("Loading your workspace...", null, new Runnable() {
                        public void run() {
                            Workspace.setActive(workspaceRoot);
                            if (Workspace.activeWorkspace.settings.useInternet.getCurrentValue()
                                    && Workspace.activeWorkspace.settings.checkUpdates.getCurrentValue()) {
                                new Thread() {
                                    public void run() {
                                        checkUpdate();
                                    }
                                }.start();
                            }
                            frame = new StudioFrame(APP_NAME + " " + APP_VERSION);
                            frame.setVisible(true);
                            frame.setDefaultCloseOperation(StudioFrame.DO_NOTHING_ON_CLOSE);
                        }

                    });
                    for (File f : ConfigCache.getKnownWorkspaces()) {
                        if (workspaceRoot.equals(f)) {
                            if (!workspaceRoot.equals(ConfigCache.getLatestWorkspace())) {
                                ConfigCache.setLatestWorkspace(f);
                            }
                            return;
                        }
                    }
                    ConfigCache.addWorkspace(workspaceRoot);
                    ConfigCache.setLatestWorkspace(workspaceRoot);

                }
            }
        });
    }

    private static void checkUpdate() {
        BufferedReader in = null;
        try {
            URL url = new URL(CHECK_UPDATE_URL);
            in = new BufferedReader(new InputStreamReader(url.openStream()));

            String inputLine, lastLine = null;
            while ((inputLine = in.readLine()) != null) {
                lastLine = inputLine;
            }
            if (lastLine != null && !lastLine.equals(APP_VERSION)) {

                // for copying style
                JLabel label = new JLabel();
                Font font = label.getFont();
                Color color = label.getBackground();

                // create some css from the label's font
                StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
                style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
                style.append("font-size:" + font.getSize() + "pt;");
                style.append("background-color: rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue()
                                     + ");");

                JEditorPane ep = new JEditorPane("text/html",
                                                 "<html><body style=\"" + style + "\">" + "You are not running the latest ATCS version.<br/>"
                                                         + "You can get the latest version (" + lastLine + ") by clicking the link below.<br/>"
                                                         + "<a href=\"" + DOWNLOAD_URL + "\">" + DOWNLOAD_URL + "</a><br/>" + "<br/>"
                                                         + "</body></html>");

                ep.setEditable(false);
                ep.setBorder(null);

                ep.addHyperlinkListener(new HyperlinkListener() {

                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        try {
                            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                                Desktop.getDesktop().browse(e.getURL().toURI());
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                JOptionPane.showMessageDialog(null, ep, "Update available", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void scaleUIFont() {
        if (SCALING != 1.0f) {
            System.out.println("Scaling fonts to " + SCALING);
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            Map<Object, Object> newDefaults = new HashMap<Object, Object>();
            for (Enumeration<Object> e = defaults.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                Object value = defaults.get(key);
                if (value instanceof Font) {
                    Font font = (Font) value;
                    int newSize = (int) (font.getSize() * SCALING);
                    if (value instanceof FontUIResource) {
                        newDefaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
                    } else {
                        newDefaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
                    }
                }
            }
            for (Object key : newDefaults.keySet()) {
                defaults.put(key, newDefaults.get(key));
            }
        }
    }

    private static String readVersionFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(ATContentStudio.class.getResourceAsStream("/ATCS_latest"))))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "unknown";
        }
    }
}
