package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Quest extends JSONElement {

    private static final long serialVersionUID = 2004839647483250099L;

    //Available from init state
    //public String id = null; inherited.
    public String name = null;

    //Available in parsed state
    public Integer visible_in_log = null;
    public List<QuestStage> stages = null;

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + name + " (" + id + ")";
    }

    public static String getStaticDesc() {
        return "Quests";
    }


    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<Quest> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List quests = (List) parser.parse(reader);
            for (Object obj : quests) {
                Map questJson = (Map) obj;
                Quest quest = fromJson(questJson);
                quest.jsonFile = jsonFile;
                quest.parent = category;
                if (quest.getDataType() == GameSource.Type.created || quest.getDataType() == GameSource.Type.altered) {
                    quest.writable = true;
                }
                category.add(quest);
            }
        } catch (FileNotFoundException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
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
    public static Quest fromJson(String jsonString) throws ParseException {
        Map questJson = (Map) new JSONParser().parse(jsonString);
        Quest quest = fromJson(questJson);
        quest.parse(questJson);
        return quest;
    }

    @SuppressWarnings("rawtypes")
    public static Quest fromJson(Map questJson) {
        Quest quest = new Quest();
        quest.id = (String) questJson.get("id");
        quest.name = (String) questJson.get("name");
        //Quests have to be parsed to have their stages initialized.
        quest.parse(questJson);
        return quest;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map questJson) {
        this.visible_in_log = JSONElement.getInteger((Number) questJson.get("showInLog"));
        List questStagesJson = (List) questJson.get("stages");
        this.stages = new ArrayList<QuestStage>();
        if (questStagesJson != null && !questStagesJson.isEmpty()) {
            for (Object questStageJsonObj : questStagesJson) {
                Map questStageJson = (Map) questStageJsonObj;
                QuestStage questStage = new QuestStage(this);
                questStage.parse(questStageJson);
                this.stages.add(questStage);
            }
        }
        this.state = State.parsed;
    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();

        for (QuestStage stage : stages) {
            stage.link();
        }
        //Nothing to link to :D
        this.state = State.linked;
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getQuestIcon();
    }

    public Image getImage() {
        return DefaultIcons.getQuestImage();
    }

    @Override
    public GameDataElement clone() {
        Quest clone = new Quest();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.name = this.name;
        clone.visible_in_log = this.visible_in_log;
        if (this.stages != null) {
            clone.stages = new ArrayList<QuestStage>();
            for (QuestStage stage : this.stages) {
                clone.stages.add((QuestStage) stage.clone(clone));
            }
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        //Nothing to link to.
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map questJson = new LinkedHashMap();
        questJson.put("id", this.id);
        if (this.name != null) questJson.put("name", this.name);
        if (this.visible_in_log != null) questJson.put("showInLog", this.visible_in_log);
        if (this.stages != null) {
            List stagesJson = new ArrayList();
            questJson.put("stages", stagesJson);
            for (QuestStage stage : this.stages) {
                stagesJson.add(stage.toJson());
            }
        }
        return questJson;
    }


    @Override
    public String getProjectFilename() {
        return "questlist_" + getProject().name + ".json";
    }

    public QuestStage getStage(Integer stageId) {
        for (QuestStage stage : stages) {
            if (stage.progress.equals(stageId)) {
                return stage;
            }
        }
        return null;
    }

}
