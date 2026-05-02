package com.loban.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 200)
        String fullname
) {
}
