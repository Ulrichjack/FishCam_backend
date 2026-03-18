package com.fishcam.application.fournisseur;


import com.fishcam.adapter.web.dto.request.CreateFournisseurRequest;
import com.fishcam.adapter.web.dto.response.FournisseurResponse;
import com.fishcam.adapter.web.mapper.FournisseurMapper;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class FournisseurServiceTest {

    @Mock
    private FournisseurRepository fournisseurRepository;

    @Mock
    private FournisseurMapper fournisseurMapper;

    @InjectMocks
    private FournisseurService fournisseurService;


    //Test1 Happy path (tout se passé bien)
    @Test
    void createFournisseur_shouldReturnResponse_whenNomIsUnique(){

        //prepare the data
        CreateFournisseurRequest request = new CreateFournisseurRequest();
        request.setNom("Congelcam");
        request.setTelephone("123");
        request.setVille("Douala");

        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(1L);
        fournisseur.setNom("Congelcam");
        fournisseur.setActif(true);

        FournisseurResponse expectedResponse = new FournisseurResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNom("Congelcam");

        //when these methods are called, return this
        when(fournisseurRepository.existsByNomIgnoreCase("Congelcam")).thenReturn(false);
        when(fournisseurMapper.toEntity(request)).thenReturn(fournisseur);
        when(fournisseurRepository.save(fournisseur)).thenReturn(fournisseur);
        when(fournisseurMapper.toResponse(fournisseur)).thenReturn(expectedResponse);

        FournisseurResponse result = fournisseurService.createFournisseur(request);

        assertThat(result).isNotNull();
        assertThat(result.getNom()).isEqualTo("Congelcam");
        assertThat(result.getId()).isEqualTo(1L);

    }

    //test 2 name exist
    @Test
    void createFournisseur_shouldThrowException_whenNomAlreadyExists(){

        CreateFournisseurRequest request = new CreateFournisseurRequest();
        request.setNom("Congelcam");

        //simulate that the name already exists
        when(fournisseurRepository.existsByNomIgnoreCase("Congelcam")).thenReturn(true);

        //verify that BusinessException is thrown
        assertThrows(BusinessException.class,
                () -> fournisseurService.createFournisseur(request));

    }

    // Test 3 — getFournisseurById found
    @Test
    void getFournisseurById_shouldReturnResponse_whenFound() {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(1L);
        fournisseur.setNom("Congelcam");

        FournisseurResponse expectedResponse = new FournisseurResponse();
        expectedResponse.setId(1L);
        expectedResponse.setNom("Congelcam");

        when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(fournisseur));
        when(fournisseurMapper.toResponse(fournisseur)).thenReturn(expectedResponse);

        FournisseurResponse result = fournisseurService.getFournisseurById(1L);

        assertThat(result.getNom()).isEqualTo("Congelcam");
    }

    // Test 4 — getFournisseurById not found
    @Test
    void getFournisseurById_shouldThrowException_whenNotFound() {
        when(fournisseurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fournisseurService.getFournisseurById(99L));
    }

    // Test 5 — deleteFournisseur sets actif to false
    @Test
    void deleteFournisseur_shouldSetActifFalse_whenExists() {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(1L);
        fournisseur.setActif(true);

        when(fournisseurRepository.findById(1L)).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepository.save(fournisseur)).thenReturn(fournisseur);

        fournisseurService.deleteFournisseur(1L);

        assertThat(fournisseur.getActif()).isFalse();
    }

}
