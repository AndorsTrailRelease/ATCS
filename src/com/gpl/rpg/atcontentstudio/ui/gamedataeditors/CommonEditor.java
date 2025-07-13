package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.Common;
import com.gpl.rpg.atcontentstudio.ui.*;
import com.gpl.rpg.atcontentstudio.utils.BasicLambda;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithArg;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithReturn;
import com.gpl.rpg.atcontentstudio.utils.UiUtils;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Supplier;

import static com.gpl.rpg.atcontentstudio.ui.Editor.addIntegerField;

public class CommonEditor {

    public static class TimedConditionsCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 7987880146189575234L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                Common.TimedActorConditionEffect effect = (Common.TimedActorConditionEffect) value;

                if (effect.condition != null) {

                    boolean immunity = effect.isImmunity();
                    boolean clear = effect.isClear();
                    boolean forever = effect.isInfinite();

                    if (clear) {
                        label.setIcon(new ImageIcon(effect.condition.getIcon()));
                        label.setText(
                                effect.chance + "% chances to clear actor condition " + effect.condition.getDesc());
                    } else if (immunity) {
                        label.setIcon(new OverlayIcon(effect.condition.getIcon(), DefaultIcons.getImmunityIcon()));
                        label.setText(
                                effect.chance + "% chances to give immunity to " + effect.condition.getDesc() + (forever ? " forever" : " for " + effect.duration + " rounds"));
                    } else {
                        label.setIcon(new ImageIcon(effect.condition.getIcon()));
                        label.setText(
                                effect.chance +
                                        "% chances to give actor condition " +
                                        effect.condition.getDesc() +
                                        " x" +
                                        effect.magnitude +
                                        (forever ? " forever" : " for " + effect.duration + " rounds"));
                    }
                } else {
                    label.setText("New, undefined actor condition effect.");
                }
            }
            return c;
        }
    }

    public static class ConditionsCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 7987880146189575234L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                Common.ActorConditionEffect effect = (Common.ActorConditionEffect) value;

                if (effect.condition != null) {
                    if (effect.isClear()) {
                        label.setIcon(new OverlayIcon(effect.condition.getIcon(), DefaultIcons.getImmunityIcon()));
                        label.setText("Immune to actor condition " + effect.condition.getDesc());
                    } else {
                        label.setIcon(new ImageIcon(effect.condition.getIcon()));
                        label.setText("Give actor condition " + effect.condition.getDesc() + " x" + effect.magnitude);
                    }
                } else {
                    label.setText("New, undefined actor condition effect.");
                }
            }
            return c;
        }
    }

    public static class HitRecievedEffectPane<EFFECT extends Common.HitReceivedEffect, LIST_MODEL_SOURCE, ELEMENT extends Common.TimedActorConditionEffect, MODEL extends OrderedListenerListModel<LIST_MODEL_SOURCE, ELEMENT>> extends HitEffectPane<EFFECT, LIST_MODEL_SOURCE, ELEMENT, MODEL> {
        /// this should just be a convenience field, to access it, without casting. DO NOT SET WITHOUT ALSO SETTING THE FIELD IN THE SUPER-CLASS!
        EFFECT effect;
        private JSpinner hitReceivedEffectHPMinTarget;
        private JSpinner hitReceivedEffectHPMaxTarget;
        private JSpinner hitReceivedEffectAPMinTarget;
        private JSpinner hitReceivedEffectAPMaxTarget;

        public HitRecievedEffectPane(String title, Supplier<ELEMENT> sourceNewSupplier, Editor editor, String applyToHint, String applyToTargetHint) {
            super(title, sourceNewSupplier, editor, applyToHint, applyToTargetHint);
        }

        void createHitReceivedEffectPaneContent(FieldUpdateListener listener, boolean writable, EFFECT e, MODEL sourceConditionsModelInput, MODEL targetConditionsModelInput) {
            effect = e;
            createHitEffectPaneContent(listener, writable, e, sourceConditionsModelInput, targetConditionsModelInput);
        }

        @Override
        protected void addFields(FieldUpdateListener listener, boolean writable) {
            super.addFields(listener, writable);
            hitReceivedEffectHPMinTarget = addIntegerField(effectPane, String.format("HP bonus min%s: ", applyToTargetHint),
                                                           effect.target.hp_boost_min, true, writable, listener);
            hitReceivedEffectHPMaxTarget = addIntegerField(effectPane, String.format("HP bonus max%s: ", applyToTargetHint),
                                                           effect.target.hp_boost_max, true, writable, listener);
            hitReceivedEffectAPMinTarget = addIntegerField(effectPane, String.format("AP bonus min%s: ", applyToTargetHint),
                                                           effect.target.ap_boost_min, true, writable, listener);
            hitReceivedEffectAPMaxTarget = addIntegerField(effectPane, String.format("AP bonus max%s: ", applyToTargetHint),
                                                           effect.target.ap_boost_max, true, writable, listener);
        }


        @Override
        public boolean valueChanged(JComponent source, Object value, GameDataElement backlink) {
            boolean updateHitReceived = super.valueChanged(source, value, backlink);
            if (!updateHitReceived) {
                if (source == hitReceivedEffectHPMinTarget) {
                    effect.target.hp_boost_min = (Integer) value;
                    updateHitReceived = true;
                } else if (source == hitReceivedEffectHPMaxTarget) {
                    effect.target.hp_boost_max = (Integer) value;
                    updateHitReceived = true;
                } else if (source == hitReceivedEffectAPMinTarget) {
                    effect.target.ap_boost_min = (Integer) value;
                    updateHitReceived = true;
                } else if (source == hitReceivedEffectAPMaxTarget) {
                    effect.target.ap_boost_max = (Integer) value;
                    updateHitReceived = true;
                }
            }

            return updateHitReceived;
        }
    }

    public static class HitEffectPane<EFFECT extends Common.HitEffect, LIST_MODEL_SOURCE, ELEMENT extends Common.TimedActorConditionEffect, MODEL extends OrderedListenerListModel<LIST_MODEL_SOURCE, ELEMENT>> extends DeathEffectPane<EFFECT, LIST_MODEL_SOURCE, ELEMENT, MODEL> {
        /// this should just be a convenience field, to access it, without casting. DO NOT SET WITHOUT ALSO SETTING THE FIELD IN THE SUPER-CLASS!
        public EFFECT effect;

        protected final String applyToTargetHint;
        private JList hitTargetConditionsList;
        private final ConditionEffectEditorPane<LIST_MODEL_SOURCE, ELEMENT, MODEL> hitTargetConditionPane;

        /*
         * create a new HitEffectPane with the selections (probably passed in from last time)
         */
        public HitEffectPane(String title, Supplier<ELEMENT> sourceNewSupplier, Editor editor, String applyToHint, String applyToTargetHint) {
            super(title, sourceNewSupplier, editor, applyToHint);
            hitTargetConditionPane = new ConditionEffectEditorPane<>(editor);

            if (applyToTargetHint == null || applyToTargetHint == "") {
                this.applyToTargetHint = "";
            } else {
                this.applyToTargetHint = String.format(" (%s)", applyToTargetHint);
            }
        }

        void createHitEffectPaneContent(FieldUpdateListener listener, boolean writable, EFFECT e, MODEL sourceConditionsModelInput, MODEL targetConditionsListModel) {
            effect = e;
            hitTargetConditionPane.conditionsModel = targetConditionsListModel;
            createDeathEffectPaneContent(listener, writable, e, sourceConditionsModelInput);
        }

        @Override
        protected void addLists(FieldUpdateListener listener, boolean writable) {
            super.addLists(listener, writable);

            String titleTarget = String.format("Actor Conditions applied to the target%s: ", applyToTargetHint);
            CommonEditor.TimedConditionsCellRenderer cellRendererTarget = new CommonEditor.TimedConditionsCellRenderer();
            BasicLambdaWithArg<ELEMENT> selectedSetTarget = (value) -> hitTargetConditionPane.selectedCondition = value;
            BasicLambdaWithReturn<ELEMENT> selectedGetTarget = () -> hitTargetConditionPane.selectedCondition;
            BasicLambda selectedResetTarget = () -> hitTargetConditionPane.selectedCondition = null;
            BasicLambdaWithArg<JPanel> updatePaneTarget = (editorPane) -> hitTargetConditionPane.updateEffectTimedConditionEditorPane(
                    editorPane, hitTargetConditionPane.selectedCondition, listener);

            var resultTarget = UiUtils.getCollapsibleItemList(listener, hitTargetConditionPane.conditionsModel,
                                                              selectedResetTarget, selectedSetTarget, selectedGetTarget,
                                                              (x) -> {
                                                              }, updatePaneTarget, writable, this.conditionSupplier,
                                                              cellRendererTarget, titleTarget, (x) -> null);
            hitTargetConditionsList = resultTarget.list;
            CollapsiblePanel hitTargetConditionsPane = resultTarget.collapsiblePanel;
            if (effect == null || effect.conditions_target == null || effect.conditions_target.isEmpty()) {
                hitTargetConditionsPane.collapse();
            }
            effectPane.add(hitTargetConditionsPane, JideBoxLayout.FIX);
        }

        @Override
        public boolean valueChanged(JComponent source, Object value, GameDataElement backlink) {
            boolean updateHit = false;
            if (super.valueChanged(source, value, backlink)) {
                updateHit = true;
            } else if (source == hitTargetConditionsList) {
                updateHit = true;
            } else if (hitTargetConditionPane.valueChanged(source, value, backlink)) {
                updateHit = true;
            }

            return updateHit;
        }
    }

    public static class DeathEffectPane<EFFECT extends Common.DeathEffect, LIST_MODEL_SOURCE, ELEMENT extends Common.TimedActorConditionEffect, MODEL extends OrderedListenerListModel<LIST_MODEL_SOURCE, ELEMENT>> {
        protected final Supplier<ELEMENT> conditionSupplier;
        protected final String title;
        protected final String applyToHint;


        EFFECT effect;
        CollapsiblePanel effectPane;
        private JSpinner effectHPMin;
        private JSpinner effectHPMax;
        private JSpinner effectAPMin;
        private JSpinner effectAPMax;
        private JList<ELEMENT> sourceConditionsList;

        private final ConditionEffectEditorPane<LIST_MODEL_SOURCE, ELEMENT, MODEL> sourceConditionPane;


        /*
         * create a new DeatchEffectPane with the selections (probably passed in from last time)
         */
        public DeathEffectPane(String title, Supplier<ELEMENT> conditionSupplier, Editor editor, String applyToHint) {
            this.title = title;
            this.conditionSupplier = conditionSupplier;
            this.sourceConditionPane = new ConditionEffectEditorPane<>(editor);
            if (applyToHint == null || applyToHint == "") {
                this.applyToHint = "";
            } else {
                this.applyToHint = String.format(" (%s)", applyToHint);
            }
        }

        void createDeathEffectPaneContent(FieldUpdateListener listener, boolean writable, EFFECT e, MODEL sourceConditionsModel) {
            effect = e;
            sourceConditionPane.conditionsModel = sourceConditionsModel;

            effectPane = new CollapsiblePanel(title);
            effectPane.setLayout(new JideBoxLayout(effectPane, JideBoxLayout.PAGE_AXIS));


            addFields(listener, writable);
            addLists(listener, writable);
        }

        protected void addFields(FieldUpdateListener listener, boolean writable) {
            effectHPMin = addIntegerField(effectPane, String.format("HP bonus min%s: ", applyToHint), effect.hp_boost_min,
                                          true, writable, listener);
            effectHPMax = addIntegerField(effectPane, String.format("HP bonus max%s: ", applyToHint), effect.hp_boost_max,
                                          true, writable, listener);
            effectAPMin = addIntegerField(effectPane, String.format("AP bonus min%s: ", applyToHint), effect.ap_boost_min,
                                          true, writable, listener);
            effectAPMax = addIntegerField(effectPane, String.format("AP bonus max%s: ", applyToHint), effect.ap_boost_max,
                                          true, writable, listener);
        }

        protected void addLists(FieldUpdateListener listener, boolean writable) {
            String titleSource = String.format("Actor Conditions applied to the source%s: ", applyToHint);
            TimedConditionsCellRenderer cellRendererSource = new TimedConditionsCellRenderer();
            BasicLambdaWithArg<ELEMENT> selectedSetSource = (value) -> sourceConditionPane.selectedCondition = value;
            BasicLambdaWithReturn<ELEMENT> selectedGetSource = () -> sourceConditionPane.selectedCondition;
            BasicLambda selectedResetSource = () -> sourceConditionPane.selectedCondition = null;
            BasicLambdaWithArg<JPanel> updatePaneSource = (editorPane) -> sourceConditionPane.updateEffectTimedConditionEditorPane(
                    editorPane, sourceConditionPane.selectedCondition, listener);

            var resultSource = UiUtils.getCollapsibleItemList(listener, sourceConditionPane.conditionsModel, selectedResetSource,
                                                              selectedSetSource, selectedGetSource, (x) -> {
                    }, updatePaneSource, writable, conditionSupplier, cellRendererSource, titleSource, (x) -> null);
            sourceConditionsList = resultSource.list;
            CollapsiblePanel sourceConditionsPane = resultSource.collapsiblePanel;
            if (effect == null || effect.conditions_source == null || effect.conditions_source.isEmpty()) {
                sourceConditionsPane.collapse();
            }
            effectPane.add(sourceConditionsPane, JideBoxLayout.FIX);
        }

        public boolean valueChanged(JComponent source, Object value, GameDataElement backlink) {
            boolean updateHit = false;
            if (source == effectHPMin) {
                effect.hp_boost_min = (Integer) value;
                updateHit = true;
            } else if (source == effectHPMax) {
                effect.hp_boost_max = (Integer) value;
                updateHit = true;
            } else if (source == effectAPMin) {
                effect.ap_boost_min = (Integer) value;
                updateHit = true;
            } else if (source == effectAPMax) {
                effect.ap_boost_max = (Integer) value;
                updateHit = true;
            } else if (source == sourceConditionsList) {
                updateHit = true;
            } else if (sourceConditionPane.valueChanged(source, value, backlink)) {
                updateHit = true;
            }
            return updateHit;
        }
    }

    static class ConditionEffectEditorPane<LIST_MODEL_SOURCE, ELEMENT extends Common.TimedActorConditionEffect, MODEL extends OrderedListenerListModel<LIST_MODEL_SOURCE, ELEMENT>> {
        private final Editor editor;
        ELEMENT selectedCondition;

        MODEL conditionsModel;
        Editor.MyComboBox conditionBox;
        JSpinner conditionChance;
        JRadioButton conditionClear;
        JRadioButton conditionApply;
        JRadioButton conditionImmunity;
        JSpinner conditionMagnitude;
        JRadioButton conditionTimed;
        JRadioButton conditionForever;
        JSpinner conditionDuration;

        ConditionEffectEditorPane(Editor editor) {
            this.editor = editor;
        }

        public void updateEffectTimedConditionWidgets(ELEMENT condition) {
            boolean writable = editor.target.writable;

            boolean immunity = condition.isImmunity();
            boolean clear = condition.isClear();
            boolean forever = condition.isInfinite();

            conditionClear.setSelected(clear);
            conditionApply.setSelected(!clear && !immunity);
            conditionImmunity.setSelected(immunity);
            conditionTimed.setSelected(!forever);
            conditionForever.setSelected(forever);

            conditionDuration.setEnabled(!clear && !forever && writable);

            conditionClear.setEnabled(writable);
            conditionApply.setEnabled(writable);
            conditionMagnitude.setEnabled(!clear && !immunity && writable);

            conditionImmunity.setEnabled(writable);
            conditionTimed.setEnabled(!clear && writable);
            conditionForever.setEnabled(!clear && writable);
        }

        public void updateEffectTimedConditionEditorPane(JPanel pane, ELEMENT condition, final FieldUpdateListener listener) {
            pane.removeAll();
            if (conditionBox != null) {
                editor.removeElementListener(conditionBox);
            }
            if (condition == null) {
                pane.revalidate();
                pane.repaint();
                return;
            }

            boolean writable = editor.target.writable;
            Project proj = editor.target.getProject();

            conditionBox = editor.addActorConditionBox(pane, proj, "Actor Condition: ", condition.condition, writable,
                                                       listener);
            conditionChance = Editor.addDoubleField(pane, "Chance: ", condition.chance, writable, listener);

            conditionClear = new JRadioButton("Clear active condition");
            pane.add(conditionClear, JideBoxLayout.FIX);
            conditionApply = new JRadioButton("Apply condition with magnitude");
            pane.add(conditionApply, JideBoxLayout.FIX);
            conditionMagnitude = addIntegerField(pane, "Magnitude: ",
                                                 condition.magnitude == null ? null : condition.magnitude >= 0 ? condition.magnitude : 0,
                                                 1, false, writable, listener);
            conditionImmunity = new JRadioButton("Give immunity to condition");
            pane.add(conditionImmunity, JideBoxLayout.FIX);

            ButtonGroup radioEffectGroup = new ButtonGroup();
            radioEffectGroup.add(conditionApply);
            radioEffectGroup.add(conditionClear);
            radioEffectGroup.add(conditionImmunity);

            conditionTimed = new JRadioButton("For a number of rounds");
            pane.add(conditionTimed, JideBoxLayout.FIX);
            conditionDuration = addIntegerField(pane, "Duration: ", condition.duration, 1, false, writable,
                                                listener);
            conditionForever = new JRadioButton("Forever");
            pane.add(conditionForever, JideBoxLayout.FIX);

            ButtonGroup radioDurationGroup = new ButtonGroup();
            radioDurationGroup.add(conditionTimed);
            radioDurationGroup.add(conditionForever);

            updateEffectTimedConditionWidgets(condition);

            conditionClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(conditionClear, conditionClear.isSelected());
                }
            });
            conditionApply.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(conditionApply, conditionApply.isSelected());
                }
            });
            conditionImmunity.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(conditionImmunity, conditionImmunity.isSelected());
                }
            });

            conditionTimed.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(conditionTimed, conditionTimed.isSelected());
                }
            });
            conditionForever.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(conditionForever, conditionForever.isSelected());
                }
            });
            pane.revalidate();
            pane.repaint();
        }

        public boolean valueChanged(JComponent source, Object value, GameDataElement backlink) {
            boolean updateHit = false;

            if (source == conditionBox) {
                if (selectedCondition.condition != null) {
                    selectedCondition.condition.removeBacklink(backlink);
                }
                selectedCondition.condition = (ActorCondition) value;
                if (selectedCondition.condition != null) {
                    selectedCondition.condition.addBacklink(backlink);
                    selectedCondition.condition_id = selectedCondition.condition.id;
                } else {
                    selectedCondition.condition_id = null;
                }
                conditionsModel.itemChanged(selectedCondition);
            } else if (source == conditionClear && (Boolean) value) {
                selectedCondition.magnitude = ActorCondition.MAGNITUDE_CLEAR;
                selectedCondition.duration = null;
                updateEffectTimedConditionWidgets(selectedCondition);
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionApply && (Boolean) value) {
                selectedCondition.magnitude = (Integer) conditionMagnitude.getValue();
                selectedCondition.duration = conditionForever.isSelected() ? ActorCondition.DURATION_FOREVER : (Integer) conditionDuration.getValue();
                setDurationToDefaultIfNone();
                updateEffectTimedConditionWidgets(selectedCondition);
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionImmunity && (Boolean) value) {
                selectedCondition.magnitude = ActorCondition.MAGNITUDE_CLEAR;
                selectedCondition.duration = conditionForever.isSelected() ? ActorCondition.DURATION_FOREVER : (Integer) conditionDuration.getValue();
                setDurationToDefaultIfNone();
                updateEffectTimedConditionWidgets(selectedCondition);
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionMagnitude) {
                selectedCondition.magnitude = (Integer) value;
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionTimed && (Boolean) value) {
                selectedCondition.duration = (Integer) conditionDuration.getValue();
                setDurationToDefaultIfNone();
                updateEffectTimedConditionWidgets(selectedCondition);
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionForever && (Boolean) value) {
                selectedCondition.duration = ActorCondition.DURATION_FOREVER;
                updateEffectTimedConditionWidgets(selectedCondition);
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionDuration) {
                selectedCondition.duration = (Integer) value;
                conditionsModel.itemChanged(selectedCondition);
                updateHit = true;
            } else if (source == conditionChance) {
                selectedCondition.chance = (Double) value;
                conditionsModel.itemChanged(selectedCondition);
            }
            return updateHit;
        }

        private void setDurationToDefaultIfNone() {
            if (selectedCondition.duration == null || selectedCondition.duration == ActorCondition.DURATION_NONE) {
                selectedCondition.duration = 1;
            }
        }
    }

    //region list-models

    public static class TargetTimedConditionsListModel extends OrderedListenerListModel<Common.HitEffect, Common.TimedActorConditionEffect> {
        public TargetTimedConditionsListModel(Common.HitEffect effect) {
            super(effect);
        }

        @Override
        protected java.util.List<Common.TimedActorConditionEffect> getItems() {
            return source.conditions_target;
        }

        @Override
        protected void setItems(java.util.List<Common.TimedActorConditionEffect> items) {
            source.conditions_target = items;
        }
    }

    public static class SourceTimedConditionsListModel extends OrderedListenerListModel<Common.DeathEffect, Common.TimedActorConditionEffect> {
        public SourceTimedConditionsListModel(Common.DeathEffect effect) {
            super(effect);
        }

        @Override
        protected java.util.List<Common.TimedActorConditionEffect> getItems() {
            return source.conditions_source;
        }

        @Override
        protected void setItems(List<Common.TimedActorConditionEffect> items) {
            source.conditions_source = items;
        }
    }

    //endregion
}
