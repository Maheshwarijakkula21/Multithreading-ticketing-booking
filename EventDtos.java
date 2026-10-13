package com.ticketbooking.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class EventDtos {
    private EventDtos() {}
    public record CreateEventRequest(@NotBlank @Size(max=140) String name, @NotBlank @Size(max=140) String venue,
        @NotNull LocalDateTime eventDate, @NotNull @DecimalMin("0.00") BigDecimal price, @NotNull @Min(1) @Max(500) Integer seatCount) {}
}
