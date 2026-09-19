package com.gpl.rpg.atcontentstudio.utils;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.ui.CollapsiblePanel;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.NestedScrollListener;
import com.gpl.rpg.atcontentstudio.ui.OrderedListenerListModel;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;
import java.util.function.Supplier;

public class UiUtils {
    private static final String RESIZE_LIST_MAX_ROWS_PROPERTY = UiUtils.class.getName() + ".resizeListToFit.maxRows";
    private static final int RESIZE_LIST_DEFAULT_ROWS = 8;

    public static class CollapsibleItemListCreation<E> {
        public CollapsiblePanel collapsiblePanel;
        public JList<E> list;
    }

    public static JPanel createRefreshButtonPane(ActionListener reloadButtonEditor) {
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS));
        JButton reloadButton = new JButton("Refresh graph");
        buttonPane.add(reloadButton, JideBoxLayout.FIX);
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);

        reloadButton.addActionListener(reloadButtonEditor);
        return buttonPane;
    }

    public static <S, E, M extends OrderedListenerListModel<S, E>> CollapsibleItemListCreation<E> getCollapsibleItemList(FieldUpdateListener listener,
                                                                                                                         M listModel,
                                                                                                                         BasicLambda selectedReset,
                                                                                                                         BasicLambdaWithArg<E> setSelected,
                                                                                                                         BasicLambdaWithReturn<E> getSelected,
                                                                                                                         BasicLambdaWithArg<E> valueChanged,
                                                                                                                         BasicLambdaWithArg<JPanel> updateEditorPane,
                                                                                                                         boolean writable,
                                                                                                                         Supplier<E> newValueSupplier,
                                                                                                                         DefaultListCellRenderer cellRenderer,
                                                                                                                         String title,
                                                                                                                         BasicLambdaWithArgAndReturn<E, GameDataElement> getReferencedObj) {
        CollapsiblePanel listPanel = new CollapsiblePanel(title);
        listPanel.setLayout(new JideBoxLayout(listPanel, JideBoxLayout.PAGE_AXIS));
        final JList<E> list = new JList<>(listModel);
        list.setCellRenderer(cellRenderer);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(list);
        NestedScrollListener.install(scroller);
        listPanel.add(scroller, JideBoxLayout.FIX);
        final JPanel editorPane = new JPanel();
        final JButton createBtn = new JButton(new ImageIcon(DefaultIcons.getCreateIcon()));
        final JButton deleteBtn = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        final JButton moveUpBtn = new JButton(new ImageIcon(DefaultIcons.getArrowUpIcon()));
        final JButton moveDownBtn = new JButton(new ImageIcon(DefaultIcons.getArrowDownIcon()));
        deleteBtn.setEnabled(false);
        moveUpBtn.setEnabled(false);
        moveDownBtn.setEnabled(false);
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            E selectedValue = list.getSelectedValue();
            if (Objects.equals(selectedValue, getSelected.doIt())) return;
            valueChanged.doIt(selectedValue);
            setSelected.doIt(selectedValue);
            if (selectedValue == null) {
                deleteBtn.setEnabled(false);
                moveUpBtn.setEnabled(false);
                moveDownBtn.setEnabled(false);
            } else {
                deleteBtn.setEnabled(true);
                moveUpBtn.setEnabled(list.getSelectedIndex() > 0);
                moveDownBtn.setEnabled(list.getSelectedIndex() < (listModel.getSize() - 1));

            }
            updateEditorPane.doIt(editorPane);
        });
        if (writable) {
            JPanel listButtonsPane = new JPanel();
            listButtonsPane.setLayout(new JideBoxLayout(listButtonsPane, JideBoxLayout.LINE_AXIS, 6));

            addRemoveAndAddButtons(listener, listModel, selectedReset, getSelected, newValueSupplier, createBtn, list, listButtonsPane, deleteBtn);
            addMoveButtonListeners(listener, listModel, getSelected, moveUpBtn, list, listButtonsPane, moveDownBtn);

            listButtonsPane.add(new JPanel(), JideBoxLayout.VARY);
            listPanel.add(listButtonsPane, JideBoxLayout.FIX);
        }

        addNavigationListeners(getReferencedObj, list);

        editorPane.setLayout(new JideBoxLayout(editorPane, JideBoxLayout.PAGE_AXIS));
        listPanel.add(editorPane, JideBoxLayout.FIX);

        CollapsibleItemListCreation<E> result = new CollapsibleItemListCreation<>();
        result.collapsiblePanel = listPanel;
        result.list = list;
        return result;
    }

    private static <S, E, M extends OrderedListenerListModel<S, E>> void addRemoveAndAddButtons(FieldUpdateListener listener, M itemsListModel, BasicLambda selectedItemReset, BasicLambdaWithReturn<E> selectedItem, Supplier<E> newValueSupplier, JButton createBtn, JList<E> itemsList, JPanel listButtonsPane, JButton deleteBtn) {
        createBtn.addActionListener(e -> {
            E tempItem = newValueSupplier.get();
            itemsListModel.addItem(tempItem);
            itemsList.setSelectedValue(tempItem, true);
            listener.valueChanged(new JLabel(), null); //Item changed, but we took care of it, just do the usual notification and JSON update stuff.
            resizeListToFit(itemsList);
        });
        listButtonsPane.add(createBtn, JideBoxLayout.FIX);

        deleteBtn.addActionListener(e -> {
            if (selectedItem.doIt() != null) {
                itemsListModel.removeItem(selectedItem.doIt());
                selectedItemReset.doIt();
                itemsList.clearSelection();
                listener.valueChanged(new JLabel(), null); //Item changed, but we took care of it, just do the usual notification and JSON update stuff.
            }
        });
        listButtonsPane.add(deleteBtn, JideBoxLayout.FIX);
    }

    private static <S, E, M extends OrderedListenerListModel<S, E>> void addMoveButtonListeners(FieldUpdateListener listener, M itemsListModel, BasicLambdaWithReturn<E> selectedItem, JButton moveUpBtn, JList<E> itemsList, JPanel listButtonsPane, JButton moveDownBtn) {
        moveUpBtn.addActionListener(e -> {
            if (selectedItem.doIt() != null) {
                itemsListModel.moveUp(selectedItem.doIt());
                itemsList.setSelectedValue(selectedItem.doIt(), true);
                listener.valueChanged(new JLabel(), null); //Item changed, but we took care of it, just do the usual notification and JSON update stuff.
            }
        });
        listButtonsPane.add(moveUpBtn, JideBoxLayout.FIX);

        moveDownBtn.addActionListener(e -> {
            if (selectedItem.doIt() != null) {
                itemsListModel.moveDown(selectedItem.doIt());
                itemsList.setSelectedValue(selectedItem.doIt(), true);
                listener.valueChanged(new JLabel(), null); //Item changed, but we took care of it, just do the usual notification and JSON update stuff.
            }
        });
        listButtonsPane.add(moveDownBtn, JideBoxLayout.FIX);
    }

    private static <E> void addNavigationListeners(BasicLambdaWithArgAndReturn<E, GameDataElement> getReferencedObj, JList<E> itemsList) {
        // Add listeners to the list for double-click and Enter key to open the editor
        itemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    E selectedValue = itemsList.getSelectedValue();
                    if (selectedValue == null) return;
                    GameDataElement referencedObj = getReferencedObj.doIt(selectedValue);
                    if (referencedObj != null) {
                        ATContentStudio.frame.openEditor(referencedObj);
                        ATContentStudio.frame.selectInTree(referencedObj);
                    }
                }
            }
        });
        itemsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    E selectedValue = itemsList.getSelectedValue();
                    if (selectedValue == null) return;
                    GameDataElement referencedObj = getReferencedObj.doIt(selectedValue);
                    if (referencedObj != null) {
                        ATContentStudio.frame.openEditor(referencedObj);
                        ATContentStudio.frame.selectInTree(referencedObj);
                    }
                }
            }
        });
    }

    /**
     * Adjusts a list's visible row count using the stored row limit, or 8 if none was set.
     *
     * @param list the list to resize
     */
    public static void resizeListToFit(JList<?> list) {
        if (list == null) return;
        Object storedMaxRows = list.getClientProperty(RESIZE_LIST_MAX_ROWS_PROPERTY);
        int maxRows = storedMaxRows instanceof Integer ? (Integer) storedMaxRows : RESIZE_LIST_DEFAULT_ROWS;
        resizeListToFit(list, maxRows);
    }

    /**
     * Adjusts a list's visible row count up to the provided row limit (-1 = now limit).
     * Adds an extra row if the list needs a horizontal scrollbar.
     *
     * @param list the list to resize
     * @param maxRows the maximum number of rows to display, or a negative value to use all rows
     */
    public static void resizeListToFit(JList<?> list, int maxRows) {
        if (list == null) return;
        list.putClientProperty(RESIZE_LIST_MAX_ROWS_PROPERTY, maxRows);
        JScrollPane scroller = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, list);
        if (scroller != null && scroller.getViewport().getWidth() <= 0) { // Not rendered yet, postpone it
            SwingUtilities.invokeLater(() -> resizeListToFit(list, maxRows));
            return;
        }

        final int rowLimit = maxRows < 0 ? list.getModel().getSize() : maxRows;
        int rowCount = Math.min(rowLimit, list.getModel().getSize());
        if (scroller != null && needsHorizontalScrollBar(list, scroller)) {
            rowCount++;
        }
        list.setVisibleRowCount(rowCount);
    }

    private static boolean needsHorizontalScrollBar(JList<?> list, JScrollPane scroller) {
        JViewport viewport = scroller.getViewport();
        if (viewport == null) {
            return false;
        }
        return list.getPreferredSize().width > viewport.getWidth();
    }
}