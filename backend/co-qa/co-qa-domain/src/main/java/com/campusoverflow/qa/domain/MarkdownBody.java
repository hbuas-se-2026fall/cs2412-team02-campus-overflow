package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/** Markdown 正文。只保存原文；渲染与 XSS 净化在基础设施层完成。 */
public record MarkdownBody(String value) {

    public static MarkdownBody ofQuestion(String raw) {
        String text = Guard.requireText(raw, 10, 2000, "QA-1002", "问题正文");
        return new MarkdownBody(text);
    }

    public static MarkdownBody ofAnswer(String raw) {
        String text = Guard.requireText(raw, 5, 3000, "QA-1003", "回答正文");
        return new MarkdownBody(text);
    }
}