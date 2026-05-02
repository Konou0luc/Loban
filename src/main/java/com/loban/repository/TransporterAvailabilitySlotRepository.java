package com.loban.repository;

import com.loban.domain.TransporterAvailabilitySlot;
import com.loban.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TransporterAvailabilitySlotRepository extends JpaRepository<TransporterAvailabilitySlot, Long> {

    List<TransporterAvailabilitySlot> findByUserOrderByStartAtAsc(User user);

    @Modifying
    @Query("DELETE FROM TransporterAvailabilitySlot s WHERE s.user.id = :uid")
    void deleteAllByUserId(@Param("uid") Long userId);

    @Query(
            "SELECT s FROM TransporterAvailabilitySlot s WHERE s.user = :user AND s.endAt >= :from ORDER BY s.startAt ASC")
    List<TransporterAvailabilitySlot> findUpcomingForUser(@Param("user") User user, @Param("from") Instant from);

    /** Chevauchement : slot ∩ ]ws,we[ non vide ⇔ start < we et end > ws */
    @Query(
            "SELECT COUNT(s) FROM TransporterAvailabilitySlot s WHERE s.user.id = :uid AND s.startAt < :we AND s.endAt > :ws")
    long countOverlappingWindow(@Param("uid") Long userId, @Param("ws") Instant windowStart, @Param("we") Instant windowEnd);
}
