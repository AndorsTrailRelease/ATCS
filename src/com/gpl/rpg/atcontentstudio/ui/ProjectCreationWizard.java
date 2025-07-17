package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.Project.ResourceSet;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProjectCreationWizard extends JDialog {

    private static final long serialVersionUID = -2854969975146867119L;

    final JTextField projectNameField;
    final JComboBox<String> atSourceSelectionCombo;
    final JComboBox<Project.ResourceSet> resourceSetToUse;

    final JButton browse;
    final JButton okButton;
    final JButton cancelButton;

    final JLabel errorLabel;

    public ProjectCreationWizard() {
        super(ATContentStudio.frame);
        setTitle("Create project");
        projectNameField = new JTextField();
        atSourceSelectionCombo = new JComboBox<String>();
        resourceSetToUse = new JComboBox<Project.ResourceSet>(new ComboBoxModel<Project.ResourceSet>() {

            Project.ResourceSet selected = Project.ResourceSet.gameData;

            @Override
            public int getSize() {
                return Project.ResourceSet.values().length;
            }

            @Override
            public ResourceSet getElementAt(int index) {
                return Project.ResourceSet.values()[index];
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
                selected = (ResourceSet) anItem;
            }

            @Override
            public Object getSelectedItem() {
                return selected;
            }

        });
        resourceSetToUse.setRenderer(new ListCellRenderer<Project.ResourceSet>() {
            @Override
            public Component getListCellRendererComponent(
                    JList<? extends ResourceSet> list, ResourceSet value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                switch (value) {
                    case allFiles:
                        return new JLabel("All available files");
                    case debugData:
                        return new JLabel("Debug data");
                    case gameData:
                        return new JLabel("Real game data");
                    default:
                        return new JLabel();
                }

            }
        });
        browse = new JButton("Browse...");
        okButton = new JButton("Ok");
        cancelButton = new JButton("Cancel");
        errorLabel = new JLabel("Enter the following information about your project.");

        projectNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateOkButtonEnablement();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateOkButtonEnablement();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateOkButtonEnablement();
            }
        });
        for (File f : Workspace.activeWorkspace.knownMapSourcesFolders) {
            atSourceSelectionCombo.addItem(f.getAbsolutePath());
        }
        atSourceSelectionCombo.setEditable(true);
        atSourceSelectionCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOkButtonEnablement();
            }
        });
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                boolean keepTrying = true;
                if (atSourceSelectionCombo.getSelectedItem() != null && ((String) atSourceSelectionCombo.getSelectedItem()).length() > 0) {
                    File f = new File((String) atSourceSelectionCombo.getSelectedItem());
                    if (f.exists()) {
                        chooser.setCurrentDirectory(f);
                        keepTrying = false;
                    }
                }
                if (keepTrying && Workspace.activeWorkspace.knownMapSourcesFolders != null && !Workspace.activeWorkspace.knownMapSourcesFolders.isEmpty()) {
                    chooser.setCurrentDirectory(Workspace.activeWorkspace.knownMapSourcesFolders.iterator().next());
                }
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(ProjectCreationWizard.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    atSourceSelectionCombo.setSelectedItem(chooser.getSelectedFile().getAbsolutePath());
                    updateOkButtonEnablement();
                }
            }
        });
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File atSourceFolder = new File((String) atSourceSelectionCombo.getSelectedItem());
                if (!Workspace.activeWorkspace.knownMapSourcesFolders.contains(atSourceFolder)) {
                    Workspace.activeWorkspace.knownMapSourcesFolders.add(atSourceFolder);
                }
                Workspace.createProject(projectNameField.getText(), atSourceFolder, (Project.ResourceSet) resourceSetToUse.getSelectedItem());
                ProjectCreationWizard.this.dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectCreationWizard.this.dispose();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = 3;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 100;
        c.weighty = 100;
        panel.add(errorLabel, c);

        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 20;
        panel.add(new JLabel("Project name: "), c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 80;
        panel.add(projectNameField, c);

        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 20;
        panel.add(new JLabel("AT Source: "), c);

        c.gridx++;
        c.weightx = 60;
        panel.add(atSourceSelectionCombo, c);

        c.gridx++;
        c.weightx = 20;
        panel.add(browse, c);

        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 20;
        panel.add(new JLabel("Resource set: "), c);

        c.gridx++;
        c.weightx = 80;
        c.gridwidth = 2;
        panel.add(resourceSetToUse, c);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.HORIZONTAL;

        c2.gridx = 1;
        c2.weightx = 80;
        buttonPane.add(new JLabel(), c2);

        c2.gridx++;
        c2.weightx = 10;
        c.fill = GridBagConstraints.NONE;
        buttonPane.add(cancelButton, c2);

        c2.gridx++;
        c2.weightx = 10;
        buttonPane.add(okButton, c2);

        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        panel.add(buttonPane, c);

        updateOkButtonEnablement();

        setContentPane(panel);

        pack();
        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
    }


    protected void updateOkButtonEnablement() {
        if (projectNameField.getText() == null || projectNameField.getText().length() <= 0) {
            errorLabel.setText("<html><font color=\"#FF0000\">Select a project name.</font></html>");
            this.okButton.setEnabled(false);
            return;
        }
        if (atSourceSelectionCombo.getSelectedItem() == null || ((String) atSourceSelectionCombo.getSelectedItem()).length() <= 0) {
            errorLabel.setText("<html><font color=\"#FF0000\">Select an AT source root folder.</font></html>");
            this.okButton.setEnabled(false);
            return;
        }
        File projFolder = new File(Workspace.activeWorkspace.baseFolder, projectNameField.getText() + File.separator);
        File sourceFolder = new File((String) atSourceSelectionCombo.getSelectedItem());
        if (projFolder.exists()) {
            errorLabel.setText("<html><font color=\"#FF0000\">A project with this name already exists.</font></html>");
            this.okButton.setEnabled(false);
            return;
        } else {
            try {
                projFolder.getCanonicalPath();
            } catch (IOException ioe) {
                errorLabel.setText("<html><font color=\"#FF0000\">" + projectNameField.getText() + " is not a valid project name.</font></html>");
                this.okButton.setEnabled(false);
                return;
            }
        }
        if (!sourceFolder.exists()) {
            errorLabel.setText("<html><font color=\"#FF0000\">The selected AT source root folder does not exist.</font></html>");
            this.okButton.setEnabled(false);
            return;
        } else {
            File res = new File(sourceFolder, GameDataSet.DEFAULT_REL_PATH_IN_SOURCE);
            if (!res.exists()) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected AT source root folder does not contain the \"res\" folder.</font></html>");
                this.okButton.setEnabled(false);
                return;
            }
            File drawable = new File(sourceFolder, SpriteSheetSet.DEFAULT_REL_PATH_IN_SOURCE);
            if (!drawable.exists()) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected AT source root folder does not contain the \"drawable\" folder.</font></html>");
                this.okButton.setEnabled(false);
                return;
            }
            File xml = new File(sourceFolder, TMXMapSet.DEFAULT_REL_PATH_IN_SOURCE);
            if (!xml.exists()) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected AT source root folder does not contain the \"xml\" folder.</font></html>");
                this.okButton.setEnabled(false);
                return;
            }
        }
        if (!projFolder.exists() && sourceFolder.exists()) {
            errorLabel.setText("<html><font color=\"#00AA00\">Everything looks good !</font></html>");
            this.okButton.setEnabled(true);
            return;
        }
    }

}
