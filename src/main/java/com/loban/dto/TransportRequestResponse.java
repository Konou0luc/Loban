package com.loban.dto;

import com.loban.domain.RequestStatus;
import com.loban.domain.TransportRequest;

public record TransportRequestResponse(
        Long id,
        Long clientId,
        String clientName,
        String pickupLocation,
        String destination,
        String parcelDescription,
        /** ISO-8601 ou null si le client n’a pas précisé de fenêtre. */
        String desiredSlotStart,
        String desiredSlotEnd,
        RequestStatus status,
        Long acceptedOfferId,
        String createdAt,
        int offerCount,
        /** Vrai si le client connecté (propriétaire) a déjà laissé un avis pour cette demande. Sinon faux. */
        boolean clientHasRated,
        /** Transporteur assigné (dès qu'une offre est acceptée). Null tant que la demande est ouverte. */
        TransporterSummaryResponse assignedTransporter
) {
    public static TransportRequestResponse from(
            TransportRequest r,
            int offerCount,
            boolean clientHasRated,
            TransporterSummaryResponse assignedTransporter) {
        return new TransportRequestResponse(
                r.getId(),
                r.getClient().getId(),
                r.getClient().getFullname(),
                r.getPickupLocation(),
                r.getDestination(),
                r.getParcelDescription(),
                r.getDesiredSlotStart() != null ? r.getDesiredSlotStart().toString() : null,
                r.getDesiredSlotEnd() != null ? r.getDesiredSlotEnd().toString() : null,
                r.getStatus(),
                r.getAcceptedOffer() != null ? r.getAcceptedOffer().getId() : null,
                r.getCreatedAt().toString(),
                offerCount,
                clientHasRated,
                assignedTransporter
        );
    }
}
