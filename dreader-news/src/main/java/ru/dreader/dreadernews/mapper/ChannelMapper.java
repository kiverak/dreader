package ru.dreader.dreadernews.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.entity.Category;
import ru.dreader.dreadernews.entity.Channel;
import ru.dreader.dreadernews.repo.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

    private final CategoryRepository categoryRepository;

    public ChannelDto toDto(Channel channel) {
        if (channel == null) {
            return null;
        }
        ChannelDto dto = new ChannelDto();
        dto.setId(channel.getId());
        dto.setPlatform(channel.getPlatform());
        dto.setName(channel.getName());
        dto.setCredentials(channel.getCredentials());
        dto.setCategoryIds(channel.getCategories().stream().map(Category::getId).toList());
        return dto;
    }

    public Channel toEntity(ChannelDto dto) {
        if (dto == null) {
            return null;
        }
        Channel channel = new Channel();
        channel.setPlatform(dto.getPlatform());
        channel.setName(dto.getName());
        channel.setCredentials(dto.getCredentials());

        List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
        channel.getCategories().addAll(categories);

        return channel;
    }

    public void updateEntity(Channel channel, ChannelDto dto) {
        if (channel == null || dto == null) {
            return;
        }
        channel.setPlatform(dto.getPlatform());
        channel.setName(dto.getName());
        channel.setCredentials(dto.getCredentials());

        List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
        channel.setCategories(categories);
    }

    public List<ChannelDto> toDtoList(List<Channel> channels) {
        return channels.stream().map(this::toDto).collect(Collectors.toList());
    }
}
