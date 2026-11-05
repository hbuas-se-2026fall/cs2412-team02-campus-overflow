package com.campusoverflow.qa.domain;

public record UserId(Long value) {
    public static UserId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须大于0");
        }
        return new UserId(value);
    }
}
