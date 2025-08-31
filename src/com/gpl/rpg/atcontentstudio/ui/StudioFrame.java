package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.ConfigCache;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.JSONElement;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class StudioFrame extends JFrame {

    private static final long serialVersionUID = -3391514100319186661L;


    final ProjectsTree projectTree;
    final EditorsArea editors;

    final WorkspaceActions actions = new WorkspaceActions();

    public StudioFrame(String name) {
        super(name);
        setIconImage(DefaultIcons.getMainIconImage());

        final JSplitPane topDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        final JSplitPane leftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        @SuppressWarnings("rawtypes")
        JList notifs = new NotificationsPane();
        projectTree = new ProjectsTree();
        editors = new EditorsArea();

        setJMenuBar(new JMenuBar());
        buildMenu();

        JScrollPane treeScroller = new JScrollPane(projectTree);
        treeScroller.getVerticalScrollBar().setUnitIncrement(16);
        leftRight.setLeftComponent(treeScroller);
        leftRight.setRightComponent(editors);
        leftRight.setName("StudioFrame.leftRight");
        topDown.setTopComponent(leftRight);
        JScrollPane notifScroller = new JScrollPane(notifs);
        notifScroller.getVerticalScrollBar().setUnitIncrement(16);
        topDown.setBottomComponent(notifScroller);
        topDown.setName("StudioFrame.topDown");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topDown, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Workspace.activeWorkspace.preferences.windowSize = StudioFrame.this.getSize();
            }
        });

        pack();
        if (Workspace.activeWorkspace.preferences.windowSize != null) {
            setSize(Workspace.activeWorkspace.preferences.windowSize);
        } else {
            setSize(800, 600);
        }

        if (Workspace.activeWorkspace.preferences.splittersPositions.get(topDown.getName()) != null) {
            topDown.setDividerLocation(Workspace.activeWorkspace.preferences.splittersPositions.get(topDown.getName()));
        } else {
            topDown.setDividerLocation(0.2);
        }
        topDown.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Workspace.activeWorkspace.preferences.splittersPositions.put(topDown.getName(), topDown.getDividerLocation());
            }
        });
        if (Workspace.activeWorkspace.preferences.splittersPositions.get(leftRight.getName()) != null) {
            leftRight.setDividerLocation(Workspace.activeWorkspace.preferences.splittersPositions.get(leftRight.getName()));
        } else {
            leftRight.setDividerLocation(0.3);
        }
        leftRight.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Workspace.activeWorkspace.preferences.splittersPositions.put(leftRight.getName(), leftRight.getDividerLocation());
            }
        });

        showAbout();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Workspace.saveActive();
                actions.exitATCS.actionPerformed(null);
            }
        });
    }

    private void buildMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(actions.createProject));
        fileMenu.add(new JMenuItem(actions.openProject));
        fileMenu.add(new JMenuItem(actions.closeProject));
        fileMenu.add(new JMenuItem(actions.deleteProject));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(actions.editWorkspaceSettings));
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(actions.exitATCS));
        getJMenuBar().add(fileMenu);

        JMenu projectMenu = new JMenu("Project");
        projectMenu.add(new JMenuItem(actions.saveElement));
        projectMenu.add(new JMenuItem(actions.deleteSelected));
        projectMenu.add(new JSeparator());
        projectMenu.add(new JMenuItem(actions.createGDE));
        projectMenu.add(new JMenuItem(actions.importJSON));
        projectMenu.add(new JMenuItem(actions.createMap));
        projectMenu.add(new JMenuItem(actions.createWorldmap));
        getJMenuBar().add(projectMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.add(new JMenuItem(actions.compareItems));
        toolsMenu.add(new JMenuItem(actions.compareNPCs));
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new JMenuItem(actions.runBeanShell));
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new JMenuItem(actions.exportProject));
        getJMenuBar().add(toolsMenu);

        JMenu viewMenu = new JMenu("View");
        JMenu changeLaF = new JMenu("Change Look and Feel");
        for (final LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
            final JMenuItem lafItem = new JMenuItem("Switch to " + i.getName());
            changeLaF.add(lafItem);
            lafItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String lookAndFeel = i.getClassName();
                    ATContentStudio.setLookAndFeel(lookAndFeel);
                    SwingUtilities.updateComponentTreeUI(ATContentStudio.frame);
                    ConfigCache.setFavoriteLaFClassName(lookAndFeel);
                }
            });
        }
        viewMenu.add(changeLaF);
        viewMenu.add(new JSeparator());
        viewMenu.add(new JMenuItem(actions.showAbout));
        getJMenuBar().add(viewMenu);
    }

    public void openEditor(JSONElement node) {
        node.link();
        editors.openEditor(node);
    }

    public void openEditor(Spritesheet node) {
        editors.openEditor(node);
    }

    public void openEditor(TMXMap node) {
        node.parse();
        editors.openEditor(node);
    }

    public void openEditor(WriterModeData node) {
        node.link();
        editors.openEditor(node);
    }


    public void openEditor(GameDataElement node) {
        if (node instanceof JSONElement) {
            openEditor((JSONElement) node);
        } else if (node instanceof Spritesheet) {
            openEditor((Spritesheet) node);
        } else if (node instanceof TMXMap) {
            openEditor((TMXMap) node);
        } else if (node instanceof WorldmapSegment) {
            openEditor((WorldmapSegment) node);
        } else if (node instanceof WriterModeData) {
            openEditor((WriterModeData) node);
        }
    }

    public void openEditor(WorldmapSegment node) {
        editors.openEditor(node);
    }

    public void closeEditor(ProjectTreeNode node) {
        editors.closeEditor(node);
    }

    public void selectInTree(ProjectTreeNode node) {
        projectTree.setSelectedNode(node);
    }

    public void editorChanged(Editor e) {
        editors.editorTabChanged(e);
    }

    public void editorChanged(ProjectTreeNode node) {
        editors.editorTabChanged(node);
    }

    public void nodeChanged(ProjectTreeNode node) {
        node.childrenChanged(new ArrayList<ProjectTreeNode>());
        ATContentStudio.frame.editorChanged(node);
    }

    public void showAbout() {
        editors.showAbout();
    }

}
