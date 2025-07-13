package com.gpl.rpg.atcontentstudio.ui.sprites;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.Editor;
import com.gpl.rpg.atcontentstudio.ui.FieldUpdateListener;
import com.gpl.rpg.atcontentstudio.ui.ListenerListModel;
import com.gpl.rpg.atcontentstudio.utils.DesktopIntegration;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideTabbedPane;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpritesheetEditor extends Editor {

    private static final long serialVersionUID = 3956109815682889863L;

    Map<String, JPanel> editorTabs = new LinkedHashMap<String, JPanel>();
    JideTabbedPane editorTabsHolder;

    private JSpinner widthField;
    private JSpinner heightField;
    private JCheckBox animatedBox;
    @SuppressWarnings("rawtypes")
    private JComboBox categoryBox;
    private JPanel spriteViewPane;


    public static JComponent getWarningLabel() {
        JLabel label = new JLabel(
                "<html><i>" +
                        "The data accompamying the image here is not part of the game.<br/>" +
                        "What you change here will be changed in your ATCS project only.<br/>" +
                        "None of this is exported to JSON or TMX, although it must be set correctly in order to choose tiles & icons correctly.<br/>" +
                        "</i></html>");
        return label;
    }

    public SpritesheetEditor(Spritesheet sheet) {
        super();
        this.icon = new ImageIcon(sheet.getIcon(0));
        this.name = sheet.id;
        this.target = sheet;

        JPanel pane = new JPanel();

        final FieldUpdateListener listener = new SpritesheetFieldUpdater();

        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new JideBoxLayout(buttonPane, JideBoxLayout.LINE_AXIS));
        JButton openImage = new JButton(new ImageIcon(DefaultIcons.getTileLayerImage()));
        openImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DesktopIntegration.openImage(((Spritesheet) target).spritesheetFile);
            }
        });
        buttonPane.add(openImage, JideBoxLayout.FIX);
        buttonPane.add(getWarningLabel(), JideBoxLayout.FIX);
        final JButton bookmark = new JButton(new ImageIcon(sheet.bookmark != null ? DefaultIcons.getBookmarkActiveIcon() : DefaultIcons.getBookmarkInactiveIcon()));
        buttonPane.add(bookmark, JideBoxLayout.FIX);
        bookmark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (target.bookmark == null) {
                    target.getProject().bookmark(target);
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkActiveIcon()));
                } else {
                    target.bookmark.delete();
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkInactiveIcon()));
                }
            }
        });
        buttonPane.add(new JPanel(), JideBoxLayout.VARY);
        pane.add(buttonPane, JideBoxLayout.FIX);
        addLabelField(pane, "Spritesheet ID: ", sheet.id);
        addLabelField(pane, "File: ", sheet.spritesheetFile.getAbsolutePath());
        widthField = addIntegerField(pane, "Sprite width (px): ", sheet.spriteWidth, false, true, listener);
        heightField = addIntegerField(pane, "Sprite height (px): ", sheet.spriteHeight, false, true, listener);
        animatedBox = addBooleanBasedCheckBox(pane, "Is an animation", sheet.animated, true, listener);
        categoryBox = addEnumValueBox(pane, "Category: ", Spritesheet.Category.values(), sheet.category, true, listener);

        spriteViewPane = new JPanel();
        updateView(spriteViewPane);
        pane.add(spriteViewPane, JideBoxLayout.FIX);

        addBacklinksList(pane, sheet);

        //Placeholder. Fills the eventual remaining space.
        pane.add(new JPanel(), JideBoxLayout.VARY);

        setLayout(new BorderLayout());
        editorTabsHolder = new JideTabbedPane(JideTabbedPane.BOTTOM);
        editorTabsHolder.setTabShape(JideTabbedPane.SHAPE_FLAT);
        editorTabsHolder.setUseDefaultShowCloseButtonOnTab(false);
        editorTabsHolder.setShowCloseButtonOnTab(false);
        add(editorTabsHolder, BorderLayout.CENTER);

        JScrollPane sheetScroller = new JScrollPane(pane);
        sheetScroller.getVerticalScrollBar().setUnitIncrement(16);
        editorTabsHolder.add("Spritesheet", sheetScroller);
        JScrollPane rawScroller = new JScrollPane(new JLabel(new ImageIcon(sheet.spritesheet)));
        rawScroller.getVerticalScrollBar().setUnitIncrement(16);
        editorTabsHolder.add("Raw image", rawScroller);
    }

    private Thread animator = new Thread();
    private boolean animate = true;
    private JLabel iconLabel;
    private List<ImageIcon> icons = null;

    public void updateView(JPanel pane) {
        Spritesheet sheet = (Spritesheet) target;
        pane.removeAll();
        pane.setLayout(new BorderLayout());
        if (sheet.animated) {
            iconLabel = new JLabel();
            iconLabel.setBackground(Color.WHITE);
            if (icons == null) {
                icons = new ArrayList<ImageIcon>();
            } else {
                icons.clear();
            }
            int i = 0;
            Image img;
            while ((img = sheet.getImage(i++)) != null) {
                icons.add(new ImageIcon(img));
            }
            if (i > 0) {
                iconLabel.setIcon(icons.get(0));
            }
            pane.add(iconLabel, BorderLayout.CENTER);
            resetAnimator();
        } else {
            JTable spritesTable = new JTable(new SpritesheetTableModel(sheet));
            spritesTable.setDefaultRenderer(Image.class, new SpritesheetCellRenderer(sheet));
            spritesTable.setCellSelectionEnabled(true);
            spritesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            spritesTable.getTableHeader().setVisible(false);
            Enumeration<TableColumn> columns = spritesTable.getColumnModel().getColumns();
            TableColumn col;
            while (columns.hasMoreElements()) {
                col = columns.nextElement();
                col.setMinWidth(sheet.spriteWidth + 4);
                col.setMaxWidth(sheet.spriteWidth + 4);
            }
            spritesTable.setRowHeight(sheet.spriteHeight + 4);
            pane.add(new JScrollPane(spritesTable), BorderLayout.CENTER);
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JList addBacklinksList(JPanel pane, Spritesheet sheet) {
        final JList list = new JList(new SpritesheetsBacklinksListModel(sheet));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (list.getSelectedValue() instanceof TMXMap) {
                        ATContentStudio.frame.openEditor((TMXMap) list.getSelectedValue());
                        ATContentStudio.frame.selectInTree((TMXMap) list.getSelectedValue());
                    } else if (list.getSelectedValue() instanceof GameDataElement) {
                        ATContentStudio.frame.openEditor((GameDataElement) list.getSelectedValue());
                        ATContentStudio.frame.selectInTree((GameDataElement) list.getSelectedValue());
                    }
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (list.getSelectedValue() instanceof TMXMap) {
                        ATContentStudio.frame.openEditor((TMXMap) list.getSelectedValue());
                        ATContentStudio.frame.selectInTree((TMXMap) list.getSelectedValue());
                    } else if (list.getSelectedValue() instanceof GameDataElement) {
                        ATContentStudio.frame.openEditor((GameDataElement) list.getSelectedValue());
                        ATContentStudio.frame.selectInTree((GameDataElement) list.getSelectedValue());
                    }
                }
            }
        });
        list.setCellRenderer(new BacklinkCellRenderer(true));
        JScrollPane scroller = new JScrollPane(list);
        scroller.setBorder(BorderFactory.createTitledBorder("Elements pointing to this spritesheet."));
        pane.add(scroller, JideBoxLayout.FIX);
        return list;
    }

    public static class SpritesheetTableModel implements TableModel {

        Spritesheet sheet;

        public SpritesheetTableModel(Spritesheet sheet) {
            this.sheet = sheet;
        }

        @Override
        public int getRowCount() {
            return (sheet.spritesheet.getHeight() / sheet.spriteHeight) + ((sheet.spritesheet.getHeight() % sheet.spriteHeight) == 0 ? 0 : 1);
        }

        @Override
        public int getColumnCount() {
            return (sheet.spritesheet.getWidth() / sheet.spriteWidth) + ((sheet.spritesheet.getWidth() % sheet.spriteWidth) == 0 ? 0 : 1);
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return Image.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return sheet.getImage((rowIndex * getColumnCount()) + columnIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        }

        List<TableModelListener> listeners = new CopyOnWriteArrayList<TableModelListener>();

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }

    }

    public static class SpritesheetCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = -4213756343124247612L;
        Spritesheet sheet;

        public SpritesheetCellRenderer(Spritesheet sheet) {
            super();
            this.sheet = sheet;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel) {
                ((JLabel) c).setText("");
                if (value != null) {
                    ((JLabel) c).setIcon(new ImageIcon((Image) value));
                    ((JLabel) c).setToolTipText(sheet.id + ":" + ((row * table.getColumnCount()) + column));
                }

            }
            return c;
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        animate = aFlag;
        if (aFlag && animator != null) {
            resetAnimator();
        }
    }

    private void resetAnimator() {
        new Thread() {
            public void run() {
                if (animator != null && animator.isAlive()) {
                    try {
                        animator.join();
                    } catch (InterruptedException e) {
                    }
                }
                animate = true;
                animator = new Thread() {
                    public void run() {
                        int i = -1;
                        while (animate) {
                            if (icons != null) {
                                synchronized (icons) {
                                    i = (i + 1) % icons.size();
                                    iconLabel.setIcon(icons.get(i));
                                }
                                iconLabel.revalidate();
                            }
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                };
                animator.start();
            }
        }.start();
    }


    public static class SpritesheetsBacklinksListModel implements ListenerListModel<ProjectTreeNode> {

        Spritesheet sheet;

        public SpritesheetsBacklinksListModel(Spritesheet sheet) {
            this.sheet = sheet;
        }

        @Override
        public int getSize() {
            return sheet.getBacklinks().size();
        }

        @Override
        public ProjectTreeNode getElementAt(int index) {
            for (ProjectTreeNode node : sheet.getBacklinks()) {
                if (index == 0) return node;
                index--;
            }
            return null;
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }
    }

    public static class BacklinkCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;

        private boolean includeType;

        public BacklinkCellRenderer(boolean includeType) {
            super();
            this.includeType = includeType;

        }

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("none");
            } else {
                if (includeType && ((ProjectTreeNode) value).getDataType() != null) {
                    label.setText(((ProjectTreeNode) value).getDataType().toString() + "/" + ((ProjectTreeNode) value).getDesc());
                } else {
                    label.setText(((ProjectTreeNode) value).getDesc());
                }
                if (((ProjectTreeNode) value).getIcon() == null) {
                    Notification.addError("Unable to find icon for " + ((ProjectTreeNode) value).getDesc());
                } else {
                    label.setIcon(new ImageIcon(((ProjectTreeNode) value).getIcon()));
                }
            }
            return label;
        }

    }

    @Override
    public void targetUpdated() {
        this.icon = new ImageIcon(((Spritesheet) target).getIcon(0));
        this.name = ((Spritesheet) target).id;
    }

    public class SpritesheetFieldUpdater implements FieldUpdateListener {
        @Override
        public void valueChanged(JComponent source, Object value) {
            Spritesheet sheet = (Spritesheet) target;
            if (source == widthField) {
                sheet.spriteWidth = (Integer) value;
                sheet.clearCache();
                updateView(spriteViewPane);
                spriteViewPane.revalidate();
                spriteViewPane.repaint();
            } else if (source == heightField) {
                sheet.spriteHeight = (Integer) value;
                sheet.clearCache();
                updateView(spriteViewPane);
                spriteViewPane.revalidate();
                spriteViewPane.repaint();
            } else if (source == animatedBox) {
                sheet.animated = (Boolean) value;
                if (!sheet.animated) {
                    animate = false;
                }
                updateView(spriteViewPane);
                spriteViewPane.revalidate();
                spriteViewPane.repaint();
            } else if (source == categoryBox) {
                sheet.category = (Spritesheet.Category) value;
            }
            sheet.save();
        }
    }

}
