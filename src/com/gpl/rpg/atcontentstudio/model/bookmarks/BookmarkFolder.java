package com.gpl.rpg.atcontentstudio.model.bookmarks;

import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.List;
import java.util.*;

public class BookmarkFolder implements BookmarkNode {

    List<BookmarkNode> contents = new LinkedList<BookmarkNode>();
    BookmarkNode parent;
    String name;
    Image closedIcon, openIcon;

    public BookmarkFolder(BookmarkNode parent, String name) {
        this(parent, name, DefaultIcons.getStdClosedIcon(), DefaultIcons.getStdOpenIcon());
    }

    public BookmarkFolder(BookmarkNode parent, String name, Image closedIcon, Image openIcon) {
        this.parent = parent;
        this.name = name;
        this.closedIcon = closedIcon;
        this.openIcon = openIcon;
    }

    @Override
    public Enumeration<? extends ProjectTreeNode> children() {
        return Collections.enumeration(contents);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return contents.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return contents.size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return contents.indexOf(node);
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return false;
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
        if (path.size() == 1 && this.getChildCount() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
    }

    @Override
    public String getDesc() {
        return name;
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Image getIcon() {
        return getClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return openIcon;
    }

    @Override
    public Image getClosedIcon() {
        return closedIcon;
    }

    @Override
    public Image getLeafIcon() {
        return getClosedIcon();
    }

    @Override
    public Type getDataType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public boolean needsSaving() {
        return false;
    }

    public void delete(BookmarkEntry bookmarkEntry) {
        if (contents.contains(bookmarkEntry)) {
            bookmarkEntry.childrenRemoved(new ArrayList<ProjectTreeNode>());
            contents.remove(bookmarkEntry);
            save();
        }
    }

    public void delete(BookmarkFolder bookmarkFolder) {
        // TODO Auto-generated method stub

    }

    public void save() {
        parent.save();
    }

    public void delete() {

    }

}
