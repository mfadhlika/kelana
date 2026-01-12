package com.fadhlika.lokasi.service;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadhlika.lokasi.exception.InternalErrorException;
import com.fadhlika.lokasi.repository.BackupRepository;

@Service
public class BackupService {
    @Autowired
    private BackupRepository backupRepository;

    public void createBackup() {
        try {
            backupRepository.createBackup();
        } catch (SQLException ex) {
            throw new InternalErrorException(ex.getMessage());
        }
    }
}
