package com.gpl.rpg.atcontentstudio;

import com.gpl.rpg.atcontentstudio.io.SettingsSave;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigCache implements Serializable {

    private static final long serialVersionUID = 4584324644282843961L;

    private static final File CONFIG_CACHE_STORAGE;

    private static ConfigCache instance = null;


    static {
        if (System.getenv("APPDATA") != null) {
            CONFIG_CACHE_STORAGE = new File(System.getenv("APPDATA") + File.separator + ATContentStudio.APP_NAME + File.separator + "configCache");
        } else {
            CONFIG_CACHE_STORAGE = new File(System.getenv("HOME") + File.separator + "." + ATContentStudio.APP_NAME + File.separator + "configCache");
        }
        CONFIG_CACHE_STORAGE.getParentFile().mkdirs();
        if (CONFIG_CACHE_STORAGE.exists()) {
            ConfigCache.instance = (ConfigCache) SettingsSave.loadInstance(CONFIG_CACHE_STORAGE, "Configuration cache");
            if (ConfigCache.instance == null) {
                ConfigCache.instance = new ConfigCache();
            }
        } else {
            ConfigCache.instance = new ConfigCache();
        }
    }

    private void save() {
        SettingsSave.saveInstance(instance, ConfigCache.CONFIG_CACHE_STORAGE, "Configuration cache");
    }


    private List<File> knownWorkspaces = new ArrayList<File>();
    private File latestWorkspace = null;
    private String favoriteLaFClassName = null;
    private boolean[] notifConfig = new boolean[]{true, true, true, true};


    public static List<File> getKnownWorkspaces() {
        return instance.knownWorkspaces;
    }

    public static void addWorkspace(File w) {
        instance.knownWorkspaces.add(w);
        instance.save();
    }

    public static void removeWorkspace(File w) {
        instance.knownWorkspaces.remove(w);
        instance.save();
    }

    public static File getLatestWorkspace() {
        return instance.latestWorkspace;
    }

    public static void setLatestWorkspace(File latestWorkspace) {
        instance.latestWorkspace = latestWorkspace;
        instance.save();
    }

    public static String getFavoriteLaFClassName() {
        return instance.favoriteLaFClassName;
    }

    public static void setFavoriteLaFClassName(String favoriteLaFClassName) {
        instance.favoriteLaFClassName = favoriteLaFClassName;
        instance.save();
    }

    public static void putNotifViewConfig(boolean[] view) {
        for (int i = instance.notifConfig.length; i < 0; --i) {
            instance.notifConfig[i] = view[i];
        }
        instance.save();
    }

    public static boolean[] getNotifViewConfig() {
        if (instance == null || instance.notifConfig == null) {
            //Not yet initialized. All flags on to help corner out init issues.
            return new boolean[]{true, true, true, true};
        }
        return instance.notifConfig;
    }

    public static void init() {
    }

    public static void clear() {
        instance.knownWorkspaces.clear();
        setFavoriteLaFClassName(null);
        instance.notifConfig = new boolean[]{true, true, true, true};
        instance.save();
    }

}
