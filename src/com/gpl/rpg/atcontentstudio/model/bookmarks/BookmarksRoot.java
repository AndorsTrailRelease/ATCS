package com.gpl.rpg.atcontentstudio.model.bookmarks;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SavedSlotCollection;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class BookmarksRoot implements BookmarkNode {

    SavedSlotCollection v = new SavedSlotCollection();

    public transient Project parent;

    BookmarkFolder ac, diag, dl, it, ic, npc, q, tmx, sp, wm;

    public BookmarksRoot(Project parent) {
        this.parent = parent;

        v.add(ac = new BookmarkFolder(this, ActorCondition.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(diag = new BookmarkFolder(this, Dialogue.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(dl = new BookmarkFolder(this, Droplist.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(it = new BookmarkFolder(this, Item.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(ic = new BookmarkFolder(this, ItemCategory.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(npc = new BookmarkFolder(this, NPC.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));
        v.add(q = new BookmarkFolder(this, Quest.getStaticDesc(), DefaultIcons.getJsonClosedIcon(), DefaultIcons.getJsonOpenIcon()));

        v.add(tmx = new BookmarkFolder(this, "TMX Maps", DefaultIcons.getTmxClosedIcon(), DefaultIcons.getTmxOpenIcon()));
        v.add(sp = new BookmarkFolder(this, "Spritesheets", DefaultIcons.getSpriteClosedIcon(), DefaultIcons.getSpriteOpenIcon()));
        v.add(wm = new BookmarkFolder(this, "Worldmap", DefaultIcons.getSpriteClosedIcon(), DefaultIcons.getSpriteOpenIcon()));
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return v.getNonEmptyElements();
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return v.getNonEmptyElementAt(arg0);
    }

    @Override
    public int getChildCount() {
        return v.getNonEmptySize();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return v.getNonEmptyIndexOf((ProjectTreeNode) arg0);
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
        if (path.size() == 1 && this.v.getNonEmptySize() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
        for (ProjectTreeNode node : v.getNonEmptyIterable()) {
            node.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "Bookmarks";
    }

    @Override
    public Project getProject() {
        return parent == null ? null : parent.getProject();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getBookmarkOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getBookmarkClosedIcon();
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
        return v.isEmpty();
    }

    @Override
    public boolean needsSaving() {
        return false;
    }

    public void save() {

    }

    @Override
    public void delete() {
    }

    public void addBookmark(GameDataElement target) {
        BookmarkEntry node;
        BookmarkFolder folder;
        if (target instanceof ActorCondition) {
            folder = ac;
        } else if (target instanceof Dialogue) {
            folder = diag;
        } else if (target instanceof Droplist) {
            folder = dl;
        } else if (target instanceof Item) {
            folder = it;
        } else if (target instanceof ItemCategory) {
            folder = ic;
        } else if (target instanceof NPC) {
            folder = npc;
        } else if (target instanceof Quest) {
            folder = q;
        } else if (target instanceof TMXMap) {
            folder = tmx;
        } else if (target instanceof Spritesheet) {
            folder = sp;
        } else if (target instanceof WorldmapSegment) {
            folder = wm;
        } else {
            return;
        }
        ProjectTreeNode higherEmptyParent = folder;
        while (higherEmptyParent != null) {
            if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
            else break;
        }
        if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;

        node = new BookmarkEntry(folder, target);
        if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
        else node.notifyCreated();
    }

}
