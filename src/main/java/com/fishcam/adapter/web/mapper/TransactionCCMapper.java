package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.TransactionCCResponse;
import com.fishcam.domain.comptecourant.TransactionCompteCourant;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TransactionCCMapper {

    TransactionCCResponse toResponse(TransactionCompteCourant entity);
}