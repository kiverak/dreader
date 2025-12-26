package ru.dreader.dreadernews.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dreader.dreadernews.dto.SourceDetails;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.mapper.SourceMapper;
import ru.dreader.dreadernews.repo.SourceRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final SourceMapper sourceMapper;

    @Transactional
    public void saveAll(List<SourceDetails> sourceDetails) {
        List<Source> sources = sourceMapper.toEntity(sourceDetails);
        sourceRepository.saveAll(sources);
    }

}
