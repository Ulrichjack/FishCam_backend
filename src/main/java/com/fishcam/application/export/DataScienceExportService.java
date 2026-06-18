package com.fishcam.application.export;

import com.fishcam.domain.achat.LigneAchat;
import com.fishcam.domain.achat.LigneAchatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataScienceExportService {

    private final LigneAchatRepository ligneAchatRepository;

    public File generateSalesCsv() {
        String filename = "backups/ventes_data_" + LocalDate.now() + ".csv";
        File file = new File(filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // En-têtes du CSV
            writer.println("Date,Poissonnerie,Produit,QuantiteCartons,PoidsKg,PrixAchatTotal,PrixVenteTotal,Marge");

            // Récupérer toutes les ventes
            List<LigneAchat> lignes = ligneAchatRepository.findAll();

            for (LigneAchat ligne : lignes) {
                String date = ligne.getAchatJournalier().getDateAchat().toString();
                String poissonnerie = ligne.getAchatJournalier().getPoissonnerie().getName().replace(",", " ");
                String produit = ligne.getProduit().getNom().replace(",", " ");
                int qte = ligne.getQuantiteCartons();
                double poids = ligne.getPoidsKg().doubleValue();
                double achat = ligne.getMontantCarton().doubleValue();
                double vente = ligne.getPrixVenteKilo().multiply(ligne.getPoidsKg()).doubleValue();
                double marge = vente - achat;

                writer.printf("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f%n",
                        date, poissonnerie, produit, qte, poids, achat, vente, marge);
            }
            log.info("📊 Fichier CSV Data Science généré : {}", filename);
            return file;
        } catch (Exception e) {
            log.error("Erreur lors de la génération du CSV", e);
            throw new RuntimeException("Erreur CSV", e);
        }
    }
}