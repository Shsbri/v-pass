package com.mycompany.tes.admin;

import com.mycompany.tes.App;
import com.mycompany.tes.KoneksiDB;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

public class AdminEditEventController implements Initializable {

    public static int idEventDipilih;

    @FXML private TextField txtEventName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;
    @FXML private DatePicker dtEventDate;
    @FXML private TextField txtImageName;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Button btnBrowse;
    @FXML private Button btnUpdate;

    private File fileGambarTerpilih;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbCategory.setItems(FXCollections.observableArrayList("Konser", "Expo", "Seminar", "Festival"));
        cmbStatus.setItems(FXCollections.observableArrayList("aktif", "nonaktif"));
        
        txtPrice.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        txtStock.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        
        btnBrowse.setOnAction(event -> pilihGambarKomputer());
        btnUpdate.setOnAction(event -> eksekusiUpdateEvent());
        
        ambilDataLamaEvent();
    }    

    private void ambilDataLamaEvent() {
        String sql = "SELECT * FROM tb_event WHERE id_event = ?";
        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idEventDipilih);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    txtEventName.setText(rs.getString("nama_event"));
                    cmbCategory.setValue(rs.getString("kategori_event"));
                    txtPrice.setText(String.valueOf(rs.getInt("harga_tiket")));
                    txtStock.setText(String.valueOf(rs.getInt("stok_tiket")));
                    dtEventDate.setValue(rs.getDate("tanggal_event").toLocalDate());
                    txtImageName.setText(rs.getString("gambar_event"));
                    cmbStatus.setValue(rs.getString("status_event"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void pilihGambarKomputer() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Ubah Banner Gambar Event");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null) {
            fileGambarTerpilih = file;
            txtImageName.setText(file.getName());
        }
    }

    private void eksekusiUpdateEvent() {
        String namaEvent = txtEventName.getText().trim();
        String kategori = cmbCategory.getValue();
        String hargaRaw = txtPrice.getText().trim();
        String stokRaw = txtStock.getText().trim();
        LocalDate tanggal = dtEventDate.getValue();
        String namaGambar = txtImageName.getText().trim();
        String status = cmbStatus.getValue();

        if (namaEvent.isEmpty() || kategori == null || hargaRaw.isEmpty() || stokRaw.isEmpty() || tanggal == null || namaGambar.isEmpty() || status == null) {
            tampilkanAlert(AlertType.WARNING, "Peringatan", "Data Kosong", "Semua kolom form wajib diisi!");
            return;
        }

        if (fileGambarTerpilih != null) {
            try {
                File folderTujuan = new File("src/main/resources/com/mycompany/tes/images/");
                File fileTujuan = new File(folderTujuan, fileGambarTerpilih.getName());
                Files.copy(fileGambarTerpilih.toPath(), fileTujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String sql = "UPDATE tb_event SET nama_event=?, kategori_event=?, harga_tiket=?, stok_tiket=?, tanggal_event=?, status_event=?, gambar_event=? WHERE id_event=?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, namaEvent);
            ps.setString(2, kategori);
            ps.setInt(3, Integer.parseInt(hargaRaw));
            ps.setInt(4, Integer.parseInt(stokRaw));
            ps.setDate(5, Date.valueOf(tanggal));
            ps.setString(6, status);
            ps.setString(7, namaGambar);
            ps.setInt(8, idEventDipilih);
            
            int hasil = ps.executeUpdate();
            if (hasil > 0) {
                tampilkanAlert(AlertType.INFORMATION, "Sukses", "Update Berhasil", "Data event berhasil diperbarui.");
                App.setRoot("admin/AdminEventList");
            }
        } catch (SQLException e) {
            tampilkanAlert(AlertType.ERROR, "Error Database", "Gagal Update", "Terjadi kegagalan query SQL.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void kembaliKeEventList(ActionEvent event) {
        try {
            App.setRoot("admin/AdminEventList");
        } catch (Exception e) {
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