import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Customer {
    private String customerId;
    private String name;

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public static Customer getCustomerDetails(DatabaseManager dbManager, String customerId) {
        try {
            String query = "SELECT * FROM customers WHERE customer_id = '" + customerId + "'";
            ResultSet rs = dbManager.executeQuery(query);
            if (rs.next()) {
                String name = rs.getString("name");
                return new Customer(customerId, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void generateCustomerAnalyticsReport(Connection connection) {
        String customerInfoQuery = "SELECT c.id AS CustomerID, c.name AS CustomerName, c.email AS CustomerEmail, "
                + "c.phone AS CustomerPhone, c.address AS CustomerAddress, SUM(b.total) AS TotalSpending, "
                + "COUNT(b.id) AS NumberOfBills, AVG(b.total) AS AvgSpendingPerBill "
                + "FROM customers c JOIN bills b ON c.id = b.customer_id "
                + "GROUP BY c.id, c.name, c.email, c.phone, c.address";

        try (PreparedStatement customerInfoStatement = connection.prepareStatement(customerInfoQuery);
             ResultSet customerInfoResultSet = customerInfoStatement.executeQuery()) {

            while (customerInfoResultSet.next()) {
                int customerId = customerInfoResultSet.getInt("CustomerID");
                String customerName = customerInfoResultSet.getString("CustomerName");
                String customerEmail = customerInfoResultSet.getString("CustomerEmail");
                String customerPhone = customerInfoResultSet.getString("CustomerPhone");
                String customerAddress = customerInfoResultSet.getString("CustomerAddress");
                double totalSpending = customerInfoResultSet.getDouble("TotalSpending");
                int numberOfBills = customerInfoResultSet.getInt("NumberOfBills");
                double avgSpendingPerBill = customerInfoResultSet.getDouble("AvgSpendingPerBill");

                System.out.printf("Customer ID: %d | Name: %s | Email: %s | Phone: %s | Address: %s | Total Spending: %.2f | Number of Bills: %d | Avg Spending Per Bill: %.2f%n",
                        customerId, customerName, customerEmail, customerPhone, customerAddress, totalSpending, numberOfBills, avgSpendingPerBill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
