package com.gpl.rpg.atcontentstudio.ui.tools;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.Editor;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class ElementTableView extends Editor {

    private static final long serialVersionUID = 8048693233599125878L;

    public ElementTableView(TableModel elementTableModel, String title, Icon icon) {
        this.target = new DummyGDE();
        this.name = title;
        this.icon = icon;

        setLayout(new BorderLayout());

        JTable table = new JTable(elementTableModel) {
            private static final long serialVersionUID = -2738230330859706440L;

            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setAutoCreateRowSorter(true);
        table.setAutoscrolls(true);
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void targetUpdated() {

    }

    class DummyGDE extends GameDataElement {

        private static final long serialVersionUID = 5889666999423783180L;

        @Override
        public GameDataSet getDataSet() {
            return null;
        }

        @Override
        public String getDesc() {
            return null;
        }

        @Override
        public void parse() {
        }

        @Override
        public void link() {
        }

        @Override
        public GameDataElement clone() {
            return null;
        }

        @Override
        public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        }

        @Override
        public String getProjectFilename() {
            return null;
        }

        @Override
        public void save() {
        }

        @Override
        public List<SaveEvent> attemptSave() {
            return null;
        }

    }

}
