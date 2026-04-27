package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateFournisseurRequest;
import com.fishcam.adapter.web.dto.response.FournisseurResponse;
import com.fishcam.domain.fournisseur.Fournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface FournisseurMapper {

    FournisseurResponse toResponse(Fournisseur fournisseur);

    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Fournisseur toEntity(CreateFournisseurRequest createFournisseurRequest);

}
