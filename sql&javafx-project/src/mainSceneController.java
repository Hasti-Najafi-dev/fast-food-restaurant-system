import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class mainSceneController {

    String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Food System Database;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
    

    @FXML
    private TabPane TabPan_id;
    
    @FXML
    private Button login_btn_id;

    @FXML
    private PasswordField login_password_textField_id;

    @FXML
    private TextField login_username_textField_id;

    @FXML
    private TextField signup_No_textField_id;

    @FXML
    private TextField signup_alley_textField_id;

    @FXML
    private Button signup_btn_id;

    @FXML
    private TextField signup_city_textField_id;

    @FXML
    private TextField signup_email_textField_id;

    @FXML
    private TextField signup_name_textField_id;

    @FXML
    private PasswordField signup_username_textField_id;

    @FXML
    private PasswordField signup_password_textField_id;

    @FXML
    void loginBtnClicked(ActionEvent event) {
        String input_username = login_username_textField_id.getText();
        String input_password = login_password_textField_id.getText();

        if (input_username.isEmpty() || input_password.isEmpty()) {
            System.out.println("Username or password is empty!");
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Username or password is empty!");
            alert.showAndWait();
            return;
        }

        try (Connection conn = DriverManager.getConnection(connectionUrl)) {

            String sql = "SELECT * FROM Person WHERE UserName = ? AND Password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, input_username);
            pstmt.setString(2, input_password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Login successful!");
                String name = rs.getString("Name");
                String username = rs.getString("UserName");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("homeScene.fxml"));
                Parent root = loader.load();

                homeSceneController homeController = loader.getController();
                homeController.setUserData(name, username);
            
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } else {
                System.out.println("Username not found or password incorrect.");
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Username not found or password incorrect.");
                alert.showAndWait();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    @FXML
    void signUpBtnClicked(ActionEvent event) {
        String name = signup_name_textField_id.getText();
        String email = signup_email_textField_id.getText();
        String password = signup_password_textField_id.getText();
        String city = signup_city_textField_id.getText();
        String alley =signup_alley_textField_id.getText();
        String n_o = signup_No_textField_id.getText();
        String username = signup_username_textField_id.getText();
        boolean flag = false;


        if (name != null && email != null && password != null && city != null && alley != null && n_o != null) {
            try (Connection conn = DriverManager.getConnection(connectionUrl);
             Statement stmt = conn.createStatement()) {
                String sql;
                sql = "SELECT UserName FROM Person" ;
                ResultSet rs;
                rs = stmt.executeQuery(sql);
                if (rs.next()) {  
                    String temp = rs.getString("UserName");
                    if (temp.equals(username)) {
                        flag = true;
                    }
                }

                if (!flag){
                    // Step 1: Insert into Customer and get generated key
                    String sqlCustomer = "INSERT INTO Customer DEFAULT VALUES;";
                    PreparedStatement pstmtCustomer = conn.prepareStatement(sqlCustomer, Statement.RETURN_GENERATED_KEYS);
                    pstmtCustomer.executeUpdate();

                    ResultSet generatedKeys = pstmtCustomer.getGeneratedKeys();
                    int custID = -1;
                    if (generatedKeys.next()) {
                        custID = generatedKeys.getInt(1);  
                    } else {
                        throw new SQLException("Creating customer failed, no ID obtained.");
                    }
                    pstmtCustomer.close();

                    // Step 2: Insert into Person using custID
                    String sqlPerson = "INSERT INTO Person (Name, Email, Password, City, Alley, N_o, CustomerID, UserName) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement pstmtPerson = conn.prepareStatement(sqlPerson);

                    pstmtPerson.setString(1, name);
                    pstmtPerson.setString(2, email);
                    pstmtPerson.setString(3, password);
                    pstmtPerson.setString(4, city);
                    pstmtPerson.setString(5, alley);
                    pstmtPerson.setString(6, n_o);
                    pstmtPerson.setInt(7, custID);
                    pstmtPerson.setString(8, username);

                    pstmtPerson.executeUpdate();
                    pstmtPerson.close();



                }else{
                    System.out.println("username has already exist");
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("username has already exist");
                    alert.showAndWait();

                }
                

                
                

                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        


    }
}