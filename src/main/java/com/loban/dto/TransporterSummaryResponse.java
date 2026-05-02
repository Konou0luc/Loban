package com.loban.dto;

import com.loban.domain.User;
import com.loban.util.JsonListStrings;

import java.util.List;

public record TransporterSummaryResponse(
        Long id,
        String fullname,
        String profilePhotoDataUrl,
        String transporterSecurityInfo,
        String vehicleType,
        String coverageArea,
        Integer yearsExperience,
        List<String> deliveryCategories,
        String availability,
        boolean identityVerified,
        boolean licenseVerified,
        boolean transporterConfirmed,
        long totalDeliveriesCompleted,
        double averageRating,
        List<AvailabilitySlotResponse> availabilitySlots
) {
    public static TransporterSummaryResponse from(
            User transporter,
            long deliveriesDone,
            double avgRating,
            List<AvailabilitySlotResponse> availabilitySlots) {
        List<String> cats = JsonListStrings.parse(transporter.getDeliveryCategoriesJson());
        boolean complete = transporter.isTransporterProfileComplete();
        boolean confirmed =
                transporter.isTransporterConfirmedBadge() || (complete && deliveriesDone >= 1);
        double avg = Double.isNaN(avgRating) ? 0.0 : Math.round(avgRating * 10.0) / 10.0;
        return new TransporterSummaryResponse(
                transporter.getId(),
                transporter.getFullname(),
                transporter.getProfilePhotoDataUrl(),
                transporter.getTransporterSecurityInfo() != null ? transporter.getTransporterSecurityInfo() : "",
                nz(transporter.getVehicleType()),
                nz(transporter.getCoverageArea()),
                transporter.getYearsExperience(),
                cats,
                nz(transporter.getAvailability()),
                transporter.isIdentityVerified(),
                transporter.isLicenseVerified(),
                confirmed,
                deliveriesDone,
                avg,
                availabilitySlots != null ? availabilitySlots : List.of());
    }

    private static String nz(String s) {
        return s != null ? s : "";
    }
}
