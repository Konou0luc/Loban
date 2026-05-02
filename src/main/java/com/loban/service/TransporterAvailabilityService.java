package com.loban.service;

import com.loban.domain.Role;
import com.loban.domain.TransporterAvailabilitySlot;
import com.loban.domain.User;
import com.loban.dto.AvailabilitySlotInput;
import com.loban.dto.AvailabilitySlotResponse;
import com.loban.dto.ReplaceAvailabilitySlotsRequest;
import com.loban.exception.ApiException;
import com.loban.repository.TransporterAvailabilitySlotRepository;
import com.loban.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransporterAvailabilityService {

    private static final Duration MIN_DURATION = Duration.ofMinutes(15);
    private static final Duration MAX_DURATION = Duration.ofHours(24);
    private static final int MAX_SLOTS = 80;

    private final TransporterAvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;

    public TransporterAvailabilityService(
            TransporterAvailabilitySlotRepository slotRepository, UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listForUserEntity(User user) {
        return slotRepository.findByUserOrderByStartAtAsc(user).stream()
                .map(AvailabilitySlotResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listMine(Long userId) {
        User u = userRepository.findById(userId).orElseThrow();
        if (u.getRole() != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Réservé aux transporteurs");
        }
        return slotRepository.findByUserOrderByStartAtAsc(u).stream()
                .map(AvailabilitySlotResponse::from)
                .toList();
    }

    /** Créneaux à venir pour affichage client (résumé transporteur). */
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listUpcomingForTransporter(User transporter, int limit) {
        Instant now = Instant.now();
        return slotRepository.findUpcomingForUser(transporter, now).stream()
                .limit(limit)
                .map(AvailabilitySlotResponse::from)
                .toList();
    }

    @Transactional
    public List<AvailabilitySlotResponse> replaceMine(Long userId, ReplaceAvailabilitySlotsRequest req) {
        User u = userRepository.findById(userId).orElseThrow();
        if (u.getRole() != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Réservé aux transporteurs");
        }
        List<AvailabilitySlotInput> inputs = req.slots();
        if (inputs.size() > MAX_SLOTS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trop de créneaux (maximum " + MAX_SLOTS + ").");
        }
        Instant now = Instant.now();
        Instant horizon = now.plusSeconds(366L * 24 * 3600);
        List<TransporterAvailabilitySlot> toSave = new ArrayList<>();
        for (AvailabilitySlotInput in : inputs) {
            Instant start = in.startAt();
            Instant end = in.endAt();
            if (!start.isBefore(end)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Chaque créneau doit avoir une fin après le début.");
            }
            Duration d = Duration.between(start, end);
            if (d.compareTo(MIN_DURATION) < 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Créneau trop court (minimum 15 minutes).");
            }
            if (d.compareTo(MAX_DURATION) > 0) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Créneau trop long (maximum 24 heures).");
            }
            if (end.isBefore(now)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Les créneaux entièrement passés ne sont pas acceptés.");
            }
            if (start.isAfter(horizon)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Créneau trop lointain dans le futur.");
            }
            toSave.add(TransporterAvailabilitySlot.builder()
                    .user(u)
                    .startAt(start)
                    .endAt(end)
                    .build());
        }
        toSave.sort(Comparator.comparing(TransporterAvailabilitySlot::getStartAt));
        slotRepository.deleteAllByUserId(u.getId());
        slotRepository.flush();
        slotRepository.saveAll(toSave);
        return slotRepository.findByUserOrderByStartAtAsc(u).stream()
                .map(AvailabilitySlotResponse::from)
                .toList();
    }
}
