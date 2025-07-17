package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.Project.ResourceSet;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SavedSlotCollection;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class GameDataSet implements ProjectTreeNode, Serializable {

    private static final long serialVersionUID = -8558067213826970968L;

    public static final String DEFAULT_REL_PATH_IN_SOURCE = "res" + File.separator + "raw" + File.separator;
    public static final String DEFAULT_REL_PATH_IN_PROJECT = "json" + File.separator;

    public static final String GAME_AC_ARRAY_NAME = "loadresource_actorconditions";
    public static final String GAME_DIALOGUES_ARRAY_NAME = "loadresource_conversationlists";
    public static final String GAME_DROPLISTS_ARRAY_NAME = "loadresource_droplists";
    public static final String GAME_ITEMS_ARRAY_NAME = "loadresource_items";
    public static final String GAME_ITEMCAT_ARRAY_NAME = "loadresource_itemcategories";
    public static final String GAME_NPC_ARRAY_NAME = "loadresource_monsters";
    public static final String GAME_QUESTS_ARRAY_NAME = "loadresource_quests";
    public static final String DEBUG_SUFFIX = "_debug";
    public static final String RESOURCE_PREFIX = "@raw/";
    public static final String FILENAME_SUFFIX = ".json";

    public File baseFolder;

    public GameDataCategory<ActorCondition> actorConditions;
    public GameDataCategory<Dialogue> dialogues;
    public GameDataCategory<Droplist> droplists;
    public GameDataCategory<Item> items;
    public GameDataCategory<ItemCategory> itemCategories;
    public GameDataCategory<NPC> npcs;
    public GameDataCategory<Quest> quests;

    public GameSource parent;
    public SavedSlotCollection v;

    public GameDataSet(GameSource source) {

        this.parent = source;
        v = new SavedSlotCollection();

        if (parent.type.equals(GameSource.Type.altered) || parent.type.equals(GameSource.Type.created)) {
            this.baseFolder = new File(parent.baseFolder, GameDataSet.DEFAULT_REL_PATH_IN_PROJECT);
            if (!baseFolder.exists()) this.baseFolder.mkdirs();
        } else if (parent.type.equals(GameSource.Type.source)) {
            this.baseFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_SOURCE);
        }

        actorConditions = new GameDataCategory<ActorCondition>(this, ActorCondition.getStaticDesc());
        dialogues = new GameDataCategory<Dialogue>(this, Dialogue.getStaticDesc());
        droplists = new GameDataCategory<Droplist>(this, Droplist.getStaticDesc());
        items = new GameDataCategory<Item>(this, Item.getStaticDesc());
        itemCategories = new GameDataCategory<ItemCategory>(this, ItemCategory.getStaticDesc());
        npcs = new GameDataCategory<NPC>(this, NPC.getStaticDesc());
        quests = new GameDataCategory<Quest>(this, Quest.getStaticDesc());

        v.add(actorConditions);
        v.add(dialogues);
        v.add(droplists);
        v.add(items);
        v.add(itemCategories);
        v.add(npcs);
        v.add(quests);

        //Start parsing to populate categories' content.
        if (parent.type == GameSource.Type.source && (parent.parent.sourceSetToUse == ResourceSet.debugData || parent.parent.sourceSetToUse == ResourceSet.gameData)) {
            String suffix = (parent.parent.sourceSetToUse == ResourceSet.debugData) ? DEBUG_SUFFIX : "";

            if (parent.referencedSourceFiles.get(GAME_AC_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_AC_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        ActorCondition.fromJson(f, actorConditions);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_DIALOGUES_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_DIALOGUES_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        Dialogue.fromJson(f, dialogues);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_DROPLISTS_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_DROPLISTS_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        Droplist.fromJson(f, droplists);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_ITEMS_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_ITEMS_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        Item.fromJson(f, items);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_ITEMCAT_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_ITEMCAT_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        ItemCategory.fromJson(f, itemCategories);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_NPC_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_NPC_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        NPC.fromJson(f, npcs);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

            if (parent.referencedSourceFiles.get(GAME_QUESTS_ARRAY_NAME + suffix) != null) {
                for (String resource : parent.referencedSourceFiles.get(GAME_QUESTS_ARRAY_NAME + suffix)) {
                    File f = new File(baseFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        Quest.fromJson(f, quests);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

        } else if (parent.type != GameSource.Type.referenced) {
            for (File f : baseFolder.listFiles()) {
                if (f.getName().startsWith("actorconditions_")) {
                    ActorCondition.fromJson(f, actorConditions);
                } else if (f.getName().startsWith("conversationlist_")) {
                    Dialogue.fromJson(f, dialogues);
                } else if (f.getName().startsWith("droplists_")) {
                    Droplist.fromJson(f, droplists);
                } else if (f.getName().startsWith("itemlist_")) {
                    Item.fromJson(f, items);
                } else if (f.getName().startsWith("itemcategories_")) {
                    ItemCategory.fromJson(f, itemCategories);
                } else if (f.getName().startsWith("monsterlist_")) {
                    NPC.fromJson(f, npcs);
                } else if (f.getName().startsWith("questlist")) {
                    Quest.fromJson(f, quests);
                }
            }
        }
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return v.getNonEmptyElements();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return v.getNonEmptyElementAt(arg0);
    }

    @Override
    public int getChildCount() {
        return v.getNonEmptySize();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return v.getNonEmptyIndexOf((ProjectTreeNode) arg0);
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void childrenAdded(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenAdded(path);
    }

    @Override
    public void childrenChanged(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenChanged(path);
    }

    @Override
    public void childrenRemoved(List<ProjectTreeNode> path) {
        if (path.size() == 1 && this.v.getNonEmptySize() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
        for (ProjectTreeNode node : v.getNonEmptyIterable()) {
            node.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "JSON data";
    }


    public void refreshTransients() {

    }

    public ActorCondition getActorCondition(String id) {
        if (actorConditions == null) return null;
        for (ActorCondition gde : actorConditions) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public Dialogue getDialogue(String id) {
        if (dialogues == null) return null;
        for (Dialogue gde : dialogues) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public Droplist getDroplist(String id) {
        if (droplists == null) return null;
        for (Droplist gde : droplists) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public Item getItem(String id) {
        if (items == null) return null;
        for (Item gde : items) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public ItemCategory getItemCategory(String id) {
        if (itemCategories == null) return null;
        for (ItemCategory gde : itemCategories) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public NPC getNPC(String id) {
        if (npcs == null) return null;
        for (NPC gde : npcs) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public NPC getNPCIgnoreCase(String id) {
        if (npcs == null) return null;
        for (NPC gde : npcs) {
            if (id.equalsIgnoreCase(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    public Quest getQuest(String id) {
        if (quests == null) return null;
        for (Quest gde : quests) {
            if (id.equals(gde.id)) {
                return gde;
            }
        }
        return null;
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }


    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getJsonClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getJsonClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getJsonOpenIcon();
    }

    public void addElement(JSONElement node) {
        ProjectTreeNode higherEmptyParent = this;
        while (higherEmptyParent != null) {
            if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
            else break;
        }
        if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
        if (node instanceof ActorCondition) {
            if (actorConditions.isEmpty() && higherEmptyParent == null) higherEmptyParent = actorConditions;
            actorConditions.add((ActorCondition) node);
            node.parent = actorConditions;
        } else if (node instanceof Dialogue) {
            if (dialogues.isEmpty() && higherEmptyParent == null) higherEmptyParent = dialogues;
            dialogues.add((Dialogue) node);
            node.parent = dialogues;
        } else if (node instanceof Droplist) {
            if (droplists.isEmpty() && higherEmptyParent == null) higherEmptyParent = droplists;
            droplists.add((Droplist) node);
            node.parent = droplists;
        } else if (node instanceof Item) {
            if (items.isEmpty() && higherEmptyParent == null) higherEmptyParent = items;
            items.add((Item) node);
            node.parent = items;
        } else if (node instanceof ItemCategory) {
            if (itemCategories.isEmpty() && higherEmptyParent == null) higherEmptyParent = itemCategories;
            itemCategories.add((ItemCategory) node);
            node.parent = itemCategories;
        } else if (node instanceof NPC) {
            if (npcs.isEmpty() && higherEmptyParent == null) higherEmptyParent = npcs;
            npcs.add((NPC) node);
            node.parent = npcs;
        } else if (node instanceof Quest) {
            if (quests.isEmpty() && higherEmptyParent == null) higherEmptyParent = quests;
            quests.add((Quest) node);
            node.parent = quests;
        } else {
            Notification.addError("Cannot add " + node.getDesc() + ". Unknown data type.");
            return;
        }
        if (node.jsonFile != null && parent.type == GameSource.Type.altered) {
            //Altered node.
            node.jsonFile = new File(this.baseFolder, node.jsonFile.getName());
        } else {
            //Created node.
            node.jsonFile = new File(this.baseFolder, node.getProjectFilename());
        }
        if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
        else node.notifyCreated();
    }


    @Override
    public GameDataSet getDataSet() {
        return this;
    }

    @Override
    public Type getDataType() {
        return parent.getDataType();
    }

    @Override
    public boolean isEmpty() {
        return v.isEmpty();
    }

    public JSONElement getGameDataElement(Class<? extends JSONElement> gdeClass, String id) {
        if (gdeClass == ActorCondition.class) {
            return getActorCondition(id);
        }
        if (gdeClass == Dialogue.class) {
            return getDialogue(id);
        }
        if (gdeClass == Droplist.class) {
            return getDroplist(id);
        }
        if (gdeClass == ItemCategory.class) {
            return getItemCategory(id);
        }
        if (gdeClass == Item.class) {
            return getItem(id);
        }
        if (gdeClass == NPC.class) {
            return getNPC(id);
        }
        if (gdeClass == Quest.class) {
            return getQuest(id);
        }
        return null;
    }

    public GameDataCategory<? extends JSONElement> getCategory(Class<? extends JSONElement> gdeClass) {
        if (gdeClass == ActorCondition.class) {
            return actorConditions;
        }
        if (gdeClass == Dialogue.class) {
            return dialogues;
        }
        if (gdeClass == Droplist.class) {
            return droplists;
        }
        if (gdeClass == ItemCategory.class) {
            return itemCategories;
        }
        if (gdeClass == Item.class) {
            return items;
        }
        if (gdeClass == NPC.class) {
            return npcs;
        }
        if (gdeClass == Quest.class) {
            return quests;
        }
        return null;
    }

    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : v.getNonEmptyIterable()) {
            if (node.needsSaving()) return true;
        }
        return false;
    }
}
