package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.GenererBilanRequest;
import com.fishcam.adapter.web.dto.response.BilanMensuelResponse;
import com.fishcam.domain.bilan.BilanMensuel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BilanMensuelMapper {

    @Mapping(target = "poissonnerieNom", source = "poissonnerie.name")
    @Mapping(target = "genereParNom",    source = "generePar.firstName") // which field ?
    BilanMensuelResponse toResponse(BilanMensuel bilan);

    List<BilanMensuelResponse> toResponseList(List<BilanMensuel> bilans);



}