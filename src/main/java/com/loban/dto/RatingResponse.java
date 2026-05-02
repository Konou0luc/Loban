package com.loban.dto;

import com.loban.domain.Rating;

public record RatingResponse(
        Long id,
        Long clientId,
        String clientName,
        Long transporterId,
        Long transportRequestId,
        Integer rating,
        String comment
) {
    public static RatingResponse from(Rating r) {
        return new RatingResponse(
                r.getId(),
                r.getClient().getId(),
                r.getClient().getFullname(),
                r.getTransporter().getId(),
                r.getTransportRequest().getId(),
                r.getRating(),
                r.getComment()
        );
    }
}
