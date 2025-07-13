package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.model.bookmarks.BookmarkEntry;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class GameDataElement implements ProjectTreeNode, Serializable {

    private static final long serialVersionUID = 2028934451226743389L;

    public static enum State {
        init, // We know the object exists, and have its key/ID.
        parsed, // We know the object's properties, but related objects are referenced by ID only.
        linked, // We know the object fully, and all links to related objects point to objects in the parsed state at least.
        created, // This is an object we are creating
        modified, // Whether altered or created, this item has been modified since creation from scratch or from JSON.
        saved // Whether altered or created, this item has been saved since last modification.
    }

    public State state = State.init;

    //Available from state init.
    public ProjectTreeNode parent;

    public boolean writable = false;

    public BookmarkEntry bookmark = null;

    //List of objects whose transition to "linked" state made them point to this instance.
    private Map<GameDataElement, Integer> backlinks = new ConcurrentHashMap<GameDataElement, Integer>();

    public String id = null;

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
    public abstract String getDesc();

    public static String getStaticDesc() {
        return "GameDataElements";
    }

    public abstract void parse();

    public abstract void link();


    @Override
    public Project getProject() {
        return parent == null ? null : parent.getProject();
    }


    public Image getIcon() {
        return null;
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
    public Image getLeafIcon() {
        return getIcon();
    }


    public abstract GameDataElement clone();

    public abstract void elementChanged(GameDataElement oldOne, GameDataElement newOne);


    @Override
    public GameSource.Type getDataType() {
        if (parent == null) {
            System.out.println("blerf.");
        }
        return parent.getDataType();
    }


    public List<BacklinksListener> backlinkListeners = new ArrayList<GameDataElement.BacklinksListener>();

    public void addBacklinkListener(BacklinksListener l) {
        backlinkListeners.add(l);
    }

    public void removeBacklinkListener(BacklinksListener l) {
        backlinkListeners.remove(l);
    }

    public void addBacklink(GameDataElement gde) {
        if (!backlinks.containsKey(gde)) {
            backlinks.put(gde, 1);
            for (BacklinksListener l : backlinkListeners) {
                l.backlinkAdded(gde);
            }
        } else {
            backlinks.put(gde, backlinks.get(gde) + 1);
        }
    }

    public void removeBacklink(GameDataElement gde) {
        if (backlinks.get(gde) == null) return;
        backlinks.put(gde, backlinks.get(gde) - 1);
        if (backlinks.get(gde) == 0) {
            backlinks.remove(gde);
            for (BacklinksListener l : backlinkListeners) {
                l.backlinkRemoved(gde);
            }
        }
    }

    public Set<GameDataElement> getBacklinks() {
        return backlinks.keySet();
    }

    public static interface BacklinksListener {
        public void backlinkAdded(GameDataElement gde);

        public void backlinkRemoved(GameDataElement gde);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public boolean needsSaving() {
        return this.state == State.modified || this.state == State.created;
    }

    public abstract String getProjectFilename();

    public abstract void save();

    public abstract List<SaveEvent> attemptSave();

    /**
     * Checks if the current state indicates that parsing/linking should be skipped.
     *
     * @return true if the operation should be skipped, false otherwise
     */
    protected boolean shouldSkipParseOrLink() {
        if (shouldSkipParse()) return true;
        if (this.state == State.linked) {
            //Already linked.
            return true;
        }
        return false;
    }

    protected boolean shouldSkipParse() {
        if (this.state == State.created || this.state == State.modified || this.state == State.saved) {
            //This type of state is unrelated to parsing/linking.
            return true;
        }
        return false;
    }

    /**
     * Ensures the element is parsed if needed based on its current state.
     */
    protected void ensureParseIfNeeded() {
        if (this.state == State.init) {
            //Not parsed yet.
            this.parse();
        }
    }
}
