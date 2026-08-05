-- Test data: currencies for DictionaryControllerTest
-- ON CONFLICT DO NOTHING allows re-running tests without cleaning the DB
INSERT INTO currency (id, title, symbol) VALUES (1, 'USD', '$') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (2, 'EUR', '€') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (3, 'RUB', '₽') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (4, 'GBP', '£') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (5, 'CNY', '¥') ON CONFLICT (id) DO NOTHING;

-- Test data: glossary entries for DictionaryControllerTest
INSERT INTO glossary_entries (source_no, language, letter, term, english, definition) VALUES
    (1001, 'ru', 'А', 'Актив', 'Asset', 'Тестовое определение актива'),
    (1002, 'ru', 'Б', 'Брокер', 'Broker', 'Тестовое определение брокера'),
    (1001, 'en', 'A', 'Asset', 'Asset', 'Test definition of asset'),
    (1002, 'en', 'B', 'Broker', 'Broker', 'Test definition of broker'),
    (1001, 'ge', 'ა', 'აქტივი', 'Asset', 'ტესტური განმარტება')
ON CONFLICT (source_no, language) DO NOTHING;
