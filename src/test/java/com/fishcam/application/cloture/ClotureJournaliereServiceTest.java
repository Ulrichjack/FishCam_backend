package com.fishcam.application.cloture;

import com.fishcam.adapter.web.dto.request.ClotureJournaliereRequest;
import com.fishcam.adapter.web.dto.request.UpdateClotureJournaliereRequest;
import com.fishcam.adapter.web.dto.response.ClotureJournaliereResponse;
import com.fishcam.adapter.web.mapper.ClotureMapper;
import com.fishcam.domain.achat.AchatJournalierRepository;
import com.fishcam.domain.achat.LigneAchatRepository;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClotureJournaliereServiceTest {

    @Mock
    private AchatJournalierRepository achatJournalierRepository;
    @Mock
    private ClotureJournaliereRepository clotureJournaliereRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LigneAchatRepository ligneAchatRepository;
    @Mock
    private PoissonnerieRepository poissonnerieRepository;
    @Mock
    private CompteCourantRepository compteCourantRepository;
    @Mock
    private TransactionCompteCourantRepository transactionCompteCourantRepository;
    @Mock
    private ClotureMapper clotureMapper;

    @InjectMocks
    private ClotureJournaliereService service;

    private final LocalDate date = LocalDate.of(2026, 9, 6);
    private Poissonnerie ville;
    private User utilisateur;

    @BeforeEach
    void setUp() {
        ville = new Poissonnerie();
        ville.setId(1L);
        ville.setName("VILLE");
        ville.setFondDeCaisseDefaut(new BigDecimal("10000"));

        utilisateur = new User();
        utilisateur.setId(7L);

        when(poissonnerieRepository.findById(1L)).thenReturn(Optional.of(ville));
        when(achatJournalierRepository.findByPoissonnerieIdAndDateAchat(1L, date))
                .thenReturn(List.of());
        when(transactionCompteCourantRepository.sumMontantByPoissonnerieAndTypeAndPeriod(
                any(), any(), any(), any())).thenReturn(BigDecimal.ZERO);
        when(transactionCompteCourantRepository.countByPoissonnerieAndTypeAndPeriod(
                any(), any(), any(), any())).thenReturn(0L);
        when(clotureJournaliereRepository.save(any(ClotureJournaliere.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(clotureMapper.toResponse(any(ClotureJournaliere.class)))
                .thenReturn(new ClotureJournaliereResponse());
    }

    @Test
    void neRajoutePasLesDepensesALaRecetteLorsDeLaCloture() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(utilisateur));
        when(clotureMapper.toEntity(any(ClotureJournaliereRequest.class)))
                .thenReturn(new ClotureJournaliere());

        ClotureJournaliereRequest request = new ClotureJournaliereRequest();
        request.setDate(date);
        request.setPoissonnerieId(1L);
        request.setArgentCaisse(new BigDecimal("460000"));
        request.setFondDeCaisse(new BigDecimal("10000"));
        request.setTransport(new BigDecimal("1500"));
        request.setRation(new BigDecimal("1500"));
        request.setAutresFrais(BigDecimal.ZERO);

        service.cloturer(request, 7L);

        ArgumentCaptor<ClotureJournaliere> captor =
                ArgumentCaptor.forClass(ClotureJournaliere.class);
        org.mockito.Mockito.verify(clotureJournaliereRepository).save(captor.capture());

        ClotureJournaliere cloture = captor.getValue();
        assertThat(cloture.getVenteRealisee()).isEqualByComparingTo("450000");
        assertThat(cloture.getTotalDepenses()).isEqualByComparingTo("3000");
        assertThat(cloture.getBeneficeNet()).isEqualByComparingTo("447000");
    }

    @Test
    void appliqueLaMemeRegleLorsDUneCorrection() {
        ClotureJournaliere existante = new ClotureJournaliere();
        existante.setId(12L);
        existante.setDate(date);
        existante.setPoissonnerie(ville);
        when(clotureJournaliereRepository.findById(12L)).thenReturn(Optional.of(existante));
        when(userRepository.findById(7L)).thenReturn(Optional.of(utilisateur));

        UpdateClotureJournaliereRequest request = new UpdateClotureJournaliereRequest();
        request.setArgentCaisse(new BigDecimal("460000"));
        request.setFondDeCaisse(new BigDecimal("10000"));
        request.setTransport(new BigDecimal("1500"));
        request.setRation(new BigDecimal("1500"));
        request.setAutresFrais(BigDecimal.ZERO);
        request.setMotifCorrection("Recalcul des dépenses déjà incluses dans la recette");

        service.corriger(12L, request, 7L);

        assertThat(existante.getVenteRealisee()).isEqualByComparingTo("450000");
        assertThat(existante.getTotalDepenses()).isEqualByComparingTo("3000");
        assertThat(existante.getBeneficeNet()).isEqualByComparingTo("447000");
    }
}
