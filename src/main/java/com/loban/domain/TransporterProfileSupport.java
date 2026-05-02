package com.loban.domain;

import java.util.Set;

public final class TransporterProfileSupport {

    public static final Set<String> VEHICLE_TYPES =
            Set.of("MOTO", "TRICYCLE", "VOITURE", "CAMIONNETTE", "CAMION");

    public static final Set<String> AVAILABILITY_OPTIONS =
            Set.of("DISPONIBLE_AUJOURDHUI", "TEMPS_PLEIN", "WEEKEND", "SUR_RESERVATION");

    public static final Set<String> DELIVERY_CATEGORIES = Set.of(
            "PETITS_COLIS",
            "DOCUMENTS",
            "MARCHANDISES",
            "ELECTROMENAGER",
            "LIVRAISON_EXPRESS");

    private TransporterProfileSupport() {}
}
