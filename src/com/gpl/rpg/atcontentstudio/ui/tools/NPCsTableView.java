package com.gpl.rpg.atcontentstudio.ui.tools;

import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NPCsTableView extends ElementTableView {

    private static final long serialVersionUID = -4196852140899079621L;

    public NPCsTableView(Project proj) {
        super(new NPCsTableModel(proj), "Compare " + proj.getNPCCountIncludingAltered() + " NPCs.", new ImageIcon(DefaultIcons.getNPCIcon()));
    }

    private static class NPCsTableModel implements TableModel {

        Project proj;

        public NPCsTableModel(Project proj) {
            this.proj = proj;
        }

        @Override
        public int getRowCount() {
            return proj.getNPCCountIncludingAltered();
        }

        @Override
        public int getColumnCount() {
            return 25;
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
                    return "Category type"; //Source type (created, altered, source)
                case 4:
                    return "Unique";
                case 5:
                    return "Class";
                case 6:
                    return "Movement type";
                case 7:
                    return "Spawngroup";
                case 8:
                    return "Faction";
                case 9:
                    return "HP";
                case 10:
                    return "AP";
                case 11:
                    return "Attack Cost";
                case 12:
                    return "AC";
                case 13:
                    return "BC";
                case 14:
                    return "AD min";
                case 15:
                    return "AD max";
                case 16:
                    return "DR";
                case 17:
                    return "CS";
                case 18:
                    return "CM";
                case 19:
                    return "On hit - HP min";
                case 20:
                    return "On hit - HP max";
                case 21:
                    return "On hit - AP min";
                case 22:
                    return "On hit - AP max";
                case 23:
                    return "On hit - # conditions";
                case 24:
                    return "Experience reward";
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Icon.class; //Icon
                case 1:
                    return String.class; //ID
                case 2:
                    return String.class; //Name
                case 3:
                    return String.class; //Source type (created, altered, source)
                case 4:
                    return Boolean.class; //"Unique";
                case 5:
                    return String.class; //"Class";
                case 6:
                    return String.class; //"Movement type";
                case 7:
                    return String.class; //"Spawngroup";
                case 8:
                    return String.class; //"Faction";
                case 9:
                    return Integer.class; //"HP";
                case 10:
                    return Integer.class; //"AP";
                case 11:
                    return Integer.class; //"Attack Cost";
                case 12:
                    return Integer.class; //"AC";
                case 13:
                    return Integer.class; //"BC";
                case 14:
                    return Integer.class; //"AD min";
                case 15:
                    return Integer.class; //"AD max";
                case 16:
                    return Integer.class; //"DR";
                case 17:
                    return Integer.class; //"CS";
                case 18:
                    return Double.class; //"CM";
                case 19:
                    return Integer.class; //"On hit - HP min";
                case 20:
                    return Integer.class; //"On hit - HP max";
                case 21:
                    return Integer.class; //"On hit - AP min";
                case 22:
                    return Integer.class; //"On hit - AP max";
                case 23:
                    return Integer.class; //"On hit - # conditions";
                case 24:
                    return Integer.class; //"Experience reward";
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            NPC npc = proj.getNPCIncludingAltered(rowIndex);
            switch (columnIndex) {
                case 0:
                    return new ImageIcon(npc.getIcon()); // Icon
                case 1:
                    return npc.id; //ID
                case 2:
                    return npc.name; //Name
                case 3:
                    return npc.getDataType().toString(); //Source type (created, altered, source)
                case 4:
                    return npc.unique != null && npc.unique == 1;//"Unique";
                case 5:
                    return npc.monster_class != null ? npc.monster_class.toString() : null; //"Class";
                case 6:
                    return npc.movement_type != null ? npc.movement_type.toString() : null; //"Movement type";
                case 7:
                    return npc.spawngroup_id; //"Spawngroup";
                case 8:
                    return npc.faction_id; //"Faction";
                case 9:
                    return npc.max_hp; //"HP";
                case 10:
                    return npc.max_ap; //"AP";
                case 11:
                    return npc.attack_cost; //"Attack Cost";
                case 12:
                    return npc.attack_chance; //"AC";
                case 13:
                    return npc.block_chance; //"BC";
                case 14:
                    return npc.attack_damage_min; //"AD min";
                case 15:
                    return npc.attack_damage_max; //"AD max";
                case 16:
                    return npc.damage_resistance; //"DR";
                case 17:
                    return npc.critical_skill; //"CS";
                case 18:
                    return npc.critical_multiplier; //"CM";
                case 19:
                    return npc.hit_effect != null ? npc.hit_effect.hp_boost_min : null; //"On hit - HP min";
                case 20:
                    return npc.hit_effect != null ? npc.hit_effect.hp_boost_max : null; //"On hit - HP max";
                case 21:
                    return npc.hit_effect != null ? npc.hit_effect.ap_boost_min : null; //"On hit - AP min";
                case 22:
                    return npc.hit_effect != null ? npc.hit_effect.ap_boost_max : null; //"On hit - AP max";
                case 23: //"On hit - # conditions";
                    if (npc.hit_effect != null) {
                        Integer val = null;
                        if (npc.hit_effect.conditions_source != null) {
                            val = npc.hit_effect.conditions_source.size();
                        }
                        if (npc.hit_effect.conditions_target != null) {
                            if (val == null) val = npc.hit_effect.conditions_target.size();
                            else val += npc.hit_effect.conditions_target.size();
                        }
                        return val;
                    }
                    return null;
                case 24:
                    return npc.getMonsterExperience();
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
