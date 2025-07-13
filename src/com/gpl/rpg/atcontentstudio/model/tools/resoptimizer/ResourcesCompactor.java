package com.gpl.rpg.atcontentstudio.model.tools.resoptimizer;

import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.ActorCondition;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.model.gamedata.Item;
import com.gpl.rpg.atcontentstudio.model.gamedata.NPC;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMapSet;
import com.gpl.rpg.atcontentstudio.model.sprites.SpriteSheetSet;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;
import com.whoischarles.util.json.Minify;
import com.whoischarles.util.json.Minify.UnterminatedCommentException;
import com.whoischarles.util.json.Minify.UnterminatedRegExpLiteralException;
import com.whoischarles.util.json.Minify.UnterminatedStringLiteralException;
import org.json.simple.JSONArray;
import tiled.core.TileSet;
import tiled.io.TMXMapWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.*;

/**
 * @author Kevin
 * <p>
 * To use this, paste the following script in the beanshell console of ATCS.
 * Don't forget to change the project number to suit your needs.
 * <p>
 * <code>
 * import com.gpl.rpg.atcontentstudio.model.tools.resoptimizer.ResourcesCompactor;
 * import com.gpl.rpg.atcontentstudio.model.Workspace;
 *
 * proj = Workspace.activeWorkspace.projects.get(0);
 * new ResourcesCompactor(proj).compactData();
 * </code>
 */
public class ResourcesCompactor {

    public static String DEFAULT_REL_PATH_IN_PROJECT = "compressed" + File.separator;

    private Project proj;
    private File baseFolder;
    private List<CompressedSpritesheet> compressedSpritesheets = new LinkedList<CompressedSpritesheet>();
    private List<File> preservedSpritesheets = new LinkedList<File>();

    private Map<SpritesheetId, SpritesheetId> spritesRelocationForObjects = new LinkedHashMap<SpritesheetId, SpritesheetId>();
    private Integer currentSpritesheetIndexForObjects = 0;
    private CompressedSpritesheet currentSpritesheetForObjects = null;

    private Map<SpritesheetId, SpritesheetId> spritesRelocationForMaps = new LinkedHashMap<SpritesheetId, SpritesheetId>();
    private Map<SpritesheetId, CompressedSpritesheet> spritesheetsBySidForMaps = new LinkedHashMap<SpritesheetId, CompressedSpritesheet>();
    private Integer currentSpritesheetIndexForMaps = 0;
    private CompressedSpritesheet currentSpritesheetForMaps = null;

    public ResourcesCompactor(Project proj) {
        this.proj = proj;
        this.baseFolder = new File(proj.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
        if (!baseFolder.exists()) baseFolder.mkdirs();
    }

    public void compactData() {
        compactJsonData();
        for (CompressedSpritesheet cs : compressedSpritesheets) {
            cs.drawFile();
        }
        for (File preserved : preservedSpritesheets) {
            FileUtils.copyFile(preserved, new File(baseFolder.getAbsolutePath() + File.separator + DEFAULT_DRAWABLE_REL_PATH + File.separator + preserved.getName()));
        }
        compactMaps();
    }

    public void compactJsonData() {
        final List<File> filesCovered = new LinkedList<File>();

        File folder = new File(baseFolder.getAbsolutePath() + File.separator + GameDataSet.DEFAULT_REL_PATH_IN_SOURCE);
        if (!folder.exists()) folder.mkdirs();

        for (ActorCondition ac : proj.baseContent.gameData.actorConditions) {
            if (filesCovered.contains(ac.jsonFile)) continue;
            File currentFile = ac.jsonFile;
            filesCovered.add(currentFile);
            List<Map> dataToSave = new ArrayList<Map>();
            for (ActorCondition acond : proj.baseContent.gameData.actorConditions) {
                if (!acond.jsonFile.equals(currentFile)) continue;
                Map json = acond.toJson();
                json.put("iconID", convertObjectSprite(acond.icon_id).toStringID());
                dataToSave.add(json);
            }
            File target = new File(folder, ac.jsonFile.getName());
            writeJson(dataToSave, target);
        }

        for (Item it : proj.baseContent.gameData.items) {
            if (filesCovered.contains(it.jsonFile)) continue;
            File currentFile = it.jsonFile;
            filesCovered.add(currentFile);
            List<Map> dataToSave = new ArrayList<Map>();
            for (Item item : proj.baseContent.gameData.items) {
                if (!item.jsonFile.equals(currentFile)) continue;
                Map json = item.toJson();
                json.put("iconID", convertObjectSprite(item.icon_id).toStringID());
                dataToSave.add(json);
            }
            File target = new File(folder, it.jsonFile.getName());
            writeJson(dataToSave, target);
        }


        for (NPC np : proj.baseContent.gameData.npcs) {
            if (filesCovered.contains(np.jsonFile)) continue;
            File currentFile = np.jsonFile;
            filesCovered.add(currentFile);
            List<Map> dataToSave = new ArrayList<Map>();
            for (NPC npc : proj.baseContent.gameData.npcs) {
                if (!npc.jsonFile.equals(currentFile)) continue;
                Map json = npc.toJson();
                if (proj.getImage(npc.icon_id).getWidth(null) == TILE_WIDTH_IN_PIXELS || proj.getImage(npc.icon_id).getHeight(null) == TILE_HEIGHT_IN_PIXELS) {
                    json.put("iconID", convertObjectSprite(npc.icon_id).toStringID());
                }
                dataToSave.add(json);
            }
            File target = new File(folder, np.jsonFile.getName());
            writeJson(dataToSave, target);
        }

        File[] remainingFiles = proj.baseContent.gameData.baseFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".json") && !filesCovered.contains(arg0);
            }
        });

        for (File source : remainingFiles) {
            File target = new File(folder, source.getName());
            minifyJson(source, target);
        }
    }

    private Minify jsonMinifier = new Minify();

    private void writeJson(List<Map> dataToSave, File target) {
        StringWriter writer = new JsonPrettyWriter();
        try {
            JSONArray.writeJSONString(dataToSave, writer);
        } catch (IOException e) {
            //Impossible with a StringWriter
        }
        String toWrite = writer.toString();
        try {
            FileWriter w = new FileWriter(target);
            w.write(jsonMinifier.minify(toWrite));
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void minifyJson(File source, File target) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FileInputStream fis = new FileInputStream(source);
            jsonMinifier.minify(fis, baos);
            FileWriter w = new FileWriter(target);
            w.write(baos.toString());
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnterminatedRegExpLiteralException e) {
            e.printStackTrace();
        } catch (UnterminatedCommentException e) {
            e.printStackTrace();
        } catch (UnterminatedStringLiteralException e) {
            e.printStackTrace();
        }
    }

    private void compactMaps() {
        for (TMXMap map : proj.baseContent.gameMaps.tmxMaps) {
            TMXMap clone = map.clone();
            for (GameDataElement gde : clone.getBacklinks()) {
                gde.removeBacklink(clone);
            }
            clone.getBacklinks().clear();
            tiled.core.Map tmx = clone.tmxMap;
            compactMap(tmx, map.id);
            clone.tmxMap = null;
            clone.groups.clear();
        }
    }

    private void compactMap(tiled.core.Map tmx, String name) {
        File target = new File(baseFolder.getAbsolutePath() + File.separator + TMXMapSet.DEFAULT_REL_PATH_IN_SOURCE + File.separator + name + ".tmx");
        if (!target.getParentFile().exists()) target.getParentFile().mkdirs();

        Map<tiled.core.Tile, SpritesheetId> localConvertions = new LinkedHashMap<tiled.core.Tile, SpritesheetId>();
        List<CompressedSpritesheet> usedSpritesheets = new LinkedList<CompressedSpritesheet>();

        List<tiled.core.TileSet> toRemove = new LinkedList<TileSet>();

        for (tiled.core.TileSet ts : tmx.getTileSets()) {
            if (!ts.getName().equalsIgnoreCase("map_dynamic_placeholders")) {
                toRemove.add(ts);
            }
        }

        for (tiled.core.TileLayer layer : tmx.getTileLayers()) {
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    tiled.core.Tile tile = layer.getTileAt(x, y);
                    if (tile != null && !tile.getTileSet().getName().equalsIgnoreCase("map_dynamic_placeholders")) {
                        SpritesheetId sid = convertMapSprite(SpritesheetId.toStringID(tile.getTileSet().getName(), tile.getId()));
                        localConvertions.put(tile, sid);
                        if (!usedSpritesheets.contains(spritesheetsBySidForMaps.get(sid))) {
                            usedSpritesheets.add(spritesheetsBySidForMaps.get(sid));
                        }
                    }
                }
            }
        }

        Map<CompressedSpritesheet, tiled.core.TileSet> csToTs = new LinkedHashMap<CompressedSpritesheet, tiled.core.TileSet>();
        for (CompressedSpritesheet cs : usedSpritesheets) {
            cs.drawFile();
            tiled.core.TileSet ts = new tiled.core.TileSet();
            csToTs.put(cs, ts);
            tiled.util.BasicTileCutter cutter = new tiled.util.BasicTileCutter(TILE_WIDTH_IN_PIXELS, TILE_HEIGHT_IN_PIXELS, 0, 0);
            try {
                ts.importTileBitmap(cs.f.getAbsolutePath(), cutter);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ts.setName(cs.prefix + Integer.toString(cs.index));
            //ts.setSource("../drawable/"+ts.getName()+TILESHEET_SUFFIX);
            tmx.addTileset(ts);
        }

        for (tiled.core.TileLayer layer : tmx.getTileLayers()) {
            for (tiled.core.Tile tile : localConvertions.keySet()) {
                SpritesheetId sid = localConvertions.get(tile);
                layer.replaceTile(tile, csToTs.get(spritesheetsBySidForMaps.get(sid)).getTile(sid.offset));
            }
        }

        for (tiled.core.TileSet ts : toRemove) {
            tmx.removeTileset(ts);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TMXMapWriter writer = new TMXMapWriter();
        writer.settings.layerCompressionMethod = TMXMapWriter.Settings.LAYER_COMPRESSION_METHOD_ZLIB;
        try {
            writer.writeMap(tmx, baos, target.getAbsolutePath());
            String xml = baos.toString();
            FileWriter w = new FileWriter(target);
            w.write(xml);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private SpritesheetId convertObjectSprite(String originalSpriteId) {
        if (spritesRelocationForObjects.containsKey(SpritesheetId.getInstance(originalSpriteId))) {
            return spritesRelocationForObjects.get(SpritesheetId.getInstance(originalSpriteId));
        } else if (currentSpritesheetForObjects == null || !currentSpritesheetForObjects.hasFreeSlot()) {
            currentSpritesheetForObjects = new CompressedSpritesheet(TILESHEET_PREFIX_FOR_OBJECTS, currentSpritesheetIndexForObjects);
            compressedSpritesheets.add(currentSpritesheetForObjects);
            currentSpritesheetIndexForObjects++;
        }
        SpritesheetId sid = currentSpritesheetForObjects.addSprite(originalSpriteId);
        spritesRelocationForObjects.put(SpritesheetId.getInstance(originalSpriteId), sid);
        return sid;
    }

    private SpritesheetId convertMapSprite(String originalSpriteId) {
        if (spritesRelocationForMaps.containsKey(SpritesheetId.getInstance(originalSpriteId))) {
            return spritesRelocationForMaps.get(SpritesheetId.getInstance(originalSpriteId));
        } else if (currentSpritesheetForMaps == null || !currentSpritesheetForMaps.hasFreeSlot()) {
            currentSpritesheetForMaps = new CompressedSpritesheet(TILESHEET_PREFIX_FOR_MAPS, currentSpritesheetIndexForMaps);
            compressedSpritesheets.add(currentSpritesheetForMaps);
            currentSpritesheetIndexForMaps++;
        }
        SpritesheetId sid = currentSpritesheetForMaps.addSprite(originalSpriteId);
        spritesRelocationForMaps.put(SpritesheetId.getInstance(originalSpriteId), sid);
        spritesheetsBySidForMaps.put(sid, currentSpritesheetForMaps);
        return sid;
    }


    private static final int TILESHEET_WIDTH_IN_SPRITES = 8;
    private static final int TILESHEET_HEIGHT_IN_SPRITES = 8;
    private static final int TILE_WIDTH_IN_PIXELS = 32;
    private static final int TILE_HEIGHT_IN_PIXELS = 32;

    private static final String TILESHEET_PREFIX_FOR_OBJECTS = "obj_";
    private static final String TILESHEET_PREFIX_FOR_MAPS = "map_";
    private static final String TILESHEET_SUFFIX = ".png";

    private static final String DEFAULT_DRAWABLE_REL_PATH = SpriteSheetSet.DEFAULT_REL_PATH_IN_SOURCE;

    private class CompressedSpritesheet {
        String prefix;
        int index;
        File f;


        boolean mustDraw = true;
        int nextFreeSlot = 0;
        String[] originalSpritesId = new String[TILESHEET_WIDTH_IN_SPRITES * TILESHEET_HEIGHT_IN_SPRITES];

        public CompressedSpritesheet(String prefix, int index) {
            this.prefix = prefix;
            this.index = index;

            File folder = new File(ResourcesCompactor.this.baseFolder.getAbsolutePath() + File.separator + DEFAULT_DRAWABLE_REL_PATH);
            if (!folder.exists()) folder.mkdirs();
            this.f = new File(folder, prefix + Integer.toString(index) + TILESHEET_SUFFIX);
        }

        public boolean hasFreeSlot() {
            return nextFreeSlot < TILESHEET_WIDTH_IN_SPRITES * TILESHEET_HEIGHT_IN_SPRITES;
        }

        public SpritesheetId addSprite(String spriteId) {
            mustDraw = true;
            originalSpritesId[nextFreeSlot] = spriteId;
            nextFreeSlot++;
            return SpritesheetId.getInstance(prefix + Integer.toString(index), nextFreeSlot - 1);
        }


        public void drawFile() {
            if (!mustDraw) return;
            BufferedImage img = new BufferedImage(TILESHEET_WIDTH_IN_SPRITES * TILE_WIDTH_IN_PIXELS, TILESHEET_HEIGHT_IN_SPRITES * TILE_HEIGHT_IN_PIXELS, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) img.getGraphics();
            Color transparent = new Color(0, 0, 0, 0);
            g.setColor(transparent);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            for (int i = 0; i < nextFreeSlot; i++) {
                g.drawImage(
                        proj.getImage(originalSpritesId[i]),
                        (i % TILESHEET_WIDTH_IN_SPRITES) * TILE_WIDTH_IN_PIXELS,
                        (i / TILESHEET_WIDTH_IN_SPRITES) * TILE_HEIGHT_IN_PIXELS,
                        TILE_WIDTH_IN_PIXELS,
                        TILE_HEIGHT_IN_PIXELS,
                        null);
            }
            try {
                ImageIO.write(img, "png", f);
                mustDraw = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

	