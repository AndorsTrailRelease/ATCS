package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarkEntry;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
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

}
