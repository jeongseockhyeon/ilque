package com.ilque;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트 클래스패스의 설정 파일이 개발용 DB와 격리되었는지 검증한다.
 *
 * <p>Spring 컨텍스트를 띄우지 않으므로 DB에 접근하지 않는다. 테스트 리소스가
 * 메인 리소스보다 먼저 로드되어 `classpath:/application.yml`이 테스트 전용
 * 설정으로 해석되는지, 그리고 그 설정이 개발용 DB URL을 포함하지 않는지 확인한다.
 */
class TestDatabaseIsolationTest {

    @Test
    @DisplayName("테스트 클래스패스의 application.yml은 개발용 DB가 아닌 테스트 전용 DB를 사용한다")
    void testClasspathApplicationYmlIsolatesDatabase() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.yml")) {
            assertThat(in)
                    .as("테스트 클래스패스에서 application.yml을 읽을 수 있어야 한다")
                    .isNotNull();

            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            String datasourceUrlLine = content.lines()
                    .map(String::trim)
                    .filter(line -> line.startsWith("url:"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("application.yml에 datasource url 설정이 없습니다."));

            assertThat(datasourceUrlLine)
                    .as("테스트 설정의 datasource url은 테스트 전용 DB를 가리켜야 한다")
                    .contains("ilque-test.db");
            assertThat(datasourceUrlLine)
                    .as("테스트 설정의 datasource url에 개발용 DB 경로가 있으면 안 된다")
                    .doesNotContain("./data/ilque.db");
        }
    }
}
