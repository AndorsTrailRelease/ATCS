package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.File;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemFilter extends JSONElement {

    @Serial
    private static final long serialVersionUID = 6141158246270055121L;

    public List<FilteredItem> include = new ArrayList<>();

    public static class FilteredItem {
        public String item_id = null;
        public Item item = null;
    }

    public static String getStaticDesc() {
        return "Item filters";
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + id;
    }

    public static void fromJson(File jsonFile, GameDataCategory<ItemFilter> category) {
        try (java.io.FileReader reader = new java.io.FileReader(jsonFile)) {
            @SuppressWarnings("unchecked")
            List<Map> filters = (List<Map>) new JSONParser().parse(reader);
            for (Map filterJson : filters) {
                ItemFilter filter = fromJson(filterJson);
                filter.jsonFile = jsonFile;
                filter.parent = category;
                        if (filter.getDataType() == GameSource.Type.created
                                || filter.getDataType() == GameSource.Type.altered) {
                    filter.writable = true;
                }
                filter.parse(filterJson);
                category.add(filter);
            }
        } catch (java.io.FileNotFoundException e) {
            com.gpl.rpg.atcontentstudio.Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (java.io.IOException e) {
            com.gpl.rpg.atcontentstudio.Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            com.gpl.rpg.atcontentstudio.Notification.addError("Error while parsing JSON file " + jsonFile.getAbsolutePath() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    public static ItemFilter fromJson(String jsonString) throws ParseException {
        Map json = (Map) new JSONParser().parse(jsonString);
        ItemFilter filter = fromJson(json);
        filter.parse(json);
        return filter;
    }

    @SuppressWarnings("rawtypes")
    public static ItemFilter fromJson(Map json) {
        ItemFilter filter = new ItemFilter();
        filter.id = (String) json.get("id");
        return filter;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map json) {
        include = new ArrayList<>();
        List items = (List) json.get("include");
        if (items != null) {
            for (Object itemObj : items) {
                if (itemObj != null) {
                    FilteredItem item = new FilteredItem();
                    if (itemObj instanceof Map itemJson) {
                        item.item_id = (String) itemJson.get("itemID");
                    } else {
                        item.item_id = itemObj.toString();
                    }
                    include.add(item);
                }
            }
        }
        state = State.parsed;
    }

    @Override
    public void link() {
        if (shouldSkipParseOrLink()) {
            return;
        }
        ensureParseIfNeeded();
        Project proj = getProject();
        if (proj == null) {
            com.gpl.rpg.atcontentstudio.Notification.addError("Error linking item filter " + id + ". No parent project found.");
            return;
        }
        if (include != null) {
            for (FilteredItem filteredItem : include) {
                if (filteredItem.item_id != null) {
                    filteredItem.item = proj.getItem(filteredItem.item_id);
                }
                addItemBacklink(filteredItem);
            }
        }
        state = State.linked;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map json = new LinkedHashMap();
        json.put("id", id);
        if (include != null) {
            List includeJson = new ArrayList();
            List itemsJson = new ArrayList();
            json.put("include", includeJson);
//             For possible future schema change to item list instead of raw IDs
//             json.put("items", itemsJson);
            for (FilteredItem filteredItem : include) {
                String itemId = filteredItem.item != null ? filteredItem.item.id : filteredItem.item_id;
                includeJson.add(itemId);

//                Map itemJson = new LinkedHashMap();
//                itemJson.put("itemID", itemId);
//                itemsJson.add(itemJson);
            }
        }
        return json;
    }

    @Override
    public GameDataElement clone() {
        ItemFilter clone = new ItemFilter();
        clone.jsonFile = jsonFile;
        clone.state = state;
        clone.id = id;
        if (include != null) {
            clone.include = new ArrayList<>();
            for (FilteredItem filteredItem : include) {
                FilteredItem filteredItemClone = new FilteredItem();
                filteredItemClone.item_id = filteredItem.item_id;
                filteredItemClone.item = filteredItem.item;
                if (filteredItemClone.item != null) {
                    filteredItemClone.item.addBacklink(clone);
                }
                clone.include.add(filteredItemClone);
            }
        }
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        if (include != null) {
            for (FilteredItem filteredItem : include) {
                if (filteredItem.item == oldOne) {
                    removeItemBacklink(filteredItem);
                    filteredItem.item = (Item) newOne;
                    addItemBacklink(filteredItem);
                }
            }
        }
    }

    public void addItemBacklink(FilteredItem filteredItem) {
        if (filteredItem != null && filteredItem.item != null) {
            filteredItem.item.addBacklink(this);
        }
    }

    public void removeItemBacklink(FilteredItem filteredItem) {
        if (filteredItem != null && filteredItem.item != null) {
            filteredItem.item.removeBacklink(this);
        }
    }

    @Override
    public String getProjectFilename() {
        return "itemfilters_" + getProject().name + ".json";
    }

    @Override
    public Image getIcon() {
        return DefaultIcons.getContainerIcon();
    }
}
