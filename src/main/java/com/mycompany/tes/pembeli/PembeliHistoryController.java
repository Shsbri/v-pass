package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
import com.mycompany.tes.SesiPengguna;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PembeliHistoryController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private TilePane tilePaneHistory;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterConcert;
    @FXML private Button btnFilterExpo;
    @FXML private Button btnFilterSeminar;
    @FXML private Button btnFilterFestival;

    private String kategoriAktif = "All";
    private final int SEED_SALT = 0x5F3759DF;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> muatRiwayatBelanja());
        muatRiwayatBelanja();
    }    

    private void muatRiwayatBelanja() {
        tilePaneHistory.getChildren().clear();
        int currentUserId = SesiPengguna.getIdPengguna();
        String kataKunci = txtSearch.getText().trim();

        StringBuilder query = new StringBuilder(
            "SELECT tr.id_transaksi, tr.jumlah_tiket, tr.total_harga, tr.status_transaksi, e.nama_event, e.kategori_event, e.tanggal_event " +
            "FROM tb_transaksi tr " +
            "JOIN tb_event e ON tr.id_event = e.id_event " +
            "WHERE tr.id_pengguna = ?"
        );

        if (!"All".equalsIgnoreCase(kategoriAktif)) {
            query.append(" AND e.kategori_event = ?");
        }
        if (!kataKunci.isEmpty()) {
            query.append(" AND e.nama_event LIKE ?");
        }

        query.append(" ORDER BY tr.id_transaksi DESC");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            int idx = 1;
            ps.setInt(idx++, currentUserId);
            
            if (!"All".equalsIgnoreCase(kategoriAktif)) {
                ps.setString(idx++, kategoriAktif);
            }
            if (!kataKunci.isEmpty()) {
                ps.setString(idx, "%" + kataKunci + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat formatTgl = new SimpleDateFormat("dd MMM yyyy");
                SimpleDateFormat formatJam = new SimpleDateFormat("HH:mm");

                while (rs.next()) {
                    int idTrans = rs.getInt("id_transaksi");
                    int tiket = rs.getInt("jumlah_tiket");
                    long total = rs.getLong("total_harga");
                    String status = rs.getString("status_transaksi");
                    String namaEvent = rs.getString("nama_event");
                    String kategori = rs.getString("kategori_event");
                    java.sql.Timestamp tgl = rs.getTimestamp("tanggal_event");

                    VBox card = buatCardHistory(idTrans, namaEvent, formatTgl.format(tgl), formatJam.format(tgl), status, kategori, tiket, total);
                    tilePaneHistory.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buatCardHistory(int idTrans, String nama, String tgl, String jam, String status, String kat, int tiket, long total) {
        VBox card = new VBox();
        card.setPrefSize(201.0, 265.0);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-color: #E5E5EA; -fx-border-radius: 20; -fx-padding: 12; -fx-cursor: hand;");

        VBox imgMock = new VBox();
        imgMock.setPrefSize(177.0, 130.0);
        imgMock.setStyle("-fx-background-color: #F2F2F7; -fx-background-radius: 14;");

        Label lblNama = new Label(nama);
        lblNama.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 0 0 0;");

        Label lblTgl = new Label(tgl);
        lblTgl.setStyle("-fx-text-fill: #666666; -fx-font-size: 12; -fx-padding: 2 0 6 0;");

        HBox badgeRow = new HBox();
        badgeRow.setSpacing(6.0);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        Label lblJam = new Label(jam);
        lblJam.setStyle("-fx-background-color: #F5F5F7; -fx-text-fill: #444444; -fx-padding: 4 8 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11;");

        Label lblStatus = new Label(status.equalsIgnoreCase("berhasil") ? "Success" : "Failed");
        if (status.equalsIgnoreCase("berhasil")) {
            lblStatus.setStyle("-fx-background-color: #E5F9F0; -fx-text-fill: #00B074; -fx-padding: 4 8 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11;");
        } else {
            lblStatus.setStyle("-fx-background-color: #FFF2F2; -fx-text-fill: #FF3B30; -fx-padding: 4 8 4 8; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11;");
        }

        badgeRow.getChildren().addAll(lblJam, lblStatus);
        card.getChildren().addAll(imgMock, lblNama, lblTgl, badgeRow);

        int bitwiseXor = idTrans ^ SEED_SALT;
        int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
        String invoiceId = "INV-" + maskedCode;

        card.setOnMouseClicked(e -> bukaPopUpDetail(idTrans, invoiceId, nama, kat, tiket, total, status));

        return card;
    }

    private void bukaPopUpDetail(int idTrans, String invoice, String nama, String kat, int tiket, long total, String status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/tes/pembeli/PembeliHistoryDetailPopUp.fxml"));
            Parent root = loader.load();

            PembeliHistoryDetailPopUpController controller = loader.getController();
            controller.setTransactionData(idTrans, invoice, nama, kat, tiket, total, status);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Transaction Receipt");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoryAction(ActionEvent event) {
        Button btnKlik = (Button) event.getSource();
        String txt = btnKlik.getText();
        
        if (txt.equalsIgnoreCase("Concert")) {
            kategoriAktif = "Konser";
        } else {
            kategoriAktif = txt;
        }
        
        muatRiwayatBelanja();
    }
}