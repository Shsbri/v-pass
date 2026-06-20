package com.mycompany.tes.admin;

import com.mycompany.tes.KoneksiDB;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminVerificationController implements Initializable {

    @FXML private ComboBox<String> cmbCategory;
    @FXML private VBox vboxRowsContainer;
    
    private int halamanAktif = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        memuatKategoriFilter();
        memuatAntreanTransaksi();
        
        cmbCategory.setOnAction(e -> handleFilterReset());
    }

    private void memuatKategoriFilter() {
        cmbCategory.getItems().clear();
        cmbCategory.getItems().add("All Category");
        
        String sql = "SELECT DISTINCT kategori_event FROM tb_event";
        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                cmbCategory.getItems().add(rs.getString("kategori_event"));
            }
            cmbCategory.getSelectionModel().selectFirst();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void memuatAntreanTransaksi() {
        vboxRowsContainer.getChildren().clear();
        String kategoriTerpilih = cmbCategory.getSelectionModel().getSelectedItem();

        String sql = "SELECT t.id_transaksi, t.jumlah_tiket, t.bukti_pembayaran, e.nama_event, e.kategori_event " +
                     "FROM tb_transaksi t " +
                     "JOIN tb_event e ON t.id_event = e.id_event " +
                     "WHERE t.status_transaksi = 'menunggu verifikasi'";

        if (kategoriTerpilih != null && !kategoriTerpilih.equals("All Category")) {
            sql += " AND e.kategori_event = ?";
        }

        sql += " ORDER BY t.id_transaksi DESC LIMIT 15 OFFSET ?";

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int indexParam = 1;
            if (kategoriTerpilih != null && !kategoriTerpilih.equals("All Category")) {
                ps.setString(indexParam++, kategoriTerpilih);
            }
            
            ps.setInt(indexParam, halamanAktif * 15);

            try (ResultSet rs = ps.executeQuery()) {
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    int idTrans = rs.getInt("id_transaksi");
                    int jmlTiket = rs.getInt("jumlah_tiket");
                    String buktiImg = rs.getString("bukti_pembayaran");
                    String namaEvt = rs.getString("nama_event");
                    String katEvt = rs.getString("kategori_event");

                    HBox row = membuatBarisKomponen(idTrans, namaEvt, katEvt, jmlTiket, buktiImg);
                    vboxRowsContainer.getChildren().add(row);
                }

                if (!adaData) {
                    Label lblEmpty = new Label("No transactions pending verification.");
                    lblEmpty.setFont(new Font(14));
                    lblEmpty.setStyle("-fx-text-fill: #8E8E93; -fx-padding: 25;");
                    vboxRowsContainer.getChildren().add(lblEmpty);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox membuatBarisKomponen(int idTrans, String namaEvt, String katEvt, int jmlTiket, String buktiImg) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxHeight(65.0);
        row.setMinHeight(65.0);
        row.setPrefHeight(65.0);
        row.setSpacing(20.0);
        row.setStyle("-fx-padding: 0 25 0 25; -fx-border-color: #F2F2F7; -fx-border-width: 0 0 1 0;");

        int seedSalt = 0x5F3759DF;
        int bitwiseXor = idTrans ^ seedSalt;
        int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
        String invoiceId = "INV-" + maskedCode;

        Label lblId = new Label(invoiceId);
        lblId.setPrefWidth(100.0);
        lblId.setStyle("-fx-text-fill: #666666; -fx-font-family: 'System'; -fx-font-weight: bold;");

        Label lblNama = new Label(namaEvt);
        lblNama.setPrefWidth(180.0);
        lblNama.setStyle("-fx-text-fill: #111111; -fx-font-family: 'System'; -fx-font-weight: bold;");

        Label lblKategori = new Label(katEvt);
        lblKategori.setPrefWidth(110.0);
        lblKategori.setStyle("-fx-text-fill: #444444;");

        Label lblTiket = new Label(jmlTiket + (jmlTiket > 1 ? " Tickets" : " Ticket"));
        lblTiket.setPrefWidth(110.0);
        lblTiket.setStyle("-fx-text-fill: #444444;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnProof = new Button("Proof");
        btnProof.setPrefSize(75.0, 32.0);
        btnProof.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E5EA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #444444; -fx-font-weight: Bold; -fx-cursor: hand;");
        btnProof.setOnAction(e -> aksiBukaBukti(buktiImg));

        Button btnYes = new Button("Yes");
        btnYes.setPrefSize(65.0, 32.0);
        btnYes.setStyle("-fx-background-color: #E5F9F0; -fx-background-radius: 10; -fx-text-fill: #00B074; -fx-font-weight: Bold; -fx-cursor: hand;");
        btnYes.setOnAction(e -> aksiVerifikasi(idTrans, "berhasil", jmlTiket));

        Button btnNo = new Button("No");
        btnNo.setPrefSize(65.0, 32.0);
        btnNo.setStyle("-fx-background-color: #FFF2F2; -fx-background-radius: 10; -fx-text-fill: #FF3B30; -fx-font-weight: Bold; -fx-cursor: hand;");
        btnNo.setOnAction(e -> aksiVerifikasi(idTrans, "gagal", jmlTiket));

        row.getChildren().addAll(lblId, lblNama, lblKategori, lblTiket, spacer, btnProof, btnYes, btnNo);
        return row;
    }

    private void aksiBukaBukti(String buktiImg) {
        if (buktiImg == null || buktiImg.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("This user has not uploaded any payment receipt image yet.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/tes/admin/AdminProofViewPopUp.fxml"));
            Parent root = loader.load();
            
            AdminProofViewPopUpController popUpController = loader.getController();
            popUpController.setReceiptImage(buktiImg);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Payment Proof Preview");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void aksiVerifikasi(int idTrans, String statusBaru, int jmlTiket) {
        String queryUpdateTrans = "UPDATE tb_transaksi SET status_transaksi = ? WHERE id_transaksi = ?";
        
        try (Connection conn = KoneksiDB.getKoneksi()) {
            conn.setAutoCommit(false); 
            
            try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdateTrans)) {
                psUpdate.setString(1, statusBaru);
                psUpdate.setInt(2, idTrans);
                psUpdate.executeUpdate();
            }

            if (statusBaru.equals("berhasil")) {
                String queryInsertTiket = "INSERT INTO tb_tiket (id_transaksi, unik_kode, status_tiket) VALUES (?, ?, 'belum_dipakai')";
                try (PreparedStatement psInsertTiket = conn.prepareStatement(queryInsertTiket)) {
                    for (int i = 0; i < jmlTiket; i++) {
                        String kodeAcakTiket = "TCK-" + System.nanoTime() + "-" + (int)(Math.random() * 900 + 100);
                        psInsertTiket.setInt(1, idTrans);
                        psInsertTiket.setString(2, kodeAcakTiket);
                        psInsertTiket.addBatch();
                    }
                    psInsertTiket.executeBatch();
                }
            }
            
            conn.commit(); 
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Transaction update process finished. Status updated to: " + statusBaru.toUpperCase());
            alert.showAndWait();
            
            memuatAntreanTransaksi(); 
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleFilterReset() {
        halamanAktif = 0;
        memuatAntreanTransaksi();
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        if (halamanAktif > 0) {
            halamanAktif--;
            memuatAntreanTransaksi();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (vboxRowsContainer.getChildren().size() == 15) {
            halamanAktif++;
            memuatAntreanTransaksi();
        }
    }
}