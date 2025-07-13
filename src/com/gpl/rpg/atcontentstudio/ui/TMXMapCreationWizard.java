package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement.State;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TMXMapCreationWizard extends JDialog {

    private static final long serialVersionUID = -474689694453543575L;
    private static final String DEFAULT_TEMPLATE = "template.tmx";


    private TMXMap creation = null;
    final File templateFile;

    final JLabel message;
    final JRadioButton useTemplate, copyMap;
    final JComboBox<TMXMap> templateCombo;
    final JTextField idField;
    final JButton ok;
    final Project proj;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TMXMapCreationWizard(final Project proj) {
        super(ATContentStudio.frame);
        this.proj = proj;
        templateFile = new File(proj.baseContent.gameMaps.mapFolder, DEFAULT_TEMPLATE);

        setTitle("Create new TMX map");

        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel("Create a new TMX map."), JideBoxLayout.FIX);

        message = new JLabel("Enter new map name:");
        pane.add(message, JideBoxLayout.FIX);

        final JPanel idPane = new JPanel();
        idPane.setLayout(new BorderLayout());
        JLabel idLabel = new JLabel("Internal ID: ");
        idPane.add(idLabel, BorderLayout.WEST);
        idField = new JTextField("");
        idField.setEditable(true);
        idPane.add(idField, BorderLayout.CENTER);
        pane.add(idPane, JideBoxLayout.FIX);

        useTemplate = new JRadioButton("Use default template file (" + DEFAULT_TEMPLATE + ")");
        useTemplate.setToolTipText(templateFile.getAbsolutePath());
        pane.add(useTemplate, JideBoxLayout.FIX);
        copyMap = new JRadioButton("Copy existing map");
        pane.add(copyMap, JideBoxLayout.FIX);

        ButtonGroup radioGroup = new ButtonGroup();
        radioGroup.add(useTemplate);
        radioGroup.add(copyMap);

        final JPanel templatePane = new JPanel();
        templatePane.setLayout(new BorderLayout());
        JLabel templateLabel = new JLabel("Template to copy: ");
        templatePane.add(templateLabel, BorderLayout.WEST);
        templateCombo = new JComboBox(new TemplateComboModel());
        templateCombo.setRenderer(new TemplateComboCellRenderer());
        if (proj.getMap(DEFAULT_TEMPLATE) != null) templateCombo.setSelectedItem(proj.getMap(DEFAULT_TEMPLATE));
        templatePane.add(templateCombo, BorderLayout.CENTER);
        pane.add(templatePane, JideBoxLayout.FIX);
        pane.add(templateCombo);

        if (templateFile.exists()) {
            useTemplate.setSelected(true);
            copyMap.setSelected(false);
            templateCombo.setEnabled(false);
        } else {
            useTemplate.setSelected(false);
            useTemplate.setEnabled(false);
            useTemplate.setToolTipText("Cannot find file " + templateFile.getAbsolutePath());
            templateCombo.setEnabled(true);
            copyMap.setSelected(true);
        }

        ActionListener radioListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (useTemplate.isSelected()) {
                    templateCombo.setEnabled(false);
                } else if (copyMap.isSelected()) {
                    templateCombo.setEnabled(true);
                }
                updateStatus();
            }
        };
        useTemplate.addActionListener(radioListener);
        copyMap.addActionListener(radioListener);

        templateCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatus();
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

                if (copyMap.isSelected()) {
                    creation = ((TMXMap) templateCombo.getSelectedItem()).clone();
                } else if (useTemplate.isSelected()) {
                    creation = new TMXMap(proj.createdContent.gameMaps, templateFile);
                    creation.parse();
                }
                creation.id = idField.getText();
                creation.tmxFile = new File(creation.id + ".tmx");
                TMXMapCreationWizard.this.setVisible(false);
                TMXMapCreationWizard.this.dispose();
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
                TMXMapCreationWizard.this.setVisible(false);
                TMXMapCreationWizard.this.dispose();
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

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(350, 250));
        updateStatus();
        pack();

        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
    }

    public void updateStatus() {
        boolean trouble = false;
        message.setText("<html><font color=\"#00AA00\">Looks OK to me.</font></html>");
        if (copyMap.isSelected() && templateCombo.getSelectedItem() == null) {
            message.setText("<html><font color=\"#FF0000\">Select a map template below:</font></html>");
            trouble = true;
        } else if (idField.getText() == null || idField.getText().length() <= 0) {
            message.setText("<html><font color=\"#FF0000\">Internal ID must not be empty.</font></html>");
            trouble = true;
        } else if (proj.getMap(idField.getText()) != null) {
            if (proj.getMap(idField.getText()).getDataType() == GameSource.Type.created) {
                message.setText("<html><font color=\"#FF0000\">A map with the same ID was already created in this project.</font></html>");
                trouble = true;
            } else if (proj.getMap(idField.getText()).getDataType() == GameSource.Type.altered) {
                message.setText("<html><font color=\"#FF0000\">A map with the same ID exists in the game and is already altered in this project.</font></html>");
                trouble = true;
            } else if (proj.getMap(idField.getText()).getDataType() == GameSource.Type.source) {
                message.setText("<html><font color=\"#FF9000\">A map with the same ID exists in the game. The new one will be added under \"altered\".</font></html>");
            }
        }

        ok.setEnabled(!trouble);

        message.revalidate();
        message.repaint();
    }

    public static interface CreationCompletedListener {
        public void mapCreated(TMXMap created);
    }

    private List<CreationCompletedListener> listeners = new CopyOnWriteArrayList<TMXMapCreationWizard.CreationCompletedListener>();

    public void addCreationListener(CreationCompletedListener l) {
        listeners.add(l);
    }

    public void notifyCreated() {
        for (CreationCompletedListener l : listeners) {
            l.mapCreated(creation);
        }
    }

    class TemplateComboModel implements ComboBoxModel<TMXMap> {

        Object selected;

        @Override
        public int getSize() {
            return proj.getMapCount();
        }

        @Override
        public TMXMap getElementAt(int index) {
            return proj.getMap(index);
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
            selected = anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

    }

    class TemplateComboCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 5621373849299980998L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel && value != null) {
                ((JLabel) c).setText(((TMXMap) value).getDesc());
                ((JLabel) c).setIcon(new ImageIcon(DefaultIcons.getTiledIconIcon()));
            }
            return c;
        }
    }


}
