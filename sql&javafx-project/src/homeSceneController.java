import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.layout.TilePane;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class homeSceneController {
    String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Food System Database;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
    
    @FXML
    private VBox tabPanParent_id;

    @FXML
    private TabPane menuTapPane_id;

    @FXML
    private Label name_label_id;

    @FXML
    private Label username_label_id;

    @FXML
    private Button payBtn_id;

    @FXML
    private Label total_price_label_id;

    @FXML
    private Label items_counter_label_id;

    @FXML
    private TextField discountCode_textFild_id;

    @FXML
    private Button discountCodeBtn_id;

    @FXML
    private Button editProfileBtn_id;

    @FXML
    private Button logoutBtn_id;

    @FXML
    private Label pageTite_id;

    @FXML
    private HBox bottomPane_id;

    @FXML
    private Button menuBtn_id;

    @FXML
    private Button reservesBtn_id;

    @FXML
    private ChoiceBox<String> choiceeBox_id;

    private int totalItems = 0;
    private double totalPrice = 0.0;

    public void setUserData(String name, String username) {
        name_label_id.setText(name);
        username_label_id.setText(username);
        loadTabsFromDatabase();
        
    }

    private List<String> favoritesItems = new ArrayList<>();
    private TilePane favoritesContainer = new TilePane();

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> selectedItems = new ArrayList<>(); 

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

    @FXML
    public void initialize() {
        choiceeBox_id.getItems().addAll("Onsite", "Offsite");
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

    int discountCodeId = -1 ;

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
                discountCodeId = rs.getInt("DiscountCodeID");
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
        if (selectedItems.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Payment");
            alert.setHeaderText(null);
            alert.setContentText("No items selected for order.");
            alert.showAndWait();
            return;
        }

        String status = (String) choiceeBox_id.getValue();
        if (status == null || status.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Payment");
            alert.setHeaderText(null);
            alert.setContentText("Please select order type (Onsite/Offsite).");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {
            conn.setAutoCommit(false); 

            // پیدا کردن CustomerID
            String customerSql = "SELECT CustomerID FROM Person WHERE UserName = ?";
            PreparedStatement custStmt = conn.prepareStatement(customerSql);
            custStmt.setString(1, username_label_id.getText());
            ResultSet custRs = custStmt.executeQuery();
            int customerId = 0;
            if (custRs.next()) {
                customerId = custRs.getInt("CustomerID");
            }

            // ایجاد سفارش جدید
            String insertBillSql = "INSERT INTO Bill(Price , CustomerID) "+ "OUTPUT INSERTED.BillID " + "VALUES (? , ?)";
            PreparedStatement billStmt = conn.prepareStatement(insertBillSql);
            billStmt.setDouble(1, totalPrice);
            billStmt.setInt(2, customerId);

            ResultSet billRs = billStmt.executeQuery();
            int BillId = -1;
            if (billRs.next()) {
                BillId = billRs.getInt("BillID");
            }

            String insertOrderSql =
                "INSERT INTO [Order] (ManagerID, DiscountCodeID, BillID, CustomerID, Date, Type, TotalPrice, Status) " +
                "OUTPUT INSERTED.OrderID " +
                "VALUES (NULL, ?, ?, ?, GETDATE(), ?, ?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql);
            if (discountCodeId != -1) {
                orderStmt.setInt(1, discountCodeId);
            } else {
                orderStmt.setNull(1, java.sql.Types.INTEGER);
            }            
            orderStmt.setInt(2, BillId);
            orderStmt.setInt(3, customerId);
            orderStmt.setString(4, "Normal"); 
            orderStmt.setDouble(5, totalPrice);
            orderStmt.setString(6, status); 

            ResultSet orderRs = orderStmt.executeQuery();
            int orderId = 0;
            if (orderRs.next()) {
                orderId = orderRs.getInt("OrderID");
            }

            Map<String, Integer> itemCountMap = new HashMap<>();
            for (String itemStr : selectedItems) {
                String itemName = itemStr.split("-")[0].trim(); // استخراج اسم
                itemCountMap.put(itemName, itemCountMap.getOrDefault(itemName, 0) + 1);
            }

            // آیتم‌های انتخابی رو در جدول Include ثبت کن
            for (Map.Entry<String, Integer> entry : itemCountMap.entrySet()) {
                String itemName = entry.getKey();
                int quantity = entry.getValue();
            
                String itemSql = "SELECT i.ItemID FROM Item i JOIN MenuItem m ON i.MenuItemID = m.FoodID WHERE m.Name = ?";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                itemStmt.setString(1, itemName);
                ResultSet itemRs = itemStmt.executeQuery();
            
                if (itemRs.next()) {
                    int itemId = itemRs.getInt("ItemID");
            
                    // اگر جدول Include ستون Quantity داره:
                    String includeSql = "INSERT INTO Include (OrderID, ItemID, Quantity) VALUES (?, ?, ?)";
                    PreparedStatement includeStmt = conn.prepareStatement(includeSql);
                    includeStmt.setInt(1, orderId);
                    includeStmt.setInt(2, itemId);
                    includeStmt.setInt(3, quantity);
                    includeStmt.executeUpdate();
            
                    // اگر جدول Include ستون Quantity نداره و فقط PK داره:
                    // => باید یا طراحی DB رو تغییر بدی یا فقط یک بار Insert کنی (quantity رو نگه نداری).
                }
            }

            conn.commit(); // پایان تراکنش

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Payment");
            alert.setHeaderText(null);
            alert.setContentText("Your order has been placed successfully!");
            alert.showAndWait();

            // ریست داده‌ها
            selectedItems.clear();
            totalItems = 0;
            totalPrice = 0;
            updateCounters();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while saving your order.");
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

    @FXML
    void reservesBtnOnClicked(ActionEvent event) {
        
        try {
            String name = name_label_id.getText();
            String username = username_label_id.getText();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("reservesScene.fxml"));
            Parent root = loader.load();

            reservesSceneController reservesSceneController = loader.getController();
            reservesSceneController.setUserData(name, username);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
           
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not load reserves page.");
            alert.showAndWait();
        }

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

}


