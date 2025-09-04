import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class reservesSceneController {
    @FXML
    private AnchorPane AnchorPane_id;

    @FXML
    private HBox bottomPane_id;

    @FXML
    private Button discountCodeBtn_id;

    @FXML
    private TextField discountCode_textFild_id;

    @FXML
    private Label items_counter_label_id;

    @FXML
    private Button logoutBtn_id;

    @FXML
    private Button menuBtn_id;

    @FXML
    private Label name_label_id;

    @FXML
    private Label pageTite_id;

    @FXML
    private Button payBtn_id;

    @FXML
    private Label total_price_label_id;

    @FXML
    private Label username_label_id;

    @FXML
    private Button editProfileBtn_id;

    @FXML
    private TabPane menuTapPane_id;

    @FXML
    private DatePicker datePicker_id;

    @FXML
    private TextField time_textFild_id;

    String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Food System Database;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    String _username;

    public void setUserData(String name, String username) {
        _username = username;
        name_label_id.setText(name);
        username_label_id.setText(username);
        loadTabsFromDatabase();

        
    }

    private List<String> favoritesItems = new ArrayList<>();
    private TilePane favoritesContainer = new TilePane();
    private TilePane historyContainer = new TilePane();

    private void loadTabsFromDatabase() {
        menuTapPane_id.getTabs().clear();
        favoritesItems.clear();

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {

            Tab favTab = new Tab("Favorites");
            favoritesContainer.setHgap(20); 
            favoritesContainer.setVgap(20); 
            favoritesContainer.setPrefColumns(5); 
            favoritesContainer.setStyle("-fx-padding: 15; -fx-alignment: CENTER_LEFT;");

            String customerSql = "SELECT CustomerID FROM Person p WHERE p.UserName = ?";
            PreparedStatement custStmt = conn.prepareStatement(customerSql);
            custStmt.setString(1, username_label_id.getText());
            ResultSet custRs = custStmt.executeQuery();
            int customerId = 0;
            if (custRs.next()) {
                customerId = custRs.getInt("CustomerID");
                System.out.println(customerId);
            }

            String favSql = 
            "SELECT Name, Price, ImagePath " +
            "FROM Favorite f " +
            "JOIN Item i ON f.ItemID = i.ItemID " +
            "JOIN MenuItem m ON i.MenuItemID = m.FoodID " +
            "WHERE CustomerID = ?";


            PreparedStatement favStmt = conn.prepareStatement(favSql);
            favStmt.setInt(1, customerId);
            ResultSet favRs = favStmt.executeQuery();

            while (favRs.next()) {
                String name = favRs.getString("Name");
                double price = favRs.getDouble("Price");
                String imagePath = favRs.getString("ImagePath");

                favoritesItems.add(name);

                VBox favCard = createFoodCard(name, price, imagePath);
                favoritesContainer.getChildren().add(favCard);
                
                
            }

            String sql = "SELECT ID, Name FROM Category";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int categoryId = rs.getInt("ID");
                String categoryName = rs.getString("Name");

                Tab tab = new Tab(categoryName);

                TilePane cardContainer = new TilePane();
                cardContainer.setHgap(20); 
                cardContainer.setVgap(20); 
                cardContainer.setPrefColumns(5); 
                cardContainer.setStyle("-fx-padding: 15; -fx-alignment: CENTER_LEFT;");

                String foodSql = "SELECT Name, Price, ImagePath FROM MenuItem WHERE CategoryID = ?";
                PreparedStatement foodStmt = conn.prepareStatement(foodSql);
                foodStmt.setInt(1, categoryId);
                ResultSet foodRs = foodStmt.executeQuery();

                while (foodRs.next()) {
                    String foodName = foodRs.getString("Name");
                    double foodPrice = foodRs.getDouble("Price");
                    String imagePath = foodRs.getString("ImagePath"); 

                    VBox card = createFoodCard(foodName, foodPrice, imagePath);
                    cardContainer.getChildren().add(card);
                }

                ScrollPane scrollPane = new ScrollPane(cardContainer);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

                tab.setContent(scrollPane);
                menuTapPane_id.getTabs().add(tab);
            }

            
            ScrollPane scrollPane = new ScrollPane(favoritesContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
            favTab.setContent(scrollPane);

            menuTapPane_id.getTabs().add(favTab);



            String historySql = "WITH TableCTE2 AS (" +
                    "    SELECT R.ReservationID, R.CustomerID, COUNT(*) as ItemCount " +
                    "    FROM Reservation R " +
                    "    JOIN ReservationDetail R2 ON R.ReservationID = R2.ReservationID " +
                    "    JOIN Item i ON i.ItemID = R2.ItemID " +
                    "    JOIN MenuItem m ON m.FoodID = i.MenuItemID " +
                    "    WHERE R.CustomerID = ? " +
                    "    GROUP BY R.ReservationID, R.CustomerID" +
                    ") " +  
                    "SELECT ReservationID " + 
                    "FROM TableCTE2";

            PreparedStatement hstmt = conn.prepareStatement(historySql);
            hstmt.setInt(1, customerId);
            ResultSet Rs = hstmt.executeQuery();
            int c = 1;

            while (Rs.next()) {
                int reservationId = Rs.getInt("ReservationID");

                String detailSql = "WITH TableCTE AS (" +
                        "    SELECT " +
                        "        R.ReservationID, " +
                        "        R.CustomerID, " +
                        "        R2.ReservationDetailID, " +
                        "        i.ItemID, " +
                        "        m.Name, " +
                        "        R2.Quantity, " +
                        "        R2.PriceAtOrderTime, " +
                        "        R.Time, "+
                        "        R.Date"+
                        "    FROM Reservation R " +
                        "    JOIN ReservationDetail R2 ON R.ReservationID = R2.ReservationID " +
                        "    JOIN Item i ON i.ItemID = R2.ItemID " +
                        "    JOIN MenuItem m ON m.FoodID = i.MenuItemID " +
                        "    WHERE R.CustomerID = ? " +
                        ") " +
                        "SELECT Name, Quantity, PriceAtOrderTime, ReservationID, Time, Date " +
                        "FROM TableCTE " +
                        "WHERE ReservationID = ?";

                PreparedStatement detailStmt = conn.prepareStatement(detailSql);
                detailStmt.setInt(1, customerId);  
                detailStmt.setInt(2, reservationId);  
                ResultSet detailRs = detailStmt.executeQuery();
                double yOffset = 0;
                System.out.println("Reservation ID: " + reservationId);
                String name;
                int quantity;
                double price;
                String time;
                String date;
                while (detailRs.next()) {
                    name = detailRs.getString("Name");
                    quantity = detailRs.getInt("Quantity");
                    price = detailRs.getDouble("PriceAtOrderTime");
                    time = detailRs.getString("Time");
                    date = detailRs.getString("Date");


                    System.out.println("  Item: " + name + ", Quantity: " + quantity + ", Price: " + price);

                }
                VBox card = createhistoryCard(c , time , date, name , quantity , price);
                card.setLayoutY(yOffset);
                AnchorPane_id.getChildren().add(card);
                yOffset += card.getHeight() + 100; 

                c++;

                detailRs.close();
                detailStmt.close();
            }

            rs.close();
            hstmt.close();


                    
                                

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> selectedItems = new ArrayList<>(); 
    private int totalItems = 0;
    private double totalPrice = 0.0;

    private VBox createFoodCard(String name, double price, String imagePath) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 10; -fx-background-radius: 10;");

        // عکس غذا
        ImageView imageView;
        try {
            imageView = new ImageView(new Image(imagePath)); 
        } catch (Exception e) {
            imageView = new ImageView(); 
        }
        imageView.setFitWidth(120);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label priceLabel = new Label(String.format("%.2f toman", price));

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            totalItems++;
            totalPrice += price;
            selectedItems.add(name + " - " + price + "toman");
            updateCounters();
        });

        Button favoritesButton = new Button(favoritesItems.contains(name) ? "Delete From Favorites" : "Add To Favorites");
        favoritesButton.setOnAction(e -> {
            try (Connection conn = DriverManager.getConnection(connectionUrl)) {

                String customerSql = "SELECT CustomerID FROM Person p WHERE p.UserName = ?";
                PreparedStatement custStmt = conn.prepareStatement(customerSql);
                custStmt.setString(1, username_label_id.getText());
                ResultSet custRs = custStmt.executeQuery();

                int customerId = 0;
                if (custRs.next()) {
                    customerId = custRs.getInt("CustomerID");
                }

                if (!favoritesItems.contains(name)) {
                    favoritesItems.add(name);
                    favoritesButton.setText("Delete From Favorites");

                    String itemSql = "SELECT i.ItemID FROM Item i JOIN MenuItem m ON i.MenuItemID = m.FoodID WHERE m.Name = ?";
                    PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                    itemStmt.setString(1, name);
                    ResultSet itemRs = itemStmt.executeQuery();

                    if (itemRs.next()) {
                        int itemId = itemRs.getInt("ItemID");

                        String insertFavSql = "INSERT INTO Favorite (CustomerID, ItemID) VALUES (?, ?)";
                        PreparedStatement insertStmt = conn.prepareStatement(insertFavSql);
                        insertStmt.setInt(1, customerId);
                        insertStmt.setInt(2, itemId);
                        insertStmt.executeUpdate();
                    }

                    VBox favCard = createFoodCard(name, price, imagePath);
                    favoritesContainer.getChildren().add(favCard);

                } else {
                    favoritesItems.remove(name);
                    favoritesButton.setText("Add To Favorites");

                    String deleteFavSql = "DELETE FROM Favorite WHERE CustomerID = ? AND ItemID IN (SELECT i.ItemID FROM Item i JOIN MenuItem m ON i.MenuItemID = m.FoodID WHERE m.Name = ?)";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteFavSql);
                    deleteStmt.setInt(1, customerId);
                    deleteStmt.setString(2, name);
                    deleteStmt.executeUpdate();

                    favoritesContainer.getChildren().removeIf(node -> {
                        if (node instanceof VBox vbox) {
                            if (vbox.getChildren().size() > 1) {
                                Node child = vbox.getChildren().get(1);
                                if (child instanceof Label lbl) {
                                    return lbl.getText().equals(name);
                                }
                            }
                        }
                        return false;
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while updating favorites in the database.");
                alert.showAndWait();
            }
        });

        card.getChildren().addAll(imageView, nameLabel, priceLabel, addButton , favoritesButton);
        return card;
    }
    private void updateCounters() {
        items_counter_label_id.setText(String.valueOf(totalItems));
        total_price_label_id.setText(String.format("%.2f toman", totalPrice));

    }
    
    private VBox createhistoryCard(int counter, String time, String date, String item, int number, double totalPrice) {

        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 10; -fx-background-radius: 10;");
    
        
        Label nameLabel = new Label(String.format("Item #%d", counter));
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
    
        Label timeLabel = new Label("Time: " + time);
        Label dateLabel = new Label("Date: " + date);
        Label itemsLabel = new Label("Item: " + item);
        Label numLabel = new Label(String.format("Quantity: %d", number));
        Label totalLabel = new Label(String.format("Price: $%.2f", totalPrice));
    
        card.getChildren().addAll(nameLabel, timeLabel, dateLabel, itemsLabel, numLabel, totalLabel);
        return card;
    }
    

    @FXML
    public void initialize() {
        discountCodeBtn_id.setOnAction(e -> applyDiscountCode());
        items_counter_label_id.setOnMouseClicked(e -> {
            if (selectedItems.isEmpty()) {
                Alert emptyAlert = new Alert(AlertType.INFORMATION);
                emptyAlert.setTitle("Selected Items");
                emptyAlert.setHeaderText(null);
                emptyAlert.setContentText("No items selected yet.");
                emptyAlert.showAndWait();
                return;
            }

            StringBuilder sb = new StringBuilder();
            for (String item : selectedItems) {
                sb.append(item).append("\n");
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Selected Items");
            alert.setHeaderText("Your selected items:");
            alert.setContentText(sb.toString());
            alert.showAndWait();
        });
    }

    @FXML
    private void applyDiscountCode() {
        String code = discountCode_textFild_id.getText().trim();
        if (code.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Discount Code");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a discount code.");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            String sql = "SELECT d.DiscountPercentage, d.ExpirationDate " +
                        "FROM DiscountCode d " +
                        "JOIN Person p ON d.CustomerID = p.CustomerID " +
                        "WHERE d.Code = ? AND p.UserName = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code);
            pstmt.setString(2, username_label_id.getText());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double discountPercent = rs.getDouble("DiscountPercentage");
                java.sql.Date expirationDate = rs.getDate("ExpirationDate");
                java.util.Date today = new java.util.Date();

                if (expirationDate != null && today.after(expirationDate)) {
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setTitle("Discount Code");
                    alert.setHeaderText(null);
                    alert.setContentText("This discount code has expired.");
                    alert.showAndWait();
                    return;
                }

                double discountAmount = totalPrice * (discountPercent / 100.0);
                double newPrice = totalPrice - discountAmount;

                total_price_label_id.setText(String.format("%.2f toman", newPrice));

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Discount Applied");
                alert.setHeaderText(null);
                alert.setContentText("Discount code applied successfully! You got " + discountPercent + "% off.");
                alert.showAndWait();

            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Discount Code");
                alert.setHeaderText(null);
                alert.setContentText("Invalid discount code or it does not belong to you.");
                alert.showAndWait();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while applying discount code.");
            alert.showAndWait();
        }
    }

    @FXML
    void payBtnOnClicked(ActionEvent event) {
        if (datePicker_id.getValue() == null || time_textFild_id.getText().trim().isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Invalid Input");
            alert.setHeaderText(null);
            alert.setContentText("Please select a date and enter a time.");
            alert.showAndWait();
            return;
        }

        java.sql.Date sqlDate = java.sql.Date.valueOf(datePicker_id.getValue());
        String time = time_textFild_id.getText().trim();
        int managerId = 1;

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            conn.setAutoCommit(false); // شروع تراکنش

            String sqlCustomer = "SELECT CustomerID FROM Person p WHERE p.UserName = ?";
            int customerId;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCustomer)) {
                pstmt.setString(1, _username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText("Customer not found.");
                        alert.showAndWait();
                        return;
                    }
                    customerId = rs.getInt("CustomerID");
                }
            }

            String insertReservationSql = "INSERT INTO Reservation (CustomerID, Date, Time) " +  "OUTPUT INSERTED.ReservationID " + " VALUES ( ?, ?, ?)";
            PreparedStatement insertReservationStmt = conn.prepareStatement(insertReservationSql);
            insertReservationStmt.setInt(1, customerId);
            insertReservationStmt.setDate(2, sqlDate);
            insertReservationStmt.setString(3, time);
            //insertReservationStmt.setInt(5, managerId);

            ResultSet ReservRs = insertReservationStmt.executeQuery();
            int reservationID = -1;
            if (ReservRs.next()) {
                reservationID = ReservRs.getInt("ReservationID");
            }

            Map<String, Integer> itemCountMap = new HashMap<>();
            for (String itemStr : selectedItems) {
                String itemName = itemStr.split("-")[0].trim(); // استخراج اسم
                itemCountMap.put(itemName, itemCountMap.getOrDefault(itemName, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : itemCountMap.entrySet()) {
                String itemName = entry.getKey();
                int quantity = entry.getValue();
            
                String itemSql = "SELECT i.ItemID FROM Item i JOIN MenuItem m ON i.MenuItemID = m.FoodID WHERE m.Name = ?";
                try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                    itemStmt.setString(1, itemName);
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        if (itemRs.next()) {
                            int itemId = itemRs.getInt("ItemID");
                            double price = getItemPrice(itemName);
            
                            String insertDetailSql = "INSERT INTO ReservationDetail (ReservationID, ItemID, Quantity, PriceAtOrderTime) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement insertDetailStmt = conn.prepareStatement(insertDetailSql)) {
                                insertDetailStmt.setInt(1, reservationID);
                                insertDetailStmt.setInt(2, itemId);
                                insertDetailStmt.setInt(3, quantity);
                                insertDetailStmt.setDouble(4, price);
                                insertDetailStmt.executeUpdate();
                            }
                        } else {
                            throw new SQLException("Item not found in DB: " + itemName);
                        }
                    }
                }
            }
            
            conn.commit(); // تایید تراکنش

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Reservation saved successfully!");
            alert.showAndWait();

            selectedItems.clear();
            totalItems = 0;
            totalPrice = 0.0;
            updateCounters();

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while saving reservation.");
            alert.showAndWait();
        }
    }
    

    private double getItemPrice(String itemName) {
        String sql = "SELECT m.Price " +
                     "FROM MenuItem m " +
                     "JOIN Item i ON i.MenuItemID = m.FoodID " +
                     "WHERE m.Name = ?";
        double itemPrice = -1;
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                itemPrice = rs.getDouble("Price");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemPrice;
    }
    
    




    @FXML
    void logoutBtnOnCkicked(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("mainScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load login page.");
            alert.showAndWait();
        }

    }

    @FXML
    void menuBtnOnClicked(ActionEvent event) {
        try {
            String name = name_label_id.getText();
            String username = username_label_id.getText();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("homeScene.fxml"));
            Parent root = loader.load();

            homeSceneController homeSceneController = loader.getController();
            homeSceneController.setUserData(name, username);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
           
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load menu page.");
            alert.showAndWait();
        }

    }

    @FXML
    void editProfileBtnOnClicked(ActionEvent event) {
        try {
            String name = name_label_id.getText();
            String username = username_label_id.getText();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("editProfileScene.fxml"));
            Parent root = loader.load();

            editProfileSceneController editProfileSceneController = loader.getController();
            editProfileSceneController.setUserData(name, username);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
           
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load Edit Profile page.");
            alert.showAndWait();
        }

    }

}