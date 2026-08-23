package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemFilter;
import com.gpl.rpg.atcontentstudio.ui.CollapsiblePanel;
import com.gpl.rpg.atcontentstudio.ui.Editor;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.OrderedListenerListModel;
import com.gpl.rpg.atcontentstudio.utils.UiUtils;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class ItemFilterEditor extends JSONElementEditor {

    @Serial
    private static final long serialVersionUID = -6821626848022370417L;

    private static final String form_view_id = "Form";
    private static final String json_view_id = "JSON";

    private ItemFilter.FilteredItem selectedItem;

    private JTextField idField;
    private Editor.MyComboBox itemCombo;
    private IncludeItemsListModel itemsListModel;
    private boolean updatingIncludeItemBox;

    public ItemFilterEditor(ItemFilter itemFilter) {
        super(itemFilter, itemFilter.getDesc(), itemFilter.getIcon());
        addEditorTab(form_view_id, getFormView());
        addEditorTab(json_view_id, getJSONView());
    }

    @Override
    public void insertFormViewDataField(JPanel pane) {

        final ItemFilter filter = (ItemFilter) target;
        final FieldUpdateListener listener = new ItemFilterFieldUpdater();

        createButtonPane(pane, filter.getProject(), filter, ItemFilter.class, filter.getIcon(), null, listener);

        idField = addTextField(pane, "Item Filter ID: ", filter.id, filter.writable, listener);

        itemsListModel = new IncludeItemsListModel(filter);
        CollapsiblePanel itemsPane = UiUtils.getCollapsibleItemList(
                listener,
                itemsListModel,
                () -> {
                    if (this.selectedItem != null) {
                        filter.removeItemBacklink(this.selectedItem);
                    }
                    this.selectedItem = null;
                },
                (selectedItem) -> this.selectedItem = selectedItem,
                () -> this.selectedItem,
                (selectedItem) -> {
                },
                (includeItemsEditorPane) -> updateFilteredItemsEditorPane(includeItemsEditorPane, this.selectedItem, listener),
                filter.writable,
                ItemFilter.FilteredItem::new,
                new IncludeItemCellRenderer(),
                "Items matching this filter: ",
                (x) -> x.item
        ).collapsiblePanel;
        if (filter.include == null || filter.include.isEmpty()) {
            itemsPane.collapse();
        }
        pane.add(itemsPane, JideBoxLayout.FIX);
    }

    private void updateFilteredItemsEditorPane(JPanel pane, ItemFilter.FilteredItem filteredItem, FieldUpdateListener listener) {
        pane.removeAll();
        if (itemCombo != null) {
            removeElementListener(itemCombo);
        }

        if (filteredItem != null) {
            Item item = filteredItem.item;
            if (item == null && target.getProject() != null && filteredItem.item_id != null) {
                item = target.getProject().getItem(filteredItem.item_id);
            }
            itemCombo = addItemBox(pane, target.getProject(), "Item: ", item, target.writable, listener);
            updatingIncludeItemBox = true;
            itemCombo.setSelectedItem(item);
            updatingIncludeItemBox = false;
        }

        pane.revalidate();
        pane.repaint();
    }

    public class IncludeItemsListModel extends OrderedListenerListModel<ItemFilter, ItemFilter.FilteredItem> {
        public IncludeItemsListModel(ItemFilter source) {
            super(source);
        }

        @Override
        protected List<ItemFilter.FilteredItem> getItems() {
            return source.include;
        }

        @Override
        protected void setItems(List<ItemFilter.FilteredItem> items) {
            source.include = items;
        }

        public ItemFilter getFilter() {
            return source;
        }
    }

    public static class IncludeItemCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel label) {
                ItemFilter.FilteredItem filteredItem = (ItemFilter.FilteredItem) value;
                Item item = filteredItem.item;
                if (item != null) {
                    label.setIcon(new ImageIcon(item.getIcon()));
                    label.setText(item.getDesc());
                } else if (filteredItem.item_id == null || filteredItem.item_id.isEmpty()) {
                    label.setText("New, undefined, included item.");
                } else {
                    label.setText(filteredItem.item_id);
                }
            }
            return c;
        }
    }

    public class ItemFilterFieldUpdater implements FieldUpdateListener {
        @Override
        public void valueChanged(JComponent source, Object value) {
            ItemFilter filter = (ItemFilter) target;
            if (source == idField) {
                if (skipNext) {
                    skipNext = false;
                    return;
                }
                if (target.id.equals(value)) return;
                if (idChanging()) {
                    filter.id = (String) value;
                    ItemFilterEditor.this.name = filter.getDesc();
                    filter.childrenChanged(new ArrayList<>());
                    ATContentStudio.frame.editorChanged(ItemFilterEditor.this);
                } else {
                    cancelIdEdit(idField);
                    return;
                }
            } else if (source == itemCombo) {
                if (updatingIncludeItemBox) return;
                if (selectedItem != null) {
                    filter.removeItemBacklink(selectedItem);
                    selectedItem.item = (Item) value;
                    selectedItem.item_id = selectedItem.item == null ? null : selectedItem.item.id;
                    filter.addItemBacklink(selectedItem);
                    itemsListModel.itemChanged(selectedItem);
                    markModified(filter);
                }
                updateJsonViewText(filter.toJsonString());
                return;
            }
            if (filter.state != GameDataElement.State.modified) {
                filter.state = GameDataElement.State.modified;
                ItemFilterEditor.this.name = filter.getDesc();
                filter.childrenChanged(new ArrayList<>());
                ATContentStudio.frame.editorChanged(ItemFilterEditor.this);
            }
            updateJsonViewText(filter.toJsonString());
        }
    }

    private void markModified(ItemFilter filter) {
        if (filter.state != GameDataElement.State.modified) {
            filter.state = GameDataElement.State.modified;
            ItemFilterEditor.this.name = filter.getDesc();
            filter.childrenChanged(new ArrayList<>());
            ATContentStudio.frame.editorChanged(ItemFilterEditor.this);
        }
    }
}
