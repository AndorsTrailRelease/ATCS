package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.ClosedProject;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Preferences.TreeNodeState;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarkEntry;
import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarkFolder;
import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarksRoot;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeDataSet;
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
                if (ATContentStudio.frame == null || ATContentStudio.frame.actions == null) {
                    return;
                }
                if (e.getPath() == null) {
                    ATContentStudio.frame.actions.selectionChanged(null, projectsTree.getSelectionPaths());
                } else {
                    ATContentStudio.frame.actions.selectionChanged((ProjectTreeNode) e.getPath().getLastPathComponent(), projectsTree.getSelectionPaths());
                }
            }
        });

    }

    public java.util.List<TreeNodeState> captureExpandedTreeNodes() {
        java.util.List<TreeNodeState> expandedTreeNodes = new ArrayList<TreeNodeState>();
        Object root = projectsTree.getModel().getRoot();
        if (!(root instanceof ProjectTreeNode)) {
            return expandedTreeNodes;
        }

        java.util.Enumeration<TreePath> expandedPaths = projectsTree.getExpandedDescendants(new TreePath(new Object[]{root}));
        if (expandedPaths == null) {
            return expandedTreeNodes;
        }

        while (expandedPaths.hasMoreElements()) {
            TreeNodeState treeNodeState = createTreeNodeState(expandedPaths.nextElement());
            if (treeNodeState != null && treeNodeState.path != null && !treeNodeState.path.isEmpty()) {
                expandedTreeNodes.add(treeNodeState);
            }
        }
        return expandedTreeNodes;
    }

    public TreeNodeState captureSelectedTreeNode() {
        return createTreeNodeState(projectsTree.getSelectionPath());
    }

    public void collapseAll() {
        Object root = projectsTree.getModel().getRoot();
        if (!(root instanceof ProjectTreeNode)) {
            return;
        }

        collapseRecursively(new TreePath(new Object[]{root}));
        projectsTree.clearSelection();
    }

    private void collapseRecursively(TreePath parentPath) {
        Object lastPathComponent = parentPath.getLastPathComponent();
        if (!(lastPathComponent instanceof ProjectTreeNode)) {
            return;
        }

        ProjectTreeNode node = (ProjectTreeNode) lastPathComponent;
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode child = node.getChildAt(i);
            collapseRecursively(parentPath.pathByAddingChild(child));
        }

        if (parentPath.getPathCount() > 1) {
            projectsTree.collapsePath(parentPath);
        }
    }

    public boolean restoreTreeState(java.util.List<TreeNodeState> expandedTreeNodes, TreeNodeState selectedTreeNode) {
        Object root = projectsTree.getModel().getRoot();
        if (!(root instanceof ProjectTreeNode)) {
            return false;
        }

        collapseAll();

        boolean restored = false;
        java.util.List<TreeNodeState> sortedExpandedTreeNodes = new ArrayList<TreeNodeState>();
        if (expandedTreeNodes != null) {
            sortedExpandedTreeNodes.addAll(expandedTreeNodes);
        }
        sortedExpandedTreeNodes.sort((left, right) -> Integer.compare(left.path.size(), right.path.size()));
        for (TreeNodeState treeNodeState : sortedExpandedTreeNodes) {
            TreePath treePath = resolveTreeNodeState((ProjectTreeNode) root, treeNodeState);
            if (treePath != null) {
                projectsTree.expandPath(treePath);
                restored = true;
            }
        }

        TreePath selectedPath = resolveTreeNodeState((ProjectTreeNode) root, selectedTreeNode);
        if (selectedPath != null) {
            projectsTree.setSelectionPath(selectedPath);
            projectsTree.scrollPathToVisible(selectedPath);
            restored = true;
        }

        return restored;
    }

    public ProjectTreeNode getSelectedNode() {
        TreePath selectionPath = projectsTree.getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        Object lastPathComponent = selectionPath.getLastPathComponent();
        return lastPathComponent instanceof ProjectTreeNode ? (ProjectTreeNode) lastPathComponent : null;
    }

    public boolean hasSelection() {
        return projectsTree.getSelectionPath() != null;
    }

    static TreeNodeState createTreeNodeState(TreePath treePath) {
        if (treePath == null) {
            return null;
        }

        java.util.List<String> segments = new ArrayList<String>();
        Object[] pathComponents = treePath.getPath();
        for (int i = 1; i < pathComponents.length; i++) {
            if (!(pathComponents[i] instanceof ProjectTreeNode)) {
                return null;
            }
            String key = getTreeNodeStateKey((ProjectTreeNode) pathComponents[i]);
            if (key == null) {
                return null;
            }
            segments.add(key);
        }

        return segments.isEmpty() ? null : new TreeNodeState(segments);
    }

    static TreePath resolveTreeNodeState(ProjectTreeNode root, TreeNodeState treeNodeState) {
        if (root == null || treeNodeState == null || treeNodeState.path == null || treeNodeState.path.isEmpty()) {
            return null;
        }

        java.util.List<Object> pathComponents = new ArrayList<Object>();
        pathComponents.add(root);
        ProjectTreeNode current = root;
        for (String segment : treeNodeState.path) {
            current = findChildByTreeStateKey(current, segment);
            if (current == null) {
                return null;
            }
            pathComponents.add(current);
        }
        return new TreePath(pathComponents.toArray());
    }

    static String getTreeNodeStateKey(ProjectTreeNode node) {
        if (node == null) {
            return null;
        }
        if (node instanceof Project) {
            return "project:" + ((Project) node).name;
        }
        if (node instanceof ClosedProject) {
            return "project:" + getClosedProjectName((ClosedProject) node);
        }
        if (node instanceof GameSource) {
            return "gameSource:" + ((GameSource) node).type.name();
        }
        if (node instanceof GameDataSet) {
            return "slot:gameData";
        }
        if (node instanceof GameDataCategory<?>) {
            return "category:" + ((GameDataCategory<?>) node).name;
        }
        if (node instanceof TMXMapSet) {
            return "slot:tmxMaps";
        }
        if (node instanceof SpriteSheetSet) {
            return "slot:spritesheets";
        }
        if (node instanceof Worldmap) {
            return "slot:worldmap";
        }
        if (node instanceof WriterModeDataSet) {
            return "slot:writerMode";
        }
        if (node instanceof BookmarksRoot) {
            return "slot:bookmarks";
        }
        if (node instanceof BookmarkFolder) {
            return "bookmarkFolder:" + ((BookmarkFolder) node).getDesc();
        }
        if (node instanceof BookmarkEntry) {
            BookmarkEntry bookmarkEntry = (BookmarkEntry) node;
            return bookmarkEntry.bookmarkedElement == null ? null : "bookmarkEntry:" + getTreeNodeStateKey(bookmarkEntry.bookmarkedElement);
        }
        if (node instanceof GameDataElement) {
            GameDataElement gameDataElement = (GameDataElement) node;
            return "element:" + node.getClass().getName() + ":" + gameDataElement.id;
        }
        return "node:" + node.getClass().getName() + ":" + node.getDesc();
    }

    private static ProjectTreeNode findChildByTreeStateKey(ProjectTreeNode parent, String key) {
        if (parent == null || key == null) {
            return null;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeNode child = parent.getChildAt(i);
            if (child instanceof ProjectTreeNode && key.equals(getTreeNodeStateKey((ProjectTreeNode) child))) {
                return (ProjectTreeNode) child;
            }
        }
        return null;
    }

    private static String getClosedProjectName(ClosedProject closedProject) {
        String desc = closedProject == null ? null : closedProject.getDesc();
        String suffix = " [closed]";
        return desc != null && desc.endsWith(suffix) ? desc.substring(0, desc.length() - suffix.length()) : desc;
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

    public void clearSelection() {
        projectsTree.clearSelection();
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
        if (node == null) {
            clearSelection();
            return;
        }
        TreePath tp = getTreePath(node);
        projectsTree.setSelectionPath(tp);
        projectsTree.scrollPathToVisible(tp);
    }

    public boolean setSelectedNodeIfBranchExpanded(ProjectTreeNode node) {
        TreePath treePath = getTreePath(node);
        if (treePath == null) {
            return false;
        }

        for (TreePath current = treePath.getParentPath(); current != null && current.getPathCount() > 1; current = current.getParentPath()) {
            if (!projectsTree.isExpanded(current)) {
                return false;
            }
        }

        boolean expandsSelectedPaths = projectsTree.getExpandsSelectedPaths();
        projectsTree.setExpandsSelectedPaths(false);
        try {
            projectsTree.setSelectionPath(treePath);
            projectsTree.scrollPathToVisible(treePath);
        } finally {
            projectsTree.setExpandsSelectedPaths(expandsSelectedPaths);
        }
        return true;
    }

    private TreePath getTreePath(ProjectTreeNode node) {
        if (node == null) {
            return null;
        }
        List<TreeNode> path = new ArrayList<TreeNode>();
        path.add(node);
        TreeNode parent = node.getParent();
        while (parent != null) {
            path.add(0, parent);
            parent = parent.getParent();
        }
        return new TreePath(path.toArray());
    }

}
