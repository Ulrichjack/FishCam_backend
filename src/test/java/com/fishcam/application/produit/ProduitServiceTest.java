package com.fishcam.application.produit;

import com.fishcam.adapter.web.dto.request.CreateProduitRequest;
import com.fishcam.adapter.web.dto.response.ProduitResponse;
import com.fishcam.adapter.web.mapper.ProduitMapper;
import com.fishcam.domain.produit.Produit;
import com.fishcam.domain.produit.ProduitRepository;
import com.fishcam.domain.produit.Unite;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProduitServiceTest {

    @Mock
    private ProduitRepository produitRepository;

    @Mock
    private ProduitMapper produitMapper;

    @InjectMocks
    private ProduitService produitService;

    // TEST 1 — Happy path (tout se passe bien)
    @Test
    void createProduit_shouldReturnResponse_whenNomIsUnique(){
        CreateProduitRequest request = new CreateProduitRequest();
        request.setNom("JAX 23+");
        request.setUnite(Unite.KG);
        request.setPoidsParCarton(new BigDecimal("20"));

        Produit produit = new Produit();
        produit.setId(1L);
        produit.setNom("JAX 23+");
        produit.setActif(true);

        ProduitResponse expectedResponse = new ProduitResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNom("JAX 23+");

        when(produitRepository.existsByNomIgnoreCase("JAX 23+")).thenReturn(false);
        when(produitMapper.toEntity(request)).thenReturn(produit);
        when(produitRepository.save(produit)).thenReturn(produit);
        when(produitMapper.toReponse(produit)).thenReturn(expectedResponse);

        // ACT — appeler la méthode
        ProduitResponse result = produitService.createProduit(request);

        // ASSERT — vérifier le résultat
        assertThat(result.getNom()).isEqualTo("JAX 23+");
    }

    // TEST 2 — Nom déjà existant
    @Test
    void createProduit_shouldThrowBusinessException_whenNomAlreadyExists() {
        // ARRANGE
        CreateProduitRequest request = new CreateProduitRequest();
        request.setNom("JAX 23+");

        when(produitRepository.existsByNomIgnoreCase("JAX 23+")).thenReturn(true);

        // ACT + ASSERT
        assertThrows(BusinessException.class,
                () -> produitService.createProduit(request));
    }

    // TEST 3 — getProduitById trouvé
    @Test
    void getProduitById_shouldReturnResponse_whenProduitExists() {
        // ARRANGE
        Produit produit = new Produit();
        produit.setId(1L);
        produit.setNom("JAX 23+");

        ProduitResponse expectedResponse = new ProduitResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNom("JAX 23+");

        // EN: Simulate findById returns the produit
        // FR: Simuler que findById retourne le produit
        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitMapper.toReponse(produit)).thenReturn(expectedResponse);

        // ACT
        ProduitResponse result = produitService.getProduitById(1L);

        // ASSERT
        assertThat(result.getNom()).isEqualTo("JAX 23+");
    }

    // TEST 4 — getProduitById non trouvé
    @Test
    void getProduitById_shouldThrowResourceNotFoundException_whenNotFound() {
        // EN: Simulate findById returns nothing
        // FR: Simuler que findById ne trouve rien
        when(produitRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(ResourceNotFoundException.class,
                () -> produitService.getProduitById(99L));
    }

    @Test
    void deleteProduit_shouldSetActifFalse_whenProduitExists() {
        // ARRANGE
        Produit produit = new Produit();
        produit.setId(1L);
        produit.setActif(true);

        when(produitRepository.findById(1L)).thenReturn(Optional.of(produit));
        when(produitRepository.save(produit)).thenReturn(produit);

        // ACT
        produitService.deleteProduit(1L);

        // ASSERT
        // EN: Verify actif is now false
        // FR: Vérifier que actif est maintenant false
        assertThat(produit.getActif()).isFalse();
    }
}







