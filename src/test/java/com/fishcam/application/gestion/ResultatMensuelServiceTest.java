package com.fishcam.application.gestion;

import com.fishcam.adapter.web.dto.response.ResultatMensuelBoutiqueResponse;
import com.fishcam.adapter.web.dto.response.ResultatMensuelGlobalResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.gestion.CategorieCharge;
import com.fishcam.domain.gestion.ModeEvaluation;
import com.fishcam.domain.gestion.ReleveSituationMensuelle;
import com.fishcam.domain.gestion.ReleveSituationMensuelleRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultatMensuelServiceTest {

    private static final LocalDate OUVERTURE = LocalDate.of(2026, 9, 30);
    private static final LocalDate CLOTURE = LocalDate.of(2026, 10, 31);

    @Mock
    private PoissonnerieRepository poissonnerieRepository;
    @Mock
    private ClotureJournaliereRepository clotureRepository;
    @Mock
    private ReleveSituationMensuelleRepository releveRepository;
    @Mock
    private ChargeGestionService chargeService;

    @InjectMocks
    private ResultatMensuelService service;

    private Poissonnerie ville;

    @BeforeEach
    void setUp() {
        ville = poissonnerie(1L, "Ville");
        lenient().when(poissonnerieRepository.findById(1L)).thenReturn(Optional.of(ville));
        lenient().when(chargeService.totauxParCategorie(any(), any())).thenReturn(Map.of());
        lenient().when(chargeService.totalApplicable(any(), eq(null))).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void corrigeLeResultatDuStockMemeSansLesCreances() {
        donneUnMoisDe(ville, "1000000", "700000");
        donneUnReleve(ville, OUVERTURE, "200000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(ville, CLOTURE, "280000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);

        ResultatMensuelBoutiqueResponse resultat = service.calculerBoutique(1L, 10, 2026);

        assertThat(resultat.getVariationStock()).isEqualByComparingTo("80000");
        assertThat(resultat.getResultatCorrigeStock()).isEqualByComparingTo("380000");
        assertThat(resultat.getResultatValide()).isNull();
        assertThat(resultat.getStatut()).isEqualTo(ResultatMensuelService.STATUT_CORRIGE_STOCK);
    }

    @Test
    void marqueLeMoisValideQuandStockEtCreancesSontComptes() {
        donneUnMoisDe(ville, "1000000", "700000");
        donneUnReleve(ville, OUVERTURE, "200000", "100000", ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(ville, CLOTURE, "280000", "150000", ModeEvaluation.COMPTAGE_PHYSIQUE);

        ResultatMensuelBoutiqueResponse resultat = service.calculerBoutique(1L, 10, 2026);

        assertThat(resultat.getResultatCorrigeStock()).isEqualByComparingTo("380000");
        assertThat(resultat.getResultatValide()).isEqualByComparingTo("430000");
        assertThat(resultat.getStatut()).isEqualTo(ResultatMensuelService.STATUT_VALIDE);
    }

    @Test
    void resteProvisoireSansReleveDOuverture() {
        donneUnMoisDe(ville, "1000000", "700000");
        when(releveRepository.findByPoissonnerieAndDateReleve(ville, OUVERTURE))
                .thenReturn(Optional.empty());
        donneUnReleve(ville, CLOTURE, "280000", "150000", ModeEvaluation.COMPTAGE_PHYSIQUE);

        ResultatMensuelBoutiqueResponse resultat = service.calculerBoutique(1L, 10, 2026);

        assertThat(resultat.getResultatCorrigeStock()).isNull();
        assertThat(resultat.getStatut()).isEqualTo(ResultatMensuelService.STATUT_PROVISOIRE);
        assertThat(resultat.getInformationsManquantes())
                .anyMatch(information -> information.contains("relevé d'ouverture"));
    }

    @Test
    void alerteQuandLeTransportEstALaFoisMensuelEtJournalier() {
        ClotureJournaliere jour = cloture("1000000", "700000");
        jour.setTransport(new BigDecimal("5000"));
        when(clotureRepository.findByPoissonnerieAndDateBetweenOrderByDateAsc(eq(ville), any(), any()))
                .thenReturn(List.of(jour));
        when(chargeService.totauxParCategorie(any(), eq(1L)))
                .thenReturn(Map.of(CategorieCharge.TRANSPORT, new BigDecimal("31000")));
        donneUnReleve(ville, OUVERTURE, "200000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(ville, CLOTURE, "280000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);

        ResultatMensuelBoutiqueResponse resultat = service.calculerBoutique(1L, 10, 2026);

        assertThat(resultat.getAlertes()).hasSize(1);
        assertThat(resultat.getAlertes().get(0)).contains("transport", "31000", "5000");
    }

    @Test
    void nAlertePasQuandUneSeuleSourceEstRenseignee() {
        donneUnMoisDe(ville, "1000000", "700000");
        when(chargeService.totauxParCategorie(any(), eq(1L)))
                .thenReturn(Map.of(CategorieCharge.TRANSPORT, new BigDecimal("31000")));
        donneUnReleve(ville, OUVERTURE, "200000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(ville, CLOTURE, "280000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);

        assertThat(service.calculerBoutique(1L, 10, 2026).getAlertes()).isEmpty();
    }

    @Test
    void consolideLeStockGlobalMemeSiUneBoutiqueNaPasSesCreances() {
        Poissonnerie bare = poissonnerie(2L, "Bare");
        when(poissonnerieRepository.findByActiveTrue()).thenReturn(List.of(ville, bare));
        donneUnMoisDe(ville, "1000000", "700000");
        donneUnMoisDe(bare, "500000", "400000");
        // Ville a ses créances, Bare ne les a pas encore reconstituées.
        donneUnReleve(ville, OUVERTURE, "200000", "100000", ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(ville, CLOTURE, "280000", "150000", ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(bare, OUVERTURE, "50000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);
        donneUnReleve(bare, CLOTURE, "70000", null, ModeEvaluation.COMPTAGE_PHYSIQUE);

        ResultatMensuelGlobalResponse global = service.calculerGlobal(10, 2026);

        assertThat(global.getVariationStock()).isEqualByComparingTo("100000");
        assertThat(global.getResultatCorrigeStock()).isEqualByComparingTo("500000");
        assertThat(global.getResultatValide()).isNull();
        assertThat(global.getStatut()).isEqualTo(ResultatMensuelService.STATUT_CORRIGE_STOCK);
        assertThat(global.getInformationsManquantes())
                .anyMatch(information -> information.startsWith("Bare : créances"));
    }

    private Poissonnerie poissonnerie(Long id, String nom) {
        Poissonnerie poissonnerie = new Poissonnerie();
        poissonnerie.setId(id);
        poissonnerie.setName(nom);
        return poissonnerie;
    }

    private ClotureJournaliere cloture(String encaissements, String achats) {
        ClotureJournaliere cloture = new ClotureJournaliere();
        cloture.setDate(CLOTURE);
        cloture.setTotalAchat(new BigDecimal(achats));
        cloture.setVenteRealisee(new BigDecimal(encaissements));
        cloture.setTotalDepenses(BigDecimal.ZERO);
        return cloture;
    }

    private void donneUnMoisDe(Poissonnerie poissonnerie, String encaissements, String achats) {
        lenient().when(clotureRepository
                        .findByPoissonnerieAndDateBetweenOrderByDateAsc(eq(poissonnerie), any(), any()))
                .thenReturn(List.of(cloture(encaissements, achats)));
    }

    private void donneUnReleve(
            Poissonnerie poissonnerie, LocalDate date, String stock, String creances, ModeEvaluation mode) {
        ReleveSituationMensuelle releve = new ReleveSituationMensuelle();
        releve.setPoissonnerie(poissonnerie);
        releve.setDateReleve(date);
        releve.setValeurStock(new BigDecimal(stock));
        releve.setTotalCreancesClients(creances == null ? null : new BigDecimal(creances));
        releve.setModeEvaluation(mode);
        lenient().when(releveRepository.findByPoissonnerieAndDateReleve(poissonnerie, date))
                .thenReturn(Optional.of(releve));
    }
}
