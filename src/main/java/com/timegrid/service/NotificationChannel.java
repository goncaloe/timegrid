package com.timegrid.service;

import com.timegrid.entity.Notification;
import com.timegrid.entity.User;
import com.timegrid.enums.NotificationType;

public interface NotificationChannel {

    String name();

    void deliver(Notification notification, NotificationType type, User recipient);

}