package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.gamedata.Requirement.RequirementType;
import com.gpl.rpg.atcontentstudio.model.maps.TMXMap;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.*;


public class Dialogue extends JSONElement {

    private static final long serialVersionUID = -6872164604703134683L;


    //Available from init state
    //public String id = null; inherited.
    public String message = null;

    //Available from parsed state;
    public List<Reward> rewards = null;
    public List<Reply> replies = null;
    public String switch_to_npc_id = null;

    //Available from linked state;
    public NPC switch_to_npc = null;

    public static class Reward {

        //Available from parsed state
        public RewardType type = null;
        public String reward_obj_id = null;
        public Integer reward_value = null;
        public String map_name = null;

        //Available from linked state
        public GameDataElement reward_obj = null;
        public TMXMap map = null;

        public enum RewardType {
            questProgress,
            removeQuestProgress,
            dropList,
            skillIncrease,
            actorCondition,
            actorConditionImmunity,
            alignmentChange,
            alignmentSet,
            giveItem,
            createTimer,
            spawnAll,
            removeSpawnArea,
            deactivateSpawnArea,
            activateMapObjectGroup,
            deactivateMapObjectGroup,
            changeMapFilter,
            mapchange
        }
    }

    public static class Reply {

        public static final String GO_NEXT_TEXT = "N";
        public static final String SHOP_PHRASE_ID = "S";
        public static final String FIGHT_PHRASE_ID = "F";
        public static final String EXIT_PHRASE_ID = "X";
        public static final String REMOVE_PHRASE_ID = "R";

        public static final List<String> KEY_PHRASE_ID = Arrays.asList(new String[]{SHOP_PHRASE_ID, FIGHT_PHRASE_ID, EXIT_PHRASE_ID, REMOVE_PHRASE_ID});

        //Available from parsed state
        public String text = null;
        public String next_phrase_id = null;
        public List<Requirement> requirements = null;

        //Available from linked state
        public Dialogue next_phrase = null;

    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + id;
    }

    public static String getStaticDesc() {
        return "Dialogues";
    }

    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<Dialogue> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List dialogues = (List) parser.parse(reader);
            for (Object obj : dialogues) {
                Map dialogueJson = (Map) obj;
                Dialogue dialogue = fromJson(dialogueJson);
                dialogue.jsonFile = jsonFile;
                dialogue.parent = category;
                if (dialogue.getDataType() == GameSource.Type.created || dialogue.getDataType() == GameSource.Type.altered) {
                    dialogue.writable = true;
                }
                category.add(dialogue);
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
    public static Dialogue fromJson(String jsonString) throws ParseException {
        Map dialogueJson = (Map) new JSONParser().parse(jsonString);
        Dialogue dialogue = fromJson(dialogueJson);
        dialogue.parse(dialogueJson);
        return dialogue;
    }

    @SuppressWarnings("rawtypes")
    public static Dialogue fromJson(Map dialogueJson) {
        Dialogue dialogue = new Dialogue();
        dialogue.id = (String) dialogueJson.get("id");
        dialogue.message = (String) dialogueJson.get("message");
        return dialogue;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map dialogueJson) {
        this.switch_to_npc_id = (String) dialogueJson.get("switchToNPC");
        List repliesJson = (List) dialogueJson.get("replies");
        if (repliesJson != null && !repliesJson.isEmpty()) {
            this.replies = new ArrayList<Dialogue.Reply>();
            for (Object replyJsonObj : repliesJson) {
                Map replyJson = (Map) replyJsonObj;
                Reply reply = new Reply();
                reply.text = (String) replyJson.get("text");
                reply.next_phrase_id = (String) replyJson.get("nextPhraseID");
                List requirementsJson = (List) replyJson.get("requires");
                if (requirementsJson != null && !requirementsJson.isEmpty()) {
                    reply.requirements = new ArrayList<Requirement>();
                    for (Object requirementJsonObj : requirementsJson) {
                        Map requirementJson = (Map) requirementJsonObj;
                        Requirement requirement = new Requirement();
                        requirement.jsonFile = this.jsonFile;
                        requirement.parent = this;
                        if (requirementJson.get("requireType") != null)
                            requirement.type = RequirementType.valueOf((String) requirementJson.get("requireType"));
                        requirement.required_obj_id = (String) requirementJson.get("requireID");
                        if (requirementJson.get("value") != null)
                            requirement.required_value = JSONElement.getInteger(Integer.parseInt(requirementJson.get("value").toString()));
                        if (requirementJson.get("negate") != null)
                            requirement.negated = (Boolean) requirementJson.get("negate");
                        requirement.state = State.parsed;
                        reply.requirements.add(requirement);
                    }
                }
                this.replies.add(reply);
            }
        }
        List rewardsJson = (List) dialogueJson.get("rewards");
        if (rewardsJson != null && !rewardsJson.isEmpty()) {
            this.rewards = new ArrayList<Dialogue.Reward>();
            for (Object rewardJsonObj : rewardsJson) {
                Map rewardJson = (Map) rewardJsonObj;
                Reward reward = new Reward();
                if (rewardJson.get("rewardType") != null)
                    reward.type = Reward.RewardType.valueOf((String) rewardJson.get("rewardType"));
                if (rewardJson.get("rewardID") != null) reward.reward_obj_id = (String) rewardJson.get("rewardID");
                if (rewardJson.get("value") != null)
                    reward.reward_value = JSONElement.getInteger((Number) rewardJson.get("value"));
                if (rewardJson.get("mapName") != null) reward.map_name = (String) rewardJson.get("mapName");
                this.rewards.add(reward);
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
        Project proj = getProject();
        if (proj == null) {
            Notification.addError("Error linking dialogue " + id + ". No parent project found.");
            return;
        }
        if (this.switch_to_npc_id != null) this.switch_to_npc = proj.getNPC(this.switch_to_npc_id);
        if (this.switch_to_npc != null) this.switch_to_npc.addBacklink(this);

        if (replies != null) {
            for (Reply reply : replies) {
                if (reply.next_phrase_id != null) {
                    if (!Reply.KEY_PHRASE_ID.contains(reply.next_phrase_id)) {
                        reply.next_phrase = proj.getDialogue(reply.next_phrase_id);
                    }
                }
                if (reply.next_phrase != null) reply.next_phrase.addBacklink(this);
                if (reply.requirements != null) {
                    for (Requirement requirement : reply.requirements) {
                        requirement.link();
                    }
                }
            }
        }
        if (rewards != null) {
            for (Reward reward : rewards) {
                if (reward.reward_obj_id != null) {
                    switch (reward.type) {
                        case activateMapObjectGroup:
                        case deactivateMapObjectGroup:
                        case spawnAll:
                        case removeSpawnArea:
                        case deactivateSpawnArea:
                        case changeMapFilter:
                        case mapchange:
                            reward.map = reward.map_name != null ? proj.getMap(reward.map_name) : null;
                            break;
                        case actorCondition:
                        case actorConditionImmunity:
                            reward.reward_obj = proj.getActorCondition(reward.reward_obj_id);
                            break;
                        case alignmentChange:
                        case alignmentSet:
                            //Nothing to do (yet ?).
                            break;
                        case createTimer:
                            //Nothing to do.
                            break;
                        case dropList:
                            reward.reward_obj = proj.getDroplist(reward.reward_obj_id);
                            break;
                        case giveItem:
                            reward.reward_obj = proj.getItem(reward.reward_obj_id);
                            break;
                        case questProgress:
                        case removeQuestProgress:
                            reward.reward_obj = proj.getQuest(reward.reward_obj_id);
                            if (reward.reward_obj != null && reward.reward_value != null) {
                                QuestStage stage = ((Quest) reward.reward_obj).getStage(reward.reward_value);
                                if (stage != null) {
                                    stage.addBacklink(this);
                                }
                            }
                            break;
                        case skillIncrease:
                            //Nothing to do (yet ?).
                            break;
                    }
                    if (reward.reward_obj != null) reward.reward_obj.addBacklink(this);
                    if (reward.map != null) reward.map.addBacklink(this);
                }
            }
        }

        this.state = State.linked;
    }


    @Override
    public Image getIcon() {
        return DefaultIcons.getDialogueIcon();
    }


    public Image getImage() {
        return DefaultIcons.getDialogueImage();
    }

    @Override
    public GameDataElement clone() {
        Dialogue clone = new Dialogue();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.message = this.message;
        clone.switch_to_npc_id = this.switch_to_npc_id;
        clone.switch_to_npc = this.switch_to_npc;
        if (clone.switch_to_npc != null) {
            clone.switch_to_npc.addBacklink(clone);
        }
        if (this.rewards != null) {
            clone.rewards = new ArrayList<Dialogue.Reward>();
            for (Reward r : this.rewards) {
                Reward rclone = new Reward();
                rclone.type = r.type;
                rclone.reward_obj_id = r.reward_obj_id;
                rclone.reward_value = r.reward_value;
                rclone.reward_obj = r.reward_obj;
                if (rclone.reward_obj != null) {
                    rclone.reward_obj.addBacklink(clone);
                }
                rclone.map = r.map;
                rclone.map_name = r.map_name;
                if (rclone.map != null) {
                    rclone.map.addBacklink(clone);
                }
                clone.rewards.add(rclone);
            }
        }
        if (this.replies != null) {
            clone.replies = new ArrayList<Dialogue.Reply>();
            for (Reply r : this.replies) {
                Reply rclone = new Reply();
                rclone.text = r.text;
                rclone.next_phrase_id = r.next_phrase_id;
                rclone.next_phrase = r.next_phrase;
                if (rclone.next_phrase != null) {
                    rclone.next_phrase.addBacklink(clone);
                }
                if (r.requirements != null) {
                    rclone.requirements = new ArrayList<Requirement>();
                    for (Requirement req : r.requirements) {
                        //Special clone method, as Requirement is a special GDE, hidden from the project tree.
                        rclone.requirements.add((Requirement) req.clone(clone));
                    }
                }
                clone.replies.add(rclone);
            }
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (switch_to_npc == oldOne) {
            oldOne.removeBacklink(this);
            switch_to_npc = (NPC) newOne;
            if (newOne != null) newOne.addBacklink(this);
        } else {
            if (replies != null) {
                for (Reply r : replies) {
                    if (r.next_phrase == oldOne) {
                        oldOne.removeBacklink(this);
                        r.next_phrase = (Dialogue) newOne;
                        if (newOne != null) newOne.addBacklink(this);
                    }
                    if (r.requirements != null) {
                        for (Requirement req : r.requirements) {
                            req.elementChanged(oldOne, newOne);
                        }
                    }
                }
            }
            if (rewards != null) {
                for (Reward r : rewards) {
                    if (r.reward_obj == oldOne) {
                        oldOne.removeBacklink(this);
                        r.reward_obj = newOne;
                        if (newOne != null) newOne.addBacklink(this);
                    }
                    if (oldOne instanceof QuestStage) {
                        if (r.reward_obj != null && r.reward_obj.equals(oldOne.parent) && r.reward_value != null && r.reward_value.equals(((QuestStage) oldOne).progress)) {
                            oldOne.removeBacklink((GameDataElement) this);
                            if (newOne != null) newOne.addBacklink((GameDataElement) this);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map dialogueJson = new LinkedHashMap();
        dialogueJson.put("id", this.id);
        if (this.message != null) dialogueJson.put("message", this.message);
        if (this.switch_to_npc != null) {
            dialogueJson.put("switchToNPC", this.switch_to_npc.id);
        } else if (this.switch_to_npc_id != null) {
            dialogueJson.put("switchToNPC", this.switch_to_npc_id);
        }
        if (this.replies != null) {
            List repliesJson = new ArrayList();
            dialogueJson.put("replies", repliesJson);
            for (Reply reply : this.replies) {
                Map replyJson = new LinkedHashMap();
                repliesJson.add(replyJson);
                if (reply.text != null) replyJson.put("text", reply.text);
                if (reply.next_phrase != null) {
                    replyJson.put("nextPhraseID", reply.next_phrase.id);
                } else if (reply.next_phrase_id != null) {
                    replyJson.put("nextPhraseID", reply.next_phrase_id);
                }
                if (reply.requirements != null) {
                    List requirementsJson = new ArrayList();
                    replyJson.put("requires", requirementsJson);
                    for (Requirement requirement : reply.requirements) {
                        Map requirementJson = new LinkedHashMap();
                        requirementsJson.add(requirementJson);
                        if (requirement.type != null) requirementJson.put("requireType", requirement.type.toString());
                        if (requirement.required_obj != null) {
                            requirementJson.put("requireID", requirement.required_obj.id);
                        } else if (requirement.required_obj_id != null) {
                            requirementJson.put("requireID", requirement.required_obj_id);
                        }
                        if (requirement.required_value != null) {
                            requirementJson.put("value", requirement.required_value);
                        }
                        if (requirement.negated != null) requirementJson.put("negate", requirement.negated);
                    }
                }
            }
        }
        if (this.rewards != null) {
            List rewardsJson = new ArrayList();
            dialogueJson.put("rewards", rewardsJson);
            for (Reward reward : this.rewards) {
                Map rewardJson = new LinkedHashMap();
                rewardsJson.add(rewardJson);
                if (reward.type != null) rewardJson.put("rewardType", reward.type.toString());
                if (reward.reward_obj != null) {
                    rewardJson.put("rewardID", reward.reward_obj.id);
                } else if (reward.reward_obj_id != null) {
                    rewardJson.put("rewardID", reward.reward_obj_id);
                }
                if (reward.reward_value != null) rewardJson.put("value", reward.reward_value);
                if (reward.map != null) {
                    rewardJson.put("mapName", reward.map.id);
                } else if (reward.map_name != null) rewardJson.put("mapName", reward.map_name);
            }
        }
        return dialogueJson;
    }

    @Override
    public String getProjectFilename() {
        return "conversationlist_" + getProject().name + ".json";
    }

}
