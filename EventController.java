package com.ticketbooking.controller;
import com.ticketbooking.dto.EventDtos;
import com.ticketbooking.model.*;
import com.ticketbooking.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api")
public class EventController {
    private final EventService service;
    public EventController(EventService service){this.service=service;}
    @GetMapping("/events")
    public List<Map<String,Object>> events(){
        return service.allEvents().stream().map(this::eventMap).toList();
    }
    @GetMapping("/events/{id}/seats")
    public ResponseEntity<?> seats(@PathVariable Long id){
        try { return ResponseEntity.ok(service.eventSeats(id).stream().map(s -> Map.<String,Object>of("id",s.getId(),"seatNumber",s.getSeatNumber(),"status",s.getStatus())).toList()); }
        catch(NoSuchElementException ex){return ResponseEntity.status(404).body(Map.of("message",ex.getMessage()));}
    }
    @PostMapping("/events")
    public ResponseEntity<?> create(@Valid @RequestBody EventDtos.CreateEventRequest req){
        try{return ResponseEntity.status(201).body(eventMap(service.create(req)));}
        catch(Exception ex){return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage()==null?"Invalid event":ex.getMessage()));}
    }
    private Map<String,Object> eventMap(Event e){
        long available=service.eventSeats(e.getId()).stream().filter(s->"AVAILABLE".equals(s.getStatus())).count();
        return Map.of("id",e.getId(),"name",e.getName(),"venue",e.getVenue(),"eventDate",e.getEventDate(),"price",e.getPrice(),"seatCount",e.getSeatCount(),"availableSeats",available);
    }
}
