package ru.dreader.dreadernews.service;

import dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.entity.Article;
import ru.dreader.dreadernews.mapper.ArticleMapper;
import ru.dreader.dreadernews.repo.ArticleRepository;
import ru.dreader.mvc.exception.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;

    @Transactional
    public void saveAll(List<ArticleDto> articleDtoList) {
        List<Article> articles = articleMapper.toEntity(articleDtoList);
        articleRepository.saveAll(articles);
    }

    @Transactional
    public void save(ArticleDto articleDto) {
        Article article = articleMapper.toEntity(articleDto);
        articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public ArticleDto getById(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));
        return articleMapper.toDto(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleDto> getLast(Long tagId, Integer size, Integer page, String sort, String order) {
        Sort sortBy = Sort.by(Sort.Direction.fromString(order != null ? order : "DESC"), sort);
        Pageable pageable = PageRequest.of(page, size, sortBy);

        Page<Article> articlePage;
        if (tagId != null) {
             articlePage = articleRepository.findByTags_Id(tagId, pageable);
        } else {
            articlePage = articleRepository.findAll(pageable);
        }

        return articlePage.getContent().stream()
                .map(articleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Article> getEarliestForDayReadyToPostBunch(final int bunchSize) {
        return articleRepository.findEarliestFor24HoursNotParsed(PageRequest.of(0, bunchSize))
                .stream()
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    @Transactional
    public void delete(List<Long> ids) {
        articleRepository.deleteAllById(ids);
    }

    public boolean checkIfExist(String url) {
        return articleRepository.existsByUrl(url);
    }

    @Transactional(readOnly = true)
    public List<Long> getOldArticleIds(Instant now) {
        return articleRepository.findOldArticleIds(now);
    }
}
