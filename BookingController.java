package com.ticketbooking.controller;
import com.ticketbooking.dto.BookingDtos;
import com.ticketbooking.model.*;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookings; private final UserRepository users;
    public BookingController(BookingService bookings, UserRepository users){this.bookings=bookings;this.users=users;}
    private User current(HttpServletRequest request){
        var session=request.getSession(false);
        if(session==null || session.getAttribute("USER_ID")==null) throw new IllegalStateException("Please login first");
        return users.findById((Long)session.getAttribute("USER_ID")).orElseThrow(()->new IllegalStateException("User not found"));
    }
    @PostMapping("/bookings")
    public ResponseEntity<?> book(@Valid @RequestBody BookingDtos.CreateBookingRequest req,HttpServletRequest request){
        try {
            User user=current(request);
            var created=bookings.book(user,req);
            return ResponseEntity.status(201).body(created.stream().map(this::map).toList());
        } catch(BookingService.SeatUnavailableException ex){return ResponseEntity.status(409).body(Map.of("message",ex.getMessage()));}
          catch(IllegalStateException ex){return ResponseEntity.status(401).body(Map.of("message",ex.getMessage()));}
          catch(NoSuchElementException | IllegalArgumentException ex){return ResponseEntity.badRequest().body(Map.of("message",ex.getMessage()));}
    }
    @GetMapping("/bookings/my")
    public ResponseEntity<?> my(HttpServletRequest request){
        try{return ResponseEntity.ok(bookings.myBookings(current(request)).stream().map(this::map).toList());}
        catch(IllegalStateException ex){return ResponseEntity.status(401).body(Map.of("message",ex.getMessage()));}
    }
    @GetMapping("/admin/bookings")
    public List<Map<String,Object>> all(){return bookings.allBookings().stream().map(this::map).toList();}
    private Map<String,Object> map(Booking b){
        return Map.of("id",b.getId(),"username",b.getUser().getUsername(),"eventId",b.getEvent().getId(),"eventName",b.getEvent().getName(),
          "seatId",b.getSeat().getId(),"seatNumber",b.getSeat().getSeatNumber(),"bookedAt",b.getBookedAt(),"status",b.getStatus());
    }
}
