package com.gpl.rpg.atcontentstudio.model.maps;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;

import java.awt.Point;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WorldmapWorldFileWriter {

    private static final int TILE_SIZE = 32;

    private WorldmapWorldFileWriter() {
    }

    /**
     * Writes a Tiled {@code .world} file for each segment in the given collection.
     *
     * @param segments worldmap segments to export
     */
    public static void saveWorldFiles(Iterable<WorldmapSegment> segments) {
        for (WorldmapSegment segment : segments) {
            saveWorldFile(segment);
        }
    }

    /**
     * Writes a Tiled {@code .world} file for the given worldmap segment.
     *
     * @param segment the segment to export
     */
    public static void saveWorldFile(WorldmapSegment segment) {
        if (segment == null || segment.parent == null) {
            return;
        }

        Project project = segment.getProject();
        if (project == null) {
            Notification.addError("Unable to export world file for " + segment.id + ": no project is attached.");
            return;
        }

        File worldFile = new File(project.baseFolder, segment.id + ".world");
        boolean saved = FileUtils.writeStringToFile(FileUtils.toJsonString(buildWorldFileData(segment, project)), worldFile, null, false);
        if (!saved) {
            Notification.addError("Error while saving world file " + worldFile.getAbsolutePath());
        }
    }

    /**
     * Builds the serialized data structure used for a Tiled {@code .world} file.
     *
     * @param segment the worldmap segment to export
     * @param project the owning project
     * @return a JSON-serializable map describing the world file contents
     */
    private static Map<String, Object> buildWorldFileData(WorldmapSegment segment, Project project) {
        Map<String, Object> worldData = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> maps = new ArrayList<Map<String, Object>>();
        File worldFileDir = project.baseFolder;
        for (String mapId : segment.mapLocations.keySet()) {
            TMXMap map = project.getMap(mapId);
            if (map == null || map.tmxMap == null) {
                Notification.addError("Unable to export world file for " + segment.id + ": missing TMX map " + mapId);
                continue;
            }

            Point location = segment.mapLocations.get(mapId);
            Map<String, Object> mapData = new LinkedHashMap<String, Object>();
            mapData.put("fileName", resolveMapReference(worldFileDir, project, map));
            mapData.put("height", map.tmxMap.getHeight() * TILE_SIZE);
            mapData.put("width", map.tmxMap.getWidth() * TILE_SIZE);
            mapData.put("x", (segment.segmentX + location.x) * TILE_SIZE);
            mapData.put("y", (segment.segmentY + location.y) * TILE_SIZE);
            maps.add(mapData);
        }
        worldData.put("maps", maps);
        worldData.put("onlyShowAdjacentMaps", Boolean.FALSE);
        worldData.put("type", "world");
        return worldData;
    }

    /**
     * Resolves the map file path stored in the world file relative to the world file directory.
     *
     * @param worldFileDir the directory where the world file is written
     * @param project the owning project
     * @param map the map being referenced
     * @return the relative path to the map file, or the fallback file name when the source file is missing
     */
    private static String resolveMapReference(File worldFileDir, Project project, TMXMap map) {
        File referenceFile = map.tmxFile;
        if (referenceFile == null) {
            return map.id + ".tmx";
        }
        Path worldPath = Paths.get(worldFileDir.getAbsolutePath()).toAbsolutePath().normalize();
        Path mapPath = Paths.get(referenceFile.getAbsolutePath()).toAbsolutePath().normalize();
        return worldPath.relativize(mapPath).toString().replace(File.separatorChar, '/');
    }
}
