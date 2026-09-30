CREATE DATABASE IF NOT EXISTS pet_management_db;
USE pet_management_db;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE,
    password VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS pets (
    pet_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50),
    species VARCHAR(30),
    breed VARCHAR(30),
    age INT,
    owner_name VARCHAR(50) DEFAULT 'None',
    status VARCHAR(20) DEFAULT 'Available'
);

INSERT INTO users (username, password)
VALUES ('admin', 'admin123')
ON DUPLICATE KEY UPDATE password = VALUES(password);

INSERT INTO pets (name, species, breed, age, owner_name, status)
SELECT 'Buddy', 'Dog', 'Golden Retriever', 3, 'None', 'Available'
WHERE NOT EXISTS (
    SELECT 1 FROM pets
    WHERE name = 'Buddy' AND species = 'Dog' AND breed = 'Golden Retriever'
);

INSERT INTO pets (name, species, breed, age, owner_name, status)
SELECT 'Whiskers', 'Cat', 'Domestic Shorthair', 2, 'None', 'Available'
WHERE NOT EXISTS (
    SELECT 1 FROM pets
    WHERE name = 'Whiskers' AND species = 'Cat' AND breed = 'Domestic Shorthair'
);

INSERT INTO pets (name, species, breed, age, owner_name, status)
SELECT 'Coco', 'Bird', 'Cockatiel', 1, 'Jordan Lee', 'Adopted'
WHERE NOT EXISTS (
    SELECT 1 FROM pets
    WHERE name = 'Coco' AND species = 'Bird' AND breed = 'Cockatiel'
);