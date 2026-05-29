package dev.fascodes.carRental.notification.service;

import dev.fascodes.carRental.common.config.RabbitConfig;
import dev.fascodes.carRental.notification.ReservationNotificationEvent;
import dev.fascodes.carRental.notification.model.Notification;
import dev.fascodes.carRental.notification.repository.NotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    public NotificationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleReservationNotification(ReservationNotificationEvent event) {
        Notification notification = new Notification();
        notification.setUserEmail(event.recipientEmail());
        notification.setReservationId(event.reservationId());
        notification.setMessage(buildMessage(event));
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private String buildMessage(ReservationNotificationEvent event) {
        return switch (event.type()) {
            case "RENTER_CONFIRMED" -> "New reservation request for '%s' (%s – %s) awaiting your confirmation."
                    .formatted(event.listingTitle(), event.dateStart(), event.dateEnd());
            case "CONFIRMED" -> "Your reservation for '%s' (%s – %s) has been confirmed by the owner."
                    .formatted(event.listingTitle(), event.dateStart(), event.dateEnd());
            default -> "Reservation update for '%s': %s."
                    .formatted(event.listingTitle(), event.type());
        };
    }
}
