
package com.ticketbooking.service;

import com.ticketbooking.dto.BookingDtos;
import com.ticketbooking.model.*;
import com.ticketbooking.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingService {

    private final SeatRepository seats;
    private final EventRepository events;
    private final BookingRepository bookings;

    public BookingService(
            SeatRepository seats,
            EventRepository events,
            BookingRepository bookings) {
        this.seats = seats;
        this.events = events;
        this.bookings = bookings;
    }

    @Transactional
    public List<Booking> book(
            User user,
            BookingDtos.CreateBookingRequest request) {

        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Please login before booking");
        }

        if (request == null || request.seatIds() == null
                || request.seatIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select at least one seat");
        }

        Event event = events.findById(request.eventId())
                .orElseThrow(() ->
                        new NoSuchElementException("Event not found"));

        List<Long> ids = request.seatIds()
                .stream()
                .distinct()
                .sorted()
                .toList();

        if (ids.size() != request.seatIds().size()) {
            throw new IllegalArgumentException(
                    "Duplicate seat IDs in request");
        }

        List<Seat> locked = seats.findAllByIdsForUpdate(ids);

        if (locked.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "One or more seats do not exist");
        }

        for (Seat seat : locked) {
            if (seat.getEvent() == null
                    || !seat.getEvent().getId().equals(event.getId())) {
                throw new IllegalArgumentException(
                        "Seat does not belong to selected event");
            }

            if (!"AVAILABLE".equals(seat.getStatus())) {
                throw new SeatUnavailableException(
                        "Seat " + seat.getSeatNumber()
                                + " is no longer available");
            }
        }

        List<Booking> created = new ArrayList<>();

        for (Seat seat : locked) {
            seat.setStatus("BOOKED");
            created.add(new Booking(user, event, seat));
        }

        seats.saveAll(locked);

        return bookings.saveAll(created);
    }

    @Transactional(readOnly = true)
    public List<Booking> myBookings(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException(
                    "Logged-in user is required");
        }

        return bookings.findByUser_IdOrderByBookedAtDesc(
                user.getId());
    }

    @Transactional(readOnly = true)
    public List<Booking> allBookings() {
        return bookings.findAllByOrderByBookedAtDesc();
    }

    public static class SeatUnavailableException
            extends RuntimeException {

        public SeatUnavailableException(String message) {
            super(message);
        }
    }
}
