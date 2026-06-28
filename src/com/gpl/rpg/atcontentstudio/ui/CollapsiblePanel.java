package com.gpl.rpg.atcontentstudio.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class CollapsiblePanel extends JPanel {

    private static final long serialVersionUID = 319384990345722150L;

    String title;
    TitledBorder border;

    // Simple dashed border painter to match other controls' focus indicator
        private record DashedBorder(Color color, int thickness, float[] dash) implements Border {
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
                    int tx = x + thickness / 2;
                    int ty = y + thickness / 2;
                    int tw = Math.max(0, width - thickness);
                    int th = Math.max(0, height - thickness);
                    g2.drawRect(tx, ty, tw, th);
                } finally {
                    g2.dispose();
                }
            }
        }

    public CollapsiblePanel(String title) {
        super();
        this.title = title;
        border = BorderFactory.createTitledBorder(title);
        setBorder(border);
        // Make the titled panel focusable so it can receive focus via tab traversal
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);
        InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "toggle");
        getActionMap().put("toggle", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleVisibility();
            }
        });

        // Highlight border when focused.  This should follow L&F style instead, but I can't figure out how to do that.
        Color focusColor = UIManager.getColor("TextField.focus");
        if (focusColor == null) focusColor = new Color(128, 128, 128);
        final javax.swing.border.Border originalBorder = border;
        final javax.swing.border.Border dashedFocus = new DashedBorder(focusColor, 1, new float[]{2f,2f});
        final javax.swing.border.Border focusedBorder = BorderFactory.createCompoundBorder(dashedFocus, originalBorder);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setBorder(focusedBorder);
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                setBorder(originalBorder);
                repaint();
            }
        });

        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);
        addMouseListener(mouseListener);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && isTitleClick(e)) {
                    requestFocusInWindow();
                    toggleVisibility();
                }
            }
        };

    private boolean isTitleClick(MouseEvent e) {
        Insets insets = border.getBorderInsets(this);
        FontMetrics fm = getFontMetrics(getFont());
        // Top inset tracks the titled-border header band in all current LAFs.
        int titleBandHeight = Math.max(insets.top, fm.getHeight() + 6);
        return e.getY() >= 0 && e.getY() <= titleBandHeight;
    }

    ComponentListener contentComponentListener = new ComponentAdapter() {
        @Override
        public void componentShown(ComponentEvent e) {
            updateBorderTitle();
        }

        @Override
        public void componentHidden(ComponentEvent e) {
            updateBorderTitle();
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;
        firePropertyChange("title", oldTitle, this.title);
        updateBorderTitle();
    }

    @Override
    public Component add(Component comp) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(String name, Component comp) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(name, comp);
        updateBorderTitle();
        return r;
    }

    @Override
    public Component add(Component comp, int index) {
        comp.addComponentListener(contentComponentListener);
        Component r = super.add(comp, index);
        updateBorderTitle();
        return r;
    }

    @Override
    public void add(Component comp, Object constraints) {
        comp.addComponentListener(contentComponentListener);
        super.add(comp, constraints);
        updateBorderTitle();
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        comp.addComponentListener(contentComponentListener);
        super.add(comp, constraints, index);
        updateBorderTitle();
    }

    @Override
    public void remove(int index) {
        Component comp = getComponent(index);
        comp.removeComponentListener(contentComponentListener);
        super.remove(index);
    }

    @Override
    public void remove(Component comp) {
        comp.removeComponentListener(contentComponentListener);
        super.remove(comp);
    }

    @Override
    public void removeAll() {
        for (Component c : getComponents()) {
            c.removeComponentListener(contentComponentListener);
        }
        super.removeAll();
    }

    protected void toggleVisibility() {
        toggleVisibility(hasInvisibleComponent());
    }

    protected void toggleVisibility(boolean visible) {
        for (Component c : getComponents()) {
            c.setVisible(visible);
        }
        updateBorderTitle();
    }

    protected void updateBorderTitle() {
        String arrow = "";
        if (getComponentCount() > 0) {
            arrow = (hasInvisibleComponent() ? "[+] " : "[-] ");
        }
        border.setTitle(arrow + title);
        repaint();
    }

    protected final boolean hasInvisibleComponent() {
        for (Component c : getComponents()) {
            if (!c.isVisible()) {
                return true;
            }
        }
        return false;
    }

    public void collapse() {
        toggleVisibility(false);
    }

    public void expand() {
        toggleVisibility(true);
    }

    public void setExpanded(boolean expand) {
        toggleVisibility(expand);
    }

} 