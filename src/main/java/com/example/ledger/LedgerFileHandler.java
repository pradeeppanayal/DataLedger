package com.example.ledger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class LedgerFileHandler {

    private final File file;
    private final String identifier;
    private static final byte[] NEWLINE = "\n".getBytes(StandardCharsets.UTF_8);


    public LedgerFileHandler(String identifier) {
        this.identifier = identifier;
        this.file = resolveFile();
    }

    public boolean isLedgerExist() {
        return this.file.exists();
    }

    public  File getFile(){
        return file;
    }
    public void appendRecord(LedgerRecord record) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            fos.write(record.serialize().getBytes(StandardCharsets.UTF_8));
            fos.write(NEWLINE);
        }
    }

    public LedgerRecord readLastRecord() throws IOException {
        if (!file.exists()) {
            return null;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long pointer = raf.length() - 1;
            StringBuilder sb = new StringBuilder();
            while (pointer >= 0) {
                raf.seek(pointer);
                char c = (char) raf.readByte();
                if (c == '\n') {
                    String line = sb.reverse().toString();
                    sb.setLength(0);
                    try {
                        return LedgerRecord.parse(line);
                    } catch (IllegalArgumentException ignored) {
                    }
                } else {
                    sb.append(c);
                }
                pointer--;
            }
            // first line if no newline
            if (sb.length() > 0) {
                String line = sb.reverse().toString();
                try {
                    return LedgerRecord.parse(line);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return null;
    }

    public List<LedgerRecord> tail(int n) throws IOException {
        LinkedList<LedgerRecord> buffer = new LinkedList<>();
        if (!file.exists()) {
            return buffer;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    LedgerRecord record = LedgerRecord.parse(line);
                    buffer.add(record);
                    if (buffer.size() > n) {
                        buffer.removeFirst();
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return buffer;
    }

    private File resolveFile() {
        File dir = new File("ledger_data");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("Failed to create storage directory");
        }
        return new File(dir, this.identifier + ".log");
    }

    public boolean verifyIntegrity() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            int prevHash = 0;
            while ((line = reader.readLine()) != null) {
                LedgerRecord record = LedgerRecord.parse(line);
                int computed = LedgerHashBuilder.computeHash(
                        record.getTimestamp(),
                        record.getEncodedContent(),
                        prevHash
                );
                if (computed != record.getHash()) {
                    return false;
                }
                prevHash = record.getHash();
            }
        }
        return true;
    }
}
