package com.mycompany.tes.pembeli;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class PembeliHistoryDetailPopUpController implements Initializable {

    @FXML private Label lblInvoiceID;
    @FXML private Label lblEventName;
    @FXML private Label lblCategory;
    @FXML private Label lblTotalTicket;
    @FXML private Label lblStatus;
    @FXML private Label lblTotalPrice;
    @FXML private Button btnDownload;

    private int idTransaksi;
    private final DecimalFormat formatUang = new DecimalFormat("#,###");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    

    public void setTransactionData(int idTrans, String invoice, String nama, String kat, int tiket, long total, String status) {
        this.idTransaksi = idTrans;
        lblInvoiceID.setText(invoice);
        lblEventName.setText(nama);
        lblCategory.setText(kat);
        lblTotalTicket.setText(tiket + " Pcs");
        lblTotalPrice.setText("Rp " + formatUang.format(total));
        
        if ("berhasil".equalsIgnoreCase(status)) {
            lblStatus.setText("SUCCESS");
            lblStatus.setStyle("-fx-text-fill: #00B074; -fx-font-weight: bold;");
            btnDownload.setVisible(true);
        } else if ("menunggu verifikasi".equalsIgnoreCase(status)) {
            lblStatus.setText("PENDING");
            lblStatus.setStyle("-fx-text-fill: #666666; -fx-font-weight: bold;");
            btnDownload.setVisible(false);
        } else if ("menunggu pembayaran".equalsIgnoreCase(status)) {
            lblStatus.setText("UNPAID");
            lblStatus.setStyle("-fx-text-fill: #FF5E00; -fx-font-weight: bold;");
            btnDownload.setVisible(false);
        } else {
            lblStatus.setText("FAILED");
            lblStatus.setStyle("-fx-text-fill: #FF3B30; -fx-font-weight: bold;");
            btnDownload.setVisible(false);
        }
    }

    @FXML
    private void handleDownloadPDF(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save E-Tickets");
        fc.setInitialFileName("Tickets_" + lblInvoiceID.getText() + ".docx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Word (*.docx)", "*.docx"));

        File fileSimpan = fc.showSaveDialog(btnDownload.getScene().getWindow());
        if (fileSimpan == null) return;

        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(fileSimpan)) {

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun rTitle = title.createRun();
            rTitle.setText("V-PASS OFFICIAL E-TICKET");
            rTitle.setBold(true);
            rTitle.setFontSize(16);
            rTitle.setFontFamily("Segoe UI");

            XWPFParagraph meta = doc.createParagraph();
            meta.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun rMeta = meta.createRun();
            rMeta.setText("Invoice Reference: " + lblInvoiceID.getText() + " | Event: " + lblEventName.getText());
            rMeta.setFontSize(10);
            rMeta.setItalic(true);

            String sql = "SELECT unik_kode FROM tb_tiket WHERE id_transaksi = ?";
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idTransaksi);
                try (ResultSet rs = ps.executeQuery()) {
                    int nomorTiket = 1;
                    while (rs.next()) {
                        XWPFParagraph borderTop = doc.createParagraph();
                        borderTop.createRun().setText("---------------------------------------------------------------------------------");
                        
                        XWPFParagraph tktRow = doc.createParagraph();
                        XWPFRun rTkt = tktRow.createRun();
                        rTkt.setText("TICKET NO " + nomorTiket++ + " / " + lblTotalTicket.getText() + "\n");
                        rTkt.setText("GATE PASS CODE : " + rs.getString("unik_kode") + "\n");
                        rTkt.setText("Holder Status : VALID / ACTIVE\n");
                        rTkt.setFontSize(12);
                        rTkt.setBold(true);
                        
                        XWPFParagraph borderBot = doc.createParagraph();
                        borderBot.createRun().setText("---------------------------------------------------------------------------------");
                    }
                }
            }

            doc.write(fos);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Your document E-Tickets successfully created!");
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}