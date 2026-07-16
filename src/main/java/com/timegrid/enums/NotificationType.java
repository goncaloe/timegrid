package com.timegrid.enums;

// não é gravado na bd: serve só para escolher o template do email
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