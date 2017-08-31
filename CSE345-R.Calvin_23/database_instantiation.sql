 CREATE DATABASE oldtest;

CREATE TABLE customer (
	customer_id int NOT NULL AUTO_INCREMENT, 
	email varchar(255), 
	address varchar(255),
	PRIMARY KEY (customer_id) 
);

CREATE TABLE outstandingfees (
	outstandingfees_id int NOT NULL AUTO_INCREMENT,
	customer_id int NOT NULL,
	amount_owed FLOAT(10,2) NOT NULL,
	due_date date NOT NULL,
	interest int  NOT NULL DEFAULT 0,
	PRIMARY KEY (outstandingfees_id),
	FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

CREATE TABLE paymethod (
	paymethod_id int NOT NULL AUTO_INCREMENT,
	customer_id int NOT NULL,
	payment_type varchar(255) DEFAULT 'credit',
	payment_cost FLOAT(10,2),
	shipping_type varchar(255) DEFAULT '10 day',
	shipping_cost FLOAT(10,2),
	international TINYINT(1) DEFAULT 0,
	hazardous TINYINT(1) DEFAULT 0,
	contents varchar(255) DEFAULT '',
	value FLOAT(10,2) DEFAULT 0,
	PRIMARY KEY (paymethod_id),
	FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);


CREATE TABLE tracking (
	tracking_id int NOT NULL AUTO_INCREMENT,
	start_timestamp timestamp DEFAULT CURRENT_TIMESTAMP,
	start_location varchar(255) NOT NULL,
	end_timestamp timestamp,
	end_location varchar(255) NOT NULL,
	PRIMARY KEY (tracking_id)
);

CREATE TABLE intermediate (
	intermediate_id int NOT NULL AUTO_INCREMENT,
	tracking_id int NOT NULL,
	int_timestamp timestamp DEFAULT CURRENT_TIMESTAMP,
	int_location varchar(255) NOT NULL,
	int_reason varchar(255),
	PRIMARY KEY (intermediate_id),
	FOREIGN KEY (tracking_id) REFERENCES tracking(tracking_id) ON DELETE CASCADE
);

CREATE TABLE package (
	package_id int NOT NULL AUTO_INCREMENT,
	reciever_id int NOT NULL,
	sender_id int NOT NULL,
	tracking_id int NOT NULL,
	paymethod_id int NOT NULL,
	weight_id FLOAT(10,4) NOT NULL,
	PRIMARY KEY (package_id),
	FOREIGN KEY (reciever_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
	FOREIGN KEY (sender_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
	FOREIGN KEY (tracking_id) REFERENCES tracking(tracking_id) ON DELETE CASCADE,
	FOREIGN KEY (paymethod_id) REFERENCES paymethod(paymethod_id) ON DELETE CASCADE
);