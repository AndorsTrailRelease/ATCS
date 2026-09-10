package com.gpl.rpg.atcontentstudio.model;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkspaceSettingsTest {

    @Test
    public void createWorldFilesOnSaveDefaultsOnAndPersists() throws Exception {
        Path tempRoot = Files.createTempDirectory("atcs-workspace-settings");
        File workspaceRoot = tempRoot.resolve("workspace").toFile();
        workspaceRoot.mkdirs();

        Workspace workspace = new Workspace(workspaceRoot);
        assertTrue(workspace.settings.createWorldFilesOnWorldmapSave.getCurrentValue());

        workspace.settings.createWorldFilesOnWorldmapSave.setCurrentValue(false);
        workspace.settings.save();

        Workspace restoredWorkspace = new Workspace(workspaceRoot);
        assertFalse(restoredWorkspace.settings.createWorldFilesOnWorldmapSave.getCurrentValue());
    }
}
