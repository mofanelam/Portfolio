drop table users;
CREATE TABLE users(user_id INTEGER primary key, email VARCHAR(50), password VARCHAR(25), balance REAL, salt INTEGER, username VARCHAR(20));
drop table bills;
CREATE TABLE bills(bill_id INTEGER primary key, amount REAL, created_at DATE, user_id INTEGER,
FOREIGN KEY (user_id) REFERENCES users(user_id));
