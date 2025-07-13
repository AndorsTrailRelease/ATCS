package com.gpl.rpg.atcontentstudio.ui.map;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.*;
import com.gpl.rpg.atcontentstudio.model.maps.*;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.ui.*;
import com.gpl.rpg.atcontentstudio.utils.DesktopIntegration;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideTabbedPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import tiled.view.MapRenderer;
import tiled.view.OrthogonalRenderer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TMXMapEditor extends Editor implements TMXMap.MapChangedOnDiskListener {

    private static final long serialVersionUID = -3079451876618342442L;


    Map<String, JPanel> editorTabs = new LinkedHashMap<String, JPanel>();
    JideTabbedPane editorTabsHolder;

    private JButton reload;

    private RSyntaxTextArea editorPane;

    private IntegerBasedCheckBox outsideBox;
    @SuppressWarnings("rawtypes")
    private JComboBox colorFilterBox;
    private LayerListModel layerListModel;
    @SuppressWarnings("rawtypes")
    private JList layerList;
    private tiled.core.MapLayer selectedLayer;
    private JButton addTileLayer;
    private JButton addObjectGroup;
    private JButton deleteLayer;

    private JPanel layerDetailsPane;
    private BooleanBasedCheckBox layerVisibleBox;
    private JCheckBox groupActiveForNewGame;
    private JTextField layerNameField;
    private MapObjectsListModel groupObjectsListModel;
    @SuppressWarnings("rawtypes")
    private JList groupObjectsList;
    private MapObject selectedMapObject;
    private JButton addMapchange;
    private JButton addSpawn;
    private JButton addRest;
    private JButton addKey;
    private JButton addReplace;
    private JButton addScript;
    private JButton addContainer;
    private JButton addSign;
    private JButton deleteObject;

    private JPanel mapObjectSettingsPane;
    @SuppressWarnings("rawtypes")
    private JComboBox droplistBox;
    @SuppressWarnings("rawtypes")
    private JComboBox dialogueBox;
    @SuppressWarnings("rawtypes")
    private JComboBox mapBox;
    private JTextField areaField;
    @SuppressWarnings("rawtypes")
    private JComboBox targetAreaCombo;
    @SuppressWarnings("rawtypes")
    private JComboBox evaluateTriggerBox;
    private JSpinner quantityField;
    private JSpinner respawnSpeedField;
    private JCheckBox spawnActiveForNewGame;
    private JCheckBox spawnIgnoreAreas;
    private JTextField spawngroupField;
    @SuppressWarnings("rawtypes")
    private JList npcList;
    private SpawnGroupNpcListModel npcListModel;

    @SuppressWarnings("rawtypes")
    private JComboBox requirementTypeCombo;
    private JPanel requirementParamsPane;
    @SuppressWarnings("rawtypes")
    private JComboBox requirementObj;
    private JComponent requirementObjId;
    private JComponent requirementValue;
    private BooleanBasedCheckBox requirementNegated;

    @SuppressWarnings("rawtypes")
    private JList replacementsList;
    private ReplacementsListModel replacementsListModel;
    private ReplaceArea.Replacement selectedReplacement;
    private JButton addReplacement;
    private JButton deleteReplacement;
    private JPanel replacementEditPane;
    @SuppressWarnings("rawtypes")
    private JComboBox sourceLayer;
    @SuppressWarnings("rawtypes")
    private JComboBox targetLayer;

    private TMXViewer tmxViewer;

    public TMXMapEditor(TMXMap map) {
        this.target = map;
        this.name = map.getDesc();
        this.icon = new ImageIcon(DefaultIcons.getTiledIconIcon());

        map.addMapChangedOnDiskListener(this);

        setLayout(new BorderLayout());
        editorTabsHolder = new JideTabbedPane(JideTabbedPane.BOTTOM);
        editorTabsHolder.setTabShape(JideTabbedPane.SHAPE_FLAT);
        editorTabsHolder.setUseDefaultShowCloseButtonOnTab(false);
        editorTabsHolder.setShowCloseButtonOnTab(false);
        add(editorTabsHolder, BorderLayout.CENTER);

        JScrollPane tmxScroller = new JScrollPane(getTmxEditorPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane xmlScroller = new JScrollPane(getXmlEditorPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //JScrollPane replScroller = new JScrollPane(getReplacementSimulatorPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        xmlScroller.getVerticalScrollBar().setUnitIncrement(16);
        editorTabsHolder.add("TMX", tmxScroller);
        editorTabsHolder.add("XML", xmlScroller);
        //editorTabsHolder.add("Replacements", replScroller);
        editorTabsHolder.add("Testing", getReplacementSimulatorPane());

    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    public JPanel getTmxEditorPane() {
        final TMXMap map = (TMXMap) target;
        final FieldUpdateListener listener = new MapFieldUpdater();

        ScrollablePanel pane = new ScrollablePanel();
        pane.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));

        addLabelField(pane, "TMX File: ", ((TMXMap) target).tmxFile.getAbsolutePath());
        createButtonPane(pane, map.getProject(), map, listener);
        outsideBox = addIntegerBasedCheckBox(pane, "Map is outdoors", map.outside, map.writable, listener);
        colorFilterBox = addEnumValueBox(pane, "Color Filter", TMXMap.ColorFilter.values(), map.colorFilter, map.writable, listener);

        JSplitPane layersViewSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        layerListModel = new LayerListModel(map);
        layerList = new JList(layerListModel);
        layerList.setCellRenderer(new LayerListRenderer());
        layerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane layerListScroller = new JScrollPane(layerList);
        layerListScroller.getVerticalScrollBar().setUnitIncrement(16);
        layerList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedLayer = (tiled.core.MapLayer) layerList.getSelectedValue();
                selectedMapObject = null;
                if (selectedLayer != null && map.writable) {
                    deleteLayer.setEnabled(true);
                } else {
                    deleteLayer.setEnabled(false);
                }
                updateLayerDetailsPane(layerDetailsPane, selectedLayer, listener);
                listener.valueChanged(layerList, selectedLayer);
            }
        });
        JPanel layersListPane = new JPanel();
        layersListPane.setLayout(new JideBoxLayout(layersListPane, JideBoxLayout.PAGE_AXIS, 6));
        layersListPane.add(layerListScroller, JideBoxLayout.VARY);
        addTileLayer = new JButton(new ImageIcon(DefaultIcons.getCreateTileLayerIcon()));
        addTileLayer.setToolTipText("Create new tile layer (graphics layer).");
        addTileLayer.setEnabled(map.writable);
        addTileLayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tiled.core.TileLayer layer = new tiled.core.TileLayer(map.tmxMap.getWidth(), map.tmxMap.getHeight());
                layerListModel.addObject(layer);
                map.state = GameDataElement.State.modified;
                map.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                targetUpdated();
            }
        });
        addObjectGroup = new JButton(new ImageIcon(DefaultIcons.getCreateObjectGroupIcon()));
        addObjectGroup.setToolTipText("Create new object group.");
        addObjectGroup.setEnabled(map.writable);
        addObjectGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layerListModel.addObject(new tiled.core.ObjectGroup());
                map.state = GameDataElement.State.modified;
                map.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                targetUpdated();
            }
        });
        deleteLayer = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
        deleteLayer.setToolTipText("Delete selected layer/group.");
        deleteLayer.setEnabled(false);
        deleteLayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layerListModel.removeObject(selectedLayer);
                map.state = GameDataElement.State.modified;
                map.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                targetUpdated();
            }
        });
        JPanel layersButtonsPane = new JPanel();
        layersButtonsPane.setLayout(new JideBoxLayout(layersButtonsPane, JideBoxLayout.LINE_AXIS, 6));
        layersButtonsPane.add(addTileLayer, JideBoxLayout.FIX);
        layersButtonsPane.add(addObjectGroup, JideBoxLayout.FIX);
        layersButtonsPane.add(new JPanel(), JideBoxLayout.VARY);
        layersButtonsPane.add(deleteLayer, JideBoxLayout.FIX);
        layersListPane.add(layersButtonsPane, JideBoxLayout.FIX);
        layersViewSplitPane.setLeftComponent(layersListPane);
        layerDetailsPane = new JPanel();
        layerDetailsPane.setLayout(new JideBoxLayout(layerDetailsPane, JideBoxLayout.PAGE_AXIS, 6));
        layersViewSplitPane.setRightComponent(layerDetailsPane);
        pane.add(layersViewSplitPane, JideBoxLayout.FIX);

        tmxViewer = new TMXViewer(((TMXMap) target), listener);
        JScrollPane tmxScroller = new JScrollPane(tmxViewer);
        tmxScroller.getVerticalScrollBar().setUnitIncrement(16);
        tmxScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        tmxScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.add(tmxScroller, JideBoxLayout.FIX);

        addTMXMapSpritesheetsList(pane, ((TMXMap) target));

        addBacklinksList(pane, map);

        pane.add(new JPanel(), JideBoxLayout.VARY);
        return pane;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void updateLayerDetailsPane(JPanel pane, tiled.core.MapLayer selected, final FieldUpdateListener listener) {
        final TMXMap map = (TMXMap) target;
        pane.removeAll();
        if (selected == null) {
            return;
        } else if (selected instanceof tiled.core.TileLayer) {
            layerNameField = addTextField(pane, "Layer name: ", selected.getName(), map.writable, listener);
            layerVisibleBox = addBooleanBasedCheckBox(pane, "Visible", selected.isVisible(), true, listener);
            pane.add(new JPanel(), JideBoxLayout.VARY);
        } else if (selected instanceof tiled.core.ObjectGroup) {
            JSplitPane objectGroupDetailsSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            JPanel groupDetailPane = new JPanel();
            groupDetailPane.setLayout(new JideBoxLayout(groupDetailPane, JideBoxLayout.PAGE_AXIS, 6));
            objectGroupDetailsSplitter.setLeftComponent(groupDetailPane);
            layerNameField = addTextField(groupDetailPane, "Group name: ", selected.getName(), map.writable, listener);
            layerVisibleBox = addBooleanBasedCheckBox(groupDetailPane, "Visible", selected.isVisible(), true, listener);
            MapObjectGroup objGroup = null;
            for (MapObjectGroup group : map.groups) {
                if (group.tmxGroup == selected) {
                    objGroup = group;
                    break;
                }
            }
            groupActiveForNewGame = addBooleanBasedCheckBox(groupDetailPane, "Active for new game", objGroup.active, map.writable, listener);
            groupObjectsListModel = new MapObjectsListModel(objGroup);
            groupObjectsList = new JList(groupObjectsListModel);
            groupObjectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            groupObjectsList.setCellRenderer(new GroupObjectsRenderer());
            JScrollPane groupObjectsScroller = new JScrollPane(groupObjectsList);
            groupObjectsScroller.getVerticalScrollBar().setUnitIncrement(16);
            groupDetailPane.add(groupObjectsScroller, JideBoxLayout.VARY);
            groupObjectsList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectedMapObject = (MapObject) groupObjectsList.getSelectedValue();
                    updateMapObjectSettingsPane(mapObjectSettingsPane, selectedMapObject, listener);
                    listener.valueChanged(groupObjectsList, selectedMapObject);
                    if (selectedMapObject != null && map.writable) {
                        deleteObject.setEnabled(true);
                    } else {
                        deleteObject.setEnabled(false);
                    }
                }
            });

            addMapchange = new JButton(new ImageIcon(DefaultIcons.getCreateMapchangeIcon()));
            addMapchange.setToolTipText("Create new mapchange area.");
            addMapchange.setEnabled(map.writable);
            addMapchange.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newMapchange(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addSpawn = new JButton(new ImageIcon(DefaultIcons.getCreateSpawnareaIcon()));
            addSpawn.setToolTipText("Create new spawn area.");
            addSpawn.setEnabled(map.writable);
            addSpawn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newSpawnArea(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addRest = new JButton(new ImageIcon(DefaultIcons.getCreateRestIcon()));
            addRest.setToolTipText("Create new rest area.");
            addRest.setEnabled(map.writable);
            addRest.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newRest(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addKey = new JButton(new ImageIcon(DefaultIcons.getCreateKeyIcon()));
            addKey.setToolTipText("Create new key area.");
            addKey.setEnabled(map.writable);
            addKey.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newKey(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addReplace = new JButton(new ImageIcon(DefaultIcons.getCreateReplaceIcon()));
            addReplace.setToolTipText("Create new replace area.");
            addReplace.setEnabled(map.writable);
            addReplace.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newReplace(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addScript = new JButton(new ImageIcon(DefaultIcons.getCreateScriptIcon()));
            addScript.setToolTipText("Create new script area.");
            addScript.setEnabled(map.writable);
            addScript.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newScript(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addContainer = new JButton(new ImageIcon(DefaultIcons.getCreateContainerIcon()));
            addContainer.setToolTipText("Create new container.");
            addContainer.setEnabled(map.writable);
            addContainer.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newContainer(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            addSign = new JButton(new ImageIcon(DefaultIcons.getCreateSignIcon()));
            addSign.setToolTipText("Create new sign post.");
            addSign.setEnabled(map.writable);
            addSign.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.addObject(MapObject.newSign(new tiled.core.MapObject(0, 0, 32, 32), map));
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });
            deleteObject = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
            deleteObject.setToolTipText("Delete selected map object.");
            deleteObject.setEnabled(false);
            deleteObject.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupObjectsListModel.removeObject(selectedMapObject);
                    map.state = GameDataElement.State.modified;
                    map.childrenChanged(new ArrayList<ProjectTreeNode>());
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                    targetUpdated();
                }
            });

            JPanel groupButtonsPane1 = new JPanel();
            groupButtonsPane1.setLayout(new JideBoxLayout(groupButtonsPane1, JideBoxLayout.LINE_AXIS, 6));
            groupButtonsPane1.add(addMapchange, JideBoxLayout.FIX);
            groupButtonsPane1.add(addSpawn, JideBoxLayout.FIX);
            groupButtonsPane1.add(addRest, JideBoxLayout.FIX);
            groupButtonsPane1.add(addKey, JideBoxLayout.FIX);
            groupButtonsPane1.add(new JPanel(), JideBoxLayout.VARY);
            JPanel groupButtonsPane2 = new JPanel();
            groupButtonsPane2.setLayout(new JideBoxLayout(groupButtonsPane2, JideBoxLayout.LINE_AXIS, 6));
            groupButtonsPane2.add(addReplace, JideBoxLayout.FIX);
            groupButtonsPane2.add(addScript, JideBoxLayout.FIX);
            groupButtonsPane2.add(addContainer, JideBoxLayout.FIX);
            groupButtonsPane2.add(addSign, JideBoxLayout.FIX);
            groupButtonsPane2.add(new JPanel(), JideBoxLayout.VARY);
            groupButtonsPane2.add(deleteObject, JideBoxLayout.FIX);
            groupDetailPane.add(groupButtonsPane1, JideBoxLayout.FIX);
            groupDetailPane.add(groupButtonsPane2, JideBoxLayout.FIX);

            mapObjectSettingsPane = new JPanel();
            mapObjectSettingsPane.setLayout(new JideBoxLayout(mapObjectSettingsPane, JideBoxLayout.PAGE_AXIS, 6));
            JScrollPane mapObjectSettingsScroller = new JScrollPane(mapObjectSettingsPane);
            mapObjectSettingsScroller.getVerticalScrollBar().setUnitIncrement(16);
            objectGroupDetailsSplitter.setRightComponent(mapObjectSettingsScroller);
            pane.add(objectGroupDetailsSplitter, JideBoxLayout.VARY);
        }
        pane.revalidate();
        pane.repaint();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void updateMapObjectSettingsPane(JPanel pane, final MapObject selected, final FieldUpdateListener listener) {
        pane.removeAll();
        boolean needVary = true;
        if (selected instanceof ContainerArea) {
            droplistBox = addDroplistBox(pane, ((TMXMap) target).getProject(), "Droplist: ", ((ContainerArea) selected).droplist, ((TMXMap) target).writable, listener);
        } else if (selected instanceof KeyArea) {
            areaField = addTextField(pane, "Area ID: ", ((KeyArea) selected).name, ((TMXMap) target).writable, listener);
            dialogueBox = addDialogueBox(pane, ((TMXMap) target).getProject(), "Message when locked: ", ((KeyArea) selected).dialogue, ((TMXMap) target).writable, listener);
            requirementTypeCombo = addEnumValueBox(pane, "Requirement type: ", Requirement.RequirementType.values(), ((KeyArea) selected).requirement.type, ((TMXMap) target).writable, listener);
            requirementParamsPane = new JPanel();
            requirementParamsPane.setLayout(new JideBoxLayout(requirementParamsPane, JideBoxLayout.PAGE_AXIS, 6));
            pane.add(requirementParamsPane, JideBoxLayout.FIX);
            updateRequirementParamsPane(requirementParamsPane, ((KeyArea) selected).requirement, listener);
        } else if (selected instanceof MapChange) {
            areaField = addTextField(pane, "Area ID: ", ((MapChange) selected).name, ((TMXMap) target).writable, listener);
            mapBox = addMapBox(pane, ((TMXMap) target).getProject(), "Target map: ", ((MapChange) selected).map, ((TMXMap) target).writable, listener);
            targetAreaCombo = new JComboBox();
            if (((MapChange) selected).map != null) {
                ((MapChange) selected).map.link();
                targetAreaCombo.setModel(new DefaultComboBoxModel((((MapChange) selected).map.getMapchangesNames().toArray())));
            }
            targetAreaCombo.setEditable(false);
            targetAreaCombo.setEnabled(((TMXMap) target).writable);
            targetAreaCombo.setSelectedItem(((MapChange) selected).place_id);
            JPanel tACPane = new JPanel();
            tACPane.setLayout(new JideBoxLayout(tACPane, JideBoxLayout.LINE_AXIS, 6));
            tACPane.add(new JLabel("Target mapchange area ID: "), JideBoxLayout.FIX);
            tACPane.add(targetAreaCombo, JideBoxLayout.VARY);
            JButton nullifyTargetArea = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
            tACPane.add(nullifyTargetArea, JideBoxLayout.FIX);
            nullifyTargetArea.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    targetAreaCombo.setSelectedItem(null);
                    listener.valueChanged(targetAreaCombo, null);
                }
            });
            targetAreaCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.valueChanged(targetAreaCombo, targetAreaCombo.getSelectedItem());
                }
            });
            pane.add(tACPane, JideBoxLayout.FIX);
        } else if (selected instanceof ReplaceArea) {
            areaField = addTextField(pane, "Area ID: ", ((ReplaceArea) selected).name, ((TMXMap) target).writable, listener);
            requirementTypeCombo = addEnumValueBox(pane, "Requirement type: ", Requirement.RequirementType.values(), ((ReplaceArea) selected).requirement.type, ((TMXMap) target).writable, listener);
            requirementParamsPane = new JPanel();
            requirementParamsPane.setLayout(new JideBoxLayout(requirementParamsPane, JideBoxLayout.PAGE_AXIS, 6));
            pane.add(requirementParamsPane, JideBoxLayout.FIX);
            updateRequirementParamsPane(requirementParamsPane, ((ReplaceArea) selected).requirement, listener);

            CollapsiblePanel replacementListPane = new CollapsiblePanel("Replacements");
            replacementListPane.setLayout(new JideBoxLayout(replacementListPane, JideBoxLayout.PAGE_AXIS));
            replacementsListModel = new ReplacementsListModel((ReplaceArea) selected);
            replacementsList = new JList(replacementsListModel);
            replacementsList.setCellRenderer(new ReplacementsListRenderer((ReplaceArea) selected));
            replacementListPane.add(new JScrollPane(replacementsList), JideBoxLayout.VARY);

            JPanel replacementListButtonsPane = new JPanel();
            replacementListButtonsPane.setLayout(new JideBoxLayout(replacementListButtonsPane, JideBoxLayout.LINE_AXIS));
            addReplacement = new JButton(new ImageIcon(DefaultIcons.getCreateIcon()));
            replacementListButtonsPane.add(addReplacement, JideBoxLayout.FIX);
            addReplacement.setEnabled(((TMXMap) target).writable);
            addReplacement.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    replacementsListModel.addObject(null, null);
                }
            });
            deleteReplacement = new JButton(new ImageIcon(DefaultIcons.getNullifyIcon()));
            replacementListButtonsPane.add(deleteReplacement, JideBoxLayout.FIX);
            deleteReplacement.setEnabled(false);
            deleteReplacement.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    replacementsListModel.removeObject(selectedReplacement);
                }
            });
            replacementListButtonsPane.add(new JPanel(), JideBoxLayout.VARY);
            replacementListPane.add(replacementListButtonsPane, JideBoxLayout.FIX);

            replacementEditPane = new JPanel();
            replacementListPane.add(replacementEditPane, JideBoxLayout.FIX);

            pane.add(new JScrollPane(replacementListPane), JideBoxLayout.VARY);


            replacementsList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    selectedReplacement = (ReplaceArea.Replacement) replacementsList.getSelectedValue();
                    updateReplacementsEditPane(replacementEditPane, (ReplaceArea) selected, selectedReplacement, listener);
                    deleteReplacement.setEnabled(((TMXMap) target).writable);
                }
            });


        } else if (selected instanceof RestArea) {
            areaField = addTextField(pane, "Area ID: ", ((RestArea) selected).name, ((TMXMap) target).writable, listener);
        } else if (selected instanceof ScriptArea) {
            evaluateTriggerBox = addEnumValueBox(pane, "Evaluate on every: ", ScriptArea.EvaluationTrigger.values(), ((ScriptArea) selected).trigger_type, ((TMXMap) target).writable, listener);
            dialogueBox = addDialogueBox(pane, ((TMXMap) target).getProject(), "Script: ", ((ScriptArea) selected).dialogue, ((TMXMap) target).writable, listener);
        } else if (selected instanceof SignArea) {
            dialogueBox = addDialogueBox(pane, ((TMXMap) target).getProject(), "Message: ", ((SignArea) selected).dialogue, ((TMXMap) target).writable, listener);
        } else if (selected instanceof SpawnArea) {
            areaField = addTextField(pane, "Spawn area ID: ", ((SpawnArea) selected).name, ((TMXMap) target).writable, listener);
            spawngroupField = addTextField(pane, "Spawn group ID: ", ((SpawnArea) selected).spawngroup_id, ((TMXMap) target).writable, listener);
            quantityField = addIntegerField(pane, "Number of spawned NPCs: ", ((SpawnArea) selected).quantity, false, ((TMXMap) target).writable, listener);
            respawnSpeedField = addIntegerField(pane, "Respawn-Speed of NPCs: ", ((SpawnArea) selected).respawnSpeed, false, ((TMXMap) target).writable, listener);
            spawnActiveForNewGame = addBooleanBasedCheckBox(pane, "Active in a new game: ", ((SpawnArea) selected).active, ((TMXMap) target).writable, listener);
            spawnIgnoreAreas = addBooleanBasedCheckBox(pane, "Monsters can walk on other game objects: ", ((SpawnArea) selected).ignoreAreas, ((TMXMap) target).writable, listener);
            npcListModel = new SpawnGroupNpcListModel((SpawnArea) selected);
            npcList = new JList(npcListModel);
            npcList.setCellRenderer(new GDERenderer(true, ((TMXMap) target).writable));
            npcList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            npcList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        ATContentStudio.frame.openEditor((JSONElement) npcList.getSelectedValue());
                        ATContentStudio.frame.selectInTree((JSONElement) npcList.getSelectedValue());
                    }
                }
            });
            npcList.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        ATContentStudio.frame.openEditor((JSONElement) npcList.getSelectedValue());
                        ATContentStudio.frame.selectInTree((JSONElement) npcList.getSelectedValue());
                    }
                }
            });
            JScrollPane npcListScroller = new JScrollPane(npcList);
            npcListScroller.getVerticalScrollBar().setUnitIncrement(16);
            pane.add(npcListScroller, JideBoxLayout.VARY);
            needVary = false;
        }
        if (needVary) pane.add(new JPanel(), JideBoxLayout.VARY);
        pane.revalidate();
        pane.repaint();
    }

    public void updateRequirementParamsPane(JPanel pane, Requirement requirement, FieldUpdateListener listener) {
        boolean writable = ((TMXMap) target).writable;
        Project project = ((TMXMap) target).getProject();
        pane.removeAll();
        if (requirement.type != null) {
            switch (requirement.type) {
                case consumedBonemeals:
                case spentGold:
                    requirementObj = null;
                    requirementObjId = null;
                    requirementValue = addIntegerField(pane, "Quantity: ", requirement.required_value, false, writable, listener);
                    break;
                case random:
                    requirementObj = null;
                    requirementObjId = addChanceField(pane, "Chance: ", requirement.required_obj_id, "50/100", writable, listener);
                    requirementValue = null;
                    break;
                case hasActorCondition:
                    requirementObj = addActorConditionBox(pane, project, "Actor Condition: ", (ActorCondition) requirement.required_obj, writable, listener);
                    requirementObjId = null;
                    requirementValue = null;
                    break;
                case inventoryKeep:
                case inventoryRemove:
                case usedItem:
                case wear:
                case wearRemove:
                    requirementObj = addItemBox(pane, project, "Item: ", (Item) requirement.required_obj, writable, listener);
                    requirementObjId = null;
                    requirementValue = addIntegerField(pane, "Quantity: ", requirement.required_value, false, writable, listener);
                    break;
                case killedMonster:
                    requirementObj = addNPCBox(pane, project, "Monster: ", (NPC) requirement.required_obj, writable, listener);
                    requirementObjId = null;
                    requirementValue = addIntegerField(pane, "Quantity: ", requirement.required_value, false, writable, listener);
                    break;
                case questLatestProgress:
                case questProgress:
                    requirementObj = addQuestBox(pane, project, "Quest: ", (Quest) requirement.required_obj, writable, listener);
                    requirementObjId = null;
                    requirementValue = addQuestStageBox(pane, project, "Quest stage: ", requirement.required_value, writable, listener, (Quest) requirement.required_obj, requirementObj);
                    break;
                case skillLevel:
                    Requirement.SkillID skillId = null;
                    try {
                        skillId = requirement.required_obj_id == null ? null : Requirement.SkillID.valueOf(requirement.required_obj_id);
                    } catch (IllegalArgumentException e) {
                    }
                    requirementObj = addEnumValueBox(pane, "Skill ID:", Requirement.SkillID.values(), skillId, writable, listener);
                    requirementObjId = null;//addTextField(pane, "Skill ID:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Level: ", requirement.required_value, false, writable, listener);
                    break;
                case timerElapsed:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Timer ID:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Timer value: ", requirement.required_value, false, writable, listener);
                    break;
                case factionScore:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Faction ID:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Minimum score: ", requirement.required_value, true, writable, listener);
                    break;
                case factionScoreEquals:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Faction ID:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Exact value: ", requirement.required_value, true, writable, listener);
                    break;
                case date:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Date type YYYYMMTT:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Minimum date value: ", requirement.required_value, true, writable, listener);
                    break;
                case dateEquals:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Date type YYYYMMTT:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Exact date value: ", requirement.required_value, true, writable, listener);
                    break;
                case time:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Time type HHMMSS:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Minimum time value: ", requirement.required_value, true, writable, listener);
                    break;
                case timeEquals:
                    requirementObj = null;
                    requirementObjId = addTextField(pane, "Time type HHMMSS:", requirement.required_obj_id, writable, listener);
                    requirementValue = addIntegerField(pane, "Exact time value: ", requirement.required_value, true, writable, listener);
                    break;
            }
        }
        requirementNegated = addBooleanBasedCheckBox(pane, "Negate this requirement.", requirement.negated, writable, listener);
        pane.revalidate();
        pane.repaint();
    }

    public void updateReplacementsEditPane(JPanel pane, ReplaceArea area, ReplaceArea.Replacement replacement, final FieldUpdateListener listener) {
        boolean writable = ((TMXMap) target).writable;
        pane.removeAll();

        sourceLayer = new JComboBox<String>(new ReplacementsLayersComboModel(area, true, replacement.sourceLayer));
        targetLayer = new JComboBox<String>(new ReplacementsLayersComboModel(area, false, replacement.targetLayer));

        sourceLayer.setEnabled(writable);
        targetLayer.setEnabled(writable);

        sourceLayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(sourceLayer, sourceLayer.getModel().getSelectedItem());
            }
        });
        targetLayer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.valueChanged(targetLayer, targetLayer.getModel().getSelectedItem());
            }
        });

        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.LINE_AXIS));
        pane.add(new JLabel("Replace "), JideBoxLayout.FIX);
        pane.add(sourceLayer, JideBoxLayout.FIX);
        pane.add(new JLabel(" by "), JideBoxLayout.FIX);
        pane.add(targetLayer, JideBoxLayout.FIX);
        pane.add(new JPanel(), JideBoxLayout.VARY);
    }

    public JPanel getXmlEditorPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.PAGE_AXIS, 6));
        editorPane = new RSyntaxTextArea();
        editorPane.setText(((TMXMap) target).toXml());
        editorPane.setEditable(false);
        editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        editorPane.setFont(editorPane.getFont().deriveFont(ATContentStudio.SCALING * editorPane.getFont().getSize()));
        pane.add(editorPane, JideBoxLayout.VARY);

        return pane;
    }

    public void updateXmlViewText(String text) {
        editorPane.setText(text);
    }


    public JPanel getReplacementSimulatorPane() {
        JPanel replacementSimulator = new JPanel();
        replacementSimulator.setLayout(new JideBoxLayout(replacementSimulator, JideBoxLayout.PAGE_AXIS));
        JPanel toolsPane = new JPanel();
        toolsPane.setLayout(new JideBoxLayout(toolsPane, JideBoxLayout.LINE_AXIS));
        final JCheckBox walkableVisibleBox = new JCheckBox("Show \"" + TMXMap.WALKABLE_LAYER_NAME + "\" layer.");
        final JCheckBox showHeroBox = new JCheckBox("Show hero on walkable tiles under the mouse pointer.");
        final JCheckBox showTooltipBox = new JCheckBox("Show tooltip with stack of tiles.");
        JPanel areasActivationPane = new JPanel();
        areasActivationPane.setLayout(new JideBoxLayout(areasActivationPane, JideBoxLayout.PAGE_AXIS));
        TreeModel areasTreeModel = new ReplaceAreasActivationTreeModel();
        final JTree areasTree = new JTree(areasTreeModel);
        areasTree.setEditable(false);
        areasTree.setRootVisible(false);
        areasTree.setCellRenderer(new ReplaceAreasActivationTreeCellRenderer());
        areasActivationPane.add(new JScrollPane(areasTree), JideBoxLayout.FIX);
        final JToggleButton activate = new JToggleButton("Activate ReplaceArea(s)");
        areasActivationPane.add(activate, JideBoxLayout.VARY);
        final TMXReplacementViewer viewer = new TMXReplacementViewer((TMXMap) target);
        JPanel activateAndViewPane = new JPanel();
        activateAndViewPane.setLayout(new JideBoxLayout(activateAndViewPane, JideBoxLayout.LINE_AXIS));

        activateAndViewPane.add(areasActivationPane, JideBoxLayout.FIX);
        activateAndViewPane.add(new JScrollPane(viewer), JideBoxLayout.VARY);

        toolsPane.add(walkableVisibleBox, JideBoxLayout.FIX);
        toolsPane.add(showTooltipBox, JideBoxLayout.FIX);
        toolsPane.add(showHeroBox, JideBoxLayout.FIX);
        toolsPane.add(new JPanel(), JideBoxLayout.VARY);

        replacementSimulator.add(toolsPane, JideBoxLayout.FIX);
        replacementSimulator.add(activateAndViewPane, JideBoxLayout.VARY);

        walkableVisibleBox.setSelected(viewer.showWalkable);
        walkableVisibleBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.showWalkable = walkableVisibleBox.isSelected();
                viewer.revalidate();
                viewer.repaint();
            }
        });

        showHeroBox.setSelected(viewer.showHeroWithMouse);
        showHeroBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.showHeroWithMouse = showHeroBox.isSelected();
            }
        });
        showTooltipBox.setSelected(viewer.showTooltip);
        showTooltipBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewer.showTooltip = showTooltipBox.isSelected();
            }
        });
        activate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (areasTree.getSelectionPaths() == null) return;
                for (TreePath paths : areasTree.getSelectionPaths()) {
                    Object target = paths.getLastPathComponent();
                    if (target instanceof ReplaceArea) {
                        viewer.activeReplacements.put((ReplaceArea) target, activate.isSelected());
                        viewer.updateLayers();
                        viewer.revalidate();
                        viewer.repaint();
                    } else if (target instanceof MapObjectGroup) {
                        for (MapObject obj : ((MapObjectGroup) target).mapObjects) {
                            if (obj instanceof ReplaceArea) {
                                viewer.activeReplacements.put((ReplaceArea) obj, activate.isSelected());
                            }
                            viewer.updateLayers();
                            viewer.revalidate();
                            viewer.repaint();
                        }
                    }

                }
            }
        });
        areasTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                ReplaceArea oldHighlight = viewer.highlighted;
                viewer.highlighted = null;
                if (areasTree.getSelectionPaths() == null) return;
                for (TreePath paths : areasTree.getSelectionPaths()) {
                    Object target = paths.getLastPathComponent();
                    if (target instanceof ReplaceArea) {
                        activate.setSelected(viewer.activeReplacements.get((ReplaceArea) target));
                        viewer.highlighted = (ReplaceArea) target;
                    } else if (target instanceof MapObjectGroup) {
                        for (MapObject obj : ((MapObjectGroup) target).mapObjects) {
                            activate.setSelected(true);
                            if (obj instanceof ReplaceArea) {
                                if (!viewer.activeReplacements.get((ReplaceArea) obj)) {
                                    activate.setSelected(false);
                                }
                            }
                        }
                    }
                }
                if (oldHighlight != viewer.highlighted) {
                    viewer.revalidate();
                    viewer.repaint();
                }
            }
        });

        return replacementSimulator;
    }

    public class ReplaceAreasActivationTreeModel implements TreeModel {

        @Override
        public Object getRoot() {
            return target;
        }

        @Override
        public Object getChild(Object parent, int index) {
            int i = index;
            if (parent instanceof TMXMap) {
                for (MapObjectGroup group : ((TMXMap) parent).groups) {
                    for (MapObject obj : group.mapObjects) {
                        if (obj instanceof ReplaceArea) {
                            if (i == 0) return group;
                            i--;
                            break;
                        }
                    }
                }
            } else if (parent instanceof MapObjectGroup) {
                for (MapObject obj : ((MapObjectGroup) parent).mapObjects) {
                    if (obj instanceof ReplaceArea) {
                        if (i == 0) return obj;
                        i--;
                    }
                }
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            int count = 0;
            if (parent instanceof TMXMap) {
                for (MapObjectGroup group : ((TMXMap) parent).groups) {
                    for (MapObject obj : group.mapObjects) {
                        if (obj instanceof ReplaceArea) {
                            count++;
                            break;
                        }
                    }
                }
            } else if (parent instanceof MapObjectGroup) {
                for (MapObject obj : ((MapObjectGroup) parent).mapObjects) {
                    if (obj instanceof ReplaceArea) {
                        count++;
                    }
                }
            }
            return count;
        }

        @Override
        public boolean isLeaf(Object node) {
            return node instanceof ReplaceArea;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            // TODO Auto-generated method stub

        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            int index = 0;
            if (parent instanceof TMXMap) {
                for (MapObjectGroup group : ((TMXMap) parent).groups) {
                    if (group == child) return index;
                    for (MapObject obj : group.mapObjects) {
                        if (obj instanceof ReplaceArea) {
                            index++;
                            break;
                        }
                    }
                }
            } else if (parent instanceof MapObjectGroup) {
                for (MapObject obj : ((MapObjectGroup) parent).mapObjects) {
                    if (obj == child) return index;
                    if (obj instanceof ReplaceArea) {
                        index++;
                    }
                }
            }
            return index;
        }

        List<TreeModelListener> listeners = new CopyOnWriteArrayList<TreeModelListener>();

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

    }

    public class ReplaceAreasActivationTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 3988638353699460533L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;

                if (value instanceof MapObjectGroup) {
                    label.setText(((MapObjectGroup) value).name);
                    label.setIcon(new ImageIcon(DefaultIcons.getObjectLayerIcon()));
                } else if (value instanceof ReplaceArea) {
                    label.setText(((ReplaceArea) value).name);
                    label.setIcon(new ImageIcon(DefaultIcons.getReplaceIcon()));
                }
            }

            return c;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static JList addTMXMapSpritesheetsList(JPanel pane, TMXMap tmxMap) {
        final JList list = new JList(new TMXMapSpritesheetsListModel(tmxMap));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ATContentStudio.frame.openEditor((Spritesheet) list.getSelectedValue());
                    ATContentStudio.frame.selectInTree((Spritesheet) list.getSelectedValue());
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ATContentStudio.frame.openEditor((Spritesheet) list.getSelectedValue());
                    ATContentStudio.frame.selectInTree((Spritesheet) list.getSelectedValue());
                }
            }
        });
        list.setCellRenderer(new SpritesheetCellRenderer(true));
        JScrollPane scroller = new JScrollPane(list);
        scroller.setBorder(BorderFactory.createTitledBorder("Spritesheets used in this map."));
        pane.add(scroller, JideBoxLayout.FIX);
        return list;
    }

    @SuppressWarnings("rawtypes")
    public class LayerListModel implements ListenerListModel {

        public TMXMap map;

        public LayerListModel(TMXMap map) {
            this.map = map;
        }

        @Override
        public int getSize() {
            return map.tmxMap.getLayerCount();
        }

        @Override
        public Object getElementAt(int index) {
            return map.tmxMap.getLayer(index);
        }


        public void objectChanged(tiled.core.MapLayer layer) {
            int index = map.tmxMap.getLayerIndex(layer);
            notifyListeners(ChangeType.CHANGED, index, index);

        }

        public void addObject(tiled.core.MapLayer layer) {
            map.addLayer(layer);
            int index = map.tmxMap.getLayerIndex(layer);
            notifyListeners(ChangeType.ADDED, index, index);
        }

        public void removeObject(tiled.core.MapLayer layer) {
            int index = map.tmxMap.getLayerIndex(layer);
            map.removeLayer(layer);
            notifyListeners(ChangeType.REMOVED, index, index);
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }
    }

    public class LayerListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -6182599528961565957L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                tiled.core.MapLayer layer = (tiled.core.MapLayer) value;
                label.setText(layer.getName());
                if (layer instanceof tiled.core.TileLayer) {
                    label.setIcon(new ImageIcon(DefaultIcons.getTileLayerIcon()));
                } else if (layer instanceof tiled.core.ObjectGroup) {
                    label.setIcon(new ImageIcon(DefaultIcons.getObjectLayerIcon()));
                }
            }
            return c;
        }
    }

    public class ReplacementsListModel extends OrderedListenerListModel<ReplaceArea, ReplaceArea.Replacement> {
        public ReplacementsListModel(ReplaceArea area) {
            super(area);
        }

        @Override
        protected List<ReplaceArea.Replacement> getItems() {
            return source.replacements;
        }

        @Override
        protected void setItems(List<ReplaceArea.Replacement> items) {
            source.replacements = items;
        }

        public void addObject(String source, String target) {
            addObject(this.source.createReplacement(source, target));
        }
    }

    public class ReplacementsListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -6182599528961565957L;
        public ReplaceArea area;

        public ReplacementsListRenderer(ReplaceArea area) {
            super();
            this.area = area;
        }

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;
                ReplaceArea.Replacement repl = (ReplaceArea.Replacement) value;
                label.setText(repl.sourceLayer + " -> " + repl.targetLayer);
            }
            return c;
        }
    }

    public class ReplacementsLayersComboModel implements ComboBoxModel<String> {
        ReplaceArea area;
        boolean modelForSource;

        public List<String> availableLayers = new ArrayList<String>();

        public String selected;

        public ReplacementsLayersComboModel(ReplaceArea area, boolean modelForSource, String selected) {
            this.area = area;
            this.modelForSource = modelForSource;
            this.selected = selected;

            availableLayers.add(null);
            for (tiled.core.MapLayer layer : ((TMXMap) target).tmxMap.getLayers()) {
                if (layer instanceof tiled.core.TileLayer) {
                    if (modelForSource && !TMXMap.isPaintedLayerName(layer.getName())) {
                        continue;
                    }
                    if (modelForSource && area.hasReplacementFor(layer.getName()) && !layer.getName().equals(selected)) {
                        continue;
                    }
                    availableLayers.add(layer.getName());
                }
            }

        }

        @Override
        public String getElementAt(int index) {
            return availableLayers.get(index);
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public int getSize() {
            return availableLayers.size();
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
            selected = (String) anItem;
        }
    }

    public class MapObjectsListModel extends OrderedListenerListModel<MapObjectGroup, MapObject> {
        public MapObjectsListModel(MapObjectGroup group) {
            super(group);
        }

        @Override
        protected List<MapObject> getItems() {
            return source.mapObjects;
        }

        @Override
        protected void setItems(List<MapObject> items) {
            source.mapObjects = items;
        }
    }

    public class GroupObjectsRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = -6182599528961565957L;

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (c instanceof JLabel) {
                ((JLabel) c).setText(((MapObject) value).name);
                ((JLabel) c).setIcon(new ImageIcon(((MapObject) value).getIcon()));
            }
            return c;
        }
    }


    public class SpawnGroupNpcListModel extends OrderedListenerListModel<SpawnArea, NPC> {
        public SpawnGroupNpcListModel(SpawnArea area) {
            super(area);
        }

        @Override
        protected List<NPC> getItems() {
            return source.spawnGroup;
        }

        @Override
        protected void setItems(List<NPC> items) {
            source.spawnGroup = items;
        }
    }


    public class TMXViewer extends JPanel implements Scrollable {

        private static final long serialVersionUID = 2845032142029325865L;


        public tiled.core.MapObject highlighted = null;
        private MapRenderer renderer;
        private FieldUpdateListener listener;
        private TMXMap map;

        public boolean resizing = false;
        public boolean moving = false;

        public Rectangle getResizeHitArea() {
            //16x16 px square in the lower right corner of area
            return new Rectangle(selectedMapObject.x + selectedMapObject.w - 16, selectedMapObject.y + selectedMapObject.h - 16, 16, 16);
        }


        public Rectangle getMoveHitArea() {
            //16x16 px square in the upper left corner of area
            return new Rectangle(selectedMapObject.x, selectedMapObject.y, 16, 16);
        }

        public Rectangle getSelectHitArea(MapObject obj) {
            //16x16 px square in the upper left corner of area
            return new Rectangle(obj.x, obj.y, 16, 16);
        }

        public TMXViewer(final TMXMap map, FieldUpdateListener listener) {
            this.listener = listener;
            renderer = createRenderer(map.tmxMap);
            this.map = map;

            setPreferredSize(renderer.getMapSize());
            setOpaque(true);

            addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (!moving && !resizing) {
                            select:
                            for (MapObjectGroup group : map.groups) {
                                if (group.visible) {
                                    for (MapObject obj : group.mapObjects) {
                                        if (getSelectHitArea(obj).contains(e.getPoint())) {
                                            TMXMapEditor.this.selectMapObject(obj);
                                            break select;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });

            if (((TMXMap) target).writable) {
                addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            resizing = false;
                            moving = false;
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (selectedMapObject != null && selectedLayer.isVisible()) {
                                if (getResizeHitArea().contains(e.getPoint())) {
                                    resizing = true;
                                } else if (getMoveHitArea().contains(e.getPoint())) {
                                    moving = true;
                                }
                            }
                        }
                    }

                });

                addMouseMotionListener(new MouseMotionListener() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        if (selectedMapObject == null) return;
                        if (!resizing && !moving) {
                            if (getResizeHitArea().contains(e.getPoint())) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                            } else if (getMoveHitArea().contains(e.getPoint())) {
                                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            } else {
                                setCursor(Cursor.getDefaultCursor());
                            }
                        }
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        if (selectedMapObject == null) return;
                        boolean valueChanged = false;
                        if (resizing) {
                            Point p = getClosestTileCorner(e.getPoint());
                            if (p.x > selectedMapObject.x) {
                                int oldW = selectedMapObject.w;
                                selectedMapObject.w = Math.min(p.x - selectedMapObject.x, map.tmxMap.getWidth() * map.tmxMap.getTileWidth());
                                if (selectedMapObject.w != oldW) valueChanged = true;
                            }
                            if (p.y > selectedMapObject.y) {
                                int oldH = selectedMapObject.h;
                                selectedMapObject.h = Math.min(p.y - selectedMapObject.y, map.tmxMap.getHeight() * map.tmxMap.getTileHeight());
                                if (selectedMapObject.h != oldH) valueChanged = true;
                            }
                        } else if (moving) {
                            Point p = getClosestTileCorner(e.getPoint());
                            if (p.x + selectedMapObject.w <= map.tmxMap.getWidth() * map.tmxMap.getTileWidth()) {
                                int oldX = selectedMapObject.x;
                                selectedMapObject.x = Math.max(p.x, 0);
                                if (selectedMapObject.x != oldX) valueChanged = true;
                            }
                            if (p.y + selectedMapObject.h <= map.tmxMap.getHeight() * map.tmxMap.getTileHeight()) {
                                int oldY = selectedMapObject.y;
                                selectedMapObject.y = Math.max(p.y, 0);
                                if (selectedMapObject.y != oldY) valueChanged = true;
                            }
                        }
                        if (valueChanged) {
                            TMXViewer.this.listener.valueChanged(TMXViewer.this, null);
                            TMXViewer.this.revalidate();
                            TMXViewer.this.repaint();
                        }
                    }
                });
            }
        }

        public void setHighlight(tiled.core.MapObject selected) {
            highlighted = selected;
            invalidate();
        }

        public void paintComponent(Graphics g) {
            final Graphics2D g2d = (Graphics2D) g.create();
            final Rectangle clip = g2d.getClipBounds();


            // Draw a gray background
            g2d.setPaint(new Color(100, 100, 100));
            g2d.fill(clip);

            // Draw each tile map layer
            for (tiled.core.MapLayer layer : ((TMXMap) target).tmxMap) {
                if (layer instanceof tiled.core.TileLayer && layer.isVisible()) {
                    renderer.paintTileLayer(g2d, (tiled.core.TileLayer) layer);
                }
            }

            if (map.colorFilter != null) {
                MapColorFilters.applyColorfilter(map.colorFilter, g2d);
            }


            // Draw each map object layer
            boolean paintSelected = false;
            for (tiled.core.MapLayer layer : ((TMXMap) target).tmxMap) {
                if (layer instanceof tiled.core.ObjectGroup && layer.isVisible()) {
                    paintSelected |= paintObjectGroup(g2d, (tiled.core.ObjectGroup) layer);
                }
            }


            if (paintSelected) {
                //TODO make this less ugly..... visually speaking.
                g2d.setColor(new Color(190, 20, 20));
                g2d.drawRect(selectedMapObject.x + selectedMapObject.w - 16, selectedMapObject.y + selectedMapObject.h - 16, 15, 15);
                g2d.drawRect(selectedMapObject.x + selectedMapObject.w - 12, selectedMapObject.y + selectedMapObject.h - 12, 11, 11);
                drawObject(selectedMapObject, g2d, new Color(190, 20, 20));
            }

            g2d.dispose();
        }

        private boolean paintObjectGroup(Graphics2D g2d, tiled.core.ObjectGroup layer) {
            boolean paintSelected = false;
            for (MapObjectGroup group : ((TMXMap) target).groups) {
                if (group.tmxGroup == layer) {
                    for (MapObject object : group.mapObjects) {
                        if (object == selectedMapObject) {
                            paintSelected = true;
                            continue;
                        } else {
                            drawObject(object, g2d, new Color(20, 20, 190));
                        }
                    }
                    break;
                }
            }
            return paintSelected;
        }

        private MapRenderer createRenderer(tiled.core.Map map) {
            switch (map.getOrientation()) {
                case tiled.core.Map.ORIENTATION_ORTHOGONAL:
                    return new OrthogonalRenderer(map);
                default:
                    return null;
            }
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL)
                return ((TMXMap) target).tmxMap.getTileWidth();
            else
                return ((TMXMap) target).tmxMap.getTileHeight();
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL) {
                final int tileWidth = ((TMXMap) target).tmxMap.getTileWidth();
                return (visibleRect.width / tileWidth - 1) * tileWidth;
            } else {
                final int tileHeight = ((TMXMap) target).tmxMap.getTileHeight();
                return (visibleRect.height / tileHeight - 1) * tileHeight;
            }
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

    }

    public static class TMXMapSpritesheetsListModel implements ListenerCollectionModel<Spritesheet> {
        TMXMap map;

        public TMXMapSpritesheetsListModel(TMXMap map) {
            this.map = map;
        }

        @Override
        public Collection getElements() {
            return map.usedSpritesheets;
        }

        List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }
    }

    public static class SpritesheetCellRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 6819681566800482793L;

        private boolean includeType;

        public SpritesheetCellRenderer(boolean includeType) {
            super();
            this.includeType = includeType;

        }

        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                label.setText("none");
            } else {
                if (includeType && ((Spritesheet) value).getDataType() != null) {
                    label.setText(((Spritesheet) value).getDataType().toString() + "/" + ((Spritesheet) value).getDesc());
                } else {
                    label.setText(((Spritesheet) value).getDesc());
                }
                if (((Spritesheet) value).getIcon() == null) {
                    Notification.addError("Unable to find icon for " + ((Spritesheet) value).getDesc());
                } else {
                    label.setIcon(new ImageIcon(((Spritesheet) value).getIcon()));
                }
            }
            return label;
        }
    }

    @Override
    public void targetUpdated() {
        this.name = ((TMXMap) target).getDesc();
        updateMessage();
        updateXmlViewText(((TMXMap) target).toXml());
        tmxViewer.repaint();
        tmxViewer.revalidate();
    }


    protected void selectMapObject(MapObject obj) {
        for (MapObjectGroup group : ((TMXMap) target).groups) {
            if (group.mapObjects.contains(obj)) {
                layerList.setSelectedValue(group.tmxGroup, true);
                groupObjectsList.setSelectedValue(obj, true);
            }
        }
    }

    public JButton createButtonPane(JPanel pane, final Project proj, final TMXMap map, final FieldUpdateListener listener) {
        JPanel savePane = new JPanel();
        savePane.setLayout(new JideBoxLayout(savePane, JideBoxLayout.LINE_AXIS, 6));
        final JButton gdeIcon = new JButton(new ImageIcon(DefaultIcons.getTiledIconImage()));
        savePane.add(gdeIcon, JideBoxLayout.FIX);
        if (map.writable) {
            gdeIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (map.needsSaving()) {
                        int confirm = JOptionPane.showConfirmDialog(TMXMapEditor.this,
                                                                    "You have unsaved changes in ATCS.\nYou'd better save your changes in ATCS before opening this map with the external editor.\nDo you want to save before opening the file?",
                                                                    "Save before opening?", JOptionPane.YES_NO_CANCEL_OPTION);
                        if (confirm == JOptionPane.CANCEL_OPTION) return;
                        if (confirm == JOptionPane.YES_OPTION) {
                            map.save();
                            ATContentStudio.frame.nodeChanged(map);
                        }
                    }
                    DesktopIntegration.openTmxMap(map.tmxFile);
                }
            });
            reload = new JButton("Reload");
            reload.setEnabled(map.changedOnDisk);
            savePane.add(reload, JideBoxLayout.FIX);
            reload.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (map.needsSaving()) {
                        int confirm = JOptionPane.showConfirmDialog(TMXMapEditor.this,
                                                                    "You modified this map in ATCS. All ATCS-made changes will be lost if you confirm.\n On the other hand, if you save using ATCS, all external changes will be lost.\n Do you want to reload?",
                                                                    "Confirm reload?", JOptionPane.OK_CANCEL_OPTION);
                        if (confirm == JOptionPane.CANCEL_OPTION) return;
                    }
                    reload.setEnabled(false);
                    (new Thread() {
                        public void run() {
                            map.reload();
                        }
                    }).start();
                }
            });
            if (map.getDataType() == GameSource.Type.altered) {
                savePane.add(message = new JLabel(ALTERED_MESSAGE), JideBoxLayout.FIX);
            } else if (map.getDataType() == GameSource.Type.created) {
                savePane.add(message = new JLabel(CREATED_MESSAGE), JideBoxLayout.FIX);
            }
            JButton save = new JButton(SAVE);
            save.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (map.state != TMXMap.State.saved) {
                        if (map.changedOnDisk) {
                            int confirm = JOptionPane.showConfirmDialog(TMXMapEditor.this,
                                                                        "You modified this map in an external tool. All external changes will be lost if you confirm.\n On the other hand, if you reload in ATCS, all ATCS-made changes will be lost.\n Do you want to save?",
                                                                        "Confirm save?", JOptionPane.OK_CANCEL_OPTION);
                            if (confirm == JOptionPane.CANCEL_OPTION) return;
                            File backup = FileUtils.backupFile(map.tmxFile);
                            if (backup != null) {
                                JOptionPane.showMessageDialog(TMXMapEditor.this, "The externally-modified file was backed up as " + backup.getAbsolutePath(), "File backed up",
                                                              JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(TMXMapEditor.this, "The externally-modified file could not be backed up.", "File backup failed", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        map.save();
                        ATContentStudio.frame.nodeChanged(map);
                    }
                }
            });
            savePane.add(save, JideBoxLayout.FIX);
            JButton delete = new JButton(DELETE);
            if (map.getDataType() == GameSource.Type.altered) {
                delete.setText(REVERT);
            }
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ATContentStudio.frame.closeEditor(map);
                    map.childrenRemoved(new ArrayList<ProjectTreeNode>());
                    map.delete();
                    GameDataElement newOne = map.getProject().getMap(map.id);
                    for (GameDataElement backlink : map.getBacklinks()) {
                        backlink.elementChanged(map, newOne);
                    }
                }
            });
            savePane.add(delete, JideBoxLayout.FIX);
            final JButton bookmark = new JButton(new ImageIcon(map.bookmark != null ? DefaultIcons.getBookmarkActiveIcon() : DefaultIcons.getBookmarkInactiveIcon()));
            savePane.add(bookmark, JideBoxLayout.FIX);
            bookmark.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (map.bookmark == null) {
                        map.getProject().bookmark(map);
                        bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkActiveIcon()));
                    } else {
                        map.bookmark.delete();
                        bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkInactiveIcon()));
                    }
                }
            });
        } else {
            if (proj.getMap(map.id) != map) {
                savePane.add(message = new JLabel(ALTERED_EXISTS_MESSAGE), JideBoxLayout.FIX);
                JButton makeWritable = new JButton(GO_TO_ALTERED);
                makeWritable.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (map.getProject().getMap(map.id) != map) {
                            ATContentStudio.frame.openEditor(map.getProject().getMap(map.id));
                            ATContentStudio.frame.closeEditor(map);
                            ATContentStudio.frame.selectInTree(map.getProject().getMap(map.id));
                        }
                    }
                });
                savePane.add(makeWritable, JideBoxLayout.FIX);

            } else {
                savePane.add(message = new JLabel(READ_ONLY_MESSAGE), JideBoxLayout.FIX);
                JButton makeWritable = new JButton(ALTER);
                makeWritable.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (map.getProject().getMap(map.id) == map) {
                            map.getProject().makeWritable(map);
                        }
                        if (map.getProject().getMap(map.id) != map) {
                            ATContentStudio.frame.openEditor(map.getProject().getMap(map.id));
                            ATContentStudio.frame.closeEditor(map);
                            ATContentStudio.frame.selectInTree(map.getProject().getMap(map.id));
                        }
                    }
                });
                savePane.add(makeWritable, JideBoxLayout.FIX);
            }
        }
        JButton prev = new JButton(new ImageIcon(DefaultIcons.getArrowLeftIcon()));
        JButton next = new JButton(new ImageIcon(DefaultIcons.getArrowRightIcon()));
        savePane.add(prev, JideBoxLayout.FIX);
        savePane.add(next, JideBoxLayout.FIX);
        if (map.getParent().getIndex(map) == 0) {
            prev.setEnabled(false);
        }
        if (map.getParent().getIndex(map) == map.getParent().getChildCount() - 1) {
            next.setEnabled(false);
        }
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectTreeNode prevNode = (ProjectTreeNode) map.getParent().getChildAt(map.getParent().getIndex(map) - 1);
                if (prevNode != null && prevNode instanceof GameDataElement) {
                    ATContentStudio.frame.openEditor((GameDataElement) prevNode);
                }
            }
        });
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectTreeNode nextNode = (ProjectTreeNode) map.getParent().getChildAt(map.getParent().getIndex(map) + 1);
                if (nextNode != null && nextNode instanceof GameDataElement) {
                    ATContentStudio.frame.openEditor((GameDataElement) nextNode);
                }
            }
        });
        final JButton bookmark = new JButton(new ImageIcon(map.bookmark != null ? DefaultIcons.getBookmarkActiveIcon() : DefaultIcons.getBookmarkInactiveIcon()));
        savePane.add(bookmark, JideBoxLayout.FIX);
        bookmark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (map.bookmark == null) {
                    map.getProject().bookmark(map);
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkActiveIcon()));
                } else {
                    map.bookmark.delete();
                    bookmark.setIcon(new ImageIcon(DefaultIcons.getBookmarkInactiveIcon()));
                }
            }
        });
        //Placeholder. Fills the eventual remaining space.
        savePane.add(new JPanel(), JideBoxLayout.VARY);
        pane.add(savePane, JideBoxLayout.FIX);
        return gdeIcon;
    }


    private int skipAreaFieldEvents = 0;

    public class MapFieldUpdater implements FieldUpdateListener {
        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void valueChanged(JComponent source, Object value) {
            TMXMap map = (TMXMap) target;
            boolean modified = true;
            if (source == layerNameField) {
                selectedLayer.setName((String) value);
                if (selectedLayer instanceof tiled.core.ObjectGroup) {
                    map.getGroup((tiled.core.ObjectGroup) selectedLayer).name = (String) value;
                }
                layerListModel.objectChanged(selectedLayer);
            } else if (source == layerVisibleBox) {
                selectedLayer.setVisible(layerVisibleBox.isSelected());
                if (selectedLayer instanceof tiled.core.ObjectGroup) {
                    map.getGroup((tiled.core.ObjectGroup) selectedLayer).visible = layerVisibleBox.isSelected();
                }
                modified = false;
                tmxViewer.revalidate();
                tmxViewer.repaint();
            } else if (source == groupActiveForNewGame) {
                if (selectedLayer instanceof tiled.core.ObjectGroup) {
                    map.getGroup((tiled.core.ObjectGroup) selectedLayer).active = groupActiveForNewGame.isSelected();
                }
                modified = true;
            } else if (source == layerList) {
                modified = false;
                tmxViewer.revalidate();
                tmxViewer.repaint();
            } else if (source == groupObjectsList) {
                modified = false;
                tmxViewer.revalidate();
                tmxViewer.repaint();
            } else if (source == areaField) {
                if (skipAreaFieldEvents > 0) skipAreaFieldEvents--;
                else {
                    selectedMapObject.name = (String) value;
                    if (selectedMapObject instanceof KeyArea) {
                        KeyArea area = (KeyArea) selectedMapObject;
                        if (area.oldSchoolRequirement) {
                            area.oldSchoolRequirement = false;
                        }
                    } else if (selectedMapObject instanceof ReplaceArea) {
                        ReplaceArea area = (ReplaceArea) selectedMapObject;
                        if (area.oldSchoolRequirement) {
                            area.oldSchoolRequirement = false;
                        }
                    }
                    groupObjectsListModel.objectChanged(selectedMapObject);
                }
            } else if (source == spawngroupField) {
                if (selectedMapObject instanceof SpawnArea) {
                    SpawnArea area = (SpawnArea) selectedMapObject;
                    if (area.spawnGroup != null && !area.spawnGroup.isEmpty()) {
                        for (NPC npc : area.spawnGroup) {
                            npc.removeBacklink(map);
                        }
                    }
                    area.spawngroup_id = (String) value;
                    selectedMapObject.link();
                    npcList.setModel(new SpawnGroupNpcListModel(area));
                    groupObjectsListModel.objectChanged(area);
                    npcList.revalidate();
                    npcList.repaint();
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                }
            } else if (source == targetAreaCombo) {
                if (selectedMapObject instanceof MapChange) {
                    MapChange area = (MapChange) selectedMapObject;
                    area.place_id = (String) value;
                }
            } else if (source == outsideBox) {
                map.outside = (Integer) value;
            } else if (source == colorFilterBox) {
                map.colorFilter = (TMXMap.ColorFilter) value;
                tmxViewer.revalidate();
                tmxViewer.repaint();
            } else if (source == droplistBox) {
                if (selectedMapObject instanceof ContainerArea) {
                    ContainerArea area = (ContainerArea) selectedMapObject;
                    if (area.droplist != null) {
                        area.droplist.removeBacklink(map);
                    }
                    area.droplist = (Droplist) value;
                    if (area.droplist != null) {
                        area.name = area.droplist.id;
                    } else {
                        area.name = null;
                    }
                    groupObjectsListModel.objectChanged(area);
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                }
            } else if (source == dialogueBox) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    if (area.dialogue != null) {
                        area.dialogue.removeBacklink(map);
                    }
                    area.dialogue = (Dialogue) value;
                    if (area.dialogue != null) {
                        area.dialogue_id = area.dialogue.id;
                    } else {
                        area.dialogue_id = null;
                    }
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                } else if (selectedMapObject instanceof ScriptArea) {
                    ScriptArea area = (ScriptArea) selectedMapObject;
                    if (area.dialogue != null) {
                        area.dialogue.removeBacklink(map);
                    }
                    area.dialogue = (Dialogue) value;
                    if (area.dialogue != null) {
                        area.name = area.dialogue.id;
                    } else {
                        area.name = null;
                    }
                    groupObjectsListModel.objectChanged(area);
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                } else if (selectedMapObject instanceof SignArea) {
                    SignArea area = (SignArea) selectedMapObject;
                    if (area.dialogue != null) {
                        area.dialogue.removeBacklink(map);
                    }
                    area.dialogue = (Dialogue) value;
                    if (area.dialogue != null) {
                        area.name = area.dialogue.id;
                    } else {
                        area.name = null;
                    }
                    groupObjectsListModel.objectChanged(area);
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                }
            } else if (source == mapBox) {
                if (selectedMapObject instanceof MapChange) {
                    MapChange area = (MapChange) selectedMapObject;
                    if (area.map != null) {
                        area.map.removeBacklink(map);
                    }
                    area.map = (TMXMap) value;
                    if (area.map != null) {
                        area.map_id = area.map.id;
                        targetAreaCombo.setModel(new DefaultComboBoxModel((area.map.getMapchangesNames().toArray())));
                    } else {
                        area.map_id = null;
                    }
                    tmxViewer.revalidate();
                    tmxViewer.repaint();
                }
            } else if (source == evaluateTriggerBox) {
                if (selectedMapObject instanceof ScriptArea) {
                    ScriptArea area = (ScriptArea) selectedMapObject;
                    area.trigger_type = (ScriptArea.EvaluationTrigger) value;
                }
            } else if (source == quantityField) {
                if (selectedMapObject instanceof SpawnArea) {
                    SpawnArea area = (SpawnArea) selectedMapObject;
                    area.quantity = (Integer) value;
                }
            } else if (source == respawnSpeedField) {
                if (selectedMapObject instanceof SpawnArea) {
                    SpawnArea area = (SpawnArea) selectedMapObject;
                    area.respawnSpeed = (Integer) value;
                }
            } else if (source == spawnActiveForNewGame) {
                if (selectedMapObject instanceof SpawnArea) {
                    SpawnArea area = (SpawnArea) selectedMapObject;
                    area.active = (Boolean) value;
                }
            } else if (source == spawnIgnoreAreas) {
                if (selectedMapObject instanceof SpawnArea) {
                    SpawnArea area = (SpawnArea) selectedMapObject;
                    area.ignoreAreas = (Boolean) value;
                }
            } else if (source == requirementTypeCombo) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    area.requirement.changeType((Requirement.RequirementType) requirementTypeCombo.getSelectedItem());
                    updateRequirementParamsPane(requirementParamsPane, area.requirement, this);
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                } else if (selectedMapObject instanceof ReplaceArea) {
                    ReplaceArea area = (ReplaceArea) selectedMapObject;
                    area.requirement.changeType((Requirement.RequirementType) requirementTypeCombo.getSelectedItem());
                    updateRequirementParamsPane(requirementParamsPane, area.requirement, this);
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                }
            } else if (source == requirementObj) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    if (area.requirement.type == Requirement.RequirementType.skillLevel) {
                        area.requirement.required_obj_id = value == null ? null : value.toString();
                    } else {
                        area.requirement.required_obj = (GameDataElement) value;
                        if (area.requirement.required_obj != null) {
                            area.requirement.required_obj_id = area.requirement.required_obj.id;
                        } else {
                            area.requirement.required_obj_id = null;
                        }
                    }
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                } else if (selectedMapObject instanceof ReplaceArea) {
                    ReplaceArea area = (ReplaceArea) selectedMapObject;
                    if (area.requirement.type == Requirement.RequirementType.skillLevel) {
                        area.requirement.required_obj_id = value == null ? null : value.toString();
                    } else {
                        area.requirement.required_obj = (GameDataElement) value;
                        if (area.requirement.required_obj != null) {
                            area.requirement.required_obj_id = area.requirement.required_obj.id;
                        } else {
                            area.requirement.required_obj_id = null;
                        }
                    }
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                }
            } else if (source == requirementObjId) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    area.requirement.required_obj_id = (String) value;
                    area.requirement.required_obj = null;
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                } else if (selectedMapObject instanceof ReplaceArea) {
                    ReplaceArea area = (ReplaceArea) selectedMapObject;
                    area.requirement.required_obj_id = (String) value;
                    area.requirement.required_obj = null;
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                }
            } else if (source == requirementValue) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    Quest quest = null;
                    QuestStage stage;
                    if (requirementValue instanceof JComboBox<?>) {
                        quest = ((Quest) area.requirement.required_obj);
                        if (quest != null && area.requirement.required_value != null) {
                            stage = quest.getStage(area.requirement.required_value);
                            if (stage != null) stage.removeBacklink(map);
                        }
                    }
                    area.requirement.required_value = (Integer) value;
                    if (quest != null) {
                        stage = quest.getStage(area.requirement.required_value);
                        if (stage != null) stage.addBacklink(map);
                    }
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                } else if (selectedMapObject instanceof ReplaceArea) {
                    ReplaceArea area = (ReplaceArea) selectedMapObject;
                    Quest quest = null;
                    QuestStage stage;
                    if (requirementValue instanceof JComboBox<?>) {
                        quest = ((Quest) area.requirement.required_obj);
                        if (quest != null && area.requirement.required_value != null) {
                            stage = quest.getStage(area.requirement.required_value);
                            if (stage != null) stage.removeBacklink(map);
                        }
                    }
                    area.requirement.required_value = (Integer) value;
                    if (quest != null) {
                        stage = quest.getStage(area.requirement.required_value);
                        if (stage != null) stage.addBacklink(map);
                    }
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                }
            } else if (source == requirementNegated) {
                if (selectedMapObject instanceof KeyArea) {
                    KeyArea area = (KeyArea) selectedMapObject;
                    area.requirement.negated = (Boolean) value;
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                } else if (selectedMapObject instanceof ReplaceArea) {
                    ReplaceArea area = (ReplaceArea) selectedMapObject;
                    area.requirement.negated = (Boolean) value;
                    if (area.oldSchoolRequirement) {
                        area.updateNameFromRequirementChange();
                        skipAreaFieldEvents += 2;
                        areaField.setText(area.name);
                    }
                }
            } else if (source == sourceLayer) {
                selectedReplacement.sourceLayer = (String) value;
                replacementsListModel.objectChanged(selectedReplacement);
                groupObjectsListModel.objectChanged(selectedMapObject);
            } else if (source == targetLayer) {
                selectedReplacement.targetLayer = (String) value;
                replacementsListModel.objectChanged(selectedReplacement);
                groupObjectsListModel.objectChanged(selectedMapObject);
            }
            if (modified) {
                if (map.state != GameDataElement.State.modified) {
                    map.state = GameDataElement.State.modified;
                    ATContentStudio.frame.editorChanged(TMXMapEditor.this);
                }
                map.childrenChanged(new ArrayList<ProjectTreeNode>());
            }
        }
    }


    public void updateMessage() {

        //TODO make this a full update of the button panel.
        TMXMap node = (TMXMap) target;
        if (node.writable) {
            if (node.getDataType() == GameSource.Type.altered) {
                message.setText(ALTERED_MESSAGE);
            } else if (node.getDataType() == GameSource.Type.created) {
                message.setText(CREATED_MESSAGE);
            }
        } else if (node.getProject().getMap(node.id) != node) {
            message.setText(ALTERED_EXISTS_MESSAGE);
        } else {
            message.setText(READ_ONLY_MESSAGE);
        }
        message.revalidate();
        message.repaint();
    }

    public class TMXReplacementViewer extends JPanel implements Scrollable {

        private static final long serialVersionUID = 2845032142029325865L;
        private TMXMap map;
        public ReplaceArea highlighted = null;
        public boolean showWalkable = true;
        public boolean showHeroWithMouse = true;
        public boolean showTooltip = true;
        private OrthogonalRenderer renderer;

        private String groundName, objectsName, aboveName, topName, walkableName;
        private Map<String, tiled.core.TileLayer> layersByName = new LinkedHashMap<String, tiled.core.TileLayer>();
        private tiled.core.TileLayer ground, objects, above, top, walkable;

        private Map<String, List<ReplaceArea>> replacementsForLayer = new LinkedHashMap<String, List<ReplaceArea>>();
        public Map<ReplaceArea, Boolean> activeReplacements = new LinkedHashMap<ReplaceArea, Boolean>();

        private boolean tooltipActivated = true;

        public TMXReplacementViewer(final TMXMap map) {
            this.map = map;
            renderer = createRenderer(map.tmxMap);

            init();

            setPreferredSize(renderer.getMapSize());
            setOpaque(true);

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    Point oldTooltippedTile = new Point(tooltippedTile);
                    tooltippedTile.setLocation(e.getX() / 32, e.getY() / 32);
                    if (!showTooltip || !((TMXMap) target).tmxMap.contains(tooltippedTile.x, tooltippedTile.y)) {
                        if (tooltipActivated) {
                            //Hides the tooltip...
                            ToolTipManager.sharedInstance().setEnabled(false);
                            ToolTipManager.sharedInstance().unregisterComponent(TMXReplacementViewer.this);
                            tooltipActivated = false;
                        }
                    } else {
                        if (showTooltip && !tooltipActivated) {
                            ToolTipManager.sharedInstance().registerComponent(TMXReplacementViewer.this);
                            ToolTipManager.sharedInstance().setEnabled(true);
                            tooltipActivated = true;
                        }
                    }
                    if (showHeroWithMouse && (oldTooltippedTile.x != tooltippedTile.x || oldTooltippedTile.y != tooltippedTile.y)) {
                        TMXReplacementViewer.this.revalidate();
                        TMXReplacementViewer.this.repaint();
                    }
                }
            });

            ToolTipManager.sharedInstance().registerComponent(this);
            setToolTipText("");

        }

        public void init() {
            groundName = objectsName = aboveName = walkableName = topName = null;
            layersByName.clear();
            ground = objects = above = walkable = top = null;
            replacementsForLayer.clear();

            for (tiled.core.MapLayer layer : map.tmxMap.getLayers()) {
                if (layer instanceof tiled.core.TileLayer) {
                    layersByName.put(layer.getName(), (tiled.core.TileLayer) layer);
                    if (TMXMap.GROUND_LAYER_NAME.equalsIgnoreCase(layer.getName())) {
                        groundName = layer.getName();
                    } else if (TMXMap.OBJECTS_LAYER_NAME.equalsIgnoreCase(layer.getName())) {
                        objectsName = layer.getName();
                    } else if (TMXMap.ABOVE_LAYER_NAME.equalsIgnoreCase(layer.getName())) {
                        aboveName = layer.getName();
                    } else if (TMXMap.TOP_LAYER_NAME.equalsIgnoreCase(layer.getName())) {
                        topName = layer.getName();
                    } else if (TMXMap.WALKABLE_LAYER_NAME.equalsIgnoreCase(layer.getName())) {
                        walkableName = layer.getName();
                    }
                }
            }

            for (MapObjectGroup group : map.groups) {
                for (MapObject object : group.mapObjects) {
                    if (object instanceof ReplaceArea) {
                        ReplaceArea area = (ReplaceArea) object;
                        if (!activeReplacements.containsKey(area)) {
                            activeReplacements.put(area, true);
                        }

                        if (area.replacements != null) {
                            for (ReplaceArea.Replacement repl : area.replacements) {
                                if (replacementsForLayer.get(repl.sourceLayer) == null) {
                                    replacementsForLayer.put(repl.sourceLayer, new ArrayList<ReplaceArea>());
                                }
                                replacementsForLayer.get(repl.sourceLayer).add(area);
                            }
                        }
                    }
                }
            }
            updateLayers();
        }

        public void updateLayers() {
            ground = mergeReplacements(groundName);
            objects = mergeReplacements(objectsName);
            above = mergeReplacements(aboveName);
            top = mergeReplacements(topName);
            walkable = mergeReplacements(walkableName);
        }

        private tiled.core.TileLayer mergeReplacements(String layerName) {
            tiled.core.TileLayer merged = null;
            if (layerName != null && layersByName.get(layerName) != null) {
                tiled.core.TileLayer original = layersByName.get(layerName);
                merged = new tiled.core.TileLayer(original.getBounds());
                merged.copyFrom(original);
                if (replacementsForLayer.containsKey(layerName)) {
                    for (ReplaceArea area : replacementsForLayer.get(layerName)) {
                        if (activeReplacements.get(area)) {
                            String targetName = null;
                            for (ReplaceArea.Replacement repl : area.replacements) {
                                if (layerName.equals(repl.sourceLayer)) {
                                    targetName = repl.targetLayer;
                                }
                            }
                            if (targetName != null) {
                                for (int x = area.x / 32; x < (area.x + area.w) / 32; x++) {
                                    for (int y = area.y / 32; y < (area.y + area.h) / 32; y++) {
                                        merged.setTileAt(x, y, layersByName.get(targetName).getTileAt(x, y));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return merged;
        }

        public void setHighlight(ReplaceArea selected) {
            highlighted = selected;
            invalidate();
        }

        public void paintComponent(Graphics g) {
            final Graphics2D g2d = (Graphics2D) g.create();
            final Rectangle clip = g2d.getClipBounds();

            // Draw a gray background
            g2d.setPaint(new Color(100, 100, 100));
            g2d.fill(clip);

            // Draw each tile map layer

            if (ground != null) {
                renderer.paintTileLayer(g2d, ground);
            }

            if (objects != null) {
                renderer.paintTileLayer(g2d, objects);
            }

            if (showHeroWithMouse && tooltippedTile != null && ((TMXMap) target).tmxMap.contains(tooltippedTile.x, tooltippedTile.y) &&
                    walkable != null && walkable.getTileAt(tooltippedTile.x, tooltippedTile.y) == null) {
                g2d.drawImage(DefaultIcons.getHeroImage(), tooltippedTile.x * 32, tooltippedTile.y * 32, 32, 32, null);
            }

            if (above != null) {
                renderer.paintTileLayer(g2d, above);
            }

            if (top != null) {
                renderer.paintTileLayer(g2d, top);
            }
            if (walkable != null && showWalkable) {
                renderer.paintTileLayer(g2d, walkable);
            }

            if (map.colorFilter != null) {
                MapColorFilters.applyColorfilter(map.colorFilter, g2d);
            }


            if (highlighted != null) {
                drawObject(highlighted, g2d, new Color(190, 20, 20));
            }

            g2d.dispose();
        }


        private OrthogonalRenderer createRenderer(tiled.core.Map map) {
            return new OrthogonalRenderer(map);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL)
                return ((TMXMap) target).tmxMap.getTileWidth();
            else
                return ((TMXMap) target).tmxMap.getTileHeight();
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
            if (orientation == SwingConstants.HORIZONTAL) {
                final int tileWidth = ((TMXMap) target).tmxMap.getTileWidth();
                return (visibleRect.width / tileWidth - 1) * tileWidth;
            } else {
                final int tileHeight = ((TMXMap) target).tmxMap.getTileHeight();
                return (visibleRect.height / tileHeight - 1) * tileHeight;
            }
        }

        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        JLabel noTileGround = new JLabel("None", new ImageIcon(DefaultIcons.getNullifyImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)), SwingConstants.LEFT);
        JLabel noTileObjects = new JLabel("None", new ImageIcon(DefaultIcons.getNullifyImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)), SwingConstants.LEFT);
        JLabel noTileAbove = new JLabel("None", new ImageIcon(DefaultIcons.getNullifyImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)), SwingConstants.LEFT);
        JLabel noTileTop = new JLabel("None", new ImageIcon(DefaultIcons.getNullifyImage().getScaledInstance(32, 32, Image.SCALE_DEFAULT)), SwingConstants.LEFT);

        {
            noTileGround.setPreferredSize(new Dimension(32, 32));
            noTileObjects.setPreferredSize(new Dimension(32, 32));
            noTileAbove.setPreferredSize(new Dimension(32, 32));
            noTileTop.setPreferredSize(new Dimension(32, 32));
        }

        Point tooltippedTile = new Point();
        JToolTip tt = null;
        Point lastTTTile = null;

        @Override
        public JToolTip createToolTip() {
            if (tooltippedTile.equals(lastTTTile)) {
                return tt;
            }
            if (!((TMXMap) target).tmxMap.contains(tooltippedTile.x, tooltippedTile.y)) {
                return super.createToolTip();
            }
            tt = super.createToolTip();
            lastTTTile = new Point(tooltippedTile);
            tt.setLayout(new BorderLayout());
            JPanel content = new JPanel();
            content.setLayout(new JideBoxLayout(content, JideBoxLayout.PAGE_AXIS));
            if (tooltippedTile != null) {
                tiled.core.Tile tile;
                Image tileImage;
                JLabel label;

                if (top != null && top.getTileAt(tooltippedTile.x, tooltippedTile.y) != null) {
                    tile = top.getTileAt(tooltippedTile.x, tooltippedTile.y);
                    tileImage = tile.getImage();
                } else {
                    tile = null;
                    tileImage = null;
                }
                if (tileImage != null) {
                    label = new JLabel(tile.getTileSet().getName(), new ImageIcon(tileImage), SwingConstants.LEFT);
                    label.setPreferredSize(new Dimension(32, 32));
                    content.add(label, JideBoxLayout.FIX);
                    //Fix when (if?) Top is advertised publicly.
//	    		} else {
//	    			content.add(noTileTop, JideBoxLayout.FIX);
                }

                if (above != null && above.getTileAt(tooltippedTile.x, tooltippedTile.y) != null) {
                    tile = above.getTileAt(tooltippedTile.x, tooltippedTile.y);
                    tileImage = tile.getImage();
                } else {
                    tile = null;
                    tileImage = null;
                }
                if (tileImage != null) {
                    label = new JLabel(tile.getTileSet().getName(), new ImageIcon(tileImage), SwingConstants.LEFT);
                    label.setPreferredSize(new Dimension(32, 32));
                    content.add(label, JideBoxLayout.FIX);
                } else {
                    content.add(noTileAbove, JideBoxLayout.FIX);
                }

                if (objects != null && objects.getTileAt(tooltippedTile.x, tooltippedTile.y) != null) {
                    tile = objects.getTileAt(tooltippedTile.x, tooltippedTile.y);
                    tileImage = tile.getImage();
                } else {
                    tile = null;
                    tileImage = null;
                }
                if (tileImage != null) {
                    label = new JLabel(tile.getTileSet().getName(), new ImageIcon(tileImage), SwingConstants.LEFT);
                    label.setPreferredSize(new Dimension(32, 32));
                    content.add(label, JideBoxLayout.FIX);
                } else {
                    content.add(noTileObjects, JideBoxLayout.FIX);
                }

                if (ground != null && ground.getTileAt(tooltippedTile.x, tooltippedTile.y) != null) {
                    tile = ground.getTileAt(tooltippedTile.x, tooltippedTile.y);
                    tileImage = tile.getImage();
                } else {
                    tile = null;
                    tileImage = null;
                }
                if (tileImage != null) {
                    label = new JLabel(tile.getTileSet().getName(), new ImageIcon(tileImage), SwingConstants.LEFT);
                    label.setPreferredSize(new Dimension(32, 32));
                    content.add(label, JideBoxLayout.FIX);
                } else {
                    content.add(noTileGround, JideBoxLayout.FIX);
                }

                content.add(new JLabel(tooltippedTile.x + ", " + tooltippedTile.y), JideBoxLayout.FIX);

            }

            tt.add(content, BorderLayout.CENTER);
            tt.setPreferredSize(tt.getLayout().preferredLayoutSize(tt));
            return tt;
        }

        @Override
        public Point getToolTipLocation(MouseEvent event) {
            return event.getPoint();//new Point(event.getX() - (event.getX() % 32), event.getY() - (event.getY() % 32));
        }

    }


    public Point getClosestTileCorner(Point p) {
        return new Point(getClosestMultiple(p.x, 32), getClosestMultiple(p.y, 32));
    }

    public int getClosestMultiple(int num, int ref) {
        int rest = num % ref;
        int result = num - rest;
        if (rest >= ref / 2) {
            result += ref;
        }
        return result;
    }


    private void drawObject(MapObject object, Graphics2D g2d, Color color) {
        g2d.setPaint(color);
        g2d.drawRect(object.x + 1, object.y + 1, object.w - 3, object.h - 3);
        g2d.drawRect(object.x + 2, object.y + 2, object.w - 5, object.h - 5);
        g2d.setPaint(color.darker().darker());
        g2d.drawLine(object.x, object.y + object.h - 1, object.x + object.w - 1, object.y + object.h - 1);
        g2d.drawLine(object.x + object.w - 1, object.y, object.x + object.w - 1, object.y + object.h - 1);
        g2d.drawLine(object.x + 3, object.y + 3, object.x + object.w - 4, object.y + 3);
        g2d.drawLine(object.x + 3, object.y + 3, object.x + 3, object.y + object.h - 4);
        g2d.setPaint(color.brighter().brighter().brighter());
        g2d.drawLine(object.x, object.y, object.x + object.w - 1, object.y);
        g2d.drawLine(object.x, object.y, object.x, object.y + object.h - 1);
        g2d.drawLine(object.x + 3, object.y + object.h - 4, object.x + object.w - 4, object.y + object.h - 4);
        g2d.drawLine(object.x + object.w - 4, object.y + 3, object.x + object.w - 4, object.y + object.h - 4);
        Image img = object.getIcon();
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.fillRect(object.x + 2, object.y + 2, img.getWidth(null), img.getHeight(null));
        g2d.drawImage(object.getIcon(), object.x + 2, object.y + 2, null);
    }


    @Override
    public void mapChanged() {
        if (reload != null) reload.setEnabled(true);
    }


    @Override
    public void mapReloaded() {
        ATContentStudio.frame.nodeChanged(target);
        ((TMXMap) target).removeMapChangedOnDiskListener(this);
        ATContentStudio.frame.closeEditor(target);
        ATContentStudio.frame.openEditor(target);

    }

}
