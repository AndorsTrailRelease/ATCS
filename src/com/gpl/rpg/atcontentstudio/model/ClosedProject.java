package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClosedProject implements ProjectTreeNode {

    String name;
    Workspace parent;

    public ClosedProject(Workspace w, String name) {
        this.parent = w;
        this.name = name;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return 0;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return null;
    }

    @Override
    public void childrenAdded(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenAdded(path);
    }

    @Override
    public void childrenChanged(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenChanged(path);
    }

    @Override
    public void childrenRemoved(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenRemoved(path);
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
    }

    @Override
    public String getDesc() {
        return name + " [closed]";
    }

    @Override
    public Project getProject() {
        return null;
    }


    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        //TODO Create a cool Project icon.
        return DefaultIcons.getStdClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        //TODO Create a cool Project icon.
        return DefaultIcons.getStdClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        //TODO Create a cool Project icon.
        return DefaultIcons.getStdOpenIcon();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Type getDataType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean needsSaving() {
        return false;
    }

}
