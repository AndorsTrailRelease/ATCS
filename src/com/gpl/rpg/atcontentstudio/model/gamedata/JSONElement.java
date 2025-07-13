package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.io.JsonPrettyWriter;
import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.List;
import java.util.Map;

public abstract class JSONElement extends GameDataElement {

    private static final long serialVersionUID = -8015398814080503982L;

    //Available from state init.
    public File jsonFile;

    @SuppressWarnings("rawtypes")
    public void parse() {
        if (shouldSkipParse()) {
            return;
        }
        JSONParser parser = new JSONParser();
        FileReader reader = null;
        try {
            reader = new FileReader(jsonFile);
            List gameDataElements = (List) parser.parse(reader);
            for (Object obj : gameDataElements) {
                Map jsonObj = (Map) obj;
                String id = (String) jsonObj.get("id");
                try {
                    if (id != null && id.equals(this.id)) {
                        this.parse(jsonObj);
                        this.state = State.parsed;
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Error in ID: " + id);
                    System.out.println(e.getMessage());
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
        } catch (IllegalArgumentException e) {
            System.out.println(id);
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

    public abstract void parse(@SuppressWarnings("rawtypes") Map jsonObj);

    @SuppressWarnings("rawtypes")
    public abstract Map toJson();

    public String toJsonString() {
        StringWriter writer = new JsonPrettyWriter();
        try {
            JSONObject.writeJSONString(this.toJson(), writer);
        } catch (IOException e) {
            //Impossible with a StringWriter
        }
        return writer.toString();
    }


    @Override
    public GameDataSet getDataSet() {
        if (parent == null) {
            System.out.println("blerf.");
        }
        return parent.getDataSet();
    }

    public void save() {
        if (this.getParent() instanceof GameDataCategory<?> && writable) {
            ((GameDataCategory<?>) this.getParent()).save(this.jsonFile);
        }
    }

    /**
     * Returns null if save occurred (no notable events).
     */
    public List<SaveEvent> attemptSave() {
        List<SaveEvent> events = ((GameDataCategory<?>) this.getParent()).attemptSave(true, this.jsonFile.getName());
        if (events == null || events.isEmpty()) {
            return null;
        }
        if (events.size() == 1 && events.get(0).type == SaveEvent.Type.alsoSave && events.get(0).target == this) {
            save();
            return null;
        }
        return events;
    }

    public static Integer getInteger(Number n) {
        return n == null ? null : n.intValue();
    }

    public static Double getDouble(Number n) {
        return n == null ? null : n.doubleValue();
    }

    public static Double parseChance(String s) {
        if (s.equals("100")) return 100d;
        else if (s.equals("70")) return 70d;
        else if (s.equals("30")) return 30d;
        else if (s.equals("25")) return 25d;
        else if (s.equals("20")) return 20d;
        else if (s.equals("10")) return 10d;
        else if (s.equals("5")) return 5d;
        else if (s.equals("1")) return 1d;
        else if (s.equals("1/1000")) return 0.1;
        else if (s.equals("1/10000")) return 0.01;
        else if (s.indexOf('/') >= 0) {
            int c = s.indexOf('/');
            double a = 1;
            try {
                a = Integer.parseInt(s.substring(0, c));
            } catch (NumberFormatException nfe) {
            }
            double b = 100;
            try {
                b = Integer.parseInt(s.substring(c + 1));
            } catch (NumberFormatException nfe) {
            }
            return a / b;
        } else {
            double a = 10;
            try {
                a = Double.parseDouble(s);
            } catch (NumberFormatException nfe) {
            }
            return a;
        }
    }

    public static String printJsonChance(Double chance) {
        if (chance.equals(100d)) return "100";
        else if (chance.equals(70d)) return "70";
        else if (chance.equals(30d)) return "30";
        else if (chance.equals(25d)) return "25";
        else if (chance.equals(20d)) return "20";
        else if (chance.equals(10d)) return "10";
        else if (chance.equals(5d)) return "5";
        else if (chance.equals(1d)) return "1";
        else if (chance.equals(0.1d)) return "1/1000";
        else if (chance.equals(0.01d)) return "1/10000";
        else {
            if (chance.intValue() == chance) return Integer.toString(chance.intValue());
            //TODO Better handling of fractions. Chance description need a complete overhaul in AT.
            //This part does not output the input content of parseDouble(String s) in the case of fractions.
            return chance.toString();
        }
    }

}
