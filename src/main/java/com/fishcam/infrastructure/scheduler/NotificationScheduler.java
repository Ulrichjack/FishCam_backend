package com.fishcam.infrastructure.scheduler;

import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final PoissonnerieRepository poissonnerieRepository;

    // ✅ 1. Se déclenche chaque soir à 19h si le PC est allumé
    @Scheduled(cron = "0 0 19 * * *")
    // Pour tester : @Scheduled(fixedRate = 30000)
    public void generateDailyReport() {
        log.info("⏰ Génération automatique des rapports journaliers (19h)");
        List<Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();

        for (Poissonnerie poissonnerie : poissonneries) {
            notificationService.createRapportJournalier(poissonnerie);
        }
    }




    // ✅ 2. Se déclenche au démarrage → vérifie les jours manquants
    @EventListener(ApplicationReadyEvent.class)
    public void checkMissedReportsOnStartup() {
        log.info(" Vérification des journées non clôturées au démarrage...");
        List<Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();

        for (Poissonnerie poissonnerie : poissonneries) {
            notificationService.verifierJoursNonClotures(poissonnerie);
        }

        log.info("✅ Vérification terminée");
    }
}