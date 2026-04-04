package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateLivreurRequest;
import com.fishcam.adapter.web.dto.response.LivreurResponse;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.livreur.Livreur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LivreurMapper {

    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.nom", target = "fournisseurNom")
    LivreurResponse toResponse(Livreur livreur);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nom", source = "request.nom") // Tell it to use the Request!
    @Mapping(target = "prenom", source = "request.prenom")
    @Mapping(target = "telephone", source = "request.telephone")
    @Mapping(target = "fournisseur", source = "fournisseur")
    @Mapping(target = "actif", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Livreur toEntity(CreateLivreurRequest request, Fournisseur fournisseur);


}
