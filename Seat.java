package com.ticketbooking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(name="uk_seat_event_number", columnNames={"event_id","seat_number"}))
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name="event_id", nullable=false) private Event event;
    @Column(name="seat_number", nullable=false, length=12) private String seatNumber;
    @Column(nullable=false, length=20) private String status = "AVAILABLE";

    protected Seat() {}
    public Seat(Event event, String seatNumber) { this.event=event; this.seatNumber=seatNumber; this.status="AVAILABLE"; }
    public Long getId(){return id;} public Event getEvent(){return event;} public String getSeatNumber(){return seatNumber;}
    public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
}
