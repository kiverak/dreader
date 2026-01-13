package ru.dreader.dreadernews.mapper;

import org.springframework.stereotype.Component;
import ru.dreader.dreadernews.dto.ChannelDto;
import ru.dreader.dreadernews.entity.Channel;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChannelMapper {

    public ChannelDto toDto(Channel channel) {
        if (channel == null) {
            return null;
        }
        ChannelDto dto = new ChannelDto();
        dto.setId(channel.getId());
        dto.setPlatform(channel.getPlatform());
        dto.setName(channel.getName());
        dto.setCredentials(channel.getCredentials());
        dto.setMinUpdatePeriodInMinutes(channel.getMinUpdatePeriodInMinutes());
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
        channel.setMinUpdatePeriodInMinutes(dto.getMinUpdatePeriodInMinutes());
        return channel;
    }
    
    public void updateEntity(Channel channel, ChannelDto dto) {
        if (channel == null || dto == null) {
            return;
        }
        channel.setPlatform(dto.getPlatform());
        channel.setName(dto.getName());
        channel.setCredentials(dto.getCredentials());
        channel.setMinUpdatePeriodInMinutes(dto.getMinUpdatePeriodInMinutes());
    }

    public List<ChannelDto> toDtoList(List<Channel> channels) {
        return channels.stream().map(this::toDto).collect(Collectors.toList());
    }
}
