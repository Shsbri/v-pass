package com.mycompany.tes.admin;

import com.mycompany.tes.App;
import com.mycompany.tes.KoneksiDB;
import com.mycompany.tes.SesiPengguna;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class AdminEditProfileController implements Initializable {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private Button btnSave;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataProfilAdmin();
        btnSave.setOnAction(event -> simpanPerubahanProfil());
    }    

    private void muatDataProfilAdmin() {
        int idAdmin = SesiPengguna.getIdPengguna();
        String sql = "SELECT * FROM tb_pengguna WHERE id_pengguna = ?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idAdmin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtName.setText(rs.getString("nama"));
                    txtEmail.setText(rs.getString("email"));
                    txtUsername.setText(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void simpanPerubahanProfil() {
        int idAdmin = SesiPengguna.getIdPengguna();
        String nama = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String username = txtUsername.getText().trim();

        if (nama.isEmpty() || email.isEmpty() || username.isEmpty()) {
            tampilkanAlert(AlertType.WARNING, "Peringatan", "Data Kosong", "Kolom Name, Email, dan Username wajib diisi!");
            return;
        }

        String sql = "UPDATE tb_pengguna SET nama = ?, email = ?, username = ? WHERE id_pengguna = ?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, username);
            ps.setInt(4, idAdmin);

            int barisTerpengaruh = ps.executeUpdate();
            if (barisTerpengaruh > 0) {
                SesiPengguna.setNama(nama);
                SesiPengguna.setEmail(email);
                SesiPengguna.setUsernameAktif(username);

                tampilkanAlert(AlertType.INFORMATION, "Sukses", "Profil Diperbarui", "Perubahan data profil Anda berhasil disimpan ke sistem.");
                App.setRoot("admin/AdminProfile");
            }
            
        } catch (SQLException e) {
            tampilkanAlert(AlertType.ERROR, "Error Database", "Gagal Update", "Username atau Email mungkin sudah digunakan oleh pengguna lain.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void kembaliKeProfile(ActionEvent event) {
        try {
            App.setRoot("admin/AdminProfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}