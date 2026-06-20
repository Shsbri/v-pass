package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
import com.mycompany.tes.SesiPengguna;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
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
        Rectangle clip = new Rectangle(528.0, 240.0);
        clip.setArcWidth(32.0);
        clip.setArcHeight(32.0);
        imgEventBanner.setClip(clip);
        
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
        int idPenggunaAktif = SesiPengguna.getIdPengguna();
        int totalBayar = hargaPerTiket * jumlahTiket;
        
        if (idPenggunaAktif == 0) {
            tampilkanAlert(AlertType.ERROR, "Error Sesi", "Sesi Identitas Kosong", "Silakan lakukan login ulang untuk mengaktifkan kredensial belanja.");
            return;
        }

        String sqlTransaksi = "INSERT INTO tb_transaksi (id_pengguna, id_event, jumlah_tiket, total_harga, status_transaksi) VALUES (?, ?, ?, ?, 'menunggu pembayaran')";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement psTrans = conn.prepareStatement(sqlTransaksi, Statement.RETURN_GENERATED_KEYS)) {
            
            psTrans.setInt(1, idPenggunaAktif);
            psTrans.setInt(2, idEvent);
            psTrans.setInt(3, jumlahTiket);
            psTrans.setLong(4, totalBayar);
            psTrans.executeUpdate();

            int idTransaksiDihasilkan = 0;
            try (ResultSet generatedKeys = psTrans.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idTransaksiDihasilkan = generatedKeys.getInt(1);
                }
            }

            if (idTransaksiDihasilkan == 0) {
                throw new java.sql.SQLException("Gagal mendapatkan kunci auto-increment pada data transaksi.");
            }

            tampilkanAlert(AlertType.INFORMATION, "Sukses", "Pemesanan Tiket Berhasil", "Invoice transaksi telah diterbitkan. Harap segera lakukan konfirmasi administrasi pembayaran di menu My Ticket.");
            
            Stage stage = (Stage) btnBuy.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            tampilkanAlert(AlertType.ERROR, "Sistem Kegagalan", "Proses Database Terputus", "Gagal mengamankan alokasi nomor transaksi tiket baru.");
            e.printStackTrace();
        }
    }

    private void tampilkanAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}