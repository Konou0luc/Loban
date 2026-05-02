package com.loban.dto;

import com.loban.domain.TransporterAvailabilitySlot;

public record AvailabilitySlotResponse(Long id, String startAt, String endAt) {
    public static AvailabilitySlotResponse from(TransporterAvailabilitySlot s) {
        return new AvailabilitySlotResponse(
                s.getId(), s.getStartAt().toString(), s.getEndAt().toString());
    }
}
