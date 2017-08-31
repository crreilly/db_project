<?php

//This script will accept arguments as follows:
//<csv file name> <db table name>
//NOTE:
//1) csv file must be in the same directory as this script is run 
//2) it is assumed that the database is the 'oldtest' database 
//3) the order of the columns should be reflected by the csv order
//4) the primary key will not be included in csv

//these are the args
$num_args = $argc;
$arg_array = $argv;

//var dumps
var_dump($num_args);
var_dump($arg_array);

//get data from csv file 
$file = fopen($arg_array[1], "r");

//connect to the db1
$servername = "localhost";
$username = "root";
$password = "ch33s3ybob23";

try {
    $conn = new PDO("mysql:host=$servername;dbname=oldtest", $username, $password);
    // set the PDO error mode to exception
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    echo "Connected successfully \n"; 
    }
catch(PDOException $e)
    {
    echo "Connection failed: " . $e->getMessage();
    }

//select the right stmt based off of the table name
switch ($arg_array[2]) {
	case 'customer':
		$stmt = $conn->prepare("INSERT INTO customer (email, address) VALUES (?,?)");
		//standard, only string inputs
		while(! feof($file)){
			$temp = fgetcsv($file);
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	case 'outstandingfees':
		$stmt = $conn->prepare("INSERT INTO outstandingfees (customer_id, amount_owed, due_date, interest) VALUES (?,?,?,?)");
		while(! feof($file)){
			$temp = fgetcsv($file);
			//check
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			//typecast
			$temp[0] = (int)$temp[0];
			$temp[1] = (float)$temp[1];
			$temp[3] = (int)$temp[3];
			
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	case 'paymethod':
		$stmt = $conn->prepare("INSERT INTO paymethod (customer_id, payment_type, payment_cost, shipping_type, shipping_cost) VALUES (?,?,?,?,?)");
		while(! feof($file)){
			$temp = fgetcsv($file);
			//check
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			//typecast
			$temp[0] = (int)$temp[0];
			$temp[2] = (float)$temp[2];
			$temp[4] = (float)$temp[4];
			
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	case 'tracking':
		$stmt = $conn->prepare("INSERT INTO tracking (start_location, end_location) VALUES (?,?)");
		while(! feof($file)){
			$temp = fgetcsv($file);
			//check
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	case 'intermediate':
		$stmt = $conn->prepare("INSERT INTO intermediate (tracking_id, int_location, int_reason) VALUES (?,?,?)");
		while(! feof($file)){
			$temp = fgetcsv($file);
			//check
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			//typecast
			$temp[0] = (int)$temp[0];
			
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	case 'package':
		$stmt = $conn->prepare("INSERT INTO package (reciever_id, sender_id, tracking_id, paymethod_id, weight_id) VALUES (?,?,?,?,?)");
		while(! feof($file)){
			$temp = fgetcsv($file);
			//check
			if(!is_array($temp)){
				break;
				var_dump($temp);
			}
			//typecast
			$temp[0] = (int)$temp[0];
			$temp[1] = (int)$temp[1];
			$temp[2] = (int)$temp[2];
			$temp[3] = (int)$temp[3];
			$temp[4] = (float)$temp[4];


			
			$stmt->execute($temp);
			//var_dump(fgetcsv($file));
		}
		break;

	default:
		echo "" . $arg_array[2] . " is not a valid table\n";
		break;
}

//close db and file
$conn = null;
fclose($file);
echo "Connection closed";
?>