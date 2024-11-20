import java.awt.CardLayout;
import java.awt.GridLayout;

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
        JButton switchToRegister = new JButton("Register");

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
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        JButton addItemButton = new JButton("Add Item");
        JButton logoutButton = new JButton("Logout");

        addItemButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "AddItem");
        });

        logoutButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Login");
        });

        panel.add(addItemButton);
        panel.add(logoutButton);

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
        JButton switchToLogin = new JButton("Back to Login");

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
        panel.setLayout(new GridLayout(9, 2, 10, 10)); 
    
        JLabel itemIDLabel = new JLabel("Item ID:");
        JTextField itemIDField = new JTextField();
        JLabel itemNameLabel = new JLabel("Item Name:");
        JTextField itemNameField = new JTextField();
        JLabel partNumberLabel = new JLabel("Part Number:");
        JTextField partNumberField = new JTextField();
        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField();
        JLabel manufacturerLabel = new JLabel("Manufacturer:");
        JTextField manufacturerField = new JTextField();
        JLabel supplierLabel = new JLabel("Supplier:");
        JTextField supplierField = new JTextField();
    
        JButton addItemButton = new JButton("Add Item");
        JButton backButton = new JButton("Back");
    
        addItemButton.addActionListener(e -> {
            String itemID = itemIDField.getText();
            String itemName = itemNameField.getText();
            String partNumber = partNumberField.getText();
            String description = descriptionField.getText();
            String manufacturer = manufacturerField.getText();
            String supplier = supplierField.getText();
    
            if (addItem(itemID, itemName, partNumber, description, manufacturer, supplier)) {
                JOptionPane.showMessageDialog(panel, "Item Added Successfully!");
                itemIDField.setText("");
                itemNameField.setText("");
                partNumberField.setText("");
                descriptionField.setText("");
                manufacturerField.setText("");
                supplierField.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to add item.");
            }
        });
    
        backButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "PostLogin");
        });
    
        panel.add(itemIDLabel);
        panel.add(itemIDField);
        panel.add(itemNameLabel);
        panel.add(itemNameField);
        panel.add(partNumberLabel);
        panel.add(partNumberField);
        panel.add(descriptionLabel);
        panel.add(descriptionField);
        panel.add(manufacturerLabel);
        panel.add(manufacturerField);
        panel.add(supplierLabel);
        panel.add(supplierField);
        panel.add(addItemButton);
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

    private static boolean addItem(String itemID, String itemName, String partNumber, String description, String manufacturer, String supplier) {

        Document newItem = new Document("itemID", itemID)
                .append("itemName", itemName)
                .append("partNumber", partNumber)
                .append("description", description)
                .append("manufacturer", manufacturer)
                .append("supplier", supplier);

        itemsCollection.insertOne(newItem); 
        return true;
    }
}
