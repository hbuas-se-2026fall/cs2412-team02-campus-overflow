package com.campusoverflow.qa.domain;

import java.util.Optional;

public interface QuestionRepository {
    Question save(Question question);
    Optional<Question> findById(Long questionId);
    void delete(Question question);
}