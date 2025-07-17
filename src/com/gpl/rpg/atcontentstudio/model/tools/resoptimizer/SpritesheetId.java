package com.gpl.rpg.atcontentstudio.model.tools.resoptimizer;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpritesheetId {
    static Map<String, SpritesheetId> instancesCache = new LinkedHashMap<String, SpritesheetId>();

    String tileset;
    int offset;

    static SpritesheetId getInstance(String id) {
        String[] values = id.split(":");
        return getInstance(values[0], Integer.parseInt(values[1]));
    }

    static SpritesheetId getInstance(String tilesetId, int offset) {
        if (!instancesCache.containsKey(toStringID(tilesetId, offset))) {
            SpritesheetId instance = new SpritesheetId(tilesetId, offset);
            instancesCache.put(instance.toStringID(), instance);
        }
        return instancesCache.get(toStringID(tilesetId, offset));
    }

    private SpritesheetId(String tileset, int offset) {
        this.tileset = tileset;
        this.offset = offset;
    }

    public String toStringID() {
        return toStringID(tileset, offset);
    }

    static String toStringID(String tileset, int offset) {
        return tileset + ":" + Integer.toString(offset);
    }

}
