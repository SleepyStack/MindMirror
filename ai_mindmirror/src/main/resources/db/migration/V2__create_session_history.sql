CREATE TABLE session_history (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 summary_text TEXT,
                                 main_topic VARCHAR(255),
                                 emotion_start VARCHAR(100),
                                 emotion_end VARCHAR(100),
                                 action_step TEXT,
                                 created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_session_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);