package com.mycompany.tes;

import com.mycompany.tes.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.mycompany.tes.KoneksiDB; 

/**
 * FXML Controller class
 *
 * @author Ahmad
 */
public class RegisterController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private TextField txtPet;
    @FXML private Hyperlink linkToLogin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init awal
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
            showJavaFXAlert(AlertType.WARNING, "Peringatan", "Formulir Tidak Lengkap", "Semua kolom form wajib diisi!");
            return;
        }
        
        if(!password.equals(confirm)){
            showJavaFXAlert(AlertType.WARNING, "Peringatan", "Password Mismatch", "Password dan konfirmasi password tidak cocok!");
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
            
            showJavaFXAlert(AlertType.INFORMATION, "Sukses", "Registrasi Berhasil", "Akun berhasil dibuat! Silakan login.");
            App.setRoot("Login");
        } catch (java.sql.SQLException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Database", "Gagal Menyimpan Data", "Terjadi kesalahan pada server database.");
            e.printStackTrace();
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Sistem", "Gagal Memuat Halaman", "File Login.fxml tidak ditemukan.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToLogin(ActionEvent event) {
        try {
            App.setRoot("Login"); 
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Berpindah Halaman", "Halaman login gagal dimuat.");
            e.printStackTrace();
        }
    }

    private void showJavaFXAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}