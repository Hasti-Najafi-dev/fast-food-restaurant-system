import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class editProfileSceneController {
    String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=Food System Database;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";

    @FXML
    private TextField edit_No_textField_id;

    @FXML
    private TextField edit_alley_textField_id;

    @FXML
    private TextField edit_city_textField_id;

    @FXML
    private TextField edit_email_textField_id;

    @FXML
    private TextField edit_name_textField_id;

    @FXML
    private PasswordField edit_password_textField_id;

    @FXML
    private TextField edit_username_textField_id;

    @FXML
    private Button logoutBtn_id;

    @FXML
    private Button menuBtn_id;

    @FXML
    private Label name_label_id;

    @FXML
    private Button ok_btn_id;

    @FXML
    private Label username_label_id;

    @FXML
    private Button reservesBtn_id;

    String name , Email , Password, City, Alley, N_o , username, PersonID;

    public void setUserData(String name, String _username) {
        username = _username;
        name_label_id.setText(name);
        username_label_id.setText(username);
        loadData();
        
    }
    


    private void loadData(){
        try (Connection conn = DriverManager.getConnection(connectionUrl);
            Statement stmt = conn.createStatement()) {
                String sql = "SELECT * FROM Person WHERE UserName = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                PersonID = rs.getString("PersonID");
                name = rs.getString("Name");
                Email = rs.getString("Email");
                Password = rs.getString("Password");
                City = rs.getString("City");
                Alley = rs.getString("Alley");
                N_o = rs.getString("N_o");

                edit_username_textField_id.setText(username);
                edit_name_textField_id.setText(name);
                edit_email_textField_id.setText(Email);
                edit_password_textField_id.setText(Password);
                edit_city_textField_id.setText(City);
                edit_alley_textField_id.setText(Alley);
                edit_No_textField_id.setText(N_o);

            } else {
                System.out.println("Username not found");
            }

            
        } catch (Exception e) {
            e.printStackTrace();
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
            alert.setContentText("Could not load Edit Profile page.");
            alert.showAndWait();
        }

    }

    @FXML
    void okBtnClicked(ActionEvent event) {
        String _name = edit_name_textField_id.getText();
        String _email = edit_email_textField_id.getText();
        String _password = edit_password_textField_id.getText();
        String _city = edit_city_textField_id.getText();
        String _alley = edit_alley_textField_id.getText();
        String _n_o = edit_No_textField_id.getText();
        String _username = edit_username_textField_id.getText();

        if (_name != null && _email != null && _password != null && _city != null && _alley != null && _n_o != null && _username != null) {
            try (Connection conn = DriverManager.getConnection(connectionUrl)) {

                // Check if new username is already taken by someone else
                String checkUsernameSql = "SELECT COUNT(*) FROM Person WHERE UserName = ? AND PersonID <> ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkUsernameSql);
                checkStmt.setString(1, _username);
                checkStmt.setString(2, PersonID);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int count = rs.getInt(1);
                    if (count > 0) {
                        // Username already exists
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Username Taken");
                        alert.setHeaderText(null);
                        alert.setContentText("The username '" + _username + "' is already taken. Please choose a different one.");
                        alert.showAndWait();
                        return;  // Stop the update process
                    }
                }

                // If username is unique, proceed with update
                String sql = "UPDATE Person SET Name = ?, Email = ?, Password = ?, City = ?, Alley = ?, N_o = ?, UserName = ? WHERE PersonID = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, _name);
                pstmt.setString(2, _email);
                pstmt.setString(3, _password);
                pstmt.setString(4, _city);
                pstmt.setString(5, _alley);
                pstmt.setString(6, _n_o);
                pstmt.setString(7, _username);
                pstmt.setString(8, PersonID);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Profile updated successfully.");
                    alert.showAndWait();

                    // Update UI labels and internal variables
                    name_label_id.setText(_name);
                    username_label_id.setText(_username);

                    name = _name;
                    Email = _email;
                    Password = _password;
                    City = _city;
                    Alley = _alley;
                    N_o = _n_o;
                    username = _username;

                } else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Update Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("No records were updated. Please try again.");
                    alert.showAndWait();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred while updating the profile.");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Input Validation");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all the fields.");
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


}
