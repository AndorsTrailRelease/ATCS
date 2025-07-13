package com.gpl.rpg.atcontentstudio.ui.sprites;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet;
import com.gpl.rpg.atcontentstudio.model.sprites.Spritesheet.Category;
import com.gpl.rpg.atcontentstudio.utils.SpriteUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class SpriteChooser extends JDialog {

    private static final long serialVersionUID = -6018113265015159521L;

    private static final int STD_WIDTH = 32;
    private static final int STD_HEIGHT = 32;
    private static final int MAX_PER_ROW = 10;

    public static Map<Project, Map<Spritesheet.Category, SpriteChooser>> cache = new LinkedHashMap<Project, Map<Spritesheet.Category, SpriteChooser>>();
    public static Map<Project, Map<Spritesheet.Category, List<Spritesheet>>> cacheValidator = new LinkedHashMap<Project, Map<Category, List<Spritesheet>>>();


    public static SpriteChooser getChooser(Project proj, Spritesheet.Category category) {
        if (cache.get(proj) == null) {
            cache.put(proj, new LinkedHashMap<Spritesheet.Category, SpriteChooser>());
        }
        if (cache.get(proj).get(category) == null) {
            cache.get(proj).put(category, new SpriteChooser(proj, category));
        } else {
            List<Spritesheet> spritesheets = new ArrayList<Spritesheet>();
            for (int i = 0; i < proj.getSpritesheetCount(); i++) {
                Spritesheet sheet = proj.getSpritesheet(i);
                if (sheet.category == category) {
                    spritesheets.add(sheet);
                }
            }
            if (!spritesheets.equals(cacheValidator.get(proj).get(category))) {
                cache.get(proj).put(category, new SpriteChooser(proj, category));
            }
        }
        SpriteChooser wanted = cache.get(proj).get(category);
        wanted.group.clearSelection();
        wanted.selectedIconId = null;
//		wanted.selectedOne = null;
        wanted.listener = null;
//		wanted.ok.setEnabled(false);
        wanted.pack();
        return wanted;
    }

    private ButtonGroup group;
//	private IconButton selectedOne = null;
//	private JButton ok;
//	private JButton cancel;

    public String selectedIconId = null;

    public SpriteChooser(Project proj, Category category) {
        super(ATContentStudio.frame);
        setTitle("Select a sprite");
        setModalityType(ModalityType.APPLICATION_MODAL);
        List<Spritesheet> spritesheets = new ArrayList<Spritesheet>();
        for (int i = 0; i < proj.getSpritesheetCount(); i++) {
            Spritesheet sheet = proj.getSpritesheet(i);
            if (sheet.category == category) {
                spritesheets.add(sheet);
            }
        }
        if (cacheValidator.get(proj) == null) {
            cacheValidator.put(proj, new LinkedHashMap<Spritesheet.Category, List<Spritesheet>>());
        }
        cacheValidator.get(proj).put(category, spritesheets);


        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;

        List<Point> reservedSlots = new ArrayList<Point>();
        Point nextFreeSlot = new Point(0, 0);

        int i;
        BufferedImage img;
        group = new ButtonGroup();
        //For every sprite find a free space in panel:
        for (Spritesheet sheet : spritesheets) {
            i = -1;
            while ((img = sheet.getImage(++i)) != null) {
                if (SpriteUtils.checkIsImageEmpty(img)) {
                    continue;
                }
                IconButton button = new IconButton(img, sheet.id, i);
                group.add(button);
                if (sheet.spriteWidth == STD_WIDTH && sheet.spriteHeight == STD_HEIGHT) {
                    pane.add(button, c);
                    c.gridx++;
                    if (c.gridx >= MAX_PER_ROW) {
                        c.gridx = 0;
                        c.gridy++;
                    }
                    nextFreeSlot.setLocation(c.gridx, c.gridy);
                } else {
                    c.gridwidth = (sheet.spriteWidth / STD_WIDTH) + (sheet.spriteWidth % STD_WIDTH == 0 ? 0 : 1);
                    c.gridheight = (sheet.spriteHeight / STD_HEIGHT) + (sheet.spriteHeight % STD_HEIGHT == 0 ? 0 : 1);
                    boolean slotOk = false;
                    while (!slotOk) {
                        slotOk = true;
                        for (int x = c.gridx; x < c.gridx + c.gridwidth; x++) {
                            for (int y = c.gridy; y < c.gridy + c.gridwidth; y++) {
                                if (reservedSlots.contains(new Point(x, y))) {
                                    slotOk = false;
                                    break;
                                }
                            }
                            if (!slotOk) {
                                break;
                            }
                        }
                        if (c.gridx + c.gridwidth > MAX_PER_ROW) {
                            c.gridx = 0;
                            c.gridy++;
                            slotOk = false;
                        } else if (!slotOk) {
                            c.gridx++;
                        }
                    }
                    pane.add(button, c);
                    for (int x = c.gridx; x < c.gridx + c.gridwidth; x++) {
                        for (int y = c.gridy; y < c.gridy + c.gridwidth; y++) {
                            reservedSlots.add(new Point(x, y));
                        }
                    }
                    c.gridwidth = 1;
                    c.gridheight = 1;
                    c.gridx = nextFreeSlot.x;
                    c.gridy = nextFreeSlot.y;
                }
                while (reservedSlots.contains(nextFreeSlot)) {
                    c.gridx++;
                    if (c.gridx >= MAX_PER_ROW) {
                        c.gridx = 0;
                        c.gridy++;
                    }
                    nextFreeSlot.setLocation(c.gridx, c.gridy);
                }
            }
        }

//		ok = new JButton("Ok");
//		cancel = new JButton("Cancel");

        c.gridx = 0;
        boolean emptyLine = false;
        while (!emptyLine) {
            c.gridy++;
            emptyLine = true;
            for (i = MAX_PER_ROW - 1; i >= 0; i--) {
                if (reservedSlots.contains(new Point(i, c.gridy))) {
                    emptyLine = false;
                    continue;
                }
            }
        }

//		JPanel buttonPane = new JPanel();
//		buttonPane.add(cancel, BorderLayout.WEST);
//		buttonPane.add(ok, BorderLayout.EAST);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        JScrollPane scroller = new JScrollPane(pane);
        scroller.getVerticalScrollBar().setUnitIncrement(16);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        wrapper.add(scroller, BorderLayout.CENTER);
//		wrapper.add(buttonPane, BorderLayout.SOUTH);

//		ok.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectedIconId = selectedOne.sheetId+":"+selectedOne.spriteIndex;
//				SpriteChooser.this.setVisible(false);
//				SpriteChooser.this.dispose();
//				if (listener != null) listener.iconSelected(selectedIconId);
//			}
//		});

//		cancel.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectedIconId = null;
//				SpriteChooser.this.setVisible(false);
//				SpriteChooser.this.dispose();
//				if (listener != null) listener.iconSelected(null);
//			}
//		});

        setContentPane(wrapper);
    }

    private SpriteChooser.SelectionListener listener = null;

    public void setSelectionListener(SpriteChooser.SelectionListener l) {
        listener = l;
    }


    public class IconButton extends JToggleButton {

        private static final long serialVersionUID = 7559407153561178455L;

        public String sheetId;
        public int spriteIndex;

        public IconButton(Image img, String sheetId, int spriteIndex) {
            super(new ImageIcon(img));
            this.sheetId = sheetId;
            this.spriteIndex = spriteIndex;
            setToolTipText(sheetId + ":" + spriteIndex);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (IconButton.this.isSelected()) {
                        selectedIconId = IconButton.this.sheetId + ":" + IconButton.this.spriteIndex;
                        SpriteChooser.this.setVisible(false);
                        SpriteChooser.this.dispose();
                        if (listener != null) listener.iconSelected(selectedIconId);
                    }
                }
            });
        }
    }

    public static interface SelectionListener {
        public void iconSelected(String selected);
    }

}
