package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonSerializable;
import com.gpl.rpg.atcontentstudio.io.SettingsSave;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.ProjectsTree.ProjectsTreeModel;
import com.gpl.rpg.atcontentstudio.ui.WorkerDialog;
import com.gpl.rpg.atcontentstudio.utils.FileUtils;
import org.jsoup.SerializationException;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class Workspace implements ProjectTreeNode, Serializable, JsonSerializable {

    private static final long serialVersionUID = 7938633033601384956L;

    public static final String WS_SETTINGS_FILE = ".workspace";
    public static final String WS_SETTINGS_FILE_JSON = ".workspace.json";

    public static Workspace activeWorkspace;

    public Preferences preferences = new Preferences();
    public File baseFolder;
    public File settingsFile;
    public transient WorkspaceSettings settings;
    public transient List<ProjectTreeNode> projects = new ArrayList<ProjectTreeNode>();
    public Set<String> projectsName = new HashSet<String>();
    public Map<String, Boolean> projectsOpenByName = new HashMap<String, Boolean>();
    public Set<File> knownMapSourcesFolders = new HashSet<File>();

    public transient ProjectsTreeModel projectsTreeModel = null;

    public Workspace(File workspaceRoot) {
        boolean freshWorkspace = false;
        baseFolder = workspaceRoot;
        if (!workspaceRoot.exists()) {
            try {
                workspaceRoot.mkdir();
            } catch (SecurityException e) {
                Notification.addError("Error creating workspace directory: "
                                              + e.getMessage());
                e.printStackTrace();
            }
        }
        settings = new WorkspaceSettings(this);
        settingsFile = new File(workspaceRoot, WS_SETTINGS_FILE_JSON);
        if (!settingsFile.exists()) {
            try {
                settingsFile.createNewFile();
                freshWorkspace = true;
            } catch (IOException e) {
                Notification.addError("Error creating workspace datafile: "
                                              + e.getMessage());
                e.printStackTrace();
            }
            Notification.addSuccess("New workspace created: "
                                            + workspaceRoot.getAbsolutePath());
        }
        if (freshWorkspace)
            save();
    }

    @Override
    public Map toMap() {
        Map map = new HashMap();
        map.put("serialVersionUID", serialVersionUID);
        map.put("preferences", preferences.toMap());
        map.put("projectsName", (new ArrayList<String>(projectsName)));
        map.put("projectsOpenByName", projectsOpenByName);
        List<String> l = new ArrayList<>(knownMapSourcesFolders.size());
        for (File f: knownMapSourcesFolders){
            l.add(f.getPath());
        }
        map.put("knownMapSourcesFolders", l);
        return map;
    }

    @Override
    public void fromMap(Map map) {
        if(serialVersionUID != (long) map.get("serialVersionUID")){
            throw new SerializationException("wrong seriaVersionUID");
        }

        preferences.fromMap((Map) map.get("preferences"));

        projectsName = new HashSet<>((List<String>) map.getOrDefault("projectsName", new HashSet<String>()));

        projectsOpenByName = (Map<String, Boolean>) map.getOrDefault("projectsOpenByName", new HashMap<String, Boolean>() );

        List<String> knownMapSourcesFolders1 = (List<String>) map.getOrDefault("knownMapSourcesFolders", new ArrayList<String>());
        knownMapSourcesFolders = new HashSet<>();
        if (knownMapSourcesFolders1 != null){
            int size = knownMapSourcesFolders1.size();
            for (String path: knownMapSourcesFolders1) {
                //TODO: catch invalid paths...?
                knownMapSourcesFolders.add(new File(path));
            }
        }

    }

    public static void setActive(File workspaceRoot) {
        Workspace w;
        File f2 = new File(workspaceRoot, WS_SETTINGS_FILE_JSON);
        if (f2.exists()) {
            w = loadWorkspaceFromJson(workspaceRoot, f2);
            w.refreshTransients();
        } else {
            Notification.addInfo("Could not find json workspace file. Checking for binary file");
            File f = new File(workspaceRoot, WS_SETTINGS_FILE);
            if (!workspaceRoot.exists() || !f.exists()) {
                w = new Workspace(workspaceRoot);
            } else {
                w = (Workspace) SettingsSave.loadInstance(f, "Workspace");
                if (w == null) {
                    w = new Workspace(workspaceRoot);
                } else {
                    w.settingsFile = f2;
                    w.baseFolder = workspaceRoot;
                    Notification.addInfo("Switched workspace to json format.");
                    w.refreshTransients();
                }
                w.save();
            }
        }
        activeWorkspace = w;
    }

    private static Workspace loadWorkspaceFromJson(File workspaceRoot, File settingsFile) {
        Workspace w = w = new Workspace(workspaceRoot);
        Map json = FileUtils.mapFromJsonFile(settingsFile);
        if (json!= null) {
            w.fromMap(json);
        }
        return w;
    }

    public static void saveActive() {
        activeWorkspace.save();
    }

    public void save() {
        settings.save();
        FileUtils.writeStringToFile(FileUtils.toJsonString(this), settingsFile, "Workspace");
    }

    @Override
    public Enumeration<ProjectTreeNode> children() {
        return Collections.enumeration(projects);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public TreeNode getChildAt(int arg0) {
        return projects.get(arg0);
    }

    @Override
    public int getChildCount() {
        return projects.size();
    }

    @Override
    public int getIndex(TreeNode arg0) {
        return projects.indexOf(arg0);
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void childrenAdded(List<ProjectTreeNode> path) {
        path.add(0, this);
        if (projectsTreeModel != null)
            projectsTreeModel.insertNode(new TreePath(path.toArray()));
    }

    @Override
    public void childrenChanged(List<ProjectTreeNode> path) {
        path.add(0, this);
        ProjectTreeNode last = path.get(path.size() - 1);
        if (projectsTreeModel != null) {
            while (path.size() > 1) {
                projectsTreeModel.changeNode(new TreePath(path.toArray()));
                path.remove(path.size() - 1);
            }

        }
        ATContentStudio.frame.editorChanged(last);
    }

    @Override
    public void childrenRemoved(List<ProjectTreeNode> path) {
        path.add(0, this);
        if (projectsTreeModel != null)
            projectsTreeModel.removeNode(new TreePath(path.toArray()));
    }

    @Override
    public void notifyCreated() {
        childrenAdded(new ArrayList<ProjectTreeNode>());
        for (ProjectTreeNode node : projects) {
            if (node != null)
                node.notifyCreated();
        }
    }

    @Override
    public String getDesc() {
        return "Workspace: " + baseFolder.getAbsolutePath();
    }

    public static void createProject(final String projectName,
                                     final File gameSourceFolder, final Project.ResourceSet sourceSet) {
        WorkerDialog.showTaskMessage("Creating project " + projectName + "...",
                                     ATContentStudio.frame, new Runnable() {
                    @Override
                    public void run() {
                        if (activeWorkspace.projectsName.contains(projectName)) {
                            Notification.addError("A project named "
                                                          + projectName
                                                          + " already exists in this workspace.");
                            return;
                        }
                        Project p = new Project(activeWorkspace, projectName,
                                                gameSourceFolder, sourceSet);
                        activeWorkspace.projects.add(p);
                        activeWorkspace.projectsName.add(projectName);
                        activeWorkspace.projectsOpenByName.put(projectName,
                                                               p.open);
                        activeWorkspace.knownMapSourcesFolders
                                .add(gameSourceFolder);
                        p.notifyCreated();
                        Notification.addSuccess("Project " + projectName
                                                        + " successfully created");
                        saveActive();
                    }
                });
    }

    public static void closeProject(Project p) {
        int index = activeWorkspace.projects.indexOf(p);
        if (index < 0) {
            Notification.addError("Cannot close unknown project " + p.name);
            return;
        }
        p.close();
        ClosedProject cp = new ClosedProject(activeWorkspace, p.name);
        activeWorkspace.projects.set(index, cp);
        activeWorkspace.projectsOpenByName.put(p.name, false);
        cp.notifyCreated();
        saveActive();
    }

    public static void openProject(final ClosedProject cp) {
        WorkerDialog.showTaskMessage("Opening project " + cp.name + "...",
                                     ATContentStudio.frame, new Runnable() {
                    @Override
                    public void run() {
                        int index = activeWorkspace.projects.indexOf(cp);
                        if (index < 0) {
                            Notification
                                    .addError("Cannot open unknown project "
                                                      + cp.name);
                            return;
                        }
                        cp.childrenRemoved(new ArrayList<ProjectTreeNode>());
                        Project p = Project.fromFolder(activeWorkspace,
                                                       new File(activeWorkspace.baseFolder, cp.name));
                        p.open();
                        activeWorkspace.projects.set(index, p);
                        activeWorkspace.projectsOpenByName.put(p.name, true);
                        p.notifyCreated();
                        saveActive();
                    }
                });
    }

    public void refreshTransients() {
        this.settings = new WorkspaceSettings(this);
        this.projects = new ArrayList<ProjectTreeNode>();
        Set<String> projectsFailed = new HashSet<String>();
        for (String projectName : projectsName) {
            if (projectsOpenByName.get(projectName)) {
                File projRoot = new File(this.baseFolder, projectName);
                if (projRoot.exists()) {
                    Project p = Project.fromFolder(this, projRoot);
                    if (p != null) {
                        projects.add(p);
                    } else {
                        Notification
                                .addError("Failed to open project "
                                                  + projectName
                                                  + ". Removing it from workspace (not from filesystem though).");
                        projectsFailed.add(projectName);
                    }
                } else {
                    Notification.addError("Unable to find project "
                                                  + projectName
                                                  + "'s root folder. Removing it from workspace");
                    projectsFailed.add(projectName);
                }
            } else {
                projects.add(new ClosedProject(this, projectName));
            }
        }
        for (String projectName : projectsFailed) {
            projectsName.remove(projectName);
            projectsOpenByName.remove(projectName);
        }
        notifyCreated();
    }

    @Override
    public Project getProject() {
        return null;
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public Image getClosedIcon() {
        return null;
    }

    @Override
    public Image getLeafIcon() {
        return null;
    }

    @Override
    public Image getOpenIcon() {
        return null;
    }

    public static void deleteProject(ClosedProject cp) {
        cp.childrenRemoved(new ArrayList<ProjectTreeNode>());
        activeWorkspace.projects.remove(cp);
        activeWorkspace.projectsOpenByName.remove(cp.name);
        activeWorkspace.projectsName.remove(cp.name);
        if (delete(new File(activeWorkspace.baseFolder, cp.name))) {
            Notification.addSuccess("Closed project " + cp.name
                                            + " successfully deleted.");
        } else {
            Notification.addError("Error while deleting closed project "
                                          + cp.name + ". Files may remain in the workspace.");
        }
        saveActive();
    }

    public static void deleteProject(Project p) {
        p.childrenRemoved(new ArrayList<ProjectTreeNode>());
        activeWorkspace.projects.remove(p);
        activeWorkspace.projectsOpenByName.remove(p.name);
        activeWorkspace.projectsName.remove(p.name);
        if (delete(p.baseFolder)) {
            Notification.addSuccess("Project " + p.name
                                            + " successfully deleted.");
        } else {
            Notification.addError("Error while deleting project " + p.name
                                          + ". Files may remain in the workspace.");
        }
        saveActive();
    }

    private static boolean delete(File f) {
        boolean b = true;
        if (Files.isSymbolicLink(f.toPath())) {
            b &= f.delete();
        } else if (f.isDirectory()) {
            for (File c : f.listFiles())
                b &= delete(c);
        }
        return b & f.delete();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public Type getDataType() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return projects.isEmpty();
    }


    @Override
    public boolean needsSaving() {
        for (ProjectTreeNode node : projects) {
            if (node.needsSaving()) return true;
        }
        return false;
    }
}
