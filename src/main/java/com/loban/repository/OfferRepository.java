package com.loban.repository;

import com.loban.domain.Offer;
import com.loban.domain.TransportRequest;
import com.loban.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    @EntityGraph(attributePaths = {"transporter", "request"})
    List<Offer> findByRequestOrderByPriceAsc(TransportRequest request);

    @EntityGraph(attributePaths = {"transporter", "request"})
    List<Offer> findByTransporterOrderByCreatedAtDesc(User transporter);

    Optional<Offer> findByRequestAndTransporter(TransportRequest request, User transporter);

    boolean existsByRequestAndTransporter(TransportRequest request, User transporter);

    long countByRequest(TransportRequest request);
}
