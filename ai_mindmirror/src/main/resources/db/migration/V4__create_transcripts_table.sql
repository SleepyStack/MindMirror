CREATE TABLE transcripts (
                             conversation_id VARCHAR(255) PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             payload JSONB,
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                             CONSTRAINT fk_transcript_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_transcripts_user_id ON transcripts(user_id);