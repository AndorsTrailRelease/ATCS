package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement.State;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WriterSketchCreationWizard extends JDialog {

    private static final long serialVersionUID = 175788847797352548L;

    private WriterModeData creation = null;
    final JLabel message;
    final JTextField idField;
    final JButton ok;
    final Project proj;

    public WriterSketchCreationWizard(Project proj) {
        this(proj, null);
    }

    public WriterSketchCreationWizard(Project proj, final Dialogue dialogue) {
        super(ATContentStudio.frame);
        this.proj = proj;

        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        pane.add(new JLabel("Create a new game data element."), JideBoxLayout.FIX);

        message = new JLabel("Select a data type below:");
        pane.add(message, JideBoxLayout.FIX);

        final JPanel idPane = new JPanel();
        idPane.setLayout(new BorderLayout());
        JLabel idLabel = new JLabel("Dialogue ID prefix: ");
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
                WriterSketchCreationWizard.this.setVisible(false);
                WriterSketchCreationWizard.this.dispose();
                if (dialogue == null) {
                    creation = new WriterModeData(idField.getText());
                    creation.state = State.created;
                } else {
                    creation = new WriterModeData(idField.getText(), dialogue);
                }
                WriterSketchCreationWizard.this.proj.createWriterSketch(creation);
//				notifyCreated();
                ATContentStudio.frame.selectInTree(creation);
                ATContentStudio.frame.openEditor(creation);
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                creation = null;
                WriterSketchCreationWizard.this.setVisible(false);
                WriterSketchCreationWizard.this.dispose();
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
        if (idField.getText() == null || idField.getText().length() <= 0) {
            message.setText("<html><font color=\"#FF0000\">Internal ID must not be empty.</font></html>");
            trouble = true;
        } else if (proj.getWriterSketch(idField.getText()) != null) {
            message.setText("<html><font color=\"#FF0000\">An item with the same ID was already created in this project.</font></html>");
            trouble = true;
        }

        ok.setEnabled(!trouble);

        message.revalidate();
        message.repaint();
    }

}
