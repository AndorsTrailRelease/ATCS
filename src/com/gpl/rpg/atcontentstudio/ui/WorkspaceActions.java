package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.*;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.saves.SavedGamesSet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeDataSet;
import com.gpl.rpg.atcontentstudio.ui.tools.BeanShellView;
import com.gpl.rpg.atcontentstudio.ui.tools.ItemsTableView;
import com.gpl.rpg.atcontentstudio.ui.tools.NPCsTableView;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorkspaceActions {

    ProjectTreeNode selectedNode = null;
    TreePath[] selectedPaths = null;

    public ATCSAction createProject = new ATCSAction("Create project...", "Opens the project creation wizard") {
        public void actionPerformed(ActionEvent e) {
            new ProjectCreationWizard().setVisible(true);
        }

    };


    public ATCSAction closeProject = new ATCSAction("Close project", "Closes the project, unloading all resources from memory") {
        public void actionPerformed(ActionEvent e) {
            if (!(selectedNode instanceof Project)) return;
            Workspace.closeProject((Project) selectedNode);
            selectedNode = null;
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode instanceof Project);
        }

    };


    public ATCSAction openProject = new ATCSAction("Open project", "Opens the project, loading all necessary resources in memory") {
        public void actionPerformed(ActionEvent e) {
            if (!(selectedNode instanceof ClosedProject)) return;
            Workspace.openProject((ClosedProject) selectedNode);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode instanceof ClosedProject);
        }

    };

    public ATCSAction deleteProject = new ATCSAction("Delete project", "Deletes the project, and all created/altered data, from disk") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode instanceof Project) {
                if (JOptionPane.showConfirmDialog(ATContentStudio.frame, "Are you sure you wish to delete this project ?\nAll files created for it will be deleted too...", "Delete this project ?",
                                                  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Workspace.deleteProject((Project) selectedNode);
                }
            } else if (selectedNode instanceof ClosedProject) {
                if (JOptionPane.showConfirmDialog(ATContentStudio.frame, "Are you sure you wish to delete this project ?\nAll files created for it will be deleted too...", "Delete this project ?",
                                                  JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Workspace.deleteProject((ClosedProject) selectedNode);
                }
            }
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode instanceof Project || selectedNode instanceof ClosedProject);
        }

    };

    public ATCSAction saveElement = new ATCSAction("Save this element", "Saves the current state of this element on disk") {
        public void actionPerformed(ActionEvent e) {
            if (!(selectedNode instanceof GameDataElement)) return;
            final GameDataElement node = ((GameDataElement) selectedNode);
            if (node.needsSaving()) {
                node.save();
                ATContentStudio.frame.nodeChanged(node);
            }
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            if (selectedNode instanceof GameDataElement) {
                setEnabled(((GameDataElement) selectedNode).needsSaving());
            } else {
                setEnabled(false);
            }
        }

    };

    public ATCSAction deleteSelected = new ATCSAction("Delete", "Deletes the selected items") {
        boolean multiMode = false;
        List<GameDataElement> elementsToDelete = null;

        public void init() {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        public void actionPerformed(ActionEvent e) {
            if (multiMode) {
                if (elementsToDelete == null) return;
                final Map<GameDataCategory<JSONElement>, Set<File>> impactedCategories = new IdentityHashMap<GameDataCategory<JSONElement>, Set<File>>();
                for (GameDataElement element : elementsToDelete) {
                    ATContentStudio.frame.closeEditor(element);
                    element.childrenRemoved(new ArrayList<ProjectTreeNode>());
                    if (element instanceof JSONElement) {
                        if (element.getParent() instanceof GameDataCategory<?>) {
                            @SuppressWarnings("unchecked")
                            GameDataCategory<JSONElement> category = (GameDataCategory<JSONElement>) element.getParent();
                            category.remove(element);
                            if (impactedCategories.get(category) == null) {
                                impactedCategories.put(category, new HashSet<File>());
                            }

                            GameDataElement newOne = element.getProject().getGameDataElement(((JSONElement) element).getClass(), element.id);
                            if (element instanceof Quest) {
                                for (QuestStage oldStage : ((Quest) element).stages) {
                                    QuestStage newStage = newOne != null ? ((Quest) newOne).getStage(oldStage.progress) : null;
                                    for (GameDataElement backlink : oldStage.getBacklinks()) {
                                        backlink.elementChanged(oldStage, newStage);
                                    }
                                }
                            }
                            for (GameDataElement backlink : element.getBacklinks()) {
                                backlink.elementChanged(element, newOne);
                            }
                            impactedCategories.get(category).add(((JSONElement) element).jsonFile);
                        }
                    } else if (element instanceof TMXMap) {
                        ((TMXMap) element).delete();
                        GameDataElement newOne = element.getProject().getMap(element.id);
                        for (GameDataElement backlink : element.getBacklinks()) {
                            backlink.elementChanged(element, newOne);
                        }
                    } else if (element instanceof WriterModeData) {
                        WriterModeDataSet parent = (WriterModeDataSet) element.getParent();
                        parent.writerModeDataList.remove(element);
                    } else if (element instanceof WorldmapSegment) {
                        if (element.getParent() instanceof Worldmap) {
                            ((Worldmap) element.getParent()).remove(element);
                            element.save();
                            for (GameDataElement backlink : element.getBacklinks()) {
                                backlink.elementChanged(element, element.getProject().getWorldmapSegment(element.id));
                            }
                        }
                    }
                }
                new Thread() {
                    @Override
                    public void run() {
                        final List<SaveEvent> events = new ArrayList<SaveEvent>();
                        List<SaveEvent> catEvents;
                        for (GameDataCategory<JSONElement> category : impactedCategories.keySet()) {
                            for (File f : impactedCategories.get(category)) {
                                catEvents = category.attemptSave(true, f.getName());
                                if (catEvents.isEmpty()) {
                                    category.save(f);
                                } else {
                                    events.addAll(catEvents);
                                }
                            }
                        }
                        if (!events.isEmpty()) {
                            new SaveItemsWizard(events, null).setVisible(true);
                        }
                    }
                }.start();
            } else {
                if (!(selectedNode instanceof GameDataElement)) return;
                final GameDataElement node = ((GameDataElement) selectedNode);
                ATContentStudio.frame.closeEditor(node);
                new Thread() {
                    @Override
                    public void run() {
                        node.childrenRemoved(new ArrayList<ProjectTreeNode>());
                        if (node instanceof JSONElement) {
                            if (node.getParent() instanceof GameDataCategory<?>) {
                                ((GameDataCategory<?>) node.getParent()).remove(node);
                                List<SaveEvent> events = node.attemptSave();
                                if (events == null || events.isEmpty()) {
                                    node.save();
                                } else {
                                    new SaveItemsWizard(events, null).setVisible(true);
                                }
                                GameDataElement newOne = node.getProject().getGameDataElement(((JSONElement) node).getClass(), node.id);
                                if (node instanceof Quest) {
                                    for (QuestStage oldStage : ((Quest) node).stages) {
                                        QuestStage newStage = newOne != null ? ((Quest) newOne).getStage(oldStage.progress) : null;
                                        for (GameDataElement backlink : oldStage.getBacklinks()) {
                                            backlink.elementChanged(oldStage, newStage);
                                        }
                                    }
                                }
                                for (GameDataElement backlink : node.getBacklinks()) {
                                    backlink.elementChanged(node, newOne);
                                }
                            }
//							((GameDataCategory<?>)node.getParent()).remove(node);
//							List<SaveEvent> events = node.attemptSave();
//							if (events == null || events.isEmpty()) {
//								node.save();
//							} else {
//								new SaveItemsWizard(events, null).setVisible(true);
//							}
                        } else if (node instanceof TMXMap) {
                            ((TMXMap) node).delete();
                            GameDataElement newOne = node.getProject().getMap(node.id);
                            for (GameDataElement backlink : node.getBacklinks()) {
                                backlink.elementChanged(node, newOne);
                            }
                        } else if (node instanceof WriterModeData) {
                            WriterModeDataSet parent = (WriterModeDataSet) node.getParent();
                            parent.writerModeDataList.remove(node);
                        } else if (node instanceof WorldmapSegment) {
                            if (node.getParent() instanceof Worldmap) {
                                ((Worldmap) node.getParent()).remove(node);
                                node.save();
                                for (GameDataElement backlink : node.getBacklinks()) {
                                    backlink.elementChanged(node, node.getProject().getWorldmapSegment(node.id));
                                }
                            }
                        }
                    }
                }.start();
            }
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            elementsToDelete = null;
            if (selectedPaths != null && selectedPaths.length > 1) {
                multiMode = false;
                elementsToDelete = new ArrayList<GameDataElement>();
                for (TreePath selected : selectedPaths) {
                    if (selected.getLastPathComponent() instanceof GameDataElement && ((GameDataElement) selected.getLastPathComponent()).writable) {
                        elementsToDelete.add((GameDataElement) selected.getLastPathComponent());
                    }
                }
                multiMode = elementsToDelete.size() > 1;
                putValue(Action.NAME, "Delete all selected elements");
                setEnabled(multiMode);
            } else if (selectedNode instanceof GameDataElement && ((GameDataElement) selectedNode).writable) {
                multiMode = false;
                if (selectedNode.getDataType() == GameSource.Type.created) {
                    putValue(Action.NAME, "Delete this element");
                    setEnabled(true);
                } else if (selectedNode.getDataType() == GameSource.Type.altered) {
                    putValue(Action.NAME, "Revert to original");
                    setEnabled(true);
                } else {
                    setEnabled(false);
                }
            } else {
                setEnabled(false);
            }
        }

    };

    public ATCSAction createGDE = new ATCSAction("Create Game Data Element (JSON)", "Opens the game object creation wizard") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new JSONCreationWizard(selectedNode.getProject()).setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction createMap = new ATCSAction("Create TMX Map", "Opens the TMX Map creation wizard") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new TMXMapCreationWizard(selectedNode.getProject()).setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction createWorldmap = new ATCSAction("Create Worldmap segment", "Opens the worldmap segment creation wizard") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new WorldmapCreationWizard(selectedNode.getProject()).setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction importJSON = new ATCSAction("Import JSON data", "Opens the JSON import wizard") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new JSONImportWizard(selectedNode.getProject()).setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction loadSave = new ATCSAction("Load saved game...", "Opens the saved game loading wizard") {
        public void actionPerformed(ActionEvent e) {
            if (!(selectedNode instanceof Project || selectedNode instanceof SavedGamesSet)) return;
            JFileChooser chooser = new JFileChooser("Select an Andor's Trail save file");
            if (chooser.showOpenDialog(ATContentStudio.frame) == JFileChooser.APPROVE_OPTION) {
                selectedNode.getProject().addSave(chooser.getSelectedFile());
                selectedNode.getProject().save();
            }
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode instanceof Project || selectedNode instanceof SavedGamesSet);
        }

    };

    public ATCSAction compareItems = new ATCSAction("Items comparator", "Opens an editor showing all the items of the project in a table") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            ATContentStudio.frame.editors.openEditor(new ItemsTableView(selectedNode.getProject()));
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction compareNPCs = new ATCSAction("NPCs comparator", "Opens an editor showing all the NPCs of the project in a table") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            ATContentStudio.frame.editors.openEditor(new NPCsTableView(selectedNode.getProject()));
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };

    public ATCSAction exportProject = new ATCSAction("Export project", "Generates a zip file containing all the created & altered resources of the project, ready to merge with the game source.") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new ExportProjectWizard(selectedNode.getProject()).setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }

    };

    public ATCSAction runBeanShell = new ATCSAction("Run Beanshell console", "Opens a beanshell scripting pad.") {
        public void actionPerformed(ActionEvent e) {
            new BeanShellView();
        }

    };

    public ATCSAction showAbout = new ATCSAction("About...", "Displays credits and other informations about ATCS") {
        public void actionPerformed(ActionEvent e) {
            ATContentStudio.frame.showAbout();
        }

    };

    public ATCSAction exitATCS = new ATCSAction("Exit", "Closes the program") {
        public void actionPerformed(ActionEvent e) {
            if (Workspace.activeWorkspace.needsSaving()) {
                int answer = JOptionPane.showConfirmDialog(ATContentStudio.frame, "There are unsaved changes in your workspace.\nExiting ATCS will discard these changes.\nDo you really want to exit?",
                                                           "Unsaved changes. Confirm exit.", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            } else {
                System.exit(0);
            }
        }

    };

    public ATCSAction createWriter = new ATCSAction("Create dialogue sketch", "Create a dialogue sketch for fast dialogue edition") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null) return;
            new WriterSketchCreationWizard(selectedNode.getProject()).setVisible(true);
//			
//			
//			if (selectedNode == null || selectedNode.getProject() == null) return;
//			WriterModeData data = new WriterModeData(selectedNode.getProject().createdContent.writerModeDataSet, "test_");
//			JFrame frame = new JFrame("Writer Mode tests");
//			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//			frame.getContentPane().setLayout(new BorderLayout());
//			frame.getContentPane().add(new WriterModeEditor(data), BorderLayout.CENTER);
//			frame.setMinimumSize(new Dimension(250, 200));
//			frame.pack();
//			frame.setVisible(true);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode.getProject() != null);
        }
    };
	
	/*public ATCSAction testCommitWriter = new ATCSAction("Export dialogue sketch", "Exports the dialogue sketch as real JSON data dialogues") {
		public void actionPerformed(ActionEvent e) {
			if (selectedNode == null || selectedNode.getProject() == null || !(selectedNode instanceof WriterModeData)) return;
			WriterModeData wData = (WriterModeData)selectedNode;
			Collection<Dialogue> exported = wData.toDialogue();
			selectedNode.getProject().createElements(new ArrayList<JSONElement>(exported));
			wData.begin.dialogue.save();
			wData.save();
		};
		public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
			setEnabled(selectedNode != null && selectedNode instanceof WriterModeData);
		}
	};*/

    public ATCSAction generateWriter = new ATCSAction("Generate dialogue sketch", "Generates a dialogue sketch from this dialogue and its tree.") {
        public void actionPerformed(ActionEvent e) {
            if (selectedNode == null || selectedNode.getProject() == null || !(selectedNode instanceof Dialogue))
                return;
            new WriterSketchCreationWizard(selectedNode.getProject(), (Dialogue) selectedNode).setVisible(true);

        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(selectedNode != null && selectedNode instanceof Dialogue);
        }
    };

    public ATCSAction editWorkspaceSettings = new ATCSAction("Edit Workspace Settings", "Change the preferences of this workspace.") {
        public void actionPerformed(ActionEvent e) {
            new WorkspaceSettingsEditor(Workspace.activeWorkspace.settings);
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
            setEnabled(true);
        }

    };

    List<ATCSAction> actions = new ArrayList<WorkspaceActions.ATCSAction>();

    public WorkspaceActions() {
        actions.add(createProject);
        actions.add(closeProject);
        actions.add(openProject);
        actions.add(deleteProject);
        actions.add(saveElement);
        actions.add(deleteSelected);
        actions.add(createGDE);
        actions.add(createMap);
        actions.add(importJSON);
        actions.add(loadSave);
        actions.add(compareItems);
        actions.add(compareNPCs);
        actions.add(exportProject);
        actions.add(showAbout);
        actions.add(exitATCS);
        actions.add(createWriter);
//		actions.add(testCommitWriter);
        actions.add(generateWriter);
        actions.add(editWorkspaceSettings);
        selectionChanged(null, null);
    }

    public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
        this.selectedNode = selectedNode;
        this.selectedPaths = selectedPaths;
        synchronized (actions) {
            for (ATCSAction action : actions) {
                action.selectionChanged(selectedNode, selectedPaths);
            }
        }
    }

    public static class ATCSAction implements Action {

        boolean enabled = true;


        public ATCSAction(String name, String desc) {
            putValue(Action.NAME, name);
            putValue(Action.SHORT_DESCRIPTION, desc);
            init();
        }

        public void init() {
        }

        public void selectionChanged(ProjectTreeNode selectedNode, TreePath[] selectedPaths) {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }

        public Map<String, Object> values = new LinkedHashMap<String, Object>();

        @Override
        public Object getValue(String key) {
            return values.get(key);
        }

        @Override
        public void putValue(String key, Object value) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, key, values.get(key), value);
            values.put(key, value);
            for (PropertyChangeListener l : listeners) {
                l.propertyChange(event);
            }
        }

        @Override
        public void setEnabled(boolean b) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, "enabled", isEnabled(), b);
            enabled = b;
            for (PropertyChangeListener l : listeners) {
                l.propertyChange(event);
            }
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        private List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            listeners.remove(listener);
        }

    }

}
