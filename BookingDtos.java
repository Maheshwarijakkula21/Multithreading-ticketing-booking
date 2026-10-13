package com.ticketbooking.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
public final class BookingDtos {
    private BookingDtos() {}
    public record CreateBookingRequest(@NotNull Long eventId, @NotEmpty List<@NotNull Long> seatIds) {}
}
