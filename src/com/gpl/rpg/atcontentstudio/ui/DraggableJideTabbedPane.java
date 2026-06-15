package com.gpl.rpg.atcontentstudio.ui;

import com.jidesoft.swing.JideTabbedPane;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A JideTabbedPane subclass that supports drag-to-reorder tab functionality.
 * Users can click and drag tabs to rearrange their order.
 */
public class DraggableJideTabbedPane extends JideTabbedPane {
    private int draggedTabIndex = -1;

    public DraggableJideTabbedPane() {
        super();
        enableTabDragging();
    }

    public DraggableJideTabbedPane(int tabPlacement) {
        super(tabPlacement);
        enableTabDragging();
    }

    private void enableTabDragging() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                draggedTabIndex = getUI().tabForCoordinate(DraggableJideTabbedPane.this, e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedTabIndex = -1;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedTabIndex < 0) return;

                int currentTab = getUI().tabForCoordinate(DraggableJideTabbedPane.this, e.getX(), e.getY());
                if (currentTab >= 0 && currentTab != draggedTabIndex) {
                    moveTab(draggedTabIndex, currentTab);
                    draggedTabIndex = currentTab;
                }
            }
        });
    }

    private void moveTab(int fromIndex, int toIndex) {
        // Preserve all tab state
        Component component = getComponentAt(fromIndex);
        String title = getTitleAt(fromIndex);
        Icon icon = getIconAt(fromIndex);
        String tooltip = getToolTipTextAt(fromIndex);

        removeTabAt(fromIndex);
        insertTab(title, icon, component, tooltip, toIndex);
        setSelectedIndex(toIndex);
    }
}

