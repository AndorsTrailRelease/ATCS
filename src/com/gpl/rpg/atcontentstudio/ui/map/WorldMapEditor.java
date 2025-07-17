package com.gpl.rpg.atcontentstudio.ui.map;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import com.gpl.rpg.atcontentstudio.ui.*;
import com.jidesoft.swing.ComboBoxSearchable;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideTabbedPane;
import com.jidesoft.swing.ListSearchable;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class WorldMapEditor extends Editor implements FieldUpdateListener {

    private static final long serialVersionUID = -8358238912588729094L;


    private RSyntaxTextArea editorPane;

    public EditMode editMode = EditMode.moveViewSelect;

    public enum EditMode {
        moveViewSelect,
        moveMaps,
        addMap
    }

    public String mapBeingAddedID = null;
    WorldMapView mapView = null;
    WorldmapSegment.NamedArea selectedLabel = null;

    MapSegmentMapsListModel msmListModel = null;
    ListSelectionModel msmListSelectionModel = null;

    MapSegmentLabelsListModel mslListModel = null;

    MapSegmentLabelMapsListModel mslmListModel = null;
    ListSelectionModel mslmListSelectionModel = null;

    ListModel<TMXMap> currentSelectionListModel = null;
    ListSelectionModel currentSelectionSelectionModel = null;
    ListModel<TMXMap> currentHighlightListModel = null;

    JList<TMXMap> mapsShown;
    JList<WorldmapSegment.NamedArea> labelList;

    JTextField labelIdField;
    JTextField labelNameField;
    JTextField labelTypeField;

    public WorldMapEditor(WorldmapSegment worldmap) {
        target = worldmap;
        this.name = worldmap.getDesc();
        this.icon = new ImageIcon(worldmap.getIcon());
        setLayout(new BorderLayout());

        JideTabbedPane editorTabsHolder = new JideTabbedPane(JideTabbedPane.BOTTOM);
        editorTabsHolder.setTabShape(JideTabbedPane.SHAPE_FLAT);
        editorTabsHolder.setUseDefaultShowCloseButtonOnTab(false);
        editorTabsHolder.setShowCloseButtonOnTab(false);
        add(editorTabsHolder, BorderLayout.CENTER);

        editorTabsHolder.add("Map", buildSegmentTab(worldmap));

        JScrollPane xmlScroller = new JScrollPane(getXmlEditorPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        xmlScroller.getVerticalScrollBar().setUnitIncrement(16);
        editorTabsHolder.add("XML", xmlScroller);
    }

    @Override
    public void targetUpdated() {
        this.name = ((GameDataElement) target).getDesc();
        updateMessage();
        updateXmlViewText(((WorldmapSegment) target).toXml());
        mapView.updateFromModel();
    }

    public JPanel getXmlEditorPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));
        editorPane = new RSyntaxTextArea();
        editorPane.setText(((WorldmapSegment) target).toXml());
        editorPane.setEditable(false);
        editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        editorPane.setFont(editorPane.getFont().deriveFont(ATContentStudio.SCALING * editorPane.getFont().getSize()));
        pane.add(editorPane, JideBoxLayout.VARY);

        return pane;
    }

    public void updateXmlViewText(String text) {
        editorPane.setText(text);
    }


    @SuppressWarnings("unchecked")
    private JPanel buildSegmentTab(final WorldmapSegment worldmap) {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS));

        addLabelField(pane, "Worldmap File: ", ((Worldmap) worldmap.getParent()).worldmapFile.getAbsolutePath());
        pane.add(createButtonPane(worldmap), JideBoxLayout.FIX);

        mapView = new WorldMapView(worldmap);
        JScrollPane mapScroller = new JScrollPane(mapView);
        final JViewport vPort = mapScroller.getViewport();

        final JSlider zoomSlider = new JSlider(WorldMapView.MIN_ZOOM, WorldMapView.MAX_ZOOM, (int) (mapView.zoomLevel / WorldMapView.ZOOM_RATIO));
        zoomSlider.setSnapToTicks(true);
        zoomSlider.setMinorTickSpacing(WorldMapView.INC_ZOOM);
        zoomSlider.setOrientation(JSlider.VERTICAL);
        JPanel zoomSliderPane = new JPanel();
        zoomSliderPane.setLayout(new JideBoxLayout(zoomSliderPane, JideBoxLayout.PAGE_AXIS));
        zoomSliderPane.add(zoomSlider, JideBoxLayout.VARY);
        zoomSliderPane.add(new JLabel(new ImageIcon(DefaultIcons.getZoomIcon())), JideBoxLayout.FIX);


        if (target.writable) {
            JPanel mapToolsPane = new JPanel();
            mapToolsPane.setLayout(new JideBoxLayout(mapToolsPane, JideBoxLayout.LINE_AXIS));
            ButtonGroup mapToolsGroup = new ButtonGroup();
            JRadioButton moveView = new JRadioButton("Select maps");
            mapToolsGroup.add(moveView);
            mapToolsPane.add(moveView, JideBoxLayout.FIX);
            JRadioButton moveMaps = new JRadioButton("Move selected map(s)");
            mapToolsGroup.add(moveMaps);
            mapToolsPane.add(moveMaps, JideBoxLayout.FIX);
            JRadioButton addMap = new JRadioButton("Add map");
            mapToolsGroup.add(addMap);
            mapToolsPane.add(addMap, JideBoxLayout.FIX);
            final GDEComboModel<TMXMap> mapComboModel = new GDEComboModel<TMXMap>(worldmap.getProject(), null) {
                private static final long serialVersionUID = 2638082961277241764L;

                @Override
                public TMXMap getTypedElementAt(int index) {
                    return project.getMap(index);
                }

                @Override
                public int getSize() {
                    return project.getMapCount() + 1;
                }
            };
            final MyComboBox mapBox = new MyComboBox(TMXMap.class, mapComboModel);
            mapBox.setRenderer(new GDERenderer(false, false));
            new ComboBoxSearchable(mapBox) {
                @Override
                protected String convertElementToString(Object object) {
                    if (object == null) return "none";
                    else return ((GameDataElement) object).getDesc();
                }
            };
            mapBox.setEnabled(false);
            mapToolsPane.add(mapBox, JideBoxLayout.FIX);

            mapToolsPane.add(new JPanel(), JideBoxLayout.VARY);
            moveView.setSelected(true);
            pane.add(mapToolsPane, JideBoxLayout.FIX);

            moveView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editMode = EditMode.moveViewSelect;
                    mapBox.setEnabled(false);
                    if (mapBeingAddedID != null) {
                        mapView.mapLocations.remove(mapBeingAddedID);
                        mapBeingAddedID = null;
                        mapView.revalidate();
                        mapView.repaint();
                    }
                }
            });

            moveMaps.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editMode = EditMode.moveMaps;
                    mapBox.setEnabled(false);
                    if (mapBeingAddedID != null) {
                        mapView.mapLocations.remove(mapBeingAddedID);
                        mapBeingAddedID = null;
                        mapView.revalidate();
                        mapView.repaint();
                    }
                }
            });

            addMap.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editMode = EditMode.addMap;
                    mapBox.setEnabled(true);
                    if (mapBox.getSelectedItem() != null) {
                        mapBeingAddedID = ((TMXMap) mapBox.getSelectedItem()).id;
                    }
                }
            });

            mapBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (mapBox.getSelectedItem() == null) {
                        mapBeingAddedID = null;
                    } else {
                        if (mapBeingAddedID != null) {
                            mapView.updateFromModel();
                        }
                        mapBeingAddedID = ((TMXMap) mapBox.getSelectedItem()).id;
                        if (mapView.mapLocations.isEmpty()) {
                            TMXMap map = target.getProject().getMap(mapBeingAddedID);
                            int w = map.tmxMap.getWidth() * WorldMapView.TILE_SIZE;
                            int h = map.tmxMap.getHeight() * WorldMapView.TILE_SIZE;
                            mapView.mapLocations.put(mapBeingAddedID, new Rectangle(0, 0, w, h));
                            mapView.recomputeSize();
                            mapView.revalidate();
                            mapView.repaint();
                        }
                    }
                }
            });

        }

        JPanel mapZoomPane = new JPanel();
        mapZoomPane.setLayout(new BorderLayout());
        mapZoomPane.add(zoomSliderPane, BorderLayout.WEST);
        mapZoomPane.add(mapScroller, BorderLayout.CENTER);

        JPanel mapPropsPane = new JPanel();
        buildMapPropsPane(mapPropsPane, worldmap);

        setCurrentSelectionModel(msmListModel, msmListSelectionModel);


        final JSplitPane mapAndPropsSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapZoomPane, mapPropsPane);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                mapAndPropsSplitter.setDividerLocation(0.8d);
            }
        });
        pane.add(mapAndPropsSplitter, JideBoxLayout.VARY);

        zoomSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                Rectangle view = vPort.getViewRect();

                float oldZoomLevel = mapView.zoomLevel;
                mapView.zoomLevel = zoomSlider.getValue() * WorldMapView.ZOOM_RATIO;

                int newCenterX = (int) (view.getCenterX() / oldZoomLevel * mapView.zoomLevel);
                int newCenterY = (int) (view.getCenterY() / oldZoomLevel * mapView.zoomLevel);

                view.x = newCenterX - (view.width / 2);
                view.y = newCenterY - (view.height / 2);

                mapView.scrollRectToVisible(view);
                mapView.revalidate();
                mapView.repaint();
            }
        });

        MouseAdapter mouseListener = new MouseAdapter() {
            final int skipRecomputeDefault = 5;
            Point dragStart = null;
            int skipRecompute = 0;

            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedMap = null;
//				boolean update = false;
                int x = (int) (e.getX() / mapView.zoomLevel);
                int y = (int) (e.getY() / mapView.zoomLevel);
                for (String s : mapView.mapLocations.keySet()) {
                    if (mapView.mapLocations.get(s).contains(x, y)) {
                        selectedMap = s;
                        break;
                    }
                }
                if (editMode == EditMode.moveViewSelect) {
                    if (selectedMap == null) return;
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (e.isControlDown() || e.isShiftDown()) {
                            if (mapView.getSelectedMapsIDs().contains(selectedMap)) {
                                if (mapView.getSelectedMapsIDs().size() > 1) {
                                    removeFromSelection(selectedMap);
//									mapView.selected.remove(selectedMap);
//									update = true;
                                }
                            } else {
                                addToSelection(selectedMap);
//								mapView.selected.add(selectedMap);
//								update = true;
                            }
                        } else {
                            clearSelection();
//							mapView.selected.clear();
                            addToSelection(selectedMap);
//							mapView.selected.add(selectedMap);
//							update = true;
                        }
                    }
                } else if (editMode == EditMode.addMap && mapBeingAddedID != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        mapView.recomputeSize();
                        pushToModel();
                    }
                    mapView.updateFromModel();
//					update = true;
                    mapBeingAddedID = null;
                }
//				if (update) {
//					validateSelection();
//				}
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    dragStart = e.getLocationOnScreen();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragStart = null;
                if (editMode == EditMode.moveMaps) {
                    pushToModel();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (editMode == EditMode.addMap && mapBeingAddedID != null) {
                    int x = (int) (e.getX() / mapView.zoomLevel);
                    int y = (int) (e.getY() / mapView.zoomLevel);
                    TMXMap map = target.getProject().getMap(mapBeingAddedID);
                    int w = map.tmxMap.getWidth() * WorldMapView.TILE_SIZE;
                    int h = map.tmxMap.getHeight() * WorldMapView.TILE_SIZE;
                    x -= w / 2;
                    x -= x % WorldMapView.TILE_SIZE;
                    y -= h / 2;
                    y -= y % WorldMapView.TILE_SIZE;
                    mapView.mapLocations.put(mapBeingAddedID, new Rectangle(x, y, w, h));
                    if (--skipRecompute <= 0) {
                        if (mapView.recomputeSize()) {
                            skipRecompute = skipRecomputeDefault;
                        }
                    }
                    mapView.revalidate();
                    mapView.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStart == null) return;
                int deltaX = e.getXOnScreen() - dragStart.x;
                int deltaY = e.getYOnScreen() - dragStart.y;

                if (editMode != EditMode.moveMaps) {
                    Rectangle view = vPort.getViewRect();
                    view.setLocation(view.x - deltaX, view.y - deltaY);
                    mapView.scrollRectToVisible(view);

                    dragStart = e.getLocationOnScreen();
                } else {
                    int mapDeltaX = (int) ((deltaX / mapView.zoomLevel));
                    int mapDeltaY = (int) ((deltaY / mapView.zoomLevel));

                    mapDeltaX -= mapDeltaX % WorldMapView.TILE_SIZE;
                    mapDeltaY -= mapDeltaY % WorldMapView.TILE_SIZE;

                    for (String s : mapView.getSelectedMapsIDs()) {
                        mapView.mapLocations.get(s).x = (worldmap.mapLocations.get(s).x * WorldMapView.TILE_SIZE) + mapDeltaX;
                        mapView.mapLocations.get(s).y = (worldmap.mapLocations.get(s).y * WorldMapView.TILE_SIZE) + mapDeltaY;
                    }

                    if (--skipRecompute <= 0) {
                        if (mapView.recomputeSize()) {
                            skipRecompute = skipRecomputeDefault;
                        }
                    }

                    mapView.revalidate();
                    mapView.repaint();
                }
            }


        };

        mapView.addMouseListener(mouseListener);
        mapView.addMouseMotionListener(mouseListener);

        mapView.addMapClickListener(new WorldMapView.MapClickListener() {
            @Override
            public void mapClicked(MouseEvent e, TMXMap m) {
                if (e.getClickCount() == 2) {
                    ATContentStudio.frame.openEditor(m);
                }
            }

            @Override
            public void mapChangeClicked(MouseEvent e, TMXMap m, TMXMap changeTarget) {
                if (e.getClickCount() == 2) {
                    ATContentStudio.frame.openEditor(changeTarget);
                }
            }

            @Override
            public void backgroundClicked(MouseEvent e) {
            }
        });

        return pane;
    }


    private void buildMapPropsPane(JPanel mapPropsPane, final WorldmapSegment worldmap) {
        JideTabbedPane tabPane = new JideTabbedPane(JideTabbedPane.TOP);

        JPanel mapListPane = new JPanel();
        mapListPane.setLayout(new JideBoxLayout(mapListPane, JideBoxLayout.PAGE_AXIS));
        mapListPane.add(new JLabel("Maps shown here"), JideBoxLayout.FIX);
        msmListModel = new MapSegmentMapsListModel(worldmap);
        mapsShown = new JList<TMXMap>(msmListModel);
        mapsShown.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        msmListSelectionModel = mapsShown.getSelectionModel();
        mapsShown.setCellRenderer(new MapCellRenderer());
        new ListSearchable(mapsShown) {
            @Override
            protected String convertElementToString(Object object) {
                return ((TMXMap) object).id;
            }
        };
        mapListPane.add(new JScrollPane(mapsShown), JideBoxLayout.VARY);
        mapsShown.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    ATContentStudio.frame.openEditor(mapsShown.getSelectedValue());
                    ATContentStudio.frame.selectInTree(mapsShown.getSelectedValue());
                }
            }
        });
        mapsShown.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ATContentStudio.frame.openEditor(mapsShown.getSelectedValue());
                    ATContentStudio.frame.selectInTree(mapsShown.getSelectedValue());
                }
            }
        });
        mapsShown.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setCurrentSelectionModel(msmListModel, msmListSelectionModel);
            }
        });


        tabPane.addTab("Map list", mapListPane);

        final JPanel labelEditPane = new JPanel();
        labelEditPane.setLayout(new JideBoxLayout(labelEditPane, JideBoxLayout.PAGE_AXIS));
        labelEditPane.add(new JLabel("Labels on the worldmap"), JideBoxLayout.FIX);

        mslListModel = new MapSegmentLabelsListModel(worldmap);
        labelList = new JList<WorldmapSegment.NamedArea>(mslListModel);
        labelList.setCellRenderer(new MapLabelCellRenderer());
        labelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        labelEditPane.add(new JScrollPane(labelList), JideBoxLayout.FLEXIBLE);

        JPanel labelListButtonsPane = new JPanel();
        labelListButtonsPane.setLayout(new JideBoxLayout(labelListButtonsPane, JideBoxLayout.LINE_AXIS));
        final JButton createLabel = new JButton(new ImageIcon(DefaultIcons.getCreateIcon()));
        labelListButtonsPane.add(createLabel, JideBoxLayout.FIX);
        final JButton deleteLabel = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        labelListButtonsPane.add(deleteLabel, JideBoxLayout.FIX);
        labelListButtonsPane.add(new JPanel(), JideBoxLayout.VARY);
        labelEditPane.add(labelListButtonsPane, JideBoxLayout.FIX);

        final JPanel labelParametersPane = new JPanel();
        labelEditPane.add(labelParametersPane, JideBoxLayout.FLEXIBLE);

        labelList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedLabel = labelList.getSelectedValue();
                updateLabelParamsPane(labelParametersPane, worldmap);
                labelEditPane.revalidate();
                labelEditPane.repaint();
            }
        });

        createLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorldmapSegment.NamedArea creation = new WorldmapSegment.NamedArea(null, null, null);
                worldmap.labels.put(WorldmapSegment.TEMP_LABEL_KEY, creation);
                worldmap.labelledMaps.put(WorldmapSegment.TEMP_LABEL_KEY, new ArrayList<String>());
                mslListModel.listChanged();
                labelList.setSelectedValue(creation, true);
            }
        });

        deleteLabel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedLabel.id != null) {
                    worldmap.labelledMaps.remove(selectedLabel.id);
                    worldmap.labels.remove(selectedLabel.id);
                } else {
                    worldmap.labelledMaps.remove(WorldmapSegment.TEMP_LABEL_KEY);
                    worldmap.labels.remove(WorldmapSegment.TEMP_LABEL_KEY);
                }
                labelList.clearSelection();
                mslListModel.listChanged();
                notifyModelModified();
            }
        });


        tabPane.addTab("Labels", labelEditPane);

        mapPropsPane.setLayout(new BorderLayout());
        mapPropsPane.add(tabPane, BorderLayout.CENTER);
    }

    private void updateLabelParamsPane(JPanel labelParametersPane, final WorldmapSegment worldmap) {
        labelParametersPane.removeAll();
        if (selectedLabel == null) {
            setCurrentHighlightModel(null);
            return;
        }
        labelParametersPane.setLayout(new JideBoxLayout(labelParametersPane, JideBoxLayout.PAGE_AXIS));

        labelIdField = addTextField(labelParametersPane, "Internal ID: ", selectedLabel.id, worldmap.writable, this);
        labelNameField = addTranslatableTextField(labelParametersPane, "Name: ", selectedLabel.name, worldmap.writable, this);
        labelTypeField = addTextField(labelParametersPane, "Type: ", selectedLabel.type, worldmap.writable, this);


        labelParametersPane.add(new JLabel("Label covers the following maps"), JideBoxLayout.FIX);

        mslmListModel = new MapSegmentLabelMapsListModel(worldmap, selectedLabel);
        final JList<TMXMap> labelCoverageList = new JList<TMXMap>(mslmListModel);
        labelCoverageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        mslmListSelectionModel = labelCoverageList.getSelectionModel();
        labelCoverageList.setCellRenderer(new MapCellRenderer());
        labelParametersPane.add(new JScrollPane(labelCoverageList), JideBoxLayout.VARY);
        labelCoverageList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                setCurrentHighlightModel(mslmListModel);
            }
        });

        JPanel labelCoverageButtonsPane = new JPanel();
        labelCoverageButtonsPane.setLayout(new JideBoxLayout(labelCoverageButtonsPane, JideBoxLayout.LINE_AXIS));
        JButton addCoverage = new JButton("Add on-map selection");
        labelCoverageButtonsPane.add(addCoverage, JideBoxLayout.FIX);
        JButton replaceCoverage = new JButton("Replace by on-map selection");
        labelCoverageButtonsPane.add(replaceCoverage, JideBoxLayout.FIX);
        JButton removeFromCoverage = new JButton("Remove selected in list");
        labelCoverageButtonsPane.add(removeFromCoverage, JideBoxLayout.FIX);
        labelCoverageButtonsPane.add(new JPanel(), JideBoxLayout.VARY);

        addCoverage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedLabel == null) return;
                String labelId = selectedLabel.id;
                if (labelId == null) labelId = WorldmapSegment.TEMP_LABEL_KEY;

                List<String> currentCoverage = worldmap.labelledMaps.get(labelId);
                if (currentCoverage == null) {
                    worldmap.labelledMaps.put(labelId, new ArrayList<String>());
                    currentCoverage = worldmap.labelledMaps.get(labelId);
                }
                for (int i = 0; i < msmListModel.getSize(); i++) {
                    if (msmListSelectionModel.isSelectedIndex(i)) {
                        if (!currentCoverage.contains(msmListModel.getElementAt(i).id)) {
                            currentCoverage.add(msmListModel.getElementAt(i).id);
                        }
                    }
                }
                mslmListModel.listChanged();
                repaintMap();
            }
        });

        replaceCoverage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedLabel == null) return;
                String labelId = selectedLabel.id;
                if (labelId == null) labelId = WorldmapSegment.TEMP_LABEL_KEY;

                List<String> currentCoverage = worldmap.labelledMaps.get(labelId);
                if (currentCoverage == null) {
                    worldmap.labelledMaps.put(labelId, new ArrayList<String>());
                    currentCoverage = worldmap.labelledMaps.get(labelId);
                } else {
                    currentCoverage.clear();
                }
                for (int i = 0; i < msmListModel.getSize(); i++) {
                    if (msmListSelectionModel.isSelectedIndex(i)) {
                        if (!currentCoverage.contains(msmListModel.getElementAt(i).id)) {
                            currentCoverage.add(msmListModel.getElementAt(i).id);
                        }
                    }
                }
                mslmListModel.listChanged();
                repaintMap();
            }
        });

        removeFromCoverage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedLabel == null) return;
                String labelId = selectedLabel.id;
                if (labelId == null) labelId = WorldmapSegment.TEMP_LABEL_KEY;

                List<String> currentCoverage = worldmap.labelledMaps.get(labelId);
                if (currentCoverage == null) return;
                List<String> toRemove = new ArrayList<String>();
                for (int i = 0; i < mslmListModel.getSize(); i++) {
                    if (mslmListSelectionModel.isSelectedIndex(i)) {
                        if (currentCoverage.contains(mslmListModel.getElementAt(i).id)) {
                            toRemove.add(mslmListModel.getElementAt(i).id);
                        }
                    }
                }
                currentCoverage.removeAll(toRemove);
                mslmListModel.listChanged();
                repaintMap();
            }
        });

        labelParametersPane.add(labelCoverageButtonsPane, JideBoxLayout.FIX);
        setCurrentHighlightModel(mslmListModel);

    }

    public class MapSegmentMapsListModel implements ListenerListModel<TMXMap> {

        WorldmapSegment segment;

        public MapSegmentMapsListModel(WorldmapSegment segment) {
            this.segment = segment;
        }

        @Override
        public int getSize() {
            return segment.mapLocations.size();
        }

        @Override
        public TMXMap getElementAt(int index) {
            return segment.getProject().getMap(((String) segment.mapLocations.keySet().toArray()[index]));
        }

        public void listChanged() {
            fireListChanged();
        }

        List<ListDataListener> listeners = new ArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }
    }

    public class MapSegmentLabelMapsListModel implements ListenerListModel<TMXMap> {
        WorldmapSegment segment;
        WorldmapSegment.NamedArea area;

        public MapSegmentLabelMapsListModel(WorldmapSegment segment, WorldmapSegment.NamedArea area) {
            this.segment = segment;
            this.area = area;
        }

        @Override
        public int getSize() {
            if (area.id == null) return segment.labelledMaps.get(WorldmapSegment.TEMP_LABEL_KEY).size();
            return segment.labelledMaps.get(area.id).size();
        }

        @Override
        public TMXMap getElementAt(int index) {
            if (area.id == null)
                return segment.getProject().getMap(segment.labelledMaps.get(WorldmapSegment.TEMP_LABEL_KEY).get(index));
            return segment.getProject().getMap(segment.labelledMaps.get(area.id).get(index));
        }

        public void listChanged() {
            fireListChanged();
        }

        List<ListDataListener> listeners = new ArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }
    }

    public class MapSegmentLabelsListModel implements ListenerListModel<WorldmapSegment.NamedArea> {

        WorldmapSegment segment;

        public MapSegmentLabelsListModel(WorldmapSegment segment) {
            this.segment = segment;
        }

        @Override
        public int getSize() {
            return segment.labels.values().size();
        }

        @Override
        public WorldmapSegment.NamedArea getElementAt(int index) {
            return new ArrayList<WorldmapSegment.NamedArea>(segment.labels.values()).get(index);
        }

        public void listChanged() {
            fireListChanged();
        }

        List<ListDataListener> listeners = new ArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }

    }

    public static class MapCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;


        public MapCellRenderer() {
            super();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("None");
            } else {
                label.setText(((GameDataElement) value).getDesc());
                if (((GameDataElement) value).getIcon() == null) {
                    Notification.addError("Unable to find icon for " + ((GameDataElement) value).getDesc());
                } else {
                    label.setIcon(new ImageIcon(((GameDataElement) value).getIcon()));
                }
            }
            return label;
        }

    }

    public static class MapLabelCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;


        public MapLabelCellRenderer() {
            super();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("None");
            } else {
                WorldmapSegment.NamedArea area = (WorldmapSegment.NamedArea) value;
                if (area.id != null) {
                    label.setText(area.name + " (" + area.id + ")");
                    label.setIcon(new ImageIcon(DefaultIcons.getLabelIcon()));
                } else {
                    label.setText("Incomplete Label. Enter an ID.");
                    label.setIcon(new ImageIcon(DefaultIcons.getNullifyIcon()));
                }
            }
            return label;
        }

    }

    public JPanel createButtonPane(final WorldmapSegment node) {
        final JButton gdeIcon = new JButton(new ImageIcon(DefaultIcons.getUIMapImage()));
        JPanel savePane = new JPanel();
        savePane.add(gdeIcon, JideBoxLayout.FIX);
        savePane.setLayout(new JideBoxLayout(savePane, JideBoxLayout.LINE_AXIS, 6));
        if (node.writable) {
            if (node.getDataType() == GameSource.Type.altered) {
                savePane.add(message = new JLabel(ALTERED_MESSAGE), JideBoxLayout.FIX);
            } else if (node.getDataType() == GameSource.Type.created) {
                savePane.add(message = new JLabel(CREATED_MESSAGE), JideBoxLayout.FIX);
            }
            JButton save = new JButton(SAVE);
            save.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (node.getParent() instanceof Worldmap) {
                        if (node.state != GameDataElement.State.saved) {
                            final List<SaveEvent> events = node.attemptSave();
                            if (events == null) {
                                ATContentStudio.frame.nodeChanged(node);
                            } else {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        new SaveItemsWizard(events, node).setVisible(true);
                                    }
                                }.start();
                            }
                        }
                    }
                }
            });
            savePane.add(save, JideBoxLayout.FIX);
            JButton delete = new JButton(DELETE);
            if (node.getDataType() == GameSource.Type.altered) {
                delete.setText(REVERT);
            }
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ATContentStudio.frame.closeEditor(node);
                    node.childrenRemoved(new ArrayList<ProjectTreeNode>());
                    if (node.getParent() instanceof Worldmap) {
                        ((Worldmap) node.getParent()).remove(node);
                        node.save();
                        for (GameDataElement backlink : node.getBacklinks()) {
                            backlink.elementChanged(node, node.getProject().getWorldmapSegment(node.id));
                        }
                    }
                }
            });
            savePane.add(delete, JideBoxLayout.FIX);
        } else {
            if (node.getProject().alteredContent.getWorldmapSegment(node.id) != null) {
                savePane.add(message = new JLabel(ALTERED_EXISTS_MESSAGE), JideBoxLayout.FIX);
                JButton makeWritable = new JButton("Go to altered");
                makeWritable.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (node.getProject().getWorldmapSegment(node.id) != node) {
                            ATContentStudio.frame.openEditor(node.getProject().getWorldmapSegment(node.id));
                            ATContentStudio.frame.closeEditor(node);
                            ATContentStudio.frame.selectInTree(node.getProject().getWorldmapSegment(node.id));
                        }
                    }
                });
                savePane.add(makeWritable, JideBoxLayout.FIX);

            } else {
                savePane.add(message = new JLabel(READ_ONLY_MESSAGE), JideBoxLayout.FIX);
                JButton makeWritable = new JButton("Alter");
                makeWritable.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (node.getProject().getWorldmapSegment(node.id) == node) {
                            node.getProject().makeWritable(node);
                        }
                        if (node.getProject().getWorldmapSegment(node.id) != node) {
                            ATContentStudio.frame.openEditor(node.getProject().getWorldmapSegment(node.id));
                            ATContentStudio.frame.closeEditor(node);
                            ATContentStudio.frame.selectInTree(node.getProject().getWorldmapSegment(node.id));
                        }
                        updateMessage();
                    }
                });
                savePane.add(makeWritable, JideBoxLayout.FIX);
            }
        }
        JButton prev = new JButton(new ImageIcon(DefaultIcons.getArrowLeftIcon()));
        JButton next = new JButton(new ImageIcon(DefaultIcons.getArrowRightIcon()));
        savePane.add(prev, JideBoxLayout.FIX);
        savePane.add(next, JideBoxLayout.FIX);
        if (node.getParent().getIndex(node) == 0) {
            prev.setEnabled(false);
        }
        if (node.getParent().getIndex(node) == node.getParent().getChildCount() - 1) {
            next.setEnabled(false);
        }
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectTreeNode prevNode = (ProjectTreeNode) node.getParent().getChildAt(node.getParent().getIndex(node) - 1);
                if (prevNode != null && prevNode instanceof GameDataElement) {
                    ATContentStudio.frame.openEditor((GameDataElement) prevNode);
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectTreeNode nextNode = (ProjectTreeNode) node.getParent().getChildAt(node.getParent().getIndex(node) + 1);
                if (nextNode != null && nextNode instanceof GameDataElement) {
                    ATContentStudio.frame.openEditor((GameDataElement) nextNode);
                }
            }
        });
        final JButton bookmark = new JButton(new ImageIcon(node.bookmark != null ? DefaultIcons.getBookmarkActiveIcon() : DefaultIcons.getBookmarkInactiveIcon()));
        savePane.add(bookmark, JideBoxLayout.FIX);
        bookmark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (node.bookmark == null) {
                    node.getProject().bookmark(node);
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkActiveIcon()));
                } else {
                    node.bookmark.delete();
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkInactiveIcon()));
                }
            }
        });
        //Placeholder. Fills the eventual remaining space.
        savePane.add(new JPanel(), JideBoxLayout.VARY);
        return savePane;
    }

    public void updateMessage() {

        //TODO make this a full update of the button panel.
        WorldmapSegment node = (WorldmapSegment) target;
        if (node.writable) {
            if (node.getDataType() == GameSource.Type.altered) {
                message.setText(ALTERED_MESSAGE);
            } else if (node.getDataType() == GameSource.Type.created) {
                message.setText(CREATED_MESSAGE);
            }
        } else if (node.getProject().alteredContent.getWorldmapSegment(node.id) != null) {
            message.setText(ALTERED_EXISTS_MESSAGE);
        } else {
            message.setText(READ_ONLY_MESSAGE);
        }
        message.revalidate();
        message.repaint();
    }

    public void pushToModel() {
        mapView.pushToModel();
        msmListModel.listChanged();
        notifyModelModified();
        updateXmlViewText(((WorldmapSegment) target).toXml());
    }

    public void notifyModelModified() {
        target.state = GameDataElement.State.modified;
        target.childrenChanged(new ArrayList<ProjectTreeNode>());
    }

    ListSelectionListener activeSelectionListSelectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            repaintMap();
        }

    };

    private void setCurrentSelectionModel(ListModel<TMXMap> listModel, ListSelectionModel listSelectionModel) {
        if (currentSelectionSelectionModel != null) {
            currentSelectionSelectionModel.removeListSelectionListener(activeSelectionListSelectionListener);
        }
        currentSelectionListModel = listModel;
        currentSelectionSelectionModel = listSelectionModel;
        mapView.selectedListModel = listModel;
        mapView.selectedSelectionModel = listSelectionModel;
        currentSelectionSelectionModel.addListSelectionListener(activeSelectionListSelectionListener);
        repaintMap();
    }

    private void setCurrentHighlightModel(ListModel<TMXMap> listModel) {
        mapView.highlightedListModel = listModel;
        repaintMap();
    }


    public void clearSelection() {
        currentSelectionSelectionModel.clearSelection();
    }

    public void addToSelection(String mapId) {
        if (mapId == null) return;
        int index = -1;
        for (int i = 0; i < currentSelectionListModel.getSize(); i++) {
            if (currentSelectionListModel.getElementAt(i).id.equals(mapId)) {
                index = i;
                break;
            }
        }
        currentSelectionSelectionModel.addSelectionInterval(index, index);
    }

    public void removeFromSelection(String mapId) {
        if (mapId == null) return;
        int index = -1;
        for (int i = 0; i < currentSelectionListModel.getSize(); i++) {
            if (currentSelectionListModel.getElementAt(i).id.equals(mapId)) {
                index = i;
                break;
            }
        }
        currentSelectionSelectionModel.removeSelectionInterval(index, index);
    }

    public void repaintMap() {
        mapView.revalidate();
        mapView.repaint();
    }

    @Override
    public void valueChanged(JComponent source, Object value) {
        WorldmapSegment worldmap = (WorldmapSegment) target;
        boolean changed = false;
        if (source == labelIdField) {
            List<String> coverage;
            if (selectedLabel.id != null) {
                coverage = worldmap.labelledMaps.get(selectedLabel.id);
                worldmap.labelledMaps.remove(selectedLabel.id);
                worldmap.labels.remove(selectedLabel.id);
            } else {
                coverage = worldmap.labelledMaps.get(WorldmapSegment.TEMP_LABEL_KEY);
                worldmap.labels.remove(WorldmapSegment.TEMP_LABEL_KEY);
            }
            selectedLabel.id = (String) value;
            if (value != null) {
                worldmap.labelledMaps.put(selectedLabel.id, coverage);
                worldmap.labels.put(selectedLabel.id, selectedLabel);
            }
            mslListModel.listChanged();
            changed = true;
        } else if (source == labelNameField) {
            selectedLabel.name = (String) value;
            mslListModel.listChanged();
            changed = true;
            repaintMap();
        } else if (source == labelTypeField) {
            selectedLabel.type = (String) value;
            changed = true;
        }

        if (changed) {
            notifyModelModified();
            updateXmlViewText(worldmap.toXml());
        }
    }

}
