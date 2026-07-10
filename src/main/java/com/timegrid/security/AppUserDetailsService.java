package com.timegrid.security;

import com.timegrid.entity.User;
import com.timegrid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmailIgnoreCase(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("user.credentials.invalid"));
        return new AppUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        User user = userRepository.findForAuthById(id)
                .orElseThrow(() -> new UsernameNotFoundException("user.credentials.invalid"));
        return new AppUserDetails(user);
    }

}
