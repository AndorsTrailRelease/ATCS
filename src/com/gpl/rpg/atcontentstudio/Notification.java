package com.gpl.rpg.atcontentstudio;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Notification {

    public static List<Notification> notifs = new ArrayList<Notification>();
    private static List<NotificationListener> listeners = new CopyOnWriteArrayList<NotificationListener>();
    public static boolean showS, showI, showW, showE;

    static {
        boolean[] config = ConfigCache.getNotifViewConfig();
        showS = config[0];
        showI = config[1];
        showW = config[2];
        showE = config[3];
    }

    public static enum Type {
        SUCCESS,
        INFO,
        WARN,
        ERROR
    }

    public Type type;
    public String text;

    public Notification(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    public String toString() {
        return "[" + type.toString() + "] " + text;
    }

    public static void clear() {
        int i = notifs.size();
        notifs.clear();
        for (NotificationListener l : listeners) {
            l.onListCleared(i);
        }
    }

    public static void addSuccess(String text) {
        if (!showS) return;
        Notification n = new Notification(Notification.Type.SUCCESS, text);
        notifs.add(n);
        for (NotificationListener l : listeners) {
            l.onNewNotification(n);
        }
    }

    public static void addInfo(String text) {
        if (!showI) return;
        Notification n = new Notification(Notification.Type.INFO, text);
        notifs.add(n);
        for (NotificationListener l : listeners) {
            l.onNewNotification(n);
        }
    }

    public static void addWarn(String text) {
        if (!showW) return;
        Notification n = new Notification(Notification.Type.WARN, text);
        notifs.add(n);
        for (NotificationListener l : listeners) {
            l.onNewNotification(n);
        }
    }

    public static void addError(String text) {
        if (!showE) return;
        Notification n = new Notification(Notification.Type.ERROR, text);
        notifs.add(n);
        for (NotificationListener l : listeners) {
            l.onNewNotification(n);
        }
    }

    public static void addNotificationListener(NotificationListener l) {
        listeners.add(l);
    }

    public static void removeNotificationListener(NotificationListener l) {
        listeners.remove(l);
    }

}