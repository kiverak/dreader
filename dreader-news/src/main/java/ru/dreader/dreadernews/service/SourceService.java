package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dto.SourceDetails;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.mapper.SourceMapper;
import ru.dreader.dreadernews.repo.SourceRepository;
import ru.dreader.dreadernews.web.NewsParserClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final TagService tagService;
    private final SourceMapper sourceMapper;
    private final NewsParserClient newsParserClient;

    @Transactional(readOnly = true)
    public SourceDetails getById(Long id) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found with id: " + id));
        return sourceMapper.toDto(source);
    }

    @Transactional(readOnly = true)
    public List<SourceDetails> getAll() {
        return sourceRepository.findAll().stream()
                .map(sourceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveAll(List<SourceDetails> sourceDetails) {
        List<Source> sources = sourceMapper.toEntity(sourceDetails);
        sourceRepository.saveAll(sources);
    }

    @Transactional
    public void saveOrUpdateAll(List<SourceDetails> sourceDetailsList) {

        Map<String, SourceDetails> detailsByName = sourceDetailsList.stream()
                .collect(Collectors.toMap(SourceDetails::name, d -> d));

        List<String> names = new ArrayList<>(detailsByName.keySet());
        List<Source> existingSources = sourceRepository.findAllBySourceNames(names);

        for (Source source : existingSources) {
            SourceDetails details = detailsByName.get(source.getName());

            source.setUrl(details.url());
            List<Tag> tags = tagService.getOrCreateByNames(details.defaultTags());
            source.setDefaultTags(tags);

            detailsByName.remove(source.getName());
        }

        if (!detailsByName.isEmpty()) {
            saveAll(new ArrayList<>(detailsByName.values()));
            log.info("{} new sources saved", detailsByName.size());
        }
    }

    @Transactional
    public SourceDetails create(SourceDetails sourceDetails) {
        if (sourceRepository.findByName(sourceDetails.name()).isPresent()) {
            throw new IllegalArgumentException("Source with name " + sourceDetails.name() + " already exists");
        }
        Source entity = sourceMapper.toEntity(sourceDetails);
        entity = sourceRepository.save(entity);

        newsParserClient.createSource(sourceDetails);

        return sourceMapper.toDto(entity);
    }

    @Transactional
    public SourceDetails update(Long id, SourceDetails sourceDetails) {
        Source source = sourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Source not found with id: " + id));

        source.setName(sourceDetails.name());
        source.setUrl(sourceDetails.url());
        List<Tag> tags = tagService.getOrCreateByNames(sourceDetails.defaultTags());
        source.setDefaultTags(tags);

        source = sourceRepository.save(source);

        newsParserClient.updateSource(id, sourceDetails);

        return sourceMapper.toDto(source);
    }

    @Transactional
    public void delete(Long id) {
        if (!sourceRepository.existsById(id)) {
            throw new IllegalArgumentException("Source not found with id: " + id);
        }
        sourceRepository.deleteById(id);

        newsParserClient.deleteSource(id);
    }

    @Transactional(readOnly = true)
    public List<Source> findAllBySourceNames(List<String> sourceNames) {
        return sourceRepository.findAllBySourceNames(sourceNames);
    }
}
