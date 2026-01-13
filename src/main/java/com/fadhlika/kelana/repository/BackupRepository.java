package com.fadhlika.kelana.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class BackupRepository {
    @Value("${kelana.backup_dir}")
    private String backupDir;

    @Autowired
    private DataSource ds;

    public void createBackup() throws SQLException {
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    String.format("backup to %s/kelana.%d.db", backupDir, System.currentTimeMillis() / 1000L));
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
