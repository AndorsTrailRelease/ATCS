package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.io.JsonSerializable;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Preferences implements Serializable, JsonSerializable {

    private static final long serialVersionUID = 2455802658424031276L;

    public static class OpenEditorState implements Serializable, JsonSerializable {

        private static final long serialVersionUID = -3848639004723719604L;

        public enum TargetType {
            actorCondition,
            dialogue,
            droplist,
            itemCategory,
            item,
            npc,
            quest,
            map,
            spritesheet,
            worldmapSegment,
            writerSketch
        }

        public String projectName;
        public TargetType targetType;
        public String targetId;
        public boolean selected;

        public OpenEditorState() {
        }

        public OpenEditorState(String projectName, TargetType targetType, String targetId, boolean selected) {
            this.projectName = projectName;
            this.targetType = targetType;
            this.targetId = targetId;
            this.selected = selected;
        }

        @Override
        public Map toMap() {
            Map map = new HashMap();
            map.put("projectName", projectName);
            if (targetType != null) {
                map.put("targetType", targetType.name());
            }
            map.put("targetId", targetId);
            map.put("selected", selected);
            return map;
        }

        @Override
        public void fromMap(Map map) {
            if (map == null) return;

            projectName = (String) map.get("projectName");
            Object targetTypeValue = map.get("targetType");
            if (targetTypeValue != null) {
                try {
                    targetType = TargetType.valueOf(targetTypeValue.toString());
                } catch (IllegalArgumentException e) {
                    targetType = null;
                }
            }
            targetId = (String) map.get("targetId");
            Object selectedValue = map.get("selected");
            if (selectedValue instanceof Boolean) {
                selected = (Boolean) selectedValue;
            } else if (selectedValue != null) {
                selected = Boolean.parseBoolean(selectedValue.toString());
            }
        }
    }

    public static class TreeNodeState implements Serializable, JsonSerializable {

        private static final long serialVersionUID = -4131779495438218618L;

        public java.util.List<String> path = new java.util.ArrayList<>();

        public TreeNodeState() {
        }

        public TreeNodeState(java.util.List<String> path) {
            if (path != null) {
                this.path = new java.util.ArrayList<>(path);
            }
        }

        @Override
        public Map toMap() {
            Map map = new HashMap();
            map.put("path", new java.util.ArrayList<>(path));
            return map;
        }

        @Override
        public void fromMap(Map map) {
            path = new java.util.ArrayList<>();
            if (map == null) return;

            java.util.List<String> loadedPath = (java.util.List<String>) map.get("path");
            if (loadedPath != null) {
                path.addAll(loadedPath);
            }
        }
    }

    public Dimension windowSize = null;
    public Point windowLocation = null;
    public Map<String, Integer> splittersPositions = new HashMap<>();
    public java.util.List<OpenEditorState> openEditors = new java.util.ArrayList<>();
    public java.util.List<TreeNodeState> expandedTreeNodes = new java.util.ArrayList<>();
    public TreeNodeState selectedTreeNode = null;

    public Preferences() {

    }

    @Override
    public Map toMap() {
        Map map = new HashMap();

        if(windowSize!= null){
            Map windowSizeMap = new HashMap<>();
            windowSizeMap.put("width", windowSize.width);
            windowSizeMap.put("height", windowSize.height);
            map.put("windowSize", windowSizeMap);
        }

            if(windowLocation != null){
              Map windowLocationMap = new HashMap<>();
              windowLocationMap.put("x", windowLocation.x);
              windowLocationMap.put("y", windowLocation.y);
              map.put("windowLocation", windowLocationMap);
            }

        map.put("splittersPositions", splittersPositions);

                if (!openEditors.isEmpty()) {
                    java.util.List<Map> openEditorsMaps = new java.util.ArrayList<>(openEditors.size());
                    for (OpenEditorState openEditorState : openEditors) {
                        if (openEditorState != null) {
                            openEditorsMaps.add(openEditorState.toMap());
                        }
                    }
                    map.put("openEditors", openEditorsMaps);
                }

        if (!expandedTreeNodes.isEmpty()) {
            java.util.List<Map> expandedTreeNodeMaps = new java.util.ArrayList<>(expandedTreeNodes.size());
            for (TreeNodeState treeNodeState : expandedTreeNodes) {
                if (treeNodeState != null && treeNodeState.path != null && !treeNodeState.path.isEmpty()) {
                    expandedTreeNodeMaps.add(treeNodeState.toMap());
                }
            }
            if (!expandedTreeNodeMaps.isEmpty()) {
                map.put("expandedTreeNodes", expandedTreeNodeMaps);
            }
        }

        if (selectedTreeNode != null && selectedTreeNode.path != null && !selectedTreeNode.path.isEmpty()) {
            map.put("selectedTreeNode", selectedTreeNode.toMap());
        }

        return map;
    }

    @Override
    public void fromMap(Map map) {
        if(map == null) return;

        Map windowSize1 = (Map) map.get("windowSize");
        if(windowSize1 != null){
            windowSize = new Dimension(((Number) windowSize1.get("width")).intValue(), ((Number) windowSize1.get("height")).intValue());
        }

            Map windowLocation1 = (Map) map.get("windowLocation");
            if(windowLocation1 != null){
              windowLocation = new Point(((Number) windowLocation1.get("x")).intValue(), ((Number) windowLocation1.get("y")).intValue());
            }

        Map<String, Number> splitters = (Map<String, Number>) map.get("splittersPositions");
        Map<String, Integer> splittersInt = new HashMap<>();
        if (splitters != null) {
            splittersInt = new HashMap<>(splitters.size());
            for (Map.Entry<String, Number> entry : splitters. entrySet()){
                splittersInt.put(entry.getKey(), entry.getValue().intValue());
            }
        }
        splittersPositions = splittersInt;

        openEditors = new java.util.ArrayList<>();
        java.util.List<Map> openEditorsList = (java.util.List<Map>) map.get("openEditors");
        if (openEditorsList != null) {
            for (Map openEditorMap : openEditorsList) {
                OpenEditorState openEditorState = new OpenEditorState();
                openEditorState.fromMap(openEditorMap);
                if (openEditorState.projectName != null && openEditorState.targetType != null && openEditorState.targetId != null) {
                    openEditors.add(openEditorState);
                }
            }
        }

        expandedTreeNodes = new java.util.ArrayList<>();
        java.util.List<Map> expandedTreeNodeList = (java.util.List<Map>) map.get("expandedTreeNodes");
        if (expandedTreeNodeList != null) {
            for (Map treeNodeMap : expandedTreeNodeList) {
                TreeNodeState treeNodeState = new TreeNodeState();
                treeNodeState.fromMap(treeNodeMap);
                if (treeNodeState.path != null && !treeNodeState.path.isEmpty()) {
                    expandedTreeNodes.add(treeNodeState);
                }
            }
        }

        selectedTreeNode = null;
        Map selectedTreeNodeMap = (Map) map.get("selectedTreeNode");
        if (selectedTreeNodeMap != null) {
            TreeNodeState treeNodeState = new TreeNodeState();
            treeNodeState.fromMap(selectedTreeNodeMap);
            if (treeNodeState.path != null && !treeNodeState.path.isEmpty()) {
                selectedTreeNode = treeNodeState;
            }
        }

    }
}
