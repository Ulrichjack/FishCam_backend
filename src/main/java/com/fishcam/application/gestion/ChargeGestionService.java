package com.fishcam.application.gestion;

import com.fishcam.adapter.web.dto.request.CreateChargeGestionRequest;
import com.fishcam.adapter.web.dto.response.ChargeGestionResponse;
import com.fishcam.domain.gestion.CategorieCharge;
import com.fishcam.domain.gestion.ChargeGestion;
import com.fishcam.domain.gestion.ChargeGestionRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeGestionService {

    private final ChargeGestionRepository chargeRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;

    @Transactional
    @LogAudit(action = "CREATE", entityName = "ChargeGestion")
    public ChargeGestionResponse creer(CreateChargeGestionRequest request, Long userId) {
        if (request.getDateFin() != null && request.getDateFin().isBefore(request.getDateDebut())) {
            throw new BusinessException("La date de fin d'une charge ne peut pas précéder sa date de début.");
        }

        Poissonnerie poissonnerie = null;
        if (request.getPoissonnerieId() != null) {
            poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        String libelle = request.getLibelle().trim();
        refuserDoublon(request, libelle);

        ChargeGestion charge = new ChargeGestion();
        charge.setPoissonnerie(poissonnerie);
        charge.setCategorie(request.getCategorie());
        charge.setLibelle(libelle);
        charge.setMontant(request.getMontant());
        charge.setRecurrente(request.getRecurrente());
        charge.setDateDebut(request.getDateDebut());
        charge.setDateFin(request.getDateFin());
        charge.setActive(true);
        charge.setCreatedBy(user);
        return toResponse(chargeRepository.save(charge));
    }

    public List<ChargeGestionResponse> listerApplicables(Integer mois, Integer annee, Long poissonnerieId) {
        YearMonth periode = validerPeriode(mois, annee);
        return trouverApplicables(periode, poissonnerieId).stream().map(this::toResponse).toList();
    }

    public BigDecimal totalApplicable(YearMonth periode, Long poissonnerieId) {
        return trouverApplicables(periode, poissonnerieId).stream()
                .map(ChargeGestion::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Montants du mois ventilés par catégorie, pour détecter un double comptage. */
    public Map<CategorieCharge, BigDecimal> totauxParCategorie(YearMonth periode, Long poissonnerieId) {
        Map<CategorieCharge, BigDecimal> totaux = new EnumMap<>(CategorieCharge.class);
        for (ChargeGestion charge : trouverApplicables(periode, poissonnerieId)) {
            totaux.merge(charge.getCategorie(), charge.getMontant(), BigDecimal::add);
        }
        return totaux;
    }

    @Transactional
    @LogAudit(action = "UPDATE", entityName = "ChargeGestion")
    public ChargeGestionResponse terminer(Long id, LocalDate dateFin) {
        ChargeGestion charge = chargeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Charge non trouvée"));
        if (dateFin.isBefore(charge.getDateDebut())) {
            throw new BusinessException("La date de fin ne peut pas précéder la date de début.");
        }
        charge.setDateFin(dateFin);
        charge.setActive(false);
        return toResponse(chargeRepository.save(charge));
    }

    /**
     * Une charge saisie deux fois est invisible dans le résultat mais double son montant
     * chaque mois. On refuse donc un même libellé, dans la même catégorie et sur le même
     * périmètre, dès que les périodes d'application se recouvrent.
     */
    private void refuserDoublon(CreateChargeGestionRequest request, String libelle) {
        LocalDate[] nouvelle = periodeEffective(
                request.getRecurrente(), request.getDateDebut(), request.getDateFin());

        chargeRepository.findByCategorieAndPerimetre(request.getCategorie(), request.getPoissonnerieId())
                .stream()
                .filter(existante -> existante.getLibelle().equalsIgnoreCase(libelle))
                .filter(existante -> seRecouvrent(nouvelle, periodeEffective(
                        existante.getRecurrente(), existante.getDateDebut(), existante.getDateFin())))
                .findFirst()
                .ifPresent(existante -> {
                    throw new BusinessException(
                            "La charge « " + libelle + " » (" + request.getCategorie()
                                    + ") existe déjà sur cette période depuis le "
                                    + existante.getDateDebut()
                                    + ". Terminez-la avant d'en créer une nouvelle, sinon le montant"
                                    + " serait compté deux fois.");
                });
    }

    /** Une charge ponctuelle ne vaut que pour son mois ; une récurrente court jusqu'à sa fin. */
    private LocalDate[] periodeEffective(Boolean recurrente, LocalDate dateDebut, LocalDate dateFin) {
        if (Boolean.TRUE.equals(recurrente)) {
            return new LocalDate[]{dateDebut, dateFin != null ? dateFin : LocalDate.MAX};
        }
        YearMonth mois = YearMonth.from(dateDebut);
        return new LocalDate[]{mois.atDay(1), mois.atEndOfMonth()};
    }

    private boolean seRecouvrent(LocalDate[] a, LocalDate[] b) {
        return !a[1].isBefore(b[0]) && !b[1].isBefore(a[0]);
    }

    private List<ChargeGestion> trouverApplicables(YearMonth periode, Long poissonnerieId) {
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();

        // Une charge terminée reste volontairement prise en compte dans ses anciens mois.
        return chargeRepository.findAllByOrderByDateDebutAscIdAsc().stream()
                .filter(charge -> correspondAuPerimetre(charge, poissonnerieId))
                .filter(charge -> charge.getRecurrente()
                        ? !charge.getDateDebut().isAfter(fin)
                            && (charge.getDateFin() == null || !charge.getDateFin().isBefore(debut))
                        : YearMonth.from(charge.getDateDebut()).equals(periode))
                .toList();
    }

    private boolean correspondAuPerimetre(ChargeGestion charge, Long poissonnerieId) {
        if (poissonnerieId == null) {
            return charge.getPoissonnerie() == null;
        }
        return charge.getPoissonnerie() != null
                && charge.getPoissonnerie().getId().equals(poissonnerieId);
    }

    private YearMonth validerPeriode(Integer mois, Integer annee) {
        try {
            return YearMonth.of(annee, mois);
        } catch (RuntimeException exception) {
            throw new BusinessException("Mois ou année invalide.");
        }
    }

    private ChargeGestionResponse toResponse(ChargeGestion charge) {
        Poissonnerie poissonnerie = charge.getPoissonnerie();
        return new ChargeGestionResponse(
                charge.getId(),
                poissonnerie != null ? poissonnerie.getId() : null,
                poissonnerie != null ? poissonnerie.getName() : "CHARGE GENERALE",
                charge.getCategorie(),
                charge.getLibelle(),
                charge.getMontant(),
                charge.getRecurrente(),
                charge.getDateDebut(),
                charge.getDateFin(),
                charge.getActive()
        );
    }
}
