package com.fishcam.application.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
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
        // dbUrl est au format jdbc:postgresql://host:port/dbname?params -> URI ne comprend
        // pas le prefixe "jdbc:", et separe proprement le path (dbname) de la query string.
        URI uri = URI.create(dbUrl.replaceFirst("^jdbc:", ""));
        String host = uri.getHost();
        int port = uri.getPort() != -1 ? uri.getPort() : 5432;
        String dbName = uri.getPath().substring(1); // retire le "/" initial

        // Créer le dossier s'il n'existe pas
        File backupDir = new File("backups");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }

        // Nom du fichier : fishcam_backup_2026-06-15.sql
        String filename = "backups/fishcam_backup_" + LocalDate.now() + ".sql";

        ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-p", String.valueOf(port),
                "-U", dbUsername,
                "-f", filename,
                dbName
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.environment().put("PGSSLMODE", "require");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("💾 Sauvegarde locale réussie : {}", filename);
            cleanOldLocalBackups(backupDir);
            return new File(filename);
        } else {
            log.error("Sortie pg_dump : {}", output);
            throw new RuntimeException("Échec de pg_dump. Code: " + exitCode + " - " + output);
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