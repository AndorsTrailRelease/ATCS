package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SpawnArea extends MapObject {

    public int quantity = 1;
    public int respawnSpeed = 10;
    public boolean active = true;
    public boolean ignoreAreas = false;
    public String spawngroup_id;
    public List<NPC> spawnGroup = new ArrayList<NPC>();

    public SpawnArea(tiled.core.MapObject obj) {
        if (obj.getProperties().getProperty("quantity") != null) {
            this.quantity = Integer.parseInt(obj.getProperties().getProperty("quantity"));
        }
        if (obj.getProperties().getProperty("respawnspeed") != null) {
            this.respawnSpeed = Integer.parseInt(obj.getProperties().getProperty("respawnspeed"));
        }
        if (obj.getProperties().getProperty("active") != null) {
            this.active = Boolean.parseBoolean(obj.getProperties().getProperty("active"));
        }
        if (obj.getProperties().getProperty("ignoreAreas") != null) {
            this.ignoreAreas = Boolean.parseBoolean(obj.getProperties().getProperty("ignoreAreas"));
        }
        if (obj.getProperties().getProperty("spawngroup") != null) {
            this.spawngroup_id = obj.getProperties().getProperty("spawngroup");
        } else if (obj.getName() != null) {
            this.spawngroup_id = obj.getName();
        }
    }

    @Override
    public void link() {
        if (spawngroup_id != null) {
            spawnGroup = parentMap.getProject().getSpawnGroup(spawngroup_id);
        } else {
            spawnGroup = new ArrayList<NPC>();
        }
        if (spawnGroup != null) {
            for (NPC npc : spawnGroup) {
                npc.addBacklink(parentMap);
            }
        }
    }

    @Override
    public Image getIcon() {
        if (spawnGroup != null && !spawnGroup.isEmpty()) {
            return spawnGroup.get(0).getIcon();
        }
        return DefaultIcons.getNullifyIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        int replacedIndex = -1;
        for (NPC npc : spawnGroup) {
            if (npc == oldOne) {
                replacedIndex = spawnGroup.indexOf(npc);
            }
        }
        if (replacedIndex >= 0) {
            oldOne.removeBacklink(parentMap);
            spawnGroup.set(replacedIndex, (NPC) newOne);
            if (newOne != null) newOne.addBacklink(parentMap);
        }
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (spawngroup_id != null) {
            tmxObject.getProperties().setProperty("spawngroup", spawngroup_id);
        }
        if (quantity != 1) {
            tmxObject.getProperties().setProperty("quantity", Integer.toString(quantity));
        }
        if (respawnSpeed != 10) {
            tmxObject.getProperties().setProperty("respawnspeed", Integer.toString(respawnSpeed));
        }
        if (!this.active) {
            tmxObject.getProperties().setProperty("active", Boolean.toString(active));
        }
        if (this.ignoreAreas) {
            tmxObject.getProperties().setProperty("ignoreAreas", Boolean.toString(ignoreAreas));
        }
    }
}
