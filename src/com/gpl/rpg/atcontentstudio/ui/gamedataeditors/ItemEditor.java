package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.CollapsiblePanel;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.IntegerBasedCheckBox;
import com.gpl.rpg.atcontentstudio.ui.OrderedListenerListModel;
import com.gpl.rpg.atcontentstudio.utils.BasicLambda;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithArg;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithReturn;
import com.gpl.rpg.atcontentstudio.utils.UiUtils;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.gpl.rpg.atcontentstudio.model.gamedata.Common.*;

public class ItemEditor extends JSONElementEditor {

    private static final long serialVersionUID = 7538154592029351986L;

    private static final String form_view_id = "Form";
    private static final String json_view_id = "JSON";

    private static final String killLabel = "Effect on every kill: ";
    private static final String useLabel = "Effect on use: ";


    private ActorConditionEffect selectedEquipEffectCondition;


    private JButton itemIcon;
    private JTextField idField;
    private JTextField nameField;
    private JTextField descriptionField;
    @SuppressWarnings("rawtypes")
    private JComboBox typeBox;
    private IntegerBasedCheckBox manualPriceBox;
    private JSpinner baseCostField;
    private MyComboBox categoryBox;
    private Integer baseManualPrice = null;

    private CollapsiblePanel equipEffectPane;
    private Item.EquipEffect equipEffect;
    private JSpinner equipDmgMin;
    private JSpinner equipDmgMax;
    private JSpinner equipBoostHP;
    private JSpinner equipBoostAP;
    private JSpinner equipBoostAC;
    private JSpinner equipBoostBC;
    private JSpinner equipBoostCS;
    private JSpinner equipSetCM;
    private JSpinner equipSetDM;
    private JSpinner equipBoostDR;
    private JSpinner equipIncMoveCost;
    private JSpinner equipIncUseCost;
    private JSpinner equipIncReequipCost;
    private JSpinner equipIncAttackCost;
    private EquipConditionsListModel equipConditionsModel;
    @SuppressWarnings("rawtypes")
    private JList equipConditionsList;
    private MyComboBox equipConditionBox;
    private JRadioButton equipConditionWithMagnitude;
    private JRadioButton equipConditionImmunity;
    private JSpinner equipConditionMagnitude;

    private CommonEditor.HitEffectPane hitEffectPane = new CommonEditor.HitEffectPane("Effect on every hit: ", TimedActorConditionEffect::new, this, null, null);
    private CommonEditor.DeathEffectPane killEffectPane = new CommonEditor.DeathEffectPane(killLabel, TimedActorConditionEffect::new, this, null);
    private CommonEditor.HitRecievedEffectPane hitReceivedEffectPane = new CommonEditor.HitRecievedEffectPane("Effect on every hit received: ", TimedActorConditionEffect::new, this, null, null);

    public ItemEditor(Item item) {
        super(item, item.getDesc(), item.getIcon());
        addEditorTab(form_view_id, getFormView());
        addEditorTab(json_view_id, getJSONView());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void insertFormViewDataField(JPanel pane) {

        final Item item = (Item) target;

        final FieldUpdateListener listener = new ItemFieldUpdater();

        itemIcon = createButtonPane(pane, item.getProject(), item, Item.class, item.getImage(), Spritesheet.Category.item, listener);

        idField = addTextField(pane, "Internal ID: ", item.id, item.writable, listener);
        nameField = addTranslatableTextField(pane, "Display name: ", item.name, item.writable, listener);
        descriptionField = addTranslatableTextField(pane, "Description: ", item.description, item.writable, listener);
        typeBox = addEnumValueBox(pane, "Type: ", Item.DisplayType.values(), item.display_type, item.writable, listener);
        manualPriceBox = addIntegerBasedCheckBox(pane, "Has manual price", item.has_manual_price, item.writable, listener);
        baseManualPrice = item.base_market_cost;
        baseCostField = addIntegerField(pane, "Base market cost: ", (item.has_manual_price != null && item.has_manual_price == 1) ? item.base_market_cost : item.computePrice(), false, item.writable,
                                        listener);
        if (!manualPriceBox.isSelected()) {
            baseCostField.setEnabled(false);
        }
        categoryBox = addItemCategoryBox(pane, item.getProject(), "Category: ", item.category, item.writable, listener);

        equipEffectPane = new CollapsiblePanel("Effect when equipped: ");
        equipEffectPane.setLayout(new JideBoxLayout(equipEffectPane, JideBoxLayout.PAGE_AXIS));
        if (item.equip_effect == null) {
            equipEffect = new Item.EquipEffect();
        } else {
            equipEffect = item.equip_effect;
        }
        equipDmgMin = addIntegerField(equipEffectPane, "Attack Damage min: ", equipEffect.damage_boost_min, true, item.writable, listener);
        equipDmgMax = addIntegerField(equipEffectPane, "Attack Damage max: ", equipEffect.damage_boost_max, true, item.writable, listener);
        equipSetDM = addIntegerField(equipEffectPane, "Damage modifier %: ", equipEffect.damage_modifier, 100, false, item.writable, listener);
        equipBoostHP = addIntegerField(equipEffectPane, "Boost max HP: ", equipEffect.max_hp_boost, true, item.writable, listener);
        equipBoostAP = addIntegerField(equipEffectPane, "Boost max AP: ", equipEffect.max_ap_boost, true, item.writable, listener);
        equipBoostAC = addIntegerField(equipEffectPane, "Boost attack chance: ", equipEffect.increase_attack_chance, true, item.writable, listener);
        equipBoostBC = addIntegerField(equipEffectPane, "Boost block chance: ", equipEffect.increase_block_chance, true, item.writable, listener);
        equipBoostCS = addIntegerField(equipEffectPane, "Boost critical skill: ", equipEffect.increase_critical_skill, true, item.writable, listener);
        equipSetCM = addDoubleField(equipEffectPane, "Critical multiplier: ", equipEffect.critical_multiplier, item.writable, listener);
        equipBoostDR = addIntegerField(equipEffectPane, "Boost damage resistance: ", equipEffect.increase_damage_resistance, true, item.writable, listener);
        equipIncMoveCost = addIntegerField(equipEffectPane, "Increase move cost: ", equipEffect.increase_move_cost, true, item.writable, listener);
        equipIncUseCost = addIntegerField(equipEffectPane, "Increase item use cost: ", equipEffect.increase_use_item_cost, true, item.writable, listener);
        equipIncReequipCost = addIntegerField(equipEffectPane, "Increase reequip cost: ", equipEffect.increase_reequip_cost, true, item.writable, listener);
        equipIncAttackCost = addIntegerField(equipEffectPane, "Increase attack cost: ", equipEffect.increase_attack_cost, true, item.writable, listener);

        String titleEquipConditions = "Actor Conditions applied when equipped: ";
        equipConditionsModel = new EquipConditionsListModel(equipEffect);
        CommonEditor.ConditionsCellRenderer cellRendererEquipConditions = new CommonEditor.ConditionsCellRenderer();
        BasicLambdaWithArg<ActorConditionEffect> selectedSetEquipConditions = (value) -> selectedEquipEffectCondition = value;
        BasicLambdaWithReturn<ActorConditionEffect> selectedGetEquipConditions = () -> selectedEquipEffectCondition;
        BasicLambda selectedResetEquipConditions = () -> selectedEquipEffectCondition = null;
        BasicLambdaWithArg<JPanel> updatePaneEquipConditions = (editorPane) -> updateEquipConditionEditorPane(editorPane, selectedEquipEffectCondition, listener);
        var resultEquipConditions = UiUtils.getCollapsibleItemList(listener,
                                                                   equipConditionsModel,
                                                                   selectedResetEquipConditions,
                                                                   selectedSetEquipConditions,
                                                                   selectedGetEquipConditions,
                                                                   (x) -> {
                                                                   },
                                                                   updatePaneEquipConditions,
                                                                   item.writable,
                                                                   ActorConditionEffect::new,
                                                                   cellRendererEquipConditions,
                                                                   titleEquipConditions,
                                                                   (x) -> null);
        equipConditionsList = resultEquipConditions.list;
        CollapsiblePanel equipConditionsPane = resultEquipConditions.collapsiblePanel;
        if (item.equip_effect == null || item.equip_effect.conditions == null || item.equip_effect.conditions.isEmpty()) {
            equipConditionsPane.collapse();
        }
        equipEffectPane.add(equipConditionsPane, JideBoxLayout.FIX);

        pane.add(equipEffectPane, JideBoxLayout.FIX);
        if (item.equip_effect == null) {
            equipEffectPane.collapse();
        }

        HitEffect hitEffect = Objects.requireNonNullElseGet(item.hit_effect, HitEffect::new);
        hitEffectPane.createHitEffectPaneContent(listener, item.writable, hitEffect,
                                                 new CommonEditor.SourceTimedConditionsListModel(hitEffect),
                                                 new CommonEditor.TargetTimedConditionsListModel(hitEffect));
        pane.add(hitEffectPane.effectPane, JideBoxLayout.FIX);

        DeathEffect killEffect = Objects.requireNonNullElseGet(item.kill_effect, DeathEffect::new);
        killEffectPane.createDeathEffectPaneContent(listener, item.writable, killEffect,
                                                    new CommonEditor.SourceTimedConditionsListModel(killEffect));
        pane.add(killEffectPane.effectPane, JideBoxLayout.FIX);

        HitReceivedEffect hitReceivedEffect = Objects.requireNonNullElseGet(item.hit_received_effect,
                                                                            HitReceivedEffect::new);
        hitReceivedEffectPane.createHitReceivedEffectPaneContent(listener, item.writable, hitReceivedEffect,
                                                                 new CommonEditor.SourceTimedConditionsListModel(
                                                                         hitReceivedEffect),
                                                                 new CommonEditor.TargetTimedConditionsListModel(
                                                                         hitReceivedEffect));
        pane.add(killEffectPane.effectPane, JideBoxLayout.FIX);


        if (item.category == null || item.category.action_type == null || item.category.action_type == ItemCategory.ActionType.none) {
            equipEffectPane.setVisible(false);
            hitEffectPane.effectPane.setVisible(false);
            killEffectPane.effectPane.setVisible(false);
        } else if (item.category.action_type == ItemCategory.ActionType.use) {
            equipEffectPane.setVisible(false);
            hitEffectPane.effectPane.setVisible(false);
            killEffectPane.effectPane.setVisible(true);
            killEffectPane.effectPane.setTitle(useLabel);
            killEffectPane.effectPane.revalidate();
            killEffectPane.effectPane.repaint();
        } else if (item.category.action_type == ItemCategory.ActionType.equip) {
            equipEffectPane.setVisible(true);
            hitEffectPane.effectPane.setVisible(true);
            killEffectPane.effectPane.setVisible(true);
            killEffectPane.effectPane.setTitle(killLabel);
            killEffectPane.effectPane.revalidate();
            killEffectPane.effectPane.repaint();
        }

    }

    public void updateEquipConditionEditorPane(JPanel pane, ActorConditionEffect condition, final FieldUpdateListener listener) {
        pane.removeAll();
        if (equipConditionBox != null) {
            removeElementListener(equipConditionBox);
        }
        if (condition == null) {
            pane.revalidate();
            pane.repaint();
            return;
        }

        boolean writable = ((Item) target).writable;
        Project proj = ((Item) target).getProject();

        equipConditionBox = addActorConditionBox(pane, proj, "Actor Condition: ", condition.condition, writable, listener);
        equipConditionWithMagnitude = new JRadioButton("Apply condition with magnitude.");
        pane.add(equipConditionWithMagnitude, JideBoxLayout.FIX);
        equipConditionMagnitude = addIntegerField(pane, "Magnitude: ", condition.magnitude, 1, false, writable, listener);
        equipConditionImmunity = new JRadioButton("Give immunity to condition.");
        pane.add(equipConditionImmunity, JideBoxLayout.FIX);

        ButtonGroup radioEffectGroup = new ButtonGroup();
        radioEffectGroup.add(equipConditionWithMagnitude);
        radioEffectGroup.add(equipConditionImmunity);

        boolean immunity = condition.magnitude == null || condition.magnitude.equals(ActorCondition.MAGNITUDE_CLEAR);
        equipConditionImmunity.setSelected(immunity);
        equipConditionWithMagnitude.setSelected(!immunity);
        equipConditionMagnitude.setEnabled(!immunity);

        equipConditionWithMagnitude.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(equipConditionWithMagnitude, equipConditionWithMagnitude.isSelected());
            }
        });
        equipConditionImmunity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(equipConditionImmunity, equipConditionImmunity.isSelected());
            }
        });


        pane.revalidate();
        pane.repaint();
    }

    public static class EquipConditionsListModel extends OrderedListenerListModel<Item.EquipEffect, ActorConditionEffect> {
        public EquipConditionsListModel(Item.EquipEffect equipEffect) {
            super(equipEffect);
        }

        @Override
        protected List<ActorConditionEffect> getItems() {
            return source.conditions;
        }

        @Override
        protected void setItems(List<ActorConditionEffect> conditions) {
            source.conditions = conditions;
        }
    }

    public static boolean isNull(Item.EquipEffect effect) {
        if (effect.conditions != null) return false;
        if (effect.critical_multiplier != null) return false;
        if (effect.damage_modifier != null) return false;
        if (effect.damage_boost_max != null) return false;
        if (effect.damage_boost_min != null) return false;
        if (effect.increase_attack_chance != null) return false;
        if (effect.increase_attack_cost != null) return false;
        if (effect.increase_block_chance != null) return false;
        if (effect.increase_critical_skill != null) return false;
        if (effect.increase_damage_resistance != null) return false;
        if (effect.increase_move_cost != null) return false;
        if (effect.increase_reequip_cost != null) return false;
        if (effect.increase_use_item_cost != null) return false;
        if (effect.max_ap_boost != null) return false;
        if (effect.max_hp_boost != null) return false;
        return true;
    }

    public class ItemFieldUpdater implements FieldUpdateListener {

        @Override
        public void valueChanged(JComponent source, Object value) {
            Item item = (Item) target;
            boolean updatePrice, updateEquip, updateHit, updateKill, updateHitReceived;
            updatePrice = updateEquip = updateHit = updateKill = updateHitReceived = false;
            if (source == idField) {
                //Events caused by cancel an ID edition. Dismiss.
                if (skipNext) {
                    skipNext = false;
                    return;
                }
                if (target.id.equals((String) value)) return;

                if (idChanging()) {
                    item.id = (String) value;
                    ItemEditor.this.name = item.getDesc();
                    item.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(ItemEditor.this);
                } else {
                    cancelIdEdit(idField);
                    return;
                }
            } else if (source == nameField) {
                item.name = (String) value;
                ItemEditor.this.name = item.getDesc();
                item.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ItemEditor.this);
            } else if (source == itemIcon) {
                item.icon_id = (String) value;
                item.childrenChanged(new ArrayList<ProjectTreeNode>());
                ItemEditor.this.icon = new ImageIcon(item.getProject().getIcon((String) value));
                ATContentStudio.frame.editorChanged(ItemEditor.this);
                itemIcon.setIcon(new ImageIcon(item.getProject().getImage((String) value)));
                itemIcon.revalidate();
                itemIcon.repaint();
            } else if (source == descriptionField) {
                item.description = descriptionField.getText();
            } else if (source == typeBox) {
                item.display_type = (Item.DisplayType) value;
            } else if (source == manualPriceBox) {
                item.has_manual_price = (Integer) value;
                if (!manualPriceBox.isSelected()) {
                    baseCostField.setEnabled(false);
                    updatePrice = true;
                } else {
                    baseCostField.setEnabled(true);
                    if (baseManualPrice != null) {
                        baseCostField.setValue(baseManualPrice);
                    }
                }
            } else if (source == baseCostField) {
                if (manualPriceBox.isSelected()) {
                    item.base_market_cost = (Integer) value;
                    baseManualPrice = item.base_market_cost;
                }
            } else if (source == categoryBox) {
                if (item.category != null) {
                    item.category.removeBacklink(item);
                }
                item.category = (ItemCategory) value;
                if (item.category != null) {
                    item.category_id = item.category.id;
                    item.category.addBacklink(item);
                } else {
                    item.category_id = null;
                }
                if (item.category == null || item.category.action_type == null || item.category.action_type == ItemCategory.ActionType.none) {
                    equipEffectPane.setVisible(false);
                    item.equip_effect = null;
                    hitEffectPane.effectPane.setVisible(false);
                    item.hit_effect = null;
                    killEffectPane.effectPane.setVisible(false);
                    item.kill_effect = null;
                    hitReceivedEffectPane.effectPane.setVisible(false);
                    item.hit_received_effect = null;
                    ItemEditor.this.revalidate();
                    ItemEditor.this.repaint();
                } else if (item.category.action_type == ItemCategory.ActionType.use) {
                    equipEffectPane.setVisible(false);
                    item.equip_effect = null;
                    hitEffectPane.effectPane.setVisible(false);
                    item.hit_effect = null;
                    killEffectPane.effectPane.setVisible(true);
                    updateKill = true;
                    hitReceivedEffectPane.effectPane.setVisible(false);
                    item.hit_received_effect = null;
                    updateHitReceived = true;
                    updateEquip = true;
                    killEffectPane.effectPane.setTitle(useLabel);
                    ItemEditor.this.revalidate();
                    ItemEditor.this.repaint();
                } else if (item.category.action_type == ItemCategory.ActionType.equip) {
                    equipEffectPane.setVisible(true);
                    hitEffectPane.effectPane.setVisible(true);
                    killEffectPane.effectPane.setVisible(true);
                    updateKill = true;
                    hitReceivedEffectPane.effectPane.setVisible(true);
                    updateHitReceived = true;
                    updateEquip = true;
                    killEffectPane.effectPane.setTitle(killLabel);
                    ItemEditor.this.revalidate();
                    ItemEditor.this.repaint();
                }
                updatePrice = true;
            } else if (source == equipDmgMin) {
                equipEffect.damage_boost_min = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipDmgMax) {
                equipEffect.damage_boost_max = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostHP) {
                equipEffect.max_hp_boost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostAP) {
                equipEffect.max_ap_boost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostAC) {
                equipEffect.increase_attack_chance = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostBC) {
                equipEffect.increase_block_chance = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostCS) {
                equipEffect.increase_critical_skill = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipSetCM) {
                equipEffect.critical_multiplier = (Double) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipSetDM) {
                equipEffect.damage_modifier = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipBoostDR) {
                equipEffect.increase_damage_resistance = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipIncMoveCost) {
                equipEffect.increase_move_cost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipIncUseCost) {
                equipEffect.increase_use_item_cost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipIncReequipCost) {
                equipEffect.increase_reequip_cost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipIncAttackCost) {
                equipEffect.increase_attack_cost = (Integer) value;
                updatePrice = true;
                updateEquip = true;
            } else if (source == equipConditionsList) {
                updateEquip = true;
            } else if (source == equipConditionBox) {
                updateConditionEffect((ActorCondition) value, item, selectedEquipEffectCondition, equipConditionsModel);
            } else if (source == equipConditionMagnitude) {
                selectedEquipEffectCondition.magnitude = (Integer) value;
                equipConditionsModel.itemChanged(selectedEquipEffectCondition);
            } else if (source == equipConditionImmunity && (Boolean) value) {
                selectedEquipEffectCondition.magnitude = ActorCondition.MAGNITUDE_CLEAR;
                equipConditionMagnitude.setEnabled(false);
                equipConditionsModel.itemChanged(selectedEquipEffectCondition);
            } else if (source == equipConditionWithMagnitude && (Boolean) value) {
                selectedEquipEffectCondition.magnitude = (Integer) equipConditionMagnitude.getValue();
                equipConditionMagnitude.setEnabled(true);
                equipConditionsModel.itemChanged(selectedEquipEffectCondition);
            } else if (hitEffectPane.valueChanged(source, value, item)) {
                updatePrice = true;
                updateHit = true;
            } else if (killEffectPane.valueChanged(source, value, item)) {
                updatePrice = true;
                updateKill = true;
            } else if (hitReceivedEffectPane.valueChanged(source, value, item)) {
                updatePrice = true;
                updateHitReceived = true;
            }

            if (updateEquip) {
                if (isNull(equipEffect)) {
                    item.equip_effect = null;
                } else {
                    item.equip_effect = equipEffect;
                }
            }
            if (updateHit) {
                if (hitEffectPane.effect.isNull()) {
                    item.hit_effect = null;
                } else {
                    item.hit_effect = hitEffectPane.effect;
                }
            }
            if (updateKill) {
                if (killEffectPane.effect.isNull()) {
                    item.kill_effect = null;
                } else {
                    item.kill_effect = killEffectPane.effect;
                }
            }
            if (updateHitReceived) {
                if (hitReceivedEffectPane.effect.isNull()) {
                    item.hit_received_effect = null;
                } else {
                    item.hit_received_effect = hitReceivedEffectPane.effect;
                }
            }
            if (updatePrice && !manualPriceBox.isSelected()) {
                baseCostField.setValue(item.computePrice());
            }


            if (item.state != GameDataElement.State.modified) {
                item.state = GameDataElement.State.modified;
                ItemEditor.this.name = item.getDesc();
                item.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ItemEditor.this);
            }
            updateJsonViewText(item.toJsonString());
        }
    }

}
