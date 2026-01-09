package ru.dreader.dreadernews.mapper;

import dto.SourceDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.entity.Source;
import ru.dreader.dreadernews.entity.Tag;
import ru.dreader.dreadernews.service.TagService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SourceMapper {

    private final TagService tagService;

    public Source toEntity(SourceDetails sourceDetails) {
        if (sourceDetails == null) {
            return null;
        }

        Source source = new Source();
        source.setId(sourceDetails.id());
        source.setName(sourceDetails.name());
        source.setUrl(sourceDetails.url());

        List<Tag> defaultTags = new ArrayList<>();
        sourceDetails.defaultTags().forEach(tagName -> {
            Tag tag = tagService.getOrCreate(tagName);
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

}
