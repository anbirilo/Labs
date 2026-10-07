package com.andrew.lab2b;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class LexemeProcessorTest {

    private final LexemeProcessor processor = new LexemeProcessor();

    @Test
    void tokenizesBySpaceDelimiter() {
        ProcessingResult result = processor.process("17 07:11:25 abc123x 42 hello", " ");
        assertArrayEquals(
                new String[]{"17", "07:11:25", "abc123x", "42", "hello"},
                result.getAllTokens()
        );
    }

    @Test
    void parsesOctalNumbersCorrectly() {
        ProcessingResult result = processor.process("17 42", " ");
        int[] expected = {15, 34};
        assertArrayEquals(expected, result.getOctalNumbers());
    }

    @Test
    void findsValidDateToken() {
        ProcessingResult result = processor.process("07:11:25", " ");
        assertArrayEquals(new String[]{"07:11:25"}, result.getSortedDates());
    }

    @Test
    void rejectsInvalidDate() {
        ProcessingResult result = processor.process("31:13:99", " ");
        assertEquals(0, result.getSortedDates().length);
    }

    @Test
    void sortsMultipleDatesChronologically() {
        ProcessingResult result = processor.process("25:12:20 01:01:20 15:06:20", " ");
        assertArrayEquals(
                new String[]{"01:01:20", "15:06:20", "25:12:20"},
                result.getSortedDates()
        );
    }

    @Test
    void insertsRandomNumberAfterDate() {
        ProcessingResult result = processor.process("17 07:11:25 hello", " ");
        assertTrue(result.getStringAfterInsert().length() > "17 07:11:25 hello".length());
        assertTrue(result.getStringAfterInsert().contains("07:11:25"));
    }

    @Test
    void insertsRandomNumberInMiddleWhenNoDates() {
        ProcessingResult result = processor.process("17 42 99", " ");
        assertTrue(result.getStringAfterInsert().length() > "17 42 99".length());
    }

    @Test
    void removesShortestDigitToLatinSubstring() {
        ProcessingResult result = processor.process("abc123x", " ");
        assertEquals("3x", result.getRemovedSubstring());
    }

    @Test
    void reportsNoSubstringFoundWhenNoLatinLetters() {
        ProcessingResult result = processor.process("привет мир", " ");
        assertEquals("(no matching substring found)", result.getRemovedSubstring());
    }

    @Test
    void handlesMultipleDifferentDelimitersInARow() {
        ProcessingResult result = processor.process("17,, ,;42;;99", ",; ");
        assertArrayEquals(new String[]{"17", "42", "99"}, result.getAllTokens());
    }

    @Test
    void findsRightmostDelimiter() {
        ProcessingResult result = processor.process("a,b;c", ",;");
        assertTrue(result.getLastDelimiterInfo().contains(";"));
    }
}