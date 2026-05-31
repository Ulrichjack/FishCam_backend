package com.fishcam.application.notification;

import com.fishcam.adapter.web.dto.response.NotificationResponse;
import com.fishcam.adapter.web.mapper.NotificationMapper;
import com.fishcam.domain.comptecourant.CompteCourant;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.TransactionCompteCourantRepository;
import com.fishcam.domain.comptecourant.TypeTransactionCC;
import com.fishcam.domain.notification.*;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final TransactionCompteCourantRepository transactionCompteCourantRepository;
    private final NotificationMapper notificationMapper;
    private final RapportJournalierRecordRepository rapportRecordRepository;

    private String fcfa(BigDecimal v) {
        if (v == null) return "0";
        return v.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }


    private User loadAndAuthorizeUser(Long requestedUserId, User currentUser) {
        if (!currentUser.getId().equals(requestedUserId) && !isSuperAdmin(currentUser)) {
            throw new AccessDeniedException("Accès refusé");
        }
        return userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotificationsPageByUser(
            Long requestedUserId,
            int page,
            int size,
            User currentUser
    ) {
        User user = loadAndAuthorizeUser(requestedUserId, currentUser);

        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(notificationMapper::toResponse);
    }

    public List<NotificationResponse> getRecentNotifications(
            Long requestedUserId,
            int limit,
            User currentUser
    ) {
        User user = loadAndAuthorizeUser(requestedUserId, currentUser);

        int safeLimit = Math.min(Math.max(limit, 1), 50); // 1..50
        Pageable pageable = PageRequest.of(0, safeLimit);

        return notificationRepository.findRecentByUser(user, pageable)
                .getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public int markAllAsRead(Long requestedUserId, User currentUser) {
        User user = loadAndAuthorizeUser(requestedUserId, currentUser);
        return notificationRepository.markAllAsReadByUser(user);
    }


    private List<User> recipientsForMoney(Poissonnerie p) {
        List<User> patrons = userRepository.findByDefaultPoissonnerie(p)
                .stream()
                .filter(u -> u.getRole() == Role.PATRON)
                .toList();

        List<User> superAdmins = userRepository.findByRole(Role.SUPER_ADMIN);

        // merge sans doublons
        return java.util.stream.Stream.concat(patrons.stream(), superAdmins.stream())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(User::getId, u -> u, (a, b) -> a),
                        m -> new java.util.ArrayList<>(m.values())
                ));
    }

    @Transactional
    public void createNotificationCompteSolde(CompteCourant compte) {
        String message = String.format(
                "Compte soldé%nClient : %s %s%nLe compte courant a été complètement soldé.",
                compte.getClient().getFirstName(),
                compte.getClient().getLastName()
        );

        for (User u : recipientsForMoney(compte.getPoissonnerie())) {
            Notification notif = new Notification();
            notif.setType(TypeNotification.COMPTE_SOLDE);
            notif.setMessage(message);
            notif.setUser(u);
            notif.setPoissonnerie(compte.getPoissonnerie());
            notif.setRead(false);
            notificationRepository.save(notif);
        }
    }




    @Transactional
    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée"));

        // ✅ Vérification ownership dans le service
        if (!notification.getUser().getId().equals(currentUser.getId()) && !isSuperAdmin(currentUser)) {
            throw new AccessDeniedException("Cette notification ne vous appartient pas");
        }

        notification.setRead(true);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long requestedUserId, User currentUser) {
        // ✅ Vérification IDOR dans le service
        if (!currentUser.getId().equals(requestedUserId) && !isSuperAdmin(currentUser)) {
            throw new AccessDeniedException("Accès refusé");
        }

        User user = userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return notificationRepository.countByUserAndReadFalse(user);
    }

    //  Helper privé centralisé
    private boolean isSuperAdmin(User user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }


    @Transactional
    public void createAlerteCompteCourant(CompteCourant compte, String typeAlerte) {
        List<User> usersToNotify = recipientsForMoney(compte.getPoissonnerie());
        if (usersToNotify.isEmpty()) {
            log.warn("⚠️ Aucun utilisateur à notifier pour poissonnerie {}", compte.getPoissonnerie().getId());
            return;
        }

        String message;
        TypeNotification type;

        if ("FRANCHISSEMENT_SEUIL".equals(typeAlerte)) {
            message = String.format(
                    "Alerte compte courant : Seuil franchi%n" +
                            "Client : %s %s%n" +
                            "Seuil : -5000 FCFA%n" +
                            "Solde actuel : %s FCFA",                    compte.getClient().getFirstName(),
                    compte.getClient().getLastName(),
                    fcfa(compte.getSolde()));
            type = TypeNotification.COMPTE_COURANT_ALERTE;
        } else {
            message = String.format(
                    "Alerte compte courant : Augmentation significative%n" +
                            "Client : %s %s%n" +
                            "Solde actuel : %s FCFA",                    compte.getClient().getFirstName(),
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
            notificationRepository.save(notification);
        }
    }


    @Transactional
    public void createRapportJournalier(Poissonnerie poissonnerie) {
        LocalDateTime debut = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fin = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        if (rapportRecordRepository.existsByPoissonnerieAndDateRapport(poissonnerie, today)) {
            log.warn("Rapport du {} déjà généré pour poissonnerie {}", today, poissonnerie.getId());
            return;
        }

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

        BigDecimal emprunts = totalEmprunts != null ? totalEmprunts : BigDecimal.ZERO;
        BigDecimal remboursements = totalRemboursements != null ? totalRemboursements : BigDecimal.ZERO;
        BigDecimal net = remboursements.subtract(emprunts);
        BigDecimal totalDettesActives = totalDettes != null ? totalDettes : BigDecimal.ZERO;

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRENCH);
        String dateFormatee = today.format(formatter);

        String message = String.format(
                "Rapport du %s%n%n" +
                        "Emprunts : %d transactions - %s FCFA%n" +
                        "Remboursements : %d transactions - %s FCFA%n" +
                        "Net (R - E) : %s FCFA%n%n" +
                        "Comptes en dette : %d clients%n" +
                        "Total dettes actives : %s FCFA",
                dateFormatee,
                nbEmprunts, fcfa(emprunts),
                nbRemboursements, fcfa(remboursements),
                fcfa(net),
                nbComptesEnDette,
                fcfa(totalDettesActives)
        );

        List<User> usersToNotify = userRepository.findByDefaultPoissonnerie(poissonnerie);

        for (User user : usersToNotify) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setPoissonnerie(poissonnerie);
            notification.setType(TypeNotification.RAPPORT_JOURNALIER);
            notification.setMessage(message);
            notification.setRead(false);
            notificationRepository.save(notification);
        }

        rapportRecordRepository.save(new RapportJournalierRecord(poissonnerie, today));
        log.info("✅ Rapport journalier clôturé pour poissonnerie {} - {}", poissonnerie.getId(), today);
    }

    @Transactional
    public void verifierJoursNonClotures(Poissonnerie poissonnerie) {

        // Vérifier les 7 derniers jours (évite de remonter trop loin)
        for (int i = 1; i <= 7; i++) {
            LocalDate jour = LocalDate.now().minusDays(i);
            boolean cloture = rapportRecordRepository
                    .existsByPoissonnerieAndDateRapport(poissonnerie, jour);

            if (!cloture) {
                // Créer une alerte pour ce jour manquant
                String message = String.format(
                        "Alerte : journée non clôturée%n%n" +
                                "Le rapport du %s n'a pas été généré.%n" +
                                "Le poste était peut-être éteint à l'heure prévue.%n" +
                                "Veuillez vérifier les transactions manuellement si nécessaire.",
                        jour
                );

                List<User> usersToNotify = userRepository.findByDefaultPoissonnerie(poissonnerie);
                for (User user : usersToNotify) {
                    // Éviter de créer la même alerte deux fois
                    boolean dejaNotifie = notificationRepository
                            .existsByUserAndMessageContainingAndType(user, jour.toString(), TypeNotification.RAPPORT_JOURNALIER);

                    if (!dejaNotifie) {
                        Notification notif = new Notification();
                        notif.setUser(user);
                        notif.setPoissonnerie(poissonnerie);
                        notif.setType(TypeNotification.RAPPORT_JOURNALIER);
                        notif.setMessage(message);
                        notif.setRead(false);
                        notificationRepository.save(notif);
                        log.warn("⚠️ Alerte journée non clôturée créée : {} - poissonnerie {}", jour, poissonnerie.getId());
                    }
                }
            }
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
            log.warn("⚠️ Aucun SUPER_ADMIN pour notifier la modification de limite");
            return;
        }

        BigDecimal augmentation = nouvelleLimit.subtract(ancienneLimit);

        String message = String.format(
                "Alerte : modification de limite de crédit%n%n" +
                        "Client : %s %s%n" +
                        "Ancienne limite : %s FCFA%n" +
                        "Nouvelle limite : %s FCFA%n" +
                        "Variation : %s FCFA%n%n" +
                        "Modifié par : %s %s (%s)",
                compte.getClient().getFirstName(),
                compte.getClient().getLastName(),
                fcfa(ancienneLimit),
                fcfa(nouvelleLimit),
                fcfa(augmentation),
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
            notificationRepository.save(notification);
        }

        log.info("✅ Notification envoyée à {} SUPER_ADMIN(s)", superAdmins.size());
    }
}
