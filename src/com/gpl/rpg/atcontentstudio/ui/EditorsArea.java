package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Preferences.OpenEditorState;
import com.gpl.rpg.atcontentstudio.model.Preferences.OpenEditorState.TargetType;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.gpl.rpg.atcontentstudio.ui.gamedataeditors.*;
import com.gpl.rpg.atcontentstudio.ui.map.TMXMapEditor;
import com.gpl.rpg.atcontentstudio.ui.map.WorldMapEditor;
import com.gpl.rpg.atcontentstudio.ui.sprites.SpritesheetEditor;
import com.gpl.rpg.atcontentstudio.ui.tools.writermode.WriterModeEditor;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditorsArea extends JPanel {

    private static final long serialVersionUID = 8801849846876081538L;
    private static final String SAVE_CURRENT_EDITOR_ACTION_KEY = "saveCurrentEditor";
    private static final String CLOSE_CURRENT_EDITOR_ACTION_KEY = "closeCurrentEditor";

    private Map<Object, Editor> editors = new LinkedHashMap<Object, Editor>();
    private DraggableJideTabbedPane tabHolder;
    private final Action saveCurrentEditorAction;
    private final Action closeCurrentEditorAction;

    private void updateCurrentEditorActions() {
        Component selected = tabHolder == null ? null : tabHolder.getSelectedComponent();
        saveCurrentEditorAction.setEnabled(selected instanceof Editor && ((Editor) selected).canSaveCurrent());
    }

    public EditorsArea() {
        super();
        setLayout(new BorderLayout());
        saveCurrentEditorAction = new AbstractAction("Save this element") {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentEditor();
            }
        };
        saveCurrentEditorAction.putValue(Action.SHORT_DESCRIPTION, "Saves the currently selected editor");
        saveCurrentEditorAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        closeCurrentEditorAction = new AbstractAction("Close Tab") {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeCurrentEditor();
            }
        };
        closeCurrentEditorAction.putValue(Action.SHORT_DESCRIPTION, "Closes the currently selected editor tab");
        closeCurrentEditorAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

        tabHolder = new DraggableJideTabbedPane();
        tabHolder.setTabPlacement(JideTabbedPane.TOP);
        tabHolder.setTabShape(JideTabbedPane.SHAPE_FLAT);
        tabHolder.setUseDefaultShowCloseButtonOnTab(false);
        tabHolder.setShowCloseButtonOnTab(true);
        tabHolder.setCloseAction(new Action() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeEditor((Editor) e.getSource());
            }

            @Override
            public void setEnabled(boolean b) {
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }

            @Override
            public void putValue(String key, Object value) {
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }
        });

        tabHolder.addChangeListener(e -> {
            Component selected = tabHolder.getSelectedComponent();
            if(ATContentStudio.frame == null) return; // Not initialized yet
            updateCurrentEditorActions();
            updateSavedOpenEditorStates();
            if (selected instanceof Editor) {
                Object target = ((Editor) selected).target;
                if (target instanceof ProjectTreeNode) {
                    ATContentStudio.frame.selectInTree((ProjectTreeNode) target);
                }
            }
        });

        KeyStroke saveShortcut = (KeyStroke) saveCurrentEditorAction.getValue(Action.ACCELERATOR_KEY);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(saveShortcut, SAVE_CURRENT_EDITOR_ACTION_KEY);
        getActionMap().put(SAVE_CURRENT_EDITOR_ACTION_KEY, saveCurrentEditorAction);

        KeyStroke closeTabShortcut = (KeyStroke) closeCurrentEditorAction.getValue(Action.ACCELERATOR_KEY);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(closeTabShortcut, CLOSE_CURRENT_EDITOR_ACTION_KEY);
        getActionMap().put(CLOSE_CURRENT_EDITOR_ACTION_KEY, closeCurrentEditorAction);

        updateCurrentEditorActions();

        add(tabHolder, BorderLayout.CENTER);
    }

    public Action getCloseCurrentEditorAction() {
        return closeCurrentEditorAction;
    }

    public Action getSaveCurrentEditorAction() {
        return saveCurrentEditorAction;
    }

    public void saveCurrentEditor() {
        Component selected = tabHolder.getSelectedComponent();
        if (selected instanceof Editor) {
            ((Editor) selected).saveCurrent();
        }
    }

    public void openEditor(Editor e) {
        if (!editors.containsKey(e.target) && !editors.containsValue(e)) {
            editors.put(e.target, e);
            tabHolder.addTab(e.name, e.icon, e);
            tabHolder.setSelectedComponent(e);
            updateCurrentEditorActions();
            updateSavedOpenEditorStates();
        }
    }

    public void closeEditor(Editor e) {
        if (editors.containsValue(e)) {
            tabHolder.remove(e);
            editors.remove(e.target);
            e.clearElementListeners();
            updateCurrentEditorActions();
            updateSavedOpenEditorStates();
        }
    }

    public void closeCurrentEditor() {
        Component selected = tabHolder.getSelectedComponent();
        if (selected instanceof Editor) {
            closeEditor((Editor) selected);
        }
    }

    public ProjectTreeNode getSelectedEditorTarget() {
        Component selected = tabHolder.getSelectedComponent();
        if (!(selected instanceof Editor)) {
            return null;
        }

        GameDataElement target = ((Editor) selected).target;
        if (target instanceof ProjectTreeNode) {
            return (ProjectTreeNode) target;
        }
        return null;
    }

    public List<OpenEditorState> captureOpenEditorStates() {
        List<OpenEditorState> openEditorStates = new ArrayList<OpenEditorState>();
        Component selectedComponent = tabHolder.getSelectedComponent();
        for (int i = 0; i < tabHolder.getTabCount(); i++) {
            Component component = tabHolder.getComponentAt(i);
            if (!(component instanceof Editor)) {
                continue;
            }

            OpenEditorState openEditorState = createOpenEditorState((Editor) component, component == selectedComponent);
            if (openEditorState != null) {
                openEditorStates.add(openEditorState);
            }
        }
        return openEditorStates;
    }

    public boolean restoreOpenEditorStates(List<OpenEditorState> openEditorStates) {
        if (openEditorStates == null || openEditorStates.isEmpty()) {
            return false;
        }

        Editor selectedEditor = null;
        int restoredCount = 0;
        for (OpenEditorState openEditorState : openEditorStates) {
            ProjectTreeNode target = resolveOpenEditorState(openEditorState);
            if (target == null) {
                continue;
            }

            openEditorForTarget(target);
            restoredCount++;
            if (openEditorState.selected) {
                Editor restoredEditor = editors.get(target);
                if (restoredEditor != null) {
                    selectedEditor = restoredEditor;
                }
            }
        }

        if (selectedEditor != null) {
            tabHolder.setSelectedComponent(selectedEditor);
        }
        return restoredCount > 0;
    }

    private OpenEditorState createOpenEditorState(Editor editor, boolean selected) {
        if (editor == null || editor.target == null) {
            return null;
        }

        return createOpenEditorState(editor.target, selected);
    }

    static OpenEditorState createOpenEditorState(GameDataElement target, boolean selected) {
        if (target == null) {
            return null;
        }

        Project project = target.getProject();
        if (project == null || project.name == null || target.id == null) {
            return null;
        }

        TargetType targetType = getTargetType(target.getClass());
        if (targetType == null) {
            return null;
        }

        return new OpenEditorState(project.name, targetType, target.id, selected);
    }

    private TargetType getTargetType(ProjectTreeNode target) {
        return target == null ? null : getTargetType(target.getClass());
    }

    static TargetType getTargetType(Class<?> targetClass) {
        if (targetClass == null) {
            return null;
        }
        if (ActorCondition.class.isAssignableFrom(targetClass)) {
            return TargetType.actorCondition;
        }
        if (Dialogue.class.isAssignableFrom(targetClass)) {
            return TargetType.dialogue;
        }
        if (Droplist.class.isAssignableFrom(targetClass)) {
            return TargetType.droplist;
        }
        if (ItemCategory.class.isAssignableFrom(targetClass)) {
            return TargetType.itemCategory;
        }
        if (Item.class.isAssignableFrom(targetClass)) {
            return TargetType.item;
        }
        if (NPC.class.isAssignableFrom(targetClass)) {
            return TargetType.npc;
        }
        if (Quest.class.isAssignableFrom(targetClass)) {
            return TargetType.quest;
        }
        if (TMXMap.class.isAssignableFrom(targetClass)) {
            return TargetType.map;
        }
        if (Spritesheet.class.isAssignableFrom(targetClass)) {
            return TargetType.spritesheet;
        }
        if (WorldmapSegment.class.isAssignableFrom(targetClass)) {
            return TargetType.worldmapSegment;
        }
        if (WriterModeData.class.isAssignableFrom(targetClass)) {
            return TargetType.writerSketch;
        }
        return null;
    }

    private ProjectTreeNode resolveOpenEditorState(OpenEditorState openEditorState) {
        if (openEditorState == null || openEditorState.projectName == null || openEditorState.targetType == null || openEditorState.targetId == null) {
            return null;
        }

        Project project = findOpenProject(openEditorState.projectName);
        if (project == null) {
            return null;
        }

        return resolveOpenEditorState(project, openEditorState);
    }

    static ProjectTreeNode resolveOpenEditorState(Project project, OpenEditorState openEditorState) {
        if (project == null || openEditorState == null || openEditorState.targetType == null || openEditorState.targetId == null) {
            return null;
        }

        switch (openEditorState.targetType) {
            case actorCondition:
                return project.getActorCondition(openEditorState.targetId);
            case dialogue:
                return project.getDialogue(openEditorState.targetId);
            case droplist:
                return project.getDroplist(openEditorState.targetId);
            case itemCategory:
                return project.getItemCategory(openEditorState.targetId);
            case item:
                return project.getItem(openEditorState.targetId);
            case npc:
                return project.getNPC(openEditorState.targetId);
            case quest:
                return project.getQuest(openEditorState.targetId);
            case map:
                return project.getMap(openEditorState.targetId);
            case spritesheet:
                return project.getSpritesheet(openEditorState.targetId);
            case worldmapSegment:
                return project.getWorldmapSegment(openEditorState.targetId);
            case writerSketch:
                return project.getWriterSketch(openEditorState.targetId);
            default:
                return null;
        }
    }

    private Project findOpenProject(String projectName) {
        if (Workspace.activeWorkspace == null || projectName == null) {
            return null;
        }

        for (ProjectTreeNode node : Workspace.activeWorkspace.projects) {
            if (node instanceof Project) {
                Project project = (Project) node;
                if (projectName.equals(project.name)) {
                    return project;
                }
            }
        }
        return null;
    }

    private void openEditorForTarget(ProjectTreeNode target) {
        if (target instanceof JSONElement) {
            openEditor((JSONElement) target);
        } else if (target instanceof Spritesheet) {
            openEditor((Spritesheet) target);
        } else if (target instanceof TMXMap) {
            openEditor((TMXMap) target);
        } else if (target instanceof WorldmapSegment) {
            openEditor((WorldmapSegment) target);
        } else if (target instanceof WriterModeData) {
            openEditor((WriterModeData) target);
        }
    }

    private void updateSavedOpenEditorStates() {
        if (Workspace.activeWorkspace == null || Workspace.activeWorkspace.preferences == null) {
            return;
        }
        Workspace.activeWorkspace.preferences.openEditors = captureOpenEditorStates();
    }

    public void openEditor(JSONElement node) {
        if (editors.containsKey(node)) {
            tabHolder.setSelectedComponent(editors.get(node));
            return;
        }
        if (node instanceof Quest) {
            openEditor(new QuestEditor((Quest) node));
        } else if (node instanceof Dialogue) {
            openEditor(new DialogueEditor((Dialogue) node));
        } else if (node instanceof Droplist) {
            openEditor(new DroplistEditor((Droplist) node));
        } else if (node instanceof ActorCondition) {
            openEditor(new ActorConditionEditor((ActorCondition) node));
        } else if (node instanceof ItemCategory) {
            openEditor(new ItemCategoryEditor((ItemCategory) node));
        } else if (node instanceof Item) {
            openEditor(new ItemEditor((Item) node));
        } else if (node instanceof NPC) {
            openEditor(new NPCEditor((NPC) node));
        }
    }

    public void openEditor(Spritesheet node) {
        if (editors.containsKey(node)) {
            tabHolder.setSelectedComponent(editors.get(node));
            return;
        }
        node.link();
        openEditor(new SpritesheetEditor((Spritesheet) node));
    }

    public void openEditor(TMXMap node) {
        if (editors.containsKey(node)) {
            tabHolder.setSelectedComponent(editors.get(node));
            return;
        }
        node.link();
        openEditor(new TMXMapEditor(node));
    }


    public void openEditor(WorldmapSegment node) {
        if (editors.containsKey(node)) {
            tabHolder.setSelectedComponent(editors.get(node));
            return;
        }
        node.link();
        openEditor(new WorldMapEditor(node));
    }

    public void openEditor(WriterModeData node) {
        if (editors.containsKey(node)) {
            tabHolder.setSelectedComponent(editors.get(node));
            return;
        }
        node.link();
        openEditor(new WriterModeEditor(node));
    }

    public void closeEditor(ProjectTreeNode node) {
        if (editors.containsKey(node)) {
            closeEditor(editors.get(node));
        }
    }

    public void editorTabChanged(Editor e) {
        int index = tabHolder.indexOfComponent(e);
        if (index >= 0) {
            tabHolder.setTitleAt(index, e.name);
            tabHolder.setIconAt(index, e.icon);
        }
        updateCurrentEditorActions();
    }

    public void editorTabChanged(ProjectTreeNode node) {
        if (editors.get(node) != null) {
            editors.get(node).targetUpdated();
            editorTabChanged(editors.get(node));
        }
    }

    public void showAbout() {
        if (editors.containsKey(AboutEditor.instance)) {
            tabHolder.setSelectedComponent(AboutEditor.instance);
            return;
        }
        openEditor(AboutEditor.instance);
    }

}
