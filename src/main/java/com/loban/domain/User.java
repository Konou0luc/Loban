package com.loban.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean transporterProfileComplete = true;

    @Column(columnDefinition = "TEXT")
    private String profilePhotoDataUrl;

    @Column(columnDefinition = "TEXT")
    private String transporterSecurityInfo;

    /** Ex. MOTO, VOITURE — voir TransporterProfileSupport.VEHICLE_TYPES */
    @Column(length = 40)
    private String vehicleType;

    @Column(length = 160)
    private String coverageArea;

    private Integer yearsExperience;

    /** JSON array de clés livraison — voir TransporterProfileSupport.DELIVERY_CATEGORIES */
    @Column(name = "delivery_categories", columnDefinition = "TEXT")
    private String deliveryCategoriesJson;

    @Column(length = 40)
    private String availability;

    @Column(length = 120)
    private String drivingLicenseNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean identityVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean licenseVerified = false;

    /** Badge « confirmé » manuel ou complété automatiquement côté logique métier si besoin */
    @Column(nullable = false)
    @Builder.Default
    private boolean transporterConfirmedBadge = false;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
