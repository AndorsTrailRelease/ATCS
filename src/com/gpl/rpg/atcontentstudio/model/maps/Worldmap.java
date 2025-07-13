package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
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
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class Worldmap extends ArrayList<WorldmapSegment> implements ProjectTreeNode {

    private static final long serialVersionUID = 4590409256594556179L;

    public static final String DEFAULT_REL_PATH_IN_SOURCE = "res/xml/worldmap.xml";
    public static final String DEFAULT_REL_PATH_IN_PROJECT = "maps" + File.separator + "worldmap.xml";

    public File worldmapFile;
    public GameSource parent;

    public Map<String, Map<String, Point>> segments = new LinkedHashMap<String, Map<String, Point>>();

    public Worldmap(GameSource gameSource) {
        this.parent = gameSource;
        if (getDataType() == Type.source) {
            worldmapFile = new File(parent.baseFolder, DEFAULT_REL_PATH_IN_SOURCE);
        } else {
            worldmapFile = new File(parent.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
        }
        if (worldmapFile.exists()) {
            loadFromFile(worldmapFile);
        }
        if (getDataType() == Type.source) {
            for (WorldmapSegment segment : this) {
                segment.writable = false;
            }
        } else {
            for (WorldmapSegment segment : this) {
                segment.writable = true;
            }
        }
    }

    private void loadFromFile(File file) {
        if (!file.exists()) return;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource insrc = new InputSource(new FileInputStream(file));
            insrc.setSystemId("http://worldmap/");
            insrc.setEncoding("UTF-8");
            doc = builder.parse(insrc);

            Element root = (Element) doc.getElementsByTagName("worldmap").item(0);
            if (root != null) {
                NodeList segmentsList = root.getElementsByTagName("segment");
                if (segmentsList != null) {
                    for (int i = 0; i < segmentsList.getLength(); i++) {
                        Element segmentNode = (Element) segmentsList.item(i);
                        String name = segmentNode.getAttribute("id");
                        add(new WorldmapSegment(this, name, segmentNode));
                    }
                }
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public Enumeration<WorldmapSegment> children() {
        return Collections.enumeration(this);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return get(arg0);
    }

    @Override
    public int getChildCount() {
        return size();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return indexOf(arg0);
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
        if (path.size() == 1 && this.getChildCount() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "Worldmap";
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getMapClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return null;
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getMapClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getMapOpenIcon();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Type getDataType() {
        return parent.getDataType();
    }

    public void save() {

        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlVersion("1.0");
            Element root = doc.createElement("worldmap");
            doc.appendChild(root);

            for (WorldmapSegment segment : this) {
                root.appendChild(segment.toXmlElement(doc));
                segment.state = GameDataElement.State.saved;
            }

            saveDocToFile(doc, worldmapFile);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public WorldmapSegment getWorldmapSegment(String id) {
        for (WorldmapSegment s : this) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        return null;
    }

    public void addSegment(WorldmapSegment node) {
        ProjectTreeNode higherEmptyParent = this;
        while (higherEmptyParent != null) {
            if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
            else break;
        }
        if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
        add(node);
        node.parent = this;
        if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
        else node.notifyCreated();
    }


    public static void saveDocToFile(Document doc, File f) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new FileOutputStream(f));
            Source input = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            transformer.transform(input, output);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : this) {
            if (node.needsSaving()) return true;
        }
        return false;
    }

}
