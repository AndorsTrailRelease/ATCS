package com.gpl.rpg.atcontentstudio.utils;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.ui.CollapsiblePanel;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.OrderedListenerListModel;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.event.*;
import java.util.function.Supplier;

public class UiUtils {
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
        CollapsiblePanel itemsPane = new CollapsiblePanel(title);
        itemsPane.setLayout(new JideBoxLayout(itemsPane, JideBoxLayout.PAGE_AXIS));
        final JList<E> list = new JList<>(listModel);
        list.setCellRenderer(cellRenderer);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemsPane.add(new JScrollPane(list), JideBoxLayout.FIX);
        final JPanel editorPane = new JPanel();
        final JButton createBtn = new JButton(new ImageIcon(DefaultIcons.getCreateIcon()));
        final JButton deleteBtn = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        final JButton moveUpBtn = new JButton(new ImageIcon(DefaultIcons.getArrowUpIcon()));
        final JButton moveDownBtn = new JButton(new ImageIcon(DefaultIcons.getArrowDownIcon()));
        deleteBtn.setEnabled(false);
        moveUpBtn.setEnabled(false);
        moveDownBtn.setEnabled(false);
        list.addListSelectionListener(e -> {
            E selectedValue = list.getSelectedValue();
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
            itemsPane.add(listButtonsPane, JideBoxLayout.FIX);
        }

        addNavigationListeners(getReferencedObj, list);

        editorPane.setLayout(new JideBoxLayout(editorPane, JideBoxLayout.PAGE_AXIS));
        itemsPane.add(editorPane, JideBoxLayout.FIX);

        return new CollapsibleItemListCreation<E>() {
            {
                collapsiblePanel = itemsPane;
                list = list;
            }
        };
    }

    private static <S, E, M extends OrderedListenerListModel<S, E>> void addRemoveAndAddButtons(FieldUpdateListener listener, M itemsListModel, BasicLambda selectedItemReset, BasicLambdaWithReturn<E> selectedItem, Supplier<E> newValueSupplier, JButton createBtn, JList<E> itemsList, JPanel listButtonsPane, JButton deleteBtn) {
        createBtn.addActionListener(e -> {
            E tempItem = newValueSupplier.get();
            itemsListModel.addItem(tempItem);
            itemsList.setSelectedValue(tempItem, true);
            listener.valueChanged(new JLabel(), null); //Item changed, but we took care of it, just do the usual notification and JSON update stuff.
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

}