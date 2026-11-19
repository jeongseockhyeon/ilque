package com.ilque.domain.user.service;

import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    static final String DEFAULT_LOCAL_USER_NAME = "Local User";

    private final UserRepository userRepository;

    public User initializeLocalUser() {
        return findLocalUser()
                .orElseGet(this::createLocalUser);
    }

    @Transactional(readOnly = true)
    public Optional<User> findLocalUser() {
        return userRepository.findFirstByOrderByUserIdAsc();
    }

    @Transactional(readOnly = true)
    public boolean existsLocalUser() {
        return findLocalUser().isPresent();
    }

    private User createLocalUser() {
        User user = User.builder()
                .displayName(DEFAULT_LOCAL_USER_NAME)
                .build();

        return userRepository.save(user);
    }
}
