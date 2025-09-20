package com.example.ledger;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class DataLedgerTest {

    @Test
    void testWriteAndTail() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        ledger.write("hello");
        ledger.write("world");

        List<String> last = ledger.tail(1);
        assertEquals(1, last.size());
        assertTrue(last.get(0).contains("world"));
    }

    @Test
    void testVerifyIntegrity() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        ledger.write("entry1");
        ledger.write("entry2");
        assertTrue(ledger.verifyIntegrity());
    }

    @Test
    void testTailEmptyLedger() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        assertTrue(ledger.tail(5).isEmpty());
    }

    @Test
    void testSingleEntryLedger() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        ledger.write("onlyOne");
        List<String> last = ledger.tail(5);
        assertEquals(1, last.size());
        assertTrue(last.get(0).contains("onlyOne"));
    }

    @Test
    void testTailLargerThanEntries() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        ledger.write("a");
        ledger.write("b");
        List<String> last = ledger.tail(10);
        assertEquals(2, last.size());
    }

    @Test
    void testNullAndEmptyWrite() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        assertFalse(ledger.write(null));
        assertFalse(ledger.write(""));
        assertTrue(ledger.tail(1).isEmpty());
    }

    @Test
    void testLoadExistingLedger() throws Exception {
        DataLedger ledger1 = DataLedger.createNew();
        ledger1.write("first");
        String id = ledger1.getFileHandler().getFile().getName().replace(".log", "");

        DataLedger ledger2 = DataLedger.load(id);
        ledger2.write("second");

        List<String> last = ledger2.tail(2);
        assertEquals(2, last.size());
        assertTrue(last.get(1).contains("second"));
    }

    @Test
    void testCorruptedFileIntegrityFails() throws Exception {
        DataLedger ledger = DataLedger.createNew();
        ledger.write("safe");

        File file = ledger.getFileHandler().getFile();
        LedgerRecord  ledgerRecord = new LedgerRecord(System.currentTimeMillis(), "random", 12345);
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(ledgerRecord.serialize());
        }

        assertFalse(ledger.verifyIntegrity());
    }

    @Test
    void testInvalidIdentifierThrows() {
        assertThrows(IllegalArgumentException.class, () -> DataLedger.load("nonexistent123"));
    }
}
