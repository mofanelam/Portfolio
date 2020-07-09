DROP TABLE inventory CASCADE CONSTRAINTS;
CREATE TABLE inventory (
  productID INTEGER NOT NULL,
  productDesc VARCHAR2(30),
  productPrice NUMERIC(8,2) NOT NULL,
  productStockAmount INTEGER NOT NULL,
  primary key(productID)
);

DROP TABLE orders CASCADE CONSTRAINTS;
CREATE TABLE orders (
  orderID INTEGER NOT NULL,
  orderType VARCHAR2(10) NOT NULL,
  orderCompleted INTEGER NOT NULL,
  orderPlaced DATE NOT NULL,
  primary key(orderID)
);

DROP TABLE order_products CASCADE CONSTRAINTS;
CREATE TABLE order_products (
  orderID INTEGER NOT NULL,
  productID INTEGER NOT NULL,
  productQuantity INTEGER NOT NULL,
  primary key(orderID, productID),
  foreign key(orderID) references orders(orderID),
  foreign key(productID) references inventory(productID)
);

DROP TABLE deliveries CASCADE CONSTRAINTS;
CREATE TABLE deliveries (
  orderID INTEGER NOT NULL,
  fName VARCHAR2(15) NOT NULL,
  lName VARCHAR2(15) NOT NULL,
  house VARCHAR2(30) NOT NULL,
  street VARCHAR2(30) NOT NULL,
  city VARCHAR2(15) NOT NULL,
  deliveryDate DATE NOT NULL,
  primary key(orderID, fName),
  foreign key(orderID) references orders(orderID)
);

DROP TABLE collections CASCADE CONSTRAINTS;
CREATE TABLE collections (
  orderID INTEGER NOT NULL,
  fName VARCHAR2(30) NOT NULL,
  lName VARCHAR2(30) NOT NULL,
  collectionDate DATE NOT NULL,
  primary key(orderID, fName),
  foreign key(orderID) references orders(orderID)
);

DROP TABLE staff CASCADE CONSTRAINTS;
CREATE TABLE staff (
  staffID INTEGER NOT NULL,
  fName VARCHAR2(30) NOT NULL,
  lName VARCHAR2(30) NOT NULL,
  primary key (staffID)
);


DROP TABLE staff_orders CASCADE CONSTRAINTS;
CREATE TABLE staff_orders (
  staffID INTEGER NOT NULL,
  orderID INTEGER NOT NULL,
  primary key(staffID, orderID),
  foreign key(staffID) references staff(staffID),
  foreign key(orderID) references orders(orderID)
);
