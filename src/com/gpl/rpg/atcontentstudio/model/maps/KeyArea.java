package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.Requirement;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;

public class KeyArea extends MapObject {

    public String dialogue_id;
    public Dialogue dialogue = null;
    public Requirement requirement;
    public boolean oldSchoolRequirement;

    public KeyArea(tiled.core.MapObject obj) {
        dialogue_id = obj.getProperties().getProperty("phrase");
        String requireType = obj.getProperties().getProperty("requireType");
        String requireId = obj.getProperties().getProperty("requireId");
        String requireValue = obj.getProperties().getProperty("requireValue");
        String requireNegation = obj.getProperties().getProperty("requireNegation");
        oldSchoolRequirement = false;
        if (requireType == null) {
            String[] fields = obj.getName().split(":");
            if (fields.length == 2) {
                requireType = Requirement.RequirementType.questProgress.toString();
                requireValue = fields[1];
                requireId = fields[0];
                oldSchoolRequirement = true;
            } else if (fields.length == 3) {
                requireValue = fields[2];
                requireType = fields[0];
                requireId = fields[1];
                oldSchoolRequirement = true;
            }
        }
        requirement = new Requirement();
        if (requireType != null) requirement.type = Requirement.RequirementType.valueOf(requireType);
        requirement.required_obj_id = requireId;
        if (requireValue != null) requirement.required_value = Integer.parseInt(requireValue);
        if (requireNegation != null) requirement.negated = Boolean.parseBoolean(requireNegation);
        requirement.state = GameDataElement.State.parsed;

    }

    @Override
    public void link() {
        if (dialogue_id != null) dialogue = parentMap.getProject().getDialogue(dialogue_id);
        if (dialogue != null) {
            dialogue.addBacklink(parentMap);
        }
        requirement.parent = parentMap;
        requirement.link();
    }

    @Override
    public Image getIcon() {
        if (dialogue != null && requirement != null) return DefaultIcons.getKeyIcon();
        return DefaultIcons.getNullifyIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (oldOne == dialogue) {
            oldOne.removeBacklink(parentMap);
            dialogue = (Dialogue) newOne;
            if (newOne != null) newOne.addBacklink(parentMap);
        }
        requirement.elementChanged(oldOne, newOne);
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (dialogue != null) {
            tmxObject.getProperties().setProperty("phrase", dialogue.id);
        } else if (dialogue_id != null) {
            tmxObject.getProperties().setProperty("phrase", dialogue_id);
        }
        if (requirement != null) {
            if (oldSchoolRequirement && Requirement.RequirementType.questProgress.equals(requirement.type) && (requirement.negated == null || !requirement.negated)) {
                tmxObject.setName(requirement.required_obj_id + ":" + ((requirement.required_value == null) ? "" : Integer.toString(requirement.required_value)));
            } else {
                if (requirement.type != null) {
                    tmxObject.getProperties().setProperty("requireType", requirement.type.toString());
                }
                if (requirement.required_obj != null) {
                    tmxObject.getProperties().setProperty("requireId", requirement.required_obj.id);
                } else if (requirement.required_obj_id != null) {
                    tmxObject.getProperties().setProperty("requireId", requirement.required_obj_id);
                }
                if (requirement.required_value != null) {
                    tmxObject.getProperties().setProperty("requireValue", requirement.required_value.toString());
                }
                if (requirement.negated != null) {
                    tmxObject.getProperties().setProperty("requireNegation", Boolean.toString(requirement.negated));
                }
            }
        }
    }

    public void updateNameFromRequirementChange() {
        if (oldSchoolRequirement && Requirement.RequirementType.questProgress.equals(requirement.type) && (requirement.negated == null || !requirement.negated)) {
            name = (requirement.negated != null && requirement.negated) ? "NOT " : "" + requirement.required_obj_id + ":" + ((requirement.required_value == null) ? "" : Integer.toString(
                    requirement.required_value));
        } else if (oldSchoolRequirement) {
            int i = 0;
            String futureName = requirement.type.toString() + "#" + Integer.toString(i);
            while (parentMap.getMapObject(futureName) != null) {
                i++;
                futureName = requirement.type.toString() + "#" + Integer.toString(i);
            }
            this.name = futureName;
        }
    }

}
