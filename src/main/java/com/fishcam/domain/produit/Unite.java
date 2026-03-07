package com.fishcam.domain.produit;

public enum Unite {
    KG("Kilogramme");
//    CARTON("Carton"),
//    PIECE("Pièce");

    private final String label;

    Unite(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
