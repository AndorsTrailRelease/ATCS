package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.Droplist;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;

public class ContainerArea extends MapObject {

    public Droplist droplist = null;

    public ContainerArea(tiled.core.MapObject obj) {
    }

    @Override
    public void link() {
        droplist = parentMap.getProject().getDroplist(name);
        if (droplist != null) {
            droplist.addBacklink(parentMap);
        }
    }

    @Override
    public Image getIcon() {
        if (droplist != null) return DefaultIcons.getContainerIcon();
        else return DefaultIcons.getNullifyIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (oldOne == droplist) {
            oldOne.removeBacklink(parentMap);
            droplist = (Droplist) newOne;
            if (newOne != null) newOne.addBacklink(parentMap);
        }
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (droplist != null) {
            tmxObject.setName(droplist.id);
        }
    }

}
