package com.loban.controller;

import com.loban.dto.AvailabilitySlotResponse;
import com.loban.dto.CompleteTransporterProfileRequest;
import com.loban.dto.ReplaceAvailabilitySlotsRequest;
import com.loban.dto.UpdateProfileRequest;
import com.loban.dto.UserResponse;
import com.loban.security.AppUserDetails;
import com.loban.service.TransporterAvailabilityService;
import com.loban.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final TransporterAvailabilityService transporterAvailabilityService;

    public UserController(UserService userService, TransporterAvailabilityService transporterAvailabilityService) {
        this.userService = userService;
        this.transporterAvailabilityService = transporterAvailabilityService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AppUserDetails principal) {
        return userService.getProfile(principal.getId());
    }

    @PatchMapping("/me")
    public UserResponse patchMe(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }

    @PostMapping("/me/transporter-profile")
    public UserResponse completeTransporterProfile(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody CompleteTransporterProfileRequest request) {
        return userService.completeTransporterProfile(principal.getId(), request);
    }

    @GetMapping("/me/availability-slots")
    public List<AvailabilitySlotResponse> listAvailabilitySlots(@AuthenticationPrincipal AppUserDetails principal) {
        return transporterAvailabilityService.listMine(principal.getId());
    }

    @PutMapping("/me/availability-slots")
    public List<AvailabilitySlotResponse> replaceAvailabilitySlots(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody ReplaceAvailabilitySlotsRequest request) {
        return transporterAvailabilityService.replaceMine(principal.getId(), request);
    }
}
