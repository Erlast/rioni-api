-- Test data: currencies for DictionaryControllerTest
-- ON CONFLICT DO NOTHING allows re-running tests without cleaning the DB
INSERT INTO currency (id, title, symbol) VALUES (1, 'USD', '$') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (2, 'EUR', '€') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (3, 'RUB', '₽') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (4, 'GBP', '£') ON CONFLICT (id) DO NOTHING;
INSERT INTO currency (id, title, symbol) VALUES (5, 'CNY', '¥') ON CONFLICT (id) DO NOTHING;
