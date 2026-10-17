package com.ilque.bootstrap;

import com.ilque.domain.user.entity.User;
import com.ilque.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalUserInitializer.class);

    private final UserService userService;

    @Override
    public void run(ApplicationArguments arguments) {
        log.info("Local User 초기화를 시작합니다.");
        try {
            User user = userService.initializeLocalUser();
            log.info("Local User 초기화를 완료했습니다. userId={}", user.getUserId());
        } catch (Exception ex) {
            log.error("Local User 초기화 실패로 애플리케이션 기동을 중단합니다.", ex);
            throw ex;
        }
    }
}
