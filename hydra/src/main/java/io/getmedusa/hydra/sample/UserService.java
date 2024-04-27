package io.getmedusa.hydra.sample;

import io.getmedusa.hydra.security.HydraUserService;
import io.getmedusa.hydra.security.model.HydraUser;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserService implements HydraUserService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<HydraUser> findUserByUsername(String username) {
        return userRepository.find(username, passwordEncoder);
    }

    @Override
    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

}
