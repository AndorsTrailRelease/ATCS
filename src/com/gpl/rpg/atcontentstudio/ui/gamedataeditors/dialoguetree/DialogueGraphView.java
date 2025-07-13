package com.gpl.rpg.atcontentstudio.ui.gamedataeditors.dialoguetree;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Workspace;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.model.gamedata.Requirement;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.ui.gamedataeditors.DialogueEditor;
import com.gpl.rpg.atcontentstudio.utils.TextUtils;
import com.gpl.rpg.atcontentstudio.utils.WeblateIntegration;
import com.gpl.rpg.atcontentstudio.utils.WeblateIntegration.WeblateTranslationUnit;
import com.jidesoft.swing.JideBoxLayout;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DialogueGraphView extends Display {

    private static final long serialVersionUID = -6431503090775579301L;

    public static final String GRAPH = "graph";
    public static final String NODES = "graph.nodes";
    public static final String EDGES = "graph.edges";
    public static final String EDGES_LABELS = "edgesLabels";

    public static final String LABEL = "label";
    public static final String ICON = "icon";
    public static final String TARGET = "target";
    public static final String REPLY = "reply";
    public static final String HIDDEN_REPLY = "hidden_reply";
    public static final String HAS_REQS = "has_reqs";

    private static final String TRANSLATION_LOADING = "Loading translation...";
    private String translationHeader = "\n---[ Translation from weblate ]---\n";


    private static final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    private Dialogue dialogue;
    private Image npcIcon;
    private Graph graph;
    private Boolean translatorMode;

    private Map<Dialogue, Node> cells = new HashMap<Dialogue, Node>();

    public DialogueGraphView(Dialogue dialogue, NPC npc) {
        super(new Visualization());
        this.dialogue = dialogue;
        if (npc != null) {
            npcIcon = npc.getIcon();
        } else {
            npcIcon = DefaultIcons.getNPCIcon();
        }
        translatorMode = Workspace.activeWorkspace.settings.useInternet.getCurrentValue() && Workspace.activeWorkspace.settings.translatorLanguage.getCurrentValue() != null;
        if (translatorMode) {
            translationHeader = "\n---[ Translation in " + Workspace.activeWorkspace.settings.translatorLanguage.getCurrentValue() + " ]---\n";
        }
        loadGraph();

        // add visual data groups
        m_vis.addGraph(GRAPH, graph);
        m_vis.setInteractive(EDGES, null, false);

        LabelRenderer nodeR = new MyLabelRenderer(LABEL);
        nodeR.setHorizontalTextAlignment(prefuse.Constants.LEFT);

        EdgeRenderer edgeR = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_LINE, prefuse.Constants.EDGE_ARROW_FORWARD);
//        edgeR.setEdgeType(prefuse.Constants.EDGE_TYPE_CURVE);

        LabelRenderer edgeLabelR = new LabelRenderer(LABEL);
        edgeLabelR.setRenderType(LabelRenderer.RENDER_TYPE_DRAW);

        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(nodeR);
        drf.setDefaultEdgeRenderer(edgeR);
        drf.add(new InGroupPredicate(EDGES_LABELS), edgeLabelR);
        m_vis.setRendererFactory(drf);
        DECORATOR_SCHEMA.setDefault(VisualItem.FILLCOLOR, ColorLib.gray(255));
        DECORATOR_SCHEMA.setDefault(VisualItem.STROKECOLOR, ColorLib.rgba(0, 0, 0, 0));
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, ColorLib.gray(0));
        m_vis.addDecorators(EDGES_LABELS, EDGES, DECORATOR_SCHEMA);

        // set up the visual operators
        // first set up all the color actions
        ColorAction nStrokeColor = new ColorAction(NODES, VisualItem.STROKECOLOR);
        nStrokeColor.setDefaultColor(ColorLib.gray(100));
        nStrokeColor.add("_hover", ColorLib.rgb(255, 100, 100));
        StrokeAction nStroke = new EdgesStrokeAction(NODES);

        ColorAction nFill = new NPCPhraseColorAction(NODES, VisualItem.FILLCOLOR);

        ColorAction eEdges = new ConnectedEdgeColorAction(EDGES, VisualItem.STROKECOLOR);
        ColorAction eArrows = new ConnectedEdgeColorAction(EDGES, VisualItem.FILLCOLOR);
        ColorAction eEdgesLabels = new ConnectedEdgeColorAction(EDGES_LABELS, VisualItem.TEXTCOLOR);

        StrokeAction eStroke = new EdgesStrokeAction(EDGES);

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
        colors.add(eEdgesLabels);
        colors.add(eStroke);
        colors.add(aFont);
        colors.add(aFontColor);
        colors.add(new RepaintAction());
        m_vis.putAction("colors", colors);

        // now create the main layout routine
        ActionList layout = new ActionList();//Activity.INFINITY);
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(GRAPH, prefuse.Constants.ORIENT_LEFT_RIGHT, 120, translatorMode ? 80 : 40, translatorMode ? 80 : 40);
        treeLayout.setLayoutAnchor(new Point2D.Double(25, 300));
        layout.add(treeLayout);
        layout.add(new EdgesLabelDecoratorLayout(EDGES_LABELS));
        layout.add(new RepaintAction());
        m_vis.putAction("layout", layout);

        // set up the display
        setSize(500, 500);
        pan(250, 250);
        setHighQuality(true);
        addControlListener(new TooltipControl());
        addControlListener(new DoubleClickControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());

        // set things running
        m_vis.run("colors");
        m_vis.run("layout");
    }

    public void loadGraph() {
        graph = new Graph(true);
        graph.addColumn(LABEL, String.class, "");
        graph.addColumn(ICON, Image.class, DefaultIcons.getNullifyIcon());
        graph.addColumn(TARGET, GameDataElement.class, null);
        graph.addColumn(REPLY, Dialogue.Reply.class, null);
        graph.addColumn(HIDDEN_REPLY, Dialogue.Reply.class, null);
        graph.addColumn(HAS_REQS, boolean.class, false);
        addDialogue(dialogue, npcIcon);
    }

    public Node addDialogue(Dialogue dialogue, Image npcIcon) {
        if (cells.get(dialogue) == null) {
            if (dialogue.switch_to_npc != null) {
                npcIcon = dialogue.switch_to_npc.getIcon();
            }
            final Node dNode = graph.addNode();
            cells.put(dialogue, dNode);
            String label;
            Thread t = null;
            if (dialogue.message == null) {
                label = "[Selector]";
            } else if (translatorMode) {
                label = dialogue.message + translationHeader + TRANSLATION_LOADING;
                final String message = dialogue.message;
                t = new Thread("Get weblate translation for " + message) {
                    public void run() {
                        WeblateTranslationUnit unit = WeblateIntegration.getTranslationUnit(message);
                        dNode.setString(LABEL, message + translationHeader + unit.translatedText);
                    }

                };
            } else {
                label = dialogue.message;
            }
            dNode.setString(LABEL, label);
            if (t != null) t.start();
            dNode.set(ICON, npcIcon);
            dNode.set(TARGET, dialogue);
            if (dialogue.replies != null) {
                Node rNode;
                int i = 1;
                for (Dialogue.Reply r : dialogue.replies) {
                    rNode = addReply(dialogue, r, npcIcon);
                    Edge e = graph.addEdge(dNode, rNode);
                    e.setString(LABEL, "#" + i++);
                    e.setBoolean(HAS_REQS, r.requirements != null && !r.requirements.isEmpty());
                }
            }
        }
        return cells.get(dialogue);
    }

    public Node addReply(Dialogue d, Dialogue.Reply r, Image npcIcon) {
        final Node rNode;
        if (r.text != null && !r.text.equals(Dialogue.Reply.GO_NEXT_TEXT)) {
            //Normal reply...
            rNode = graph.addNode();
//			rNode.setString(LABEL, translatorMode ? r.text + "\n---\n" + WeblateIntegration.getTranslationUnit(r.text).translatedText : r.text);
            String label;
            Thread t = null;
            if (translatorMode) {
                label = r.text + translationHeader + TRANSLATION_LOADING;
                final String message = r.text;
                t = new Thread("Get weblate translation for " + message) {
                    public void run() {
                        WeblateTranslationUnit unit = WeblateIntegration.getTranslationUnit(message);
                        rNode.setString(LABEL, message + translationHeader + unit.translatedText);
                    }

                };
            } else {
                label = r.text;
            }
            rNode.setString(LABEL, label);
            if (t != null) t.start();
            rNode.set(ICON, DefaultIcons.getHeroIcon());
            rNode.set(TARGET, d);
            rNode.set(REPLY, r);
            if (r.next_phrase != null) {
                //...that leads to another phrase
                Node dNode = addDialogue(r.next_phrase, npcIcon);
                graph.addEdge(rNode, dNode);
            } else if (Dialogue.Reply.KEY_PHRASE_ID.contains(r.next_phrase_id)) {
                //...that leads to a key phrase
                Node kNode = addKeyPhraseNode(d, r.next_phrase_id);
                kNode.set(REPLY, r);
                graph.addEdge(rNode, kNode);
            }
        } else if (r.next_phrase != null) {
            //Go directly to next phrase
            rNode = addDialogue(r.next_phrase, npcIcon);
            //Add a pointer to the hidden reply, in order to fetch requirements later.
            rNode.set(HIDDEN_REPLY, r);
        } else if (Dialogue.Reply.KEY_PHRASE_ID.contains(r.next_phrase_id)) {
            //Go directly to key phrase
            rNode = addKeyPhraseNode(d, r.next_phrase_id);
            rNode.set(REPLY, r);
        } else {
            //Incomplete.
            rNode = graph.addNode();
            rNode.setString(LABEL, "[Incomplete reply]");
            rNode.set(ICON, DefaultIcons.getNullifyIcon());
        }
        return rNode;
    }

    public Node addKeyPhraseNode(Dialogue d, String key) {
        Node kNode = graph.addNode();
        if (key.equals(Dialogue.Reply.EXIT_PHRASE_ID)) {
            kNode.setString(LABEL, "[Ends dialogue]");
            kNode.set(ICON, DefaultIcons.getNullifyIcon());
        } else if (key.equals(Dialogue.Reply.FIGHT_PHRASE_ID)) {
            kNode.setString(LABEL, "[Starts fight]");
            kNode.set(ICON, DefaultIcons.getCombatIcon());
        } else if (key.equals(Dialogue.Reply.REMOVE_PHRASE_ID)) {
            kNode.setString(LABEL, "[NPC vanishes]");
            kNode.set(ICON, DefaultIcons.getNPCCloseIcon());
        } else if (key.equals(Dialogue.Reply.SHOP_PHRASE_ID)) {
            kNode.setString(LABEL, "[Start trading]");
            kNode.set(ICON, DefaultIcons.getGoldIcon());
        } else {
            //Should never reach, unless new key phrase ID are added to the model, but not here...
            kNode.setString(LABEL, "[WTF !!]" + key);
        }
        kNode.set(TARGET, d);
        return kNode;
    }

    class MyLabelRenderer extends LabelRenderer {
        public MyLabelRenderer(String label) {
            super(label);
        }

        @Override
        protected Image getImage(VisualItem item) {
            return (Image) item.get(ICON);
        }

        @Override
        protected String getText(VisualItem item) {
            return TextUtils.wordWrap(super.getText(item), 40);
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
                if (((EdgeItem) item).getSourceItem().isHover()) {
                    return outgoing;
                } else if (((EdgeItem) item).getTargetItem().isHover()) {
                    return incoming;
                }
            }

            return none;
        }
    }

    class NPCPhraseColorAction extends ColorAction {

        final int none = ColorLib.gray(255);
        final int hover = ColorLib.gray(200);
        final int has_rewards = ColorLib.rgb(255, 255, 0);
        final int has_rewards_hover = ColorLib.rgb(200, 200, 0);


        public NPCPhraseColorAction(String group, String field) {
            super(group, field);
        }

        @Override
        public int getColor(VisualItem item) {
            // Change color for Dialogues, not replies.
            if (item.get(TARGET) != null && item.get(REPLY) == null) {
                Dialogue d = (Dialogue) item.get(TARGET);
                if (d.rewards != null && !d.rewards.isEmpty()) {
                    if (item.isHover()) {
                        return has_rewards_hover;
                    } else {
                        return has_rewards;
                    }
                }
            }
            if (item.isHover()) {
                return hover;
            } else {
                return none;
            }
        }
    }

    class EdgesStrokeAction extends StrokeAction {

        public final BasicStroke req_stroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{3.5f, 2.5f}, 1.0f);
        public final BasicStroke hover_stroke = new BasicStroke(3.0f);
        public final BasicStroke hover_req_stroke = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[]{3.5f, 2.5f}, 1.0f);

        public EdgesStrokeAction(String group) {
            super(group);
        }

        @Override
        public BasicStroke getStroke(VisualItem item) {
            if (item.getBoolean(HAS_REQS)) {
                if (item instanceof EdgeItem) {
                    if (((EdgeItem) item).getSourceItem().isHover()) {
                        return hover_req_stroke;
                    } else if (((EdgeItem) item).getTargetItem().isHover()) {
                        return hover_req_stroke;
                    }
                }
                return req_stroke;
            } else {
                if (item.isHover()) {
                    return hover_stroke;
                } else if (item instanceof EdgeItem) {
                    if (((EdgeItem) item).getSourceItem().isHover()) {
                        return hover_stroke;
                    } else if (((EdgeItem) item).getTargetItem().isHover()) {
                        return hover_stroke;
                    }
                }
            }
            return super.getStroke(item);
        }

    }

    class EdgesLabelDecoratorLayout extends Layout {
        public EdgesLabelDecoratorLayout(String group) {
            super(group);
        }

        public void run(double frac) {
            Iterator<?> iter = m_vis.items(m_group);
            while (iter.hasNext()) {
                DecoratorItem decorator = (DecoratorItem) iter.next();
                if (decorator.getDecoratedItem() instanceof EdgeItem) {
                    EdgeItem edgeItem = (EdgeItem) decorator.getDecoratedItem();
                    double deltaX = edgeItem.getTargetItem().getX() - edgeItem.getSourceItem().getX();
                    double deltaY = edgeItem.getTargetItem().getY() - edgeItem.getSourceItem().getY();
                    double hypo = Math.hypot(deltaX, deltaY);

                    double edgePropX = deltaX / hypo;
                    double edgePropY = deltaY / hypo;

                    Point2D start = new Point2D.Double(edgeItem.getSourceItem().getBounds().getCenterX(), edgeItem.getSourceItem().getBounds().getCenterY());
                    Point2D end = new Point2D.Double(edgeItem.getTargetItem().getBounds().getCenterX(), edgeItem.getTargetItem().getBounds().getCenterY());
                    Point2D[] realStart = new Point2D[]{new Point2D.Double(), new Point2D.Double()};
//	            	Point2D[] realEnd = new Point2D[]{new Point2D.Double(), new Point2D.Double()};

                    int i = GraphicsLib.intersectLineRectangle(start, end, edgeItem.getSourceItem().getBounds(), realStart);
                    if (i > 0) {
                        start = realStart[0];
                    }
//	            	i = GraphicsLib.intersectLineRectangle(start, end, edgeItem.getTargetItem().getBounds(), realEnd);
//	            	if (i > 0) {
//	            		end = realEnd[0];
//	            	}

                    double coef = 20 * Math.atan(hypo / 100.0) + 20;

                    setX(decorator, null, start.getX() + coef * edgePropX + 6 * Math.random() - 3);
                    setY(decorator, null, start.getY() + coef * edgePropY + 6 * Math.random() - 3);
                } else {
                    VisualItem decoratedItem = decorator.getDecoratedItem();
                    Rectangle2D bounds = decoratedItem.getBounds();
                    double x = bounds.getCenterX();
                    double y = bounds.getCenterY();
                    setX(decorator, null, x + 10 * Math.random() - 5);
                    setY(decorator, null, y + 10 * Math.random() - 5);
                }
            }
        }
    }

    class DoubleClickControl extends ControlAdapter {
        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            if (e.getClickCount() == 2) {
                if (item.get(TARGET) != null) {
                    ATContentStudio.frame.openEditor((GameDataElement) item.get(TARGET));
                }
            }
        }
    }

    class TooltipControl extends ControlAdapter {

        @Override
        public void itemEntered(VisualItem item, MouseEvent e) {
            if (item.get(TARGET) != null) {
                tooltippedItem = item;
                if (!tooltipActivated) {
                    setToolTipText("");
                    ToolTipManager.sharedInstance().registerComponent(DialogueGraphView.this);
                    ToolTipManager.sharedInstance().setEnabled(true);
                    tooltipActivated = true;
                }
            }
        }

        @Override
        public void itemExited(VisualItem item, MouseEvent e) {
            //Hides the tooltip...
            ToolTipManager.sharedInstance().setEnabled(false);
            ToolTipManager.sharedInstance().unregisterComponent(DialogueGraphView.this);
            tooltipActivated = false;
        }
    }

    JToolTip tt = null;
    private VisualItem tooltippedItem = null;
    private VisualItem lastTTItem = null;
    private boolean tooltipActivated = false;

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        return new Point(event.getX() + 5, event.getY() + 5);
    }

    @Override
    public JToolTip createToolTip() {
        if (tt == null) tt = super.createToolTip();
        if (tooltippedItem == lastTTItem) {
            return tt;
        }
        tt = super.createToolTip();
        lastTTItem = tooltippedItem;
        tt.setLayout(new BorderLayout());
        JPanel content = new JPanel();
        content.setLayout(new JideBoxLayout(content, JideBoxLayout.PAGE_AXIS));
        JLabel label;
        if (tooltippedItem != null) {
            Object target = tooltippedItem.get(TARGET);
            if (target != null) {
                if (target instanceof Dialogue) {
                    Dialogue d = (Dialogue) target;
                    label = new JLabel(new ImageIcon(DefaultIcons.getDialogueIcon()));
                    label.setText(d.id);
                    content.add(label, JideBoxLayout.FIX);
                    Object replObj = tooltippedItem.get(REPLY);
                    if (replObj == null) {
                        replObj = tooltippedItem.get(HIDDEN_REPLY);
                    }
                    if (replObj != null && replObj instanceof Dialogue.Reply) {
                        Dialogue.Reply r = (Dialogue.Reply) replObj;
                        if (r.requirements != null && !r.requirements.isEmpty()) {
                            JLabel reqTitle = new JLabel("--Requirements--", SwingConstants.CENTER);
                            content.add(reqTitle, JideBoxLayout.FIX);
                            for (Requirement req : r.requirements) {
                                label = new JLabel("", SwingConstants.CENTER);
                                DialogueEditor.decorateRequirementJLabel(label, req);
                                content.add(label, JideBoxLayout.FIX);
                            }
                        }
                    }
                    if (d.rewards != null && !d.rewards.isEmpty()) {
                        JLabel rewTitle = new JLabel("--Rewards--", SwingConstants.CENTER);
                        rewTitle.setAlignmentY(CENTER_ALIGNMENT);
                        content.add(rewTitle, JideBoxLayout.FIX);
                        for (Dialogue.Reward r : d.rewards) {
                            label = new JLabel("", SwingConstants.CENTER);
                            DialogueEditor.decorateRewardJLabel(label, r);
                            content.add(label, JideBoxLayout.FIX);
                        }
                    }
                }
            }

        }

        tt.add(content, BorderLayout.CENTER);
        tt.setPreferredSize(tt.getLayout().preferredLayoutSize(tt));
        return tt;
    }

}


