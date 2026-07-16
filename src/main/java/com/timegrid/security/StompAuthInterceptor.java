package com.timegrid.security;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// o handshake HTTP de /ws é público, o token só é validado no frame CONNECT
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        if (accessor.getCommand() == StompCommand.CONNECT) {
            UserDetails user = authenticate(accessor.getFirstNativeHeader("Authorization"))
                    .orElseThrow(() -> new BadCredentialsException("auth.unauthorized"));
            accessor.setUser(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } else if ((accessor.getCommand() == StompCommand.SUBSCRIBE || accessor.getCommand() == StompCommand.SEND)
                && accessor.getUser() == null) {
            throw new BadCredentialsException("auth.unauthorized");
        }
        return message;
    }

    private Optional<UserDetails> authenticate(String header) {
        if (header == null || !header.startsWith(BEARER)) {
            return Optional.empty();
        }
        return jwtService.parse(header.substring(BEARER.length()))
                .flatMap(claims -> {
                    try {
                        UserDetails user = userDetailsService.loadUserById(jwtService.subject(claims));
                        return user.isEnabled() ? Optional.of(user) : Optional.empty();
                    } catch (RuntimeException e) {
                        return Optional.empty();
                    }
                });
    }

}
