package com.loban.repository;

import com.loban.domain.Rating;
import com.loban.domain.TransportRequest;
import com.loban.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByTransportRequestAndClient(TransportRequest request, User client);

    List<Rating> findByTransporterOrderByIdDesc(User transporter);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.transporter.id = :tid")
    Optional<Double> averageRatingForTransporter(@Param("tid") Long transporterId);
}
