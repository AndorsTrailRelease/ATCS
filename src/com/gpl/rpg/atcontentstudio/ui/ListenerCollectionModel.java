package com.gpl.rpg.atcontentstudio.ui;

import java.util.Collection;

public interface ListenerCollectionModel<E> extends ListenerListModel<E> {
    public Collection<E> getElements();

    @Override
    default int getSize() {
        Collection<E> elements = getElements();
        if (elements == null) return 0;
        return elements.size();
    }

    @Override
    default E getElementAt(int index) {
        for (E obj : getElements()) {
            if (index == 0) return obj;
            index--;
        }
        return null;
    }

}
