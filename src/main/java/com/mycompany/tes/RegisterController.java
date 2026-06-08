/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.tes;

import com.mycompany.tes.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javax.swing.*;

import com.mycompany.tes.KoneksiDB; 

/**
 * FXML Controller class
 *
 * @author Ahmad
 */
public class RegisterController implements Initializable {

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirm;
    @FXML
    private TextField txtPet;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void ActRegist(ActionEvent event) {
        String nama = txtName.getText();
        String email = txtEmail.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirm = txtConfirm.getText(); 
        String peliharaan = txtPet.getText();
        
        if(nama.isBlank() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty() || peliharaan.isEmpty()){
            JOptionPane.showMessageDialog(null, "Semua kolom form wajib diisi deks!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if(!password.equals(confirm)){
             JOptionPane.showMessageDialog(null, "Passwordnya beda kidz!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
            
        String sql = "insert into tb_pengguna (nama, email, username, password, role, nama_peliharaan) values (?, ?, ?, ?, 'pembeli', ?)";
            
        try (java.sql.Connection conn = KoneksiDB.getKoneksi();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
                
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setString(5, peliharaan);
                
            ps.executeUpdate();
            JOptionPane.showMessageDialog(null, "Gelo deks2 bisa");
            App.setRoot("Login");
        } catch (java.sql.SQLException e) {
            System.out.println("tiga");
            JOptionPane.showMessageDialog(null, "Kocak");
            e.printStackTrace();
        }catch (java.io.IOException e) {
            System.out.println("empat");
            // MENANGKAP EROR JALUR FXML YANG DILEMPAR OLEH SETROOT
            JOptionPane.showMessageDialog(null, "Gagal memuat halaman Login.fxml deks!");
            e.printStackTrace();
        }
    }
}