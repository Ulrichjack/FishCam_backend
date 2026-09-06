package com.fishcam.application.comptecourant;

import com.fishcam.adapter.web.dto.request.DetteInitialeRequest;
import com.fishcam.adapter.web.dto.request.CorrectionDetteInitialeRequest;
import com.fishcam.adapter.web.mapper.CompteCourantMapper;
import com.fishcam.adapter.web.mapper.TransactionCCMapper;
import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.client.Client;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.comptecourant.CompteCourant;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.StatutCompteCourant;
import com.fishcam.domain.comptecourant.TransactionCompteCourant;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCustomRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.epargne.TransactionEpargneRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompteCourantServiceTest {

    @Mock private CompteCourantRepository compteCourantRepository;
    @Mock private TransactionCompteCourantRepository transactionCompteCourantRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private PoissonnerieRepository poissonnerieRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private CompteCourantMapper compteCourantMapper;
    @Mock private TransactionCCMapper transactionCCMapper;
    @Mock private TransactionCustomRepository transactionCustomRepository;
    @Mock private TransactionEpargneRepository transactionEpargneRepository;

    @InjectMocks private CompteCourantService service;

    @Test
    void detteInitialeAjusteLeSoldeAvecUnTypeDistinctDeLEmpruntDuJour() {
        Poissonnerie ville = new Poissonnerie();
        ville.setId(1L);
        ville.setName("VILLE");

        Client client = new Client();
        client.setActive(true);
        client.setPoissonnerie(ville);

        CompteCourant compte = new CompteCourant();
        compte.setId(10L);
        compte.setClient(client);
        compte.setSolde(new BigDecimal("-5000"));
        compte.setLimiteCreditMax(new BigDecimal("50000"));
        compte.setStatut(StatutCompteCourant.ACTIF);

        User user = new User();
        user.setId(7L);

        DetteInitialeRequest request = new DetteInitialeRequest(
                10L, new BigDecimal("75000"), LocalDate.of(2026, 8, 12), "Cahier page 14");

        when(compteCourantRepository.findByIdWithLock(10L)).thenReturn(Optional.of(compte));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(transactionCompteCourantRepository.save(any(TransactionCompteCourant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.enregistrerDetteInitiale(request, 7L);

        ArgumentCaptor<TransactionCompteCourant> captor = ArgumentCaptor.forClass(TransactionCompteCourant.class);
        verify(transactionCompteCourantRepository).save(captor.capture());
        TransactionCompteCourant transaction = captor.getValue();

        assertThat(transaction.getType()).isEqualTo(TypeTransactionCC.DETTE_INITIALE);
        assertThat(transaction.getDateDetteOrigine()).isEqualTo(LocalDate.of(2026, 8, 12));
        assertThat(transaction.getSoldePrecedent()).isEqualByComparingTo("-5000");
        assertThat(transaction.getSoldeApres()).isEqualByComparingTo("-80000");
        assertThat(compte.getSolde()).isEqualByComparingTo("-80000");
    }

    @Test
    void correctionAnnuleLAncienneDetteEtCreeLaNouvelleSansEffacerLaTrace() {
        Poissonnerie ville = new Poissonnerie();
        ville.setId(1L);

        Client client = new Client();
        client.setActive(true);
        client.setPoissonnerie(ville);

        CompteCourant compte = new CompteCourant();
        compte.setId(10L);
        compte.setClient(client);
        compte.setSolde(new BigDecimal("-90000"));
        compte.setStatut(StatutCompteCourant.ACTIF);

        TransactionCompteCourant origine = new TransactionCompteCourant();
        origine.setId(21L);
        origine.setCompteCourant(compte);
        origine.setType(TypeTransactionCC.DETTE_INITIALE);
        origine.setMontant(new BigDecimal("75000"));
        origine.setDateDetteOrigine(LocalDate.of(2026, 8, 12));
        origine.setTransactionDate(LocalDateTime.of(2026, 9, 6, 12, 0));

        User user = new User();
        user.setId(7L);

        CorrectionDetteInitialeRequest request = new CorrectionDetteInitialeRequest();
        request.setNouveauMontant(new BigDecimal("60000"));
        request.setNouvelleDateDetteOrigine(LocalDate.of(2026, 8, 13));
        request.setMotif("Montant mal recopié");

        when(transactionCompteCourantRepository.findByIdWithLock(21L)).thenReturn(Optional.of(origine));
        when(transactionCompteCourantRepository.existsByTransactionOrigineAndType(
                origine, TypeTransactionCC.ANNULATION_DETTE_INITIALE)).thenReturn(false);
        when(compteCourantRepository.findByIdWithLock(10L)).thenReturn(Optional.of(compte));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(transactionCompteCourantRepository.save(any(TransactionCompteCourant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.corrigerDetteInitiale(21L, request, 7L);

        ArgumentCaptor<TransactionCompteCourant> captor = ArgumentCaptor.forClass(TransactionCompteCourant.class);
        verify(transactionCompteCourantRepository, times(2)).save(captor.capture());
        List<TransactionCompteCourant> mouvements = captor.getAllValues();

        assertThat(mouvements.get(0).getType()).isEqualTo(TypeTransactionCC.ANNULATION_DETTE_INITIALE);
        assertThat(mouvements.get(0).getTransactionOrigine()).isSameAs(origine);
        assertThat(mouvements.get(1).getType()).isEqualTo(TypeTransactionCC.DETTE_INITIALE);
        assertThat(mouvements.get(1).getMontant()).isEqualByComparingTo("60000");
        assertThat(mouvements.get(1).getDateDetteOrigine()).isEqualTo(LocalDate.of(2026, 8, 13));
        assertThat(compte.getSolde()).isEqualByComparingTo("-75000");
    }
}
