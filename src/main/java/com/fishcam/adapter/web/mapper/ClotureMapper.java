package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.ClotureJournaliereRequest;
import com.fishcam.adapter.web.dto.response.ClotureJournaliereResponse;
import com.fishcam.domain.cloture.ClotureJournaliere;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClotureMapper {


    @Mapping(source = "cloturePar.firstName", target = "clotureParNom")
    @Mapping(source = "cloturePar.id", target = "clotureParId")
    @Mapping(source = "poissonnerie.name", target = "poissonnerieNom")
    @Mapping(source = "poissonnerie.id", target = "poissonnerieId")
    @Mapping(target = "ecartVente", ignore = true)
    ClotureJournaliereResponse toResponse(ClotureJournaliere clotureJournaliere);

    //  toEntity ignores fields set manually in service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poissonnerie", ignore = true)
    @Mapping(target = "cloturePar", ignore = true)
    @Mapping(target = "totalAchat", ignore = true)
    @Mapping(target = "totalVentePrevisible", ignore = true)
    @Mapping(target = "montantDettesJour", ignore = true)
    @Mapping(target = "montantRembourseJour", ignore = true)
    @Mapping(target = "nombreDettesJour", ignore = true)
    @Mapping(target = "venteRealisee", ignore = true)
    @Mapping(target = "totalDepenses", ignore = true)
    @Mapping(target = "beneficeNet", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ClotureJournaliere toEntity(ClotureJournaliereRequest request);



}
