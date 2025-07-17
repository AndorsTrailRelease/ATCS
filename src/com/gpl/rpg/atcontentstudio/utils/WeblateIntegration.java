package com.gpl.rpg.atcontentstudio.utils;

import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.utils.WeblateIntegration.WeblateTranslationUnit.Status;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class WeblateIntegration {

    static final String WEBLATE_SIPASH_KEY = "Weblate Sip Hash";

    public static String weblateHash(String str, String ctx) {

        byte[] data = null;

        if (str != null) {
            byte[] strBytes;
            try {
                strBytes = str.getBytes("UTF-8");
                byte[] ctxBytes = ctx.getBytes("UTF-8");
                data = new byte[strBytes.length + ctxBytes.length];
                System.arraycopy(strBytes, 0, data, 0, strBytes.length);
                System.arraycopy(ctxBytes, 0, data, strBytes.length, ctxBytes.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        } else {
            try {
                data = ctx.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return HashUtils.siphash(WEBLATE_SIPASH_KEY, data);
    }

    public static String getWeblateLabelURI(String text) {
        return "https://hosted.weblate.org/translate/andors-trail/game-content/" + Workspace.activeWorkspace.settings.translatorLanguage.getCurrentValue() + "/?checksum=" + weblateHash(text, "");
    }

    public static class WeblateTranslationUnit {
        public enum Status {
            notAllowed, error, absent, notTranslated, warning, fuzzy, done
        }

        public Status status;
        public String translatedText;
    }

    public static WeblateTranslationUnit getTranslationUnit(String text) {
        WeblateTranslationUnit unit = new WeblateTranslationUnit();
        if (!Workspace.activeWorkspace.settings.useInternet.getCurrentValue()) {
            unit.status = Status.notAllowed;
            unit.translatedText = "Allow internet connection in the workspace settings to get translation status";
        } else if (Workspace.activeWorkspace.settings.translatorLanguage == null) {
            unit.status = Status.notAllowed;
            unit.translatedText = "Select a target language in the workspace settings to get translation status";
        } else {
            unit.status = Status.absent;
            unit.translatedText = "Cannot find this on weblate";
            String hash = weblateHash(text, "");
            try {
                Document wlDoc = Jsoup.connect(getWeblateLabelURI(text)).get();
                Element textArea = wlDoc.getElementById("id_" + hash + "_0");
                if (textArea != null) {
                    String trans = textArea.text();
                    if (trans != null) {
                        unit.translatedText = trans.trim();
                        if (unit.translatedText.isEmpty()) {
                            unit.translatedText = "Not yet translated";
                            unit.status = Status.notTranslated;
                        } else {
                            unit.status = Status.done;
                        }
                    }
                    Element fuzzyBox = wlDoc.getElementById("id_" + hash + "_fuzzy");
                    if (fuzzyBox != null && fuzzyBox.hasAttr("checked")) {
                        if ("checked".equals(fuzzyBox.attr("checked"))) {
                            unit.status = Status.fuzzy;
                        }
                    } else {
                        Elements dangerZone = wlDoc.getElementsByAttributeValue("class", "panel panel-danger");
                        if (dangerZone != null && !dangerZone.isEmpty()) {
                            unit.status = Status.warning;
                        }
                    }
                }
            } catch (IOException e) {
                unit.status = Status.error;
                unit.translatedText = "Cannot connect to weblate: " + e.getMessage();
                e.printStackTrace();
            }
        }
        return unit;
    }


}
