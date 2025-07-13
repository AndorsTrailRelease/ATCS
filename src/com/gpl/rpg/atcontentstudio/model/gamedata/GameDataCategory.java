package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import com.gpl.rpg.atcontentstudio.model.*;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.json.simple.JSONArray;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class GameDataCategory<E extends JSONElement> extends ArrayList<E> implements ProjectTreeNode, Serializable {

    private static final long serialVersionUID = 5486008219704443733L;

    public GameDataSet parent;
    public String name;

    public GameDataCategory(GameDataSet parent, String name) {
        super();
        this.parent = parent;
        this.name = name;
    }

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
        return indexOf(node);
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
        return Collections.enumeration(this);
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
        for (E node : this) {
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
    public Type getDataType() {
        return parent.getDataType();
    }

    @SuppressWarnings("rawtypes")
    public void save(File jsonFile) {
        if (getDataType() != GameSource.Type.created && getDataType() != GameSource.Type.altered) {
            Notification.addError("Error while trying to write json file " + jsonFile.getAbsolutePath() + " : Game Source type " + getDataType().toString() + " should not be saved.");
            return;
        }
        List<Map> dataToSave = new ArrayList<Map>();
        for (E element : this) {
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
        StringWriter writer = new JsonPrettyWriter();
        try {
            JSONArray.writeJSONString(dataToSave, writer);
        } catch (IOException e) {
            //Impossible with a StringWriter
        }
        String toWrite = writer.toString();
        try {
            FileWriter w = new FileWriter(jsonFile);
            w.write(toWrite);
            w.close();
            for (E element : this) {
                element.state = GameDataElement.State.saved;
            }
            Notification.addSuccess("Json file " + jsonFile.getAbsolutePath() + " saved.");
        } catch (IOException e) {
            Notification.addError("Error while writing json file " + jsonFile.getAbsolutePath() + " : " + e.getMessage());
            e.printStackTrace();
        }

    }


    public List<SaveEvent> attemptSave(boolean checkImpactedCategory, String fileName) {
        List<SaveEvent> events = new ArrayList<SaveEvent>();
        GameDataCategory<? extends JSONElement> impactedCategory = null;
        String impactedFileName = fileName;
        Map<String, Integer> containedIds = new LinkedHashMap<String, Integer>();
        for (JSONElement node : this) {
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
                for (E n : this) {
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

    public boolean remove(E o) {
        int index = getProject().getNodeIndex(o);
        boolean result = super.remove(o);
        getProject().fireElementRemoved(o, index);
        return result;
    }

    @Override
    public boolean needsSaving() {
        for (E node : this) {
            if (node.needsSaving()) return true;
        }
        return false;
    }

}
