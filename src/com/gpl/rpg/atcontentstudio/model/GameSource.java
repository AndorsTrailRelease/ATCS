package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.io.JsonSerializable;
import com.gpl.rpg.atcontentstudio.model.Project.ResourceSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class GameSource implements ProjectTreeNode, Serializable, JsonSerializable {

    private static final long serialVersionUID = -1512979360971918158L;

    public static final String DEFAULT_REL_PATH_FOR_GAME_RESOURCE = "res" + File.separator + "values" + File.separator + "loadresources.xml";
    public static final String DEFAULT_REL_PATH_FOR_DEBUG_RESOURCE = "res" + File.separator + "values" + File.separator + "loadresources_debug.xml";

    public transient GameDataSet gameData;
    public transient TMXMapSet gameMaps;
    public transient SpriteSheetSet gameSprites;
    public transient Worldmap worldmap;
    public transient WriterModeDataSet writerModeDataSet;
    private transient SavedSlotCollection v;


    @Override
    public Map toMap() {
        Map map = new HashMap();
        map.put("type", type.toString());
        map.put("baseFolder", baseFolder.getPath());
        return map;
    }

    @Override
    public void fromMap(Map map) {
        if(map==null)return;
        type = Enum.valueOf(Type.class, (String)map.get("type"));
        baseFolder = new File((String) map.get("baseFolder"));
    }

    public static enum Type {
        source,
        referenced,
        altered,
        created
    }

    public File baseFolder;
    public Type type;

    public transient Project parent;

    public transient Map<String, List<String>> referencedSourceFiles = null;

    public GameSource(Map json, Project parent) {
        fromMap(json);
        refreshTransients(parent);
    }
    public GameSource(File folder, Project parent) {
        this.parent = parent;
        this.baseFolder = folder;
        this.type = Type.source;
        initData();
    }

    public GameSource(Project parent, Type type) {
        this.parent = parent;
        this.baseFolder = new File(parent.baseFolder, type.toString());
        this.type = type;
        initData();
    }

    public void refreshTransients(Project p) {
        parent = p;
        initData();
    }

    public void initData() {
        if (type == Type.source) {
            if (parent.sourceSetToUse == ResourceSet.gameData || parent.sourceSetToUse == ResourceSet.debugData) {
                referencedSourceFiles = new LinkedHashMap<String, List<String>>();
                readResourceList();
            }
        }
        if (type == Type.created) {
            this.writerModeDataSet = new WriterModeDataSet(this);
        }
        this.gameData = new GameDataSet(this);
        this.gameMaps = new TMXMapSet(this);
        this.gameSprites = new SpriteSheetSet(this);
        this.worldmap = new Worldmap(this);
        v = new SavedSlotCollection();
        v.add(gameData);
        v.add(gameMaps);
        v.add(gameSprites);
        v.add(worldmap);
        if (type == Type.created) {
            v.add(writerModeDataSet);
        }
    }

    public void readResourceList() {
        File xmlFile;
        if (parent.sourceSetToUse == ResourceSet.gameData) {
            xmlFile = new File(baseFolder, DEFAULT_REL_PATH_FOR_GAME_RESOURCE);
        } else if (parent.sourceSetToUse == ResourceSet.debugData) {
            xmlFile = new File(baseFolder, DEFAULT_REL_PATH_FOR_DEBUG_RESOURCE);
        } else {
            return;
        }

        if (!xmlFile.exists()) return;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource insrc = new InputSource(new FileInputStream(xmlFile));
//	            insrc.setSystemId("http://worldmap/");
            insrc.setEncoding("UTF-8");
            doc = builder.parse(insrc);

            Element root = (Element) doc.getElementsByTagName("resources").item(0);
            if (root != null) {
                NodeList arraysList = root.getElementsByTagName("array");
                if (arraysList != null) {
                    for (int i = 0; i < arraysList.getLength(); i++) {
                        Element arrayNode = (Element) arraysList.item(i);
                        String name = arrayNode.getAttribute("name");
                        List<String> arrayContents = new ArrayList<String>();
                        NodeList arrayItems = arrayNode.getElementsByTagName("item");
                        if (arrayItems != null) {
                            for (int j = 0; j < arrayItems.getLength(); j++) {
                                arrayContents.add(((Element) arrayItems.item(j)).getTextContent());
                            }
                            referencedSourceFiles.put(name, arrayContents);
                        }
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        switch (type) {
            case altered:
                return (needsSaving() ? "*" : "") + "Altered data";
            case created:
                return (needsSaving() ? "*" : "") + "Created data";
            case referenced:
                return (needsSaving() ? "*" : "") + "Referenced data";
            case source:
                return (needsSaving() ? "*" : "") + "AT Source"; //The fact that it is from "source" is already mentionned by its parent.
            default:
                return (needsSaving() ? "*" : "") + "Game data";
        }
    }


    @Override
    public Project getProject() {
        return parent == null ? null : parent.getProject();
    }

    public Image getIcon(String iconId) {
        String[] data = iconId.split(":");
        for (Spritesheet sheet : gameSprites.spritesheets) {
            if (sheet.id.equals(data[0])) {
                return sheet.getIcon(Integer.parseInt(data[1]));
            }
        }
        return null;
    }

    public Image getImage(String iconId) {
        String[] data = iconId.split(":");
        for (Spritesheet sheet : gameSprites.spritesheets) {
            if (sheet.id.equals(data[0])) {
                return sheet.getImage(Integer.parseInt(data[1]));
            }
        }
        return null;
    }


    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getATClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getATClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getATOpenIcon();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }


    @Override
    public Type getDataType() {
        return type;
    }

    @Override
    public boolean isEmpty() {
        return v.isEmpty();
    }

    public WorldmapSegment getWorldmapSegment(String id) {
        return worldmap.getWorldmapSegment(id);
    }

    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : v.getNonEmptyIterable()) {
            if (node.needsSaving()) return true;
        }
        return false;
    }
}
