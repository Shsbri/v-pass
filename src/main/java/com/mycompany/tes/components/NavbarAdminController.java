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

public class NavbarAdminController implements Initializable {

    @FXML private Button btnEventList;
    @FXML private Button btnVerification;
    @FXML private Button btnTransactionList;
    @FXML private Button btnCustomerList;
    @FXML private Button btnTicketList;
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
                resetStyleTombol();
                
                String currentRoot = App.getCurrentRoot();
                if (currentRoot != null) {
                    String page = currentRoot.toLowerCase();
                    
                    if (page.contains("admineventlist")) {
                        setTombolAktif(btnEventList);
                    } else if (page.contains("adminverification")) {
                        setTombolAktif(btnVerification);
                    } else if (page.contains("admintransactionlist")) {
                        setTombolAktif(btnTransactionList);
                    } else if (page.contains("admincustomerlist")) {
                        setTombolAktif(btnCustomerList);
                    } else if (page.contains("adminticketlist")) {
                        setTombolAktif(btnTicketList);
                    } else if (page.contains("adminprofile")) {
                        setTombolAktif(btnProfile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }    

    private void resetStyleTombol() {
        String pasif = "-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-weight: normal;";
        btnEventList.setStyle(pasif);
        btnVerification.setStyle(pasif);
        btnTransactionList.setStyle(pasif);
        btnCustomerList.setStyle(pasif);
        btnTicketList.setStyle(pasif);
        btnProfile.setStyle(pasif);
    }

    private void setTombolAktif(Button btn) {
        btn.setStyle("-fx-background-color: #111111; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    @FXML
    private void keAdminEvents(ActionEvent event) {
        try { App.setRoot("admin/AdminEventList"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void keAdminVerification(ActionEvent event) {
        try { App.setRoot("admin/AdminVerification"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void keAdminTransactionList(ActionEvent event) {
        try { App.setRoot("admin/AdminTransactionList"); } catch (Exception e) { e.printStackTrace(); }
    }
    
    @FXML
    private void keAdminCustomerList(ActionEvent event) {
        try { App.setRoot("admin/AdminCustomerList"); } catch (Exception e) { e.printStackTrace(); }
    }
    
    @FXML
    private void keAdminTicketList(ActionEvent event) {
        try { App.setRoot("admin/AdminTicketList"); } catch (Exception e) { e.printStackTrace(); }
    }
    
    @FXML 
    private void keAdminProfile(ActionEvent event) { 
        try { App.setRoot("admin/AdminProfile"); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onLogoutClick(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Logout");
        alert.setHeaderText("Apakah Anda yakin ingin keluar?");
        alert.setContentText("Sesi administrator Anda akan diakhiri.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                SesiPengguna.clearSesi();
                App.setRoot("Login");
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}