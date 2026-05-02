package com.loban.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOfferDto(
        @NotNull
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
        BigDecimal price
) {
}
