
package com.ticketbooking.repository;

import com.ticketbooking.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"user", "event", "seat"})
    List<Booking> findByUser_IdOrderByBookedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "event", "seat"})
    List<Booking> findAllByOrderByBookedAtDesc();
}
