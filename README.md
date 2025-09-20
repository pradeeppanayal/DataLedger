# DataLedger

A lightweight **append-only ledger system** written in Java.  
Each record is:
- Timestamped
- Base64-encoded
- Chained using a rolling hash for tamper detection

This allows the ledger to detect corruption and ensure data integrity without relying on a heavy database.

---

## Features
- ✅ **Append-only writes** — entries are immutable once written  
- ✅ **Hash chain integrity** — prevents tampering of historical entries  
- ✅ **Efficient load** — only last line is parsed when reloading a ledger  
- ✅ **Tail support** — fetch the last *N* entries without loading entire file  
- ✅ **Corruption detection** — verify the ledger file’s integrity  
- ✅ **Simple API** — `createNew()`, `write()`, `load()`, `verifyIntegrity()`, `tail()`

---

## Usage

### Create and write to a new ledger
```java
DataLedger ledger = DataLedger.createNew();
ledger.write("hello");
ledger.write("world");
