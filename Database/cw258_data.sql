--8 Data for INVENTORY
INSERT INTO inventory VALUES (1, 'Fluffy Doll', 5, 2000);
INSERT INTO inventory VALUES (2, 'Fluffy Doll', 1500, 500);
INSERT INTO inventory VALUES (3, 'Fluffy Doll', 1000, 10);
INSERT INTO inventory VALUES (4, 'Fluffy Doll', 2500, 15);
INSERT INTO inventory VALUES (5, 'Fluffy Doll', 50, 25);
INSERT INTO inventory VALUES (6, 'Fluffy Doll', 2000, 300);
INSERT INTO inventory VALUES (7, 'Fluffy Doll', 5000, 200);
--INSERT INTO inventory VALUES (8, 'Fluffy Doll', 30, 1000);

--8 Data for Orders
INSERT INTO orders VALUES (1, 'Instore', 1, '01-JAN-10');
INSERT INTO orders VALUES (2, 'Instore', 1, '01-JAN-10');
INSERT INTO orders VALUES (3, 'Collection', 1, '01-MAY-10');
INSERT INTO orders VALUES (4, 'Collection', 0, '04-MAY-10');
INSERT INTO orders VALUES (5, 'Instore', 1, '12-JUN-10');
INSERT INTO orders VALUES (6, 'Instore', 1, '12-JUN-10');
INSERT INTO orders VALUES (7, 'Delivery', 0, '25-AUG-10');
INSERT INTO orders VALUES (8, 'Instore', 1, '20-SEP-10');
INSERT INTO orders VALUES (9, 'Collection', 0, '20-SEP-10');

--8 Data for Order_products
INSERT INTO order_products VALUES (1, 1, 20);
INSERT INTO order_products VALUES (2, 2, 15);
INSERT INTO order_products VALUES (3, 3, 5);
INSERT INTO order_products VALUES (4, 4, 10);
INSERT INTO order_products VALUES (4, 3, 10);
INSERT INTO order_products VALUES (4, 5, 10);
INSERT INTO order_products VALUES (5, 5, 20);
INSERT INTO order_products VALUES (6, 6, 30);
INSERT INTO order_products VALUES (7, 7, 50);
INSERT INTO order_products VALUES (8, 7, 70);
INSERT INTO order_products VALUES (9, 4, 100);

--2 Data for Collection
INSERT INTO collections VALUES (3, 'Lam', 'Lam', '02-MAY-10');
INSERT INTO collections VALUES (4, 'Chancellor', 'David', '06-MAY-10');
INSERT INTO collections VALUES (9, 'Chancellor', 'David', '06-MAY-10');

--1 Data for Delivery
INSERT INTO deliveries VALUES (7, 'John', 'Hamster', 'Corner Cottage', 'End Lane', 'Liverpool', '30-MAY-10');

--8 Data for STAFF
INSERT INTO staff VALUES (1, 'John', 'Chan');
INSERT INTO staff VALUES (2, 'Joey', 'Lee');
INSERT INTO staff VALUES (3, 'Jason', 'Chau');
INSERT INTO staff VALUES (4, 'Holiday', 'Lau');
INSERT INTO staff VALUES (5, 'Johnathan', 'Lam');
INSERT INTO staff VALUES (6, 'Allison', 'Chan');
INSERT INTO staff VALUES (7, 'James', 'Li');
INSERT INTO staff VALUES (8, 'Andrew', 'Tam');

--8 Data for Staff-Order
INSERT INTO staff_orders VALUES (1, 1);
INSERT INTO staff_orders VALUES (2, 2);
INSERT INTO staff_orders VALUES (3, 3);
INSERT INTO staff_orders VALUES (7, 4);
INSERT INTO staff_orders VALUES (5, 5);
INSERT INTO staff_orders VALUES (6, 6);
INSERT INTO staff_orders VALUES (7, 7);
INSERT INTO staff_orders VALUES (7, 8);
INSERT INTO staff_orders VALUES (7, 9);
