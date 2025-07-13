package com.gpl.rpg.atcontentstudio.model.tools.writermode;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.Dialogue;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriterModeData extends GameDataElement {
    private static final long serialVersionUID = -7062544089063979696L;

    public File jsonFile;

    public Image npcIcon;
//	public String sketchName;


    public List<String> rootsId = new ArrayList<String>();
    public List<WriterDialogue> roots = new ArrayList<WriterDialogue>();
    public WriterDialogue begin;
    public Map<String, WriterDialogue> nodesById = new LinkedHashMap<String, WriterDialogue>();

    //public Map<String, WriterDialogue> dialogueThreads = new LinkedHashMap<String, WriterDialogue>();
    public Map<String, Integer> threadsNextIndex = new LinkedHashMap<String, Integer>();


    public WriterModeData(String id_prefix) {
        this.id = id_prefix;
    }

    @SuppressWarnings("rawtypes")
    public WriterModeData(WriterModeDataSet parent, Map jsonObj) {
        this.parent = parent;
        this.jsonFile = parent.writerFile;
        this.parse(jsonObj);
        this.state = State.parsed;
    }

    public WriterModeData(String id_prefix, Dialogue imported) {
        this.id = id_prefix;
        this.begin = new WriterDialogue(imported);
        this.state = State.linked;
    }

    public int getNextIndex(String id_prefix) {
        Integer index = threadsNextIndex.get(id_prefix);
        if (index == null) index = 0;
        while (getProject().getDialogue(id_prefix + index) != null) {
            index++;
        }
        threadsNextIndex.put(id_prefix, index + 1);
        return index;
    }


    public abstract class WriterNode {
        public String text;

        public abstract String getTitle();

    }

    public WriterDialogue createDialogue(Dialogue dialogue) {
        if (dialogue.message == null) {
            return new SelectorDialogue(dialogue);
        } else {
            return new WriterDialogue(dialogue);
        }
    }

    public class WriterDialogue extends WriterNode {
        public String id;
        public String id_prefix;
        public int index;
        public List<WriterReply> replies = new ArrayList<WriterReply>();
        public List<WriterReply> parents = new ArrayList<WriterReply>();
        public String dialogue_id;
        public Dialogue dialogue;

        public WriterDialogue() {
        }

        public WriterDialogue(Dialogue dialogue) {
            this.dialogue = dialogue;
            this.text = dialogue.message;
            this.id = this.dialogue_id = dialogue.id;
            Pattern p = Pattern.compile("(.*)([0-9]+)");
            Matcher m = p.matcher(dialogue.id);
            if (m.matches()) {
                this.id_prefix = m.group(1);
                this.index = Integer.parseInt(m.group(2));
            } else {
                this.id_prefix = this.id + "_";
            }
            nodesById.put(this.id, this);
            if (dialogue.replies != null) {
                for (Dialogue.Reply reply : dialogue.replies) {
                    if (Dialogue.Reply.GO_NEXT_TEXT.equals(reply.text) || reply.text == null) {
                        replies.add(new EmptyReply(this, reply));
                    } else {
                        replies.add(new WriterReply(this, reply));
                    }
                }
            }
        }

        public WriterDialogue(String id_prefix) {
            text = "";
            this.id_prefix = id_prefix;
            index = getNextIndex(id_prefix);
        }

        @Override
        public String getTitle() {
            return "Dialogue " + getID();
        }

        public String getID() {
            return this.id != null ? this.id : this.id_prefix + this.index;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public void toJson(List<WriterDialogue> visited, List<Map> jsonData) {
            if (visited.contains(this)) return;
            visited.add(this);
            Map dialogueJson = new LinkedHashMap();
            jsonData.add(dialogueJson);
            dialogueJson.put("id", id);
            dialogueJson.put("id_prefix", id_prefix);
            dialogueJson.put("index", index);
            dialogueJson.put("text", text);
            if (dialogue != null) {
                dialogueJson.put("dialogue", dialogue.id);
            } else if (dialogue_id != null) {
                dialogueJson.put("dialogue", dialogue_id);
            }
            dialogueJson.put("special", isSpecial());
            dialogueJson.put("begin", begin == this);
            if (!replies.isEmpty()) {
                List repliesJson = new ArrayList();
                for (WriterReply reply : replies) {
                    repliesJson.add(reply.toJson(visited, jsonData));
                }
                dialogueJson.put("replies", repliesJson);
            }
        }

        @SuppressWarnings("rawtypes")
        public WriterDialogue(Map json) {
            this.id = (String) json.get("id");
            this.index = ((Number) json.get("index")).intValue();
            this.id_prefix = (String) json.get("id_prefix");
            if (threadsNextIndex.get(id_prefix) == null || threadsNextIndex.get(id_prefix) <= index) {
                threadsNextIndex.put(id_prefix, index + 1);
            }
            this.text = (String) json.get("text");
            this.dialogue_id = (String) json.get("dialogue");
            if (json.get("begin") != null && ((Boolean) json.get("begin"))) begin = this;
            if (json.get("replies") != null) {
                List repliesJson = (List) json.get("replies");
                for (Object rJson : repliesJson) {
                    if (((Map) rJson).get("special") != null && (Boolean) ((Map) rJson).get("special")) {
                        //TODO Check different cases. But there are none currently.
                        this.replies.add(new EmptyReply(this, ((Map) rJson)));
                    } else {
                        this.replies.add(new WriterReply(this, (Map) rJson));
                    }
                }
            }
        }

        public boolean isSpecial() {
            return false;
        }


        public Dialogue toDialogue(Map<WriterDialogue, Dialogue> visited, List<Dialogue> created, List<Dialogue> modified) {
            if (visited.get(this) != null) return visited.get(this);
            //Creating a new Dialogue
            if (dialogue == null) {
                dialogue = new Dialogue();
                dialogue.id = getID();
                dialogue.state = GameDataElement.State.parsed;
                created.add(dialogue);
            } else {
                if (hasChanged()) {
                    if (dialogue.writable) {
                        //Modifying a created or altered Dialogue
                        dialogue.state = GameDataElement.State.modified;
                        modified.add(dialogue);
                    } else {
                        //Altering a game source Dialogue
                        //Dialogue clone = (Dialogue) dialogue.clone();
                        dialogue.getProject().makeWritable(dialogue);
                        Dialogue clone = dialogue.getProject().getDialogue(dialogue.id);
                        if (this.replies != null) {
                            for (WriterReply wReply : this.replies) {
                                if (wReply.reply != null) {
                                    wReply.reply = clone.replies.get(dialogue.replies.indexOf(wReply.reply));
                                }
                            }
                        }
                        dialogue = clone;
                        dialogue.state = GameDataElement.State.parsed;
                        created.add(dialogue);
                    }
                }
            }
            visited.put(this, dialogue);
            dialogue.message = this.text;
            if (this.replies != null && !this.replies.isEmpty()) {
                if (dialogue.replies == null) {
                    dialogue.replies = new ArrayList<Dialogue.Reply>();
                } else {
                    dialogue.replies.clear();
                }
                for (WriterReply wReply : this.replies) {
                    //if (wReply.reply != null && dialogue.replies)
                    dialogue.replies.add(wReply.toReply(visited, created, modified));
                }
            } else {
                dialogue.replies = null;
            }
            return dialogue;
        }

        public boolean hasChanged() {
            return dialogue == null ||
                    text == null ? dialogue.message != null : !text.equals(dialogue.message) ||
                    repliesHaveChanged();
        }

        public boolean repliesHaveChanged() {
            if (replies.isEmpty() && (dialogue.replies == null || dialogue.replies.isEmpty())) return false;
            if (!replies.isEmpty() && (dialogue.replies == null || dialogue.replies.isEmpty())) return true;
            if (replies.isEmpty() && (dialogue.replies != null && !dialogue.replies.isEmpty())) return true;
            if (replies.size() != dialogue.replies.size()) return true;
            for (WriterReply reply : replies) {
                if (reply.hasChanged()) return true;
            }
            return false;
        }

    }

    public abstract class SpecialDialogue extends WriterDialogue {

        public SpecialDialogue() {
        }

        public boolean isSpecial() {
            return true;
        }

        public abstract SpecialDialogue duplicate();

        public SpecialDialogue(Dialogue dialogue) {
            super(dialogue);
        }
    }

    public class SelectorDialogue extends SpecialDialogue {
        public SelectorDialogue() {
        }

        public SpecialDialogue duplicate() {
            return new SelectorDialogue();
        }

        public SelectorDialogue(Dialogue dialogue) {
            super(dialogue);
        }
    }

    public class ShopDialogue extends SpecialDialogue {
        public static final String id = Dialogue.Reply.SHOP_PHRASE_ID;

        public SpecialDialogue duplicate() {
            return new ShopDialogue();
        }
    }

    public class FightDialogue extends SpecialDialogue {
        public static final String id = Dialogue.Reply.FIGHT_PHRASE_ID;

        public SpecialDialogue duplicate() {
            return new FightDialogue();
        }
    }

    public class EndDialogue extends SpecialDialogue {
        public static final String id = Dialogue.Reply.EXIT_PHRASE_ID;

        public SpecialDialogue duplicate() {
            return new EndDialogue();
        }
    }

    public class RemoveNPCDialogue extends SpecialDialogue {
        public static final String id = Dialogue.Reply.REMOVE_PHRASE_ID;

        public SpecialDialogue duplicate() {
            return new RemoveNPCDialogue();
        }
    }

    public class WriterReply extends WriterNode {
        public WriterDialogue parent;
        public String next_dialogue_id;
        public WriterDialogue next_dialogue;
        public Dialogue.Reply reply;

        public WriterReply() {
        }

        public WriterReply(WriterDialogue parent) {
            this.parent = parent;
            this.text = "";
            parent.replies.add(this);
        }

        public WriterReply(WriterDialogue parent, Dialogue.Reply reply) {
            this.parent = parent;
            this.reply = reply;
            this.text = reply.text;
            this.next_dialogue_id = reply.next_phrase_id;
            if (nodesById.get(this.next_dialogue_id) != null) {
                this.next_dialogue = nodesById.get(this.next_dialogue_id);
            } else if (reply.next_phrase != null) {
                this.next_dialogue = new WriterDialogue(reply.next_phrase);
            }
        }

        @SuppressWarnings("rawtypes")
        public WriterReply(WriterDialogue parent, Map json) {
            this.parent = parent;
            this.text = (String) json.get("text");
            if (json.containsKey("next_dialogue_id")) {
                next_dialogue_id = (String) json.get("next_dialogue_id");
            }
        }

        @Override
        public String getTitle() {
            return "Reply in " + parent.id_prefix + parent.index;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public Map toJson(List<WriterDialogue> visited, List<Map> jsonData) {
            Map replyJson = new LinkedHashMap();
            replyJson.put("text", text);
            replyJson.put("special", isSpecial());
            if (next_dialogue != null) {
                replyJson.put("next_dialogue_id", next_dialogue.getID());
                next_dialogue.toJson(visited, jsonData);
            }
            return replyJson;
        }

        public boolean isSpecial() {
            return false;
        }

        public Dialogue.Reply toReply(Map<WriterDialogue, Dialogue> visited, List<Dialogue> created, List<Dialogue> modified) {
            if (reply == null) {
                reply = new Dialogue.Reply();
            }
            reply.text = this.text;
            if (this.next_dialogue != null) {
                this.next_dialogue.toDialogue(visited, created, modified);
                reply.next_phrase_id = this.next_dialogue.getID();
            } else if (this.next_dialogue_id != null) {
                reply.next_phrase_id = this.next_dialogue_id;
            } else {
                reply.next_phrase_id = Dialogue.Reply.EXIT_PHRASE_ID;
            }
            return reply;
        }

        public boolean hasChanged() {
            if (reply == null) return true;
            if (text == null && reply.text != null) return true;
            if (text != null && reply.text == null) return true;
            if (text != null && !text.equals(reply.text)) return true;
            String targetDialogueId = next_dialogue != null ? next_dialogue.getID() : next_dialogue_id;
            String replyTargetDialogueId = reply.next_phrase != null ? reply.next_phrase.id : reply.next_phrase_id;
            if (targetDialogueId == null && replyTargetDialogueId != null) return true;
            if (targetDialogueId != null && replyTargetDialogueId == null) return true;
            if (targetDialogueId != null && !targetDialogueId.equals(replyTargetDialogueId)) return true;
            return false;
        }


    }

    public class SpecialReply extends WriterReply {

        public boolean isSpecial() {
            return true;
        }

        public SpecialReply(WriterDialogue parent, Dialogue.Reply reply) {
            super(parent, reply);
        }

        public SpecialReply(WriterDialogue parent) {
            super(parent);
        }

        public SpecialReply(WriterDialogue parent, @SuppressWarnings("rawtypes") Map json) {
            super(parent, json);
        }
    }

    public class EmptyReply extends SpecialReply {

        public EmptyReply(WriterDialogue parent, Dialogue.Reply reply) {
            super(parent, reply);
            text = Dialogue.Reply.GO_NEXT_TEXT;
        }

        public EmptyReply(WriterDialogue parent) {
            super(parent);
            text = Dialogue.Reply.GO_NEXT_TEXT;
        }

        public EmptyReply(WriterDialogue parent, @SuppressWarnings("rawtypes") Map json) {
            super(parent, json);
            text = Dialogue.Reply.GO_NEXT_TEXT;
        }
    }


    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + id;
    }

    @Override
    public Project getProject() {
        return parent.getProject();
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getDialogueIcon();
    }

    @Override
    public Image getOpenIcon() {
        return null;
    }

    @Override
    public Image getClosedIcon() {
        return null;
    }

    @Override
    public Image getLeafIcon() {
        return getIcon();
    }

    @Override
    public GameDataSet getDataSet() {
        return null;
    }

    @Override
    public GameDataElement clone() {
        //TODO
        return null;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        // Useless here.

    }

    @Override
    public String getProjectFilename() {
        return WriterModeDataSet.DEFAULT_REL_PATH_IN_PROJECT;
    }

    @Override
    public void save() {
        ((WriterModeDataSet) this.getParent()).save(this.jsonFile);
    }

    @Override
    public List<SaveEvent> attemptSave() {
        List<SaveEvent> events = ((WriterModeDataSet) parent).attemptSave();
        if (events == null || events.isEmpty()) {
            return null;
        }
        if (events.size() == 1 && events.get(0).type == SaveEvent.Type.alsoSave && events.get(0).target == this) {
            save();
            return null;
        }
        return events;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map toJson() {
        List<Map> jsonData = new ArrayList<Map>();
        begin.toJson(new ArrayList<WriterModeData.WriterDialogue>(), jsonData);
        Map jsonObj = new LinkedHashMap();
        jsonObj.put("id", id);
        jsonObj.put("dialogues", jsonData);
        return jsonObj;
    }

    @SuppressWarnings("rawtypes")
    public void parse() {
        if (shouldSkipParse()) return;
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List gameDataElements = (List) parser.parse(reader);
            for (Object obj : gameDataElements) {
                Map jsonObj = (Map) obj;
                String id = (String) jsonObj.get("id");
                if (id != null && id.equals(this.id)) {
                    this.parse(jsonObj);
                    this.state = State.parsed;
                    break;
                }
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
    public void parse(Map json) {
        this.id = (String) json.get("id");
//		this.sketchName = (String) json.get("name");
//		List jsonRootsId = (List) json.get("roots_id");
//		if (jsonRootsId != null) {
//			for (Object jsonRootId : jsonRootsId) {
//				rootsId.add((String) jsonRootId);
//			}
//		}
        List jsonDialogues = (List) json.get("dialogues");
        if (jsonDialogues != null) {
            for (Object jsonDialogue : jsonDialogues) {
                WriterDialogue dialogue = new WriterDialogue((Map) jsonDialogue);
                nodesById.put(dialogue.getID(), dialogue);
            }
        }

        this.state = State.parsed;
    }

    @Override
    public void link() {
        if (this.state == State.created) {
            this.begin = new WriterDialogue();
            begin.id_prefix = id;
            begin.index = getNextIndex(id);
            begin.text = "";
        }
        if (this.state == State.init) {
            //Not parsed yet.
            this.parse();
        }
        if (this.state == State.parsed) {
            for (String prefix : threadsNextIndex.keySet()) {
                while (getProject().getDialogue(prefix + threadsNextIndex.get(prefix)) != null) {
                    threadsNextIndex.put(prefix, threadsNextIndex.get(prefix) + 1);
                }
            }
            for (WriterDialogue dialogue : nodesById.values()) {
                if (dialogue.dialogue_id != null) {
                    dialogue.dialogue = getProject().getDialogue(dialogue.dialogue_id);
                }
                if (dialogue.replies == null) continue;
                for (WriterReply reply : dialogue.replies) {
                    if (reply.next_dialogue_id != null) {
                        if (isSpecial(reply.next_dialogue_id)) {
                            reply.next_dialogue = getSpecial(reply.next_dialogue_id);
                        } else {
                            reply.next_dialogue = nodesById.get(reply.next_dialogue_id);
                        }
                    }
                    //TODO Seriously, this is failure-prone by design. Can't do much better though...
                    if (dialogue.dialogue != null) {
                        List<Dialogue.Reply> linked = new ArrayList<Dialogue.Reply>(dialogue.dialogue.replies.size());
                        if (dialogue.dialogue != null && dialogue.dialogue.replies != null) {
                            //Try to hook to existing replies... not as easy when there's no ID.
                            Dialogue.Reply best = null;
                            int score, maxScore = 0;
                            for (Dialogue.Reply dReply : dialogue.dialogue.replies) {
                                //Never link twice to the same...
                                if (linked.contains(dReply)) continue;
                                score = 0;
                                //Arbitrary values... hopefully this gives good results.
                                //Same target gives good hope of preserving at least the structure.
                                if (dReply.next_phrase_id != null && dReply.next_phrase_id.equals(reply.next_dialogue_id))
                                    score += 50;
                                //Same text is almost as good as an ID, but there may be duplicates due to requirements system...
                                if (dReply.text != null && dReply.text.equals(reply.text)) score += 40;
                                //Same slot in the list. That's not so bad if all else fails, and could help sort duplicates with same text.
                                if (dialogue.dialogue.replies.indexOf(dReply) == dialogue.replies.indexOf(reply))
                                    score += 20;
                                //Both have null text. It's not much, but it's something....
                                if (dReply.text == null && reply.text == null) score += 10;
                                if (score > maxScore) {
                                    maxScore = score;
                                    best = dReply;
                                }
                            }
                            if (maxScore > 0) {
                                reply.reply = best;
                                linked.add(best);
                            }
                        }
                    }
                }
            }
            for (String rootId : rootsId) {
                roots.add(nodesById.get(rootId));
            }

        }
        if (this.state == State.linked) {
            //Already linked.
            return;
        }

        this.state = State.linked;
    }

    public boolean isSpecial(String id) {
        if (id == null) return false;
        if (ShopDialogue.id.equals(id)) return true;
        if (FightDialogue.id.equals(id)) return true;
        if (EndDialogue.id.equals(id)) return true;
        if (RemoveNPCDialogue.id.equals(id)) return true;
        return false;
    }

    public SpecialDialogue getSpecial(String id) {
        if (id == null) return null;
        if (ShopDialogue.id.equals(id)) return new ShopDialogue();
        if (FightDialogue.id.equals(id)) return new FightDialogue();
        if (EndDialogue.id.equals(id)) return new EndDialogue();
        if (RemoveNPCDialogue.id.equals(id)) return new RemoveNPCDialogue();
        return null;
    }

    public List<Dialogue> toDialogue() {
        Map<WriterModeData.WriterDialogue, Dialogue> visited = new LinkedHashMap<WriterModeData.WriterDialogue, Dialogue>();
        List<Dialogue> created = new ArrayList<Dialogue>();
        List<Dialogue> modified = new ArrayList<Dialogue>();
        begin.toDialogue(visited, created, modified);
        for (Dialogue modifiedDialogue : modified) {
            modifiedDialogue.childrenChanged(new ArrayList<ProjectTreeNode>());
        }
        return created;
    }

}
