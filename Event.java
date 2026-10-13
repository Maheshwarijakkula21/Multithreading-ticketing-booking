package com.ticketbooking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 140) private String name;
    @Column(nullable = false, length = 140) private String venue;
    @Column(nullable = false) private LocalDateTime eventDate;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal price;
    @Column(nullable = false) private Integer seatCount;

    protected Event() {}
    public Event(String name, String venue, LocalDateTime eventDate, BigDecimal price, Integer seatCount) {
        this.name=name; this.venue=venue; this.eventDate=eventDate; this.price=price; this.seatCount=seatCount;
    }
    public Long getId(){return id;} public String getName(){return name;} public String getVenue(){return venue;}
    public LocalDateTime getEventDate(){return eventDate;} public BigDecimal getPrice(){return price;}
    public Integer getSeatCount(){return seatCount;}
}
