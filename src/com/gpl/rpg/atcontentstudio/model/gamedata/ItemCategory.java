package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemCategory extends JSONElement {

    private static final long serialVersionUID = -348864002519568300L;

    public static final String ICON_NO_SLOT_RES = "/com/gpl/rpg/atcontentstudio/img/equip_square.png";
    public static final String ICON_BODY_RES = "/com/gpl/rpg/atcontentstudio/img/equip_body.png";
    public static final String ICON_FEET_RES = "/com/gpl/rpg/atcontentstudio/img/equip_feet.png";
    public static final String ICON_HAND_RES = "/com/gpl/rpg/atcontentstudio/img/equip_hand.png";
    public static final String ICON_HEAD_RES = "/com/gpl/rpg/atcontentstudio/img/equip_head.png";
    public static final String ICON_NECK_RES = "/com/gpl/rpg/atcontentstudio/img/equip_neck.png";
    public static final String ICON_RING_RES = "/com/gpl/rpg/atcontentstudio/img/equip_ring.png";
    public static final String ICON_SHIELD_RES = "/com/gpl/rpg/atcontentstudio/img/equip_shield.png";
    public static final String ICON_WEAPON_RES = "/com/gpl/rpg/atcontentstudio/img/equip_weapon.png";

    public static Image no_slot_image = null;
    public static Image no_slot_icon = null;

    public static Image body_image = null;
    public static Image body_icon = null;

    public static Image feet_image = null;
    public static Image feet_icon = null;

    public static Image hand_image = null;
    public static Image hand_icon = null;

    public static Image head_image = null;
    public static Image head_icon = null;

    public static Image neck_image = null;
    public static Image neck_icon = null;

    public static Image ring_image = null;
    public static Image ring_icon = null;

    public static Image shield_image = null;
    public static Image shield_icon = null;

    public static Image weapon_image = null;
    public static Image weapon_icon = null;


    //Available from init state
    //public String id = null; inherited.
    public String name = null;
    public InventorySlot slot = null;

    //Available from parsed state
    public ActionType action_type = null;
    public Size size = null;

    //Available from linked state
    //None

    public static enum ActionType {
        none,
        use,
        equip
    }

    public static enum Size {
        none,
        light,
        std,
        large
    }

    public static enum InventorySlot {
        weapon,
        shield,
        head,
        body,
        hand,
        feet,
        neck,
        leftring,
        rightring
    }

    @Override
    public String getDesc() {
        return (needsSaving() ? "*" : "") + name + " (" + id + ")";
    }

    public static String getStaticDesc() {
        return "Item categories";
    }


    @SuppressWarnings("rawtypes")
    public static void fromJson(File jsonFile, GameDataCategory<ItemCategory> category) {
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List itemCategories = (List) parser.parse(reader);
            for (Object obj : itemCategories) {
                Map itemCatJson = (Map) obj;
                ItemCategory itemCat = fromJson(itemCatJson);
                itemCat.jsonFile = jsonFile;
                itemCat.parent = category;
                if (itemCat.getDataType() == GameSource.Type.created || itemCat.getDataType() == GameSource.Type.altered) {
                    itemCat.writable = true;
                }
                category.add(itemCat);
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
    public static ItemCategory fromJson(String jsonString) throws ParseException {
        Map itemCatJson = (Map) new JSONParser().parse(jsonString);
        ItemCategory item = fromJson(itemCatJson);
        item.parse(itemCatJson);
        return item;
    }

    @SuppressWarnings("rawtypes")
    public static ItemCategory fromJson(Map itemCatJson) {
        ItemCategory itemCat = new ItemCategory();
        itemCat.id = (String) itemCatJson.get("id");
        itemCat.name = (String) itemCatJson.get("name");
        if (itemCatJson.get("inventorySlot") != null)
            itemCat.slot = InventorySlot.valueOf((String) itemCatJson.get("inventorySlot"));
        return itemCat;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void parse(Map itemCatJson) {
        if (itemCatJson.get("actionType") != null)
            action_type = ActionType.valueOf((String) itemCatJson.get("actionType"));
        if (itemCatJson.get("size") != null) size = Size.valueOf((String) itemCatJson.get("size"));
        this.state = State.parsed;

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
    public Image getIcon() {
        return getIcon(this.slot);
    }

    public Image getImage() {
        return getImage(this.slot);
    }

    public static Image getImage(InventorySlot slot) {
        if (slot == null) {
            return getImage(ICON_NO_SLOT_RES, no_slot_image, "no_slot_image");
        }
        switch (slot) {
            case body:
                return getImage(ICON_BODY_RES, body_image, "body_image");
            case feet:
                return getImage(ICON_FEET_RES, feet_image, "feet_image");
            case hand:
                return getImage(ICON_HAND_RES, hand_image, "hand_image");
            case head:
                return getImage(ICON_HEAD_RES, head_image, "head_image");
            case leftring:
            case rightring:
                return getImage(ICON_RING_RES, ring_image, "ring_image");
            case neck:
                return getImage(ICON_NECK_RES, neck_image, "neck_image");
            case shield:
                return getImage(ICON_SHIELD_RES, shield_image, "shield_image");
            case weapon:
                return getImage(ICON_WEAPON_RES, weapon_image, "weapon_image");
            default:
                return getImage(ICON_NO_SLOT_RES, no_slot_image, "no_slot_image");
        }
    }

    public static Image getIcon(InventorySlot slot) {
        if (slot == null) {
            return getIcon(ICON_NO_SLOT_RES, no_slot_image, no_slot_icon, "no_slot_image", "no_slot_icon");
        }
        switch (slot) {
            case body:
                return getIcon(ICON_BODY_RES, body_image, body_icon, "body_image", "body_icon");
            case feet:
                return getIcon(ICON_FEET_RES, feet_image, feet_icon, "feet_image", "feet_icon");
            case hand:
                return getIcon(ICON_HAND_RES, hand_image, hand_icon, "hand_image", "hand_icon");
            case head:
                return getIcon(ICON_HEAD_RES, head_image, head_icon, "head_image", "head_icon");
            case leftring:
            case rightring:
                return getIcon(ICON_RING_RES, ring_image, ring_icon, "ring_image", "ring_icon");
            case neck:
                return getIcon(ICON_NECK_RES, neck_image, neck_icon, "neck_image", "neck_icon");
            case shield:
                return getIcon(ICON_SHIELD_RES, shield_image, shield_icon, "shield_image", "shield_icon");
            case weapon:
                return getIcon(ICON_WEAPON_RES, weapon_image, weapon_icon, "weapon_image", "weapon_icon");
            default:
                return getIcon(ICON_NO_SLOT_RES, no_slot_image, no_slot_icon, "no_slot_image", "no_slot_icon");
        }
    }

    public static Image getImage(String res, Image img, String fieldName) {
        if (img == null) {
            try {
                img = ImageIO.read(ItemCategory.class.getResourceAsStream(res));
                ItemCategory.class.getField(fieldName).set(null, img);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Notification.addError("Failed to load item category icon " + res);
                e.printStackTrace();
            }
        }
        return img;
    }

    public static Image getIcon(String res, Image img, Image icon, String imgFieldName, String iconFieldName) {
        if (icon == null) {
            icon = getImage(res, img, imgFieldName).getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            try {
                ItemCategory.class.getField(iconFieldName).set(null, icon);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return icon;
    }

    @Override
    public GameDataElement clone() {
        ItemCategory clone = new ItemCategory();
        clone.jsonFile = this.jsonFile;
        clone.state = this.state;
        clone.id = this.id;
        clone.name = this.name;
        clone.size = this.size;
        clone.slot = this.slot;
        clone.action_type = this.action_type;
        return clone;
    }

    @Override
    public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        // Nothing to link to.
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map toJson() {
        Map itemCatJson = new LinkedHashMap();
        itemCatJson.put("id", this.id);
        if (this.name != null) itemCatJson.put("name", this.name);
        if (this.action_type != null) itemCatJson.put("actionType", this.action_type.toString());
        if (this.size != null) itemCatJson.put("size", this.size.toString());
        if (this.slot != null) itemCatJson.put("inventorySlot", this.slot.toString());
        return itemCatJson;
    }


    @Override
    public String getProjectFilename() {
        return "itemcategories_" + getProject().name + ".json";
    }


}
