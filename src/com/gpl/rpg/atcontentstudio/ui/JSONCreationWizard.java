package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameDataElement.State;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.sprites.SpriteChooser;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JSONCreationWizard extends JDialog {

    private static final long serialVersionUID = -5744628699021314026L;

    public static enum DataType {
        none,
        actorCondition,
        dialogue,
        droplist,
        item,
        itemCategory,
        npc,
        quest
    }

    private JSONElement creation = null;
    final JLabel message;
    @SuppressWarnings("rawtypes")
    final JComboBox dataTypeCombo;
    final JTextField idField;
    final JTextField nameField;
    final JButton ok;
    final Project proj;

    public JSONCreationWizard(final Project proj, Class<? extends GameDataElement> dataClass) {
        this(proj);
        if (dataClass == ActorCondition.class) {
            dataTypeCombo.setSelectedItem(DataType.actorCondition);
        } else if (dataClass == Dialogue.class) {
            dataTypeCombo.setSelectedItem(DataType.dialogue);
        } else if (dataClass == Droplist.class) {
            dataTypeCombo.setSelectedItem(DataType.droplist);
        } else if (dataClass == Item.class) {
            dataTypeCombo.setSelectedItem(DataType.item);
        } else if (dataClass == ItemCategory.class) {
            dataTypeCombo.setSelectedItem(DataType.itemCategory);
        } else if (dataClass == NPC.class) {
            dataTypeCombo.setSelectedItem(DataType.npc);
        } else if (dataClass == Quest.class) {
            dataTypeCombo.setSelectedItem(DataType.quest);
        }
        dataTypeCombo.setEnabled(false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public JSONCreationWizard(final Project proj) {
        super(ATContentStudio.frame);
        this.proj = proj;
        setTitle("Create Game Data Element (JSON)");

        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel("Create a new game data element."), JideBoxLayout.FIX);

        message = new JLabel("Select a data type below:");
        pane.add(message, JideBoxLayout.FIX);

        dataTypeCombo = new JComboBox(new DataTypeComboModel());
        dataTypeCombo.setRenderer(new DataTypeComboCellRenderer());
        pane.add(dataTypeCombo);

        final JPanel idPane = new JPanel();
        idPane.setLayout(new BorderLayout());
        JLabel idLabel = new JLabel("Internal ID: ");
        idPane.add(idLabel, BorderLayout.WEST);
        idField = new JTextField("");
        idField.setEditable(true);
        idPane.add(idField, BorderLayout.CENTER);
        pane.add(idPane, JideBoxLayout.FIX);

        final JPanel namePane = new JPanel();
        namePane.setLayout(new BorderLayout());
        JLabel nameLabel = new JLabel("Display name: ");
        namePane.add(nameLabel, BorderLayout.WEST);
        nameField = new JTextField("");
        nameField.setEditable(true);
        namePane.add(nameField, BorderLayout.CENTER);
        pane.add(namePane, JideBoxLayout.FIX);

        final JPanel iconPane = new JPanel();
        iconPane.setLayout(new BorderLayout());
        final JLabel iconLabel = new JLabel("Icon: ");
        iconPane.add(iconLabel, BorderLayout.WEST);
        final JButton iconButton = new JButton(new ImageIcon(DefaultIcons.getActorConditionImage()));
        iconPane.add(iconButton, BorderLayout.CENTER);
        pane.add(iconPane, JideBoxLayout.FIX);
        iconPane.setVisible(true);

        dataTypeCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    idPane.setVisible(true);
                    switch ((DataType) e.getItem()) {
                        case actorCondition:
                            iconPane.setVisible(true);
                            namePane.setVisible(true);
                            iconButton.setIcon(new ImageIcon(DefaultIcons.getActorConditionImage()));
                            creation = new ActorCondition();
                            break;
                        case dialogue:
                            iconPane.setVisible(false);
                            namePane.setVisible(false);
                            creation = new Dialogue();
                            break;
                        case droplist:
                            iconPane.setVisible(false);
                            namePane.setVisible(false);
                            creation = new Droplist();
                            break;
                        case item:
                            iconPane.setVisible(true);
                            namePane.setVisible(true);
                            creation = new Item();
                            iconButton.setIcon(new ImageIcon(DefaultIcons.getItemImage()));
                            break;
                        case itemCategory:
                            iconPane.setVisible(false);
                            namePane.setVisible(true);
                            creation = new ItemCategory();
                            break;
                        case npc:
                            iconPane.setVisible(true);
                            namePane.setVisible(true);
                            creation = new NPC();
                            iconButton.setIcon(new ImageIcon(DefaultIcons.getNPCImage()));
                            break;
                        case quest:
                            iconPane.setVisible(false);
                            namePane.setVisible(true);
                            creation = new Quest();
                            break;
                        default:
                            idPane.setVisible(false);
                            iconPane.setVisible(false);
                            namePane.setVisible(false);
                            creation = null;
                            break;
                    }
                    updateStatus();
                    idPane.revalidate();
                    namePane.revalidate();
                    iconPane.revalidate();
                    idPane.repaint();
                    namePane.repaint();
                    iconPane.repaint();
                }
            }
        });

        iconButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Spritesheet.Category cat = null;
                switch ((DataType) dataTypeCombo.getSelectedItem()) {
                    case actorCondition:
                        cat = Spritesheet.Category.actorcondition;
                        break;
                    case dialogue:
                        break;
                    case droplist:
                        break;
                    case item:
                        cat = Spritesheet.Category.item;
                        break;
                    case itemCategory:
                        break;
                    case npc:
                        cat = Spritesheet.Category.monster;
                        break;
                    case quest:
                        break;
                    default:
                        break;

                }
                if (cat == null) return;
                SpriteChooser chooser = SpriteChooser.getChooser(proj, cat);
                chooser.setSelectionListener(new SpriteChooser.SelectionListener() {
                    @Override
                    public void iconSelected(String selected) {
                        if (selected != null) {
                            switch ((DataType) dataTypeCombo.getSelectedItem()) {
                                case actorCondition:
                                    ((ActorCondition) creation).icon_id = selected;
                                    break;
                                case item:
                                    ((Item) creation).icon_id = selected;
                                    break;
                                case npc:
                                    ((NPC) creation).icon_id = selected;
                                    break;
                                case dialogue:
                                case droplist:
                                case itemCategory:
                                case quest:
                                default:
                                    break;

                            }
                            iconButton.setIcon(new ImageIcon(proj.getImage(selected)));
                            iconButton.revalidate();
                            iconButton.repaint();
                            updateStatus();
                        }
                    }
                });
                chooser.setVisible(true);
            }
        });

        pane.add(new JPanel(), JideBoxLayout.VARY);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS, 6));
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);
        JButton cancel = new JButton("Cancel");
        buttonPane.add(cancel, JideBoxLayout.FIX);
        ok = new JButton("Ok");
        buttonPane.add(ok, JideBoxLayout.FIX);
        pane.add(new JPanel(), JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch ((DataType) dataTypeCombo.getSelectedItem()) {
                    case actorCondition:
                        ((ActorCondition) creation).display_name = nameField.getText();
                        break;
                    case item:
                        ((Item) creation).name = nameField.getText();
                        break;
                    case npc:
                        ((NPC) creation).name = nameField.getText();
                        break;
                    case dialogue:
                    case droplist:
                        break;
                    case itemCategory:
                        ((ItemCategory) creation).name = nameField.getText();
                        break;
                    case quest:
                        ((Quest) creation).name = nameField.getText();
                        break;
                    default:
                        return;
                }
                creation.id = idField.getText();
                JSONCreationWizard.this.setVisible(false);
                JSONCreationWizard.this.dispose();
                creation.state = State.created;
                proj.createElement(creation);
                notifyCreated();
                ATContentStudio.frame.selectInTree(creation);
                ATContentStudio.frame.openEditor(creation);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                creation = null;
                JSONCreationWizard.this.setVisible(false);
                JSONCreationWizard.this.dispose();
            }
        });

        DocumentListener statusUpdater = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }
        };
        idField.getDocument().addDocumentListener(statusUpdater);
        nameField.getDocument().addDocumentListener(statusUpdater);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(350, 250));
        idPane.setVisible(false);
        iconPane.setVisible(false);
        namePane.setVisible(false);
        updateStatus();
        pack();

        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
    }

    public void updateStatus() {
        boolean trouble = false;
        message.setText("<html><font color=\"#00AA00\">Looks OK to me.</font></html>");
        if (creation == null) {
            message.setText("<html><font color=\"#FF0000\">Select a data type below:</font></html>");
            trouble = true;
        } else if (idField.getText() == null || idField.getText().length() <= 0) {
            message.setText("<html><font color=\"#FF0000\">Internal ID must not be empty.</font></html>");
            trouble = true;
        } else {
            switch ((DataType) dataTypeCombo.getSelectedItem()) {
                case actorCondition:
                    if (nameField.getText() == null || nameField.getText().length() <= 0) {
                        message.setText("<html><font color=\"#FF0000\">An actor condition must have a name.</font></html>");
                        trouble = true;
                    } else if (((ActorCondition) creation).icon_id == null) {
                        message.setText("<html><font color=\"#FF0000\">An actor condition must have an icon.</font></html>");
                        trouble = true;
                    } else if (proj.getActorCondition(idField.getText()) != null) {
                        if (proj.getActorCondition(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">An actor condition with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getActorCondition(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">An actor condition with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getActorCondition(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">An actor condition with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case item:
                    if (nameField.getText() == null || nameField.getText().length() <= 0) {
                        message.setText("<html><font color=\"#FF0000\">An item must have a name.</font></html>");
                        trouble = true;
                    } else if (((Item) creation).icon_id == null) {
                        message.setText("<html><font color=\"#FF0000\">An item must have an icon.</font></html>");
                        trouble = true;
                    } else if (proj.getItem(idField.getText()) != null) {
                        if (proj.getItem(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">An item with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getItem(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">An item with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getItem(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">An item with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case npc:
                    if (nameField.getText() == null || nameField.getText().length() <= 0) {
                        message.setText("<html><font color=\"#FF0000\">A NPC must have a name.</font></html>");
                        trouble = true;
                    } else if (((NPC) creation).icon_id == null) {
                        message.setText("<html><font color=\"#FF0000\">A NPC must have an icon.</font></html>");
                        trouble = true;
                    } else if (proj.getNPC(idField.getText()) != null) {
                        if (proj.getNPC(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">A NPC with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getNPC(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">A NPC with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getNPC(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">A NPC with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case dialogue:
                    if (proj.getDialogue(idField.getText()) != null) {
                        if (proj.getDialogue(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">A dialogue with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getDialogue(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">A dialogue with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getDialogue(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">A dialogue with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case droplist:
                    if (proj.getDroplist(idField.getText()) != null) {
                        if (proj.getDroplist(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">A droplist with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getDroplist(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">A droplist with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getDroplist(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">A droplist with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case itemCategory:
                    if (nameField.getText() == null || nameField.getText().length() <= 0) {
                        message.setText("<html><font color=\"#FF0000\">An item category must have a name.</font></html>");
                        trouble = true;
                    } else if (proj.getItemCategory(idField.getText()) != null) {
                        if (proj.getItemCategory(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">An item category with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getItemCategory(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">An item category with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getItemCategory(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">An item category with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                case quest:
                    if (nameField.getText() == null || nameField.getText().length() <= 0) {
                        message.setText("<html><font color=\"#FF0000\">A quest must have a name.</font></html>");
                        trouble = true;
                    } else if (proj.getQuest(idField.getText()) != null) {
                        if (proj.getQuest(idField.getText()).getDataType() == GameSource.Type.created) {
                            message.setText("<html><font color=\"#FF0000\">A quest with the same ID was already created in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getQuest(idField.getText()).getDataType() == GameSource.Type.altered) {
                            message.setText("<html><font color=\"#FF0000\">A quest with the same ID exists in the game and is already altered in this project.</font></html>");
                            trouble = true;
                        } else if (proj.getQuest(idField.getText()).getDataType() == GameSource.Type.source) {
                            message.setText("<html><font color=\"#FF9000\">A quest with the same ID exists in the game. It will be added under \"altered\".</font></html>");
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        ok.setEnabled(!trouble);

        message.revalidate();
        message.repaint();
    }

    public static String dataTypeDesc(DataType type) {
        switch (type) {
            case actorCondition:
                return "Actor Condition";
            case dialogue:
                return "Dialogue";
            case droplist:
                return "Droplist";
            case item:
                return "Item";
            case itemCategory:
                return "Item Category";
            case npc:
                return "NPC";
            case quest:
                return "Quest";
            default:
                return "Select below";
        }
    }

    @SuppressWarnings("rawtypes")
    public static class DataTypeComboModel implements ComboBoxModel {

        DataType selected = DataType.none;

        @Override
        public int getSize() {
            return DataType.values().length;
        }

        @Override
        public Object getElementAt(int index) {
            return DataType.values()[index];
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = (DataType) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }
    }

    public static class DataTypeComboCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 5621373849299980998L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                ((JLabel) c).setText(JSONCreationWizard.dataTypeDesc((DataType) value));
                switch ((DataType) value) {
                    case actorCondition:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getActorConditionIcon()));
                        break;
                    case dialogue:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getDialogueIcon()));
                        break;
                    case droplist:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getDroplistIcon()));
                        break;
                    case item:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getItemIcon()));
                        break;
                    case itemCategory:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getDroplistIcon()));
                        break;
                    case npc:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getNPCIcon()));
                        break;
                    case quest:
                        ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getQuestIcon()));
                        break;
                    default:
                        break;

                }
            }
            return c;
        }
    }

    public static interface CreationCompletedListener {
        public void elementCreated(JSONElement created);
    }

    private List<CreationCompletedListener> listeners = new CopyOnWriteArrayList<JSONCreationWizard.CreationCompletedListener>();

    public void addCreationListener(CreationCompletedListener l) {
        listeners.add(l);
    }

    public void notifyCreated() {
        for (CreationCompletedListener l : listeners) {
            l.elementCreated(creation);
        }
    }

}
