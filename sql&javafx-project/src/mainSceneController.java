import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    
        try (Connection conn = DriverManager.getConnection(connectionUrl);
             Statement stmt = conn.createStatement()) {
    
            String sql = "SELECT * FROM Person";
            ResultSet rs = stmt.executeQuery(sql);
    
            boolean loginSuccessful = false;
    
            while (rs.next()) {
                String username = rs.getString("UserName");
                String password = rs.getString("Password");
    

                if (username.equals(input_username)) {
                    if (password.equals(input_password)) {
                        System.out.println("Login successful!");
                        loginSuccessful = true;
                        break;  
                    } else {
                        System.out.println("Incorrect password!");
                    }
                }
            }
    
            if (!loginSuccessful) {
                System.out.println("Username not found or password incorrect.");
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
                String sql = "SELECT MAX(PersonID) as MaxPersonID FROM Person";
                ResultSet rs = stmt.executeQuery(sql);

                int numlastId = 0;
                if (rs.next()) {  
                    String lastId = rs.getString("MaxPersonID");
                    if (lastId != null) {
                        numlastId = Integer.parseInt(lastId);
                    }
                }
                numlastId = numlastId + 1;

                sql = "SELECT UserName FROM Person" ;
                rs = stmt.executeQuery(sql);
                if (rs.next()) {  
                    String temp = rs.getString("UserName");
                    if (temp.equals(username)) {
                        flag = true;
                    }
                }

                if (!flag){
                    sql = "INSERT INTO Person (PersonID, Name, Email, Password, City, Alley, N_o , UserName) VALUES (?, ?, ?, ?, ?, ?, ? , ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);

                    pstmt.setInt(1, numlastId);
                    pstmt.setString(2, name); 
                    pstmt.setString(3, email); 
                    pstmt.setString(4, password); 
                    pstmt.setString(5, city);    
                    pstmt.setString(6, alley);   
                    pstmt.setString(7, n_o);     
                    pstmt.setString(8, username); 

                    pstmt.executeUpdate();

                    TabPan_id.getSelectionModel().select(0);



                }else{
                    System.out.println("username has already exist");

                }
                

                
                

                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        


    }
}