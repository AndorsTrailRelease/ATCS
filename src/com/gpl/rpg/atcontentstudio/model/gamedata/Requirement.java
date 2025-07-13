package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Requirement extends JSONElement {

    private static final long serialVersionUID = 7295593297142310955L;

    private static Map<RequirementType, List<RequirementType>> COMPATIBLE_TYPES = new LinkedHashMap<RequirementType, List<RequirementType>>();

    static {
        List<RequirementType> questTypes = new ArrayList<RequirementType>();
        questTypes.add(RequirementType.questProgress);
        questTypes.add(RequirementType.questLatestProgress);
        COMPATIBLE_TYPES.put(RequirementType.questProgress, questTypes);
        COMPATIBLE_TYPES.put(RequirementType.questLatestProgress, questTypes);

        List<RequirementType> countedItemTypes = new ArrayList<RequirementType>();
        countedItemTypes.add(RequirementType.inventoryRemove);
        countedItemTypes.add(RequirementType.inventoryKeep);
        countedItemTypes.add(RequirementType.usedItem);
        countedItemTypes.add(RequirementType.wear);
        countedItemTypes.add(RequirementType.wearRemove);
        COMPATIBLE_TYPES.put(RequirementType.inventoryRemove, countedItemTypes);
        COMPATIBLE_TYPES.put(RequirementType.inventoryKeep, countedItemTypes);
        COMPATIBLE_TYPES.put(RequirementType.usedItem, countedItemTypes);
        COMPATIBLE_TYPES.put(RequirementType.wear, countedItemTypes);
        COMPATIBLE_TYPES.put(RequirementType.wearRemove, countedItemTypes);
    }

    //Available from parsed state
    public RequirementType type = null;
    public String required_obj_id = null;
    public Integer required_value = null;
    public Boolean negated = null;

    //Available from linked state
    public GameDataElement required_obj = null;

    public enum RequirementType {
        questProgress,
        questLatestProgress,
        inventoryRemove,
        inventoryKeep,
        wear,
        skillLevel,
        killedMonster,
        timerElapsed,
        usedItem,
        spentGold,
        consumedBonemeals,
        hasActorCondition,
        factionScore,
        random,
        factionScoreEquals,
        wearRemove,
        date,
        dateEquals,
        time,
        timeEquals
    }

    public enum SkillID {
        weaponChance, weaponDmg, barter, dodge, barkSkin, moreCriticals, betterCriticals, speed                // Raises max ap
        , coinfinder, moreExp, cleave                // +10ap on kill
        , eater                // +1hp per kill
        , fortitude            // +N hp per levelup
        , evasion            // increase successful flee chance & reduce chance of monster attack
        , regeneration        // +N hp per round
        , lowerExploss, magicfinder, resistanceMental    // lowers chance to get negative active conditions by monsters (Mental like Dazed)
        , resistancePhysical    // lowers chance to get negative active conditions by monsters (Physical Capacity like Minor fatigue)
        , resistanceBlood    // lowers chance to get negative active conditions by monsters (Blood Disorder like Weak Poison)
        , shadowBless, sporeImmunity, crit1            // lowers atk ability
        , crit2            // lowers def ability ,rejuvenation	// Reduces magnitudes of conditions
        , rejuvenation    // Reduces magnitudes of conditions
        , taunt            // Causes AP loss of attackers that miss
        , concussion        // AC loss for monsters with (AC-BC)>N
        , weaponProficiencyDagger, weaponProficiency1hsword, weaponProficiency2hsword, weaponProficiencyAxe, weaponProficiencyBlunt, weaponProficiencyUnarmed, weaponProficiencyPole, armorProficiencyShield, armorProficiencyUnarmored, armorProficiencyLight, armorProficiencyHeavy, fightstyleDualWield, fightstyle2hand, fightstyleWeaponShield, specializationDualWield, specialization2hand, specializationWeaponShield
    }

    @Override
    public String getDesc() {
        String obj_id = "";
        if (required_obj_id != null) {
            obj_id = required_obj_id;
            if (type != null && type == RequirementType.random) {
                obj_id = " Chance " + obj_id + (required_obj_id.contains("/") ? "" : "%");
            } else {
                obj_id += ":";
            }
        }

        return ((negated != null && negated) ? "NOT " : "")
                + (type == null ? "" : type.toString() + ":")
                + obj_id
                + (required_value == null ? "" : required_value.toString());
    }

    @Override
    public void parse() {
        throw new Error("Thou shalt not reach this method.");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map toJson() {
        throw new Error("Thou shalt not reach this method.");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map jsonObj) {
        throw new Error("Thou shalt not reach this method.");
    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();
        Project proj = getProject();
        if (proj == null) {
            Notification.addError("Error linking requirement " + getDesc() + ". No parent project found.");
            return;
        }
        switch (type) {
            case hasActorCondition:
                this.required_obj = proj.getActorCondition(required_obj_id);
                break;
            case inventoryKeep:
            case inventoryRemove:
            case usedItem:
            case wear:
            case wearRemove:
                this.required_obj = proj.getItem(required_obj_id);
                break;
            case killedMonster:
                this.required_obj = proj.getNPC(required_obj_id);
                break;
            case questLatestProgress:
            case questProgress:
                this.required_obj = proj.getQuest(required_obj_id);
                if (this.required_obj != null && this.required_value != null) {
                    QuestStage stage = ((Quest) this.required_obj).getStage(this.required_value);
                    if (stage != null) {
                        stage.addBacklink((GameDataElement) this.parent);
                    }
                }
                break;
            case consumedBonemeals:
            case skillLevel:
            case spentGold:
            case timerElapsed:
            case factionScore:
            case factionScoreEquals:
            case random:
            case date:
            case dateEquals:
            case time:
            case timeEquals:
                break;
        }
        if (this.required_obj != null) this.required_obj.addBacklink((GameDataElement) this.parent);
        this.state = State.linked;
    }

    @Override
    public GameDataElement clone() {
        return clone(null);
    }

    public GameDataElement clone(GameDataElement parent) {
        Requirement clone = new Requirement();
        clone.parent = parent;
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.required_obj_id = this.required_obj_id;
        clone.required_value = this.required_value;
        clone.negated = this.negated;
        clone.required_obj = this.required_obj;
        clone.type = this.type;
        if (clone.required_obj != null && parent != null) {
            clone.required_obj.addBacklink(parent);
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (this.required_obj == oldOne) {
            oldOne.removeBacklink((GameDataElement) this.parent);
            this.required_obj = newOne;
            if (newOne != null) newOne.addBacklink((GameDataElement) this.parent);
        }
        if (oldOne instanceof QuestStage) {
            if (this.required_obj != null && this.required_obj.equals(oldOne.parent) && this.required_value != null && this.required_value.equals(((QuestStage) oldOne).progress)) {
                oldOne.removeBacklink((GameDataElement) this.parent);
                if (newOne != null) newOne.addBacklink((GameDataElement) this.parent);
            }
        }
    }

    @Override
    public String getProjectFilename() {
        throw new Error("Thou shalt not reach this method.");
    }

    public void changeType(RequirementType destType) {
        if (COMPATIBLE_TYPES.get(type) == null || !COMPATIBLE_TYPES.get(type).contains(destType)) {
            required_obj = null;
            required_obj_id = null;
            required_value = null;
        }

        if (destType == RequirementType.random) {
            required_obj_id = "50/100";
        }

        type = destType;
    }

}
