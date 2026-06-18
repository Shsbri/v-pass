package com.mycompany.tes.pembeli;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class PembeliEventDetailController implements Initializable {

    @FXML private ImageView imgEventBanner;
    @FXML private Label lblEventName;
    @FXML private Label lblEventDate;
    @FXML private Label lblPricePerTicket;
    @FXML private Label lblEventDescription;
    @FXML private Label lblQuantity;
    @FXML private Label lblTotalPrice;
    @FXML private Button btnMinus;
    @FXML private Button btnPlus;
    @FXML private Button btnBuy;

    private int idEvent;
    private int hargaPerTiket = 0;
    private int jumlahTiket = 1;
    private final DecimalFormat formatUang = new DecimalFormat("#,###");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateKalkulasiTampilan();
    }    

    public void setDataEvent(int id, String nama, String tanggal, String deskripsi, int harga, String namaGambar) {
        this.idEvent = id;
        this.hargaPerTiket = harga;
        
        lblEventName.setText(nama);
        lblEventDate.setText(tanggal);
        lblPricePerTicket.setText("Rp " + formatUang.format(harga) + " / ticket");
        
        if (deskripsi == null || deskripsi.trim().isEmpty()) {
            lblEventDescription.setText("No description available for this event.");
        } else {
            lblEventDescription.setText(deskripsi);
        }

        try {
            File fileImg = new File("images/" + namaGambar);
            if (fileImg.exists()) {
                imgEventBanner.setImage(new Image(fileImg.toURI().toString()));
                imgEventBanner.setPreserveRatio(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateKalkulasiTampilan();
    }

    @FXML
    private void handleMinus(ActionEvent event) {
        if (jumlahTiket > 1) {
            jumlahTiket--;
            updateKalkulasiTampilan();
        }
    }

    @FXML
    private void handlePlus(ActionEvent event) {
        if (jumlahTiket < 10) {
            jumlahTiket++;
            updateKalkulasiTampilan();
        }
    }

    private void updateKalkulasiTampilan() {
        lblQuantity.setText(String.valueOf(jumlahTiket));
        int totalHarga = hargaPerTiket * jumlahTiket;
        lblTotalPrice.setText("Rp " + formatUang.format(totalHarga));
        
        btnMinus.setDisable(jumlahTiket <= 1);
        btnPlus.setDisable(jumlahTiket >= 10);
    }

    @FXML
    private void handleBuy(ActionEvent event) {
        System.out.println("Processing transaction for event ID: " + idEvent + " with " + jumlahTiket + " tickets.");
        
        Stage stage = (Stage) btnBuy.getScene().getWindow();
        stage.close();
    }
}