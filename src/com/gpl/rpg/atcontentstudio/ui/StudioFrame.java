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
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
        treeScroller.getVerticalScrollBar().setUnitIncrement(12);
        leftRight.setLeftComponent(treeScroller);
        leftRight.setRightComponent(editors);
        leftRight.setName("StudioFrame.leftRight");
        topDown.setTopComponent(leftRight);
        JScrollPane notifScroller = new JScrollPane(notifs);
        notifScroller.getVerticalScrollBar().setUnitIncrement(12);
        topDown.setBottomComponent(notifScroller);
        topDown.setName("StudioFrame.topDown");

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topDown, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (isInNormalWindowState()) {
                    Workspace.activeWorkspace.preferences.windowSize = StudioFrame.this.getSize();
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                if (isInNormalWindowState()) {
                    Workspace.activeWorkspace.preferences.windowLocation = StudioFrame.this.getLocation();
                }
            }
        });

        pack();
        restoreNormalWindowState();
        if (Workspace.activeWorkspace.preferences.windowSize != null) {
            setSize(Workspace.activeWorkspace.preferences.windowSize);
        } else {
            setSize(800, 600);
        }
        if (isSavedLocationUsable(Workspace.activeWorkspace.preferences.windowLocation)) {
            setLocation(Workspace.activeWorkspace.preferences.windowLocation);
        } else {
            setLocationRelativeTo(null);
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
            public void windowOpened(WindowEvent e) {
                SwingUtilities.invokeLater(() -> restoreNormalWindowState());
            }

            @Override
            public void windowClosing(WindowEvent e) {
                Workspace.saveActive();
                actions.exitATCS.actionPerformed(null);
            }
        });
    }

    private boolean isInNormalWindowState() {
        return (getExtendedState() & Frame.MAXIMIZED_BOTH) == 0;
    }

    private void restoreNormalWindowState() {
        setExtendedState(getExtendedState() & ~Frame.MAXIMIZED_BOTH & ~Frame.ICONIFIED);
    }

    private boolean isSavedLocationUsable(Point savedLocation) {
        if (savedLocation == null) {
            return false;
        }

        Rectangle savedBounds = new Rectangle(savedLocation, getSize());
        for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle screenBounds = device.getDefaultConfiguration().getBounds();
            if (screenBounds.intersects(savedBounds)) {
                return true;
            }
        }

        return false;
    }

    private void buildMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(new JMenuItem(actions.newWorkspace)).setMnemonic(KeyEvent.VK_N);
        fileMenu.add(new JMenuItem(actions.openWorkspace)).setMnemonic(KeyEvent.VK_W);
        fileMenu.add(createRecentWorkspacesMenu()).setMnemonic(KeyEvent.VK_R);
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(actions.createProject)).setMnemonic(KeyEvent.VK_P);
        fileMenu.add(new JMenuItem(actions.openProject)).setMnemonic(KeyEvent.VK_O);
        fileMenu.add(new JMenuItem(actions.closeProject)).setMnemonic(KeyEvent.VK_C);
        fileMenu.add(new JMenuItem(actions.deleteProject)).setMnemonic(KeyEvent.VK_D);
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(actions.editWorkspaceSettings)).setMnemonic(KeyEvent.VK_E);
        fileMenu.add(new JSeparator());
        fileMenu.add(new JMenuItem(actions.exitATCS)).setMnemonic(KeyEvent.VK_X);
        getJMenuBar().add(fileMenu);

        JMenu projectMenu = new JMenu("Project");
        projectMenu.setMnemonic(KeyEvent.VK_P);
        projectMenu.add(new JMenuItem(editors.getSaveCurrentEditorAction())).setMnemonic(KeyEvent.VK_S);
        projectMenu.add(new JMenuItem(actions.deleteSelected)).setMnemonic(KeyEvent.VK_R);
        projectMenu.add(new JMenuItem(editors.getCloseCurrentEditorAction())).setMnemonic(KeyEvent.VK_W);
        projectMenu.add(new JSeparator());
        projectMenu.add(new JMenuItem(actions.createGDE)).setMnemonic(KeyEvent.VK_J);
        projectMenu.add(new JMenuItem(actions.importJSON)).setMnemonic(KeyEvent.VK_I);
        projectMenu.add(new JMenuItem(actions.createMap)).setMnemonic(KeyEvent.VK_M);
        projectMenu.add(new JMenuItem(actions.createWorldmap)).setMnemonic(KeyEvent.VK_W);
        getJMenuBar().add(projectMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic(KeyEvent.VK_T);
        toolsMenu.add(new JMenuItem(actions.compareItems)).setMnemonic(KeyEvent.VK_I);
        toolsMenu.add(new JMenuItem(actions.compareNPCs)).setMnemonic(KeyEvent.VK_N);
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new JMenuItem(actions.runBeanShell)).setMnemonic(KeyEvent.VK_B);
        toolsMenu.add(new JSeparator());
        toolsMenu.add(new JMenuItem(actions.exportProject)).setMnemonic(KeyEvent.VK_E);
        getJMenuBar().add(toolsMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        JMenu changeLaF = new JMenu("Look & Feel");
        changeLaF.setMnemonic(KeyEvent.VK_L);
        int j = 1;
        for (final LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
            JMenuItem lafItem = null;
            if( j <= 9) {
                lafItem = new JMenuItem(j + ": " + i.getName());
                lafItem.setMnemonic(KeyEvent.VK_0 + j);
            } else {
                lafItem = new JMenuItem(i.getName());
            }

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
            j++;
        }
        viewMenu.add(changeLaF);
        viewMenu.add(new JSeparator());
        viewMenu.add(new JMenuItem(actions.showAbout)).setMnemonic(KeyEvent.VK_A);
        getJMenuBar().add(viewMenu);

        if (ATContentStudio.hasDefaultExportTarget()) {
            JButton exportButton = new JButton("Export");
            exportButton.addActionListener(actions.exportProject);
            exportButton.setBorderPainted(false);
            exportButton.setFocusPainted(false);
            exportButton.setContentAreaFilled(false);
            getJMenuBar().add(exportButton);
        }
    }

    private JMenu createRecentWorkspacesMenu() {
        final JMenu recentWorkspacesMenu = new JMenu("Recent Workspaces");
        recentWorkspacesMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                populateRecentWorkspacesMenu(recentWorkspacesMenu);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
        populateRecentWorkspacesMenu(recentWorkspacesMenu);
        return recentWorkspacesMenu;
    }

    private void populateRecentWorkspacesMenu(JMenu recentWorkspacesMenu) {
        recentWorkspacesMenu.removeAll();

        List<File> recentWorkspaces = getRecentWorkspaceChoices();
        if (recentWorkspaces.isEmpty()) {
            JMenuItem emptyItem = new JMenuItem("No other workspaces");
            emptyItem.setEnabled(false);
            recentWorkspacesMenu.add(emptyItem);
            return;
        }

        for (File workspaceRoot : recentWorkspaces) {
            JMenuItem workspaceItem = new JMenuItem(formatWorkspaceMenuLabel(workspaceRoot));
            workspaceItem.setToolTipText(workspaceRoot.getAbsolutePath());
            workspaceItem.addActionListener(e -> actions.launchWorkspace(workspaceRoot, "Open Workspace"));
            recentWorkspacesMenu.add(workspaceItem);
        }
    }

    private String formatWorkspaceMenuLabel(File workspaceRoot) {
        String workspaceName = workspaceRoot.getName();
        if (workspaceName == null || workspaceName.isEmpty()) {
            workspaceName = workspaceRoot.getAbsolutePath();
        }
        return ATContentStudio.getWorkspaceDisplayPath(workspaceRoot);
    }

    private List<File> getRecentWorkspaceChoices() {
        List<File> recentWorkspaces = new ArrayList<>();
        Set<String> seenPaths = new LinkedHashSet<>();

        addRecentWorkspaceChoice(recentWorkspaces, seenPaths, ConfigCache.getLatestWorkspace());

        List<File> knownWorkspaces = ConfigCache.getKnownWorkspaces();
        for (int i = knownWorkspaces.size() - 1; i >= 0; i--) {
            addRecentWorkspaceChoice(recentWorkspaces, seenPaths, knownWorkspaces.get(i));
        }

        return recentWorkspaces;
    }

    private void addRecentWorkspaceChoice(List<File> recentWorkspaces, Set<String> seenPaths, File workspaceRoot) {
        if (workspaceRoot == null) {
            return;
        }

        File normalizedWorkspaceRoot = workspaceRoot.getAbsoluteFile();
        if (!Workspace.isValidWorkspaceRoot(normalizedWorkspaceRoot)) {
            return;
        }
        if (Workspace.activeWorkspace != null && normalizedWorkspaceRoot.equals(Workspace.activeWorkspace.baseFolder.getAbsoluteFile())) {
            return;
        }

        String workspacePath = normalizedWorkspaceRoot.getAbsolutePath();
        if (seenPaths.add(workspacePath)) {
            recentWorkspaces.add(normalizedWorkspaceRoot);
        }
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
