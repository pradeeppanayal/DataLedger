package com.example.ledger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LedgerRecord {

    private final long timestamp;
    private final String encodedContent;
    private final int hash;

    public LedgerRecord(long timestamp, String encodedContent, int hash) {
        this.timestamp = timestamp;
        this.encodedContent = encodedContent;
        this.hash = hash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getEncodedContent() {
        return encodedContent;
    }

    public int getHash() {
        return hash;
    }

    public String getDecodedContent() {
        return new String(Base64.getDecoder().decode(encodedContent), StandardCharsets.UTF_8);
    }

    public String serialize() {
        return timestamp + " " + encodedContent + " " + hash;
    }

    public static LedgerRecord parse(String line) throws IllegalArgumentException {
        String[] parts = line.split(" ", 3);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid record: " + line);
        long timestamp = Long.parseLong(parts[0]);
        String encoded = parts[1];
        int hash = Integer.parseInt(parts[2]);
        return new LedgerRecord(timestamp, encoded, hash);
    }
}
