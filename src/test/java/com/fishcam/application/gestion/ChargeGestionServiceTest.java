package com.fishcam.application.gestion;

import com.fishcam.adapter.web.dto.request.CreateChargeGestionRequest;
import com.fishcam.domain.gestion.CategorieCharge;
import com.fishcam.domain.gestion.ChargeGestion;
import com.fishcam.domain.gestion.ChargeGestionRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.BusinessException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeGestionServiceTest {

    @Mock
    private ChargeGestionRepository chargeRepository;
    @Mock
    private PoissonnerieRepository poissonnerieRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChargeGestionService service;

    private Poissonnerie lele;

    @BeforeEach
    void setUp() {
        lele = new Poissonnerie();
        lele.setId(3L);
        lele.setName("Lele");

        User patron = new User();
        patron.setId(7L);
        lenient().when(poissonnerieRepository.findById(3L)).thenReturn(Optional.of(lele));
        lenient().when(userRepository.findById(7L)).thenReturn(Optional.of(patron));
        lenient().when(chargeRepository.save(any(ChargeGestion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void refuseUneChargeRecurrenteSaisieDeuxFois() {
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(chargeExistante(true, LocalDate.of(2026, 1, 1), null)));

        assertThatThrownBy(() -> service.creer(demande(true, LocalDate.of(2026, 9, 1), null), 7L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existe déjà");

        verify(chargeRepository, never()).save(any());
    }

    @Test
    void refuseUnDoublonMemeAvecUneCasseDifferente() {
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(chargeExistante(true, LocalDate.of(2026, 1, 1), null)));

        CreateChargeGestionRequest demande = demande(true, LocalDate.of(2026, 9, 1), null);
        demande.setLibelle("  loyer lele  ");

        assertThatThrownBy(() -> service.creer(demande, 7L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void accepteLaChargeSuivanteQuandLaPrecedenteEstTerminee() {
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(chargeExistante(
                        true, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 8, 31))));

        assertThat(service.creer(demande(true, LocalDate.of(2026, 9, 1), null), 7L)).isNotNull();
        verify(chargeRepository).save(any(ChargeGestion.class));
    }

    @Test
    void accepteDeuxChargesPonctuellesSurDesMoisDifferents() {
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(chargeExistante(false, LocalDate.of(2026, 8, 12), null)));

        assertThat(service.creer(demande(false, LocalDate.of(2026, 9, 4), null), 7L)).isNotNull();
        verify(chargeRepository).save(any(ChargeGestion.class));
    }

    @Test
    void refuseDeuxChargesPonctuellesDansLeMemeMois() {
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(chargeExistante(false, LocalDate.of(2026, 9, 2), null)));

        assertThatThrownBy(() -> service.creer(demande(false, LocalDate.of(2026, 9, 25), null), 7L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void modifieUneChargeSansLaConfondreAvecElleMeme() {
        ChargeGestion charge = chargeExistante(true, LocalDate.of(2026, 8, 1), null);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(charge));
        CreateChargeGestionRequest correction = demande(true, LocalDate.of(2026, 8, 1), null);
        correction.setMontant(new BigDecimal("17500"));

        var response = service.modifier(1L, correction, 7L);

        assertThat(response.getMontant()).isEqualByComparingTo("17500");
        verify(chargeRepository).save(charge);
    }

    @Test
    void changementFuturCreeUneNouvelleVersionSansReecrireLePasse() {
        ChargeGestion charge = chargeExistante(true, LocalDate.of(2026, 8, 1), null);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));
        when(chargeRepository.findByCategorieAndPerimetre(CategorieCharge.LOYER, 3L))
                .thenReturn(List.of(charge));
        CreateChargeGestionRequest octobre = demande(true, LocalDate.of(2026, 10, 1), null);
        octobre.setMontant(new BigDecimal("20000"));

        var response = service.modifier(1L, octobre, 7L);

        assertThat(charge.getDateFin()).isEqualTo(LocalDate.of(2026, 9, 30));
        assertThat(response.getDateDebut()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(response.getMontant()).isEqualByComparingTo("20000");
    }

    @Test
    void supprimeLogiquementUneChargeEtConserveLaTrace() {
        ChargeGestion charge = chargeExistante(true, LocalDate.of(2026, 8, 1), null);
        when(chargeRepository.findById(1L)).thenReturn(Optional.of(charge));

        var response = service.supprimer(1L, 7L);

        assertThat(response.getSupprimee()).isTrue();
        assertThat(charge.getActive()).isFalse();
        assertThat(charge.getSupprimeePar().getId()).isEqualTo(7L);
        assertThat(charge.getSupprimeeLe()).isNotNull();
    }

    private CreateChargeGestionRequest demande(Boolean recurrente, LocalDate debut, LocalDate fin) {
        CreateChargeGestionRequest demande = new CreateChargeGestionRequest();
        demande.setPoissonnerieId(3L);
        demande.setCategorie(CategorieCharge.LOYER);
        demande.setLibelle("Loyer Lele");
        demande.setMontant(new BigDecimal("15000"));
        demande.setRecurrente(recurrente);
        demande.setDateDebut(debut);
        demande.setDateFin(fin);
        return demande;
    }

    private ChargeGestion chargeExistante(Boolean recurrente, LocalDate debut, LocalDate fin) {
        ChargeGestion charge = new ChargeGestion();
        charge.setId(1L);
        charge.setPoissonnerie(lele);
        charge.setCategorie(CategorieCharge.LOYER);
        charge.setLibelle("Loyer Lele");
        charge.setMontant(new BigDecimal("15000"));
        charge.setRecurrente(recurrente);
        charge.setDateDebut(debut);
        charge.setDateFin(fin);
        return charge;
    }
}
