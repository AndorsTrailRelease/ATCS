package com.gpl.rpg.atcontentstudio.ui.tools.writermode;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData;
import com.gpl.rpg.atcontentstudio.model.tools.writermode.WriterModeData.*;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.Editor;
import com.gpl.rpg.atcontentstudio.utils.TextUtils;
import com.jidesoft.swing.JideBoxLayout;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.*;
import prefuse.data.expression.ColumnExpression;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.NullRenderer;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WriterModeEditor extends Editor {

    private static final long serialVersionUID = -6591631891278528494L;

    private static final String HELP_TEXT =
            "<html><body><table>"
                    + "<tr><td valign=\"middle\">(Esc.)</td><td valign=\"middle\"> Cancel edition</td></tr>"
                    + "<tr><td valign=\"middle\">(Ctrl+Enter)</td><td valign=\"middle\"> Confirm changes</td></tr>"
                    + "<tr><td valign=\"middle\">(Shift+Enter)</td><td valign=\"middle\"> Create next of same kind</td></tr>"
                    + "<tr><td valign=\"middle\">(Alt+Enter)</td><td valign=\"middle\"> Create next of other kind</td></tr>"
                    + "</table></body></html>";


    private JComponent overlay = null;
    private JComponent helpWindow = null;
    private Display view;

    final private WriterModeData data;
    private WriterNode selected;
    private WriterNode prevSelected = null;

    public WriterModeEditor(WriterModeData data) {
        this.target = data;
        this.data = data;
        this.name = data.id;
        this.icon = new ImageIcon(DefaultIcons.getDialogueIcon());
        selected = data.begin;
        view = new WriterGraphView();
        view.setLocation(0, 0);

        setLayout(new BorderLayout());
        add(createButtonPane(), BorderLayout.NORTH);
        add(view, BorderLayout.CENTER);
    }


    public void dataAltered() {
        data.state = GameDataElement.State.modified;
        data.childrenChanged(new ArrayList<ProjectTreeNode>());
        ATContentStudio.frame.editorChanged(this);
    }

    public JPanel createButtonPane() {
        JPanel pane = new JPanel();
        pane.setLayout(new JideBoxLayout(pane, JideBoxLayout.LINE_AXIS));
        JButton save = new JButton("Save sketch");
        JButton export = new JButton("Export sketch to game data");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                data.save();
                data.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(WriterModeEditor.this);
            }
        });
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<Dialogue> created = data.toDialogue();
                data.getProject().createElements(created);
                //data.begin.dialogue.save();
                data.save();
                data.childrenChanged(new ArrayList<ProjectTreeNode>());
                ATContentStudio.frame.editorChanged(WriterModeEditor.this);
            }
        });
        pane.add(save, JideBoxLayout.FIX);
        pane.add(export, JideBoxLayout.FIX);
        pane.add(new JPanel(), JideBoxLayout.VARY);
        return pane;
    }


    private void disposeOverlay() {
        if (overlay != null) view.remove(overlay);
        overlay = null;
        view.requestFocus();
        view.revalidate();
    }


    private void createHelpWindow() {
        JInternalFrame window = new JInternalFrame("Help", true, true);
        window.setLayout(new BorderLayout());
        JEditorPane area = new JEditorPane();
        area.setContentType("text/html");
        area.setText(HELP_TEXT);
        area.setEditable(false);

        window.add(new JScrollPane(area));
        window.setSize(350, 250);
        window.setLocation(0, 0);

        view.add(window);
        helpWindow = window;
    }

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String NULL_NODES = "graph.nullNodes";
    public static final String EDGES = "graph.edges";
    public static final String EDGES_LABELS = "edgesLabels";

    public static final String LABEL = "label";
    public static final String ICON = "icon";
    public static final String TARGET = "target";

    public static final String IS_REPLY = "reply";
    public static final String THREAD_START = "threadStart";
    public static final String SELECTED = "selected";
    public static final String IS_TREE_EDGE = "isTreeEdge";

    private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    private class WriterGraphView extends Display {
        private static final long serialVersionUID = -7992763190713052045L;

        private MyGraph graph;
        private Map<WriterModeData.WriterNode, Node> cells = new LinkedHashMap<WriterModeData.WriterNode, Node>();

        private Node nullNode = null;
        private Edge pendingEdge = null;
        private boolean edgePending = false;

        public WriterGraphView() {
            super(new Visualization());
            loadGraph();
            nullNode = graph.addNode();
            nullNode.setBoolean(NULL_NODES, true);

            // add visual data groups
            m_vis.addGraph(GRAPH, graph);
            m_vis.setInteractive(EDGES, null, false);

            LabelRenderer nodeR = new MyLabelRenderer(LABEL);
            nodeR.setHorizontalTextAlignment(prefuse.Constants.LEFT);

            EdgeRenderer edgeR = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);

            LabelRenderer edgeLabelR = new LabelRenderer(LABEL);
            edgeLabelR.setRenderType(LabelRenderer.RENDER_TYPE_DRAW);

            DefaultRendererFactory drf = new DefaultRendererFactory();
            drf.setDefaultRenderer(nodeR);
            drf.setDefaultEdgeRenderer(edgeR);
            drf.add(new InGroupPredicate(EDGES_LABELS), edgeLabelR);
            drf.add(new ColumnExpression(NULL_NODES), new NullRenderer());
            m_vis.setRendererFactory(drf);
            DECORATOR_SCHEMA.setDefault(VisualItem.FILLCOLOR, ColorLib.gray(255));
            DECORATOR_SCHEMA.setDefault(VisualItem.STROKECOLOR, ColorLib.rgba(0, 0, 0, 0));
            DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(0));
            m_vis.addDecorators(EDGES_LABELS, EDGES, DECORATOR_SCHEMA);

            // set up the visual operators
            // first set up all the color actions
            ColorAction nStrokeColor = new NodeStrokeColorAction(NODES, VisualItem.STROKECOLOR);
//            nStrokeColor.setDefaultColor(ColorLib.gray(100));
//            nStrokeColor.add("_hover", ColorLib.rgb(255,100,100));
            StrokeAction nStroke = new StrokeAction(NODES);

            ColorAction nFill = new ColorAction(NODES, VisualItem.FILLCOLOR);
            nFill.setDefaultColor(ColorLib.gray(255));
//            ColorAction nFill = new NPCPhraseColorAction(NODES, VisualItem.FILLCOLOR);
//            
            ColorAction eEdges = new ConnectedEdgeColorAction(EDGES, VisualItem.STROKECOLOR);
//            eEdges.setDefaultColor(ColorLib.gray(100));
            ColorAction eArrows = new ConnectedEdgeColorAction(EDGES, VisualItem.FILLCOLOR);
//            eArrows.setDefaultColor(ColorLib.gray(100));
//            ColorAction eEdgesLabels = new ConnectedEdgeColorAction(EDGES_LABELS, VisualItem.TEXTCOLOR);

//            StrokeAction eStroke = new EdgesStrokeAction(EDGES);

            FontAction aFont = new FontAction();
            ColorAction aFontColor = new ColorAction(NODES, VisualItem.TEXTCOLOR);
            aFontColor.setDefaultColor(ColorLib.rgb(0, 0, 0));

            // bundle the color actions
            ActionList colors = new ActionList(Activity.INFINITY);
            colors.add(nStrokeColor);
            colors.add(nFill);
            colors.add(nStroke);
            colors.add(eEdges);
            colors.add(eArrows);
//            colors.add(eEdgesLabels);
//            colors.add(eStroke);
            colors.add(aFont);
            colors.add(aFontColor);
            colors.add(new RepaintAction());
            m_vis.putAction("colors", colors);

            // now create the main layout routine
            ActionList layout = new ActionList();//Activity.INFINITY);
            NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(GRAPH, prefuse.Constants.ORIENT_LEFT_RIGHT, 120, 40, 40);
            treeLayout.setLayoutAnchor(new Point2D.Double(25, 300));
            layout.add(treeLayout);
//            layout.add(new EdgesLabelDecoratorLayout(EDGES_LABELS));
            layout.add(new RepaintAction());
            m_vis.putAction("layout", layout);

            ActionList scrollToSelectedList = new ActionList();
            Action scrollToSelected = new ScrollToSelectedAction(false);
            scrollToSelectedList.add(scrollToSelected);
            m_vis.putAction("scrollToSelected", scrollToSelectedList);


            ActionList scrollToSelectedAndEditList = new ActionList();
            Action scrollToSelectedAndEdit = new ScrollToSelectedAction(true);
            scrollToSelectedAndEditList.add(scrollToSelectedAndEdit);
            m_vis.putAction("scrollToSelectedAndEdit", scrollToSelectedAndEditList);


            // set up the display
            setSize(500, 500);
            pan(250, 250);
            setHighQuality(true);
//            addControlListener(new TooltipControl());
            addControlListener(new GraphInputControl());
            addControlListener(new WheelZoomControl());
            addControlListener(new ZoomControl());
            addControlListener(new PanControl());

            // set things running
            m_vis.run("colors");
            m_vis.run("layout");
            m_vis.run("scrollToSelected");

            setFocusTraversalKeysEnabled(false);
        }

        public void loadGraph() {
            graph = new MyGraph(true, IS_TREE_EDGE);

            graph.addColumn(LABEL, String.class, "");
            graph.addColumn(ICON, Image.class, DefaultIcons.getNullifyIcon());
            graph.addColumn(TARGET, WriterModeData.WriterNode.class, null);

            graph.addColumn(IS_REPLY, boolean.class, false);
            graph.addColumn(THREAD_START, boolean.class, false);
            graph.addColumn(SELECTED, boolean.class, false);
            graph.addColumn(IS_TREE_EDGE, boolean.class, true);
            graph.addColumn(NULL_NODES, boolean.class, false);

            if (data != null && data.begin != null) {
                selected = data.begin;
                addDialogueNode(data.begin);
            }
        }

        public Node addDialogueNode(WriterModeData.WriterDialogue dialogue) {
            if (cells.get(dialogue) == null) {
                Node dNode = graph.addNode();
                cells.put(dialogue, dNode);
                dNode.setString(LABEL, dialogue.text);
                dNode.set(TARGET, dialogue);

                if (dialogue.index == 0) {
                    dNode.setBoolean(THREAD_START, true);
                }

                Node rNode;
//   			int i = 1;
                for (WriterModeData.WriterReply reply : dialogue.replies) {
                    if (reply instanceof EmptyReply && reply.next_dialogue != null) {
                        if (cells.get(reply.next_dialogue) == null) {
                            rNode = addDialogueNode(reply.next_dialogue);
                            /*Edge e = */
                            graph.addEdge(dNode, rNode);
                        } else {
                            rNode = cells.get(reply.next_dialogue);
                            Edge e = graph.addEdge(dNode, rNode);
                            e.setBoolean(IS_TREE_EDGE, false);
                        }
                    } else {
                        if (cells.get(reply) == null) {
                            rNode = addReplyNode(reply);
                            /*Edge e = */
                            graph.addEdge(dNode, rNode);
//   						e.setString(LABEL, "#"+i++);
                        } else {
                            rNode = cells.get(reply);
                            Edge e = graph.addEdge(dNode, rNode);
                            e.setBoolean(IS_TREE_EDGE, false);
                        }
                    }
                }
            }
            return cells.get(dialogue);
        }

        public Node addReplyNode(WriterModeData.WriterReply reply) {
            if (cells.get(reply) == null) {
                Node rNode = graph.addNode();
                rNode.setBoolean(IS_REPLY, true);
                cells.put(reply, rNode);
                if (reply.text != null) {
                    rNode.setString(LABEL, reply.text);
                    rNode.set(TARGET, reply);

                    if (reply.next_dialogue != null) {
                        if (cells.get(reply.next_dialogue) == null) {
                            Node dNode = addDialogueNode(reply.next_dialogue);
                            /*Edge e = */
                            graph.addEdge(rNode, dNode);
                        } else {
                            Node dNode = cells.get(reply.next_dialogue);
                            Edge e = graph.addEdge(rNode, dNode);
                            e.setBoolean(IS_TREE_EDGE, false);
                        }
                    }
                }
            }
            return cells.get(reply);
        }

        public void addEdge(WriterNode source, WriterNode target) {
            if (graph.getEdge(cells.get(source), cells.get(target)) != null) return;
            Edge e = graph.addEdge(cells.get(source), cells.get(target));
            e.setBoolean(IS_TREE_EDGE, false);
            m_vis.run("colors");
            m_vis.run("layout");

        }


        class MyLabelRenderer extends LabelRenderer {
            public MyLabelRenderer(String label) {
                super(label);
            }

            @Override
            protected Image getImage(VisualItem item) {
                return item.getBoolean(IS_REPLY) ? DefaultIcons.getHeroIcon() : DefaultIcons.getNPCIcon();
            }

            @Override
            protected String getText(VisualItem item) {
                if (!item.getBoolean(IS_REPLY) && super.getText(item) == null) return "[Selector]";
                return TextUtils.wordWrap(super.getText(item), 40);
            }
        }

        class NodeStrokeColorAction extends ColorAction {

            final int defaultColor = ColorLib.gray(100);
            final int hoverColor = ColorLib.rgb(255, 100, 100);
            final int selectedColor = ColorLib.rgb(100, 100, 255);

            public NodeStrokeColorAction(String group, String field) {
                super(group, field);
            }

            @Override
            public int getColor(VisualItem item) {
                if (item.get(TARGET) != null && item.get(TARGET) == selected) {
                    return selectedColor;
                }
                if (item.isHover()) {
                    return hoverColor;
                }
                return defaultColor;
            }
        }

        class ConnectedEdgeColorAction extends ColorAction {

            final int outgoing = ColorLib.rgb(255, 100, 100);
            final int incoming = ColorLib.rgb(100, 255, 100);
            final int none = ColorLib.gray(100);

            public ConnectedEdgeColorAction(String group, String field) {
                super(group, field);
            }

            @Override
            public int getColor(VisualItem item) {
                if (item instanceof DecoratorItem) {
                    item = ((DecoratorItem) item).getDecoratedItem();
                }
                if (item instanceof EdgeItem) {
                    if (((EdgeItem) item).getSourceItem() != null && ((EdgeItem) item).getSourceItem().isHover()) {
                        return outgoing;
                    } else if (((EdgeItem) item).getTargetItem().isHover()) {
                        return incoming;
                    }
                }

                return none;
            }
        }

        class GraphInputControl extends ControlAdapter {
            @Override
            public void itemClicked(VisualItem item, MouseEvent e) {
                if (edgePending) {
                    WriterNode target = (WriterNode) item.get(TARGET);
                    lockPendingEdge(target);
                } else {
                    if (e.getClickCount() == 1) {
                        if (item.get(TARGET) != null) {
                            prevSelected = selected;
                            selected = (WriterNode) item.get(TARGET);
                        }
                    } else if (e.getClickCount() == 2) {
                        if (item.get(TARGET) != null) {

                            showEditorOnSelectedAt(e.getPoint());
                        }
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                selected = null;
                view.requestFocus();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (edgePending && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    stopPendingEdge();
                }
                KeyStroke event = KeyStroke.getKeyStrokeForEvent(e);
                if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true))) {
                    selectAndScroll(nextNodeUp(selected));
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true))) {
                    selectAndScroll(nextNodeDown(selected));
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true)) || event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK, true))) {
                    selectAndScroll(nextNodeLeft(selected));
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true)) || event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, true))) {
                    selectAndScroll(nextNodeRight(selected));
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true))) {
                    showEditorOnSelected();
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK, true))) {
                    createNextDefaultNode.actionPerformed(null);
                } else if (event.equals(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true))) {
                    if (selected instanceof WriterDialogue) {
                        createContinueTalkingNode.actionPerformed(null);
                    } else if (selected instanceof WriterReply) {
                        createOtherReplyNode.actionPerformed(null);
                    }
                }
            }

            @Override
            public void itemKeyReleased(VisualItem item, KeyEvent e) {
                keyReleased(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (selected != null && e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    startPendingEdge();
                }
            }

            @Override
            public void itemKeyPressed(VisualItem item, KeyEvent e) {
                keyPressed(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (edgePending) {
                    Point p = e.getPoint();
                    Point2D p2 = getAbsoluteCoordinate(p, null);
                    m_vis.getVisualItem(NODES, nullNode).setX(p2.getX());
                    m_vis.getVisualItem(NODES, nullNode).setY(p2.getY());
                    m_vis.run("colors");
                    revalidate();
                    repaint();
                    disposeOverlay();
                }
            }

            @Override
            public void itemMoved(VisualItem item, MouseEvent e) {
                mouseMoved(e);
            }

        }


        public void startPendingEdge() {
            if (edgePending) return;
            if (selected instanceof WriterDialogue ||
                    (selected instanceof WriterReply && ((WriterReply) selected).next_dialogue == null)) {
                pendingEdge = graph.addEdge(cells.get(selected), nullNode);
                edgePending = true;
                m_vis.run("colors");
                m_vis.run("layout");
                revalidate();
                repaint();
            }
        }


        public void stopPendingEdge() {
            if (!edgePending) return;
            graph.removeEdge(pendingEdge);
            pendingEdge = null;
            edgePending = false;
            m_vis.run("colors");
            m_vis.run("layout");
            revalidate();
            repaint();
        }

        public void lockPendingEdge(WriterNode target) {
            if (selected instanceof WriterReply) {
                if (target instanceof WriterDialogue && ((WriterReply) selected).next_dialogue == null) {
                    ((WriterReply) selected).next_dialogue = (WriterDialogue) target;
                    stopPendingEdge();
                    addEdge(selected, target);
                }
            } else if (selected instanceof WriterDialogue) {
                if (target instanceof WriterReply) {
                    WriterReply clone = data.new WriterReply((WriterDialogue) selected);
                    clone.text = ((WriterReply) target).text;
                    if (((WriterReply) target).next_dialogue instanceof SpecialDialogue) {
                        clone.next_dialogue = ((SpecialDialogue) ((WriterReply) target).next_dialogue).duplicate();
                    } else {
                        clone.next_dialogue = ((WriterReply) target).next_dialogue;
                    }
                    stopPendingEdge();
                    addReplyNode(clone);
                    addEdge(selected, clone);
                    if (clone.next_dialogue != null) addEdge(clone, clone.next_dialogue);
                } else if (target instanceof WriterDialogue) {
                    WriterReply empty = data.new EmptyReply((WriterDialogue) selected);
                    empty.next_dialogue = (WriterDialogue) target;
                    stopPendingEdge();
                    addEdge(selected, target);
                }
            }
            dataAltered();
        }

        static final String disposeEditorString = "disposeEditor";
        final AbstractAction disposeEditor = new AbstractAction("Dispose Editor") {
            private static final long serialVersionUID = 6640035253411399809L;

            @Override
            public void actionPerformed(ActionEvent e) {
                disposeOverlay();
            }
        };


        static final String commitEditAndDisposeEditorString = "commitEditAndDisposeEditor";
        final AbstractAction commitEditAndDisposeEditor = new AbstractAction("Commit Edit") {
            private static final long serialVersionUID = 8039766217709796328L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (area == null) return;
                commitAreaText();
                m_vis.run("colors");
                revalidate();
                repaint();
                disposeOverlay();
                dataAltered();
            }
        };

        private void commitAreaText() {
            selected.text = area.getText();
            cells.get(selected).set(LABEL, selected.text);
        }

        //        static final String createNextDefaultNodeString = "createNextDefaultNode";
        final AbstractAction createNextDefaultNode = new AbstractAction("Create next default") {
            private static final long serialVersionUID = 1658086056088672748L;

            @Override
            public void actionPerformed(ActionEvent e) {
                stopPendingEdge();
                if (selected == null) return;
                WriterNode newWrNode = null;
                Node newNode = null;
                if (selected instanceof WriterDialogue) {
                    newWrNode = data.new WriterReply((WriterDialogue) selected);
                    newNode = addReplyNode(((WriterReply) newWrNode));
                } else if (selected instanceof WriterReply) {
                    if (((WriterReply) selected).next_dialogue != null) {
                        newWrNode = ((WriterReply) selected).next_dialogue;
                        newNode = cells.get(newWrNode);
                    } else {
                        newWrNode = data.new WriterDialogue(((WriterReply) selected).parent.id_prefix);
                        ((WriterReply) selected).next_dialogue = ((WriterDialogue) newWrNode);
                        newNode = addDialogueNode(((WriterDialogue) newWrNode));
                    }
                }
                if (newNode != null) {
                    /*Edge edge = */
                    graph.addEdge(cells.get(selected), newNode);
                    setSelected(newWrNode);

                    m_vis.run("colors");
                    m_vis.run("layout");
                    m_vis.run("scrollToSelectedAndEdit");

                    revalidate();
                    repaint();
                }
                dataAltered();
            }
        };

        static final String commitAndCreateNextDefaultNodeString = "commitAndCreateNextDefaultNode";
        final AbstractAction commitAndCreateNextDefaultNode = new AbstractAction("Commit And Create next default") {
            private static final long serialVersionUID = 1658086056088672748L;

            @Override
            public void actionPerformed(ActionEvent e) {
                commitAreaText();
                stopPendingEdge();
                createNextDefaultNode.actionPerformed(e);
            }
        };


        static final String createContinueTalkingNodeString = "createContinueTalkingNode";
        final AbstractAction createContinueTalkingNode = new AbstractAction("Create next phrase without reply") {
            private static final long serialVersionUID = 1658086056088672748L;

            @Override
            public void actionPerformed(ActionEvent e) {
                commitAreaText();
                stopPendingEdge();
                WriterDialogue newWrNode;
                Node newNode;
                if (selected instanceof WriterDialogue) {
                    EmptyReply temp = data.new EmptyReply((WriterDialogue) selected);
                    newWrNode = data.new WriterDialogue(((WriterDialogue) selected).id_prefix);
                    temp.next_dialogue = newWrNode;

                    newNode = addDialogueNode(newWrNode);
                    /*Edge edge = */
                    graph.addEdge(cells.get(selected), newNode);
                    setSelected(newWrNode);

                    m_vis.run("colors");
                    m_vis.run("layout");
                    m_vis.run("scrollToSelectedAndEdit");
                    revalidate();
                    repaint();
                }
                dataAltered();
            }
        };

        static final String createOtherReplyNodeString = "createOtherReplyNode";
        final AbstractAction createOtherReplyNode = new AbstractAction("Create another reply with the same parent") {
            private static final long serialVersionUID = 1658086056088672748L;

            @Override
            public void actionPerformed(ActionEvent e) {
                commitAreaText();
                stopPendingEdge();
                WriterReply newWrNode;
                Node newNode;
                if (selected instanceof WriterReply) {
                    newWrNode = data.new WriterReply(((WriterReply) selected).parent);
                    newNode = addReplyNode(newWrNode);
                    /*Edge edge = */
                    graph.addEdge(cells.get(((WriterReply) selected).parent), newNode);
                    setSelected(newWrNode);

                    m_vis.run("colors");
                    m_vis.run("layout");
                    m_vis.run("scrollToSelectedAndEdit");
                    revalidate();
                    repaint();
                }
                dataAltered();
            }
        };

        static final String showHelpString = "showHelp";
        final AbstractAction showHelp = new AbstractAction("Show help window") {
            private static final long serialVersionUID = 1658086056088672748L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (helpWindow == null) {
                    createHelpWindow();
                }
                helpWindow.setVisible(true);
            }
        };

        class ScrollToSelectedAction extends Action {

            final boolean openEditor;

            public ScrollToSelectedAction(boolean openEditor) {
                super();
                this.openEditor = openEditor;
            }

            @Override
            public void run(double frac) {
                new Thread() {
                    @Override
                    public void run() {
                        if (selected == null) return;
                        VisualItem newItem = WriterGraphView.this.m_vis.getVisualItem(NODES, cells.get(selected));
                        if (prevSelected != null) {
                            VisualItem prevItem = m_vis.getVisualItem(NODES, cells.get(prevSelected));
                            Point2D target = getScreenCoordinates(new Point2D.Double(prevItem.getX(), prevItem.getY()), null);
                            animatePan(prevItem.getX() - newItem.getX(), prevItem.getY() - newItem.getY(), 200);
                            if (openEditor) showEditorOnSelectedAt(target);
                        } else {
                            animatePanToAbs(new Point2D.Double(newItem.getX(), newItem.getY()), 200);
                        }
                    }
                }.start();
            }
        }


        private void showEditorOnSelected() {
            if (selected == null) return;
            Node selNode = cells.get(selected);
            if (selNode != null) {
                VisualItem vItem = m_vis.getVisualItem(NODES, selNode);
                if (vItem != null) {
                    showEditorOnSelectedAt(getScreenCoordinates(new Point2D.Double(vItem.getX(), vItem.getY()), null));
                }
            }
        }

        JTextArea area;

        private void showEditorOnSelectedAt(final Point2D p) {
            if (overlay != null) disposeOverlay();

            //System.out.println(p);
            area = new JTextArea(selected.text);
            JInternalFrame frame = new JInternalFrame(selected.getTitle(), true);
            frame.getContentPane().setLayout(new BorderLayout());
            JPanel pane = new JPanel();
            pane.setLayout(new BorderLayout());
            pane.add(new JScrollPane(area));
            frame.setSize(250, 80);
            frame.setLocation(new Point((int) p.getX(), (int) p.getY()));
            frame.setVisible(true);
            frame.getContentPane().add(pane, BorderLayout.CENTER);
            ((BasicInternalFrameUI) frame.getUI()).getNorthPane().remove(0);

            JButton commit = new JButton(commitEditAndDisposeEditor);
            commit.setToolTipText("Save text and close editor (Ctrl + Enter)");

            JButton cancel = new JButton(disposeEditor);
            cancel.setToolTipText("Discard changes and close editor (Escape)");


            view.add(frame);
            overlay = frame;
            frame.requestFocus();
            area.requestFocus();
            area.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    disposeOverlay();
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });

            area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true), disposeEditorString);
            area.getActionMap().put(disposeEditorString, disposeEditor);

            area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK, true), commitEditAndDisposeEditorString);
            area.getActionMap().put(commitEditAndDisposeEditorString, commitEditAndDisposeEditor);

            area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK, true), commitAndCreateNextDefaultNodeString);
            area.getActionMap().put(commitAndCreateNextDefaultNodeString, commitAndCreateNextDefaultNode);

            area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true), showHelpString);
            area.getActionMap().put(showHelpString, showHelp);

            if (selected instanceof WriterDialogue) {
                area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true), createContinueTalkingNodeString);
                area.getActionMap().put(createContinueTalkingNodeString, createContinueTalkingNode);
            } else if (selected instanceof WriterReply) {
                area.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true), createOtherReplyNodeString);
                area.getActionMap().put(createOtherReplyNodeString, createOtherReplyNode);
            }

        }


        public void setSelected(WriterNode wrNode) {
            prevSelected = selected;
            selected = wrNode;
        }

        public void selectAndScroll(WriterNode node) {
            if (node != null) {
                setSelected(node);

                m_vis.run("colors");
                m_vis.run("layout");
                m_vis.run("scrollToSelected");

                revalidate();
                repaint();
            }
        }

//        public void selectScrollAndEdit(WriterNode node) {
//			if (node != null) {
//					setSelected(node);
//
//					m_vis.run("colors");
//					m_vis.run("layout");
//					m_vis.run("scrollToSelectedAndEdit");
//
//					revalidate();
//					repaint();
//				}
//		}


        public WriterNode nextNodeRight(WriterNode wrNode) {
            Node node = cells.get(wrNode);
            Node nextNode = graph.getFirstTreeChild(node);
            return nextNode == null ? null : (WriterNode) nextNode.get(TARGET);
        }

        public WriterNode nextNodeLeft(WriterNode wrNode) {
            Node node = cells.get(wrNode);
            Node nextNode = graph.getTreeParent(node);
            return nextNode == null ? null : (WriterNode) nextNode.get(TARGET);
        }

        public WriterNode nextNodeUp(WriterNode wrNode) {
            if (wrNode == null) return null;
            Node node = cells.get(wrNode);
            Node nextNode = graph.getPreviousTreeSibling(node);
            if (nextNode == null) nextNode = findPreviousSiblingWithLastChildAtDepth(node, 0);
            return nextNode == null ? null : (WriterNode) nextNode.get(TARGET);
        }


        private Node findPreviousSiblingWithLastChildAtDepth(Node node, int depth) {
            if (graph.getTreeParent(node) == null) return null;
            Node prevSibl = graph.getPreviousTreeSibling(node);
            if (prevSibl != null) {
                Node candidate = findLastChildAtDepth(prevSibl, depth);
                return candidate != null ? candidate : findPreviousSiblingWithLastChildAtDepth(prevSibl, depth);
            } else {
                return findPreviousSiblingWithLastChildAtDepth(graph.getTreeParent(node), depth + 1);
            }
        }

        private Node findLastChildAtDepth(Node node, int depth) {
            if (depth == 0) return node;
            if (graph.getLastTreeChild(node) != null)
                return findLastChildAtDepth(graph.getLastTreeChild(node), depth - 1);
            return findPreviousSiblingWithLastChildAtDepth(node, depth);
        }


        public WriterNode nextNodeDown(WriterNode wrNode) {
            if (wrNode == null) return null;
            Node node = cells.get(wrNode);
            Node nextNode = graph.getNextTreeSibling(node);
            if (nextNode == null) nextNode = findNextSiblingWithLastChildAtDepth(node, 0);
            return nextNode == null ? null : (WriterNode) nextNode.get(TARGET);
        }


        private Node findNextSiblingWithLastChildAtDepth(Node node, int depth) {
            if (graph.getTreeParent(node) == null) return null;
            Node nextSibl = graph.getNextTreeSibling(node);
            if (nextSibl != null) {
                Node candidate = findFirstChildAtDepth(nextSibl, depth);
                return candidate != null ? candidate : findNextSiblingWithLastChildAtDepth(nextSibl, depth);
            } else {
                return findNextSiblingWithLastChildAtDepth(graph.getTreeParent(node), depth + 1);
            }
        }

        private Node findFirstChildAtDepth(Node node, int depth) {
            if (depth == 0) return node;
            if (graph.getFirstTreeChild(node) != null)
                return findFirstChildAtDepth(graph.getFirstTreeChild(node), depth - 1);
            return findNextSiblingWithLastChildAtDepth(node, depth);
        }


        public Point2D getScreenCoordinates(Point2D abs, Point2D screen) {
            return getTransform().transform(abs, screen);
        }


        public class MyGraph extends Graph {


            private String m_spanningTreeFilter;
            private FilteredSpanningTree m_filteredSpanning = null;

            public MyGraph(boolean directed, String spanningFilterColumn) {
                super(directed);
                m_spanningTreeFilter = spanningFilterColumn;
            }

            public Tree getFilteredSpanningTree() {
                if (m_filteredSpanning == null)
                    return getFilteredSpanningTree((Node) nodes().next());
                else
                    return m_filteredSpanning;
            }

            public Tree getFilteredSpanningTree(Node root) {
                nodeCheck(root, true);
                if (m_filteredSpanning == null) {
                    m_filteredSpanning = new FilteredSpanningTree(this, root, m_spanningTreeFilter);
                } else if (m_filteredSpanning.getRoot() != root) {
                    m_filteredSpanning.buildSpanningTree(root);
                }
                return m_filteredSpanning;
            }

            @Override
            protected void updateDegrees(int e, int s, int t, int incr) {
                super.updateDegrees(e, s, t, incr);
                clearFilteredSpanningTree();
            }

            public void clearFilteredSpanningTree() {
                m_filteredSpanning = null;
            }

            public Node getTreeParent(Node n) {
                return getFilteredSpanningTree().getParent(n);
            }

            public Node getNextTreeSibling(Node n) {
                return getFilteredSpanningTree().getNextSibling(n);
            }

            public Node getPreviousTreeSibling(Node n) {
                return getFilteredSpanningTree().getPreviousSibling(n);
            }

            public Node getFirstTreeChild(Node n) {
                return getFilteredSpanningTree().getFirstChild(n);
            }

            public Node getLastTreeChild(Node n) {
                return getFilteredSpanningTree().getLastChild(n);
            }
        }

//    	class TooltipControl extends ControlAdapter {
//		
//		@Override
//		public void itemEntered(VisualItem item, MouseEvent e) {
//			if (item.get(TARGET) != null) {
//				tooltippedItem = item;
//				if (!tooltipActivated) {
//					setToolTipText("");
//					ToolTipManager.sharedInstance().registerComponent(WriterGraphView.this);
//					ToolTipManager.sharedInstance().setEnabled(true);
//					tooltipActivated = true;
//				}
//			}
//		}
//		@Override
//		public void itemExited(VisualItem item, MouseEvent e) {
//			//Hides the tooltip...
//			ToolTipManager.sharedInstance().setEnabled(false);
//			ToolTipManager.sharedInstance().unregisterComponent(WriterGraphView.this);
//			tooltipActivated = false;
//		}
//	}
//	
//	JToolTip tt = null;
//	private VisualItem tooltippedItem = null;
//	private VisualItem lastTTItem = null;
//    private boolean tooltipActivated = false;
//	
//	@Override
//	public Point getToolTipLocation(MouseEvent event) {
//		return new Point(event.getX() + 5, event.getY() + 5);
//	}
//	
//	@Override
//	public JToolTip createToolTip() {
//		if (tt == null) tt = super.createToolTip();
//		if (tooltippedItem == lastTTItem) {
//			return tt;
//		}
//		tt = super.createToolTip();
//		lastTTItem = tooltippedItem;
//    	tt.setLayout(new BorderLayout());
//    	JPanel content = new JPanel();
//    	content.setLayout(new JideBoxLayout(content, JideBoxLayout.PAGE_AXIS));
//    	JLabel label;
//    	if (tooltippedItem != null) {
//    		Object target = tooltippedItem.get(TARGET);
//    		if (target != null) {
//    			if (target instanceof Dialogue) {
//    				Dialogue d = (Dialogue) target;
//    				label = new JLabel(new ImageIcon(DefaultIcons.getDialogueIcon()));
//    				label.setText(d.id);
//    				content.add(label, JideBoxLayout.FIX);
//    				if (tooltippedItem.get(REPLY) == null) {
//    					if (d.rewards != null && !d.rewards.isEmpty()) {
//    						for (Dialogue.Reward r : d.rewards) {
//    							label = new JLabel();
//    							DialogueEditor.decorateRewardJLabel(label, r);
//    							content.add(label, JideBoxLayout.FIX);
//    						}
//    					}
//    				} else { 
//    					Object replObj = tooltippedItem.get(REPLY);
//    					if (replObj instanceof Dialogue.Reply) {
//    						Dialogue.Reply r = (Dialogue.Reply) replObj;
//    						if (r.requirements != null && !r.requirements.isEmpty()) {
//    							for (Requirement req : r.requirements) {
//    								label = new JLabel();
//    								DialogueEditor.decorateRequirementJLabel(label, req);
//        							content.add(label, JideBoxLayout.FIX);
//    							}
//    						}
//    					}
//    				}
//    			}
//    		}
//    		
//    	}
//    	
//    	tt.add(content, BorderLayout.CENTER);
//    	tt.setPreferredSize(tt.getLayout().preferredLayoutSize(tt));
//    	return tt;
//	}

    }

    @Override
    public void targetUpdated() {
        this.icon = new ImageIcon(((GameDataElement) target).getIcon());
        this.name = ((GameDataElement) target).getDesc();
    }

}
