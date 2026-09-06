package com.fishcam.application.bilan;

import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BilanMensuelServiceTest {

    @Mock
    private ClotureJournaliereRepository clotureRepository;
    @Mock
    private PoissonnerieRepository poissonnerieRepository;

    @InjectMocks
    private BilanMensuelService service;

    private Poissonnerie ville;

    @BeforeEach
    void setUp() {
        ville = new Poissonnerie();
        ville.setId(1L);
        ville.setName("Ville");
        when(poissonnerieRepository.findById(1L)).thenReturn(Optional.of(ville));
    }

    @Test
    void additionneLesDettesDeTousLesJoursDuMois() {
        when(clotureRepository.findByPoissonnerieAndMoisAndAnnee(ville, 8, 2026))
                .thenReturn(List.of(
                        cloture(LocalDate.of(2026, 8, 1), "50000", "12000"),
                        cloture(LocalDate.of(2026, 8, 2), "30000", "8000"),
                        cloture(LocalDate.of(2026, 8, 3), "40000", "5000")));

        BilanMensuelResponse bilan = service.getBilanMensuel(1L, 8, 2026);

        // Avant correction, seul le dernier jour (5000) etait remonte.
        assertThat(bilan.getMontantDettesMois()).isEqualByComparingTo("25000");
    }

    @Test
    void designeLeMoinsMauvaisJourQuandLeMoisEstEntierementDeficitaire() {
        when(clotureRepository.findByPoissonnerieAndMoisAndAnnee(ville, 8, 2026))
                .thenReturn(List.of(
                        cloture(LocalDate.of(2026, 8, 1), "-50000", "0"),
                        cloture(LocalDate.of(2026, 8, 2), "-10000", "0"),
                        cloture(LocalDate.of(2026, 8, 3), "-30000", "0")));

        BilanMensuelResponse bilan = service.getBilanMensuel(1L, 8, 2026);

        // Avant correction : date nulle alors que la colonne est NOT NULL en base.
        assertThat(bilan.getMeilleurJourBenefice()).isEqualTo(LocalDate.of(2026, 8, 2));
        assertThat(bilan.getBeneficeMeilleurJour()).isEqualByComparingTo("-10000");
    }

    private ClotureJournaliere cloture(LocalDate date, String benefice, String dettes) {
        ClotureJournaliere cloture = new ClotureJournaliere();
        cloture.setDate(date);
        cloture.setTotalAchat(BigDecimal.ZERO);
        cloture.setVenteRealisee(BigDecimal.ZERO);
        cloture.setTotalDepenses(BigDecimal.ZERO);
        cloture.setTotalVentePrevisible(BigDecimal.ZERO);
        cloture.setBeneficeNet(new BigDecimal(benefice));
        cloture.setMontantDettesJour(new BigDecimal(dettes));
        return cloture;
    }
}
