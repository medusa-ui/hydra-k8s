package io.getmedusa.hydra.security;

import io.getmedusa.hydra.security.model.HydraUser;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

public interface HydraUserService extends ReactiveUserDetailsService {

    Mono<HydraUser> findUserByUsername(String username);

    default PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    default Mono<UserDetails> findByUsername(String username) {
        return findUserByUsername(username).cast(UserDetails.class);
    }

}
