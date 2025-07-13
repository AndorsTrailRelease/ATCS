package com.gpl.rpg.atcontentstudio.model.saves;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class SavedGamesSet implements ProjectTreeNode, Serializable {

    private static final long serialVersionUID = -6565834239789184087L;

    public Vector<SavedGame> saves; //For simulations.

    public Project parent;

    public SavedGamesSet(Project parent) {
        this.parent = parent;
        saves = new Vector<SavedGame>();
    }

    public void refreshTransients() {
        for (SavedGame save : saves) {
            try {
                save.refreshTransients(this);
            } catch (IOException e) {
                Notification.addError(e.getMessage());
            }
        }
    }

    public void addSave(File f) {
        try {
            ProjectTreeNode higherEmptyParent = this;
            while (higherEmptyParent != null) {
                if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                    higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
                else break;
            }
            if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
            SavedGame node = new SavedGame(this, f);
            saves.add(node);
            if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
            else node.notifyCreated();
        } catch (IOException e) {
            Notification.addError(e.getMessage());
        }
    }

    public SavedGame getSave(File f) {
        for (SavedGame save : saves) {
            if (save.savedFile.equals(f)) return save;
        }
        return null;
    }

    @Override
    public Enumeration<? extends ProjectTreeNode> children() {
        return saves.elements();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return saves.elementAt(arg0);
    }

    @Override
    public int getChildCount() {
        return saves.size();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return saves.indexOf(arg0);
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
        for (SavedGame s : saves) {
            s.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "Saved games";
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }


    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getSavClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getSavClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getSavOpenIcon();
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
        return saves.isEmpty();
    }


    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : saves) {
            if (node.needsSaving()) return true;
        }
        return false;
    }
}
