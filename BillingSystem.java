import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BillingSystem {
    private DatabaseManager dbManager;

    public BillingSystem(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public static void handleBillingMenu(Connection connection) {
        Scanner scanner = new Scanner(System.in);

        // Display billing menu options
        System.out.println("Billing Menu:");
        System.out.println("1. New Customer");
        System.out.println("2. Existing Customer");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                handleNewCustomer(connection);
                break;
            case 2:
                handleExistingCustomer(connection);
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
                break;
        }
    }

    public static void handleNewCustomer(Connection connection) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Ask for customer details
            System.out.print("Enter customer name: ");
            String customerName = scanner.nextLine();
            System.out.print("Enter customer email address: ");
            String customerEmail = scanner.nextLine();
            System.out.print("Enter customer phone: ");
            String customerPhone = scanner.nextLine();
            System.out.print("Enter customer address: ");
            String customerAddress = scanner.nextLine();
            
            // Validate input
            if (!isValidCustomerInput(customerName, customerEmail, customerPhone, customerAddress)) {
                System.out.println("Invalid input. Please ensure all fields are correctly filled.");
                return;
            }

            // Check if the email address already exists
            if (isExistingEmail(connection, customerEmail)) {
                System.out.println("The email address provided already exists. Please enter a different email address.");
                return;
            }
            
            
            // Insert new customer
            
            int customerId = insertNewCustomer(connection, customerName, customerEmail, customerPhone, customerAddress);

            // Create a new bill for the new customer
            int billId = insertNewBill(connection, customerId);
            
            System.out.println("New bill created with ID: " + billId + " for customer ID: " + customerId);
            handleBillingOperations(connection, billId, customerId);
        } catch (SQLException e) {
            e.printStackTrace();
            
        }
    }
    public static boolean isValidCustomerInput(String name, String email, String phone, String address) {
        // Check if any field is empty
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            address == null || address.trim().isEmpty()) {
            return false;
        }

        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(emailRegex);
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            return false;
        }

        // Validate phone number format (assuming a 10-digit phone number for this example)
        String phoneRegex = "^[0-9]{10}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(phone);
        if (!phoneMatcher.matches()) {
            return false;
        }

        return true;
    }
    public static boolean isExistingEmail(Connection connection, String email) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM customers WHERE email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt("count");
                    return count > 0;
                }
            }
        }
        return false;
    }
    public static void handleExistingCustomer(Connection connection) {
        try {
            Scanner scanner = new Scanner(System.in);

            // Ask for customer details
            System.out.print("Enter customer name: ");
            String customerName = scanner.nextLine();
            System.out.print("Enter customer email address: ");
            String customerEmail = scanner.nextLine();

            // Get customer ID
            int customerId = getCustomerId(connection, customerName, customerEmail);

            if (customerId != 0) {
                // Create a new bill for the existing customer
                int billId = insertNewBill(connection, customerId);

                System.out.println("New bill created with ID: " + billId + " for customer ID: " + customerId);
                handleBillingOperations(connection, billId, customerId);
            } else {
                System.out.println("Customer not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int insertNewBill(Connection connection, int customerId) throws SQLException {
        // Insert a bill with a default valid total value to satisfy the check constraint
        String query = "INSERT INTO bills (customer_id, tot_quantity, total, payment_method) VALUES (?, 0, 1.0, 'cash')";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, customerId);
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating bill failed, no ID obtained.");
                }
            }
        }
    }


    public static int insertNewCustomer(Connection connection, String customerName, String customerEmail, String customerPhone, String customerAddress) throws SQLException {
        String query = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, customerName);
            preparedStatement.setString(2, customerEmail);
            preparedStatement.setString(3, customerPhone);
            preparedStatement.setString(4, customerAddress);
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }

    public static int getCustomerId(Connection connection, String customerName, String customerEmail) throws SQLException {
        int customerId = 0;
        String query = "SELECT id FROM customers WHERE name = ? AND email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, customerName);
            preparedStatement.setString(2, customerEmail);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    customerId = resultSet.getInt("id");
                }
            }
        }
        return customerId;
    }
    public void generateBill(Customer customer, Product product, int quantity) {
        double totalAmount = product.getPrice() * quantity;
        System.out.println("Generating bill for " + customer.getName() + ":");
        System.out.println(quantity + " x " + product.getName() + " @ " + product.getPrice() + " each");
        System.out.println("Total: " + totalAmount);

        try {
            Connection conn = dbManager.getConnection();
            String query = "INSERT INTO bills (customer_id, product_id, quantity, total_amount) VALUES ('"
                    + customer.getCustomerId() + "', '" + product.getProductId() + "', " + quantity + ", " + totalAmount + ")";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            System.out.println("Bill generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void handleBillingOperations(Connection connection, int billId, int customerId) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayBillingOperationsMenu();
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                	insertProductIntoBill(connection, billId,customerId);
                    break;
                case 2:
                	updateProductIntoBill(connection, billId,customerId);
                    break;
                case 3:
                    deleteProductFromBill(connection, billId,customerId);
                    break;
                case 4:
                    displayProductFromBill(connection, billId, customerId);
                    break; 
                case 5:
                   return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    public static void displayBillingOperationsMenu() {
        System.out.println("Billing Operations Menu:");
        System.out.println("1. Add Product to Bill");
        System.out.println("2. Update Product in Bill");
        System.out.println("3. Delete Product from Bill");
        System.out.println("4. Display and Proceed to Payment");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

//*************************************************handlePaymentMethods*******************************************

    public static void handlePaymentMethods(Connection connection, int billId, int customerId) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        if (!isValidBillAndCustomer(connection, billId, customerId)) {
            System.out.println("Invalid Bill ID or Customer ID. Returning to main menu.");
            return;
        }
        // Display payment methods menu options
        System.out.println("Payment Methods Menu:");
        System.out.println("1. UPI");
        System.out.println("2. Cash");
        System.out.println("3. Credit Card");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        boolean paymentSuccess = false;

        switch (choice) {
            case 1:
                paymentSuccess = handleUPIPayment(connection, billId, customerId);
                break;
            case 2:
                paymentSuccess = handleCashPayment(connection, billId, customerId);
                break;
            case 3:
                paymentSuccess = handleCreditCardPayment(connection, billId, customerId);
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
                return;
        }

        if (paymentSuccess) {
            updateProductStockAfterPayment(connection, billId);
            System.out.println("Payment successful. Stock updated.");
            System.out.print("Would you like to proceed with our feedback section? (yes/no): ");
            String proceedPayment = scanner.next();
            if ("yes".equalsIgnoreCase(proceedPayment)) {
                addFeedback(connection, customerId);
            } else {
                System.out.println("Returning to main menu.");
                return;
            }
        } else {
            System.out.println("Payment failed. Returning to main menu.");
        }
    }

    public static boolean isValidBillAndCustomer(Connection connection, int billId, int customerId) {
        String query = "SELECT COUNT(*) FROM bills WHERE id = ? AND customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, billId);
            statement.setInt(2, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void updateProductStockAfterPayment(Connection connection, int billId) throws SQLException {
        // Query to get the quantities of products in the bill
        String query = "SELECT product_id, quantity FROM bill_items WHERE bill_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, billId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int productId = resultSet.getInt("product_id");
                    int quantity = resultSet.getInt("quantity");

                    // Update the stock in the products table
                    String updateStockQuery = "UPDATE products SET stock = stock - ? WHERE id = ?";
                    try (PreparedStatement updateStockStatement = connection.prepareStatement(updateStockQuery)) {
                        updateStockStatement.setInt(1, quantity);
                        updateStockStatement.setInt(2, productId);
                        updateStockStatement.executeUpdate();
                    }
                }
            }
        }
    }

//*************************************handleCustomerAndStockReports****************************************************

    public static void handleCustomerAndStockReports(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // Display customer and stock reports menu options
        System.out.println("Customer and Stock Reports Menu:");
        System.out.println("1. Customer Report");
        System.out.println("2. Stock Report");
        System.out.println("3. Re-Stocking Product");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                handleCustomerReportOptions(connection);
                break;
            case 2:
                handleStockReportOptions(connection);
                break;
            case 3:
            	restockingOptions(connection);
                break;
            default:
                System.out.println("Invalid choice. Returning to main menu.");
                break;
        }
    }
 //************************************************Functions executions***********************************************************  
 //=======================================================Insert Product==========================================================
    public static void insertProductIntoBill(Connection connection, int billId, int customerId) throws SQLException {
        try {
            Scanner scanner = new Scanner(System.in);

            // Ask for product name
            System.out.print("Enter product name: ");
            String productName = scanner.nextLine();

            // Query to retrieve product details
            String query = "SELECT id, stock, rate FROM products WHERE name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, productName);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int productId = resultSet.getInt("id");
                        double rate = resultSet.getDouble("rate");
                        int stock = resultSet.getInt("stock");

                        // Ask for quantity
                        System.out.print("Enter quantity: ");
                        int quantity = scanner.nextInt();

                        // Query to get the total quantity already added to the bill
                        String totalQuantityQuery = "SELECT IFNULL(SUM(quantity), 0) AS totalQuantity FROM bill_items WHERE bill_id = ? AND product_id = ?";
                        try (PreparedStatement totalQuantityStatement = connection.prepareStatement(totalQuantityQuery)) {
                            totalQuantityStatement.setInt(1, billId);
                            totalQuantityStatement.setInt(2, productId);
                            try (ResultSet totalQuantityResultSet = totalQuantityStatement.executeQuery()) {
                                if (totalQuantityResultSet.next()) {
                                    int totalQuantityInBill = totalQuantityResultSet.getInt("totalQuantity");
                                    int availableStock = stock - totalQuantityInBill;

                                    // Check if stock is sufficient
                                    if (availableStock >= quantity) {
                                        // Update total quantity and total amount
                                        double amount = rate * quantity;
                                        updateBillTotals(connection, billId, quantity, amount);

                                        // Insert the product into the bill_items table
                                        insertBillItem(connection, billId, productId, quantity, rate);

                                        System.out.println("Product added to the bill successfully.");
                                    } else {
                                        // Insufficient stock
                                        System.out.println("Insufficient stock for the selected product.");
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Product not found.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

	public static void insertBillItem(Connection connection, int billId, int productId, int quantity, double price) throws SQLException {
	    String query = "INSERT INTO bill_items (bill_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	        preparedStatement.setInt(1, billId);
	        preparedStatement.setInt(2, productId);
	        preparedStatement.setInt(3, quantity);
	        preparedStatement.setDouble(4, price);
	        preparedStatement.executeUpdate();
	    }
	}
//====================================================Update Product===========================================================	
	public static void updateProductIntoBill(Connection connection, int billId, int customerId) {
	    try {
	        Scanner scanner = new Scanner(System.in);

	        // Get product name and new quantity as input
	        System.out.print("Enter product name: ");
	        String productName = scanner.nextLine();
	        System.out.print("Enter new quantity: ");
	        int newQuantity = scanner.nextInt();

	        // Get product details from the products table
	        String getProductDetailsQuery = "SELECT id, rate, stock FROM products WHERE name = ?";
	        int productId = 0;
	        double productPrice = 0.0;
	        int stock = 0;
	        try (PreparedStatement getProductDetailsStatement = connection.prepareStatement(getProductDetailsQuery)) {
	            getProductDetailsStatement.setString(1, productName);
	            try (ResultSet resultSet = getProductDetailsStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    productId = resultSet.getInt("id");
	                    productPrice = resultSet.getDouble("rate");
	                    stock = resultSet.getInt("stock");
	                } else {
	                    System.out.println("Product not found.");
	                    return;
	                }
	            }
	        }

	        // Check if the product exists in the bill_items table
	        String getBillItemQuery = "SELECT id, quantity, price FROM bill_items WHERE bill_id = ? AND product_id = ?";
	        int billItemId = 0;
	        int oldQuantity = 0;
	        double oldPrice = 0.0;
	        try (PreparedStatement getBillItemStatement = connection.prepareStatement(getBillItemQuery)) {
	            getBillItemStatement.setInt(1, billId);
	            getBillItemStatement.setInt(2, productId);
	            try (ResultSet resultSet = getBillItemStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    billItemId = resultSet.getInt("id");
	                    oldQuantity = resultSet.getInt("quantity");
	                    oldPrice = resultSet.getDouble("price");
	                } else {
	                    System.out.println("Product not found in the bill.");
	                    return;
	                }
	            }
	        }

	        // Update the quantity and price in the bill item
	        if (newQuantity > stock) {
	            System.out.println("Insufficient stock.");
	            return;
	        }
	        String updateProductQuery = "UPDATE bill_items SET quantity = ?, price = ? WHERE id = ?";
	        try (PreparedStatement updateProductStatement = connection.prepareStatement(updateProductQuery)) {
	            updateProductStatement.setInt(1, newQuantity);
	            updateProductStatement.setDouble(2, productPrice * newQuantity);
	            updateProductStatement.setInt(3, billItemId);
	            updateProductStatement.executeUpdate();
	        }

	        // Update the bill totals
	        double amountDifference = (productPrice * newQuantity) - (oldPrice * oldQuantity);
	        updateBillTotals(connection, billId, newQuantity - oldQuantity, amountDifference);
	        
	        //Update bill items
	        updateBillItem(connection, billId, productId, newQuantity, productPrice);

	        System.out.println("Product updated in the bill successfully.");
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static void updateBillTotals(Connection connection, int billId, int quantityDifference, double amountDifference) throws SQLException {
	    String updateBillQuery = "UPDATE bills SET tot_quantity = tot_quantity + ?, total = total + ? WHERE id = ?";
	    try (PreparedStatement updateBillStatement = connection.prepareStatement(updateBillQuery)) {
	        updateBillStatement.setInt(1, quantityDifference);
	        updateBillStatement.setDouble(2, amountDifference);
	        updateBillStatement.setInt(3, billId);
	        updateBillStatement.executeUpdate();
	    }
	}
	public static void updateBillItem(Connection connection, int billId, int productId, int quantity, double price) throws SQLException {
	    String query = "UPDATE bill_items SET quantity = ?, price = ? WHERE bill_id = ? AND product_id = ?";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
	        preparedStatement.setInt(1, quantity);
	        preparedStatement.setDouble(2, price);
	        preparedStatement.setInt(3, billId);
	        preparedStatement.setInt(4, productId);
	        preparedStatement.executeUpdate();
	    }
	}
//=====================================================Delete Product==========================================================
	public static void deleteProductFromBill(Connection connection, int billId, int customerId) {
	    try {
	        Scanner scanner = new Scanner(System.in);
	        System.out.print("Enter the product name: ");
	        String productName = scanner.nextLine().trim(); // Trim to remove leading/trailing whitespace

	        // Get the bill item ID based on the provided bill ID, customer ID, and product name
	        String getBillItemIdQuery = "SELECT bi.id " +
	                                     "FROM bill_items bi " +
	                                     "JOIN bills b ON bi.bill_id = b.id " +
	                                     "JOIN products p ON bi.product_id = p.id " +
	                                     "WHERE b.id = ? AND b.customer_id = ? AND LOWER(p.name) = LOWER(?)"; // Case-insensitive comparison
	        try (PreparedStatement getBillItemIdStatement = connection.prepareStatement(getBillItemIdQuery)) {
	            getBillItemIdStatement.setInt(1, billId);
	            getBillItemIdStatement.setInt(2, customerId);
	            getBillItemIdStatement.setString(3, productName);
	            try (ResultSet resultSet = getBillItemIdStatement.executeQuery()) {
	                if (resultSet.next()) {
	                    int billItemId = resultSet.getInt("id");

	                    // Get the quantity and price of the product in the bill item
	                    String getProductDetailsQuery = "SELECT quantity, price FROM bill_items WHERE id = ?";
	                    try (PreparedStatement getProductDetailsStatement = connection.prepareStatement(getProductDetailsQuery)) {
	                        getProductDetailsStatement.setInt(1, billItemId);
	                        try (ResultSet productResultSet = getProductDetailsStatement.executeQuery()) {
	                            if (productResultSet.next()) {
	                                int quantity = productResultSet.getInt("quantity");
	                                double price = productResultSet.getDouble("price");

	                                // Delete the bill item
	                                String deleteBillItemQuery = "DELETE FROM bill_items WHERE id = ?";
	                                try (PreparedStatement deleteBillItemStatement = connection.prepareStatement(deleteBillItemQuery)) {
	                                    deleteBillItemStatement.setInt(1, billItemId);
	                                    deleteBillItemStatement.executeUpdate();

	                                    // Update the bill totals
	                                    updateBillTotals(connection, billId, -quantity, -price);

	                                    System.out.println("Product deleted from the bill successfully.");
	                                }
	                            }
	                        }
	                    }
	                } else {
	                    System.out.println("Product not found.");
	                }
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public static void deleteBillItem(Connection connection, int billId, String productName) throws SQLException {
		String deleteQuery = 
		        "DELETE FROM bill_items " +
		        "WHERE bill_id = ? AND product_id = ( " +
		        "  SELECT p.id FROM products p WHERE p.name = ? " +
		        ")";
	    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
	        preparedStatement.setInt(1, billId);
	        preparedStatement.setString(2, productName);
	        preparedStatement.executeUpdate();
	    }
	}
//=======================================================Display product==================================================== 	
	public static void displayProductFromBill(Connection connection, int billId, int customerId) throws SQLException {
		Scanner scanner = new Scanner(System.in);
	    String query = "SELECT b.id AS BillID, b.payment_method AS PaymentMethod, b.bill_date AS BillDate, " +
	                   "p.name AS ProductName, p.price AS MRP, bi.price AS Rate, bi.quantity AS Quantity, " +
	                   "(bi.price * bi.quantity) AS Amount " +
	                   "FROM bills b " +
	                   "JOIN bill_items bi ON b.id = bi.bill_id " +
	                   "JOIN products p ON bi.product_id = p.id " +
	                   "WHERE b.id = ? AND b.customer_id = ?";
	    try (PreparedStatement statement = connection.prepareStatement(query)) {
	        statement.setInt(1, billId);
	        statement.setInt(2, customerId);
	        try (ResultSet resultSet = statement.executeQuery()) {
	            System.out.println("--------------------------------------------------------------------------------------------------------");
	            System.out.printf("| %-8s | %-20s | %-20s | %-10s | %-8s | %-8s | %-8s |%n",
	                              "BillID", "BillDate", "ProductName", "MRP", "Rate", "Quantity", "Amount");
	            System.out.println("--------------------------------------------------------------------------------------------------------");
	            int totalQuantity = 0;
	            double totalAmount = 0;
	            while (resultSet.next()) {
	                int billID = resultSet.getInt("BillID");
	                String paymentMethod = resultSet.getString("PaymentMethod");
	                Timestamp billDate = resultSet.getTimestamp("BillDate");
	                String productName = resultSet.getString("ProductName");
	                double mrp = resultSet.getDouble("MRP");
	                double rate = resultSet.getDouble("Rate");
	                int quantity = resultSet.getInt("Quantity");
	                double amount = resultSet.getDouble("Amount");

	                totalQuantity += quantity;
	                totalAmount += amount;

	                System.out.printf("| %-8d | %-20s | %-20s | %-10.2f | %-8.2f | %-8d | %-8.2f |%n",
	                                  billID, billDate, productName, mrp, rate, quantity, amount);
	            }
	            System.out.println("---------------------------------------------------------------------------------------------------------");
	            System.out.printf("| %-79s | %-9d | %-9.2f |%n", "Total", totalQuantity, totalAmount);
	            System.out.println("---------------------------------------------------------------------------------------------------------");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    System.out.print("Would you like to proceed with payment methods? (yes/no): ");
        String proceedPayment = scanner.next();
        if ("yes".equalsIgnoreCase(proceedPayment)) {
        	proceedToPayment(connection,billId,customerId);
        	return;
        } 
      else {
            System.out.println("Returning to main menu.");
            return;
        }
	}
	public static void proceedToPayment(Connection connection, int billId, int customerId) throws SQLException {
        System.out.println("Payment method (cash, credit_card, upi) ");

        handlePaymentMethods(connection, billId, customerId);
        System.out.println("Payment completed successfully. Thank you for your purchase!");
    }

//**************************************************payment methods***********************************************************
//========================================================UPI Payment=========================================================	
	public static boolean handleUPIPayment(Connection connection, int billId, int customerId) {
	    Scanner scanner = new Scanner(System.in);

	    System.out.print("Enter UPI ID (e.g., user@bank): ");
	    String upiId = scanner.next();

	    System.out.print("Enter transaction reference number (e.g., 1234567890): ");
	    String transactionRef = scanner.next();

	    try {
	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'upi' WHERE id = ? AND customer_id = ?";
	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
	            statement.setInt(1, billId);
	            statement.setInt(2, customerId);
	            statement.executeUpdate();
	        }

	        System.out.println("Payment successful using UPI.");
	        System.out.println("UPI ID: " + upiId);
	        System.out.println("Transaction Reference Number: " + transactionRef);

	        displayProductAfterBill(connection, billId, customerId);

	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public static boolean handleCashPayment(Connection connection, int billId, int customerId) {
	    Scanner scanner = new Scanner(System.in);

	    try {
	        String getTotalAmountQuery = "SELECT SUM(price * quantity) AS total FROM bill_items WHERE bill_id = ?";
	        double totalAmount = 0.0;
	        try (PreparedStatement statement = connection.prepareStatement(getTotalAmountQuery)) {
	            statement.setInt(1, billId);
	            try (ResultSet resultSet = statement.executeQuery()) {
	                if (resultSet.next()) {
	                    totalAmount = resultSet.getDouble("total");
	                } else {
	                    System.out.println("No matching bill items found.");
	                    return false;
	                }
	            }
	        }
	        System.out.println("Total Amount is : " + totalAmount);

	        System.out.print("Enter amount tendered: ");
	        double amountTendered;
	        while (true) {
	            if (scanner.hasNextDouble()) {
	                amountTendered = scanner.nextDouble();
	                if (amountTendered >= 0) {
	                    break;
	                } else {
	                    System.out.println("Please enter a positive amount:");
	                }
	            } else {
	                System.out.println("Invalid input. Please enter a numeric value:");
	                scanner.next();
	            }
	        }

	        if (amountTendered < totalAmount) {
	            System.out.println("Insufficient amount tendered. Please provide enough cash to cover the bill.");
	            return false;
	        }

	        double changeDue = amountTendered - totalAmount;

	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'cash' WHERE id = ? AND customer_id = ?";
	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
	            statement.setInt(1, billId);
	            statement.setInt(2, customerId);
	            statement.executeUpdate();
	        }

	        System.out.println("Payment successful using Cash.");
	        System.out.printf("Total Amount: %.2f%n", totalAmount);
	        System.out.printf("Amount Tendered: %.2f%n", amountTendered);
	        System.out.printf("Change Due: %.2f%n", changeDue);

	        displayProductAfterBill(connection, billId, customerId);

	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public static boolean handleCreditCardPayment(Connection connection, int billId, int customerId) {
	    Scanner scanner = new Scanner(System.in);

	    try {
	        String getTotalAmountQuery = "SELECT SUM(price * quantity) AS total FROM bill_items WHERE bill_id = ?";
	        double totalAmount = 0.0;
	        try (PreparedStatement statement = connection.prepareStatement(getTotalAmountQuery)) {
	            statement.setInt(1, billId);
	            try (ResultSet resultSet = statement.executeQuery()) {
	                if (resultSet.next()) {
	                    totalAmount = resultSet.getDouble("total");
	                } else {
	                    System.out.println("No matching bill found.");
	                    return false;
	                }
	            }
	        }

	        System.out.println("Enter credit card details for payment");
	        System.out.print("Enter credit card number (e.g., 1234567812345678): ");
	        String cardNumber = scanner.next();
	        System.out.print("Enter card expiry date (MM/YY, e.g., 12/25): ");
	        String expiryDate = scanner.next();
	        System.out.print("Enter CVV (e.g., 123): ");
	        String cvv = scanner.next();

	        boolean isAuthorized = authorizeCreditCard(cardNumber, expiryDate, cvv, totalAmount);

	        if (!isAuthorized) {
	            System.out.println("Credit card authorization failed. Please try again.");
	            return false;
	        }

	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'credit_card' WHERE id = ? AND customer_id = ?";
	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
	            statement.setInt(1, billId);
	            statement.setInt(2, customerId);
	            statement.executeUpdate();
	        }

	        System.out.println("Payment successful using Credit Card.");
	        System.out.printf("Total Amount: %.2f%n", totalAmount);

	        displayProductAfterBill(connection, billId, customerId);

	        return true;
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	public static boolean authorizeCreditCard(String cardNumber, String expiryDate, String cvv, double amount) {
	    // Dummy authorization logic for the credit card
	    if (cardNumber.length() == 16 && cvv.length() == 3) {
	        return true;
	    }
	    return false;
	}

    
    public static void displayProductAfterBill(Connection connection, int billId, int customerId) {
        String query = "SELECT b.id AS BillID, b.payment_method AS PaymentMethod, b.bill_date AS BillDate, " +
                "p.name AS ProductName, p.price AS MRP, bi.price AS Rate, bi.quantity AS Quantity, " +
                "(bi.price * bi.quantity) AS Amount " +
                "FROM bills b " +
                "JOIN bill_items bi ON b.id = bi.bill_id " +
                "JOIN products p ON bi.product_id = p.id " +
                "WHERE b.id = ? AND b.customer_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, billId);
            statement.setInt(2, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("\n-------------------------------------------------------------------------");
                System.out.println("                         Nellai Mart              ");
                System.out.println("            123, Main Bazaar Street, Tirunelveli  ");
                System.out.println("-------------------------------------------------------------------------");
                System.out.printf("Bill ID: %d%n", billId);
                System.out.printf("Customer ID: %d%n", customerId);
                System.out.println("=========================================================================");
                System.out.printf("%-20s %-10s %-10s %-8s %-8s%n", "Product Name", "MRP", "Rate", "Qty", "Amount");
                System.out.println("=========================================================================");

                int totalQuantity = 0;
                double totalAmount = 0;
                boolean hasItems = false; // Flag to track if any items are found in the bill
                while (resultSet.next()) {
                    String productName = resultSet.getString("ProductName");
                    double mrp = resultSet.getDouble("MRP");
                    double rate = resultSet.getDouble("Rate");
                    int quantity = resultSet.getInt("Quantity");
                    double amount = resultSet.getDouble("Amount");

                    totalQuantity += quantity;
                    totalAmount += amount;
                    hasItems = true;

                    System.out.printf("%-20s %-10.2f %-10.2f %-8d %-8.2f%n", productName, mrp, rate, quantity, amount);
                }

                if (!hasItems) {
                    System.out.println("No items found in the bill.");
                } else {
                    System.out.println("=========================================================================");
                    System.out.printf("%-20s %-10s %-10s %-8d %-8.2f%n", "Total", "", "", totalQuantity, totalAmount);
                    System.out.println("=========================================================================");

                    // Get payment method
                    String getPaymentMethodQuery = "SELECT payment_method FROM bills WHERE id = ?";
                    try (PreparedStatement paymentStatement = connection.prepareStatement(getPaymentMethodQuery)) {
                        paymentStatement.setInt(1, billId);
                        try (ResultSet paymentResultSet = paymentStatement.executeQuery()) {
                            if (paymentResultSet.next()) {
                                String paymentMethod = paymentResultSet.getString("payment_method");
                                System.out.printf("Payment Method: %s%n", paymentMethod);
                            }
                        }
                    }

                    System.out.println("=========================================================================");
                    System.out.println("                   Thank you for shopping with us! ");
                    System.out.println("=========================================================================");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//====================================================Feedback method==========================================================
    public static void addFeedback(Connection connection, int customerId) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your feedback rating (1-5): ");
        int rating;
        while (true) {
            if (scanner.hasNextInt()) {
                rating = scanner.nextInt();
                if (rating >= 1 && rating <= 5) {
                    break;
                } else {
                    System.out.println("Please enter a rating between 1 and 5:");
                }
            } else {
                System.out.println("Invalid input. Please enter a numeric value:");
                scanner.next();
            }
        }
        scanner.nextLine(); // Consume newline character

        System.out.print("Enter your comments: ");
        String comments = scanner.nextLine();

        // Get current date
        LocalDate feedbackDate = LocalDate.now();

        String query = "INSERT INTO feedback (customer_id, feedback_date, rating, comments) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, customerId);
            preparedStatement.setDate(2, Date.valueOf(feedbackDate));
            preparedStatement.setInt(3, rating);
            preparedStatement.setString(4, comments);
            preparedStatement.executeUpdate();
            System.out.println("Thank you for your feedback!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

  //*************************************handleCustomerAndStockReports****************************************************

      public static void handleCustomerAndStockReports(Connection connection) throws SQLException {
          Scanner scanner = new Scanner(System.in);

          // Display customer and stock reports menu options
          System.out.println("Customer and Stock Reports Menu:");
          System.out.println("1. Customer Report");
          System.out.println("2. Stock Report");
          System.out.println("3. Re-Stocking Product");
          System.out.print("Enter your choice: ");
          int choice = scanner.nextInt();

          switch (choice) {
              case 1:
                  handleCustomerReportOptions(connection);
                  break;
              case 2:
                  handleStockReportOptions(connection);
                  break;
              case 3:
              	restockingOptions(connection);
                  break;
              default:
                  System.out.println("Invalid choice. Returning to main menu.");
                  break;
          }
      }
   //************************************************Functions executions***********************************************************  
   //=======================================================Insert Product==========================================================
      public static void insertProductIntoBill(Connection connection, int billId, int customerId) throws SQLException {
          try {
              Scanner scanner = new Scanner(System.in);

              // Ask for product name
              System.out.print("Enter product name: ");
              String productName = scanner.nextLine();

              // Query to retrieve product details
              String query = "SELECT id, stock, rate FROM products WHERE name = ?";
              try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                  preparedStatement.setString(1, productName);
                  try (ResultSet resultSet = preparedStatement.executeQuery()) {
                      if (resultSet.next()) {
                          int productId = resultSet.getInt("id");
                          double rate = resultSet.getDouble("rate");
                          int stock = resultSet.getInt("stock");

                          // Ask for quantity
                          System.out.print("Enter quantity: ");
                          int quantity = scanner.nextInt();

                          // Query to get the total quantity already added to the bill
                          String totalQuantityQuery = "SELECT IFNULL(SUM(quantity), 0) AS totalQuantity FROM bill_items WHERE bill_id = ? AND product_id = ?";
                          try (PreparedStatement totalQuantityStatement = connection.prepareStatement(totalQuantityQuery)) {
                              totalQuantityStatement.setInt(1, billId);
                              totalQuantityStatement.setInt(2, productId);
                              try (ResultSet totalQuantityResultSet = totalQuantityStatement.executeQuery()) {
                                  if (totalQuantityResultSet.next()) {
                                      int totalQuantityInBill = totalQuantityResultSet.getInt("totalQuantity");
                                      int availableStock = stock - totalQuantityInBill;

                                      // Check if stock is sufficient
                                      if (availableStock >= quantity) {
                                          // Update total quantity and total amount
                                          double amount = rate * quantity;
                                          updateBillTotals(connection, billId, quantity, amount);

                                          // Insert the product into the bill_items table
                                          insertBillItem(connection, billId, productId, quantity, rate);

                                          System.out.println("Product added to the bill successfully.");
                                      } else {
                                          // Insufficient stock
                                          System.out.println("Insufficient stock for the selected product.");
                                      }
                                  }
                              }
                          }
                      } else {
                          System.out.println("Product not found.");
                      }
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

  	public static void insertBillItem(Connection connection, int billId, int productId, int quantity, double price) throws SQLException {
  	    String query = "INSERT INTO bill_items (bill_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
  	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
  	        preparedStatement.setInt(1, billId);
  	        preparedStatement.setInt(2, productId);
  	        preparedStatement.setInt(3, quantity);
  	        preparedStatement.setDouble(4, price);
  	        preparedStatement.executeUpdate();
  	    }
  	}
  //====================================================Update Product===========================================================	
  	public static void updateProductIntoBill(Connection connection, int billId, int customerId) {
  	    try {
  	        Scanner scanner = new Scanner(System.in);

  	        // Get product name and new quantity as input
  	        System.out.print("Enter product name: ");
  	        String productName = scanner.nextLine();
  	        System.out.print("Enter new quantity: ");
  	        int newQuantity = scanner.nextInt();

  	        // Get product details from the products table
  	        String getProductDetailsQuery = "SELECT id, rate, stock FROM products WHERE name = ?";
  	        int productId = 0;
  	        double productPrice = 0.0;
  	        int stock = 0;
  	        try (PreparedStatement getProductDetailsStatement = connection.prepareStatement(getProductDetailsQuery)) {
  	            getProductDetailsStatement.setString(1, productName);
  	            try (ResultSet resultSet = getProductDetailsStatement.executeQuery()) {
  	                if (resultSet.next()) {
  	                    productId = resultSet.getInt("id");
  	                    productPrice = resultSet.getDouble("rate");
  	                    stock = resultSet.getInt("stock");
  	                } else {
  	                    System.out.println("Product not found.");
  	                    return;
  	                }
  	            }
  	        }

  	        // Check if the product exists in the bill_items table
  	        String getBillItemQuery = "SELECT id, quantity, price FROM bill_items WHERE bill_id = ? AND product_id = ?";
  	        int billItemId = 0;
  	        int oldQuantity = 0;
  	        double oldPrice = 0.0;
  	        try (PreparedStatement getBillItemStatement = connection.prepareStatement(getBillItemQuery)) {
  	            getBillItemStatement.setInt(1, billId);
  	            getBillItemStatement.setInt(2, productId);
  	            try (ResultSet resultSet = getBillItemStatement.executeQuery()) {
  	                if (resultSet.next()) {
  	                    billItemId = resultSet.getInt("id");
  	                    oldQuantity = resultSet.getInt("quantity");
  	                    oldPrice = resultSet.getDouble("price");
  	                } else {
  	                    System.out.println("Product not found in the bill.");
  	                    return;
  	                }
  	            }
  	        }

  	        // Update the quantity and price in the bill item
  	        if (newQuantity > stock) {
  	            System.out.println("Insufficient stock.");
  	            return;
  	        }
  	        String updateProductQuery = "UPDATE bill_items SET quantity = ?, price = ? WHERE id = ?";
  	        try (PreparedStatement updateProductStatement = connection.prepareStatement(updateProductQuery)) {
  	            updateProductStatement.setInt(1, newQuantity);
  	            updateProductStatement.setDouble(2, productPrice * newQuantity);
  	            updateProductStatement.setInt(3, billItemId);
  	            updateProductStatement.executeUpdate();
  	        }

  	        // Update the bill totals
  	        double amountDifference = (productPrice * newQuantity) - (oldPrice * oldQuantity);
  	        updateBillTotals(connection, billId, newQuantity - oldQuantity, amountDifference);
  	        
  	        //Update bill items
  	        updateBillItem(connection, billId, productId, newQuantity, productPrice);

  	        System.out.println("Product updated in the bill successfully.");
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	    }
  	}

  	public static void updateBillTotals(Connection connection, int billId, int quantityDifference, double amountDifference) throws SQLException {
  	    String updateBillQuery = "UPDATE bills SET tot_quantity = tot_quantity + ?, total = total + ? WHERE id = ?";
  	    try (PreparedStatement updateBillStatement = connection.prepareStatement(updateBillQuery)) {
  	        updateBillStatement.setInt(1, quantityDifference);
  	        updateBillStatement.setDouble(2, amountDifference);
  	        updateBillStatement.setInt(3, billId);
  	        updateBillStatement.executeUpdate();
  	    }
  	}
  	public static void updateBillItem(Connection connection, int billId, int productId, int quantity, double price) throws SQLException {
  	    String query = "UPDATE bill_items SET quantity = ?, price = ? WHERE bill_id = ? AND product_id = ?";
  	    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
  	        preparedStatement.setInt(1, quantity);
  	        preparedStatement.setDouble(2, price);
  	        preparedStatement.setInt(3, billId);
  	        preparedStatement.setInt(4, productId);
  	        preparedStatement.executeUpdate();
  	    }
  	}
  //=====================================================Delete Product==========================================================
  	public static void deleteProductFromBill(Connection connection, int billId, int customerId) {
  	    try {
  	        Scanner scanner = new Scanner(System.in);
  	        System.out.print("Enter the product name: ");
  	        String productName = scanner.nextLine().trim(); // Trim to remove leading/trailing whitespace

  	        // Get the bill item ID based on the provided bill ID, customer ID, and product name
  	        String getBillItemIdQuery = "SELECT bi.id " +
  	                                     "FROM bill_items bi " +
  	                                     "JOIN bills b ON bi.bill_id = b.id " +
  	                                     "JOIN products p ON bi.product_id = p.id " +
  	                                     "WHERE b.id = ? AND b.customer_id = ? AND LOWER(p.name) = LOWER(?)"; // Case-insensitive comparison
  	        try (PreparedStatement getBillItemIdStatement = connection.prepareStatement(getBillItemIdQuery)) {
  	            getBillItemIdStatement.setInt(1, billId);
  	            getBillItemIdStatement.setInt(2, customerId);
  	            getBillItemIdStatement.setString(3, productName);
  	            try (ResultSet resultSet = getBillItemIdStatement.executeQuery()) {
  	                if (resultSet.next()) {
  	                    int billItemId = resultSet.getInt("id");

  	                    // Get the quantity and price of the product in the bill item
  	                    String getProductDetailsQuery = "SELECT quantity, price FROM bill_items WHERE id = ?";
  	                    try (PreparedStatement getProductDetailsStatement = connection.prepareStatement(getProductDetailsQuery)) {
  	                        getProductDetailsStatement.setInt(1, billItemId);
  	                        try (ResultSet productResultSet = getProductDetailsStatement.executeQuery()) {
  	                            if (productResultSet.next()) {
  	                                int quantity = productResultSet.getInt("quantity");
  	                                double price = productResultSet.getDouble("price");

  	                                // Delete the bill item
  	                                String deleteBillItemQuery = "DELETE FROM bill_items WHERE id = ?";
  	                                try (PreparedStatement deleteBillItemStatement = connection.prepareStatement(deleteBillItemQuery)) {
  	                                    deleteBillItemStatement.setInt(1, billItemId);
  	                                    deleteBillItemStatement.executeUpdate();

  	                                    // Update the bill totals
  	                                    updateBillTotals(connection, billId, -quantity, -price);

  	                                    System.out.println("Product deleted from the bill successfully.");
  	                                }
  	                            }
  	                        }
  	                    }
  	                } else {
  	                    System.out.println("Product not found.");
  	                }
  	            }
  	        }
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	    }
  	}

  	public static void deleteBillItem(Connection connection, int billId, String productName) throws SQLException {
  		String deleteQuery = 
  		        "DELETE FROM bill_items " +
  		        "WHERE bill_id = ? AND product_id = ( " +
  		        "  SELECT p.id FROM products p WHERE p.name = ? " +
  		        ")";
  	    try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
  	        preparedStatement.setInt(1, billId);
  	        preparedStatement.setString(2, productName);
  	        preparedStatement.executeUpdate();
  	    }
  	}
  //=======================================================Display product==================================================== 	
  	public static void displayProductFromBill(Connection connection, int billId, int customerId) throws SQLException {
  		Scanner scanner = new Scanner(System.in);
  	    String query = "SELECT b.id AS BillID, b.payment_method AS PaymentMethod, b.bill_date AS BillDate, " +
  	                   "p.name AS ProductName, p.price AS MRP, bi.price AS Rate, bi.quantity AS Quantity, " +
  	                   "(bi.price * bi.quantity) AS Amount " +
  	                   "FROM bills b " +
  	                   "JOIN bill_items bi ON b.id = bi.bill_id " +
  	                   "JOIN products p ON bi.product_id = p.id " +
  	                   "WHERE b.id = ? AND b.customer_id = ?";
  	    try (PreparedStatement statement = connection.prepareStatement(query)) {
  	        statement.setInt(1, billId);
  	        statement.setInt(2, customerId);
  	        try (ResultSet resultSet = statement.executeQuery()) {
  	            System.out.println("--------------------------------------------------------------------------------------------------------");
  	            System.out.printf("| %-8s | %-20s | %-20s | %-10s | %-8s | %-8s | %-8s |%n",
  	                              "BillID", "BillDate", "ProductName", "MRP", "Rate", "Quantity", "Amount");
  	            System.out.println("--------------------------------------------------------------------------------------------------------");
  	            int totalQuantity = 0;
  	            double totalAmount = 0;
  	            while (resultSet.next()) {
  	                int billID = resultSet.getInt("BillID");
  	                String paymentMethod = resultSet.getString("PaymentMethod");
  	                Timestamp billDate = resultSet.getTimestamp("BillDate");
  	                String productName = resultSet.getString("ProductName");
  	                double mrp = resultSet.getDouble("MRP");
  	                double rate = resultSet.getDouble("Rate");
  	                int quantity = resultSet.getInt("Quantity");
  	                double amount = resultSet.getDouble("Amount");

  	                totalQuantity += quantity;
  	                totalAmount += amount;

  	                System.out.printf("| %-8d | %-20s | %-20s | %-10.2f | %-8.2f | %-8d | %-8.2f |%n",
  	                                  billID, billDate, productName, mrp, rate, quantity, amount);
  	            }
  	            System.out.println("---------------------------------------------------------------------------------------------------------");
  	            System.out.printf("| %-79s | %-9d | %-9.2f |%n", "Total", totalQuantity, totalAmount);
  	            System.out.println("---------------------------------------------------------------------------------------------------------");
  	        }
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	    }
  	    System.out.print("Would you like to proceed with payment methods? (yes/no): ");
          String proceedPayment = scanner.next();
          if ("yes".equalsIgnoreCase(proceedPayment)) {
          	proceedToPayment(connection,billId,customerId);
          	return;
          } 
        else {
              System.out.println("Returning to main menu.");
              return;
          }
  	}
  	public static void proceedToPayment(Connection connection, int billId, int customerId) throws SQLException {
          System.out.println("Payment method (cash, credit_card, upi) ");

          handlePaymentMethods(connection, billId, customerId);
          System.out.println("Payment completed successfully. Thank you for your purchase!");
      }

  //**************************************************payment methods***********************************************************
  //========================================================UPI Payment=========================================================	
  	public static boolean handleUPIPayment(Connection connection, int billId, int customerId) {
  	    Scanner scanner = new Scanner(System.in);

  	    System.out.print("Enter UPI ID (e.g., user@bank): ");
  	    String upiId = scanner.next();

  	    System.out.print("Enter transaction reference number (e.g., 1234567890): ");
  	    String transactionRef = scanner.next();

  	    try {
  	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'upi' WHERE id = ? AND customer_id = ?";
  	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
  	            statement.setInt(1, billId);
  	            statement.setInt(2, customerId);
  	            statement.executeUpdate();
  	        }

  	        System.out.println("Payment successful using UPI.");
  	        System.out.println("UPI ID: " + upiId);
  	        System.out.println("Transaction Reference Number: " + transactionRef);

  	        displayProductAfterBill(connection, billId, customerId);

  	        return true;
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	        return false;
  	    }
  	}

  	public static boolean handleCashPayment(Connection connection, int billId, int customerId) {
  	    Scanner scanner = new Scanner(System.in);

  	    try {
  	        String getTotalAmountQuery = "SELECT SUM(price * quantity) AS total FROM bill_items WHERE bill_id = ?";
  	        double totalAmount = 0.0;
  	        try (PreparedStatement statement = connection.prepareStatement(getTotalAmountQuery)) {
  	            statement.setInt(1, billId);
  	            try (ResultSet resultSet = statement.executeQuery()) {
  	                if (resultSet.next()) {
  	                    totalAmount = resultSet.getDouble("total");
  	                } else {
  	                    System.out.println("No matching bill items found.");
  	                    return false;
  	                }
  	            }
  	        }
  	        System.out.println("Total Amount is : " + totalAmount);

  	        System.out.print("Enter amount tendered: ");
  	        double amountTendered;
  	        while (true) {
  	            if (scanner.hasNextDouble()) {
  	                amountTendered = scanner.nextDouble();
  	                if (amountTendered >= 0) {
  	                    break;
  	                } else {
  	                    System.out.println("Please enter a positive amount:");
  	                }
  	            } else {
  	                System.out.println("Invalid input. Please enter a numeric value:");
  	                scanner.next();
  	            }
  	        }

  	        if (amountTendered < totalAmount) {
  	            System.out.println("Insufficient amount tendered. Please provide enough cash to cover the bill.");
  	            return false;
  	        }

  	        double changeDue = amountTendered - totalAmount;

  	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'cash' WHERE id = ? AND customer_id = ?";
  	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
  	            statement.setInt(1, billId);
  	            statement.setInt(2, customerId);
  	            statement.executeUpdate();
  	        }

  	        System.out.println("Payment successful using Cash.");
  	        System.out.printf("Total Amount: %.2f%n", totalAmount);
  	        System.out.printf("Amount Tendered: %.2f%n", amountTendered);
  	        System.out.printf("Change Due: %.2f%n", changeDue);

  	        displayProductAfterBill(connection, billId, customerId);

  	        return true;
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	        return false;
  	    }
  	}

  	public static boolean handleCreditCardPayment(Connection connection, int billId, int customerId) {
  	    Scanner scanner = new Scanner(System.in);

  	    try {
  	        String getTotalAmountQuery = "SELECT SUM(price * quantity) AS total FROM bill_items WHERE bill_id = ?";
  	        double totalAmount = 0.0;
  	        try (PreparedStatement statement = connection.prepareStatement(getTotalAmountQuery)) {
  	            statement.setInt(1, billId);
  	            try (ResultSet resultSet = statement.executeQuery()) {
  	                if (resultSet.next()) {
  	                    totalAmount = resultSet.getDouble("total");
  	                } else {
  	                    System.out.println("No matching bill found.");
  	                    return false;
  	                }
  	            }
  	        }

  	        System.out.println("Enter credit card details for payment");
  	        System.out.print("Enter credit card number (e.g., 1234567812345678): ");
  	        String cardNumber = scanner.next();
  	        System.out.print("Enter card expiry date (MM/YY, e.g., 12/25): ");
  	        String expiryDate = scanner.next();
  	        System.out.print("Enter CVV (e.g., 123): ");
  	        String cvv = scanner.next();

  	        boolean isAuthorized = authorizeCreditCard(cardNumber, expiryDate, cvv, totalAmount);

  	        if (!isAuthorized) {
  	            System.out.println("Credit card authorization failed. Please try again.");
  	            return false;
  	        }

  	        String updatePaymentMethodQuery = "UPDATE bills SET payment_method = 'credit_card' WHERE id = ? AND customer_id = ?";
  	        try (PreparedStatement statement = connection.prepareStatement(updatePaymentMethodQuery)) {
  	            statement.setInt(1, billId);
  	            statement.setInt(2, customerId);
  	            statement.executeUpdate();
  	        }

  	        System.out.println("Payment successful using Credit Card.");
  	        System.out.printf("Total Amount: %.2f%n", totalAmount);

  	        displayProductAfterBill(connection, billId, customerId);

  	        return true;
  	    } catch (SQLException e) {
  	        e.printStackTrace();
  	        return false;
  	    }
  	}

  	public static boolean authorizeCreditCard(String cardNumber, String expiryDate, String cvv, double amount) {
  	    // Dummy authorization logic for the credit card
  	    if (cardNumber.length() == 16 && cvv.length() == 3) {
  	        return true;
  	    }
  	    return false;
  	}

      
      public static void displayProductAfterBill(Connection connection, int billId, int customerId) {
          String query = "SELECT b.id AS BillID, b.payment_method AS PaymentMethod, b.bill_date AS BillDate, " +
                  "p.name AS ProductName, p.price AS MRP, bi.price AS Rate, bi.quantity AS Quantity, " +
                  "(bi.price * bi.quantity) AS Amount " +
                  "FROM bills b " +
                  "JOIN bill_items bi ON b.id = bi.bill_id " +
                  "JOIN products p ON bi.product_id = p.id " +
                  "WHERE b.id = ? AND b.customer_id = ?";
          try (PreparedStatement statement = connection.prepareStatement(query)) {
              statement.setInt(1, billId);
              statement.setInt(2, customerId);
              try (ResultSet resultSet = statement.executeQuery()) {
                  System.out.println("\n-------------------------------------------------------------------------");
                  System.out.println("                         Nellai Mart              ");
                  System.out.println("            123, Main Bazaar Street, Tirunelveli  ");
                  System.out.println("-------------------------------------------------------------------------");
                  System.out.printf("Bill ID: %d%n", billId);
                  System.out.printf("Customer ID: %d%n", customerId);
                  System.out.println("=========================================================================");
                  System.out.printf("%-20s %-10s %-10s %-8s %-8s%n", "Product Name", "MRP", "Rate", "Qty", "Amount");
                  System.out.println("=========================================================================");

                  int totalQuantity = 0;
                  double totalAmount = 0;
                  boolean hasItems = false; // Flag to track if any items are found in the bill
                  while (resultSet.next()) {
                      String productName = resultSet.getString("ProductName");
                      double mrp = resultSet.getDouble("MRP");
                      double rate = resultSet.getDouble("Rate");
                      int quantity = resultSet.getInt("Quantity");
                      double amount = resultSet.getDouble("Amount");

                      totalQuantity += quantity;
                      totalAmount += amount;
                      hasItems = true;

                      System.out.printf("%-20s %-10.2f %-10.2f %-8d %-8.2f%n", productName, mrp, rate, quantity, amount);
                  }

                  if (!hasItems) {
                      System.out.println("No items found in the bill.");
                  } else {
                      System.out.println("=========================================================================");
                      System.out.printf("%-20s %-10s %-10s %-8d %-8.2f%n", "Total", "", "", totalQuantity, totalAmount);
                      System.out.println("=========================================================================");

                      // Get payment method
                      String getPaymentMethodQuery = "SELECT payment_method FROM bills WHERE id = ?";
                      try (PreparedStatement paymentStatement = connection.prepareStatement(getPaymentMethodQuery)) {
                          paymentStatement.setInt(1, billId);
                          try (ResultSet paymentResultSet = paymentStatement.executeQuery()) {
                              if (paymentResultSet.next()) {
                                  String paymentMethod = paymentResultSet.getString("payment_method");
                                  System.out.printf("Payment Method: %s%n", paymentMethod);
                              }
                          }
                      }

                      System.out.println("=========================================================================");
                      System.out.println("                   Thank you for shopping with us! ");
                      System.out.println("=========================================================================");
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
  //====================================================Feedback method==========================================================
      public static void addFeedback(Connection connection, int customerId) {
          Scanner scanner = new Scanner(System.in);

          System.out.print("Enter your feedback rating (1-5): ");
          int rating;
          while (true) {
              if (scanner.hasNextInt()) {
                  rating = scanner.nextInt();
                  if (rating >= 1 && rating <= 5) {
                      break;
                  } else {
                      System.out.println("Please enter a rating between 1 and 5:");
                  }
              } else {
                  System.out.println("Invalid input. Please enter a numeric value:");
                  scanner.next();
              }
          }
          scanner.nextLine(); // Consume newline character

          System.out.print("Enter your comments: ");
          String comments = scanner.nextLine();

          // Get current date
          LocalDate feedbackDate = LocalDate.now();

          String query = "INSERT INTO feedback (customer_id, feedback_date, rating, comments) VALUES (?, ?, ?, ?)";
          try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
              preparedStatement.setInt(1, customerId);
              preparedStatement.setDate(2, Date.valueOf(feedbackDate));
              preparedStatement.setInt(3, rating);
              preparedStatement.setString(4, comments);
              preparedStatement.executeUpdate();
              System.out.println("Thank you for your feedback!");
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
  //********************************************Report generation*********************************************************************

  //********************************************************customer analysis*********************************************************
      public static void handleCustomerReportOptions(Connection connection) throws SQLException {
          Scanner scanner = new Scanner(System.in);
          System.out.println("Customer Report Options:");
          System.out.println("1. View specific customer's report");
          System.out.println("2. View all customers' reports");
          System.out.print("Enter your choice: ");
          int choice = scanner.nextInt();

          switch (choice) {
              case 1:
                  System.out.print("Enter customer ID: ");
                  int customerId = scanner.nextInt();
                  if (isCustomerIdPresent(connection, customerId)) {
                      generateCustomerAnalyticsReport(connection, customerId);
                  } else {
                      System.out.println("Customer ID not found.");
                  }
                  break;
              case 2:
                  generateCustomerAnalyticsReport(connection);
                  break;
              default:
                  System.out.println("Invalid choice.");
                  break;
          }
      }

      public static boolean isCustomerIdPresent(Connection connection, int customerId) {
          String checkCustomerQuery = "SELECT COUNT(*) FROM customers WHERE id = ?";
          try (PreparedStatement checkCustomerStatement = connection.prepareStatement(checkCustomerQuery)) {
              checkCustomerStatement.setInt(1, customerId);
              try (ResultSet resultSet = checkCustomerStatement.executeQuery()) {
                  if (resultSet.next()) {
                      return resultSet.getInt(1) > 0;
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
          return false;
      }
      
      public static void generateCustomerAnalyticsReport(Connection connection, int customerId) {
          String customerInfoQuery = "SELECT c.id AS CustomerID, c.name AS CustomerName, c.email AS CustomerEmail, " +
                                     "c.phone AS CustomerPhone, c.address AS CustomerAddress, " +
                                     "SUM(b.total) AS TotalSpending, COUNT(b.id) AS NumberOfBills, " +
                                     "AVG(b.total) AS AvgSpendingPerBill " +
                                     "FROM customers c " +
                                     "LEFT JOIN bills b ON c.id = b.customer_id " +
                                     "WHERE c.id = ? " +
                                     "GROUP BY c.id";

          try (PreparedStatement customerStatement = connection.prepareStatement(customerInfoQuery)) {
              customerStatement.setInt(1, customerId);
              try (ResultSet customerInfoResultSet = customerStatement.executeQuery()) {
                  if (customerInfoResultSet.next()) {
                      System.out.println("Customer Report:");
                      System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                      System.out.printf("| %-10s | %-10s | %-20s | %-30s | %-15s | %-30s | %-15s | %-15s | %-20s | %-20s |%n",
                                        "SNO", "CustomerID", "CustomerName", "CustomerEmail", "CustomerPhone", "CustomerAddress",
                                        "TotalSpending", "NumberOfBills", "AvgSpendingPerBill", "AvgMonthlySpending");
                      System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

                      int count = 1;
                      String customerName = customerInfoResultSet.getString("CustomerName");
                      String customerEmail = customerInfoResultSet.getString("CustomerEmail");
                      String customerPhone = customerInfoResultSet.getString("CustomerPhone");
                      String customerAddress = customerInfoResultSet.getString("CustomerAddress");
                      double totalSpending = customerInfoResultSet.getDouble("TotalSpending");
                      int numberOfBills = customerInfoResultSet.getInt("NumberOfBills");
                      double avgSpendingPerBill = customerInfoResultSet.getDouble("AvgSpendingPerBill");

                      // Retrieve average monthly spending for the customer
                      String avgMonthlySpendingQuery = "SELECT AVG(monthly_spending) AS AvgMonthlySpending " +
                                                       "FROM (SELECT DATE_FORMAT(b.bill_date, '%Y-%m') AS Month, " +
                                                       "SUM(b.total) AS monthly_spending " +
                                                       "FROM bills b " +
                                                       "WHERE b.customer_id = ? " +
                                                       "GROUP BY Month) AS monthly_totals";

                      double avgMonthlySpending = 0.0;

                      try (PreparedStatement avgMonthlySpendingStatement = connection.prepareStatement(avgMonthlySpendingQuery)) {
                          avgMonthlySpendingStatement.setInt(1, customerId);
                          try (ResultSet avgMonthlySpendingResultSet = avgMonthlySpendingStatement.executeQuery()) {
                              if (avgMonthlySpendingResultSet.next()) {
                                  avgMonthlySpending = avgMonthlySpendingResultSet.getDouble("AvgMonthlySpending");
                              }
                          }
                      }

                      // Print the customer's overall information along with average monthly spending
                      System.out.printf("| %-10d | %-10d | %-20s | %-30s | %-15s | %-30s | %-15.2f | %-15d | %-20.2f | %-20.2f |%n",
                                        count++, customerId, customerName, customerEmail, customerPhone, customerAddress,
                                        totalSpending, numberOfBills, avgSpendingPerBill, avgMonthlySpending);

                      // Retrieve and print the date of each purchase for the customer
                      String purchaseDatesQuery = "SELECT b.bill_date AS DateOfPurchase " +
                                                  "FROM bills b " +
                                                  "WHERE b.customer_id = ?";

                      try (PreparedStatement purchaseDatesStatement = connection.prepareStatement(purchaseDatesQuery)) {
                          purchaseDatesStatement.setInt(1, customerId);
                          try (ResultSet purchaseDatesResultSet = purchaseDatesStatement.executeQuery()) {
                              while (purchaseDatesResultSet.next()) {
                                  String dateOfPurchase = purchaseDatesResultSet.getString("DateOfPurchase");
                                  System.out.printf("Purchase Date: %s%n", dateOfPurchase);
                              }
                          }
                      }
                      System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                  } else {
                      System.out.println("Customer not found.");
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
      public static void generateCustomerAnalyticsReport(Connection connection) {
          String customerInfoQuery = "SELECT c.id AS CustomerID, c.name AS CustomerName, c.email AS CustomerEmail, " +
                                     "c.phone AS CustomerPhone, c.address AS CustomerAddress, " +
                                     "SUM(b.total) AS TotalSpending, COUNT(b.id) AS NumberOfBills, " +
                                     "AVG(b.total) AS AvgSpendingPerBill " +
                                     "FROM customers c " +
                                     "LEFT JOIN bills b ON c.id = b.customer_id " +
                                     "GROUP BY c.id";

          try (PreparedStatement customerStatement = connection.prepareStatement(customerInfoQuery);
               ResultSet customerInfoResultSet = customerStatement.executeQuery()) {

              System.out.println("Customer Report:");
              System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
              System.out.printf("| %-10s | %-10s | %-20s | %-30s | %-15s | %-30s | %-15s | %-15s | %-20s | %-20s |%n",
                                "SNO", "CustomerID", "CustomerName", "CustomerEmail", "CustomerPhone", "CustomerAddress",
                                "TotalSpending", "NumberOfBills", "AvgSpendingPerBill", "AvgMonthlySpending");
              System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

              int count = 1;
              while (customerInfoResultSet.next()) {
                  int customerId = customerInfoResultSet.getInt("CustomerID");
                  String customerName = customerInfoResultSet.getString("CustomerName");
                  String customerEmail = customerInfoResultSet.getString("CustomerEmail");
                  String customerPhone = customerInfoResultSet.getString("CustomerPhone");
                  String customerAddress = customerInfoResultSet.getString("CustomerAddress");
                  double totalSpending = customerInfoResultSet.getDouble("TotalSpending");
                  int numberOfBills = customerInfoResultSet.getInt("NumberOfBills");
                  double avgSpendingPerBill = customerInfoResultSet.getDouble("AvgSpendingPerBill");

                  // Retrieve average monthly spending for the customer
                  String avgMonthlySpendingQuery = "SELECT AVG(monthly_spending) AS AvgMonthlySpending " +
                                                   "FROM (SELECT DATE_FORMAT(b.bill_date, '%Y-%m') AS Month, " +
                                                   "SUM(b.total) AS monthly_spending " +
                                                   "FROM bills b " +
                                                   "WHERE b.customer_id = ? " +
                                                   "GROUP BY Month) AS monthly_totals";

                  double avgMonthlySpending = 0.0;

                  try (PreparedStatement avgMonthlySpendingStatement = connection.prepareStatement(avgMonthlySpendingQuery)) {
                      avgMonthlySpendingStatement.setInt(1, customerId);
                      try (ResultSet avgMonthlySpendingResultSet = avgMonthlySpendingStatement.executeQuery()) {
                          if (avgMonthlySpendingResultSet.next()) {
                              avgMonthlySpending = avgMonthlySpendingResultSet.getDouble("AvgMonthlySpending");
                          }
                      }
                  }

                  // Print the customer's overall information along with average monthly spending
                  System.out.printf("| %-10d | %-10d | %-20s | %-30s | %-15s | %-30s | %-15.2f | %-15d | %-20.2f | %-20.2f |%n",
                                    count++, customerId, customerName, customerEmail, customerPhone, customerAddress,
                                    totalSpending, numberOfBills, avgSpendingPerBill, avgMonthlySpending);

                  // Retrieve and print the date of each purchase for the customer
                  String purchaseDatesQuery = "SELECT b.bill_date AS DateOfPurchase " +
                                              "FROM bills b " +
                                              "WHERE b.customer_id = ?";

                  try (PreparedStatement purchaseDatesStatement = connection.prepareStatement(purchaseDatesQuery)) {
                      purchaseDatesStatement.setInt(1, customerId);
                      try (ResultSet purchaseDatesResultSet = purchaseDatesStatement.executeQuery()) {
                          while (purchaseDatesResultSet.next()) {
                              String dateOfPurchase = purchaseDatesResultSet.getString("DateOfPurchase");
                              System.out.printf("Purchase Date: %s%n", dateOfPurchase);
                          }
                      }
                  }
              }

              System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
   //***********************************************************Stock analysis******************************************************************   
      public static void handleStockReportOptions(Connection connection) throws SQLException {
          Scanner scanner = new Scanner(System.in);
          System.out.println("Stock Report Options:");
          System.out.println("1. View specific product's report");
          System.out.println("2. View all products' reports");
          System.out.print("Enter your choice: ");
          int choice = scanner.nextInt();

          switch (choice) {
              case 1:
              	System.out.print("Enter product ID: ");
                  int productId = scanner.nextInt();
                  if (isProductIdPresent(connection, productId)) {
                      generateStockAnalyticalReport(connection, productId);
                  } else {
                      System.out.println("Product ID not found.");
                  }
                  break;
              case 2:
                  generateStockAnalyticalReport(connection);
                  break;
              default:
                  System.out.println("Invalid choice.");
                  break;
          }
      }
      public static boolean isProductIdPresent(Connection connection, int productId) throws SQLException {
          String checkProductQuery = "SELECT COUNT(*) FROM products WHERE id = ?";
          try (PreparedStatement checkProductStatement = connection.prepareStatement(checkProductQuery)) {
              checkProductStatement.setInt(1, productId);
              try (ResultSet resultSet = checkProductStatement.executeQuery()) {
                  if (resultSet.next()) {
                      return resultSet.getInt(1) > 0;
                  }
              }
          }
          return false;
      }
      
      public static void generateStockAnalyticalReport(Connection connection, int productId) throws SQLException {
          String productInfoQuery = "SELECT " +
                                        "p.id AS ProductID, " +
                                        "p.name AS ProductName, " +
                                        "p.price AS Price, " +
                                        "p.rate AS Rate, " +
                                        "COALESCE(p.stock, 0) AS Stock, " +
                                        "COALESCE(SUM(bi.quantity), 0) AS TotalQuantitySold, " +
                                        "COALESCE(SUM(bi.quantity * p.rate), 0) AS TotalRevenue " +
                                    "FROM " +
                                        "products p " +
                                    "LEFT JOIN " +
                                        "bill_items bi ON p.id = bi.product_id " +
                                    "WHERE " +
                                        "p.id = ? " +
                                    "GROUP BY " +
                                        "p.id";

          try (PreparedStatement productStatement = connection.prepareStatement(productInfoQuery)) {
              productStatement.setInt(1, productId);
              try (ResultSet productInfoResultSet = productStatement.executeQuery()) {
                  if (productInfoResultSet.next()) {
                      System.out.println("Product Report:");
                      System.out.println("----------------------------------------------------------------------------------------------------");
                      System.out.printf("| %-10s | %-20s | %-10s | %-10s | %-10s | %-15s | %-15s |%n",
                                          "ProductID", "ProductName", "Price", "Rate", "Stock", "TotalQuantitySold", "TotalRevenue");
                      System.out.println("----------------------------------------------------------------------------------------------------");

                      String productName = productInfoResultSet.getString("ProductName");
                      double price = productInfoResultSet.getDouble("Price");
                      double rate = productInfoResultSet.getDouble("Rate");
                      int stock = productInfoResultSet.getInt("Stock");
                      int totalQuantitySold = productInfoResultSet.getInt("TotalQuantitySold");
                      double totalRevenue = productInfoResultSet.getDouble("TotalRevenue");

                      System.out.printf("| %-10d | %-20s | %-10.2f | %-10.2f | %-10d | %-15d | %-15.2f |%n",
                                          productId, productName, price, rate, stock, totalQuantitySold, totalRevenue);
                      System.out.println("----------------------------------------------------------------------------------------------------");
                  } else {
                      System.out.println("Product not found.");
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

      
      public static void generateStockAnalyticalReport(Connection connection) throws SQLException {
          String productInfoQuery = "SELECT " +
                                        "p.id AS ProductID, " +
                                        "p.name AS ProductName, " +
                                        "p.price AS Price, " +
                                        "p.rate AS Rate, " +
                                        "p.stock AS Stock, " +
                                        "COALESCE(SUM(bi.quantity), 0) AS TotalQuantitySold, " +
                                        "COALESCE(SUM(bi.quantity * p.rate), 0) AS TotalRevenue " +
                                    "FROM " +
                                        "products p " +
                                    "LEFT JOIN " +
                                        "bill_items bi ON p.id = bi.product_id " +
                                    "GROUP BY " +
                                        "p.id";

          try (PreparedStatement productStatement = connection.prepareStatement(productInfoQuery);
               ResultSet productInfoResultSet = productStatement.executeQuery()) {
              System.out.println("Stock Report:");
              System.out.println("-------------------------------------------------------------------------------------------------------------");
              System.out.printf("| %-10s | %-20s | %-10s | %-10s | %-10s | %-15s | %-15s |%n",
                                  "ProductID", "ProductName", "Price", "Rate", "Stock", "TotalQuantitySold", "TotalRevenue");
              System.out.println("-------------------------------------------------------------------------------------------------------------");

              while (productInfoResultSet.next()) {
                  int productId = productInfoResultSet.getInt("ProductID");
                  String productName = productInfoResultSet.getString("ProductName");
                  double price = productInfoResultSet.getDouble("Price");
                  double rate = productInfoResultSet.getDouble("Rate");
                  int stock = productInfoResultSet.getInt("Stock");
                  int totalQuantitySold = productInfoResultSet.getInt("TotalQuantitySold");
                  double totalRevenue = productInfoResultSet.getDouble("TotalRevenue");

                  System.out.printf("| %-10d | %-20s | %-10.2f | %-10.2f | %-10d | %-15d | %-15.2f |%n",
                                      productId, productName, price, rate, stock, totalQuantitySold, totalRevenue);
              }
              System.out.println("-------------------------------------------------------------------------------------------------------------");
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }


  //**************************************************ReStocking products********************************************************
      public static void restockingOptions(Connection connection) throws SQLException {
          Scanner scanner = new Scanner(System.in);
          System.out.println("Restocking Options:");
          System.out.println("1. Add new product");
          System.out.println("2. Update product stock");
          System.out.print("Enter your choice: ");
          int choice = scanner.nextInt();

          switch (choice) {
              case 1:
                  addNewProduct(connection);
                  break;
              case 2:
                  updateProductStock(connection);
                  break;
              default:
                  System.out.println("Invalid choice.");
                  break;
          }
      }
      public static void addNewProduct(Connection connection) {
          Scanner scanner = new Scanner(System.in);

          // Get product details from user
          System.out.print("Enter product name: ");
          String productName = scanner.nextLine();

          System.out.print("Enter product price: ");
          double price = scanner.nextDouble();

          System.out.print("Enter product rate: ");
          double rate = scanner.nextDouble();

          System.out.print("Enter product stock: ");
          int stock = scanner.nextInt();

          // Validate inputs
          if (productName == null || productName.isEmpty()) {
              System.out.println("Product name cannot be null or empty.");
              return;
          }

          if (price <= 0) {
              System.out.println("Price must be greater than zero.");
              return;
          }

          if (rate < 0 || rate > price) {
              System.out.println("Rate must be non-negative and less than or equal to price.");
              return;
          }

          if (stock < 0) {
              System.out.println("Stock cannot be negative.");
              return;
          }

          // Check if product already exists
          String checkQuery = "SELECT COUNT(*) AS count FROM products WHERE name = ?";
          String insertQuery = "INSERT INTO products (name, price, rate, stock) VALUES (?, ?, ?, ?)";

          try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
              checkStatement.setString(1, productName);

              try (ResultSet resultSet = checkStatement.executeQuery()) {
                  if (resultSet.next() && resultSet.getInt("count") > 0) {
                      System.out.println("Product already exists.");
                      return;
                  }
              }

              // Add new product
              try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                  insertStatement.setString(1, productName);
                  insertStatement.setDouble(2, price);
                  insertStatement.setDouble(3, rate);
                  insertStatement.setInt(4, stock);

                  int rowsInserted = insertStatement.executeUpdate();
                  if (rowsInserted > 0) {
                      System.out.println("Product added successfully.");
                  } else {
                      System.out.println("Failed to add product.");
                  }
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
      
      public static void updateProductStock(Connection connection) {
          Scanner scanner = new Scanner(System.in);

          // Get product ID, name, and new stock quantity from user
          System.out.print("Enter product ID: ");
          int productId = scanner.nextInt();
          scanner.nextLine();  // Consume newline

          System.out.print("Enter product name: ");
          String productName = scanner.nextLine();

          System.out.print("Enter additional stock quantity to add: ");
          int additionalStock = scanner.nextInt();

          // Validate inputs
          if (productId <= 0) {
              System.out.println("Product ID must be greater than zero.");
              return;
          }

          if (productName == null || productName.trim().isEmpty()) {
              System.out.println("Product name cannot be empty.");
              return;
          }

          if (additionalStock < 0) {
              System.out.println("Additional stock quantity cannot be negative.");
              return;
          }

          // Update product stock
          String updateQuery = "UPDATE products SET stock = stock + ? WHERE id = ? AND name = ?";

          try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
              updateStatement.setInt(1, additionalStock);
              updateStatement.setInt(2, productId);
              updateStatement.setString(3, productName);

              int rowsUpdated = updateStatement.executeUpdate();
              if (rowsUpdated > 0) {
                  System.out.println("Product stock updated successfully.");
              } else {
                  System.out.println("Product does not exist.");
              }
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }

  }


    public void printPurchaseHistory(Connection connection, int customerId) {
        String purchaseHistoryQuery = "SELECT b.bill_date, p.name, b.quantity, b.total_amount " +
                                      "FROM bills b JOIN products p ON b.product_id = p.id " +
                                      "WHERE b.customer_id = ?";

        try (PreparedStatement purchaseHistoryStatement = connection.prepareStatement(purchaseHistoryQuery)) {
            purchaseHistoryStatement.setInt(1, customerId);
            try (ResultSet purchaseHistoryResultSet = purchaseHistoryStatement.executeQuery()) {
                while (purchaseHistoryResultSet.next()) {
                    String billDate = purchaseHistoryResultSet.getString("bill_date");
                    String productName = purchaseHistoryResultSet.getString("name");
                    int quantity = purchaseHistoryResultSet.getInt("quantity");
                    double totalAmount = purchaseHistoryResultSet.getDouble("total_amount");
                    System.out.printf("Date: %s | Product: %s | Quantity: %d | Total: %.2f%n", billDate, productName, quantity, totalAmount);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
