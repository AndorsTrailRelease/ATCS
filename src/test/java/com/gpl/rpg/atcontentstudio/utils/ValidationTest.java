package com.gpl.rpg.atcontentstudio.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidationTest {

    @Test
    public void acceptsLowercaseInternalIds() {
        assertTrue(Validation.isValidInternalId("quest_1"));
    }

    @Test
    public void acceptsIdsWithOnlyAllowedCharacters() {
        assertTrue(Validation.isValidInternalId("a"));
        assertTrue(Validation.isValidInternalId("a_b2_3"));
        assertTrue(Validation.isValidInternalId("123"));
        assertTrue(Validation.isValidInternalId("id_"));
    }

    @Test
    public void rejectsNullOrEmptyIds() {
        assertFalse(Validation.isValidInternalId(""));
    }

    @Test
    public void rejectsNullString() {
        assertFalse(Validation.isValidInternalId(null));
    }

    @Test
    public void rejectsLeadingAndTrailingSpaces() {
        assertFalse(Validation.isValidInternalId(" bad"));
        assertFalse(Validation.isValidInternalId("bad "));
    }

    @Test
    public void rejectsUppercaseLetters() {
        assertFalse(Validation.isValidInternalId("Bad"));
        assertFalse(Validation.isValidInternalId("quest_ID"));
    }

    @Test
    public void rejectsWhitespaceAndPunctuation() {
        assertFalse(Validation.isValidInternalId("Bad Id"));
        assertFalse(Validation.isValidInternalId("bad-id"));
        assertFalse(Validation.isValidInternalId("bad.id"));
    }

    @Test
    public void reportsTheExpectedValidationError() {
        assertEquals("Internal IDs may only contain lowercase letters, digits, and underscores.",
                Validation.getInternalIdValidationError());
    }
}
