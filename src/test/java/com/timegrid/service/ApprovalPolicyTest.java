package com.timegrid.service;

import com.timegrid.exception.ForbiddenException;
import com.timegrid.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalPolicyTest {

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private ApprovalPolicy approvalPolicy;

    @Test
    void checkNotOwnRequestRejectsOwner() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);

        assertThatThrownBy(() -> approvalPolicy.checkNotOwnRequest(userId, "time-entry.approve-self"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void withoutOwnApprovalTargetsRemovesApproveAndRejectForOwner() {
        UUID userId = UUID.randomUUID();
        when(currentUser.userId()).thenReturn(userId);

        assertThat(approvalPolicy.withoutOwnApprovalTargets(userId, List.of("APPROVED", "REJECTED", "CANCELLED")))
                .containsExactly("CANCELLED");
    }

    @Test
    void withoutOwnApprovalTargetsKeepsTargetsForOtherUsers() {
        when(currentUser.userId()).thenReturn(UUID.randomUUID());

        assertThat(approvalPolicy.withoutOwnApprovalTargets(UUID.randomUUID(), List.of("APPROVED", "REJECTED")))
                .containsExactly("APPROVED", "REJECTED");
    }

}