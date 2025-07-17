package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.jidesoft.swing.JideBoxLayout;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class JSONImportWizard extends JDialog {

    private static final long serialVersionUID = 661234868711700156L;

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

    Project proj;

    JPanel pane;
    JLabel message;
    @SuppressWarnings("rawtypes")
    JComboBox dataTypeCombo;
    JRadioButton importFromFile;
    JRadioButton importPasted;
    JPanel fileSelectionPane;
    JTextField jsonFileName;
    JButton browse;
    RSyntaxTextArea jsonPasteArea;
    JScrollPane scroller;
    @SuppressWarnings("rawtypes")
    JList createdPreview;
    JPanel buttonPane;
    JButton ok, cancel;
    ActionListener okListener, cancelListener;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JSONImportWizard(Project proj) {

        super(ATContentStudio.frame);
        setTitle("Import data from JSON");

        this.proj = proj;

        pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel("Import data in JSON format."), JideBoxLayout.FIX);

        message = new JLabel();

        dataTypeCombo = new JComboBox(new DataTypeComboModel());
        dataTypeCombo.setRenderer(new DataTypeComboCellRenderer());

        dataTypeCombo.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    checkEnableNext();
                }
            }
        });

        importPasted = new JRadioButton("Paste JSON text");
        importFromFile = new JRadioButton("Select .json file");
        importPasted.setSelected(true);
        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(importPasted);
        radioGroup.add(importFromFile);

        importPasted.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (importPasted.isSelected()) {
                    scroller.setVisible(true);
                    fileSelectionPane.setVisible(false);
                    pane.revalidate();
                    pane.repaint();
                }
            }
        });
        importFromFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (importFromFile.isSelected()) {
                    scroller.setVisible(false);
                    fileSelectionPane.setVisible(true);
                    pane.revalidate();
                    pane.repaint();
                }
            }
        });

        jsonFileName = new JTextField();
        jsonFileName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnableNext();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnableNext();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnableNext();
            }
        });
        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser() {
                    private static final long serialVersionUID = -3001082967957619011L;

                    @Override
                    public boolean accept(File f) {
                        if (f.isDirectory() || f.getName().endsWith(".json") || f.getName().endsWith(".JSON")) {
                            return super.accept(f);
                        } else {
                            return false;
                        }
                    }
                };
                jfc.setMultiSelectionEnabled(false);
                int result = jfc.showOpenDialog(ATContentStudio.frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    jsonFileName.setText(jfc.getSelectedFile().getAbsolutePath());
                    checkEnableNext();
                }
            }
        });
        fileSelectionPane = new JPanel();
        fileSelectionPane.setLayout(new JideBoxLayout(fileSelectionPane, JideBoxLayout.LINE_AXIS, 6));
        fileSelectionPane.add(new JLabel("JSON File: "), JideBoxLayout.FIX);
        fileSelectionPane.add(jsonFileName, JideBoxLayout.VARY);
        fileSelectionPane.add(browse, JideBoxLayout.FIX);

        jsonPasteArea = new RSyntaxTextArea();
        jsonPasteArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

        jsonPasteArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnableNext();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnableNext();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnableNext();
            }

        });


        buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS, 6));
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);
        cancel = new JButton("Cancel");
        buttonPane.add(cancel, JideBoxLayout.FIX);
        ok = new JButton("Next");
        buttonPane.add(ok, JideBoxLayout.FIX);

        createdPreview = new JList(new GDEListModel(new ArrayList<GameDataElement>()));

        showFirstScreen();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(450, 350));
        pack();

        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);

    }

    private void showFirstScreen() {
        pane.removeAll();
        message.setText("Select a data type & paste your JSON data below:");
        pane.add(message, JideBoxLayout.FIX);
        pane.add(dataTypeCombo, JideBoxLayout.FIX);
        pane.add(importPasted, JideBoxLayout.FIX);
        pane.add(importFromFile, JideBoxLayout.FIX);
        pane.add(fileSelectionPane, JideBoxLayout.FIX);
        scroller = new JScrollPane(jsonPasteArea);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        JPanel scrollHolder = new JPanel();
        scrollHolder.setLayout(new BorderLayout());
        scrollHolder.add(scroller, BorderLayout.CENTER);
        pane.add(scrollHolder, JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);
        ok.setText("Next");
        ok.setEnabled(jsonPasteArea.getText() != null && jsonPasteArea.getText().length() > 0 && dataTypeCombo.getSelectedItem() != null && dataTypeCombo.getSelectedItem() != DataType.none);
        ok.removeActionListener(okListener);
        okListener = new ActionListener() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> errors = new ArrayList<String>();
                List<String> warnings = new ArrayList<String>();
                List<JSONElement> created = new ArrayList<JSONElement>();
                Object jsonParserOutput = null;
                try {
                    if (importPasted.isSelected()) {
                        jsonParserOutput = new JSONParser().parse(jsonPasteArea.getText());
                    } else if (importFromFile.isSelected()) {
                        jsonParserOutput = new JSONParser().parse(new FileReader(new File(jsonFileName.getText())));
                    }
                } catch (ParseException e1) {
                    errors.add("Invalid JSON content: " + e1.getMessage());
                } catch (FileNotFoundException e1) {
                    errors.add("Unable to access file: " + e1.getMessage());
                } catch (IOException e1) {
                    errors.add("Error while accessing file: " + e1.getMessage());
                }
                if (jsonParserOutput != null) {
                    List<Map> jsonObjects = null;
                    if (jsonParserOutput instanceof List) {
                        jsonObjects = (List) jsonParserOutput;
                    } else if (jsonParserOutput instanceof Map) {
                        jsonObjects = new ArrayList<Map>();
                        jsonObjects.add((Map) jsonParserOutput);
                    } else {
                        errors.add("Invalid JSON content: neither an array nor an object.");
                    }
                    if (jsonObjects != null) {
                        JSONElement node;
                        JSONElement existingNode;
                        int i = 0;
                        for (Map jsonObject : jsonObjects) {
                            switch ((DataType) dataTypeCombo.getSelectedItem()) {
                                case actorCondition:
                                    node = ActorCondition.fromJson(jsonObject);
                                    existingNode = proj.getActorCondition(node.id);
                                    break;
                                case item:
                                    node = Item.fromJson(jsonObject);
                                    existingNode = proj.getItem(node.id);
                                    break;
                                case npc:
                                    node = NPC.fromJson(jsonObject);
                                    existingNode = proj.getNPC(node.id);
                                    break;
                                case dialogue:
                                    node = Dialogue.fromJson(jsonObject);
                                    existingNode = proj.getDialogue(node.id);
                                    break;
                                case droplist:
                                    node = Droplist.fromJson(jsonObject);
                                    existingNode = proj.getDroplist(node.id);
                                    break;
                                case itemCategory:
                                    node = ItemCategory.fromJson(jsonObject);
                                    existingNode = proj.getItemCategory(node.id);
                                    break;
                                case quest:
                                    node = Quest.fromJson(jsonObject);
                                    existingNode = proj.getQuest(node.id);
                                    break;
                                default:
                                    return;
                            }
                            i++;
                            if (node instanceof JSONElement) {
                                node.parse(jsonObject);
                                created.add(node);
                                if (existingNode != null) {
                                    if (existingNode.getDataType() == GameSource.Type.created) {
                                        errors.add("An item with id " + node.id + " is already created in this project.");
                                    } else if (existingNode.getDataType() == GameSource.Type.altered) {
                                        errors.add("An item with id " + node.id + " is already altered in this project.");
                                    } else {
                                        node.jsonFile = existingNode.jsonFile;
                                        warnings.add("An item with id " + node.id + " exists in the used game source. This one will be inserted as \"altered\"");
                                    }
                                }
                            } else {
                                warnings.add("Failed to load element #" + i);
                            }
                        }
                    }
                }
                if (errors.isEmpty() && warnings.isEmpty()) {
                    showImportPreviewScreen(created);
                } else if (!errors.isEmpty()) {
                    showErrorScreen(errors);
                } else {
                    showWarningScreen(warnings, created);
                }
            }
        };
        ok.addActionListener(okListener);
        cancel.setText("Cancel");
        cancel.removeActionListener(cancelListener);
        cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONImportWizard.this.setVisible(false);
                JSONImportWizard.this.dispose();
            }
        };
        cancel.addActionListener(cancelListener);
        if (importPasted.isSelected()) {
            scroller.setVisible(true);
            fileSelectionPane.setVisible(false);
            pane.revalidate();
            pane.repaint();
        } else if (importFromFile.isSelected()) {
            scroller.setVisible(false);
            fileSelectionPane.setVisible(true);
            pane.revalidate();
            pane.repaint();
        }
        pane.revalidate();
        pane.repaint();
    }

    private void checkEnableNext() {
        if (dataTypeCombo.getSelectedItem() != null && dataTypeCombo.getSelectedItem() != DataType.none) {
            if (importPasted.isSelected()) {
                ok.setEnabled(jsonPasteArea.getText() != null && jsonPasteArea.getText().length() > 0);
            } else if (importFromFile.isSelected()) {
                ok.setEnabled(jsonFileName.getText() != null && jsonFileName.getText().length() > 0 && new File(jsonFileName.getText()).exists() && !(new File(jsonFileName.getText()).isDirectory()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void showImportPreviewScreen(final List<JSONElement> created) {
        pane.removeAll();
        message.setText("The following data has been found. Click \"Ok\" to confirm.");
        pane.add(message, JideBoxLayout.FIX);
        createdPreview.setModel(new GDEListModel(created));
        createdPreview.setCellRenderer(new GDERenderer(false));
        pane.add(new JScrollPane(createdPreview), JideBoxLayout.FIX);
        pane.add(new JPanel(), JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);
        ok.setText("Ok");
        ok.removeActionListener(okListener);
        okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proj.createElements(created);
                JSONElement lastNode = created.get(created.size() - 1);
                if (lastNode != null) {
                    //	lastNode.save();
                    ATContentStudio.frame.selectInTree(lastNode);
                }
                JSONImportWizard.this.setVisible(false);
                JSONImportWizard.this.dispose();
            }
        };
        ok.addActionListener(okListener);
        cancel.setText("Back");
        cancel.removeActionListener(cancelListener);
        cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFirstScreen();
            }
        };
        cancel.addActionListener(cancelListener);

        pane.revalidate();
        pane.repaint();
    }

    @SuppressWarnings("unchecked")
    private void showErrorScreen(List<String> errors) {
        pane.removeAll();
        message.setText("Failed to import. The following error(s) have been encountered:");
        pane.add(message, JideBoxLayout.FIX);
        createdPreview.setModel(new GDEListModel(errors));
        createdPreview.setCellRenderer(new ErrorRenderer());
        pane.add(new JScrollPane(createdPreview), JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);
        cancel.setText("Back");
        cancel.removeActionListener(cancelListener);
        cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFirstScreen();
            }
        };
        cancel.addActionListener(cancelListener);
        ok.setText("Close");
        ok.removeActionListener(okListener);
        okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONImportWizard.this.setVisible(false);
                JSONImportWizard.this.dispose();
            }
        };
        ok.addActionListener(okListener);

        pane.revalidate();
        pane.repaint();
    }

    @SuppressWarnings("unchecked")
    private void showWarningScreen(List<String> warnings, final List<JSONElement> created) {
        pane.removeAll();
        message.setText("The following warnings(s) were raised while importing:");
        pane.add(message, JideBoxLayout.FIX);
        createdPreview.setModel(new GDEListModel(warnings));
        createdPreview.setCellRenderer(new WarningRenderer());
        pane.add(new JScrollPane(createdPreview), JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);

        ok.setText("Continue anyway");
        ok.removeActionListener(okListener);
        okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showImportPreviewScreen(created);
            }
        };
        ok.addActionListener(okListener);

        cancel.setText("Close");
        cancel.removeActionListener(cancelListener);
        cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JSONImportWizard.this.setVisible(false);
                JSONImportWizard.this.dispose();
            }
        };
        cancel.addActionListener(cancelListener);

        pane.revalidate();
        pane.repaint();
    }

    public class GDERenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;

        private boolean includeType;

        public GDERenderer(boolean includeType) {
            super();
            this.includeType = includeType;

        }

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("none");
            } else {
                if (includeType && ((GameDataElement) value).getDataType() != null) {
                    label.setText(((GameDataElement) value).getDataType().toString() + "/" + ((GameDataElement) value).getDesc());
                } else {
                    label.setText(((GameDataElement) value).getDesc());
                }
                switch ((DataType) dataTypeCombo.getSelectedItem()) {
                    case actorCondition:
                        label.setIcon(new ImageIcon(proj.getIcon(((ActorCondition) value).icon_id)));
                        break;
                    case item:
                        label.setIcon(new ImageIcon(proj.getIcon(((Item) value).icon_id)));
                        break;
                    case npc:
                        label.setIcon(new ImageIcon(proj.getIcon(((NPC) value).icon_id)));
                        break;
                    case dialogue:
                        label.setIcon(new ImageIcon(((Dialogue) value).getIcon()));
                        break;
                    case droplist:
                        label.setIcon(new ImageIcon(((Droplist) value).getIcon()));
                        break;
                    case itemCategory:
                        label.setIcon(new ImageIcon(((ItemCategory) value).getIcon()));
                        break;
                    case quest:
                        label.setIcon(new ImageIcon(((Quest) value).getIcon()));
                        break;
                    default:
                        Notification.addError("Unable to find icon for " + ((GameDataElement) value).getDesc());
                }
            }
            return label;
        }

    }

    public static class ErrorRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -4265342800284721660L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                ((JLabel) c).setIcon(NotificationsPane.icons.get(Notification.Type.ERROR));
            }
            return c;
        }
    }


    public static class WarningRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -3836045237946111606L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                ((JLabel) c).setIcon(NotificationsPane.icons.get(Notification.Type.WARN));
            }
            return c;
        }
    }

    @SuppressWarnings("rawtypes")
    public static class GDEListModel implements ListenerCollectionModel {

        List<? extends Object> source;

        public GDEListModel(List<? extends Object> source) {
            this.source = source;
        }

        @Override
        public Collection getElements() {
            return source;
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
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
                ((JLabel) c).setText(dataTypeDesc((DataType) value));
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

}
