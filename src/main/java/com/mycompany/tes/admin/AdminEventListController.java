package com.mycompany.tes.admin;

import com.mycompany.tes.KoneksiDB;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AdminEventListController implements Initializable {

    @FXML private VBox containerEvent;
    @FXML private ComboBox<String> cmbTime;
    @FXML private ComboBox<String> cmbCategory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTime.setItems(FXCollections.observableArrayList("All Time", "This Month", "Upcoming Events", "Past Events"));
        cmbCategory.setItems(FXCollections.observableArrayList("All Category", "Konser", "Expo", "Seminar", "Festival"));
        
        cmbTime.setValue("All Time");
        cmbCategory.setValue("All Category");

        muatDataDariDatabase();
    }    

    private void muatDataDariDatabase() {
        containerEvent.getChildren().clear();
        
        StringBuilder sql = new StringBuilder("SELECT * FROM tb_event WHERE 1=1");
        
        String filterWaktu = cmbTime.getValue();
        if (filterWaktu != null) {
            if (filterWaktu.equals("This Month")) {
                sql.append(" AND YEAR(tanggal_event) = YEAR(NOW()) AND MONTH(tanggal_event) = MONTH(NOW())");
            } else if (filterWaktu.equals("Upcoming Events")) {
                sql.append(" AND tanggal_event >= DATE_FORMAT(NOW() + INTERVAL 1 MONTH, '%Y-%m-01')");
            } else if (filterWaktu.equals("Past Events")) {
                sql.append(" AND tanggal_event < DATE_FORMAT(NOW(), '%Y-%m-01')");
            }
        }

        String filterKategori = cmbCategory.getValue();
        if (filterKategori != null && !filterKategori.equals("All Category")) {
            sql.append(" AND kategori_event = ?");
        }

        sql.append(" ORDER BY id_event DESC");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (filterKategori != null && !filterKategori.equals("All Category")) {
                ps.setString(1, filterKategori);
            }

            try (ResultSet rs = ps.executeQuery()) {
                NumberFormat formatterDuit = NumberFormat.getInstance(new Locale("id", "ID"));
                SimpleDateFormat formatTgl = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));

                while (rs.next()) {
                    int idEvent = rs.getInt("id_event");
                    String namaEvent = rs.getString("nama_event");
                    String kategori = rs.getString("kategori_event");
                    int stok = rs.getInt("stok_tiket");
                    int harga = rs.getInt("harga_tiket");
                    java.sql.Date tglEvent = rs.getDate("tanggal_event");
                    String status = rs.getString("status_event");

                    String formatHarga = "IDR " + formatterDuit.format(harga);
                    String formatTanggal = formatTgl.format(tglEvent);

                    HBox row = new HBox();
                    row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    row.setMaxHeight(65.0);
                    row.setMinHeight(65.0);
                    row.setPrefHeight(65.0);
                    row.setSpacing(20.0);
                    row.setStyle("-fx-padding: 0 25 0 25; -fx-border-color: #F2F2F7; -fx-border-width: 0 0 1 0;");

                    Label lblId = new Label("EVT-" + idEvent);
                    lblId.setPrefWidth(80.0);
                    lblId.setTextFill(javafx.scene.paint.Color.web("#666666"));
                    lblId.setFont(new Font("System Medium", 14.0));

                    Label lblNama = new Label(namaEvent);
                    lblNama.setPrefWidth(180.0);
                    lblNama.setTextFill(javafx.scene.paint.Color.web("#111111"));
                    lblNama.setFont(new Font("System Bold", 14.0));
                    lblNama.setWrapText(true); 

                    Label lblHarga = new Label(formatHarga);
                    lblHarga.setPrefWidth(130.0);
                    lblHarga.setTextFill(javafx.scene.paint.Color.web("#FF5E00"));
                    lblHarga.setFont(new Font("System Bold", 14.0));

                    Label lblKategori = new Label(kategori);
                    lblKategori.setPrefWidth(100.0);
                    lblKategori.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblKategori.setFont(new Font(14.0));

                    Label lblTanggal = new Label(formatTanggal);
                    lblTanggal.setPrefWidth(120.0);
                    lblTanggal.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblTanggal.setFont(new Font("System Medium", 14.0));

                    Label lblStok = new Label(formatterDuit.format(stok) + " Tickets");
                    lblStok.setPrefWidth(110.0);
                    lblStok.setTextFill(javafx.scene.paint.Color.web("#444444"));
                    lblStok.setFont(new Font(14.0));

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button btnEdit = new Button("Edit");
                    btnEdit.setMnemonicParsing(false);
                    btnEdit.setPrefHeight(32.0);
                    btnEdit.setPrefWidth(65.0);
                    btnEdit.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E5EA; -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #444444; -fx-font-weight: Bold; -fx-cursor: hand;");

                    btnEdit.setOnAction(event -> {
                        AdminEditEventController.idEventDipilih = idEvent;
                        try {
                            com.mycompany.tes.App.setRoot("admin/AdminEditEvent");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    ToggleButton btnToggle = new ToggleButton();
                    btnToggle.setMnemonicParsing(false);
                    btnToggle.setPrefHeight(26.0);
                    btnToggle.setPrefWidth(54.0);
                    
                    if (status.equalsIgnoreCase("aktif")) {
                        btnToggle.setSelected(true);
                        btnToggle.setText("ON");
                        btnToggle.setStyle("-fx-background-color: #00B074; -fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10; -fx-cursor: hand;");
                    } else {
                        btnToggle.setSelected(false);
                        btnToggle.setText("OFF");
                        btnToggle.setStyle("-fx-background-color: #E5E5EA; -fx-background-radius: 15; -fx-text-fill: #666666; -fx-font-weight: bold; -fx-font-size: 10; -fx-cursor: hand;");
                    }

                    btnToggle.setOnAction(event -> handleToggleStatus(btnToggle));

                    row.getChildren().addAll(lblId, lblNama, lblHarga, lblKategori, lblTanggal, lblStok, spacer, btnEdit, btnToggle);
                    containerEvent.getChildren().add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        muatDataDariDatabase();
    }

    private void handleToggleStatus(ToggleButton btnSumber) {
        if (btnSumber.isSelected()) {
            btnSumber.setText("ON");
            btnSumber.setStyle("-fx-background-color: #00B074; -fx-background-radius: 15; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10; -fx-cursor: hand;");
        } else {
            btnSumber.setText("OFF");
            btnSumber.setStyle("-fx-background-color: #E5E5EA; -fx-background-radius: 15; -fx-text-fill: #666666; -fx-font-weight: bold; -fx-font-size: 10; -fx-cursor: hand;");
        }
    }

    @FXML
    private void keFormAddEvent(ActionEvent event) {
        try {
            com.mycompany.tes.App.setRoot("admin/AdminAddEvent");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}