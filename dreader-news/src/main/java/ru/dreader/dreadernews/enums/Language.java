package ru.dreader.dreadernews.enums;

import lombok.Getter;

@Getter
public enum Language {
    RU("ru"),
    EN("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public static Language fromCode(String code) {
        for (Language language : Language.values()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        throw new IllegalArgumentException("Invalid language code: " + code);
    }

}
