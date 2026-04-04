//package com.fishcam.application.bilan;
//
//
//import com.fishcam.adapter.web.dto.request.GenererBilanRequest;
//import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
//import com.fishcam.domain.cloture.ClotureJournaliere;
//import com.fishcam.domain.cloture.ClotureJournaliereRepository;
//import com.fishcam.domain.poissonnerie.Poissonnerie;
//import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
//import com.fishcam.domain.user.User;
//import com.fishcam.infrastructure.exception.BusinessException;
//import com.fishcam.infrastructure.exception.ResourceNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class BilanMensuelService {
//
//    private final ClotureJournaliereRepository clotureRepository;
//    private final PoissonnerieRepository poissonnerieRepository;
//    private final BilanMensuelMapper bilanMensuelMapper;
//
//    @Transactional
//    public BilanMensuelResponse generateBilan(GenererBilanRequest request, User currentUser){
//
//        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Poissonnerie non trouvée avec l'id : " + request.getPoissonnerieId()));
//
//        if (bilanRepository.existsByPoissonnerieAndMoisAndAnnee(poissonnerie, request.getMois(), request.getAnnee())){
//            throw new BusinessException("Bilan existant déjà pour cette boutique");
//        }
//
//        List<ClotureJournaliere> clotures = clotureRepository.findByPoissonnerieAndMoisAndAnnee(poissonnerie, request.getMois(), request.getAnnee());
//        if (clotures.isEmpty()) {
//            throw new BusinessException("Aucune clôture trouvée pour ce mois");
//        }
//
//        BigDecimal totalAchatMois = BigDecimal.ZERO;
//        BigDecimal totalVenteRealisee = BigDecimal.ZERO;
//        BigDecimal totalDepensesMois = BigDecimal.ZERO;
//        BigDecimal beneficeNetMois = BigDecimal.ZERO;
//        BigDecimal meilleurBenefice = BigDecimal.ZERO;
//        BigDecimal totalVentePrevisibleMois = BigDecimal.ZERO;
//        LocalDate meilleurJour     = null;
//
//
//        for (ClotureJournaliere cloture : clotures){
//            totalAchatMois     = totalAchatMois.add(cloture.getTotalAchat());
//            totalVenteRealisee = totalVenteRealisee.add(cloture.getVenteRealisee());
//            totalDepensesMois  = totalDepensesMois.add(cloture.getTotalDepenses());
//            beneficeNetMois    = beneficeNetMois.add(cloture.getBeneficeNet());
//            totalVentePrevisibleMois = totalVentePrevisibleMois
//                    .add(cloture.getTotalVentePrevisible());
//
//            if (cloture.getBeneficeNet().compareTo(meilleurBenefice) > 0) {
//                meilleurBenefice = cloture.getBeneficeNet();
//                meilleurJour     = cloture.getDate();
//            }
//
//        }
//        BigDecimal montantDettes = clotures
//                .get(clotures.size() - 1)
//                .getMontantDettesJour();
//
//        BilanMensuel bilan = new BilanMensuel();
//        bilan.setPoissonnerie(poissonnerie);
//        bilan.setGenerePar(currentUser);
//        bilan.setMois(request.getMois());
//        bilan.setAnnee(request.getAnnee());
//        bilan.setTotalAchatMois(totalAchatMois);
//        bilan.setTotalVenteRealisee(totalVenteRealisee);
//        bilan.setTotalDepensesMois(totalDepensesMois);
//        bilan.setBeneficeNetMois(beneficeNetMois);
//        bilan.setMontantDettesMois(montantDettes);
//        bilan.setTotalVentePrevisibleMois(totalVentePrevisibleMois);
//        bilan.setMeilleurJourBenefice(meilleurJour);
//        bilan.setBeneficeMeilleurJour(meilleurBenefice);
//
//        bilan.setNombreJoursTravailles(clotures.size());
//
//        BilanMensuel saved = bilanRepository.save(bilan);
//        return bilanMensuelMapper.toResponse(saved);
//    }
//
//    public BilanMensuelResponse getBilan(    Long poissonnerieId, Integer mois, Integer annee){
//        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));
//
//        BilanMensuel bilanMensuel =  bilanRepository.findByPoissonnerieAndMoisAndAnnee(poissonnerie, mois, annee)
//                .orElseThrow(()-> new BusinessException("Aucun bilan trouvée pour cette date"));
//        return  bilanMensuelMapper.toResponse(bilanMensuel);
//    }
//
//    public List<BilanMensuelResponse> getHistorique(
//            Long poissonnerieId){
//        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));
//        return bilanRepository.findByPoissonnerieOrderByAnneeDescMoisDesc(poissonnerie)
//                .stream()
//                .map(bilanMensuelMapper::toResponse)
//                .toList();
//    }
//}
