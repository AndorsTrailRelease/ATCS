package com.gpl.rpg.atcontentstudio.model;

import java.util.Enumeration;
import java.util.Vector;

public class SavedSlotCollection {

    Vector<ProjectTreeNode> contents = new Vector<ProjectTreeNode>();

    public void add(ProjectTreeNode node) {
        contents.add(node);
    }

    public int getNonEmptySize() {
//		return contents.size();
        int size = 0;
        for (ProjectTreeNode node : contents) {
            if (!node.isEmpty()) size++;
        }
        return size;
    }

    public Enumeration<ProjectTreeNode> getNonEmptyElements() {
//		return contents.elements();
        Vector<ProjectTreeNode> v = new Vector<ProjectTreeNode>();
        for (ProjectTreeNode node : contents) {
            if (!node.isEmpty()) v.add(node);
        }
        return v.elements();
    }

    public ProjectTreeNode getNonEmptyElementAt(int index) {
//		return contents.get(index);
        int i = 0;
        while (i < contents.size()) {
            if (!contents.get(i).isEmpty()) index--;
            if (index == -1) return contents.get(i);
            i++;
        }
        return null;
    }


    public int getNonEmptyIndexOf(ProjectTreeNode node) {
//		return contents.indexOf(node);
        int index = contents.indexOf(node);
        int trueIndex = index;
        for (int i = 0; i < trueIndex; i++) {
            if (contents.get(i).isEmpty()) index--;
        }
        return index;
    }


    public Vector<ProjectTreeNode> getNonEmptyIterable() {
//		return contents;
        Vector<ProjectTreeNode> v = new Vector<ProjectTreeNode>();
        for (ProjectTreeNode node : contents) {
            if (!node.isEmpty()) v.add(node);
        }
        return v;
    }

    public boolean isEmpty() {
//		return contents.isEmpty();
        for (ProjectTreeNode node : contents) {
            if (!node.isEmpty()) return false;
        }
        return true;
    }
}
