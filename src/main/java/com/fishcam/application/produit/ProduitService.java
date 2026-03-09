package com.fishcam.application.produit;

import com.fishcam.adapter.web.dto.request.CreateProduitRequest;
import com.fishcam.adapter.web.dto.request.UpdateProduitRequest;
import com.fishcam.adapter.web.dto.response.ProduitResponse;
import com.fishcam.adapter.web.mapper.ProduitMapper;
import com.fishcam.domain.produit.Produit;
import com.fishcam.domain.produit.ProduitRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class ProduitService {

    private final ProduitRepository produitRepository;
    private final ProduitMapper produitMapper;

    @Transactional
    public ProduitResponse createProduit(CreateProduitRequest request){
            if(produitRepository.existsByNom(request.getNom())){
                throw new BusinessException("Le produit ayant ce nom existe deja ");
            }
            Produit produit = produitMapper.toEntity(request);
            produit.setActif(true);

            Produit savedProduit = produitRepository.save(produit);
            return produitMapper.toReponse(savedProduit);
    }



    public Page<ProduitResponse> getAllProduits(Pageable pageable){
         Page<Produit> produitPage = produitRepository.findByActifTrue(pageable);
         return produitPage.map(produitMapper::toReponse);

    }

    public List <ProduitResponse> searchProduits(String q){

        return produitRepository.findByNomContainingIgnoreCaseAndActifTrue(q).stream()
                .map(produitMapper::toReponse).toList();
    }


    public  ProduitResponse getProduitById (Long id){
        Produit produit = produitRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Produit non trouvé avec l'id : " + id));

        return  produitMapper.toReponse(produit);
    }

    @Transactional
    public ProduitResponse updateProduit(Long productId, UpdateProduitRequest request){
        Produit produit = produitRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le produit non trouve avec l'id : " +productId ));
        if(request.getNom() != null ) {
            if (!request.getNom().equals(produit.getNom())
                    && produitRepository.existsByNom(request.getNom())) {
                throw new BusinessException("Ce nom existe déjà");
            }
            produit.setNom(request.getNom());
        }

        if(request.getUnite() != null ){
            produit.setUnite(request.getUnite());
        }
        if(request.getPoidsParCarton() != null ){
            if(request.getPoidsParCarton().compareTo(BigDecimal.ZERO) <= 0){
                throw new BusinessException("Le poids par carton doit être positif");
            }
            produit.setPoidsParCarton(request.getPoidsParCarton());
        }
       Produit savedProduit = produitRepository.save(produit);
        return produitMapper.toReponse(savedProduit);
    }


    @Transactional
    public void  deleteProduit(Long produitId){
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Le produit non trouve avec l'id : " + produitId
                ));
        produit.setActif(false);
        produitRepository.save(produit);
    }





}
