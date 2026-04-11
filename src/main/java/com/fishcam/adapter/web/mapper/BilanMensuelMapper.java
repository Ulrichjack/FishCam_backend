package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.domain.bilan.BilanMensuel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BilanMensuelMapper {

    @Mapping(target = "poissonnerieNom", source = "poissonnerie.name")
    @Mapping(target = "genereParNom", source = "generePar.firstName")
    @Mapping(target = "poissonnerieId", source = "poissonnerie.id")
    @Mapping(target = "genereParId", source = "generePar.id")
    BilanMensuelResponse toResponse(BilanMensuel bilan);

    List<BilanMensuelResponse> toResponseList(List<BilanMensuel> bilans);



}