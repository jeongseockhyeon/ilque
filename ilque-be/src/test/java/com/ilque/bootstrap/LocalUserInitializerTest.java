package com.ilque.bootstrap;

import com.ilque.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class LocalUserInitializerTest {

    @Mock
    private UserService userService;

    @Test
    @DisplayName("애플리케이션 초기화 시 Local User 초기화 메서드를 호출한다")
    void run_callsInitializeLocalUser() {
        // given
        LocalUserInitializer initializer =
                new LocalUserInitializer(userService);

        ApplicationArguments args =
                new DefaultApplicationArguments(new String[]{});

        // when
        initializer.run(args);

        // then
        then(userService).should()
                .initializeLocalUser();
    }
}
