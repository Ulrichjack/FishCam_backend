package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.ResultatMensuelBoutiqueResponse;
import com.fishcam.adapter.web.dto.response.ResultatMensuelGlobalResponse;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfExportServiceTest {

    private final PdfExportService pdfService = new PdfExportService();

    @Test
    void genereUnRapportGlobalLisibleMemeQuandLeMoisEstProvisoire() throws Exception {
        ResultatMensuelBoutiqueResponse bare = boutique("Bare", "VALIDE", new BigDecimal("521114"));
        ResultatMensuelBoutiqueResponse ville = boutique("Ville", "PROVISOIRE", null);
        ville.setVariationStock(null);
        ville.setVariationCreances(null);
        ville.setInformationsManquantes(List.of("relevé d'ouverture au 2026-08-31"));

        ResultatMensuelGlobalResponse resultat = new ResultatMensuelGlobalResponse();
        resultat.setMois(9);
        resultat.setAnnee(2026);
        resultat.setBoutiques(List.of(bare, ville));
        resultat.setTotalAchats(new BigDecimal("10000000"));
        resultat.setTotalEncaissements(new BigDecimal("11500000"));
        resultat.setTotalDepensesJournalieres(BigDecimal.ZERO);
        resultat.setTotalChargesBoutiques(new BigDecimal("621500"));
        resultat.setChargesGenerales(new BigDecimal("410000"));
        resultat.setResultatProvisoire(new BigDecimal("468500"));
        resultat.setVariationStock(null);
        resultat.setVariationCreances(null);
        resultat.setResultatValide(null);
        resultat.setStatut("PROVISOIRE");
        resultat.setInformationsManquantes(List.of(
                "Ville : relevé d'ouverture au 2026-08-31"));

        byte[] pdf = pdfService.exportResultatMensuelGlobalToPdf(resultat);

        assertThat(pdf).startsWith("%PDF".getBytes());
        PdfReader reader = new PdfReader(pdf);
        assertThat(reader.getNumberOfPages()).isGreaterThanOrEqualTo(1);
        String texte = new PdfTextExtractor(reader).getTextFromPage(1)
                .replaceAll("\\s+", " ");
        assertThat(texte).contains("TOTAL ENTREPRISE", "Bare", "Ville", "PROVISOIRE");
        reader.close();
    }

    private ResultatMensuelBoutiqueResponse boutique(
            String nom,
            String statut,
            BigDecimal resultatValide) {
        ResultatMensuelBoutiqueResponse boutique = new ResultatMensuelBoutiqueResponse();
        boutique.setPoissonnerieNom(nom);
        boutique.setTotalAchats(new BigDecimal("5000000"));
        boutique.setTotalEncaissements(new BigDecimal("5750000"));
        boutique.setDepensesJournalieres(BigDecimal.ZERO);
        boutique.setChargesMensuelles(new BigDecimal("300000"));
        boutique.setResultatProvisoire(new BigDecimal("450000"));
        boutique.setVariationStock(new BigDecimal("50000"));
        boutique.setVariationCreances(new BigDecimal("21114"));
        boutique.setResultatValide(resultatValide);
        boutique.setStatut(statut);
        boutique.setInformationsManquantes(List.of());
        return boutique;
    }
}
