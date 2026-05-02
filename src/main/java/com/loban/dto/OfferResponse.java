package com.loban.dto;

import com.loban.domain.Offer;

import java.math.BigDecimal;

public record OfferResponse(
        Long id,
        Long requestId,
        Long transporterId,
        String transporterName,
        BigDecimal price,
        String createdAt
) {
    public static OfferResponse from(Offer o) {
        return new OfferResponse(
                o.getId(),
                o.getRequest().getId(),
                o.getTransporter().getId(),
                o.getTransporter().getFullname(),
                o.getPrice(),
                o.getCreatedAt().toString()
        );
    }
}
