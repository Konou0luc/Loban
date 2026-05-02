package com.loban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTransportRequestDto(
        @NotBlank @Size(max = 500)
        String pickupLocation,

        @NotBlank @Size(max = 500)
        String destination,

        @NotBlank @Size(max = 2000)
        String parcelDescription,

        /** Début de la fenêtre souhaitée (avec {@link #desiredSlotEnd}), ou les deux absents. */
        Instant desiredSlotStart,

        Instant desiredSlotEnd
) {
}
