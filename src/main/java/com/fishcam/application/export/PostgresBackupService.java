package com.fishcam.application.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;

@Slf4j
@Service
public class PostgresBackupService {

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public File generateSqlBackup() throws Exception {
        String dbName = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);

        // Créer le dossier s'il n'existe pas
        File backupDir = new File("backups");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Nom du fichier : fishcam_backup_2026-06-15.sql
        String filename = "backups/fishcam_backup_" + LocalDate.now() + ".sql";

        ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-U", dbUsername,
                "-f", filename,
                dbName
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("💾 Sauvegarde locale réussie : {}", filename);
            cleanOldLocalBackups(backupDir);
            return new File(filename);
        } else {
            throw new RuntimeException("Échec de pg_dump. Code: " + exitCode);
        }
    }

    private void cleanOldLocalBackups(File backupDir) {
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".sql") || name.endsWith(".csv"));
        if (files != null) {
            long sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);
            for (File file : files) {
                if (file.lastModified() < sevenDaysAgo) {
                    if (file.delete()) {
                        log.info("🗑️ Ancien backup local supprimé : {}", file.getName());
                    }
                }
            }
        }
    }
}