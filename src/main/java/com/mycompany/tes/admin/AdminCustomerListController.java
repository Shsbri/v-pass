package com.mycompany.tes.admin;

import com.mycompany.tes.KoneksiDB;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AdminCustomerListController implements Initializable {

    @FXML private ComboBox<String> cmbTime;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private VBox containerCustomer;
    
    private int halamanAktif = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTime.setItems(FXCollections.observableArrayList("All Time", "This Year", "This Month"));
        cmbTime.setValue("All Time");
        
        cmbStatus.setItems(FXCollections.observableArrayList("Active Users", "Suspended / Deleted"));
        cmbStatus.setValue("Active Users");
        
        muatDataCustomer();
    }    

    private void muatDataCustomer() {
        containerCustomer.getChildren().clear();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM tb_pengguna WHERE role = 'pembeli'");
        
        String statusTerpilih = cmbStatus.getValue();
        if ("Active Users".equals(statusTerpilih)) {
            sql.append(" AND is_deleted = 'active'");
        } else {
            sql.append(" AND is_deleted = 'deleted'");
        }
        
        String filterWaktu = cmbTime.getValue();
        if (filterWaktu != null) {
            if (filterWaktu.equals("This Year")) {
                sql.append(" AND YEAR(created_at) = YEAR(NOW())");
            } else if (filterWaktu.equals("This Month")) {
                sql.append(" AND YEAR(created_at) = YEAR(NOW()) AND MONTH(created_at) = MONTH(NOW())");
            }
        }

        String kataKunci = txtSearch.getText().trim();
        if (!kataKunci.isEmpty()) {
            sql.append(" AND (id_pengguna LIKE ? OR username LIKE ?)");
        }

        sql.append(" ORDER BY id_pengguna DESC LIMIT 15 OFFSET ?");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int indexParam = 1;
            if (!kataKunci.isEmpty()) {
                String kueriBersih = kataKunci.toUpperCase().replace("USR-", "");
                ps.setString(indexParam++, "%" + kueriBersih + "%");
                ps.setString(indexParam++, "%" + kataKunci + "%");
            }
            
            ps.setInt(indexParam, halamanAktif * 15);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPengguna = rs.getInt("id_pengguna");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String petName = rs.getString("nama_peliharaan");
                    String statusSistem = rs.getString("is_deleted");

                    HBox row = new HBox();
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    row.setMaxHeight(65.0);
                    row.setMinHeight(65.0);
                    row.setPrefHeight(65.0);
                    row.setSpacing(20.0);
                    row.setStyle("-fx-padding: 0 25 0 25; -fx-border-color: #F2F2F7; -fx-border-width: 0 0 1 0;");

                    Label lblId = new Label("USR-" + idPengguna);
                    lblId.setPrefWidth(120.0);
                    lblId.setTextFill(javafx.scene.paint.Color.web("#666666"));
                    lblId.setFont(new Font("System Medium", 14.0));

                    Label lblNama = new Label(username);
                    lblNama.setPrefWidth(130.0);
                    lblNama.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblNama.setFont(new Font("System Bold", 14.0));

                    Label lblEmail = new Label(email);
                    lblEmail.setPrefWidth(160.0);
                    lblEmail.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblEmail.setFont(new Font(14.0));

                    Label lblPass = new Label("••••••••");
                    lblPass.setPrefWidth(120.0);
                    lblPass.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblPass.setFont(new Font(14.0));

                    Label lblPet = new Label(petName != null ? petName : "-");
                    lblPet.setPrefWidth(120.0);
                    lblPet.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblPet.setFont(new Font(14.0));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnDelete = new Button();
                    btnDelete.setMnemonicParsing(false);
                    btnDelete.setPrefHeight(32.0);
                    btnDelete.setPrefWidth(85.0);

                    if ("active".equals(statusSistem)) {
                        btnDelete.setText("Suspend");
                        btnDelete.setStyle("-fx-background-color: #FFF2F2; -fx-background-radius: 10; -fx-text-fill: #FF3B30; -fx-font-weight: Bold; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> ubahStatusCustomer(idPengguna, username, "deleted"));
                    } else {
                        btnDelete.setText("Restore");
                        btnDelete.setStyle("-fx-background-color: #E5F9F0; -fx-background-radius: 10; -fx-text-fill: #00B074; -fx-font-weight: Bold; -fx-cursor: hand;");
                        btnDelete.setOnAction(event -> ubahStatusCustomer(idPengguna, username, "active"));
                    }

                    row.getChildren().addAll(lblId, lblNama, lblEmail, lblPass, lblPet, spacer, btnDelete);
                    containerCustomer.getChildren().add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ubahStatusCustomer(int idTarget, String namaTarget, String statusTujuan) {
        Alert konfirmasi = new Alert(AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Status");
        if ("deleted".equals(statusTujuan)) {
            konfirmasi.setHeaderText("Suspend Akun Customer");
            konfirmasi.setContentText("Apakah kamu yakin ingin menonaktifkan customer \"" + namaTarget + "\"? Riwayat transaksi akan tetap aman disimpan.");
        } else {
            konfirmasi.setHeaderText("Pulihkan Akun Customer");
            konfirmasi.setContentText("Apakah kamu yakin ingin mengaktifkan kembali customer \"" + namaTarget + "\"?");
        }
        
        Optional<ButtonType> opsi = konfirmasi.showAndWait();
        if (opsi.isPresent() && opsi.get() == ButtonType.OK) {
            String sql = "UPDATE tb_pengguna SET is_deleted = ? WHERE id_pengguna = ?";
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, statusTujuan);
                ps.setInt(2, idTarget);
                int barisDiubah = ps.executeUpdate();
                
                if (barisDiubah > 0) {
                    Alert sukses = new Alert(AlertType.INFORMATION);
                    sukses.setTitle("Sukses");
                    sukses.setHeaderText(null);
                    sukses.setContentText("Status akun customer berhasil diperbarui.");
                    sukses.showAndWait();
                    
                    muatDataCustomer();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        halamanAktif = 0;
        muatDataCustomer();
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        if (halamanAktif > 0) {
            halamanAktif--;
            muatDataCustomer();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (containerCustomer.getChildren().size() == 15) {
            halamanAktif++;
            muatDataCustomer();
        }
    }
}