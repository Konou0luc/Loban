package com.loban.service;

import com.loban.domain.Rating;
import com.loban.domain.Role;
import com.loban.domain.RequestStatus;
import com.loban.dto.CreateRatingDto;
import com.loban.dto.RatingResponse;
import com.loban.exception.ApiException;
import com.loban.repository.RatingRepository;
import com.loban.repository.TransportRequestRepository;
import com.loban.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TransportRequestRepository transportRequestRepository;
    private final UserRepository userRepository;

    public RatingService(
            RatingRepository ratingRepository,
            TransportRequestRepository transportRequestRepository,
            UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.transportRequestRepository = transportRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse create(Long requestId, CreateRatingDto dto, Long clientId) {
        var request = transportRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Demande introuvable"));
        if (!request.getClient().getId().equals(clientId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Seul le client peut noter ce transport");
        }
        if (request.getStatus() != RequestStatus.DELIVERED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Le colis doit être livré avant notation");
        }
        if (request.getAcceptedOffer() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Aucune offre acceptée");
        }
        var client = userRepository.findById(clientId).orElseThrow();
        var transporter = request.getAcceptedOffer().getTransporter();
        if (ratingRepository.findByTransportRequestAndClient(request, client).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Vous avez déjà noté ce transport");
        }
        Rating rating = Rating.builder()
                .client(client)
                .transporter(transporter)
                .transportRequest(request)
                .rating(dto.rating())
                .comment(dto.comment())
                .build();
        ratingRepository.save(rating);
        return RatingResponse.from(rating);
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> listForTransporter(Long transporterId, Long viewerId, Role role) {
        if (!transporterId.equals(viewerId) || role != Role.TRANSPORTER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé");
        }
        var transporter = userRepository.findById(transporterId).orElseThrow();
        return ratingRepository.findByTransporterOrderByIdDesc(transporter).stream()
                .map(RatingResponse::from)
                .toList();
    }
}
