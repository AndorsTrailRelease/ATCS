package com.gpl.rpg.atcontentstudio.model.bookmarks;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.Quest;
import com.gpl.rpg.atcontentstudio.model.gamedata.QuestStage;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class BookmarkEntry implements BookmarkNode {

    public GameDataElement bookmarkedElement;
    public BookmarkFolder parent;

    public BookmarkEntry(BookmarkFolder parent, GameDataElement target) {
        this.parent = parent;
        this.bookmarkedElement = target;
        target.bookmark = this;
        parent.contents.add(this);
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return null;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
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
    public int getIndex(TreeNode node) {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return true;
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
        if (bookmarkedElement instanceof QuestStage) {
            String text = ((GameDataElement) bookmarkedElement).getDesc();
            if (text.length() > 60) {
                text = text.substring(0, 57) + "...";
            }
            return ((GameDataElement) bookmarkedElement).getDataType().toString() +
                    "/" +
                    ((Quest) ((QuestStage) bookmarkedElement).parent).id +
                    "#" +
                    ((QuestStage) bookmarkedElement).progress +
                    ":" +
                    text;
        } else {
            return ((GameDataElement) bookmarkedElement).getDataType().toString() + "/" + ((GameDataElement) bookmarkedElement).getDesc();
        }
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
        return bookmarkedElement.getIcon();
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
        return getIcon();
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

    public void delete() {
        bookmarkedElement.bookmark = null;
        parent.delete(this);
    }

    @Override
    public void save() {
        parent.save();
    }

}
