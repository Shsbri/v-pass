package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
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
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PembeliHomepageController implements Initializable {

    @FXML private TextField txtSearch;
    @FXML private Button btnFilterAll;
    @FXML private Button btnFilterConcert;
    @FXML private Button btnFilterExpo;
    @FXML private Button btnFilterSeminar;
    @FXML private Button btnFilterFestival;
    @FXML private TilePane tilePaneEvent;

    private String kategoriAktif = "All";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        muatDataCardEvent();
    }    

    private void muatDataCardEvent() {
        tilePaneEvent.getChildren().clear();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM tb_event WHERE LOWER(status_event) = 'aktif'");
        
        if (!kategoriAktif.equals("All")) {
            sql.append(" AND kategori_event = ?");
        }
        
        String pencarian = txtSearch.getText().trim();
        if (!pencarian.isEmpty()) {
            sql.append(" AND nama_event LIKE ?");
        }
        
        sql.append(" ORDER BY id_event DESC");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int indeksParam = 1;
            if (!kategoriAktif.equals("All")) {
                ps.setString(indeksParam++, kategoriAktif);
            }
            if (!pencarian.isEmpty()) {
                ps.setString(indeksParam, "%" + pencarian + "%");
            }

            try (ResultSet rs = ps.executeQuery()) {
                SimpleDateFormat formatTgl = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
                DecimalFormat formatUang = new DecimalFormat("#,###");

                while (rs.next()) {
                    final int idEvent = rs.getInt("id_event");
                    final String namaEvent = rs.getString("nama_event");
                    final java.sql.Date tglEvent = rs.getDate("tanggal_event");
                    final String namaGambar = rs.getString("gambar_event");
                    final String deskripsiEvent = rs.getString("deskripsi_event");
                    final int hargaTiket = rs.getInt("harga_tiket");

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

                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(172.0, 129.0);
                    clip.setArcWidth(28.0);
                    clip.setArcHeight(28.0);
                    imageView.setClip(clip);

                    containerGambar.getChildren().add(imageView);

                    Label lblNama = new Label(namaEvent);
                    lblNama.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblNama.setFont(new Font("System Bold", 14.0));
                    VBox.setMargin(lblNama, new Insets(10.0, 0, 0, 0));

                    Label lblTanggal = new Label(formatTgl.format(tglEvent));
                    lblTanggal.setTextFill(javafx.scene.paint.Color.web("#666666"));
                    lblTanggal.setFont(new Font(12.0));
                    VBox.setMargin(lblTanggal, new Insets(2.0, 0, 10.0, 0));

                    HBox containerBawah = new HBox();
                    containerBawah.setAlignment(Pos.CENTER_LEFT);
                    VBox.setVgrow(containerBawah, Priority.ALWAYS);

                    Label lblHarga = new Label("Rp " + formatUang.format(hargaTiket));
                    lblHarga.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblHarga.setFont(new Font("System Bold", 13.0));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnOrder = new Button("Order");
                    btnOrder.setMnemonicParsing(false);
                    btnOrder.setStyle("-fx-background-color: linear-gradient(to right, #FF5E00, #A800FF); -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold;");

                    btnOrder.setOnAction(e -> {
                        bukaPopUpDetailEvent(idEvent, namaEvent, formatTgl.format(tglEvent), deskripsiEvent, hargaTiket, namaGambar);
                    });

                    containerBawah.getChildren().addAll(lblHarga, spacer, btnOrder);
                    card.getChildren().addAll(containerGambar, lblNama, lblTanggal, containerBawah);
                    
                    tilePaneEvent.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void bukaPopUpDetailEvent(int id, String nama, String tanggal, String deskripsi, int harga, String gambar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/tes/pembeli/PembeliEventDetail.fxml"));
            Parent root = loader.load();
            
            PembeliEventDetailController detailController = loader.getController();
            detailController.setDataEvent(id, nama, tanggal, deskripsi, harga, gambar);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Event Detail - " + nama);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCategoryFilter(ActionEvent event) {
        Button btnTerpilih = (Button) event.getSource();
        String teksTombol = btnTerpilih.getText();

        if (teksTombol.equalsIgnoreCase("All")) {
            kategoriAktif = "All";
        } else {
            kategoriAktif = teksTombol;
        }

        aturGayaTombolFilter(btnFilterAll, kategoriAktif.equals("All"));
        aturGayaTombolFilter(btnFilterConcert, kategoriAktif.equalsIgnoreCase("Konser"));
        aturGayaTombolFilter(btnFilterExpo, kategoriAktif.equalsIgnoreCase("Expo"));
        aturGayaTombolFilter(btnFilterSeminar, kategoriAktif.equalsIgnoreCase("Seminar"));
        aturGayaTombolFilter(btnFilterFestival, kategoriAktif.equalsIgnoreCase("Festival"));

        muatDataCardEvent();
    }

    private void aturGayaTombolFilter(Button btn, boolean isAktif) {
        if (isAktif) {
            btn.setStyle("-fx-background-color: #111111; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 8 18 8 18; -fx-cursor: hand; -fx-font-weight: bold;");
        } else {
            btn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #444444; -fx-background-radius: 20; -fx-border-color: #E5E5EA; -fx-border-radius: 20; -fx-padding: 8 18 8 18; -fx-cursor: hand; -fx-font-weight: normal;");
        }
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        muatDataCardEvent();
    }
} 