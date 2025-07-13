package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.Quest;
import com.gpl.rpg.atcontentstudio.model.gamedata.QuestStage;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.IntegerBasedCheckBox;
import com.gpl.rpg.atcontentstudio.ui.OrderedListenerListModel;
import com.gpl.rpg.atcontentstudio.utils.BasicLambda;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithArg;
import com.gpl.rpg.atcontentstudio.utils.BasicLambdaWithReturn;
import com.gpl.rpg.atcontentstudio.utils.UiUtils;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuestEditor extends JSONElementEditor {

    private static final long serialVersionUID = 5701667955210615366L;

    private static final String form_view_id = "Form";
    private static final String json_view_id = "JSON";

    private QuestStage selectedStage = null;

    private JTextField idField;
    private JTextField nameField;
    private IntegerBasedCheckBox visibleBox;
    private StagesListModel stagesListModel;
    private JList<QuestStage> stagesList;

    //	private JPanel stagesParamPane;
    private JSpinner progressField;
    private JTextArea logTextField;
    private JSpinner xpRewardField;
    private IntegerBasedCheckBox finishQuestBox;


    public QuestEditor(Quest quest) {
        super(quest, quest.getDesc(), quest.getIcon());
        addEditorTab(form_view_id, getFormView());
        addEditorTab(json_view_id, getJSONView());
    }

    public void insertFormViewDataField(JPanel pane) {
        final Quest quest = ((Quest) target);

        final FieldUpdateListener listener = new QuestFieldUpdater();

        createButtonPane(pane, quest.getProject(), quest, Quest.class, quest.getImage(), null, listener);


        idField = addTextField(pane, "Internal ID: ", quest.id, quest.writable, listener);
        nameField = addTranslatableTextField(pane, "Quest Name: ", quest.name, quest.writable, listener);
        visibleBox = addIntegerBasedCheckBox(pane, "Visible in quest log", quest.visible_in_log, quest.writable, listener);

        String title = "Quest stages: ";
        StagesCellRenderer cellRenderer = new StagesCellRenderer();
        stagesListModel = new StagesListModel(quest);
        BasicLambdaWithArg<QuestStage> selectedSet = (value) -> selectedStage = value;
        BasicLambdaWithReturn<QuestStage> selectedGet = () -> selectedStage;
        BasicLambda selectedReset = () -> selectedStage = null;
        BasicLambdaWithArg<JPanel> updatePane = (editorPane) -> updateStageEditorPane(editorPane, selectedStage, listener);

        var result = UiUtils.getCollapsibleItemList(listener,
                                                    stagesListModel,
                                                    selectedReset,
                                                    selectedSet,
                                                    selectedGet,
                                                    (x) -> {
                                                    },
                                                    updatePane,
                                                    quest.writable,
                                                    () -> new QuestStage(quest),
                                                    cellRenderer,
                                                    title,
                                                    (x) -> null);
        stagesList = result.list;

        if (quest.stages == null || quest.stages.isEmpty()) {
            result.collapsiblePanel.collapse();
        }
        pane.add(result.collapsiblePanel, JideBoxLayout.FIX);
    }

    public void updateStageEditorPane(JPanel pane, QuestStage selectedStage, FieldUpdateListener listener) {
        pane.removeAll();
        if (selectedStage != null) {
            boolean writable = ((Quest) target).writable;
            progressField = addIntegerField(pane, "Progress ID: ", selectedStage.progress, false, writable, listener);
            logTextField = addTranslatableTextArea(pane, "Log text: ", selectedStage.log_text, writable, listener);
            xpRewardField = addIntegerField(pane, "XP Reward: ", selectedStage.exp_reward, false, writable, listener);
            finishQuestBox = addIntegerBasedCheckBox(pane, "Finishes quest", selectedStage.finishes_quest, writable, listener);
            addBacklinksList(pane, selectedStage, "Elements linking to this quest stage");

        }
        pane.revalidate();
        pane.repaint();
    }


    public static class StagesListModel extends OrderedListenerListModel<Quest, QuestStage> {
        public StagesListModel(Quest quest) {
            super(quest);
        }

        @Override
        protected List<QuestStage> getItems() {
            return source.stages;
        }

        @Override
        protected void setItems(List<QuestStage> items) {
            source.stages = items;
        }
    }


    public static class StagesCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 7987880146189575234L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = ((JLabel) c);
                label.setText(((QuestStage) value).getDesc());
                label.setIcon(new ImageIcon(((QuestStage) value).getIcon()));
            }
            return c;
        }
    }


    public class QuestFieldUpdater implements FieldUpdateListener {

        @Override
        public void valueChanged(JComponent source, Object value) {
            Quest quest = (Quest) target;
            if (source == idField) {
                //Events caused by cancel an ID edition. Dismiss.
                if (skipNext) {
                    skipNext = false;
                    return;
                }
                if (target.id.equals((String) value)) return;

                if (idChanging()) {
                    quest.id = (String) value;
                    QuestEditor.this.name = quest.getDesc();
                    quest.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(QuestEditor.this);
                } else {
                    cancelIdEdit(idField);
                    return;
                }
            } else if (source == nameField) {
                quest.name = (String) value;
                QuestEditor.this.name = quest.getDesc();
                quest.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(QuestEditor.this);
            } else if (source == visibleBox) {
                quest.visible_in_log = (Integer) value;
            } else if (source == progressField) {
                selectedStage.progress = (Integer) value;
                stagesListModel.itemChanged(selectedStage);
            } else if (source == logTextField) {
                selectedStage.log_text = (String) value;
                stagesListModel.itemChanged(selectedStage);
            } else if (source == xpRewardField) {
                selectedStage.exp_reward = (Integer) value;
                stagesListModel.itemChanged(selectedStage);
            } else if (source == finishQuestBox) {
                selectedStage.finishes_quest = (Integer) value;
                stagesListModel.itemChanged(selectedStage);
            }


            if (quest.state != GameDataElement.State.modified) {
                quest.state = GameDataElement.State.modified;
                QuestEditor.this.name = quest.getDesc();
                quest.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(QuestEditor.this);
            }
            updateJsonViewText(quest.toJsonString());
        }


    }


}
