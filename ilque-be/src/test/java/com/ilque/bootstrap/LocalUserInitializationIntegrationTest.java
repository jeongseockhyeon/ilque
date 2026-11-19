package com.ilque.bootstrap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.repository.UserRepository;
import com.ilque.domain.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트용 SQLite 파일 DB를 이용한 Local User 초기화 통합 테스트.
 * 실제 DB 파일이나 사용자 데이터를 건드리지 않는다.
 */
@SpringBootTest
class LocalUserInitializationIntegrationTest {

    private static final Path DB_DIR = createTempDir();

    private static Path createTempDir() {
        try {
            return java.nio.file.Files.createTempDirectory("ilque-init-test-");
        } catch (IOException e) {
            throw new UncheckedIOException("테스트용 임시 디렉터리 생성 실패", e);
        }
    }

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + DB_DIR.resolve("ilque-test.db")
                        + "?journal_mode=WAL&synchronous=NORMAL&busy_timeout=5000");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("애플리케이션 기동 시 Local User가 없으면 기본 사용자를 1명 생성한다")
    void startup_createsSingleLocalUserWhenAbsent() {
        // when (ApplicationRunner가 기동 시 이미 실행됨)
        // then
        assertThat(userRepository.count()).isEqualTo(1);
        User user = userService.findLocalUser().orElseThrow();
        assertThat(user.getDisplayName()).isEqualTo("Local User");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("재실행 시 기존 사용자를 재사용하고 중복 생성하지 않는다")
    void reinitialize_reusesExistingUser() {
        // given
        User first = userService.findLocalUser().orElseThrow();

        // when
        User second = userService.initializeLocalUser();

        // then
        assertThat(second.getUserId()).isEqualTo(first.getUserId());
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
