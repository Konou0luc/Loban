package com.loban.controller;

import com.loban.dto.CreateRatingDto;
import com.loban.dto.RatingResponse;
import com.loban.security.AppUserDetails;
import com.loban.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/requests/{requestId}/ratings")
    public RatingResponse rate(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CreateRatingDto dto) {
        return ratingService.create(requestId, dto, principal.getId());
    }

    @GetMapping("/ratings/mine")
    public List<RatingResponse> myRatings(@AuthenticationPrincipal AppUserDetails principal) {
        return ratingService.listForTransporter(principal.getId(), principal.getId(), principal.getRole());
    }
}
