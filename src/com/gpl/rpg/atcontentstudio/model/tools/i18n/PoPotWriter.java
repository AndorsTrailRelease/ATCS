package com.gpl.rpg.atcontentstudio.model.tools.i18n;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PoPotWriter {

    Map<String, List<String>> stringsResources = new LinkedHashMap<String, List<String>>();
    Map<String, String> translations = new LinkedHashMap<String, String>();
    File f;

    public static void writePoFile(Map<String, List<String>> stringsResources, Map<String, String> translations, File destination) {
        try {
            Writer fw = new OutputStreamWriter(new FileOutputStream(destination), StandardCharsets.UTF_8);
            if (translations.get("") != null) {
                fw.write(translations.get(""));
                writeEndOfEntry(fw);
            }
            if (translations.get("translator-credits") != null) {
                List<String> refs = new LinkedList<String>();
                refs.add("[none]");
                writeReferences(fw, refs);
                writeMsgId(fw, "translator-credits");
                writeMsgStr(fw, translations.get("translator-credits"));
                writeEndOfEntry(fw);
            }
            for (String msg : stringsResources.keySet()) {
                writeReferences(fw, stringsResources.get(msg));
                writeMsgId(fw, msg);
                writeMsgStr(fw, translations.get(msg));
                writeEndOfEntry(fw);
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writePotFile(Map<String, List<String>> stringsResources, File destination) {
        try {
            FileWriter fw = new FileWriter(destination);
            for (String msg : stringsResources.keySet()) {
                writeReferences(fw, stringsResources.get(msg));
                writeMsgId(fw, msg);
                writeMsgStr(fw, "");
                writeEndOfEntry(fw);
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeReferences(Writer w, List<String> references) throws IOException {
        for (String ref : references) {
            w.write("#: ");
            w.write(ref);
            w.write("\n");
        }
    }

    private static void writeMsgId(Writer w, String msg) throws IOException {
        w.write("msgid \"");
        w.write(msg);
        w.write("\"\n");
    }

    private static void writeMsgStr(Writer w, String translation) throws IOException {
        w.write("msgstr \"");
        w.write(translation == null ? "" : translation);
        w.write("\"\n");
    }

    private static void writeEndOfEntry(Writer w) throws IOException {
        w.write("\n");
    }

}
