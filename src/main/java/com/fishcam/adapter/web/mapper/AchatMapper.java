package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateFactureRequest;
import com.fishcam.adapter.web.dto.request.CreateLigneRequest;
import com.fishcam.adapter.web.dto.response.FactureDetailResponse;
import com.fishcam.adapter.web.dto.response.FactureResponse;
import com.fishcam.adapter.web.dto.response.LigneAchatResponse;
import com.fishcam.domain.achat.AchatJournalier;
import com.fishcam.domain.achat.LigneAchat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AchatMapper {


    @Mapping(source = "poissonnerie.id", target = "poissonnerieId")
    @Mapping(source = "poissonnerie.name", target = "poissonnerieNom")
    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.nom", target = "fournisseurNom")
    @Mapping(source = "enregistrePar.id", target = "enregistreParId")
    @Mapping(source = "enregistrePar.firstName", target = "enregistreParNom")
    @Mapping(target = "totalAchat", ignore = true)
    FactureResponse toResponse(AchatJournalier achatJournalier);


    @Mapping(source = "poissonnerie.id", target = "poissonnerieId")
    @Mapping(source = "poissonnerie.name", target = "poissonnerieNom")
    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.nom", target = "fournisseurNom")
    @Mapping(source = "enregistrePar.id", target = "enregistreParId")
    @Mapping(source = "enregistrePar.firstName", target = "enregistreParNom")
    @Mapping(target = "ligneAchatResponses", ignore = true)
    @Mapping(target = "totalAchat", ignore = true)
    @Mapping(target = "totalVente", ignore = true)
    @Mapping(target = "margeTotal", ignore = true)
    FactureDetailResponse toDetailResponse(AchatJournalier achatJournalier);


    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "produit.nom", target = "produitNom")
    @Mapping(target = "prixAchatKilo", ignore = true)
    @Mapping(target = "prixVenteTotal", ignore = true)
    @Mapping(target = "margeKilo", ignore = true)
    @Mapping(target = "margeTotal", ignore = true)
    LigneAchatResponse toLigneResponse(LigneAchat ligneAchat);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cloture", ignore = true)
    @Mapping(target = "poissonnerie", ignore = true)
    @Mapping(target = "fournisseur", ignore = true)
    @Mapping(target = "enregistrePar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AchatJournalier toEntity(CreateFactureRequest createFactureRequest);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "achatJournalier", ignore = true)
    LigneAchat toLigneEntity(CreateLigneRequest createLigneRequest);

}


