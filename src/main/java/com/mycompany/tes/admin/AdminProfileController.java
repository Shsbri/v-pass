package com.mycompany.tes.admin;

import com.mycompany.tes.App;
import com.mycompany.tes.SesiPengguna;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class AdminProfileController implements Initializable {

    @FXML private Label lblNamaAtas;
    @FXML private Label lblEmail;
    @FXML private Label lblUsername;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (SesiPengguna.getIdPengguna() != 0) {
            lblNamaAtas.setText(SesiPengguna.getNama());
            lblEmail.setText(SesiPengguna.getEmail());
            lblUsername.setText(SesiPengguna.getUsernameAktif());
        } else {
            lblNamaAtas.setText("Admin Guest");
            lblEmail.setText("guest@vpass.com");
            lblUsername.setText("guest");
        }
    }    
    
    @FXML
    private void keFormEditProfile(ActionEvent event) {
        try {
            App.setRoot("admin/AdminEditProfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void keFormAddAdmin(ActionEvent event) {
        try {
            App.setRoot("admin/AdminRegistVPASS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}