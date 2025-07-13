package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuestStage extends JSONElement {

    private static final long serialVersionUID = 8313645819951513431L;

    public Integer progress = null;
    public String log_text = null;
    public Integer exp_reward = null;
    public Integer finishes_quest = null;

    public QuestStage(Quest parent) {
        this.parent = parent;
    }

    public QuestStage clone(Quest cloneParent) {
        QuestStage clone = new QuestStage(cloneParent);
        clone.progress = progress != null ? new Integer(progress) : null;
        clone.log_text = log_text != null ? new String(log_text) : null;
        clone.exp_reward = exp_reward != null ? new Integer(exp_reward) : null;
        clone.finishes_quest = finishes_quest != null ? new Integer(finishes_quest) : null;
        clone.id = this.id;
        return clone;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map jsonObj) {
        progress = JSONElement.getInteger((Number) jsonObj.get("progress"));
        this.id = ((Quest) parent).id + ":" + progress;
        log_text = (String) jsonObj.get("logText");
        exp_reward = JSONElement.getInteger((Number) jsonObj.get("rewardExperience"));
        finishes_quest = JSONElement.getInteger((Number) jsonObj.get("finishesQuest"));
        state = State.parsed;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map stageJson = new LinkedHashMap();
        if (progress != null) stageJson.put("progress", progress);
        if (log_text != null) stageJson.put("logText", log_text);
        if (exp_reward != null) stageJson.put("rewardExperience", exp_reward);
        if (finishes_quest != null) stageJson.put("finishesQuest", finishes_quest);
        return stageJson;
    }

    @Override
    public String getDesc() {
        return progress + " - " + (exp_reward != null ? "[" + exp_reward + "XP]" : "") + ((finishes_quest != null) && (finishes_quest == 1) ? "[END]" : "") + log_text;
    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();

        //Nothing to link to :D
        this.state = State.linked;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        // Nothing to link to.
    }

    @Override
    public String getProjectFilename() {
        return ((Quest) parent).getProjectFilename();
    }

    @Override
    public GameDataElement clone() {
        return null;
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getQuestIcon();
    }

    public Image getImage() {
        return DefaultIcons.getQuestImage();
    }


}