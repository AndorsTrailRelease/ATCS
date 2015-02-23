package com.gpl.rpg.atcontentstudio.model.maps;

import java.util.ArrayList;
import java.util.List;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;

public class MapObjectGroup {

	
	public tiled.core.ObjectGroup tmxGroup;
	public TMXMap parentMap;
	public String name;
	public boolean visible;
	public List<MapObject> mapObjects = new ArrayList<MapObject>();
	
	public MapObjectGroup(tiled.core.ObjectGroup layer, TMXMap map) {
		this.tmxGroup = layer;
		this.name = layer.getName();
		this.visible = layer.isVisible();
		this.parentMap = map;
		for (tiled.core.MapObject obj : layer.getObjectsList()) {
			mapObjects.add(MapObject.buildObject(obj, map));
		}
	}

	public void link() {
		for (MapObject obj : mapObjects) {
			obj.link();
		}
	}

	public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
		for (MapObject object : mapObjects) {
			object.elementChanged(oldOne, newOne);
		}
	}

	public void pushBackToTiledProperties() {
		if (tmxGroup != null) {
			tmxGroup.clear();
		} else {
			tmxGroup = new tiled.core.ObjectGroup();
		}
		tmxGroup.setVisible(visible);
		tmxGroup.setName(name);
		for (MapObject object : mapObjects) {
			tmxGroup.addObject(object.toTmxObject());
		}
	}
}
