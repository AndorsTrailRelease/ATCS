package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.CollapsiblePanel;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.IntegerBasedCheckBox;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.util.ArrayList;

public class ActorConditionEditor extends JSONElementEditor {

    private static final long serialVersionUID = 799130864545495819L;

    private static final String form_view_id = "Form";
    private static final String json_view_id = "JSON";

    private JButton acIcon;
    private JTextField idField;
    private JTextField nameField;
    private JTextField descriptionField;
    @SuppressWarnings("rawtypes")
    private JComboBox categoryBox;
    private IntegerBasedCheckBox positiveBox;
    private IntegerBasedCheckBox stackingBox;

    //private JTextField roundVisualField;
    @SuppressWarnings("rawtypes")
    private JComboBox roundVisualField;
    private JSpinner roundHpMinField;
    private JSpinner roundHpMaxField;
    private JSpinner roundApMinField;
    private JSpinner roundApMaxField;

    //private JTextField fullRoundVisualField;
    @SuppressWarnings("rawtypes")
    private JComboBox fullRoundVisualField;
    private JSpinner fullRoundHpMinField;
    private JSpinner fullRoundHpMaxField;
    private JSpinner fullRoundApMinField;
    private JSpinner fullRoundApMaxField;

    private JSpinner abilityHpField;
    private JSpinner abilityApField;
    private JSpinner abilityMoveCost;
    private JSpinner abilityUseCost;
    private JSpinner abilityReequipCost;
    private JSpinner abilityAttackCost;
    private JSpinner abilityAttackChance;
    private JSpinner abilityDamageMinField;
    private JSpinner abilityDamageMaxField;
    private JSpinner abilityCriticalSkill;
    private JSpinner abilityBlockChance;
    private JSpinner abilityDamageResistance;


    public ActorConditionEditor(ActorCondition ac) {
        super(ac, ac.getDesc(), ac.getIcon());
        addEditorTab(form_view_id, getFormView());
        addEditorTab(json_view_id, getJSONView());
    }

    @Override
    public void insertFormViewDataField(JPanel pane) {
        final ActorCondition ac = ((ActorCondition) target);

        final FieldUpdateListener listener = new ActorConditionFieldUpdater();

        acIcon = createButtonPane(pane, ac.getProject(), ac, ActorCondition.class, ac.getImage(), Spritesheet.Category.actorcondition, listener);

        idField = addTextField(pane, "Internal ID: ", ac.id, ac.writable, listener);
        nameField = addTranslatableTextField(pane, "Display name: ", ac.display_name, ac.writable, listener);
        descriptionField = addTranslatableTextField(pane, "Description: ", ac.description, ac.writable, listener);
        categoryBox = addEnumValueBox(pane, "Category: ", ActorCondition.ACCategory.values(), ac.category, ac.writable, listener);
        positiveBox = addIntegerBasedCheckBox(pane, "Positive", ac.positive, ac.writable, listener);
        stackingBox = addIntegerBasedCheckBox(pane, "Stacking", ac.stacking, ac.writable, listener);


        CollapsiblePanel roundEffectPane = new CollapsiblePanel("Effect every round (6s): ");
        roundEffectPane.setLayout(new JideBoxLayout(roundEffectPane, JideBoxLayout.PAGE_AXIS));
        final ActorCondition.RoundEffect roundEffect;
        if (ac.round_effect != null) {
            roundEffect = ac.round_effect;
        } else {
            roundEffect = new ActorCondition.RoundEffect();
        }
        roundVisualField = addEnumValueBox(roundEffectPane, "Visual effect ID:", ActorCondition.VisualEffectID.values(), roundEffect.visual_effect, ac.writable, listener);
        roundHpMinField = addIntegerField(roundEffectPane, "HP Bonus Min: ", roundEffect.hp_boost_min, true, ac.writable, listener);
        roundHpMaxField = addIntegerField(roundEffectPane, "HP Bonus Max: ", roundEffect.hp_boost_max, true, ac.writable, listener);
        roundApMinField = addIntegerField(roundEffectPane, "AP Bonus Min: ", roundEffect.ap_boost_min, true, ac.writable, listener);
        roundApMaxField = addIntegerField(roundEffectPane, "AP Bonus Max: ", roundEffect.ap_boost_max, true, ac.writable, listener);
        roundEffectPane.setExpanded(ac.round_effect != null);
        pane.add(roundEffectPane, JideBoxLayout.FIX);


        CollapsiblePanel fullRoundEffectPane = new CollapsiblePanel("Effect every full round (25s): ");
        fullRoundEffectPane.setLayout(new JideBoxLayout(fullRoundEffectPane, JideBoxLayout.PAGE_AXIS));
        final ActorCondition.RoundEffect fullRoundEffect;
        if (ac.full_round_effect != null) {
            fullRoundEffect = ac.full_round_effect;
        } else {
            fullRoundEffect = new ActorCondition.RoundEffect();
        }
        fullRoundVisualField = addEnumValueBox(fullRoundEffectPane, "Visual effect ID:", ActorCondition.VisualEffectID.values(), fullRoundEffect.visual_effect, ac.writable, listener);
        fullRoundHpMinField = addIntegerField(fullRoundEffectPane, "HP Bonus min: ", fullRoundEffect.hp_boost_min, true, ac.writable, listener);
        fullRoundHpMaxField = addIntegerField(fullRoundEffectPane, "HP Bonus max: ", fullRoundEffect.hp_boost_max, true, ac.writable, listener);
        fullRoundApMinField = addIntegerField(fullRoundEffectPane, "AP Bonus min: ", fullRoundEffect.ap_boost_min, true, ac.writable, listener);
        fullRoundApMaxField = addIntegerField(fullRoundEffectPane, "AP Bonus max: ", fullRoundEffect.ap_boost_max, true, ac.writable, listener);
        fullRoundEffectPane.setExpanded(ac.full_round_effect != null);
        pane.add(fullRoundEffectPane, JideBoxLayout.FIX);

        CollapsiblePanel abilityEffectPane = new CollapsiblePanel("Constant ability effect: ");
        abilityEffectPane.setLayout(new JideBoxLayout(abilityEffectPane, JideBoxLayout.PAGE_AXIS));
        ActorCondition.AbilityEffect abilityEffect;
        if (ac.constant_ability_effect != null) {
            abilityEffect = ac.constant_ability_effect;
        } else {
            abilityEffect = new ActorCondition.AbilityEffect();
        }
        abilityHpField = addIntegerField(abilityEffectPane, "Boost max HP: ", abilityEffect.max_hp_boost, true, ac.writable, listener);
        abilityApField = addIntegerField(abilityEffectPane, "Boost max AP: ", abilityEffect.max_ap_boost, true, ac.writable, listener);
        abilityDamageMinField = addIntegerField(abilityEffectPane, "Boost min damage: ", abilityEffect.increase_damage_min, true, ac.writable, listener);
        abilityDamageMaxField = addIntegerField(abilityEffectPane, "Boost max damage: ", abilityEffect.increase_damage_max, true, ac.writable, listener);
        abilityAttackChance = addIntegerField(abilityEffectPane, "Boost attack chance: ", abilityEffect.increase_attack_chance, true, ac.writable, listener);
        abilityBlockChance = addIntegerField(abilityEffectPane, "Boost block chance: ", abilityEffect.increase_block_chance, true, ac.writable, listener);
        abilityCriticalSkill = addIntegerField(abilityEffectPane, "Boost critical skill: ", abilityEffect.increase_critical_skill, true, ac.writable, listener);
        abilityDamageResistance = addIntegerField(abilityEffectPane, "Boost damage resistance: ", abilityEffect.increase_damage_resistance, true, ac.writable, listener);
        abilityMoveCost = addIntegerField(abilityEffectPane, "Increase move cost: ", abilityEffect.increase_move_cost, true, ac.writable, listener);
        abilityAttackCost = addIntegerField(abilityEffectPane, "Increase attack cost: ", abilityEffect.increase_attack_cost, true, ac.writable, listener);
        abilityUseCost = addIntegerField(abilityEffectPane, "Increase item use cost: ", abilityEffect.increase_use_cost, true, ac.writable, listener);
        abilityReequipCost = addIntegerField(abilityEffectPane, "Increase reequip cost: ", abilityEffect.increase_reequip_cost, true, ac.writable, listener);
        abilityEffectPane.setExpanded(ac.constant_ability_effect != null);
        pane.add(abilityEffectPane, JideBoxLayout.FIX);

    }


    //TODO enhancement. Split this in smaller pieces (one for each base field, and one for each "*Effect". later, later....
    public class ActorConditionFieldUpdater implements FieldUpdateListener {
        @Override
        public void valueChanged(JComponent source, Object value) {
            ActorCondition aCond = (ActorCondition) target;
            if (source == idField) {
                //Events caused by cancel an ID edition. Dismiss.
                if (skipNext) {
                    skipNext = false;
                    return;
                }
                if (target.id.equals((String) value)) return;

                if (idChanging()) {
                    aCond.id = (String) value;
                    ActorConditionEditor.this.name = aCond.getDesc();
                    aCond.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(ActorConditionEditor.this);
                } else {
                    cancelIdEdit(idField);
                    return;
                }
            } else if (source == nameField) {
                aCond.display_name = (String) value;
                ActorConditionEditor.this.name = aCond.getDesc();
                aCond.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ActorConditionEditor.this);
            } else if (source == descriptionField) {
                aCond.description = (String) value;
                aCond.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ActorConditionEditor.this);
            } else if (source == acIcon) {
                aCond.icon_id = (String) value;
                aCond.childrenChanged(new ArrayList<ProjectTreeNode>());
                ActorConditionEditor.this.icon = new ImageIcon(aCond.getProject().getIcon((String) value));
                ATContentStudio.frame.editorChanged(ActorConditionEditor.this);
                acIcon.setIcon(new ImageIcon(aCond.getProject().getImage((String) value)));
                acIcon.revalidate();
                acIcon.repaint();
            } else if (source == positiveBox) {
                aCond.positive = (Integer) value;
            } else if (source == stackingBox) {
                aCond.stacking = (Integer) value;
            } else if (source == categoryBox) {
                aCond.category = (ActorCondition.ACCategory) value;
            } else if (source == roundVisualField) {
                if (value == null) {
                    if (aCond.round_effect != null) {
                        aCond.round_effect.visual_effect = null;
                        if (isEmpty(aCond.round_effect)) {
                            aCond.round_effect = null;
                        }
                    }
                } else {
                    if (aCond.round_effect == null) {
                        aCond.round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.round_effect.visual_effect = (ActorCondition.VisualEffectID) value;
                }
            } else if (source == roundHpMinField) {
                if (value == null) {
                    if (aCond.round_effect != null) {
                        aCond.round_effect.hp_boost_min = null;
                        if (isEmpty(aCond.round_effect)) {
                            aCond.round_effect = null;
                        }
                    }
                } else {
                    if (aCond.round_effect == null) {
                        aCond.round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.round_effect.hp_boost_min = (Integer) value;
                }
            } else if (source == roundHpMaxField) {
                if (value == null) {
                    if (aCond.round_effect != null) {
                        aCond.round_effect.hp_boost_max = null;
                        if (isEmpty(aCond.round_effect)) {
                            aCond.round_effect = null;
                        }
                    }
                } else {
                    if (aCond.round_effect == null) {
                        aCond.round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.round_effect.hp_boost_max = (Integer) value;
                }
            } else if (source == roundApMinField) {
                if (value == null) {
                    if (aCond.round_effect != null) {
                        aCond.round_effect.ap_boost_min = null;
                        if (isEmpty(aCond.round_effect)) {
                            aCond.round_effect = null;
                        }
                    }
                } else {
                    if (aCond.round_effect == null) {
                        aCond.round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.round_effect.ap_boost_min = (Integer) value;
                }
            } else if (source == roundApMaxField) {
                if (value == null) {
                    if (aCond.round_effect != null) {
                        aCond.round_effect.ap_boost_max = null;
                        if (isEmpty(aCond.round_effect)) {
                            aCond.round_effect = null;
                        }
                    }
                } else {
                    if (aCond.round_effect == null) {
                        aCond.round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.round_effect.ap_boost_max = (Integer) value;
                }
            } else if (source == fullRoundVisualField) {
                if (value == null) {
                    if (aCond.full_round_effect != null) {
                        aCond.full_round_effect.visual_effect = null;
                        if (isEmpty(aCond.full_round_effect)) {
                            aCond.full_round_effect = null;
                        }
                    }
                } else {
                    if (aCond.full_round_effect == null) {
                        aCond.full_round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.full_round_effect.visual_effect = (ActorCondition.VisualEffectID) value;
                }
            } else if (source == fullRoundHpMinField) {
                if (value == null) {
                    if (aCond.full_round_effect != null) {
                        aCond.full_round_effect.hp_boost_min = null;
                        if (isEmpty(aCond.full_round_effect)) {
                            aCond.full_round_effect = null;
                        }
                    }
                } else {
                    if (aCond.full_round_effect == null) {
                        aCond.full_round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.full_round_effect.hp_boost_min = (Integer) value;
                }
            } else if (source == fullRoundHpMaxField) {
                if (value == null) {
                    if (aCond.full_round_effect != null) {
                        aCond.full_round_effect.hp_boost_max = null;
                        if (isEmpty(aCond.full_round_effect)) {
                            aCond.full_round_effect = null;
                        }
                    }
                } else {
                    if (aCond.full_round_effect == null) {
                        aCond.full_round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.full_round_effect.hp_boost_max = (Integer) value;
                }
            } else if (source == fullRoundApMinField) {
                if (value == null) {
                    if (aCond.full_round_effect != null) {
                        aCond.full_round_effect.ap_boost_min = null;
                        if (isEmpty(aCond.full_round_effect)) {
                            aCond.full_round_effect = null;
                        }
                    }
                } else {
                    if (aCond.full_round_effect == null) {
                        aCond.full_round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.full_round_effect.ap_boost_min = (Integer) value;
                }
            } else if (source == fullRoundApMaxField) {
                if (value == null) {
                    if (aCond.full_round_effect != null) {
                        aCond.full_round_effect.ap_boost_max = null;
                        if (isEmpty(aCond.full_round_effect)) {
                            aCond.full_round_effect = null;
                        }
                    }
                } else {
                    if (aCond.full_round_effect == null) {
                        aCond.full_round_effect = new ActorCondition.RoundEffect();
                    }
                    aCond.full_round_effect.ap_boost_max = (Integer) value;
                }
            } else if (source == abilityHpField) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.max_hp_boost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.max_hp_boost = (Integer) value;
                }
            } else if (source == abilityApField) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.max_ap_boost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.max_ap_boost = (Integer) value;
                }
            } else if (source == abilityMoveCost) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_move_cost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_move_cost = (Integer) value;
                }
            } else if (source == abilityUseCost) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_use_cost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_use_cost = (Integer) value;
                }
            } else if (source == abilityReequipCost) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_reequip_cost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_reequip_cost = (Integer) value;
                }
            } else if (source == abilityAttackCost) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_attack_cost = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_attack_cost = (Integer) value;
                }
            } else if (source == abilityAttackChance) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_attack_chance = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_attack_chance = (Integer) value;
                }
            } else if (source == abilityDamageMinField) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_damage_min = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_damage_min = (Integer) value;
                }
            } else if (source == abilityDamageMaxField) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_damage_max = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_damage_max = (Integer) value;
                }
            } else if (source == abilityCriticalSkill) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_critical_skill = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_critical_skill = (Integer) value;
                }
            } else if (source == abilityBlockChance) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_block_chance = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_block_chance = (Integer) value;
                }
            } else if (source == abilityDamageResistance) {
                if (value == null) {
                    if (aCond.constant_ability_effect != null) {
                        aCond.constant_ability_effect.increase_damage_resistance = null;
                        if (isEmpty(aCond.constant_ability_effect)) {
                            aCond.constant_ability_effect = null;
                        }
                    }
                } else {
                    if (aCond.constant_ability_effect == null) {
                        aCond.constant_ability_effect = new ActorCondition.AbilityEffect();
                    }
                    aCond.constant_ability_effect.increase_damage_resistance = (Integer) value;
                }
            }

            if (aCond.state != GameDataElement.State.modified) {
                aCond.state = GameDataElement.State.modified;
                ActorConditionEditor.this.name = aCond.getDesc();
                aCond.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ActorConditionEditor.this);
            }
            updateJsonViewText(aCond.toJsonString());
        }

        private boolean isEmpty(ActorCondition.RoundEffect round_effect) {
            return round_effect == null ||
                    (round_effect.visual_effect == null &&
                            round_effect.hp_boost_min == null &&
                            round_effect.hp_boost_max == null &&
                            round_effect.ap_boost_min == null &&
                            round_effect.ap_boost_max == null);
        }

        private boolean isEmpty(ActorCondition.AbilityEffect ability_effect) {
            return ability_effect == null ||
                    (ability_effect.max_hp_boost == null &&
                            ability_effect.max_ap_boost == null &&
                            ability_effect.increase_move_cost == null &&
                            ability_effect.increase_use_cost == null &&
                            ability_effect.increase_reequip_cost == null &&
                            ability_effect.increase_attack_cost == null &&
                            ability_effect.increase_attack_chance == null &&
                            ability_effect.increase_damage_min == null &&
                            ability_effect.increase_damage_max == null &&
                            ability_effect.increase_critical_skill == null &&
                            ability_effect.increase_block_chance == null &&
                            ability_effect.increase_damage_resistance == null);
        }
    }


}
