module com.mycompany.tes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.base;
    requires java.desktop;

    opens com.mycompany.tes to javafx.fxml;
    exports com.mycompany.tes;
}
