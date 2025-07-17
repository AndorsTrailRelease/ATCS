package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataCategory;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.*;

public class SaveItemsWizard extends JDialog {

    private static final long serialVersionUID = -3301878024575930527L;

    List<SaveEvent> events;

    @SuppressWarnings("rawtypes")
    JList movedToCreated;
    @SuppressWarnings("rawtypes")
    JList movedToAltered;
    @SuppressWarnings("rawtypes")
    JList willBeSaved;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public SaveItemsWizard(List<SaveEvent> events, GameDataElement originalRequester) {
        super(ATContentStudio.frame);
        this.events = events;
        final List<SaveEvent> movedToAlteredList = new ArrayList<SaveEvent>();
        final List<SaveEvent> movedToCreatedList = new ArrayList<SaveEvent>();
        final List<SaveEvent> alsoSavedList = new ArrayList<SaveEvent>();
        final List<SaveEvent> errors = new ArrayList<SaveEvent>();
        for (SaveEvent event : events) {
            if (event.error) {
                errors.add(event);
            } else {
                switch (event.type) {
                    case alsoSave:
                        alsoSavedList.add(event);
                        break;
                    case moveToAltered:
                        movedToAlteredList.add(event);
                        break;
                    case moveToCreated:
                        movedToCreatedList.add(event);
                        break;
                }
            }
        }

        if (!errors.isEmpty()) {
            setTitle("Errors in project. Cannot save.");
            JPanel pane = new JPanel();
            pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));
            if (originalRequester != null) {
                pane.add(new JLabel(" While trying to save: "), JideBoxLayout.FIX);
                JLabel origItemDesc = new JLabel();
                origItemDesc.setIcon(new ImageIcon(originalRequester.getIcon()));
                origItemDesc.setText(originalRequester.getDataType().toString() + "/" + originalRequester.id);
                pane.add(origItemDesc, JideBoxLayout.FIX);
                pane.add(new JLabel(" the following errors have been encountered and must be corrected before saving can occur: "), JideBoxLayout.FIX);
            } else {
                pane.add(new JLabel("After deleting element(s), the following errors have been encountered and must be coorected before saving can occur: "), JideBoxLayout.FIX);
            }

            movedToCreated = new JList(errors.toArray());
            movedToCreated.setCellRenderer(new SaveEventsListCellRenderer());
            JPanel movedToCreatedPane = new JPanel();
            movedToCreatedPane.setLayout(new BorderLayout());
            movedToCreatedPane.add(new JScrollPane(movedToCreated), BorderLayout.CENTER);
            pane.add(movedToCreatedPane, JideBoxLayout.FLEXIBLE);

            pane.add(new JPanel(), JideBoxLayout.VARY);

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS, 6));
            buttonPane.add(new JPanel(), JideBoxLayout.VARY);
            JButton cancelButton = new JButton("Ok, back to work...");
            buttonPane.add(cancelButton, JideBoxLayout.FIX);
            pane.add(buttonPane, JideBoxLayout.FIX);
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SaveItemsWizard.this.setVisible(false);
                    SaveItemsWizard.this.dispose();
                }
            });


            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(pane, BorderLayout.CENTER);

        } else {
            setTitle("Other elements impacted.");
            JPanel pane = new JPanel();
            pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));
            if (originalRequester != null) {
                pane.add(new JLabel(" While trying to save: "), JideBoxLayout.FIX);
                JLabel origItemDesc = new JLabel();
                origItemDesc.setIcon(new ImageIcon(originalRequester.getIcon()));
                origItemDesc.setText(originalRequester.getDataType().toString() + "/" + originalRequester.id);
                pane.add(origItemDesc, JideBoxLayout.FIX);
                pane.add(new JLabel(" the following side-effects have been identified and must be applied to the project before saving: "), JideBoxLayout.FIX);
            } else {
                pane.add(new JLabel("After deleting element(s), the following side-effects have been identified and must be applied to the project before saving: "), JideBoxLayout.FIX);
            }

            if (!movedToCreatedList.isEmpty()) {
                movedToCreated = new JList(movedToCreatedList.toArray());
                movedToCreated.setCellRenderer(new SaveEventsListCellRenderer());
                JPanel movedToCreatedPane = new JPanel();
                movedToCreatedPane.setLayout(new BorderLayout());
                movedToCreatedPane.setBorder(BorderFactory.createTitledBorder("The following elements will be moved under the \"Created\" folder and saved:"));
                movedToCreatedPane.add(new JScrollPane(movedToCreated), BorderLayout.CENTER);
                pane.add(movedToCreatedPane, JideBoxLayout.FLEXIBLE);
            }

            if (!movedToAlteredList.isEmpty()) {
                movedToAltered = new JList(movedToAlteredList.toArray());
                movedToAltered.setCellRenderer(new SaveEventsListCellRenderer());
                JPanel movedToAlteredPane = new JPanel();
                movedToAlteredPane.setLayout(new BorderLayout());
                movedToAlteredPane.setBorder(BorderFactory.createTitledBorder("The following elements will be moved under the \"Altered\" folder and saved:"));
                movedToAlteredPane.add(new JScrollPane(movedToAltered), BorderLayout.CENTER);
                pane.add(movedToAlteredPane, JideBoxLayout.FLEXIBLE);
            }

            if (!alsoSavedList.isEmpty()) {
                willBeSaved = new JList(alsoSavedList.toArray());
                willBeSaved.setCellRenderer(new SaveEventsListCellRenderer());
                JPanel willBeSavedPane = new JPanel();
                willBeSavedPane.setLayout(new BorderLayout());
                willBeSavedPane.setBorder(BorderFactory.createTitledBorder("The following elements will be saved too:"));
                willBeSavedPane.add(new JScrollPane(willBeSaved), BorderLayout.CENTER);
                pane.add(willBeSavedPane, JideBoxLayout.FLEXIBLE);
            }

            pane.add(new JPanel(), JideBoxLayout.VARY);

            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS, 6));
            buttonPane.add(new JPanel(), JideBoxLayout.VARY);
            JButton cancelButton = new JButton("Cancel");
            buttonPane.add(cancelButton, JideBoxLayout.FIX);
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SaveItemsWizard.this.setVisible(false);
                    SaveItemsWizard.this.dispose();
                }
            });
            JButton okButton = new JButton("Apply all changes and save");
            buttonPane.add(okButton, JideBoxLayout.FIX);
            pane.add(buttonPane, JideBoxLayout.FIX);
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    Map<GameDataCategory<JSONElement>, Set<File>> jsonToSave = new IdentityHashMap<GameDataCategory<JSONElement>, Set<File>>();
                    for (SaveEvent event : movedToCreatedList) {
                        if (event.target instanceof JSONElement) {
                            if (!jsonToSave.containsKey(event.target.getParent())) {
                                jsonToSave.put((GameDataCategory<JSONElement>) event.target.getParent(), new HashSet<File>());
                            }
                            jsonToSave.get((GameDataCategory<JSONElement>) event.target.getParent()).add(((JSONElement) event.target).jsonFile);

                            event.target.getProject().moveToCreated((JSONElement) event.target);

                            if (!jsonToSave.containsKey(event.target.getParent())) {
                                jsonToSave.put((GameDataCategory<JSONElement>) event.target.getParent(), new HashSet<File>());
                            }
                            jsonToSave.get((GameDataCategory<JSONElement>) event.target.getParent()).add(((JSONElement) event.target).jsonFile);
                        }
                        //TODO movable maps, when ID is editable.

                    }
                    for (SaveEvent event : movedToAlteredList) {
                        if (event.target instanceof JSONElement) {
                            if (!jsonToSave.containsKey(event.target.getParent())) {
                                jsonToSave.put((GameDataCategory<JSONElement>) event.target.getParent(), new HashSet<File>());
                            }
                            jsonToSave.get((GameDataCategory<JSONElement>) event.target.getParent()).add(((JSONElement) event.target).jsonFile);

                            event.target.getProject().moveToAltered((JSONElement) event.target);

                            if (!jsonToSave.containsKey(event.target.getParent())) {
                                jsonToSave.put((GameDataCategory<JSONElement>) event.target.getParent(), new HashSet<File>());
                            }
                            jsonToSave.get((GameDataCategory<JSONElement>) event.target.getParent()).add(((JSONElement) event.target).jsonFile);
                        }
                        //TODO movable maps, when ID is editable.
                    }
                    for (SaveEvent event : alsoSavedList) {
                        if (event.target instanceof JSONElement) {
                            if (!jsonToSave.containsKey(event.target.getParent())) {
                                jsonToSave.put((GameDataCategory<JSONElement>) event.target.getParent(), new HashSet<File>());
                            }
                            jsonToSave.get((GameDataCategory<JSONElement>) event.target.getParent()).add(((JSONElement) event.target).jsonFile);
                        }
                    }

                    for (GameDataCategory<JSONElement> cat : jsonToSave.keySet()) {
                        if (jsonToSave.get(cat) != null && !jsonToSave.get(cat).isEmpty()) {
                            for (File f : jsonToSave.get(cat)) {
                                cat.save(f);
                            }
                        }
                    }

                    for (SaveEvent event : movedToCreatedList) {
                        ATContentStudio.frame.nodeChanged(event.target);
                    }
                    for (SaveEvent event : movedToAlteredList) {
                        ATContentStudio.frame.nodeChanged(event.target);
                    }
                    for (SaveEvent event : alsoSavedList) {
                        ATContentStudio.frame.nodeChanged(event.target);
                    }
                    SaveItemsWizard.this.setVisible(false);
                    SaveItemsWizard.this.dispose();
                }
            });

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(pane, BorderLayout.CENTER);

        }

        pack();
    }

    public class SaveEventsListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 5764079243906396333L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                SaveEvent event = (SaveEvent) value;
                label.setIcon(new ImageIcon(event.target.getIcon()));
                if (event.error) {
                    label.setText(event.target.getDataType().toString() + "/" + event.target.id + ": " + event.errorText);
                } else {
                    label.setText(event.target.getDataType().toString() + "/" + event.target.id);
                }
            }
            return c;
        }

    }

}
