package com.mycompany.tes;

import com.mycompany.tes.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.mycompany.tes.KoneksiDB;

/**
 *
 * @author Ahmad
 */
public class LoginController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkToRegister;
    @FXML private ImageView imgLogoLogin;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Image logoAsset = new Image(getClass().getResourceAsStream("/com/mycompany/tes/images/logo.png"));
            imgLogoLogin.setImage(logoAsset);
        } catch (Exception e) {
            System.out.println("Aset logo belum ditemukan di folder images.");
        }
    }    

    @FXML
    private void ActLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showJavaFXAlert(AlertType.WARNING, "Peringatan", "Login Gagal", "Username dan Password wajib diisi!");
            return;
        }

        String sql = "SELECT * FROM tb_pengguna WHERE username = ? AND password = ?";

        try (java.sql.Connection conn = KoneksiDB.getKoneksi();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String roleYgDitemukan = rs.getString("role");

                    com.mycompany.tes.SesiPengguna.setIdPengguna(rs.getInt("id_pengguna"));
                    com.mycompany.tes.SesiPengguna.setNama(rs.getString("nama"));
                    com.mycompany.tes.SesiPengguna.setEmail(rs.getString("email"));
                    com.mycompany.tes.SesiPengguna.setUsernameAktif(username);
                    com.mycompany.tes.SesiPengguna.setRole(roleYgDitemukan);

                    showJavaFXAlert(AlertType.INFORMATION, "Sukses", "Login Berhasil", "Selamat datang kembali, " + com.mycompany.tes.SesiPengguna.getNama() + "!");

                    if (roleYgDitemukan.equalsIgnoreCase("admin")) {
                        App.setRoot("admin/AdminEventList");
                    } else if (roleYgDitemukan.equalsIgnoreCase("pembeli")) {
                        App.setRoot("pembeli/PembeliHomepage");
                    } else {
                        showJavaFXAlert(AlertType.ERROR, "Error Sistem", "Role Tidak Dikenali", "Hak akses akun kamu tidak terdaftar di sistem.");
                    }

                } else {
                    showJavaFXAlert(AlertType.ERROR, "Login Gagal", "Kombinasi Salah", "Username atau Password salah, silakan cek kembali.");
                }
            }

        } catch (java.sql.SQLException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Database", "Koneksi Terputus", "Gagal menghubungkan ke server database V-PASS.");
            e.printStackTrace();
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Sistem", "Gagal Memuat Dashboard", "File tampilan FXML dashboard tujuan tidak ditemukan.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            App.setRoot("Register");
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Berpindah Halaman", "Halaman registrasi gagal dimuat.");
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