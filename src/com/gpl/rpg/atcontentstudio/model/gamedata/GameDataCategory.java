package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.*;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class GameDataCategory<E extends JSONElement> implements ProjectTreeNode {
    //region Data
    private final ArrayList<String> keyList = new ArrayList<>();
    private final HashMap<String, E> dataMap = new HashMap<>();

    //endregion


    public GameDataSet parent;
    public String name;

    public GameDataCategory(GameDataSet parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    //region Helpers
    public E get(String key) {
        return dataMap.get(key);
    }

    public E get(int index) {
        String key = keyList.get(index);
        return dataMap.get(key);
    }

    public E getIgnoreCase(String key) {
        for (String k : keyList) {
            if (k.equalsIgnoreCase(key)) {
                return dataMap.get(k);
            }
        }
        return null;
    }

    public E put(String key, E element) {
        if (!dataMap.containsKey(key)) {
            keyList.add(key);
        }
        return dataMap.put(key, element);
    }

    public void add(E quest) {
        String key = quest.id;
        put(key, quest);
    }

    public E remove(String key) {
        if (dataMap.containsKey(key)) {
            keyList.remove(key);
        }
        return dataMap.remove(key);
    }

    public E remove(int index) {
        String key = keyList.get(index);
        keyList.remove(index);
        return dataMap.remove(key);
    }

    public boolean removeGeneric(JSONElement element){
        return remove((E) element);
    }
    public boolean remove(E element) {
        String key = element.id;
        int index = getProject().getNodeIndex(element);
        boolean result = false;
        if (dataMap.containsKey(key)) {
            keyList.remove(key);
            dataMap.remove(key);
            result = true;
        }
        getProject().fireElementRemoved(element, index);
        return result;
    }

    public int size() {
        return dataMap.size();
    }
    public int indexOf(String key) {
        return keyList.indexOf(key);
    }
    public int indexOf(E element) {
        String key = element.id;
        return keyList.indexOf(key);
    }

    public ArrayList<E> toList() {
        ArrayList<E> list = new ArrayList<>();
        for (String key : keyList) {
            list.add(dataMap.get(key));
        }
        return list;
    }

    //endregion

    //region copied implementation of ProjectTreeNode

    @Override
    public TreeNode getChildAt(int childIndex) {
        return get(childIndex);
    }

    @Override
    public int getChildCount() {
        return size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return indexOf((E) node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Enumeration<E> children() {
        return Collections.enumeration(toList());
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
        if (path.size() == 1 && this.getChildCount() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
        for (E node : toList()) {
            node.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + this.name;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this);
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

    @Override
    public GameDataSet getDataSet() {
        return parent.getDataSet();
    }

    @Override
    public GameSource.Type getDataType() {
        return parent.getDataType();
    }

    @Override
    public boolean isEmpty() {
        return dataMap.isEmpty();
    }

    @SuppressWarnings("rawtypes")
    public void save(File jsonFile) {
        if (getDataType() != GameSource.Type.created && getDataType() != GameSource.Type.altered) {
            Notification.addError("Error while trying to write json file " + jsonFile.getAbsolutePath() + " : Game Source type " + getDataType().toString() + " should not be saved.");
            return;
        }
        List<Map> dataToSave = new ArrayList<Map>();
        for (E element : toList()) {
            if (element.jsonFile.equals(jsonFile)) {
                dataToSave.add(element.toJson());
            }
        }
        if (dataToSave.isEmpty() && jsonFile.exists()) {
            if (jsonFile.delete()) {
                Notification.addSuccess("File " + jsonFile.getAbsolutePath() + " deleted.");
            } else {
                Notification.addError("Error deleting file " + jsonFile.getAbsolutePath());
            }

            return;
        }

        String toWrite = FileUtils.toJsonString(dataToSave);
        if(FileUtils.writeStringToFile(toWrite, jsonFile, "JSON file '"+jsonFile.getAbsolutePath()+"'")){
            for (E element : dataMap.values()) {
                element.state = GameDataElement.State.saved;
            }
        }
    }


    public List<SaveEvent> attemptSave(boolean checkImpactedCategory, String fileName) {
        List<SaveEvent> events = new ArrayList<SaveEvent>();
        GameDataCategory<? extends JSONElement> impactedCategory = null;
        String impactedFileName = fileName;
        Map<String, Integer> containedIds = new LinkedHashMap<String, Integer>();
        ArrayList<E> list = toList();
        for (JSONElement node : list) {
            if (node.getDataType() == GameSource.Type.created && getProject().baseContent.gameData.getGameDataElement(node.getClass(), node.id) != null) {
                if (getProject().alteredContent.gameData.getGameDataElement(node.getClass(), node.id) != null) {
                    events.add(new SaveEvent(SaveEvent.Type.moveToAltered, node, true, "Element ID matches one already present in the altered game content. Change this ID before saving."));
                } else {
                    events.add(new SaveEvent(SaveEvent.Type.moveToAltered, node));
                    impactedFileName = getProject().baseContent.gameData.getGameDataElement(node.getClass(), node.id).jsonFile.getName();
                    impactedCategory = getProject().alteredContent.gameData.getCategory(node.getClass());
                }
            } else if (this.getDataType() == GameSource.Type.altered && getProject().baseContent.gameData.getGameDataElement(node.getClass(), node.id) == null) {
                if (getProject().createdContent.gameData.getGameDataElement(node.getClass(), node.id) != null) {
                    events.add(new SaveEvent(SaveEvent.Type.moveToCreated, node, true, "Element ID matches one already present in the created game content. Change this ID before saving."));
                } else {
                    events.add(new SaveEvent(SaveEvent.Type.moveToCreated, node));
                    impactedCategory = getProject().createdContent.gameData.getCategory(node.getClass());
                    impactedFileName = node.getProjectFilename();
                }
            } else if (node.needsSaving()) {
                events.add(new SaveEvent(SaveEvent.Type.alsoSave, node));
            }
            if (containedIds.containsKey(node.id)) {
                containedIds.put(node.id, containedIds.get(node.id) + 1);
            } else {
                containedIds.put(node.id, 1);
            }
        }
        for (String key : containedIds.keySet()) {
            if (containedIds.get(key) > 1) {
                E node = null;
                for (E n : list) {
                    if (key.equals(n.id)) {
                        node = n;
                        break;
                    }
                }
                events.add(new SaveEvent(SaveEvent.Type.alsoSave, node, true,
                                         "There are " + containedIds.get(node.id) + " elements with this ID in this category. Change the conflicting IDs before saving."));
            }
        }
        if (checkImpactedCategory && impactedCategory != null) {
            events.addAll(impactedCategory.attemptSave(false, impactedFileName));
        }
        return events;
    }

    @Override
    public boolean needsSaving() {
        for (E node : dataMap.values()) {
            if (node.needsSaving()) return true;
        }
        return false;
    }

    //endregion

}

