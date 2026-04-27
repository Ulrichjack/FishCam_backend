package com.fishcam.adapter.web.mapper;


import com.fishcam.adapter.web.dto.request.CreateEmployeRequest;
import com.fishcam.adapter.web.dto.response.EmployeResponse;
import com.fishcam.domain.employe.Employe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeMapper {

    @Mapping(source = "poissonnerie.id", target = "poissonnerieId")
    @Mapping(source = "poissonnerie.name", target = "poissonnerieNom")
    @Mapping(source = "user.id", target = "userId")
    EmployeResponse toResponse(Employe employe);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poissonnerie", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employe toEntity(CreateEmployeRequest createEmployeRequest);

}
