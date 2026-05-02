package com.loban.dto;

import com.loban.domain.Role;
import com.loban.domain.User;
import com.loban.util.JsonListStrings;

import java.util.List;

public record UserResponse(
        Long id,
        String fullname,
        String email,
        Role role,
        String createdAt,
        /** Pour les clients : toujours true. Pour les transporteurs : profil onboarding terminé. */
        boolean transporterProfileComplete,
        /** Transporteur — photo (URL ou data). */
        String profilePhotoDataUrl,
        /** Bio / présentation affichée aux clients. */
        String transporterSecurityInfo,
        String vehicleType,
        String coverageArea,
        Integer yearsExperience,
        List<String> deliveryCategories,
        String availability,
        /** Numéro de permis — renvoyé uniquement au propriétaire du compte. */
        String drivingLicenseNumber,
        boolean identityVerified,
        boolean licenseVerified,
        /** Badge « confirmé » : stocké ou accordé si livraisons terminées. */
        boolean transporterConfirmed,
        long totalDeliveriesCompleted,
        double averageRating,
        List<AvailabilitySlotResponse> availabilitySlots
) {
    public static UserResponse fromClient(User user) {
        boolean complete = user.getRole() != Role.TRANSPORTER || user.isTransporterProfileComplete();
        return new UserResponse(
                user.getId(),
                user.getFullname(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt().toString(),
                complete,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                null,
                false,
                false,
                false,
                0,
                0.0,
                List.of());
    }

    public static UserResponse fromTransporter(
            User user,
            long totalDeliveriesCompleted,
            double averageRating,
            List<AvailabilitySlotResponse> availabilitySlots) {
        boolean complete = user.getRole() != Role.TRANSPORTER || user.isTransporterProfileComplete();
        List<String> cats = JsonListStrings.parse(user.getDeliveryCategoriesJson());
        boolean confirmed =
                user.isTransporterConfirmedBadge() || (complete && totalDeliveriesCompleted >= 1);
        return new UserResponse(
                user.getId(),
                user.getFullname(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt().toString(),
                complete,
                user.getProfilePhotoDataUrl(),
                user.getTransporterSecurityInfo(),
                user.getVehicleType(),
                user.getCoverageArea(),
                user.getYearsExperience(),
                cats,
                user.getAvailability(),
                user.getDrivingLicenseNumber(),
                user.isIdentityVerified(),
                user.isLicenseVerified(),
                confirmed,
                totalDeliveriesCompleted,
                round1(averageRating),
                availabilitySlots != null ? availabilitySlots : List.of());
    }

    private static double round1(double v) {
        if (Double.isNaN(v)) {
            return 0.0;
        }
        return Math.round(v * 10.0) / 10.0;
    }
}
