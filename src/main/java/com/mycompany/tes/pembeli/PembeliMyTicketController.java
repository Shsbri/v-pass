package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
import com.mycompany.tes.SesiPengguna;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PembeliMyTicketController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterConcert;
    @FXML private Button btnFilterExpo;
    @FXML private Button btnFilterSeminar;
    @FXML private Button btnFilterFestival;
    @FXML private TilePane tilePaneTicket;

    private String kategoriAktif = "All";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataTiketPembeli();
    }    

    public void muatDataTiketPembeli() {
        tilePaneTicket.getChildren().clear();
        int idUser = SesiPengguna.getIdPengguna();

        StringBuilder sql = new StringBuilder(
            "SELECT tr.id_transaksi, e.nama_event, e.kategori_event, e.tanggal_event, e.gambar_event, tr.jumlah_tiket, tr.total_harga, tr.status_transaksi " +
            "FROM tb_transaksi tr " +
            "JOIN tb_event e ON tr.id_event = e.id_event " +
            "WHERE tr.id_pengguna = ? AND (LOWER(tr.status_transaksi) = 'menunggu pembayaran' OR LOWER(tr.status_transaksi) = 'menunggu verifikasi')"
        );

        if (!kategoriAktif.equals("All")) {
            sql.append(" AND e.kategori_event = ?");
        }

        String kataKunci = txtSearch.getText().trim();
        if (!kataKunci.isEmpty()) {
            sql.append(" AND e.nama_event LIKE ?");
        }

        sql.append(" ORDER BY tr.id_transaksi DESC");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            ps.setInt(idx++, idUser);
            
            if (!kategoriAktif.equals("All")) {
                ps.setString(idx++, kategoriAktif);
            }
            if (!kataKunci.isEmpty()) {
                ps.setString(idx, "%" + kataKunci + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat formatTgl = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                DecimalFormat formatUang = new DecimalFormat("#,###");

                while (rs.next()) {
                    final int idTrans = rs.getInt("id_transaksi");
                    final String namaEvent = rs.getString("nama_event");
                    final String kategori = rs.getString("kategori_event");
                    final java.sql.Date tglEvent = rs.getDate("tanggal_event");
                    final String namaGambar = rs.getString("gambar_event");
                    final int jmlTiket = rs.getInt("jumlah_tiket");
                    final long totalHarga = rs.getLong("total_harga");
                    final String statusTrans = rs.getString("status_transaksi");

                    VBox card = new VBox();
                    card.setPrefHeight(260.0);
                    card.setPrefWidth(196.0);
                    card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-border-color: #E5E5EA; -fx-border-radius: 20; -fx-padding: 12;");

                    AnchorPane containerGambar = new AnchorPane();
                    containerGambar.setPrefHeight(129.0);
                    containerGambar.setPrefWidth(172.0);
                    containerGambar.setStyle("-fx-background-color: #F2F2F7; -fx-background-radius: 14;");

                    ImageView imageView = new ImageView();
                    imageView.setFitHeight(129.0);
                    imageView.setFitWidth(172.0);
                    imageView.setPickOnBounds(true);

                    try {
                        File fileImg = new File("images/" + namaGambar);
                        if (fileImg.exists()) {
                            imageView.setImage(new Image(fileImg.toURI().toString()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Rectangle clip = new Rectangle(172.0, 129.0);
                    clip.setArcWidth(28.0);
                    clip.setArcHeight(28.0);
                    imageView.setClip(clip);
                    containerGambar.getChildren().add(imageView);

                    Label lblNama = new Label(namaEvent);
                    lblNama.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblNama.setFont(new Font("System Bold", 14.0));
                    VBox.setMargin(lblNama, new Insets(10.0, 0, 0, 0));

                    Label lblDetail = new Label(formatTgl.format(tglEvent) + " • " + jmlTiket + " Pcs");
                    lblDetail.setTextFill(javafx.scene.paint.Color.web("#666666"));
                    lblDetail.setFont(new Font(12.0));
                    VBox.setMargin(lblDetail, new Insets(2.0, 0, 10.0, 0));

                    HBox containerBawah = new HBox();
                    containerBawah.setAlignment(Pos.CENTER_LEFT);
                    VBox.setVgrow(containerBawah, Priority.ALWAYS);

                    Label lblTotal = new Label("Rp " + formatUang.format(totalHarga));
                    lblTotal.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblTotal.setFont(new Font("System Bold", 13.0));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnStatus = new Button();
                    btnStatus.setMnemonicParsing(false);
                    
                    if (statusTrans.equalsIgnoreCase("menunggu verifikasi")) {
                        btnStatus.setText("Verifying");
                        btnStatus.setStyle("-fx-background-color: #F2F2F7; -fx-text-fill: #8E8E93; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 11;");
                        btnStatus.setDisable(true);
                    } else {
                        btnStatus.setText("Pay");
                        btnStatus.setStyle("-fx-background-color: #FFF2E6; -fx-text-fill: #FF5E00; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 11;");
                        btnStatus.setOnAction(e -> {
                            bukaPopUpInvoice(idTrans, namaEvent, kategori, jmlTiket, totalHarga);
                        });
                    }

                    containerBawah.getChildren().addAll(lblTotal, spacer, btnStatus);
                    card.getChildren().addAll(containerGambar, lblNama, lblDetail, containerBawah);

                    tilePaneTicket.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bukaPopUpInvoice(int idTrans, String namaEvent, String kategori, int jmlTiket, long total) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/tes/pembeli/PembeliInvoicePopUp.fxml"));
            Parent root = loader.load();
            
            PembeliInvoicePopUpController controller = loader.getController();
            controller.setInvoiceData(idTrans, namaEvent, kategori, jmlTiket, total, this);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Transaction Invoice #" + idTrans);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        Button btnTerpilih = (Button) event.getSource();
        String teksTombol = btnTerpilih.getText();

        if (teksTombol.equalsIgnoreCase("All")) {
            kategoriAktif = "All";
        } else if (teksTombol.equalsIgnoreCase("Concert") || teksTombol.equalsIgnoreCase("Konser")) {
            kategoriAktif = "Konser";
        } else {
            kategoriAktif = teksTombol;
        }

        styleTombolFilter(btnFilterAll, kategoriAktif.equals("All"));
        styleTombolFilter(btnFilterConcert, kategoriAktif.equalsIgnoreCase("Konser"));
        styleTombolFilter(btnFilterExpo, kategoriAktif.equalsIgnoreCase("Expo"));
        styleTombolFilter(btnFilterSeminar, kategoriAktif.equalsIgnoreCase("Seminar"));
        styleTombolFilter(btnFilterFestival, kategoriAktif.equalsIgnoreCase("Festival"));

        muatDataTiketPembeli();
    }

    private void styleTombolFilter(Button btn, boolean isAktif) {
        if (isAktif) {
            btn.setStyle("-fx-background-color: #111111; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 18 8 18; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #444444; -fx-background-radius: 20; -fx-border-color: #E5E5EA; -fx-border-radius: 20; -fx-padding: 8 18 8 18; -fx-cursor: hand; -fx-font-weight: normal;");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        muatDataTiketPembeli();
    }
}