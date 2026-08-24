package com.gpl.rpg.atcontentstudio.ui;

import com.gpl.rpg.atcontentstudio.model.GameDataElement;
import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.SaveEvent;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class SortedGDEComboModelTest {

    @Test
    public void selectedItemSyncsWithBaseModelInBothDirections() {
        TestComboModel baseModel = new TestComboModel();
        TestElement alpha = new TestElement("Alpha");
        TestElement bravo = new TestElement("Bravo");

        baseModel.add(alpha);
        baseModel.add(bravo);

        Editor.SortedGDEComboModel<TestElement> sortedModel = new Editor.SortedGDEComboModel<>(baseModel);

        Object alphaRow = sortedModel.getElementAt(1);
        sortedModel.setSelectedItem(alphaRow);
        assertSame(alpha, baseModel.getSelectedItem());
        assertSame(alphaRow, sortedModel.getSelectedItem());
        assertSame(alpha, sortedModel.getSelectedDelegate());

        baseModel.setSelectedItem(bravo);
        baseModel.fireContentsChanged();
        assertSame(bravo, sortedModel.getSelectedDelegate());
        assertSame(findWrappedRow(sortedModel, bravo), sortedModel.getSelectedItem());
    }

    @Test
    public void nullAndHeaderSelectionsDoNotBreakSynchronization() {
        TestComboModel baseModel = new TestComboModel();
        TestElement alpha = new TestElement("Alpha");
        baseModel.add(alpha);

        List<Editor.SortedGDEComboModel.Section<TestElement>> sections = Collections.singletonList(
                new Editor.SortedGDEComboModel.Section<>(
                        "Header",
                        element -> true,
                        Editor.SortedGDEComboModel.buildComparator(),
                        TestElement::getDesc
                )
        );
        Editor.SortedGDEComboModel<TestElement> sortedModel = new Editor.SortedGDEComboModel<>(baseModel, sections);

        Object headerRow = sortedModel.getElementAt(1);
        Object alphaRow = sortedModel.getElementAt(2);

        sortedModel.setSelectedItem(headerRow);
        assertNull(baseModel.getSelectedItem());
        assertNull(sortedModel.getSelectedItem());
        assertNull(sortedModel.getSelectedDelegate());

        sortedModel.setSelectedItem(alphaRow);
        assertSame(alpha, baseModel.getSelectedItem());
        assertSame(alphaRow, sortedModel.getSelectedItem());
        assertSame(alpha, sortedModel.getSelectedDelegate());

        sortedModel.setSelectedItem(null);
        assertNull(baseModel.getSelectedItem());
        assertNull(sortedModel.getSelectedItem());
        assertNull(sortedModel.getSelectedDelegate());
    }

    private static GameDataElement findWrappedRow(Editor.SortedGDEComboModel<TestElement> model, TestElement element) {
        for (int i = 1; i < model.getSize(); i++) {
            GameDataElement row = model.getElementAt(i);
            if (row != null && row.getDesc().equals(element.getDesc())) {
                return row;
            }
        }
        return null;
    }

    private static final class TestElement extends GameDataElement {
        private final String desc;

        private TestElement(String desc) {
            this.desc = desc;
        }

        @Override
        public String getDesc() {
            return desc;
        }

        @Override
        public void parse() {
        }

        @Override
        public void link() {
        }

        @Override
        public GameDataElement clone() {
            return new TestElement(desc);
        }

        @Override
        public void elementChanged(GameDataElement oldOne, GameDataElement newOne) {
        }

        @Override
        public String getProjectFilename() {
            return desc;
        }

        @Override
        public void save() {
        }

        @Override
        public List<SaveEvent> attemptSave() {
            return Collections.emptyList();
        }

        @Override
        public GameSource.Type getDataType() {
            return null;
        }

        @Override
        public GameDataSet getDataSet() {
            return null;
        }
    }

    private static final class TestComboModel extends Editor.GDEComboModel<TestElement> {
        private final List<TestElement> items = new ArrayList<>();

        private TestComboModel() {
            super(null, null);
        }

        private void add(TestElement item) {
            items.add(item);
            itemAdded(item, items.size() - 1);
        }

        private void fireContentsChanged() {
            fireContentsChanged(this, 0, Math.max(0, getSize() - 1));
        }

        @Override
        public int getSize() {
            return items.size() + 1;
        }

        @Override
        public TestElement getTypedElementAt(int index) {
            return items.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = (TestElement) anItem;
        }

    }
}
