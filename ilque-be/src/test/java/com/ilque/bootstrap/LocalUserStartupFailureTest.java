package com.ilque.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import com.ilque.IlqueBeApplication;
import com.ilque.domain.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Local User 초기화 실패가 애플리케이션 기동 실패로 이어지는지 검증한다.
 * Electron 패키징 없이 Spring Boot 테스트 환경에서만 검증하며,
 * 개발용 DB 파일을 건드리지 않는다.
 */
@ExtendWith(OutputCaptureExtension.class)
class LocalUserStartupFailureTest {

    static final RuntimeException INJECTED_FAILURE = new RuntimeException("DB 조회 실패(테스트)");

    @Configuration
    @Import(IlqueBeApplication.class)
    static class FailingStartupApp {

        @Bean
        @Primary
        UserService failingUserService() {
            UserService failure = mock(UserService.class);
            when(failure.initializeLocalUser()).thenThrow(INJECTED_FAILURE);
            return failure;
        }
    }

    @Test
    @DisplayName("Local User 초기화 실패 시 애플리케이션 기동이 실패한다")
    void initializationFailure_failsApplicationStartup(CapturedOutput output) throws IOException {
        // given
        Path dbFile = Files.createTempDirectory("ilque-startup-failure-test-").resolve("ilque-test.db");
        SpringApplication app = new SpringApplication(FailingStartupApp.class);
        app.setWebApplicationType(WebApplicationType.NONE);

        // 테스트 전용 임시 DB를 사용하도록 DB 관련 속성을 명령줄 인자로 전달한다.
        // (기본 속성과 달리 명령줄 인자는 application.yml보다 우선순위가 높아,
        //  공통 테스트 설정이 바뀌어도 이 테스트는 항상 임시 DB만 사용한다.)
        String[] args = {
                "--spring.datasource.url=jdbc:sqlite:" + dbFile
                        + "?journal_mode=WAL&synchronous=NORMAL&busy_timeout=5000",
                "--spring.datasource.driver-class-name=org.sqlite.JDBC",
                "--spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect",
                "--spring.jpa.hibernate.ddl-auto=create-drop",
                "--spring.main.banner-mode=off"
        };

        // when & then
        assertThatThrownBy(() -> app.run(args)).isSameAs(INJECTED_FAILURE);
        assertThat(output).contains("Local User 초기화 실패로 애플리케이션 기동을 중단합니다.");
    }
}
