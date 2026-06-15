package com.mycompany.tes.components;

import com.mycompany.tes.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

/**
 * Controller untuk komponen Navbar Pembeli
 * @author Ahmad & Yulio
 */
public class NavbarPembeliController implements Initializable {

    @FXML private Button btnHome;
    @FXML private Button btnTicket;
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Init awal jika diperlukan
    }    

    @FXML
    private void keHomepage(ActionEvent event) {
        try {
            App.setRoot("pembeli/PembeliHomepage");
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Memuat Halaman", "File PembeliHomepage.fxml tidak ditemukan.");
            e.printStackTrace();
        }
    }

    @FXML
    private void keMyTicket(ActionEvent event) {
        try {
            App.setRoot("pembeli/PembeliMyTicket");
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Memuat Halaman", "File PembeliMyTicket.fxml tidak ditemukan.");
            e.printStackTrace();
        }
    }

    @FXML
    private void keHistory(ActionEvent event) {
        try {
            App.setRoot("pembeli/PembeliHistory");
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Memuat Halaman", "File PembeliHistory.fxml tidak ditemukan.");
            e.printStackTrace();
        }
    }

    @FXML
    private void keProfile(ActionEvent event) {
        try {
            // Catatan: Sesuaikan nama file profile pembeli kalian di sini (misal PembeliProfile atau PembeliEditProfile)
            App.setRoot("pembeli/PembeliProfile"); 
        } catch (java.io.IOException e) {
            showJavaFXAlert(AlertType.ERROR, "Error Navigasi", "Gagal Memuat Halaman", "File PembeliProfile.fxml tidak ditemukan.");
            e.printStackTrace();
        }
    }

    // Helper Method untuk menampilkan Alert standar JavaFX jika fxml gagal dimuat
    private void showJavaFXAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}