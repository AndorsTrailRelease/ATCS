package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.Requirement;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ReplaceArea extends MapObject {

    public Requirement requirement;
    public boolean oldSchoolRequirement = false;

    public List<ReplaceArea.Replacement> replacements = null;

    public ReplaceArea(tiled.core.MapObject obj) {
        String requireType = obj.getProperties().getProperty("requireType");
        String requireId = obj.getProperties().getProperty("requireId");
        String requireValue = obj.getProperties().getProperty("requireValue");
        String requireNegation = obj.getProperties().getProperty("requireNegation");
        if (requireType == null) {
            String[] fields = obj.getName().split(":");
            if (fields.length == 2) {
                requireType = Requirement.RequirementType.questProgress.toString();
                requireValue = fields[1];
                requireId = fields[0];
                oldSchoolRequirement = true;
            } /*else if (fields.length == 3) {
				requireValue = fields[2];
				requireType = fields[0];
				requireId = fields[1];
			}*/
        }
        requirement = new Requirement();
        if (requireType != null) requirement.type = Requirement.RequirementType.valueOf(requireType);
        requirement.required_obj_id = requireId;
        if (requireValue != null) requirement.required_value = Integer.parseInt(requireValue);
        if (requireNegation != null) requirement.negated = Boolean.parseBoolean(requireNegation);
        requirement.state = GameDataElement.State.parsed;


        for (Object s : obj.getProperties().keySet()) {
            if (!TMXMap.isPaintedLayerName(s.toString())) continue;
            if (replacements == null) replacements = new ArrayList<ReplaceArea.Replacement>();
            replacements.add(new Replacement(s.toString(), obj.getProperties().getProperty(s.toString())));
        }

    }

    @Override
    public void link() {
        requirement.parent = parentMap;
        requirement.link();
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getReplaceIcon();
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        requirement.elementChanged(oldOne, newOne);
    }

    public ReplaceArea.Replacement createReplacement(String source, String target) {
        Replacement repl = new Replacement(source, target);
        return repl;
    }

    @Override
    public void savePropertiesInTmxObject(tiled.core.MapObject tmxObject) {
        if (replacements != null) {
            for (Replacement r : replacements)
                tmxObject.getProperties().setProperty(r.sourceLayer, r.targetLayer);
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

    //Don't use yet !
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

    public class Replacement {
        public String sourceLayer, targetLayer;

        public Replacement(String source, String target) {
            this.sourceLayer = source;
            this.targetLayer = target;
        }
    }

    public boolean hasReplacementFor(String name) {
        if (name == null) return false;
        for (Replacement repl : replacements) {
            if (name.equalsIgnoreCase(repl.sourceLayer)) return true;
        }
        return false;
    }
}
