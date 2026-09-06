package com.fishcam.application.gestion;

import com.fishcam.adapter.web.dto.response.ResultatMensuelBoutiqueResponse;
import com.fishcam.adapter.web.dto.response.ResultatMensuelGlobalResponse;
import com.fishcam.adapter.web.dto.response.ResultatAnnuelResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.gestion.CategorieCharge;
import com.fishcam.domain.gestion.ReleveSituationMensuelle;
import com.fishcam.domain.gestion.ReleveSituationMensuelleRepository;
import com.fishcam.domain.gestion.ModeEvaluation;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultatMensuelService {

    /** Stock inconnu : seul le résultat provisoire est disponible. */
    static final String STATUT_PROVISOIRE = "PROVISOIRE";
    /** Stock compté aux deux bornes, créances encore inconnues. */
    static final String STATUT_CORRIGE_STOCK = "CORRIGE_STOCK";
    /** Stock et créances connus, mais au moins une borne est une estimation. */
    static final String STATUT_ESTIME = "ESTIME";
    /** Stock et créances connus, comptage physique aux deux bornes. */
    static final String STATUT_VALIDE = "VALIDE";

    private static final List<String> STATUTS_DU_PLUS_FAIBLE_AU_PLUS_SUR =
            List.of(STATUT_PROVISOIRE, STATUT_CORRIGE_STOCK, STATUT_ESTIME, STATUT_VALIDE);

    private final PoissonnerieRepository poissonnerieRepository;
    private final ClotureJournaliereRepository clotureRepository;
    private final ReleveSituationMensuelleRepository releveRepository;
    private final ChargeGestionService chargeService;

    public ResultatMensuelBoutiqueResponse calculerBoutique(Long poissonnerieId, Integer mois, Integer annee) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
        return calculerBoutique(poissonnerie, validerPeriode(mois, annee));
    }

    public ResultatMensuelGlobalResponse calculerGlobal(Integer mois, Integer annee) {
        YearMonth periode = validerPeriode(mois, annee);
        List<ResultatMensuelBoutiqueResponse> boutiques = poissonnerieRepository.findByActiveTrue()
                .stream()
                .map(poissonnerie -> calculerBoutique(poissonnerie, periode))
                .toList();

        BigDecimal totalAchats = somme(boutiques, ResultatMensuelBoutiqueResponse::getTotalAchats);
        BigDecimal totalEncaissements = somme(boutiques, ResultatMensuelBoutiqueResponse::getTotalEncaissements);
        BigDecimal totalDepensesJournalieres = somme(boutiques, ResultatMensuelBoutiqueResponse::getDepensesJournalieres);
        BigDecimal totalChargesBoutiques = somme(boutiques, ResultatMensuelBoutiqueResponse::getChargesMensuelles);
        BigDecimal chargesGenerales = chargeService.totalApplicable(periode, null);

        BigDecimal resultatProvisoire = somme(boutiques, ResultatMensuelBoutiqueResponse::getResultatProvisoire)
                .subtract(chargesGenerales);

        List<String> informationsManquantes = new ArrayList<>();
        List<String> alertes = new ArrayList<>();
        for (ResultatMensuelBoutiqueResponse boutique : boutiques) {
            boutique.getInformationsManquantes().forEach(information ->
                    informationsManquantes.add(boutique.getPoissonnerieNom() + " : " + information));
            boutique.getAlertes().forEach(alerte ->
                    alertes.add(boutique.getPoissonnerieNom() + " : " + alerte));
        }

        // Le stock se consolide dès qu'il est connu partout : une créance manquante dans une
        // boutique ne doit pas annuler le comptage physique fait dans les autres.
        boolean stockConnuPartout = !boutiques.isEmpty() && boutiques.stream()
                .allMatch(boutique -> boutique.getVariationStock() != null);
        boolean creancesConnuesPartout = !boutiques.isEmpty() && boutiques.stream()
                .allMatch(boutique -> boutique.getResultatValide() != null);

        BigDecimal variationStock = stockConnuPartout
                ? somme(boutiques, ResultatMensuelBoutiqueResponse::getVariationStock)
                : null;
        BigDecimal resultatCorrigeStock = stockConnuPartout
                ? resultatProvisoire.add(variationStock)
                : null;
        BigDecimal variationCreances = creancesConnuesPartout
                ? somme(boutiques, ResultatMensuelBoutiqueResponse::getVariationCreances)
                : null;
        BigDecimal resultatValide = creancesConnuesPartout
                ? resultatCorrigeStock.add(variationCreances)
                : null;

        return new ResultatMensuelGlobalResponse(
                mois,
                annee,
                boutiques,
                totalAchats,
                totalEncaissements,
                totalDepensesJournalieres,
                totalChargesBoutiques,
                chargesGenerales,
                resultatProvisoire,
                variationStock,
                variationCreances,
                resultatCorrigeStock,
                resultatValide,
                statutLePlusFaible(boutiques),
                informationsManquantes,
                alertes
        );
    }

    public ResultatAnnuelResponse calculerAnnuel(Integer annee) {
        try {
            Year.of(annee);
        } catch (RuntimeException exception) {
            throw new BusinessException("Année invalide.");
        }
        if (annee > Year.now().getValue()) {
            throw new BusinessException("Impossible de calculer une année future.");
        }

        int dernierMois = annee.equals(Year.now().getValue())
                ? YearMonth.now().getMonthValue() : 12;
        List<ResultatMensuelGlobalResponse> mois = java.util.stream.IntStream
                .rangeClosed(1, dernierMois)
                .mapToObj(numero -> calculerGlobal(numero, annee))
                .toList();

        BigDecimal totalAchats = sommeGlobale(mois, ResultatMensuelGlobalResponse::getTotalAchats);
        BigDecimal totalEncaissements = sommeGlobale(mois, ResultatMensuelGlobalResponse::getTotalEncaissements);
        BigDecimal totalDepenses = sommeGlobale(mois, resultat -> resultat.getTotalDepensesJournalieres()
                .add(resultat.getTotalChargesBoutiques()).add(resultat.getChargesGenerales()));
        BigDecimal resultatProvisoire = sommeGlobale(mois, ResultatMensuelGlobalResponse::getResultatProvisoire);

        boolean stocksConnus = !mois.isEmpty() && mois.stream()
                .allMatch(resultat -> resultat.getResultatCorrigeStock() != null);
        boolean resultatsValides = !mois.isEmpty() && mois.stream()
                .allMatch(resultat -> resultat.getResultatValide() != null);
        BigDecimal corrigeStock = stocksConnus
                ? sommeGlobale(mois, ResultatMensuelGlobalResponse::getResultatCorrigeStock) : null;
        BigDecimal valide = resultatsValides
                ? sommeGlobale(mois, ResultatMensuelGlobalResponse::getResultatValide) : null;
        String statut = mois.stream().map(ResultatMensuelGlobalResponse::getStatut)
                .min(java.util.Comparator.comparingInt(STATUTS_DU_PLUS_FAIBLE_AU_PLUS_SUR::indexOf))
                .orElse(STATUT_PROVISOIRE);

        return new ResultatAnnuelResponse(annee, mois, totalAchats, totalEncaissements,
                totalDepenses, totalDepenses.subtract(
                        sommeGlobale(mois, ResultatMensuelGlobalResponse::getTotalDepensesJournalieres)),
                resultatProvisoire, corrigeStock, valide, statut);
    }

    private ResultatMensuelBoutiqueResponse calculerBoutique(Poissonnerie poissonnerie, YearMonth periode) {
        LocalDate debut = periode.atDay(1);
        LocalDate fin = periode.atEndOfMonth();
        List<ClotureJournaliere> clotures = clotureRepository
                .findByPoissonnerieAndDateBetweenOrderByDateAsc(poissonnerie, debut, fin);

        BigDecimal achats = clotures.stream().map(ClotureJournaliere::getTotalAchat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal encaissements = clotures.stream().map(ClotureJournaliere::getVenteRealisee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal depensesJournalieres = clotures.stream().map(ClotureJournaliere::getTotalDepenses)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<CategorieCharge, BigDecimal> chargesParCategorie =
                chargeService.totauxParCategorie(periode, poissonnerie.getId());
        BigDecimal chargesMensuelles = chargesParCategorie.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal resultatProvisoire = ResultatMensuelCalculator.calculerProvisoire(
                encaissements, achats, depensesJournalieres, chargesMensuelles);

        List<String> alertes = detecterDoublesComptages(clotures, chargesParCategorie);

        Optional<ReleveSituationMensuelle> initial = releveRepository
                .findByPoissonnerieAndDateReleve(poissonnerie, debut.minusDays(1));
        Optional<ReleveSituationMensuelle> finale = releveRepository
                .findByPoissonnerieAndDateReleve(poissonnerie, fin);

        List<String> manquantes = new ArrayList<>();
        if (initial.isEmpty()) {
            manquantes.add("relevé d'ouverture au " + debut.minusDays(1));
        }
        if (finale.isEmpty()) {
            manquantes.add("relevé de clôture au " + fin);
        }

        if (initial.isPresent() && initial.get().getTotalCreancesClients() == null) {
            manquantes.add("créances clients d'ouverture au " + debut.minusDays(1));
        }
        if (finale.isPresent() && finale.get().getTotalCreancesClients() == null) {
            manquantes.add("créances clients de clôture au " + fin);
        }

        BigDecimal variationStock = null;
        BigDecimal resultatCorrigeStock = null;
        BigDecimal variationCreances = null;
        BigDecimal resultatValide = null;

        // Le stock seul suffit à corriger le résultat : c'est la correction la plus lourde
        // (marchandise achetée non vendue) et elle ne doit pas attendre les créances.
        if (initial.isPresent() && finale.isPresent()) {
            variationStock = finale.get().getValeurStock().subtract(initial.get().getValeurStock());
            resultatCorrigeStock = resultatProvisoire.add(variationStock);

            if (initial.get().getTotalCreancesClients() != null
                    && finale.get().getTotalCreancesClients() != null) {
                variationCreances = finale.get().getTotalCreancesClients()
                        .subtract(initial.get().getTotalCreancesClients());
                resultatValide = ResultatMensuelCalculator.calculerValide(
                        resultatProvisoire,
                        initial.get().getValeurStock(),
                        finale.get().getValeurStock(),
                        initial.get().getTotalCreancesClients(),
                        finale.get().getTotalCreancesClients());
            }
        }

        boolean relevesPhysiques = initial.isPresent() && finale.isPresent()
                && initial.get().getModeEvaluation() == ModeEvaluation.COMPTAGE_PHYSIQUE
                && finale.get().getModeEvaluation() == ModeEvaluation.COMPTAGE_PHYSIQUE;

        String statut;
        if (resultatCorrigeStock == null) {
            statut = STATUT_PROVISOIRE;
        } else if (resultatValide == null) {
            statut = STATUT_CORRIGE_STOCK;
        } else {
            statut = relevesPhysiques ? STATUT_VALIDE : STATUT_ESTIME;
        }

        return new ResultatMensuelBoutiqueResponse(
                poissonnerie.getId(),
                poissonnerie.getName(),
                periode.getMonthValue(),
                periode.getYear(),
                clotures.size(),
                clotures.isEmpty() ? null : clotures.get(clotures.size() - 1).getDate(),
                achats,
                encaissements,
                depensesJournalieres,
                chargesMensuelles,
                resultatProvisoire,
                initial.map(ReleveSituationMensuelle::getDateReleve).orElse(null),
                initial.map(ReleveSituationMensuelle::getValeurStock).orElse(null),
                initial.map(ReleveSituationMensuelle::getTotalCreancesClients).orElse(null),
                finale.map(ReleveSituationMensuelle::getDateReleve).orElse(null),
                finale.map(ReleveSituationMensuelle::getValeurStock).orElse(null),
                finale.map(ReleveSituationMensuelle::getTotalCreancesClients).orElse(null),
                variationStock,
                variationCreances,
                resultatCorrigeStock,
                resultatValide,
                statut,
                manquantes,
                alertes
        );
    }

    /**
     * Le transport et la ration existent à deux endroits : en dépense de clôture journalière
     * et en charge mensuelle. Renseigner les deux déduit la même dépense deux fois.
     */
    private List<String> detecterDoublesComptages(
            List<ClotureJournaliere> clotures,
            Map<CategorieCharge, BigDecimal> chargesParCategorie) {

        List<String> alertes = new ArrayList<>();
        ajouterSiDoubleComptage(alertes, chargesParCategorie, CategorieCharge.TRANSPORT, "transport",
                clotures.stream().map(ClotureJournaliere::getTransport)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        ajouterSiDoubleComptage(alertes, chargesParCategorie, CategorieCharge.RATION, "ration",
                clotures.stream().map(ClotureJournaliere::getRation)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        return alertes;
    }

    private void ajouterSiDoubleComptage(
            List<String> alertes,
            Map<CategorieCharge, BigDecimal> chargesParCategorie,
            CategorieCharge categorie,
            String libelle,
            BigDecimal totalJournalier) {

        BigDecimal charge = chargesParCategorie.get(categorie);
        if (charge == null
                || charge.signum() <= 0
                || totalJournalier.signum() <= 0) {
            return;
        }
        alertes.add("le " + libelle + " est compté deux fois : " + charge
                + " en charge mensuelle et " + totalJournalier
                + " saisis dans les clôtures du mois. Ne conservez qu'une seule source.");
    }

    /** Le résultat global n'est jamais plus sûr que la boutique la moins bien renseignée. */
    private String statutLePlusFaible(List<ResultatMensuelBoutiqueResponse> boutiques) {
        if (boutiques.isEmpty()) {
            return STATUT_PROVISOIRE;
        }
        return boutiques.stream()
                .map(ResultatMensuelBoutiqueResponse::getStatut)
                .min(java.util.Comparator.comparingInt(STATUTS_DU_PLUS_FAIBLE_AU_PLUS_SUR::indexOf))
                .orElse(STATUT_PROVISOIRE);
    }

    private YearMonth validerPeriode(Integer mois, Integer annee) {
        try {
            return YearMonth.of(annee, mois);
        } catch (RuntimeException exception) {
            throw new BusinessException("Mois ou année invalide.");
        }
    }

    private BigDecimal somme(
            List<ResultatMensuelBoutiqueResponse> boutiques,
            java.util.function.Function<ResultatMensuelBoutiqueResponse, BigDecimal> extracteur) {
        return boutiques.stream().map(extracteur).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sommeGlobale(
            List<ResultatMensuelGlobalResponse> resultats,
            java.util.function.Function<ResultatMensuelGlobalResponse, BigDecimal> extracteur) {
        return resultats.stream().map(extracteur).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
