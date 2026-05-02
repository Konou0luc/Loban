package com.loban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UpdateTransportRequestDto(
        @NotBlank @Size(max = 500) String pickupLocation,
        @NotBlank @Size(max = 500) String destination,
        @NotBlank @Size(max = 2000) String parcelDescription,
        Instant desiredSlotStart,
        Instant desiredSlotEnd
) {}
