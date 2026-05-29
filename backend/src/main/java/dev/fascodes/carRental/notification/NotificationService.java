package dev.fascodes.carRental.notification;

import dev.fascodes.carRental.common.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleReservationNotification(ReservationNotificationEvent event) {
        log.info("Notification [{}] for {} — listing: '{}', dates: {} to {}",
                event.type(),
                event.recipientEmail(),
                event.listingTitle(),
                event.dateStart(),
                event.dateEnd()
        );
        // TODO: send email via JavaMailSender
    }
}
