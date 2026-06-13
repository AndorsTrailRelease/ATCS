package com.gpl.rpg.atcontentstudio.model;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PreferencesTest {

    private Map<Preferences.OpenEditorState.TargetType, String> allSupportedTargetTypeIds() {
        Map<Preferences.OpenEditorState.TargetType, String> idsByTargetType = new LinkedHashMap<>();
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.actorCondition, "poisoned");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.dialogue, "intro_001");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.droplist, "bandit_loot");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.itemCategory, "weapons");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.item, "longsword");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.npc, "rat_king");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.quest, "missing_scout");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.map, "forest_01");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.spritesheet, "monsters");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.worldmapSegment, "western_reach");
        idsByTargetType.put(Preferences.OpenEditorState.TargetType.writerSketch, "intro_writer");
        return idsByTargetType;
    }

    @Test
    public void openEditorStateRoundTripPreservesGetterAlignedTargetType() {
        Preferences.OpenEditorState original = new Preferences.OpenEditorState(
                "demo-project",
                Preferences.OpenEditorState.TargetType.item,
                "longsword",
                true
        );

        Preferences.OpenEditorState restored = new Preferences.OpenEditorState();
        restored.fromMap(original.toMap());

        assertEquals("demo-project", restored.projectName);
        assertEquals(Preferences.OpenEditorState.TargetType.item, restored.targetType);
        assertEquals("longsword", restored.targetId);
        assertTrue(restored.selected);
    }

    @Test
    public void preferencesToMapStoresFriendlyTargetTypeName() {
        Preferences preferences = new Preferences();
        preferences.openEditors.add(new Preferences.OpenEditorState(
                "demo-project",
                Preferences.OpenEditorState.TargetType.writerSketch,
                "intro_writer",
                false
        ));

        Map<?, ?> preferencesMap = preferences.toMap();
        List<?> openEditors = (List<?>) preferencesMap.get("openEditors");
        assertNotNull(openEditors);
        assertEquals(1, openEditors.size());
        Map<?, ?> openEditor = (Map<?, ?>) openEditors.toArray()[0];

        assertEquals("writerSketch", openEditor.get("targetType"));
    }

    @Test
    public void preferencesToMapStoresAllSupportedTargetTypeNames() {
        Map<Preferences.OpenEditorState.TargetType, String> expectedNames = new LinkedHashMap<>();
        expectedNames.put(Preferences.OpenEditorState.TargetType.actorCondition, "actorCondition");
        expectedNames.put(Preferences.OpenEditorState.TargetType.dialogue, "dialogue");
        expectedNames.put(Preferences.OpenEditorState.TargetType.droplist, "droplist");
        expectedNames.put(Preferences.OpenEditorState.TargetType.itemCategory, "itemCategory");
        expectedNames.put(Preferences.OpenEditorState.TargetType.item, "item");
        expectedNames.put(Preferences.OpenEditorState.TargetType.npc, "npc");
        expectedNames.put(Preferences.OpenEditorState.TargetType.quest, "quest");
        expectedNames.put(Preferences.OpenEditorState.TargetType.map, "map");
        expectedNames.put(Preferences.OpenEditorState.TargetType.spritesheet, "spritesheet");
        expectedNames.put(Preferences.OpenEditorState.TargetType.worldmapSegment, "worldmapSegment");
        expectedNames.put(Preferences.OpenEditorState.TargetType.writerSketch, "writerSketch");

        for (Map.Entry<Preferences.OpenEditorState.TargetType, String> entry : expectedNames.entrySet()) {
            Preferences.OpenEditorState state = new Preferences.OpenEditorState(
                    "demo-project",
                    entry.getKey(),
                    entry.getValue() + "_id",
                    false
            );

            Map<?, ?> serialized = state.toMap();
            assertEquals(entry.getValue(), serialized.get("targetType"));

            Preferences.OpenEditorState restored = new Preferences.OpenEditorState();
            restored.fromMap(serialized);
            assertEquals(entry.getKey(), restored.targetType);
        }
    }

    @Test
    public void preferencesRoundTripPreservesOpenEditors() {
        Preferences preferences = new Preferences();
        Map<Preferences.OpenEditorState.TargetType, String> idsByTargetType = allSupportedTargetTypeIds();
        int selectedIndex = idsByTargetType.size() - 1;
        int index = 0;
        for (Map.Entry<Preferences.OpenEditorState.TargetType, String> entry : idsByTargetType.entrySet()) {
            preferences.openEditors.add(new Preferences.OpenEditorState(
                    "demo-project",
                    entry.getKey(),
                    entry.getValue(),
                    index == selectedIndex
            ));
            index++;
        }

        Preferences restored = new Preferences();
        restored.fromMap(preferences.toMap());

        assertEquals(idsByTargetType.size(), restored.openEditors.size());

        index = 0;
        for (Map.Entry<Preferences.OpenEditorState.TargetType, String> entry : idsByTargetType.entrySet()) {
            Preferences.OpenEditorState restoredState = restored.openEditors.get(index);
            assertEquals("demo-project", restoredState.projectName);
            assertEquals(entry.getKey(), restoredState.targetType);
            assertEquals(entry.getValue(), restoredState.targetId);
            assertEquals(index == selectedIndex, restoredState.selected);
            index++;
        }
    }
}

