package ru.dreader.dreadernews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.dto.ProcessedArticleDto;
import ru.dreader.dreadernews.entity.ProcessedArticle;
import ru.dreader.dreadernews.mapper.ProcessedArticleMapper;
import ru.dreader.dreadernews.repo.ProcessedArticleRepository;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessedArticleService {

    private final ProcessedArticleRepository processedArticleRepository;
    private final ProcessedArticleMapper processedArticleMapper;

    @Transactional(readOnly = true)
    public ProcessedArticleDto getById(Long id) {
        ProcessedArticle article = processedArticleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));
        return processedArticleMapper.toDto(article);
    }

    @Transactional(readOnly = true)
    public List<ProcessedArticleDto> getLast(Long tagId, Integer size, Integer page, String sort, String order) {
        Sort sortBy = Sort.by(Sort.Direction.fromString(order != null ? order : "DESC"), sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<ProcessedArticle> articlePage;
        if (tagId != null) {
            articlePage = processedArticleRepository.findByTags_Id(tagId, pageable);
        } else {
            articlePage = processedArticleRepository.findAll(pageable);
        }

        return articlePage.getContent().stream()
                .map(processedArticleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProcessedArticle getEarliestForDayReadyToPost() {
        List<ProcessedArticle> list = processedArticleRepository.findEarliestFor24HoursNotParsed(PageRequest.of(0, 1))
                .stream()
                .toList();

        return list.isEmpty() ? null : list.getFirst();
    }

    @Transactional(readOnly = true)
    public List<ProcessedArticle> getEarliestForDayReadyToPostBunch(final int bunchSize) {
        return processedArticleRepository.findEarliestFor24HoursNotParsed(PageRequest.of(0, bunchSize))
                .stream()
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        processedArticleRepository.deleteById(id);
    }

    @Transactional
    public void delete(List<Long> iDs) {
        processedArticleRepository.deleteAllById(iDs);
    }
}
