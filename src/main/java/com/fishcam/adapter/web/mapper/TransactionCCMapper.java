package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.TransactionCCResponse;
import com.fishcam.domain.comptecourant.TransactionCompteCourant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TransactionCCMapper {

    @Mapping(target = "transactionOrigineId", source = "transactionOrigine.id")
    @Mapping(target = "annulee", ignore = true)
    TransactionCCResponse toResponse(TransactionCompteCourant entity);
}
