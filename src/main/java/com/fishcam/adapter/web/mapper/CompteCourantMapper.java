package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.CompteCourantDetailResponse;
import com.fishcam.adapter.web.dto.response.CompteCourantResponse;
import com.fishcam.domain.comptecourant.CompteCourant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ClientMapper.class, PoissonnerieMapper.class, UserMapper.class, TransactionCCMapper.class})
public interface CompteCourantMapper {

    //@Mapping(source = "limiteCredit", target = "limiteCreditMax")
    CompteCourantResponse toResponse(CompteCourant entity);

    @Mapping(source = "limiteCreditMax", target = "limiteCredit") // ← entité→response (noms différents)
    @Mapping(target = "transactions", ignore = true)
    @Mapping(target = "nombreTransactions", ignore = true)
    @Mapping(target = "totalEmprunts", ignore = true)
    @Mapping(target = "totalRemboursements", ignore = true)
    CompteCourantDetailResponse toDetailResponse(CompteCourant entity);
}