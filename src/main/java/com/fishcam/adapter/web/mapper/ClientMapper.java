package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateClientRequest;
import com.fishcam.adapter.web.dto.request.UpdateClientRequest;
import com.fishcam.adapter.web.dto.response.ClientDetailResponse;
import com.fishcam.adapter.web.dto.response.ClientResponse;
import com.fishcam.domain.client.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PoissonnerieMapper.class, UserMapper.class})
public interface ClientMapper {

    ClientResponse toResponse(Client entity);

    @Mapping(target = "soldeCompteCourant", ignore = true)
    @Mapping(target = "soldeEpargne", ignore = true)
    ClientDetailResponse toDetailResponse(Client entity);

    @Mapping(source = "poissonnerieId", target = "poissonnerie", ignore = true)
    Client toEntity(CreateClientRequest request);

    void updateEntityFromRequest(UpdateClientRequest request, @MappingTarget Client entity);
}