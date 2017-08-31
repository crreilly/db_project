package testdb;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

//Created by: Calvin Reilly

public class InitialView {

	private JFrame frame;
	private JTextField txtEmailInput;
	private JTextField textCEmail;
	private JTextField textCAddress;
	private JTextField textOStart;
	private JTextField textOFinal;
	private JTextField textOPay;
	private JTextField textOShip;
	private JTextField textOCont;
	private JTextField textOVal;
	private JTextField textORecieve;
	private JTextField textOSend;
	private JTextField textOWeight;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		//the view
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InitialView window = new InitialView();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InitialView() throws SQLException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() throws SQLException {
		frame = new JFrame();
		frame.setBounds(100, 100, 1017, 593);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		//------------------------- Track Package -----------------------------//
		
		JLabel lblPackageTracking = new JLabel("Package Tracking");
		lblPackageTracking.setBounds(421, 47, 120, 43);
		frame.getContentPane().add(lblPackageTracking);
		
		JLabel lblEmail = new JLabel("Email:");
		lblEmail.setBounds(352, 106, 56, 16);
		frame.getContentPane().add(lblEmail);
		
		txtEmailInput = new JTextField();
		txtEmailInput.setBounds(412, 103, 116, 22);
		frame.getContentPane().add(txtEmailInput);
		txtEmailInput.setColumns(10);

		JButton btnCheckPackageStatus = new JButton("Check Package Status");
		btnCheckPackageStatus.addActionListener(new ActionListener() {
			//will first check if the email is valid, second check if they have any packages
			//third show packages with a status:
			public void actionPerformed(ActionEvent arg0) {
				//set up db
				String url = "jdbc:mysql://localhost:3306/oldtest?zeroDateTimeBehavior=convertToNull";
				String username = "root";
				String password = "ch33s3ybob23";
				
				//connect
				Connection connection = null;
				try {connection = DriverManager.getConnection(url, username, password);
					String textInput = null;
					Boolean isValid = true;
					try{
						textInput = txtEmailInput.getText();
					} catch (Exception e){
						JOptionPane.showMessageDialog(frame, "Please input your email into the box.");
						isValid = false;
					}
					
					//this is where we do all the stuff
					if(isValid && (!textInput.isEmpty())){
						//first get all of the tracking_ids
						//then get the data
						
						//concat all findings onto this string
						String toUser = "";
						
						String queryTID = "select tracking_id, package_id from package join customer on package.reciever_id = customer.customer_id where customer.email = '" + textInput + "'";
						Boolean tIDExists = false;
						Statement stmtTID = connection.createStatement();
						ResultSet rsTID = stmtTID.executeQuery(queryTID);
						
						//loop through all available packages
						while (rsTID.next()){
							//there is at least 1 tracking_id
							tIDExists = true;
							
							int tracking_id = rsTID.getInt("tracking_id");
							int package_id = rsTID.getInt("package_id");
							
							//for each tracking_id we first check the tracking table
							String queryET = "select * from tracking where tracking_id = " + tracking_id;
							Statement stmtET = connection.createStatement();
							ResultSet rsET = stmtET.executeQuery(queryET);
							rsET.next();
							
							String endTime = rsET.getString("end_timestamp");
							
							if (endTime == null){
								//it has not reached its destination, check for intermediate stops
								String queryINT = "select * from intermediate where tracking_id = "+ tracking_id +" order by intermediate_id desc limit 1";
								Statement stmtINT = connection.createStatement();
								ResultSet rsINT = stmtINT.executeQuery(queryINT);
								
								//now see the latest intermediate stop (if there has been one)
								if(rsINT.next()){
									//there is one! get it
									String locINT = rsINT.getString("int_location");
									String timeINT = rsINT.getString("int_timestamp");
									toUser = toUser + "| Package Number: " + package_id + " | Location: " + locINT + " | At: " + timeINT + " |\n"; 
								} else {
									//there hasn't been one, use the starting location/time
									String locET = rsET.getString("start_location");
									String timeET = rsET.getString("start_timestamp");
									toUser = toUser + "| Package Number: " + package_id + " | Location: " + locET + " | At: " + timeET + " |\n"; 
								}
								
								//close INT
								try { rsINT.close(); } catch (Throwable ignore){System.out.println("rsINT close failed");};
								try { stmtINT.close(); } catch (Throwable ignore){System.out.println("stmtINT close failed");};
							} else {
								//it has reached its destination, show arrival time and location
								String endLoc = rsET.getString("end_location");
								toUser = toUser + "| Package Number: " + package_id + " | Location: " + endLoc + " | At: " + endTime + " |\n"; 
							}
							
							
							//close ET
							try { rsET.close(); } catch (Throwable ignore){System.out.println("rsET close failed");};
							try { stmtET.close(); } catch (Throwable ignore){System.out.println("stmtET close failed");};
						}
						
						//close TID
						try { rsTID.close(); } catch (Throwable ignore){System.out.println("rsTID close failed");};
						try { stmtTID.close(); } catch (Throwable ignore){System.out.println("stmtTID close failed");};
						
						//was there a tracking_id?
						if (!tIDExists){
							//no, tell them that
							JOptionPane.showMessageDialog(frame, "No packages exist for this email.");
						} else {
							//yes, print results
							JOptionPane.showMessageDialog(frame, toUser);
						}
						
					} else {
						JOptionPane.showMessageDialog(frame, "Please input your email into the box.");
					}
				} catch (SQLException e) {
					throw new IllegalStateException("Cannot connect the database!", e);
				} finally {
					try {
						connection.close();
						if(connection.isClosed()){
							System.out.println("db closed");
						}
					} catch (SQLException e) {
						System.out.println("Closing database failed");
					}	
				}
			}
		});
		btnCheckPackageStatus.setBounds(397, 163, 157, 55);
		frame.getContentPane().add(btnCheckPackageStatus);
		
		//------------------------- Create Customer -----------------------------//
		
		JLabel lblCreateCustomer = new JLabel("Create Customer ");
		lblCreateCustomer.setBounds(764, 60, 141, 16);
		frame.getContentPane().add(lblCreateCustomer);
		
		textCEmail = new JTextField();
		textCEmail.setBounds(758, 100, 116, 22);
		frame.getContentPane().add(textCEmail);
		textCEmail.setColumns(10);
		
		textCAddress = new JTextField();
		textCAddress.setBounds(758, 135, 116, 22);
		frame.getContentPane().add(textCAddress);
		textCAddress.setColumns(10);
		
		JLabel lblEmail_1 = new JLabel("Email:");
		lblEmail_1.setBounds(690, 103, 56, 16);
		frame.getContentPane().add(lblEmail_1);
		
		JLabel lblAddress = new JLabel("Address:");
		lblAddress.setBounds(690, 138, 56, 16);
		frame.getContentPane().add(lblAddress);
		
		JButton btnCNew = new JButton("Create Customer");
		btnCNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//set up db
				String url = "jdbc:mysql://localhost:3306/oldtest?zeroDateTimeBehavior=convertToNull";
				String username = "root";
				String password = "ch33s3ybob23";
				
				//connect
				Connection connection = null;
				try {connection = DriverManager.getConnection(url, username, password);
					String CEmail = textCEmail.getText();
					String CAddress = textCAddress.getText();
					Boolean isValid = true;
					
					//validate fields
					if(CEmail.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your email into the box.");
					} else if(CAddress.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your address into the box.");
					}
					
					//this is where we do all the stuff
					if(isValid){
						
						String query = "INSERT INTO customer (email, address) VALUES ('" + CEmail +"', '"+ CAddress +"')";
						PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmt.executeUpdate(); 
						ResultSet rs = stmt.getGeneratedKeys();    
						rs.next();  
						int customer_id = rs.getInt(1);
						
						//close TID
						try { rs.close(); } catch (Throwable ignore){System.out.println("rs close failed");};
						try { stmt.close(); } catch (Throwable ignore){System.out.println("stmt close failed");};
						
						//success message
						JOptionPane.showMessageDialog(frame, "Success! New customer "+ customer_id + " created.");
						
					}
				} catch (SQLException e) {
					throw new IllegalStateException("Cannot connect the database!", e);
				} finally {
					try {
						connection.close();
						if(connection.isClosed()){
							System.out.println("db closed");
						}
					} catch (SQLException e) {
						System.out.println("Closing database failed");
					}	
				}
			}
		});
		btnCNew.setBounds(746, 170, 141, 40);
		frame.getContentPane().add(btnCNew);
		
		JLabel lblNewOrder = new JLabel("New Order");
		lblNewOrder.setBounds(442, 258, 86, 22);
		frame.getContentPane().add(lblNewOrder);
		
		textOStart = new JTextField();
		textOStart.setBounds(126, 303, 116, 22);
		frame.getContentPane().add(textOStart);
		textOStart.setColumns(10);
		
		textOFinal = new JTextField();
		textOFinal.setBounds(126, 338, 116, 22);
		frame.getContentPane().add(textOFinal);
		textOFinal.setColumns(10);
		
		//------------------------- New Order -----------------------------//
		
		JLabel lblStartingLocation = new JLabel("Starting Location:");
		lblStartingLocation.setBounds(12, 304, 101, 16);
		frame.getContentPane().add(lblStartingLocation);
		
		JLabel lblFinalDestination = new JLabel("Final Destination:");
		lblFinalDestination.setBounds(12, 341, 102, 16);
		frame.getContentPane().add(lblFinalDestination);
		
		textOPay = new JTextField();
		textOPay.setBounds(412, 341, 116, 22);
		frame.getContentPane().add(textOPay);
		textOPay.setColumns(10);
		
		textOShip = new JTextField();
		textOShip.setBounds(412, 411, 116, 22);
		frame.getContentPane().add(textOShip);
		textOShip.setColumns(10);
		
		JLabel lblPaymentType = new JLabel("Payment Type:");
		lblPaymentType.setBounds(292, 306, 86, 16);
		frame.getContentPane().add(lblPaymentType);
		
		JLabel lblPaymentAmount = new JLabel("Payment Amount:");
		lblPaymentAmount.setBounds(292, 341, 116, 16);
		frame.getContentPane().add(lblPaymentAmount);
		
		JLabel lblShippingType = new JLabel("Shipping Type:");
		lblShippingType.setBounds(292, 379, 116, 16);
		frame.getContentPane().add(lblShippingType);
		
		JLabel lblShippingAmount = new JLabel("Shipping Amount");
		lblShippingAmount.setBounds(292, 414, 108, 16);
		frame.getContentPane().add(lblShippingAmount);
		
		JComboBox comboOPay = new JComboBox();
		comboOPay.setModel(new DefaultComboBoxModel(new String[] {"Credit", "Account"}));
		comboOPay.setBounds(412, 303, 116, 22);
		frame.getContentPane().add(comboOPay);
		
		JComboBox comboOShip = new JComboBox();
		comboOShip.setModel(new DefaultComboBoxModel(new String[] {"One Day", "Two Day"}));
		comboOShip.setBounds(412, 376, 116, 22);
		frame.getContentPane().add(comboOShip);
		
		JComboBox comboOInter = new JComboBox();
		comboOInter.setModel(new DefaultComboBoxModel(new String[] {"No", "Yes"}));
		comboOInter.setBounds(126, 373, 116, 22);
		frame.getContentPane().add(comboOInter);
		
		JComboBox comboOHazard = new JComboBox();
		comboOHazard.setModel(new DefaultComboBoxModel(new String[] {"No", "Yes"}));
		comboOHazard.setBounds(126, 411, 116, 22);
		frame.getContentPane().add(comboOHazard);
		
		textOCont = new JTextField();
		textOCont.setBounds(126, 446, 116, 22);
		frame.getContentPane().add(textOCont);
		textOCont.setColumns(10);
		
		textOVal = new JTextField();
		textOVal.setBounds(126, 481, 116, 22);
		frame.getContentPane().add(textOVal);
		textOVal.setColumns(10);
		
		JLabel lblInternational = new JLabel("International:");
		lblInternational.setBounds(12, 376, 101, 16);
		frame.getContentPane().add(lblInternational);
		
		JLabel lblHazardous = new JLabel("Hazardous:");
		lblHazardous.setBounds(12, 414, 101, 16);
		frame.getContentPane().add(lblHazardous);
		
		JLabel lblContents = new JLabel("Contents:");
		lblContents.setBounds(12, 449, 56, 16);
		frame.getContentPane().add(lblContents);
		
		JLabel lblValue = new JLabel("Value:");
		lblValue.setBounds(12, 484, 56, 16);
		frame.getContentPane().add(lblValue);
		
		textORecieve = new JTextField();
		textORecieve.setBounds(412, 446, 116, 22);
		frame.getContentPane().add(textORecieve);
		textORecieve.setColumns(10);
		
		textOSend = new JTextField();
		textOSend.setBounds(412, 481, 116, 22);
		frame.getContentPane().add(textOSend);
		textOSend.setColumns(10);
		
		JLabel lblRecieverId = new JLabel("Reciever ID:");
		lblRecieverId.setBounds(292, 449, 101, 16);
		frame.getContentPane().add(lblRecieverId);
		
		JLabel lblSenderId = new JLabel("Sender ID:");
		lblSenderId.setBounds(292, 484, 86, 16);
		frame.getContentPane().add(lblSenderId);
		
		JButton btnCreateNewOrder = new JButton("Create New Order");
		btnCreateNewOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//set up db
				String url = "jdbc:mysql://localhost:3306/oldtest?zeroDateTimeBehavior=convertToNull";
				String username = "root";
				String password = "ch33s3ybob23";
				
				//connect
				Connection connection = null;
				try {connection = DriverManager.getConnection(url, username, password);
					//get the field values
					String OStart = textOStart.getText();
					String OFinal = textOFinal.getText();
					String OInter = (String)comboOInter.getSelectedItem();
					String OHazard = (String)comboOHazard.getSelectedItem();
					String OCont = textOCont.getText();
					String OVal = textOVal.getText();
					String COPay = (String)comboOPay.getSelectedItem();
					String TOPay = textOPay.getText();
					String COShip = (String)comboOShip.getSelectedItem();
					String TOShip = textOShip.getText();
					String ORecieve = textORecieve.getText();
					String OSend = textOSend.getText();
					String OWeight = textOWeight.getText();
					
					//do we need contents and value?
					Boolean isValid = true;
					Boolean needCV = false;
					if (OInter.equals("Yes") || OHazard.equals("Yes")){
						needCV = true;
					}
					
					//validate fields
					if(OStart.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Starting Location into the box.");
					} else if(OFinal.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Final Destination into the box.");
					} else if(OCont.isEmpty() && needCV){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Contents into the box.");
					} else if(OVal.isEmpty() && needCV){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Value into the box.");
					} else if(TOPay.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Payment Amount into the box.");
					} else if(TOShip.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Shipping Amount into the box.");
					} else if(ORecieve.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Reciever ID into the box.");
					} else if(OSend.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Sender ID into the box.");
					} else if(OWeight.isEmpty()){
						isValid = false;
						JOptionPane.showMessageDialog(frame, "Please input your Weight into the box.");
					}
					
					//now make strings into ints/floats
					Float val = null;
					if(needCV){
						val = Float.parseFloat(OVal);
					}
					Float pay = Float.parseFloat(TOPay);
					Float ship = Float.parseFloat(TOShip);
					//is also the reciever id
					Integer customer_id = Integer.parseInt(ORecieve);
					Integer sender_id = Integer.parseInt(OSend);
					Float weight = Float.parseFloat(OWeight);
					
					//now turn hazard and international into tinyints
					Integer international = 0;
					Integer hazardous = 0;
					if (OInter.equals("Yes")){
						international = 1;
					}
					if (OHazard.equals("Yes")){
						hazardous = 1;
					}
					
					//this is where we do all the stuff
					if(isValid){
						
						//create a new tracking id and get it
						String query = "INSERT INTO tracking (start_location, end_location) VALUES ('"+ OStart +"', '"+ OFinal +"')";
						PreparedStatement stmtT = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmtT.executeUpdate(); 
						ResultSet rsT = stmtT.getGeneratedKeys();    
						rsT.next();  
						int tracking_id = rsT.getInt(1);
						
						//close T
						try { rsT.close(); } catch (Throwable ignore){System.out.println("rsT close failed");};
						try { stmtT.close(); } catch (Throwable ignore){System.out.println("stmtT close failed");};
						
						//create a new paymethod id and get it
						if (needCV){
							//need hazardous fields
							query = "INSERT INTO paymethod (customer_id, payment_type, payment_cost, shipping_type, shipping_cost, international, hazardous, contents, value) VALUES ("+ customer_id +",'"+ COPay +"',"+ pay +",'"+ COShip +"',"+ ship +","+ international +","+ hazardous +",'"+ OCont +"',"+ val +")";
						} else {
							//don't need hazardous
							query = "INSERT INTO paymethod (customer_id, payment_type, payment_cost, shipping_type, shipping_cost, international, hazardous, contents, value) VALUES ("+ customer_id +",'"+ COPay +"',"+ pay +",'"+ COShip +"',"+ ship +",0,0,null,null)";
						}
						PreparedStatement stmtPay = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmtPay.executeUpdate(); 
						ResultSet rsPay = stmtPay.getGeneratedKeys();    
						rsPay.next();  
						int paymethod_id = rsPay.getInt(1);
						
						//close Pay
						try { rsPay.close(); } catch (Throwable ignore){System.out.println("rsPay close failed");};
						try { stmtPay.close(); } catch (Throwable ignore){System.out.println("stmtPay close failed");};
						
						//create a new package id and get it
						query = "INSERT INTO package (reciever_id, sender_id, tracking_id, paymethod_id, weight_id) VALUES ("+ customer_id +","+ sender_id +","+ tracking_id +","+ paymethod_id +","+ weight +")";
						PreparedStatement stmtPac = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
						stmtPac.executeUpdate(); 
						ResultSet rsPac = stmtPac.getGeneratedKeys();    
						rsPac.next();  
						int package_id = rsPac.getInt(1);
						
						//close Pac
						try { rsPac.close(); } catch (Throwable ignore){System.out.println("rsPac close failed");};
						try { stmtPac.close(); } catch (Throwable ignore){System.out.println("stmtPac close failed");};
						
						//if the type of payment = Account, then we add a bill to the account via outstandingfees
						if(COPay.equals("Account")){
							//format amount owed and date owed by
							Float totalOut = pay + ship;
							Date date = new Date();
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							Integer year = cal.get(Calendar.YEAR);
							Integer month = cal.get(Calendar.MONTH);
							//set to 1 month from today (month is 0 based)
							month = month + 2;
							Integer day = cal.get(Calendar.DAY_OF_MONTH);
							String dateDue = "" + year + "-" + month + "-" + day; 
							query = "INSERT INTO outstandingfees (customer_id, amount_owed, due_date, interest) VALUES ("+ customer_id +","+ totalOut +", '"+ dateDue +"', 0)";
							PreparedStatement stmtOut = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
							stmtOut.executeUpdate(); 
							ResultSet rsOut = stmtOut.getGeneratedKeys();    
							rsOut.next();  
							int oF_id = rsOut.getInt(1);
							
							//close Out
							try { rsOut.close(); } catch (Throwable ignore){System.out.println("rsOut close failed");};
							try { stmtOut.close(); } catch (Throwable ignore){System.out.println("stmtOut close failed");};
							
							//modified success message
							JOptionPane.showMessageDialog(frame, "Success! New package "+ package_id + " created.\n Bill #" + oF_id +" added to customer #" + customer_id +".");
						} else {
							//standard success message
							JOptionPane.showMessageDialog(frame, "Success! New package "+ package_id + " created.");
						}
					}
				} catch (SQLException e) {
					throw new IllegalStateException("Cannot connect the database!", e);
				} finally {
					try {
						connection.close();
						if(connection.isClosed()){
							System.out.println("db closed");
						}
					} catch (SQLException e) {
						System.out.println("Closing database failed");
					}	
				}
				
			}
		});
		btnCreateNewOrder.setBounds(612, 375, 151, 58);
		frame.getContentPane().add(btnCreateNewOrder);
		
		textOWeight = new JTextField();
		textOWeight.setBounds(412, 516, 116, 22);
		frame.getContentPane().add(textOWeight);
		textOWeight.setColumns(10);
		
		JLabel lblWeight = new JLabel("Weight:");
		lblWeight.setBounds(292, 519, 56, 16);
		frame.getContentPane().add(lblWeight);
	}
}
