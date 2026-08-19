package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.RecapitulatifResponse;
import com.fishcam.application.rapport.RecapitulatifService;
import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Archive mensuelle : un seul .zip contenant le dump SQL, le CSV data-science et
 * le recapitulatif PDF de chaque poissonnerie active pour le mois demande.
 *
 * Independant de la sauvegarde quotidienne (BackupScheduler / CloudBackupService),
 * qui continue de tourner tous les jours a 19h.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyArchiveService {

    private final PostgresBackupService postgresBackupService;
    private final DataScienceExportService dataScienceExportService;
    private final CloudflareR2StorageService cloudflareR2StorageService;
    private final RecapitulatifService recapitulatifService;
    private final PdfExportService pdfExportService;
    private final PoissonnerieRepository poissonnerieRepository;
    private final BackupRecordRepository backupRecordRepository;

    /**
     * Genere l'archive du mois, l'envoie sur Cloudflare R2 et trace l'execution en base.
     *
     * @param mois mois a archiver (ex: 2026-07)
     * @return le nom du fichier zip envoye sur R2
     */
    public String archiveMonth(YearMonth mois) throws Exception {
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();

        File sqlFile = postgresBackupService.generateSqlBackup();
        File csvFile = dataScienceExportService.generateSalesCsv();

        File zipFile = new File("backups/archive_mensuelle_" + mois + ".zip");

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addFile(zip, "base_de_donnees/" + sqlFile.getName(), sqlFile);
            addFile(zip, "data/" + csvFile.getName(), csvFile);

            List<Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();
            for (Poissonnerie poissonnerie : poissonneries) {
                RecapitulatifResponse recap = recapitulatifService
                        .generateRecapitulatif(poissonnerie.getId(), debut, fin);

                // Pas de cloture sur la periode : on n'embarque pas un PDF vide
                if (recap.getLignes().isEmpty()) {
                    log.info("Archive mensuelle : aucune cloture pour {} en {}, PDF ignore",
                            poissonnerie.getName(), mois);
                    continue;
                }

                byte[] pdf = pdfExportService.exportRecapitulatifToPdf(
                        recap, poissonnerie.getName(), libelleMois(mois));

                String nomFichier = "recapitulatifs/Recapitulatif_"
                        + poissonnerie.getName().replaceAll("[^a-zA-Z0-9]", "_")
                        + "_" + mois + ".pdf";
                addBytes(zip, nomFichier, pdf);
            }
        }

        cloudflareR2StorageService.uploadBackup(zipFile);

        BackupRecord record = new BackupRecord();
        record.setDateExecution(LocalDateTime.now());
        record.setType(TypeBackup.CLOUD_MONTHLY);
        record.setSuccess(true);
        backupRecordRepository.save(record);

        log.info("📦 Archive mensuelle envoyee sur R2 : {}", zipFile.getName());

        // Le zip contient deja tout : inutile de le garder sur le disque ephemere de Render
        if (!zipFile.delete()) {
            log.warn("Impossible de supprimer l'archive locale {}", zipFile.getName());
        }

        return zipFile.getName();
    }

    private String libelleMois(YearMonth mois) {
        String nom = mois.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return nom.toUpperCase() + " " + mois.getYear();
    }

    private void addFile(ZipOutputStream zip, String entryName, File file) throws Exception {
        zip.putNextEntry(new ZipEntry(entryName));
        try (InputStream in = Files.newInputStream(file.toPath())) {
            in.transferTo(zip);
        }
        zip.closeEntry();
    }

    private void addBytes(ZipOutputStream zip, String entryName, byte[] content) throws Exception {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content);
        zip.closeEntry();
    }
}
