package dreadernewsparser.service;

import dreadernewsparser.entity.Source;
import dreadernewsparser.entity.Tag;
import dreadernewsparser.mapper.SourceMapper;
import dreadernewsparser.repo.SourceRepository;
import dto.SourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final TagService tagService;
    private final SourceMapper sourceMapper;

    public List<Source> findAll() {
        return sourceRepository.findAll();
    }

    public List<SourceDetails> findAllSourceDetails() {
        return sourceRepository.findAll().stream()
                .map(sourceMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<SourceDetails> findById(Long id) {
        return sourceRepository.findById(id)
                .map(sourceMapper::toDto);
    }

    @Transactional
    public SourceDetails save(SourceDetails sourceDetails) {
        Source source = sourceMapper.toEntity(sourceDetails);
        Source savedSource = sourceRepository.save(source);
        return sourceMapper.toDto(savedSource);
    }

    @Transactional
    public SourceDetails update(Long id, SourceDetails sourceDetails) {
        return sourceRepository.findById(id)
                .map(source -> {
                    List<Tag> tags = sourceDetails.defaultTags().stream()
                            .map(tagService::getOrCreate)
                            .collect(Collectors.toList());

                    sourceMapper.updateEntity(source, sourceDetails, tags);
                    Source updatedSource = sourceRepository.save(source);
                    return sourceMapper.toDto(updatedSource);
                })
                .orElseThrow(() -> new RuntimeException("Source not found with id " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        sourceRepository.deleteById(id);
    }
}
