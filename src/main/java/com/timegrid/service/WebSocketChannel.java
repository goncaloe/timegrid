package com.timegrid.service;

import com.timegrid.dto.NotificationResponse;
import com.timegrid.entity.Notification;
import com.timegrid.entity.User;
import com.timegrid.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketChannel implements NotificationChannel {

    static final String DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String name() {
        return "websocket";
    }

    @Override
    public void deliver(Notification notification, NotificationType type, User recipient) {
        // o principal da sessão STOMP é o AppUserDetails, cujo nome é o username
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), DESTINATION,
                NotificationResponse.from(notification));
    }

}
