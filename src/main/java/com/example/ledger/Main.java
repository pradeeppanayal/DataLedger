package com.example.ledger;

public class Main {

    public static void main(String[] args) throws Exception {
        DataLedger ledger = DataLedger.createNew();

        for (int i = 1; i < 100; i++) {
            ledger.write("Line " + i);
        }

        System.out.println("Last 2 entries:");
        for (String entry : ledger.tail(2)) {
            System.out.println(entry);
        }
    }
}
