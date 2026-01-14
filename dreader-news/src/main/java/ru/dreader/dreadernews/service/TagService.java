package ru.dreader.dreadernews.service;

import ru.dreader.dreadernews.entity.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreader.dreadernews.repo.TagRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag getOrCreate(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(name);
                    return tagRepository.save(newTag);
                });
    }

    @Transactional
    public List<Tag> getOrCreateByNames(List<String> names) {
        List<Tag> existingTags = tagRepository.findAllByNameIn(names);
        Set<String> existingNames = existingTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        List<Tag> newTags = names.stream()
                .filter(name -> !existingNames.contains(name))
                .distinct()
                .map(name -> {
                    Tag tag = new Tag();
                    tag.setName(name);
                    return tag;
                })
                .collect(Collectors.toList());

        if (!newTags.isEmpty()) {
            existingTags.addAll(tagRepository.saveAll(newTags));
        }
        return existingTags;
    }

    @Transactional
    public void deleteById(Long id) {
        tagRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByArticleId(Long articleId) {
        return tagRepository.findByArticleId(articleId);
    }

    @Transactional(readOnly = true)
    public List<Tag> getTagsByProcessedArticleId(Long id) {
        return tagRepository.findByProcessedArticleId(id);
    }
}