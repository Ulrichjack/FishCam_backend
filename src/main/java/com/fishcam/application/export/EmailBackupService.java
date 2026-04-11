package com.fishcam.application.export;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailBackupService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.target}")
    private String targetEmail;

    public void sendMonthlyBackup(String jsonDatabase, File sqlFile){

            MimeMessage message =  mailSender.createMimeMessage();
            try{

                String moisAnnee = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH));
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom(fromEmail);
                helper.setTo(targetEmail);
                helper.setSubject("🐟 Fish-Cam: Sauvegarde Mensuelle - " + moisAnnee.toUpperCase());
                helper.setText("Bonjour Patron,\n\nVoici la sauvegarde de sécurité de la base de données pour le mois de " + moisAnnee + ".\n\nCordialement,\nL'équipe Fish-Cam.");
                ByteArrayResource resource = new ByteArrayResource(jsonDatabase.getBytes(StandardCharsets.UTF_8));

                helper.addAttachment(sqlFile.getName(), sqlFile);
                helper.addAttachment("backup_mensuel.json", resource);

                mailSender.send(message);
                log.info("Database backup sent successfully Gmail");
            }catch (Exception e){
                log.error("Failed to send email", e);
            }

    }



}
