package dev.fascodes.carRental.notification;

public record ReservationNotificationEvent(
        Long reservationId,
        String recipientEmail,
        String recipientUsername,
        String listingTitle,
        String dateStart,
        String dateEnd,
        String type // "RENTER_CONFIRMED" or "CONFIRMED"
) {}
