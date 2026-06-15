package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.utils.UiUtils;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class CollapsibleScrollList extends CollapsiblePanel {

    private static final long serialVersionUID = -6209867809145467136L;

    private final JList<?> list;
    private final JScrollPane scroller;
    private final ListDataListener resizeListener = new ListDataListener() {
        @Override
        public void intervalAdded(ListDataEvent e) {
            resizeList();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            resizeList();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            resizeList();
        }
    };

    public CollapsibleScrollList(String title, JList<?> list) {
        this(title, list, false);
    }
//    public CollapsibleScrollList(String title, JList<?> list, ListCellRenderer<?> renderer, boolean collapsed) {
//        this(title, list, collapsed);
//        list.setCellRenderer(renderer);
//    }
    public CollapsibleScrollList(String title, JList<?> list, boolean collapsed) {
        super(title);
        this.list = list;
        this.scroller = new JScrollPane(list);
        this.scroller.getVerticalScrollBar().setUnitIncrement(12);

        // Add scrollwheel handler
        NestedScrollListener.install(scroller);
        attachResizeListener(list.getModel());
        list.addPropertyChangeListener("model", evt -> {
            if (evt.getOldValue() instanceof ListModel) {
                detachResizeListener((ListModel<?>) evt.getOldValue());
            }
            if (evt.getNewValue() instanceof ListModel) {
                attachResizeListener((ListModel<?>) evt.getNewValue());
            }
            resizeList();
        });

        setLayout(new JideBoxLayout(this, JideBoxLayout.PAGE_AXIS));
        add(scroller, JideBoxLayout.FIX);

        add(new JPanel(), JideBoxLayout.FIX);  // We need to add a blank jpanel so it collapses property for some reason
        resizeList();

        if (collapsed) {
            collapse();
        }
    }

    private void attachResizeListener(ListModel<?> model) {
        if (model != null) {
            model.addListDataListener(resizeListener);
        }
    }

    private void detachResizeListener(ListModel<?> model) {
        if (model != null) {
            model.removeListDataListener(resizeListener);
        }
    }

    private void resizeList() {
        // Run it in the event dispatch thread.  If we're already in it, do it now
        Runnable r = () -> {
            int newRowCount = Math.min(8, list.getModel().getSize());
            if (list.getVisibleRowCount() != newRowCount) {
                list.setVisibleRowCount(newRowCount);
            }

            revalidate();
            repaint();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    public JList<?> getList() {
        return list;
    }

    public JScrollPane getScroller() {
        return scroller;
    }
}

