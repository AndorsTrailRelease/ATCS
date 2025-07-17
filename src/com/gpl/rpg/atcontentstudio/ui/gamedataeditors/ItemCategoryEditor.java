package com.gpl.rpg.atcontentstudio.ui.gamedataeditors;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.ItemCategory;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ItemCategoryEditor extends JSONElementEditor {

    private static final long serialVersionUID = -2893876158803488355L;

    private static final String form_view_id = "Form";
    private static final String json_view_id = "JSON";

    public ItemCategoryEditor(ItemCategory ic) {
        super(ic, ic.getDesc(), ic.getIcon());
        addEditorTab(form_view_id, getFormView());
        addEditorTab(json_view_id, getJSONView());
    }

    private JButton icIcon;
    private JTextField idField;
    private JTextField nameField;
    @SuppressWarnings("rawtypes")
    private JComboBox slotBox;
    @SuppressWarnings("rawtypes")
    private JComboBox typeBox;
    @SuppressWarnings("rawtypes")
    private JComboBox sizeBox;

    @SuppressWarnings("unchecked")
    @Override
    public void insertFormViewDataField(JPanel pane) {
        final ItemCategory ic = ((ItemCategory) target);
        final FieldUpdateListener listener = new ItemCategoryFieldUpdater();

        icIcon = createButtonPane(pane, ic.getProject(), ic, ItemCategory.class, ic.getImage(), null, listener);


        idField = addTextField(pane, "Internal ID: ", ic.id, ic.writable, listener);
        nameField = addTranslatableTextField(pane, "Display name: ", ic.name, ic.writable, listener);
        typeBox = addEnumValueBox(pane, "Action type: ", ItemCategory.ActionType.values(), ic.action_type, ic.writable, listener);
        slotBox = addEnumValueBox(pane, "Inventory slot: ", ItemCategory.InventorySlot.values(), ic.slot, ic.writable, listener);
        sizeBox = addEnumValueBox(pane, "Item size: ", ItemCategory.Size.values(), ic.size, ic.writable, listener);
        if (ic.action_type != ItemCategory.ActionType.equip) {
            slotBox.setEnabled(false);
        }
        slotBox.setRenderer(new SlotCellRenderer());
    }

    public class SlotCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -8359181274986492979L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                ((JLabel) c).setIcon(new ImageIcon(ItemCategory.getIcon((ItemCategory.InventorySlot) value)));
                if (value == null) {
                    if (typeBox.getSelectedItem() == ItemCategory.ActionType.equip) {
                        ((JLabel) c).setText("Undefined. Select the slot to use when equipped.");
                    } else {
                        ((JLabel) c).setText("Non equippable. Select \"equip\" action type.");
                    }
                }
            }
            return c;
        }
    }

    public class ItemCategoryFieldUpdater implements FieldUpdateListener {
        @Override
        public void valueChanged(JComponent source, Object value) {
            ItemCategory ic = (ItemCategory) target;
            if (source == idField) {
                //Events caused by cancel an ID edition. Dismiss.
                if (skipNext) {
                    skipNext = false;
                    return;
                }
                if (target.id.equals((String) value)) return;

                if (idChanging()) {
                    ic.id = (String) value;
                    ItemCategoryEditor.this.name = ic.getDesc();
                    ic.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(ItemCategoryEditor.this);
                } else {
                    cancelIdEdit(idField);
                    return;
                }
            } else if (source == nameField) {
                ic.name = (String) value;
                ItemCategoryEditor.this.name = ic.getDesc();
                ic.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ItemCategoryEditor.this);
            } else if (source == slotBox) {
                ic.slot = (ItemCategory.InventorySlot) value;
                icIcon.setIcon(new ImageIcon(ic.getImage()));
                ItemCategoryEditor.this.icon = new ImageIcon(ic.getIcon());
                ic.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ItemCategoryEditor.this);
            } else if (source == typeBox) {
                ic.action_type = (ItemCategory.ActionType) value;
                if (ic.action_type != ItemCategory.ActionType.equip && ic.slot != null) {
                    ic.slot = null;
                    slotBox.setSelectedItem(null);
                    slotBox.setEnabled(false);
                    icIcon.setIcon(new ImageIcon(ic.getImage()));
                    ItemCategoryEditor.this.icon = new ImageIcon(ic.getIcon());
                    ic.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(ItemCategoryEditor.this);
                } else if (ic.action_type == ItemCategory.ActionType.equip) {
                    slotBox.setEnabled(true);
                }
            } else if (source == sizeBox) {
                ic.size = (ItemCategory.Size) value;
            }

            if (ic.state != GameDataElement.State.modified) {
                ic.state = GameDataElement.State.modified;
                ItemCategoryEditor.this.name = ic.getDesc();
                ic.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(ItemCategoryEditor.this);
            }
            updateJsonViewText(ic.toJsonString());
        }
    }

}
