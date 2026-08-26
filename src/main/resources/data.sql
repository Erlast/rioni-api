INSERT INTO currency (id, title, symbol) VALUES (1, 'USD', '$');
INSERT INTO currency (id, title, symbol) VALUES (2, 'EUR', '€');
INSERT INTO currency (id, title, symbol) VALUES (3, 'RUB', '₽');
INSERT INTO currency (id, title, symbol) VALUES (4, 'GBP', '£');
INSERT INTO currency (id, title, symbol) VALUES (5, 'CNY', '¥');

INSERT INTO tariffs (id, name, description) VALUES
    (1, 'Standard', 'Standard trading tariff'),
    (2, 'Premium', 'Premium trading tariff with extra benefits'),
    (3, 'Pro', 'Pro tariff for active traders');

INSERT INTO profile (name, nbs, ndu, surname, patronymic, email, phone, gender, citizenship, date_of_birth, place_of_birth, document_type, passport_number, passport_issue_date, passport_expiry_date) VALUES ('Александр', '7701234567', '7709876543', 'Смирнов', 'Петрович', 'a.smirnov@mail.ru', '+79061234567', 'M', 'РФ', '1985-03-15', 'Москва', 'PASSPORT', '4512 876543', '2015-08-20', '2025-08-20');
INSERT INTO profile (name, nbs, ndu, surname, patronymic, email, phone, gender, citizenship, date_of_birth, place_of_birth, document_type, passport_number, passport_issue_date, passport_expiry_date) VALUES ('Елена', '7702345678', '7708765432', 'Козлова', 'Ивановна', 'e.kozlova@gmail.com', '+79069876543', 'F', 'РФ', '1992-11-28', 'Санкт-Петербург', 'ID_CARD', '1412 567890', '2018-03-10', '2028-03-10');
