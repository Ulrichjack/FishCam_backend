package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateClientRequest;
import com.fishcam.adapter.web.dto.request.UpdateClientRequest;
import com.fishcam.adapter.web.dto.response.ClientDetailResponse;
import com.fishcam.adapter.web.dto.response.ClientResponse;
import com.fishcam.domain.client.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ClientMapper {

    @Mapping(target = "soldeCompteCourant", ignore = true)
    ClientResponse toResponse(Client entity);

    @Mapping(target = "soldeCompteCourant", ignore = true)
    @Mapping(target = "soldeEpargne", ignore = true)
    ClientDetailResponse toDetailResponse(Client entity);


    @Mapping(target = "poissonnerie", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "compteCourant", ignore = true)
    Client toEntity(CreateClientRequest request);

    void updateEntityFromRequest(UpdateClientRequest request, @MappingTarget Client entity);
}