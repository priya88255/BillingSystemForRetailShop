import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Product {
    private String productId;
    private String name;
    private double price;

    public Product(String productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public static Product getProductDetails(DatabaseManager dbManager, String productId) {
        try {
            String query = "SELECT * FROM products WHERE product_id = '" + productId + "'";
            ResultSet rs = dbManager.executeQuery(query);
            if (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                return new Product(productId, name, price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addNewProduct(Connection connection) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter product ID: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        System.out.print("Enter product price: ");
        double price = scanner.nextDouble();

        System.out.print("Enter product stock quantity: ");
        int stock = scanner.nextInt();

        String insertQuery = "INSERT INTO products (id, name, price, stock) VALUES (?, ?, ?, ?)";

        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setInt(1, productId);
            insertStatement.setString(2, productName);
            insertStatement.setDouble(3, price);
            insertStatement.setInt(4, stock);

            int rowsInserted = insertStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Product added successfully.");
            } else {
                System.out.println("Failed to add product.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateProductStock(Connection connection) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter product ID: ");
        int productId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter product name: ");
        String productName = scanner.nextLine();

        System.out.print("Enter additional stock quantity to add: ");
        int additionalStock = scanner.nextInt();

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
