package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Preferences;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.Droplist;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.model.gamedata.Quest;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import org.junit.Test;
import sun.misc.Unsafe;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class EditorsAreaOpenEditorStateTest {

    @Test
    public void resolveOpenEditorStateRoutesAllSupportedTargetTypes() {
        RecordingProject project = RecordingProject.create();
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

        for (Map.Entry<Preferences.OpenEditorState.TargetType, String> entry : idsByTargetType.entrySet()) {
            ProjectTreeNode expected = project.add(entry.getKey(), entry.getValue());
            Preferences.OpenEditorState state = new Preferences.OpenEditorState("demo-project", entry.getKey(), entry.getValue(), false);
            assertSame(expected, EditorsArea.resolveOpenEditorState(project, state));
        }
    }

    @Test
    public void createOpenEditorStateUsesInheritedIdForSpritesheet() throws Exception {
        Project project = allocate(Project.class);
        project.name = "demo-project";

        GameSource gameSource = allocate(GameSource.class);
        gameSource.parent = project;

        SpriteSheetSet spriteSheetSet = allocate(SpriteSheetSet.class);
        spriteSheetSet.parent = gameSource;

        Spritesheet spritesheet = allocate(Spritesheet.class);
        spritesheet.parent = spriteSheetSet;
        spritesheet.id = "monsters";

        assertEquals("monsters", ((GameDataElement) spritesheet).id);

        Preferences.OpenEditorState state = EditorsArea.createOpenEditorState(spritesheet, true);
        assertNotNull(state);
        assertEquals("demo-project", state.projectName);
        assertEquals(Preferences.OpenEditorState.TargetType.spritesheet, state.targetType);
        assertEquals("monsters", state.targetId);
        assertTrue(state.selected);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        return (T) unsafe.allocateInstance(type);
    }

    private static class RecordingProject extends Project {
        private ActorCondition actorCondition;
        private Dialogue dialogue;
        private Droplist droplist;
        private ItemCategory itemCategory;
        private Item item;
        private NPC npc;
        private Quest quest;
        private TMXMap map;
        private Spritesheet spritesheet;
        private WorldmapSegment worldmapSegment;
        private WriterModeData writerSketch;

        private RecordingProject() {
            super((Workspace) null, (java.io.File) null);
        }

        static RecordingProject create() {
            try {
                return allocate(RecordingProject.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        ProjectTreeNode add(Preferences.OpenEditorState.TargetType targetType, String id) {
            try {
                switch (targetType) {
                    case actorCondition:
                        actorCondition = allocate(ActorCondition.class);
                        actorCondition.id = id;
                        return actorCondition;
                    case dialogue:
                        dialogue = allocate(Dialogue.class);
                        dialogue.id = id;
                        return dialogue;
                    case droplist:
                        droplist = allocate(Droplist.class);
                        droplist.id = id;
                        return droplist;
                    case itemCategory:
                        itemCategory = allocate(ItemCategory.class);
                        itemCategory.id = id;
                        return itemCategory;
                    case item:
                        item = allocate(Item.class);
                        item.id = id;
                        return item;
                    case npc:
                        npc = allocate(NPC.class);
                        npc.id = id;
                        return npc;
                    case quest:
                        quest = allocate(Quest.class);
                        quest.id = id;
                        return quest;
                    case map:
                        map = allocate(TMXMap.class);
                        map.id = id;
                        return map;
                    case spritesheet:
                        spritesheet = allocate(Spritesheet.class);
                        spritesheet.id = id;
                        return spritesheet;
                    case worldmapSegment:
                        worldmapSegment = allocate(WorldmapSegment.class);
                        worldmapSegment.id = id;
                        return worldmapSegment;
                    case writerSketch:
                        writerSketch = allocate(WriterModeData.class);
                        writerSketch.id = id;
                        return writerSketch;
                    default:
                        return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ActorCondition getActorCondition(String id) {
            return actorCondition;
        }

        @Override
        public Dialogue getDialogue(String id) {
            return dialogue;
        }

        @Override
        public Droplist getDroplist(String id) {
            return droplist;
        }

        @Override
        public ItemCategory getItemCategory(String id) {
            return itemCategory;
        }

        @Override
        public Item getItem(String id) {
            return item;
        }

        @Override
        public NPC getNPC(String id) {
            return npc;
        }

        @Override
        public Quest getQuest(String id) {
            return quest;
        }

        @Override
        public TMXMap getMap(String id) {
            return map;
        }

        @Override
        public Spritesheet getSpritesheet(String id) {
            return spritesheet;
        }

        @Override
        public WorldmapSegment getWorldmapSegment(String id) {
            return worldmapSegment;
        }

        @Override
        public WriterModeData getWriterSketch(String id) {
            return writerSketch;
        }
    }

    private static class DummyNode implements ProjectTreeNode {
        private final String desc;
        private final Project project;

        DummyNode(String desc, Project project) {
            this.desc = desc;
            this.project = project;
        }

        @Override
        public void childrenAdded(List<ProjectTreeNode> path) {
        }

        @Override
        public void childrenChanged(List<ProjectTreeNode> path) {
        }

        @Override
        public void childrenRemoved(List<ProjectTreeNode> path) {
        }

        @Override
        public void notifyCreated() {
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public GameDataSet getDataSet() {
            return null;
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public Image getOpenIcon() {
            return null;
        }

        @Override
        public Image getClosedIcon() {
            return null;
        }

        @Override
        public Image getLeafIcon() {
            return null;
        }

        @Override
        public GameSource.Type getDataType() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean needsSaving() {
            return false;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public TreeNode getParent() {
            return null;
        }

        @Override
        public int getIndex(TreeNode node) {
            return -1;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return Collections.emptyEnumeration();
        }
    }
}


