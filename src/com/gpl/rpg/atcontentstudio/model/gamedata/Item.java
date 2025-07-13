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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.gpl.rpg.atcontentstudio.model.gamedata.Common.*;

public class Item extends JSONElement {

    private static final long serialVersionUID = -516874303672548638L;

    //Available from init state
    //public String id = null; inherited.
    public String name = null;
    public DisplayType display_type = null;
    public String icon_id = null;

    //Available from parsed state
    public Integer has_manual_price = null;
    public Integer base_market_cost = null;
    public String category_id = null;
    public String description = null;
    public HitEffect hit_effect = null;
    public HitReceivedEffect hit_received_effect = null;
    public DeathEffect kill_effect = null;
    public EquipEffect equip_effect = null;

    //Available from linked state
    public ItemCategory category = null;


    public static class EquipEffect {
        //Available from parsed state
        public Integer damage_boost_min = null;
        public Integer damage_boost_max = null;
        public Integer max_hp_boost = null;
        public Integer max_ap_boost = null;
        public List<ActorConditionEffect> conditions = null;
        public Integer increase_move_cost = null;
        public Integer increase_use_item_cost = null;
        public Integer increase_reequip_cost = null;
        public Integer increase_attack_cost = null;
        public Integer increase_attack_chance = null;
        public Integer increase_critical_skill = null;
        public Integer increase_block_chance = null;
        public Integer increase_damage_resistance = null;
        public Double critical_multiplier = null;
        public Integer damage_modifier = null;
    }


    public static enum DisplayType {
        ordinary,
        quest,
        extraordinary,
        legendary,
        rare
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + name + " (" + id + ")";
    }

    public static String getStaticDesc() {
        return "Items";
    }

    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<Item> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List items = (List) parser.parse(reader);
            for (Object obj : items) {
                Map itemJson = (Map) obj;
                Item item = fromJson(itemJson);
                item.jsonFile = jsonFile;
                item.parent = category;
                if (item.getDataType() == GameSource.Type.created || item.getDataType() == GameSource.Type.altered) {
                    item.writable = true;
                }
                category.add(item);
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
    public static Item fromJson(String jsonString) throws ParseException {
        Map itemJson = (Map) new JSONParser().parse(jsonString);
        Item item = fromJson(itemJson);
        item.parse(itemJson);
        return item;
    }

    @SuppressWarnings("rawtypes")
    public static Item fromJson(Map itemJson) {
        Item item = new Item();
        item.icon_id = (String) itemJson.get("iconID");
        item.id = (String) itemJson.get("id");
        item.name = (String) itemJson.get("name");
        if (itemJson.get("displaytype") != null)
            item.display_type = DisplayType.valueOf((String) itemJson.get("displaytype"));
        return item;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map itemJson) {

        this.has_manual_price = JSONElement.getInteger((Number) itemJson.get("hasManualPrice"));
        this.base_market_cost = JSONElement.getInteger((Number) itemJson.get("baseMarketCost"));
        //TODO change the debug json data....
//		this.category_id = (String) itemJson.get("category");
        if (itemJson.get("category") != null) this.category_id = (String) itemJson.get("category").toString();
        this.description = (String) itemJson.get("description");

        Map equipEffect = (Map) itemJson.get("equipEffect");
        if (equipEffect != null) {
            this.equip_effect = new EquipEffect();
            if (equipEffect.get("increaseAttackDamage") != null) {
                this.equip_effect.damage_boost_min = JSONElement.getInteger((Number) (((Map) equipEffect.get("increaseAttackDamage")).get("min")));
                this.equip_effect.damage_boost_max = JSONElement.getInteger((Number) (((Map) equipEffect.get("increaseAttackDamage")).get("max")));
            }
            this.equip_effect.max_hp_boost = JSONElement.getInteger((Number) equipEffect.get("increaseMaxHP"));
            this.equip_effect.max_ap_boost = JSONElement.getInteger((Number) equipEffect.get("increaseMaxAP"));
            this.equip_effect.increase_move_cost = JSONElement.getInteger((Number) equipEffect.get("increaseMoveCost"));
            this.equip_effect.increase_use_item_cost = JSONElement.getInteger((Number) equipEffect.get("increaseUseItemCost"));
            this.equip_effect.increase_reequip_cost = JSONElement.getInteger((Number) equipEffect.get("increaseReequipCost"));
            this.equip_effect.increase_attack_cost = JSONElement.getInteger((Number) equipEffect.get("increaseAttackCost"));
            this.equip_effect.increase_attack_chance = JSONElement.getInteger((Number) equipEffect.get("increaseAttackChance"));
            this.equip_effect.increase_critical_skill = JSONElement.getInteger((Number) equipEffect.get("increaseCriticalSkill"));
            this.equip_effect.increase_block_chance = JSONElement.getInteger((Number) equipEffect.get("increaseBlockChance"));
            this.equip_effect.increase_damage_resistance = JSONElement.getInteger((Number) equipEffect.get("increaseDamageResistance"));
            //TODO correct game data, to unify format.
//			this.equip_effect.critical_multiplier = JSONElement.getDouble((Number) equipEffect.get("setCriticalMultiplier"));
            if (equipEffect.get("setCriticalMultiplier") != null)
                this.equip_effect.critical_multiplier = JSONElement.getDouble(Double.parseDouble(equipEffect.get("setCriticalMultiplier").toString()));
            this.equip_effect.damage_modifier = JSONElement.getInteger((Number) equipEffect.get("setNonWeaponDamageModifier"));

            List conditionsJson = (List) equipEffect.get("addedConditions");
            if (conditionsJson != null && !conditionsJson.isEmpty()) {
                this.equip_effect.conditions = new ArrayList<>();
                for (Object conditionJsonObj : conditionsJson) {
                    Map conditionJson = (Map) conditionJsonObj;
                    ActorConditionEffect condition = new ActorConditionEffect();
                    condition.condition_id = (String) conditionJson.get("condition");
                    condition.magnitude = JSONElement.getInteger((Number) conditionJson.get("magnitude"));
                    this.equip_effect.conditions.add(condition);
                }
            }
        }


        Map hitEffect = (Map) itemJson.get("hitEffect");
        if (hitEffect != null) {
            this.hit_effect = parseHitEffect(hitEffect);
        }

        Map hitReceivedEffect = (Map) itemJson.get("hitReceivedEffect");
        if (hitReceivedEffect != null) {
            this.hit_received_effect = parseHitReceivedEffect(hitReceivedEffect);
        }

        Map killEffect = (Map) itemJson.get("killEffect");
        if (killEffect == null) {
            killEffect = (Map) itemJson.get("useEffect");
        }
        if (killEffect != null) {
            this.kill_effect = parseDeathEffect(killEffect);
        }
        this.state = State.parsed;
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
        if (this.category_id != null) this.category = proj.getItemCategory(this.category_id);
        if (this.category != null) this.category.addBacklink(this);
        if (this.equip_effect != null && this.equip_effect.conditions != null) {
            linkConditions(this.equip_effect.conditions, proj, this);
        }

        linkEffects(this.hit_effect, proj, this);
        linkEffects(this.hit_received_effect, proj, this);
        linkEffects(this.kill_effect, proj, this);
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
        Item clone = new Item();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.name = this.name;
        clone.icon_id = this.icon_id;
        clone.base_market_cost = this.base_market_cost;
        clone.category = this.category;
        if (clone.category != null) {
            clone.category.addBacklink(clone);
        }
        clone.category_id = this.category_id;
        clone.description = this.description;
        clone.display_type = this.display_type;
        clone.has_manual_price = this.has_manual_price;
        if (this.equip_effect != null) {
            clone.equip_effect = new EquipEffect();
            clone.equip_effect.damage_modifier = this.equip_effect.damage_modifier;
            clone.equip_effect.critical_multiplier = this.equip_effect.critical_multiplier;
            clone.equip_effect.damage_boost_max = this.equip_effect.damage_boost_max;
            clone.equip_effect.damage_boost_min = this.equip_effect.damage_boost_min;
            clone.equip_effect.increase_attack_chance = this.equip_effect.increase_attack_chance;
            clone.equip_effect.increase_attack_cost = this.equip_effect.increase_attack_cost;
            clone.equip_effect.increase_block_chance = this.equip_effect.increase_block_chance;
            clone.equip_effect.increase_critical_skill = this.equip_effect.increase_critical_skill;
            clone.equip_effect.increase_damage_resistance = this.equip_effect.increase_damage_resistance;
            clone.equip_effect.increase_move_cost = this.equip_effect.increase_move_cost;
            clone.equip_effect.increase_reequip_cost = this.equip_effect.increase_reequip_cost;
            clone.equip_effect.increase_use_item_cost = this.equip_effect.increase_use_item_cost;
            clone.equip_effect.max_ap_boost = this.equip_effect.max_ap_boost;
            clone.equip_effect.max_hp_boost = this.equip_effect.max_hp_boost;
            if (this.equip_effect.conditions != null) {
                clone.equip_effect.conditions = new ArrayList<>();
                for (ActorConditionEffect c : this.equip_effect.conditions) {
                    ActorConditionEffect cclone = new ActorConditionEffect();
                    cclone.magnitude = c.magnitude;
                    cclone.condition_id = c.condition_id;
                    cclone.condition = c.condition;
                    if (cclone.condition != null) {
                        cclone.condition.addBacklink(clone);
                    }
                    clone.equip_effect.conditions.add(cclone);
                }
            }
        }
        if (this.hit_effect != null) {
            clone.hit_effect = new HitEffect();
            copyHitEffectValues(clone.hit_effect, this.hit_effect, clone);
        }
        if (this.hit_received_effect != null) {
            clone.hit_received_effect = new HitReceivedEffect();
            copyHitReceivedEffectValues(clone.hit_received_effect, this.hit_received_effect, clone);
        }
        if (this.kill_effect != null) {
            clone.kill_effect = new DeathEffect();
            copyDeathEffectValues(clone.kill_effect, this.kill_effect, clone);
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (this.category == oldOne) {
            oldOne.removeBacklink(this);
            this.category = (ItemCategory) newOne;
            if (newOne != null) newOne.addBacklink(this);
        } else {
            if (this.equip_effect != null) {
                if (this.equip_effect.conditions != null) {
                    actorConditionElementChanged(this.equip_effect.conditions, oldOne, newOne, this);
                }
            }
            if (this.hit_effect != null) {
                actorConditionElementChanged(this.hit_effect.conditions_source, oldOne, newOne, this);
                actorConditionElementChanged(this.hit_effect.conditions_target, oldOne, newOne, this);
            }

            if (this.kill_effect != null) {
                actorConditionElementChanged(this.kill_effect.conditions_source, oldOne, newOne, this);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map itemJson = new LinkedHashMap();
        itemJson.put("id", this.id);
        writeIconToMap(itemJson, this.icon_id);
        if (this.name != null) itemJson.put("name", this.name);
        if (this.display_type != null) itemJson.put("displaytype", this.display_type.toString());

        if (this.has_manual_price != null) itemJson.put("hasManualPrice", this.has_manual_price);
        if (this.base_market_cost != null) itemJson.put("baseMarketCost", this.base_market_cost);
        if (this.category != null) {
            itemJson.put("category", this.category.id);
        } else if (this.category_id != null) {
            itemJson.put("category", this.category_id);
        }
        writeDescriptionToMap(itemJson, this.description);
        if (this.equip_effect != null) {
            Map equipEffectJson = new LinkedHashMap();
            itemJson.put("equipEffect", equipEffectJson);
            writeMinMaxToMap(equipEffectJson, "increaseAttackDamage", this.equip_effect.damage_boost_min, this.equip_effect.damage_boost_max, 0);
            if (this.equip_effect.max_hp_boost != null)
                equipEffectJson.put("increaseMaxHP", this.equip_effect.max_hp_boost);
            if (this.equip_effect.max_ap_boost != null)
                equipEffectJson.put("increaseMaxAP", this.equip_effect.max_ap_boost);
            if (this.equip_effect.increase_move_cost != null)
                equipEffectJson.put("increaseMoveCost", this.equip_effect.increase_move_cost);
            if (this.equip_effect.increase_use_item_cost != null)
                equipEffectJson.put("increaseUseItemCost", this.equip_effect.increase_use_item_cost);
            if (this.equip_effect.increase_reequip_cost != null)
                equipEffectJson.put("increaseReequipCost", this.equip_effect.increase_reequip_cost);
            if (this.equip_effect.increase_attack_cost != null)
                equipEffectJson.put("increaseAttackCost", this.equip_effect.increase_attack_cost);
            if (this.equip_effect.increase_attack_chance != null)
                equipEffectJson.put("increaseAttackChance", this.equip_effect.increase_attack_chance);
            if (this.equip_effect.increase_critical_skill != null)
                equipEffectJson.put("increaseCriticalSkill", this.equip_effect.increase_critical_skill);
            if (this.equip_effect.increase_block_chance != null)
                equipEffectJson.put("increaseBlockChance", this.equip_effect.increase_block_chance);
            if (this.equip_effect.increase_damage_resistance != null)
                equipEffectJson.put("increaseDamageResistance", this.equip_effect.increase_damage_resistance);
            if (this.equip_effect.critical_multiplier != null)
                equipEffectJson.put("setCriticalMultiplier", this.equip_effect.critical_multiplier);
            if (this.equip_effect.damage_modifier != null)
                equipEffectJson.put("setNonWeaponDamageModifier", this.equip_effect.damage_modifier);
            if (this.equip_effect.conditions != null) {
                List conditionsJson = new ArrayList();
                equipEffectJson.put("addedConditions", conditionsJson);
                for (ActorConditionEffect condition : this.equip_effect.conditions) {
                    Map conditionJson = new LinkedHashMap();
                    conditionsJson.add(conditionJson);
                    writeConditionEffectToMap(condition, conditionJson);
                }
            }
        }
        writeHitEffectToMap(itemJson, this.hit_effect, "hitEffect");
        writeHitReceivedEffectToMap(itemJson, this.hit_received_effect, "hitReceivedEffect");

        String key;
        if (this.category != null && this.category.action_type != null && this.category.action_type == ItemCategory.ActionType.equip) {
            key = "killEffect";
        } else if (this.category != null && this.category.action_type != null && this.category.action_type == ItemCategory.ActionType.use) {
            key = "useEffect";
        } else {
            System.out.println("Could not create JSON-Map for Item: Failed to determine if the item should be used or equipped.");
            key = null;
        }
        if (key != null) {
            writeDeathEffectToMap(itemJson, this.kill_effect, key);
        }

        return itemJson;
    }


    @Override
    public String getProjectFilename() {
        return "itemlist_" + getProject().name + ".json";
    }

    public Integer computePrice() {
        int price = 0;
        if (category != null && category.action_type != null) {
            if (category.action_type == ItemCategory.ActionType.use) {
                price += kill_effect == null ? 0 : calculateUseCost();
            } else if (category.action_type == ItemCategory.ActionType.equip) {
                price += equip_effect == null ? 0 : calculateEquipCost(isWeapon());
                price += hit_effect == null ? 0 : calculateHitCost();
                price += kill_effect == null ? 0 : calculateKillCost();
            }
        }
        return Math.max(1, price);
    }

    public int zeroForNull(Integer val) {
        return val == null ? 0 : val;
    }

    public double zeroForNull(Double val) {
        return val == null ? 0 : val;
    }

    public boolean isWeapon() {
        return category != null && category.action_type != null && category.action_type == ItemCategory.ActionType.equip && category.slot != null && category.slot == ItemCategory.InventorySlot.weapon;
    }

    public int calculateUseCost() {
        final float averageHPBoost = (zeroForNull(kill_effect.hp_boost_min) + zeroForNull(kill_effect.hp_boost_max)) / 2.0f;
        if (averageHPBoost == 0) return 0;
        return (int) (0.1 * Math.signum(averageHPBoost) * Math.pow(Math.abs(averageHPBoost), 2) + 3 * averageHPBoost);
    }

    public int calculateEquipCost(boolean isWeapon) {
        final int costBC = (int) (3 * Math.pow(Math.max(0, zeroForNull(equip_effect.increase_block_chance)), 2.5) + 28 * zeroForNull(equip_effect.increase_block_chance));
        final int costAC = (int) (0.4 * Math.pow(Math.max(0, zeroForNull(equip_effect.increase_attack_chance)), 2.5) - 6 * Math.pow(
                Math.abs(Math.min(0, zeroForNull(equip_effect.increase_attack_chance))), 2.7));
        final int costAP = isWeapon ?
                (int) (0.2 * Math.pow(10.0f / zeroForNull(equip_effect.increase_attack_cost), 8) - 25 * zeroForNull(equip_effect.increase_attack_cost))
                : -3125 * zeroForNull(equip_effect.increase_attack_cost);
        final int costDR = 1325 * zeroForNull(equip_effect.increase_damage_resistance);
        final int costDMG_Min = isWeapon ?
                (int) (10 * Math.pow(Math.max(0, zeroForNull(equip_effect.damage_boost_min)), 2.5))
                : (int) (10 * Math.pow(Math.max(0, zeroForNull(equip_effect.damage_boost_min)), 3) + zeroForNull(equip_effect.damage_boost_min) * 80);
        final int costDMG_Max = isWeapon ?
                (int) (2 * Math.pow(Math.max(0, zeroForNull(equip_effect.damage_boost_max)), 2.1))
                : (int) (2 * Math.pow(Math.max(0, zeroForNull(equip_effect.damage_boost_max)), 3) + zeroForNull(equip_effect.damage_boost_max) * 20);
        final int costCS = (int) (2.2 * Math.pow(zeroForNull(equip_effect.increase_critical_skill), 3));
        final int costCM = (int) (50 * Math.pow(Math.max(0, zeroForNull(equip_effect.critical_multiplier)), 2));

        final int costMaxHP = (int) (30 * Math.pow(Math.max(0, zeroForNull(equip_effect.max_hp_boost)), 1.2) + 70 * zeroForNull(equip_effect.max_hp_boost));
        final int costMaxAP = (int) (50 * Math.pow(Math.max(0, zeroForNull(equip_effect.max_ap_boost)), 3) + 750 * zeroForNull(equip_effect.max_ap_boost));
        final int costMovement = (int) (510 * Math.pow(Math.max(0, -zeroForNull(equip_effect.increase_move_cost)), 2.5) - 350 * zeroForNull(equip_effect.increase_move_cost));
        final int costUseItem = (int) (915 * Math.pow(Math.max(0, -zeroForNull(equip_effect.increase_use_item_cost)), 3) - 430 * zeroForNull(equip_effect.increase_use_item_cost));
        final int costReequip = (int) (450 * Math.pow(Math.max(0, -zeroForNull(equip_effect.increase_reequip_cost)), 2) - 250 * zeroForNull(equip_effect.increase_reequip_cost));

        return costBC + costAC + costAP + costDR + costDMG_Min + costDMG_Max + costCS + costCM
                + costMaxHP + costMaxAP
                + costMovement + costUseItem + costReequip;
    }


    public int calculateHitCost() {
        final float averageHPBoost = (zeroForNull(hit_effect.hp_boost_min) + zeroForNull(hit_effect.hp_boost_max)) / 2.0f;
        final float averageAPBoost = (zeroForNull(hit_effect.ap_boost_min) + zeroForNull(hit_effect.ap_boost_max)) / 2.0f;
        if (averageHPBoost == 0 && averageAPBoost == 0) return 0;

        final int costBoostHP = (int) (2770 * Math.pow(Math.max(0, averageHPBoost), 2.5) + 450 * averageHPBoost);
        final int costBoostAP = (int) (3100 * Math.pow(Math.max(0, averageAPBoost), 2.5) + 300 * averageAPBoost);
        return costBoostHP + costBoostAP;
    }

    public int calculateKillCost() {
        final float averageHPBoost = (zeroForNull(kill_effect.hp_boost_min) + zeroForNull(kill_effect.hp_boost_max)) / 2.0f;
        final float averageAPBoost = (zeroForNull(kill_effect.ap_boost_min) + zeroForNull(kill_effect.ap_boost_max)) / 2.0f;
        if (averageHPBoost == 0 && averageAPBoost == 0) return 0;

        final int costBoostHP = (int) (923 * Math.pow(Math.max(0, averageHPBoost), 2.5) + 450 * averageHPBoost);
        final int costBoostAP = (int) (1033 * Math.pow(Math.max(0, averageAPBoost), 2.5) + 300 * averageAPBoost);
        return costBoostHP + costBoostAP;
    }
}
