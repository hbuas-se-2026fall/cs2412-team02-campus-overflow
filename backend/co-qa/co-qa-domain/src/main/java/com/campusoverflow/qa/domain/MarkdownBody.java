package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/** Markdown 正文。只保存原文；渲染与 XSS 净化在基础设施层完成。 */
public record MarkdownBody(String value) {

    public static MarkdownBody ofQuestion(String raw) {
        Guard.notBlank(raw, "问题正文不能为空");
        Guard.minLength(raw, 10, "问题正文至少10个字符");
        Guard.maxLength(raw, 2000, "问题正文不能超过2000字符");
        return new MarkdownBody(raw);
    }

    public static MarkdownBody ofAnswer(String raw) {
        Guard.notBlank(raw, "回答正文不能为空");
        Guard.minLength(raw, 5, "回答正文至少5个字符");
        Guard.maxLength(raw, 3000, "回答正文不能超过3000字符");
        return new MarkdownBody(raw);
    }
}