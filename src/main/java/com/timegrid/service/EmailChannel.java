package com.timegrid.service;

import com.timegrid.config.NotificationProperties;
import com.timegrid.entity.Notification;
import com.timegrid.entity.User;
import com.timegrid.enums.NotificationType;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailSendException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

@Component
@ConditionalOnProperty(name = "timegrid.notifications.email.enabled", havingValue = "true")
@RequiredArgsConstructor
public class EmailChannel implements NotificationChannel {

    private static final Locale LOCALE = Locale.ENGLISH;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationProperties properties;

    @Override
    public String name() {
        return "email";
    }

    // assíncrono: com SMTP lento não atrasa a resposta de quem aprova/submete (ver config/AsyncConfig)
    @Async
    @Override
    public void deliver(Notification notification, NotificationType type, User recipient) {
        Context context = new Context(LOCALE);
        context.setVariable("recipientName", recipient.getFullName());
        context.setVariable("title", notification.getTitle());
        context.setVariable("message", notification.getMessage());
        context.setVariable("link",
                notification.getLink() != null ? properties.baseUrl() + notification.getLink() : null);
        String html = templateEngine.process(type.template(), context);

        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, false, StandardCharsets.UTF_8.name());
            helper.setFrom(properties.email().from());
            helper.setTo(recipient.getEmail());
            helper.setSubject("[TimeGrid] " + notification.getTitle());
            helper.setText(html, true);
            mailSender.send(mail);
        } catch (MessagingException e) {
            throw new MailSendException("Não foi possível preparar o email", e);
        }
    }

}
