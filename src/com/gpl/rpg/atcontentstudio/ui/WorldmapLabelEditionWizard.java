package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorldmapLabelEditionWizard extends JDialog {

    private static final long serialVersionUID = 4911946705579386332L;

    final JLabel message;
    final JTextField idField;
    final JTextField labelField;
    final JTextField typeField;
    final JButton ok;
    final WorldmapSegment segment;
    final WorldmapSegment.NamedArea label;

    boolean createMode;

    public WorldmapLabelEditionWizard(WorldmapSegment segment) {
        this(segment, new WorldmapSegment.NamedArea(null, null, null), true);
    }

    public WorldmapLabelEditionWizard(WorldmapSegment segment, WorldmapSegment.NamedArea label) {
        this(segment, label, false);
    }

    public WorldmapLabelEditionWizard(WorldmapSegment segment, WorldmapSegment.NamedArea namedArea, boolean createMode) {
        super(ATContentStudio.frame);
        this.createMode = createMode;
        this.segment = segment;
        this.label = namedArea;

        setTitle(createMode ? "Create Worldmap Label" : "Edit Worldmap Label");

        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel(createMode ? "Create a worldmap label." : "Edit a worldmap label."), JideBoxLayout.FIX);

        message = new JLabel("Enter a label ID below: ");
        pane.add(message, JideBoxLayout.FIX);

        final JPanel idPane = new JPanel();
        idPane.setLayout(new BorderLayout());
        JLabel idLabel = new JLabel("Internal ID: ");
        idPane.add(idLabel, BorderLayout.WEST);
        idField = new JTextField(label.id);
        idField.setEditable(true);
        idPane.add(idField, BorderLayout.CENTER);
        pane.add(idPane, JideBoxLayout.FIX);

        final JPanel labelPane = new JPanel();
        labelPane.setLayout(new BorderLayout());
        JLabel labelLabel = new JLabel("Label: ");
        labelPane.add(labelLabel, BorderLayout.WEST);
        labelField = new JTextField(label.name);
        labelField.setEditable(true);
        labelPane.add(labelField, BorderLayout.CENTER);
        pane.add(labelPane, JideBoxLayout.FIX);

        final JPanel typePane = new JPanel();
        typePane.setLayout(new BorderLayout());
        JLabel typeLabel = new JLabel("Type: ");
        typePane.add(typeLabel, BorderLayout.WEST);
        typeField = new JTextField(label.type);
        if (typeField.getText().equals("")) {
            typeField.setText("settlement");
        }
        typeField.setEditable(true);
        typePane.add(typeField, BorderLayout.CENTER);
        pane.add(typePane, JideBoxLayout.FIX);

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
                label.id = idField.getText();
                label.name = labelField.getText();
                label.type = labelField.getText();
                WorldmapLabelEditionWizard.this.setVisible(false);
                WorldmapLabelEditionWizard.this.dispose();
                if (WorldmapLabelEditionWizard.this.createMode) {
                    WorldmapLabelEditionWizard.this.segment.labels.put(label.id, label);
                }
                notifyCreated();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorldmapLabelEditionWizard.this.setVisible(false);
                WorldmapLabelEditionWizard.this.dispose();
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
        labelField.getDocument().addDocumentListener(statusUpdater);
        typeField.getDocument().addDocumentListener(statusUpdater);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(350, 170));
        updateStatus();
        pack();

        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
    }

    public void updateStatus() {
        boolean trouble = false;
        message.setText("<html><font color=\"#00AA00\">Looks OK to me.</font></html>");
        if (idField.getText() == null || idField.getText().length() <= 0) {
            message.setText("<html><font color=\"#FF0000\">Internal ID must not be empty.</font></html>");
            trouble = true;
        } else if (segment.labels.get(idField.getText()) != null && segment.labels.get(idField.getText()) != label) {
            message.setText("<html><font color=\"#FF0000\">A worldmap label with the same ID already exists in this worldmap.</font></html>");
            trouble = true;
        } else if (labelField.getText() == null || labelField.getText().length() <= 0) {
            message.setText("<html><font color=\"#FF0000\">Label must not be empty.</font></html>");
            trouble = true;
        }
//		message.setText("<html><font color=\"#FF9000\">This is a Warning example</font></html>");

        ok.setEnabled(!trouble);

        message.revalidate();
        message.repaint();
    }

    public static interface CreationCompletedListener {
        public void labelCreated(WorldmapSegment.NamedArea created);
    }

    private List<CreationCompletedListener> listeners = new CopyOnWriteArrayList<WorldmapLabelEditionWizard.CreationCompletedListener>();

    public void addCreationListener(CreationCompletedListener l) {
        listeners.add(l);
    }

    public void notifyCreated() {
        for (CreationCompletedListener l : listeners) {
            l.labelCreated(label);
        }
    }
}
