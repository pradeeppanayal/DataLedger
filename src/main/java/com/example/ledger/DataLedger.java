package com.example.ledger;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataLedger {

    private final LedgerFileHandler fileHandler;
    private int lastHash = 0;

    private DataLedger(LedgerFileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    public LedgerFileHandler getFileHandler(){
        return  this.fileHandler;
    }
 
    public static DataLedger createNew() {
        String uid = UUID.randomUUID().toString();
        return new DataLedger(new LedgerFileHandler(uid));
    }

    public static DataLedger load(String identifier) throws IOException {
        LedgerFileHandler handler = new LedgerFileHandler(identifier);

        if (!handler.isLedgerExist()) throw new IllegalArgumentException("Ledger not found: " + identifier);

        DataLedger ledger = new DataLedger(handler);

        LedgerRecord last = handler.readLastRecord();
        if (last != null) {
            ledger.lastHash = last.getHash();
        }

        return ledger;
    }

    public synchronized boolean write(String content) throws IOException {
        if (content == null || content.isEmpty()) return false;

        String encoded = Base64.getEncoder().encodeToString(content.getBytes());
        long timestamp = System.currentTimeMillis();
        int hash = LedgerHashBuilder.computeHash(timestamp, encoded, lastHash);

        LedgerRecord record = new LedgerRecord(timestamp, encoded, hash);
        fileHandler.appendRecord(record);
        lastHash = hash;

        return true;
    }

    public List<String> tail(int n) throws IOException {
        List<LedgerRecord> records = fileHandler.tail(n);
        return records.stream()
                .map(r -> r.getTimestamp() + " " + r.getDecodedContent())
                .collect(Collectors.toList());
    }

    public  boolean  verifyIntegrity() throws IOException{
        return fileHandler.verifyIntegrity();
    }
}
