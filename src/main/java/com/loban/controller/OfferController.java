package com.loban.controller;

import com.loban.dto.CreateOfferDto;
import com.loban.dto.OfferResponse;
import com.loban.security.AppUserDetails;
import com.loban.service.OfferService;
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
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping("/requests/{requestId}/offers")
    public OfferResponse create(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CreateOfferDto dto) {
        return offerService.create(requestId, dto, principal.getId());
    }

    @GetMapping("/offers/mine")
    public List<OfferResponse> mine(@AuthenticationPrincipal AppUserDetails principal) {
        return offerService.listMine(principal.getId());
    }
}
