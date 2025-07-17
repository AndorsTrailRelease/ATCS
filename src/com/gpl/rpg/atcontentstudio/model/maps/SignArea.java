package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;

public class SignArea extends MapObject {

    public Dialogue dialogue = null;

    public SignArea(tiled.core.MapObject obj) {

    }

    @Override
    public void link() {
        if (name != null) dialogue = parentMap.getProject().getDialogue(name);
        if (dialogue != null) {
            dialogue.addBacklink(parentMap);
        }
    }

    @Override
    public Image getIcon() {
        if (dialogue != null) return DefaultIcons.getSignIcon();
        else return DefaultIcons.getNullifyIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (oldOne == dialogue) {
            oldOne.removeBacklink(parentMap);
            dialogue = (Dialogue) newOne;
            if (newOne != null) newOne.addBacklink(parentMap);
        }
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (dialogue != null) {
            tmxObject.setName(dialogue.id);
        }
    }

}
