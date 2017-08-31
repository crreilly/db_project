Created by: Calvin Reilly

How to run the code

1) connect to a MySQL database in the command line
2) open the 'database_instantiation.sql' file
3) run each command in the order that they appear in the file
4) open the 'populate-db.php' file and modify the $url, $user, and $password variables as necessary
5) run the 'populate_db.php' file for each table csv in this order: customer.csv, outsandingfees.csv, paymethod.csv, tracking.csv, intermediate.csv, and package.csv
	- >php populate_db.php <table1.csv> <table1>
	- an example command would be >php populate_db.php customer.csv customer
6) now open up the java project and run it
7) try out the UI!

System Requirements

1) PHP version 7.*
2) java
3) MySql version 5.*
4) JDBC

Test System

I tested my code on a windows 10 computer with 8 GB of ram, an Intel i5 processor, and a 1TB harddrive. 

Attached Files

1)CReillyCSE345.jar 			- An executable jar file that opens to the UI
2)CSE345_Project-R.Calvin_23.docx 	- The final report
3)customer.csv				- CSV file for database populating
4)database_instantiation.sql		- All the queries needed to set up the DB
5)ERDiagram.png				- The ER Diagram in png format
6)ERDiagram.vsdx			- The ER Diagram in Visio format
7)intermediate.csv			- CSV file for database populating
8)outstandingfees.csv			- CSV file for database populating
9)package.csv				- CSV file for database populating
10)paymethod.csv			- CSV file for database populating
11)populate_db.php			- PHP script for database populating
12)project_archive.zip			- The source code for the java portion
13)Readme.txt				- This file
14)tracking.csv				- CSV file for database populating
