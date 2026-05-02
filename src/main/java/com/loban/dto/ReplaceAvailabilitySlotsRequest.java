package com.loban.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReplaceAvailabilitySlotsRequest(
        @NotNull @Size(max = 80) @Valid List<AvailabilitySlotInput> slots
) {}
