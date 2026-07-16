package com.gpl.rpg.atcontentstudio.model.gamedata;

import com.gpl.rpg.atcontentstudio.Notification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JSONElementValidationTest {

    private boolean originalShowErrors;

    @Before
    public void setUp() {
        originalShowErrors = Notification.showE;
        Notification.showE = true;
        Notification.clear();
    }

    @After
    public void tearDown() {
        Notification.clear();
        Notification.showE = originalShowErrors;
    }

    @Test
    public void validateIdsIgnoresMapsWithoutIdField() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", "dialogue");
        List<Object> children = new ArrayList<>();
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("text", "hello");
        children.add(nested);
        root.put("replies", children);

        JSONElement.validateIds(root, new File("/tmp/sample.json"), "");

        assertTrue(Notification.notifs.isEmpty());
    }

    @Test
    public void validateIdsReportsInvalidIdOnlyForIdBearingMaps() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("id", "bad id");
        root.put("name", "dialogue");
        Map<String, Object> child = new LinkedHashMap<>();
        child.put("id", "good_id");
        root.put("replies", java.util.Collections.singletonList(child));

        JSONElement.validateIds(root, new File("/tmp/sample.json"), "");

        assertEquals(1, Notification.notifs.size());
        assertEquals(Notification.Type.ERROR, Notification.notifs.get(0).type);
        assertTrue(Notification.notifs.get(0).text.contains("bad id"));
    }
}
