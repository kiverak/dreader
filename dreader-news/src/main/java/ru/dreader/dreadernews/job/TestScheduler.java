package ru.dreader.dreadernews.job;

import dto.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.service.UserInfoService;

@Log4j2
@Service
@RequiredArgsConstructor
public class TestScheduler {

    private final UserInfoService userInfoService;
//    @Scheduled(fixedRate = 5_000) // каждые 60 секунд
    public void getUser() {
        System.out.println("Getting sources...");
        UserInfo userById = userInfoService.getUserById("1306a4a2-244c-41dd-9f9e-e97b4cdf70b0");
        log.info(userById);
    }

}
