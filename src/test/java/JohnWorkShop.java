import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
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

    private static List<Document> cart = new ArrayList<>();
    private static final Dimension BUTTON_SIZE = new Dimension(100, 30);

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
        JPanel buyItemPanel = createBuyItemPanel(frame);

        frame.add(loginPanel, "Login");
        frame.add(registerPanel, "Register");
        frame.add(postLoginPanel, "PostLogin");
        frame.add(addItemPanel, "AddItem");
        frame.add(buyItemPanel, "BuyItem");

        frame.setVisible(true);
    }

    private static void setButtonSize(JButton button) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);   
        button.setMaximumSize(BUTTON_SIZE); 
    }

    private static JPanel createLoginPanel(JFrame frame) {
        JPanel panel = new JPanel(null); 
        panel.setBackground(Color.WHITE);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(150, 150, 100, 25);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(250, 150, 200, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(150, 200, 100, 25);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(250, 200, 200, 25);

        JButton loginButton = new JButton("Login");
        setButtonSize(loginButton);
        loginButton.setBounds(150, 300, BUTTON_SIZE.width, BUTTON_SIZE.height);

        JButton registerButton = new JButton("Register");
        setButtonSize(registerButton);
        registerButton.setBounds(300, 300, BUTTON_SIZE.width, BUTTON_SIZE.height);

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

        registerButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Register");
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        return panel;
    }

    private static JPanel createPostLoginPanel(JFrame frame) {
        JPanel panel = new JPanel(null);
    
        JButton addItemButton = new JButton("Add Item");
        JButton buyItemButton = new JButton("Buy Item");
        JButton logoutButton = new JButton("Logout");
    
        setButtonSize(addItemButton);
        setButtonSize(buyItemButton);
        setButtonSize(logoutButton);
    
        addItemButton.setBounds(250, 100, BUTTON_SIZE.width, BUTTON_SIZE.height);
        buyItemButton.setBounds(250, 150, BUTTON_SIZE.width, BUTTON_SIZE.height);
        logoutButton.setBounds(250, 200, BUTTON_SIZE.width, BUTTON_SIZE.height);
    

        addItemButton.addActionListener(e -> {

            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "AddItem");
        });
    
        buyItemButton.addActionListener(e -> {

            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "BuyItem");
        });
    
        logoutButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Login");
        });
    
        panel.add(addItemButton);
        panel.add(buyItemButton);
        panel.add(logoutButton);
    
        return panel;
    }
    

    private static JPanel createBuyItemPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel itemListPanel = new JPanel();
        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(itemListPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        List<Document> items = itemsCollection.find().into(new ArrayList<>());

        for (Document item : items) {
            JPanel itemPanel = new JPanel();
            itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
            itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            itemPanel.setBackground(Color.LIGHT_GRAY);

            JLabel nameLabel = new JLabel("Name: " + item.getString("itemName"));
            JLabel partNumberLabel = new JLabel("Part Number: " + item.getString("partNumber"));
            JLabel descriptionLabel = new JLabel("Description: " + item.getString("description"));
            JLabel priceLabel = new JLabel("Price: $" + item.getString("price"));

            JButton addToCartButton = new JButton("Add to Cart");
            addToCartButton.addActionListener(e -> cart.add(item));

            itemPanel.add(nameLabel);
            itemPanel.add(partNumberLabel);
            itemPanel.add(descriptionLabel);
            itemPanel.add(priceLabel);
            itemPanel.add(addToCartButton);

            itemListPanel.add(itemPanel);
            itemListPanel.add(Box.createVerticalStrut(10));
        }

        JButton viewCartButton = new JButton("View Cart");
        viewCartButton.addActionListener(e -> {
            StringBuilder cartDetails = new StringBuilder();
            if (cart.isEmpty()) {
                cartDetails.append("Your cart is empty!");
            } else {
                for (Document item : cart) {
                    cartDetails.append(String.format("Name: %s, Price: $%s\n", item.getString("itemName"), item.getString("price")));
                }
            }

            JOptionPane.showMessageDialog(frame, cartDetails.toString(), "Cart", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Cart is empty! Add items before buying.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                cart.clear();
                JOptionPane.showMessageDialog(frame, "Purchase successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 2, 10, 10));
        bottomPanel.add(viewCartButton);
        bottomPanel.add(buyButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "PostLogin");
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(backButton, BorderLayout.NORTH);

        return panel;
    }

    private static JPanel createRegisterPanel(JFrame frame) {
        JPanel panel = new JPanel(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(150, 100, 100, 25);
        JTextField usernameField = new JTextField();
        usernameField.setBounds(250, 100, 200, 25);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(150, 150, 100, 25);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(250, 150, 200, 25);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setBounds(150, 200, 150, 25);
        JPasswordField confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(250, 200, 200, 25);

        JButton registerButton = new JButton("Register");
        setButtonSize(registerButton);
        registerButton.setBounds(150, 300, BUTTON_SIZE.width, BUTTON_SIZE.height);

        JButton backButton = new JButton("Back");
        setButtonSize(backButton);
        backButton.setBounds(300, 300, BUTTON_SIZE.width, BUTTON_SIZE.height);

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

        backButton.addActionListener(e -> {
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
        panel.add(backButton);

        return panel;
    }

    private static JPanel createAddItemPanel(JFrame frame) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField itemNameField = new JTextField(15);
        JTextField partNumberField = new JTextField(15);
        JTextField descriptionField = new JTextField(15);
        JTextField manufacturerField = new JTextField(15);
        JTextField supplierField = new JTextField(15);
        JTextField priceField = new JTextField(15);

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            String itemID = generateItemID(); 
            String itemName = itemNameField.getText();
            String partNumber = partNumberField.getText();
            String description = descriptionField.getText();
            String manufacturer = manufacturerField.getText();
            String supplier = supplierField.getText();
            String price = priceField.getText();

            if (itemName.isEmpty() || partNumber.isEmpty() || description.isEmpty() || manufacturer.isEmpty() || supplier.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

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

        Document user = new Document("username", username).append("password", password);
        usersCollection.insertOne(user);
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
