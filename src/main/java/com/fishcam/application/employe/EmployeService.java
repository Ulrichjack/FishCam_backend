package com.fishcam.application.employe;

import com.fishcam.adapter.web.dto.request.CreateEmployeRequest;
import com.fishcam.adapter.web.dto.request.UpdateEmployeRequest;
import com.fishcam.adapter.web.dto.response.EmployeResponse;
import com.fishcam.adapter.web.mapper.EmployeMapper;
import com.fishcam.domain.employe.Employe;
import com.fishcam.domain.employe.EmployeRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final EmployeMapper employeMapper;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;


    @LogAudit(action = "CREATE", entityName = "Employe")
    @Transactional
    public EmployeResponse createEmploye(CreateEmployeRequest request) {

        Poissonnerie poissonnerie = poissonnerieRepository.findById(request.getPoissonnerieId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + request.getPoissonnerieId()
                ));
        if (request.getTelephone() != null && employeRepository.existsByTelephone(request.getTelephone())) {
            throw new BusinessException("Un employé avec ce numéro existe déjà.");
        }
        User userLie = null;
        if (request.getUserId() != null) {
            userLie = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur lié non trouvé"));
        }

        Employe employe = employeMapper.toEntity(request);

        employe.setPoissonnerie(poissonnerie);
        employe.setActif(true);
        employe.setUser(userLie);

        Employe savedEmploye = employeRepository.save(employe);
        return employeMapper.toResponse(savedEmploye);
    }


    public List<EmployeResponse> getEmployesByPoissonnerie(Long poissonnerieId) {
        Poissonnerie poissonnerie = poissonnerieRepository.findById(poissonnerieId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Poissonnerie non trouvée avec l'id : " + poissonnerieId));
        return employeRepository.findByPoissonnerieId(poissonnerieId)
                .stream()
                .map(employeMapper::toResponse)
                .toList();
    }

    public EmployeResponse getEmployeById(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employé non trouvée avec l'id : " + employeId));

        return employeMapper.toResponse(employe);
    }

    @LogAudit(action = "UPDATE", entityName = "Employe")
    @Transactional
    public EmployeResponse updateEmploye(Long employeId, UpdateEmployeRequest request) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employé non trouvée avec l'id : " + employeId));

        if (request.getNom() != null && !request.getNom().trim().isBlank()) {
            employe.setNom(request.getNom().trim());
        }
        if (request.getPrenom() != null && !request.getPrenom().trim().isBlank()) {
            employe.setPrenom(request.getPrenom().trim());
        }
        if (request.getPoste() != null && !request.getPoste().trim().isBlank()) {
            employe.setPoste(request.getPoste().trim());
        }
        if (request.getSalaire() != null) {
            employe.setSalaire(request.getSalaire());
        }
        if (request.getTelephone() != null && !request.getTelephone().equals(employe.getTelephone())) {
            if (employeRepository.existsByTelephone(request.getTelephone())) {
                throw new BusinessException("Ce numéro de téléphone est déjà utilisé par un autre employé.");
            }
        }
        Employe savedEmploye = employeRepository.save(employe);
        return employeMapper.toResponse(savedEmploye);

    }

    @LogAudit(action = "DELETE", entityName = "Employe")
    @Transactional
    public void deleteEmploye(Long employeId) {
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employé non trouve avec l'id : " + employeId
                ));
        employe.setActif(false);
        employeRepository.save(employe);
    }

    public boolean existsByTelephoneAndPoissonnerie(String phone, Long poissonnerieId) {
        return poissonnerieRepository.findById(poissonnerieId)
                .map(p -> employeRepository.findByTelephone(phone).isPresent())
                .orElse(false);
    }

}
