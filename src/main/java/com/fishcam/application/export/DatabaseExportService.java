package com.fishcam.application.export;

import com.fishcam.domain.achat.AchatJournalierRepository;
import com.fishcam.domain.achat.LigneAchatRepository;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.cloture.ClotureJournaliereRepository;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.employe.EmployeRepository;
import com.fishcam.domain.epargne.EpargneRepository;
import com.fishcam.domain.epargne.TransactionEpargneRepository;
import com.fishcam.domain.livreur.EvaluationLivreurRepository;
import com.fishcam.domain.livreur.LivreurRepository;
import com.fishcam.domain.notification.NotificationRepository;
import com.fishcam.domain.notification.RapportJournalierRecordRepository;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.produit.ProduitRepository;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DatabaseExportService {

    private final UserRepository userRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final ClientRepository clientRepository;
    private final AchatJournalierRepository achatJournalierRepository;
    private final EmployeRepository employeRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final EpargneRepository epargneRepository;
    private final LigneAchatRepository ligneAchatRepository;
    private final ClotureJournaliereRepository clotureJournaliereRepository;
    private final TransactionCompteCourantRepository transactionCompteCourantRepository;
    private final TransactionEpargneRepository transactionEpargneRepository;
    private final EvaluationLivreurRepository evaluationLivreurRepository;
    private final LivreurRepository livreurRepository;
    private final NotificationRepository notificationRepository;
    private final RapportJournalierRecordRepository rapportJournalierRecordRepository;
    private final BackupRecordRepository backupRecordRepository;

    public Map<String, Object> exportAllData() {
        Map<String, Object> database = new HashMap<>();

        database.put("users", userRepository.findAll());
        database.put("poissonneries", poissonnerieRepository.findAll());
        database.put("produits", produitRepository.findAll());
        database.put("fournisseurs", fournisseurRepository.findAll());
        database.put("clients", clientRepository.findAll());
        database.put("achats", achatJournalierRepository.findAll());
        database.put("employes", employeRepository.findAll());
        database.put("compte_courants", compteCourantRepository.findAll());
        database.put("epargnes", epargneRepository.findAll());
        database.put("lignes_achats", ligneAchatRepository.findAll());
        database.put("clotures", clotureJournaliereRepository.findAll());
        database.put("transaction_compte_courants",transactionCompteCourantRepository.findAll());
        database.put("transaction_epargnes", transactionEpargneRepository.findAll());
        database.put("evaluation_livreur", evaluationLivreurRepository.findAll());
        database.put("livreurs", livreurRepository.findAll());
        database.put("notifications", notificationRepository.findAll());
        database.put("rapports", rapportJournalierRecordRepository.findAll());
        database.put("backup_record", backupRecordRepository.findAll());

        return database;
    }
}