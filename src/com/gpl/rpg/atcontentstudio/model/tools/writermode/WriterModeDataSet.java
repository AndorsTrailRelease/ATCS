package com.gpl.rpg.atcontentstudio.model.tools.writermode;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import com.gpl.rpg.atcontentstudio.model.*;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class WriterModeDataSet implements ProjectTreeNode, Serializable {

    private static final long serialVersionUID = 5434504851883441971L;
    public static final String DEFAULT_REL_PATH_IN_PROJECT = "writer.json";


    public GameSource parent;
    public File writerFile;

    public List<WriterModeData> writerModeDataList = new ArrayList<WriterModeData>();

    public WriterModeDataSet(GameSource gameSource) {
        this.parent = gameSource;
        writerFile = new File(parent.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
        parse();
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return writerModeDataList.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return writerModeDataList.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return writerModeDataList.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration children() {
        return Collections.enumeration(writerModeDataList);
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
        path.add(0, this);
        parent.childrenRemoved(path);
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "Dialogue sketches";
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getStdClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getStdOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getStdClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return null;
    }

    @Override
    public Type getDataType() {
        return parent.getDataType();
    }

    @Override
    public boolean isEmpty() {
        return writerModeDataList.isEmpty();
    }


    @SuppressWarnings("rawtypes")
    public void save(File jsonFile) {
        List<Map> dataToSave = new ArrayList<Map>();
        for (WriterModeData data : writerModeDataList) {
            if (data.jsonFile.equals(jsonFile)) {
                dataToSave.add(data.toJson());
            }
        }
        if (dataToSave.isEmpty() && writerFile.exists()) {
            if (writerFile.delete()) {
                Notification.addSuccess("File " + writerFile.getAbsolutePath() + " deleted.");
            } else {
                Notification.addError("Error deleting file " + writerFile.getAbsolutePath());
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
            FileWriter w = new FileWriter(writerFile);
            w.write(toWrite);
            w.close();
            for (WriterModeData element : writerModeDataList) {
                element.state = GameDataElement.State.saved;
            }
            Notification.addSuccess("Json file " + writerFile.getAbsolutePath() + " saved.");
        } catch (IOException e) {
            Notification.addError("Error while writing json file " + writerFile.getAbsolutePath() + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<SaveEvent> attemptSave() {
        List<SaveEvent> events = new ArrayList<SaveEvent>();
        for (WriterModeData data : writerModeDataList) {
            if (data.needsSaving()) {
                events.add(new SaveEvent(SaveEvent.Type.alsoSave, data));
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    public void parse() {
        if (!writerFile.exists()) return;
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(writerFile);
            List writerDataListJson = (List) parser.parse(reader);
            for (Object obj : writerDataListJson) {
                Map jsonObj = (Map) obj;
                WriterModeData data = new WriterModeData(this, jsonObj);
                data.writable = true;
                writerModeDataList.add(data);
            }
        } catch (FileNotFoundException e) {
            Notification.addError("Error while parsing JSON file " + writerFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Notification.addError("Error while parsing JSON file " + writerFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            Notification.addError("Error while parsing JSON file " + writerFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public WriterModeData getWriterSketch(String id) {
        for (WriterModeData sketch : writerModeDataList) {
            if (id.equals(sketch.id)) {
                return sketch;
            }
        }
        return null;
    }

    public WriterModeData get(int index) {
        return writerModeDataList.get(index);
    }

    public void add(WriterModeData node) {
        ProjectTreeNode higherEmptyParent = this;
        while (higherEmptyParent != null) {
            if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
            else break;
        }
        if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
        writerModeDataList.add(node);
        node.writable = true;
        if (node.jsonFile == null) node.jsonFile = this.writerFile;
        node.parent = this;
        if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
        else node.notifyCreated();
    }


    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : writerModeDataList) {
            if (node.needsSaving()) return true;
        }
        return false;
    }

}
