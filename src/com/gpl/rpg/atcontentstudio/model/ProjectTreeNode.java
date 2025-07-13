package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.List;

public interface ProjectTreeNode extends TreeNode {

    public void childrenAdded(List<ProjectTreeNode> path);

    public void childrenChanged(List<ProjectTreeNode> path);

    public void childrenRemoved(List<ProjectTreeNode> path);

    public void notifyCreated();

    public String getDesc();

    /**
     * Unnecessary for anything not below a Project. Can return null.
     *
     * @return the parent Project or null.
     */
    public Project getProject();


    /**
     * Unnecessary for anything not below a GameDataSet. Can return null.
     *
     * @return the parent GameDataSet or null.
     */
    public GameDataSet getDataSet();

    public Image getIcon();

    /**
     * @return The icon depicting this node when it is an open folder. Can be null for leaves.
     */
    public Image getOpenIcon();

    /**
     * @return The icon depicting this node when it is a closed folder. Can be null for leaves.
     */
    public Image getClosedIcon();

    /**
     * @return The icon depicting this node when it is a leaf. Should return the closed one for empty folders.
     */
    public Image getLeafIcon();

    /**
     * Unnecessary for anything not below a GameSource. Can return null.
     *
     * @return the parent GameSource or null.
     */
    public GameSource.Type getDataType();

    public boolean isEmpty();

    public boolean needsSaving();

}
