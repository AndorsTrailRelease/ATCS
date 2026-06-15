package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Preferences;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import org.junit.Test;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectsTreeStateTest {

    @Test
    public void treeNodeStateRoundTripResolvesBackToOriginalPath() {
        DummyNode workspace = new DummyNode("workspace", null);
        DummyNode project = new DummyNode("project", workspace);
        DummyNode spritesheets = new DummyNode("spritesheets", project);
        DummyNode monsters = new DummyNode("monsters", spritesheets);

        TreePath original = new TreePath(new Object[]{workspace, project, spritesheets, monsters});
        Preferences.TreeNodeState state = ProjectsTree.createTreeNodeState(original);
        assertNotNull(state);
        assertEquals(3, state.path.size());

        TreePath resolved = ProjectsTree.resolveTreeNodeState(workspace, state);
        assertNotNull(resolved);
        assertSame(project, resolved.getPathComponent(1));
        assertSame(spritesheets, resolved.getPathComponent(2));
        assertSame(monsters, resolved.getLastPathComponent());
    }

    @Test
    public void resolveTreeNodeStateReturnsNullWhenPathDoesNotExist() {
        DummyNode workspace = new DummyNode("workspace", null);
        new DummyNode("project", workspace);

        Preferences.TreeNodeState missing = new Preferences.TreeNodeState(java.util.Arrays.asList(
                "node:" + DummyNode.class.getName() + ":other-project"
        ));

        assertNull(ProjectsTree.resolveTreeNodeState(workspace, missing));
    }

    private static class DummyNode implements ProjectTreeNode {
        private final String desc;
        private final DummyNode parent;
        private final List<DummyNode> children = new ArrayList<DummyNode>();

        DummyNode(String desc, DummyNode parent) {
            this.desc = desc;
            this.parent = parent;
            if (parent != null) {
                parent.children.add(this);
            }
        }

        @Override
        public void childrenAdded(List<ProjectTreeNode> path) {
        }

        @Override
        public void childrenChanged(List<ProjectTreeNode> path) {
        }

        @Override
        public void childrenRemoved(List<ProjectTreeNode> path) {
        }

        @Override
        public void notifyCreated() {
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public GameDataSet getDataSet() {
            return null;
        }

        @Override
        public Image getIcon() {
            return null;
        }

        @Override
        public Image getOpenIcon() {
            return null;
        }

        @Override
        public Image getClosedIcon() {
            return null;
        }

        @Override
        public Image getLeafIcon() {
            return null;
        }

        @Override
        public GameSource.Type getDataType() {
            return null;
        }

        @Override
        public boolean isEmpty() {
            return children.isEmpty();
        }

        @Override
        public boolean needsSaving() {
            return false;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return children.get(childIndex);
        }

        @Override
        public int getChildCount() {
            return children.size();
        }

        @Override
        public TreeNode getParent() {
            return parent;
        }

        @Override
        public int getIndex(TreeNode node) {
            return children.indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return Collections.enumeration(children);
        }
    }
}


