package com.thedebugnaths.ai_mindmirror.entity;

import jakarta.persistence.Column;

public class SessionHistory {
    @Column(columnDefinition = "TEXT")
    private String summaryText;

    private String mainTopic;
    private String emotionStart;
    private String emotionEnd;

    @Column(columnDefinition = "TEXT")
    private String actionStep;
}
