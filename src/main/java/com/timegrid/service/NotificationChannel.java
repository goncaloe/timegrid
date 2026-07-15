package com.timegrid.service;

import com.timegrid.entity.Notification;
import com.timegrid.entity.User;

public interface NotificationChannel {

    String name();

    void deliver(Notification notification, User recipient);

}