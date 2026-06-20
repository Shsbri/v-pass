package com.mycompany.tes.pembeli;

import com.mycompany.tes.KoneksiDB;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class PembeliInvoicePopUpController implements Initializable {

    @FXML private VBox invoiceContainer;
    @FXML private Label lblTransactionID;
    @FXML private Label lblEventName;
    @FXML private Label lblCategory;
    @FXML private Label lblTotalTicket;
    @FXML private Label lblTotalPrice;
    @FXML private ImageView imgQris;
    @FXML private Button btnDownload;
    @FXML private Button btnUpload;

    private int idTransaksi;
    private final DecimalFormat formatUang = new DecimalFormat("#,###");
    private PembeliMyTicketController parentController;
    
    private File fileBuktiTerpilih = null;
    private Image citraQrisAsli = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            File fileQris = new File("images/qris.png");
            if (fileQris.exists()) {
                citraQrisAsli = new Image(fileQris.toURI().toString());
                imgQris.setImage(citraQrisAsli);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

    public void setInvoiceData(int idTrans, String namaEvent, String kategori, int jmlTiket, long total, PembeliMyTicketController parent) {
        this.idTransaksi = idTrans;
        this.parentController = parent;
        
        int seedSalt = 0x5F3759DF;
        int bitwiseXor = idTrans ^ seedSalt;
        int maskedCode = (int) (Math.abs((bitwiseXor * 2654435761L) % 900000L) + 100000);
        
        lblTransactionID.setText("INV-" + maskedCode);
        
        lblEventName.setText(namaEvent);
        lblCategory.setText(kategori);
        lblTotalTicket.setText(jmlTiket + " Pcs");
        lblTotalPrice.setText("Rp " + formatUang.format(total));
    }

    @FXML
    private void handleUploadBukti(ActionEvent event) {
        if (fileBuktiTerpilih == null) {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select Payment Receipt Image");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            
            File fileOpen = fc.showOpenDialog(btnUpload.getScene().getWindow());
            if (fileOpen != null) {
                fileBuktiTerpilih = fileOpen;
                imgQris.setImage(new Image(fileBuktiTerpilih.toURI().toString()));
                
                btnDownload.setText("Cancel");
                btnUpload.setText("Confirm & Submit");
                btnUpload.setStyle("-fx-background-color: #248A3D; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        } else {
            eksekusiSimpanDatabase();
        }
    }

    private void eksekusiSimpanDatabase() {
        try {
            File folderTujuan = new File("images/bukti/");
            if (!folderTujuan.exists()) {
                folderTujuan.mkdirs();
            }
            
            String namaFileBaru = "BUKTI-" + idTransaksi + "-" + System.currentTimeMillis() + ".png";
            File fileTujuan = new File(folderTujuan, namaFileBaru);
            
            Files.copy(fileBuktiTerpilih.toPath(), fileTujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            String sql = "UPDATE tb_transaksi SET status_transaksi = 'menunggu verifikasi', bukti_pembayaran = ? WHERE id_transaksi = ?";
            try (Connection conn = KoneksiDB.getKoneksi();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setString(1, namaFileBaru);
                ps.setInt(2, idTransaksi);
                
                int hasil = ps.executeUpdate();
                if (hasil > 0) {
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Receipt Uploaded Successfully");
                    alert.setContentText("Your payment status updated. Verification is ongoing by administration.");
                    alert.showAndWait();
                    
                    if (parentController != null) {
                        parentController.muatDataTiketPembeli();
                    }
                    
                    Stage stage = (Stage) btnUpload.getScene().getWindow();
                    stage.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDownloadInvoice(ActionEvent event) {
        if (fileBuktiTerpilih != null) {
            fileBuktiTerpilih = null;
            imgQris.setImage(citraQrisAsli);
            
            btnDownload.setText("Download");
            btnUpload.setText("Upload Payment Proof");
            btnUpload.setStyle("-fx-background-color: #111111; -fx-background-radius: 12; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Invoice Image");
        fc.setInitialFileName("Invoice_" + idTransaksi + ".png");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"));
        
        File fileSimpan = fc.showSaveDialog(btnDownload.getScene().getWindow());
        if (fileSimpan != null) {
            try {
                SnapshotParameters sp = new SnapshotParameters();
                WritableImage snapshot = invoiceContainer.snapshot(sp, null);
                
                int width = (int) snapshot.getWidth();
                int height = (int) snapshot.getHeight();
                
                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                PixelReader pr = snapshot.getPixelReader();
                
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        bufferedImage.setRGB(x, y, pr.getArgb(x, y));
                    }
                }
                
                ImageIO.write(bufferedImage, "png", fileSimpan);
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Invoice image saved successfully to: " + fileSimpan.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Export Failed");
                alert.setContentText("An failure occurred while downloading the file image.");
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }
} 