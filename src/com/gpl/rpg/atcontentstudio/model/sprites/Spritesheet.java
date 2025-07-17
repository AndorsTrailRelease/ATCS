package com.gpl.rpg.atcontentstudio.model.sprites;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;

import javax.imageio.ImageIO;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class Spritesheet extends GameDataElement {

    private static final long serialVersionUID = -5981708088278528586L;

    public SpriteSheetSet parent;
    public File spritesheetFile;
    public int spriteWidth = 32;
    public int spriteHeight = 32;
    public String id;
    public Category category = Category.none;
    public boolean animated = false;

    public enum Category {
        none,
        monster,
        item,
        actorcondition
    }

    //Lazy initialization.
    public BufferedImage spritesheet = null;
    public Map<Integer, BufferedImage> cache_full_size = new LinkedHashMap<Integer, BufferedImage>();
    public Map<Integer, Image> cache_icon = new LinkedHashMap<Integer, Image>();

    public Spritesheet(SpriteSheetSet parent, File f) {
        this.spritesheetFile = f;
        this.id = f.getName().substring(0, f.getName().lastIndexOf("."));
        this.parent = parent;

        String cat = getProject().getSpritesheetsProperty("atcs.spritesheet." + this.id + ".category");
        if (cat != null) {
            this.category = Category.valueOf(cat);
        }
        String sizex = getProject().getSpritesheetsProperty("atcs.spritesheet." + this.id + ".sizex");
        if (sizex != null) {
            this.spriteWidth = Integer.parseInt(sizex);
        }
        String sizey = getProject().getSpritesheetsProperty("atcs.spritesheet." + this.id + ".sizey");
        if (sizey != null) {
            this.spriteHeight = Integer.parseInt(sizey);
        }
        String anim = getProject().getSpritesheetsProperty("atcs.spritesheet." + this.id + ".animate");
        if (anim != null) {
            this.animated = Boolean.parseBoolean(anim);
        }
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return null;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return null;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return 0;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void childrenAdded(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenAdded(path);
    }

    @Override
    public void childrenChanged(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenChanged(path);
    }

    @Override
    public void childrenRemoved(List<ProjectTreeNode> path) {
        path.add(0, this);
        parent.childrenRemoved(path);
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + spritesheetFile.getName();
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    public int getSpriteCount() {
        if (spritesheet == null) {
            try {
                spritesheet = ImageIO.read(spritesheetFile);
            } catch (IOException e) {
                Notification.addError("Error loading image " + spritesheetFile.getAbsolutePath() + " : " + e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }
        return (int) (Math.ceil(((double) spritesheet.getWidth()) / ((double) spriteWidth)) * Math.ceil(((double) spritesheet.getHeight()) / ((double) spriteHeight)));
    }

    public BufferedImage getImage(int index) {
        if (spritesheet == null) {
            try {
                spritesheet = ImageIO.read(spritesheetFile);
            } catch (IOException e) {
                Notification.addError("Error loading image " + spritesheetFile.getAbsolutePath() + " : " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        if (cache_full_size.get(index) != null) {
            return cache_full_size.get(index);
        }
        BufferedImage result = new BufferedImage(spriteWidth, spriteHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();
        int sx1, sy1;
        sx1 = (index * spriteWidth) % spritesheet.getWidth();
        sy1 = spriteHeight * ((index * spriteWidth) / spritesheet.getWidth());
        if (sx1 + spriteWidth > spritesheet.getWidth() || sy1 + spriteHeight > spritesheet.getHeight()) {
            g.finalize();
            return null;
        }
        g.drawImage(spritesheet, 0, 0, spriteWidth, spriteHeight, sx1, sy1, sx1 + spriteWidth, sy1 + spriteHeight, null);
        result.flush();
        g.finalize();
        cache_full_size.put(index, result);
        return result;
    }

    public Image getIcon(int index) {
        if (cache_icon.get(index) != null) {
            return cache_icon.get(index);
        }
        Image result = getImage(index);
        if (result == null) return null;
        result = result.getScaledInstance((int) (16 * ATContentStudio.SCALING), (int) (16 * ATContentStudio.SCALING), Image.SCALE_SMOOTH);
        cache_icon.put(index, result);
        return result;
    }

    public void clearCache() {
        cache_full_size.clear();
        cache_icon.clear();
    }


    @Override
    public Image getIcon() {
        return getIcon(0);
    }

    @Override
    public Image getLeafIcon() {
        return getIcon();
    }

    @Override
    public Image getClosedIcon() {
        return null;
    }

    @Override
    public Image getOpenIcon() {
        return null;
    }


    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Type getDataType() {
        return parent.getDataType();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void parse() {
        if (this.state == GameDataElement.State.init) {
            this.state = GameDataElement.State.parsed;
        }
    }

    @Override
    public void link() {
        if (this.state == GameDataElement.State.init) {
            this.parse();
        }
        if (this.state == GameDataElement.State.parsed) {
            this.state = GameDataElement.State.linked;
        }
    }

    @Override
    public GameDataElement clone() {
        Spritesheet clone = new Spritesheet((SpriteSheetSet) getParent(), new File(spritesheetFile.getAbsolutePath()));
        clone.id = this.id;
        clone.animated = this.animated;
        clone.category = this.category;
        clone.spriteWidth = this.spriteWidth;
        clone.spriteHeight = this.spriteHeight;
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        //nothing linked.
    }

    @Override
    public String getProjectFilename() {
        return spritesheetFile.getName();
    }

    @Override
    public void save() {
        if (this.category != null)
            getProject().setSpritesheetsProperty("atcs.spritesheet." + this.id + ".category", this.category.toString());
        if (this.spriteWidth != 32)
            getProject().setSpritesheetsProperty("atcs.spritesheet." + this.id + ".sizex", Integer.toString(this.spriteWidth));
        if (this.spriteHeight != 32)
            getProject().setSpritesheetsProperty("atcs.spritesheet." + this.id + ".sizey", Integer.toString(this.spriteHeight));
        if (this.animated)
            getProject().setSpritesheetsProperty("atcs.spritesheet." + this.id + ".animate", Boolean.toString(this.animated));
        getProject().save();

        this.state = GameDataElement.State.saved;
    }

    @Override
    public List<SaveEvent> attemptSave() {
        save();
        return null;
    }
}
