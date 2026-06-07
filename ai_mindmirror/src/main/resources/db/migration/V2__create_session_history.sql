CREATE TABLE session_history (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 summary_text TEXT,
                                 main_topic VARCHAR(255),
                                 emotion_start VARCHAR(100),
                                 emotion_end VARCHAR(100),
                                 action_step TEXT,
                                 created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 agent_id VARCHAR(255),
                                 tool_id VARCHAR(255),
                                 conversation_id VARCHAR(255),
                                 status VARCHAR(50) DEFAULT 'ACTIVE',

                                 CONSTRAINT fk_session_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_session_history_status ON session_history(status);