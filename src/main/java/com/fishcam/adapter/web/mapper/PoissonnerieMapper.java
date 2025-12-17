package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.request.UpdatePoissonnerieRequest;
import com.fishcam.adapter.web.dto.response.PoissonnerieResponse;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PoissonnerieMapper {


    PoissonnerieResponse toResponse(Poissonnerie entity);

    Poissonnerie toEntity (CreatePoissonnerieRequest request);

    //Met à jour une entité existante avec les données du Request
    //@MappingTarget : modifie l'objet en paramètre
    //Seuls les champs non-null du Request sont appliqués
    void updateEntityFromRequest(UpdatePoissonnerieRequest request, @MappingTarget Poissonnerie entity);

}
