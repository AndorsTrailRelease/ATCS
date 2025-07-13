package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.ATContentStudio;
import com.gpl.rpg.atcontentstudio.Notification;
import com.gpl.rpg.atcontentstudio.NotificationListener;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


@SuppressWarnings("rawtypes")
public class NotificationsPane extends JList {

    private static final long serialVersionUID = -1100364214372392608L;

    public static final Map<Notification.Type, Icon> icons = new LinkedHashMap<Notification.Type, Icon>(Notification.Type.values().length);

    static {
        icons.put(Notification.Type.SUCCESS, new ImageIcon(DefaultIcons.getStatusGreenIcon()));
        icons.put(Notification.Type.INFO, new ImageIcon(DefaultIcons.getStatusBlueIcon()));
        icons.put(Notification.Type.WARN, new ImageIcon(DefaultIcons.getStatusOrangeIcon()));
        icons.put(Notification.Type.ERROR, new ImageIcon(DefaultIcons.getStatusRedIcon()));
    }


    @SuppressWarnings("unchecked")
    public NotificationsPane() {
        super();
        MyListModel model = new MyListModel();
        setModel(model);
        setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = new JLabel();
                Font f = label.getFont();
                label.setIcon(NotificationsPane.icons.get(((Notification) value).type));
                label.setText(((Notification) value).text);
                if (isSelected) {
//					label.setBackground(Color.RED);
                    label.setBorder(BorderFactory.createLineBorder(Color.BLUE));
//					label.setForeground(Color.WHITE);
                }
                f = f.deriveFont(10f * ATContentStudio.SCALING);
                label.setFont(f);
                return label;
            }
        });
        Notification.addNotificationListener(model);
    }


    private class MyListModel implements ListenerListModel<Notification>, NotificationListener {

        @Override
        public Notification getElementAt(int index) {
            return Notification.notifs.get(index);
        }

        @Override
        public List<ListDataListener> getListeners() {
            return listeners;
        }

        @Override
        public int getSize() {
            return Notification.notifs.size();
        }

        @Override
        public void onNewNotification(Notification n) {
            notifyListeners(NotificationsPane.this, ChangeType.ADDED, Notification.notifs.size() - 1, Notification.notifs.size() - 1);
            NotificationsPane.this.ensureIndexIsVisible(Notification.notifs.indexOf(n));
        }

        @Override
        public void onListCleared(int i) {
            notifyListeners(NotificationsPane.this, ChangeType.REMOVED, 0, i);
        }

        private List<ListDataListener> listeners = new CopyOnWriteArrayList<ListDataListener>();
    }
}