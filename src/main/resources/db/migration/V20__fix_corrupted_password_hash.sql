-- V20__fix_corrupted_password_hash.sql
-- Fix corrupted password hash for login 'kozlova1'
-- Password: securePassword456
-- Hash generated using htpasswd -bnBC 10 "" securePassword456 | tr -d ':\n' | sed 's/$2y/$2a/'

UPDATE rioni_dev.profile_passwords pp
SET password_hash = '$2a$10$IZ9/S/ocXhnVR890aCCYSe6YOR0jV19CQJhPqIoxj/43FZ6WL/cRy'
FROM rioni_dev.profile p
WHERE pp.profile_id = p.id AND p.login = 'kozlova1';