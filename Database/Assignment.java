/* Design Choices

-- 1 StaffID, OrderID, ProductID and OrderCompleted variables shouldn't be
-- set as Integer as there shouldn't be any arithmetic work be done to
-- these variables, by setting them VARCHAR2 will reduce the risk of mistakenly changed their value.

-- 2 Add the TotalValueSold in both Table: "ORDER_PRODUCTS" and "STAFF_ORDERS"
-- as it helps and reduced the work for doing other options in the coursework

-- 3 In addtition to 2, Since we will need the employees sales figure eventually, it would be great
-- to have the DATE orderDate in staff_orders as well. It will help tracking the order while date is
-- one of the concern in the queries.

*/

/* SQL Schema

-- So basically every tuple in the tables can't be NULL as all information are needed
-- Since a checkquantity function has been used in the java code, it is not necessary to
-- build CONSTRAINTS like productStockAmount >= 0 but still, it could be the case
-- if someone insert it from sqlplus or doing sql injection. So it helps to protect the database from it.

-- The inventory table takes productID as it's primary key because it is what other tables will rely on.

-- The productPrice and productStockAmount can't be lower than 0
DROP TABLE inventory CASCADE CONSTRAINTS;
CREATE TABLE inventory (
  productID INTEGER NOT NULL CHECK (productID >= 0),
  productDesc VARCHAR2(30),
  productPrice NUMERIC(8,2) NOT NULL CHECK (productPrice >= 0),
  productStockAmount INTEGER NOT NULL CHECK (productStockAmount >= 0),
  primary key(productID)
);

-- The orders table takes orderID as primary key but the table is mainl used to trace the date of orderplaced.

DROP TABLE orders CASCADE CONSTRAINTS;
CREATE TABLE orders (
  orderID INTEGER NOT NULL,
  orderType VARCHAR2(10) NOT NULL,
  orderCompleted INTEGER NOT NULL,
  orderPlaced DATE NOT NULL,
  primary key(orderID)
);

-- The order_products is a table that tracks the quantity of products sold and it intuitively takes
-- both of the orderID and productID as their primary key but also reference them from table orders and inventory.

-- The productQuantity can't be lower than 0

DROP TABLE order_products CASCADE CONSTRAINTS;
CREATE TABLE order_products (
  orderID INTEGER NOT NULL,
  productID INTEGER NOT NULL,
  productQuantity INTEGER NOT NULL CHECK (productQuantity >= 0),
  primary key(orderID, productID),
  foreign key(orderID) references orders(orderID),
  foreign key(productID) references inventory(productID)
);

-- Primary key will be orderID and fName as orderID will allow database user to find out information with regards
-- to the order and with the first name, he/she can make sure that it's that person who is coming to be delivered.

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

-- Same as deliveries.

DROP TABLE collections CASCADE CONSTRAINTS;
CREATE TABLE collections (
  orderID INTEGER NOT NULL,
  fName VARCHAR2(30) NOT NULL,
  lName VARCHAR2(30) NOT NULL,
  collectionDate DATE NOT NULL,
  primary key(orderID, fName),
  foreign key(orderID) references orders(orderID)
);

-- Staff table keep tracks of the employeeName and staffID is unique.
-- Other functions rely on this table to get the full employeeName.

DROP TABLE staff CASCADE CONSTRAINTS;
CREATE TABLE staff (
  staffID INTEGER NOT NULL,
  fName VARCHAR2(30) NOT NULL,
  lName VARCHAR2(30) NOT NULL,
  primary key (staffID)
);

-- Staff_orders help track with the staff who managed to make that order and sell
-- relevant products.

DROP TABLE staff_orders CASCADE CONSTRAINTS;
CREATE TABLE staff_orders (
  staffID INTEGER NOT NULL,
  orderID INTEGER NOT NULL,
  primary key(staffID, orderID),
  foreign key(staffID) references staff(staffID),
  foreign key(orderID) references orders(orderID)
);

*/

import java.io.*;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

class Assignment {

  private static String readEntry(String prompt)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			System.out.print(prompt);
			System.out.flush();
			int c = System.in.read();
			while(c != '\n' && c != -1) {
				buffer.append((char)c);
				c = System.in.read();
			}
			return buffer.toString().trim();
		}
		catch (IOException e)
		{
			return "";
		}
 	}

	/**
	* @param conn An open database connection
	* @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
    //A function to return the current max order number.
    int order_id = getOrderID(conn) + 1;
    String query = "";
    Statement stmt = null;

    //These queries need to be excuted 1 time
    //Insert entry into orders
    try {
      stmt = conn.createStatement();
      query = String.format("INSERT INTO orders VALUES (%d, 'InStore', 1, '%s')", order_id, orderDate);
      stmt.executeQuery(query);
      //Insert into staff Order
      query = String.format("INSERT INTO staff_orders VALUES (%d, %d)" , staffID, order_id);
      stmt.executeQuery(query);

      //These queries need to be excuted productIDs.length times
      //Insert entry into order_products and it depends on (foreign key) order_id in table order.
      for (int i = 0; i < productIDs.length; i++) {
        query = String.format("INSERT INTO order_products VALUES (%d, %d ,%d)" , order_id, productIDs[i], quantities[i]);
        stmt.executeQuery(query);
        //Update the Inventory amount
        query = String.format("UPDATE inventory SET productStockAmount =  productStockAmount - '%d' WHERE productID = '%d'", quantities[i], productIDs[i]);
        stmt.executeQuery(query);
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    } finally{
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    printUpdate(conn, productIDs);
	}

	/**
	* @param conn An open database connection
	* @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
	* @param fName The first name of the customer who will collect the order
	* @param LName The last name of the customer who will collect the order
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID) {
    //A function to return the current max order number.
    int order_id = getOrderID(conn) + 1;
    String query = "";
    Statement stmt = null;
		// Incomplete - Code for option 1 goes here

    //These queries need to be excuted 1 time
    //Insert entry into orders
    try {
      stmt = conn.createStatement();
      query = String.format("INSERT INTO orders VALUES (%d, 'Collection', 0, '%s')", order_id, orderDate);
      stmt.executeQuery(query);
      //Insert into staff Order
      query = String.format("INSERT INTO staff_orders VALUES (%d, %d)" , staffID, order_id);
      stmt.executeQuery(query);

      query = String.format("INSERT INTO collections VALUES (%d, '%s', '%s', '%s')" , order_id, fName, LName, collectionDate);
      stmt.executeQuery(query);

      //These queries need to be excuted productIDs.length times
      //Insert entry into order_products and it depends on (foreign key) order_id in table order.
      for (int i = 0; i < productIDs.length; i++) {
        query = String.format("INSERT INTO order_products VALUES (%d, %d ,%d)" , order_id, productIDs[i], quantities[i]);
        stmt.executeQuery(query);
        //Update the Inventory amount
        query = String.format("UPDATE inventory SET productStockAmount =  productStockAmount - '%d' WHERE productID = '%d'", quantities[i], productIDs[i]);
        stmt.executeQuery(query);
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    } finally {
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    printUpdate(conn, productIDs);
	}

	/**
	* @param conn An open database connection
	* @param productIDs An array of productIDs associated with an order
  * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
	* @param fName The first name of the customer who will receive the order
	* @param LName The last name of the customer who will receive the order
	* @param house The house name or number of the delivery address
	* @param street The street name of the delivery address
	* @param city The city name of the delivery address
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
				   String house, String street, String city, int staffID) {
    //A function to return the current max order number.
    int order_id = getOrderID(conn) + 1;
    String query = "";
    Statement stmt = null;
		// Incomplete - Code for option 1 goes here

    //These queries need to be excuted 1 time
    //Insert entry into orders
    try {
      stmt = conn.createStatement();
      query = String.format("INSERT INTO orders VALUES (%d, 'Collection', 0, '%s')", order_id, orderDate);
      stmt.executeQuery(query);
      //Insert into staff Order
      query = String.format("INSERT INTO staff_orders VALUES (%d, %d)" , staffID, order_id);
      stmt.executeQuery(query);

      query = String.format("INSERT INTO deliveries VALUES (%d, '%s', '%s', '%s', '%s','%s', '%s')" , order_id, fName, LName, house, street, city, deliveryDate);
      stmt.executeQuery(query);

      //These queries need to be excuted productIDs.length times
      //Insert entry into order_products and it depends on (foreign key) order_id in table order.
      for (int i = 0; i < productIDs.length; i++) {
        query = String.format("INSERT INTO order_products VALUES (%d, %d ,%d)" , order_id, productIDs[i], quantities[i]);
        stmt.executeQuery(query);
        //Update the Inventory amount
        query = String.format("UPDATE inventory SET productStockAmount =  productStockAmount - '%d' WHERE productID = '%d'", quantities[i], productIDs[i]);
        stmt.executeQuery(query);
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    } finally {
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    printUpdate(conn, productIDs);
	}

	/**
	* @param conn An open database connection
	*/
	public static void option4(Connection conn)
	{
		// Incomplete - Code for option 4 goes here
    String query = "";
    Statement stmt = null;
    String productID = "";
    String productDesc = "";
    int totalValueSold = 0;
    String output = "";

    try {
      stmt = conn.createStatement();
      // Firstly create a View with productID, description and totalValueSold in each orders.
      query = "CREATE VIEW temp4 AS"
      + " SELECT i.productID, i.productDesc, i.productprice * op.productQuantity AS TotalValueSold"
      + " FROM inventory i"
      + " LEFT JOIN order_products op"
      + " ON i.productID = op.productID"
      + " ORDER BY TotalValueSold DESC";
      stmt.executeQuery(query);

      // Then, Sum up the totalvalueSold from each orders to get each productID's totalValueSold.
      query = "SELECT productID, productDesc, SUM(totalValueSold) as TotalValueSold"
      + " FROM temp4"
      + " GROUP BY productID, productDesc"
      + " ORDER BY totalValueSold Desc";

      //Then, output the result with productID, description and the actual total Value sold
      ResultSet rs = stmt.executeQuery(query);
      //The use of %-10 means there will be at least 10 space so that the output can look better and neat.
      //The productID has been set to String as it is easier to do string concatenation with String.
      System.out.printf("%-10s %-20s %-10s\n", "ProductID" + ",", "ProductDesc"+ ",", "TotalValueSold");
      while(rs.next()) {
        productID = rs.getString("PRODUCTID");
        productDesc = rs.getString("PRODUCTDESC");
        totalValueSold = rs.getInt("TOTALVALUESOLD");
        System.out.printf("%-10s %-20s £%-10d\n", productID + "," , productDesc + ",", totalValueSold);
      }
    } catch (SQLException se) {
        // se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
        System.out.println("If you check and type option 6 again please.");
    } finally{
      try{
        if(stmt != null)
          //In order to clean up space and ensure it works next time, drop the view.(Apply to other options as well).
          query = " DROP VIEW temp4";
          stmt.executeQuery(query);
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
	}

	/**
	* @param conn An open database connection
	* @param date The target date to test collection deliveries against
	*/
	public static void option5(Connection conn, String date)
	{
		// Incomplete - Code for option 5 goes here
    String query = "";
    Statement stmt = null;
    //Since we don't know the size / number of orders that are prior to the input date,
    //It is better to use arraylist then convert it into array.
    ArrayList<Integer> orderIDAL = new ArrayList<>();
    ArrayList<Integer> productIDAL = new ArrayList<>();
    ArrayList<Integer> productQuantityAL = new ArrayList<>();

    try {
      stmt = conn.createStatement();

      //The following query gets productID and productQuantity for orderType that is collections
      // and the orderCompleted value is 0 (not yet collected) and the time that is -8 to the date provided.
      query = String.format("SELECT op.orderID, op.productID, op.productQuantity FROM orders o"
      + " LEFT JOIN order_products op"
      + " ON o.orderID = op.orderID"
      + " LEFT JOIN collections c"
      + " on c.orderID = o.orderID"
      + " WHERE o.orderType = 'Collection' AND o.orderCompleted = 0"
      + " AND c.collectionDate < to_date('%s', 'DD-MON-YY') - 8", date);
      ResultSet rs = stmt.executeQuery(query);

      while(rs.next()) {
        //orderIDAL.add(rs.getInt("ORDERID"));
        productIDAL.add(rs.getInt("PRODUCTID"));
        productQuantityAL.add(rs.getInt("PRODUCTQUANTITY"));
      }

      //Basically same as the query above but it is for distinct orderID, to get the total numbers of orders being made.
      query = String.format("SELECT DISTINCT op.orderID FROM orders o"
      + " LEFT JOIN order_products op"
      + " ON o.orderID = op.orderID"
      + " LEFT JOIN collections c"
      + " on c.orderID = o.orderID"
      + " WHERE o.orderType = 'Collection' AND o.orderCompleted = 0"
      + " AND c.collectionDate < to_date('%s', 'DD-MON-YY') - 8", date);
      ResultSet rs2 = stmt.executeQuery(query);

      while(rs2.next()) {
        orderIDAL.add(rs2.getInt("ORDERID"));
      }

    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    } finally{
      try{
        if(stmt != null)
          stmt.executeQuery(query);
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }

    int[] orderIDs = new int[orderIDAL.size()];
    int[] productIDs = new int[productIDAL.size()];
    int[] productQuantities = new int[productQuantityAL.size()];

    //Converting arraylists into arrays. (Different size of productIDAL/productQuantityAL and orderIDAL)

    for(int i = 0; i < productIDs.length; i++) {
      productIDs[i] = productIDAL.get(i).intValue();
      productQuantities[i] = productQuantityAL.get(i).intValue();
    }
    for(int j = 0; j < orderIDs.length; j++) {
      orderIDs[j] = orderIDAL.get(j).intValue();
    }
    Statement stmt2 = null;

    try {
      stmt2 = conn.createStatement();
      //After find out the orderID and the productQuantities, need to update the productStockAmount in
      //Table inventory.
      for (int k = 0; k < productIDs.length; k++) {
        query = String.format("UPDATE inventory SET productStockAmount ="
        + " productStockAmount + '%d' WHERE productID = '%d'", productQuantities[k], productIDs[k]);
        stmt2.executeQuery(query);
      }

      //Then need to delete relevant data in the database.
      for (int j = 0; j < orderIDs.length; j++) {
      query = String.format("DELETE FROM collections WHERE orderID = '%d'", orderIDs[j]);
      stmt2.executeQuery(query);
      query = String.format("DELETE FROM order_products WHERE orderID = '%d'", orderIDs[j]);
      stmt2.executeQuery(query);
      query = String.format("DELETE FROM staff_orders WHERE orderID = '%d'", orderIDs[j]);
      stmt2.executeQuery(query);
      query = String.format("DELETE FROM orders WHERE orderID = '%d'", orderIDs[j]);
      stmt2.executeQuery(query);
      //Lastly, output the message
      System.out.printf("Order %d has been cancelled\n", orderIDs[j]);
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    } finally {
      try{
        if(stmt2 != null)
          stmt2.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
	}

	/**
	* @param conn An open database connection
	*/
	public static void option6(Connection conn)
	{
		// Incomplete - Code for option 6 goes here
    String query = "";
    Statement stmt = null;
    String employeeName = "";
    int totalValueSold = 0;
    String output = "";

    try {
      stmt = conn.createStatement();
      // A VIEW has been created to get the employee full name, totalValueSold by that employee(getting from staff_orders -> order_products
      // to get it's productID and ->inventory to get it's price.
      query = "CREATE VIEW temp6_1 AS"
      + " SELECT e.fname || ' ' || e.lname as EmployeeName,"
      + " i.productprice * op.productQuantity AS TotalValueSold"
      + " FROM staff_orders s"
      + " LEFT JOIN order_products op"
      + " ON s.orderID = op.orderID"
      + " LEFT JOIN inventory i"
      + " ON i.productID = op.productID"
      + " LEFT JOIN staff e"
      + " ON s.staffID = e.staffID";
      stmt.executeQuery(query);

      // Create a view and sum up the totalValueSold in each orders with regards to each employee

      query = " SELECT employeeName, SUM(TotalValueSold) as ValueSold"
      + " FROM temp6_1"
      + " GROUP BY employeeName"
      + " HAVING SUM(TotalValueSold) >= 50000"
      + " ORDER BY ValueSold DESC";

      ResultSet rs = stmt.executeQuery(query);

      //Similar to the output strategy before.
      System.out.printf("%-20s %-10s\n", "EmployeeName" + ",", "TotalValueSold");
      while(rs.next()) {
        employeeName = rs.getString("EMPLOYEENAME");
        totalValueSold = rs.getInt("VALUESOLD");
        System.out.printf("%-20s £%-10d\n", employeeName + "," , totalValueSold);
      }
    } catch (SQLException se) {
        se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
        System.out.println("If you check and type option 6 again please.");
    } finally{
      try{
        if(stmt != null)
          query = "DROP VIEW temp6_1";
          stmt.executeQuery(query);
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
	}

	/**
	* @param conn An open database connection
	*/
	public static void option7(Connection conn)
	{
		// Incomplete - Code for option 7 goes here
    String query = "";
    Statement stmt = null;
    //ArrayLists are made as the number of products that sold more than £20000 is dynamic.
    ArrayList<Integer> productIDAL = new ArrayList<>();
    ArrayList<Integer> quantityAL = new ArrayList<>();
    ArrayList<String> employeeNameAL = new ArrayList<>();
    String employeeName = "";
    int productID;
    //These are set to be string as it is easier to manipulate when output and using in queries.
    String productID_String = "";
    String productID_Single = "";
    String output = "";

    try {
      stmt = conn.createStatement();

      // The first view was created to get productID, price, description and totalvaluesold with
      // regards to items in inventory.

      query = "CREATE VIEW temp7_1 AS"
      + " SELECT i.productID, i.productPrice, i.productDesc, i.productprice * op.productQuantity AS TotalValueSold"
      + " FROM inventory i"
      + " LEFT JOIN order_products op"
      + " ON i.productID = op.productID"
      + " ORDER BY TotalValueSold DESC";
      stmt.executeQuery(query);

      //The second view was created to sum up the number with the same productID, while
      //only items that has been sold more than £20000 has been selected.

      query = "CREATE VIEW temp7_2 AS"
      + " SELECT productID, productPrice, SUM(totalValueSold) as TotalValueSold"
      + " FROM temp7_1"
      + " GROUP BY productID, productPrice"
      + " HAVING SUM(totalValueSold) >= 20000";
      stmt.executeQuery(query);

      //Then the third view has been created to find out employeeName, productID and quantity with price
      //(because it is easier to use SUM and GROUP with fewer attributes(columns).

      query = "CREATE VIEW temp7_3 AS"
      + " SELECT s.fname || ' ' || s.lname as EmployeeName, op.productID, op.productQuantity, productPrice"
      + " FROM temp7_2 t"
      + " LEFT JOIN order_products op"
      + " ON t.productID = op.productID"
      + " LEFT JOIN  staff_orders so"
      + " ON op.orderID = so.orderID"
      + " LEFT JOIN staff s"
      + " ON so.staffID = s.staffID";
      stmt.executeQuery(query);

      //The fourth view was created to sum up the quantity with regards to EmployeeName.

      query = "CREATE VIEW temp7_4 AS"
      + " SELECT employeeName, productID, productPrice, SUM(productQuantity) as quantity"
      + " From temp7_3"
      + " GROUP BY employeeName, productID, productPrice";
      stmt.executeQuery(query);

      //Then the fifth view was created to get the monetaryvalue of selling products
      //and other information that sold more than £20000. However, there might be same
      //products being sold in differnt orders so further SQL manipulations is needed.

      query = "CREATE VIEW temp7_5 AS"
      + " SELECT employeeName, productID, quantity, productPrice * quantity AS TotalMonetaryValue"
      + " FROM temp7_4"
      + " ORDER BY TotalMonetaryValue DESC";
      stmt.executeQuery(query);

      // There might be the same prodcuts (>£20000) being sold by a same employee
      // So a distinct productID is needed and it is being passed and added into the arraylist.

      query = "SELECT DISTINCT productID FROM temp7_5";
      ResultSet rs = stmt.executeQuery(query);
      while(rs.next()) {
        productID = rs.getInt("PRODUCTID");
        productIDAL.add(productID);
      }

      //Then the total monetaryValue has been added up to get the order. The order is
      //essential as the option specifically asked for it so in a descending order as well.

      query = "SELECT EmployeeName, SUM(totalMonetaryValue) AS totalMonetaryValue"
      + " FROM temp7_5"
      + " GROUP BY EmployeeName"
      + " ORDER BY totalMonetaryValue DESC";
      ResultSet rs2 = stmt.executeQuery(query);
      while(rs2.next()) {
        employeeName = rs2.getString("EMPLOYEENAME");
        employeeNameAL.add(employeeName);
      }

      //Print out the first line of the output, with the title employeename and different productIDs.
      //At the same time, productID_String has been added which will be usde in the pivot function.

      System.out.printf("%-20s", "EmployeeName,");
      for(int i = 0; i < productIDAL.size(); i++) {
        if( i == productIDAL.size() - 1) {
          productID_String = productID_String + Integer.toString(productIDAL.get(i).intValue());
          System.out.printf("Product %-6s\n", productIDAL.get(i).intValue());
        } else {
          productID_String = productID_String + Integer.toString(productIDAL.get(i).intValue()) + " , ";
          System.out.printf("Product %-6s", Integer.toString(productIDAL.get(i).intValue()) + ", ");
        }
      }

      //A string array has been used to inject and output quantity sold on a specified product
      //as it is to be run inside a nested for loop.

      String[] productQuantityArray = new String[productIDAL.size()];


      for(int i = 0; i < employeeNameAL.size(); i++) {
        // The query gets each tuple(row) of the matrix with regards to the employee
        // and the column names are passed by the string created earlier(dynamically)
        query = String.format("SELECT * FROM (SELECT employeeName, productID, quantity"
        + " FROM temp7_5)"
        + " PIVOT"
        + " (max(quantity) for productID in( %s ))"
        + " WHERE employeeName = '%s'", productID_String, employeeNameAL.get(i));
        ResultSet rs3 = stmt.executeQuery(query);

        while(rs3.next()) {
          //A output strategy is used to get it output properly and the productID_Single kept changing to get
          //String passed inside resultset rs3 as the name of the column is dynamic.
          //Moreover, as there might be nulls inside the SQL and if a null value has been returned,
          //the corresponding productQuantityArray[j] value has to be set to 0, not only fulfilling
          //the need from option7 output but also prevent from exceptions.
          System.out.printf("%-20s", employeeNameAL.get(i) + ",");
          for (int j = 0; j < productIDAL.size(); j++) {
            productID_Single = Integer.toString(productIDAL.get(j).intValue());
            productQuantityArray[j] = rs3.getString(productID_Single);
            if(productQuantityArray[j] == null)
              productQuantityArray[j] = "0";
            if( j == productIDAL.size() - 1) {
              //If j == productIDAL.size() -1, it means it is the last element, so no "," is needed.
              System.out.printf("%-14s\n" , productQuantityArray[j]);
            } else {
              System.out.printf("%-14s" , productQuantityArray[j] + ",");
            }
          }
        }
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
        System.out.println("If you check and type option 7 again please.");
    } finally{
      try{
        if(stmt != null)
          query = " DROP VIEW temp7_1";
          stmt.executeQuery(query);
          query = " DROP VIEW temp7_2";
          stmt.executeQuery(query);
          query = " DROP VIEW temp7_3";
          stmt.executeQuery(query);
          query = " DROP VIEW temp7_4";
          stmt.executeQuery(query);
          query = " DROP VIEW temp7_5";
          stmt.executeQuery(query);
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
	}

	/**
	* @param conn An open database connection
	* @param year The target year we match employee and product sales against
	*/
	public static void option8(Connection conn, int year)
	{
		// Incomplete - Code for option 8 goes here
    String query = "";
    Statement stmt = null;
    String employeeName = "";
    int upYear = year + 1;
    int downYear = year - 1;
    try {
      stmt = conn.createStatement();

      //Create a view that includes staffID, orderplaced date, productID and totalValueSOld that is in the specified year.
      //Two +1 and -1 variables from the input year are used as the orders within a year is between
      //The 31-December of the previous year and 1-January of the next year from the input year.
      //The following view was created with regards to the staff_orders(*To find out value sold by each staff in a year*)
      //Get a list of names with selling >£30000 of value in the specified year, so staff that appears
      //in the list later will only be staff that match the first condition of the option.
      //Moreover, it needs to be executed separately
      //because the sum function needs to be grouped.

      query = String.format("CREATE VIEW temp8_1 AS"
      + " SELECT so.staffID, SUM(i.productPrice * op.productQuantity) AS"
      + " totalValueSold"
      + " FROM staff_orders so"
      + " LEFT JOIN orders o"
      + " ON so.orderID = o.orderID"
      + " LEFT JOIN staff s"
      + " ON so.staffID = s.staffID"
      + " LEFT JOIN order_products op"
      + " ON so.orderID = op.orderID"
      + " LEFT JOIN inventory i"
      + " ON i.productID = op.productID"
      + " WHERE o.orderplaced > to_date('31-Dec-%d', 'DD-MON-YY') AND o.orderplaced < to_date('31-Jan-%d', 'DD-MON-YY')", downYear, upYear)
      + " GROUP BY so.staffID"
      + " HAVING SUM(i.productPrice * op.productQuantity) > 30000";
      stmt.executeQuery(query);

      //This query create a view that consists of productID, orderplaced date and totalValueSold with regards
      //to productID in inventory. It basically find out all products being sold within the year(with the totalvalue of each orders made)

      query = String.format("CREATE VIEW temp8_2 AS"
      + " SELECT i.productID,SUM(i.productprice * op.productQuantity) AS TotalValueSold"
      + " FROM inventory i"
      + " LEFT JOIN order_products op"
      + " ON i.productID = op.productID"
      + " LEFT JOIN orders o"
      + " ON op.orderID = o.orderID"
      + " WHERE o.orderplaced > to_date('31-Dec-%d', 'DD-MON-YY') AND o.orderplaced < to_date('31-Jan-%d', 'DD-MON-YY')", downYear, upYear)
      + " GROUP BY i.productID"
      + " HAVING SUM(i.productprice * op.productQuantity) > 20000";
      stmt.executeQuery(query);;


      //Another view was created to find out distinct staffID and productIDs
      //as the staff might sell the same product in different orders.

      query = "CREATE VIEW temp8_3 AS"
      + " SELECT Distinct t.staffID, op.productID"
      + " FROM temp8_1 t"
      + " LEFT JOIN staff s"
      + " ON t.staffID = s.staffID"
      + " LEFT JOIN staff_orders so"
      + " ON t.staffID = so.staffID"
      + " LEFT JOIN order_products op"
      + " ON op.orderID = so.orderID";
      stmt.executeQuery(query);

      //Another view was to be created to get the productID.

      query = "CREATE VIEW temp8_4 AS"
      + " SELECT * from temp8_3 WHERE productID IN (SELECT productID from temp8_2)";
      stmt.executeQuery(query);

      //The last view has been created to get the number of sales that is >£20000 products
      //which has been sold in the specified year by each staff that has sold more than £30000
      //stuff in the specified year.

      query = "CREATE VIEW temp8_5 AS"
      + " SELECT staffID, COUNT(productID) AS num  FROM temp8_4"
      + " GROUP BY staffID";
      stmt.executeQuery(query);

      //The number was then matched with the View temp8_4 to find out
      //if the staff has sold the same number/entries of products that sold
      //more than £20000 this year. (At least sold one) and the employeeName
      //will be output.

      query = "SELECT  s.fname || ' ' || s.lname as EmployeeName"
      + " FROM temp8_5 t"
      + " LEFT JOIN staff s"
      + " ON t.staffID = s.staffID"
      + " WHERE num = (SELECT COUNT(*) FROM temp8_2)";
      ResultSet rs = stmt.executeQuery(query);

      while(rs.next()) {
        employeeName = rs.getString("EMPLOYEENAME");
        System.out.println(employeeName);
      }
    } catch (SQLException se) {
        //se.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
        System.out.println("If you check and type option 8 again please.");
    } finally{
      try{
        if(stmt != null)
          query = " DROP VIEW temp8_1";
          stmt.executeQuery(query);
          query = " DROP VIEW temp8_2";
          stmt.executeQuery(query);
          query = " DROP VIEW temp8_3";
          stmt.executeQuery(query);
          query = " DROP VIEW temp8_4";
          stmt.executeQuery(query);
          query = " DROP VIEW temp8_5";
          stmt.executeQuery(query);
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
	}

	public static Connection getConnection()
	{
		String user;
		String passwrd;
		Connection conn;

		try
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");
		}
		catch (ClassNotFoundException x)
		{
			System.out.println ("Driver could not be loaded");
		}

		user = readEntry("Enter database account:");
		passwrd = readEntry("Enter a password:");
		try
		{
			conn = DriverManager.getConnection("jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy",user,passwrd);
			return conn;
		}
		catch(SQLException e)
		{
			System.out.println("Error retrieving connection");
			return null;
		}
	}


  public static int getOrderID(Connection conn) {
    String query = "";
    int orderID = 0;
    Statement stmt = null;
    try {
      //Get the max orderID and increment it in options 1-3.
      stmt = conn.createStatement();
      query = "SELECT MAX (orderID) FROM orders";
      ResultSet rs = stmt.executeQuery(query);
      if(rs.next()) {
        orderID = rs.getInt("MAX(ORDERID)");
        // System.out.println("orderID is: "+ orderID);
      } else {
        orderID = 0;
      }
      //return orderID;
    } catch (SQLException e) {
        System.out.println("");
        System.out.println("The SQL query is invalid");
        //e.printStackTrace();
    }finally{
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    return orderID;
  }

  public static void printUpdate(Connection conn, int[] productIDs) {
    //Print updates when options 1-3 are selected
    String query = "";
    int productStockAmount = -1;
    Statement stmt = null;
    for(int i = 0; i < productIDs.length; i++) {
      try {
        stmt = conn.createStatement();
        //Query to get the after Updated productStockAmount number and print it out.
        query = String.format("SELECT productStockAmount FROM inventory WHERE productID = %d", productIDs[i]);
        ResultSet rs = stmt.executeQuery(query);
        if(rs.next()) {
          productStockAmount = rs.getInt("productStockAmount");
          System.out.printf("Product ID %d stock is now at : %d\n", productIDs[i], productStockAmount);
        }
      } catch (SQLException e) {
          System.out.println("");
          System.out.println("The SQL query is invalid");
          //e.printStackTrace();
      }finally{
        try{
          if(stmt != null)
            stmt.close();
        }catch(SQLException se) {
          System.out.println("The connection is not closed properly");
          se.printStackTrace();
        }
      }
    }
  }

  public static int convertInt(String input) {
    int result = -1;
    try {
      result = Integer.parseInt(input);
    } catch (NumberFormatException e) {
      System.out.println("Please enter valid numbers.");
      //e.printStackTrace();
    }
    return result;
  }

  public static boolean checkDate(String date) {
    // Check to see if the date format is correct.
    boolean result = true;
    String dateFormat = "dd-MMM-yy";
    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
    Date d = new Date();
    try {
      d = sdf.parse(date);
    } catch (ParseException pe) {
      result = false;
      System.out.println("The time format is wrong, please use dd-MMM-yy");
      //pe.printStackTrace();
    }
    return result;
  }

  public static int checkYear(String year) {
    int result = -1;
    int yr = 0;
    try {
      yr = Integer.parseInt(year);
    } catch (NumberFormatException e) {
      System.out.println("The year typed was not appropriate, please type in an appropriate value");
      //e.printStackTrace();
    }
    // Check if the year is in reasonable range.
    if(yr <= 1960 || yr>= 2050) {
      System.out.println("The year typed was not appropriate, please type something between 1960 and 2050");
    } else {
      year = year.substring(year.length() - 2);
      yr = Integer.parseInt(year);
      result = yr;
    }
    return result;
  }


  public static boolean checkStaffID(Connection conn, int staffID) {
    String query = "";
    boolean result = false;
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      // Check if staffID exists in the staff table.
      query = String.format("SELECT 1 FROM staff WHERE staffID = '%d'", staffID);
      ResultSet rs = stmt.executeQuery(query);
      if(rs.next()) {
        result = true;
      } else {
        System.out.println("You have entered a non-exist staffID, please re-enter your staff ID.");
      }
    } catch (SQLException e) {
        //e.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    }finally{
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    return result;
  }

  public static boolean checkProductID(Connection conn, int productID) {
    String query = "";
    boolean result = false;
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      // Check if the product ID exists in the inventory table
      query = String.format("SELECT 1 FROM inventory WHERE productID = '%d'", productID);
      ResultSet rs = stmt.executeQuery(query);
      if(rs.next()) {
        result = true;
      } else {
        System.out.println("You have entered a non-exist productID, please re-enter another productID.");
      }
    } catch (SQLException e) {
        //e.printStackTrace();
        System.out.println("");
        System.out.println("The SQL query is invalid");
    }finally{
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    return result;
  }

  public static boolean checkQuantity(Connection conn, int productID, int quantity) {
    String query = "";
    boolean result = false;
    Statement stmt = null;
    try {
      stmt = conn.createStatement();
      //Check if the productID exist
      if(quantity >= 0) {
        query = String.format("SELECT 1 FROM inventory WHERE productID = '%d'"
        + " AND productStockAmount >= '%d'", productID, quantity);
        ResultSet rs = stmt.executeQuery(query);
        if(rs.next()) {
          result = true;
        } else {
          System.out.println("Sorry, there isn't enough stock in the inventory.");
        }
      } else {
        System.out.println("Please enter positive number.");
      }
    } catch (SQLException e) {
        System.out.println("");
        System.out.println("The SQL query is invalid");
        //e.printStackTrace();
    }finally{
      try{
        if(stmt != null)
          stmt.close();
      }catch(SQLException se) {
        System.out.println("The connection is not closed properly");
        se.printStackTrace();
      }
    }
    return result;
  }

	public static void main(String args[]) throws SQLException, IOException {
		// You should only need to fetch the connection details once
		// Incomplete
		// Code to present a looping menu, read in input data and call the appropriate option menu goes here
		// You may use readEntry to retrieve input data
    Connection conn;
    boolean connected = true;

    // Ensure the database / application is connected.
    do {
      conn = getConnection();
      if(conn != null) {
        connected = true;
        menu(conn);
      } else {
        connected = false;
        System.out.println("");
        System.out.println("You have entered the wrong username / password");
        System.out.println("Please execute this application again with the correct details");
        System.out.println("");
      }
    } while(!connected);
	}

  public static void menu(Connection conn) throws SQLException {
    String choices = "";
    boolean done = true;

    // A menu made for user to choose and will come out after finishing each options.
    while (done != false) {
      System.out.println("");
      System.out.println("");
      System.out.println ("(1) In-Store Purchases ");
      System.out.println ("(2) Collection ");
      System.out.println ("(3) Delivery ");
      System.out.println ("(4) Biggest Sellers ");
      System.out.println ("(5) Reserved Stock ");
      System.out.println ("(6) Staff Life-Time Success ");
      System.out.println ("(7) Staff Contribution ");
      System.out.println ("(8) Employees of the Year ");
      System.out.println ("(0) Quit ");
      choices = readEntry("Enter your choice: ");
        switch (choices) {
          //Since options 4, 6, 7 have no parameters, so no preparation is needed.
          case "1": prep_option1(conn);
                    break;
          case "2": prep_option2(conn);
                    break;
          case "3": prep_option3(conn);
                    break;
          case "4": option4(conn);
                  break;
          case "5": prep_option5(conn);
                  break;
          case "6": option6(conn);
                  break;
          case "7": option7(conn);
                  break;
          case "8": prep_option8(conn);
                  break;
          case "0": done = false;
                    conn.close();
                  break;
          default:
                  //Check if any other input has been keyed in and remind.
                  System.out.println("");
                  System.out.println("Your input is invalid");
                  System.out.println("Please key in anything between 0-8");
                  System.out.println("");
                  break;
      }
    }
  }

  public static void prep_option1(Connection conn) {
    //Using arraylist and convert it back to array since the number of product
    //that is in the order is unknown.
    ArrayList<Integer> productIDAL = new ArrayList<>();
    ArrayList<Integer> quantityAL = new ArrayList<>();
    String orderDate = "";
    int staffID;
    int productid = 0;
    int quantity = 0;
    boolean noError = true;
    boolean redo = false;

    String another_product = "";
    boolean order = true;

    do{
      //Do a check with product id with the function checkProductID to make sure the productID exists.
      do {
        productid = convertInt((readEntry("Enter a productID: ")));
        redo = (!checkProductID(conn, productid));
      }while(redo);
      //Do a check with the quantity to make sure there is enough stock for the specified product(ID).
      do {
        quantity = convertInt((readEntry("Enter the quantity sold: ")));
        redo = (!checkQuantity(conn, productid, quantity));
      }while(redo);
      productIDAL.add(productid);
      quantityAL.add(quantity);
      another_product = readEntry("Is there another product in the order?: ");
      if (!another_product.equals("Y") && !another_product.equals("N")) {
        boolean invalid = true;
        do {
          System.out.println("You have entered an invalid input");
          System.out.println("Please Enter either 'Y' or 'N'.");
          another_product = readEntry("Is there another product in the order?: ");
          if(another_product.equals("Y") || another_product.equals("N")) {
            invalid = false;
          }
        }while(invalid);
      }
      if (another_product.equals("N")) {
         order = false;
      }
    }while (order);

    //Copy from ArrayList to the Array
    int[] productIDs = new int[productIDAL.size()];
    int[] quantities = new int[quantityAL.size()];

    //Check if things are right (productIDAL size is same as quantityAL size)

    if(productIDAL.size() != quantityAL.size()) {
      System.out.println("There is something wrong with the input.");
      System.out.println("You have to re-submit all the input again.");
      prep_option1(conn);
    }

    //Just in case that java isn't up to date so used a for loop to convert data. (Instead of array functions)
    for(int i = 0; i < productIDs.length; i++) {
      productIDs[i] = productIDAL.get(i).intValue();
      quantities[i] = quantityAL.get(i).intValue();
    }

    //Check if the format of the orderDate input is correct by calling function checkDate(orderDate).
    do {
      orderDate = readEntry("Enter the date sold: ");
      redo = (!checkDate(orderDate));
    }while(redo);

    //Check if the staffID exists by calling the function checkstaffID(conn, staffID);
    do {
      staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
      redo = (!checkStaffID(conn, staffID));
    }while(redo);

    //Pass it to option 1.
    try {
      option1(conn, productIDs, quantities, orderDate, staffID);
    } catch (Exception e) {
      System.out.println("There is something wrong with what you have entered.");
      System.out.println("Please check and enter the detail again.");
      prep_option1(conn);
    }
  }


  public static void prep_option2(Connection conn) {
    ArrayList<Integer> productIDAL = new ArrayList<>();
    ArrayList<Integer> quantityAL = new ArrayList<>();

    String orderDate = "";
    String collectionDate = "";
    String fname = "";
    String Lname = "";
    int staffID;
    int productid = 0;
    int quantity = 0;
    boolean noError = true;
    boolean redo = false;

    String another_product = "";
    boolean order = true;

    //Re-using what was in prep_option1
    do{
      do {
        productid = convertInt((readEntry("Enter a productID: ")));
        redo = (!checkProductID(conn, productid));
      }while(redo);
      do {
        quantity = convertInt((readEntry("Enter the quantity sold: ")));
        redo = (!checkQuantity(conn, productid, quantity));
      }while(redo);
      productIDAL.add(productid);
      quantityAL.add(quantity);
      another_product = readEntry("Is there another product in the order?: ");
      if (!another_product.equals("Y") && !another_product.equals("N")) {
        boolean invalid = true;
        do {
          System.out.println("You have entered an invalid input");
          System.out.println("Please Enter either 'Y' or 'N'.");
          another_product = readEntry("Is there another product in the order?: ");
          if(another_product.equals("Y") || another_product.equals("N")) {
            invalid = false;
          }
        }while(invalid);
      }
      if (another_product.equals("N")) {
         order = false;
      }
    }while (order);

    //Copy from ArrayList to the Array
    int[] productIDs = new int[productIDAL.size()];
    int[] quantities = new int[quantityAL.size()];

    //Check if things are right (productIDAL size is same as quantityAL size)
    if(productIDAL.size() != quantityAL.size()) {
      System.out.println("There is something wrong with the input.");
      System.out.println("You have to re-submit all the input again.");
      prep_option1(conn);
    }

    //Just in case that java isn't up to date so used a for loop to convert data.
    for(int i = 0; i < productIDs.length; i++) {
      productIDs[i] = productIDAL.get(i).intValue();
      quantities[i] = quantityAL.get(i).intValue();
    }

    //Take the order date and collection date
    do {
      orderDate = readEntry("Enter the date sold: ");
      redo = (!checkDate(orderDate));
    }while(redo);

    do {
      collectionDate = readEntry("Enter the date of collection: ");
      redo = (!checkDate(collectionDate));
    }while(redo);

    fname = readEntry("Enter the first name of the collector: ");
    Lname = readEntry("Enter the last name of the collector: ");

    do {
      staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
      redo = (!checkStaffID(conn, staffID));
    }while(redo);

    //Check Data before passing it to option1
    //Pass it to option 2.
    try {
      option2(conn, productIDs, quantities, orderDate, collectionDate, fname, Lname, staffID);
    } catch (Exception e) {
      System.out.println("There is something wrong with what you have entered.");
      System.out.println("Please check and enter the detail again.");
      prep_option2(conn);
    }
  }

  public static void prep_option3(Connection conn) {
    //Similar to prep_option2
    ArrayList<Integer> productIDAL = new ArrayList<>();
    ArrayList<Integer> quantityAL = new ArrayList<>();

    String orderDate = "";
    String deliveryDate = "";
    String fname = "";
    String Lname = "";
    String house = "";
    String street = "";
    String city = "";
    int staffID;
    int productid = 0;
    int quantity = 0;
    boolean noError = true;
    boolean redo = false;

    String another_product = "";
    boolean order = true;

    //Re-using what was in prep_option1
    do{
      do {
        productid = convertInt((readEntry("Enter a productID: ")));
        redo = (!checkProductID(conn, productid));
      }while(redo);
      do {
        quantity = convertInt((readEntry("Enter the quantity sold: ")));
        redo = (!checkQuantity(conn, productid, quantity));
      }while(redo);
      productIDAL.add(productid);
      quantityAL.add(quantity);
      another_product = readEntry("Is there another product in the order?: ");
      if (!another_product.equals("Y") && !another_product.equals("N")) {
        boolean invalid = true;
        do {
          System.out.println("You have entered an invalid input");
          System.out.println("Please Enter either 'Y' or 'N'.");
          another_product = readEntry("Is there another product in the order?: ");
          if(another_product.equals("Y") || another_product.equals("N")) {
            invalid = false;
          }
        }while(invalid);
      }
      if (another_product.equals("N")) {
         order = false;
      }
    }while (order);

    //Copy from ArrayList to the Array
    int[] productIDs = new int[productIDAL.size()];
    int[] quantities = new int[quantityAL.size()];

    //Check if things are right (productIDAL size is same as quantityAL size)
    if(productIDAL.size() != quantityAL.size()) {
      System.out.println("There is something wrong with the input.");
      System.out.println("You have to re-submit all the input again.");
      prep_option1(conn);
    }

    //Just in case that java isn't up to date so used a for loop to convert data.
    for(int i = 0; i < productIDs.length; i++) {
      productIDs[i] = productIDAL.get(i).intValue();
      quantities[i] = quantityAL.get(i).intValue();
    }

    //Take the order date and collection date
    do {
      orderDate = readEntry("Enter the date sold: ");
      redo = (!checkDate(orderDate));
    }while(redo);

    do {
      deliveryDate = readEntry("Enter the date of collection: ");
      redo = (!checkDate(deliveryDate));
    }while(redo);

    fname = readEntry("Enter the first name of the collector: ");
    Lname = readEntry("Enter the last name of the collector: ");
    house = readEntry("Enter the house name/No: ");
    street = readEntry("Enter the street: ");
    city = readEntry("Enter the City: ");

    do {
      staffID = Integer.parseInt(readEntry("Enter your staff ID: "));
      redo = (!checkStaffID(conn, staffID));
    }while(redo);

    //Check Data before passing it to option1
    //Pass it to option 3.

    try {
      option3(conn, productIDs, quantities, orderDate, deliveryDate, fname, Lname, house, street, city, staffID);
    } catch (Exception e) {
      System.out.println("There is something wrong with what you have entered.");
      System.out.println("Please check and enter the detail again.");
      prep_option3(conn);
    }
  }

  public static void prep_option5(Connection conn) {
    //Need to get the String: Date prepared
    String date = "";
    boolean noError = true;
    date = readEntry("Enter the date: ");
    noError = checkDate(date);
    if(!noError) {
      prep_option5(conn);
    } else {
      option5(conn, date);
    }
  }

  public static void prep_option8(Connection conn) {
    //Need to get the int: year prepared
    String yr = "";
    int year = -1;
    boolean noError = true;
    yr = readEntry("Enter the year: ");
    year = checkYear(yr);
    if(year < 0) {
      prep_option8(conn);
    } else {
      option8(conn, year);
    }
  }

}
