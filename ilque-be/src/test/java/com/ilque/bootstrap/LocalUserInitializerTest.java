package com.ilque.bootstrap;

import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

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

        given(userService.initializeLocalUser()).willReturn(User.builder().build());

        // when
        initializer.run(args);

        // then
        then(userService).should()
                .initializeLocalUser();
    }

    @Test
    @DisplayName("Local User 초기화 실패 시 예외를 전파해 기동을 실패 처리한다")
    void run_propagatesInitializationFailure() {
        // given
        LocalUserInitializer initializer =
                new LocalUserInitializer(userService);

        ApplicationArguments args =
                new DefaultApplicationArguments(new String[]{});

        RuntimeException failure = new RuntimeException("DB 조회 실패");
        willThrow(failure).given(userService).initializeLocalUser();

        // when & then
        assertThatThrownBy(() -> initializer.run(args)).isSameAs(failure);
    }
}
