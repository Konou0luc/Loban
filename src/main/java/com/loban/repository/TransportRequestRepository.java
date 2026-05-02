package com.loban.repository;

import com.loban.domain.RequestStatus;
import com.loban.domain.TransportRequest;
import com.loban.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportRequestRepository extends JpaRepository<TransportRequest, Long> {

    @EntityGraph(attributePaths = {"client", "acceptedOffer", "acceptedOffer.transporter"})
    @Override
    Optional<TransportRequest> findById(Long id);

    @EntityGraph(attributePaths = {"client", "acceptedOffer"})
    List<TransportRequest> findByClientOrderByCreatedAtDesc(User client);

    @EntityGraph(attributePaths = {"client", "acceptedOffer"})
    List<TransportRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    @EntityGraph(attributePaths = {"client", "acceptedOffer", "acceptedOffer.transporter"})
    List<TransportRequest> findByAcceptedOffer_TransporterOrderByCreatedAtDesc(User transporter);

    long countByAcceptedOffer_TransporterAndStatus(User transporter, RequestStatus status);
}
