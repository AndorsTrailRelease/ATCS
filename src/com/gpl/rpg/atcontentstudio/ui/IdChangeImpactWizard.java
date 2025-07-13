package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class IdChangeImpactWizard extends JDialog {

    private static final long serialVersionUID = 8532169707953315739L;

    public static enum Result {
        ok, cancel
    }

    Result result = null;

    private IdChangeImpactWizard(GameDataElement changing, List<GameDataElement> toModify, List<GameDataElement> toAlter) {
        super(ATContentStudio.frame, true);
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS));

        pane.add(new JLabel("Changing the id for \"" + changing.getDesc() + "\" has impacts on your project:"), JideBoxLayout.FIX);
        pane.add(new JLabel("The following elements from your project will be modified:"), JideBoxLayout.FIX);
        JList<GameDataElement> modifList = new JList<GameDataElement>(new Vector<GameDataElement>(toModify));
        modifList.setCellRenderer(new ChangeImpactListCellRenderer());
        pane.add(new JScrollPane(modifList), JideBoxLayout.FIX);
        pane.add(new JLabel("The following elements from the game source will be altered:"), JideBoxLayout.FIX);
        JList<GameDataElement> alterList = new JList<GameDataElement>(new Vector<GameDataElement>(toAlter));
        alterList.setCellRenderer(new ChangeImpactListCellRenderer());
        pane.add(new JScrollPane(alterList), JideBoxLayout.FIX);
        pane.add(new JLabel("Press Ok to apply the changes, or Cancel to cancel your edition of the object's ID"), JideBoxLayout.FIX);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS));
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.cancel;
                dispose();
            }
        });
        buttonPane.add(cancelButton, JideBoxLayout.FIX);
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.ok;
                dispose();
            }
        });
        buttonPane.add(okButton, JideBoxLayout.FIX);
        pane.add(buttonPane, JideBoxLayout.FIX);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);
        pack();
    }


    public static Result showIdChangeImapctWizard(GameDataElement changing, List<GameDataElement> toModify, List<GameDataElement> toAlter) {
        IdChangeImpactWizard wizard = new IdChangeImpactWizard(changing, toModify, toAlter);
        wizard.setVisible(true);
        return wizard.result;
    }

    public class ChangeImpactListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 5764079243906396333L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                GameDataElement target = ((GameDataElement) value);
                label.setIcon(new ImageIcon(target.getIcon()));
                label.setText(target.getDataType().toString() + "/" + target.getDesc());
            }
            return c;
        }

    }

}
