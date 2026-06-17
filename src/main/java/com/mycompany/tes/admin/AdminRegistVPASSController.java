package com.mycompany.tes.admin;

import com.mycompany.tes.App;
import com.mycompany.tes.KoneksiDB;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * 
 * @author Ahmad
 */
public class AdminRegistVPASSController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private TextField txtPet;
    @FXML private Button btnRegist;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnRegist.setOnAction(event -> handleRegisterAdmin());
    }    

    private void handleRegisterAdmin() {
        String nama = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirm.getText();
        String petName = txtPet.getText().trim();

        if (nama.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || petName.isEmpty()) {
            showJavaFXAlert(AlertType.WARNING, "Peringatan", "Data Belum Lengkap", "Semua kolom form wajib diisi!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showJavaFXAlert(AlertType.ERROR, "Input Salah", "Password Tidak Cocok", "Konfirmasi password harus sama dengan password utama.");
            return;
        }

        String sql = "INSERT INTO tb_pengguna (nama, username, email, password, nama_peliharaan, role) VALUES (?, ?, ?, ?, ?, 'admin')";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setString(5, petName);
            
            int barisTerpengaruh = ps.executeUpdate();
            
            if (barisTerpengaruh > 0) {
                showJavaFXAlert(AlertType.INFORMATION, "Sukses", "Admin Berhasil Ditambahkan", "Akun administrator baru dengan username \"" + username + "\" telah aktif.");
                clearForm();
                
                App.setRoot("admin/AdminProfile");
            }
            
        } catch (SQLException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Database", "Gagal Menyimpan Data", "Username atau Email mungkin sudah terdaftar di sistem.");
            e.printStackTrace();
        } catch (Exception e) {
            showJavaFXAlert(AlertType.ERROR, "Error Sistem", "Gagal Berpindah Halaman", "Terjadi kesalahan saat memuat halaman kembali.");
            e.printStackTrace();
        }
    }

    private void clearForm() {
        txtName.clear();
        txtEmail.clear();
        txtUsername.clear();
        txtPassword.clear();
        txtConfirm.clear();
        txtPet.clear();
    }

    @FXML
    private void kembaliKeProfile(ActionEvent event) {
        try {
            App.setRoot("admin/AdminProfile");
        } catch (Exception e) {
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