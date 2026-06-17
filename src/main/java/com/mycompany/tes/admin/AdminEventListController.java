/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.tes.admin;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Ahmad
 */
public class AdminEventListController implements Initializable {

    @FXML
    private TableView<?> tableEvents;
    @FXML
    private TableColumn<?, ?> colId;
    @FXML
    private TableColumn<?, ?> colNamaEvent;
    @FXML
    private TableColumn<?, ?> colKategori;
    @FXML
    private TableColumn<?, ?> colHarga;
    @FXML
    private TableColumn<?, ?> colStok;
    @FXML
    private TableColumn<?, ?> colAksi;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
