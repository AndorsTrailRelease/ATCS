package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.io.SettingsSave;
import com.gpl.rpg.atcontentstudio.model.maps.Worldmap;
import com.gpl.rpg.atcontentstudio.model.maps.WorldmapSegment;
import org.junit.Test;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectTest {

    private static void createSourceLayout(File sourceRoot) {
        new File(sourceRoot, "res/raw").mkdirs();
        new File(sourceRoot, "res/xml").mkdirs();
        new File(sourceRoot, "res/drawable").mkdirs();
    }

    private static WorldmapSegment addSegment(Worldmap worldmap, String id, int x, int y) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element segmentNode = doc.createElement("segment");
        segmentNode.setAttribute("id", id);
        segmentNode.setAttribute("x", Integer.toString(x));
        segmentNode.setAttribute("y", Integer.toString(y));
        WorldmapSegment segment = new WorldmapSegment(worldmap, id, segmentNode);
        segment.parse();
        worldmap.addSegment(segment);
        return segment;
    }

    @Test
    public void jsonRoundTripPopulatesAllJsonBackedProjectFields() throws Exception {
        Path tempRoot = Files.createTempDirectory("atcs-project-json-roundtrip");
        File workspaceRoot = tempRoot.resolve("workspace").toFile();
        File sourceRoot = tempRoot.resolve("source").toFile();
        workspaceRoot.mkdirs();
        sourceRoot.mkdirs();
        createSourceLayout(sourceRoot);

        Workspace workspace = new Workspace(workspaceRoot);
        Project original = new Project(workspace, "demo-project", sourceRoot, Project.ResourceSet.allFiles);
        original.open = false;
        original.save();

        Project restored = new Project(workspace, new File(original.baseFolder, Project.SETTINGS_FILE_JSON));

        assertEquals("demo-project", restored.name);
        assertEquals(original.baseFolder.getAbsoluteFile(), restored.baseFolder.getAbsoluteFile());
        assertFalse(restored.open);
        assertEquals(Project.ResourceSet.allFiles, restored.sourceSetToUse);

        assertNotNull(restored.baseContent);
        assertEquals(GameSource.Type.source, restored.baseContent.type);
        assertEquals(sourceRoot.getAbsoluteFile(), restored.baseContent.baseFolder.getAbsoluteFile());
    }

    @Test
    public void legacyProjectFileIsMigratedAndLoadedThroughJsonPath() throws Exception {
        Path tempRoot = Files.createTempDirectory("atcs-project-legacy-roundtrip");
        File workspaceRoot = tempRoot.resolve("workspace").toFile();
        File sourceRoot = tempRoot.resolve("source").toFile();
        workspaceRoot.mkdirs();
        sourceRoot.mkdirs();
        createSourceLayout(sourceRoot);

        Workspace workspace = new Workspace(workspaceRoot);
        Project original = new Project(workspace, "legacy-project", sourceRoot, Project.ResourceSet.debugData);
        original.open = false;
        original.save();

        File projectRoot = original.baseFolder;
        File jsonFile = new File(projectRoot, Project.SETTINGS_FILE_JSON);
        File legacyFile = new File(projectRoot, Project.SETTINGS_FILE);
        SettingsSave.saveInstance(original, legacyFile, "Project");
        assertTrue(legacyFile.isFile());
        assertTrue(jsonFile.delete());

        Project restored = Project.fromFolder(workspace, projectRoot);

        assertNotNull(restored);
        assertTrue(jsonFile.isFile());
        assertEquals("legacy-project", restored.name);
        assertEquals(projectRoot.getAbsoluteFile(), restored.baseFolder.getAbsoluteFile());
        assertFalse(restored.open);
        assertEquals(Project.ResourceSet.debugData, restored.sourceSetToUse);
        assertNotNull(restored.baseContent);
        assertEquals(GameSource.Type.source, restored.baseContent.type);
        assertEquals(sourceRoot.getAbsoluteFile(), restored.baseContent.baseFolder.getAbsoluteFile());
    }

    @Test
    public void regenerateWorldFilesWritesCreatedAndAlteredWorldMaps() throws Exception {
        Path tempRoot = Files.createTempDirectory("atcs-project-worldfiles");
        File workspaceRoot = tempRoot.resolve("workspace").toFile();
        File sourceRoot = tempRoot.resolve("source").toFile();
        workspaceRoot.mkdirs();
        sourceRoot.mkdirs();
        createSourceLayout(sourceRoot);

        Workspace workspace = new Workspace(workspaceRoot);
        Project project = new Project(workspace, "world-project", sourceRoot, Project.ResourceSet.allFiles);
        new File(project.baseFolder, "created/maps").mkdirs();
        new File(project.baseFolder, "altered/maps").mkdirs();

        addSegment(project.createdContent.worldmap, "created_segment", 1, 2);
        addSegment(project.alteredContent.worldmap, "altered_segment", 3, 4);

        project.regenerateWorldFiles();

        assertTrue(new File(project.createdContent.worldmap.worldmapFile.getParentFile(), "created_segment.world").isFile());
        assertTrue(new File(project.alteredContent.worldmap.worldmapFile.getParentFile(), "altered_segment.world").isFile());
    }

    @Test
    public void savingOneWorldmapSegmentOnlyCreatesItsOwnWorldFile() throws Exception {
        Path tempRoot = Files.createTempDirectory("atcs-project-worldmap-segment-save");
        File workspaceRoot = tempRoot.resolve("workspace").toFile();
        File sourceRoot = tempRoot.resolve("source").toFile();
        workspaceRoot.mkdirs();
        sourceRoot.mkdirs();
        createSourceLayout(sourceRoot);

        Workspace workspace = new Workspace(workspaceRoot);
        Project project = new Project(workspace, "worldmap-segment-project", sourceRoot, Project.ResourceSet.allFiles);
        new File(project.baseFolder, "created/maps").mkdirs();

        WorldmapSegment first = addSegment(project.createdContent.worldmap, "first_segment", 0, 0);
        WorldmapSegment second = addSegment(project.createdContent.worldmap, "second_segment", 0, 0);
        File firstWorld = new File(project.createdContent.worldmap.worldmapFile.getParentFile(), first.id + ".world");
        File secondWorld = new File(project.createdContent.worldmap.worldmapFile.getParentFile(), second.id + ".world");

        workspace.settings.createWorldFilesOnWorldmapSave.setCurrentValue(true);
        first.save();

        assertTrue(firstWorld.isFile());
        assertFalse(secondWorld.isFile());
    }
}
