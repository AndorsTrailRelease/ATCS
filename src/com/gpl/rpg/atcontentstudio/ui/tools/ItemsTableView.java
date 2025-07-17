package com.gpl.rpg.atcontentstudio.ui.tools;

import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemsTableView extends ElementTableView {

    private static final long serialVersionUID = 1474255176349837609L;

    public ItemsTableView(Project proj) {
        super(new ItemsTableModel(proj), "Compare " + proj.getItemCountIncludingAltered() + " items.", new ImageIcon(DefaultIcons.getItemIcon()));
    }

    private static class ItemsTableModel implements TableModel {

        Project proj;

        public ItemsTableModel(Project proj) {
            this.proj = proj;
        }

        @Override
        public int getRowCount() {
//			return proj.getItemCount() + 1;
            return proj.getItemCountIncludingAltered();
        }

        @Override
        public int getColumnCount() {
            return 43;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Icon"; //Icon
                case 1:
                    return "ID"; //ID
                case 2:
                    return "Name"; //Name
                case 3:
                    return "Folder type"; //Source type (created, altered, source)
                case 4:
                    return "Use type"; //Use type ("none", "use", or equip slot name).
                case 5:
                    return "Category"; //Category id.
                case 6:
                    return "DisplayType"; //Display type (ordinary, rare, extraordinary...)
                case 7:
                    return "Manually set price ?"; //Has manual price
                case 8:
                    return "Price"; //Price
                case 9:
                    return "On use/hit - HP min";
                case 10:
                    return "On use/hit - HP max";
                case 11:
                    return "On use/hit - AP min";
                case 12:
                    return "On use/hit - AP max";
                case 13:
                    return "On use/hit - # conditions";
                case 14:
                    return "On kill - HP min";
                case 15:
                    return "On kill - HP max";
                case 16:
                    return "On kill - AP min";
                case 17:
                    return "On kill - AP max";
                case 18:
                    return "On kill - # conditions";
                case 19:
                    return "On hit recv - HP min";
                case 20:
                    return "On hit recv - HP max";
                case 21:
                    return "On hit recv - AP min";
                case 22:
                    return "On hit recv - AP max";
                case 23:
                    return "On hit recv - # conditions";
                case 24:
                    return "On hit recv - Tgt HP min";
                case 25:
                    return "On hit recv - Tgt HP max";
                case 26:
                    return "On hit recv - Tgt AP min";
                case 27:
                    return "On hit recv - Tgt AP max";
                case 28:
                    return "AD min";
                case 29:
                    return "AD max";
                case 30:
                    return "Damage modifier %";
                case 31:
                    return "Max HP";
                case 32:
                    return "Max AP";
                case 33:
                    return "Attack cost";
                case 34:
                    return "AC";
                case 35:
                    return "BC";
                case 36:
                    return "DR";
                case 37:
                    return "CS";
                case 38:
                    return "CM";
                case 39:
                    return "Move cost";
                case 40:
                    return "Use cost";
                case 41:
                    return "Reequip cost";
                case 42:
                    return "# conditions";
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
//			return String.class;
            switch (columnIndex) {
                case 0:
                    return Icon.class; // Icon
                case 1:
                    return String.class; //ID
                case 2:
                    return String.class; //Name
                case 3:
                    return String.class; //Source type (created, altered, source)
                case 4:
                    return String.class; //Use type ("none", "use", or equip slot name).
                case 5:
                    return String.class; //Category id.
                case 6:
                    return String.class; //Display type (ordinary, rare, extraordinary...)
                case 7:
                    return Boolean.class; //Has manual price
                case 8:
                    return Integer.class; //Price
                case 9:
                    return Integer.class;//"On use/hit - HP min";
                case 10:
                    return Integer.class;//"On use/hit - HP max";
                case 11:
                    return Integer.class;//"On use/hit - AP min";
                case 12:
                    return Integer.class;//"On use/hit - AP max";
                case 13:
                    return Integer.class;//"On use/hit - # conditions";
                case 14:
                    return Integer.class;//"On kill - HP min";
                case 15:
                    return Integer.class;//"On kill - HP max";
                case 16:
                    return Integer.class;//"On kill - AP min";
                case 17:
                    return Integer.class;//"On kill - AP max";
                case 18:
                    return Integer.class;//"On kill - # conditions";
                case 19:
                    return Integer.class;//"On hit recv - HP min";
                case 20:
                    return Integer.class;//"On hit recv - HP max";
                case 21:
                    return Integer.class;//"On hit recv - AP min";
                case 22:
                    return Integer.class;//"On hit recv - AP max";
                case 23:
                    return Integer.class;//"On hit recv - # conditions";
                case 24:
                    return Integer.class;//"On hit recv - Tgt HP min";
                case 25:
                    return Integer.class;//"On hit recv - Tgt HP max";
                case 26:
                    return Integer.class;//"On hit recv - Tgt AP min";
                case 27:
                    return Integer.class;//"On hit recv - Tgt AP max";
                case 28:
                    return Integer.class;//"AD min";
                case 29:
                    return Integer.class;//"AD max";
                case 30:
                    return Integer.class;//"Damage modifier";
                case 31:
                    return Integer.class;//"Max HP";
                case 32:
                    return Integer.class;//"Max AP";
                case 33:
                    return Integer.class;//"Attack cost";
                case 34:
                    return Integer.class;//"AC";
                case 35:
                    return Integer.class;//"BC";
                case 36:
                    return Integer.class;//"DR";
                case 37:
                    return Integer.class;//"CS";
                case 38:
                    return Double.class;//"CM";
                case 39:
                    return Integer.class;//"Move cost";
                case 40:
                    return Integer.class;//"Use cost";
                case 41:
                    return Integer.class;//"Reequip cost";
                case 42:
                    return Integer.class;//"# conditions";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
//			if (rowIndex == 0) {
//				return getColumnName(columnIndex);
//			}
//			Item item = proj.getItem(rowIndex - 1);
            Item item = proj.getItemIncludingAltered(rowIndex);
            boolean canUse = item.category != null && item.category.action_type == ItemCategory.ActionType.use;
            boolean canEquip = item.category != null && item.category.action_type == ItemCategory.ActionType.equip;
            switch (columnIndex) {
                case 0:
                    return new ImageIcon(item.getIcon()); //Icon
                case 1:
                    return item.id; //ID
                case 2:
                    return item.name; //Name
                case 3:
                    return item.getDataType().toString(); //Source type (created, altered, source)
                case 4: //Use type ("none", "use", or equip slot name).
                    if (item.category == null) return "none";
                    if (item.category.action_type == null) return "none";
                    if (item.category.action_type != ItemCategory.ActionType.equip)
                        return item.category.action_type.toString();
                    return item.category.slot.toString();
                case 5:
                    return item.category != null ? item.category.id : (item.category_id != null ? item.category_id : null); //Category id.
                case 6:
                    return item.display_type != null ? item.display_type.toString() : null; //Category id.
                case 7:
                    return item.has_manual_price == null ? false : (item.has_manual_price == 1); //Has manual price
                case 8: //Price
                    if (item.has_manual_price == null || item.has_manual_price != 1) return item.computePrice();
                    return item.base_market_cost;
                case 9:
                    return canUse ? (item.kill_effect != null ? item.kill_effect.hp_boost_min : null) : (item.hit_effect != null ? item.hit_effect.hp_boost_min : null);//"On use/hit - HP min";
                case 10:
                    return canUse ? (item.kill_effect != null ? item.kill_effect.hp_boost_max : null) : (item.hit_effect != null ? item.hit_effect.hp_boost_max : null);//"On use/hit - HP max";
                case 11:
                    return canUse ? (item.kill_effect != null ? item.kill_effect.ap_boost_min : null) : (item.hit_effect != null ? item.hit_effect.ap_boost_min : null);//"On use/hit - AP min";
                case 12:
                    return canUse ? (item.kill_effect != null ? item.kill_effect.ap_boost_max : null) : (item.hit_effect != null ? item.hit_effect.ap_boost_max : null);//"On use/hit - AP max";
                case 13: //"On use/hit - # conditions";
                    if (canUse) {
                        if (item.kill_effect != null && item.kill_effect.conditions_source != null) {
                            return item.kill_effect.conditions_source.size();
                        }
                        return 0;
                    } else if (item.hit_effect != null) {
                        int val = 0;
                        if (item.hit_effect.conditions_source != null) {
                            val += item.hit_effect.conditions_source.size();
                        }
                        if (item.hit_effect.conditions_target != null) {
                            val += item.hit_effect.conditions_target.size();
                        }
                        return val;
                    }
                    return null;
                case 14:
                    return (!canUse && item.kill_effect != null) ? item.kill_effect.hp_boost_min : null;//"On kill - HP min";
                case 15:
                    return (!canUse && item.kill_effect != null) ? item.kill_effect.hp_boost_max : null;//"On kill - HP max";
                case 16:
                    return (!canUse && item.kill_effect != null) ? item.kill_effect.ap_boost_min : null;//"On kill - AP min";
                case 17:
                    return (!canUse && item.kill_effect != null) ? item.kill_effect.ap_boost_max : null;//"On kill - AP max";
                case 18:
                    return (!canUse && item.kill_effect != null && item.kill_effect.conditions_source != null) ? item.kill_effect.conditions_source.size() : null;//"On kill - # conditions";
                case 19:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.hp_boost_min : null;//"On hit recv - HP min";
                case 20:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.hp_boost_max : null;//"On hit recv - HP max";
                case 21:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.ap_boost_min : null;//"On hit recv - AP min";
                case 22:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.ap_boost_max : null;//"On hit recv - AP max";
                case 23:
                    return (!canUse &&
                            item.hit_received_effect != null &&
                            item.hit_received_effect.conditions_source != null) ? item.hit_received_effect.conditions_source.size() : null;//"On hit recv - # conditions";
                case 24:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.hp_boost_min : null;//"On hit recv - Tgt HP min";
                case 25:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.hp_boost_max : null;//"On hit recv - Tgt HP max";
                case 26:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.ap_boost_min : null;//"On hit recv - Tgt AP min";
                case 27:
                    return (!canUse && item.hit_received_effect != null) ? item.hit_received_effect.ap_boost_max : null;//"On hit recv - Tgt AP max";
                case 28:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.damage_boost_min : null;//"AD min";
                case 29:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.damage_boost_max : null;//"AD max";
                case 30:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.damage_modifier : null;//"Damage modifier";
                case 31:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.max_hp_boost : null;//"Max HP";
                case 32:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.max_ap_boost : null;//"Max AP";
                case 33:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_attack_cost : null;//"Attack cost";
                case 34:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_attack_chance : null;//"AC";
                case 35:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_block_chance : null;//"BC";
                case 36:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_damage_resistance : null;//"DR";
                case 37:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_critical_skill : null;//"CS";
                case 38:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.critical_multiplier : null;//"CM";
                case 39:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_move_cost : null;//"Move cost";
                case 40:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_use_item_cost : null;//"Use cost";
                case 41:
                    return (canEquip && item.equip_effect != null) ? item.equip_effect.increase_reequip_cost : null;//"Reequip cost";
                case 42:
                    return (canEquip && item.equip_effect != null && item.equip_effect.conditions != null) ? item.equip_effect.conditions.size() : null;//"# conditions";
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            //not editable.
        }

        List<TableModelListener> listeners = new CopyOnWriteArrayList<TableModelListener>();

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

    }

}
