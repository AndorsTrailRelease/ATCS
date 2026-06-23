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
        questProgress("Quest stage has been achieved"),
        questLatestProgress("Quest is now at stage"),
        inventoryRemove("Hero has item in inventory (and remove it)"),
        inventoryKeep("Hero has item in inventory"),
        wear("Hero is wearing item"),
        skillLevel("Skill level is at least"),
        killedMonster("Hero has killed monster(s)"),
        timerElapsed("Timer has elapsed"),
        usedItem("Hero has used item"),
        spentGold("Hero has spent gold"),
        consumedBonemeals("Hero has consumed bonemeal potion(s)"),
        hasActorCondition("Hero has actor condition"),
        factionScore("Faction score is at least"),
        random("Random chance"),
        factionScoreEquals("Faction score is exactly"),
        wearRemove("Hero is wearing item (and remove it from inventory)"),
        date("Date is on or after"),
        dateEquals("Date is exactly"),
        time("Time is at least"),
        timeEquals("Time is exactly"),
        skillIncrease("Skill increase is available");

        private final String description;

        RequirementType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SkillID {
        // Offensive / weapon skills
        weaponChance("Chance to hit with weapons"),
        weaponDmg("Weapon damage bonus"),

        // General / utility skills
        barter("Better prices when trading"),
        dodge("Chance to dodge attacks"),
        barkSkin("Defensive skill (bark skin)") ,
        moreCriticals("Increased chance to score critical hits"),
        betterCriticals("Stronger critical hit damage"),
        speed("Increases action points / initiative"), // Raises max AP

        // Pickup / passive bonuses
        coinfinder("Find more coins on kills"),
        moreExp("Gain more experience from kills"),

        // Special attack / on-kill effects
        cleave("Cleave: damage multiple enemies (+AP on kill)"), // +10ap on kill

        // Sustenance / sustain effects
        eater("Extra HP gained per kill"), // +1hp per kill

        // Health / survivability
        fortitude("Extra HP per level-up"), // +N hp per levelup
        evasion("Increase chance to successfully flee and reduce incoming attacks"), // increase successful flee chance & reduce chance of monster attack
        regeneration("Regenerate HP each round"), // +N hp per round

        // Misc finders / resistances
        lowerExploss("Reduce experience loss on death"),
        magicfinder("Find magical items more often"),
        resistanceMental("Resist mental conditions (e.g. Dazed)"), // lowers chance to get negative active conditions by monsters (Mental like Dazed)
        resistancePhysical("Resist physical conditions (e.g. Minor fatigue)"), // lowers chance to get negative active conditions by monsters (Physical Capacity like Minor fatigue)
        resistanceBlood("Resist blood disorders (e.g. Weak Poison)"), // lowers chance to get negative active conditions by monsters (Blood Disorder like Weak Poison)

        // Blessings / immunities / crit modifiers
        shadowBless("Shadow blessing (flavor/defensive)") ,
        sporeImmunity("Immunity to spore effects"),
        crit1("Lower attack ability modifier (crit tier 1)"), // lowers atk ability
        crit2("Lower defense ability modifier (crit tier 2)"), // lowers def ability
        rejuvenation("Reduce magnitudes of active conditions"), // Reduces magnitudes of conditions

        // Crowd-control / disruption
        taunt("Taunt: causes AP loss for attackers that miss"), // Causes AP loss of attackers that miss
        concussion("Concussion: reduces AC for certain monsters"), // AC loss for monsters with (AC-BC)>N

        // Weapon/armor proficiencies and fighting styles
        weaponProficiencyDagger("Dagger proficiency"),
        weaponProficiency1hsword("1H sword proficiency"),
        weaponProficiency2hsword("2H sword proficiency"),
        weaponProficiencyAxe("Axe proficiency"),
        weaponProficiencyBlunt("Blunt weapon proficiency"),
        weaponProficiencyUnarmed("Unarmed proficiency"),
        weaponProficiencyPole("Polearm proficiency"),
        armorProficiencyShield("Shield proficiency"),
        armorProficiencyUnarmored("Unarmored proficiency"),
        armorProficiencyLight("Light armor proficiency"),
        armorProficiencyHeavy("Heavy armor proficiency"),
        fightstyleDualWield("Dual-wield fighting style"),
        fightstyle2hand("Two-handed fighting style"),
        fightstyleWeaponShield("Weapon and shield fighting style"),
        specializationDualWield("Specialization: dual wield"),
        specialization2hand("Specialization: two-handed"),
        specializationWeaponShield("Specialization: weapon+shield");

        private final String description;

        SkillID(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
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
            Notification.addError("Error linking requirement '%s' from %s.  No parent project found.".formatted(getDesc(), jsonFile));
            return;
        }

        if(type == null) {
            Notification.addError("Error linking requirement '%s' in element '%s' from %s.  Requirement type is null.".formatted(getDesc(), ((GameDataElement) parent).id, jsonFile));
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
            case skillIncrease:
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
