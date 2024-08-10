import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class RetailShopBillingSystems {
    public static void main(String[] args) throws Exception {
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.connect();
            System.out.println("Connected to the database.");

            Scanner scanner = new Scanner(System.in);
            displayWelcomeMessage();

            while (true) {
                displayMainMenu();
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        BillingSystem.handleBillingMenu(dbManager.getConnection());
                        break;
                    case 2:
                        System.out.println("Enter BillId of the customer:");
                        int billId = scanner.nextInt();
                        System.out.println("Enter CustomerId of the customer:");
                        int customerId = scanner.nextInt();
                        BillingSystem.handlePaymentMethods(dbManager.getConnection(), billId, customerId);
                        break;
                    case 3:
                        BillingSystem.handleCustomerAndStockReports(dbManager.getConnection());
                        break;
                    case 4:
                        System.out.println("Thank you for visiting Nellai Mart! Have a great day!");
                        scanner.close();
                        dbManager.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void displayWelcomeMessage() {
        System.out.println("Welcome to Nellai Mart - Retail Shop Billing System!");
    }

    private static void displayMainMenu() {
        System.out.println("Main Menu:");
        System.out.println("1. Billing Menu");
        System.out.println("2. Payment Methods");
        System.out.println("3. Customer and Stock Reports");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }
}
