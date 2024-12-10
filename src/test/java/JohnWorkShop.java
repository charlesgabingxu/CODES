import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
    private static String currentUser = null;

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

    private static JPanel createViewTransactionsPanel(JFrame frame, String currentUser) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
        JLabel title = new JLabel("Your Transactions");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
    
        MongoCollection<Document> transactionsCollection = database.getCollection("transactions");
        List<Document> transactions = transactionsCollection.find(Filters.eq("username", currentUser)).into(new ArrayList<>());
    
        List<Document> userTransactions = fetchTransactions(currentUser);
        for (Document transaction : userTransactions) {
            System.out.println(transaction.toJson());
        }

        if (transactions.isEmpty()) {
            panel.add(new JLabel("No transactions found."));
        } else {
            for (Document transaction : transactions) {
                String type = transaction.getString("type");
    
                if ("purchase".equals(type)) {
                    String itemName = transaction.getString("itemName");
                    String price = transaction.getString("price");
                    panel.add(new JLabel("- Purchased: " + itemName + " for $" + price));
                } else if ("maintenance".equals(type)) {
                    String maintenanceType = transaction.getString("MaintenanceType");
                    String schedule = transaction.getString("MaintenanceSchedule");
                    String status = transaction.getString("Status");
    
                    JPanel maintenancePanel = new JPanel();
                    maintenancePanel.setLayout(new BoxLayout(maintenancePanel, BoxLayout.Y_AXIS));
                    maintenancePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                    maintenancePanel.setBackground(Color.LIGHT_GRAY);
    
                    maintenancePanel.add(new JLabel("- Maintenance Booking: " + maintenanceType + " on " + schedule));
                    maintenancePanel.add(new JLabel("Status: " + (status == null ? "Pending" : status)));
    
                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.addActionListener(e -> {
                        int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to cancel this booking?", "Cancel Booking", JOptionPane.YES_NO_OPTION);
                        if (confirmation == JOptionPane.YES_OPTION) {
                            transactionsCollection.updateOne(
                                    Filters.and(
                                            Filters.eq("username", currentUser),
                                            Filters.eq("type", "maintenance"),
                                            Filters.eq("MaintenanceSchedule", schedule)
                                    ),
                                    new Document("$set", new Document("Status", "Canceled"))
                            );
                            JOptionPane.showMessageDialog(frame, "Maintenance booking canceled successfully.");
                            JPanel updatedPanel = createViewTransactionsPanel(frame, currentUser);
                            frame.getContentPane().add(updatedPanel, "ViewTransactions");
                            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                            layout.show(frame.getContentPane(), "ViewTransactions");
                        }
                    });
    
                    maintenancePanel.add(cancelButton);
                    panel.add(maintenancePanel);
                }
            }
        }
    
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "PostLogin");
        });
        panel.add(backButton);
    
        return panel;
    }

    private static List<Document> fetchTransactions(String username) {
        MongoCollection<Document> transactionsCollection = database.getCollection("transactions");
        return transactionsCollection.find(Filters.eq("username", username)).into(new ArrayList<>());
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
                currentUser = username;
                JOptionPane.showMessageDialog(panel, "Login Successful!");
                JPanel postLoginPanel = createPostLoginPanel(frame);
                frame.getContentPane().add(postLoginPanel, "PostLogin");
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
        JButton addBookButton = new JButton("Add Book");
        JButton viewTransactionsButton = new JButton("View Transactions");
        JButton logoutButton = new JButton("Logout");
    
        setButtonSize(addItemButton);
        setButtonSize(buyItemButton);
        setButtonSize(addBookButton);
        setButtonSize(viewTransactionsButton);
        setButtonSize(logoutButton);
    
        addItemButton.setBounds(250, 100, BUTTON_SIZE.width, BUTTON_SIZE.height);
        buyItemButton.setBounds(250, 150, BUTTON_SIZE.width, BUTTON_SIZE.height);
        addBookButton.setBounds(250, 200, BUTTON_SIZE.width, BUTTON_SIZE.height);
        viewTransactionsButton.setBounds(250, 250, BUTTON_SIZE.width, BUTTON_SIZE.height);
        logoutButton.setBounds(250, 300, BUTTON_SIZE.width, BUTTON_SIZE.height);

        if (!"Administrator92410".equals(currentUser)) {
            addItemButton.setVisible(false);
        }

        addItemButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "AddItem");
        });
        
        buyItemButton.addActionListener(e -> {
            JPanel buyItemPanel = createBuyItemPanel(frame);
            frame.getContentPane().add(buyItemPanel, "BuyItem");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "BuyItem");
        });
    
        addBookButton.addActionListener(e -> {
            JPanel addBookPanel = new JPanel();
            addBookPanel.setLayout(new BoxLayout(addBookPanel, BoxLayout.Y_AXIS));

            JLabel scheduleLabel = new JLabel("Maintenance Schedule (Format: TIME, MONTH, DAY, YEAR):");
            addBookPanel.add(scheduleLabel);

            JTextField scheduleField = new JTextField(300); 
            scheduleField.setMaximumSize(new Dimension(450, 20)); 
            addBookPanel.add(scheduleField);

            JLabel maintenanceTypeLabel = new JLabel("Maintenance Type:");
            addBookPanel.add(maintenanceTypeLabel);

            JTextField maintenanceTypeField = new JTextField(300); 
            maintenanceTypeField.setMaximumSize(new Dimension(450, 20)); 
            addBookPanel.add(maintenanceTypeField);
                    
            JButton submitButton = new JButton("Submit");
            submitButton.addActionListener(submitEvent -> {
                String schedule = scheduleField.getText();
                String maintenanceType = maintenanceTypeField.getText();
                
                if (!isValidSchedule(schedule)) {
                    JOptionPane.showMessageDialog(addBookPanel,
                            "Invalid Schedule Format! Please use '10:00 AM, December 07, 2024'.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
        
                if (schedule.isEmpty() || maintenanceType.isEmpty()) {
                    JOptionPane.showMessageDialog(addBookPanel, "Please fill in both fields.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    boolean success = addBook(maintenanceType, schedule, currentUser);
                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Maintenance Book Added Successfully!");
                        CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                        layout.show(frame.getContentPane(), "PostLogin");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to add Maintenance Book. Schedule conflict.");
                    }
                }
            });
        
            addBookPanel.add(submitButton);
        
            JButton backButton = new JButton("Back");
            backButton.addActionListener(backEvent -> {
                CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
                layout.show(frame.getContentPane(), "PostLogin");
            });
        
            addBookPanel.add(backButton);
        
            frame.getContentPane().add(addBookPanel, "AddBook");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "AddBook");
        });

        viewTransactionsButton.addActionListener(e -> {
            JPanel viewTransactionsPanel = createViewTransactionsPanel(frame, currentUser);
            frame.getContentPane().add(viewTransactionsPanel, "ViewTransactions");
            
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "ViewTransactions");
        });
    
        logoutButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "Login");
        });
    
        panel.add(addItemButton);
        panel.add(buyItemButton);
        panel.add(addBookButton);
        panel.add(viewTransactionsButton);
        panel.add(logoutButton);
    
        return panel;
    }

    private static boolean isValidSchedule(String schedule) {
        String regex = "^(0[1-9]|1[0-2]):[0-5][0-9] (AM|PM), " + 
                       "(January|February|March|April|May|June|July|August|September|October|November|December) " + 
                       "\\d{2}, \\d{4}$";
        return schedule.matches(regex);
    }
    
    private static boolean addBook(String maintenanceType, String maintenanceSchedule, String username) {
        try {
            MongoCollection<Document> maintenanceRepairsCollection = database.getCollection("maintenancerepairs");
    
            Document conflictingBooking = maintenanceRepairsCollection.find(
                    Filters.eq("MaintenanceSchedule", maintenanceSchedule)
            ).first();
    
            if (conflictingBooking != null) {
                return false;
            }
    
            Document newBook = new Document("MaintenanceType", maintenanceType)
                    .append("MaintenanceSchedule", maintenanceSchedule)
                    .append("username", username);
    
            maintenanceRepairsCollection.insertOne(newBook);
    
            MongoCollection<Document> transactionsCollection = database.getCollection("transactions");
            Document transaction = new Document("username", username)
                    .append("type", "maintenance")
                    .append("MaintenanceType", maintenanceType)
                    .append("MaintenanceSchedule", maintenanceSchedule)
                    .append("timestamp", System.currentTimeMillis());
            transactionsCollection.insertOne(transaction);
    
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private static boolean purchaseItem(Document item, String buyer) {
        try {
            MongoCollection<Document> transactionsCollection = database.getCollection("transactions");
            MongoCollection<Document> itemsCollection = database.getCollection("items");
            
            Document purchasedItem = new Document(item).append("buyer", buyer);
    
            itemsCollection.replaceOne(Filters.eq("itemID", item.getString("itemID")), purchasedItem);
    
            Document transaction = new Document("username", buyer)
                    .append("type", "purchase")
                    .append("itemID", item.getString("itemID"))
                    .append("itemName", item.getString("itemName"))
                    .append("price", item.getString("price"))
                    .append("timestamp", System.currentTimeMillis());
            transactionsCollection.insertOne(transaction);
    
            return true; 
        } catch (Exception e) {
            e.printStackTrace();
            return false; 
        }
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
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Your cart is empty!", "Cart", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
    
            JPanel cartPanel = new JPanel();
            cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
    
            for (Document item : new ArrayList<>(cart)) {
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
                itemPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                itemPanel.setBackground(Color.LIGHT_GRAY);
    
                JLabel nameLabel = new JLabel("Name: " + item.getString("itemName"));
                JLabel priceLabel = new JLabel("Price: $" + item.getString("price"));
    
                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(removeEvent -> {
                    cart.remove(item);
                    JOptionPane.showMessageDialog(frame, "Item removed from the cart.", "Cart", JOptionPane.INFORMATION_MESSAGE);
                    SwingUtilities.getWindowAncestor(cartPanel).dispose();
                });
    
                itemPanel.add(nameLabel);
                itemPanel.add(priceLabel);
                itemPanel.add(removeButton);
                cartPanel.add(itemPanel);
                cartPanel.add(Box.createVerticalStrut(10));
            }
    
        JScrollPane cartScrollPane = new JScrollPane(cartPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cartScrollPane.setPreferredSize(new Dimension(300, 400));
        JOptionPane.showMessageDialog(frame, cartScrollPane, "Your Cart", JOptionPane.PLAIN_MESSAGE);

        });
    
        JButton buyButton = new JButton("Buy");
        buyButton.addActionListener(e -> {
            if (cart.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Cart is empty! Add items before buying.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                for (Document item : new ArrayList<>(cart)) {
                    boolean success = purchaseItem(item, currentUser);
                    if (!success) {
                        JOptionPane.showMessageDialog(frame, "Failed to purchase: " + item.getString("itemName"), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
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
            Document existingItem = itemsCollection.find(
                Filters.or(
                    Filters.eq("itemName", itemName),
                    Filters.eq("partNumber", partNumber)
                )
            ).first();

            if (existingItem != null) {
                JOptionPane.showMessageDialog(null,
                    "Item with the same name or part number already exists!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

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

