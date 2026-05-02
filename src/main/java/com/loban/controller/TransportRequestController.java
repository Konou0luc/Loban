package com.loban.controller;

import com.loban.dto.CreateTransportRequestDto;
import com.loban.dto.OfferResponse;
import com.loban.dto.TransportRequestResponse;
import com.loban.dto.UpdateTransportRequestDto;
import com.loban.security.AppUserDetails;
import com.loban.service.OfferService;
import com.loban.service.TransportRequestService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class TransportRequestController {

    private final TransportRequestService transportRequestService;
    private final OfferService offerService;

    public TransportRequestController(
            TransportRequestService transportRequestService,
            OfferService offerService) {
        this.transportRequestService = transportRequestService;
        this.offerService = offerService;
    }

    @PostMapping
    public TransportRequestResponse create(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CreateTransportRequestDto dto) {
        return transportRequestService.create(dto, principal.getId());
    }

    @GetMapping("/mine")
    public List<TransportRequestResponse> mine(@AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.listMine(principal.getId());
    }

    @GetMapping("/open")
    public List<TransportRequestResponse> open(@AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.listOpen(principal.getId(), principal.getRole());
    }

    @GetMapping("/my-deliveries")
    public List<TransportRequestResponse> myDeliveries(@AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.listMyDeliveries(principal.getId());
    }

    @GetMapping("/{id}")
    public TransportRequestResponse getOne(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.getById(id, principal.getId(), principal.getRole());
    }

    @PatchMapping("/{id}")
    public TransportRequestResponse updateMine(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody UpdateTransportRequestDto dto) {
        return transportRequestService.updateMine(id, principal.getId(), dto);
    }

    @GetMapping("/{id}/offers")
    public List<OfferResponse> offers(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        return offerService.listForRequest(id, principal.getId(), principal.getRole());
    }

    @GetMapping("/{id}/compare")
    public List<OfferResponse> compare(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.listOffersForComparison(id, principal.getId());
    }

    @PostMapping("/{id}/select-offer/{offerId}")
    public TransportRequestResponse selectOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.selectOffer(id, offerId, principal.getId());
    }

    @PostMapping("/{id}/advance-delivery")
    public TransportRequestResponse advance(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        return transportRequestService.advanceDelivery(id, principal.getId());
    }
}
