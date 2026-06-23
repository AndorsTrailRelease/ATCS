package com.gpl.rpg.atcontentstudio.io;

import com.gpl.rpg.atcontentstudio.Notification;

import java.io.*;

public class SettingsSave {

    /**
     * Serializes an object to a file.  No longer used for Projects since June 2025; still used for other data.
     * @param obj - object to serialize
     * @param f - file to serialize to
     * @param type - Object type, used in notification error messages only
     */
    public static void saveInstance(Object obj, File f, String type) {
        try {
            FileOutputStream fos = new FileOutputStream(f);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(obj);
                oos.flush();
                oos.close();
                Notification.addSuccess(type + " successfully saved.");
            } catch (IOException e) {
                e.printStackTrace();
                Notification.addError(type + " saving error: " + e.getMessage());
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Notification.addError(type + " saving error: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Notification.addError(type + " saving error: " + e.getMessage());
        }
    }

    /**
     * Deserializes an object from a file.
     * No longer used for Projects since June 2025, except for compatibility, but used by ConfigCache and Workspace
     * TODO: Convert ConfigCache and Workspace saves to json format also?
     * @param f - file to deserialize from
     * @param type - used in notification error messages only
     * @return - deserialized object, to be cast to the appropriate type by caller
     */
    public static Object loadInstance(File f, String type) {
        FileInputStream fis;
        Object result = null;
        try {
            fis = new FileInputStream(f);
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(fis);
                try {
                    result = ois.readObject();
                    Notification.addSuccess(type + " successfully loaded.");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Notification.addError(type + " loading error: " + e.getMessage());
                } finally {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Notification.addError(type + " loading error: " + e.getMessage());
            } finally {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Notification.addError(type + " loading error: " + e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Notification.addError(type + " loading error: " + e.getMessage());
        }
        return result;
    }

}
