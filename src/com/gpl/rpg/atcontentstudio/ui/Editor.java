package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectElementListener;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.utils.WeblateIntegration;
import com.jidesoft.swing.ComboBoxSearchable;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;

public abstract class Editor extends JPanel implements ProjectElementListener {

    private static final long serialVersionUID = 241750514033596878L;
    private static final FieldUpdateListener nullListener = new FieldUpdateListener() {
        @Override
        public void valueChanged(JComponent source, Object value) {
        }
    };

    public static final String SAVE = "Save";
    public static final String DELETE = "Delete";
    public static final String REVERT = "Revert to original";
    public static final String ALTER = "Alter";
    public static final String GO_TO_ALTERED = "Go to altered";


    public static final String READ_ONLY_MESSAGE =
            "<html><i>" +
                    "This element is not modifiable.<br/>" +
                    "Click on the \"Alter\" button to create a writable copy." +
                    "</i></html>";

    public static final String ALTERED_EXISTS_MESSAGE =
            "<html><i>" +
                    "This element is not modifiable.<br/>" +
                    "A writable copy exists in this project. Click on \"Go to altered\" to open it." +
                    "</i></html>";

    public static final String ALTERED_MESSAGE =
            "<html><i>" +
                    "This element is a writable copy of an element of the referenced game source.<br/>" +
                    "Take care not to break existing content when modifying it." +
                    "</i></html>";

    public static final String CREATED_MESSAGE =
            "<html><i>" +
                    "This element is a creation of yours.<br/>" +
                    "Do as you please." +
                    "</i></html>";


    public String name = "Editor";
    public Icon icon = null;
    public GameDataElement target = null;

    public JLabel message = null;


    public static JTextField addLabelField(JPanel pane, String label, String value) {
        return addTextField(pane, label, value, false, nullListener);
    }

    public static void addTranslationPane(JPanel pane, final JTextComponent tfComponent, final String initialValue) {
        if (Workspace.activeWorkspace.settings.translatorLanguage.getCurrentValue() != null) {
            JPanel labelPane = new JPanel();
            labelPane.setLayout(new JideBoxLayout(labelPane, JideBoxLayout.LINE_AXIS, 6));
            final JLabel translateLinkLabel = new JLabel(getWeblateLabelLink(initialValue));
            labelPane.add(translateLinkLabel, JideBoxLayout.FIX);
            labelPane.add(new JLabel(" "), JideBoxLayout.FIX);
            final JLabel translationStatus = new JLabel("Retrieving...");
            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusUnknownIcon()));
            translationStatus.setToolTipText("Connecting to weblate...");
            labelPane.add(translationStatus, JideBoxLayout.VARY);
            new Thread() {
                public void run() {
                    WeblateIntegration.WeblateTranslationUnit unit = WeblateIntegration.getTranslationUnit(initialValue);
                    switch (unit.status) {
                        case absent:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusRedIcon()));
                            translationStatus.setToolTipText("This string isn't managed by weblate (yet).");
                            break;
                        case done:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusGreenIcon()));
                            translationStatus.setToolTipText("This string is translated on weblate.");
                            break;
                        case fuzzy:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusOrangeIcon()));
                            translationStatus.setToolTipText("This string is translated on weblate, but needs a review.");
                            break;
                        case notTranslated:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusRedIcon()));
                            translationStatus.setToolTipText("This string isn't translated in your language on weblate yet.");
                            break;
                        case warning:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusOrangeIcon()));
                            translationStatus.setToolTipText("This string is translated on weblate, but triggered some weblate checks.");
                            break;
                        case error:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusRedIcon()));
                            translationStatus.setToolTipText("Cannot connect to weblate. Check internet connection and firewall settings.");
                            break;
                        case notAllowed:
                            translationStatus.setIcon(new ImageIcon(DefaultIcons.getStatusBlueIcon()));
                            translationStatus.setToolTipText("You have not allowed ATCS to access to internet. You can change this in the workspace settings.");
                            break;
                    }
                    translationStatus.setText(unit.translatedText);
                }

            }.start();
            pane.add(labelPane, JideBoxLayout.FIX);
            tfComponent.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {
                    translateLinkLabel.setText(getWeblateLabelLink(tfComponent.getText().replaceAll("\n", Matcher.quoteReplacement("\n"))));
                    translateLinkLabel.revalidate();
                    translateLinkLabel.repaint();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    translateLinkLabel.setText(getWeblateLabelLink(tfComponent.getText().replaceAll("\n", Matcher.quoteReplacement("\n"))));
                    translateLinkLabel.revalidate();
                    translateLinkLabel.repaint();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    translateLinkLabel.setText(getWeblateLabelLink(tfComponent.getText().replaceAll("\n", Matcher.quoteReplacement("\n"))));
                    translateLinkLabel.revalidate();
                    translateLinkLabel.repaint();
                }
            });
            translateLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            translateLinkLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        try {
                            Desktop.getDesktop().browse(new URI(WeblateIntegration.getWeblateLabelURI(tfComponent.getText().replaceAll("\n", Matcher.quoteReplacement("\n")))));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public static JTextField addTranslatableTextField(JPanel pane, String label, String initialValue, boolean editable, final FieldUpdateListener listener) {
        final JTextField tfField = addTextField(pane, label, initialValue, editable, listener);
        addTranslationPane(pane, tfField, initialValue);
        return tfField;
    }

    public static String getWeblateLabelLink(String text) {
        return "<html><a href=\"" + WeblateIntegration.getWeblateLabelURI(text) + "\">Translate on weblate</a></html>";
    }

    public static JTextField addTextField(JPanel pane, String label, String initialValue, boolean editable, final FieldUpdateListener listener) {
        final JTextField tfField = new JTextField(initialValue);
        addTextComponent(pane, label, editable, listener, tfField, false, false);
        tfField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(tfField, tfField.getText());
            }
        });
        return tfField;
    }

    public static <T extends JTextComponent> T addTextComponent(JPanel pane, String label, boolean editable, final FieldUpdateListener listener, T tfField, boolean specialNewLinesHandling, boolean scrollable) {
        JPanel tfPane = new JPanel();
        tfPane.setLayout(new JideBoxLayout(tfPane, JideBoxLayout.LINE_AXIS, 6));
        JLabel tfLabel = new JLabel(label);
        tfPane.add(tfLabel, JideBoxLayout.FIX);
        tfField.setEditable(editable);
        JComponent component;
        if (scrollable) {
            component = new JScrollPane(tfField);
        } else {
            component = tfField;
        }
        tfPane.add(component, JideBoxLayout.VARY);
        JButton nullify = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        tfPane.add(nullify, JideBoxLayout.FIX);
        nullify.setEnabled(editable);
        pane.add(tfPane, JideBoxLayout.FIX);

        nullify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfField.setText("");
                listener.valueChanged(tfField, null);
            }
        });
        tfField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = tfField.getText();
                if (specialNewLinesHandling) text = text.replaceAll("\n", Matcher.quoteReplacement("\n"));
                listener.valueChanged(tfField, text);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = tfField.getText();
                if (specialNewLinesHandling) text = text.replaceAll("\n", Matcher.quoteReplacement("\n"));
                listener.valueChanged(tfField, text);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                String text = tfField.getText();
                if (specialNewLinesHandling) text = text.replaceAll("\n", Matcher.quoteReplacement("\n"));
                listener.valueChanged(tfField, text);
            }
        });
        return tfField;
    }


    public static JTextArea addTranslatableTextArea(JPanel pane, String label, String initialValue, boolean editable, final FieldUpdateListener listener) {
        final JTextArea tfArea = addTextArea(pane, label, initialValue, editable, listener);
        addTranslationPane(pane, tfArea, initialValue);
        return tfArea;
    }

    public static JTextArea addTextArea(JPanel pane, String label, String initialValue, boolean editable, final FieldUpdateListener listener) {
        String text = initialValue == null ? "" : initialValue.replaceAll("\\n", "\n");
        final JTextArea tfArea = new JTextArea(text);
        tfArea.setRows(2);
        tfArea.setLineWrap(true);
        tfArea.setWrapStyleWord(true);

        addTextComponent(pane, label, editable, listener, tfArea, true, true);
        return tfArea;
    }

//	public static JSpinner addIntegerField(JPanel pane, String label, Integer initialValue, boolean allowNegatives, boolean editable) {
//		return addIntegerField(pane, label, initialValue, allowNegatives, editable, nullListener);
//	}

    public static JSpinner addIntegerField(JPanel pane, String label, Integer initialValue, boolean allowNegatives, boolean editable, final FieldUpdateListener listener) {
        return addIntegerField(pane, label, initialValue, 0, allowNegatives, editable, listener);
    }

    public static <T extends Number & Comparable<T>> JSpinner addNumberField(JPanel pane, String label, boolean editable, final FieldUpdateListener listener, T minimum, T maximum, Number stepSize, T value, T defaultValue) {
        JPanel tfPane = new JPanel();
        tfPane.setLayout(new JideBoxLayout(tfPane, JideBoxLayout.LINE_AXIS, 6));
        JLabel tfLabel = new JLabel(label);
        tfPane.add(tfLabel, JideBoxLayout.FIX);
        if (!(((minimum == null) || (minimum.compareTo(value) <= 0)) &&
                ((maximum == null) || (maximum.compareTo(value) >= 0)))) {
            System.err.printf("Value for number field outside of range: %s <= %s <= %s is false%n", minimum, value, maximum);
            value = defaultValue;
        }
        final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, minimum, maximum, stepSize));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
        spinner.setEnabled(editable);
        ((DefaultFormatter) ((NumberEditor) spinner.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
        tfPane.add(spinner, JideBoxLayout.VARY);
        JButton nullify = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        tfPane.add(nullify, JideBoxLayout.FIX);
        nullify.setEnabled(editable);
        pane.add(tfPane, JideBoxLayout.FIX);
        spinner.addChangeListener(e -> listener.valueChanged(spinner, spinner.getValue()));
        nullify.addActionListener(e -> {
            spinner.setValue(0);
            listener.valueChanged(spinner, null);
        });
        return spinner;
    }

    public static JSpinner addIntegerField(JPanel pane, String label, Integer initialValue, Integer defaultValue, boolean allowNegatives, boolean editable, final FieldUpdateListener listener) {
        int value = initialValue != null ? initialValue : defaultValue;
        int minimum = allowNegatives ? Integer.MIN_VALUE : 0;
        int maximum = Integer.MAX_VALUE;
        return addNumberField(pane, label, editable, listener, minimum, maximum, 1, value, defaultValue);
    }


    private static final String percent = "%";
    private static final String ratio = "x/y";

    public static JComponent addChanceField(JPanel pane, String label, String initialValue, String defaultValue, boolean editable, final FieldUpdateListener listener) {
        int defaultChance = 1;
        int defaultMaxChance = 100;
        if (defaultValue != null) {
            if (defaultValue.contains("/")) {
                int c = defaultValue.indexOf('/');
                try {
                    defaultChance = Integer.parseInt(defaultValue.substring(0, c));
                } catch (NumberFormatException nfe) {
                }
                try {
                    defaultMaxChance = Integer.parseInt(defaultValue.substring(c + 1));
                } catch (NumberFormatException nfe) {
                }
            } else {
                try {
                    defaultChance = Integer.parseInt(defaultValue);
                } catch (NumberFormatException nfe) {
                }
            }
        }

        boolean currentFormIsRatio = true;
        int chance = defaultChance;
        int maxChance = defaultMaxChance;
        if (initialValue != null) {
            if (initialValue.contains("/")) {
                int c = initialValue.indexOf('/');
                try {
                    chance = Integer.parseInt(initialValue.substring(0, c));
                } catch (NumberFormatException nfe) {
                }
                try {
                    maxChance = Integer.parseInt(initialValue.substring(c + 1));
                } catch (NumberFormatException nfe) {
                }
            } else {
                try {
                    chance = Integer.parseInt(initialValue);
                    currentFormIsRatio = false;
                } catch (NumberFormatException nfe) {
                }
            }
        }

        final JPanel tfPane = new JPanel();
        tfPane.setLayout(new JideBoxLayout(tfPane, JideBoxLayout.LINE_AXIS, 6));
        JLabel tfLabel = new JLabel(label);
        tfPane.add(tfLabel, JideBoxLayout.FIX);

        final JComboBox<String> entryTypeBox = new JComboBox<String>(new String[]{percent, ratio});
        if (currentFormIsRatio) {
            entryTypeBox.setSelectedItem(ratio);
        } else {
            entryTypeBox.setSelectedItem(percent);
        }
        entryTypeBox.setEnabled(editable);
        tfPane.add(entryTypeBox, JideBoxLayout.FIX);
        /////////////////////////////////////////////////////////////////////////////////////////////////// make sure "chance" is between 1 and 100. If lower than 1 get 1. If higher than 100, get chance/maxChance * 100... Then do the same with defaultChance, in case no value exist.
        final SpinnerNumberModel percentModel = new SpinnerNumberModel(
                initialValue != null ? ((chance > 1 ? chance : 1) < 100 ? chance : (chance * 100 / maxChance)) : ((defaultChance > 1 ? defaultChance : 1) < 100 ? defaultChance : (defaultChance * 100 /
                        defaultMaxChance)),
                1, 100, 1);
        final SpinnerNumberModel ratioChanceModel = new SpinnerNumberModel(initialValue != null ? chance : defaultChance, 1, Integer.MAX_VALUE, 1);

        final JSpinner chanceSpinner = new JSpinner(currentFormIsRatio ? ratioChanceModel : percentModel);
        if (!currentFormIsRatio)
            ((JSpinner.DefaultEditor) chanceSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
        chanceSpinner.setEnabled(editable);
        ((DefaultFormatter) ((NumberEditor) chanceSpinner.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
        tfPane.add(chanceSpinner, JideBoxLayout.FLEXIBLE);

        final JLabel ratioLabel = new JLabel("/");
        tfPane.add(ratioLabel, JideBoxLayout.FIX);

        final JSpinner maxChanceSpinner = new JSpinner(new SpinnerNumberModel(initialValue != null ? maxChance : defaultMaxChance, 1, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) maxChanceSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
        maxChanceSpinner.setEnabled(editable);
        ((DefaultFormatter) ((NumberEditor) maxChanceSpinner.getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);
        tfPane.add(maxChanceSpinner, JideBoxLayout.FLEXIBLE);

        if (!currentFormIsRatio) {
            ratioLabel.setVisible(false);
            maxChanceSpinner.setVisible(false);
            tfPane.revalidate();
            tfPane.repaint();
        }

        final JButton nullify = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        tfPane.add(nullify, JideBoxLayout.FIX);
        nullify.setEnabled(editable);
        pane.add(tfPane, JideBoxLayout.FIX);

        entryTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (entryTypeBox.getSelectedItem() == percent) {
                    int chance = ((Integer) chanceSpinner.getValue());
                    int maxChance = ((Integer) maxChanceSpinner.getValue());
                    chance *= 100;
                    chance /= maxChance;
                    chance = Math.max(0, Math.min(100, chance));
                    chanceSpinner.setModel(percentModel);
                    chanceSpinner.setValue(chance);
                    ((JSpinner.DefaultEditor) chanceSpinner.getEditor()).getTextField().setHorizontalAlignment(JTextField.LEFT);
                    ratioLabel.setVisible(false);
                    maxChanceSpinner.setVisible(false);
                    tfPane.revalidate();
                    tfPane.repaint();
                    listener.valueChanged(chanceSpinner, chanceSpinner.getValue().toString());
                } else if (entryTypeBox.getSelectedItem() == ratio) {
                    int chance = ((Integer) chanceSpinner.getValue());
                    chanceSpinner.setModel(ratioChanceModel);
                    chanceSpinner.setValue(chance);
                    maxChanceSpinner.setValue(100);
                    ratioLabel.setVisible(true);
                    maxChanceSpinner.setVisible(true);
                    tfPane.revalidate();
                    tfPane.repaint();
                    listener.valueChanged(chanceSpinner, chanceSpinner.getValue().toString() + "/" + maxChanceSpinner.getValue().toString());
                }
            }
        });
        chanceSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (entryTypeBox.getSelectedItem() == percent) {
                    listener.valueChanged(chanceSpinner, chanceSpinner.getValue().toString());
                } else if (entryTypeBox.getSelectedItem() == ratio) {
                    listener.valueChanged(chanceSpinner, chanceSpinner.getValue().toString() + "/" + maxChanceSpinner.getValue().toString());
                }
            }
        });
        maxChanceSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                listener.valueChanged(chanceSpinner, chanceSpinner.getValue().toString() + "/" + maxChanceSpinner.getValue().toString());
            }
        });
        nullify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chanceSpinner.setValue(1);
                listener.valueChanged(chanceSpinner, null);
            }
        });
        return chanceSpinner;
    }

//	public static JSpinner addDoubleField(JPanel pane, String label, Double initialValue, boolean editable) {
//		return addDoubleField(pane, label, initialValue, editable, nullListener);
//	}

    public static JSpinner addDoubleField(JPanel pane, String label, Double initialValue, boolean editable, final FieldUpdateListener listener) {
        double minimum = 0.0d;
        double defaultValue = 0.0d;
        double value = initialValue != null ? initialValue : minimum;
        double maximum = Float.valueOf(Float.MAX_VALUE).doubleValue();
        return addNumberField(pane, label, editable, listener, minimum, maximum, 1.0d, value, defaultValue);
    }

    public static IntegerBasedCheckBox addIntegerBasedCheckBox(JPanel pane, String label, Integer initialValue, boolean editable) {
        return addIntegerBasedCheckBox(pane, label, initialValue, editable, nullListener);
    }

    public static IntegerBasedCheckBox addIntegerBasedCheckBox(JPanel pane, String label, Integer initialValue, boolean editable, final FieldUpdateListener listener) {
        JPanel ibcbPane = new JPanel();
        ibcbPane.setLayout(new BorderLayout());
        final IntegerBasedCheckBox ibcb = new IntegerBasedCheckBox();
        ibcb.setText(label);
        ibcb.setIntegerValue(initialValue);
        ibcb.setEnabled(editable);
        ibcbPane.add(ibcb, BorderLayout.WEST);
        ibcbPane.add(new JPanel(), BorderLayout.CENTER);
        pane.add(ibcbPane, JideBoxLayout.FIX);
        ibcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(ibcb, ibcb.getIntegerValue());
            }
        });
        return ibcb;
    }

    public static BooleanBasedCheckBox addBooleanBasedCheckBox(JPanel pane, String label, Boolean initialValue, boolean editable, final FieldUpdateListener listener) {
        JPanel bbcbPane = new JPanel();
        bbcbPane.setLayout(new BorderLayout());
        final BooleanBasedCheckBox bbcb = new BooleanBasedCheckBox();
        bbcb.setText(label);
        bbcb.setBooleanValue(initialValue);
        bbcb.setEnabled(editable);
        bbcbPane.add(bbcb, BorderLayout.WEST);
        bbcbPane.add(new JPanel(), BorderLayout.CENTER);
        pane.add(bbcbPane, JideBoxLayout.FIX);
        bbcb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(bbcb, bbcb.isSelected());
            }
        });
        return bbcb;
    }

    @SuppressWarnings("rawtypes")
    public static JComboBox addEnumValueBox(JPanel pane, String label, Enum[] values, Enum initialValue, boolean writable) {
        return addEnumValueBox(pane, label, values, initialValue, writable, new FieldUpdateListener() {
            @Override
            public void valueChanged(JComponent source, Object value) {
            }
        });
    }

    @SuppressWarnings("rawtypes")
    public static JComboBox addEnumValueBox(JPanel pane, String label, Enum[] values, Enum initialValue, boolean writable, final FieldUpdateListener listener) {
        JPanel comboPane = new JPanel();
        comboPane.setLayout(new JideBoxLayout(comboPane, JideBoxLayout.LINE_AXIS, 6));
        JLabel comboLabel = new JLabel(label);
        comboPane.add(comboLabel, JideBoxLayout.FIX);
        @SuppressWarnings("unchecked") final JComboBox enumValuesCombo = new JComboBox(values);
        enumValuesCombo.setEnabled(writable);
        enumValuesCombo.setSelectedItem(initialValue);
        comboPane.add(enumValuesCombo, JideBoxLayout.VARY);
        enumValuesCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    listener.valueChanged(enumValuesCombo, e.getItem());
                }
            }
        });
        JButton nullify = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        comboPane.add(nullify, JideBoxLayout.FIX);
        nullify.setEnabled(writable);
        nullify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enumValuesCombo.setSelectedItem(null);
                listener.valueChanged(enumValuesCombo, null);
            }
        });

        pane.add(comboPane, JideBoxLayout.FIX);
        return enumValuesCombo;
    }


    public MyComboBox addNPCBox(JPanel pane, Project proj, String label, NPC npc, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<NPC> comboModel = new GDEComboModel<NPC>(proj, npc) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public NPC getTypedElementAt(int index) {
                return project.getNPC(index);
            }

            @Override
            public int getSize() {
                return project.getNPCCount() + 1;
            }
        };
        return addGDEBox(pane, label, npc, NPC.class, comboModel, writable, listener);
    }

    public MyComboBox addActorConditionBox(JPanel pane, Project proj, String label, ActorCondition acond, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<ActorCondition> comboModel = new GDEComboModel<ActorCondition>(proj, acond) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public ActorCondition getTypedElementAt(int index) {
                return project.getActorCondition(index);
            }

            @Override
            public int getSize() {
                return project.getActorConditionCount() + 1;
            }
        };
        return addGDEBox(pane, label, acond, ActorCondition.class, comboModel, writable, listener);
    }

    public MyComboBox addItemBox(JPanel pane, Project proj, String label, Item item, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<Item> comboModel = new GDEComboModel<Item>(proj, item) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public Item getTypedElementAt(int index) {
                return project.getItem(index);
            }

            @Override
            public int getSize() {
                return project.getItemCount() + 1;
            }
        };
        return addGDEBox(pane, label, item, Item.class, comboModel, writable, listener);
    }

    public MyComboBox addItemCategoryBox(JPanel pane, Project proj, String label, ItemCategory ic, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<ItemCategory> comboModel = new GDEComboModel<ItemCategory>(proj, ic) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public ItemCategory getTypedElementAt(int index) {
                return project.getItemCategory(index);
            }

            @Override
            public int getSize() {
                return project.getItemCategoryCount() + 1;
            }
        };
        return addGDEBox(pane, label, ic, ItemCategory.class, comboModel, writable, listener);
    }

    public MyComboBox addQuestBox(JPanel pane, Project proj, String label, Quest quest, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<Quest> comboModel = new GDEComboModel<Quest>(proj, quest) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public Quest getTypedElementAt(int index) {
                return project.getQuest(index);
            }

            @Override
            public int getSize() {
                return project.getQuestCount() + 1;
            }
        };
        return addGDEBox(pane, label, quest, Quest.class, comboModel, writable, listener);
    }

    public MyComboBox addDroplistBox(JPanel pane, Project proj, String label, Droplist droplist, boolean writable, FieldUpdateListener listener) {
        final GDEComboModel<Droplist> comboModel = new GDEComboModel<Droplist>(proj, droplist) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public Droplist getTypedElementAt(int index) {
                return project.getDroplist(index);
            }

            @Override
            public int getSize() {
                return project.getDroplistCount() + 1;
            }
        };
        return addGDEBox(pane, label, droplist, Droplist.class, comboModel, writable, listener);
    }

    public MyComboBox addDialogueBox(JPanel pane, Project proj, String label, Dialogue dialogue, boolean writable, final FieldUpdateListener listener) {
        final GDEComboModel<Dialogue> comboModel = new GDEComboModel<Dialogue>(proj, dialogue) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public Dialogue getTypedElementAt(int index) {
                return project.getDialogue(index);
            }

            @Override
            public int getSize() {
                return project.getDialogueCount() + 1;
            }
        };
        return addGDEBox(pane, label, dialogue, Dialogue.class, comboModel, writable, listener);
    }

    public MyComboBox addMapBox(JPanel pane, Project proj, String label, TMXMap map, boolean writable, final FieldUpdateListener listener) {
        final GDEComboModel<TMXMap> comboModel = new GDEComboModel<TMXMap>(proj, map) {
            private static final long serialVersionUID = 2638082961277241764L;

            @Override
            public TMXMap getTypedElementAt(int index) {
                return project.getMap(index);
            }

            @Override
            public int getSize() {
                return project.getMapCount() + 1;
            }
        };
        return addGDEBox(pane, label, map, TMXMap.class, comboModel, writable, listener);
    }

    @SuppressWarnings("unchecked")
    public MyComboBox addGDEBox(JPanel pane, String label, GameDataElement gde, final Class<? extends GameDataElement> dataClass, final GDEComboModel<? extends GameDataElement> comboModel, final boolean writable, final FieldUpdateListener listener) {
        JPanel gdePane = new JPanel();
        gdePane.setLayout(new JideBoxLayout(gdePane, JideBoxLayout.LINE_AXIS, 6));
        JLabel gdeLabel = new JLabel(label);
        gdePane.add(gdeLabel, JideBoxLayout.FIX);
        final MyComboBox gdeBox = new MyComboBox(dataClass, comboModel);
        gdeBox.setRenderer(new GDERenderer(false, writable));
        new ComboBoxSearchable(gdeBox) {
            @Override
            protected String convertElementToString(Object object) {
                if (object == null) return "none";
                else return ((GameDataElement) object).getDesc();
            }
        };
        gdeBox.setEnabled(writable);
        gdePane.add(gdeBox, JideBoxLayout.VARY);
        final JButton goToGde = new JButton((Icon) ((gde != null) ? new ImageIcon(gde.getIcon()) : (writable ? new ImageIcon(DefaultIcons.getCreateIcon()) : null)));
        goToGde.setEnabled(gde != null || writable);
        goToGde.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameDataElement selected = ((GameDataElement) comboModel.getSelectedItem());
                if (selected != null) {
                    ATContentStudio.frame.openEditor(((GameDataElement) comboModel.getSelectedItem()));
                    ATContentStudio.frame.selectInTree((GameDataElement) comboModel.getSelectedItem());
                } else if (writable) {
                    JSONCreationWizard wizard = new JSONCreationWizard(((GameDataElement) target).getProject(), dataClass);
                    wizard.addCreationListener(new JSONCreationWizard.CreationCompletedListener() {

                        @Override
                        public void elementCreated(JSONElement created) {
                            gdeBox.setSelectedItem(created);
                        }
                    });
                    wizard.setVisible(true);
                }
            }
        });
        gdePane.add(goToGde, JideBoxLayout.FIX);
        gdeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gdeBox.getModel().getSelectedItem() == null) {
                    goToGde.setIcon((writable ? new ImageIcon(DefaultIcons.getCreateIcon()) : null));
                    goToGde.setEnabled(writable);
                } else {
                    goToGde.setIcon(new ImageIcon(((GameDataElement) comboModel.getSelectedItem()).getIcon()));
                    goToGde.setEnabled(true);
                }
                listener.valueChanged(gdeBox, gdeBox.getModel().getSelectedItem());
            }
        });
        JButton nullify = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        gdePane.add(nullify, JideBoxLayout.FIX);
        nullify.setEnabled(writable);
        nullify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gdeBox.setSelectedItem(null);
            }
        });
        pane.add(gdePane, JideBoxLayout.FIX);

        return gdeBox;
    }

    public JComboBox<QuestStage> addQuestStageBox(JPanel pane, Project proj, String label, Integer initialValue, boolean writable, final FieldUpdateListener listener, Quest quest, @SuppressWarnings("rawtypes") final JComboBox questSelectionBox) {
        JPanel gdePane = new JPanel();
        gdePane.setLayout(new JideBoxLayout(gdePane, JideBoxLayout.LINE_AXIS, 6));
        JLabel gdeLabel = new JLabel(label);
        gdePane.add(gdeLabel, JideBoxLayout.FIX);

        QuestStage initial = null;
        if (quest != null) {
            initial = quest.getStage(initialValue);
        }
        final QuestStageComboModel comboModel = new QuestStageComboModel(proj, initial, quest);
        final JComboBox<QuestStage> combo = new JComboBox<QuestStage>(comboModel);
        combo.setRenderer(new GDERenderer(false, writable));
        new ComboBoxSearchable(combo) {
            @Override
            protected String convertElementToString(Object object) {
                if (object == null) return "none";
                else return ((GameDataElement) object).getDesc();
            }
        };
        questSelectionBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (comboModel.selected != null) {
                    Editor.this.target.removeBacklink(comboModel.selected);
                }
                Quest newQuest = (Quest) questSelectionBox.getSelectedItem();
                comboModel.changeQuest(newQuest);
                combo.revalidate();
            }
        });
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(combo, comboModel.selected == null ? null : comboModel.selected.progress);
            }
        });


        combo.setEnabled(writable);
        gdePane.add(combo, JideBoxLayout.VARY);

        pane.add(gdePane, JideBoxLayout.FIX);

        return combo;
    }


    @SuppressWarnings({"rawtypes"})
    public JList addBacklinksList(JPanel pane, GameDataElement gde) {
        return addBacklinksList(pane, gde, "Elements linking to this one");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public JList addBacklinksList(JPanel pane, GameDataElement gde, String title) {
        final JList list = new JList(new GDEBacklinksListModel(gde));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ATContentStudio.frame.openEditor((GameDataElement) list.getSelectedValue());
                    ATContentStudio.frame.selectInTree((GameDataElement) list.getSelectedValue());
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ATContentStudio.frame.openEditor((GameDataElement) list.getSelectedValue());
                    ATContentStudio.frame.selectInTree((GameDataElement) list.getSelectedValue());
                }
            }
        });
        list.setCellRenderer(new GDERenderer(true, false));
        CollapsiblePanel colPane = new CollapsiblePanel(title);
        colPane.setLayout(new JideBoxLayout(colPane, JideBoxLayout.PAGE_AXIS));
        colPane.add(new JScrollPane(list), JideBoxLayout.FIX);
        colPane.add(new JPanel(), JideBoxLayout.FIX);
        if (gde.getBacklinks() == null || gde.getBacklinks().isEmpty()) {
            colPane.collapse();
        }
        pane.add(colPane, JideBoxLayout.FIX);
        return list;
    }

    public static abstract class GDEComboModel<E extends GameDataElement> extends AbstractListModel<E> implements ComboBoxModel<E> {

        private static final long serialVersionUID = -5854574666510314715L;

        public Project project;
        public E selected;

        public GDEComboModel(Project proj, E initial) {
            this.project = proj;
            this.selected = initial;
        }

        @Override
        public abstract int getSize();

        @Override
        public E getElementAt(int index) {
            if (index == 0) {
                return null;
            }
            return getTypedElementAt(index - 1);
        }

        public abstract E getTypedElementAt(int index);

        @SuppressWarnings("unchecked")
        @Override
        public void setSelectedItem(Object anItem) {
            selected = (E) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        public void itemAdded(E item, int index) {
            fireIntervalAdded(this, index, index);
        }

        public void itemRemoved(E item, int index) {
            fireIntervalRemoved(this, index, index);
        }

    }

    public static class GDERenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;

        private boolean includeType;
        private boolean writable;

        public GDERenderer(boolean includeType, boolean writable) {
            super();
            this.includeType = includeType;
            this.writable = writable;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("None" + (writable ? ". Click on the button to create one." : ""));
            } else {
                if (includeType && ((GameDataElement) value).getDataType() != null) {
                    if (value instanceof QuestStage) {
                        String text = ((GameDataElement) value).getDesc();
                        if (text.length() > 60) {
                            text = text.substring(0, 57) + "...";
                        }
                        label.setText(((GameDataElement) value).getDataType().toString() + "/" + ((Quest) ((QuestStage) value).parent).id + "#" + ((QuestStage) value).progress + ":" + text);
                    } else {
                        label.setText(((GameDataElement) value).getDataType().toString() + "/" + ((GameDataElement) value).getDesc());
                    }
                } else {
                    if (value instanceof QuestStage) {
                        String text = ((GameDataElement) value).getDesc();
                        if (text.length() > 60) {
                            text = text.substring(0, 57) + "...";
                        }
                        label.setText(text);
                    } else {
                        label.setText(((GameDataElement) value).getDesc());
                    }
                }
                if (((GameDataElement) value).getIcon() == null) {
                    Notification.addError("Unable to find icon for " + ((GameDataElement) value).getDesc());
                } else {
                    label.setIcon(new ImageIcon(((GameDataElement) value).getIcon()));
                }
            }
            return label;
        }

    }

    public static class QuestStageComboModel extends AbstractListModel<QuestStage> implements ComboBoxModel<QuestStage> {

        private static final long serialVersionUID = -5854574666510314715L;

        public Project project;
        public Quest currentQuest;
        public QuestStage selected;

        public QuestStageComboModel(Project proj, QuestStage initial, Quest quest) {
            this.project = proj;
            this.currentQuest = quest;
            this.selected = initial;
        }

        @Override
        public int getSize() {
            if (currentQuest == null) return 1;
            return currentQuest.stages.size() + 1;
        }

        @Override
        public QuestStage getElementAt(int index) {
            if (index == 0) {
                return null;
            }
            return currentQuest.stages.get(index - 1);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = (QuestStage) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        public void itemAdded(QuestStage item, int index) {
            fireIntervalAdded(this, index, index);
        }

        public void itemRemoved(QuestStage item, int index) {
            fireIntervalRemoved(this, index, index);
        }

        public void changeQuest(Quest newQuest) {
            int size = getSize();
            currentQuest = null;
            selected = null;
            fireIntervalRemoved(this, 1, size);
            currentQuest = newQuest;
            fireIntervalAdded(this, 1, getSize());
        }

    }


    public static class GDEBacklinksListModel implements ListenerCollectionModel<GameDataElement> {

        GameDataElement source;

        public GDEBacklinksListModel(GameDataElement source) {
            super();
            this.source = source;
            source.addBacklinkListener(new GameDataElement.BacklinksListener() {
                @Override
                public void backlinkRemoved(GameDataElement gde) {
                    fireListChanged();
                }

                @Override
                public void backlinkAdded(GameDataElement gde) {
                    fireListChanged();
                }
            });
        }

        @Override
        public Collection<GameDataElement> getElements() {
            return source.getBacklinks();
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public class MyComboBox extends JComboBox implements ProjectElementListener {

        private static final long serialVersionUID = -4184228604170642567L;

        Class<? extends GameDataElement> dataType;

        public MyComboBox(Class<? extends GameDataElement> dataType, ComboBoxModel model) {
            super(model);
            this.dataType = dataType;
            Editor.this.addElementListener(dataType, this);
        }

        @Override
        public void elementAdded(GameDataElement added, int index) {
            ((GDEComboModel) getModel()).itemAdded(added, index);
        }

        @Override
        public void elementRemoved(GameDataElement removed, int index) {
            ((GDEComboModel) getModel()).itemRemoved(removed, index);
        }

        @Override
        public Class<? extends GameDataElement> getDataType() {
            return dataType;
        }

    }

    public abstract void targetUpdated();


    transient Map<Class<? extends GameDataElement>, List<ProjectElementListener>> projectElementListeners = new HashMap<Class<? extends GameDataElement>, List<ProjectElementListener>>();

    public void addElementListener(Class<? extends GameDataElement> interestingType, ProjectElementListener listener) {
        if (projectElementListeners.get(interestingType) == null) {
            projectElementListeners.put(interestingType, new ArrayList<ProjectElementListener>());
            target.getProject().addElementListener(interestingType, this);
        }
        projectElementListeners.get(interestingType).add(listener);
    }

    public void removeElementListener(ProjectElementListener listener) {
        if (listener == null) return;
        if (projectElementListeners.get(listener.getDataType()) != null) {
            projectElementListeners.get(listener.getDataType()).remove(listener);
            if (projectElementListeners.get(listener.getDataType()).isEmpty()) {
                target.getProject().removeElementListener(listener.getDataType(), this);
                projectElementListeners.remove(listener.getDataType());
            }
        }
    }

    public void elementAdded(GameDataElement element, int index) {
        if (projectElementListeners.get(element.getClass()) != null) {
            for (ProjectElementListener l : projectElementListeners.get(element.getClass())) {
                l.elementAdded(element, index);
            }
        }
    }

    public void elementRemoved(GameDataElement element, int index) {
        if (projectElementListeners.get(element.getClass()) != null) {
            for (ProjectElementListener l : projectElementListeners.get(element.getClass())) {
                l.elementRemoved(element, index);
            }
        }
    }

    public void clearElementListeners() {
        for (Class<? extends GameDataElement> type : projectElementListeners.keySet()) {
            target.getProject().removeElementListener(type, this);
        }
    }

    public Class<? extends GameDataElement> getDataType() {
        return null;
    }


    public <E extends Common.ActorConditionEffect, T extends OrderedListenerListModel<?, E>> void updateConditionEffect(ActorCondition value,
                                                                                                                        GameDataElement backlink,
                                                                                                                        E selectedHitEffectTargetCondition,
                                                                                                                        T hitTargetConditionsModel) {
        if (selectedHitEffectTargetCondition.condition != null) {
            selectedHitEffectTargetCondition.condition.removeBacklink(backlink);
        }
        selectedHitEffectTargetCondition.condition = value;
        if (selectedHitEffectTargetCondition.condition != null) {
            selectedHitEffectTargetCondition.condition_id = selectedHitEffectTargetCondition.condition.id;
            selectedHitEffectTargetCondition.condition.addBacklink(backlink);
        } else {
            selectedHitEffectTargetCondition.condition_id = null;
        }
        hitTargetConditionsModel.itemChanged(selectedHitEffectTargetCondition);
    }

}
