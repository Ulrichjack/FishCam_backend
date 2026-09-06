package com.fishcam.application.comptecourant;

import com.fishcam.domain.comptecourant.TransactionCompteCourant;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreanceClientService {

    private static final EnumSet<TypeTransactionCC> TYPES_DATE_ORIGINE = EnumSet.of(
            TypeTransactionCC.DETTE_INITIALE,
            TypeTransactionCC.ANNULATION_DETTE_INITIALE);

    private final TransactionCompteCourantRepository transactionRepository;

    /**
     * Reconstitue la dette encore due par compte à une date, puis additionne uniquement
     * les soldes débiteurs. Les dettes du cahier utilisent leur date d'origine lorsqu'elle existe.
     */
    public BigDecimal totalCreancesAu(Poissonnerie poissonnerie, LocalDate dateSituation) {
        Map<Long, BigDecimal> soldes = new HashMap<>();
        for (TransactionCompteCourant transaction : transactionRepository.findMouvementsEffectifsAu(
                poissonnerie,
                dateSituation,
                dateSituation.plusDays(1).atStartOfDay(),
                TYPES_DATE_ORIGINE)) {
            BigDecimal variation = switch (transaction.getType()) {
                case EMPRUNT, DETTE_INITIALE -> transaction.getMontant().negate();
                case REMBOURSEMENT, ANNULATION_DETTE_INITIALE -> transaction.getMontant();
            };
            soldes.merge(transaction.getCompteCourant().getId(), variation, BigDecimal::add);
        }

        return soldes.values().stream()
                .filter(solde -> solde.signum() < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
