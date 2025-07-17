package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ActorCondition extends JSONElement {

    private static final long serialVersionUID = -3969824899972048507L;

    public static final Integer MAGNITUDE_CLEAR = -99;
    public static final Integer DURATION_FOREVER = 999;
    public static final Integer DURATION_NONE = 0;

    // Available from init state
    //public String id; inherited.
    public String icon_id;
    public String display_name;
    public String description;

    // Available from parsed state
    public ACCategory category = null;
    public Integer positive = null;
    public Integer stacking = null;
    public RoundEffect round_effect = null;
    public RoundEffect full_round_effect = null;
    public AbilityEffect constant_ability_effect = null;

    public enum ACCategory {
        spiritual,
        mental,
        physical,
        blood
    }

    public static enum VisualEffectID {
        redSplash, blueSwirl, greenSplash, miss
    }

    public static class RoundEffect implements Cloneable {
        // Available from parsed state
        public VisualEffectID visual_effect = null;
        public Integer hp_boost_min = null;
        public Integer hp_boost_max = null;
        public Integer ap_boost_min = null;
        public Integer ap_boost_max = null;

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class AbilityEffect implements Cloneable {
        // Available from parsed state
        public Integer max_hp_boost = null;
        public Integer max_ap_boost = null;
        public Integer increase_move_cost = null;
        public Integer increase_use_cost = null;
        public Integer increase_reequip_cost = null;
        public Integer increase_attack_cost = null;
        public Integer increase_attack_chance = null;
        public Integer increase_damage_min = null;
        public Integer increase_damage_max = null;
        public Integer increase_critical_skill = null;
        public Integer increase_block_chance = null;
        public Integer increase_damage_resistance = null;

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + display_name + " (" + id + ")";
    }

    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<ActorCondition> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List actorConditions = (List) parser.parse(reader);
            for (Object obj : actorConditions) {
                Map aCondJson = (Map) obj;
                ActorCondition aCond = fromJson(aCondJson);
                aCond.jsonFile = jsonFile;
                aCond.parent = category;
                if (aCond.getDataType() == GameSource.Type.created || aCond.getDataType() == GameSource.Type.altered) {
                    aCond.writable = true;
                }
                category.add(aCond);
            }
        } catch (FileNotFoundException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @SuppressWarnings("rawtypes")
    public static ActorCondition fromJson(String jsonString) throws ParseException {
        Map aCondJson = (Map) new JSONParser().parse(jsonString);
        ActorCondition aCond = fromJson(aCondJson);
        aCond.parse(aCondJson);
        return aCond;
    }

    @SuppressWarnings("rawtypes")
    public static ActorCondition fromJson(Map aCondJson) {
        ActorCondition aCond = new ActorCondition();
        aCond.icon_id = (String) aCondJson.get("iconID");
        aCond.id = (String) aCondJson.get("id");
        aCond.display_name = (String) aCondJson.get("name");
        return aCond;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map aCondJson) {

        if (aCondJson.get("description") != null) this.description = (String) aCondJson.get("description");
        if (aCondJson.get("category") != null) this.category = ACCategory.valueOf((String) aCondJson.get("category"));
        this.positive = JSONElement.getInteger((Number) aCondJson.get("isPositive"));
        Map abilityEffect = (Map) aCondJson.get("abilityEffect");
        if (abilityEffect != null) {
            this.constant_ability_effect = new AbilityEffect();
            this.constant_ability_effect.increase_attack_chance = JSONElement.getInteger((Number) abilityEffect.get("increaseAttackChance"));
            if (abilityEffect.get("increaseAttackDamage") != null) {
                this.constant_ability_effect.increase_damage_min = JSONElement.getInteger((Number) (((Map) abilityEffect.get("increaseAttackDamage")).get("min")));
                this.constant_ability_effect.increase_damage_max = JSONElement.getInteger((Number) (((Map) abilityEffect.get("increaseAttackDamage")).get("max")));
            }
            this.constant_ability_effect.max_hp_boost = JSONElement.getInteger((Number) abilityEffect.get("increaseMaxHP"));
            this.constant_ability_effect.max_ap_boost = JSONElement.getInteger((Number) abilityEffect.get("increaseMaxAP"));
            this.constant_ability_effect.increase_move_cost = JSONElement.getInteger((Number) abilityEffect.get("increaseMoveCost"));
            this.constant_ability_effect.increase_use_cost = JSONElement.getInteger((Number) abilityEffect.get("increaseUseItemCost"));
            this.constant_ability_effect.increase_reequip_cost = JSONElement.getInteger((Number) abilityEffect.get("increaseReequipCost"));
            this.constant_ability_effect.increase_attack_cost = JSONElement.getInteger((Number) abilityEffect.get("increaseAttackCost"));
            this.constant_ability_effect.increase_critical_skill = JSONElement.getInteger((Number) abilityEffect.get("increaseCriticalSkill"));
            this.constant_ability_effect.increase_block_chance = JSONElement.getInteger((Number) abilityEffect.get("increaseBlockChance"));
            this.constant_ability_effect.increase_damage_resistance = JSONElement.getInteger((Number) abilityEffect.get("increaseDamageResistance"));
        }
        this.stacking = JSONElement.getInteger((Number) aCondJson.get("isStacking"));
        Map roundEffect = (Map) aCondJson.get("roundEffect");
        if (roundEffect != null) {
            this.round_effect = new RoundEffect();
            if (roundEffect.get("increaseCurrentHP") != null) {
                this.round_effect.hp_boost_max = JSONElement.getInteger((Number) (((Map) roundEffect.get("increaseCurrentHP")).get("max")));
                this.round_effect.hp_boost_min = JSONElement.getInteger((Number) (((Map) roundEffect.get("increaseCurrentHP")).get("min")));
            }
            if (roundEffect.get("increaseCurrentAP") != null) {
                this.round_effect.ap_boost_max = JSONElement.getInteger((Number) (((Map) roundEffect.get("increaseCurrentAP")).get("max")));
                this.round_effect.ap_boost_min = JSONElement.getInteger((Number) (((Map) roundEffect.get("increaseCurrentAP")).get("min")));
            }
            String vfx = (String) roundEffect.get("visualEffectID");
            this.round_effect.visual_effect = null;
            if (vfx != null) {
                try {
                    this.round_effect.visual_effect = VisualEffectID.valueOf(vfx);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        Map fullRoundEffect = (Map) aCondJson.get("fullRoundEffect");
        if (fullRoundEffect != null) {
            this.full_round_effect = new RoundEffect();
            if (fullRoundEffect.get("increaseCurrentHP") != null) {
                this.full_round_effect.hp_boost_max = JSONElement.getInteger((Number) (((Map) fullRoundEffect.get("increaseCurrentHP")).get("max")));
                this.full_round_effect.hp_boost_min = JSONElement.getInteger((Number) (((Map) fullRoundEffect.get("increaseCurrentHP")).get("min")));
            }
            if (fullRoundEffect.get("increaseCurrentAP") != null) {
                this.full_round_effect.ap_boost_max = JSONElement.getInteger((Number) (((Map) fullRoundEffect.get("increaseCurrentAP")).get("max")));
                this.full_round_effect.ap_boost_min = JSONElement.getInteger((Number) (((Map) fullRoundEffect.get("increaseCurrentAP")).get("min")));
            }
            String vfx = (String) fullRoundEffect.get("visualEffectID");
            this.full_round_effect.visual_effect = null;
            if (vfx != null) {
                try {
                    this.full_round_effect.visual_effect = VisualEffectID.valueOf(vfx);
                } catch (IllegalArgumentException e) {
                }
            }
        }
        this.state = State.parsed;

    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();
        if (this.icon_id != null) {
            String spritesheetId = this.icon_id.split(":")[0];
            if (getProject().getSpritesheet(spritesheetId) == null) {
                System.out.println("Actor Condition");
                System.out.println(this.id);
                System.out.println("failed to load spritesheet:");
                System.out.println(spritesheetId);
                System.out.println("while creating backlink for icon_id:");
                System.out.println(this.icon_id);
            }
            getProject().getSpritesheet(spritesheetId).addBacklink(this);
        }

        this.state = State.linked;
    }


    public static String getStaticDesc() {
        return "Actor Conditions";
    }


    @Override
    public Image getIcon() {
        return getProject().getIcon(icon_id);
    }

    public Image getImage() {
        return getProject().getImage(icon_id);
    }

    @Override
    public JSONElement clone() {
        ActorCondition clone = new ActorCondition();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.display_name = this.display_name;
        clone.description = this.description;
        clone.icon_id = this.icon_id;
        clone.category = this.category;
        clone.positive = this.positive;
        clone.stacking = this.stacking;
        if (this.round_effect != null) {
            clone.round_effect = (RoundEffect) this.round_effect.clone();
        }
        if (this.constant_ability_effect != null) {
            clone.constant_ability_effect = (AbilityEffect) constant_ability_effect.clone();
        }
        if (this.full_round_effect != null) {
            clone.full_round_effect = (RoundEffect) this.full_round_effect.clone();
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        //Nothing to link to.
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map jsonAC = new LinkedHashMap();
        jsonAC.put("id", this.id);
        if (this.icon_id != null) jsonAC.put("iconID", this.icon_id);
        if (this.display_name != null) jsonAC.put("name", this.display_name);
        if (this.description != null) jsonAC.put("description", this.description);
        if (this.category != null) jsonAC.put("category", this.category.toString());
        if (this.positive != null && this.positive == 1) jsonAC.put("isPositive", this.positive);
        if (this.stacking != null && this.stacking == 1) jsonAC.put("isStacking", this.stacking);
        if (this.round_effect != null) {
            Map jsonRound = new LinkedHashMap();
            if (this.round_effect.visual_effect != null)
                jsonRound.put("visualEffectID", this.round_effect.visual_effect.toString());
            if (this.round_effect.hp_boost_min != null || this.round_effect.hp_boost_max != null) {
                Map jsonHP = new LinkedHashMap();
                if (this.round_effect.hp_boost_min != null) jsonHP.put("min", this.round_effect.hp_boost_min);
                else jsonHP.put("min", 0);
                if (this.round_effect.hp_boost_max != null) jsonHP.put("max", this.round_effect.hp_boost_max);
                else jsonHP.put("max", 0);
                jsonRound.put("increaseCurrentHP", jsonHP);
            }
            if (this.round_effect.ap_boost_min != null || this.round_effect.ap_boost_max != null) {
                Map jsonAP = new LinkedHashMap();
                if (this.round_effect.ap_boost_min != null) jsonAP.put("min", this.round_effect.ap_boost_min);
                else jsonAP.put("min", 0);
                if (this.round_effect.ap_boost_max != null) jsonAP.put("max", this.round_effect.ap_boost_max);
                else jsonAP.put("max", 0);
                jsonRound.put("increaseCurrentAP", jsonAP);
            }
            jsonAC.put("roundEffect", jsonRound);
        }
        if (this.full_round_effect != null) {
            Map jsonFullRound = new LinkedHashMap();
            if (this.full_round_effect.visual_effect != null)
                jsonFullRound.put("visualEffectID", this.full_round_effect.visual_effect.toString());
            if (this.full_round_effect.hp_boost_min != null || this.full_round_effect.hp_boost_max != null) {
                Map jsonHP = new LinkedHashMap();
                if (this.full_round_effect.hp_boost_min != null) jsonHP.put("min", this.full_round_effect.hp_boost_min);
                else jsonHP.put("min", 0);
                if (this.full_round_effect.hp_boost_max != null) jsonHP.put("max", this.full_round_effect.hp_boost_max);
                else jsonHP.put("max", 0);
                jsonFullRound.put("increaseCurrentHP", jsonHP);
            }
            if (this.full_round_effect.ap_boost_min != null || this.full_round_effect.ap_boost_max != null) {
                Map jsonAP = new LinkedHashMap();
                if (this.full_round_effect.ap_boost_min != null) jsonAP.put("min", this.full_round_effect.ap_boost_min);
                else jsonAP.put("min", 0);
                if (this.full_round_effect.ap_boost_max != null) jsonAP.put("max", this.full_round_effect.ap_boost_max);
                else jsonAP.put("max", 0);
                jsonFullRound.put("increaseCurrentAP", jsonAP);
            }
            jsonAC.put("fullRoundEffect", jsonFullRound);
        }
        if (this.constant_ability_effect != null) {
            Map jsonAbility = new LinkedHashMap();
            if (this.constant_ability_effect.increase_attack_chance != null)
                jsonAbility.put("increaseAttackChance", this.constant_ability_effect.increase_attack_chance);
            if (this.constant_ability_effect.increase_damage_min != null || this.constant_ability_effect.increase_damage_max != null) {
                Map jsonAD = new LinkedHashMap();
                if (this.constant_ability_effect.increase_damage_min != null)
                    jsonAD.put("min", this.constant_ability_effect.increase_damage_min);
                else jsonAD.put("min", 0);
                if (this.constant_ability_effect.increase_damage_max != null)
                    jsonAD.put("max", this.constant_ability_effect.increase_damage_max);
                else jsonAD.put("max", 0);
                jsonAbility.put("increaseAttackDamage", jsonAD);
            }
            if (this.constant_ability_effect.max_hp_boost != null)
                jsonAbility.put("increaseMaxHP", this.constant_ability_effect.max_hp_boost);
            if (this.constant_ability_effect.max_ap_boost != null)
                jsonAbility.put("increaseMaxAP", this.constant_ability_effect.max_ap_boost);
            if (this.constant_ability_effect.increase_move_cost != null)
                jsonAbility.put("increaseMoveCost", this.constant_ability_effect.increase_move_cost);
            if (this.constant_ability_effect.increase_use_cost != null)
                jsonAbility.put("increaseUseItemCost", this.constant_ability_effect.increase_use_cost);
            if (this.constant_ability_effect.increase_reequip_cost != null)
                jsonAbility.put("increaseReequipCost", this.constant_ability_effect.increase_reequip_cost);
            if (this.constant_ability_effect.increase_attack_cost != null)
                jsonAbility.put("increaseAttackCost", this.constant_ability_effect.increase_attack_cost);
            if (this.constant_ability_effect.increase_critical_skill != null)
                jsonAbility.put("increaseCriticalSkill", this.constant_ability_effect.increase_critical_skill);
            if (this.constant_ability_effect.increase_block_chance != null)
                jsonAbility.put("increaseBlockChance", this.constant_ability_effect.increase_block_chance);
            if (this.constant_ability_effect.increase_damage_resistance != null)
                jsonAbility.put("increaseDamageResistance", this.constant_ability_effect.increase_damage_resistance);
            jsonAC.put("abilityEffect", jsonAbility);
        }
        return jsonAC;
    }

    @Override
    public String getProjectFilename() {
        return "actorconditions_" + getProject().name + ".json";
    }

}
