package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ConfigCache;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceSelector extends JFrame {

    private static final long serialVersionUID = 7518745499760748574L;

    public String selected = null;

    public WorkspaceSelector() {
        super("Select your workspace");
        setIconImage(DefaultIcons.getMainIconImage());

        //Data
        final List<File> workspaces = ConfigCache.getKnownWorkspaces();
        final List<String> wsPaths = new ArrayList<String>();

        //Active widgets declaration
        final JComboBox<String> combo = new JComboBox<String>();
        final JButton browse = new JButton("Browse...");
        final JButton cancel = new JButton("Cancel");
        final JButton ok = new JButton("Ok");

        //Widgets behavior
        combo.setEditable(true);
        for (File f : workspaces) {
            String path = f.getAbsolutePath();
            wsPaths.add(path);
            combo.addItem(path);
        }
        if (ConfigCache.getLatestWorkspace() != null) {
            combo.setSelectedItem(wsPaths.get(workspaces.indexOf(ConfigCache.getLatestWorkspace())));
        }
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (combo.getSelectedItem() != null) {
                    ok.setEnabled(true);
                }
            }
        });


        ok.setEnabled(ConfigCache.getLatestWorkspace() != null);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceSelector.this.selected = (String) combo.getSelectedItem();
                WorkspaceSelector.this.dispose();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkspaceSelector.this.selected = null;
                WorkspaceSelector.this.dispose();
            }
        });

        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc;
                if (workspaces.isEmpty()) {
                    fc = new JFileChooser();
                } else {
                    if (ConfigCache.getLatestWorkspace() != null) {
                        fc = new JFileChooser(ConfigCache.getLatestWorkspace());
                    } else {
                        fc = new JFileChooser(workspaces.get(0));
                    }
                }
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                fc.setAcceptAllFileFilterUsed(false);
                int result = fc.showSaveDialog(WorkspaceSelector.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String selected = fc.getSelectedFile().getAbsolutePath();
                    for (String s : wsPaths) {
                        if (s.equals(selected)) {
                            selected = s;
                        }
                    }
                    combo.setSelectedItem(selected);
                }
            }
        });


        //Layout, labels and dialog behavior.
        setTitle("Select your workspace");

        JLabel logoLabel = new JLabel();
        try {
            logoLabel = new JLabel(new ImageIcon(ImageIO.read(WorkspaceSelector.class.getResource("/com/gpl/rpg/atcontentstudio/img/atcs_logo_banner.png"))), JLabel.CENTER);
        } catch (IOException e1) {
        }

        JPanel dialogPane = new JPanel();
        dialogPane.setLayout(new BorderLayout());

        dialogPane.add(logoLabel, BorderLayout.NORTH);
        dialogPane.add(new JLabel("Workspace : "), BorderLayout.WEST);
        dialogPane.add(combo, BorderLayout.CENTER);
        dialogPane.add(browse, BorderLayout.EAST);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        buttonPane.add(new JLabel(), c);

        c.fill = GridBagConstraints.VERTICAL;
        c.weightx = 0;
        c.gridx++;
        buttonPane.add(cancel, c);

        c.gridx++;
        buttonPane.add(ok, c);

        dialogPane.add(buttonPane, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setContentPane(dialogPane);
        setResizable(false);
    }

}
