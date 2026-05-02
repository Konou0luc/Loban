package com.loban.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompleteTransporterProfileRequest(
        @NotBlank @Size(max = 700_000) String profilePhotoDataUrl,
        @NotBlank @Size(min = 20, max = 8000) String transporterSecurityInfo,
        @NotBlank @Size(max = 40) String vehicleType,
        @NotBlank @Size(max = 160) String coverageArea,
        @NotNull @Min(0) @Max(80) Integer yearsExperience,
        @NotEmpty List<@NotBlank @Size(max = 48) String> deliveryCategories,
        @NotBlank @Size(max = 48) String availability,
        @NotBlank @Size(min = 4, max = 120) String drivingLicenseNumber
) {}
