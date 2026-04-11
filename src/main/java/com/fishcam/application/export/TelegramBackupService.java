package com.fishcam.application.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBackupService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public void sendBackup(String jsonDatabase) {
        if (botToken == null || botToken.contains("YOUR_BOT")) {
            log.warn("Telegram bot token is not configured. Backup skipped.");
            return;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendDocument";
        String filename = "fishcam_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        String nomMois = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH)).toUpperCase();

        // 1. Convert the JSON String to a ByteArrayResource so Telegram sees it as a file
        ByteArrayResource fileAsResource = new ByteArrayResource(jsonDatabase.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        // 2. Prepare the multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("document", fileAsResource);
        body.add("caption", "📦 Fish-Cam DB Backup (JSON)\n" +
                "📅 Période : " + nomMois + "\n" +
                "⏱️ Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "\n" +
                "ℹ️ Format complet brut (Toutes les tables).");
        // 3. Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(url, requestEntity, String.class);
            log.info(" Database backup sent successfully to Telegram!");
        } catch (Exception e) {
            log.error("❌ Error sending backup to Telegram", e);
        }
    }

    public void sendSqlBackup(File sqlFile) {
        if (botToken == null || botToken.contains("YOUR_BOT")) {
            log.warn("Telegram bot token is not configured. Backup skipped.");
            return;
        }

        String url = "https://api.telegram.org/bot" + botToken + "/sendDocument";
        String filename = "fishcam_sql_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        String nomMois = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH)).toUpperCase();

        // 1. Convert the JSON String to a ByteArrayResource so Telegram sees it as a file
        FileSystemResource fileAsResource = new FileSystemResource(sqlFile);

        // 2. Prepare the multipart form data
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("document", fileAsResource);
        body.add("caption", "🗄️ Fish-Cam DB Backup (SQL Postgres)\n" +
                "📅 Période : " + nomMois + "\n" +
                "⏱️ Généré le : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + "\n" +
                "ℹ️ Format SQL prêt à être restauré sur le serveur.");
        // 3. Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForEntity(url, requestEntity, String.class);
            log.info(" Database backup sent successfully to Telegram!");
        } catch (Exception e) {
            log.error("❌ Error sending backup to Telegram", e);
        }
    }
}