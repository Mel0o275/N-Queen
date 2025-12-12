module com.example.nnn {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.nnn to javafx.fxml;
    exports com.example.nnn;
}