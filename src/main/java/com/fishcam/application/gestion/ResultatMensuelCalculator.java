package com.fishcam.application.gestion;

import java.math.BigDecimal;

public final class ResultatMensuelCalculator {

    private ResultatMensuelCalculator() {
    }

    public static BigDecimal calculerProvisoire(
            BigDecimal encaissements,
            BigDecimal achats,
            BigDecimal depensesJournalieres,
            BigDecimal chargesMensuelles) {
        return encaissements
                .subtract(achats)
                .subtract(depensesJournalieres)
                .subtract(chargesMensuelles);
    }

    public static BigDecimal calculerValide(
            BigDecimal resultatProvisoire,
            BigDecimal stockInitial,
            BigDecimal stockFinal,
            BigDecimal creancesInitiales,
            BigDecimal creancesFinales) {
        return resultatProvisoire
                .add(stockFinal.subtract(stockInitial))
                .add(creancesFinales.subtract(creancesInitiales));
    }
}
