package com.gpl.rpg.atcontentstudio.model;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceSettings {

    public static final String VERSION_KEY = "ATCS_Version";
    public static final String FILENAME = "workspace_settings.json";

    public static final int SETTINGS_VERSION = 1;

    public Workspace parent;
    public File file;

    public static Boolean DEFAULT_USE_SYS_MAP_EDITOR = true;
    public Setting<Boolean> useSystemDefaultMapEditor = new PrimitiveSetting<Boolean>("useSystemDefaultMapEditor", DEFAULT_USE_SYS_MAP_EDITOR);
    public static String DEFAULT_MAP_EDITOR_COMMAND = "tiled";
    public Setting<String> mapEditorCommand = new PrimitiveSetting<String>("mapEditorCommand", DEFAULT_MAP_EDITOR_COMMAND);

    public static Boolean DEFAULT_USE_SYS_IMG_VIEWER = true;
    public Setting<Boolean> useSystemDefaultImageViewer = new PrimitiveSetting<Boolean>("useSystemDefaultImageViewer", DEFAULT_USE_SYS_IMG_VIEWER);
    public static Boolean DEFAULT_USE_SYS_IMG_EDITOR = false;
    public Setting<Boolean> useSystemDefaultImageEditor = new PrimitiveSetting<Boolean>("useSystemDefaultImageEditor", DEFAULT_USE_SYS_IMG_EDITOR);
    public static String DEFAULT_IMG_EDITOR_COMMAND = "gimp";
    public Setting<String> imageEditorCommand = new PrimitiveSetting<String>("imageEditorCommand", DEFAULT_IMG_EDITOR_COMMAND);

    public static String[] LANGUAGE_LIST = new String[]{null, "de", "ru", "pl", "fr", "it", "es", "nl", "uk", "ca", "sv", "pt", "pt_BR", "zh_Hant", "zh_Hans", "ja", "cs", "tr", "ko", "hu", "sl", "bg", "id", "fi", "th", "gl", "ms", "pa", "az", "nb"};
    public Setting<String> translatorLanguage = new NullDefaultPrimitiveSetting<String>("translatorLanguage");
    public static Boolean DEFAULT_ALLOW_INTERNET = true;
    public Setting<Boolean> useInternet = new PrimitiveSetting<Boolean>("useInternet", DEFAULT_ALLOW_INTERNET);
    public static Boolean DEFAULT_CHECK_UPDATE = true;
    public Setting<Boolean> checkUpdates = new PrimitiveSetting<Boolean>("checkUpdates", DEFAULT_CHECK_UPDATE);


    public List<Setting<? extends Object>> settings = new ArrayList<Setting<? extends Object>>();

    public WorkspaceSettings(Workspace parent) {
        this.parent = parent;
        settings.add(useSystemDefaultMapEditor);
        settings.add(mapEditorCommand);
        settings.add(useSystemDefaultImageViewer);
        settings.add(useSystemDefaultImageEditor);
        settings.add(imageEditorCommand);
        settings.add(translatorLanguage);
        settings.add(useInternet);
        settings.add(checkUpdates);
        file = new File(parent.baseFolder, FILENAME);
        if (file.exists()) {
            load(file);
        }
    }

    public void load(File f) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(f);
            @SuppressWarnings("rawtypes")
            Map jsonSettings = (Map) parser.parse(reader);
            Integer version = ((Number) jsonSettings.get(VERSION_KEY)).intValue();
            if (version != null) {
                if (version >= 1) {
                    loadv1(jsonSettings);
                }
            }

        } catch (Exception e) {
            Notification.addError("Error while parsing workspace settings: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @SuppressWarnings("rawtypes")
    private void loadv1(Map jsonSettings) {
        for (Setting s : settings) {
            s.readFromJson(jsonSettings);
        }
    }

    @SuppressWarnings("unchecked")
    public void save() {
        @SuppressWarnings("rawtypes")
        Map json = new LinkedHashMap();
        for (Setting<? extends Object> s : settings) {
            s.saveToJson(json);
        }

        if (json.isEmpty()) {
            //Everything is default.
            file.delete();
            return;
        }

        json.put(VERSION_KEY, SETTINGS_VERSION);
        StringWriter writer = new JsonPrettyWriter();
        try {
            JSONObject.writeJSONString(json, writer);
        } catch (IOException e) {
            //Impossible with a StringWriter
        }
        String toWrite = writer.toString();
        try {
            FileWriter w = new FileWriter(file);
            w.write(toWrite);
            w.close();
            Notification.addSuccess("Workspace settings saved.");
        } catch (IOException e) {
            Notification.addError("Error while saving workspace settings : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetDefault() {
        for (Setting<? extends Object> s : settings) {
            s.resetDefault();
        }
    }

    public abstract class Setting<X extends Object> {

        public String id;
        public X value, defaultValue;

        public void setCurrentValue(X value) {
            this.value = value;
        }

        public X getCurrentValue() {
            return value;
        }

        public X getDefaultValue() {
            return defaultValue;
        }

        public void resetDefault() {
            value = defaultValue;
        }

        public abstract void readFromJson(@SuppressWarnings("rawtypes") Map json);

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void saveToJson(Map json) {
            if (!defaultValue.equals(value)) json.put(id, value);
        }
    }

    public class PrimitiveSetting<X extends Object> extends Setting<X> {


        public PrimitiveSetting(String id, X defaultValue) {
            this.id = id;
            this.value = this.defaultValue = defaultValue;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void readFromJson(Map json) {
            if (json.get(id) != null) value = (X) json.get(id);
        }


    }

    public class NullDefaultPrimitiveSetting<X extends Object> extends PrimitiveSetting<X> {

        public NullDefaultPrimitiveSetting(String id) {
            super(id, null);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public void saveToJson(Map json) {
            if (value != null) json.put(id, value);
        }
    }

    public class ListSetting<X extends Object> extends Setting<List<X>> {

        public ListSetting(String id, List<X> defaultValue) {
            this.id = id;
            this.value = this.defaultValue = defaultValue;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public void readFromJson(Map json) {
            value = new ArrayList<X>();
            if (json.get(id) != null) {
                for (Object o : ((List) json.get(id))) {
                    value.add((X) o);
                }
            }
        }

    }


}
