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

public class AdminAddEventController implements Initializable {

    @FXML private TextField txtEventName;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;
    @FXML private DatePicker dtEventDate;
    @FXML private TextField txtImageName;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Button btnBrowse;
    @FXML private Button btnSave;

    private File fileGambarTerpilih;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    cmbCategory.setItems(FXCollections.observableArrayList("Konser", "Expo", "Seminar", "Festival"));
    cmbStatus.setItems(FXCollections.observableArrayList("aktif", "nonaktif"));
    
    txtPrice.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
        if (change.getControlNewText().matches("\\d*")) {
            return change;
        }
        return null;
    }));

    txtStock.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
        if (change.getControlNewText().matches("\\d*")) {
            return change;
        }
        return null;
    }));

    btnBrowse.setOnAction(event -> pilihGambarKomputer());
    btnSave.setOnAction(event -> simpanEventBaru());
    }

    private void pilihGambarKomputer() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Banner Gambar Event");
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fc.showOpenDialog(btnBrowse.getScene().getWindow());
        if (file != null) {
            fileGambarTerpilih = file;
            txtImageName.setText(file.getName());
        }
    }

    private void simpanEventBaru() {
        String namaEvent = txtEventName.getText().trim();
        String kategori = cmbCategory.getValue();
        String hargaRaw = txtPrice.getText().trim();
        String stokRaw = txtStock.getText().trim();
        LocalDate tanggal = dtEventDate.getValue();
        String namaGambar = txtImageName.getText().trim();
        String status = cmbStatus.getValue();

        if (namaEvent.isEmpty() || kategori == null || hargaRaw.isEmpty() || stokRaw.isEmpty() || tanggal == null || namaGambar.isEmpty() || status == null || fileGambarTerpilih == null) {
            tampilkanAlert(AlertType.WARNING, "Peringatan", "Data Belum Lengkap", "Semua formulir input termasuk gambar wajib diisi!");
            return;
        }

        int harga;
        int stok;
        try {
            harga = Integer.parseInt(hargaRaw);
            stok = Integer.parseInt(stokRaw);
        } catch (NumberFormatException e) {
            tampilkanAlert(AlertType.ERROR, "Input Salah", "Format Angka Tidak Valid", "Harga tiket dan stok tiket harus berupa angka murni!");
            return;
        }

        try {
            File folderTujuan = new File("src/main/resources/com/mycompany/tes/images/");
            if (!folderTujuan.exists()) {
                folderTujuan.mkdirs();
            }
            
            File fileTujuan = new File(folderTujuan, fileGambarTerpilih.getName());
            Files.copy(fileGambarTerpilih.toPath(), fileTujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
        } catch (Exception e) {
            tampilkanAlert(AlertType.ERROR, "Error Sistem", "Gagal Upload Gambar", "Sistem gagal menyalin file gambar ke folder aset.");
            e.printStackTrace();
            return;
        }

        String sql = "INSERT INTO tb_event (nama_event, kategori_event, harga_tiket, stok_tiket, tanggal_event, status_event, gambar_event) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, namaEvent);
            ps.setString(2, kategori);
            ps.setInt(3, harga);
            ps.setInt(4, stok);
            ps.setDate(5, Date.valueOf(tanggal));
            ps.setString(6, status);
            ps.setString(7, namaGambar);
            
            int hasil = ps.executeUpdate();
            if (hasil > 0) {
                tampilkanAlert(AlertType.INFORMATION, "Sukses", "Event Berhasil Ditambahkan", "Data event dan file gambar berhasil disimpan.");
                App.setRoot("admin/AdminEventList");
            }
            
        } catch (SQLException e) {
            tampilkanAlert(AlertType.ERROR, "Error Database", "Gagal Menyimpan Data", "Terjadi kesalahan database saat menyimpan query.");
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