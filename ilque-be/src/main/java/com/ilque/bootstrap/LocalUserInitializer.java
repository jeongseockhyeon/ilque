package com.ilque.bootstrap;

import com.ilque.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalUserInitializer implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments arguments) {
        userService.initializeLocalUser();
    }
}
