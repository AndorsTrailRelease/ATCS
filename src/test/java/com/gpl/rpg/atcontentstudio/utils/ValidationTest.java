package com.gpl.rpg.atcontentstudio.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationTest {

    @Test
    public void acceptsLowercaseInternalIds() {
        assertTrue(Validation.isValidInternalId("quest_1"));
    }

    @Test
    public void rejectsInvalidInternalIds() {
        assertFalse(Validation.isValidInternalId("Bad Id"));
    }
}
