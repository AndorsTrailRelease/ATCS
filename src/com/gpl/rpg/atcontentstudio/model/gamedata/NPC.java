package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
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

import static com.gpl.rpg.atcontentstudio.model.gamedata.Common.*;

public class NPC extends JSONElement {

    private static final long serialVersionUID = 1093728879485491933L;

    //Available from init state
    //public String id = null; inherited.
    public String name = null;
    public String icon_id = null;

    //Available from parsed state
    public Integer max_hp = null;
    public Integer max_ap = null;
    public Integer move_cost = null;
    public Integer unique = null;
    public MonsterClass monster_class = null;
    public MovementType movement_type = null;
    public Integer attack_damage_max = null;
    public Integer attack_damage_min = null;
    public String spawngroup_id = null;
    public String faction_id = null;
    public String dialogue_id = null;
    public String droplist_id = null;
    public Integer attack_cost = null;
    public Integer attack_chance = null;
    public Integer critical_skill = null;
    public Double critical_multiplier = null;
    public Integer block_chance = null;
    public Integer damage_resistance = null;
    public HitEffect hit_effect = null;
    public HitReceivedEffect hit_received_effect = null;
    public DeathEffect death_effect = null;

    //Available from linked state
    public Dialogue dialogue = null;
    public Droplist droplist = null;

    public enum MonsterClass {
        humanoid,
        insect,
        demon,
        construct,
        animal,
        giant,
        undead,
        reptile,
        ghost
    }

    public enum MovementType {
        none,
        helpOthers,
        protectSpawn,
        wholeMap
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + name + " (" + id + ")";
    }

    public static String getStaticDesc() {
        return "NPCs";
    }


    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<NPC> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List npcs = (List) parser.parse(reader);
            for (Object obj : npcs) {
                Map npcJson = (Map) obj;
                NPC npc = fromJson(npcJson);
                npc.jsonFile = jsonFile;
                npc.parent = category;
                if (npc.getDataType() == GameSource.Type.created || npc.getDataType() == GameSource.Type.altered) {
                    npc.writable = true;
                }
                category.add(npc);
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
    public static NPC fromJson(String jsonString) throws ParseException {
        Map npcJson = (Map) new JSONParser().parse(jsonString);
        NPC npc = fromJson(npcJson);
        npc.parse(npcJson);
        return npc;
    }

    @SuppressWarnings("rawtypes")
    public static NPC fromJson(Map npcJson) {
        NPC npc = new NPC();
        npc.icon_id = (String) npcJson.get("iconID");
        npc.id = (String) npcJson.get("id");
        npc.name = (String) npcJson.get("name");
        return npc;
    }


    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map npcJson) {

        this.max_hp = JSONElement.getInteger((Number) npcJson.get("maxHP"));
        this.max_ap = JSONElement.getInteger((Number) npcJson.get("maxAP"));
        this.move_cost = JSONElement.getInteger((Number) npcJson.get("moveCost"));
        this.unique = JSONElement.getInteger((Number) npcJson.get("unique"));
        if (npcJson.get("monsterClass") != null)
            this.monster_class = MonsterClass.valueOf((String) npcJson.get("monsterClass"));
        if (npcJson.get("movementAggressionType") != null)
            this.movement_type = MovementType.valueOf((String) npcJson.get("movementAggressionType"));
        if (npcJson.get("attackDamage") != null) {
            this.attack_damage_min = JSONElement.getInteger((Number) (((Map) npcJson.get("attackDamage")).get("min")));
            this.attack_damage_max = JSONElement.getInteger((Number) (((Map) npcJson.get("attackDamage")).get("max")));
        }
        this.spawngroup_id = (String) npcJson.get("spawnGroup");
        this.faction_id = (String) npcJson.get("faction");
        this.dialogue_id = (String) npcJson.get("phraseID");
        this.droplist_id = (String) npcJson.get("droplistID");
        this.attack_cost = JSONElement.getInteger((Number) npcJson.get("attackCost"));
        this.attack_chance = JSONElement.getInteger((Number) npcJson.get("attackChance"));
        this.critical_skill = JSONElement.getInteger((Number) npcJson.get("criticalSkill"));
        //TODO correct game data, to unify format.
//		this.critical_multiplier = JSONElement.getDouble((Number) npcJson.get("criticalMultiplier"));
        if (npcJson.get("criticalMultiplier") != null)
            this.critical_multiplier = JSONElement.getDouble(Double.parseDouble(npcJson.get("criticalMultiplier").toString()));

        this.block_chance = JSONElement.getInteger((Number) npcJson.get("blockChance"));
        this.damage_resistance = JSONElement.getInteger((Number) npcJson.get("damageResistance"));

        Map hitEffect = (Map) npcJson.get("hitEffect");
        if (hitEffect != null) {
            this.hit_effect = parseHitEffect(hitEffect);
        }

        Map hitReceivedEffect = (Map) npcJson.get("hitReceivedEffect");
        if (hitReceivedEffect != null) {
            this.hit_received_effect = parseHitReceivedEffect(hitReceivedEffect);
        }

        Map deathEffect = (Map) npcJson.get("deathEffect");
        if (deathEffect != null) {
            this.death_effect = parseDeathEffect(deathEffect);
        }
    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();
        Project proj = getProject();
        if (proj == null) {
            Notification.addError("Error linking item " + id + ". No parent project found.");
            return;
        }
        linkIcon(proj, this.icon_id, this);
        if (this.dialogue_id != null) this.dialogue = proj.getDialogue(this.dialogue_id);
        if (this.dialogue != null) this.dialogue.addBacklink(this);

        if (this.droplist_id != null) this.droplist = proj.getDroplist(this.droplist_id);
        if (this.droplist != null) this.droplist.addBacklink(this);

        linkEffects(this.hit_effect, proj, this);
        linkEffects(this.hit_received_effect, proj, this);
        linkEffects(this.death_effect, proj, this);
        this.state = State.linked;
    }

    @Override
    public Image getIcon() {
        return getProject().getIcon(icon_id);
    }

    public Image getImage() {
        return getProject().getImage(icon_id);
    }

    @Override
    public GameDataElement clone() {
        NPC clone = new NPC();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.name = this.name;
        clone.icon_id = this.icon_id;
        clone.attack_chance = this.attack_chance;
        clone.attack_cost = this.attack_cost;
        clone.attack_damage_min = this.attack_damage_min;
        clone.attack_damage_max = this.attack_damage_max;
        clone.block_chance = this.block_chance;
        clone.critical_multiplier = this.critical_multiplier;
        clone.critical_skill = this.critical_skill;
        clone.damage_resistance = this.damage_resistance;
        clone.dialogue = this.dialogue;
        if (clone.dialogue != null) {
            clone.dialogue.addBacklink(clone);
        }
        clone.dialogue_id = this.dialogue_id;
        clone.droplist = this.droplist;
        if (clone.droplist != null) {
            clone.droplist.addBacklink(clone);
        }
        clone.droplist_id = this.droplist_id;
        clone.faction_id = this.faction_id;
        if (this.hit_effect != null) {
            clone.hit_effect = new HitEffect();
            copyHitEffectValues(clone.hit_effect, this.hit_effect, clone);
        }
        if (this.hit_received_effect != null) {
            clone.hit_received_effect = new HitReceivedEffect();
            copyHitReceivedEffectValues(clone.hit_received_effect, this.hit_received_effect, clone);
        }
        if (this.death_effect != null) {
            clone.death_effect = new DeathEffect();
            copyDeathEffectValues(clone.death_effect, this.death_effect, clone);
        }
        clone.max_ap = this.max_ap;
        clone.max_hp = this.max_hp;
        clone.monster_class = this.monster_class;
        clone.move_cost = this.move_cost;
        clone.movement_type = this.movement_type;
        clone.spawngroup_id = this.spawngroup_id;
        clone.unique = this.unique;
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (dialogue == oldOne) {
            oldOne.removeBacklink(this);
            this.dialogue = (Dialogue) newOne;
            if (newOne != null) newOne.addBacklink(this);
        } else {
            if (this.droplist == oldOne) {
                oldOne.removeBacklink(this);
                this.droplist = (Droplist) newOne;
                if (newOne != null) newOne.addBacklink(this);
            } else {
                if (this.hit_effect != null) {
                    actorConditionElementChanged(this.hit_effect.conditions_source, oldOne, newOne, this);
                    actorConditionElementChanged(this.hit_effect.conditions_target, oldOne, newOne, this);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map npcJson = new LinkedHashMap();
        npcJson.put("id", this.id);
        if (this.name != null) npcJson.put("name", this.name);
        writeIconToMap(npcJson, this.icon_id);
        if (this.max_hp != null) npcJson.put("maxHP", this.max_hp);
        if (this.max_ap != null) npcJson.put("maxAP", this.max_ap);
        if (this.move_cost != null) npcJson.put("moveCost", this.move_cost);
        if (this.unique != null) npcJson.put("unique", this.unique);
        if (this.monster_class != null) npcJson.put("monsterClass", this.monster_class.toString());
        if (this.movement_type != null) npcJson.put("movementAggressionType", this.movement_type.toString());
        writeMinMaxToMap(npcJson, "attackDamage", this.attack_damage_min, attack_damage_max, 0);
        if (this.spawngroup_id != null) npcJson.put("spawnGroup", this.spawngroup_id);
        if (this.faction_id != null) npcJson.put("faction", this.faction_id);
        if (this.dialogue != null) {
            npcJson.put("phraseID", this.dialogue.id);
        } else if (this.dialogue_id != null) {
            npcJson.put("phraseID", this.dialogue_id);
        }
        if (this.droplist != null) {
            npcJson.put("droplistID", this.droplist.id);
        } else if (this.droplist_id != null) {
            npcJson.put("droplistID", this.droplist_id);
        }
        if (this.attack_cost != null) npcJson.put("attackCost", this.attack_cost);
        if (this.attack_chance != null) npcJson.put("attackChance", this.attack_chance);
        if (this.critical_skill != null) npcJson.put("criticalSkill", this.critical_skill);
        if (this.critical_multiplier != null) npcJson.put("criticalMultiplier", this.critical_multiplier);
        if (this.block_chance != null) npcJson.put("blockChance", this.block_chance);
        if (this.damage_resistance != null) npcJson.put("damageResistance", this.damage_resistance);
        writeHitEffectToMap(npcJson, this.hit_effect, "hitEffect");
        writeHitReceivedEffectToMap(npcJson, this.hit_received_effect, "hitReceivedEffect");
        writeDeathEffectToMap(npcJson, this.death_effect, "deathEffect");
        return npcJson;
    }


    @Override
    public String getProjectFilename() {
        return "monsterlist_" + getProject().name + ".json";
    }

    public int getMonsterExperience() {
        double EXP_FACTOR_DAMAGERESISTANCE = 9;
        double EXP_FACTOR_SCALING = 0.7;

        double attacksPerTurn = Math.floor((double) (max_ap != null ? max_ap : 10.0) / (double) (attack_cost != null ? attack_cost : 10.0));
        double avgDamagePotential = 0;
        if (attack_damage_min != null || attack_damage_max != null) {
            avgDamagePotential = ((double) (attack_damage_min != null ? attack_damage_min : 0) + (double) (attack_damage_max != null ? attack_damage_max : 0)) / 2.0;
        }
        double avgCrit = 0;
        if (critical_skill != null && critical_multiplier != null) {
            avgCrit = (double) (critical_skill / 100.0) * critical_multiplier;
        }
        double avgAttackHP = attacksPerTurn * ((double) (attack_chance != null ? attack_chance : 0) / 100.0) * avgDamagePotential * (1 + avgCrit);
        double avgDefenseHP = ((max_hp != null ? max_hp : 1) * (1 + ((double) (block_chance != null ? block_chance : 0) / 100.0))) +
                (EXP_FACTOR_DAMAGERESISTANCE * (damage_resistance != null ? damage_resistance : 0));
        double attackConditionBonus = 0;
        if (hit_effect != null && hit_effect.conditions_target != null && hit_effect.conditions_target.size() > 0) {
            attackConditionBonus = 50;
        }
        double experience = (((avgAttackHP * 3) + avgDefenseHP) * EXP_FACTOR_SCALING) + attackConditionBonus;

        return new Double(Math.ceil(experience)).intValue();
    }


}
