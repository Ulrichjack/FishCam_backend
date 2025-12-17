package com.fishcam.application.notification;

import com.fishcam.adapter.web.dto.response.NotificationResponse;
import com.fishcam.adapter.web.mapper.NotificationMapper;
import com.fishcam.domain.comptecourant.CompteCourant;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.notification.Notification;
import com.fishcam.domain.notification.NotificationRepository;
import com.fishcam.domain.notification.TypeNotification;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final TransactionCompteCourantRepository transactionCompteCourantRepository;
    private final NotificationMapper notificationMapper;

    public List<NotificationResponse> getNotificationsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));

        notification.setRead(true);
    }

    public long countUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void createAlerteCompteCourant(CompteCourant compte, String typeAlerte) {
        List<User> usersToNotify = userRepository.findByDefaultPoissonnerie(compte.getPoissonnerie());

        if (usersToNotify.isEmpty()) {
            System.err.println("⚠️ Aucun utilisateur à notifier pour poissonnerie " + compte.getPoissonnerie().getId());
            return;
        }

        String message;
        TypeNotification type;

        if ("FRANCHISSEMENT_SEUIL".equals(typeAlerte)) {
            message = String.format("🔴 %s %s a franchi le seuil de -5000 FCFA (solde: %s FCFA)",
                    compte.getClient().getFirstName(),
                    compte.getClient().getLastName(),
                    compte.getSolde());
            type = TypeNotification.COMPTE_COURANT_ALERTE;
        } else {
            message = String.format("⚠️ %s %s a augmenté sa dette de façon significative (solde: %s FCFA)",
                    compte.getClient().getFirstName(),
                    compte.getClient().getLastName(),
                    compte.getSolde());
            type = TypeNotification.COMPTE_COURANT_ALERTE;
        }

        for (User user : usersToNotify) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setPoissonnerie(compte.getPoissonnerie());
            notification.setType(type);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void createNotificationCompteSolde(CompteCourant compte, User user) {
        Notification notif = new Notification();
        notif.setType(TypeNotification.COMPTE_SOLDE);
        notif.setMessage("✅ Bravo ! Le compte de " +
                compte.getClient().getFirstName() + " " + compte.getClient().getLastName() +
                " a été complètement soldé !");
        notif.setUser(user);
        notif.setPoissonnerie(compte.getPoissonnerie());
        notif.setRead(false);
        notificationRepository.save(notif);
    }

    @Transactional
    public void createRapportJournalier(Poissonnerie poissonnerie) {
        LocalDateTime debut = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = LocalDateTime.now();

        Long nbEmprunts = transactionCompteCourantRepository.countByPoissonnerieAndTypeAndPeriod(
                poissonnerie, TypeTransactionCC.EMPRUNT, debut, fin);

        BigDecimal totalEmprunts = transactionCompteCourantRepository.sumMontantByPoissonnerieAndTypeAndPeriod(
                poissonnerie, TypeTransactionCC.EMPRUNT, debut, fin);

        Long nbRemboursements = transactionCompteCourantRepository.countByPoissonnerieAndTypeAndPeriod(
                poissonnerie, TypeTransactionCC.REMBOURSEMENT, debut, fin);

        BigDecimal totalRemboursements = transactionCompteCourantRepository.sumMontantByPoissonnerieAndTypeAndPeriod(
                poissonnerie, TypeTransactionCC.REMBOURSEMENT, debut, fin);

        Long nbComptesEnDette = compteCourantRepository.countComptesEnDette(poissonnerie);
        BigDecimal totalDettes = compteCourantRepository.sumTotalDettes(poissonnerie);

        String message = String.format(
                "📊 Rapport du %s\n\n" +
                        "💰 Emprunts : %d transactions - %s FCFA\n" +
                        "💵 Remboursements : %d transactions - %s FCFA\n" +
                        "📉 Solde net : %s FCFA\n\n" +
                        "⚠️ Comptes en dette : %d clients\n" +
                        "💳 Total dettes actives : %s FCFA",
                LocalDateTime.now().toLocalDate(),
                nbEmprunts, totalEmprunts != null ? totalEmprunts : BigDecimal.ZERO,
                nbRemboursements, totalRemboursements != null ? totalRemboursements : BigDecimal.ZERO,
                (totalRemboursements != null ? totalRemboursements : BigDecimal.ZERO)
                        .subtract(totalEmprunts != null ? totalEmprunts : BigDecimal.ZERO),
                nbComptesEnDette,
                totalDettes
        );

        List<User> usersToNotify = userRepository.findByDefaultPoissonnerie(poissonnerie);

        for (User user : usersToNotify) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setPoissonnerie(poissonnerie);
            notification.setType(TypeNotification.RAPPORT_JOURNALIER);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }


    @Transactional
    public void createAlerteModificationLimite(
            CompteCourant compte,
            BigDecimal ancienneLimit,
            BigDecimal nouvelleLimit,
            User modifiePar) {

        // Récupérer tous les SUPER_ADMIN
        List<User> superAdmins = userRepository.findByRole(Role.SUPER_ADMIN);

        if (superAdmins.isEmpty()) {
            System.err.println("⚠️ Aucun SUPER_ADMIN pour notifier la modification de limite");
            return;
        }

        BigDecimal augmentation = nouvelleLimit.subtract(ancienneLimit);

        String message = String.format(
                "⚠️ MODIFICATION LIMITE IMPORTANTE\n\n" +
                        "Client : %s %s\n" +
                        "Ancienne limite : %s FCFA\n" +
                        "Nouvelle limite : %s FCFA\n" +
                        "Augmentation : +%s FCFA\n\n" +
                        "Modifié par : %s %s (%s)",
                compte.getClient().getFirstName(),
                compte.getClient().getLastName(),
                ancienneLimit,
                nouvelleLimit,
                augmentation,
                modifiePar.getFirstName(),
                modifiePar.getLastName(),
                modifiePar.getRole()
        );

        for (User admin : superAdmins) {
            Notification notification = new Notification();
            notification.setUser(admin);
            notification.setPoissonnerie(compte.getPoissonnerie());
            notification.setType(TypeNotification.INFO);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        System.out.println("✅ Notification envoyée à " + superAdmins.size() + " SUPER_ADMIN(s)");
    }
}