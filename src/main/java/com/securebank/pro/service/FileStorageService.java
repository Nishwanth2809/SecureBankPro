package com.securebank.pro.service;

import java.util.List;
import com.securebank.pro.entity.Account;
import com.securebank.pro.entity.Transaction;

public interface FileStorageService {
    void saveTransactionLog(Transaction transaction);
    List<String> readTransactionHistory();
    void exportStatement(Account account, String destFilePath);
    void backupDatabase(String backupFilePath);
    void restoreDatabase(String backupFilePath);
}
