package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.DepotEpargneRequest;
import com.fishcam.adapter.web.dto.request.RetraitEpargneRequest;
import com.fishcam.adapter.web.dto.response.TransactionEpargneResponse;
import com.fishcam.domain.epargne.TransactionEpargne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface TransactionEpargneMapper {


    @Mapping(source = "epargneId", target = "epargne", ignore = true)
    @Mapping(constant = "DEPOT", target = "type")
    @Mapping(target = "id", ignore = true)              // ← AJOUTER
    @Mapping(target = "poissonnerie", ignore = true)    // ← AJOUTER
    @Mapping(target = "effectuePar", ignore = true)     // ← AJOUTER
    @Mapping(target = "transactionDate", ignore = true)
    TransactionEpargne toEntity(DepotEpargneRequest request);


    @Mapping(constant = "RETRAIT", target = "type")
    @Mapping(target = "id", ignore = true)              // ← AJOUTER
    @Mapping(target = "epargne", ignore = true)         // ← AJOUTER
    @Mapping(target = "poissonnerie", ignore = true)    // ← AJOUTER
    @Mapping(target = "effectuePar", ignore = true)     // ← AJOUTER
    @Mapping(target = "transactionDate", ignore = true)
    TransactionEpargne toEntity(RetraitEpargneRequest request);


    TransactionEpargneResponse toResponse(TransactionEpargne entity);

}
