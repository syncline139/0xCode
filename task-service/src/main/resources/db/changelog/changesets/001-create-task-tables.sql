--liquibase formatted sql

--changeset syncline:001

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(64) NOT NULL,
    difficulty VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    language VARCHAR(64) NOT NULL,
    initial_code TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE task_criteria (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(64) NOT NULL,
    weight INTEGER NOT NULL,
    required BOOLEAN NOT NULL,
    order_index INTEGER NOT NULL,
    start_line INTEGER,
    end_line INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_task_criteria_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_criteria_weight_positive CHECK (weight > 0),
    CONSTRAINT chk_task_criteria_order_index_non_negative CHECK (order_index >= 0),
    CONSTRAINT chk_task_criteria_start_line_positive CHECK (start_line IS NULL OR start_line > 0),
    CONSTRAINT chk_task_criteria_end_line_positive CHECK (end_line IS NULL OR end_line > 0),
    CONSTRAINT chk_task_criteria_line_range CHECK (
        start_line IS NULL OR end_line IS NULL OR end_line >= start_line
    )
);

CREATE TABLE task_submissions (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    answer_text TEXT,
    submitted_code TEXT,
    status VARCHAR(64) NOT NULL,
    score INTEGER,
    ai_feedback TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    checked_at TIMESTAMP,

    CONSTRAINT fk_task_submissions_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT chk_task_submissions_score_range CHECK (score IS NULL OR (score >= 0 AND score <= 100))
);

CREATE INDEX idx_task_criteria_task_id ON task_criteria(task_id);
CREATE INDEX idx_task_submissions_task_id ON task_submissions(task_id);
CREATE INDEX idx_task_submissions_user_id ON task_submissions(user_id);
