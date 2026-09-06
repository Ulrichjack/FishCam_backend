package com.fishcam.application.gestion;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ResultatMensuelCalculatorTest {

    @Test
    void calculeLeResultatProvisoireDAoutAvecLesChargesConnues() {
        BigDecimal resultat = ResultatMensuelCalculator.calculerProvisoire(
                new BigDecimal("24396500"),
                new BigDecimal("22004500"),
                BigDecimal.ZERO,
                new BigDecimal("1226900")
        );

        assertThat(resultat).isEqualByComparingTo("1165100");
    }

    @Test
    void ajouteLesHaussesDeStockEtDeCreances() {
        BigDecimal resultat = ResultatMensuelCalculator.calculerValide(
                new BigDecimal("1000000"),
                new BigDecimal("200000"),
                new BigDecimal("280000"),
                new BigDecimal("100000"),
                new BigDecimal("250000")
        );

        assertThat(resultat).isEqualByComparingTo("1230000");
    }

    @Test
    void retireLesBaissesDeStockEtDeCreances() {
        BigDecimal resultat = ResultatMensuelCalculator.calculerValide(
                new BigDecimal("1000000"),
                new BigDecimal("300000"),
                new BigDecimal("200000"),
                new BigDecimal("250000"),
                new BigDecimal("200000")
        );

        assertThat(resultat).isEqualByComparingTo("850000");
    }
}
