package com.loban.service;

import com.loban.domain.RequestStatus;
import com.loban.domain.Role;
import com.loban.domain.TransporterProfileSupport;
import com.loban.domain.User;
import com.loban.dto.CompleteTransporterProfileRequest;
import com.loban.dto.TransporterSummaryResponse;
import com.loban.dto.UpdateProfileRequest;
import com.loban.dto.UserResponse;
import com.loban.exception.ApiException;
import com.loban.repository.RatingRepository;
import com.loban.repository.TransportRequestRepository;
import com.loban.repository.UserRepository;
import com.loban.util.JsonListStrings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CloudinaryImageService cloudinaryImageService;
    private final TransportRequestRepository transportRequestRepository;
    private final RatingRepository ratingRepository;
    private final TransporterAvailabilityService transporterAvailabilityService;

    public UserService(
            UserRepository userRepository,
            CloudinaryImageService cloudinaryImageService,
            TransportRequestRepository transportRequestRepository,
            RatingRepository ratingRepository,
            TransporterAvailabilityService transporterAvailabilityService) {
        this.userRepository = userRepository;
        this.cloudinaryImageService = cloudinaryImageService;
        this.transportRequestRepository = transportRequestRepository;
        this.ratingRepository = ratingRepository;
        this.transporterAvailabilityService = transporterAvailabilityService;
    }

    public UserResponse buildUserResponse(User user) {
        if (user.getRole() != Role.TRANSPORTER) {
            return UserResponse.fromClient(user);
        }
        long del =
                transportRequestRepository.countByAcceptedOffer_TransporterAndStatus(user, RequestStatus.DELIVERED);
        double avg = ratingRepository.averageRatingForTransporter(user.getId()).orElse(0.0);
        var slots = transporterAvailabilityService.listForUserEntity(user);
        return UserResponse.fromTransporter(user, del, avg, slots);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        var user = userRepository.findById(userId).orElseThrow();
        return buildUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        var user = userRepository.findById(userId).orElseThrow();
        if (req.fullname() != null && !req.fullname().isBlank()) {
            user.setFullname(req.fullname().trim());
        }
        return buildUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public TransporterSummaryResponse transporterPublicSummary(User transporter) {
        long del =
                transportRequestRepository.countByAcceptedOffer_TransporterAndStatus(transporter, RequestStatus.DELIVERED);
        double avg = ratingRepository.averageRatingForTransporter(transporter.getId()).orElse(0.0);
        var upcoming = transporterAvailabilityService.listUpcomingForTransporter(transporter, 48);
        return TransporterSummaryResponse.from(transporter, del, avg, upcoming);
    }

    @Transactional
    public UserResponse completeTransporterProfile(Long userId, CompleteTransporterProfileRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getRole() != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Réservé aux transporteurs");
        }
        validateCategories(req.deliveryCategories());
        String photo = req.profilePhotoDataUrl().trim();
        String storedPhoto = resolveProfilePhotoStorage(photo, user.getId());
        user.setProfilePhotoDataUrl(storedPhoto);
        user.setTransporterSecurityInfo(req.transporterSecurityInfo().trim());
        user.setVehicleType(normalizeEnum(req.vehicleType(), TransporterProfileSupport.VEHICLE_TYPES, "type de véhicule"));
        user.setCoverageArea(req.coverageArea().trim());
        user.setYearsExperience(req.yearsExperience());
        user.setDeliveryCategoriesJson(JsonListStrings.toJson(normalizeCategories(req.deliveryCategories())));
        user.setAvailability(
                normalizeEnum(req.availability(), TransporterProfileSupport.AVAILABILITY_OPTIONS, "disponibilité"));
        user.setDrivingLicenseNumber(req.drivingLicenseNumber().trim());
        user.setTransporterProfileComplete(true);
        userRepository.save(user);
        return buildUserResponse(user);
    }

    private void validateCategories(List<String> categories) {
        var seen = new HashSet<String>();
        for (String c : categories) {
            String key = c.trim().toUpperCase(Locale.ROOT).replace('-', '_');
            if (!TransporterProfileSupport.DELIVERY_CATEGORIES.contains(key)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Catégorie de livraison non reconnue : " + c);
            }
            seen.add(key);
        }
        if (seen.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choisissez au moins une catégorie de livraison.");
        }
    }

    private List<String> normalizeCategories(List<String> raw) {
        return raw.stream()
                .map(s -> s.trim().toUpperCase(Locale.ROOT).replace('-', '_'))
                .distinct()
                .toList();
    }

    private String normalizeEnum(String value, java.util.Set<String> allowed, String label) {
        String v = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        if (!allowed.contains(v)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Valeur invalide pour " + label);
        }
        return v;
    }

    /**
     * Accepte une URL Cloudinary/https déjà connue, ou une data URL : upload vers Cloudinary si configuré,
     * sinon conservation du data URL (mode dev).
     */
    private String resolveProfilePhotoStorage(String photo, long userId) {
        if (photo.startsWith("https://")) {
            return photo;
        }
        if (!photo.startsWith("data:image/")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La photo doit être une image (data URL ou URL https).");
        }
        byte[] raw = CloudinaryImageService.decodeDataUrlBase64(photo);
        if (cloudinaryImageService.isConfigured()) {
            return cloudinaryImageService.uploadProfilePhoto(raw, userId);
        }
        return photo;
    }
}
