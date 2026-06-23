package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.Project.ResourceSet;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SavedSlotCollection;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.utils.Profiling;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


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
        long loadStart = Profiling.LOAD ? System.nanoTime() : 0L;
        int filesLoaded = 0;

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
        quests = new GameDataCategory<>(this, Quest.getStaticDesc());

        v.add(actorConditions);
        v.add(dialogues);
        v.add(droplists);
        v.add(items);
        v.add(itemCategories);
        v.add(npcs);
        v.add(quests);

        //Start parsing to populate categories' content.
        if (parent.type == GameSource.Type.source && (parent.parent.sourceSetToUse == ResourceSet.debugData || parent.parent.sourceSetToUse == ResourceSet.gameData)) {
            // This block loads ONLY files that are defined in the res/values/loadresources.xml or loadresources_debug.xml.  Any "extra" files in the source repo are ignored.
            String suffix = (parent.parent.sourceSetToUse == ResourceSet.debugData) ? DEBUG_SUFFIX : "";
            if (Profiling.LOAD) {
                Profiling.printf("Loading %s data files from %s using resource arrays (suffix '%s')...", parent.type, baseFolder.getAbsolutePath(), suffix);
                Profiling.increaseIndent();
            }

            filesLoaded += loadReferencedJson(GAME_AC_ARRAY_NAME + suffix, actorConditions, ActorCondition::fromJson, "actor conditions");
            filesLoaded += loadReferencedJson(GAME_DIALOGUES_ARRAY_NAME + suffix, dialogues, Dialogue::fromJson, "dialogues");
            filesLoaded += loadReferencedJson(GAME_DROPLISTS_ARRAY_NAME + suffix, droplists, Droplist::fromJson, "droplists");
            filesLoaded += loadReferencedJson(GAME_ITEMS_ARRAY_NAME + suffix, items, Item::fromJson, "items");
            filesLoaded += loadReferencedJson(GAME_ITEMCAT_ARRAY_NAME + suffix, itemCategories, ItemCategory::fromJson, "item categories");
            filesLoaded += loadReferencedJson(GAME_NPC_ARRAY_NAME + suffix, npcs, NPC::fromJson, "NPCs");
            filesLoaded += loadReferencedJson(GAME_QUESTS_ARRAY_NAME + suffix, quests, Quest::fromJson, "quests");
            if (Profiling.LOAD) {
                Profiling.decreaseIndent();
                Profiling.printf("Loaded %d data files in %d ms", filesLoaded, Profiling.elapsedMillis(loadStart));
            }

        } else {
            if (Profiling.LOAD) {
                Profiling.printf("Loading %s data files from %s using directory contents...", parent.type, baseFolder.getAbsolutePath());
                Profiling.increaseIndent();
            }
            // This block runs for other gameSource types (altered, created) and for the "source" (repo) type when the selected source is "All Files".
            // It loads EVERYTHING in the directory. Since we don't have the XML-defined resourceKey to determine the category of file, we infer it from the filename.
            List<File> files = new ArrayList<>(Arrays.stream(Objects.requireNonNull(baseFolder.listFiles())).toList());
            files.sort(Comparator.comparing(File::getName));
            long folderStart = Profiling.LOAD ? System.nanoTime() : 0L;
            for (File f : files) {
                if (loadSingleFile(f, actorConditions, "actorconditions_", ActorCondition::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, dialogues, "conversationlist_", Dialogue::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, droplists, "droplists_", Droplist::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, items, "itemlist_", Item::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, itemCategories, "itemcategories_", ItemCategory::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, npcs, "monsterlist_", NPC::fromJson)) filesLoaded++;
                else if (loadSingleFile(f, quests, "questlist", Quest::fromJson)) filesLoaded++;
            }
            if (Profiling.LOAD) {
                Profiling.decreaseIndent();
                Profiling.printf("Loaded %d project data files from %s in %d ms", filesLoaded, baseFolder.getAbsolutePath(), Profiling.elapsedMillis(folderStart));
            }
        }

    }

    /**
     * Loads all JSON files referenced by a resource array in the source project metadata.
     *
     * @param resourceKey the resource-array key to read from {@code referencedSourceFiles}
     * @param category the destination category to populate
     * @param loader callback used to load each discovered file into the category
     * @param label human-readable category label used for profiling output
     * @param <T> the element type handled by the category
     * @return the number of files successfully loaded
     */
    private <T extends JSONElement> int loadReferencedJson(String resourceKey, GameDataCategory<T> category, JsonLoader<T> loader, String label) {
        List<String> resources = parent.referencedSourceFiles.get(resourceKey);
        if (resources == null) return 0;
        long catStart = Profiling.LOAD ? System.nanoTime() : 0L;
        int count = 0;
        for (String resource : resources) {
            File f = new File(baseFolder, resource.replace(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
            if (f.exists()) {
                loader.load(f, category);
                count++;
                if (Profiling.VERBOSE) {
                    Profiling.printf("Loaded %s", f.getName());
                }
            } else {
                Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
            }
        }
        if (Profiling.LOAD) {
            Profiling.printf("Loaded %s: %d files in %d ms", label, count, Profiling.elapsedMillis(catStart));
        }
        return count;
    }

    /**
     * Attempts to load a single file into the provided category when its name matches the expected prefix.
     *
     * @param f file to inspect and possibly load
     * @param category the destination category to populate
     * @param prefix filename prefix that identifies the category
     * @param loader callback used to load the file into the category
     * @param <T> the element type handled by the category
     * @return {@code true} if the file matched the prefix and was loaded, otherwise {@code false}
     */
    private <T extends JSONElement> boolean loadSingleFile(File f, GameDataCategory<T> category, String prefix, JsonLoader<T> loader) {
        if (!f.getName().startsWith(prefix)) return false;
        loader.load(f, category);
        if (Profiling.VERBOSE) {
            Profiling.printf("Loaded %s", f.getName());
        }
        return true;
    }

    /**
     * Functional interface used to adapt the different category-specific JSON load methods.
     *
     * @param <T> the element type handled by the category
     */
    @FunctionalInterface
    private interface JsonLoader<T extends JSONElement> {
        /**
         * Loads the specified file into the supplied category.
         *
         * @param file the JSON file to load
         * @param category the target category
         */
        void load(File file, GameDataCategory<T> category);
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
        return actorConditions.get(id);
    }

    public Dialogue getDialogue(String id) {
        if (dialogues == null) return null;
        return dialogues.get(id);
    }

    public Droplist getDroplist(String id) {
        if (droplists == null) return null;
        return droplists.get(id);
    }

    public Item getItem(String id) {
        if (items == null) return null;
        return items.get(id);
    }

    public ItemCategory getItemCategory(String id) {
        if (itemCategories == null) return null;
        return itemCategories.get(id);
    }

    public NPC getNPC(String id) {
        if (npcs == null) return null;
        return npcs.get(id);
    }

    public NPC getNPCIgnoreCase(String id) {
        if (npcs == null) return null;
        return npcs.getIgnoreCase(id);
    }

    public Quest getQuest(String id) {
        if (quests == null) return null;
        return quests.get(id);
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
