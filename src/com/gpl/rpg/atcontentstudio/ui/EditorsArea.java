package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.saves.SavedGame;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.gpl.rpg.atcontentstudio.ui.gamedataeditors.*;
import com.gpl.rpg.atcontentstudio.ui.map.TMXMapEditor;
import com.gpl.rpg.atcontentstudio.ui.map.WorldMapEditor;
import com.gpl.rpg.atcontentstudio.ui.saves.SavedGameEditor;
import com.gpl.rpg.atcontentstudio.ui.sprites.SpritesheetEditor;
import com.gpl.rpg.atcontentstudio.ui.tools.writermode.WriterModeEditor;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

public class EditorsArea extends JPanel {

    private static final long serialVersionUID = 8801849846876081538L;

    private Map<Object, Editor> editors = new LinkedHashMap<Object, Editor>();
    private JideTabbedPane tabHolder;

    public EditorsArea() {
        super();
        setLayout(new BorderLayout());
        tabHolder = new JideTabbedPane();
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
        add(tabHolder, BorderLayout.CENTER);
    }

    public void openEditor(Editor e) {
        if (!editors.containsKey(e.target) && !editors.containsValue(e)) {
            editors.put(e.target, e);
            tabHolder.addTab(e.name, e.icon, e);
            tabHolder.setSelectedComponent(e);
        }
    }

    public void closeEditor(Editor e) {
        if (editors.containsValue(e)) {
            tabHolder.remove(e);
            editors.remove(e.target);
            e.clearElementListeners();
        }
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


    public void openEditor(SavedGame save) {
        if (editors.containsKey(save)) {
            tabHolder.setSelectedComponent(editors.get(save));
            return;
        }
        openEditor(new SavedGameEditor(save));
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
