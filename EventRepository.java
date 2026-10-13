package com.ticketbooking.repository;
import com.ticketbooking.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EventRepository extends JpaRepository<Event,Long> {}
