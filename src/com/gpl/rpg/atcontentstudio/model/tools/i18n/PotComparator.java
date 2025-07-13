package com.gpl.rpg.atcontentstudio.model.tools.i18n;

import com.gpl.rpg.atcontentstudio.model.Project;
import net.launchpad.tobal.poparser.POEntry;
import net.launchpad.tobal.poparser.POFile;
import net.launchpad.tobal.poparser.POParser;

import javax.swing.*;
import java.io.File;
import java.io.FileFilter;
import java.util.*;


/**
 * @author Kevin
 * <p>
 * To use this, paste the following script in the beanshell console of ATCS.
 * Don't forget to change the project number to suit your needs.
 * <p>
 * <code>
 *
 * import com.gpl.rpg.atcontentstudio.model.Workspace;
 * import com.gpl.rpg.atcontentstudio.model.tools.i18n.PotGenerator;
 * import com.gpl.rpg.atcontentstudio.model.tools.i18n.PotComparator;
 *
 * proj = Workspace.activeWorkspace.projects.get(7);
 * PotGenerator.generatePotFileForProject(proj);
 * comp = new PotComparator(proj);
 * comp.compare();
 * comp.updatePoFiles(proj);
 * </code>
 */
public class PotComparator {

    Map<String, List<String>> stringsResourcesNew = new LinkedHashMap<String, List<String>>();
    Map<String, String> resourcesStringsNew = new LinkedHashMap<String, String>();

    Map<String, List<String>> stringsResourcesOld = new LinkedHashMap<String, List<String>>();
    Map<String, String> resourcesStringsOld = new LinkedHashMap<String, String>();

    Map<String, String> msgIdToReplace = new LinkedHashMap<String, String>();
    List<String> msgIdToReview = new LinkedList<String>();
    List<String> msgIdOutdated = new LinkedList<String>();


    public PotComparator(Project proj) {
        POParser parser = new POParser();

        POFile newPot = parser.parseFile(new File(proj.alteredContent.baseFolder.getAbsolutePath() + File.separator + "english.pot"));
        if (newPot == null) {
            System.err.println("Cannot locate new english.pot file at " + proj.alteredContent.baseFolder.getAbsolutePath() + File.separator);
        }
        extractFromPoFile(newPot, stringsResourcesNew, resourcesStringsNew);

        POFile oldPot = parser.parseFile(new File(proj.baseContent.baseFolder.getAbsolutePath() + File.separator + "assets" + File.separator + "translation" + File.separator + "english.pot"));
        if (oldPot == null) {
            System.err.println("Cannot locate old english.pot file at " + proj.baseContent.baseFolder.getAbsolutePath() + File.separator + "assets" + File.separator + "translations" + File.separator);
        }
        extractFromPoFile(oldPot, stringsResourcesOld, resourcesStringsOld);
    }


    private void extractFromPoFile(POFile po, Map<String, List<String>> stringsResources, Map<String, String> resourcesStrings) {
        for (POEntry entry : po.getEntryArray()) {
            Vector<String> resources = entry.getStringsByType(POEntry.StringType.REFERENCE);
            Vector<String> msgids = entry.getStringsByType(POEntry.StringType.MSGID);
            if (resources == null || resources.size() == 0 || msgids == null || msgids.size() == 0) continue;
            String msgid = msgids.get(0);
            if (msgids.size() > 1) {
                for (int i = 1; i < msgids.size(); i++) {
                    msgid += msgids.get(i);
                }
            }
            if (msgid.contains("\\n")) {
                msgid = msgid.replaceAll("\\\\n", "\\\\n\"\n\"");
                msgid = "\"\n\"" + msgid;
            }
            for (String resLine : resources) {
                String[] resArray = resLine.split(" ");
                for (String res : resArray) {
                    resourcesStrings.put(res, msgid);
                    if (stringsResources.get(msgid) == null) {
                        stringsResources.put(msgid, new LinkedList<String>());
                    }
                    stringsResources.get(msgid).add(res);
                }
            }
        }
    }

    public void compare() {
        for (String oldRes : resourcesStringsOld.keySet()) {
            String newString = resourcesStringsNew.get(oldRes);
            String oldString = resourcesStringsOld.get(oldRes);
            if (newString != null) {
                if (!newString.equals(oldString)) {
                    List<String> allOldResources = stringsResourcesOld.get(oldString);
                    List<String> allNewResources = stringsResourcesNew.get(oldString);
                    StringBuffer sb = new StringBuffer();
                    sb.append("---------------------------------------------\n");
                    sb.append("--- TYPO CHECK ------------------------------\n");
                    sb.append("---------------------------------------------\n");
                    sb.append("String at: " + oldRes + "\n");
                    if (allOldResources.size() > 1) {
                        sb.append("Also present at:\n");
                        for (String res : allOldResources) {
                            if (!res.equals(oldRes)) {
                                sb.append("- " + res + "\n");
                            }
                        }
                    }
                    if (allNewResources != null) {
                        sb.append("Still present at: \n");
                        for (String res : allNewResources) {
                            sb.append("- " + res + "\n");
                        }
                    }
                    sb.append("Was : \"" + oldString + "\"\n");
                    sb.append("Now : \"" + newString + "\"\n");
                    System.out.println(sb.toString());
                    showTypoDialog(oldString, newString, sb.toString());
                }
            } else {
                List<String> allOldResources = stringsResourcesOld.get(oldString);
                List<String> allNewResources = stringsResourcesNew.get(oldString);
                if (allOldResources.size() >= 1) {
                    System.out.println("---------------------------------------------");
                    System.out.println("--- REMOVED RESOURCE ------------------------");
                    System.out.println("---------------------------------------------");
                    System.out.println("String at: " + oldRes);
                    if (allOldResources.size() > 1) {
                        System.out.println("And also at:");
                        for (String res : allOldResources) {
                            if (!res.equals(oldRes)) {
                                System.out.println("- " + res);
                            }
                        }
                    }
                    System.out.println("Was: \"" + oldString + "\"");
                    if (allNewResources == null) {
                        System.out.println("Absent from new.");
                    } else {
                        System.out.println("Still present at: ");
                        for (String res : allNewResources) {
                            System.out.println("- " + res);
                        }

                    }
                }
            }
        }
        removedStrings:
        for (String oldString : stringsResourcesOld.keySet()) {
            if (stringsResourcesNew.get(oldString) == null) {
                List<String> allOldResources = stringsResourcesOld.get(oldString);
                if (allOldResources.size() >= 1) {
                    if (allOldResources.size() > 0) {
                        for (String res : allOldResources) {
                            String newString = resourcesStringsNew.get(res);
                            if (newString != null) {
                                continue removedStrings;
                            }
                        }
                    }
                    System.out.println("---------------------------------------------");
                    System.out.println("--- REMOVED STRING --------------------------");
                    System.out.println("---------------------------------------------");
                    System.out.println("String: \"" + oldString + "\"");
                    if (allOldResources.size() > 0) {
                        System.out.println("Was at:");
                        for (String res : allOldResources) {
                            System.out.println("- " + res);
                        }
                    }
                    System.out.println("This string is absent from the new file, and its attached resources are missing too.");
                }
            }
        }
    }

    private void showTypoDialog(String oldMsg, String newMsg, String checkReport) {
        String typo = "Typo";
        String review = "Review";
        String outdated = "Outdated";
        String none = "None";
        Object[] options = new Object[]{typo, review, outdated, none};

        int result = JOptionPane.showOptionDialog(null, checkReport, "Choose action", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, typo);

        if (result < 0 || result >= options.length) {
            System.out.println("No decision");
            return;
        }

        System.out.println("Decision: " + options[result]);

        if (options[result] != none) {
            msgIdToReplace.put(oldMsg, newMsg);
            if (options[result] == review) {
                msgIdToReview.add(newMsg);
            } else if (options[result] == outdated) {
                msgIdOutdated.add(newMsg);
            }
        }

    }


    public void updatePoFiles(Project proj) {
        File poFolder = new File(proj.baseContent.baseFolder.getAbsolutePath() + File.separator + "assets" + File.separator + "translation");
        File[] poFiles = poFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File arg0) {
                return arg0.isFile() && arg0.getName().endsWith(".po");
            }
        });

        for (File f : poFiles) {
            updatePoFile(proj, f);
        }
    }

    private void updatePoFile(Project proj, File f) {
        POParser parser = new POParser();
        POFile poFile = parser.parseFile(f);

        Map<String, String> translations = new LinkedHashMap<String, String>();

        //Collect existing translations
        if (poFile.getHeader() != null) {
            Vector<String> msgstrs = poFile.getHeader().getStringsByType(POEntry.StringType.HEADER);
            String header = "";
            if (!msgstrs.isEmpty()) {
                if (msgstrs.size() == 1) {
                    header = msgstrs.get(0);
                } else {
                    for (String msgstr : msgstrs) {
                        header += msgstr;
                        header += "\n";
                    }
                }
            }
            translations.put("", header);
        }

        for (POEntry entry : poFile.getEntryArray()) {
            Vector<String> msgids = entry.getStringsByType(POEntry.StringType.MSGID);
            Vector<String> msgstrs = entry.getStringsByType(POEntry.StringType.MSGSTR);
            if (msgids == null || msgids.size() == 0) continue;
            String msgid = msgids.get(0);
            if (msgids.size() > 1) {
                for (int i = 1; i < msgids.size(); i++) {
                    msgid += msgids.get(i);
                }
            }
            if (msgid.contains("\\n")) {
                msgid = msgid.replaceAll("\\\\n", "\\\\n\"\n\"");
                msgid = "\"\n\"" + msgid;
            }
            String translation = "";
            if (!msgstrs.isEmpty()) {
                if (msgstrs.size() == 1) {
                    translation = msgstrs.get(0);
                } else {
                    for (String msgstr : msgstrs) {
                        translation += msgstr;
                    }
                }
                if (translation.contains("\\n")) {
                    translation = translation.replaceAll("\\\\n", "\\\\n\"\n\"");
                    translation = "\"\n\"" + translation;
                }
            }
            translations.put(msgid, translation);
        }

        //Patch data
        for (String oldId : msgIdToReplace.keySet()) {
            String newId = msgIdToReplace.get(oldId);
            if (translations.containsKey(oldId)) {
                String trans = translations.get(oldId);
                translations.remove(oldId);
                translations.put(newId, trans);
            }
        }

        for (String msgid : msgIdToReview) {
            if (translations.containsKey(msgid)) {
                String trans = translations.get(msgid);
                if (trans != null && trans.length() >= 1) translations.put(msgid, "[REVIEW]" + trans);
            }
        }

        for (String msgid : msgIdOutdated) {
            if (translations.containsKey(msgid)) {
                String trans = translations.get(msgid);
                if (trans != null && trans.length() >= 1) translations.put(msgid, "[OUTDATED]" + trans);
            }
        }

        PoPotWriter.writePoFile(stringsResourcesNew, translations, new File(proj.alteredContent.baseFolder.getAbsolutePath() + File.separator + f.getName()));
    }

}
