CREATE TABLE glossary_entries (
    id BIGSERIAL PRIMARY KEY,
    source_no INT NOT NULL,
    language VARCHAR(5) NOT NULL,
    letter VARCHAR(10) NOT NULL,
    term TEXT NOT NULL,
    english TEXT,
    definition TEXT NOT NULL,
    UNIQUE (source_no, language)
);

CREATE INDEX idx_glossary_entries_language ON glossary_entries(language);
