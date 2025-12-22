package dreadernewsparser.mapper;

import dreadernewsparser.dto.SourceDetails;
import dreadernewsparser.entity.Source;
import dreadernewsparser.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SourceMapper {

    public Source toEntity(SourceDetails sourceDetails, List<Tag> defaultTags) {
        if (sourceDetails == null) {
            return null;
        }

        Source source = new Source();
        source.setName(sourceDetails.name());
        source.setUrl(sourceDetails.url());
        source.setDefaultTags(defaultTags);
        return source;
    }

    public SourceDetails toDto(Source source) {
        if (source == null) {
            return null;
        }

        List<String> tagNames = source.getDefaultTags() != null ?
                source.getDefaultTags().stream().map(Tag::getName).collect(Collectors.toList()) :
                Collections.emptyList();

        return new SourceDetails(
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