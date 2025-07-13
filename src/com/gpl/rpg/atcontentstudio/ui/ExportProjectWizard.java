package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;
import com.jidesoft.swing.JideBoxLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ExportProjectWizard extends JDialog {

    private static final long serialVersionUID = -8745083621008868612L;

    JPanel pane;
    JLabel errorLabel, fileSelectionLabel;
    JRadioButton asZip, overSources;
    JComboBox<String> target;
    JButton browse;
    JButton okButton, cancelButton;

    Project proj;

    public ExportProjectWizard(Project proj) {

        super(ATContentStudio.frame);
        setTitle("Export project for inclusion in-game");

        this.proj = proj;

        pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        errorLabel = new JLabel();

        pane.add(errorLabel, JideBoxLayout.FIX);
        pane.add(new JLabel("Export this ATCS project..."), JideBoxLayout.FIX);

        ButtonGroup radioGroup = new ButtonGroup();

        asZip = new JRadioButton("... as a Zip archive");
        radioGroup.add(asZip);
        overSources = new JRadioButton("... into a game source folder");
        radioGroup.add(overSources);
        overSources.setSelected(true);

        pane.add(asZip, JideBoxLayout.FIX);
        pane.add(overSources, JideBoxLayout.FIX);

        ActionListener updateListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateState();
            }
        };
        asZip.addActionListener(updateListener);
        overSources.addActionListener(updateListener);

        target = new JComboBox<String>();
        target.setEditable(true);
        target.addActionListener(updateListener);
        browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser() {
                    private static final long serialVersionUID = -3001082967957619011L;

                    @Override
                    public boolean accept(File f) {
                        if (asZip.isSelected()) {
                            if (f.isDirectory() || f.getName().endsWith(".zip") || f.getName().endsWith(".ZIP")) {
                                return super.accept(f);
                            } else {
                                return false;
                            }
                        } else {
                            return f.isDirectory();
                        }
                    }
                };
                jfc.setFileSelectionMode(asZip.isSelected() ? JFileChooser.FILES_AND_DIRECTORIES : JFileChooser.DIRECTORIES_ONLY);
                jfc.setSelectedFile(new File(target.getSelectedItem() == null ? "" : target.getSelectedItem().toString()));
                jfc.setMultiSelectionEnabled(false);
                int result = jfc.showOpenDialog(ATContentStudio.frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File f = jfc.getSelectedFile();
                    if (asZip.isSelected() && !f.getAbsolutePath().substring(f.getAbsolutePath().length() - 4, f.getAbsolutePath().length()).equalsIgnoreCase(".zip")) {
                        f = new File(f.getAbsolutePath() + ".zip");
                    }
                    target.setSelectedItem(f.getAbsolutePath());
                    updateState();
                }
            }
        });
        JPanel fileSelectionPane = new JPanel();
        fileSelectionPane.setLayout(new JideBoxLayout(fileSelectionPane, JideBoxLayout.LINE_AXIS, 6));
        fileSelectionLabel = new JLabel("Zip file: ");
        fileSelectionPane.add(fileSelectionLabel, JideBoxLayout.FIX);
        fileSelectionPane.add(target, JideBoxLayout.VARY);
        fileSelectionPane.add(browse, JideBoxLayout.FIX);

        pane.add(fileSelectionPane, JideBoxLayout.FIX);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS, 6));
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);
        cancelButton = new JButton("Cancel");
        buttonPane.add(cancelButton, JideBoxLayout.FIX);
        okButton = new JButton("Ok");
        buttonPane.add(okButton, JideBoxLayout.FIX);

        pane.add(new JPanel(), JideBoxLayout.VARY);

        pane.add(buttonPane, JideBoxLayout.FIX);


        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ExportProjectWizard.this.setVisible(false);
                ExportProjectWizard.this.dispose();
            }
        });

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (asZip.isSelected()) {
                    ExportProjectWizard.this.proj.exportProjectAsZipPackage(new File(target.getSelectedItem().toString()));
                } else {
                    ExportProjectWizard.this.proj.exportProjectOverGameSource(new File(target.getSelectedItem().toString()));
                }
                ExportProjectWizard.this.setVisible(false);
                ExportProjectWizard.this.dispose();
            }
        });

        updateState();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(pane, BorderLayout.CENTER);

        setMinimumSize(new Dimension(500, 150));
        pack();

        Dimension sdim = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension wdim = getSize();
        setLocation((sdim.width - wdim.width) / 2, (sdim.height - wdim.height) / 2);
    }

    private void updateState() {
        if (asZip.isSelected()) {
            fileSelectionLabel.setText("Zip file: ");
        } else {
            fileSelectionLabel.setText("Game source folder: ");
        }


        File f = new File(target.getSelectedItem() == null ? "" : target.getSelectedItem().toString());
        if (asZip.isSelected()) {
            if (target.getSelectedItem() == null || target.getSelectedItem().toString().length() <= 0) {
                errorLabel.setText("<html><font color=\"#FF0000\">You must select where to save the zip file.</font></html>");
                okButton.setEnabled(false);
            } else if (f.isDirectory()) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected target is a directory. It should be a zip file.</font></html>");
                okButton.setEnabled(false);
            } else if (!(f.getName().toLowerCase().endsWith(".zip"))) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected target is not a zip file. It should be a zip file.</font></html>");
                okButton.setEnabled(false);
            } else if (f.exists()) {
                errorLabel.setText("<html><font color=\"#FF9000\">The selected target is an existing zip file. It will be overwritten.</font></html>");
                okButton.setEnabled(true);
            } else {
                errorLabel.setText("<html><font color=\"#00AA00\">Everything looks good !</font></html>");
                okButton.setEnabled(true);
            }
        } else {
            if (target.getSelectedItem() == null || target.getSelectedItem().toString().length() <= 0) {
                errorLabel.setText("<html><font color=\"#FF0000\">You must select an AT source root folder.</font></html>");
                okButton.setEnabled(false);
            } else if (!f.isDirectory() || !f.exists()) {
                errorLabel.setText("<html><font color=\"#FF0000\">The selected AT source is not a folder. It should be an existing AT source root folder.</font></html>");
                okButton.setEnabled(false);
            } else {
                File res = new File(f, GameDataSet.DEFAULT_REL_PATH_IN_SOURCE);
                File drawable = new File(f, SpriteSheetSet.DEFAULT_REL_PATH_IN_SOURCE);
                File xml = new File(f, TMXMapSet.DEFAULT_REL_PATH_IN_SOURCE);
                if (!res.exists()) {
                    errorLabel.setText("<html><font color=\"#FF9000\">The selected AT source root folder does not contain the \"res\" folder.</font></html>");
                    okButton.setEnabled(true);
                } else if (!drawable.exists()) {
                    errorLabel.setText("<html><font color=\"#FF9000\">The selected AT source root folder does not contain the \"drawable\" folder.</font></html>");
                    okButton.setEnabled(true);
                } else if (!xml.exists()) {
                    errorLabel.setText("<html><font color=\"#FF9000\">The selected AT source root folder does not contain the \"xml\" folder.</font></html>");
                    okButton.setEnabled(true);
                } else {
                    errorLabel.setText("<html><font color=\"#00AA00\">Everything looks good !</font></html>");
                    okButton.setEnabled(true);
                }
            }
        }
        revalidate();
        repaint();

    }


}
