package com.mycompany.tes.admin;

import com.mycompany.tes.KoneksiDB;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AdminTransactionListController implements Initializable {

    @FXML private ComboBox<String> cmbTimeFilter;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;
    @FXML private Button btnExport;
    @FXML private VBox vboxArchiveContainer;
    
    private final DecimalFormat formatUang = new DecimalFormat("#,###");
    private final int SEED_SALT = 0x5F3759DF;
    private int halamanAktif = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbTimeFilter.getItems().addAll("All Time", "This Year", "This Month", "Today");
        cmbTimeFilter.getSelectionModel().selectFirst();
        
        muatArsipTransaksi();
    }    

    private void muatArsipTransaksi() {
        vboxArchiveContainer.getChildren().clear();
        String filterWaktu = cmbTimeFilter.getSelectionModel().getSelectedItem();
        String kataKunci = txtSearch.getText().trim();

        StringBuilder query = new StringBuilder(
            "SELECT tr.id_transaksi, p.username AS username_pembeli, tr.jumlah_tiket, tr.total_harga, tr.status_transaksi " +
            "FROM tb_transaksi tr " +
            "JOIN tb_pengguna p ON tr.id_pengguna = p.id_pengguna " +
            "WHERE tr.status_transaksi IN ('berhasil', 'gagal', 'menunggu pembayaran')"
        );

        if ("Today".equals(filterWaktu)) {
            query.append(" AND DATE(tr.waktu_transaksi) = CURRENT_DATE()");
        } else if ("This Month".equals(filterWaktu)) {
            query.append(" AND MONTH(tr.waktu_transaksi) = MONTH(CURRENT_DATE()) AND YEAR(tr.waktu_transaksi) = YEAR(CURRENT_DATE())");
        } else if ("This Year".equals(filterWaktu)) {
            query.append(" AND YEAR(tr.waktu_transaksi) = YEAR(CURRENT_DATE())");
        }

        int idHasilDekripsiPencarian = -1;
        if (!kataKunci.isEmpty()) {
            if (kataKunci.toUpperCase().startsWith("INV-")) {
                try {
                    String stringAngka = kataKunci.substring(4);
                    int targetMasked = Integer.parseInt(stringAngka);
                    
                    for (int idUji = 1; idUji <= 100000; idUji++) {
                        int bitwiseXor = idUji ^ SEED_SALT;
                        int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
                        if (maskedCode == targetMasked) {
                            idHasilDekripsiPencarian = idUji;
                            break;
                        }
                    }
                } catch (Exception e) {
                    idHasilDekripsiPencarian = -1;
                }
                query.append(" AND tr.id_transaksi = ?");
            } else {
                query.append(" AND p.username LIKE ?");
            }
        }

        query.append(" ORDER BY tr.id_transaksi DESC LIMIT 15 OFFSET ?");

        try (Connection conn = KoneksiDB.getKoneksi();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {

            int indexParam = 1;
            if (!kataKunci.isEmpty()) {
                if (idHasilDekripsiPencarian != -1) {
                    ps.setInt(indexParam++, idHasilDekripsiPencarian);
                } else if (kataKunci.toUpperCase().startsWith("INV-")) {
                    ps.setInt(indexParam++, 0); 
                } else {
                    ps.setString(indexParam++, "%" + kataKunci + "%");
                }
            }
            
            ps.setInt(indexParam, halamanAktif * 15);

            try (ResultSet rs = ps.executeQuery()) {
                boolean adaData = false;
                while (rs.next()) {
                    adaData = true;
                    int idTrans = rs.getInt("id_transaksi");
                    String usernameUser = rs.getString("username_pembeli");
                    int jmlTiket = rs.getInt("jumlah_tiket");
                    long totalHarga = rs.getLong("total_harga");
                    String status = rs.getString("status_transaksi");

                    HBox row = buatBarisArsip(idTrans, usernameUser, jmlTiket, totalHarga, status);
                    vboxArchiveContainer.getChildren().add(row);
                }

                if (!adaData) {
                    Label lblKosong = new Label("No historical records match your search criteria.");
                    lblKosong.setFont(new Font(14));
                    lblKosong.setStyle("-fx-text-fill: #8E8E93; -fx-padding: 25;");
                    vboxArchiveContainer.getChildren().add(lblKosong);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buatBarisArsip(int idTrans, String usernameUser, int jmlTiket, long totalHarga, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxHeight(65.0);
        row.setMinHeight(65.0);
        row.setPrefHeight(65.0);
        row.setSpacing(20.0);
        row.setStyle("-fx-padding: 0 25 0 25; -fx-border-color: #F2F2F7; -fx-border-width: 0 0 1 0;");

        int bitwiseXor = idTrans ^ SEED_SALT;
        int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
        String invoiceId = "INV-" + maskedCode;

        Label lblId = new Label(invoiceId);
        lblId.setPrefWidth(140.0);
        lblId.setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");

        Label lblNama = new Label(usernameUser);
        lblNama.setPrefWidth(140.0);
        lblNama.setStyle("-fx-text-fill: #111111; -fx-font-weight: bold;");

        Label lblTiket = new Label(jmlTiket + (jmlTiket > 1 ? " Tickets" : " Ticket"));
        lblTiket.setPrefWidth(140.0);
        lblTiket.setStyle("-fx-text-fill: #444444;");

        Label lblTotal = new Label("IDR " + formatUang.format(totalHarga));
        lblTotal.setPrefWidth(140.0);
        lblTotal.setStyle("-fx-text-fill: #444444;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnStatus = new Button();
        btnStatus.setPrefSize(95.0, 32.0);
        
        if ("berhasil".equalsIgnoreCase(status)) {
            btnStatus.setText("Success");
            btnStatus.setStyle("-fx-background-color: #E5F9F0; -fx-text-fill: #00B074; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: default;");
        } else if ("gagal".equalsIgnoreCase(status)) {
            btnStatus.setText("Rejected");
            btnStatus.setStyle("-fx-background-color: #FFF2F2; -fx-text-fill: #FF3B30; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: default;");
        } else {
            btnStatus.setText("Pending");
            btnStatus.setStyle("-fx-background-color: #F2F2F7; -fx-text-fill: #666666; -fx-background-radius: 10; -fx-font-weight: bold; -fx-cursor: default;");
        }

        row.getChildren().addAll(lblId, lblNama, lblTiket, lblTotal, spacer, btnStatus);
        return row;
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Transaction Report");
        fc.setInitialFileName("V_PASS_Report.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Workbook (*.xlsx)", "*.xlsx"));
        
        File fileSimpan = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (fileSimpan == null) return;

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(fileSimpan)) {
            
            Sheet sheet = workbook.createSheet("Report");
            Row rowHeader = sheet.createRow(0);
            String[] headers = {"Invoice ID", "Username", "Tickets", "Total Price (IDR)", "Status"};
            
            for (int i = 0; i < headers.length; i++) {
                rowHeader.createCell(i).setCellValue(headers[i]);
            }

            String sql = "SELECT tr.id_transaksi, p.username, tr.jumlah_tiket, tr.total_harga, tr.status_transaksi " +
                         "FROM tb_transaksi tr JOIN tb_pengguna p ON tr.id_pengguna = p.id_pengguna";
                         
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                int indexBaris = 1;
                while (rs.next()) {
                    Row rowData = sheet.createRow(indexBaris++);
                    int idTrans = rs.getInt("id_transaksi");
                    int bitwiseXor = idTrans ^ SEED_SALT;
                    int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
                    
                    rowData.createCell(0).setCellValue("INV-" + maskedCode);
                    rowData.createCell(1).setCellValue(rs.getString("username"));
                    rowData.createCell(2).setCellValue(rs.getInt("jumlah_tiket"));
                    rowData.createCell(3).setCellValue(rs.getLong("total_harga"));
                    rowData.createCell(4).setCellValue(rs.getString("status_transaksi"));
                }
            }
            
            workbook.write(fos);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Export Success");
            alert.setContentText("Report excel successfully generated to:\n" + fileSimpan.getAbsolutePath());
            alert.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFilter(ActionEvent event) {
        halamanAktif = 0;
        muatArsipTransaksi();
    }

    @FXML
    private void handlePreviousPage(ActionEvent event) {
        if (halamanAktif > 0) {
            halamanAktif--;
            muatArsipTransaksi();
        }
    }

    @FXML
    private void handleNextPage(ActionEvent event) {
        if (vboxArchiveContainer.getChildren().size() == 15) {
            halamanAktif++;
            muatArsipTransaksi();
        }
    }
}