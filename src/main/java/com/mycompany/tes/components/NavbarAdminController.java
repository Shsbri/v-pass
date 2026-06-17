package com.mycompany.tes.components;

import com.mycompany.tes.App;
import com.mycompany.tes.SesiPengguna;
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
 * 
 * @author Ahmad
 */
public class NavbarAdminController implements Initializable {

    @FXML private Button btnEvents;
    @FXML private Button btnTransactions;
    @FXML private ImageView imgLogo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Image logoAsset = new Image(getClass().getResourceAsStream("/com/mycompany/tes/images/logo.png"));
            imgLogo.setImage(logoAsset);
        } catch (Exception e) {
            System.out.println("Aset logo belum diletakkan di folder images.");
        }

        Platform.runLater(() -> {
            if (url != null) {
               String pathFile = url.toString().toLowerCase();
               btnEvents.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666;");
               btnTransactions.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666;");
               
               if (pathFile.contains("event")) {
                   setTombolAktif(btnEvents);
               } else if (pathFile.contains("transaction")) {
                   setTombolAktif(btnTransactions);
               }
            }
        });
    }    

    private void setTombolAktif(Button btn) {
        btn.setStyle("-fx-background-color: #111111; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    private void keAdminEvents(ActionEvent event) {
        try { App.setRoot("admin/AdminEventList"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void keAdminTransactions(ActionEvent event) {
        // Kerangka navigasi transaksi admin jika nanti sudah siap dibuat halaman FXML-nya
        System.out.println("Navigasi ke Halaman Transaksi Admin.");
    }

    @FXML
    private void onLogoutClick(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout Admin");
        alert.setHeaderText("Apakah Anda yakin ingin keluar dari Panel Admin?");
        alert.setContentText("Sesi administrator Anda akan diakhiri.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SesiPengguna.clearSesi();
                App.setRoot("Login");
            } catch (java.io.IOException e) {
                System.out.println("Gagal memuat ulang halaman Login.fxml");
                e.printStackTrace();
            }
        }
    }
}