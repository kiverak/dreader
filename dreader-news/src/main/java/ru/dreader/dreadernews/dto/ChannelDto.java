package ru.dreader.dreadernews.dto;

import lombok.Data;
import ru.dreader.dreadernews.enums.Platform;

import java.util.Map;

@Data
public class ChannelDto {
    private Long id;
    private Platform platform;
    private String name;
    private Map<String, String> credentials;
    private int minUpdatePeriodInMinutes;
}
