package com.loban.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AvailabilitySlotInput(@NotNull Instant startAt, @NotNull Instant endAt) {}
