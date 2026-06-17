package com.mycompany.tes.components;

import com.mycompany.tes.App;
import com.mycompany.tes.SesiPengguna; // Impor jembatan sesi global
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * FXML Controller class untuk Komponen Sidebar Navigasi Pembeli
 * @author Ahmad
 */
public class NavbarPembeliController implements Initializable {

    @FXML private Button btnHome;
    @FXML private Button btnTicket;
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;
    @FXML private ImageView imgLogo; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Image imageAsset = new Image(getClass().getResourceAsStream("/com/mycompany/tes/images/logo.png"));
            imgLogo.setImage(imageAsset);
        } catch (Exception e) {
            System.out.println("Aset logo belum diletakkan di folder images.");
        }

        Platform.runLater(() -> {
            if (url != null) {
                String pathFile = url.toString().toLowerCase();
                resetSemuaTombol();
                
                if (pathFile.contains("homepage")) {
                    setTombolAktif(btnHome);
                } else if (pathFile.contains("myticket")) {
                    setTombolAktif(btnTicket);
                } else if (pathFile.contains("history")) {
                    setTombolAktif(btnHistory);
                } else if (pathFile.contains("profile")) {
                    setTombolAktif(btnProfile);
                }
            }
        });
    }

    private void resetSemuaTombol() {
        String stylePasif = "-fx-background-color: transparent; -fx-text-fill: #666666;";
        btnHome.setStyle(stylePasif);
        btnTicket.setStyle(stylePasif);
        btnHistory.setStyle(stylePasif);
        btnProfile.setStyle(stylePasif);
    }

    private void setTombolAktif(Button btn) {
        btn.setStyle("-fx-background-color: #111111; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML private void keHomepage(ActionEvent event) { try { App.setRoot("pembeli/PembeliHomepage"); } catch (Exception e) {} }
    @FXML private void keMyTicket(ActionEvent event) { try { App.setRoot("pembeli/PembeliMyTicket"); } catch (Exception e) {} }
    @FXML private void keHistory(ActionEvent event) { try { App.setRoot("pembeli/PembeliHistory"); } catch (Exception e) {} }
    @FXML private void keProfile(ActionEvent event) { try { App.setRoot("pembeli/PembeliProfile"); } catch (Exception e) {} }

    // 🎯 FUNGSI LOGOUT DENGAN KONFIRMASI TERLEBIH DAHULU
    @FXML
    private void onLogoutClick(ActionEvent event) {
        // 1. Membuat pop-up konfirmasi
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Apakah Anda yakin ingin keluar?");
        alert.setContentText("Sesi Anda akan dihapus dan Anda harus login kembali.");

        // 2. Menunggu respon/pilihan dari pengguna (OK / Cancel)
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Bersihkan token username yang tersimpan di sistem
                SesiPengguna.clearSesi();
                
                // Tendang balik ke pintu utama (Halaman Login)
                App.setRoot("Login");
            } catch (java.io.IOException e) {
                System.out.println("Gagal memuat ulang halaman Login.fxml");
                e.printStackTrace();
            }
        } else {
            // Jika memilih Cancel, pop-up akan tertutup dan pengguna tetap di halaman semula
            System.out.println("Logout dibatalkan oleh pengguna.");
        }
    }
}