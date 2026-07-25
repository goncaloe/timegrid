package com.timegrid.service;

import com.timegrid.exception.ForbiddenException;
import com.timegrid.security.CurrentUser;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// mesma verificação repetida em registos de horas e ausêncais: quem aprova não pode ser o dono do pedido
@Component
@RequiredArgsConstructor
public class ApprovalPolicy {

    private static final Set<String> APPROVAL_TARGETS = Set.of("APPROVED", "REJECTED");

    private final CurrentUser currentUser;

    public void checkNotOwnRequest(UUID ownerId, String messageKey) {
        if (ownerId.equals(currentUser.userId())) {
            throw new ForbiddenException(messageKey);
        }
    }

    // allowedActions tem de refletir a mesma regra, senão a UI mostra Aprovar/Rejeitar num pedido próprio e o
    // pedido acaba em 403; devolve uma lista nova porque a lista recebida vem da cache do WorkflowService
    public List<String> withoutOwnApprovalTargets(UUID ownerId, List<String> targets) {
        if (!ownerId.equals(currentUser.userId())) {
            return targets;
        }
        return targets.stream().filter(target -> !APPROVAL_TARGETS.contains(target)).toList();
    }

}