package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import org.apache.poi.util.Units;
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
            rTitle.setFontSize(18);
            rTitle.setFontFamily("Segoe UI");

            XWPFParagraph meta = doc.createParagraph();
            meta.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun rMeta = meta.createRun();
            rMeta.setText("Invoice Reference: " + lblInvoiceID.getText() + " | Event: " + lblEventName.getText());
            rMeta.setFontSize(11);
            rMeta.setItalic(true);
            
            XWPFParagraph space = doc.createParagraph();
            space.createRun().setText("\n");

            String sql = "SELECT unik_kode FROM tb_tiket WHERE id_transaksi = ?";
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idTransaksi);
                
                try (ResultSet rs = ps.executeQuery()) {
                    int nomorTiket = 1;
                    File imgTemplate = new File("images/ticket_template.png");

                    while (rs.next()) {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/tes/pembeli/TicketDesign.fxml"));
                        AnchorPane ticketPane = loader.load();
                        
                        ImageView imgView = (ImageView) ticketPane.lookup("#imgTemplate");
                        Label txtEvent = (Label) ticketPane.lookup("#txtEvent");
                        Label txtStatus = (Label) ticketPane.lookup("#txtStatus");
                        Label txtTicketNo = (Label) ticketPane.lookup("#txtTicketNo");
                        Label txtGateCode = (Label) ticketPane.lookup("#txtGateCode");
                        
                        if (imgTemplate.exists()) {
                            imgView.setImage(new Image(imgTemplate.toURI().toString()));
                        }
                        
                        txtEvent.setText(lblEventName.getText().toUpperCase());
                        txtStatus.setText("STATUS: VALID / ACTIVE");
                        txtTicketNo.setText("TICKET " + nomorTiket++ + " OF " + lblTotalTicket.getText());
                        txtGateCode.setText(rs.getString("unik_kode"));

                        Scene dummyScene = new Scene(ticketPane);
                        ticketPane.applyCss();
                        ticketPane.layout();
                        
                        SnapshotParameters params = new SnapshotParameters();
                        params.setFill(Color.TRANSPARENT);
                        WritableImage snapshot = ticketPane.snapshot(params, null);
                        
                        int lebar = (int) snapshot.getWidth();
                        int tinggi = (int) snapshot.getHeight();
                        java.awt.image.BufferedImage bufImg = new java.awt.image.BufferedImage(lebar, tinggi, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                        PixelReader pxReader = snapshot.getPixelReader();
                        
                        for (int y = 0; y < tinggi; y++) {
                            for (int x = 0; x < lebar; x++) {
                                bufImg.setRGB(x, y, pxReader.getArgb(x, y));
                            }
                        }
                        
                        File tempSnapshotFile = new File("images/temp_ticket.png");
                        ImageIO.write(bufImg, "png", tempSnapshotFile);
                        
                        XWPFParagraph ticketParagraph = doc.createParagraph();
                        ticketParagraph.setAlignment(ParagraphAlignment.CENTER);
                        XWPFRun runTicket = ticketParagraph.createRun();

                        try (FileInputStream fis = new FileInputStream(tempSnapshotFile)) {
                            runTicket.addPicture(
                                fis, 
                                XWPFDocument.PICTURE_TYPE_PNG, 
                                tempSnapshotFile.getName(), 
                                Units.toEMU(460), 
                                Units.toEMU(149)  
                            );
                        }
                        
                        tempSnapshotFile.delete();

                        XWPFParagraph separatorParagraph = doc.createParagraph();
                        separatorParagraph.setAlignment(ParagraphAlignment.CENTER);
                        XWPFRun runSeparator = separatorParagraph.createRun();
                        runSeparator.setText("\n---------------------------------------------------------------------------------\n");
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