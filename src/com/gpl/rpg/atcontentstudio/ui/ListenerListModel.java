package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;

public interface ListenerListModel<E> extends ListModel<E> {
    List<ListDataListener> getListeners();

    default void notifyListeners(ChangeType event, int index0, int index1) {
        notifyListeners(this, event, index0, index1);
    }

    default void notifyListeners(Object source, ChangeType event, int index0, int index1) {
        int eventCode;
        switch (event) {
            case CHANGED:
                eventCode = ListDataEvent.CONTENTS_CHANGED;
                break;
            case ADDED:
                eventCode = ListDataEvent.INTERVAL_ADDED;
                break;
            case REMOVED:
                eventCode = ListDataEvent.INTERVAL_REMOVED;
                break;
            default:
                throw new IllegalArgumentException();
        }

        for (ListDataListener l : getListeners()) {
            ListDataEvent e = new ListDataEvent(source, eventCode, index0, index1);
            switch (event) {
                case CHANGED: {
                    l.contentsChanged(e);
                    break;
                }
                case ADDED: {
                    l.intervalAdded(e);
                    break;
                }
                case REMOVED: {
                    l.intervalRemoved(e);
                    break;
                }
            }
        }
    }

    default void addListDataListener(ListDataListener l) {
        getListeners().add(l);
    }

    default void removeListDataListener(ListDataListener l) {
        getListeners().remove(l);
    }

    default void fireListChanged() {
        notifyListeners(this, ChangeType.CHANGED, 0, getSize() - 1);
    }

    enum ChangeType {
        CHANGED,
        ADDED,
        REMOVED,
    }
}
