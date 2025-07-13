package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;

import java.awt.*;

public abstract class MapObject {

    public int x, y, w, h;
    public String name;

    public TMXMap parentMap;

    public Types type;

    protected static enum Types {
        mapchange,
        spawn,
        rest,
        key,
        replace,
        script,
        container,
        sign
    }


    public static MapObject buildObject(tiled.core.MapObject obj, TMXMap parentMap) {
        MapObject result = null;
        if (obj.getType() != null && !obj.getType().equals("") && Types.valueOf(obj.getType()) != null) {
            switch (Types.valueOf(obj.getType())) {
                case key:
                    result = new KeyArea(obj);
                    result.type = Types.key;
                    break;
                case mapchange:
                    result = new MapChange(obj);
                    result.type = Types.mapchange;
                    break;
                case replace:
                    result = new ReplaceArea(obj);
                    result.type = Types.replace;
                    break;
                case rest:
                    result = new RestArea(obj);
                    result.type = Types.rest;
                    break;
                case script:
                    result = new ScriptArea(obj);
                    result.type = Types.script;
                    break;
                case sign:
                    result = new SignArea(obj);
                    result.type = Types.sign;
                    break;
                case spawn:
                    result = new SpawnArea(obj);
                    result.type = Types.spawn;
                    break;
                case container:
                    result = new ContainerArea(obj);
                    result.type = Types.container;
                    break;
            }
        } else {
            Notification.addWarn("Unknown map object type: " + obj.getType() + "with name " + obj.getName() + " in map " + parentMap.id);
        }
        if (result != null) {
            result.x = obj.getX();
            result.y = obj.getY();
            result.w = obj.getWidth();
            result.h = obj.getHeight();
            result.name = obj.getName();
            result.parentMap = parentMap;
        }
        return result;
    }

    public abstract void link();

    public abstract Image getIcon();

    public abstract void elementChanged(GameDataElement oldOne, GameDataElement newOne);

    public tiled.core.MapObject toTmxObject() {
        tiled.core.MapObject tmxObject = new tiled.core.MapObject(x, y, w, h);
        tmxObject.setName(name);
        tmxObject.setType(type.toString());
        savePropertiesInTmxObject(tmxObject);
        return tmxObject;
    }

    public abstract void savePropertiesInTmxObject(tiled.core.MapObject tmxObject);

    public static MapObject newMapchange(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Mapchange");
        MapObject result = new MapChange(obj);
        result.type = Types.mapchange;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newSpawnArea(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Spawnarea");
        MapObject result = new SpawnArea(obj);
        result.type = Types.spawn;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newRest(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Rest");
        MapObject result = new RestArea(obj);
        result.type = Types.rest;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newKey(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Key");
        MapObject result = new KeyArea(obj);
        result.type = Types.key;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newReplace(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Replace");
        MapObject result = new ReplaceArea(obj);
        result.type = Types.replace;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newScript(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Script");
        MapObject result = new ScriptArea(obj);
        result.type = Types.script;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newContainer(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Container");
        MapObject result = new ContainerArea(obj);
        result.type = Types.container;
        initObj(result, obj, parentMap);
        return result;
    }

    public static MapObject newSign(tiled.core.MapObject obj, TMXMap parentMap) {
        if (obj.getName() == null) obj.setName("Sign");
        MapObject result = new SignArea(obj);
        result.type = Types.sign;
        initObj(result, obj, parentMap);
        return result;
    }

    private static MapObject initObj(MapObject result, tiled.core.MapObject obj, TMXMap parentMap) {
        result.x = obj.getX();
        result.y = obj.getY();
        result.w = obj.getWidth();
        result.h = obj.getHeight();
        result.name = obj.getName();
        result.parentMap = parentMap;
        return result;
    }


}
