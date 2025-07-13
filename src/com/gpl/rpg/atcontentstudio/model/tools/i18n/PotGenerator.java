package com.gpl.rpg.atcontentstudio.model.tools.i18n;

import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PotGenerator {

    public static void generatePotFileForProject(Project proj) {
        Map<String, List<String>> stringsResources = new LinkedHashMap<String, List<String>>();
        Map<String, String> resourcesStrings = new LinkedHashMap<String, String>();

        GameSource gsrc = proj.baseContent;

        for (ActorCondition ac : gsrc.gameData.actorConditions) {
            pushString(stringsResources, resourcesStrings, ac.display_name, getPotContextComment(ac));
            pushString(stringsResources, resourcesStrings, ac.description, getPotContextComment(ac) + ":description");
        }

        for (Dialogue d : gsrc.gameData.dialogues) {
            pushString(stringsResources, resourcesStrings, d.message, getPotContextComment(d));
            if (d.replies == null) continue;
            for (Dialogue.Reply r : d.replies) {
                if (r.text != null && !r.text.equals(Dialogue.Reply.GO_NEXT_TEXT)) {
                    pushString(stringsResources, resourcesStrings, r.text, getPotContextComment(d) + ":" + d.replies.indexOf(r));
                }
            }
        }

        for (ItemCategory ic : gsrc.gameData.itemCategories) {
            pushString(stringsResources, resourcesStrings, ic.name, getPotContextComment(ic));
        }

        for (Item i : gsrc.gameData.items) {
            pushString(stringsResources, resourcesStrings, i.name, getPotContextComment(i));
            pushString(stringsResources, resourcesStrings, i.description, getPotContextComment(i) + ":description");
        }

        for (NPC npc : gsrc.gameData.npcs) {
            pushString(stringsResources, resourcesStrings, npc.name, getPotContextComment(npc));
        }

        for (Quest q : gsrc.gameData.quests) {
            if (q.visible_in_log != null && q.visible_in_log != 0) {
                pushString(stringsResources, resourcesStrings, q.name, getPotContextComment(q));
                for (QuestStage qs : q.stages) {
                    pushString(stringsResources, resourcesStrings, qs.log_text, getPotContextComment(q) + ":" + Integer.toString(qs.progress));
                }
            }
        }

        for (WorldmapSegment ws : gsrc.worldmap) {
            for (WorldmapSegment.NamedArea area : ws.labels.values()) {
                pushString(stringsResources, resourcesStrings, area.name, gsrc.worldmap.worldmapFile.getName() + ":" + ws.id + ":" + area.id);
            }
        }

        File f = new File(proj.alteredContent.baseFolder, "english.pot");
        PoPotWriter.writePotFile(stringsResources, f);

    }

    private static void pushString(Map<String, List<String>> stringsResources, Map<String, String> resourcesStrings, String translatableString, String resourceIdentifier) {
        if (translatableString == null) return;
        if (translatableString.length() == 0) return;
        if (translatableString.contains("\"")) {
            translatableString = translatableString.replaceAll("\"", "\\\\\"");
        }
        if (translatableString.contains("\n")) {
            translatableString = translatableString.replaceAll("\n", "\\\\n\"\n\"");
            translatableString = "\"\n\"" + translatableString;
        }
        resourcesStrings.put(resourceIdentifier, translatableString);
        List<String> resourceIdentifiers = stringsResources.get(translatableString);
        if (resourceIdentifiers == null) {
            resourceIdentifiers = new LinkedList<String>();
            stringsResources.put(translatableString, resourceIdentifiers);
        }
        resourceIdentifiers.add(resourceIdentifier);
    }

    private static String getPotContextComment(JSONElement e) {
        return e.jsonFile.getName() + ":" + e.id;
    }


}
