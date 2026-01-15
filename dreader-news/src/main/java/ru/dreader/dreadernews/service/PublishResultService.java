package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.entity.PublishResult;
import ru.dreader.dreadernews.repo.PublishResultRepository;

@Service
@RequiredArgsConstructor
public class PublishResultService {

    private final PublishResultRepository publishResultRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(PublishResult result) {
        publishResultRepository.save(result);
    }

}
