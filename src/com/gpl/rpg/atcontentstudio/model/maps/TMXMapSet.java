package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.Project.ResourceSet;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;

import javax.swing.tree.TreeNode;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TMXMapSet implements ProjectTreeNode {

    public static final String DEFAULT_REL_PATH_IN_SOURCE = "res" + File.separator + "xml" + File.separator;
    public static final String DEFAULT_REL_PATH_IN_PROJECT = "maps" + File.separator;
    public static final String DEFAULT_REL_PATH_TO_DRAWABLE = ".." + File.separator + "drawable" + File.separator;

    public static final String GAME_MAPS_ARRAY_NAME = "loadresource_maps";
    public static final String DEBUG_SUFFIX = "_debug";
    public static final String RESOURCE_PREFIX = "@xml/";
    public static final String FILENAME_SUFFIX = ".tmx";

    public File mapFolder = null;
    public List<TMXMap> tmxMaps;

    public ProjectTreeNode parent;

    public TMXMapSet(GameSource source) {
        this.parent = source;
        if (source.type == GameSource.Type.source) {
            this.mapFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_SOURCE);
        } else if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
            this.mapFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
            if (!this.mapFolder.exists()) {
                this.mapFolder.mkdirs();
            }
            FileUtils.makeSymlink(getProject().baseContent.gameSprites.drawableFolder, new File(mapFolder.getAbsolutePath() + File.separator + DEFAULT_REL_PATH_TO_DRAWABLE));
        }
        this.tmxMaps = new ArrayList<TMXMap>();

        if (source.type == GameSource.Type.source && (source.parent.sourceSetToUse == ResourceSet.debugData || source.parent.sourceSetToUse == ResourceSet.gameData)) {
            String suffix = (source.parent.sourceSetToUse == ResourceSet.debugData) ? DEBUG_SUFFIX : "";

            if (source.referencedSourceFiles.get(GAME_MAPS_ARRAY_NAME + suffix) != null) {
                for (String resource : source.referencedSourceFiles.get(GAME_MAPS_ARRAY_NAME + suffix)) {
                    File f = new File(mapFolder, resource.replaceAll(RESOURCE_PREFIX, "") + FILENAME_SUFFIX);
                    if (f.exists()) {
                        TMXMap map = new TMXMap(this, f);
                        tmxMaps.add(map);
                    } else {
                        Notification.addWarn("Unable to locate resource " + resource + " in the game source for project " + getProject().name);
                    }
                }
            }

        } else if (this.mapFolder != null) {
            for (File f : this.mapFolder.listFiles()) {
                if (f.getName().endsWith(".tmx") || f.getName().endsWith(".TMX")) {
                    TMXMap map = new TMXMap(this, f);
                    if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
                        map.writable = true;
                    }
                    tmxMaps.add(map);
                }
            }
        }
        Collections.sort(tmxMaps, new Comparator<TMXMap>() {
            @Override
            public int compare(TMXMap o1, TMXMap o2) {
                return o1.id.compareTo(o2.id);
            }
        });
        if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
            final Path folderPath = Paths.get(mapFolder.getAbsolutePath());
            Thread watcher = new Thread("Map folder watcher for " + getProject().name + "/" + source.type) {
                public void run() {
                    WatchService watchService;

                    while (getProject().open) {
                        try {
                            watchService = FileSystems.getDefault().newWatchService();
                            /*WatchKey watchKey = */
                            folderPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
                            WatchKey wk;
                            validService:
                            while (getProject().open) {
                                wk = watchService.poll(10, TimeUnit.SECONDS);
                                if (wk != null) {
                                    for (WatchEvent<?> event : wk.pollEvents()) {
                                        Path changed = (Path) event.context();
                                        String name = changed.getFileName().toString();
                                        String id = name.substring(0, name.length() - 4);
                                        TMXMap map = getMap(id);
                                        if (map != null) {
                                            map.mapChangedOnDisk();
                                        }
                                    }
                                    if (!wk.reset()) {
                                        watchService.close();
                                        break validService;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            watcher.start();
        }
    }

    @Override
    public Enumeration<TMXMap> children() {
        return Collections.enumeration(tmxMaps);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return tmxMaps.get(arg0);
    }

    @Override
    public int getChildCount() {
        return tmxMaps.size();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return tmxMaps.indexOf(arg0);
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return false;
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
        if (path.size() == 1 && this.getChildCount() == 1) {
            childrenRemoved(new ArrayList<ProjectTreeNode>());
        } else {
            path.add(0, this);
            parent.childrenRemoved(path);
        }
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
        for (TMXMap map : tmxMaps) {
            map.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + "TMX Maps";
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }


    @Override
    public Image getIcon() {
        return getOpenIcon();
    }

    @Override
    public Image getClosedIcon() {
        return DefaultIcons.getTmxClosedIcon();
    }

    @Override
    public Image getLeafIcon() {
        return DefaultIcons.getTmxClosedIcon();
    }

    @Override
    public Image getOpenIcon() {
        return DefaultIcons.getTmxOpenIcon();
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
        return tmxMaps.isEmpty();
    }

    public TMXMap getMap(String id) {
        if (tmxMaps == null) return null;
        for (TMXMap map : tmxMaps) {
            if (id.equals(map.id)) {
                return map;
            }
        }
        return null;
    }

    public void addMap(TMXMap node) {
        ProjectTreeNode higherEmptyParent = this;
        while (higherEmptyParent != null) {
            if (higherEmptyParent.getParent() != null && ((ProjectTreeNode) higherEmptyParent.getParent()).isEmpty())
                higherEmptyParent = (ProjectTreeNode) higherEmptyParent.getParent();
            else break;
        }
        if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
        tmxMaps.add(node);
        if (node.tmxFile != null) {
            //Altered node.
            node.tmxFile = new File(this.mapFolder, node.tmxFile.getName());
        } else {
            //Created node.
            node.tmxFile = new File(this.mapFolder, node.id + ".tmx");
        }
        node.parent = this;
        if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
        else node.notifyCreated();
    }

    public TMXMap get(int index) {
        return tmxMaps.get(index);
    }

    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : tmxMaps) {
            if (node.needsSaving()) return true;
        }
        return false;
    }

}
