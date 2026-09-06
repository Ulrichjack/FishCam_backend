package com.fishcam.application.comptecourant;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.comptecourant.CompteCourant;
import com.fishcam.domain.comptecourant.TransactionCompteCourant;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreanceClientServiceTest {

    @Mock private TransactionCompteCourantRepository transactionRepository;
    @InjectMocks private CreanceClientService service;

    @Test
    void additionneLesSoldesDebiteursEtIgnoreUnTropPercuClient() {
        Poissonnerie ville = new Poissonnerie();
        ville.setId(1L);
        CompteCourant premier = compte(10L, ville);
        CompteCourant second = compte(11L, ville);

        List<TransactionCompteCourant> mouvements = List.of(
                mouvement(premier, TypeTransactionCC.DETTE_INITIALE, "100000"),
                mouvement(premier, TypeTransactionCC.ANNULATION_DETTE_INITIALE, "100000"),
                mouvement(premier, TypeTransactionCC.DETTE_INITIALE, "60000"),
                mouvement(premier, TypeTransactionCC.REMBOURSEMENT, "10000"),
                mouvement(second, TypeTransactionCC.EMPRUNT, "20000"),
                mouvement(second, TypeTransactionCC.REMBOURSEMENT, "30000"));

        when(transactionRepository.findMouvementsEffectifsAu(
                eq(ville), eq(LocalDate.of(2026, 9, 30)), any(), any()))
                .thenReturn(mouvements);

        assertThat(service.totalCreancesAu(ville, LocalDate.of(2026, 9, 30)))
                .isEqualByComparingTo("50000");
    }

    private CompteCourant compte(Long id, Poissonnerie poissonnerie) {
        Client client = new Client();
        client.setPoissonnerie(poissonnerie);
        CompteCourant compte = new CompteCourant();
        compte.setId(id);
        compte.setClient(client);
        return compte;
    }

    private TransactionCompteCourant mouvement(
            CompteCourant compte, TypeTransactionCC type, String montant) {
        TransactionCompteCourant transaction = new TransactionCompteCourant();
        transaction.setCompteCourant(compte);
        transaction.setType(type);
        transaction.setMontant(new BigDecimal(montant));
        return transaction;
    }
}
