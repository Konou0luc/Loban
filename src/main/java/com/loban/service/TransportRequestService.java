package com.loban.service;

import com.loban.domain.Offer;
import com.loban.domain.Role;
import com.loban.domain.RequestStatus;
import com.loban.domain.TransportRequest;
import com.loban.domain.User;
import com.loban.dto.CreateTransportRequestDto;
import com.loban.dto.OfferResponse;
import com.loban.dto.UpdateTransportRequestDto;
import com.loban.dto.TransporterSummaryResponse;
import com.loban.dto.TransportRequestResponse;
import com.loban.exception.ApiException;
import com.loban.repository.OfferRepository;
import com.loban.repository.RatingRepository;
import com.loban.repository.TransporterAvailabilitySlotRepository;
import com.loban.repository.TransportRequestRepository;
import com.loban.repository.UserRepository;
import com.loban.util.MoneyFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TransportRequestService {

    private final TransportRequestRepository transportRequestRepository;
    private final OfferRepository offerRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TransporterAvailabilitySlotRepository transporterAvailabilitySlotRepository;

    public TransportRequestService(
            TransportRequestRepository transportRequestRepository,
            OfferRepository offerRepository,
            RatingRepository ratingRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            UserService userService,
            TransporterAvailabilitySlotRepository transporterAvailabilitySlotRepository) {
        this.transportRequestRepository = transportRequestRepository;
        this.offerRepository = offerRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.transporterAvailabilitySlotRepository = transporterAvailabilitySlotRepository;
    }

    @Transactional
    public TransportRequestResponse create(CreateTransportRequestDto dto, Long clientId) {
        User client = userRepository.findById(clientId).orElseThrow();
        if (client.getRole() != Role.CLIENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seuls les clients peuvent créer une demande");
        }
        Instant ws = dto.desiredSlotStart();
        Instant we = dto.desiredSlotEnd();
        validateDesiredSlotWindow(ws, we);

        TransportRequest tr = TransportRequest.builder()
                .client(client)
                .pickupLocation(dto.pickupLocation())
                .destination(dto.destination())
                .parcelDescription(dto.parcelDescription())
                .desiredSlotStart(ws)
                .desiredSlotEnd(we)
                .status(RequestStatus.PENDING)
                .build();
        transportRequestRepository.save(tr);
        return map(tr);
    }

    @Transactional
    public TransportRequestResponse updateMine(Long requestId, Long clientId, UpdateTransportRequestDto dto) {
        TransportRequest r = transportRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!r.getClient().getId().equals(clientId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        if (r.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette demande ne peut plus être modifiée.");
        }
        if (offerRepository.countByRequest(r) > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Des offres ont déjà été reçues : la demande ne peut plus être modifiée.");
        }
        validateDesiredSlotWindow(dto.desiredSlotStart(), dto.desiredSlotEnd());
        r.setPickupLocation(dto.pickupLocation().trim());
        r.setDestination(dto.destination().trim());
        r.setParcelDescription(dto.parcelDescription().trim());
        r.setDesiredSlotStart(dto.desiredSlotStart());
        r.setDesiredSlotEnd(dto.desiredSlotEnd());
        transportRequestRepository.save(r);
        return map(r);
    }

    private void validateDesiredSlotWindow(Instant ws, Instant we) {
        boolean hasWs = ws != null;
        boolean hasWe = we != null;
        if (hasWs != hasWe) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Indiquez le début et la fin de la fenêtre souhaitée, ou laissez les deux champs vides.");
        }
        if (!hasWs) {
            return;
        }
        if (!ws.isBefore(we)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fin de la fenêtre doit être après le début.");
        }
        Duration d = Duration.between(ws, we);
        if (d.toMinutes() < 15) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fenêtre doit durer au moins 15 minutes.");
        }
        if (d.toDays() > 14) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fenêtre ne peut pas dépasser 14 jours.");
        }
        Instant now = Instant.now();
        if (we.isBefore(now)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La fenêtre souhaitée ne peut pas être entièrement passée.");
        }
    }

    @Transactional(readOnly = true)
    public List<TransportRequestResponse> listMine(Long clientId) {
        User client = userRepository.findById(clientId).orElseThrow();
        return transportRequestRepository.findByClientOrderByCreatedAtDesc(client).stream()
                .map(tr -> map(tr, ratingRepository.findByTransportRequestAndClient(tr, client).isPresent()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransportRequestResponse> listOpen(Long userId, Role role) {
        User transporterForFilter = null;
        if (role == Role.TRANSPORTER) {
            User u = userRepository.findById(userId).orElseThrow();
            if (!u.isTransporterProfileComplete()) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Complétez votre profil transporteur (photo et informations) pour accéder aux demandes.");
            }
            transporterForFilter = u;
        }
        List<TransportRequest> pending =
                transportRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
        final User tf = transporterForFilter;
        if (tf != null) {
            pending = pending.stream().filter(r -> visibleToTransporterForOpenMarket(r, tf)).toList();
        }
        return pending.stream().map(this::map).toList();
    }

    /** Sans fenêtre client → visible pour tous les transporteurs. Avec fenêtre → au moins un créneau qui chevauche. */
    private boolean visibleToTransporterForOpenMarket(TransportRequest r, User transporter) {
        if (r.getDesiredSlotStart() == null || r.getDesiredSlotEnd() == null) {
            return true;
        }
        long n = transporterAvailabilitySlotRepository.countOverlappingWindow(
                transporter.getId(), r.getDesiredSlotStart(), r.getDesiredSlotEnd());
        return n > 0;
    }

    @Transactional(readOnly = true)
    public List<TransportRequestResponse> listMyDeliveries(Long transporterId) {
        User t = userRepository.findById(transporterId).orElseThrow();
        if (t.getRole() != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Réservé aux transporteurs");
        }
        return transportRequestRepository.findByAcceptedOffer_TransporterOrderByCreatedAtDesc(t).stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransportRequestResponse getById(Long id, Long userId, Role role) {
        TransportRequest r = transportRequestRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        authorizeRead(r, userId, role);
        boolean clientHasRated = false;
        if (role == Role.CLIENT && r.getClient().getId().equals(userId)) {
            User client = userRepository.findById(userId).orElseThrow();
            clientHasRated = ratingRepository.findByTransportRequestAndClient(r, client).isPresent();
        }
        return map(r, clientHasRated);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> listOffersForComparison(Long requestId, Long userId) {
        TransportRequest r = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!r.getClient().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        return offerRepository.findByRequestOrderByPriceAsc(r).stream()
                .map(OfferResponse::from)
                .toList();
    }

    @Transactional
    public TransportRequestResponse selectOffer(Long requestId, Long offerId, Long clientId) {
        TransportRequest r = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!r.getClient().getId().equals(clientId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Action réservée au client");
        }
        if (r.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Une offre a déjà été retenue ou la demande est close");
        }
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offre introuvable"));
        if (!offer.getRequest().getId().equals(requestId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Offre incompatible avec cette demande");
        }
        r.setAcceptedOffer(offer);
        r.setStatus(RequestStatus.ACCEPTED);
        transportRequestRepository.save(r);

        notificationService.notifyUser(
                offer.getTransporter().getId(),
                "Offre acceptée",
                r.getClient().getFullname() + " a retenu votre offre à " + MoneyFormat.fcfa(offer.getPrice())
                        + " pour un transport vers " + r.getDestination() + ".",
                "OFFER_ACCEPTED");

        return map(r);
    }

    @Transactional
    public TransportRequestResponse advanceDelivery(Long requestId, Long transporterId) {
        TransportRequest r = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (r.getAcceptedOffer() == null || !r.getAcceptedOffer().getTransporter().getId().equals(transporterId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vous n'êtes pas le transporteur assigné");
        }
        RequestStatus next = switch (r.getStatus()) {
            case ACCEPTED -> RequestStatus.IN_PROGRESS;
            case IN_PROGRESS -> RequestStatus.DELIVERED;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Statut ne permet pas d'avancer la livraison");
        };
        r.setStatus(next);
        transportRequestRepository.save(r);

        notificationService.notifyUser(
                r.getClient().getId(),
                "Mise à jour de livraison",
                "Le statut de votre envoi vers " + r.getDestination() + " est maintenant : "
                        + statutLivraisonEnFrancais(next) + ".",
                "DELIVERY_STATUS");

        return map(r);
    }

    private void authorizeRead(TransportRequest r, Long userId, Role role) {
        if (r.getClient().getId().equals(userId)) {
            return;
        }
        if (role == Role.TRANSPORTER) {
            User viewer = userRepository.findById(userId).orElseThrow();
            if (!viewer.isTransporterProfileComplete() && r.getStatus() == RequestStatus.PENDING) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Complétez votre profil transporteur pour accéder aux demandes ouvertes.");
            }
            if (r.getStatus() == RequestStatus.PENDING) {
                return;
            }
            if (r.getAcceptedOffer() != null && r.getAcceptedOffer().getTransporter().getId().equals(userId)) {
                return;
            }
            boolean hasOffer = offerRepository.findByRequestOrderByPriceAsc(r).stream()
                    .anyMatch(o -> o.getTransporter().getId().equals(userId));
            if (hasOffer) {
                return;
            }
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
    }

    /** Texte affiché dans les notifications et messages — pas de constantes type IN_PROGRESS. */
    private static String statutLivraisonEnFrancais(RequestStatus s) {
        return switch (s) {
            case PENDING -> "en attente d'offres";
            case ACCEPTED -> "acceptée";
            case IN_PROGRESS -> "en cours";
            case DELIVERED -> "livrée";
        };
    }

    private TransportRequestResponse map(TransportRequest r) {
        return map(r, false);
    }

    private TransportRequestResponse map(TransportRequest r, boolean clientHasRated) {
        int count = (int) offerRepository.countByRequest(r);
        TransporterSummaryResponse assigned = null;
        if (r.getAcceptedOffer() != null) {
            assigned = userService.transporterPublicSummary(r.getAcceptedOffer().getTransporter());
        }
        return TransportRequestResponse.from(r, count, clientHasRated, assigned);
    }
}
