#!/usr/bin/env python3
"""
Generates the Flyway data migration V26__import_glossary_data.sql from the
Rioni Capital glossary source files in src/main/resources/golossary/.

The source files are Bitrix PHP fragments:
  - rioni_glossary_ru_bitrix_active_letter.php  -> PHP array  $glossaryGroups = ['А' => [...], ...]
  - rioni_glossary_en_bitrix_active_letter.php  -> PHP array  $glossaryGroups = ['A' => [...], ...]
  - rioni_glossary_ge_bitrix_active_letter.php  -> JSON heredoc $glossaryItems = json_decode(<<<'JSON' ... JSON, true);

Run from the project root:
    python3 scripts/generate_glossary_migration.py
"""
import json
import re
from collections import Counter
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
GLOSSARY_DIR = PROJECT_ROOT / "src/main/resources/golossary"
OUTPUT_FILE = PROJECT_ROOT / "src/main/resources/db/migration/V26__import_glossary_data.sql"
BATCH_SIZE = 50

RU_FILE = GLOSSARY_DIR / "rioni_glossary_ru_bitrix_active_letter.php"
EN_FILE = GLOSSARY_DIR / "rioni_glossary_en_bitrix_active_letter.php"
GE_FILE = GLOSSARY_DIR / "rioni_glossary_ge_bitrix_active_letter.php"

# Group header, e.g.:  'А' => [
GROUP_HEADER_RE = re.compile(r"^\s*'([^']+)'\s*=>\s*\[", re.MULTILINE)

# Single entry block, e.g.:
# [
#     'id' => 193,
#     'term' => '...',
#     'english' => '...',
#     'definition' => '...',
# ],
# 'english' is optional (absent in the English file). Values never contain
# straight single quotes (verified: no escaped quotes in the sources).
ENTRY_RE = re.compile(
    r"\[\s*'id'\s*=>\s*(\d+)\s*,\s*'term'\s*=>\s*'((?:[^'])*)',\s*"
    r"(?:'english'\s*=>\s*'((?:[^'])*)',\s*)?"
    r"'definition'\s*=>\s*'((?:[^'])*)',\s*\]",
    re.DOTALL,
)

# Georgian file: JSON array inside a PHP heredoc terminated by "JSON, true);"
GE_JSON_RE = re.compile(r"<<<'JSON'\n(.*?)\nJSON\s*,\s*true\s*\)\s*;", re.DOTALL)


def parse_php_glossary(content: str) -> list:
    """Parse RU/EN PHP-array format -> list of (letter, source_no, term, english, definition)."""
    array_start = content.index("$glossaryGroups = [")
    # The PHP data array closes with a standalone "];" line (before the JS block).
    array_end = content.index("\n];", array_start) + len("\n];")
    php_data = content[array_start:array_end]

    entries = []
    group_matches = list(GROUP_HEADER_RE.finditer(php_data))
    for i, header in enumerate(group_matches):
        letter = header.group(1)
        group_start = header.end()
        group_end = group_matches[i + 1].start() if i + 1 < len(group_matches) else len(php_data)
        group_text = php_data[group_start:group_end]
        for m in ENTRY_RE.finditer(group_text):
            entries.append((
                letter,
                int(m.group(1)),
                m.group(2),
                m.group(3),
                m.group(4),
            ))
    return entries


def parse_ge_glossary(content: str) -> list:
    """Parse the Georgian JSON-heredoc format -> list of (letter, source_no, term, english, definition)."""
    m = GE_JSON_RE.search(content)
    if not m:
        raise RuntimeError("Georgian JSON heredoc not found in " + str(GE_FILE))
    items = json.loads(m.group(1))
    entries = []
    for item in items:
        if "source_no" not in item:
            raise RuntimeError(f"Missing source_no in Georgian item: {item.get('term')!r}")
        entries.append((
            item["term"][0],
            int(item["source_no"]),
            item["term"],
            item.get("english"),
            item["definition"],
        ))
    return entries


def sql_str(value) -> str:
    if value is None:
        return "NULL"
    return "'" + value.replace("'", "''") + "'"


def main() -> None:
    rows = []  # (language, source_no, letter, term, english, definition)

    ru_entries = parse_php_glossary(RU_FILE.read_text(encoding="utf-8"))
    for letter, source_no, term, english, definition in ru_entries:
        rows.append(("ru", source_no, letter, term, english, definition))

    en_entries = parse_php_glossary(EN_FILE.read_text(encoding="utf-8"))
    for letter, source_no, term, english, definition in en_entries:
        # The English file has no 'english' field; the term itself is English.
        rows.append(("en", source_no, letter, term, term, definition))

    ge_entries = parse_ge_glossary(GE_FILE.read_text(encoding="utf-8"))
    for letter, source_no, term, english, definition in ge_entries:
        rows.append(("ge", source_no, letter, term, english, definition))

    # --- validation -------------------------------------------------------
    if not rows:
        raise RuntimeError("No glossary entries parsed - nothing to import")

    dupes = [k for k, v in Counter((r[0], r[1]) for r in rows).items() if v > 1]
    if dupes:
        raise RuntimeError(f"Duplicate (language, source_no) pairs: {dupes}")

    counts = Counter(r[0] for r in rows)
    print("Parsed glossary entries:")
    for lang in ("ru", "en", "ge"):
        print(f"  {lang}: {counts[lang]}")
    print(f"  total: {len(rows)}")

    # --- SQL generation ---------------------------------------------------
    lines = [
        "-- Generated by scripts/generate_glossary_migration.py - DO NOT EDIT MANUALLY.",
        "-- Source files: src/main/resources/golossary/rioni_glossary_{ru,en,ge}_bitrix_active_letter.php",
        f"-- Imported entries: {len(rows)} (ru={counts['ru']}, en={counts['en']}, ge={counts['ge']})",
        "",
        "INSERT INTO glossary_entries (source_no, language, letter, term, english, definition) VALUES",
    ]
    for i in range(0, len(rows), BATCH_SIZE):
        batch = rows[i:i + BATCH_SIZE]
        for j, (language, source_no, letter, term, english, definition) in enumerate(batch):
            values = ", ".join([
                str(source_no),
                sql_str(language),
                sql_str(letter),
                sql_str(term),
                sql_str(english),
                sql_str(definition),
            ])
            last_in_batch = (j == len(batch) - 1)
            last_overall = (i + j == len(rows) - 1)
            if last_overall:
                lines.append(f"    ({values});")
            elif last_in_batch:
                lines.append(f"    ({values}),")
                if i + BATCH_SIZE < len(rows):
                    lines.append("")
            else:
                lines.append(f"    ({values}),")
    lines.append("")

    OUTPUT_FILE.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {OUTPUT_FILE} ({len(lines)} lines)")


if __name__ == "__main__":
    main()
