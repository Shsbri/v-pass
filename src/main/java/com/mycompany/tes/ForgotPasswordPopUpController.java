package com.mycompany.tes;

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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ForgotPasswordPopUpController implements Initializable {

    @FXML private TextField txtVerifyUsername;
    @FXML private TextField txtVerifyPetName;
    @FXML private VBox panelPetSection;
    @FXML private VBox panelNewPasswordSection;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmNewPassword;
    @FXML private Button btnCancel;
    @FXML private Button btnSubmit;

    private boolean isAccountVerified = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void handleSubmitAction(ActionEvent event) {
        if (!isAccountVerified) {
            prosesVerifikasiPertanyaanKeamanan();
        } else {
            prosesSimpanPasswordBaru();
        }
    }

    private void prosesVerifikasiPertanyaanKeamanan() {
        String username = txtVerifyUsername.getText().trim();
        String petName = txtVerifyPetName.getText().trim();

        if (username.isEmpty() || petName.isEmpty()) {
            tampilkanPesan(AlertType.WARNING, "Validasi Kosong", "Semua bidang verifikasi wajib diisi!");
            return;
        }

        String sql = "SELECT * FROM tb_pengguna WHERE username = ? AND nama_peliharaan = ?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, petName);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    isAccountVerified = true;
                    txtVerifyUsername.setDisable(true);
                    txtVerifyPetName.setDisable(true);
                    
                    panelNewPasswordSection.setDisable(false);
                    panelNewPasswordSection.setOpacity(1.0);
                    
                    btnSubmit.setText("Reset Password");
                    btnSubmit.setStyle("-fx-background-color: #00B074; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand;");
                    
                    tampilkanPesan(AlertType.INFORMATION, "Verifikasi Sukses", "Identitas cocok! Silakan buat password baru Anda.");
                } else {
                    tampilkanPesan(AlertType.ERROR, "Verifikasi Gagal", "Username atau nama peliharaan salah. Akses ditolak.");
                }
            }
        } catch (SQLException e) {
            tampilkanPesan(AlertType.ERROR, "Database Error", "Terjadi kegagalan interaksi dengan server database.");
            e.printStackTrace();
        }
    }

    private void prosesSimpanPasswordBaru() {
        String username = txtVerifyUsername.getText().trim();
        String passBaru = txtNewPassword.getText();
        String konfirmasiPass = txtConfirmNewPassword.getText();

        if (passBaru.isEmpty() || konfirmasiPass.isEmpty()) {
            tampilkanPesan(AlertType.WARNING, "Validasi Kosong", "Sandi baru dan konfirmasi tidak boleh kosong!");
            return;
        }

        if (!passBaru.equals(konfirmasiPass)) {
            tampilkanPesan(AlertType.WARNING, "Tidak Cocok", "Konfirmasi password tidak sesuai dengan sandi baru Anda.");
            return;
        }

        String sqlUpdate = "UPDATE tb_pengguna SET password = ? WHERE username = ?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {

            ps.setString(1, passBaru);
            ps.setString(2, username);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                tampilkanPesan(AlertType.INFORMATION, "Sukses", "Kata sandi V-PASS berhasil diperbarui! Silakan login kembali.");
                tutupJendela();
            } else {
                tampilkanPesan(AlertType.ERROR, "Gagal Ganti Password", "Data gagal diperbarui. Akun tidak ditemukan.");
            }
        } catch (SQLException e) {
            tampilkanPesan(AlertType.ERROR, "Database Error", "Gagal menyimpan password baru ke database.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        tutupJendela();
    }

    private void tutupJendela() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void tampilkanPesan(AlertType tipe, String judul, String isi) {
        Alert alert = new Alert(tipe);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(isi);
        alert.showAndWait();
    }
}