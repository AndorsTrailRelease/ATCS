package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorldmapSegment extends GameDataElement {

    private static final long serialVersionUID = 2658610076889592723L;

    public static final String TEMP_LABEL_KEY = "ATCS_INTERNAL_TEMPORARY_KEY_FOR_LABEL";

    public int segmentX;
    public int segmentY;
    public Map<String, Point> mapLocations = new LinkedHashMap<String, Point>();
    public Map<String, List<String>> labelledMaps = new LinkedHashMap<String, List<String>>();
    public Map<String, NamedArea> labels = new LinkedHashMap<String, NamedArea>();
    public Element xmlNode;

    public WorldmapSegment(Worldmap parent, String name, Element xmlNode) {
        this.parent = parent;
        this.id = name;
        this.xmlNode = xmlNode;
    }

    @Override
    public GameDataSet getDataSet() {
        return parent.getDataSet();
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + id;
    }

    @Override
    public void parse() {
        segmentX = Integer.parseInt(xmlNode.getAttribute("x"));
        segmentY = Integer.parseInt(xmlNode.getAttribute("y"));
        NodeList mapsList = xmlNode.getElementsByTagName("map");
        for (int j = 0; j < mapsList.getLength(); j++) {
            Element mapNode = (Element) mapsList.item(j);
            mapLocations.put(mapNode.getAttribute("id"), new Point(Integer.parseInt(mapNode.getAttribute("x")) - segmentX, Integer.parseInt(mapNode.getAttribute("y")) - segmentY));
            String area;
            if ((area = mapNode.getAttribute("area")) != null && !"".equals(area)) {
                if (labelledMaps.get(area) == null) {
                    labelledMaps.put(area, new ArrayList<String>());
                }
                labelledMaps.get(area).add(mapNode.getAttribute("id"));
            }
        }
        NodeList namedAreasNodeList = xmlNode.getElementsByTagName("namedarea");
        for (int j = 0; j < namedAreasNodeList.getLength(); j++) {
            Element namedAreaNode = (Element) namedAreasNodeList.item(j);
            labels.put(namedAreaNode.getAttribute("id"), new NamedArea(namedAreaNode.getAttribute("id"), namedAreaNode.getAttribute("name"), namedAreaNode.getAttribute("type")));
        }
        this.state = State.parsed;
    }

    @Override
    public void link() {
        if (this.state == State.init) {
            this.parse();
        } else if (this.state == State.linked) {
            return;
        }
        for (String mapName : mapLocations.keySet()) {
            if (getProject().getMap(mapName) != null) {
                getProject().getMap(mapName).addBacklink(this);
            }
        }
    }

    @Override
    public WorldmapSegment clone() {
        WorldmapSegment clone = new WorldmapSegment((Worldmap) parent, id, (Element) xmlNode.cloneNode(true));

        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        boolean modified = false;
        if (newOne == null && writable) {
            //A referenced map may have been deleted.
            if (mapLocations.containsKey(oldOne.id)) {
                mapLocations.remove(oldOne.id);
                modified = true;
            }
            List<String> deprecatedLabels = new ArrayList<String>();
            for (String label : labelledMaps.keySet()) {
                if (labelledMaps.get(label).contains(oldOne.id)) {
                    labelledMaps.get(label).remove(oldOne.id);
                    modified = true;
                    if (labelledMaps.get(label).isEmpty()) {
                        deprecatedLabels.add(label);
                    }
                }
            }
            for (String label : deprecatedLabels) {
                labelledMaps.remove(label);
                labels.remove(label);
            }
        }

        oldOne.removeBacklink(this);
        if (newOne != null) newOne.addBacklink(this);

        if (modified) {
            this.state = GameDataElement.State.modified;
            childrenChanged(new ArrayList<ProjectTreeNode>());
            ATContentStudio.frame.editorChanged(this);
        }
    }

    @Override
    public String getProjectFilename() {
        return "worldmap.xml";
    }

    @Override
    public void save() {
        ((Worldmap) parent).save();
    }

    public String toXml() {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.setXmlVersion("1.0");
            Element root = doc.createElement("worldmap");
            doc.appendChild(root);
            root.appendChild(this.toXmlElement(doc));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result output = new StreamResult(baos);
            Source input = new DOMSource(doc);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
            transformer.transform(input, output);
            return baos.toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;

    }

    public Element toXmlElement(Document doc) {
        Element element = doc.createElement("segment");
        element.setAttribute("id", id);
        element.setAttribute("x", Integer.toString(segmentX));
        element.setAttribute("y", Integer.toString(segmentY));

        for (String s : mapLocations.keySet()) {
            Element map = doc.createElement("map");
            map.setAttribute("id", s);
            map.setAttribute("x", Integer.toString(mapLocations.get(s).x + segmentX));
            map.setAttribute("y", Integer.toString(mapLocations.get(s).y + segmentY));
            for (String label : labelledMaps.keySet()) {
                if (TEMP_LABEL_KEY.equals(label)) continue;
                if (labelledMaps.get(label).contains(s)) {
                    map.setAttribute("area", label);
                }
            }
            element.appendChild(map);
        }

        for (String key : labels.keySet()) {
            if (TEMP_LABEL_KEY.equals(key)) continue;
            NamedArea area = labels.get(key);
            Element namedArea = doc.createElement("namedarea");
            namedArea.setAttribute("id", area.id);
            namedArea.setAttribute("name", area.name);
            namedArea.setAttribute("type", area.type);
            element.appendChild(namedArea);
        }

        return element;
    }


    @Override
    public List<SaveEvent> attemptSave() {
        // TODO Auto-generated method stub
        save();
        return null;
    }

    public static class NamedArea {
        public String id;
        public String name;
        public String type;

        public NamedArea(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getUIMapIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getUIMapIcon();
    }

}
