package com.mycompany.tes.pembeli;

import com.mycompany.tes.App;
import com.mycompany.tes.KoneksiDB;
import com.mycompany.tes.SesiPengguna;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class PembeliProfileController implements Initializable {

    @FXML private Label lblNamaAtas;
    @FXML private Label lblEmail;
    @FXML private Label lblUsername;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String usernameSession = SesiPengguna.getUsernameAktif();

        if (usernameSession == null) {
            lblNamaAtas.setText("Guest");
            lblEmail.setText("-");
            lblUsername.setText("-");
            return;
        }

        String sql = "SELECT nama, email, username FROM tb_pengguna WHERE username = ?";

        try (java.sql.Connection conn = KoneksiDB.getKoneksi();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usernameSession);
            
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String namaDb = rs.getString("nama");
                    String emailDb = rs.getString("email");
                    String usernameDb = rs.getString("username");

                    lblNamaAtas.setText(namaDb);
                    lblEmail.setText(emailDb);
                    lblUsername.setText(usernameDb);
                }
            }
            
        } catch (java.sql.SQLException e) {
            System.out.println("Gagal memuat info data akun profil pengguna dari database.");
            e.printStackTrace();
        }
    }    

    @FXML
    private void keHalamanEditProfile(ActionEvent event) {
        try {
            App.setRoot("pembeli/PembeliEditProfile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}