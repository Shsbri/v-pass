package com.mycompany.tes.components;

import com.mycompany.tes.App;
import com.mycompany.tes.SesiPengguna;
import java.io.File;
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

public class NavbarPembeliController implements Initializable {

    @FXML private Button btnHome;
    @FXML private Button btnTicket;
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;
    @FXML private ImageView imgLogo; 

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            File fileLogo = new File("images/logo.png");
            if (fileLogo.exists()) {
                imgLogo.setImage(new Image(fileLogo.toURI().toString()));
            }
        } catch (Exception e) {
            System.out.println("Aset logo belum diletakkan di folder images.");
        }

        Platform.runLater(() -> {
            try {
                resetSemuaTombol();
                
                String currentRoot = App.getCurrentRoot(); 
                if (currentRoot != null) {
                    String page = currentRoot.toLowerCase();
                    
                    if (page.contains("homepage")) {
                        setTombolAktif(btnHome);
                    } else if (page.contains("ticket")) {
                        setTombolAktif(btnTicket);
                    } else if (page.contains("history")) {
                        setTombolAktif(btnHistory);
                    } else if (page.contains("profile")) {
                        setTombolAktif(btnProfile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void resetSemuaTombol() {
        String stylePasif = "-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-weight: normal;";
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

    @FXML
    private void onLogoutClick(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Apakah Anda yakin ingin keluar?");
        alert.setContentText("Sesi Anda akan dihapus dan Anda harus login kembali.");

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