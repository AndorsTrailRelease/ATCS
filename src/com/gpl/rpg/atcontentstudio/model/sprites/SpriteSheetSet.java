package com.gpl.rpg.atcontentstudio.model.sprites;

import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class SpriteSheetSet implements ProjectTreeNode {

    public static final String DEFAULT_REL_PATH_IN_SOURCE = "res" + File.separator + "drawable" + File.separator;
    public static final String DEFAULT_REL_PATH_IN_PROJECT = "spritesheets" + File.separator;

    public File drawableFolder = null;

    public transient List<Spritesheet> spritesheets;

    public GameSource parent;

    public SpriteSheetSet(GameSource source) {
        this.parent = source;
        if (source.type == GameSource.Type.source)
            this.drawableFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_SOURCE);
        else if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
            this.drawableFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
            if (!this.drawableFolder.exists()) {
                this.drawableFolder.mkdirs();
            }
        }
        spritesheets = new ArrayList<Spritesheet>();
        if (this.drawableFolder != null) {
            for (File f : this.drawableFolder.listFiles()) {
                if (f.getName().endsWith(".png") || f.getName().endsWith(".PNG")) {
                    spritesheets.add(new Spritesheet(this, f));
                }
            }
        }
        spritesheets.sort(Comparator.comparing(s -> s.id));
    }

    @Override
    public Enumeration<Spritesheet> children() {
        return Collections.enumeration(spritesheets);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return spritesheets.get(arg0);
    }

    @Override
    public int getChildCount() {
        return spritesheets.size();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return spritesheets.indexOf(arg0);
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
        for (Spritesheet s : spritesheets) {
            s.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "Spritesheets";
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
        return DefaultIcons.getSpriteClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getSpriteClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getSpriteOpenIcon();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Type getDataType() {
        return parent.getDataType();
    }

    @Override
    public boolean isEmpty() {
        return spritesheets.isEmpty();
    }

    public Spritesheet getSpritesheet(String id) {
        if (spritesheets == null) return null;
        for (Spritesheet sheet : spritesheets) {
            if (id.equals(sheet.id)) {
                return sheet;
            }
        }
        return null;
    }

    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : spritesheets) {
            if (node.needsSaving()) return true;
        }
        return false;
    }
}
