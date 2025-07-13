package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.andorstrainer.AndorsTrainer;
import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarkEntry;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.saves.SavedGame;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.jidesoft.swing.TreeSearchable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProjectsTree extends JPanel {

    private static final long serialVersionUID = 6332593891796576708L;

    private JTree projectsTree;

    private JPopupMenu popupMenu;

    private Thread konamiTimeout = null;
    private boolean exit = false;
    private int timeout = 200;
    private Integer[] konamiBuffer = new Integer[]{null, null, null, null, null, null, null, null, null, null};
    private boolean konamiCodeEntered = false;

    public ProjectsTree() {
        super();
        setLayout(new BorderLayout());
        projectsTree = new JTree(new ProjectsTreeModel());
        new TreeSearchable(projectsTree) {
            @Override
            protected String convertElementToString(Object object) {
                return ((ProjectTreeNode) ((TreePath) object).getLastPathComponent()).getDesc();
            }
        };
        add(projectsTree, BorderLayout.CENTER);
        projectsTree.setRootVisible(false);
        projectsTree.setShowsRootHandles(true);
        projectsTree.setExpandsSelectedPaths(true);

        popupMenu = new JPopupMenu();
        makePopupMenu();

        projectsTree.setCellRenderer(new ProjectsTreeCellRenderer());
        projectsTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (projectsTree.getSelectionPath() != null) {
                        itemAction((ProjectTreeNode) projectsTree.getSelectionPath().getLastPathComponent());
                    }
                } else {
                    if (konamiTimeout == null) {
                        startKonamiCount();
                    }
                    int i = 0;
                    while (i < konamiBuffer.length && konamiBuffer[i] != null) {
                        i++;
                    }
                    if (i < konamiBuffer.length) {
                        konamiBuffer[i] = e.getKeyCode();
                        if (!compareBuffers()) {
                            exit = true;
                        } else {
                            resetTimeout();
                        }
                    }
                }
            }
        });
        projectsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupActivated(e);
                } else if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    TreePath path = projectsTree.getPathForLocation(e.getX(), e.getY());
                    projectsTree.setSelectionPath(path);
                    if (path != null) {
                        itemAction((ProjectTreeNode) path.getLastPathComponent());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupActivated(e);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupActivated(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupActivated(e);
                }
            }
        });
        projectsTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
//				List<TreePath> newPaths = new ArrayList<TreePath>();
//				for (TreePath path : e.getPaths()) {
//					if (e.isAddedPath(path)) newPaths.add(path);
//				}
                if (e.getPath() == null) {
                    ATContentStudio.frame.actions.selectionChanged(null, projectsTree.getSelectionPaths());
                } else {
                    ATContentStudio.frame.actions.selectionChanged((ProjectTreeNode) e.getPath().getLastPathComponent(), projectsTree.getSelectionPaths());
                }
            }
        });

    }

    public void makePopupMenu() {
        popupMenu.removeAll();

        if (ATContentStudio.frame == null || ATContentStudio.frame.actions == null) return;
        WorkspaceActions actions = ATContentStudio.frame.actions;

        boolean addNextSeparator = false;
        if (actions.createProject.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.createProject));
        }
        if (actions.openProject.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.openProject));
        }
        if (actions.closeProject.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.closeProject));
        }
        if (actions.deleteProject.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.deleteProject));
        }
        if (addNextSeparator) {
            popupMenu.add(new JSeparator());
            addNextSeparator = false;
        }

        if (actions.saveElement.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.saveElement));
        }
        if (actions.deleteSelected.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.deleteSelected));
        }
        if (addNextSeparator) {
            popupMenu.add(new JSeparator());
            addNextSeparator = false;
        }

        if (actions.createGDE.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.createGDE));
        }
        if (actions.importJSON.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.importJSON));
        }
        if (actions.createMap.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.createMap));
        }
        if (actions.createWorldmap.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.createWorldmap));
        }
        if (actions.loadSave.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.loadSave));
        }
        if (addNextSeparator) {
            popupMenu.add(new JSeparator());
            addNextSeparator = false;
        }


        if (actions.compareItems.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.compareItems));
        }
        if (actions.compareNPCs.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.compareNPCs));
        }
        if (actions.runBeanShell.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.runBeanShell));
        }
        if (actions.exportProject.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.exportProject));
        }
        if (addNextSeparator) {
            popupMenu.add(new JSeparator());
            addNextSeparator = false;
        }

        if (actions.createWriter.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.createWriter));
        }
//		if (actions.testCommitWriter.isEnabled()) {
//			addNextSeparator = true;
//			popupMenu.add(new JMenuItem(actions.testCommitWriter));
//		}
        if (actions.generateWriter.isEnabled()) {
            addNextSeparator = true;
            popupMenu.add(new JMenuItem(actions.generateWriter));
        }
        if (addNextSeparator) {
            popupMenu.add(new JSeparator());
            addNextSeparator = false;
        }

        if (konamiCodeEntered) {
            JMenuItem openTrainer = new JMenuItem("Start Andor's Trainer...");
            popupMenu.add(openTrainer);
            popupMenu.addSeparator();
            openTrainer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new Thread() {
                        public void run() {
                            AndorsTrainer.startApp(false);
                        }
                    }.start();
                }
            });
        }

//		if (projectsTree.getSelectionPath() == null || projectsTree.getSelectionPath().getLastPathComponent() == null) {
//			JMenuItem addProject = new JMenuItem("Create project...");
//			popupMenu.add(addProject);
//			addProject.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					new ProjectCreationWizard().setVisible(true);
//				}
//			});
//			popupMenu.addSeparator();
//		} else if (projectsTree.getSelectionPaths().length > 1) {
//			boolean deleteAll = false;
//			final List<GameDataElement> elementsToDelete = new ArrayList<GameDataElement>();
//			for (TreePath selected : projectsTree.getSelectionPaths()) {
//				if (selected.getLastPathComponent() instanceof GameDataElement && ((GameDataElement)selected.getLastPathComponent()).writable) {
//					elementsToDelete.add((GameDataElement) selected.getLastPathComponent());
//					deleteAll = true;
//				} else {
//					deleteAll = false;
//					break;
//				}
//			}
//			if (deleteAll) {
//				JMenuItem deleteItems = new JMenuItem("Delete all selected elements");
//				popupMenu.add(deleteItems);
//				deleteItems.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						final Map<GameDataCategory<JSONElement>, Set<File>> impactedCategories = new IdentityHashMap<GameDataCategory<JSONElement>, Set<File>>();
//						for (GameDataElement element : elementsToDelete) {
//							ATContentStudio.frame.closeEditor(element);
//							element.childrenRemoved(new ArrayList<ProjectTreeNode>());
//							if (element instanceof JSONElement) {
//								@SuppressWarnings("unchecked")
//								GameDataCategory<JSONElement> category = (GameDataCategory<JSONElement>) element.getParent();
//								category.remove(element);
//								if (impactedCategories.get(category) == null) {
//									impactedCategories.put(category, new HashSet<File>());
//								}
//								impactedCategories.get(category).add(((JSONElement) element).jsonFile);
//							} else if (element instanceof TMXMap) {
//								TMXMapSet parent = (TMXMapSet) element.getParent();
//								parent.tmxMaps.remove(element);
//							}
//						}
//						new Thread() {
//							@Override
//							public void run() {
//								final List<SaveEvent> events = new ArrayList<SaveEvent>();
//								List<SaveEvent> catEvents = null;
//								for (GameDataCategory<JSONElement> category : impactedCategories.keySet()) {
//									for (File f : impactedCategories.get(category)) {
//										catEvents = category.attemptSave(true, f.getName());
//										if (catEvents.isEmpty()) {
//											category.save(f);
//										} else {
//											events.addAll(catEvents);
//										}
//									}
//								}
//								if (!events.isEmpty()) {
//									new SaveItemsWizard(events, null).setVisible(true);
//								}
//							}
//						}.start();
//					}
//				});
//			}
//			
//			popupMenu.addSeparator();
//		} else {
//			final ProjectTreeNode selected = (ProjectTreeNode) projectsTree.getSelectionPath().getLastPathComponent();
//			if (selected instanceof Project) {
//				JMenuItem closeProject = new JMenuItem("Close Project...");
//				JMenuItem deleteProject = new JMenuItem("Delete Project...");
//				popupMenu.add(closeProject);
//				popupMenu.add(deleteProject);
//				popupMenu.addSeparator();
//				closeProject.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						Workspace.closeProject((Project) selected);
//					}
//				});
//				deleteProject.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						int confirm = JOptionPane.showConfirmDialog(ProjectsTree.this, "Are you sure you wish to delete this project ?\nAll files created for it will be deleted too...", "Delete this project ?", JOptionPane.OK_CANCEL_OPTION);
//						if (confirm == JOptionPane.OK_OPTION) {
//							Workspace.deleteProject(((Project)projectsTree.getSelectionPath().getLastPathComponent()));
//						}
//					}
//				});
//			}
//			if (selected instanceof ClosedProject) {
//				JMenuItem openProject = new JMenuItem("Open Project...");
//				JMenuItem deleteProject = new JMenuItem("Delete Project...");
//				popupMenu.add(openProject);
//				popupMenu.add(deleteProject);
//				popupMenu.addSeparator();
//				openProject.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						Workspace.openProject(((ClosedProject)selected));
//					}
//				});
//				deleteProject.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						int confirm = JOptionPane.showConfirmDialog(ProjectsTree.this, "Are you sure you wish to delete this project ?\nAll files created for it will be deleted too...", "Delete this project ?", JOptionPane.OK_CANCEL_OPTION);
//						if (confirm == JOptionPane.OK_OPTION) {
//							Workspace.deleteProject(((ClosedProject)selected));
//						}
//					}
//				});
//			}
//			if (selected.getProject() != null) {
//				final Project proj = ((ProjectTreeNode)selected).getProject();
//				JMenuItem createGDE = new JMenuItem("Create Game Data Element (JSON)");
//				popupMenu.add(createGDE);
//				createGDE.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						new JSONCreationWizard(proj).setVisible(true);
//					}
//				});
//				JMenuItem importJson = new JMenuItem("Import JSON data");
//				popupMenu.add(importJson);
//				importJson.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						new JSONImportWizard(proj).setVisible(true);
//					}
//				});
//				//TODO move somewhere else
//				JMenu compareElementsMenu = new JMenu("Open comparator for...");
//				JMenuItem compareItems = new JMenuItem("Items");
//				compareElementsMenu.add(compareItems);
//				compareItems.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						ATContentStudio.frame.editors.openEditor(new ItemsTableView(selected.getProject()));
//					}
//				});
//				JMenuItem compareNPCs = new JMenuItem("NPCs");
//				compareElementsMenu.add(compareNPCs);
//				compareNPCs.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						ATContentStudio.frame.editors.openEditor(new NPCsTableView(selected.getProject()));
//					}
//				});
//				popupMenu.add(compareElementsMenu);
//				
//				JMenuItem exportProjectPackage = new JMenuItem("Export project");
//				exportProjectPackage.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						JFileChooser chooser = new JFileChooser() {
//							private static final long serialVersionUID = 8039332384370636746L;
//							public boolean accept(File f) {
//								return f.isDirectory() || f.getName().endsWith(".zip") || f.getName().endsWith(".ZIP"); 
//							}
//						};
//						chooser.setMultiSelectionEnabled(false);
//						int result = chooser.showSaveDialog(ATContentStudio.frame);
//						if (result == JFileChooser.APPROVE_OPTION) {
//							selected.getProject().generateExportPackage(chooser.getSelectedFile());
//						}
//					}
//				});
//				popupMenu.add(exportProjectPackage);
//				popupMenu.addSeparator();
//			}
//			if (selected instanceof GameDataElement) {
//				final GameDataElement node = ((GameDataElement)selected); 
//				if (node.state == GameDataElement.State.modified){
//					JMenuItem saveItem = new JMenuItem("Save this element");
//					saveItem.addActionListener(new ActionListener() {
//						@Override
//						public void actionPerformed(ActionEvent e) {
//							node.save();
//							ATContentStudio.frame.nodeChanged(node);
//						}
//					});
//					popupMenu.add(saveItem);
//				}
//				
//				JMenuItem deleteItem = null;
//				if (node.getDataType() == GameSource.Type.created) {
//					deleteItem = new JMenuItem("Delete this element");
//				} else if (node.getDataType() == GameSource.Type.altered) {
//					deleteItem = new JMenuItem("Revert to original");
//				} 
//				if (deleteItem != null) {
//					popupMenu.add(deleteItem);
//					deleteItem.addActionListener(new ActionListener() {
//						@SuppressWarnings("unchecked")
//						@Override
//						public void actionPerformed(ActionEvent e) {
//							ATContentStudio.frame.closeEditor(node);
//							new Thread() {
//								@Override
//								public void run() {
//									node.childrenRemoved(new ArrayList<ProjectTreeNode>());
//									if (node.getParent() instanceof GameDataCategory<?>) {
//										((GameDataCategory<?>)node.getParent()).remove(node);
//										List<SaveEvent> events = node.attemptSave();
//										if (events == null || events.isEmpty()) {
//											node.save();
//										} else {
//											new SaveItemsWizard(events, null).setVisible(true);
//										}
//									}
//								}
//							}.start();
//						}
//					});
//				}
//				popupMenu.addSeparator();
//				
//			}
//			if (selected instanceof Project || selected instanceof SavedGamesSet) {
//				JMenuItem addSave = new JMenuItem("Load saved game...");
//				popupMenu.add(addSave);
//				popupMenu.addSeparator();
//				addSave.addActionListener(new ActionListener() {
//					@Override
//					public void actionPerformed(ActionEvent e) {
//						JFileChooser chooser = new JFileChooser("Select an Andor's Trail save file");
//						if (chooser.showOpenDialog(ATContentStudio.frame) == JFileChooser.APPROVE_OPTION) {
//							selected.getProject().addSave(chooser.getSelectedFile());
//							selected.getProject().save();
//						}
//					}
//				});
//				
//			}
//		}
//		if (konamiCodeEntered) {
//			JMenuItem openTrainer = new JMenuItem("Start Andor's Trainer...");
//			popupMenu.add(openTrainer);
//			popupMenu.addSeparator();
//			openTrainer.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					new Thread() {
//						public void run() {
//							AndorsTrainer.startApp(false);
//						}
//					}.start();
//				}
//			});
//		}
//		JMenu changeLaF = new JMenu("Change Look and Feel");
//		for (final LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
//			final JMenuItem lafItem = new JMenuItem("Switch to "+i.getName());
//			changeLaF.add(lafItem);
//			lafItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					try {
//						UIManager.setLookAndFeel(i.getClassName());
//						SwingUtilities.updateComponentTreeUI(ATContentStudio.frame);
//						ConfigCache.setFavoriteLaFClassName(i.getClassName());
//					} catch (ClassNotFoundException e1) {
//						e1.printStackTrace();
//					} catch (InstantiationException e1) {
//						e1.printStackTrace();
//					} catch (IllegalAccessException e1) {
//						e1.printStackTrace();
//					} catch (UnsupportedLookAndFeelException e1) {
//						e1.printStackTrace();
//					}
//				}
//			});
//		}
//		popupMenu.add(changeLaF);
//		popupMenu.addSeparator();
//		JMenuItem showAbout = new JMenuItem("About...");
//		popupMenu.add(showAbout);
//		popupMenu.addSeparator();
//		showAbout.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				ATContentStudio.frame.showAbout();
//			}
//		});
    }

    public void popupActivated(MouseEvent e) {
        TreePath path = projectsTree.getPathForLocation(e.getX(), e.getY());
        TreePath[] allSelected = projectsTree.getSelectionPaths();
        boolean selectClickedItem = true;
        if (allSelected != null) {
            for (TreePath selected : allSelected) {
                if (selected.equals(path)) {
                    selectClickedItem = false;
                    break;
                }
            }
        }
        if (selectClickedItem) projectsTree.setSelectionPath(path);
        makePopupMenu();
        if (popupMenu.getComponentCount() > 0) {
            popupMenu.show(projectsTree, e.getX(), e.getY());
        }
    }

    public void itemAction(ProjectTreeNode node) {
        if (node instanceof JSONElement) {
            ATContentStudio.frame.openEditor((JSONElement) node);
        } else if (node instanceof Spritesheet) {
            ATContentStudio.frame.openEditor((Spritesheet) node);
        } else if (node instanceof TMXMap) {
            ATContentStudio.frame.openEditor((TMXMap) node);
        } else if (node instanceof WorldmapSegment) {
            ATContentStudio.frame.openEditor((WorldmapSegment) node);
        } else if (node instanceof WriterModeData) {
            ATContentStudio.frame.openEditor((WriterModeData) node);
        } else if (node instanceof BookmarkEntry) {
            ATContentStudio.frame.openEditor(((BookmarkEntry) node).bookmarkedElement);
        } else if (node instanceof SavedGame) {
            if (konamiCodeEntered) {
                ATContentStudio.frame.openEditor((SavedGame) node);
            }
        }
    }

    public class ProjectsTreeModel implements TreeModel {

        public ProjectsTreeModel() {
            Workspace.activeWorkspace.projectsTreeModel = this;
        }

        @Override
        public Object getRoot() {
            return Workspace.activeWorkspace;
        }

        @Override
        public Object getChild(Object parent, int index) {
            return ((ProjectTreeNode) parent).getChildAt(index);
        }

        @Override
        public int getChildCount(Object parent) {
            return ((ProjectTreeNode) parent).getChildCount();
        }

        @Override
        public boolean isLeaf(Object node) {
            return ((ProjectTreeNode) node).isLeaf();
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            //Unused
        }

        public void insertNode(TreePath node) {
            for (TreeModelListener l : listeners) {
                l.treeNodesInserted(new TreeModelEvent(node.getLastPathComponent(), node.getParentPath().getPath(),
                                                       new int[]{((ProjectTreeNode) node.getParentPath().getLastPathComponent()).getIndex((ProjectTreeNode) node.getLastPathComponent())},
                                                       new Object[]{node.getLastPathComponent()}));
            }
        }

        public void changeNode(TreePath node) {
            for (TreeModelListener l : listeners) {
                l.treeNodesChanged(new TreeModelEvent(node.getLastPathComponent(), node.getParentPath(),
                                                      new int[]{((ProjectTreeNode) node.getParentPath().getLastPathComponent()).getIndex((ProjectTreeNode) node.getLastPathComponent())},
                                                      new Object[]{node.getLastPathComponent()}));
            }
        }

        public void removeNode(TreePath node) {
            for (TreeModelListener l : listeners) {
                l.treeNodesRemoved(new TreeModelEvent(node.getLastPathComponent(), node.getParentPath(),
                                                      new int[]{((ProjectTreeNode) node.getParentPath().getLastPathComponent()).getIndex((ProjectTreeNode) node.getLastPathComponent())},
                                                      new Object[]{node.getLastPathComponent()}));
            }
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            return ((ProjectTreeNode) parent).getIndex((ProjectTreeNode) child);
        }

        List<TreeModelListener> listeners = new CopyOnWriteArrayList<TreeModelListener>();

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

    }

    public class ProjectsTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 8100380694034797135L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                String text = ((ProjectTreeNode) value).getDesc();
                if (text != null) label.setText(text);
                Image img;
                if (leaf) img = ((ProjectTreeNode) value).getLeafIcon();
                else if (expanded) img = ((ProjectTreeNode) value).getOpenIcon();
                else img = ((ProjectTreeNode) value).getClosedIcon();

                if (img != null) {
                    label.setIcon(new ImageIcon(img));
                }
            }

            return c;
        }
    }

    public void setSelectedNode(ProjectTreeNode node) {
        List<TreeNode> path = new ArrayList<TreeNode>();
        path.add(node);
        TreeNode parent = node.getParent();
        while (parent != null) {
            path.add(0, parent);
            parent = parent.getParent();
        }
        TreePath tp = new TreePath(path.toArray());
        projectsTree.setSelectionPath(tp);
        projectsTree.scrollPathToVisible(tp);
    }

    protected void startKonamiCount() {
        resetTimeout();
        exit = false;
        konamiTimeout = new Thread() {
            @Override
            public void run() {
                while (!exit && timeout > 0) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    timeout -= 10;
                }
                konamiTimeout = null;
                konamiBuffer = new Integer[]{null, null, null, null, null, null, null, null, null, null};
            }
        };
        konamiTimeout.start();
    }

    protected void resetTimeout() {
        timeout = 400;
    }

    protected boolean compareBuffers() {
        if (konamiBuffer[0] == null) return true;
        else if (konamiBuffer[0] != KeyEvent.VK_UP) return false;

        if (konamiBuffer[1] == null) return true;
        else if (konamiBuffer[1] != KeyEvent.VK_UP) return false;

        if (konamiBuffer[2] == null) return true;
        else if (konamiBuffer[2] != KeyEvent.VK_DOWN) return false;

        if (konamiBuffer[3] == null) return true;
        else if (konamiBuffer[3] != KeyEvent.VK_DOWN) return false;

        if (konamiBuffer[4] == null) return true;
        else if (konamiBuffer[4] != KeyEvent.VK_LEFT) return false;

        if (konamiBuffer[5] == null) return true;
        else if (konamiBuffer[5] != KeyEvent.VK_RIGHT) return false;

        if (konamiBuffer[6] == null) return true;
        else if (konamiBuffer[6] != KeyEvent.VK_LEFT) return false;

        if (konamiBuffer[7] == null) return true;
        else if (konamiBuffer[7] != KeyEvent.VK_RIGHT) return false;

        if (konamiBuffer[8] == null) return true;
        else if (konamiBuffer[8] != KeyEvent.VK_B) return false;

        if (konamiBuffer[9] == null) return true;
        else if (konamiBuffer[9] != KeyEvent.VK_A) return false;

        konamiCodeEntered = true;

        exit = true;
        return true;
    }

}
