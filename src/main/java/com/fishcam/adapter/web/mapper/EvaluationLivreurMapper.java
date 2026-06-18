package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.request.CreateEvaluationRequest;
import com.fishcam.adapter.web.dto.response.EvaluationLivreurResponse;
import com.fishcam.domain.achat.AchatJournalier;
import com.fishcam.domain.livreur.EvaluationLivreur;
import com.fishcam.domain.livreur.Livreur;
import com.fishcam.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface  EvaluationLivreurMapper {

    @Mapping(source = "livreur.id", target = "livreurId")
    @Mapping(source = "livreur.nom", target = "livreurNom")
    @Mapping(source = "livreur.prenom", target = "livreurPrenom")
    @Mapping(expression = "java(evaluationLivreur.getUser().getFirstName() + \" \" + evaluationLivreur.getUser().getLastName())", target = "evaluatorNom")
    EvaluationLivreurResponse toResponse(EvaluationLivreur evaluationLivreur);


    @Mapping(target = "livreur", source = "livreur")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "achatJournalier", source = "achatJournalier")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "dateEvaluation", ignore = true)
    EvaluationLivreur toEntity(CreateEvaluationRequest request, Livreur livreur, User user, AchatJournalier achatJournalier);

}
