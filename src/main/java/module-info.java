module com.mycompany.tes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.desktop;
    requires org.apache.poi.ooxml;

    // Folder Utama
    opens com.mycompany.tes to javafx.fxml;
    exports com.mycompany.tes;

    // Folder Admin 
    opens com.mycompany.tes.admin to javafx.fxml;
    exports com.mycompany.tes.admin;

    // Folder Pembeli
    opens com.mycompany.tes.pembeli to javafx.fxml;
    exports com.mycompany.tes.pembeli;
    
    // Folder Components (Navbar, dkk)
    opens com.mycompany.tes.components to javafx.fxml;
    exports com.mycompany.tes.components;
}
