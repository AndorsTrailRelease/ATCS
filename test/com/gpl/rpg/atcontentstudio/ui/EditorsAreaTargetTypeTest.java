package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.model.Preferences;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.Droplist;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.model.gamedata.Quest;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EditorsAreaTargetTypeTest {

    @Test
    public void getTargetTypeCoversAllSupportedTargets() {
        assertEquals(Preferences.OpenEditorState.TargetType.actorCondition, EditorsArea.getTargetType(ActorCondition.class));
        assertEquals(Preferences.OpenEditorState.TargetType.dialogue, EditorsArea.getTargetType(Dialogue.class));
        assertEquals(Preferences.OpenEditorState.TargetType.droplist, EditorsArea.getTargetType(Droplist.class));
        assertEquals(Preferences.OpenEditorState.TargetType.itemCategory, EditorsArea.getTargetType(ItemCategory.class));
        assertEquals(Preferences.OpenEditorState.TargetType.item, EditorsArea.getTargetType(Item.class));
        assertEquals(Preferences.OpenEditorState.TargetType.npc, EditorsArea.getTargetType(NPC.class));
        assertEquals(Preferences.OpenEditorState.TargetType.quest, EditorsArea.getTargetType(Quest.class));
        assertEquals(Preferences.OpenEditorState.TargetType.map, EditorsArea.getTargetType(TMXMap.class));
        assertEquals(Preferences.OpenEditorState.TargetType.spritesheet, EditorsArea.getTargetType(Spritesheet.class));
        assertEquals(Preferences.OpenEditorState.TargetType.worldmapSegment, EditorsArea.getTargetType(WorldmapSegment.class));
        assertEquals(Preferences.OpenEditorState.TargetType.writerSketch, EditorsArea.getTargetType(WriterModeData.class));
        assertNull(EditorsArea.getTargetType(Preferences.class));
    }
}

