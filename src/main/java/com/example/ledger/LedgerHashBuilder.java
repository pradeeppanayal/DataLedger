package com.example.ledger;

public class LedgerHashBuilder {

    private LedgerHashBuilder() {
    }

    public static int computeHash(long timestamp, String encodedContent, int previousHash) {
        return (timestamp + " " + encodedContent + " " + previousHash).hashCode();
    }
}
