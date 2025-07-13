package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;


public class MapChange extends MapObject {

    public String map_id;
    public TMXMap map = null;
    public String place_id;

    public MapChange(tiled.core.MapObject obj) {
        this.map_id = obj.getProperties().getProperty("map");
        this.place_id = obj.getProperties().getProperty("place");
    }

    @Override
    public void link() {
        if (map_id != null) this.map = parentMap.getProject().getMap(map_id);
        if (map != null) {
            map.addBacklink(parentMap);
        }
        //TODO reinstate this if data validation system ever exist.
//		else Notification.addWarn("Incomplete mapchange area \""+name+"\" in map \""+parentMap.id+"\". This is OK if it's an arrival only (no exit through this point).");
    }

    @Override
    public Image getIcon() {
        if (name != null) return DefaultIcons.getTiledIconIcon();
        else return DefaultIcons.getNullifyIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (oldOne == map) {
            oldOne.removeBacklink(parentMap);
            map = (TMXMap) newOne;
            if (newOne != null) newOne.addBacklink(parentMap);
        }
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (map != null) {
            tmxObject.getProperties().setProperty("map", map.id);
        } else if (map_id != null) {
            tmxObject.getProperties().setProperty("map", map_id);
        }
        if (place_id != null) {
            tmxObject.getProperties().setProperty("place", place_id);
        }
    }

}
