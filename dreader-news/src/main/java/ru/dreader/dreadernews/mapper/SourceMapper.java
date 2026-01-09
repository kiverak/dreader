package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import dto.SourceDetails;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.entity.Tag;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.repo.TagRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SourceMapper {

    private final TagRepository tagRepository;

    public Source toEntity(SourceDetails sourceDetails) {
        if (sourceDetails == null) {
            return null;
        }

        Source source = new Source();
        source.setName(sourceDetails.name());
        source.setUrl(sourceDetails.url());

        List<Tag> defaultTags = new ArrayList<>();
        sourceDetails.defaultTags().forEach(tagName -> {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
            defaultTags.add(tag);
        });
        source.setDefaultTags(defaultTags);
        return source;
    }

    public List<Source> toEntity(List<SourceDetails> sourceDetails) {
        List<Source> sources = new ArrayList<>();
        for (SourceDetails details : sourceDetails) {
            sources.add(toEntity(details));
        }
        return sources;
    }

    public SourceDetails toDto(Source source) {
        if (source == null) {
            return null;
        }

        List<String> tagNames = source.getDefaultTags() != null ?
                source.getDefaultTags().stream().map(Tag::getName).collect(Collectors.toList()) :
                Collections.emptyList();

        return new SourceDetails(
                source.getId(),
                source.getName(),
                source.getUrl(),
                tagNames
        );
    }
    
    public void updateEntity(Source source, SourceDetails sourceDetails, List<Tag> defaultTags) {
        if (sourceDetails == null || source == null) {
            return;
        }
        source.setName(sourceDetails.name());
        source.setUrl(sourceDetails.url());
        source.setDefaultTags(defaultTags);
    }
}