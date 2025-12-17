package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateEpargneRequest;
import com.fishcam.adapter.web.dto.response.EpargneDetailResponse;
import com.fishcam.adapter.web.dto.response.EpargneResponse;
import com.fishcam.domain.epargne.Epargne;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EpargneMapper {

    EpargneResponse toResponse(Epargne entity);

    @Mapping(target = "nombreTransactions", ignore = true)
    @Mapping(target = "totalDepots", ignore = true)
    @Mapping(target = "totalRetraits", ignore = true)
    EpargneDetailResponse toDetailResponse(Epargne entity);

    @Mapping(source = "clientId", target = "client", ignore = true)
    @Mapping(source = "poissonnerieId", target = "poissonnerie", ignore = true)
    @Mapping(source = "initialAmount", target = "currentBalance")
    Epargne toEntity(CreateEpargneRequest request);

}
