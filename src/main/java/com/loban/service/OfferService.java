package com.loban.service;

import com.loban.domain.Offer;
import com.loban.domain.Role;
import com.loban.domain.TransportRequest;
import com.loban.domain.User;
import com.loban.dto.CreateOfferDto;
import com.loban.dto.OfferResponse;
import com.loban.exception.ApiException;
import com.loban.domain.RequestStatus;
import com.loban.repository.OfferRepository;
import com.loban.repository.TransportRequestRepository;
import com.loban.repository.UserRepository;
import com.loban.util.MoneyFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public OfferService(
            OfferRepository offerRepository,
            TransportRequestRepository transportRequestRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.offerRepository = offerRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public OfferResponse create(Long requestId, CreateOfferDto dto, Long transporterId) {
        User transporter = userRepository.findById(transporterId).orElseThrow();
        if (transporter.getRole() != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seuls les transporteurs peuvent soumettre une offre");
        }
        if (!transporter.isTransporterProfileComplete()) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Complétez votre profil transporteur avant de répondre aux demandes.");
        }
        TransportRequest request = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cette demande n'accepte plus d'offres");
        }
        if (offerRepository.existsByRequestAndTransporter(request, transporter)) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous avez déjà soumis une offre pour cette demande");
        }
        Offer offer = Offer.builder()
                .request(request)
                .transporter(transporter)
                .price(dto.price())
                .build();
        offerRepository.save(offer);

        notificationService.notifyUser(
                request.getClient().getId(),
                "Nouvelle offre",
                transporter.getFullname() + " propose " + MoneyFormat.fcfa(offer.getPrice()) + " pour votre envoi.",
                "OFFER_CREATED");

        return OfferResponse.from(offer);
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> listForRequest(Long requestId, Long userId, Role role) {
        TransportRequest request = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        authorizeViewOffers(request, userId, role);

        List<Offer> offers = offerRepository.findByRequestOrderByPriceAsc(request);
        if (role == Role.CLIENT && request.getClient().getId().equals(userId)) {
            return offers.stream().map(OfferResponse::from).toList();
        }
        if (role == Role.TRANSPORTER) {
            return offers.stream()
                    .filter(o -> o.getTransporter().getId().equals(userId))
                    .map(OfferResponse::from)
                    .toList();
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> listMine(Long transporterId) {
        User transporter = userRepository.findById(transporterId).orElseThrow();
        return offerRepository.findByTransporterOrderByCreatedAtDesc(transporter).stream()
                .map(OfferResponse::from)
                .toList();
    }

    private void authorizeViewOffers(TransportRequest request, Long userId, Role role) {
        if (role == Role.CLIENT && request.getClient().getId().equals(userId)) {
            return;
        }
        if (role == Role.TRANSPORTER) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
    }
}
