package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.io.JsonSerializable;

import java.awt.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Preferences implements Serializable, JsonSerializable {

    private static final long serialVersionUID = 2455802658424031276L;

    public Dimension windowSize = null;
    public Map<String, Integer> splittersPositions = new HashMap<>();

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

        map.put("splittersPositions", splittersPositions);
        return map;
    }

    @Override
    public void fromMap(Map map) {
        if(map == null) return;

        Map windowSize1 = (Map) map.get("windowSize");
        if(windowSize1 != null){
            windowSize = new Dimension(((Number) windowSize1.get("width")).intValue(), ((Number) windowSize1.get("height")).intValue());
        }

        Map<String, Number> splitters = (Map<String, Number>) map.get("splittersPositions");
        Map<String, Integer> splittersInt = new HashMap<>(splitters.size());
        for (Map.Entry<String, Number> entry : splitters. entrySet()){
            splittersInt.put(entry.getKey(), entry.getValue().intValue());
        }
        splittersPositions = splittersInt;

    }
}
