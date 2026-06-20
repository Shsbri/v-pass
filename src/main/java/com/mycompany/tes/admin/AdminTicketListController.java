package com.mycompany.tes.admin;

import com.mycompany.tes.KoneksiDB;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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

public class AdminTicketListController implements Initializable {

    @FXML private Label lblSummaryStat;
    @FXML private ComboBox<String> cmbStatusFilter;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private VBox vboxTicketContainer;
    
    private int halamanAktif = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbStatusFilter.getItems().addAll("All Status", "belum_dipakai", "sudah_dipakai");
        cmbStatusFilter.getSelectionModel().selectFirst();
        
        hitungTotalStatistik();
        muatLembaranTiket();
    }    

    private void hitungTotalStatistik() {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status_tiket = 'sudah_dipakai' THEN 1 ELSE 0 END) as checked_in FROM tb_tiket";
        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int checkedIn = rs.getInt("checked_in");
                lblSummaryStat.setText("Checked-In: " + checkedIn + " / " + total + " Tickets");
            }
        } catch (Exception e) {
            lblSummaryStat.setText("Checked-In: 0 / 0 Tickets");
        }
    }

    private void muatLembaranTiket() {
        vboxTicketContainer.getChildren().clear();
        String statusTerpilih = cmbStatusFilter.getSelectionModel().getSelectedItem();
        String kataKunci = txtSearch.getText().trim();

        StringBuilder query = new StringBuilder(
            "SELECT t.unik_kode, e.nama_event, p.username, t.status_tiket " +
            "FROM tb_tiket t " +
            "JOIN tb_transaksi tr ON t.id_transaksi = tr.id_transaksi " +
            "JOIN tb_event e ON tr.id_event = e.id_event " +
            "JOIN tb_pengguna p ON tr.id_pengguna = p.id_pengguna " +
            "WHERE 1=1"
        );

        if (statusTerpilih != null && !statusTerpilih.equals("All Status")) {
            query.append(" AND t.status_tiket = ?");
        }
        if (!kataKunci.isEmpty()) {
            query.append(" AND t.unik_kode LIKE ?");
        }

        query.append(" ORDER BY t.id_tiket DESC LIMIT 15 OFFSET ?");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            int indexParam = 1;
            if (statusTerpilih != null && !statusTerpilih.equals("All Status")) {
                ps.setString(indexParam++, statusTerpilih);
            }
            if (!kataKunci.isEmpty()) {
                ps.setString(indexParam++, "%" + kataKunci + "%");
            }
            
            ps.setInt(indexParam, halamanAktif * 15);

            try (ResultSet rs = ps.executeQuery()) {
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    String kodeTiket = rs.getString("unik_kode");
                    String namaEvent = rs.getString("nama_event");
                    String username = rs.getString("username");
                    String status = rs.getString("status_tiket");

                    HBox row = buatBarisTiket(kodeTiket, namaEvent, username, status);
                    vboxTicketContainer.getChildren().add(row);
                }

                if (!adaData) {
                    Label lblKosong = new Label("No issued ticket matches found.");
                    lblKosong.setFont(new Font(14));
                    lblKosong.setStyle("-fx-text-fill: #8E8E93; -fx-padding: 25;");
                    vboxTicketContainer.getChildren().add(lblKosong);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buatBarisTiket(String kodeTiket, String namaEvent, String username, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxHeight(65.0);
        row.setMinHeight(65.0);
        row.setPrefHeight(65.0);
        row.setSpacing(20.0);
        row.setStyle("-fx-padding: 0 25 0 25; -fx-border-color: #F2F2F7; -fx-border-width: 0 0 1 0;");

        Label lblKode = new Label(kodeTiket);
        lblKode.setPrefWidth(180.0);
        lblKode.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold;");

        Label lblEvent = new Label(namaEvent);
        lblEvent.setPrefWidth(160.0);
        lblEvent.setStyle("-fx-text-fill: #444444;");

        Label lblUser = new Label(username);
        lblUser.setPrefWidth(120.0);
        lblUser.setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnStatus = new Button();
        btnStatus.setPrefSize(110.0, 32.0);
        
        if ("belum_dipakai".equalsIgnoreCase(status)) {
            btnStatus.setText("Pass");
            btnStatus.setStyle("-fx-background-color: #E5F9F0; -fx-text-fill: #00B074; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: hand;");
            btnStatus.setOnAction(e -> prosesCheckInTiket(kodeTiket));
        } else {
            btnStatus.setText("Checked In");
            btnStatus.setStyle("-fx-background-color: #F2F2F7; -fx-text-fill: #8E8E93; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: default;");
            btnStatus.setDisable(true);
        }

        row.getChildren().addAll(lblKode, lblEvent, lblUser, spacer, btnStatus);
        return row;
    }

    private void prosesCheckInTiket(String kodeTiket) {
        Alert konfirmasi = new Alert(AlertType.CONFIRMATION);
        konfirmasi.setTitle("Ticket Confirmation");
        konfirmasi.setHeaderText("Validate Entrance Pass");
        konfirmasi.setContentText("Are you sure you want to let this ticket pass? This action will permanently burn the ticket's active state.");

        Optional<ButtonType> opsi = konfirmasi.showAndWait();
        if (opsi.isPresent() && opsi.get() == ButtonType.OK) {
            String sql = "UPDATE tb_tiket SET status_tiket = 'sudah_dipakai' WHERE unik_kode = ?";
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, kodeTiket);
                int hasil = ps.executeUpdate();
                
                if (hasil > 0) {
                    Alert sukses = new Alert(AlertType.INFORMATION);
                    sukses.setTitle("Success");
                    sukses.setHeaderText(null);
                    sukses.setContentText("Ticket validation successful! You can now let the customer enter.");
                    sukses.showAndWait();
                    
                    hitungTotalStatistik();
                    muatLembaranTiket();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        halamanAktif = 0;
        muatLembaranTiket();
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        if (halamanAktif > 0) {
            halamanAktif--;
            muatLembaranTiket();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (vboxTicketContainer.getChildren().size() == 15) {
            halamanAktif++;
            muatLembaranTiket();
        }
    }
}