package com.ticketbooking.repository;
import com.ticketbooking.model.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByEventIdOrderById(Long eventId);
    long countByEventIdAndStatus(Long eventId, String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id in :ids order by s.id")
    List<Seat> findAllByIdsForUpdate(@Param("ids") List<Long> ids);
}
