package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class NestedScrollListener implements MouseWheelListener {

    private final JScrollPane childScrollPane;
    private JScrollPane parentScrollPane;
    private MouseWheelListener defaultListener;

    public NestedScrollListener(JScrollPane childScrollPane) {
        this.childScrollPane = childScrollPane;

        // 1. Extract the look-and-feel's default scrolling listener
        MouseWheelListener[] listeners = childScrollPane.getMouseWheelListeners();
        if (listeners.length > 0) {
            this.defaultListener = listeners[0];
            // Remove it so it doesn't automatically consume the mouse movement
            childScrollPane.removeMouseWheelListener(defaultListener);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (parentScrollPane == null) {
            parentScrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, childScrollPane);
        }

        JScrollBar scrollBar = childScrollPane.getVerticalScrollBar();
        int value = scrollBar.getValue();
        int min = scrollBar.getMinimum();
        int max = scrollBar.getMaximum() - scrollBar.getModel().getExtent();

        boolean canScroll = scrollBar.isVisible() && max > min;
        boolean reachedTop = (e.getWheelRotation() < 0 && value <= min);
        boolean reachedBottom = (e.getWheelRotation() > 0 && value >= max);

        if ((!canScroll || reachedTop || reachedBottom) && parentScrollPane != null) {
            parentScrollPane.dispatchEvent(convertWheelEventToParent(e));
        } else if (defaultListener != null) {
            defaultListener.mouseWheelMoved(e);
        } else if (parentScrollPane != null) {
            parentScrollPane.dispatchEvent(convertWheelEventToParent(e));
        }
    }

    private MouseWheelEvent convertWheelEventToParent(MouseWheelEvent e) {
        Point parentPoint = SwingUtilities.convertPoint(childScrollPane, e.getPoint(), parentScrollPane);
        return new MouseWheelEvent(
                parentScrollPane,
                e.getID(),
                e.getWhen(),
                e.getModifiersEx(),
                parentPoint.x,
                parentPoint.y,
                e.getXOnScreen(),
                e.getYOnScreen(),
                e.getClickCount(),
                e.isPopupTrigger(),
                e.getScrollType(),
                e.getScrollAmount(),
                e.getWheelRotation(),
                e.getPreciseWheelRotation()
        );
    }

    public static void install(JScrollPane childScrollPane) {
        // Safe installation hook wrapper
        childScrollPane.addMouseWheelListener(new NestedScrollListener(childScrollPane));
    }
}
