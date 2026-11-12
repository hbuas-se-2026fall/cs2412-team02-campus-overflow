package com.campusoverflow.qa.domain;

public class Vote {
    private Long id;
    private Long voterId;
    private String targetType;
    private Long targetId;
    private String type;

    private Vote(Long voterId, String targetType, Long targetId, String type) {
        this.voterId = voterId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.type = type;
    }

    public static Vote create(Long voterId, String targetType, Long targetId, String type) {
        return new Vote(voterId, targetType, targetId, type);
    }

    // 核心：v.id() 方法
    public Long id() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVoterId() {
        return voterId;
    }
    public String getTargetType() {
        return targetType;
    }
    public Long getTargetId() {
        return targetId;
    }
    public String getType() {
        return type;
    }
}