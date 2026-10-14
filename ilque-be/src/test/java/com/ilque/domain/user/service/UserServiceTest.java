package com.ilque.domain.user.service;

import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Local User가 존재하지 않으면 false를 반환한다")
    void existsLocalUser_returnsFalseWhenAbsent() {
        // given
        given(userRepository.findFirstByOrderByUserIdAsc())
                .willReturn(Optional.empty());

        // when
        boolean result = userService.existsLocalUser();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Local User가 존재하면 true를 반환한다")
    void existsLocalUser_returnsTrueWhenPresent() {
        // given
        User user = User.builder()
                .displayName("Local User")
                .build();

        given(userRepository.findFirstByOrderByUserIdAsc())
                .willReturn(Optional.of(user));

        // when
        boolean result = userService.existsLocalUser();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Local User가 없으면 기본 Local User를 생성한다")
    void initializeLocalUser_createsWhenAbsent() {
        // given
        given(userRepository.findFirstByOrderByUserIdAsc())
                .willReturn(Optional.empty());
        User saved = User.builder()
                .displayName("Local User")
                .build();
        given(userRepository.save(any(User.class)))
                .willReturn(saved);

        // when
        User result = userService.initializeLocalUser();

        // then
        assertThat(result).isEqualTo(saved);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("Local User가 있으면 기존 사용자를 재사용하고 중복 생성하지 않는다")
    void initializeLocalUser_reusesWhenPresent() {
        // given
        User existing = User.builder()
                .displayName("Local User")
                .build();
        given(userRepository.findFirstByOrderByUserIdAsc())
                .willReturn(Optional.of(existing));

        // when
        User result = userService.initializeLocalUser();

        // then
        assertThat(result).isEqualTo(existing);
        then(userRepository).should(never()).save(any(User.class));
    }
}
