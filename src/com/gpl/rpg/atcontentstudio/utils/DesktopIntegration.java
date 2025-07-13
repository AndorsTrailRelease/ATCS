package com.gpl.rpg.atcontentstudio.utils;

import com.gpl.rpg.atcontentstudio.model.Workspace;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DesktopIntegration {

    public static void openTmxMap(File f) {
        if (Workspace.activeWorkspace.settings.useSystemDefaultMapEditor.getCurrentValue()) {
            try {
                Desktop.getDesktop().open(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Runtime.getRuntime().exec(tokenize(Workspace.activeWorkspace.settings.mapEditorCommand.getCurrentValue() + " \"" + f.getAbsolutePath() + "\""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void openImage(File f) {
        if (Workspace.activeWorkspace.settings.useSystemDefaultImageViewer.getCurrentValue()) {
            try {
                Desktop.getDesktop().open(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Workspace.activeWorkspace.settings.useSystemDefaultImageEditor.getCurrentValue()) {
            try {
                Desktop.getDesktop().edit(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Runtime.getRuntime().exec(tokenize(Workspace.activeWorkspace.settings.imageEditorCommand.getCurrentValue() + " \"" + f.getAbsolutePath() + "\""));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static enum OSType {
        Windows, MacOS, NIX, Other
    }

    public static OSType detectedOS = detectOS();

    private static OSType detectOS() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) return OSType.MacOS;
        if (os.indexOf("win") >= 0) return OSType.Windows;
        if ((os.indexOf("nux") >= 0) || (os.indexOf("nix") >= 0) || (os.indexOf("aix") >= 0) || (os.indexOf("sunos") >= 0) || (os.indexOf("solaris") >= 0))
            return OSType.NIX;
        return OSType.Other;
    }


    private static List<Character> quotes = Arrays.asList(new Character[]{'\'', '"'});
    private static List<Character> delims = Arrays.asList(new Character[]{' ', '\r', '\n', '\t'});

    private static String[] tokenize(String command) {
        List<String> tokens = new ArrayList<String>();
        boolean inQuote = false;
        char usedQuote = '\0';
        StringBuilder sb = new StringBuilder();

        for (char c : command.toCharArray()) {
            if (inQuote) {
                if (c == usedQuote) {
                    inQuote = false;
                    continue;
                } else {
                    sb.append(c);
                }
            } else {
                if (quotes.contains(c)) {
                    inQuote = true;
                    usedQuote = c;
                } else if (delims.contains(c)) {
                    if (sb.length() > 0) {
                        tokens.add(sb.toString());
                        sb = new StringBuilder();
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        if (sb.length() > 0) {
            tokens.add(sb.toString());
        }
        return tokens.toArray(new String[tokens.size()]);
    }


}
