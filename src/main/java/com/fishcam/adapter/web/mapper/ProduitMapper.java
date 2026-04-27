package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateProduitRequest;
import com.fishcam.adapter.web.dto.response.ProduitResponse;
import com.fishcam.domain.produit.Produit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProduitMapper {


    ProduitResponse toReponse(Produit produit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Produit toEntity(CreateProduitRequest request);

}
