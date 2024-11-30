import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class JohnWorkShop {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> usersCollection;
    private static MongoCollection<Document> itemsCollection;

    public static void main(String[] args) {
        connectToMongoDB();
        SwingUtilities.invokeLater(JohnWorkShop::createAndShowGUI);
    }

    private static void connectToMongoDB() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("johnworkshop");
        usersCollection = database.getCollection("users");
        itemsCollection = database.getCollection("items");
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("JohnWorkShop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new CardLayout());
        frame.setResizable(false);

        JPanel loginPanel = createLoginPanel(frame);
        JPanel registerPanel = createRegisterPanel(frame);
        JPanel addItemPanel = createAddItemPanel(frame);
        JPanel postLoginPanel = createPostLoginPanel(frame);

        frame.add(loginPanel, "Login");
        frame.add(registerPanel, "Register");
        frame.add(postLoginPanel, "PostLogin");
        frame.add(addItemPanel, "AddItem");

        frame.setVisible(true);
    }

    private static JPanel createLoginPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new java.awt.Dimension(150, 40));

        JButton switchToRegister = new JButton("Register");
        switchToRegister.setPreferredSize(new java.awt.Dimension(150, 40));

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticateUser(username, password)) {
                JOptionPane.showMessageDialog(panel, "Login Successful!");
                CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                layout.show(frame.getContentPane(), "PostLogin");
            } else {
                JOptionPane.showMessageDialog(panel, "Invalid Username or Password.");
            }
        });

        switchToRegister.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Register");
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(switchToRegister);

        return panel;
    }

    private static JPanel createPostLoginPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton addItemButton = new JButton("Add Item");
        addItemButton.setPreferredSize(new java.awt.Dimension(100, 30));

        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new java.awt.Dimension(100, 30));

        addItemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        addItemButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "AddItem");
        });

        logoutButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Login");
        });

        panel.add(Box.createVerticalGlue());
        panel.add(addItemButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(logoutButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private static JPanel createRegisterPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new java.awt.Dimension(150, 40));

        JButton switchToLogin = new JButton("Back to Login");
        switchToLogin.setPreferredSize(new java.awt.Dimension(150, 40));

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(panel, "Passwords do not match!");
                return;
            }

            if (registerUser(username, password)) {
                JOptionPane.showMessageDialog(panel, "Registration Successful!");
                CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                layout.show(frame.getContentPane(), "Login");
            } else {
                JOptionPane.showMessageDialog(panel, "Username already exists!");
            }
        });

        switchToLogin.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Login");
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordField);
        panel.add(registerButton);
        panel.add(switchToLogin);

        return panel;
    }

    private static JPanel createAddItemPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Input fields for item details
        JTextField itemNameField = new JTextField(15);
        JTextField partNumberField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        JTextField manufacturerField = new JTextField(15);
        JTextField supplierField = new JTextField(15);
        JTextField priceField = new JTextField(15);

        // Submit Button Action
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String itemID = generateItemID(); // Generate a unique itemID
            String itemName = itemNameField.getText();
            String partNumber = partNumberField.getText();
            String description = descriptionField.getText();
            String manufacturer = manufacturerField.getText();
            String supplier = supplierField.getText();
            String price = priceField.getText();

            // Ensure all fields are filled
            if (itemName.isEmpty() || partNumber.isEmpty() || description.isEmpty() || manufacturer.isEmpty() || supplier.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call addItem and show confirmation dialog
            if (addItem(itemID, itemName, partNumber, description, manufacturer, supplier, price)) {
                JOptionPane.showMessageDialog(frame, "Item added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add item. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(new JLabel("Item Name:"));
        panel.add(itemNameField);
        panel.add(new JLabel("Part Number:"));
        panel.add(partNumberField);
        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);
        panel.add(new JLabel("Manufacturer:"));
        panel.add(manufacturerField);
        panel.add(new JLabel("Supplier:"));
        panel.add(supplierField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);

        panel.add(submitButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "PostLogin");
        });
        panel.add(backButton);

        return panel;
    }

    private static boolean authenticateUser(String username, String password) {
        Document user = usersCollection.find(Filters.eq("username", username)).first();
        return user != null && user.getString("password").equals(password);
    }

    private static boolean registerUser(String username, String password) {
        if (usersCollection.find(Filters.eq("username", username)).first() != null) {
            return false;
        }

        Document newUser = new Document("username", username)
                .append("password", password);
        usersCollection.insertOne(newUser);
        return true;
    }

    private static boolean addItem(String itemID, String itemName, String partNumber, String description, String manufacturer, String supplier, String price) {
        try {
            Document newItem = new Document("itemID", itemID)
                    .append("itemName", itemName)
                    .append("partNumber", partNumber)
                    .append("description", description)
                    .append("manufacturer", manufacturer)
                    .append("supplier", supplier)
                    .append("price", price);

            itemsCollection.insertOne(newItem);
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
    }

    private static String generateItemID() {
        long timestamp = System.currentTimeMillis();
        return "ITEM" + timestamp;
    }
}
