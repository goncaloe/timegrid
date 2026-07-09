package com.timegrid.service;

import com.timegrid.exception.ForbiddenException;
import com.timegrid.security.CurrentUser;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalPolicy {

    private final CurrentUser currentUser;

    // mesma verificação repetida em registos de horas e ausêncais: quem aprova não pode ser o dono do pedido
    public void checkNotOwnRequest(UUID ownerId, String messageKey) {
        if (ownerId.equals(currentUser.userId())) {
            throw new ForbiddenException(messageKey);
        }
    }

}
