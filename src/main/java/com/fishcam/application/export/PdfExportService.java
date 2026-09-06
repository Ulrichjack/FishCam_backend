// --- START OF FILE PdfExportService.java ---
package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class PdfExportService {

    private final Color BRAND_GREEN = new Color(24, 82, 22); // #185216
    private final Color LIGHT_GRAY_BG = new Color(245, 245, 245);
    private final Color BORDER_COLOR = new Color(200, 200, 200);

    // --- MÉTHODE UTILITAIRE POUR LE LOGO (POSITION ABSOLUE) ---
    private void addLogo(Document document) {
        try {
            ClassPathResource imgFile = new ClassPathResource("static/logo.png");
            if (imgFile.exists()) {
                Image logo = Image.getInstance(imgFile.getURL());
                logo.scaleToFit(140, 140);

                // X : Aligné sur la marge gauche (40f)
                float x = document.leftMargin();

                // Y : Tout en haut de la page moins la hauteur de l'image
                // getTop() donne la limite haute de la zone de texte.
                // On ajoute 80f pour remonter dans la marge qu'on a créée.
                float y = document.getPageSize().getTop() - 80f;

                logo.setAbsolutePosition(x, y);
                document.add(logo);
            }
        } catch (Exception e) {
            log.warn("Impossible de charger le logo pour le PDF : {}", e.getMessage());
        }
    }

    private void addFooter(Document document) {
        try {
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Généré automatiquement par le système Fish-Cam ERP",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
        } catch (Exception e) {
            log.warn("Erreur ajout footer");
        }
    }

    private PdfPCell createCell(String text, Font font, int alignment, boolean isHeader) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(6f);
        cell.setPaddingBottom(8f);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setBorderColor(BORDER_COLOR);
        if (isHeader) cell.setBackgroundColor(LIGHT_GRAY_BG);
        return cell;
    }

    // ==========================================
    // 1. FACTURE D'ACHAT
    // ==========================================
    public byte[] exportFactureToPdf(FactureDetailResponse facture) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // ATTENTION ICI : La marge du haut est à 120 pour laisser la place au logo absolu !
        Document document = new Document(PageSize.A4, 40, 40, 90, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addLogo(document); // Ajoute le logo flottant

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND_GREEN);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph title = new Paragraph("FACTURE D'ACHAT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(new Phrase("Boutique : " + facture.getPoissonnerieNom(), headerFont));
            infoTable.addCell(new Phrase("Date : " + facture.getDateAchat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont));
            infoTable.addCell(new Phrase("Fournisseur : " + facture.getFournisseurNom(), normalFont));
            infoTable.addCell(new Phrase("Enregistré par : " + facture.getEnregistreParNom(), normalFont));
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3f, 1.5f, 2f, 2f, 2f, 2f});

            String[] headers = {"Cartons", "Produit", "Poids", "Achat", "Vente/kg", "Vente Total", "Marge"};
            for (String header : headers) {
                table.addCell(createCell(header, boldFont, Element.ALIGN_CENTER, true));
            }

            for (LigneAchatResponse ligne : facture.getLigneAchatResponses()) {
                table.addCell(createCell(String.valueOf(ligne.getQuantiteCartons()), normalFont, Element.ALIGN_CENTER, false));
                table.addCell(createCell(ligne.getProduitNom(), normalFont, Element.ALIGN_LEFT, false));
                table.addCell(createCell(ligne.getPoidsKg().stripTrailingZeros().toPlainString() + " kg", normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getMontantCarton()), normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getPrixVenteKilo()), normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getPrixVenteTotal()), normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getMargeTotal()), normalFont, Element.ALIGN_RIGHT, false));
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            Paragraph totalAchat = new Paragraph("Total Achat : " + formatMoney(facture.getTotalAchat()) + " FCFA", boldFont);
            totalAchat.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalAchat);

            Paragraph totalVente = new Paragraph("Vente Prévisible : " + formatMoney(facture.getTotalVente()) + " FCFA", boldFont);
            totalVente.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalVente);

            Paragraph marge = new Paragraph("Marge Totale : " + formatMoney(facture.getMargeTotal()) + " FCFA",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BRAND_GREEN));
            marge.setAlignment(Element.ALIGN_RIGHT);
            document.add(marge);

            addFooter(document);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF Facture", e);
        }
        return out.toByteArray();
    }

    // ==========================================
    // 2. FICHE D'ÉPARGNE
    // ==========================================
    public byte[] exportEpargneToPdf(EpargneDetailResponse epargne) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Marge haute à 120
        Document document = new Document(PageSize.A4, 40, 40, 90, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addLogo(document);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph title = new Paragraph("GIC FNJLCP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("FORCE NATIONALE DES JEUNES POUR LA LUTTE CONTRE LA PAUVRETE", boldFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            Paragraph siege = new Paragraph("Siège : FISH-CAM (" + epargne.getClient().getPoissonnerie().getName() + ")", normalFont);
            siege.setAlignment(Element.ALIGN_CENTER);
            document.add(siege);

            Paragraph tel = new Paragraph("Tél : 676.02.88.00 / 699.02.58.64", normalFont);
            tel.setAlignment(Element.ALIGN_CENTER);
            tel.setSpacingAfter(15);
            document.add(tel);

            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -5f)));
            document.add(Chunk.NEWLINE);

            Paragraph ficheTitle = new Paragraph("FICHE D'EPARGNE N° " + String.format("%03d", epargne.getId()) + " / " + java.time.LocalDate.now().getYear(), headerFont);
            ficheTitle.setAlignment(Element.ALIGN_CENTER);
            ficheTitle.setSpacingAfter(15);
            document.add(ficheTitle);

            document.add(new Paragraph("Nom : " + epargne.getClient().getLastName().toUpperCase(), boldFont));
            document.add(new Paragraph("Prénom : " + epargne.getClient().getFirstName(), normalFont));
            document.add(new Paragraph("Tél : " + epargne.getClient().getPhone(), normalFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 2f, 3f, 2f});

            String[] headers = {"Date", "Retrait\nwithdrawal", "Versement\nDépôt", "Solde\nBalance", "Solde en lettres\nBalance in letter", "Signature\nvisa"};
            for (String header : headers) {
                table.addCell(createCell(header, boldFont, Element.ALIGN_CENTER, true));
            }

            BigDecimal soldeCourant = BigDecimal.ZERO;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
            List<TransactionEpargneResponse> transactionsAsc = new ArrayList<>(epargne.getTransactions());
            Collections.reverse(transactionsAsc);

            for (var tx : transactionsAsc) {
                String retraitStr = "-";
                String versementStr = "-";

                if (tx.getType().name().equals("DEPOT")) {
                    versementStr = formatMoney(tx.getAmount());
                    soldeCourant = soldeCourant.add(tx.getAmount());
                } else if (tx.getType().name().equals("RETRAIT")) {
                    retraitStr = formatMoney(tx.getAmount());
                    soldeCourant = soldeCourant.subtract(tx.getAmount());
                }

                table.addCell(createCell(tx.getTransactionDate().format(formatter), normalFont, Element.ALIGN_CENTER, false));
                table.addCell(createCell(retraitStr, normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(versementStr, normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(soldeCourant), boldFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(" ", normalFont, Element.ALIGN_CENTER, false));
                table.addCell(createCell(" ", normalFont, Element.ALIGN_CENTER, false));
            }

            document.add(table);
            addFooter(document);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF Epargne", e);
        }
        return out.toByteArray();
    }

    // ==========================================
    // 3. RÉCAPITULATIF MENSUEL
    // ==========================================
    public byte[] exportRecapitulatifToPdf(RecapitulatifResponse recap, String poissonnerieNom, String moisAnnee) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Marge haute à 120
        Document document = new Document(PageSize.A4, 40, 40, 90, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addLogo(document);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BRAND_GREEN);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph title = new Paragraph("FISH-CAM", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("POISSONNERIE LA REFERENCE", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);

            Paragraph agence = new Paragraph("AGENCE DE : " + poissonnerieNom.toUpperCase(), boldFont);
            agence.setAlignment(Element.ALIGN_CENTER);
            agence.setSpacingAfter(10);
            document.add(agence);

            document.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1f, 100f, Color.BLACK, Element.ALIGN_CENTER, -5f)));
            document.add(Chunk.NEWLINE);

            Paragraph inventaire = new Paragraph("INVENTAIRE MOIS DE " + moisAnnee.toUpperCase(), boldFont);
            inventaire.setSpacingAfter(15);
            document.add(inventaire);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 3f, 3f, 3f});

            String[] headers = {"DATE", "MONTANT ACHAT", "VENTE PREVISIBLE", "VENTE REALISEE"};
            for (String header : headers) {
                table.addCell(createCell(header, boldFont, Element.ALIGN_CENTER, true));
            }

            for (RecapitulatifLigneResponse ligne : recap.getLignes()) {
                table.addCell(createCell(String.valueOf(ligne.getJour().getDayOfMonth()), normalFont, Element.ALIGN_CENTER, false));
                table.addCell(createCell(formatMoney(ligne.getAchat()), normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getPrevu()), normalFont, Element.ALIGN_RIGHT, false));
                table.addCell(createCell(formatMoney(ligne.getRealise()), normalFont, Element.ALIGN_RIGHT, false));
            }

            table.addCell(createCell("TOTAL (FCFA)", boldFont, Element.ALIGN_CENTER, true));
            table.addCell(createCell(formatMoney(recap.getTotalAchat()), boldFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell(formatMoney(recap.getTotalPrevu()), boldFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell(formatMoney(recap.getTotalRealise()), boldFont, Element.ALIGN_RIGHT, true));

            document.add(table);

            // Ce récapitulatif reste un relevé des saisies quotidiennes. Il ne connaît
            // ni les charges mensuelles, ni les variations de stock et de créances.
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font beneficeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND_GREEN);

            PdfPTable synthese = new PdfPTable(2);
            synthese.setWidthPercentage(60);
            synthese.setHorizontalAlignment(Element.ALIGN_RIGHT);
            synthese.setWidths(new float[]{3f, 2f});
            synthese.setSpacingBefore(15);

            synthese.addCell(createCell("TOTAL DEPENSES (FCFA)", boldFont, Element.ALIGN_LEFT, true));
            synthese.addCell(createCell(formatMoney(recap.getTotalDepenses()), boldFont, Element.ALIGN_RIGHT, true));

            PdfPCell labelBenefice = createCell("SOLDE DES SAISIES (FCFA)", labelFont, Element.ALIGN_LEFT, true);
            PdfPCell valeurBenefice = createCell(formatMoney(recap.getTotalBenefice()), beneficeFont, Element.ALIGN_RIGHT, true);
            synthese.addCell(labelBenefice);
            synthese.addCell(valeurBenefice);

            document.add(synthese);

            Paragraph avertissement = new Paragraph(
                    "Ce solde n'est pas le bénéfice mensuel définitif : les charges mensuelles, "
                            + "le stock et les créances clients sont présentés dans le rapport de résultat mensuel.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.DARK_GRAY));
            avertissement.setSpacingBefore(10);
            document.add(avertissement);

            addFooter(document);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF Recapitulatif", e);
        }
        return out.toByteArray();
    }

    // ==========================================
    // 4. RÉSULTAT MENSUEL GLOBAL (3 BOUTIQUES)
    // ==========================================
    public byte[] exportResultatMensuelGlobalToPdf(ResultatMensuelGlobalResponse resultat) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 30, 30, 75, 35);

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addLogo(document);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 17, BRAND_GREEN);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

            Paragraph title = new Paragraph("RÉSULTAT MENSUEL DE GESTION", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph periode = new Paragraph(
                    libelleMois(resultat.getMois(), resultat.getAnnee()) + " - " + resultat.getStatut(),
                    subtitleFont);
            periode.setAlignment(Element.ALIGN_CENTER);
            periode.setSpacingAfter(14);
            document.add(periode);

            PdfPTable table = new PdfPTable(11);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{
                    2.2f, 1.7f, 1.9f, 1.6f, 1.7f, 1.9f,
                    1.5f, 1.9f, 1.6f, 1.9f, 2f
            });
            table.setHeaderRows(1);

            String[] headers = {
                    "BOUTIQUE", "ACHATS", "ENCAISSEMENTS", "DÉP. JOUR",
                    "CHARGES BOUT.", "RÉSULTAT PROV.", "VAR. STOCK",
                    "RÉSULTAT STOCK", "VAR. CRÉANCES", "RÉSULTAT FINAL", "STATUT"
            };
            for (String header : headers) {
                table.addCell(createCell(header, boldFont, Element.ALIGN_CENTER, true));
            }

            for (ResultatMensuelBoutiqueResponse boutique : resultat.getBoutiques()) {
                table.addCell(createCell(boutique.getPoissonnerieNom(), boldFont, Element.ALIGN_LEFT, false));
                table.addCell(moneyCell(boutique.getTotalAchats(), normalFont, false));
                table.addCell(moneyCell(boutique.getTotalEncaissements(), normalFont, false));
                table.addCell(moneyCell(boutique.getDepensesJournalieres(), normalFont, false));
                table.addCell(moneyCell(boutique.getChargesMensuelles(), normalFont, false));
                table.addCell(moneyCell(boutique.getResultatProvisoire(), normalFont, false));
                table.addCell(moneyCell(boutique.getVariationStock(), normalFont, false));
                table.addCell(moneyCell(boutique.getResultatCorrigeStock(), normalFont, false));
                table.addCell(moneyCell(boutique.getVariationCreances(), normalFont, false));
                table.addCell(moneyCell(boutique.getResultatValide(), boldFont, false));
                table.addCell(createCell(boutique.getStatut(), boldFont, Element.ALIGN_CENTER, false));
            }

            table.addCell(createCell("TOTAL ENTREPRISE", boldFont, Element.ALIGN_LEFT, true));
            table.addCell(moneyCell(resultat.getTotalAchats(), boldFont, true));
            table.addCell(moneyCell(resultat.getTotalEncaissements(), boldFont, true));
            table.addCell(moneyCell(resultat.getTotalDepensesJournalieres(), boldFont, true));
            table.addCell(moneyCell(resultat.getTotalChargesBoutiques(), boldFont, true));
            table.addCell(moneyCell(resultat.getResultatProvisoire(), boldFont, true));
            table.addCell(moneyCell(resultat.getVariationStock(), boldFont, true));
            table.addCell(moneyCell(resultat.getResultatCorrigeStock(), boldFont, true));
            table.addCell(moneyCell(resultat.getVariationCreances(), boldFont, true));
            table.addCell(moneyCell(resultat.getResultatValide(), boldFont, true));
            table.addCell(createCell(resultat.getStatut(), boldFont, Element.ALIGN_CENTER, true));

            document.add(table);

            PdfPTable creances = new PdfPTable(4);
            creances.setWidthPercentage(68);
            creances.setHorizontalAlignment(Element.ALIGN_LEFT);
            creances.setSpacingBefore(10);
            creances.setWidths(new float[]{2.2f, 1.8f, 1.8f, 1.8f});
            for (String header : new String[]{"CRÉANCES CLIENTS", "FISHCAM", "CAHIER", "ÉCART"}) {
                creances.addCell(createCell(header, boldFont, Element.ALIGN_CENTER, true));
            }
            for (ResultatMensuelBoutiqueResponse boutique : resultat.getBoutiques()) {
                creances.addCell(createCell(
                        boutique.getPoissonnerieNom(), boldFont, Element.ALIGN_LEFT, false));
                creances.addCell(moneyCell(
                        boutique.getCreancesClientsCalculees(), normalFont, false));
                creances.addCell(moneyCell(boutique.getCreancesFinales(), normalFont, false));
                creances.addCell(moneyCell(
                        boutique.getEcartCreancesClients(), boldFont, false));
            }
            document.add(creances);

            Paragraph chargesGenerales = new Paragraph(
                    "Charges générales déjà retirées du résultat provisoire global : "
                            + formatMoney(resultat.getChargesGenerales()) + " FCFA.",
                    boldFont);
            chargesGenerales.setSpacingBefore(10);
            document.add(chargesGenerales);

            Font explanationFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
            Paragraph formule = new Paragraph(
                    "Lecture : résultat provisoire = encaissements - achats - dépenses - charges. "
                            + "Résultat corrigé = résultat provisoire + variation du stock + variation des créances. "
                            + "Les charges générales ne sont pas réparties dans les lignes des boutiques ; "
                            + "elles sont retirées une seule fois dans le total entreprise.",
                    explanationFont);
            formule.setSpacingBefore(7);
            document.add(formule);

            Paragraph statuts = new Paragraph(
                    "PROVISOIRE : le stock d'une borne manque. CORRIGÉ_STOCK : le stock est corrigé, "
                            + "mais les créances manquent. ESTIMÉ : calcul complet avec au moins une estimation. "
                            + "VALIDÉ : deux relevés physiques et les créances sont renseignées.",
                    explanationFont);
            statuts.setSpacingBefore(4);
            document.add(statuts);

            if (resultat.getInformationsManquantes() != null
                    && !resultat.getInformationsManquantes().isEmpty()) {
                Paragraph manqueTitle = new Paragraph("Informations manquantes :", boldFont);
                manqueTitle.setSpacingBefore(8);
                document.add(manqueTitle);
                com.lowagie.text.List liste = new com.lowagie.text.List(false, 12f);
                for (String information : resultat.getInformationsManquantes()) {
                    liste.add(new ListItem(information, explanationFont));
                }
                document.add(liste);
            }

            if (resultat.getAlertes() != null && !resultat.getAlertes().isEmpty()) {
                Paragraph alertesTitle = new Paragraph("Alertes à vérifier avant validation :", boldFont);
                alertesTitle.setSpacingBefore(8);
                document.add(alertesTitle);
                com.lowagie.text.List liste = new com.lowagie.text.List(false, 12f);
                for (String alerte : resultat.getAlertes()) {
                    liste.add(new ListItem(alerte, explanationFont));
                }
                document.add(liste);
            }

            addFooter(document);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF du résultat mensuel", e);
        }
        return out.toByteArray();
    }

    private PdfPCell moneyCell(BigDecimal amount, Font font, boolean isHeader) {
        String valeur = amount == null ? "-" : formatMoney(amount);
        return createCell(valeur, font, Element.ALIGN_RIGHT, isHeader);
    }

    private String libelleMois(Integer mois, Integer annee) {
        String nomMois = Month.of(mois).getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return nomMois.toUpperCase(Locale.FRENCH) + " " + annee;
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,d", amount.setScale(0, RoundingMode.HALF_UP).longValue()).replace(',', ' ');
    }
}
// --- END OF FILE ---
