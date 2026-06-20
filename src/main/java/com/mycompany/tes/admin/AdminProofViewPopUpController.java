package com.mycompany.tes.admin;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AdminProofViewPopUpController implements Initializable {

    @FXML private ImageView imgReceipt;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    

    public void setReceiptImage(String pathGambarBukti) {
        try {
            File fileImg = new File("images/bukti/" + pathGambarBukti);
            if (fileImg.exists()) {
                imgReceipt.setImage(new Image(fileImg.toURI().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}