package com.timegrid.enums;

public enum NotificationType {

    STATUS_CHANGED("mail/status-changed"),
    PENDING_APPROVAL("mail/pending-approval");

    private final String template;

    NotificationType(String template) {
        this.template = template;
    }

    public String template() {
        return template;
    }

}