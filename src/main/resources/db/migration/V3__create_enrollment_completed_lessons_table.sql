CREATE TABLE enrollment_completed_lessons (
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    PRIMARY KEY (enrollment_id, lesson_id),
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);