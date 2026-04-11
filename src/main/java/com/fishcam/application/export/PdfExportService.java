package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportFactureToPdf(FactureDetailResponse facture) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Créer le document avec des marges
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLUE);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // 1. Titre
            Paragraph title = new Paragraph("FACTURE D'ACHAT - FISH-CAM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 2. Informations de la facture
            document.add(new Paragraph("Boutique : " + facture.getPoissonnerieNom(), headerFont));
            document.add(new Paragraph("Date : " + facture.getDateAchat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            document.add(new Paragraph("Fournisseur : " + facture.getFournisseurNom(), normalFont));
            document.add(new Paragraph("Enregistré par : " + facture.getEnregistreParNom(), normalFont));
            document.add(new Paragraph("Statut : " + (facture.getCloture() ? "Clôturée" : "En cours"),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, facture.getCloture() ? Color.GREEN : Color.RED)));
            document.add(Chunk.NEWLINE);

            // 3. Création du tableau (7 colonnes)
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3f, 1.5f, 2f, 2f, 2f, 2f}); // Largeurs des colonnes

            // En-têtes du tableau
            String[] headers = {"Cartons", "Produit", "Poids", "Achat", "Vente/kg", "Vente Total", "Marge"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Lignes du tableau
            for (LigneAchatResponse ligne : facture.getLigneAchatResponses()) {
                table.addCell(createCell(String.valueOf(ligne.getQuantiteCartons()), normalFont));
                table.addCell(createCell(ligne.getProduitNom(), normalFont));
                table.addCell(createCell(ligne.getPoidsKg() + " kg", normalFont));
                table.addCell(createCell(ligne.getMontantCarton() + " F", normalFont));
                table.addCell(createCell(ligne.getPrixVenteKilo() + " F", normalFont));
                table.addCell(createCell(ligne.getPrixVenteTotal() + " F", normalFont));
                table.addCell(createCell(ligne.getMargeTotal() + " F", normalFont));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // 4. Résumé financier
            Paragraph totalAchat = new Paragraph("Total Achat : " + facture.getTotalAchat() + " FCFA", boldFont);
            totalAchat.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalAchat);

            Paragraph totalVente = new Paragraph("Vente Prévisible : " + facture.getTotalVente() + " FCFA", boldFont);
            totalVente.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalVente);

            Paragraph marge = new Paragraph("Marge Totale : " + facture.getMargeTotal() + " FCFA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLUE));
            marge.setAlignment(Element.ALIGN_RIGHT);
            document.add(marge);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }

        return out.toByteArray();
    }

    //fiche epargne
    public byte[] exportEpargneToPdf(EpargneDetailResponse epargne) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Polices
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            // 1. En-tête (GIC FNJLCP)
            Paragraph title = new Paragraph("GIC FNJLCP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("FORCE NATIONALE DES JEUNES POUR LA LUTTE CONTRE LA PAUVRETE", smallFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(5);
            document.add(subtitle);

            Paragraph siege = new Paragraph("Siège : FISH-CAM (Poissonnerie la Référence)", smallFont);
            siege.setAlignment(Element.ALIGN_CENTER);
            siege.setSpacingAfter(5);
            document.add(siege);

            Paragraph tel = new Paragraph("Tél : 676.02.88.00 / 699.02.58.64", smallFont);
            tel.setAlignment(Element.ALIGN_CENTER);
            tel.setSpacingAfter(20);
            document.add(tel);

            // Ligne de séparation
            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -5f)));
            document.add(Chunk.NEWLINE);

            // 2. Informations du Client
            Paragraph ficheTitle = new Paragraph("FICHE D'EPARGNE N°" + epargne.getId(), headerFont);
            ficheTitle.setAlignment(Element.ALIGN_CENTER);
            ficheTitle.setSpacingAfter(15);
            document.add(ficheTitle);

            document.add(new Paragraph("Nom : " + epargne.getClient().getLastName(), normalFont));
            document.add(new Paragraph("Prénom : " + epargne.getClient().getFirstName(), normalFont));
            document.add(new Paragraph("Tél : " + epargne.getClient().getPhone(), normalFont));
            document.add(Chunk.NEWLINE);

            // 3. Création du tableau (6 colonnes)
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 2f, 3f, 2f});

            // En-têtes du tableau
            String[] headers = {"Date", "Retrait\nwithdrawal", "Versement\nDépôt", "Solde\nBalance", "Solde en lettres\nBalance in letter", "Signature\nvisa"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // 4. Lignes du tableau (Transactions)
            BigDecimal soldeCourant = BigDecimal.ZERO;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");

            // Reverse the list so we calculate the running balance from oldest to newest
            List<TransactionEpargneResponse> transactionsAsc =
                    new ArrayList<>(epargne.getTransactions());
            Collections.reverse(transactionsAsc);

            for (var tx : transactionsAsc) {
                String retraitStr = "-";
                String versementStr = "-";

                if (tx.getType().name().equals("DEPOT")) {
                    versementStr = String.valueOf(tx.getAmount().intValue());
                    soldeCourant = soldeCourant.add(tx.getAmount());
                } else if (tx.getType().name().equals("RETRAIT")) {
                    retraitStr = String.valueOf(tx.getAmount().intValue());
                    soldeCourant = soldeCourant.subtract(tx.getAmount());
                }

                table.addCell(createCell(tx.getTransactionDate().format(formatter), normalFont));
                table.addCell(createCell(retraitStr, normalFont));
                table.addCell(createCell(versementStr, normalFont));
                table.addCell(createCell(String.valueOf(soldeCourant.intValue()), normalFont));
                table.addCell(createCell(" ", normalFont)); // Ligne vide pour écriture manuelle
                table.addCell(createCell(" ", normalFont)); // Ligne vide pour signature
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF Epargne", e);
        }

        return out.toByteArray();
    }

    public byte[] exportRecapitulatifToPdf(RecapitulatifResponse recap, String poissonnerieNom, String moisAnnee) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // 1. Header (FISH-CAM)
            Paragraph title = new Paragraph("FISH-CAM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("POISSONNERIE LA REFERENCE", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            Paragraph tel = new Paragraph("TEL: 676 02 88 00 / 699 02 58 64", normalFont);
            tel.setAlignment(Element.ALIGN_CENTER);
            document.add(tel);

            Paragraph agence = new Paragraph("AGENCE DE : " + poissonnerieNom, boldFont);
            agence.setAlignment(Element.ALIGN_CENTER);
            agence.setSpacingAfter(10);
            document.add(agence);

            // Line separator
            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -5f)));
            document.add(Chunk.NEWLINE);

            Paragraph inventaire = new Paragraph("INVENTAIRE MOIS DE " + moisAnnee, boldFont);
            inventaire.setSpacingAfter(15);
            document.add(inventaire);

            // 2. Table setup (4 columns)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3f, 3f, 3f});

            // Headers
            String[] headers = {"DATE", "MONTANT ACHAT", "VENTE PREVISIBLE", "VENTE REALISEE"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // We loop through RecapitulatifLigneResponse, not LigneAchatResponse!
            for (RecapitulatifLigneResponse ligne : recap.getLignes()) {

                // Column 1: Date (Just the day of the month, like "1", "2", "3")
                String jourStr = String.valueOf(ligne.getJour().getDayOfMonth());
                table.addCell(createCell(jourStr, normalFont));

                // Column 2: Montant Achat
                String achatStr = String.valueOf(ligne.getAchat().intValue());
                table.addCell(createCell(achatStr, normalFont));

                // Column 3: Vente Previsible
                String prevuStr = String.valueOf(ligne.getPrevu().intValue());
                table.addCell(createCell(prevuStr, normalFont));

                // Column 4: Vente Realisee
                String realiseStr = String.valueOf(ligne.getRealise().intValue());
                table.addCell(createCell(realiseStr, normalFont));
            }

            // ==========================================
            // 4. Add the final "TOTAL(FCFA)" row
            // ==========================================

            // We use boldFont for the totals row to make it stand out
            table.addCell(createCell("TOTAL(FCFA)", boldFont));

            String totalAchatStr = String.valueOf(recap.getTotalAchat().intValue());
            table.addCell(createCell(totalAchatStr, boldFont));

            String totalPrevuStr = String.valueOf(recap.getTotalPrevu().intValue());
            table.addCell(createCell(totalPrevuStr, boldFont));

            String totalRealiseStr = String.valueOf(recap.getTotalRealise().intValue());
            table.addCell(createCell(totalRealiseStr, boldFont));

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF Recapitulatif", e);
        }

        return out.toByteArray();
    }

    // Fonction utilitaire pour centrer le texte dans les cellules
    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

}