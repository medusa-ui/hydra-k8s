package io.getmedusa.hydra.sample;

import io.getmedusa.hydra.security.model.HydraUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepository {

    public Mono<HydraUser> find(String username, PasswordEncoder passwordEncoder) {
        HydraUser user = new HydraUser();

        user.setUsername(username);
        user.setEncodedPassword(passwordEncoder.encode("world"));

        return Mono.just(user);
    }
}