package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
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

public class WorldmapCreationWizard extends JDialog {

    private static final long serialVersionUID = 6491044105090917567L;

    private WorldmapSegment creation = new WorldmapSegment(null, null, null);

    final JLabel message;
    final JTextField idField;
    final JButton ok;
    final Project proj;


    public WorldmapCreationWizard(final Project proj) {
        super(ATContentStudio.frame);
        this.proj = proj;
        setTitle("Create Worldmap segment");

        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel("Create a new worldmap segment."), JideBoxLayout.FIX);

        message = new JLabel("Enter a map segment ID below: ");
        pane.add(message, JideBoxLayout.FIX);

        final JPanel idPane = new JPanel();
        idPane.setLayout(new BorderLayout());
        JLabel idLabel = new JLabel("Internal ID: ");
        idPane.add(idLabel, BorderLayout.WEST);
        idField = new JTextField("");
        idField.setEditable(true);
        idPane.add(idField, BorderLayout.CENTER);
        pane.add(idPane, JideBoxLayout.FIX);

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
                creation.id = idField.getText();
                WorldmapCreationWizard.this.setVisible(false);
                WorldmapCreationWizard.this.dispose();
                proj.createWorldmapSegment(creation);
                notifyCreated();
                ATContentStudio.frame.selectInTree(creation);
                ATContentStudio.frame.openEditor(creation);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                creation = null;
                WorldmapCreationWizard.this.setVisible(false);
                WorldmapCreationWizard.this.dispose();
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

        setMinimumSize(new Dimension(350, 120));
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
        } else if (proj.getWorldmapSegment(idField.getText()) != null) {
            if (proj.getWorldmapSegment(idField.getText()).getDataType() == GameSource.Type.created) {
                message.setText("<html><font color=\"#FF0000\">A worldmap segment with the same ID was already created in this project.</font></html>");
                trouble = true;
            } else if (proj.getWorldmapSegment(idField.getText()).getDataType() == GameSource.Type.altered) {
                message.setText("<html><font color=\"#FF0000\">A worldmap segment with the same ID exists in the game and is already altered in this project.</font></html>");
                trouble = true;
            } else if (proj.getWorldmapSegment(idField.getText()).getDataType() == GameSource.Type.source) {
                message.setText("<html><font color=\"#FF9000\">A worldmap segment with the same ID exists in the game. It will be added under \"altered\".</font></html>");
            }
        }

        ok.setEnabled(!trouble);

        message.revalidate();
        message.repaint();
    }

    public static interface CreationCompletedListener {
        public void segmentCreated(WorldmapSegment created);
    }

    private List<CreationCompletedListener> listeners = new CopyOnWriteArrayList<WorldmapCreationWizard.CreationCompletedListener>();

    public void addCreationListener(CreationCompletedListener l) {
        listeners.add(l);
    }

    public void notifyCreated() {
        for (CreationCompletedListener l : listeners) {
            l.segmentCreated(creation);
        }
    }
}
