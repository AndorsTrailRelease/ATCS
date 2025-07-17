package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

public class AboutEditor extends Editor {

    private static final long serialVersionUID = 6230549148222457139L;

    public static final String WELCOME_STRING =
            "<html><head>" +
                    "<meta http-equiv=\\\"Content-Type\\\" content=\\\"text/html; charset=UTF-8\\\" />" +
                    "</head><body>" +
                    "<table><tr valign=\"top\">" +
                    "<td><img src=\"" + ATContentStudio.class.getResource("/com/gpl/rpg/atcontentstudio/img/atcs_border_banner.png") + "\"/></td>" +
                    "<td><font size=+1>Welcome to " + ATContentStudio.APP_NAME + " " + ATContentStudio.APP_VERSION + "</font><br/>" +
                    "<br/>" +
                    "This is a content editor for Andor's Trail.<br/>" +
                    "<b>Right click on the left area or use the \"File\" menu to create a project.</b><br/>" +
                    "<br/>" +
                    "Play <a href=\"https://play.google.com/store/apps/details?id=com.gpl.rpg.AndorsTrail\">Andor's Trail</a> for free on your Android device.<br/>" +
                    "Visit <a href=\"https://andorstrail.com/\">the official forum</a> to give or receive help.<br/>" +
                    "Open the project's <a href=\"https://github.com/Zukero/andors-trail/\">GitHub project page</a> to check out the game's source code.<br/>" +
                    "<br/>" +
                    "For content creation help, make sure to use the following resources:<br/>" +
                    "<a href=\"https://andorstrail.com/viewtopic.php?f=6&t=4560\">The contribution guide on the forums</a><br/>" +
                    "<a href=\"https://andorstrail.com/wiki/doku.php?id=andors_trail_wiki:developer_section\">The developer section of the Andor's Trail wiki</a><br/>" +
                    "<a href=\"https://docs.google.com/document/d/1BwWD1tLgPcmA2bwudrVnOc6f2dkPLFCjWdn7tXlIp5g\">The design outline document on Google Drive/Docs</a><br/>" +
                    "<br/>" +
                    "<font size=+1>Credits:</font><br/>" +
                    "<br/>" +
                    "Author: <a href=\"https://andorstrail.com/memberlist.php?mode=viewprofile&u=2875\">Zukero</a><br/>" +
                    "Licence: <a href=\"http://www.gnu.org/licenses/gpl-3.0.html\">GPL v3</a><br/>" +
                    "Sources are included in this package and on <a href=\"https://github.com/Zukero/ATCS\">GitHub</a>.<br/>" +
                    "<br/>" +
                    "Contributors: <br/>" +
                    "Quentin Delvallet<br/>" +
                    "Žižkin<br/>" +
                    "Gonk<br/>" +
                    "<a href=\"https://github.com/OMGeeky\">OMGeeky</a><br/>" +
                    "<br/>" +
                    "This project uses the following libraries:<br/>" +
                    "<a href=\"http://code.google.com/p/json-simple/\">JSON.simple</a> by Yidong Fang & Chris Nokleberg.<br/>" +
                    "License: <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache License 2.0</a><br/>" +
                    "<br/>" +
                    "<a href=\"http://fifesoft.com/rsyntaxtextarea/\">RSyntaxTextArea</a> by Robert Futrell.<br/>" +
                    "License: <a href=\"http://fifesoft.com/rsyntaxtextarea/RSyntaxTextArea.License.txt\">Modified BSD License (a.k.a. 3-Clause BSD)</a><br/>" +
                    "<br/>" +
                    "<a href=\"http://www.jidesoft.com/products/oss.htm\">JIDE Common Layer</a> by JIDE Software.<br/>" +
                    "License: <a href=\"http://openjdk.java.net/legal/gplv2+ce.html\">GPL v2 with classpath exception</a><br/>" +
                    "<br/>" +
                    "A modified version of <a href=\"https://github.com/bjorn/tiled/tree/master/util/java/libtiled-java\">libtiled-java</a> by Adam Turk & Thorbjorn Lindeijer.<br/>" +
                    "License: <a href=\"https://github.com/bjorn/tiled/blob/master/LICENSE.BSD\">Simplified BSD License (a.k.a 2-Clause BSD)</a><br/>" +
                    "Sources of the modified version are included in this package.<br/>" +
                    "<br/>" +
                    "<a href=\"http://prefuse.org/\">Prefuse</a> by the Berkeley Institue of Design.<br/>" +
                    "License: <a href=\"http://prefuse.org/license-prefuse.txt\">Modified BSD License (a.k.a 3-Clause BSD)</a><br/>" +
                    "<br/>" +
                    "<a href=\"http://www.beanshell.org/\">BeanShell</a> by Pat Niemeyer.<br/>" +
                    "License: <a href=\"http://www.beanshell.org/license.html\">LGPL v3</a><br/>" +
                    "<br/>" +
                    "A slightly modified version of <a href=\"https://github.com/zackehh/siphash-java\">SipHash for Java</a> by Isaac Whitfield.<br/>" +
                    "License: <a href=\"https://github.com/zackehh/siphash-java/blob/master/LICENSE\">MIT License</a><br/>" +
                    "<br/>" +
                    "<a href=\"https://jsoup.org/\">jsoup</a> by Jonathan Hedley<br/>" +
                    "License: <a href=\"https://jsoup.org/license\">MIT License</a><br/>" +
                    "<br/>" +
                    "A slightly modified version of <a href=\"https://launchpad.net/po-parser\">General PO Parser</a> by Bal�zs T�th<br/>" +
                    "License: <a href=\"http://www.gnu.org/licenses/gpl-3.0.html\">GPL v3</a><br/>" +
                    "<br/>" +
                    "A slightly modified version of <a href=\"www.whoischarles.com\">Minify.java</a> by Charles Bihis<br/>" +
                    "License: <a href=\"https://github.com/charlesbihis/minify#license\">Douglas Crockford variant of MIT License</a><br/>" +
                    "<br/>" +
                    "See the tabs below to find the full license text for each of these.<br/>" +
                    "<br/>" +
                    "The Windows installer was created with:<br/>" +
                    "<a href=\"http://nsis.sourceforge.net/Main_Page\">NSIS (Nullsoft Scriptable Install System) v2.46</a>" +
                    "</td></tr></table>" +
                    "</body></html>";


    public static final AboutEditor instance = new AboutEditor();

    @SuppressWarnings("resource")
    private AboutEditor() {
        this.name = "About " + ATContentStudio.APP_NAME;
        this.icon = new ImageIcon(DefaultIcons.getMainIconIcon());
        this.target = new GameDataElement() {
            private static final long serialVersionUID = -227480102288529682L;

            @Override
            public GameDataSet getDataSet() {
                return null;
            }

            @Override
            public String getDesc() {
                return null;
            }

            @Override
            public void parse() {
            }

            @Override
            public void link() {
            }

            @Override
            public GameDataElement clone() {
                return null;
            }

            @Override
            public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
            }

            @Override
            public String getProjectFilename() {
                return null;
            }

            @Override
            public void save() {
            }

            @Override
            public List<SaveEvent> attemptSave() {
                return null;
            }
        };

        setLayout(new BorderLayout());
        JideTabbedPane editorTabsHolder = new JideTabbedPane(JideTabbedPane.BOTTOM);
        editorTabsHolder.setTabShape(JideTabbedPane.SHAPE_FLAT);
        editorTabsHolder.setUseDefaultShowCloseButtonOnTab(false);
        editorTabsHolder.setShowCloseButtonOnTab(false);
        add(editorTabsHolder, BorderLayout.CENTER);

        editorTabsHolder.add("Welcome", getInfoPane(WELCOME_STRING, "text/html"));
        editorTabsHolder.add("JSON.simple License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.JSON.simple.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("RSyntaxTextArea License",
                             getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/RSyntaxTextArea.License.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("JIDE Common Layer License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.JIDE.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("libtiled-java License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.libtiled.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("prefuse License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/license-prefuse.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("BeanShell License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.LGPLv3.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("SipHash for Java License",
                             getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.siphash-zackehh.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("jsoup License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.jsoup.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("General PO Parser License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.GPLv3.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("Minify.java License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.minify.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));
        editorTabsHolder.add("ATCS License", getInfoPane(new Scanner(ATContentStudio.class.getResourceAsStream("/LICENSE.GPLv3.txt"), "UTF-8").useDelimiter("\\A").next(), "text/text"));

    }

    private JPanel getInfoPane(String content, String mime) {
        JEditorPane welcome = new JEditorPane();
        welcome.setContentType(mime);
        welcome.setText(content);
        welcome.setEditable(false);
        welcome.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent arg0) {
                arg0.getEventType();
                if (arg0.getEventType() == EventType.ACTIVATED) {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        try {
                            Desktop.getDesktop().browse(arg0.getURL().toURI());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());
        pane.add(new JScrollPane(welcome), BorderLayout.CENTER);
        return pane;
    }


    @Override
    public void targetUpdated() {
    }

}
