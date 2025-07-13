package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
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


public class Droplist extends JSONElement {

    private static final long serialVersionUID = -2903944916807382571L;

    //Available from init state
    //public String id = null; inherited.

    //Available from parsed state;
    public List<DroppedItem> dropped_items = null;

    //Available from linked state;
    //None

    public static class DroppedItem {
        //Available from parsed state;
        public String item_id = null;
        public String chance = null;
        public Integer quantity_min = null;
        public Integer quantity_max = null;

        //Available from linked state;
        public Item item = null;
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + id;
    }

    public static String getStaticDesc() {
        return "Droplists";
    }

    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<Droplist> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List droplists = (List) parser.parse(reader);
            for (Object obj : droplists) {
                Map droplistJson = (Map) obj;
                Droplist droplist = fromJson(droplistJson);
                droplist.jsonFile = jsonFile;
                droplist.parent = category;
                if (droplist.getDataType() == GameSource.Type.created || droplist.getDataType() == GameSource.Type.altered) {
                    droplist.writable = true;
                }
                category.add(droplist);
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
    public static Droplist fromJson(String jsonString) throws ParseException {
        Map droplistJson = (Map) new JSONParser().parse(jsonString);
        Droplist droplist = fromJson(droplistJson);
        droplist.parse(droplistJson);
        return droplist;
    }

    @SuppressWarnings("rawtypes")
    public static Droplist fromJson(Map droplistJson) {
        Droplist droplist = new Droplist();
        droplist.id = (String) droplistJson.get("id");
        return droplist;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map droplistJson) {
        List droppedItemsJson = (List) droplistJson.get("items");
        if (droppedItemsJson != null && !droppedItemsJson.isEmpty()) {
            this.dropped_items = new ArrayList<DroppedItem>();
            for (Object droppedItemJsonObj : droppedItemsJson) {
                Map droppedItemJson = (Map) droppedItemJsonObj;
                DroppedItem droppedItem = new DroppedItem();
                droppedItem.item_id = (String) droppedItemJson.get("itemID");
                //if (droppedItemJson.get("chance") != null) droppedItem.chance = JSONElement.parseChance(droppedItemJson.get("chance").toString());
                droppedItem.chance = (String) droppedItemJson.get("chance");
                Map droppedItemQtyJson = (Map) droppedItemJson.get("quantity");
                if (droppedItemQtyJson != null) {
                    droppedItem.quantity_min = JSONElement.getInteger((Number) droppedItemQtyJson.get("min"));
                    droppedItem.quantity_max = JSONElement.getInteger((Number) droppedItemQtyJson.get("max"));
                }
                this.dropped_items.add(droppedItem);
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
            Notification.addError("Error linking droplist " + id + ". No parent project found.");
            return;
        }
        if (dropped_items != null) {
            for (DroppedItem droppedItem : dropped_items) {
                if (droppedItem.item_id != null) droppedItem.item = proj.getItem(droppedItem.item_id);
                if (droppedItem.item != null) droppedItem.item.addBacklink(this);
            }
        }
        this.state = State.linked;
    }


    public static Image getImage() {
        return DefaultIcons.getDroplistImage();
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getDroplistIcon();
    }

    @Override
    public GameDataElement clone() {
        Droplist clone = new Droplist();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        if (this.dropped_items != null) {
            clone.dropped_items = new ArrayList<Droplist.DroppedItem>();
            for (DroppedItem di : this.dropped_items) {
                DroppedItem diclone = new DroppedItem();
                diclone.chance = di.chance;
                diclone.item_id = di.item_id;
                diclone.quantity_min = di.quantity_min;
                diclone.quantity_max = di.quantity_max;
                diclone.item = di.item;
                if (diclone.item != null) {
                    diclone.item.addBacklink(clone);
                }
                clone.dropped_items.add(diclone);
            }
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (dropped_items != null) {
            for (DroppedItem di : dropped_items) {
                if (di.item == oldOne) {
                    oldOne.removeBacklink(this);
                    di.item = (Item) newOne;
                    if (newOne != null) newOne.addBacklink(this);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map droplistJson = new LinkedHashMap();
        droplistJson.put("id", this.id);
        if (this.dropped_items != null) {
            List droppedItemsJson = new ArrayList();
            droplistJson.put("items", droppedItemsJson);
            for (DroppedItem droppedItem : this.dropped_items) {
                Map droppedItemJson = new LinkedHashMap();
                droppedItemsJson.add(droppedItemJson);
                if (droppedItem.item != null) {
                    droppedItemJson.put("itemID", droppedItem.item.id);
                } else if (droppedItem.item_id != null) {
                    droppedItemJson.put("itemID", droppedItem.item_id);
                }
                //if (droppedItem.chance != null) droppedItemJson.put("chance", JSONElement.printJsonChance(droppedItem.chance));
                if (droppedItem.chance != null) droppedItemJson.put("chance", droppedItem.chance);
                if (droppedItem.quantity_min != null || droppedItem.quantity_max != null) {
                    Map quantityJson = new LinkedHashMap();
                    droppedItemJson.put("quantity", quantityJson);
                    if (droppedItem.quantity_min != null) quantityJson.put("min", droppedItem.quantity_min);
                    else quantityJson.put("min", 0);
                    if (droppedItem.quantity_max != null) quantityJson.put("max", droppedItem.quantity_max);
                    else quantityJson.put("max", 0);
                }
            }
        }
        return droplistJson;
    }


    @Override
    public String getProjectFilename() {
        return "droplists_" + getProject().name + ".json";
    }

}
