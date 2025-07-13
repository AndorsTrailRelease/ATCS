package com.gpl.rpg.atcontentstudio.model.saves;

import com.gpl.rpg.andorstrainer.io.SavedGameIO;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SavedGame extends GameDataElement {

    private static final long serialVersionUID = -6443495534761084990L;

    public File savedFile;
    transient public com.gpl.rpg.andorstrainer.model.SavedGame loadedSave = null;
    transient public SavedGamesSet parent;

    public SavedGame(SavedGamesSet parent, File f) throws IOException {
        savedFile = f;
        refreshTransients(parent);
    }

    public void refreshTransients(SavedGamesSet parent) throws IOException {
        this.parent = parent;
        this.loadedSave = SavedGameIO.loadFile(savedFile);
        if (this.loadedSave == null) {
            throw new IOException("Unable to load save: " + savedFile.getAbsolutePath());
        }
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
    public TreeNode getChildAt(int arg0) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public int getIndex(TreeNode arg0) {
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
        return (needsSaving() ? "*" : "") + loadedSave.displayInfo;
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getHeroIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getHeroIcon();
    }

    @Override
    public Image getClosedIcon() {
        return null;
    }

    @Override
    public Image getOpenIcon() {
        return null;
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
        return false;
    }

    @Override
    public void parse() {
        // TODO Auto-generated method stub

    }

    @Override
    public void link() {
        // TODO Auto-generated method stub

    }

    @Override
    public GameDataElement clone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getProjectFilename() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void save() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<SaveEvent> attemptSave() {
        // TODO Auto-generated method stub
        return null;
    }
}
