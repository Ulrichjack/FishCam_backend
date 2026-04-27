package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.RecapitulatifResponse;
import com.fishcam.application.achat.AchatJournalierService;
import com.fishcam.application.epargne.EpargneService;
import com.fishcam.application.export.PdfExportService;
import com.fishcam.adapter.web.dto.response.FactureDetailResponse;
import com.fishcam.adapter.web.dto.response.EpargneDetailResponse;
import com.fishcam.application.poissonnerie.PoissonnerieService;
import com.fishcam.application.rapport.RecapitulatifService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@Tag(name = "Export PDF", description = "Génération et téléchargement des documents PDF")
public class ExportController {

    private final PdfExportService pdfExportService;
    private final AchatJournalierService achatJournalierService;
    private final EpargneService epargneService;
    private final RecapitulatifService recapitulatifService;
    private final PoissonnerieService poissonnerieService;

    @GetMapping("/factures/{id}/pdf")
    @Operation(summary = "Télécharger la facture en PDF")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<byte[]> exportFacture(@PathVariable Long id) {

        // 1. Get the data
        FactureDetailResponse facture = achatJournalierService.getFactureDetail(id);

        // 2. Generate the PDF byte array
        byte[] pdfBytes = pdfExportService.exportFactureToPdf(facture);

        // 3. Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "facture_" + id + ".pdf");

        // 4. Return the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/epargnes/{id}/pdf")
    @Operation(summary = "Télécharger l'Épargne en PDF")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ResponseEntity<byte[]> exportEpargne(@PathVariable Long id) {

        // 1. Get the data
        EpargneDetailResponse epargne = epargneService.getEpargneDetail(id);

        // 2. Generate the PDF byte array
        byte[] pdfBytes = pdfExportService.exportEpargneToPdf(epargne);

        // 3. Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "fiche_épargne_" + id + ".pdf");

        // 4. Return the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/recapitulatif/{id}/pdf")
    @Operation(summary = "Télécharger le Récapitulatif des ventes en PDF")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ResponseEntity<byte[]> exportRecapitulatif(
            @RequestParam Long poissonnerieId,
            @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE )LocalDate start,
            @RequestParam @DateTimeFormat (iso = DateTimeFormat.ISO.DATE )LocalDate end){

        // 1. Get the data
        RecapitulatifResponse recap = recapitulatifService.generateRecapitulatif(poissonnerieId, start, end);

        // 2. Get the shop name and format the date range
        String poissonnerieNom = poissonnerieService.getPoissonnerieById(poissonnerieId).getName();
        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periode = start.format(formatter) + " AU " + end.format(formatter);


        // 3. Generate the PDF byte array
        byte[] pdfBytes = pdfExportService.exportRecapitulatifToPdf(recap, poissonnerieNom, periode);

        // 4. Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "récapitulatif_" + start.toString() + ".pdf");

        // 5. Return the response
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

}