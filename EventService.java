package com.ticketbooking.service;
import com.ticketbooking.dto.EventDtos;
import com.ticketbooking.model.*;
import com.ticketbooking.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class EventService {
    private final EventRepository events; private final SeatRepository seats;
    public EventService(EventRepository events, SeatRepository seats) { this.events=events; this.seats=seats; }
    @Transactional(readOnly=true) public List<Event> allEvents(){ return events.findAll(); }
    @Transactional(readOnly=true) public Event getEvent(Long id){ return events.findById(id).orElseThrow(() -> new NoSuchElementException("Event not found")); }
    @Transactional(readOnly=true) public List<Seat> eventSeats(Long id) {
        getEvent(id); return seats.findByEventIdOrderById(id);
    }
    @Transactional
    public Event create(EventDtos.CreateEventRequest req) {
        Event event=events.save(new Event(req.name().trim(),req.venue().trim(),req.eventDate(),req.price(),req.seatCount()));
        List<Seat> newSeats=new ArrayList<>();
        for(int i=1;i<=req.seatCount();i++) newSeats.add(new Seat(event,String.format("S%03d",i)));
        seats.saveAll(newSeats);
        return event;
    }
}
