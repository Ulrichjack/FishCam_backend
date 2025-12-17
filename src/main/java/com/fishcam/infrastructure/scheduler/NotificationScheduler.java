package com.fishcam.infrastructure.scheduler;

import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final PoissonnerieRepository poissonnerieRepository;

    @Scheduled(cron = "0 0 19 * * *")
    public void generateDailyReport() {
        List<Poissonnerie> poissonneries = poissonnerieRepository.findByActiveTrue();

        for (Poissonnerie poissonnerie : poissonneries) {
            notificationService.createRapportJournalier(poissonnerie);
        }
    }
}